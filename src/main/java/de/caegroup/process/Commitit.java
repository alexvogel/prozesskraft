package de.caegroup.process;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


public class Commitit
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
		Option oinstance = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] process instance file")
//				.isRequired()
				.create("instance");
		
		Option ostep = OptionBuilder.withArgName("STEPNAME")
				.hasArg()
				.withDescription("[optional, default: root] process step to commit to")
//				.isRequired()
				.create("step");
		
		Option odir = OptionBuilder.withArgName("DIR")
				.hasArg()
				.withDescription("[optional] the content of this directory will be committed as files.")
				.create("dir");
		
		Option ofile = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional] this file will be committed as file. key will be set to 'default'")
//				.isRequired()
				.create("file");
		
		Option ovarfile = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional] every line of this file that contains s.th. like KEY=VALUE will be committed as a variable.")
//				.isRequired()
				.create("varfile");
		
		Option okey = OptionBuilder.withArgName("KEY")
				.hasArg()
				.withDescription("[optional, default: default] this string will be considered as the key for the commit.")
//				.isRequired()
				.create("key");

		Option ovariable = OptionBuilder.withArgName("VALUE")
				.hasArg()
				.withDescription("[optional] this string will be committed as a variable.")
//				.isRequired()
				.create("variable");

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( oinstance );
		options.addOption( ostep );
		options.addOption( odir );
		options.addOption( ofile );
		options.addOption( ovarfile );
		options.addOption( okey );
		options.addOption( ovariable );
		
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
			formatter.printHelp("commit", options);
			System.exit(0);
		}
		
		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		if ( !( commandline.hasOption("instance")) )
		{
			System.out.println("option -instance is mandatory.");
			exiter();
		}

		else if ( !( commandline.hasOption("dir")) && !( commandline.hasOption("file")) && !( commandline.hasOption("varfile")) && !( commandline.hasOption("varname")) && !( commandline.hasOption("varvalue")) && !( commandline.hasOption("variable")))
		{
			System.out.println("at least one of these options needed. -dir -file -varfile -variable -varname -varvalue.");
			exiter();
		}
		
		else if ( ( commandline.hasOption("varname") && !( commandline.hasOption("varvalue")) ) || ( !(commandline.hasOption("varname")) && commandline.hasOption("varvalue")) )
		{
			System.out.println("use options -varname and -varvalue only in combination with each other.");
			exiter();
		}

		/*----------------------------
		  die eigentliche business logic
		----------------------------*/

		// setzen des steps
		String stepname = "root";
		if (commandline.hasOption("step")) {stepname = commandline.getOptionValue("step");}

		// setzen des key
		String key = "default";
		if (commandline.hasOption("key")) {key = commandline.getOptionValue("key");}

		
		Process p1 = new Process();
			
		p1.setInfilebinary( commandline.getOptionValue("instance") );
		System.out.println("info: reading process instance "+commandline.getOptionValue("instance"));
		Process p2 = p1.readBinary();
		p2.setOutfilebinary(commandline.getOptionValue("instance"));
		
		// step ueber den namen heraussuchen
		Step step = p2.getStep(stepname);

		// committen
		if (commandline.hasOption("file"))
		{
			step.commitFile(key, commandline.getOptionValue("file"));
		}
		
		if (commandline.hasOption("varfile"))
		{
			step.commitvarfile(commandline.getOptionValue("varfile"));
		}
		
		if (commandline.hasOption("dir"))
		{
			step.commitdir(commandline.getOptionValue("dir"));
		}
		
		if (commandline.hasOption("variable"))
		{
			step.commitVariable(key, commandline.getOptionValue("variable"));
		}
		
		p2.writeBinary();
		System.out.println("info: writing process instance "+p2.getOutfilebinary());
		
	}
	
	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
