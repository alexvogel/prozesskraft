package de.caegroup.process;

import java.io.IOException;
import java.io.PrintWriter;
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
		
		Option ooutput = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional; default: out.pl] this file will be generated and contains perlcode.")
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

		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		ArrayList<String> perlcode = new ArrayList<String>();
		Process p1 = new Process();
		
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
		
		// perlcode generieren
		if (commandline.hasOption("step"))
		{
			String stepname = commandline.getOptionValue("step");
			if (p2.isStep(stepname))
			{
				// step ueber den namen heraussuchen
				Step step = p2.getStep(stepname);
				System.err.println("generating perlcode for step "+stepname);
				perlcode = step.getStepAsPerlScript();
			}
			else
			{
				System.err.println("stepname "+stepname+" is unknown in process "+p2.getName());
				exiter();
			}
		}
		else
		{
			System.err.println("generating perlcode for process "+p2.getName());
			perlcode = p2.getProcessAsPerlScript();
		}
		
		// Ausgabedatei festlegen
		String output = "out.pl";
		if (commandline.hasOption("output"))
		{
			output = commandline.getOptionValue("output");
		}
		System.out.println("info: generating perlcode and exporting in file: "+output);
		
		// perlcode in Datei schreiben
		PrintWriter writer = new PrintWriter(output, "UTF-8");
		for (String line : perlcode)
		{
			writer.println(line);
		}
		writer.close();
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
