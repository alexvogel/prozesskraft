package de.prozesskraft.pkraft;

//import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;

public class Waitinstance
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static Ini ini;
	static long startInMillis = System.currentTimeMillis();
	
	/*----------------------------
	  constructors
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{

		/*----------------------------
		  get options from ini-file
		----------------------------*/
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Waitinstance.class) + "/" + "../etc/pkraft-waitinstance.ini");

		if (inifile.exists())
		{
			try
			{
				ini = new Ini(inifile);
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
		else
		{
			System.err.println("ini file does not exist: "+inifile.getAbsolutePath());
			System.exit(1);
		}
		
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option ohelp = new Option("help", "print this message");
		Option ov = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option oinstance = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory if no -scandir] instance file (process.pmb) that this program will wait till its status is 'error' or 'finished'")
//				.isRequired()
				.create("instance");
		
		Option oscandir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[mandatory if no -instance] directory tree with instances (process.pmb). the first instance found will be tracked.")
//				.isRequired()
				.create("scandir");
		
		Option omaxrun = OptionBuilder.withArgName("INTEGER")
				.hasArg()
				.withDescription("[optional, default: 4320] time period (in minutes, default: 3 days) this program waits till it aborts further waiting.")
//				.isRequired()
				.create("maxrun");

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( oinstance );
		options.addOption( oscandir );
		options.addOption( omaxrun );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		// parse the command line arguments
		commandline = parser.parse( options,  args );
		
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( commandline.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("waitinstance", options);
			System.exit(0);
		}

		if ( commandline.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@caegroup.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		Integer maxrun = new Integer(4320);
		String pathInstance = null;
		String pathScandir = null;

		// instance & scandir
		if ( !( commandline.hasOption("instance")) &&  !( commandline.hasOption("scandir")) )
		{
			System.err.println("one of the options -instance/-scandir is mandatory");
			exiter();
		}
		else if ( ( commandline.hasOption("instance")) &&  ( commandline.hasOption("scandir")) )
		{
			System.err.println("both options -instance/-scandir are not allowed");
			exiter();
		}
		else if(commandline.hasOption("instance"))
		{
			pathInstance = commandline.getOptionValue("instance");
		}
		else if(commandline.hasOption("scandir"))
		{
			pathScandir = commandline.getOptionValue("scandir");
		}
		
		// maxrun
		if( commandline.hasOption("maxrun"))
		{
			maxrun = new Integer(commandline.getOptionValue("maxrun"));
		}
		/*----------------------------
		  die lizenz ueberpruefen und ggf abbrechen
		----------------------------*/

		// check for valid license
		ArrayList<String> allPortAtHost = new ArrayList<String>();
		allPortAtHost.add(ini.get("license-server", "license-server-1"));
		allPortAtHost.add(ini.get("license-server", "license-server-2"));
		allPortAtHost.add(ini.get("license-server", "license-server-3"));

		MyLicense lic = new MyLicense(allPortAtHost, "1", "user-edition", "0.1");

		// lizenz-logging ausgeben
		for(String actLine : (ArrayList<String>) lic.getLog())
		{
			System.err.println(actLine);
		}

		// abbruch, wenn lizenz nicht valide
		if (!lic.isValid())
		{
			System.exit(1);
		}

		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		
		// scannen nach dem ersten process.pmb 
		if((pathScandir != null) && (pathInstance == null))
		{
			String[] allBinariesOfScanDir = getProcessBinaries(pathScandir);
			
			if(allBinariesOfScanDir.length == 0)
			{
				System.err.println("no instance (process.pmb) found in directory tree "+pathScandir);
				exiter();
			}
			else
			{
				pathInstance = allBinariesOfScanDir[0];
				System.err.println("found instance: "+pathInstance);
			}
		}
		
		// ueberpruefen ob instance file existiert
		java.io.File fileInstance = new java.io.File(pathInstance);

		if(!fileInstance.exists())
		{
			System.err.println("instance file does not exist: " + fileInstance.getAbsolutePath() );
			exiter();
		}

		if(!fileInstance.isFile())
		{
			System.err.println("instance file is not a file: " + fileInstance.getAbsolutePath() );
			exiter();
		}

		// zeitpunkt wenn spaetestens beendet werden soll
		long runTill = System.currentTimeMillis() + (maxrun * 60 * 1000);

		// logging
		System.err.println("waiting for instance: " + fileInstance.getAbsolutePath() );
		System.err.println("checking its status every 5 minutes");
		System.err.println("now is: " + new Timestamp(startInMillis).toString());
		System.err.println("maxrun till: " + new Timestamp(runTill).toString());
		
		// instanz einlesen
		Process p1 = new Process();
		p1.setInfilebinary(fileInstance.getAbsolutePath());
		Process p2 = p1.readBinary();
		
		// schleife, die prozess einliest und ueberprueft ob er noch laeuft
		while( ! (p2.getStatus().equals("error") || p2.getStatus().equals("finished")) )
		{
			// logging
			System.err.println(new Timestamp(System.currentTimeMillis()) + " instance status: " + p2.getStatus());

			// 5 minuten schlafen: 300000 millis
			try {
				Thread.sleep(300000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// ist die maximale laufzeit von this erreicht, dann soll beendet werden (3 tage)
			if(System.currentTimeMillis() > runTill)
			{
				System.err.println("exiting because of maxrun. now is: " + new Timestamp(System.currentTimeMillis()));
				System.exit(2);
			}

			// den prozess frisch einlesen
			p2 = p1.readBinary();
		}
		
		System.err.println("exiting because instance status is: " + p2.getStatus());
		System.err.println("now is: " + new Timestamp(System.currentTimeMillis()).toString());
		System.exit(0);
		
	}
	
	/**
	 * ermittelt alle process binaries innerhalb eines directory baumes
	 * @param pathScandir
	 * @return
	 */
	private static String[] getProcessBinaries(String pathScandir)
	{
		final ArrayList<String> allProcessBinaries = new ArrayList<String>();

		// den directory-baum durchgehen und fuer jeden eintrag ein entity erstellen
		try {
			Files.walkFileTree(Paths.get(pathScandir), new FileVisitor<Path>()
			{
				// called after a directory visit is complete
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}

				// called before a directory visit
				public FileVisitResult preVisitDirectory(Path walkingDir, BasicFileAttributes attrs) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}

				// called for each file visited. the basic file attributes of the file are also available
				public FileVisitResult visitFile(Path walkingFile, BasicFileAttributes attrs) throws IOException
				{
					// ist es ein process.pmb file?
					if(walkingFile.endsWith("process.pmb"))
					{
						allProcessBinaries.add(new java.io.File(walkingFile.toString()).getAbsolutePath());
					}
					
					return FileVisitResult.CONTINUE;
				}
				
				// called for each file if the visit failed
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return allProcessBinaries.toArray(new String[allProcessBinaries.size()]);
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
