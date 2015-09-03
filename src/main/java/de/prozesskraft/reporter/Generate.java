package de.prozesskraft.reporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
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
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.xml.bind.JAXBException;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.prozesskraft.commons.*;
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

		// damit man diese anwendung auch mit whitespaces in den values aufrufen kann (z.B. -parameter name=Alexander Vogel), wird args[] umgestaltet
		Multimap<String,String> newArgs = HashMultimap.create();
		boolean lastMemberWasNamedArgument = false;
		String lastNamedArgument = null;
		String lastArgument = null;
		for(String actArg : args)
		{
			// wenn es eine option der form -parameter ist
			if(actArg.matches("^-.+$"))
			{
				// den zuletzt gesehenen value im multiMap speichern
				if(lastArgument != null)
				{
					newArgs.put(lastNamedArgument, lastArgument);
				}

				lastArgument = null;
				lastNamedArgument = actArg;
				lastMemberWasNamedArgument = true;
			}
			// wenn es ein value ist, aber davor war ein optionname wie -parameter
			else if(lastMemberWasNamedArgument)
			{
				lastArgument = actArg;
				
				lastMemberWasNamedArgument = false;
			}
			else
			{
				lastArgument += " " + actArg;
				lastMemberWasNamedArgument = false;
			}
		}
		
		// den letzten value im multiMap speichern
		if(lastArgument != null)
		{
			if(lastNamedArgument != null && lastArgument != null)
			{
				newArgs.put(lastNamedArgument, lastArgument);
			}
		}

		ArrayList<String> newArgAsList = new ArrayList<String>();
		// newargs nach args kopieren
		for(String actKey : newArgs.keySet())
		{
			newArgAsList.add(actKey);
			newArgAsList.addAll(newArgs.get(actKey));
		}
		args = newArgAsList.toArray(new String[newArgAsList.size()]);

		// ausgeben
		System.err.println("anzahl der parameterNamen: " +newArgs.keySet().size());
		for(String actString : args)
		{
			System.err.println(actString);
		}
		
		System.exit(1);
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
		Option oprint = new Option("print", "prints the layout of given template");
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
		
		Option oparameter = OptionBuilder.withArgName("KEY=VALUE")
				.hasArgs()
				.withDescription("[optional] parameter that should be incorporated into the report. forexample -parameter myMail=george.fryer@domain.com. KEY has to be unique.")
//				.isRequired()
				.create("parameter");
		
		Option oparameterFile = OptionBuilder.withArgName("FILE")
				.hasArgs()
				.withDescription("[optional] file that contains parameter in the form KEY=VALUE. each parameter in a separate line. empty lines or lines starting with # will be ignored. KEY has to be unique within all parameters.")
//				.isRequired()
				.create("parameterFile");
		
		Option ofield = OptionBuilder.withArgName("COLUMNNAME=VALUE")
				.hasArgs()
				.withDescription("[optional] field that should be incorporated into a table of the report. forexample -field result=success. the amount of VALUEs for every COLUMNNAME has to be the same.")
//				.isRequired()
				.create("field");
		
		Option ofieldFile = OptionBuilder.withArgName("FILE")
				.hasArgs()
				.withDescription("[optional] file that contains field information in csv format with the first line as a header (header => key)")
//				.isRequired()
				.create("fieldFile");
		
		Option ooutput = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[mandatory; default: report.<ext>] the generated report will be written to this output file. the extension depends on the defined -format")
//				.isRequired()
				.create("output");

		Option oappendJasperFilled = OptionBuilder.withArgName("FILE")
				.hasArg()
				.withDescription("[optional] this report will be appended as an subreport")
//				.isRequired()
				.create("appendJasperFilled");
		
		Option of = new Option("f", "[optional] force overwrite output file if it already exists");
		
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( oprint );
		options.addOption( otemplate );
		options.addOption( oformat );
		options.addOption( oparameter );
		options.addOption( oparameterFile );
		options.addOption( ofield );
		options.addOption( ofieldFile );
		options.addOption( ooutput );
		options.addOption( oappendJasperFilled );
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
			formatter.printHelp("generate", options);
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
		Map<String,String> parameter = new HashMap<String,String>();
//		Map<String,ArrayList<String>> field = new HashMap<String,ArrayList<String>>();
		List<Map<String,String>> field = new ArrayList<Map<String,String>> ();
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

		// wenn -print, dann nur die im template vorhandenen platzhalter ausgeben
		if ( ( commandline.hasOption("print")) )
		{
			Reporter reporter = new Reporter();
			
			// set template
			reporter.setJrxml(template);
			
			// auf stdout ausgeben
			reporter.printPlaceholder();
			
			// beenden
			System.exit(0);
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

		if ( commandline.hasOption("parameter") )
		{
			String[] parameters = commandline.getOptionValues("parameter");
			
			// parameter parsen (an '=' splitten) und ins map eintragen
			for(String actParameter : parameters)
			{
				String[] keyValue = actParameter.split("=", 2);
				
				if(keyValue.length == 2)
				{
					parameter.put((String)keyValue[0], (String)keyValue[1]);
				}
				else
				{
					System.err.println("error in option -parameter " + actParameter);
					Generate.exiter();
				}
				
			}
		}

		// fieldvalues aus csv einlesen und im map speichern
		if ( commandline.hasOption("fieldFile") )
		{
			String[] fieldFiles = commandline.getOptionValues("fieldFile");
			
			System.err.println("reading fieldFiles");

			// jedes csv-file einlesen, parsen, den inhalt dem grossen field-map hinzufuegen
			for(String actFieldFile : fieldFiles)
			{
				System.err.println("reading fieldFile "+actFieldFile);
				Reader reader = new FileReader(actFieldFile);
				Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withHeader().parse(reader);
				for(CSVRecord actRecord : records)
				{
					Map<String,String> recordAsMap = actRecord.toMap();
					System.err.println("reading line from fieldFile with " + recordAsMap.keySet().size() + " entries");
					field.add(recordAsMap);
				}
			}
		}

		// die Eingabeparameter parsen und im map speichern
		if ( commandline.hasOption("field") )
		{
			String[] fields = commandline.getOptionValues("field");

			// fields parsen (an '=' splitten) und ins map eintragen
			Map<String,ArrayList<String>> fieldsTmp = new HashMap<String,ArrayList<String>>();
			for(String actField : fields)
			{
				String[] keyValue = actField.split("=", 2);

				if(keyValue.length == 2)
				{
//					Map<String,String> recordMap = new HashMap<String,String>();
//					recordMap.put(keyValue[0], keyValue[1]);
					
					// gibts den key schon? dann hinzufuegen
					if(fieldsTmp.containsKey(keyValue[0]))
					{
						fieldsTmp.get(keyValue[0]).add(keyValue[1]);
					}
					// gibts den key noch nicht, dann eine liste erstellen und den value hinzufuegen
					else
					{
						ArrayList<String> liste = new ArrayList<String>();
						liste.add(keyValue[1]);
						fieldsTmp.put(keyValue[0], liste);
					}
				}
				else
				{
					System.err.println("error in option -field " + actField);
					Generate.exiter();
				}
			}

			// testen ob jeder schluessel im fieldsTmp die gleiche anzahl an eintraegen enthaelt
			// dies muss sein, da sonst nicht klar ist welche position in der entsprechenden spalte leer bleiben soll
			Integer anzahlZeilen = null;
			Integer anzahlDerLetztenSpalte = null;
			for(String actKey : fieldsTmp.keySet())
			{
				anzahlZeilen = fieldsTmp.get(actKey).size();
				if(anzahlDerLetztenSpalte != null)
				{
					if(fieldsTmp.get(actKey).size() != anzahlDerLetztenSpalte)
					{
						System.err.println("error in option -field. you have to provide the same amount of values for every columnname.");
						exiter();
					}
					else
					{
						anzahlDerLetztenSpalte = fieldsTmp.get(actKey).size();
					}
				}
				else
				{
					anzahlDerLetztenSpalte = fieldsTmp.get(actKey).size();
				}
			}
			
			// hinzufuegen der fiels, die ueber aufrufoption kommen zur der gesamt field map
			for(int i=0; i<anzahlZeilen; i++)
			{
				Map<String,String> recordAsMap = new HashMap<String,String>();
				for(String actKey : fieldsTmp.keySet())
				{
					recordAsMap.put(actKey, fieldsTmp.get(actKey).get(i));
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
		reporter.setName("main");
		
		// set template
		reporter.setJrxml(template);

		// compile template
		try {
			reporter.compile();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// set the parameter
		for(String actParameterKey : parameter.keySet())
		{
			System.err.println("setting parameter " + actParameterKey + "=" + parameter.get(actParameterKey) );
			reporter.setParameter(actParameterKey, parameter.get(actParameterKey));
		}

		// fuer jede zeile einen map erstellen und im reporter hinzufuegen
		for(Map<String,String> recordAsMap : field)
		{
			for(String actKey : recordAsMap.keySet())
			{
				System.err.println("adding field " + actKey + "=" +  recordAsMap.get(actKey));
			}
			// dem report hinzufuegen
			reporter.addField(recordAsMap);
		}

		// fill report
		try
		{
			System.err.println("filling report");
			reporter.fillReport();
		}
		catch (JRException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// alle -appendJasperFilled anhaengen
		if ( commandline.hasOption("appendJasperFilled") )
		{
			// die letzte seite des berichts entfernen ACHTUNG!! Das kann moeglicherweise ein Fehler sein
			// notwendig bei multibeulen+
//			reporter.removeLastPage();

			// alle appenJasperFilled appenden...
			for(String actAppendJasperFilled : commandline.getOptionValues("appendJasperFilled"))
			{
				try
				{
					System.err.println("appending a jasperFilled to current report " +actAppendJasperFilled );
					reporter.appendJasperFilled(actAppendJasperFilled);
				}
				catch (JRException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// write filled report as binary
		try
		{
			System.err.println("writing the filled report to file " +output + ".jasperFilled" );
			reporter.writeJasperFilled(output + ".jasperFilled");
		}
		catch (JRException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// export to output as pdf
		if(format.equals("pdf"))
		{
			try {
				reporter.exportToPdf(output);
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as pptx
		else if(format.equals("pptx"))
		{
			try {
				reporter.exportToPptx(output);
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as docx
		else if(format.equals("docx"))
		{
			try {
				reporter.exportToDocx(output);
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as html
		else if(format.equals("html"))
		{
			try {
				reporter.exportToHtml(output);
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// export to output as odt
		else if(format.equals("odt"))
		{
			try {
				reporter.exportToOdt(output);
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("unknown -format " + format);
		}
		
		System.err.println("report generated: " + output);
		
		// output der parameter im conf-format
		System.err.println("writing parameter content to file: " + output + ".parameter.conf");
		reporter.exportParametersToFile(output + ".parameter.conf");

		// output der fields im csv-format
//		System.err.println("if available, writing field content to file: " + output + ".field.conf");
//		reporter.exportFieldsToFile(output + ".parameter.conf");
	}

	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}


}
