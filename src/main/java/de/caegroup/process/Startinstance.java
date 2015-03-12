package de.caegroup.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		Option obasedir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[optional, default .] base directory where instance shourd run.")
//				.isRequired()
				.create("basedir");
		
		Option odefinition = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional] definition file of the process you want to start an instance from.")
//				.isRequired()
				.create("definition");
		
		Option onostart = OptionBuilder.withArgName("")
//				.hasArg()
				.withDescription("[optional] oppresses the start of the instance. (only create the process-instance)")
//				.isRequired()
				.create("nostart");
		
		Option opdomain = OptionBuilder.withArgName("STRING")
				.hasArg()
				.withDescription("[optional] domain of the process (mandatory if you omit -definition)")
//				.isRequired()
				.create("pdomain");
		
		Option opname = OptionBuilder.withArgName("STRING")
				.hasArg()
				.withDescription("[optional] name of the process you want to start an instance from (mandatory if you omit -definition)")
//				.isRequired()
				.create("pname");
		
		Option opversion = OptionBuilder.withArgName("STRING")
				.hasArg()
				.withDescription("[optional] version of the process you want to start an instance from (mandatory if you omit -definition)")
//				.isRequired()
				.create("pversion");
		
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
		options.addOption( obasedir );
		options.addOption( odefinition );
		options.addOption( onostart );
		options.addOption( opname );
		options.addOption( opversion );
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
		if ( !( commandline.hasOption("definition")) && (!(commandline.hasOption("pname")) || !(commandline.hasOption("pversion")) || !(commandline.hasOption("pdomain"))  ) )
		{
			System.err.println("option -definition or the options -pname & -pversion & -pdomain are mandatory");
			exiter();
		}

		if ( (commandline.hasOption("definition") && ((commandline.hasOption("pversion")) || (commandline.hasOption("pname")) || (commandline.hasOption("pdomain")) )  ) )
		{
			System.err.println("you must not use option -definition with -pversion or -pname or -pdomain");
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
		String pathToDefinition = "";
		
		if(commandline.hasOption("definition"))
		{
			pathToDefinition = commandline.getOptionValue("definition");
		}
		else if(commandline.hasOption("pname") && commandline.hasOption("pversion") && commandline.hasOption("pdomain") )
		{
			pathToDefinition.replaceAll("/+$", "");
			pathToDefinition = ini.get("process", "domain-installation-directory") + "/" + commandline.getOptionValue("pdomain") + "/" + commandline.getOptionValue("pname") + "/" + commandline.getOptionValue("pversion") + "/process.xml";
		}
		else
		{
			System.err.println("option -definition or the options -pname & -pversion & -pdomain are mandatory");
			exiter();
		}
		
		// check ob das ermittelte oder uebergebene xml-file ueberhaupt existiert
		java.io.File xmlDefinition = new java.io.File(pathToDefinition);
		if( !(xmlDefinition.exists()) || !(xmlDefinition.isFile()) )
		{
			System.err.println("process definition does not exist: "+pathToDefinition);
			exiter();
		}
		
		p1.setInfilexml( xmlDefinition.getCanonicalPath() );
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

				if (commandline.getOptionValue("commitfile").matches(".+=.+"))
					{
						String keyValue = commandline.getOptionValue("commitfile");
						String[] parts = keyValue.split("=");
						de.caegroup.process.File file = new de.caegroup.process.File();

						if(parts.length == 1)
						{
							file.setKey("default");
							file.setGlob("parts[0]");
						}
						else if(parts.length == 2)
						{
							file.setKey("parts[0]");
							file.setGlob("parts[1]");
						}
						else
						{
							System.err.println("error in option -commitvariable");
							exiter();
						}
						step.addFile(file);
					}
					else
					{
						System.err.println("-commitfile "+commandline.getOptionValue("commitfile")+" does not match pattern \"NAME=VALUE\".");
						exiter();
					}
			}

			if (commandline.hasOption("commitvariable"))
			{
				if (commandline.getOptionValue("commitvariable").matches(".+=.+"))
				{
					String keyValue = commandline.getOptionValue("commitvariable");
					String[] parts = keyValue.split("=");
					Variable variable = new Variable();

					if(parts.length == 1)
					{
						variable.setKey("default");
						variable.setValue("parts[0]");
					}
					else if(parts.length == 2)
					{
						variable.setKey(parts[0]);
						variable.setValue(parts[1]);
					}
					else
					{
						System.err.println("error in option -commitvariable");
						exiter();
					}
					step.addVariable(variable);
				}
				else
				{
					System.err.println("-commitvariable "+commandline.getOptionValue("commitvariable")+" does not match pattern \"NAME=VALUE\".");
					exiter();
				}
			}
			
			if (commandline.hasOption("basedir"))
			{
				p2.setBaseDir(commandline.getOptionValue("basedir"));
			}
			
			commit.doIt();
			
			p2.makeRootdir();
			System.out.println("info: writing process instance "+p2.getOutfilebinary());
			
			System.out.println("info: starting processmanager for instance "+p2.getOutfilebinary());

			p2.writeBinary();

			try
			{
				Thread.sleep(1500, 0);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			Runtime.getRuntime().exec("process-manager -help");

			// starten nur, falls es nicht abgewaehlt wurde
			if( ! commandline.hasOption("nostart"))
			{
				System.err.println("launching process with process-manager");
				
				String aufrufString = ini.get("apps", "process-manager") + " -instance "+p2.getOutfilebinary();
			
				System.err.println("calling: "+aufrufString);
	
				ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(aufrufString.split(" ")));
				
				ProcessBuilder pb = new ProcessBuilder(processSyscallWithArgs);

				//		ProcessBuilder pb = new ProcessBuilder("processmanager -instance "+p2.getOutfilebinary());
		//		Map<String,String> env = pb.environment();
		//		String path = env.get("PATH");
		//		System.out.println("PATH: "+path);
		//		
				java.lang.Process p = pb.start();
				System.err.println("pid: "+p.hashCode());
			}
				
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
