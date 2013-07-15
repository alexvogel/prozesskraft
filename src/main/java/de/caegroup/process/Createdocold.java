package de.caegroup.process;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.xerces.impl.xpath.regex.ParseException;

import de.caegroup.process.Process;

public class Createdocold
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
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
		Option help = new Option("help", "print this message");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option definition = OptionBuilder.withArgName("definition")
				.hasArg()
				.withDescription("[mandatory] process definition file")
//				.isRequired()
				.create("definition");
		
		Option template = OptionBuilder.withArgName("template")
				.hasArg()
				.withDescription("[optional] document template file (odf)")
				.create("template");
		
		Option output = OptionBuilder.withArgName("output")
				.hasArg()
				.withDescription("[optional, default=out.odt] output file (odf) with documentation of process definition")
//				.isRequired()
				.create("output");
		
//		Option property = OptionBuilder.withArgName( "property=value" )
//				.hasArgs(2)
//				.withValueSeparator()
//				.withDescription( "use value for given property" )
//				.create("D");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( definition );
		options.addOption( template );
		options.addOption( output );
//		options.addOption( property );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			line = parser.parse( options,  args );
		}
		catch ( ParseException exp )
		{
			// oops, something went wrong
			System.err.println( "Parsing failed. Reason: "+ exp.getMessage());
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( line.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("createdocold", options);
			System.exit(0);
		}
		
		/*----------------------------
		  querying the commandline
		----------------------------*/
//		if ( line.hasOption("definitionfile") )
//		{
//			String hello = line.getOptionValue("definitionfile");
//		}
		
		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		
		if ( line.hasOption("definition") )
		{
		
			Process p1 = new Process();
			
			p1.setInfilexml( line.getOptionValue("definition") );
			System.out.println("info: reading process definition "+line.getOptionValue("definition"));
//			Process p2 = p1.readXml();
	
			// document file schreiben
			if ( line.hasOption("output") ) 
			{
//				p2.setOutFileDoc( line.getOptionValue("output") );
			}
			else
			{
//				p2.setOutFileDoc( "out.odt");
			}
			
			if ( line.hasOption("template") )
			{
//				p2.setFiledoctemplate( line.getOptionValue("template") );
			}
//			p2.writeDoc();
//			System.out.println("info: writing process documentation "+p2.getOutfiledoc());
		}
		
		else
		{
			System.out.println("try -help for help.");
		}

	}
}
