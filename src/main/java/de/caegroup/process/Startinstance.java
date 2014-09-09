package de.caegroup.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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

import de.caegroup.commons.MyLicense;
import de.caegroup.commons.WhereAmI;
import de.caegroup.process.Process;
import de.caegroup.process.Step;

public class Startinstance
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static Ini ini;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{

		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Startinstance.class) + "/" + "../etc/process-startinstance.ini");

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
		Option orootdir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[optional, default ./<random>] root directory of instance you are about to start.")
//				.isRequired()
				.create("rootdir");
		
		Option odefinition = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] definition file of process you want to start an instance from.")
//				.isRequired()
				.create("definition");
		
		Option ofile = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional] this file will be committed as file.")
//				.isRequired()
				.create("commitfile");
		
		Option ovariable = OptionBuilder.withArgName("NAME=VALUE")
				.hasArg()
				.withDescription("[optional] this string will be committed as a variable.")
//				.isRequired()
				.create("commitvariable");

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( orootdir );
		options.addOption( odefinition );
		options.addOption( ofile );
		options.addOption( ovariable );
		
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
			formatter.printHelp("startinstance", options);
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
		if ( !( commandline.hasOption("definition")) )
		{
			System.out.println("option -definition is mandatory.");
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

		Process p1 = new Process();
			
		p1.setInfilexml( commandline.getOptionValue("definition") );
		Process p2;
		try
		{
			p2 = p1.readXml();

		
			// step, an den die commits gehen, soll 'root' sein.
			Step step = p2.getStep(p2.getRootstepname());
			
			// den Commit 'by-process-commitit' heraussuchen oder einen neuen Commit dieses Namens erstellen
			Commit commit = step.getCommit("by-hand");
			if(commit == null)
			{
				commit = new Commit(step);
				commit.setName("by-process-commitit");
			}

			// committen von files (ueber einen glob)
			if (commandline.hasOption("commitfile"))
			{
				de.caegroup.process.File file = new de.caegroup.process.File();
				file.setGlob(commandline.getOptionValue("commitfile"));
				file.setKey("default");

				commit.addFile(file);
			}
			
			if (commandline.hasOption("commitvariable"))
			{
				if (commandline.getOptionValue("commitvariable").matches(".+=.+"))
				{
					String keyValue = commandline.getOptionValue("commitvariable");
					String[] parts = keyValue.split("=");
					Variable variable = new Variable();
					variable.setKey(parts[0]);

					if(parts.length < 2)
					{
						variable.setValue("default");
					}
					else
					{
						variable.setValue(parts[1]);
					}
					step.addVariable(variable);
				}
				else
				{
					System.err.println("-commitvariable "+commandline.getOptionValue("commitvariable")+" does not match pattern \"NAME=VALUE\".");
					exiter();
				}
			}
			
			if (commandline.hasOption("rootdir"))
			{
				p2.setRootdir(commandline.getOptionValue("rootdir"));
			}
			
			commit.doIt();
			
			p2.makeRootdir();
			p2.writeBinary();
			System.out.println("info: writing process instance "+p2.getOutfilebinary());
			
			System.out.println("info: starting processmanager for instance "+p2.getOutfilebinary());
	
			try
			{
				Thread.sleep(1500, 0);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			Runtime.getRuntime().exec("process-manager -help");
	
			System.out.println("AUFRUF: process manager -instance "+p2.getOutfilebinary());
			String[] args_for_syscall = {"process manager", "-instance", p2.getOutfilebinary()};
			ProcessBuilder pb = new ProcessBuilder(args_for_syscall);
	
			//		ProcessBuilder pb = new ProcessBuilder("processmanager -instance "+p2.getOutfilebinary());
	//		Map<String,String> env = pb.environment();
	//		String path = env.get("PATH");
	//		System.out.println("PATH: "+path);
	//		
			java.lang.Process p = pb.start();
			System.out.println("PROCESS: "+p.hashCode());

		} catch (JAXBException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
