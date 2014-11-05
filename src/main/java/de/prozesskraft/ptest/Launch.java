package de.prozesskraft.ptest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.caegroup.commons.*;
import de.prozesskraft.ptest.*;

import java.nio.file.attribute.*;

public class Launch
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String web = "www.prozesskraft.de";
	static String author = "alexander.vogel@prozesskraft.de";
	static String version = "[% version %]";
	static String date = "[% date %]";
	static Ini ini;
	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
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
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Launch.class) + "/" + "../etc/ptest-launch.ini");

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
		Option ospl = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[mandatory] directory with sample input data")
//				.isRequired()
				.create("spl");

		Option oinstancedir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[mandatory, default: .] directory where the test will be performed")
//				.isRequired()
				.create("instancedir");

		Option ocall = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory, default: random call in spl-directory] file with call-string")
//				.isRequired()
				.create("call");

		Option oaltapp = OptionBuilder.withArgName("STRING")
				.hasArg()
				.withDescription("[optional] alternative app. this String replaces the first line of the .call-file.")
//				.isRequired()
				.create("altapp");

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();

		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( ospl );
		options.addOption( oinstancedir );
		options.addOption( ocall );
		options.addOption( oaltapp );

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
			exiter();
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( commandline.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("launch", options);
			System.exit(0);
		}

		else if ( commandline.hasOption("v"))
		{
			System.out.println("web:     "+web);
			System.out.println("author: "+author);
			System.out.println("version:"+version);
			System.out.println("date:     "+date);
			System.exit(0);
		}

		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		boolean error = false;
		String spl = null;
		String instancedir = null;
		String call = null;
		String altapp = null;

		// spl initialisieren
		if ( commandline.hasOption("spl") )
		{
			spl = commandline.getOptionValue("spl");
		}
		else
		{
			System.err.println("option -spl is mandatory");
			error = true;
		}

		// instancedir initialisieren
		if ( commandline.hasOption("instancedir") )
		{
			instancedir = commandline.getOptionValue("instancedir");
		}
		else
		{
			instancedir = System.getProperty("user.dir");
		}

		// call initialisieren
		if ( commandline.hasOption("call") )
		{
			call = commandline.getOptionValue("call");
		}

		// altapp initialisieren
		if ( commandline.hasOption("altapp") )
		{
			altapp = commandline.getOptionValue("altapp");
		}
		
		// wenn fehler, dann exit
		if(error)
		{
			exiter();
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

		// das erste spl-objekt geben lassen
		Spl actSpl =new Splset(spl).getSpl().get(0) ;

		// den call, result und altapp ueberschreiben
		actSpl.setName("default");
		
		if(call != null)
		{
			actSpl.setCall(new java.io.File(call));
		}
		if(actSpl.getCall() == null)
		{
			System.err.println("error: no call information found");
			System.exit(1);
		}
		
		if(altapp != null)
		{
			actSpl.setAltapp(altapp);
		}

		actSpl.setResult(null);
		
		// das instancedir erstellen
		java.io.File actSplInstanceDir = new java.io.File(instancedir);
		System.err.println("info: creating directory " + actSplInstanceDir.getCanonicalPath());
		actSplInstanceDir.mkdirs();
		
		// die beispieldaten in das instancedir kopieren
		for(java.io.File actInputFile : actSpl.getInput())
		{
			// namen des targetfiles festlegen
			java.io.File targetFile = new java.io.File(actSplInstanceDir.getCanonicalPath() + "/" + actInputFile.getName());
			
			// input file in das instancedir kopieren
			Files.copy(actInputFile.toPath(), targetFile.toPath());
		}

		// das logfile des Syscalls (zum debuggen des programms "process syscall" gedacht)
		String AbsLogSyscallWrapper = actSplInstanceDir.getCanonicalPath()+"/.log";

		try
		{
			// den Aufrufstring fuer die externe App (process syscall --version 0.6.0)) splitten
			// beim aufruf muss das erste argument im path zu finden sein, sonst gibt die fehlermeldung 'no such file or directory'
			ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(ini.get("apps", "process-syscall").split(" ")));

			// die sonstigen argumente hinzufuegen
			processSyscallWithArgs.add("-call");
			processSyscallWithArgs.add(actSpl.getCallAsString());
//			processSyscallWithArgs.add("\""+call+"\"");
			processSyscallWithArgs.add("-stdout");
			processSyscallWithArgs.add(instancedir + "/" + ".stdout.txt");
			processSyscallWithArgs.add("-stderr");
			processSyscallWithArgs.add(instancedir + "/" + ".stderr.txt");
			processSyscallWithArgs.add("-pid");
			processSyscallWithArgs.add(instancedir + "/" + ".pid");
			processSyscallWithArgs.add("-mylog");
			processSyscallWithArgs.add(AbsLogSyscallWrapper);
			processSyscallWithArgs.add("-maxrun");
			processSyscallWithArgs.add(""+3000);

			// erstellen prozessbuilder
			ProcessBuilder pb = new ProcessBuilder(processSyscallWithArgs);

			// erweitern des PATHs um den prozesseigenen path
//			Map<String,String> env = pb.environment();
//			String path = env.get("PATH");
//			log("debug", "$PATH="+path);
//			path = this.parent.getAbsPath()+":"+path;
//			env.put("PATH", path);
//			log("info", "path: "+path);
			
			// setzen der aktuellen directory (in der syscall ausgefuehrt werden soll)
			java.io.File directory = new java.io.File(instancedir);
			System.err.println("info: setting execution directory to: "+directory.getCanonicalPath());
			pb.directory(directory);

			// zum debuggen ein paar ausgaben
//			java.lang.Process p1 = Runtime.getRuntime().exec("date >> ~/tmp.debug.work.txt");
//			p1.waitFor();
//			java.lang.Process p2 = Runtime.getRuntime().exec("ls -la "+this.getParent().getAbsdir()+" >> ~/tmp.debug.work.txt");
//			p2.waitFor();
//			java.lang.Process pro = Runtime.getRuntime().exec("nautilus");
//			java.lang.Process superpro = Runtime.getRuntime().exec(processSyscallWithArgs.toArray(new String[processSyscallWithArgs.size()]));
//			p3.waitFor();
			
			System.err.println("info: calling: " + pb.command());

			// starten des prozesses
			java.lang.Process sysproc = pb.start();

//			alternativer aufruf
//			java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(args_for_syscall, " "));
			
//			log("info", "call executed. pid="+sysproc.hashCode());

			// wait 2 seconds for becoming the pid-file visible
			Thread.sleep(2000);
		}
		catch (Exception e2)
		{
			System.err.println("error: " + e2.getMessage());
			System.exit(1);
		}
		
	}

	private static void exiter()
	{
		System.err.println("try -help for help.");
		System.exit(1);
	}


}
