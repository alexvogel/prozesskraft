package de.caegroup.process;

import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import de.caegroup.process.Process;
import de.caegroup.process.Step;

public class Checkconsistency
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	
	/*----------------------------
	  constructors
	----------------------------*/
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
		Option odefinition = OptionBuilder.withArgName("definition")
				.hasArg()
				.withDescription("[mandatory] process model in xml format.")
//				.isRequired()
				.create("definition");

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( odefinition );
		
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
		  die eigentliche business logic
		----------------------------*/

		Process p1 = new Process();
			
		p1.setInfilexml( commandline.getOptionValue("definition") );
		Process p2;
		try
		{
			p2 = p1.readXml();

			if (p2.isProcessConsistent())
			{
				System.out.println("process structure is consistent.");
			}
			else
			{
				System.out.println("process structure is NOT consistent.");
			}
			
			p2.printLog();
			
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
