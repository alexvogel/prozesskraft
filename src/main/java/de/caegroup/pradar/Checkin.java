package de.caegroup.pradar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
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

public class Checkin
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	static int defaultPortNumber = 37888;
	
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
				.withDescription("[optional] hostname the processinstance is running")
//				.isRequired()
				.create("host");
		
		Option id = OptionBuilder.withArgName("id")
				.hasArg()
				.withDescription("[optional] unique id over all instances of all possible processes")
				.create("id");
		
		Option parentid = OptionBuilder.withArgName("parentid")
				.hasArg()
				.withDescription("[optional] id of the parent process")
				.create("parentid");
		
		Option user = OptionBuilder.withArgName("user")
				.hasArg()
				.withDescription("[optional] owner of processinstance")
//				.isRequired()
				.create("user");
				
		Option dbfile = OptionBuilder.withArgName("dbfile")
				.hasArg()
				.withDescription("[optional] dbfile")
//				.isRequired()
				.create("dbfile");
				
		Option resource = OptionBuilder.withArgName("resource")
				.hasArg()
				.withDescription("[optional] full path to a resource. e.g. logfile")
//				.isRequired()
				.create("resource");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( process );
		options.addOption( id );
		options.addOption( parentid );
		options.addOption( host );
		options.addOption( user );
		options.addOption( dbfile );
		options.addOption( resource );
		
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
		
		Db db = new Db();
		Entity entity = new Entity();

		if (line.hasOption("process"))
		{
			entity.setProcess(line.getOptionValue("process"));
		}
		else
		{
			System.out.println("-process is mandatory.");
			System.out.println("try -help for help.");
			System.exit(1);
		}

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
			else
			{
				System.err.println("file not found: " + file.getAbsolutePath());
				System.exit(1);
			}
		}
		
		if (line.hasOption("id"))
		{
			entity.setId(line.getOptionValue("id"));
		}
		else
		{
//			Random generator = new Random();
			entity.genId();
			System.out.println("<id>"+entity.getId()+"<id>");
		}

		if (line.hasOption("parentid"))
		{
			entity.setParentid(line.getOptionValue("parentid"));
		}
		else
		{
			entity.setParentid("0");
		}

		// setzen des hosts vom -host
		if (line.hasOption("host"))
		{
			entity.setHost(line.getOptionValue("host"));
		}
		// setzen des hosts vom system
		else
		{
			try
			{
				InetAddress addr = InetAddress.getLocalHost();
				entity.setHost(addr.getHostName());
			} catch (UnknownHostException e)
			{
				// mache nichts, dann greift der default 'HAL'
			}
		}
		
		// setzen der resource, falls angegeben
		if (line.hasOption("resource"))
		{
			entity.setResource(line.getOptionValue("resource"));
		}
		
		// setzen des user vom system
		entity.setUser(System.getProperty("user.name"));
		
		// setzen von active auf 'true'
		entity.setActive("true");
		
		// einchecken in die DB
		Socket server = null;
		
		try
		{
			server = new Socket("localhost", defaultPortNumber);
			OutputStream out = server.getOutputStream();
			InputStream in = server.getInputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
			ObjectInputStream  objectIn  = new ObjectInputStream(in);
			
			objectOut.writeObject("/soft/deploy/data/pradar/pradar.db");
			objectOut.writeObject("checkin");
			objectOut.writeObject(entity);
			
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectException e)
		{
			System.err.println("cannot connect to pradar-server on port "+defaultPortNumber);
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);

	}
}
