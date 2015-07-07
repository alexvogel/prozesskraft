package de.prozesskraft.reporter;

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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.caegroup.commons.*;
import de.prozesskraft.reporter.*;

import java.nio.file.attribute.*;

public class Generate
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine commandline;
	static String web = "www.prozesskraft.de";
	static String author = "alexander.vogel@prozesskraft.de";
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
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Generate.class) + "/" + "../etc/reporter-generate.ini");

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
		Option otemplate = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory] the template that will be filled.")
//				.isRequired()
				.create("template");
		
		Option oformat = OptionBuilder.withArgName("pdf|pptx|docx|html|odt")
				.hasArg()
				.withDescription("[optional; default: pdf] the report will be rendered in this format.")
//				.isRequired()
				.create("format");
		
		Option ovariable = OptionBuilder.withArgName("VARIABLE")
				.hasArgs()
				.withDescription("[optional] variable that should be incorporated into the report. forexample -variable myMail=george.fryer@domain.com")
//				.isRequired()
				.create("variable");
		
		Option ooutput = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory; default: report.<ext>] the generated report will be written to this output file. the extension depends on the defined -format")
//				.isRequired()
				.create("output");
		
		Option of = new Option("f", "[optional] force overwrite output file if it already exists");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( otemplate );
		options.addOption( oformat );
		options.addOption( ovariable );
		options.addOption( ooutput );
		options.addOption( of );
		
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
			formatter.printHelp("fingerprint", options);
			System.exit(0);
		}

		else if ( commandline.hasOption("v"))
		{
			System.out.println("web:     "+web);
			System.out.println("author: "+author);
			System.out.println("version:"+version);
			System.out.println("date:     "+date);
			System.exit(0);
		}

		/*----------------------------
		  ueberpruefen ob eine schlechte kombination von parametern angegeben wurde
		----------------------------*/
		String template = "";
		String format = "";
		Map<String,String> variable = new HashMap<String,String>();
		String output = "";

		if ( !( commandline.hasOption("template")) )
		{
			System.err.println("mandatory option -template");
			Generate.exiter();
		}
		else
		{
			template = commandline.getOptionValue("template");
		}

		if ( !( commandline.hasOption("format")) )
		{
			System.err.println("setting default for -format=pdf");
			format = "pdf";
		}
		else
		{
			format = commandline.getOptionValue("format");
		}

		if ( commandline.hasOption("variable") )
		{
			String[] variables = commandline.getOptionValues("variable");
			
			// variable parsen (an '=' splitten) und ins map eintragen
			for(String actVariable : variables)
			{
				String[] keyValue = actVariable.split("=", 2);
				
				if(keyValue.length == 2)
				{
					variable.put((String)keyValue[0], (String)keyValue[1]);
				}
				else
				{
					System.err.println("error in option -variable " + actVariable);
					Generate.exiter();
				}
				
			}
		}
		
		if ( !( commandline.hasOption("output")) )
		{
			System.err.println("setting default for -output=report." + format);
			output = "report." + format;
		}
		else
		{
			output = commandline.getOptionValue("output");
		}

		// wenn output bereits existiert -> abbruch
		java.io.File outputFile = new File(output);
		if(outputFile.exists())
		{
			if(commandline.hasOption("f"))
			{
				outputFile.delete();
			}
			else
			{
				System.err.println("error: output file (" + output + ") already exists. use -f to force overwrite.");
				System.exit(1);
			}
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
		long jetztMillis = System.currentTimeMillis();
		String randomDir = "/tmp/"+jetztMillis+"_reporterGenerate";
		java.io.File randomDirAsFile = new java.io.File(randomDir);
		randomDirAsFile.mkdirs();
		
		// create object
		Reporter reporter = new Reporter();
		
		// set template
		reporter.setJrxml(template);
		
		// set jasper auf tmp directory
		reporter.setJasper(randomDir + "/jasper");

		// compile template
		try {
			reporter.compile();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set the parameter
		for(String actParameterKey : variable.keySet())
		{
			reporter.setParameter(actParameterKey, variable.get(actParameterKey));
		}

		// export to output as pdf
		if(format.equals("pdf"))
		{
			reporter.setPdf(output);
			try {
				reporter.exportToPdf();
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as pptx
		else if(format.equals("pptx"))
		{
			reporter.setPptx(output);
			try {
				reporter.exportToPptx();
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as docx
		else if(format.equals("docx"))
		{
			reporter.setDocx(output);
			try {
				reporter.exportToDocx();
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as html
		else if(format.equals("html"))
		{
			reporter.setHtml(output);
			try {
				reporter.exportToHtml();
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as odt
		else if(format.equals("odt"))
		{
			reporter.setOdt(output);
			try {
				reporter.exportToOdt();
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("unknown -format " + format);
		}
	}

	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
