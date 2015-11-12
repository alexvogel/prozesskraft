package de.prozesskraft.ptest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import de.prozesskraft.commons.*;
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

		Option oaddopt = OptionBuilder.withArgName("STRING")
				.hasArg()
				.withDescription("[optional] add an option to the call.")
//				.isRequired()
				.create("addopt");

		Option onolaunch = new Option("nolaunch", "only create instance directory, copy all spl files, but do NOT launch the process");

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
		options.addOption( oaddopt );
		options.addOption( onolaunch );

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
		ArrayList<String> addopt = new ArrayList<String>();

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
		
		// addopt initialisieren
		if ( commandline.hasOption("addopt") )
		{
			for(String actString : commandline.getOptionValues("addopt"))
			{
				addopt.add(actString);
			}
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

		if(addopt.size() > 0)
		{
			actSpl.setAddopt(addopt);
		}
		
		actSpl.setResult(null);

		// das instancedir erstellen
		java.io.File actSplInstanceDir = new java.io.File(instancedir);
		System.err.println("info: creating directory " + actSplInstanceDir.getCanonicalPath());
		actSplInstanceDir.mkdirs();

		// Inputdaten in das InstanceDir exportieren
		actSpl.exportInput(actSplInstanceDir);

		// exit, wenn --nolaunch
		if(commandline.hasOption("nolaunch"))
		{
			System.err.println("info: exiting, because of -nolaunch");
			System.exit(0);
		}
		
		// das logfile des Syscalls (zum debuggen des programms "process syscall" gedacht)
		String AbsLogSyscallWrapper = actSplInstanceDir.getCanonicalPath()+"/.log";
		String AbsStdout = actSplInstanceDir.getCanonicalPath()+"/.stdout.txt";
		String AbsStderr = actSplInstanceDir.getCanonicalPath()+"/.stderr.txt";
		String AbsPid = actSplInstanceDir.getCanonicalPath()+"/.pid";
		
		// beim starten von syscall werden parameter mit whitespaces an diesen auseinandergeschnitten und der nachfolgende aufruf schlaeft fehl
		// deshalb sollen whitespaces durch eine 'zeichensequenz' ersetzt werden
		// syscall ersetzt die zeichensequenz wieder zurueck in ein " "
		ArrayList<String> callFuerSyscall = actSpl.getCallAsArrayList();
		ArrayList<String> callFuerSyscallMitTrennzeichen = new ArrayList<String>();
		for(String actString : callFuerSyscall)
		{
			callFuerSyscallMitTrennzeichen.add(actString.replaceAll("\\s+", "%WHITESPACE%"));
		}
		
		try
		{
			// den Aufrufstring fuer die externe App (process syscall --version 0.6.0)) splitten
			// beim aufruf muss das erste argument im path zu finden sein, sonst gibt die fehlermeldung 'no such file or directory'
			ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(ini.get("apps", "pkraft-syscall").split(" ")));

			// die sonstigen argumente hinzufuegen
			processSyscallWithArgs.add("-call");
			processSyscallWithArgs.add(String.join(" ", callFuerSyscallMitTrennzeichen));
//			processSyscallWithArgs.add("\""+call+"\"");
			processSyscallWithArgs.add("-stdout");
			processSyscallWithArgs.add(AbsStdout);
			processSyscallWithArgs.add("-stderr");
			processSyscallWithArgs.add(AbsStderr);
			processSyscallWithArgs.add("-pid");
			processSyscallWithArgs.add(AbsPid);
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

			// einfangen der stdout- und stderr des subprozesses
			InputStream is_stdout = sysproc.getInputStream();
			InputStream is_stderr = sysproc.getErrorStream();
			
			// Send your InputStream to an InputStreamReader:
			InputStreamReader isr_stdout = new InputStreamReader(is_stdout);
			InputStreamReader isr_stderr = new InputStreamReader(is_stderr);

			// That needs to go to a BufferedReader:
			BufferedReader br_stdout = new BufferedReader(isr_stdout);
			BufferedReader br_stderr = new BufferedReader(isr_stderr);
			
//			// oeffnen der OutputStreams zu den Ausgabedateien
//			FileWriter fw_stdout = new FileWriter(sStdout);
//			FileWriter fw_stderr = new FileWriter(sStderr);
			
			// zeilenweise in die files schreiben
			String line_out = new String();
			String line_err = new String();

			while(br_stdout.readLine() != null){}
			
//			while (((line_out = br_stdout.readLine()) != null) || ((line_err = br_stderr.readLine()) != null))
//			{
//				if (!(line_out == null))
//				{
//					System.out.println(line_out);
//					System.out.flush();
//				}
//				if (!(line_err == null))
//				{
//					System.err.println(line_err);
//					System.err.flush();
//				}
//			}

			int exitValue = sysproc.waitFor();

//			fw_stdout.close();
//			fw_stderr.close();

			System.err.println("exitvalue: "+exitValue);

			sysproc.destroy();

			System.exit(exitValue);

			
			//			alternativer aufruf
//			java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(args_for_syscall, " "));
			
//			log("info", "call executed. pid="+sysproc.hashCode());

			// wait 2 seconds for becoming the pid-file visible
//			Thread.sleep(2000);
			
//			int exitValue = sysproc.waitFor();
			
//			// der prozess soll bis laengstens
//			if(exitValue != 0)
//			{
//				System.err.println("error: call returned a value indicating an error: "+exitValue);
//			}
//			else
//			{
//				System.err.println("info: call returned value: "+exitValue);
//			}
			
//			System.err.println("info: "+new Date().toString());
//			System.err.println("info: bye");
//
//			sysproc.destroy();
//
//			System.exit(sysproc.exitValue());
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
