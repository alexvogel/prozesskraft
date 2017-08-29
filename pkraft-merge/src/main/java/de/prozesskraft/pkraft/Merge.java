package de.prozesskraft.pkraft;

//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;

public class Merge
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static Ini ini;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{

		/*----------------------------
		  get options from ini-file
		----------------------------*/
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Merge.class) + "/" + "../etc/pkraft-merge.ini");

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
		Option ohelp = new Option("help", "print this message");
		Option ov = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option oinstance = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] instance you want to merge another instance into.")
//				.isRequired()
				.create("instance");
		
		Option oguest = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] this instance will be merged into -instance.")
//				.isRequired()
				.create("guest");
		
		Option obasedir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[optional] in this base-directory the result instance (merge of -instance and -guest) will be placed. this directory has to exist. omit to use the base-directory of -instance.")
//				.isRequired()
				.create("basedir");

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( oinstance );
		options.addOption( oguest );
		options.addOption( obasedir );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		// parse the command line arguments
		commandline = parser.parse( options,  args );
		
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( commandline.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("merge", options);
			System.exit(0);
		}

		if ( commandline.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@caegroup.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		if ( !( commandline.hasOption("instance")))
		{
			System.err.println("option -instance is mandatory");
			exiter();
		}
		if ( !( commandline.hasOption("guest")))
		{
			System.err.println("at least one option -guest is mandatory");
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
		  die eigentliche business logic
		----------------------------*/
		String pathToInstance = commandline.getOptionValue("instance");
		java.io.File fileInstance = new java.io.File(pathToInstance);

		String[] pathToGuest = commandline.getOptionValues("guest");

		String baseDir = null;
		if(commandline.hasOption("basedir"))
		{
			java.io.File fileBaseDir = new java.io.File(commandline.getOptionValue("basedir"));
			if(!fileBaseDir.exists())
			{
				System.err.println("basedir does not exist: " + fileBaseDir.getAbsolutePath());
				exiter();
			}
			else if(!fileBaseDir.isDirectory())
			{
				System.err.println("basedir is not a directory: " + fileBaseDir.getAbsolutePath());
				exiter();
			}
			baseDir = commandline.getOptionValue("basedir");
		}

		// ueberpruefen ob die process.pmb files vorhanden sind
		// wenn es nicht vorhanden ist, dann mit fehlermeldung abbrechen
		if(!fileInstance.exists())
		{
			System.err.println("instance file does not exist: " + fileInstance.getAbsolutePath());
			exiter();
		}
		for(String pathGuest : pathToGuest)
		{
			java.io.File fileGuest = new java.io.File(pathGuest);

			// wenn es nicht vorhanden ist, dann mit fehlermeldung abbrechen
			if(!fileGuest.exists())
			{
				System.err.println("guest file does not exist: " + fileGuest.getAbsolutePath());
				exiter();
			}
		}

		// base - instance einlesen
		Process p1 = new Process();
		p1.setInfilebinary(pathToInstance);
		p1.setOutfilebinary(pathToInstance);
		Process p2 = p1.readBinary();

		// alle guests einlesen
		ArrayList<Process> alleGuests = new ArrayList<Process>();
		for(String actPathGuest : pathToGuest)
		{
			Process p30 = new Process();
			p30.setInfilebinary(actPathGuest);
			Process pGuest = p30.readBinary();

			// testen ob base-instanz und aktuelle guestinstanz vom gleichen typ sind
			if(!p2.getName().equals(pGuest.getName()))
			{
				System.err.println("error: instances are not from the same type (-instance=" + p2.getName() + " != -guest=" + pGuest.getName());
				exiter();
			}

			// testen ob base-instanz und aktuelle guestinstanz von gleicher version sind
			if(!p2.getVersion().equals(pGuest.getVersion()))
			{
				System.err.println("error: instances are not from the same version (" + p2.getVersion() + "!=" + pGuest.getVersion());
				exiter();
			}

			alleGuests.add(pGuest);
		}

		// den main-prozess trotzdem nochmal einlesen um subprozesse extrahieren zu koennen
		Process p3 = new Process();
		p3.setInfilebinary(pathToInstance);
		Process process = p3.readBinary();
		
		// den main-prozess ueber die static function klonen
		// das anmelden bei pradar erfolgt erst ganz zum schluss, denn beim clonen werden nachfolgende steps resettet, die zu diesem zeitpunkt noch intakt sind
		Process clonedProcess = cloneProcess(process, null);

		// alle steps durchgehen und falls subprocesses existieren auch fuer diese ein cloning durchfuehren
		for(Step actStep : process.getStep())
		{
			if (actStep.getSubprocess() != null)
			{
				Process pDummy = new Process();
				pDummy.setInfilebinary(actStep.getAbsdir() + "/process.pmb");
				Process processInSubprocess = pDummy.readBinary();
//				System.err.println("info: reading process freshly from file: " + actStep.getAbsdir() + "/process.pmb");
				if(processInSubprocess != null)
				{
					Process clonedSubprocess = cloneProcess(processInSubprocess, clonedProcess);
					// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
					String call2 = ini.get("apps", "pradar-attend") + " -instance " + clonedSubprocess.getRootdir() + "/process.pmb"; 
					System.err.println("info: calling: "+call2);

					try
					{
						java.lang.Process sysproc = Runtime.getRuntime().exec(call2);
					}
					catch (IOException e)
					{
						System.err.println("error: " + e.getMessage());
					}
				}
			}
		}

		// alle dependent steps der zielinstanz einsammeln
		// dies wird zum resetten benoetigt, damit steps nicht doppelt resettet werden
		Map<Step,String> dependentSteps = new HashMap<Step,String>();
		
		// alle guest prozesse merge durchfuehren
		for(Process actGuestProcess : alleGuests)
		{
			System.err.println("info: merging guest process " + actGuestProcess.getInfilebinary());

			// alle fanned steps (ehemalige multisteps) des zu mergenden prozesses in die fanned multisteps des bestehenden prozesses integrieren
			for(Step actStep : actGuestProcess.getStep())
			{
				if(actStep.isAFannedMultistep())
				{
					System.err.println("info: merging from guest instance step " + actStep.getName());
					Step clonedStepForIntegrationInClonedProcess = actStep.clone();
					if(clonedProcess.integrateStep(clonedStepForIntegrationInClonedProcess))
					{
						System.err.println("info: merging step successfully.");
						// die downstream steps vom merge-punkt merken
						for(Step actStepToResetBecauseOfDependency : clonedProcess.getStepDependent(actStep.getName()))
						{
							dependentSteps.put(actStepToResetBecauseOfDependency, "dummy");
						}
						
						// der step einen subprocess enthaelt muss der subprocess nach der integration bei pradar gemeldet werden
						// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
						if(clonedStepForIntegrationInClonedProcess.getSubprocess() != null && clonedStepForIntegrationInClonedProcess.getSubprocess().getProcess() != null)
						{
							String call5 = ini.get("apps", "pradar-attend") + " -instance " + clonedStepForIntegrationInClonedProcess.getAbsdir() + "/process.pmb"; 
							System.err.println("info: calling: "+call5);
							try
							{
								java.lang.Process sysproc = Runtime.getRuntime().exec(call5);
							}
							catch (IOException e)
							{
								System.err.println("error: " + e.getMessage());
							}
						}
					}
					else
					{
						System.err.println("error: merging step failed.");
					}
				}
				else
				{
					System.err.println("debug: because it's not a multistep, ignoring from guest instance step " + actStep.getName());
				}
			}
		}

		// alle steps downstream der merge-positionen resetten
		for(Step actStep : dependentSteps.keySet())
		{
			actStep.resetBecauseOfDependency();
		}
		
		// speichern der ergebnis instanz
		clonedProcess.writeBinary();

		// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
		String call2 = ini.get("apps", "pradar-attend") + " -instance " + clonedProcess.getRootdir() + "/process.pmb"; 
		System.err.println("info: calling: "+call2);

		try
		{
			java.lang.Process sysproc = Runtime.getRuntime().exec(call2);
		}
		catch (IOException e)
		{
			System.err.println("error: " + e.getMessage());
		}

	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}

	/**
	 * diese funktion wird verwendet in pkraft-clone, pkraft-merge und fast gleich in pradar-gui
	 * clone Process mit Daten
	 * returns process-id
	 * @param entity
	 */
	public static Process cloneProcess(Process process, Process parentProcess)
	{

		// klonen mit data
		Process clone = null;
		if(parentProcess == null)
		{
			System.err.println("info: stepname of parentProcess is: " + process.getStepnameOfParent());
			System.err.println("info: process.cloneWithData(null, null)");
			clone = process.cloneWithData(null, null);
			System.err.println("info: instance cloned: original=" + process.getRootdir() + "/process.pmb, clone=" + clone.getRootdir() + "/process.pmb");
		}
		else
		{
			System.err.println("info: stepname of parentProcess is: " + process.getStepnameOfParent());
			System.err.println("info: process.cloneWithData(" + parentProcess.getRootdir() + "/dir4step_" + process.getStepnameOfParent() + ", " + parentProcess.getId());
			clone = process.cloneWithData(parentProcess.getRootdir() + "/dir4step_" + process.getStepnameOfParent(), parentProcess.getId());
			System.err.println("info: instance cloned as a child: original=" + process.getRootdir() + "/process.pmb, clone=" + clone.getRootdir() + "/process.pmb");
		}

//		// das original speichern, weil auch hier aenderungen vorhanden sind (zaehler fuer klone)
		process.setOutfilebinary(process.getInfilebinary());
		process.writeBinary();

		// rueckgabe der id. kann beim klonen von childprozessen verwendet werden
		return clone;
	}

}
