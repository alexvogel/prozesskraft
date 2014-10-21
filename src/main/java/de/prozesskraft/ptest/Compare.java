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
import java.util.EnumSet;

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

import de.caegroup.commons.*;
import de.prozesskraft.ptest.*;

import java.nio.file.attribute.*;

public class Compare
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
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Compare.class) + "/" + "../etc/ptest-compare.ini");

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
		Option oref = OptionBuilder.withArgName("PATH")
				.hasArg()
				.withDescription("[mandatory] directory or fingerprint, that the --exam will be checked against")
//				.isRequired()
				.create("ref");
		
		Option oexam = OptionBuilder.withArgName("PATH")
				.hasArg()
				.withDescription("[mandatory] directory or fingerprint, that will be checked against --ref")
//				.isRequired()
				.create("exam");
		
		Option oresult = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory; default: result.txt] the result (success|failed) of the comparison will be printed to this file")
//				.isRequired()
				.create("result");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( oref );
		options.addOption( oexam );
		options.addOption( oresult );
		
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
			formatter.printHelp("compare", options);
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
		boolean error = false;
		String result = "";
		if ( !( commandline.hasOption("ref")) )
		{
			System.err.println("option -ref is mandatory");
			error = true;
		}
		if ( !( commandline.hasOption("exam")) )
		{
			System.err.println("option -exam is mandatory");
			error = true;
		}
		
		if(error)
		{
			exiter();
		}
		
		if ( !( commandline.hasOption("result")) )
		{
			System.err.println("setting default: -result=result.txt");
			result = "result.txt";
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
		
		// einlesen der referenzdaten
		java.io.File refPath = new java.io.File(commandline.getOptionValue("ref"));
		
		Dir refDir = new Dir();

		// wenn es ein directory ist, muss der fingerprint erzeugt werden
		if(refPath.exists() && refPath.isDirectory())
		{
			refDir.setBasepath(refPath.getCanonicalPath());
			refDir.genFingerprint();
		}
		// wenn es ein fingerprint ist, muss er eingelesen werden
		else if(refPath.exists())
		{
			refDir.setInfilexml(refPath.getCanonicalPath());
			try {
				refDir.readXml();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// einlesen der prueflingsdaten
		java.io.File examPath = new java.io.File(commandline.getOptionValue("exam"));

		Dir examDir = new Dir();

		// wenn es ein directory ist, muss der fingerprint erzeugt werden
		if(examPath.exists() && examPath.isDirectory())
		{
			examDir.setBasepath(examPath.getCanonicalPath());
			examDir.genFingerprint();
		}
		// wenn es ein fingerprint ist, muss er eingelesen werden
		else if(examPath.exists())
		{
			examDir.setInfilexml(examPath.getCanonicalPath());
			try
			{
				examDir.readXml();
			}
			catch (JAXBException e)
			{
				System.err.println("error while reading xml");
				e.printStackTrace();
			}
		}

		// durchfuehren des vergleichs
		boolean resultValue = refDir.checkDir(examDir);
		if(resultValue == true)
		{
			System.out.println("SUCCESS");
		}
		else
		{
			System.out.println("FAILED");
		}
		
	}

	private static void exiter()
	{
		System.err.println("try -help for help.");
		System.exit(1);
	}


}
