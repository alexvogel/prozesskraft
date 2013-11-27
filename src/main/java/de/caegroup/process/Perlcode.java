package de.caegroup.process;

import java.io.IOException;
import java.util.ArrayList;

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

		else if ( !( commandline.hasOption("step")) )
		{
			System.err.println("perlcode will be generated for the whole process");
			exiter();
		}
		
		else if ( ( commandline.hasOption("varname") && !( commandline.hasOption("varvalue")) ) || ( !(commandline.hasOption("varname")) && commandline.hasOption("varvalue")) )
		{
			System.err.println("use options -varname and -varvalue only in combination with each other.");
			exiter();
		}

		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		ArrayList<String> perlcode = new ArrayList<String>();
		Process p1 = new Process();
		
		p1.setInfilebinary( commandline.getOptionValue("definition") );
		System.err.println("info: reading process definition "+commandline.getOptionValue("definition"));
		Process p2 = p1.readBinary();
		p2.setOutfilebinary(commandline.getOptionValue("instance"));
		
		// perlcode generieren
		if (commandline.hasOption("step"))
		{
			String stepname = commandline.getOptionValue("step");
			if (p2.isStep(stepname))
			{
				// step ueber den namen heraussuchen
				Step step = p2.getStep(stepname);
				System.err.println("generating perlcode for step "+stepname);
				perlcode = step.getPerlcode();
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
			perlcode = p2.getPerlcode();
		}
		
		// perlcode in Datei schreiben
		
		
		p2.writeBinary();
		System.out.println("info: writing process instance "+p2.getOutfilebinary());
		
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
