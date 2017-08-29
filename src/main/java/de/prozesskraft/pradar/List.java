package de.prozesskraft.pradar;

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
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
//import org.apache.xerces.impl.xpath.regex.ParseException;




import de.prozesskraft.pradar.Entity;
import de.prozesskraft.commons.*;
import de.prozesskraft.pradar.*;

public class List
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
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(List.class) + "/" + "../etc/pradar-list.ini");
		
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
		
		Option id2 = OptionBuilder.withArgName("id2")
				.hasArg()
				.withDescription("[optional] filter for field 'id2'")
				.create("id2");
		
		Option parentid = OptionBuilder.withArgName("parentid")
				.hasArg()
				.withDescription("[optional] filter for field 'parentid'")
				.create("parentid");
		
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
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( process );
		options.addOption( id );
		options.addOption( id2 );
		options.addOption( parentid );
		options.addOption( host );
		options.addOption( user );
		options.addOption( exitcode );
		options.addOption( active );
		
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
		  die eigentliche business logic
		----------------------------*/
		
		Entity entity = new Entity();

		// definition des filters fuer id
		if (line.hasOption("id"))
		{
			entity.setId(line.getOptionValue("id"));
		}
		else
		{
			entity.setId("");
		}

		// definition des filters fuer id2
		if (line.hasOption("id2"))
		{
			entity.setId2(line.getOptionValue("id2"));
		}
		else
		{
			entity.setId2("");
		}

		// definition des filters fuer parentid
		if (line.hasOption("parentid"))
		{
			entity.setParentid(line.getOptionValue("parentid"));
		}
		else
		{
			entity.setParentid("");
		}

		// definition des filters fuer process
		if (line.hasOption("process"))
		{
			entity.setProcess(line.getOptionValue("process"));
		}
		else
		{
			entity.setProcess("");
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
		else
		{
			entity.setExitcode("");
		}

		// definition des filters fuer host
		if (line.hasOption("host"))
		{
			entity.setHost(line.getOptionValue("host"));
		}
		else
		{
			entity.setHost("");
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
				
				// Objekte zum server uebertragen
				objectOut.writeObject("list");
				objectOut.writeObject(entity);

				// Antwort vom Server lesen. (Liste bereits Druckfertig aufbereitet)
				try
				{
					ArrayList<String> list = (ArrayList<String>) objectIn.readObject();
					System.out.println("Anzahl der Zeilen: "+list.size());
					Iterator<String> iterstring = list.iterator();
					while (iterstring.hasNext())
					{
						System.out.println(iterstring.next());
					}
				} catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
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
