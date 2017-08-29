package de.prozesskraft.pkraft;

import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
//import org.w3c.dom.*;
//import org.xml.sax.*;
//import javax.xml.parsers.*;
//import java.io.*;
//import java.io.NotSerializableException;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
//import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
//import org.apache.xerces.impl.xpath.regex.ParseException;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class Manager
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	static Ini ini;
	static boolean weiterlaufen = true;

	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, CloneNotSupportedException
	{

//		try
//		{
//			if (args.length != 1)
//			{
//				System.out.println("Please specify Inputfile and Outputfile (prozessinstanz.lri)");
//			}
//			
//		}
//		catch (ArrayIndexOutOfBoundsException e)
//		{
//			System.out.println("***ArrayIndexOutOfBoundsException: Please specify procesdefinition.lrd and processinstance.lri\n" + e.toString());
//		}
		
		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Manager.class) + "/" + "../etc/pkraft-manager.ini");

		if (inifile.exists())
		{
			try
			{
				ini = new Ini(inifile);
			}
			catch (InvalidFileFormatException e1)
			{
			// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
			// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else
		{
			System.err.println("ini file does not exist: "+inifile.getAbsolutePath());
			System.exit(1);
		}

		/*----------------------------
		  create boolean options
		----------------------------*/
		Option help = new Option("help", "print this message");
		Option v = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option instance = OptionBuilder.withArgName("instance")
				.hasArg()
				.withDescription("[mandatory] process instance file")
//				.isRequired()
				.create("instance");
				
		Option stop = OptionBuilder.withArgName("stop")
//				.hasArg()
				.withDescription("[optional] stops a running manager for given instance")
//				.isRequired()
				.create("stop");
				
		Option kill = OptionBuilder.withArgName("kill")
//				.hasArg()
				.withDescription("[optional] kills all applications that have been started by steps")
//				.isRequired()
				.create("kill");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( instance );
		options.addOption( stop );
		options.addOption( kill );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			line = parser.parse( options,  args );
		}
//		catch ( ParseException exp )
		catch ( Exception exp )
		{
			// oops, something went wrong
			System.err.println( "Parsing failed. Reason: "+ exp.getMessage());
			exiter();
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( line.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("manager", options);
			System.exit(0);
		}
		
		else if ( line.hasOption("v"))
		{
			System.out.println("author:  info@prozesskraft.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		else if (!( line.hasOption("instance") ))
		{
			exiter();
		}
		
		/*----------------------------
		  die lizenz ueberpruefen und ggf abbrechen
		----------------------------*/

		// check for valid license
		ArrayList<String> allPortAtHost = new ArrayList<String>();
		allPortAtHost.add(ini.get("license-server", "license-server-1"));
		allPortAtHost.add(ini.get("license-server", "license-server-2"));
		allPortAtHost.add(ini.get("license-server", "license-server-3"));
		
		MyLicense lic = new MyLicense(allPortAtHost, "1", "user-edition", "0.1");
		
		// lizenz-logging ausgeben
		for(String actLine : (ArrayList<String>) lic.getLog())
		{
			System.err.println(actLine);
		}

		// abbruch, wenn lizenz nicht valide
		if (!lic.isValid())
		{
			System.exit(1);
		}
		
		/*----------------------------
		  business logic
		----------------------------*/
		
		Process actualProcess = null;
		try
		{

			Process p1 = new Process();
			
			// die dauer des loops festlegen. Dies soll kein standardwert sein, da sonst bei vielen subprozessen die Prozessorlast stark oszilliert
			// zwischen 12 und 17 sekunden
			Random rand = new Random(System.currentTimeMillis());
			int loop_period_seconds = rand.nextInt((17 - 12) + 1) + 12;
			System.err.println("loop period is randomly set to: "+loop_period_seconds);
			
			double managerid = p1.genManagerid();
	
			java.io.File fileBinary = new java.io.File(line.getOptionValue("instance"));
			String pathBinary = "";
			
			if (fileBinary.exists())
			{
				pathBinary = fileBinary.getAbsolutePath();
				System.err.println("file does exist: "+pathBinary);
			}
			else
			{
				System.err.println("file does not exist: "+fileBinary.getAbsolutePath());
				exiter();
			}

			if ( line.hasOption("stop") )
			{
				p1.setInfilebinary(pathBinary);
				Process p2 = p1.readBinary();
				p2.log("debug", "setting new manager-Id (0) to signal actual manager ("+p2.getManagerid()+") that he is no longer in charge ");
				p2.setManagerid(0);
				p2.run = false;
				p2.setOutfilebinary(pathBinary);

				p2.writeBinary();

				if(line.hasOption("kill"))
				{
					p2.kill();
				}
				
				boolean pradar =  (!(p2.isWrapper()));

				// pradar checkout
				if(pradar)
				{
					pradarAttend(p2.getRootdir()+"/process.pmb");
//					pradarCheckout(p2.getId(), p2.getName(), "0");
				}

				System.exit(0);
			}
			
			// prozessinstanz einlesen
			p1.setInfilebinary(pathBinary);
			
			Process p2;
			p2 = p1.readBinary();
			
			// beim aufruf des programms wird erstmal die instanz occupiert
			p2.setManagerid(managerid);
			
			System.err.println("debug: manager "+managerid+": occupying instance.");
			p2.log("info", "manager "+managerid+": occupying instance.");
			p2.log("debug", "manager "+managerid+": setting new manager-id to signal other running managers that they are not longer needed.");
	
			p2.log("debug", "manager "+managerid+": setting binary file for input to: "+pathBinary);
	//		System.out.println("setting binary file for input to: "+line.getOptionValue("instance"));
	
			p2.log("debug", "manager "+managerid+": reading binary file: "+pathBinary);
	
			p2.setInfilebinary(pathBinary);
			p2.setOutfilebinary(pathBinary);
			p2.log("debug", "manager "+managerid+": setting binary file for output: "+pathBinary);
	
			// instanz auf platte schreiben (um anderen managern zu signalisieren, dass sie nicht mehr gebraucht werden
	//		System.out.println("setting manager-id to: "+managerid);
	
			p2.log("debug", "manager "+managerid+": writing process to binary file to occupy instance.");
	
			// wenn es kein wrapper-prozess ist, dann soll die komunikation mit pradar vom manager uebernommen werden
			boolean pradar =  (!(p2.isWrapper()));

			System.err.println("debug: setting instance to run");
			p2.run = true;
	
			// die letzten festgestellten werte fuer die abarbeitung
//			int lastStepcount = 0;
//			int lastStepcountFinishedOrCanceled = 0;

			// pradar checkin
			if(pradar && p2.run && p2.touchInMillis == 0)
			{
				pradarAttend(p2.getRootdir()+"/process.pmb");
//				p2.log("debug", "pradar-checkin id="+p2.getId()+", process="+p2.getName()+", processversion="+p2.getVersion()+", id2="+p2.getId2()+", parentid="+p2.getParentid()+", resource="+p2.getRootdir()+"/process.pmb");
//				pradarCheckin(p2.getId(), p2.getName(), p2.getVersion(), p2.getId2(), p2.getParentid(), getPid(), p2.getRootdir()+"/process.pmb");
			}
				
			System.err.println("debug: writing binary");
			
			p2.writeBinary();

			while(weiterlaufen)
			{
				// prozess instanz frisch einlesen
				System.err.println("debug: rereading instance");
				Process p3 = p2.readBinary();
				System.err.println("debug: rereading instance done");
				
				// die groesse des binary-files festhalten
				long fileSizeInKB = fileBinary.length() / 1024;
				
				System.err.println("debug: file size is now " + String.valueOf(fileSizeInKB) + " kB");
				p3.getTimeSerieBinarySize().addValue(String.valueOf(fileSizeInKB));
				System.err.println("debug: after the timeseries");
				//p3.fileBinary.length() / 1024;
				
				weiterlaufen = p3.run;
				
				p3.log("debug", "manager "+managerid+": actual infilexml is: "+p3.getInfilexml());
				p3.log("debug", "manager "+managerid+": reading binary file: "+p2.getInfilebinary());
				// die manager-id mit eigener vergleichen. wenn nicht gleich, dann beenden.
				if (!(p3.getManagerid() == managerid))
				{
	//					p3.log("warn", "manager "+managerid+": it appears that another manager (id: "+p3.getManagerid()+") took over. killing myself. bye.");
					System.err.println("it appears another instance of manager (id: "+p3.getManagerid()+") took over. so i'm (id: "+managerid+") not longer needed. killing myself. byebye.");
					System.exit(0);
				}
	
				// prozess laufen lassen
				p3.doIt(ini.get("apps", "pkraft-syscall"), ini.get("apps", "pkraft-manager"), ini.get("process", "domain-installation-directory"));

				// pradar aktualisieren
				if(pradar)
				{
	
//					lastStepcount =  p3.getStep().size();
//					lastStepcountFinishedOrCanceled = p3.getStepFinishedOrCanceled().size();

					// pradar aktualisieren
					pradarAttend(p3.getRootdir()+"/process.pmb");
					
//					p3.log("info", "manager "+managerid+": pradar progress  "+lastStepcountFinishedOrCanceled+"/"+lastStepcount);
//					pradarProgress(p3.getId(), p3.getName(), getPid(), lastStepcountFinishedOrCanceled, lastStepcount);

					// evtl. stoeren sich zwei kurz aufeinander folgende aufrufe von pradar...
//					Thread.sleep(500);
					
					// finished
					if(p3.getStatus().equals("finished"))
					{
						System.err.println("debug: status is finished");

						// wenn der prozess den status 'finished' hat, soll dieses programm beendet werden
						p3.run = false;
						p3.log("info", "manager "+managerid+": process instance is finished. goodbye from manager id "+p3.getManagerid());
						p3.setTimeOfProcessFinishedOrError(System.currentTimeMillis());
						
						// pradar aktualisieren
//						pradarAttend(p3.getRootdir()+"/process.pmb");

//						// pradar checkout
//						p3.log("info", "manager "+managerid+": pradar checkout id="+p3.getId()+", process="+p3.getName()+", exitcode=0");
//						pradarCheckout(p3.getId(), p3.getName(), "0");
						
//						// die timeserie rausschreiben
//						p2.getTimeSerieLoadAverage().writeFile(p2.getRootdir() + "/.serieLoadAverage.txt");
					}
					
					// error
					else if(p3.getStatus().equals("error"))
					{
						System.err.println("debug: status is error");
						p3.run = false;
						p3.log("info", "error in process detected. setting run = false");
						p3.log("info", "stopping manager "+p2.getManagerid());
						p3.setTimeOfProcessFinishedOrError(System.currentTimeMillis());

						// errorcode string erzeugen
						String exitCode = "error-in-steps:";
						for(Step actStep : p3.getStepError())
						{
							exitCode = exitCode + "," + actStep.getName();
						}

						// pradar aktualisieren
//						pradarAttend(p2.getRootdir()+"/process.pmb");

//						// pradar checkout
//						p3.log("debug", "pradar-checkout id="+p3.getId()+", process="+p3.getName()+", exitcode="+exitCode);
//						pradarCheckout(p3.getId(), p3.getName(), exitCode);

//						// die timeserie rausschreiben
//						p2.getTimeSerieLoadAverage().writeFile(p2.getRootdir() + "/.serieLoadAverage.txt");
					}

//					// error
//					else if(p3.getStatus().equals("paused"))
//					{
//						p3.run = false;
//						p3.log("info", "process has been paused. setting run = false");
//						p2.log("info", "stopping manager "+p2.getManagerid());
//
//						// errorcode string erzeugen
//						String exitCode = "error in step(s):";
//						for(Step actStep : p3.getStepError())
//						{
//							exitCode = exitCode + " " + actStep.getName();
//						}
//
//						// pradar checkout
//						pradarCheckout(p3.getId(), p3.getName(), exitCode);
//					}
				}
				
				updateFile(p3);
	
				if(p3.run == false)
				{
					System.err.println("debug: exiting");
					System.exit(0);
				}

				try
				{
					// der thread soll so lange schlafen, wie die periode lang ist. die schlafdauer wird mit der anzahl multipliziert, wie oft das loadAverage zu hoch war (max 5)
					int faktorForPeriod = Math.min(10, p3.counterLoadAverageTooHigh + 1);

					int secondsToSleep = loop_period_seconds * faktorForPeriod;
					System.err.println("debug: sleeping for " + secondsToSleep + " seconds");
					
					int millisecondsToSleep = secondsToSleep*1000;
					System.err.println("debug: sleeping for " + millisecondsToSleep + " milliseconds");
					
					Thread.sleep(millisecondsToSleep);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();

					// ausgabe in das debugLogFile
					exiterException(actualProcess.getOutfilebinary(), e.getMessage());
				}
			}
		}
		catch(Exception e)
		{
			if(actualProcess != null)
			{
				actualProcess.log("fatal", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));
				updateFile(actualProcess);
				e.printStackTrace();
	
				// ausgabe in das debugLogFile
				exiterException(actualProcess.getOutfilebinary(), e.getMessage());

			}

			System.exit(10);
		}
	}

	private static void pradarAttend(String pathToInstance)
	{
		String[] argsForProgress = {ini.get("apps", "pradar-attend"), "-instance "+pathToInstance};
		try
		{
			java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			// ausgabe in das debugLogFile
			exiterException(pathToInstance, e.getMessage());
		}
	}
	
//	private static void pradarProgress(String instanceId, String processName, String pid, int lastStepcountFinishedOrCanceled, int lastStepcount)
//	{
//		String[] argsForProgress = {ini.get("apps", "pradar-progress"), "-id="+instanceId, "-process="+processName, "-pid="+pid, "-completed="+lastStepcountFinishedOrCanceled, "-stepcount="+lastStepcount};
//		try
//		{
//			java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//	
//	private static void pradarCheckin(String instanceId, String processName, String processVersion, String id2, String parentId, String pid, String resource)
//	{
//		String[] argsForProgress = {ini.get("apps", "pradar-checkin"), "-id="+instanceId, "-process="+processName, "-processversion="+processVersion, "-id2="+id2, "-parentid="+parentId, "-pid="+pid, "-resource="+resource};
//		try
//		{
//			java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
//	
//	private static void pradarCheckout(String instanceId, String processName, String exitCode)
//	{
//		String[] argsForProgress = {ini.get("apps", "pradar-checkout"), "-id="+instanceId, "-process="+processName, "-exitcode=\""+exitCode+"\""};
//		try
//		{
//			java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * ermittelt die pid dieses manager-laufs
	 * @return
	 */
	private static String getPid()
	{
		String fullPid = ManagementFactory.getRuntimeMXBean().getName();
		
		String[] splitPid = fullPid.split("@");
		
		return splitPid[0];
	}
	
	private static void updateFile(Process process)
	{
		// alle log-eintraege in die entsprechenden files auslagern (und aus den elementen entfernen)
		System.err.println("relocating all logs to .debug files in the step directories");
		process.logRelocate();
		System.err.println("relocation done");

		
		process.setDatetonow();
		process.touch();
		process.detStatus();
		process.writeBinary();

		// die timeserie(n) rausschreiben
		try
		{
			process.getTimeSerieLoadAverage().writeFile(process.getRootdir() + "/.serieLoadAverage.txt");
			process.getTimeSerieBinarySize().writeFile(process.getRootdir() + "/.serieBinarySizeInKB.txt");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// ausgabe in das debugLogFile
			exiterException(process.getOutfilebinary(), e.getMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// ausgabe in das debugLogFile
			exiterException(process.getOutfilebinary(), e.getMessage());
		}
	}

	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}

	private static void exiterException(String pathToInstance, String eMessage)
	{
		try
		{
			java.io.FileWriter writer = new FileWriter(pathToInstance + ".pkraft-manager.stacktrace", true);
			writer.write(new Timestamp(System.currentTimeMillis()).toString());
			writer.write(eMessage);
			writer.write("--------------");
			writer.close();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(1);
	}



}
