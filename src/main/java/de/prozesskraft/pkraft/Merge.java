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
			formatter.printHelp("startinstance", options);
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
			System.err.println("option -guest is mandatory");
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

		String pathToGuest = commandline.getOptionValue("guest");
		java.io.File fileGuest = new java.io.File(pathToGuest);

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

		// wenn es nicht vorhanden ist, dann mit fehlermeldung abbrechen
		if(!fileInstance.exists())
		{
			System.err.println("instance file does not exist: " + fileInstance.getAbsolutePath());
			exiter();
		}
		// wenn es nicht vorhanden ist, dann mit fehlermeldung abbrechen
		if(!fileGuest.exists())
		{
			System.err.println("guest file does not exist: " + fileGuest.getAbsolutePath());
			exiter();
		}

		// instance einlesen
		Process p1 = new Process();
		p1.setInfilebinary(pathToInstance);
		p1.setOutfilebinary(pathToInstance);
		Process p2 = p1.readBinary();

		// guest einlesen
		Process p30 = new Process();
		p30.setInfilebinary(pathToGuest);
		Process pGuest = p1.readBinary();

		// testen ob beide instanzen vom gleichen typ sind
		if(!p2.getName().equals(pGuest.getName()))
		{
			System.err.println("error: instances are not from the same type (-instance=" + p2.getName() + " != -guest=" + pGuest.getName());
			exiter();
		}
		
		// testen ob beide instanzen von gleicher version sind
		if(!p2.getVersion().equals(pGuest.getVersion()))
		{
			System.err.println("error: instances are not from the same version (" + p2.getVersion() + "!=" + pGuest.getVersion());
			exiter();
		}

		System.err.println("info: clone instance to directory: " + baseDir);
		Process cloneInstance = p2.cloneWithData(baseDir);
		cloneInstance.setOutfilebinary(cloneInstance.getRootdir() + "/process.pmb");

		// weil beim clonen auch beim original felder veraendert werden (zaehler fuer klone, etc.) soll auch das original neu geschrieben werden
		System.err.println("info: schreiben des binary files: " + p2.getOutfilebinary());
		p2.writeBinary();
		

		// sind sie vom gleichen typ
		if(!cloneInstance.getName().equals(pGuest.getName()))
		{
			System.err.println("error: instances are not from the same type (" + cloneInstance.getName() + "!=" + pGuest.getName());
			exiter();
		}

		// sind sie vom gleichen version
		if(!cloneInstance.getVersion().equals(pGuest.getVersion()))
		{
			System.err.println("error: instances are not from the same version (" + cloneInstance.getVersion() + "!=" + pGuest.getVersion());
			exiter();
		}

		// merge durchfuehren
		// alle fanned steps (ehemalige multisteps) des zu mergenden prozesses in die fanned multisteps des bestehenden prozesses integrieren
		for(Step actStep : pGuest.getStep())
		{
			if(actStep.isAFannedMultistep())
			{
				System.err.println("info: merging from external instance step " + actStep.getName());
				if(cloneInstance.integrateStep(actStep))
				{
					System.err.println("info: merging step successfully.");
				}
				else
				{
					System.err.println("error: merging step failed.");
				}
			}
			else
			{
				System.err.println("debug: because it's not a multistep, ignoring from external instance step " + actStep.getName());
			}
		}
		
		// speichern der ergebnis instanz
		cloneInstance.writeBinary();
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
