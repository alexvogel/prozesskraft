package de.caegroup.pradar;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import de.caegroup.commons.WhereAmI;
import de.caegroup.network.ConcurrentServer;
import org.ini4j.*;

public class Server
{

	/*----------------------------
	  fields
	----------------------------*/
	static CommandLine line;
	static Ini ini;
	static int portNumber;
	static File dbFile;
	static String sshIdRelPath;

	/*----------------------------
	  constructors
	----------------------------*/
	/*----------------------------
	  main
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
	{
		
		Server tmp = new Server();
		dbFile = WhereAmI.getDefaultDbfile(tmp.getClass());
		sshIdRelPath = WhereAmI.getDefaultSshIdRsa();
		portNumber = WhereAmI.getDefaultPortNumber();
		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = WhereAmI.getDefaultInifile(tmp.getClass());
		

		if (inifile.exists())
		{
			try
			{
				ini = new Ini(inifile);
				if (ini.get("pradar-server", "port") != null )
				{
					portNumber = Integer.parseInt(ini.get("pradar-server", "port"));
				}
				if (ini.get("pradar-db", "pradar-db-path") != null )
				{
					dbFile = new File(ini.get("pradar-db", "pradar-db"));
				}
				if (ini.get("ssh", "ssh-id") != null )
				{
					sshIdRelPath = ini.get("ssh", "ssh-id");
				}
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
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option oHelp = new Option("help", "print this message");
		Option oV = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option oPort = OptionBuilder.withArgName("port")
				.hasArg()
				.withDescription("[optional] port number. default is "+portNumber)
//				.isRequired()
				.create("port");
		
		Option oDbfile = OptionBuilder.withArgName("dbfile")
				.hasArg()
				.withDescription("[optional] dbfile")
//				.isRequired()
				.create("dbfile");
		
		Option oStop = OptionBuilder.withArgName("stop")
				.withDescription("[optional] stops the server.")
//				.isRequired()
				.create("stop");
				
		Option oRestart = OptionBuilder.withArgName("restart")
				.withDescription("[optional] restarts the server.")
//				.isRequired()
				.create("restart");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( oHelp );
		options.addOption( oV );
		options.addOption( oPort );
		options.addOption( oDbfile );
		options.addOption( oStop );
		options.addOption( oRestart );
		
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
			formatter.printHelp("server", options);
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
		  die eigentliche business logic
		----------------------------*/
		
		if (line.hasOption("port"))
		{
			portNumber = Integer.parseInt(line.getOptionValue("port"));
		}
		else if ((ini.get("pradar-server", "port") != null ))
		{
			portNumber = Integer.parseInt(ini.get("pradar-server", "port"));
		}
		else
		{
			// verwende den schon gueltigen default
		}
		
		if (line.hasOption("dbfile"))
		{
			dbFile = new File((line.getOptionValue("dbfile")));
		}
		else if ((ini.get("pradar-db", "pradar-db-path") != null ))
		{
			dbFile = new File((ini.get("pradar-db", "pradar-db-path")));
		}
		else
		{
			// verwende den default im ConcurrentServer (ServerSocket)
		}
		
		if ( (line.hasOption("stop")) || (line.hasOption("restart")) )
		{
			try
			{
				System.out.println("stopping pradar-server.");
				Socket server = new Socket("localhost", portNumber);
				OutputStream out = server.getOutputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(out);
				objectOut.writeObject("stop");
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				server.close();
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ConnectException e)
			{
				System.out.println("pradar-server is already stopped.");
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if ( line.hasOption("restart") || !(line.hasOption("stop")) )
		{
			System.out.println("starting pradar-server to listen on port "+portNumber);
			ConcurrentServer server = new ConcurrentServer(sshIdRelPath, portNumber, dbFile);
		}
	}
}
