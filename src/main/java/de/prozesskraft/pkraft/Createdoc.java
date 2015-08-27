package de.prozesskraft.pkraft;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption.*;
//import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.sf.jasperreports.engine.JRException;



//import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.PresentationMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.PresentationML.MainPresentationPart;
import org.docx4j.openpackaging.parts.PresentationML.SlideLayoutPart;
import org.docx4j.openpackaging.parts.PresentationML.SlidePart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
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
import org.pptx4j.Pptx4jException;

import de.prozesskraft.pmodel.PmodelViewPage;
import de.prozesskraft.reporter.*;
import de.prozesskraft.commons.AutoCropBorder;
import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;

public class Createdoc
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	static Ini ini;
	static Display display = Display.getDefault();
	protected static Shell shell;
	static String processTopologyImagePath;
	static Map<String,String> stepTopologyImagePath = new HashMap<String,String>();
	static Map<String,String> pdfRankFiles = new HashMap<String,String>();
	static Map<String,String> pptxRankFiles = new HashMap<String,String>();
	static boolean produktiv = true;

	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, IOException
	{

		Createdoc tmp = new Createdoc();
		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File installDir = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Createdoc.class) + "/..");
		
		File inifile = new java.io.File(installDir.getAbsolutePath() + "/etc/pkraft-createdoc.ini");

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
		Option odefinition = OptionBuilder.withArgName("definition")
				.hasArg()
				.withDescription("[mandatory] process definition file")
//				.isRequired()
				.create("definition");
		
		Option oformat = OptionBuilder.withArgName("format")
				.hasArg()
				.withDescription("[mandatory, default=pdf] output format (pdf|pptx) ")
				.create("format");
		
		Option ooutput = OptionBuilder.withArgName("output")
				.hasArg()
				.withDescription("[mandatory, default=out.<format>] output file with full documentation of process definition")
//				.isRequired()
				.create("output");
		
////		Option property = OptionBuilder.withArgName( "property=value" )
////				.hasArgs(2)
////				.withValueSeparator()
////				.withDescription( "use value for given property" )
////				.create("D");
//		
//		/*----------------------------
//		  create options object
//		----------------------------*/
		Options options = new Options();
		
		options.addOption( ohelp );
		options.addOption( ov );
		options.addOption( odefinition );
		options.addOption( oformat );
		options.addOption( ooutput );
		
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
			System.out.println("web:     www.prozesskraft.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		/*----------------------------
		  die variablen festlegen
		----------------------------*/
		int error = 0;
		String definition = null;
		String format = null;
		String output = null;
		
		// festlegen von definition
		if ( line.hasOption("definition") )
		{
			definition = line.getOptionValue("definition");
			if (!(new java.io.File(definition).exists()))
			{
				System.err.println("file does not exist "+definition);
			}
		}
		else
		{
			System.err.println("parameter -definition is mandatory");
			error++;
		}
		
		// festlegen von format
		if ( line.hasOption("format") )
		{
			if (line.getOptionValue("format").matches("pdf|pptx"))
			{
				format = line.getOptionValue("format");
			}
			else
			{
				System.err.println("for -format use only pdf|pptx");
				error++;
			}
		}
		else
		{
			format = "pdf";
		}
		
		// festlegen von output
		if ( line.hasOption("output") )
		{
			output = line.getOptionValue("output");
		}
		else
		{
			output = "out."+format;
		}
		
		// feststellen ob output bereits existiert
		if(new java.io.File(output).exists())
		{
			System.err.println("output already exists: "+output);
			error++;
		}
		
		// aussteigen, falls fehler aufgetaucht sind
		if (error > 0)
		{
			System.err.println("error(s) occured. try -help for help.");
			System.exit(1);
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
		
		Process process = new Process();
		Reporter report;

		process.setInfilexml(definition);
		
		System.out.println("info: reading process definition "+definition);

		
		try
		{
			process.readXml();
			process.setStepRanks();
		}
		
		catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//festlegen des temporaeren verzeichnisses fuer die Daten und Pfade erzeugen
		long jetztMillis = System.currentTimeMillis();
		String randomPathJasperFilled = "/tmp/"+jetztMillis+"_jasperFilled";
		String randomPathPng          = "/tmp/"+jetztMillis+"_png";
		String randomPathPdf          = "/tmp/"+jetztMillis+"_pdf";
		String randomPathPptx         = "/tmp/"+jetztMillis+"_pptx";
		
		new File(randomPathJasperFilled).mkdirs();
		new File(randomPathPng).mkdirs();
		new File(randomPathPdf).mkdirs();
		new File(randomPathPptx).mkdirs();
		
//////////////////////////////////////////

		// erstellen der Bilder
		
		// konfigurieren der processing ansicht
//		PmodelViewPage page = new PmodelViewPage(process);
		PmodelViewPage page = new PmodelViewPage();
		page.einstellungen.getProcess().setStepRanks();
		page.einstellungen.setSize(100);
		page.einstellungen.setZoom(100);
//		page.einstellungen.setZoom(8 * 100/process.getMaxLevel());
		page.einstellungen.setTextsize(0);
		page.einstellungen.setRanksize(7);
		page.einstellungen.setWidth(2500);
		page.einstellungen.setHeight(750);
		page.einstellungen.setGravx(10);
		page.einstellungen.setGravy(0);
		page.einstellungen.setRootpositionratiox((float)0.05);
		page.einstellungen.setRootpositionratioy((float)0.5);
		page.einstellungen.setProcess(process);
	
		createContents(page);

		// mit open kann die page angezeigt werden
		if (!(produktiv))
		{
			open();
		}
		
//		// warten
//		System.out.println("stabilisierung ansicht: 5 sekunden warten gravitation = "+page.einstellungen.getGravx());
//		long jetzt5 = System.currentTimeMillis();
//		while (System.currentTimeMillis() < jetzt5 + 5000)
//		{
//			
//		}
//
//		page.einstellungen.setGravx(10);
//
		// warten
		int wartezeitSeconds = 1;
		if (produktiv) {wartezeitSeconds = page.einstellungen.getProcess().getStep().size()*2;}
		System.out.println("stabilisierung ansicht: "+wartezeitSeconds+" sekunden warten gravitation = "+page.einstellungen.getGravx());
		long jetzt6 = System.currentTimeMillis();
		while (System.currentTimeMillis() < jetzt6 + (wartezeitSeconds * 1000))
		{
			
		}

		page.einstellungen.setFix(true);
		
		// VORBEREITUNG) bild speichern
		processTopologyImagePath = randomPathPng+"/processTopology.png";
		page.savePic(processTopologyImagePath);
		// zuerst 1 sekunde warten, dann autocrop
		long jetzt = System.currentTimeMillis();
		while (System.currentTimeMillis() < jetzt + 1000)
		{
			
		}
		new AutoCropBorder(processTopologyImagePath);
		
		// VORBEREITUNG) fuer jeden step ein bild speichern
		for(Step actualStep : process.getStep())
		{
			
			// root ueberspringen
//			if (actualStep.isRoot());
			
			String stepImagePath = randomPathPng+"/step_"+actualStep.getName()+"_Topology.png";
			
			// Farbe des Steps auf finished (gruen) aendern
			page.einstellungen.getProcess().getRootStep().setStatusOverwrite("waiting");
			actualStep.setStatusOverwrite("finished");
			
			// etwas warten, bis die farbe bezeichnet wurde
			long jetzt4 = System.currentTimeMillis();
			while (System.currentTimeMillis() < jetzt4 + 500)
			{
				
			}
			
			page.savePic(stepImagePath);
			// zuerst 1 sekunde warten, dann autocrop
			long jetzt3 = System.currentTimeMillis();
			while (System.currentTimeMillis() < jetzt3 + 1000)
			{
				
			}
			new AutoCropBorder(stepImagePath);

			stepTopologyImagePath.put(actualStep.getName(), stepImagePath);
			
			// farbe wieder auf grau aendern
			actualStep.setStatusOverwrite(null);
			
			System.out.println("erstelle bild fuer step: "+actualStep.getName());

			long jetzt2 = System.currentTimeMillis();
			while (System.currentTimeMillis() < jetzt2 + 1000)
			{
				
			}
		}

		page.destroy();

//////////////////////////////////////////
		report = new Reporter();
		
		// P03) erstellen des p03
		System.out.println("info: generating p03.");
		
		String pdfPathP03 = null;
		String pptxPathP03 = null;
		String jasperPathP03 = null;
		String jasperFilledPathP03 = null;
		
		// P03) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p03") != null )
		{
			pdfPathP03 = randomPathPdf+"/p03.pdf";
			pptxPathP03 = randomPathPptx+"/p03.pptx";
			jasperPathP03 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p03");
			jasperFilledPathP03 = (randomPathJasperFilled+"/p03.jasperFilled");
			
			pdfRankFiles.put("0.0.03", pdfPathP03);
			pptxRankFiles.put("0.0.03", pptxPathP03);
		}
		else
		{
			System.err.println("no entry 'p03' found in ini file");
			System.exit(1);
		}
		
		DateFormat dateFormat = new SimpleDateFormat("dd. MM. yyyy");
		Date date = new Date();
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processDatum", dateFormat.format(date));
		report.setParameter("processArchitectLogoImagePath", installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "logo"));
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		
		try {
			report.fillReportFileToFile(jasperPathP03, jasperFilledPathP03);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP03, pdfPathP03);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP03, pptxPathP03);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
		
//System.exit(0);

//////////////////////////////////////////
		report = new Reporter();
		
		// P05) erstellen des p05
		System.out.println("info: generating p05.");
		
		String pdfPathP05 = null;
		String pptxPathP05 = null;
		String jasperPathP05 = null;
		String jasperFilledPathP05 = null;
		
		// P05) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p05") != null )
		{
			pdfPathP05 = randomPathPdf+"/p05.pdf";
			pptxPathP05 = randomPathPptx+"/p05.pptx";
			jasperPathP05 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p05");
			jasperFilledPathP05 = (randomPathJasperFilled+"/p05.jasperFilled");
			
			pdfRankFiles.put("0.0.05", pdfPathP05);
			pptxRankFiles.put("0.0.05", pptxPathP05);
		}

		else
		{
			System.err.println("no entry 'p05' found in ini file");
			System.exit(1);
		}
		
			report.setParameter("processName", process.getName());
			report.setParameter("processVersion", process.getVersion());
			report.setParameter("processArchitectCompany", process.getArchitectCompany());
			report.setParameter("processArchitectName", process.getArchitectName());
			report.setParameter("processArchitectMail", process.getArchitectMail());
			report.setParameter("processCustomerCompany", process.getCustomerCompany());
			report.setParameter("processCustomerName", process.getCustomerName());
			report.setParameter("processCustomerMail", process.getCustomerMail());
		
		try {
			report.fillReportFileToFile(jasperPathP05, jasperFilledPathP05);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP05, pdfPathP05);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP05, pptxPathP05);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
		
		//System.exit(0);
				
//////////////////////////////////////////
		report = new Reporter();

		// P08) erstellen des p08
		System.out.println("info: generating p08.");

		String pdfPathP08 = null;
		String pptxPathP08 = null;
		String jasperPathP08 = null;
		String jasperFilledPathP08 = null;
		
		// P08) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p08") != null )
		{
			pdfPathP08 = randomPathPdf+"/p08.pdf";
			pptxPathP08 = randomPathPptx+"/p08.pptx";
			jasperPathP08 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p08");
			jasperFilledPathP08 = (randomPathJasperFilled+"/p08.jasperFilled");
			
			pdfRankFiles.put("0.0.08", pdfPathP08);
			pptxRankFiles.put("0.0.08", pptxPathP08);
		}
		else
		{
			System.err.println("no entry 'p08' found in ini file");
			System.exit(1);
		}

		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		report.setParameter("processDescription", process.getDescription());

		try {
			report.fillReportFileToFile(jasperPathP08, jasperFilledPathP08);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP08, pdfPathP08);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP08, pptxPathP08);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;

//System.exit(0);

//////////////////////////////////////////
		
		report = new Reporter();
		
		// P10) erstellen des p10
		System.out.println("info: generating p10.");
		
		String pdfPathP10 = null;
		String pptxPathP10 = null;
		String jasperPathP10 = null;
		String jasperFilledPathP10 = null;
		
		// P10) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p10") != null )
		{
			pdfPathP10 = randomPathPdf+"/p10.pdf";
			pptxPathP10 = randomPathPptx+"/p10.pptx";
			jasperPathP10 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p10");
			jasperFilledPathP10 = (randomPathJasperFilled+"/p10.jasperFilled");
			
			pdfRankFiles.put("0.0.10", pdfPathP10);
			pptxRankFiles.put("0.0.10", pptxPathP10);
		}
		else
		{
			System.err.println("no entry 'p10' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());

		// rootstep holen
		Step rootStep = process.getStep(process.getRootstepname());
		
		// ueber alle commit iterieren
		for(Commit actualCommit : rootStep.getCommit())
		{
			
			// ueber alle files iterieren
			for(de.prozesskraft.pkraft.File actualFile : actualCommit.getFile())
			{
				HashMap<String,Object> row = new HashMap<String,Object>();
				
				// Spalte 'origin'
				row.put("origin", "user/cb2");
				
				// Spalte 'objectType'
				row.put("objectType", "file");
				
				// Spalte 'minOccur'
				row.put("minOccur", ""+actualFile.getMinoccur());
				
				// Spalte 'maxOccur'
				row.put("maxOccur", ""+actualFile.getMaxoccur());
				
				// Spalte 'objectKey'
				row.put("objectKey", actualFile.getKey());
				
				// die steps herausfinden, die dieses file benoetigen
				ArrayList<Step> allStepsThatNeedThisFileFromRoot = process.getStepWhichNeedFromRoot("file", actualFile.getKey());
				String stepnameListe = "";
				for(Step actStep : allStepsThatNeedThisFileFromRoot)
				{
					stepnameListe += "\n=> " + actStep.getName();
				}
				
				// Spalte 'objectDescription'
				row.put("objectDescription", actualFile.getDescription() + stepnameListe);

				// Datensatz dem report hinzufuegen
				report.addField(row);
			}

			// ueber alle variablen iterieren
			for(de.prozesskraft.pkraft.Variable actualVariable : actualCommit.getVariable())
			{
				HashMap<String,Object> row = new HashMap<String,Object>();
				
				// Spalte 'origin'
				row.put("origin", "user/cb2");
				
				// Spalte 'objectType'
				row.put("objectType", "variable");
				
				// Spalte 'minOccur'
				row.put("minOccur", ""+actualVariable.getMinoccur());
				
				// Spalte 'maxOccur'
				row.put("maxOccur", ""+actualVariable.getMaxoccur());
				
				// Spalte 'objectKey'
				row.put("objectKey", actualVariable.getKey());
				
				// die steps herausfinden, die dieses file benoetigen
				ArrayList<Step> allStepsThatNeedThisObjectFromRoot = process.getStepWhichNeedFromRoot("variable", actualVariable.getKey());
				String stepnameListe = "";
				for(Step actStep : allStepsThatNeedThisObjectFromRoot)
				{
					stepnameListe += "\n=> " + actStep.getName();
				}
				
				// Spalte 'objectDescription'
				row.put("objectDescription", actualVariable.getDescription() + stepnameListe);

				// Datensatz dem report hinzufuegen
				report.addField(row);
			}

		}

		try {
			report.fillReportFileToFile(jasperPathP10, jasperFilledPathP10);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP10, pdfPathP10);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP10, pptxPathP10);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
		
//////////////////////////////////////////

		report = new Reporter();
		
		// P20) erstellen des p20
		System.out.println("info: generating p20.");
		
		String pdfPathP20 = null;
		String pptxPathP20 = null;
		String jasperPathP20 = null;
		String jasperFilledPathP20 = null;
		
		// P20) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p20") != null )
		{
			pdfPathP20 = randomPathPdf+"/p20.pdf";
			pptxPathP20 = randomPathPptx+"/p20.pptx";
			jasperPathP20 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p20");
			jasperFilledPathP20 = (randomPathJasperFilled+"/p20.jasperFilled");
			
			pdfRankFiles.put("0.0.20", pdfPathP20);
			pptxRankFiles.put("0.0.20", pptxPathP20);
		}
		else
		{
			System.err.println("no entry 'p20' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());

		// ueber alle steps iterieren (ausser root)
		for(Step actualStep : (ArrayList<Step>)process.getStep())
		{
			
			// ueberspringen wenn es sich um root handelt
			if(!(actualStep.getName().equals(process.getRootstepname())))
			{
				// ueber alle commit iterieren
				for(Commit actualCommit : actualStep.getCommit())
				{

					// nur die, die toroot=true ( und spaeter auch tosdm=true)
					if(actualCommit.isTorootPresent())
					{
						// ueber alle files iterieren
						for(de.prozesskraft.pkraft.File actualFile : actualCommit.getFile())
						{
	
							HashMap<String,Object> row = new HashMap<String,Object>();
						
							// Spalte 'destination'
							row.put("destination", "user/cb2");
							
							// Spalte 'objectType'
							row.put("objectType", "file");
							
							// Spalte 'minOccur'
							row.put("minOccur", ""+actualFile.getMinoccur());
							
							// Spalte 'maxOccur'
							row.put("maxOccur", ""+actualFile.getMaxoccur());
							
							// Spalte 'objectKey'
							row.put("objectKey", actualFile.getKey());
							
							// Spalte 'objectDescription'
							row.put("objectDescription", actualFile.getDescription() + "\n<= " + actualStep.getName());
		
							// Datensatz dem report hinzufuegen
							report.addField(row);
						}
	
						// ueber alle variablen iterieren
						for(de.prozesskraft.pkraft.Variable actualVariable : actualCommit.getVariable())
						{
							HashMap<String,Object> row = new HashMap<String,Object>();
							
							// Spalte 'objectType'
							row.put("destination", "user/cb2");
		
							// Spalte 'objectType'
							row.put("objectType", "variable");
							
							// Spalte 'minOccur'
							row.put("minOccur", ""+actualVariable.getMinoccur());
							
							// Spalte 'maxOccur'
							row.put("maxOccur", ""+actualVariable.getMaxoccur());
							
							// Spalte 'objectKey'
							row.put("objectKey", actualVariable.getKey());
							
							// Spalte 'objectDescription'
							row.put("objectDescription", actualVariable.getDescription() + "\n<= " + actualStep.getName());
		
							// Datensatz dem report hinzufuegen
							report.addField(row);
						}
					}
				}
			}

		}
		
		try {
			report.fillReportFileToFile(jasperPathP20, jasperFilledPathP20);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP20, pdfPathP20);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP20, pptxPathP20);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
	
//////////////////////////////////////////

		report = new Reporter();
		
		// P30) erstellen des p30
		System.out.println("info: generating p30.");
		
		String pdfPathP30 = null;
		String pptxPathP30 = null;
		String jasperPathP30 = null;
		String jasperFilledPathP30 = null;
		
		// P30) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p30") != null )
		{
			pdfPathP30 = randomPathPdf+"/p30.pdf";
			pptxPathP30 = randomPathPptx+"/p30.pptx";
			jasperPathP30 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p30");
			jasperFilledPathP30 = (randomPathJasperFilled+"/p30.jasperFilled");
			
			pdfRankFiles.put("0.0.30", pdfPathP30);
			pptxRankFiles.put("0.0.30", pptxPathP30);
		}
		else
		{
			System.err.println("no entry 'p30' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		
		// P1) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);

		try {
			report.fillReportFileToFile(jasperPathP30, jasperFilledPathP30);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP30, pdfPathP30);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP30, pptxPathP30);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
		
//		System.exit(0);
//////////////////////////////////////////
		
		report = new Reporter();
			
		// P40) erstellen des p40
		System.out.println("info: generating p40.");
		
		String pdfPathP40 = null;
		String pptxPathP40 = null;
		String jasperPathP40 = null;
		String jasperFilledPathP40 = null;
		
		// P40) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("pkraft-createdoc", "p40") != null )
		{
			pdfPathP40 = randomPathPdf+"/p40.pdf";
			pptxPathP40 = randomPathPptx+"/p40.pptx";
			jasperPathP40 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p40");
			jasperFilledPathP40 = (randomPathJasperFilled+"/p40.jasperFilled");
			
			pdfRankFiles.put("0.0.40", pdfPathP40);
			pptxRankFiles.put("0.0.40", pptxPathP40);
		}
		else
		{
			System.err.println("no entry 'p40' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		
		// P40) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);

		// Tabelle erzeugen

		ArrayList<Step> steps = process.getStep();
		for(int x = 0; x < steps.size(); x++)
		{
			HashMap<String,Object> row = new HashMap<String,Object>();
			Step actualStep = steps.get(x);

			// erste Spalte ist 'rank'
			// um die korrekte sortierung zu erhalten soll der rank-string auf jeweils 2 Stellen erweitert werden
			String[] rankArray = actualStep.getRank().split("\\.");
			Integer[] rankArrayInt = new Integer[rankArray.length];
			for(int y=0; y < rankArray.length; y++)
			{
				rankArrayInt[y] = Integer.parseInt(rankArray[y]);
			}
			String rankFormated = String.format("%02d.%02d", rankArrayInt);
			row.put("stepRank", rankFormated);
			
			// zweite Spalte ist 'stepname'
			row.put("stepName", actualStep.getName());
//				System.out.println("stepName: "+actualStep.getName());

			// dritte Spalte ist 'Beschreibung'
			row.put("stepDescription", actualStep.getDescription());
//				System.out.println("stepRank: "+actualStep.getDescription());

			// wenn nicht der root-step, dann row eintragen
			if (!(actualStep.getName().equals(process.getRootstepname())))
			{
				report.addField(row);
			}
		}
		
		try {
			report.fillReportFileToFile(jasperPathP40, jasperFilledPathP40);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.convertFileToPdf(jasperFilledPathP40, pdfPathP40);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.convertFileToPptx(jasperFilledPathP40, pptxPathP40);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
		
//////////////////////////////////////////

		// fuer jeden Step einen eigenen Input Report erzeugen
		
		for(Step actualStep : process.getStep())
		{
			// root-step ueberspringen
			if (actualStep.getName().equals(process.getRootstepname()))
			{
				System.out.println("skipping step root");
			}
			
			// alle anderen auswerten
			else
			{
				
				report = new Reporter();
				
				// P51x) erstellen des p51
				System.out.println("info: generating p51 for step "+actualStep.getRank()+" => "+actualStep.getName());
				
				String stepRank = actualStep.getRank();
				
				String pdfPathP51 = null;
				String pptxPathP51 = null;
				String jasperPathP51 = null;
				String jasperFilledPathP51 = null;
				
				// P51x) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
				if (ini.get("pkraft-createdoc", "p51") != null )
				{
					pdfPathP51 = randomPathPdf+"/p5."+stepRank+".1.pdf";
					pptxPathP51 = randomPathPptx+"/p5."+stepRank+".1.pptx";
					jasperPathP51 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p51");
					jasperFilledPathP51 = randomPathJasperFilled+"/p5."+stepRank+".1.jasperFilled";
					
					String[] rankArray = stepRank.split("\\.");
					Integer[] rankArrayInt = new Integer[rankArray.length];
					for(int x=0; x < rankArray.length; x++)
					{
						rankArrayInt[x] = Integer.parseInt(rankArray[x]);
					}
					String rankFormated = String.format("%03d.%03d", rankArrayInt);

					pdfRankFiles.put(rankFormated+".1", pdfPathP51);
					pptxRankFiles.put(rankFormated+".1", pptxPathP51);
				}
				else
				{
					System.err.println("no entry 'p51' found in ini file");
					System.exit(1);
				}
	
				report.setParameter("processName", process.getName());
				report.setParameter("processVersion", process.getVersion());
				report.setParameter("processArchitectCompany", process.getArchitectCompany());
				report.setParameter("processArchitectName", process.getArchitectName());
				report.setParameter("processArchitectMail", process.getArchitectMail());
				report.setParameter("processCustomerCompany", process.getCustomerCompany());
				report.setParameter("processCustomerName", process.getCustomerName());
				report.setParameter("processCustomerMail", process.getCustomerMail());

				report.setParameter("stepName", actualStep.getName());
				report.setParameter("stepRank", stepRank);
				report.setParameter("stepDescription", actualStep.getDescription());
				
				String aufruf = "";
				if(actualStep.getWork() != null)
				{
					// zusammensetzen des scriptaufrufs
					String interpreter = "";
					
					if (actualStep.getWork().getInterpreter() != null)
					{
						interpreter = actualStep.getWork().getInterpreter();
					}
					
					aufruf = interpreter+" "+actualStep.getWork().getCommand();
					for(Callitem actualCallitem : actualStep.getWork().getCallitem())
					{
						aufruf += " "+actualCallitem.getPar();
						if( !(actualCallitem.getDel() == null) )
						{
							aufruf += actualCallitem.getDel();
						}
						if( !(actualCallitem.getVal() == null) )
						{
							aufruf += actualCallitem.getVal();
						}
					}
				}
				else if(actualStep.getSubprocess() != null)
				{
					aufruf = ini.get("apps", "pkraft-startinstance");
					aufruf += " --pdomain "+actualStep.getSubprocess().getDomain();
					aufruf += " --pname "+actualStep.getSubprocess().getName();
					aufruf += " --pversion "+actualStep.getSubprocess().getVersion();
					
					for(Commit actCommit : actualStep.getSubprocess().getStep().getCommit())
					{
						for(de.prozesskraft.pkraft.File actFile : actCommit.getFile())
						{
							aufruf += " --commitfile "+actFile.getGlob();
						}
						for(Variable actVariable : actCommit.getVariable())
						{
							aufruf += " --commitvariable "+actVariable.getKey()+"="+actVariable.getValue();
						}
					}
				}
				report.setParameter("stepWorkCall", aufruf);
				
				// P51x) bild an report melden
				report.setParameter("stepTopologyImagePath", stepTopologyImagePath.get(actualStep.getName()));
	
			
				// ueber alle lists iterieren
				for(List actualList : actualStep.getList())
				{
					HashMap<String,Object> row = new HashMap<String,Object>();
					
					// Spalte 'Woher?'
					row.put("origin", "-");
					
					// Spalte 'typ'
					row.put("objectType", "wert");
					
					// Spalte 'minOccur'
					row.put("minOccur", "-");
					
					// Spalte 'maxOccur'
					row.put("maxOccur", "-");

					// Spalte 'Label'
					row.put("objectKey", actualList.getName());
	
					// Spalte 'Label'
					String listString = actualList.getItem().toString();
					row.put("objectDescription", listString.substring(1, listString.length()-1));
	
					report.addField(row);
				}
				
				// ueber alle inits iterieren
				for(Init actualInit : actualStep.getInit())
				{
					HashMap<String,Object> row = new HashMap<String,Object>();
					
					// Spalte 'Woher?'
					if(actualInit.getFromstep().equals(process.getRootstepname()))
					{
						row.put("origin", "user/cb2");
					}
					else
					{
						row.put("origin", actualInit.getFromstep());
					}
					
					// Spalte 'typ'
					row.put("objectType", actualInit.getFromobjecttype());
					
					// Spalte 'minOccur'
					row.put("minOccur", ""+actualInit.getMinoccur());
					
					// Spalte 'maxOccur'
					row.put("maxOccur", ""+actualInit.getMaxoccur());
					
					// Spalte 'Label'
					row.put("objectKey", actualInit.getListname());
	
					// Spalte 'Label'
					row.put("objectDescription", "-");
	
					report.addField(row);
				}
	
				try {
					report.fillReportFileToFile(jasperPathP51, jasperFilledPathP51);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// export to pdf
				try {
					report.convertFileToPdf(jasperFilledPathP51, pdfPathP51);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// export to pptx
				try {
					report.convertFileToPptx(jasperFilledPathP51, pptxPathP51);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				report = null;
			}
		}
		
//////////////////////////////////////////

// fuer jeden Step einen eigenen Output Report erzeugen

		for(Step actualStep : process.getStep())
		{
			// root-step ueberspringen
			if (actualStep.getName().equals(process.getRootstepname()))
			{
				System.out.println("skipping step root");
			}
			
			// alle anderen auswerten
			else
			{
			
				report = new Reporter();
				
				// P52x) erstellen des p52
				System.out.println("info: generating p52 for step "+actualStep.getRank()+" => "+actualStep.getName());
				
				String stepRank = actualStep.getRank();
				
				String pdfPathP52 = null;
				String pptxPathP52 = null;
				String jasperPathP52 = null;
				String jasperFilledPathP52 = null;
				
				// P52x) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
				if (ini.get("pkraft-createdoc", "p52") != null )
				{
					pdfPathP52 = randomPathPdf+"/p5."+stepRank+".2.pdf";
					pptxPathP52 = randomPathPptx+"/p5."+stepRank+".2.pptx";
					jasperPathP52 = installDir.getAbsolutePath() + "/" + ini.get("pkraft-createdoc", "p52");
					jasperFilledPathP52 = randomPathJasperFilled+"/p5."+stepRank+".1.jasperFilled";
					
					String[] rankArray = stepRank.split("\\.");
					Integer[] rankArrayInt = new Integer[rankArray.length];
					for(int x=0; x < rankArray.length; x++)
					{
						rankArrayInt[x] = Integer.parseInt(rankArray[x]);
					}
					String rankFormated = String.format("%03d.%03d", rankArrayInt);

					pdfRankFiles.put(rankFormated+".2", pdfPathP52);
					pptxRankFiles.put(rankFormated+".2", pptxPathP52);
				}
				else
				{
					System.err.println("no entry 'p52' found in ini file");
					System.exit(1);
				}
				
				report.setParameter("processName", process.getName());
				report.setParameter("processVersion", process.getVersion());
				report.setParameter("processArchitectCompany", process.getArchitectCompany());
				report.setParameter("processArchitectName", process.getArchitectName());
				report.setParameter("processArchitectMail", process.getArchitectMail());
				report.setParameter("processCustomerCompany", process.getCustomerCompany());
				report.setParameter("processCustomerName", process.getCustomerName());
				report.setParameter("processCustomerMail", process.getCustomerMail());
				
				report.setParameter("stepName", actualStep.getName());
				report.setParameter("stepRank", stepRank);
				
				// logfile ermitteln
				String logfile ="-";
				if(actualStep.getWork() != null)
				{
					if (actualStep.getWork().getLogfile() == null || actualStep.getWork().getLogfile().equals(""))
					{
						report.setParameter("stepWorkLogfile", actualStep.getWork().getLogfile());
					}
				}
				else if(actualStep.getSubprocess() != null)
				{
					logfile = ".log";
				}
				report.setParameter("stepWorkLogfile", logfile);
				
				// zusammensetzen der return/exitcode informationen
				String exitInfo = "exit 0 = kein fehler aufgetreten";
				exitInfo += "\nexit >0 = ein fehler ist aufgetreten.";
				if(actualStep.getWork() != null)
				{
					for(Exit actualExit : actualStep.getWork().getExit())
					{
						exitInfo += "\nexit "+actualExit.getValue()+" = "+actualExit.getMsg();
					}
				}
				report.setParameter("stepWorkExit", exitInfo);
				
				// P52x) bild an report melden
				report.setParameter("stepTopologyImagePath", stepTopologyImagePath.get(actualStep.getName()));
				
				
				// ueber alle inits iterieren
				for(Commit actualCommit : actualStep.getCommit())
				{
				
					// ueber alle files iterieren
					for(de.prozesskraft.pkraft.File actualFile : actualCommit.getFile())
					{

						HashMap<String,Object> row = new HashMap<String,Object>();
					
						// Spalte 'destination'
						if (actualCommit.isTorootPresent())
						{
							row.put("destination", "user/cb2");
						}
						else
						{
							row.put("destination", "prozessintern");
						}
						
						// Spalte 'objectType'
						row.put("objectType", "file");
						
						// Spalte 'minOccur'
						row.put("minOccur", ""+actualFile.getMinoccur());
						
						// Spalte 'maxOccur'
						row.put("maxOccur", ""+actualFile.getMaxoccur());
						
						// Spalte 'objectKey'
						row.put("objectKey", actualFile.getKey());
						
						// Spalte 'objectDescription'
						row.put("objectDescription", actualFile.getDescription());
	
						// Datensatz dem report hinzufuegen
						report.addField(row);
					}

					// ueber alle variablen iterieren
					for(de.prozesskraft.pkraft.Variable actualVariable : actualCommit.getVariable())
					{
						HashMap<String,Object> row = new HashMap<String,Object>();
						
						// Spalte 'destination'
						if (actualCommit.isTorootPresent())
						{
							row.put("destination", "user/cb2");
						}
						else
						{
							row.put("destination", "prozessintern");
						}
						
						// Spalte 'objectType'
						row.put("objectType", "variable");
						
						// Spalte 'minOccur'
						row.put("minOccur", ""+actualVariable.getMinoccur());
						
						// Spalte 'maxOccur'
						row.put("maxOccur", ""+actualVariable.getMaxoccur());
						
						// Spalte 'objectKey'
						row.put("objectKey", actualVariable.getKey());
						
						// Spalte 'objectDescription'
						row.put("objectDescription", actualVariable.getDescription());
	
						// Datensatz dem report hinzufuegen
						report.addField(row);
					}
					
				}
				try {
					report.fillReportFileToFile(jasperPathP52, jasperFilledPathP52);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// export to pdf
				try {
					report.convertFileToPdf(jasperFilledPathP52, pdfPathP52);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// export to pptx
				try {
					report.convertFileToPptx(jasperFilledPathP52, pptxPathP52);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				report = null;
			}
		}

		// warten bis alles auf platte geschrieben ist
		try{
			Thread.sleep(1000);
		}
		catch (InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}
		
		// merge and output
		if(format.equals("pdf"))
		{
			mergePdf(pdfRankFiles, output);
		}
		else if(format.equals("pptx"))
		{
			mergePptx(pptxRankFiles, output);
		}

		
		
		
		
		
		
		
		
		System.out.println("info: generating process documentation ready.");
		System.exit(0);
	}
//////////////////////////////////////////

	
	/**
	 * merge the pdfs
	 */
	private static void mergePdf(Map<String,String> pdfRankFiles, String output)
	{
		System.out.println("merging pdfs to a single file");
		
		Set<String> keySet = pdfRankFiles.keySet();
		ArrayList<String> listKey = new ArrayList(keySet);
		Collections.sort(listKey);
		
		try
		{
			PDDocument document = new PDDocument();
//			if(document.getNumberOfPages() > 0)
//			{
//				System.out.println("deleting empty page");
//				document.removePage(0);
//			}
			for(String actualKey : listKey)
			{

				PDDocument part = PDDocument.load(pdfRankFiles.get(actualKey));
				System.out.println("merging "+pdfRankFiles.get(actualKey));
				ArrayList<PDPage> list = (ArrayList<PDPage>)part.getDocumentCatalog().getAllPages();
				for(PDPage page : list)
				{
					document.addPage(page);
				}
				
			}
			try
			{
				System.out.println("writing "+output);
				document.save(output);
			} catch (COSVisitorException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//////////////////////////////////////////
	/**
	 * merge the pptx
	 * Dieses mergePptx funktioniert nicht!
	 * Evtl. muss eine Lizenz von plutext.com fuer MergePptx gekauft werden
	 */
	private static void mergePptx(Map<String,String> pptxRankFiles, String output)
	{
		System.out.println("merging pptx to a single file");
		
		Set<String> keySet = pptxRankFiles.keySet();
		ArrayList<String> listKey = new ArrayList(keySet);
		Collections.sort(listKey);
		
		try
		{
			java.io.File targetFile = new java.io.File(output);
			
			PresentationMLPackage targetPackage = PresentationMLPackage.createPackage();
			
			MainPresentationPart pp = (MainPresentationPart) targetPackage.getParts().getParts().get(new PartName("/ppt/presentation.xml"));
//			SlideLayoutPart layoutPart = (SlideLayoutPart) targetPackage.getParts().getParts().get(new PartName("/ppt/slideLayouts/slideLayout1.xml"));
			
//			int counter = 1;
			
			for(String actualKey : listKey)
			{
				java.io.File sourceFile = new java.io.File(pptxRankFiles.get(actualKey));
				PresentationMLPackage sourcePackage = (PresentationMLPackage) OpcPackage.load(sourceFile);
				
				SlidePart slidePart = sourcePackage.getMainPresentationPart().getSlide(0);
				Relationship rel = slidePart.getSourceRelationship();
				
				
				slidePart.setPartName(new PartName("/ppt/slides/"+actualKey));
				System.err.println("creating slidePart "+"/ppt/slides/"+actualKey);
				
				RelationshipsPart slidePartRel = slidePart.getRelationshipsPart();

//----
//				slidePartRel.setPartName(new PartName("/ppt/slides/_rel/"+actualKey+".rel"));
//				
//				RelationshipsPart slidePartRel = new RelationshipsPart(slidePart.getPartName());
//				slidePartRel.addPart(slidePart, "slide", ctm);
//				
//				pp.addTargetPart(slidePartRel);
//				pp.addTargetPart(slidePart);
//----
				
				pp.addSlide(slidePart);
				
			}
			
			System.out.println("slide added");
			targetPackage.save(targetFile);
			
		}
		catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Pptx4jException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Open the window.
	 */
	public static void open()
	{
//		Display display = Display.getDefault();
		shell.open();
		shell.layout();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected static void createContents(PmodelViewPage page)
	{
		shell = new Shell();
		shell.setSize(3500, 1500);
		shell.setText("SWT Application");
		shell.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shell, SWT.NONE);

		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));

		Composite composite2 = new Composite(composite, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		GridData gd_composite2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite2.minimumWidth = 10;
		gd_composite2.minimumHeight = 10;
		composite2.setLayoutData(gd_composite2);

		Frame frame = SWT_AWT.new_Frame(composite2);
		
//		Label lblNewLabel = new Label(composite, SWT.BORDER);
//		lblNewLabel.setBounds(100, 10, 66, 17);
//		lblNewLabel.setText("sxsdsds");
		
		frame.add(page, BorderLayout.CENTER);
		page.init();
		frame.pack();
		frame.setLocation(0, 0);
		frame.setVisible(true);
		
		page.refresh();
	}

	

}
