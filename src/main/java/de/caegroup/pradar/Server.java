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
import java.net.ServerSocket;
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

import de.caegroup.network.ConcurrentServer;
import org.ini4j.*;

public class Server
{

	/*----------------------------
	  fields
	----------------------------*/
	static CommandLine line;
	Ini ini;
	File file;

	/*----------------------------
	  constructors
	----------------------------*/
	public Server()
	{
		file = WhereAmI.getInifile(this.getClass());
		try
		{
			ini = new Ini(file);
		} catch (InvalidFileFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*----------------------------
	  main
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
	{
		
		Server salut = new Server();
		
		System.out.println(salut.file.getAbsolutePath());
		System.exit(0);

		int defaultPortNumber = 37888;
		int portNumber;
		String defaultPathToDb = new Db().getDbfile().getAbsolutePath();
		String pathToDb;
		
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option help = new Option("help", "print this message");
		Option v = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option port = OptionBuilder.withArgName("port")
				.hasArg()
				.withDescription("[optional] port number. default is "+defaultPortNumber)
//				.isRequired()
				.create("port");
		
		Option dbfile = OptionBuilder.withArgName("dbfile")
				.hasArg()
				.withDescription("[optional] dbfile. default is "+defaultPathToDb)
//				.isRequired()
				.create("dbfile");
				
		Option stop = OptionBuilder.withArgName("stop")
				.withDescription("[optional] stops the server.")
//				.isRequired()
				.create("stop");
				
		Option restart = OptionBuilder.withArgName("restart")
				.withDescription("[optional] restarts the server.")
//				.isRequired()
				.create("restart");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( port );
		options.addOption( dbfile );
		options.addOption( stop );
		options.addOption( restart );
		
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
			formatter.printHelp("checkout", options);
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

		if (line.hasOption("dbfile"))
		{
			File file = new File(line.getOptionValue("dbfile"));
			if (file.exists())
			{
				db.setDbfile(file);
			}
			else
			{
				System.err.println("file does not exist: "+file.getAbsolutePath());
			}
		}
		else
		{
			pathToDb = defaultPathToDb;
		}
		
		if (line.hasOption("port"))
		{
			portNumber = Integer.parseInt(line.getOptionValue("port"));
		}
		else
		{
			portNumber = defaultPortNumber;
		}
		
		if ( (line.hasOption("stop")) || (line.hasOption("restart")) )
		{
			Socket server;
			try
			{
				server = new Socket("localhost", defaultPortNumber);
				OutputStream out = server.getOutputStream();
				InputStream in = server.getInputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(out);
				ObjectInputStream  objectIn  = new ObjectInputStream(in);
				objectOut.writeObject("stop");
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ConnectException e)
			{
				System.out.println("server is already stopped.");
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if ( line.hasOption("restart") || !(line.hasOption("stop")) )
		{
			ConcurrentServer server = new ConcurrentServer(portNumber);
		}
	}
}
