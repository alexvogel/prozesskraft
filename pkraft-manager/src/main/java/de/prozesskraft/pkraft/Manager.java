package de.prozesskraft.pkraft;

import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.google.caliper.memory.ObjectGraphMeasurer;

public class Manager
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	static Ini ini;
	static boolean weiterlaufen = true;
	static boolean pradar = true;
	
	static Double managerid = null;
	volatile static Long lastRun = null;
	volatile static boolean exit = false;
	volatile static int loopMinutes = 1;
	static int fuzzyness = ThreadLocalRandom.current().nextInt(-2000, 2000+1);
	static float factorSleepBecauseOfLoadAverage = 1.0f;
	
	static java.io.File fileBinary = null;
	
	static Map<WatchKey,Path> keys = null;

	static Thread watcherThread = null;
	
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
			exit = true;
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
			exit = true;
			System.exit(0);
		}
		
		else if ( line.hasOption("v"))
		{
			System.out.println("author:  info@prozesskraft.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			exit = true;
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
			exit = true;
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
//			Random rand = new Random(System.currentTimeMillis());
//			int loop_period_seconds = rand.nextInt((17 - 12) + 1) + 12;
//			System.err.println("loop period is randomly set to: "+loop_period_seconds);
			
			fileBinary = new java.io.File(line.getOptionValue("instance"));
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

			if ( line.hasOption("stop") || line.hasOption("kill"))
			{
				p1.setInfilebinary(pathBinary);
				Process p2 = p1.readBinary();
				p2.log("debug", "setting new manager-Id (0) to signal actual manager ("+p2.getManagerid()+") that he is no longer in charge ");
				System.err.println("info: stopping instance");
				System.err.println("debug: setting new manager-Id (0) to signal actual manager ("+p2.getManagerid()+") that he is no longer in charge ");
				p2.setManagerid(0);
				p2.run = false;
				p2.setOutfilebinary(pathBinary);

				p2.writeBinary();

				if(line.hasOption("kill"))
				{
					System.err.println("info: killing all steps of instance");
					String returnStringOfKills = p2.kill();
					System.err.println("info: killing returns: " + returnStringOfKills);
				}
				
				boolean pradar =  (!(p2.isWrapper()));

				// pradar checkout
				if(pradar)
				{
					pradarAttend(p2.getRootdir()+"/process.pmb");
//					pradarCheckout(p2.getId(), p2.getName(), "0");
				}

				exit = true;
				System.exit(0);
			}
			
			startZyklischerThread(0);
			
			// prozessinstanz einlesen
			p1.setInfilebinary(pathBinary);
			
			managerid = p1.genManagerid();

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
	
			// pradar checkin
			if(pradar && p2.run && p2.touchInMillis == 0)
			{
				pradarAttend(p2.getRootdir()+"/process.pmb");
//				p2.log("debug", "pradar-checkin id="+p2.getId()+", process="+p2.getName()+", processversion="+p2.getVersion()+", id2="+p2.getId2()+", parentid="+p2.getParentid()+", resource="+p2.getRootdir()+"/process.pmb");
//				pradarCheckin(p2.getId(), p2.getName(), p2.getVersion(), p2.getId2(), p2.getParentid(), getPid(), p2.getRootdir()+"/process.pmb");
			}
				
			System.err.println("debug: writing binary");
			
			p2.writeBinary();

			// process weiter schubsen
			pushProcessAsFarAsPossible(pathBinary, false);

//			try
//			{
//				// der thread soll so lange schlafen, wie die periode lang ist. die schlafdauer wird mit der anzahl multipliziert, wie oft das loadAverage zu hoch war (max 5)
//				int faktorForPeriod = Math.min(10, p2.counterLoadAverageTooHigh + 1);
//
//				int secondsToSleep = loop_period_seconds * faktorForPeriod;
//				System.err.println("debug: sleeping for " + secondsToSleep + " seconds");
//				
//				int millisecondsToSleep = secondsToSleep*1000;
//				System.err.println("debug: sleeping for " + millisecondsToSleep + " milliseconds");
//				
//				Thread.sleep(millisecondsToSleep);
//			}
//			catch (InterruptedException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//
//				// ausgabe in das debugLogFile
//				exiterException(actualProcess.getOutfilebinary(), e);
//			}
		}
		catch(Exception e)
		{
			if(actualProcess != null)
			{
				actualProcess.log("fatal", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));
				updateFile(actualProcess);
				e.printStackTrace();
	
				// ausgabe in das debugLogFile
				exiterException(actualProcess.getOutfilebinary(), e);

			}
			exit = true;

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
			exiterException(pathToInstance, e);
		}
	}
	
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

		// die prozess instanz schreiben
//		process.setDatetonow();
		process.touch();
//		process.detStatus();
		process.writeBinary();

		// die timeserie(n) rausschreiben
		try
		{
			// statistics verzeichnis erstellen, falls noch nicht existent
			java.io.File statisticsDirectory = new java.io.File(process.getStatisticDir());
			if(!statisticsDirectory.exists())
			{
				statisticsDirectory.mkdirs();
			}
			
			// die statisticsfiles rausschreiben
			process.getTimeSerieLoadAverage().writeFile(process.getStatisticDir() + "/serieLoadAverage.txt");
			process.getTimeSerieBinarySize().writeFile(process.getStatisticDir() + "/serieBinarySizeInKB.txt");
			process.getTimeSerieStepSize().writeFile(process.getStatisticDir() + "/serieStepSize.txt");
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			// ausgabe in das debugLogFile
			exiterException(process.getOutfilebinary(), e);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// ausgabe in das debugLogFile
			exiterException(process.getOutfilebinary(), e);
		}
		
		// debugging
		// die groesse aller steps rausschreiben 
	}

	/**
	 * es soll so lange der process weitergetrieben werden, bis es keine veraenderung in den stati mehr gibt
	 */
	private static void pushProcessAsFarAsPossible(String pathBinary, boolean onlyPush)
	{
		// zeitmarker setzen fuer timerThread
		lastRun = System.currentTimeMillis();
		
		// prozess instanz frisch einlesen
		System.err.println("debug: rereading instance");
		Process p1 = new Process();
		p1.setInfilebinary(pathBinary);
		p1.setOutfilebinary(pathBinary);
		Process process = p1.readBinary();
		System.err.println("debug: rereading instance done");
		process.log("debug", "rereading instance done");
		
		// falls managerIds nicht zusammenpassen, soll beendet werden
		// und dem alternativen thread mit flag signalisieren, dass es vorbei ist
		if(!managerid.equals(process.getManagerid()))
		{
			System.err.println("i'm manager "+managerid+" - another instance of pkraft-manager took over " + process.getManagerid() + ". killing myself.");
			exit = true;
			System.exit(0);
		}
		
		// beenden, falls process.run == false ist
		if(!process.run)
		{
			System.err.println("info: process manager exits, because process.run is false");
			exit = true;
			System.exit(0);
		}
			
		// 1) timeSerie loadAverage
		double actLoadAverage = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
		process.getTimeSerieLoadAverage().addValue(String.valueOf(actLoadAverage));
		
		if( (process.getStepStartLoadAverageBelow() != null) && (actLoadAverage > process.getStepStartLoadAverageBelow()) )
		{
			// warte-faktor erhoehen
			factorSleepBecauseOfLoadAverage += 0.5;
		}
		else
		{
			// warte faktor resetten
			factorSleepBecauseOfLoadAverage = 1.0f;
		}
		
		// 2) die groesse des binary-files festhalten
		long fileSizeInKB = fileBinary.length() / 1024;
		
		System.err.println("debug: file size is now " + String.valueOf(fileSizeInKB) + " kB");
		process.getTimeSerieBinarySize().addValue(String.valueOf(fileSizeInKB));
		//p3.fileBinary.length() / 1024;
		
		// DEBUGGING
		// 3) die groesse der einzelnen steps festhalten
		String dieGroessenAlsString = "";
		for(Step actStep : process.getStep())
		{
			dieGroessenAlsString += "   " + actStep.getName() + "=" + ObjectGraphMeasurer.measure(actStep);
		}
		process.getTimeSerieStepSize().addValue(dieGroessenAlsString);
		// DEBUGGING
		
		process.log("debug", "manager "+managerid+": actual infilexml is: "+process.getInfilexml());
		process.log("debug", "manager "+managerid+": reading binary file: "+process.getInfilebinary());
		// die manager-id mit eigener vergleichen. wenn nicht gleich, dann beenden.
		if (!(process.getManagerid() == managerid))
		{
//					p3.log("warn", "manager "+managerid+": it appears that another manager (id: "+p3.getManagerid()+") took over. killing myself. bye.");
			System.err.println("it appears another instance of manager (id: "+process.getManagerid()+") took over. so i'm (id: "+managerid+") not longer needed. killing myself. byebye.");
			exit = true;
			System.exit(0);
		}

		boolean imProzessHatSichWasGeaendert = true;
		System.err.println("debug: variable imProzessHatSichWasGeaendert manuell gesetzt auf " + imProzessHatSichWasGeaendert);
		process.log("debug", "variable imProzessHatSichWasGeaendert manuell gesetzt auf " + imProzessHatSichWasGeaendert);

		while(process.run && imProzessHatSichWasGeaendert)
		{
			System.err.println("debug: deshalb wird die while schleife erst mal durchlaufen");
			process.log("debug", "deshalb wird die while schleife erst mal durchlaufen");

			// prozess laufen lassen
			process.doIt(ini.get("apps", "pkraft-syscall"), ini.get("apps", "pkraft-manager"), ini.get("process", "domain-installation-directory"));

			// setzen der schlafdauer sleepMinutes. richtet sich nach dem feld stepStartDelayMinutes falls dieses existiert
			if(process.stepStartDelayMinutesMinimumOfInitializedSteps() != null)
			{
				loopMinutes = process.stepStartDelayMinutesMinimumOfInitializedSteps();
				if(loopMinutes < 1)
				{
					loopMinutes = 1;
				}
			}
			else
			{
				loopMinutes = 5;
			}
			
			// hat sich was geaendert?
			imProzessHatSichWasGeaendert = process.isStepStatusChangedWhileLastDoIt();
			System.err.println("debug: did some step changed its status? " + imProzessHatSichWasGeaendert);
			process.log("debug", "did some step changed its status? " + imProzessHatSichWasGeaendert);
			
			// finished
			if(process.getStatus().equals("finished"))
			{
				System.err.println("debug: status is finished");

				// wenn der prozess den status 'finished' hat, soll dieses programm beendet werden
				process.run = false;
				process.log("info", "manager "+managerid+": process instance is finished. goodbye from manager id "+process.getManagerid());
				process.setTimeOfProcessFinishedOrError(System.currentTimeMillis());
			}
			
			// error
			else if(process.getStatus().equals("error"))
			{
				System.err.println("debug: status is error");
				process.run = false;
				process.log("info", "error in process detected. setting run = false");
				process.log("info", "stopping manager "+process.getManagerid());
				process.setTimeOfProcessFinishedOrError(System.currentTimeMillis());

				// errorcode string erzeugen
				String exitCode = "error-in-steps:";
				for(Step actStep : process.getStepError())
				{
					exitCode = exitCode + "," + actStep.getName();
				}
			}
		}

		// binary und statistik files updaten
		updateFile(process);

		// pradar updaten
		pradarAttend(process.getInfilebinary());
		
		if(!process.run)
		{
			exit = true;
			System.exit(0);
		}
		
		// da prozess nicht mehr weiterging, werden watchKeys auf laufende steps erstellt
		if(!onlyPush)
		{
			process.log("info", "creating WatchKeys on every working step");
			System.err.println("info: creating WatchKeys on every working step");
			try
			{
				createWatchKeysForAllRunningSteps(process);
			}
			// falls das scheitert, soll einfach 1 minute gewartet werden
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				System.err.println("error: failed to register file watchers. sleeping 1 minute instead");
				e.printStackTrace();
			}
		}
	}

	/**
	 * erstellt fuer jeden running step einen watchkey
	 * es soll jedes stepverzeichnis mit dem status 'working' observiert werden bis das file ".exit" erscheint
	 * @param process
	 * @throws IOException 
	 */
	private static void createWatchKeysForAllRunningSteps(Process process) throws IOException
	{
		// diesen Thread ablegen, damit er vom zyklischen thread gekillt werden kann
		watcherThread = Thread.currentThread();
		
		// einen neuen map erzeugen fuer die watchKeys
		keys = new HashMap<WatchKey,Path>();
		
		WatchService watcher = FileSystems.getDefault().newWatchService();
		
		// Anlegen des WatchKeys fuer den Prozess (falls er gestoppt wird, erfolgt die Komunikation mit diesem manager ueber das binaerfile)
		Path processDir = Paths.get(process.getRootdir());
		System.err.println("info: creating a watchkey for the process path " + process.getRootdir());
		WatchKey keyProcess = processDir.register(watcher, ENTRY_MODIFY);
		keys.put(keyProcess, processDir);
		
		// Anlegen der WatchKeys fuer jeden laufenden Step
		for(Step actStep : process.getStep())
		{
			if(actStep.getStatus().equals("working"))
			{
				Path stepDir = Paths.get(actStep.getAbsdir());
				try
				{
					System.err.println("info: step " + actStep.getName() + " is working -> creating a watchkey for its path " + actStep.getAbsdir());
					System.err.println("debug: creating...");
					WatchKey key = stepDir.register(watcher, ENTRY_CREATE);
					System.err.println("debug: creating...done. putting to the map");
					keys.put(key, stepDir);
					System.err.println("debug: creating...done. putting to the map...done");
				}
				catch(IOException e)
				{
					System.err.println(e);
				}
				catch(Exception e)
				{
					System.err.println(e);
				}
				
				
				java.io.File stepDirExitFile = new java.io.File(actStep.getAbsdir() + "/.exit");
				java.io.File stepDirStatusFile = new java.io.File(actStep.getAbsdir() + "/.status");
				
				// falls die datei bereits existiert, wird sofort erneut der Prozess weitergeschoben
				// dies ist dann der fall, wenn ein step gestartet wurde, und danach der manager neu gestartet wurde
				if(stepDirExitFile.exists())
				{
					System.err.println("info: .exit file already exists -> shortcutting to pushing the process");

					// alle keys loeschen
					keys = null;

					// den prozess weiter pushen
					pushProcessAsFarAsPossible(process.getRootdir()+"/process.pmb", false);
				}
				// falls der step ein process ist, bibts dort kein .exit file sondern ein .status file
				else if(stepDirStatusFile.exists())
				{
					System.err.println("info: .status file already exists.");
					try
					{
						java.util.List<String> statusInhalt = Files.readAllLines(stepDirStatusFile.toPath(), Charset.defaultCharset());
						if(statusInhalt.size() > 0)
						{
							String firstLine = statusInhalt.get(0);
							System.err.println("info: status changed to: " + firstLine);

						
							System.err.println("info: .status file contains status " + firstLine);
							// wenn ein finaler status, dann soll manager aufgeweckt werden
							if(firstLine.equals("error") || firstLine.equals("finished"))
							{
								System.err.println("info: --> shortcutting to pushing process");
								// alle keys loeschen
								keys = null;
		
								// den prozess weiter pushen
								pushProcessAsFarAsPossible(process.getRootdir()+"/process.pmb", false);
							}
						}
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						System.err.println("IOException: trying to read file: " + stepDirStatusFile.getAbsolutePath());
						e.printStackTrace();
					}
					catch (ExceptionInInitializerError e)
					{
						System.err.println("ExceptionInInitializerError: trying to read file: " + stepDirStatusFile.getAbsolutePath());
						e.printStackTrace();
					}
				}
				

			}
		}
		
		process.log("info", "now into the watchloop");

		// warten auf ein Signal von einem WatchKey
		for(;;)
		{

			WatchKey key;
			try
			{
				key = watcher.take();
			}
			catch (InterruptedException e)
			{
				System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- watcher thread: interrupted! returning to alternativer Thread");
				return;
			}
			
			Path dir = keys.get(key);
			if(dir == null)
			{
				System.err.println("WatchKey not recognized!!");
				continue;
			}
			
			for(WatchEvent<?> event : key.pollEvents())
			{
//				System.err.println("debug: poll event " + event);

				WatchEvent.Kind kind = event.kind();
				
				WatchEvent<Path> ev = (WatchEvent<Path>)event;
				Path name = ev.context();
				// dieses logging fuehrt zur aenderung von stderr.txt und .log, was wiederum ein ENTRY_MODIFY ausloest etc. endlosschleife bis platte volllaeuft
//				System.err.println("debug: poll context " + name);
				Path child = dir.resolve(name);
//				System.err.println("debug: poll child " + child);

				if(kind == ENTRY_CREATE)
				{
					if(child.endsWith(".exit"))
					{
						System.err.println("info: waking up, because file created: " + child.toString());

						// alle keys loeschen
						keys = null;

						// den prozess weiter pushen
						pushProcessAsFarAsPossible(process.getRootdir()+"/process.pmb", false);
					}
				}
				if((kind == ENTRY_MODIFY) && (child.endsWith("process.pmb")))
				{
//					System.err.println("info: waking up, because process binary file has been modified: " + child.toString());

					// alle keys loeschen
					keys = null;

					// den prozess weiter pushen
					pushProcessAsFarAsPossible(process.getRootdir()+"/process.pmb", false);
				}
				if(kind == ENTRY_CREATE || kind == ENTRY_MODIFY)
				{
					if(child.endsWith(".status"))
					{
						try
						{
							java.util.List<String> statusInhalt = Files.readAllLines(child, Charset.defaultCharset());
							if(statusInhalt.size() > 0)
							{
								String firstLine = statusInhalt.get(0);
								System.err.println("info: status changed to: " + firstLine);

							
								// wenn ein finaler status, dann soll manager aufgeweckt werden
								if(firstLine.equals("error") || firstLine.equals("finished"))
								{
									System.err.println("info: waking up, because status changed to: " + firstLine);
									// alle keys loeschen
									keys = null;
			
									// den prozess weiter pushen
									pushProcessAsFarAsPossible(process.getRootdir()+"/process.pmb", false);
								}
							}
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							System.err.println("IOException: trying to read file: " + child.toString());
							e.printStackTrace();
						}
						catch (ExceptionInInitializerError e)
						{
							System.err.println("ExceptionInInitializerError: trying to read file: " + child.toString());
							e.printStackTrace();
						}
						
					}
				}
				
				// reset the triggered key
				key.reset();
			}
		}
	}
	
	/**
	 * startet einen thread, der zeitgesteuert aufwacht und den prozess weiterschiebt
	 */
	private static void startZyklischerThread(int initialWaitSecond)
	{
		// falls dieser thread von hier aus gestartet wird, soll kurz gewartet werden
		try {
			Thread.sleep(initialWaitSecond * 1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// einen timer thread erstellen, der regelmaessig den prozess aufweckt, auch wenn sehr langlaufende steps gerade aktiv sind
		new Thread(new Runnable() {
			public void run() {

				// flag, ob dieser thread einen neuen gestartet hat
				boolean spawn = false;

				System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: start");
				while(!exit && !spawn)
				{
					
					
					long tatsaechlicheSleepDauer = (long) (factorSleepBecauseOfLoadAverage * ((loopMinutes * 60 * 1000) + fuzzyness));
					try
					{
						System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: sleeping "+ tatsaechlicheSleepDauer/1000 + " seconds (loopMinutes="+loopMinutes+", faktorSleepBecauseOfLoadAverage="+factorSleepBecauseOfLoadAverage+", fuzzyness="+fuzzyness+")");
						Thread.sleep(tatsaechlicheSleepDauer);
					}
					catch (NumberFormatException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
					// war der letzte zugriff laenger als der haelfte der regulaeren wartezeit her? Dann Prozess pushen
					if((System.currentTimeMillis() - lastRun) > (0.5 * tatsaechlicheSleepDauer) )
					{
						System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: last process push has been MORE than 0.5 * "+tatsaechlicheSleepDauer/1000+" seconds ago at " + new Timestamp(lastRun));
						System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: waking up");
						
						if(watcherThread != null)
						{
							System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: interrupting watcherThread");
							
							watcherThread.interrupt();
							watcherThread = null;
						}
						
						// ein neuer 
						startZyklischerThread(5);
						spawn = true;
						
						pushProcessAsFarAsPossible(line.getOptionValue("instance"), false);
						
						System.err.println(new Timestamp(System.currentTimeMillis()) + ": ----- alternative thread: end");
					}
					else
					{
						System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: last process push has been LESS than 0.5 * "+tatsaechlicheSleepDauer/1000+" seconds ago at " + new Timestamp(lastRun));
						System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: going to sleep again");
						
					}
					
				}

				// thread beenden
				System.err.println(new Timestamp(System.currentTimeMillis()) + ": ---- alternative thread: exit");
			}
		}).start();
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		exit = true;
		System.exit(1);
	}

	private static void exiterException(String pathToInstance, Exception e)
	{
		try
		{
			java.io.FileWriter writer = new FileWriter(pathToInstance + ".pkraft-manager.stacktrace", true);
			writer.write(new Timestamp(System.currentTimeMillis()).toString() + "\n");
			writer.write(e.getMessage() + "\n");
			writer.write(e.getStackTrace() + "\n");
			writer.write("--------------" + "\n");
			writer.close();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		exit = true;
		System.exit(1);
	}



}
