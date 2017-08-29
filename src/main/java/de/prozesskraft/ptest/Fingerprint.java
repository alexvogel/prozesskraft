package de.prozesskraft.ptest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Scanner;

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

import de.prozesskraft.commons.*;
import de.prozesskraft.ptest.*;

import java.nio.file.attribute.*;

public class Fingerprint
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String web = "www.prozesskraft.de";
	static String author = "alexander.vogel@prozesskraft.de";
	static String version = "[% version %]";
	static String date = "[% date %]";
	static Ini ini;
	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{

//		try
//		{
//			if (args.length != 3)
//			{
//				System.out.println("Please specify processdefinition file (xml) and an outputfilename");
//			}
//			
//		}
//		catch (ArrayIndexOutOfBoundsException e)
//		{
//			System.out.println("***ArrayIndexOutOfBoundsException: Please specify processdefinition.xml, openoffice_template.od*, newfile_for_processdefinitions.odt\n" + e.toString());
//		}
		
		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Fingerprint.class) + "/" + "../etc/ptest-fingerprint.ini");

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
		Option opath = OptionBuilder.withArgName("PATH")
				.hasArg()
				.withDescription("[mandatory; default: .] the root path for the tree you want to make a fingerprint from.")
//				.isRequired()
				.create("path");
		
		Option osizetol = OptionBuilder.withArgName("FLOAT")
				.hasArg()
				.withDescription("[optional; default: 0.02] the sizeTolerance (as factor in percent) of all file entries will be set to this value. [0.0 < sizetol < 1.0]")
//				.isRequired()
				.create("sizetol");
		
		Option omd5 = OptionBuilder.withArgName("no|yes")
				.hasArg()
				.withDescription("[optional; default: yes] should be the md5sum of files determined? no|yes")
//				.isRequired()
				.create("md5");
		
		Option oignore = OptionBuilder.withArgName("STRING")
				.hasArgs()
				.withDescription("[optional] path-pattern that should be ignored when creating the fingerprint")
//				.isRequired()
				.create("ignore");
		
		Option oignorefile = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional] file with path-patterns (one per line) that should be ignored when creating the fingerprint")
//				.isRequired()
				.create("ignorefile");
		
		Option ooutput = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory; default: <path>/fingerprint.xml] fingerprint file")
//				.isRequired()
				.create("output");
		
		Option of = new Option("f", "[optional] force overwrite fingerprint file if it already exists");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( opath );
		options.addOption( osizetol );
		options.addOption( omd5 );
		options.addOption( oignore );
		options.addOption( oignorefile );
		options.addOption( ooutput );
		options.addOption( of );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			commandline = parser.parse( options,  args );
			
		}
		catch ( Exception exp )
		{
			// oops, something went wrong
			System.err.println( "Parsing failed. Reason: "+ exp.getMessage());
			exiter();
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( commandline.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("fingerprint", options);
			System.exit(0);
		}

		else if ( commandline.hasOption("v"))
		{
			System.out.println("web:     "+web);
			System.out.println("author: "+author);
			System.out.println("version:"+version);
			System.out.println("date:     "+date);
			System.exit(0);
		}

		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		String path = "";
		String sizetol = "";
		boolean md5 = false;
		Float sizetolFloat = null;
		String output = "";
		java.io.File ignorefile = null;
		ArrayList<String> ignore = new ArrayList<String>();

		if ( !( commandline.hasOption("path")) )
		{
			System.err.println("setting default for -path=.");
			path = ".";
		}
		else
		{
			path = commandline.getOptionValue("path");
		}

		if ( !( commandline.hasOption("sizetol")) )
		{
			System.err.println("setting default for -sizetol=0.02");
			sizetol = "0.02";
			sizetolFloat = 0.02F;
		}
		else
		{
			sizetol = commandline.getOptionValue("sizetol");
			sizetolFloat = Float.parseFloat(sizetol);
			
			if((sizetolFloat > 1) || (sizetolFloat < 0))
			{
				System.err.println("use only values >=0.0 and <1.0 for -sizetol");
				System.exit(1);
			}
		}

		if ( !( commandline.hasOption("md5")) )
		{
			System.err.println("setting default for -md5=yes");
			md5 = true;
		}
		else if(commandline.getOptionValue("md5").equals("no"))
		{
			md5 = false;
		}
		else if(commandline.getOptionValue("md5").equals("yes"))
		{
			md5 = true;
		}
		else
		{
			System.err.println("use only values no|yes for -md5");
			System.exit(1);
		}

		if (  commandline.hasOption("ignore") )
		{
			ignore.addAll(Arrays.asList(commandline.getOptionValues("ignore")));
		}
		
		if (  commandline.hasOption("ignorefile") )
		{
			ignorefile = new java.io.File(commandline.getOptionValue("ignorefile"));
			if(! ignorefile.exists())
			{
				System.err.println("warn: ignore file does not exist: "+ignorefile.getCanonicalPath());
			}
		}
		
		if ( !( commandline.hasOption("output")) )
		{
			System.err.println("setting default for -output="+path+"/fingerprint.xml");
			output = path+"/fingerprint.xml";
		}
		else
		{
			output = commandline.getOptionValue("output");
		}
		
		// wenn output bereits existiert -> abbruch
		java.io.File outputFile = new File(output);
		if(outputFile.exists())
		{
			if(commandline.hasOption("f"))
			{
				outputFile.delete();
			}
			else
			{
				System.err.println("error: output file (" + output + ") already exists. use -f to force overwrite.");
				System.exit(1);
			}
		}
		
//		if ( !( commandline.hasOption("output")) )
//		{
//			System.err.println("option -output is mandatory.");
//			exiter();
//		}

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
		Dir dir = new Dir();
		dir.setBasepath(path);
		dir.setOutfilexml(output);

		// ignore file in ein Array lesen
		if((ignorefile != null) && (ignorefile.exists()))
		{
			Scanner sc = new Scanner(ignorefile);
			while(sc.hasNextLine())
			{
				ignore.add(sc.nextLine());
			}
			sc.close();
		}

		
//		// autoignore hinzufuegen
//		String autoIgnoreString = ini.get("autoignore", "autoignore");
//		ignoreLines.addAll(Arrays.asList(autoIgnoreString.split(",")));

//		// debug
//		System.out.println("ignorefile content:");
//		for(String actLine : ignore)
//		{
//			System.out.println("line: "+actLine);
//		}
		
		try
		{
			dir.genFingerprint(sizetolFloat, md5, ignore);
		}
		catch (NullPointerException e)
		{
			System.err.println("file/dir does not exist "+path);
			e.printStackTrace();
			exiter();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			exiter();
		}

		System.out.println("writing to file: "+dir.getOutfilexml());
		dir.writeXml();
		
	}

	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
