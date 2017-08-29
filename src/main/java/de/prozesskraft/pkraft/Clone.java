package de.prozesskraft.pkraft;

//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

public class Clone
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
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Clone.class) + "/" + "../etc/pkraft-clone.ini");

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
		Option oinstance = OptionBuilder.withArgName("File")
				.hasArg()
				.withDescription("[mandatory] process you want to clone.")
//				.isRequired()
				.create("instance");
		
		Option obasedir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[optional, default: <basedirOfInstance>] base directory you want to place the root directory of the clone. this directory must exist at call time.")
//				.isRequired()
				.create("basedir");


		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( oinstance );
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
			formatter.printHelp("clone", options);
			System.exit(0);
		}

		if ( commandline.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@prozesskraft.de");
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
		java.io.File fileBaseDir = null;
		
		// wenn es nicht vorhanden ist, dann mit fehlermeldung abbrechen
		if(!fileInstance.exists())
		{
			System.err.println("instance file does not exist.");
			exiter();
		}

		// testen ob eventuell vorhandene angaben basedir
		if(commandline.hasOption("basedir"))
		{
			fileBaseDir = new java.io.File(commandline.getOptionValue("basedir"));
			if(!fileBaseDir.exists())
			{
				System.err.println("error: -basedir: directory does not exist");
				exiter();
			}
			if(!fileBaseDir.isDirectory())
			{
				System.err.println("error: -basedir: is not a directory");
				exiter();
			}
		}
		
		// den main-prozess trotzdem nochmal einlesen um subprozesse extrahieren zu koennen
		Process p1 = new Process();
		p1.setInfilebinary(pathToInstance);
		Process process = p1.readBinary();
		
		// directories setzen, falls angegeben
		if(fileBaseDir != null)
		{
			process.setBaseDir(fileBaseDir.getCanonicalPath());
		}

		// den main-prozess ueber die static function klonen
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
					cloneProcess(processInSubprocess, clonedProcess);
				}
			}
		}
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}

	/**
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
			clone = process.cloneWithData(null, null);
			System.err.println("info: cloning instance: original=" + process.getRootdir() + "/process.pmb, clone=" + clone.getRootdir() + "/process.pmb");
		}
		else
		{
			clone = process.cloneWithData(parentProcess.getRootdir() + "/dir4step_" + process.getStepnameOfParent(), parentProcess.getId());
			System.err.println("debug: stepname of parentProcess is: " + process.getStepnameOfParent());
			System.err.println("debug: process.cloneWithData(" + parentProcess.getRootdir() + "/dir4step_" + process.getStepnameOfParent() + ", " + parentProcess.getId());
			System.err.println("info: cloning instance as a child: original=" + process.getRootdir() + "/process.pmb, clone=" + clone.getRootdir() + "/process.pmb");
		}

//		// das original speichern, weil auch hier aenderungen vorhanden sind (zaehler fuer klone)
		process.setOutfilebinary(process.getInfilebinary());
		process.writeBinary();

		// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
		String call2 = ini.get("apps", "pradar-attend") + " -instance " + clone.getRootdir() + "/process.pmb"; 
		System.err.println("info: calling: "+call2);

		try
		{
			java.lang.Process sysproc = Runtime.getRuntime().exec(call2);
		}
		catch (IOException e)
		{
			System.err.println("error: " + e.getMessage());
		}
		
		// rueckgabe der id. kann beim klonen von childprozessen verwendet werden
		return clone;
	}

}
