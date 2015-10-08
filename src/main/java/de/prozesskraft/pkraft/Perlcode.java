package de.prozesskraft.pkraft;

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

import java.nio.file.attribute.*;

public class Perlcode
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String author = "alexander.vogel@caegroup.de";
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
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Perlcode.class) + "/" + "../etc/pkraft-perlcode.ini");

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
		Option ostep = OptionBuilder.withArgName("STEPNAME")
				.hasArg()
				.withDescription("[optional] stepname of step to generate a script for. if this parameter is omitted, a script for the process will be generated.")
//				.isRequired()
				.create("step");
		
		Option ooutput = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[mandatory] directory for generated files. must not exist when calling.")
//				.isRequired()
				.create("output");
		
		Option odefinition = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] process definition file.")
//				.isRequired()
				.create("definition");
		
		Option onolist = OptionBuilder.withArgName("")
//				.hasArg()
				.withDescription("[optional] with this parameter the multiple use of multi-optionis is forced. otherwise it is possible to use an integrated comma-separeated list.")
//				.isRequired()
				.create("nolist");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( ostep );
		options.addOption( ooutput );
		options.addOption( odefinition );
		options.addOption( onolist );
		
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
			formatter.printHelp("perlcode", options);
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
		if ( !( commandline.hasOption("definition")) )
		{
			System.err.println("option -definition is mandatory.");
			exiter();
		}
		
		if ( !( commandline.hasOption("output")) )
		{
			System.err.println("option -output is mandatory.");
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
		java.io.File outputDir = new java.io.File(commandline.getOptionValue("output"));
		java.io.File outputDirProcessScript = new java.io.File(commandline.getOptionValue("output"));
		java.io.File outputDirBin = new java.io.File(commandline.getOptionValue("output") + "/bin");
		java.io.File outputDirLib = new java.io.File(commandline.getOptionValue("output") + "/lib");
		boolean nolist = false;
		if (commandline.hasOption("nolist"))
		{
			nolist = true;
		}
		
		if (outputDir.exists())
		{
			System.err.println("fatal: directory already exists: " + outputDir.getCanonicalPath());
			exiter();
		}
		else
		{
			outputDir.mkdir();
		}
		
		p1.setInfilexml( commandline.getOptionValue("definition") );
		System.err.println("info: reading process definition "+commandline.getOptionValue("definition"));
		Process p2 = null;
		try {
			p2 = p1.readXml();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("error");
			exiter();
		}
		
		// perlcode generieren fuer einen bestimmten step
		if (commandline.hasOption("step"))
		{
			outputDirBin.mkdir();
			String stepname = commandline.getOptionValue("step");
			writeStepAsPerlcode(p2, stepname, outputDirBin, nolist);
		}
		
		// perlcode generieren fuer den gesamten process
		else
		{
			outputDirBin.mkdir();
			writeProcessAsPerlcode(p2, outputDirProcessScript, outputDirBin, nolist);

			// copy all perllibs from the lib directory
			outputDirLib.mkdir();
			final Path source = Paths.get(WhereAmI.getInstallDirectoryAbsolutePath(Perlcode.class) + "/../perllib");
			final Path target = Paths.get(outputDirLib.toURI());
			
			copyDirectoryTree.copyDirectoryTree(source, target);
		}
	}

	/**
	 * writes a Process as Perlcode
	 * @param process
	 * @param outputDir
	 * @throws IOException
	 */
	private static void writeProcessAsPerlcode(Process process, java.io.File outputDirProcess, java.io.File outputDirStep, boolean nolist) throws IOException
	{
		System.err.println("generating perlcode for process "+process.getName());
		writeFile.writeFile(new java.io.File(outputDirProcess.getCanonicalPath()+"/"+process.getName()), process.getProcessAsPerlScript(nolist));
		
		for(Step actualStep : process.getStep())
		{
			if(! actualStep.getName().matches("^root$") && actualStep.getType().matches("automatic"))
			{
				writeStepAsPerlcode(process, actualStep.getName(), outputDirStep, nolist);
			}
		}
	}
	
	/**
	 * writes a Process-Step as Perlcode
	 * @param process
	 * @param stepname
	 * @param outputDir
	 * @throws IOException
	 */
	private static void writeStepAsPerlcode(Process process, String stepname, java.io.File outputDir, boolean nolist) throws IOException
	{
		if (process.isStep(stepname))
		{
			// step ueber den namen heraussuchen
			Step step = process.getStep(stepname);
			System.err.println("generating perlcode for step "+stepname);
			
			writeFile.writeFile(new java.io.File(outputDir.getCanonicalPath()+"/"+step.getWork().getCommand()), step.getStepAsPerlScript(nolist));
		}
		else
		{
			System.err.println("stepname "+stepname+" is unknown in process "+process.getName());
			exiter();
		}
	}
	
//	/**
//	 * writes a file
//	 * @param targetFile
//	 * @param fileContent
//	 * @throws IOException
//	 */
//	private static void writeFile(java.io.File targetFile, ArrayList<String> fileContent) throws IOException
//	{
//		if(targetFile.exists())
//		{
//			System.err.println("error: skipping creating file, because a file does already exist: " + targetFile.getCanonicalPath());
//		}
//		else
//		{
//			System.out.println("info: writing file: " + targetFile.getCanonicalPath());
//			try
//			{
//				PrintWriter writer = new PrintWriter(targetFile.getCanonicalPath(), "UTF-8");
//				for (String line : fileContent)
//				{
//					writer.println(line);
//				}
//				writer.close();
//			}
//			catch (FileNotFoundException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			catch (UnsupportedEncodingException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
