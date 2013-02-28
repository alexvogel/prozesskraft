package de.caegroup.pradar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
//import org.apache.xerces.impl.xpath.regex.ParseException;

import de.caegroup.pradar.*;;

public class List
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
		Option process = OptionBuilder.withArgName("process")
				.hasArg()
				.withDescription("[optional] filter for field 'process'")
//				.isRequired()
				.create("process");
		
		Option host = OptionBuilder.withArgName("host")
				.hasArg()
				.withDescription("[optional] filter for field 'host'")
//				.isRequired()
				.create("host");
		
		Option id = OptionBuilder.withArgName("id")
				.hasArg()
				.withDescription("[optional] filter for field 'id'")
				.create("id");
		
		Option user = OptionBuilder.withArgName("user|all")
				.hasArg()
				.withDescription("[optional; default=<you>] filter for field 'user'")
//				.isRequired()
				.create("user");
				
		Option exitcode = OptionBuilder.withArgName("exitcode")
				.hasArg()
				.withDescription("[optional] filter for field 'exitcode'")
//				.isRequired()
				.create("exitcode");
				
		Option active = OptionBuilder.withArgName("true|false")
				.hasArg()
				.withDescription("[optional; default=true] filter for field 'active'")
//				.isRequired()
				.create("active");
				
		Option dbfile = OptionBuilder.withArgName("dbfile")
				.hasArg()
				.withDescription("[optional] specify dbfile if not default")
//				.isRequired()
				.create("dbfile");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( process );
		options.addOption( id );
		options.addOption( host );
		options.addOption( user );
		options.addOption( exitcode );
		options.addOption( active );
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
//			formatter.printHelp("list --version [% version %]", options);
			formatter.printHelp("list", options);
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
		Entity entity = new Entity();
		System.out.println(entity.getId());
		System.out.println(entity.getIdSqlPattern());
		entity.setProcess(line.getOptionValue("process"));

		//		Properties systemproperties = System.getProperties();
//		for (Enumeration e = systemproperties.propertyNames(); e.hasMoreElements();)
//		{
//			String prop = (String) e.nextElement();
//			System.out.println("Property: " + prop + " , Wert: " + systemproperties.getProperty(prop));
//		}

		if (line.hasOption("dbfile"))
		{
			File file = new File(line.getOptionValue("dbfile"));
			if (file.exists())
			{
				db.setDbfile(file);
			}
		}
		
		if (!(db.getDbfile().exists()))
		{
			System.err.println("file not found: " + db.getDbfile().getAbsolutePath());
			System.exit(1);
		}
		
		// definition des filters fuer id
		if (line.hasOption("id"))
		{
			entity.setId(Long.valueOf(line.getOptionValue("id")));
		}

		// definition des filters fuer process
		if (line.hasOption("process"))
		{
			entity.setProcess(line.getOptionValue("process"));
		}

		// definition des filters fuer user
		// default ist aktueller user
		if (line.hasOption("user"))
		{
			entity.setUser(line.getOptionValue("user"));
		}
		else
		{
			entity.setUser(entity.getActualuser());
		}
		
		// definition des filters fuer exitcode
		if (line.hasOption("exitcode"))
		{
			entity.setExitcode(line.getOptionValue("exitcode"));
		}

		// definition des filters fuer host
		if (line.hasOption("host"))
		{
			entity.setHost(line.getOptionValue("host"));
		}

		// definition des filters fuer active
		if (line.hasOption("active"))
		{
			entity.setActive(line.getOptionValue("active"));
		}
		else
		{
			entity.setActive("true");
		}

		// liste ausgeben und definierten filter dabei anwenden
		db.list(entity);
	}
}
