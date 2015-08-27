package de.prozesskraft.pkraft;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
//import org.apache.xerces.impl.xpath.regex.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.prozesskraft.pmodel.PmodelViewPage;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.reporter.*;
import de.prozesskraft.commons.WhereAmI;

public class Compilejrxml
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
//	static Ini ini;
	static Display display = Display.getDefault();
	protected static Shell shell;
	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
	{

		Compilejrxml tmp = new Compilejrxml();
		/*----------------------------
		  get options from ini-file
		----------------------------*/
//		File inifile = WhereAmI.getDefaultInifile(tmp.getClass());
//
//		if (inifile.exists())
//		{
//			try
//			{
//				ini = new Ini(inifile);
//			}
//			catch (InvalidFileFormatException e1)
//			{
//			// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			catch (IOException e1)
//			{
//			// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
//		else
//		{
//			System.err.println("ini file does not exist: "+inifile.getAbsolutePath());
//			System.exit(1);
//		}

		
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option ohelp = new Option("help", "print this message");
		Option v = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option ojrxmldir = OptionBuilder.withArgName("jrxmldir")
				.hasArg()
				.withDescription("[mandatory] directory with jasper definition files. only jrxml-files will be processed")
				.isRequired()
				.create("jrxmldir");
		
		Option ooutputdir = OptionBuilder.withArgName("outputdir")
				.hasArg()
				.withDescription("[mandatory] directory for jasper (jasper)-files output file. files will get overwritten.")
				.create("outputdir");
		
//		/*----------------------------
//		  create options object
//		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( v );
		options.addOption( ojrxmldir );
		options.addOption( ooutputdir );
////		options.addOption( property );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		// parse the command line arguments
		line = parser.parse( options,  args );
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( line.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("createdoc", options);
			System.exit(0);
		}
		
		if ( line.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@caegroup.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		/*----------------------------
		  die variablen festlegen
		----------------------------*/
		int error = 0;
		String jrxmldir = null;
		String outputdir = null;
		
		// festlegen
		if ( line.hasOption("jrxmldir") )
		{
			jrxmldir = line.getOptionValue("jrxmldir");
			if (!(new java.io.File(jrxmldir).exists()))
			{
				System.err.println("directory does not exist: "+jrxmldir);
				error++;
			}
			else if (!(new java.io.File(jrxmldir).isDirectory()))
			{
				System.err.println("is not a directory: "+jrxmldir);
				error++;
			}
		}
		else
		{
			System.err.println("parameter -jrxmldir is mandatory");
			error++;
		}
		
		// festlegen von outputdir
		if ( line.hasOption("outputdir") )
		{
			outputdir = line.getOptionValue("outputdir");
			if (!(new java.io.File(outputdir).exists()))
			{
				System.err.println("directory does not exist: "+outputdir);
				error++;
			}
		}
		else
		{
			System.err.println("parameter -outputdir is mandatory");
			error++;
		}
		
		// aussteigen, falls fehler aufgetaucht sind
		if (error > 0)
		{
			System.err.println("error(s) occured. try -help for help.");
			System.exit(1);
		}
		
		/*----------------------------
		  die eigentliche business logic
		----------------------------*/
		
		Reporter report = new Reporter();
		
		Pattern pattern = Pattern.compile("^(.+)\\.jrxml$");

		// alle files in dem jrxml-directory
		ArrayList<File> allFiles = new ArrayList<File>(Arrays.asList(new File(jrxmldir).listFiles()));
		
		// alle files durchgehen
		for(java.io.File actualFile : allFiles)
		{
			Matcher matcher = pattern.matcher(actualFile.getName());
			// nur die mit der extension "jrxml" verarbeiten
			if (matcher.matches())
			{
				System.out.println("processing jrxml-file: "+actualFile.getAbsolutePath());
				String basename = matcher.group(1);
				
				File outFile = new File(outputdir+"/"+basename+".jasper");
				
				System.out.println("creating jasper-file: "+outFile.getAbsolutePath());

				try {
					report.compileFileToFile(actualFile.getAbsolutePath(), outFile.getAbsolutePath());
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else
			{
				System.out.println("skipping file because it does not fit the pattern *.jrxml: "+actualFile.getAbsolutePath());
			}
		}
		System.out.println("i'm done. bye.");
		System.exit(0);
	}
}
