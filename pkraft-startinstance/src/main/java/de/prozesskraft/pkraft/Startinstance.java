package de.prozesskraft.pkraft;

//import java.io.File;
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

import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;

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
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Startinstance.class) + "/" + "../etc/pkraft-startinstance.ini");

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
		
		Option ocommitfile = OptionBuilder.withArgName("KEY=FILE; FILE")
				.hasArg()
				.withDescription("[optional] this file will be committed as file. omit KEY= if KEY==FILENAME.")
//				.isRequired()
				.create("commitfile");

		Option ocommitvariable = OptionBuilder.withArgName("KEY=VALUE; VALUE")
				.hasArg()
				.withDescription("[optional] this string will be committed as a variable. omit KEY= if KEY==VALUE")
//				.isRequired()
				.create("commitvariable");

		Option ocommitfiledummy = OptionBuilder.withArgName("KEY=FILE; FILE")
				.hasArg()
				.withDescription("[optional] use this parameter like --commitfile. the file will not be checked against the process interface and therefore allows to commit files which are not expected by the process definition. use this parameter only for test purposes e.g. to commit dummy output files for accelerated tests of complex processes or the like.")
//				.isRequired()
				.create("commitfiledummy");

		Option ocommitvariabledummy = OptionBuilder.withArgName("KEY=VALUE; VALUE")
				.hasArg()
				.withDescription("[optional] use this parameter like --commitvariable. the variable will not be checked against the process interface and therefore allows to commit variables which are not expected by the process definition. use this parameter only for test purposes e.g. to commit dummy output variables for accelerated tests of complex processes or the like.")
//				.isRequired()
				.create("commitvariabledummy");

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
		options.addOption( ocommitfile );
		options.addOption( ocommitvariable );
		options.addOption( ocommitfiledummy );
		options.addOption( ocommitvariabledummy );
		
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
		Process p2 = null;

		try
		{
			p2 = p1.readXml();
		}
		catch (JAXBException e1)
		{
			System.err.println(e1.getMessage());
		}

		// das processBinary vorneweg schon mal erstellen, da es sein kann das das committen laenger dauert und ein pradar-attend den neuen prozess schon mal aufnehmen will
		// root-verzeichnis erstellen
		if (commandline.hasOption("basedir"))
		{
			p2.setBaseDir(commandline.getOptionValue("basedir"));
		}

		p2.makeRootdir();
		
		// den pfad fuers binary setzen
		p2.setOutfilebinary(p2.getRootdir() + "/process.pmb");

		System.err.println("info: writing process instance "+p2.getOutfilebinary());
		
		// binary schreiben
		p2.writeBinary();
		
		// step, an den die commits gehen, soll 'root' sein.
		Step stepRoot = p2.getRootStep();
			
		// committen von files (ueber einen glob)
		if (commandline.hasOption("commitfile"))
		{
			for(String actOptionCommitfile : commandline.getOptionValues("commitfile"))
			{
				String[] parts = actOptionCommitfile.split("=");
				File userFile = new File();

				if(parts.length == 1)
				{
					userFile.setKey(new java.io.File(parts[0]).getName());
					userFile.setGlob(parts[0]);
				}
				else if(parts.length == 2)
				{
					userFile.setKey(parts[0]);
					userFile.setGlob(parts[1]);
				}
				else
				{
					System.err.println("error in option -commitfile "+actOptionCommitfile);
					exiter();
				}
				
				// die auf der kommandozeile uebergebenen Informationen sollen in die vorhandenen commits im rootStep gemappt werden
				// alle vorhandenen commits in step root durchgehen und dem passenden file zuordnen
				for(Commit actCommit : stepRoot.getCommit())
				{
					// alle files des aktuellen commits
					for(File actFile : actCommit.getFile())
					{
						if(actFile.getKey().equals(userFile.getKey()))
						{
							// wenn actFile schon ein valider eintrag ist, dann soll ein klon befuellt werden
							if( actFile.getGlob() != null)
							{
								// wenn die maximale erlaubte anzahl noch nicht erreicht ist
								if( actCommit.getFile(actFile.getKey()).size() < actFile.getMaxoccur() )
								{
									File newFile = actFile.clone();
									newFile.setGlob(userFile.getGlob());
									System.err.println("entering file into commit '"+actCommit.getName()+"' ("+newFile.getKey()+"="+newFile.getGlob()+")");
									actCommit.addFile(newFile);
									break;
								}
								else
								{
									System.err.println("fatal: you only may commit "+actFile.getMaxoccur()+" " + actFile.getKey()+"-files into commit "+actCommit.getName());
									exiter();
								}
							}
							// ansonsten das bereits vorhandene file im commit mit den daten befuellen
							else
							{
								actFile.setGlob(userFile.getGlob());
								actFile.setGlobdir(p2.getBaseDir());
								System.err.println("entering file into commit '"+actCommit.getName()+"' ("+actFile.getKey()+"="+actFile.getGlob()+")");
								break;
							}
						}
					}
				}
			}
		}

		// committen von files (ueber einen glob)
		if (commandline.hasOption("commitfiledummy"))
		{
			// diese files werden nicht in bestehende commits der prozessdefinition eingetragen, sondern in ein spezielles commit
			Commit commitFiledummy = new Commit();
			commitFiledummy.setName("fileDummy");
			stepRoot.addCommit(commitFiledummy);

			for(String actOptionCommitfiledummy : commandline.getOptionValues("commitfiledummy"))
			{
				String[] parts = actOptionCommitfiledummy.split("=");
				File userFile = new File();
				commitFiledummy.addFile(userFile);

				if(parts.length == 1)
				{
					userFile.setKey(new java.io.File(parts[0]).getName());
					userFile.setGlob(parts[0]);
				}
				else if(parts.length == 2)
				{
					userFile.setKey(parts[0]);
					userFile.setGlob(parts[1]);
				}
				else
				{
					System.err.println("error in option -commitfiledummy "+actOptionCommitfiledummy);
					exiter();
				}
				userFile.setGlobdir(p2.getBaseDir());
				System.err.println("entering (dummy-)file into commit '"+commitFiledummy.getName()+"' ("+userFile.getKey()+"="+userFile.getGlob()+")");
			}
		}

		if (commandline.hasOption("commitvariable"))
			{
				for(String actOptionCommitvariable : commandline.getOptionValues("commitvariable"))
				{
					if (actOptionCommitvariable.matches(".+=.+"))
					{
						String[] parts = actOptionCommitvariable.split("=");
						Variable userVariable = new Variable();
	
						if(parts.length == 1)
						{
							userVariable.setKey("default");
							userVariable.setValue(parts[0]);
						}
						else if(parts.length == 2)
						{
							userVariable.setKey(parts[0]);
							userVariable.setValue(parts[1]);
						}
						else
						{
							System.err.println("error in option -commitvariable");
							exiter();
						}
//						commit.addVariable(variable);

						// die auf der kommandozeile uebergebenen Informationen sollen in die vorhandenen commits im rootStep gemappt werden
						// alle vorhandenen commits in step root durchgehen und dem passenden file zuordnen
						for(Commit actCommit : stepRoot.getCommit())
						{
							// alle files des aktuellen commits
							for(Variable actVariable : actCommit.getVariable())
							{
								if(actVariable.getKey().equals(userVariable.getKey()))
								{
									// wenn actFile schon ein valider eintrag ist, dann soll ein klon befuellt werden
									if( actVariable.getGlob() != null)
									{
										// wenn die maximale erlaubte anzahl noch nicht erreicht ist
										if( actCommit.getVariable(actVariable.getKey()).size() < actVariable.getMaxoccur() )
										{
											Variable newVariable = actVariable.clone();
											newVariable.setValue(userVariable.getValue());
											System.err.println("entering variable into commit '"+actCommit.getName()+"' ("+newVariable.getKey()+"="+newVariable.getValue()+")");
											actCommit.addVariable(newVariable);
											break;
										}
										else
										{
											System.err.println("fatal: you only may commit "+actVariable.getMaxoccur()+" " + actVariable.getKey()+"-variable(s) into commit "+actCommit.getName());
											exiter();
										}
									}
									// ansonsten das bereits vorhandene file im commit mit den daten befuellen
									else
									{
										actVariable.setValue(userVariable.getValue());
										System.err.println("entering variable into commit '"+actCommit.getName()+"' ("+actVariable.getKey()+"="+actVariable.getValue()+")");
										break;
									}
								}
							}
						}
					}
					else
					{
						System.err.println("-commitvariable "+actOptionCommitvariable+" does not match pattern \"NAME=VALUE\".");
						exiter();
					}
					
				}
			}

		if (commandline.hasOption("commitvariabledummy"))
		{
			// diese files werden nicht in bestehende commits der prozessdefinition eingetragen, sondern in ein spezielles commit
			Commit commitVariabledummy = new Commit();
			commitVariabledummy.setName("variableDummy");
			stepRoot.addCommit(commitVariabledummy);

			for(String actOptionCommitvariabledummy : commandline.getOptionValues("commitvariabledummy"))
			{
				String[] parts = actOptionCommitvariabledummy.split("=");
				Variable userVariable = new Variable();
				commitVariabledummy.addVariable(userVariable);

				if(parts.length == 1)
				{
					userVariable.setKey(parts[0]);
					userVariable.setValue(parts[0]);
				}
				else if(parts.length == 2)
				{
					userVariable.setKey(parts[0]);
					userVariable.setValue(parts[1]);
				}
				else
				{
					System.err.println("error in option -commitvariabledummy");
					exiter();
				}

				System.err.println("entering variable into commit '"+commitVariabledummy.getName()+"' ("+userVariable.getKey()+"="+userVariable.getValue()+")");
			}
		}

//		if (commandline.hasOption("basedir"))
//		{
//			p2.setBaseDir(commandline.getOptionValue("basedir"));
//		}

//			commit.doIt();
		stepRoot.commit();
		
		// root-verzeichnis erstellen
		p2.makeRootdir();
		
		// den pfad fuers binary setzen
//		p2.setOutfilebinary(p2.getRootdir() + "/process.pmb");

		System.err.println("info: writing process instance "+p2.getOutfilebinary());
		
		// binary schreiben
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
			System.err.println("info: starting processmanager for instance "+p2.getOutfilebinary());
			String aufrufString = ini.get("apps", "pkraft-manager") + " -instance "+p2.getOutfilebinary();
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
		else
		{
			System.err.println("info: NOT starting processmanager for instance "+p2.getOutfilebinary());
		}
			
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
