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


public class Wrap
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
		Option ov = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option ooutput = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory; default: .] file for generated wrapper process.")
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
		options.addOption( ov );
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
			formatter.printHelp("wrap", options);
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
		  die eigentliche business logic
		----------------------------*/
		Process p1 = new Process();
		java.io.File output = new java.io.File(commandline.getOptionValue("output"));

		if (output.exists())
		{
			System.err.println("warn: already exists: " + output.getCanonicalPath());
			exiter();
		}

		p1.setInfilexml( commandline.getOptionValue("definition") );
		System.err.println("info: reading process definition "+commandline.getOptionValue("definition"));
		
		// dummy process
		Process p2 = null;
		
		try {
			p2 = p1.readXml();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("error");
			exiter();
		}
		
		// den wrapper process generieren
		Process p3 = p2.getProcessAsWrapper();
		p3.setOutfilexml(output.getAbsolutePath());

		// den neuen wrap-process rausschreiben
		p3.writeXml();
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
