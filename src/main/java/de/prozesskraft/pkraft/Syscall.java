package de.prozesskraft.pkraft;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;

public class Syscall {

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String web = "www.prozesskraft.de";
	static String author = "alexander.vogel@prozesskraft.de";
	static String version = "[% version %]";
	static String date = "[% date %]";

	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{
	 
		for(String arg : args)
		{
			System.out.println("syscall: "+arg);
		}
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
			System.out.println("web:     "+web);
			System.out.println("author:  "+author);
			System.out.println("version: "+version);
			System.out.println("date:    "+date);
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
			String sMylog = commandline.getOptionValue("mylog");
			String sPid = commandline.getOptionValue("pid");
			final String sStdout = commandline.getOptionValue("stdout");
			final String sStderr = commandline.getOptionValue("stderr");
			final String sMaxrun = commandline.getOptionValue("maxrun");

		    // die ausgabefiles in einen map aufnehmen
		    Map<String,String> filesKeyPath = new HashMap<String,String>();
		    filesKeyPath.put("mylog", sMylog);
		    filesKeyPath.put("pid", sPid);
		    filesKeyPath.put("stdout", sStdout);
		    filesKeyPath.put("stderr", sStderr);
		    filesKeyPath.put("exit", new java.io.File(sPid).getParentFile().getAbsolutePath() + "/.exit");
		    
		    
			// wenn die auch nur eines von den ausgabefiles mylog, stderr, stdout, pid bereits existieren, sollen alle ausgabefiles eine um 1 hochgezaehlte nummer erhalten
		    int zaehler = 0;

		    boolean irgendEinAusgabeFileExistiert = true;
		    while(irgendEinAusgabeFileExistiert)
		    {
		    	// renamen der ausgabefiles, falls zaehler == 1
		    	if(zaehler == 1)
		    	{
				    for(String key : filesKeyPath.keySet())
				    {
				    	filesKeyPath.put(key, filesKeyPath.get(key) + "_" + zaehler);
				    }
		    	}
		    	if(zaehler > 1)
		    	{
				    for(String key : filesKeyPath.keySet())
				    {
				    	filesKeyPath.put(key, filesKeyPath.get(key).replaceFirst("_\\d+$", "_"+zaehler));
				    }
		    	}

		    	irgendEinAusgabeFileExistiert = false;
			    for(String actFileAbsPath : filesKeyPath.values())
			    {
			    	File file = new File(actFileAbsPath);

			    	if(file.exists())
			    	{
			    		irgendEinAusgabeFileExistiert = true;
			    	}
			    }
			    
			    if(irgendEinAusgabeFileExistiert)
			    {
			    	zaehler++;
			    }
		    }

			// startzeit merken
			Date startDate = new Date();
			final Date termDate = new Date(startDate.getTime() + Integer.parseInt(sMaxrun)*60*1000);

			// Aufruf in das call -logfile schreiben
			PrintWriter writerLog = new PrintWriter(filesKeyPath.get("mylog"));
			writerLog.println("this program runs in directory:");
			writerLog.println(System.getProperty("user.dir"));
			writerLog.println("------------------------------------------------------");
			writerLog.println("pkraft-syscall has been called with this env DISPLAY="+System.getenv("DISPLAY"));
			writerLog.println("------------------------------------------------------");
			writerLog.println("pkraft-syscall has been called like this...");
			writerLog.println("------------------------------------------------------");
			writerLog.println("pkraft syscall \\");
			writerLog.println("-call \""+ sCall +"\" \\");
			writerLog.println("-stdout "+ filesKeyPath.get("stdout") + " \\");
			writerLog.println("-stderr "+ filesKeyPath.get("stderr") + " \\");
			writerLog.println("-pid "+ filesKeyPath.get("pid") + " \\");
			writerLog.println("-mylog "+ filesKeyPath.get("mylog") + " \\");
			writerLog.println("-maxrun "+sMaxrun + " \\");
			writerLog.println("------------------------------------------------------");
			writerLog.println("start at         : "+ startDate.toString());
			writerLog.println("will terminate at: "+ termDate.toString());
			writerLog.println("------------------------------------------------------");

			// Umleitung von STDOUT und STDERR dieses Scripts in das angegebene logfile
			FileOutputStream logStream = new FileOutputStream(filesKeyPath.get("mylog"), true);
			PrintStream logPrintStream = new PrintStream(logStream);
			System.setOut(logPrintStream);
			System.setErr(logPrintStream);

			// den aufruf in einzelne parameter zerlegen
			// normalerweise wuerde splitten an blanks ausreichen, jedoch kann es sein, das parameter leerzeichen enthalten und somit eine falsche splittung stattfindet
			// splitten an hochkommas zuerst, wenn es eine gerade anzahl davon gibt
			ArrayList<String> hochKommaSplit = new ArrayList<String>(Arrays.asList(sCall.split("'")));
			
			// in diesem array soll ein zwischenschritt der verarbeiteten parameter gehalten werden
			ArrayList<String> processSyscallWithArgsTmp = new ArrayList<String>();
			
			for(int i=0; i<hochKommaSplit.size(); i++)
			{
				// wenn i gerade ist, soll es weiter an blanks gesplittet werden
				if((i % 2) == 0)
				{
					System.err.println("GERADE: muss noch an blanks gesplittet werden: " + hochKommaSplit.get(i));
					ArrayList<String> unterSplit = new ArrayList<String>(Arrays.asList(hochKommaSplit.get(i).split(" ")));
					System.err.println("anzahl der splitter: "+unterSplit.size());
					processSyscallWithArgsTmp.addAll(unterSplit);
				}
				else
				{
					System.err.println("UNGERADE: muss NICHT an blanks gesplittet werden: " + hochKommaSplit.get(i));
					processSyscallWithArgsTmp.add(hochKommaSplit.get(i));
				}
			}

			// leere argumente entfernen
			writerLog.println("empty arguments have been removed");
			for(int i=0; i<processSyscallWithArgsTmp.size(); i++)
			{
				if(processSyscallWithArgsTmp.get(i).equals(""))
				{
					processSyscallWithArgsTmp.remove(i);
				}
			}
			
			// Zeichenfolge %WHITESPACE% ersetzen durch " "
			ArrayList<String> processSyscallWithArgsTmp2 = new ArrayList<String>();
			writerLog.println("placeholder %WHITESPACE% replaced with a single blank");
			for(String actString : processSyscallWithArgsTmp)
			{
				processSyscallWithArgsTmp2.add(actString.replace("%WHITESPACE%", " "));
			}
			
			// in diesem array sollen die endgueltig verarbeiteten parameter gehalten werden
			ArrayList<String> processSyscallWithArgs = new ArrayList<String>();
//			// noch vorhandene blanks in argumenten maskieren
//			writerLog.println("all values with whitespaces have been surrounded by single-quotes to 'mask' them");
//			int nr = 0;
//			for(String actArg : processSyscallWithArgsTmp)
//			{
//				if(nr == 0)
//				{
//					processSyscallWithArgs.add(actArg);
//				}
//				// ist ein whitespace vorhanden?, dann mit hochkomma maskieren
//				else if(actArg.matches("^.*\\s.*$"))
//				{
//					processSyscallWithArgs.add("'" + actArg + "'" );
//				}
//				else
//				{
//					processSyscallWithArgs.add(actArg);
//				}
//				nr++;
//			}
			processSyscallWithArgs = processSyscallWithArgsTmp2;

			writerLog.println("pkraft-syscall has called this...");
			writerLog.println("------------------------------------------------------");

			// das argumenten array ins logfile schreiben
			for(String actString : processSyscallWithArgs)
			{
				writerLog.println(actString + " \\");
			}
			
			writerLog.println("----------------- STDOUT and STDERR------------------");
			writerLog.close();

			// hat fehler produziert bei parametern mit blanks
//			ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(sCall.split(" ")));

			// erstellen prozessbuilder
			ProcessBuilder pb = new ProcessBuilder(processSyscallWithArgs);

			// errorstream nach out umleiten
//			pb.redirectErrorStream(true);
			
			// bevor der prozess gestartet wird, soll eine dummy-pid eingetragen werden
			PrintWriter writerDummyPid = new PrintWriter(filesKeyPath.get("pid"));
			writerDummyPid.println("dummyPID");
			writerDummyPid.close();
			
			// Aufruf taetigen
			final java.lang.Process sysproc = pb.start();

			// feststellen der Process-ID des laufenden JavaVM und in die PID-Datei schreiben
//			String pid = ManagementFactory.getRuntimeMXBean().getName();
			Integer pid = getPid(sysproc);
			
			// da die pid von der ManagementFactory normalerweise die Form "267353@ws11.caegroup" hat
			// soll nur die fuehrende Zahl erfasst werden
//			String patt = "(\\d+)";
//			Pattern r = Pattern.compile(patt);
//			Matcher m = r.matcher(pid);
			PrintWriter writerPid = new PrintWriter(filesKeyPath.get("pid"));
			// wenn eine fuehrende zahl gefunden wird, wird diese als pid verwendet
			System.out.println("PID WIRD FESTGESTELLT ALS: "+String.valueOf(pid));
			writerPid.println(String.valueOf(pid));
			writerPid.close();

			// oeffnen der OutputStream zur STDOUT-Ausgabedatei
			final FileWriter fw_stdout = new FileWriter(filesKeyPath.get("stdout"));
			// oeffnen der OutputStream zur STDERR-Ausgabedatei
			final FileWriter fw_stderr = new FileWriter(filesKeyPath.get("stderr"));
			
			// InputStream erstellen (wichtig, sonst bleiben scripte, die eingaben ueber stdin erwarten haengen)
			InputStream is = sysproc.getInputStream();
			
			// einen timer thread erstellen, der die ganze sache nach einem bestimmten zeitraum abschiessen soll
			new Thread(new Runnable() {
				public void run() {
					try
					{
						Thread.sleep(Integer.parseInt(sMaxrun)*60*1000);
					}
					catch (NumberFormatException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try
					{
						fw_stdout.close();
						fw_stderr.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					System.out.println("------------------------------------------------------");
					System.out.println("forced termination at "+new Date().toString());
					System.out.println("exitvalue: "+9999);

					sysproc.destroy();
					System.exit(9999);
				}
			}).start();
					
			// neuen thread, der STDOUT abfaengt und in eine datei schreibt
			new Thread(new Runnable() {
				public void run() {
					try {
						
						// einfangen der stdout- und stderr
						InputStream is_stdout = sysproc.getInputStream();
						
						// Send your InputStream to an InputStreamReader:
						InputStreamReader isr_stdout = new InputStreamReader(is_stdout);
						
						// That needs to go to a BufferedReader:
						BufferedReader br_stdout = new BufferedReader(isr_stdout);

						String line_out = new String();

						// schleife schreibt den aktuellen inhalt in das file
						while (((line_out = br_stdout.readLine()) != null))
						{
							if (!(line_out == null))
							{
								System.out.println("STDOUT:"+line_out); // wird umgeleitet ins myLog
								fw_stdout.write(line_out);
								fw_stdout.write("\n");
								fw_stdout.flush();
							}
						}

						fw_stdout.close();
	
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();
			
			// neuen thread, der STDERR abfaengt und in eine datei schreibt
			new Thread(new Runnable() {
				public void run() {
					try {
						
						// einfangen der stderr
						InputStream is_stderr = sysproc.getErrorStream();
						
						// Send your InputStream to an InputStreamReader:
						InputStreamReader isr_stderr = new InputStreamReader(is_stderr);
						
						// That needs to go to a BufferedReader:
						BufferedReader br_stderr = new BufferedReader(isr_stderr);

						// oeffnen der OutputStreams zu den Ausgabedateien
						final FileWriter fw_stderr = new FileWriter(sStderr);
						
						String line_err = new String();

						// schleife schreibt den aktuellen inhalt in das file
						while (((line_err = br_stderr.readLine()) != null))
						{
							if (!(line_err == null))
							{
								System.err.println("STDERR:"+line_err); // wird umgeleitet ins myLog
								fw_stderr.write(line_err);
								fw_stderr.write("\n");
								fw_stderr.flush();
							}
						}

						fw_stderr.close();
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

			// neuen thread, der STDIN abfaengt und flusht
			new Thread(new Runnable() {
				public void run() {
					try {
						
						// einfangen des stdin
						OutputStream os_stdin = sysproc.getOutputStream();

						// Send your OutputStream to an OutputStreamWriter:
						OutputStreamWriter osw_stdin = new OutputStreamWriter(os_stdin);
						
						// That needs to go to a BufferedWriter:
						BufferedWriter bw_stdin = new BufferedWriter(osw_stdin);

						bw_stdin.flush();
						bw_stdin.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).start();

			int exitValue = sysproc.waitFor();

			System.out.println("exitvalue: "+exitValue);

			sysproc.destroy();

			// Schreiben einer .exit Datei
			PrintWriter writerExit = new PrintWriter(filesKeyPath.get("exit"));
			writerExit.println(exitValue);
			writerExit.close();

			System.exit(exitValue);
			

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

	public static Integer getPid(Process process)
	{
	    try
	    {
	        Class<?> ProcessImpl = process.getClass();
	        Field field = ProcessImpl.getDeclaredField("pid");
	        field.setAccessible(true);
	        return field.getInt(process);
	    }
	    catch (Exception e)
	    {
	        return null;
	    }
	}
}
