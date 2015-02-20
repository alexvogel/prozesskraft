package de.caegroup.process;

import de.caegroup.commons.MyLicense;
import de.caegroup.commons.WhereAmI;
import de.caegroup.process.Process;
import de.caegroup.process.Step;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
//import de.caegroup.view.Stepconnector;
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
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Manager.class) + "/" + "../etc/process-manager.ini");

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
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( instance );
		options.addOption( stop );
		
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
			
			long loop_period_seconds = 10;
			
			double managerid = p1.genManagerid();
	
			java.io.File fileBinary = new java.io.File(line.getOptionValue("instance"));
			String pathBinary = "";
			
			if (fileBinary.exists())
			{
				pathBinary = fileBinary.getAbsolutePath();
			}
			else
			{
				System.out.println("file does not exist: "+fileBinary.getAbsolutePath());
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
				System.exit(0);
			}
			
			// prozessinstanz einlesen
			p1.setInfilebinary(pathBinary);
			
			Process p2;
			p2 = p1.readBinary();
			
			// beim aufruf des programms wird erstmal die instanz occupiert
			p2.setManagerid(managerid);
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
	
			p2.run = true;
	
			// wenn es kein wrapper-prozess ist, dann soll die komunikation mit pradar vom manager uebernommen werden
			boolean pradar =  (!(p2.isWrapper()));
	
			// die letzten festgestellten werte fuer die abarbeitung
			int lastStepcount = 0;
			int lastStepcountFinished = 0;
	
			// pradar checkin
			if(pradar && p2.run && p2.touchInMillis == 0)
			{
				String[] argsForCheckin = {ini.get("apps", "pradar-checkin"), "-id="+p2.getId(), "-process="+p2.getName(), "-resource="+pathBinary};
				p2.log("info", "call: " + StringUtils.join(argsForCheckin, " "));
				try
				{
					java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForCheckin, " "));
				}
				catch (IOException e)
				{
					p2.log("warn", e.getMessage());
				}
			}
				
			p2.writeBinary();
			
			while(weiterlaufen)
			{
				// prozess instanz frisch einlesen
				Process p3 = p2.readBinary();
				actualProcess = p3;
				
				weiterlaufen = p3.run;
				
				p3.log("debug", "manager "+managerid+": actual infilexml is: "+p3.getInfilexml());
				p3.log("debug", "manager "+managerid+": reading binary file: "+p2.getInfilebinary());
				// die manager-id mit eigener vergleichen. wenn nicht gleich, dann beenden.
				if (!(p3.getManagerid() == managerid))
				{
	//					p3.log("warn", "manager "+managerid+": it appears that another manager (id: "+p3.getManagerid()+") took over. killing myself. bye.");
					System.out.println("it appears another instance of manager (id: "+p3.getManagerid()+") took over. so i'm (id: "+managerid+") not longer needed. killing myself. byebye.");
					System.exit(0);
				}
	
				// bevor gestartet wird, soll der pradar-eintrag aktualisiert werden
				if(pradar)
				{
	
					// wenn prozess den status finished oder error hat, soll pradar checkout werden
					if(p3.getStatus().equals("finished"))
					{
						// progress nur aktualisieren, falls sich der fortschritt veraendert hat
						if((lastStepcount != p3.getStepTogo().size()+p3.getStepFinished().size()) || (lastStepcountFinished != p3.getStepFinished().size()))
						{
							lastStepcount = p3.getStepTogo().size()+p3.getStepFinished().size();
							lastStepcountFinished = p3.getStepFinished().size();
							String[] argsForProgress = {ini.get("apps", "pradar-progress"), "-id="+p3.getId(), "-process="+p3.getName(), "-completed="+lastStepcountFinished, "-stepcount="+lastStepcount};
							p3.log("info", "call: " + StringUtils.join(argsForProgress, " "));
							try
							{
								java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
							}
							catch (IOException e)
							{
								p3.log("warn", "IOException when trying to pradar progress");
							}
						}
						// checkout
						String[] argsForCheckout = {ini.get("apps", "pradar-checkout"), "-id="+p3.getId(), "-process="+p3.getName(), "-exitcode=0"};
						p3.log("info", "call: " + StringUtils.join(argsForCheckout, " "));
						try
						{
							java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForCheckout, " "));
						}
						catch (IOException e)
						{
							p3.log("warn", "IOException when trying to pradar checkout");
						}
					}
					
					else if(p3.getStatus().equals("error"))
					{
						// progress nur aktualisieren, falls sich der fortschritt veraendert hat
						if((lastStepcount != p3.getStepTogo().size()+p3.getStepFinished().size()) || (lastStepcountFinished != p3.getStepFinished().size()))
						{
							lastStepcount = p3.getStepTogo().size()+p3.getStepFinished().size();
							lastStepcountFinished = p3.getStepFinished().size();
							String[] argsForProgress = {ini.get("apps", "pradar-progress"), "-id="+p3.getId(), "-process="+p3.getName(), "-completed="+lastStepcountFinished, "-stepcount="+lastStepcount};
							p3.log("info", "call: " + StringUtils.join(argsForProgress, " "));
							try
							{
								java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
							}
							catch (IOException e)
							{
								p3.log("warn", "IOException when trying to pradar progress");
							}
						}
						
						// errorcode string erzeugen
						String exitCode = "error in step(s):";
						for(Step actStep : p3.getStepError())
						{
							exitCode = exitCode + " " + actStep.getName();
						}
						String[] argsForCheckout = {ini.get("apps", "pradar-checkout"), "-id="+p3.getId(), "-process="+p3.getName(), "-exitcode=\""+exitCode+"\""};
						p3.log("info", "call: " + StringUtils.join(argsForCheckout, " "));
						try
						{
							java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForCheckout, " "));
						}
						catch (IOException e)
						{
							p3.log("warn", "IOException when trying to pradar checkout");
						}
					}
					
					// mit letztem progress vergleichen, falls sich etwas veraendert hat , soll ein progress abgesetzt werden
					else if(p3.getStatus().equals("working"))
					{
						// progress nur aktualisieren, falls sich der fortschritt veraendert hat
						if((lastStepcount != p3.getStepTogo().size()+p3.getStepFinished().size()) || (lastStepcountFinished != p3.getStepFinished().size()))
						{
							lastStepcount = p3.getStepTogo().size()+p3.getStepFinished().size();
							lastStepcountFinished = p3.getStepFinished().size();
							String[] argsForProgress = {ini.get("apps", "pradar-progress"), "-id="+p3.getId(), "-process="+p3.getName(), "-completed="+lastStepcountFinished, "-stepcount="+lastStepcount};
							p3.log("info", "call: " + StringUtils.join(argsForProgress, " "));
							try
							{
								java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(argsForProgress, " "));
							}
							catch (IOException e)
							{
								p3.log("warn", "IOException when trying to pradar progress");
							}
						}
					}
				}
				// prozess laufen lassen
				p3.doIt(ini.get("apps", "process-syscall"), ini.get("apps", "process-startinstance"), ini.get("process", "domain-installation-directory"));
	
				// austeigen wenn status==error
				if(p3.getStatus().equals("error"))
				{
					p3.run = false;
					p3.log("info", "error in process detected. setting run = false");
					p2.log("info", "stopping manager "+p2.getManagerid());
				}
	
				if(p3.getStatus().equals("finished"))
				{
					// wenn der prozess den status 'finished' hat, soll dieses programm beendet werden
					p3.log("info", "manager "+managerid+": process instance is finished. goodbye from manager id "+p3.getManagerid());
					p3.run = false;
				}
	
				updateFile(p3);
	
				if(p3.run == false)
				{
					System.exit(0);
				}

				try
				{
					Thread.sleep(loop_period_seconds*1000);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			if(actualProcess != null)
			{
				actualProcess.log("fatal", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));
				updateFile(actualProcess);
			}
			e.printStackTrace();
			System.exit(10);
		}
	}

	private static void updateFile(Process process)
	{
		process.setDatetonow();
		process.touch();
		process.detStatus(); 
		process.writeBinary();
	}

	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}



}
