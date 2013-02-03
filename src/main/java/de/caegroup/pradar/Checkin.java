package de.caegroup.pradar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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

public class Checkin
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
				.withDescription("[mandatory] process")
//				.isRequired()
				.create("process");
		
		Option host = OptionBuilder.withArgName("host")
				.hasArg()
				.withDescription("[mandatory] hostname the processinstance is running")
//				.isRequired()
				.create("host");
		
		Option id = OptionBuilder.withArgName("id")
				.hasArg()
				.withDescription("[optional] unique id over all instances of all possible processes")
				.create("id");
		
		Option user = OptionBuilder.withArgName("user")
				.hasArg()
				.withDescription("[optional] owner of processinstance")
//				.isRequired()
				.create("user");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( process );
		options.addOption( id );
		options.addOption( host );
		
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
//			formatter.printHelp("checkin --version [% version %]", options);
			formatter.printHelp("checkin", options);
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
		
		if ( !(line.hasOption("process")) || !(line.hasOption("host")) )
		{
			System.out.println("at least one mandatory argument is missing.");
			System.out.println("try -help for help.");
			System.exit(1);
		}
		
		else
		{
			String programname = System.getProperty("sun.java.command");
			System.out.println("Systemproperty: "+programname);

//			File program = new File(System.getProperty("user.dir"));
//			String basedirectory = program.getParent();
//			File conffile = new File(basedirectory+"/etc/pradar.conf");
			
//			Properties conf = new Properties();
//			try
//			{
//				conf.load(new FileInputStream(conffile));
//			} catch (IOException e)
//			{
//				System.err.println("cannot read pradar config file: "+conffile.getAbsolutePath());
//				System.exit(2);
//				e.printStackTrace();
//			}
//			File dbfile = new File(conf.getProperty("dbfile"));

			File cwd = new File(".");
			File dbfile = new File(cwd.getAbsolutePath()+"/pradar.db");
			System.out.println("Databasefile: "+dbfile.getAbsolutePath());
			Db db = new Db(dbfile);
			System.out.println("Db-Object: "+db.toString());
			db.initDb();
//			System.err.println("cannot open database in "+dbfile.getAbsolutePath());
			
//			Entity entity = db.genEntity();
//			entity.setProcessname(line.getOptionValue("process"));
//			entity.setHost(line.getOptionValue("host"));
//			if (line.hasOption("id")) { entity.setId(line.getOptionValue("id")); }
//			if (line.hasOption("user")) { entity.setId(line.getOptionValue("user")); }
//			entity.setCheckinToNow();
			
			// TODO ist id schon vorhanden?
		}

	}
}
