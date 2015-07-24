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
import de.prozesskraft.commons.WhereAmI;

//import de.prozesskraft.pradar.*;

public class Checkin
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
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Checkin.class) + "/" + "../etc/pradar-checkin.ini");
		
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
//		Option domain = OptionBuilder.withArgName("domain")
//				.hasArg()
//				.withDescription("[mandatory] domain")
////				.isRequired()
//				.create("domain");
		
		Option process = OptionBuilder.withArgName("process")
				.hasArg()
				.withDescription("[mandatory] process")
//				.isRequired()
				.create("process");
		
		Option processversion = OptionBuilder.withArgName("processversion")
				.hasArg()
				.withDescription("[optional] processversion")
//				.isRequired()
				.create("processversion");
		
		Option host = OptionBuilder.withArgName("host")
				.hasArg()
				.withDescription("[optional] hostname the processinstance is running")
//				.isRequired()
				.create("host");
		
		Option id = OptionBuilder.withArgName("id")
				.hasArg()
				.withDescription("[optional] unique id over all instances of all possible processes")
				.create("id");
		
		Option id2 = OptionBuilder.withArgName("id2")
				.hasArg()
				.withDescription("[optional] identification tag for users")
				.create("id2");
		
		Option pid = OptionBuilder.withArgName("pid")
				.hasArg()
				.withDescription("[optional] pid of the program that symbolizes whether process is still running")
				.create("pid");
		
		Option parentid = OptionBuilder.withArgName("parentid")
				.hasArg()
				.withDescription("[optional] id of the parent process")
				.create("parentid");
		
		Option user = OptionBuilder.withArgName("user")
				.hasArg()
				.withDescription("[optional] owner of processinstance")
//				.isRequired()
				.create("user");
				
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
		options.addOption( processversion );
		options.addOption( id );
		options.addOption( id2 );
		options.addOption( pid );
		options.addOption( parentid );
		options.addOption( host );
		options.addOption( user );
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
		  die eigentliche business logic
		----------------------------*/
		
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

		if (line.hasOption("id"))
		{
			entity.setId(line.getOptionValue("id"));
		}
		else
		{
//			Random generator = new Random();
			entity.genId();
		}

		if (line.hasOption("processversion"))
		{
			entity.setVersion(line.getOptionValue("processversion"));
		}
		else
		{
			entity.setVersion("");
		}

		if (line.hasOption("id2"))
		{
			entity.setId2(line.getOptionValue("id2"));
		}
		else
		{
//			Random generator = new Random();
			entity.setId2("noname");
		}

		if (line.hasOption("pid"))
		{
			entity.setPid(line.getOptionValue("pid"));
		}
		else
		{
//			Random generator = new Random();
			entity.setPid("");
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
				objectOut.writeObject("checkin");
				objectOut.writeObject(entity);

				// nachricht wurde erfolgreich an server gesendet --> schleife beenden
				pradar_server_not_found = false;
				
				System.out.println("<id>"+entity.getId()+"<id>");
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
