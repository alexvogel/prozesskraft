package de.caegroup.process;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


public class Perlcode
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String author = "alexander.vogel@caegroup.de";
	static String version = "[% version %]";
	static String date = "[% date %]";
	
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
		  create boolean options
		----------------------------*/
		Option ohelp = new Option("help", "print this message");
		
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
				.withDescription("[mandatory; default: .] directory for generated files.")
//				.isRequired()
				.create("output");
		
		Option odefinition = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] process definition file.")
//				.isRequired()
				.create("definition");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ostep );
		options.addOption( ooutput );
		options.addOption( odefinition );
		
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
			System.out.println("");
			System.out.println("author: "+author+" | version: "+version+" | date: "+date);
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
		
		String output = "";
		if ( !( commandline.hasOption("output")) )
		{
			System.err.println("setting -output=.");
			output = ".";
		}
		else
		{
			output = commandline.getOptionValue("output");
		}
		
		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		Process p1 = new Process();
		java.io.File outputDir = new java.io.File(output);

		if (outputDir.exists())
		{
			System.err.println("warn: already exists: " + outputDir.getCanonicalPath());
			if (! outputDir.isDirectory())
			{
				System.err.println("fatal: not a directory: " + outputDir.getCanonicalPath());
				exiter();
			}
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
			String stepname = commandline.getOptionValue("step");
			writeStepAsPerlcode(p2, stepname, outputDir);
		}
		
		// perlcode generieren fuer den gesamten process
		else
		{
			writeProcessAsPerlcode(p2, outputDir);
		}
		
	}
	
	/**
	 * writes a Process as Perlcode
	 * @param process
	 * @param outputDir
	 * @throws IOException
	 */
	private static void writeProcessAsPerlcode(Process process, java.io.File outputDir) throws IOException
	{
		System.err.println("generating perlcode for process "+process.getName());
		writeFile(new java.io.File(outputDir.getCanonicalPath()+"/"+process.getName()), process.getProcessAsPerlScript());
		
		for(Step actualStep : process.getStep())
		{
			writeStepAsPerlcode(process, actualStep.getName(), outputDir);
		}
	}
	
	/**
	 * writes a Process-Step as Perlcode
	 * @param process
	 * @param stepname
	 * @param outputDir
	 * @throws IOException
	 */
	private static void writeStepAsPerlcode(Process process, String stepname, java.io.File outputDir) throws IOException
	{
		if (process.isStep(stepname))
		{
			// step ueber den namen heraussuchen
			Step step = process.getStep(stepname);
			System.err.println("generating perlcode for step "+stepname);
			writeFile(new java.io.File(outputDir.getCanonicalPath()+"/"+step.getWork().getCommand()), step.getStepAsPerlScript());
		}
		else
		{
			System.err.println("stepname "+stepname+" is unknown in process "+process.getName());
			exiter();
		}
	}
	
	/**
	 * writes a file
	 * @param targetFile
	 * @param fileContent
	 * @throws IOException
	 */
	private static void writeFile(java.io.File targetFile, ArrayList<String> fileContent) throws IOException
	{
		if(targetFile.exists())
		{
			System.err.println("error: skipping creating file, because a file does already exist: " + targetFile.getCanonicalPath());
		}
		else
		{
			System.out.println("info: writing file: " + targetFile.getCanonicalPath());
			try
			{
				PrintWriter writer = new PrintWriter(targetFile.getCanonicalPath(), "UTF-8");
				for (String line : fileContent)
				{
					writer.println(line);
				}
				writer.close();
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
