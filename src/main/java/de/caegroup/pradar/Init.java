package de.caegroup.pradar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
//import org.apache.xerces.impl.xpath.regex.ParseException;

import de.caegroup.pradar.*;;

public class Init
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
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
		  create boolean options
		----------------------------*/
		Option help = new Option("help", "print this message");
		Option v = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option dbfile = OptionBuilder.withArgName("dbfile")
				.hasArg()
				.withDescription("[mandatory] dbfile")
//				.isRequired()
				.create("dbfile");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( dbfile );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			line = parser.parse( options,  args );
		}
		catch ( Exception exp )
		{
			// oops, something went wrong
			System.err.println( "Parsing failed. Reason: "+ exp.getMessage());
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( line.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("init", options);
			System.exit(0);
		}
		
		if ( line.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@caegroup.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		/*----------------------------
		  querying the commandline
		----------------------------*/
//		if ( line.hasOption("definitionfile") )
//		{
//			String hello = line.getOptionValue("definitionfile");
//		}
		
		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		Db db = new Db();
		
		if ( !(line.hasOption("dbfile")))
		{
			System.out.println("assuming you mean the default location.");
		}
		
		else
		{
			String string_dbfile = line.getOptionValue("dbfile");
			File file = new File (string_dbfile);
			db.setDbfile(file);
		}
		
		if (db.getDbfile().exists())
		{
			System.out.println("file " + db.getDbfile().getAbsolutePath() + " already exists. please remove.");
			System.exit(0);
		}

		else
		{
			db.initDb();
		}
	}
}
