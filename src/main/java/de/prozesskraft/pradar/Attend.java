package de.prozesskraft.pradar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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

import de.prozesskraft.pkraft.Log;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pradar.Entity;
import de.prozesskraft.commons.WhereAmI;

//import de.prozesskraft.pradar.*;

public class Attend
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
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Attend.class) + "/" + "../etc/pradar-attend.ini");
		
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
				.withDescription("[optional] process instance file you want to enter / update in pradar")
//				.isRequired()
				.create("instance");
		
		Option odir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[optional] all process instance files that are found in this directory (recursively), will be attended")
//				.isRequired()
				.create("dir");
		
		Option owait = OptionBuilder.withArgName("INTEGER")
				.hasArg()
				.withDescription("[optional] amount of seconds to wait before attend. sometimes the processBinary needs some time to appear.")
//				.isRequired()
				.create("wait");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( oinstance );
		options.addOption( odir );
		options.addOption( owait );
		
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
			formatter.printHelp("attend", options);
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
		if ( !( commandline.hasOption("instance")) && !( commandline.hasOption("dir")))
		{
			System.err.println("at least one of the options -instance, -dir is mandatory");
			exiter();
		}

		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		
		// soll gewartet werden, wird gewartet
		if(commandline.hasOption("wait"))
		{
			Integer wait = Integer.parseInt(commandline.getOptionValue("wait"));
			System.err.println("waiting "+commandline.getOptionValue("wait")+" seconds before performing attend.");
			
			try {
				Thread.sleep(1000 * wait);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// hier sollen alle processBinaries  (aus -instance und -dir) gesammelt werden
		ArrayList<java.io.File> allProcessBinaries = new ArrayList<java.io.File>();
		
		// alle -instance eintraege uebernehmen
		if( commandline.hasOption("instance"))
		{
			for(String actInstance : commandline.getOptionValues("instance"))
			{
				// ist die datei vorhanden?
				java.io.File fileProcessBinary = new java.io.File(actInstance);
				if(!fileProcessBinary.exists())
				{
					System.err.println("error: process instance file does not exist: " + actInstance);
					exiter();
				}
				else
				{
					allProcessBinaries.add(fileProcessBinary);
				}
			}
		}

		// falls ein directory angegeben wurde, soll en alle process.pmb files darin aufgesammelt werden
		if( commandline.hasOption("dir"))
		{
			java.io.File dirFile = new java.io.File(commandline.getOptionValue("dir"));

			// CHECKS
			if(!dirFile.exists())
			{
				System.err.println("error: directory does not exist: " + dirFile.getAbsolutePath());
				exiter();
			}
			else if(!dirFile.isDirectory())
			{
				System.err.println("error: is not a directory: " + dirFile.getAbsolutePath());
				exiter();
			}
			
			ArrayList<java.io.File> directoriesToSearch = new ArrayList<java.io.File>();
			directoriesToSearch.add(dirFile);
			
			while(!directoriesToSearch.isEmpty())
			{
				// falls unterverzeichnisse gefunden werden, werden sie hier aufgesammelt
				ArrayList<java.io.File> newDirectoriesToSearch = new ArrayList<java.io.File>();
				
				// alle directories durchgehen
				for(java.io.File actDir : directoriesToSearch)
				{
					// alle files ueberpruefen ob sie auf process.pmb enden
					
					// alle process.pmb in unterverzeichnissen finden
					for(java.io.File actFile : actDir.listFiles())
					{
						if(actFile.isDirectory())
						{
							newDirectoriesToSearch.add(actFile);
						}
						else if(actFile.isFile() && actFile.getName().equals("process.pmb"))
						{
							System.err.println("found process.pmb: " + actFile.getAbsolutePath());
							allProcessBinaries.add(actFile);
						}
					}
				}

				// unterdirectories sollen weiterdurchsucht werden
				directoriesToSearch = newDirectoriesToSearch;
			}
		}
		
		// fuer alle processBinaries (aus -instance und -dir) einen attend durchfuehren
		for(java.io.File fileProcessBinary : allProcessBinaries)
		{
			
			// instanz einlesen
			Process p1 = new Process();
			p1.setInfilebinary(fileProcessBinary.getAbsolutePath());
			Process process = p1.readBinary();
			process.setOutfilebinary(fileProcessBinary.getAbsolutePath());
	
			// den status evtl. vorhandener subprocesses refreshen
			try {
				process.refreshSubprocessStatus();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				System.err.println("error: could not refresh status of subprocesses. " + e2.getMessage());
				e2.printStackTrace();
			}
			
			// 1) .status file schreiben
			// Filewriter initialisieren
			FileWriter logWriter;
			try
			{
				logWriter = new FileWriter(process.getRootdir() + "/.status", false);
				logWriter.write(process.getStatus());
				logWriter.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.err.println("error: cannot update .status file " + process.getRootdir() + "/.status");
			}
			
			// 2) daten einsammeln und an den pradarserver senden
			// ein pradar entity erstellen
			Entity entity = new Entity();
	
			// die felder des pradar entities mit den daten befuellen
			entity.setProcess(process.getName());
			entity.setId(process.getId());
			entity.setVersion(process.getVersion());
			entity.setId2(process.getId2());
			entity.setParentid(process.getParentid());
	
			entity.setSerialVersionUID(""+Process.getSerialversionuid());
			
			// setzen des hosts vom system
			try
			{
				InetAddress addr = InetAddress.getLocalHost();
				entity.setHost(addr.getHostName());
			} catch (UnknownHostException e)
			{
				// mache nichts, dann greift der default 'HAL'
			}
	
			// die zeiten aus dem process setzen
			entity.setCheckin(process.getTimeOfProcessCreated());
	
			// die resource setzen
			entity.setResource(process.getRootdir() + "/process.pmb");
	
			// setzen des user vom system
			entity.setUser(System.getProperty("user.name"));
	
			// setzen von active auf 'true'
			if(process.getStatus().equals("working"))
			{
				entity.setActive("true");
			}
			else
			{
				entity.setActive("false");
				entity.setCheckout(process.getTouchInMillis());
			}
	
			// stepcounts setzen
			entity.setStepcount("" + process.getStep().size());
			entity.setStepcountcompleted("" + process.getStepFinishedOrCanceled().size());
	
			// exitcode setzen
			if(process.getStatus().equals("finished"))
			{
				entity.setExitcode("0");
			}
			else if(process.getStatus().equals("working") || process.getStatus().equals("waiting") || process.getStatus().equals("paused"))
			{
				// belassen bei ""
			}
			else
			{
				entity.setExitcode(process.getStatus());
			}
	
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
					objectOut.writeObject("attend");
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
	
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
