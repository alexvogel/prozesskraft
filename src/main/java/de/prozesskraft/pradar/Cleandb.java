package de.prozesskraft.pradar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.prozesskraft.commons.WhereAmI;

public class Cleandb
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	static Ini ini;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
	{

		/*----------------------------
		  get options from ini-file
		----------------------------*/
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Cleandb.class) + "/" + "../etc/pradar-cleandb.ini");
		
		ArrayList<String> pradar_server_list = new ArrayList<String>();
		
		try
		{
			ini = new Ini(inifile);
			for(int x = 1; x <= 5; x++)
			{
				if (ini.get("pradar-server", "pradar-server-"+x) != null )
				{
					pradar_server_list.add(ini.get("pradar-server", "pradar-server-"+x));
				}
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
		
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option oHelp = new Option("help", "print this message");
		Option oV = new Option("v", "prints version and build-date");
		Option oF = new Option("f", "force");
		
		/*----------------------------
		  create argument options
		----------------------------*/
//		Option dbfile = OptionBuilder.withArgName("dbfile")
//				.hasArg()
//				.withDescription("[mandatory] dbfile")
////				.isRequired()
//				.create("dbfile");
//		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( oHelp );
		options.addOption( oV );
		
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
		try
		{
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
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		/*----------------------------
		  die eigentliche business logic
		----------------------------*/

		// einchecken in die DB
		Socket server = null;
		
		boolean pradar_server_not_found = true;
		
		// ueber alle server aus ini-file iterieren und dem ersten den auftrag erteilen
		Iterator<String> iter_pradar_server = pradar_server_list.iterator();
		while(pradar_server_not_found && iter_pradar_server.hasNext())
		{
			String port_and_machine_as_string = iter_pradar_server.next();
			String [] port_and_machine = port_and_machine_as_string.split("@");

			int portNumber = Integer.parseInt(port_and_machine[0]);
			String machineName = port_and_machine[1];
			System.err.println("trying pradar-server "+portNumber+"@"+machineName);
			try
			{
				// socket einrichten und Out/Input-Streams setzen
				server = new Socket(machineName, portNumber);
				OutputStream out = server.getOutputStream();
				InputStream in = server.getInputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(out);
				ObjectInputStream  objectIn  = new ObjectInputStream(in);

				objectOut.writeObject("cleandb");

				// nachricht wurde erfolgreich an server gesendet --> schleife beenden
				pradar_server_not_found = false;
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				System.err.println("unknown host "+machineName+" (UnknownHostException)");
			}
			catch (ConnectException e)
			{
				System.err.println("no pradar-server found at "+portNumber+"@"+machineName);
	//			e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (pradar_server_not_found)
		{
			System.out.println("no pradar-server found.");
		}
	}
}
