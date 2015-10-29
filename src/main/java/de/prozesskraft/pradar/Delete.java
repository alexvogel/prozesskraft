package de.prozesskraft.pradar;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
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
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
//import org.apache.xerces.impl.xpath.regex.ParseException;

import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pradar.Entity;
import de.prozesskraft.commons.WhereAmI;

//import de.prozesskraft.pradar.*;

public class Delete
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static Ini ini;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
	{

		/*----------------------------
		  get options from ini-file
		----------------------------*/
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Delete.class) + "/" + "../etc/pradar-delete.ini");
		
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
		
		Option oinstance = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] process instance file you want to enter / update in pradar")
//				.isRequired()
				.create("instance");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();

		options.addOption( help );
		options.addOption( v );
		options.addOption( oinstance );

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
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( commandline.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("delete", options);
			System.exit(0);
		}
		
		if ( commandline.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@prozesskraft.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}

		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		if ( !( commandline.hasOption("instance")))
		{
			System.err.println("option -instance is mandatory");
			exiter();
		}

		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		
		// fuer alle angegebenen instanzen einen delete durchfuehren
		for(String pathProcessBinary : commandline.getOptionValues("instance"))
		{
			// ist die datei vorhanden?
			java.io.File fileProcessBinary = new java.io.File(pathProcessBinary);
			if(!fileProcessBinary.exists())
			{
				System.err.println("error: process instance file does not exist: " + pathProcessBinary);
				exiter();
			}

			// instanz einlesen
			Process p1 = new Process();
			p1.setInfilebinary(fileProcessBinary.getAbsolutePath());
			Process process = p1.readBinary();
			process.setOutfilebinary(fileProcessBinary.getAbsolutePath());

			// 2) daten einsammeln und an den pradarserver senden
			// ein pradar entity erstellen
			Entity entity = new Entity();
	
			// die felder des pradar entities mit den daten befuellen
			entity.setProcess(process.getName());
			entity.setId(process.getId());
			entity.setVersion(process.getVersion());
			entity.setId2(process.getId2());
			entity.setParentid(process.getParentid());

			// setzen des hosts vom system
			try
			{
				InetAddress addr = InetAddress.getLocalHost();
				entity.setHost(addr.getHostName());
			} catch (UnknownHostException e)
			{
				// mache nichts, dann greift der default 'HAL'
			}
	
			// die resource setzen
			entity.setResource(process.getRootdir() + "/process.pmb");
	
			// setzen des user vom system
			entity.setUser(System.getProperty("user.name"));
	
			// debugging
	//		entity.print();
			
			// eintragen in die DB
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
					objectOut.writeObject("delete");
					objectOut.writeObject(entity);
	
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
	
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
