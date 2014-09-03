package de.caegroup.process;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class Syscall {

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String author = "alexander.vogel@caegroup.de";
	static String version = "[% version %]";
	static String date = "[% date %]";

	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{
	 
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option ohelp = new Option("help", "print this message");
		Option ov = new Option("v", "prints version and build-date");

		/*----------------------------
		  create argument options
		----------------------------*/
		Option ocall = OptionBuilder.withArgName("STRING")
				.hasArg()
				.withDescription("[mandatory] the full call that should be executed. escape whitespaces or use \"\"")
//				.isRequired()
				.create("call");
		
		Option omylog = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] the executed call will be printed to this file.")
//				.isRequired()
				.create("mylog");
		
		Option ostdout = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] the STDOUT of the executed call will be redirected into this file.")
//				.isRequired()
				.create("stdout");
		
		Option ostderr = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] the STDERR of the executed call will be redirected into this file.")
//				.isRequired()
				.create("stderr");

		Option opid = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] the process-Id of the executed call will be written to this file.")
//				.isRequired()
				.create("pid");

		Option omaxrun = OptionBuilder.withArgName("INTEGER")
				.hasArg()
				.withDescription("[mandatory] after this amount of minutes the run will get terminated..")
//				.isRequired()
				.create("maxrun");

		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( ocall );
		options.addOption( ostdout );
		options.addOption( ostderr );
		options.addOption( opid );
		options.addOption( omylog );
		options.addOption( omaxrun );
		
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
			formatter.printHelp("syscall", options);
			System.exit(0);
		}
		
		else if ( commandline.hasOption("v"))
		{
			System.out.println("web:     www.prozesskraft.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		if ( !( commandline.hasOption("call")) )
		{
			System.err.println("option -call is mandatory.");
			exiter();
		}
		
		if ( !( commandline.hasOption("stdout")) )
		{
			System.err.println("option -stdout is mandatory.");
			exiter();
		}
		
		if ( !( commandline.hasOption("stderr")) )
		{
			System.err.println("option -stderr is mandatory.");
			exiter();
		}
		
		if ( !( commandline.hasOption("pid")) )
		{
			System.err.println("option -pid is mandatory.");
			exiter();
		}
		
		if ( !( commandline.hasOption("mylog")) )
		{
			System.err.println("option -mylog is mandatory.");
			exiter();
		}
		
		if ( !( commandline.hasOption("maxrun")) )
		{
			System.err.println("option -maxrun is mandatory.");
			exiter();
		}
		
		try {

			String sCall = commandline.getOptionValue("call");
			String sLog = commandline.getOptionValue("log");
			String sPid = commandline.getOptionValue("pid");
			String sStdout = commandline.getOptionValue("stdout");
			String sStderr = commandline.getOptionValue("stderr");
			String sMaxrun = commandline.getOptionValue("maxrun");

			
			// startzeit merken
			Date startDate = new Date();
			Date termDate = new Date(startDate.getTime() + Integer.parseInt(sMaxrun)*60*1000);
						
			// Aufruf in das call -logfile schreiben
			PrintWriter writerLog = new PrintWriter(sLog);
			writerLog.println(new Timestamp(System.currentTimeMillis()));
			writerLog.println("process syscall");
			writerLog.println("-call \""+sCall+"\"");
			writerLog.println("-stdout "+sStdout);
			writerLog.println("-stderr "+sStderr);
			writerLog.println("-pid "+sPid);
			writerLog.println("-log "+sLog);
			writerLog.println("-maxrun "+sMaxrun);
			writerLog.println(sCall);
			writerLog.println("------------------------------------------------------");
			writerLog.println("start at               : "+ startDate.toString());
			writerLog.println("will terminate at: "+ termDate.toString());
			writerLog.println("-------------- STDOUT and STDERR----------------");
			writerLog.close();

			// Umleitung von STDOUT und STDERR dieses Scripts in das angegebene logfile
			FileOutputStream logStream = new FileOutputStream(sLog);
			PrintStream logPrintStream = new PrintStream(logStream);
			System.setOut(logPrintStream);
			System.setErr(logPrintStream);

			// Aufruf taetigen
			java.lang.Process sysproc = Runtime.getRuntime().exec(sCall);

			// feststellen der Process-ID des laufenden JavaVM und in die PID-Datei schreiben
			String pid = ManagementFactory.getRuntimeMXBean().getName();

			// da die pid von der ManagementFactory normalerweise die Form "267353@ws11.caegroup" hat
			// soll nur die fuehrende Zahl erfasst werden
			String patt = "(\\d+)";
			Pattern r = Pattern.compile(patt);
			Matcher m = r.matcher(pid);
			PrintWriter writerPid = new PrintWriter(sPid);
			// wenn eine fuehrende zahl gefunden wird, wird diese als pid verwendet
			if (m.find())
			{
				System.out.println("PID WIRD FESTGESTELLT ALS: "+m.group(1));
				writerPid.println(m.group(1));
			}
			// wenn keine fuehrende zahl gefunden wird, wird der gesamte string als pid verwendet
			else
			{
				writerPid.println(pid);
			}
			writerPid.close();
			
			// einfangen der stdout- und stderr
			InputStream is_stdout = sysproc.getInputStream();
			InputStream is_stderr = sysproc.getErrorStream();
			
			// Send your InputStream to an InputStreamReader:
			InputStreamReader isr_stdout = new InputStreamReader(is_stdout);
			InputStreamReader isr_stderr = new InputStreamReader(is_stderr);

			// That needs to go to a BufferedReader:
			BufferedReader br_stdout = new BufferedReader(isr_stdout);
			BufferedReader br_stderr = new BufferedReader(isr_stderr);
			
			// oeffnen der OutputStreams zu den Ausgabedateien
			FileWriter fw_stdout = new FileWriter(sStdout);
			FileWriter fw_stderr = new FileWriter(sStderr);
			
			// zeilenweise in die files schreiben
			String line_out = new String();
			String line_err = new String();


			while ((((line_out = br_stdout.readLine()) != null) && ((line_err = br_stderr.readLine()) != null)) || ((line_err = br_stderr.readLine()) != null))
			{
				if (!(line_out == null))
				{
					System.out.println(line_out);
					fw_stdout.write(line_out);
					fw_stdout.write("\n");
					fw_stdout.flush();
				}
				if (!(line_err == null))
				{
					System.out.println(line_err);
					fw_stderr.write(line_err);
					fw_stderr.write("\n");
					fw_stderr.flush();
				}
			}

//			while ((line_err = br_stderr.readLine()) != null)
//			{
//				System.out.println(line_err);
//				fw_stderr.write(line_err);
//				fw_stderr.write("\n");
//				fw_stdout.flush();
//			}
//			
			
			// der prozess soll bis laengstens
			sysproc.wait(Integer.parseInt(sMaxrun) * 60 *1000);
			
			System.out.println("terminating at "+new Date().toString());
			sysproc.destroy();
			
//			sysproc.waitFor();
			int sysproc_exitvalue = sysproc.exitValue();
			System.out.println("EXITVALUE: "+sysproc_exitvalue);
			
			fw_stderr.write("EXITVALUE: "+sysproc_exitvalue);
			
			fw_stdout.close();
			fw_stderr.close();

			System.exit(sysproc_exitvalue);

		}
		catch (IOException e)
		{

			// I haven't figured out how to trip this yet.
			// Which makes sense. Java doesn't really know
			// if your process failed. That must be determined
			// from the exit value.
			System.out.println("IOException: Exception happened - here's what I know: ");
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			
			// You need this for that waitFor() diddy.
			System.out.println("InterruptedException: Something got interrupted, I guess: ");
			e.printStackTrace();
		}

	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
