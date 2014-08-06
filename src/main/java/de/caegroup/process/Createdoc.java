package de.caegroup.process;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption.*;
import java.sql.Timestamp;
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

import de.caegroup.commons.AutoCropBorder;
import de.caegroup.commons.WhereAmI;
import de.caegroup.pmodel.PmodelViewPage;
import de.caegroup.process.Process;
import de.caegroup.report.*;

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
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(Createdoc.class) + "/" + "../etc/process-createdoc.ini");

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
		  die eigentliche business logic
		----------------------------*/
		
		Process process = new Process();
		Report report;

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
		page.einstellungen.setProcess(process);
		page.einstellungen.getProcess().setStepRanks();
		page.einstellungen.setSize(100);
		page.einstellungen.setZoom(100);
//		page.einstellungen.setZoom(8 * 100/process.getMaxLevel());
		page.einstellungen.setLabelsize(0);
		page.einstellungen.setTextsize(0);
		page.einstellungen.setRanksize(7);
		page.einstellungen.setWidth(2500);
		page.einstellungen.setHeight(750);
		page.einstellungen.setGravx(10);
		page.einstellungen.setGravy(0);
		page.einstellungen.setRootpositionratiox((float)0.05);
		page.einstellungen.setRootpositionratioy((float)0.5);
	
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
			if (actualStep.getName().equals(process.getRootstepname()));
			
			String stepImagePath = randomPathPng+"/step_"+actualStep.getName()+"_Topology.png";
			
			// Farbe des Steps auf working aendern
			actualStep.setStatus("working");
			
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
			actualStep.setStatus("waiting");
			
			System.out.println("erstelle bild fuer step: "+actualStep.getName());

			long jetzt2 = System.currentTimeMillis();
			while (System.currentTimeMillis() < jetzt2 + 1000)
			{
				
			}
		}

		page.destroy();

//////////////////////////////////////////
		report = new Report();
		
		// P03) erstellen des p03
		System.out.println("info: generating p03.");
		
		// P03) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p03") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p03"));
			report.setJasperFilled(randomPathJasperFilled+"/p03.jasperFilled");
			report.setPdf(randomPathPdf+"/p03.pdf");
			pdfRankFiles.put("0.0.03", randomPathPdf+"/p03.pdf");
			report.setPptx(randomPathPptx+"/p03.pptx");
			pptxRankFiles.put("0.0.03", randomPathPptx+"/p03.pptx");
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
		report.setParameter("processArchitectLogoImagePath", ini.get("process-createdoc", "logo"));
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		
		try {
			report.fillPReport();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.exportToPdf();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.exportToPptx();
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
		report = new Report();
		
		// P05) erstellen des p05
		System.out.println("info: generating p05.");
		
		// P05) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p05") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p05"));
			report.setJasperFilled(randomPathJasperFilled+"/p05.jasperFilled");
			report.setPdf(randomPathPdf+"/p05.pdf");
			pdfRankFiles.put("0.0.05", randomPathPdf+"/p05.pdf");
			report.setPptx(randomPathPptx+"/p05.pptx");
			pptxRankFiles.put("0.0.05", randomPathPptx+"/p05.pptx");
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
			report.fillPReport();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pdf
		try {
			report.exportToPdf();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.exportToPptx();
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
		
		report = new Report();
		
		// P10) erstellen des p10
		System.out.println("info: generating p10.");
		
		// P10) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p10") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p10"));
			report.setJasperFilled(randomPathJasperFilled+"/p10.jasperFilled");
			report.setPdf(randomPathPdf+"/p10.pdf");
			pdfRankFiles.put("0.1.0", randomPathPdf+"/p10.pdf");
			report.setPptx(randomPathPptx+"/p10.pptx");
			pptxRankFiles.put("0.1.0", randomPathPptx+"/p10.pptx");
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
			for(de.caegroup.process.File actualFile : actualCommit.getFile())
			{
				HashMap<String,Object> row = new HashMap<String,Object>();
				
				// Spalte 'objectType'
				row.put("origin", "user/cb2");
				
				// Spalte 'objectType'
				row.put("objectType", "datei");
				
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
			for(de.caegroup.process.Variable actualVariable : actualCommit.getVariable())
			{
				HashMap<String,Object> row = new HashMap<String,Object>();
				
				// Spalte 'objectType'
				row.put("origin", "user/cb2");
				
				// Spalte 'objectType'
				row.put("objectType", "wert");
				
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
			report.fillPReport();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// export to pdf
		try {
			report.exportToPdf();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.exportToPptx();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
		
//////////////////////////////////////////

		report = new Report();
		
		// P20) erstellen des p20
		System.out.println("info: generating p20.");
		
		// P20) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p20") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p20"));
			report.setJasperFilled(randomPathJasperFilled+"/p20.jasperFilled");
			report.setPdf(randomPathPdf+"/p20.pdf");
			pdfRankFiles.put("0.2.0", randomPathPdf+"/p20.pdf");
			report.setPptx(randomPathPptx+"/p20.pptx");
			pptxRankFiles.put("0.2.0", randomPathPptx+"/p20.pptx");
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
					if(actualCommit.getToroot())
					{
						// ueber alle files iterieren
						for(de.caegroup.process.File actualFile : actualCommit.getFile())
						{
	
							HashMap<String,Object> row = new HashMap<String,Object>();
						
							// Spalte 'objectType'
							row.put("destination", "user/cb2");
							
							// Spalte 'objectType'
							row.put("objectType", "datei");
							
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
						for(de.caegroup.process.Variable actualVariable : actualCommit.getVariable())
						{
							HashMap<String,Object> row = new HashMap<String,Object>();
							
							// Spalte 'objectType'
							row.put("destination", "user/cb2");
		
							// Spalte 'objectType'
							row.put("objectType", "wert");
							
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
				}
			}

		}
		
		try {
			report.fillPReport();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// export to pdf
		try {
			report.exportToPdf();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.exportToPptx();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		report = null;
	
//////////////////////////////////////////

		report = new Report();
		
		// P30) erstellen des p30
		System.out.println("info: generating p30.");
		
		// P30) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p30") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p30"));
			report.setJasperFilled(randomPathJasperFilled+"/p30.jasperFilled");
			report.setPdf(randomPathPdf+"/p30.pdf");
			pdfRankFiles.put("0.3.0", randomPathPdf+"/p30.pdf");
			report.setPptx(randomPathPptx+"/p30.pptx");
			pptxRankFiles.put("0.3.0", randomPathPptx+"/p30.pptx");
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
			report.fillPReport();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// export to pdf
		try {
			report.exportToPdf();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// export to pptx
		try {
			report.exportToPptx();
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
		
		report = new Report();
			
		// P40) erstellen des p40
		System.out.println("info: generating p40.");
		
		// P40) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p40") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p40"));
			report.setJasperFilled(randomPathJasperFilled+"/p40.jasperFilled");
			report.setPdf(randomPathPdf+"/p40.pdf");
			pdfRankFiles.put("0.4.0", randomPathPdf+"/p40.pdf");
			report.setPptx(randomPathPptx+"/p40.pptx");
			pptxRankFiles.put("0.4.0", randomPathPptx+"/p40.pptx");
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
			report.fillPReport();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// export to pdf
		try {
			report.exportToPdf();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// export to pptx
		try {
			report.exportToPptx();
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
				
				report = new Report();
				
				// P51x) erstellen des p51
				System.out.println("info: generating p51 for step "+actualStep.getRank()+" => "+actualStep.getName());
				
				String stepRank = actualStep.getRank();
				
				// P51x) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
				if (ini.get("process-createdoc", "p51") != null )
				{
					report.setJasper(ini.get("process-createdoc", "p51"));
					report.setJasperFilled(randomPathJasperFilled+"/p5."+stepRank+".1.jasperFilled");
					report.setPdf(randomPathPdf+"/p5."+stepRank+".1.pdf");
					report.setPptx(randomPathPptx+"/p5."+stepRank+".1.pptx");
					String[] rankArray = stepRank.split("\\.");
					Integer[] rankArrayInt = new Integer[rankArray.length];
					for(int x=0; x < rankArray.length; x++)
					{
						rankArrayInt[x] = Integer.parseInt(rankArray[x]);
					}
					String rankFormated = String.format("%03d.%03d", rankArrayInt);

					pdfRankFiles.put(rankFormated+".1", randomPathPdf+"/p5."+stepRank+".1.pdf");
					pptxRankFiles.put(rankFormated+".1", randomPathPptx+"/p5."+stepRank+".1.pptx");
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
				
				// zusammensetzen des scriptaufrufs
				String interpreter = "";
				
				if (actualStep.getWork().getInterpreter() != null)
				{
					interpreter = actualStep.getWork().getInterpreter();
				}
				
				String aufruf = interpreter+" "+actualStep.getWork().getCommand();
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
					report.fillPReport();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// export to pdf
				try {
					report.exportToPdf();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// export to pptx
				try {
					report.exportToPptx();
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
			
				report = new Report();
				
				// P52x) erstellen des p52
				System.out.println("info: generating p52 for step "+actualStep.getRank()+" => "+actualStep.getName());
				
				String stepRank = actualStep.getRank();
				
				// P52x) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
				if (ini.get("process-createdoc", "p52") != null )
				{
					report.setJasper(ini.get("process-createdoc", "p52"));
					report.setJasperFilled(randomPathJasperFilled+"/p5."+stepRank+".2.jasperFilled");
					report.setPdf(randomPathPdf+"/p5."+stepRank+".2.pdf");
					report.setPptx(randomPathPptx+"/p5."+stepRank+".2.pptx");
					String[] rankArray = stepRank.split("\\.");
					Integer[] rankArrayInt = new Integer[rankArray.length];
					for(int x=0; x < rankArray.length; x++)
					{
						rankArrayInt[x] = Integer.parseInt(rankArray[x]);
					}
					String rankFormated = String.format("%03d.%03d", rankArrayInt);
					
					pdfRankFiles.put(rankFormated+".2", randomPathPdf+"/p5."+stepRank+".2.pdf");
					pptxRankFiles.put(rankFormated+".2", randomPathPptx+"/p5."+stepRank+".2.pptx");
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
				if (actualStep.getWork().getLogfile() == null || actualStep.getWork().getLogfile().equals(""))
				{
					report.setParameter("stepWorkLogfile", "-");
				}
				else
				{
					report.setParameter("stepWorkLogfile", actualStep.getWork().getLogfile());
				}

				// zusammensetzen der return/exitcode informationen
				String exitInfo = "exit 0 = kein fehler aufgetreten";
				exitInfo += "\nexit >0 = ein fehler ist aufgetreten.";
				for(Exit actualExit : actualStep.getWork().getExit())
				{
					exitInfo += "\nexit "+actualExit.getValue()+" = "+actualExit.getMsg();
				}
				report.setParameter("stepWorkExit", exitInfo);
				
				// P52x) bild an report melden
				report.setParameter("stepTopologyImagePath", stepTopologyImagePath.get(actualStep.getName()));
				
				
				// ueber alle inits iterieren
				for(Commit actualCommit : actualStep.getCommit())
				{
				
					// ueber alle files iterieren
					for(de.caegroup.process.File actualFile : actualCommit.getFile())
					{

						HashMap<String,Object> row = new HashMap<String,Object>();
					
						// Spalte 'destination'
						if (actualCommit.getToroot())
						{
							row.put("destination", "user/cb2");
						}
						else
						{
							row.put("destination", "prozessintern");
						}
						
						// Spalte 'objectType'
						row.put("objectType", "datei");
						
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
					for(de.caegroup.process.Variable actualVariable : actualCommit.getVariable())
					{
						HashMap<String,Object> row = new HashMap<String,Object>();
						
						// Spalte 'destination'
						if (actualCommit.getToroot())
						{
							row.put("destination", "user/cb2");
						}
						else
						{
							row.put("destination", "prozessintern");
						}
						
						// Spalte 'objectType'
						row.put("objectType", "wert");
						
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
					report.fillPReport();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// export to pdf
				try {
					report.exportToPdf();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// export to pptx
				try {
					report.exportToPptx();
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
				
				slidePart.setPartName(new PartName("/ppt/slides/"+actualKey));
				
				RelationshipsPart slidePartRel = slidePart.getRelationshipsPart();
				slidePartRel.setPartName(slidePart.getPartName());
				
//				RelationshipsPart slidePartRel = new RelationshipsPart(slidePart.getPartName());
//				slidePartRel.addPart(slidePart, "slide", ctm)
				
				pp.addTargetPart(slidePart);
				
				
				
				
//				Map<PartName,Part> partMap = sourcePackage.getParts().getParts();
//				for(PartName name : partMap.keySet())
//				{
//					Part part = partMap.get(name);
//					if(part instanceof SlidePart)
//					{
////						SlidePart slide = (SlidePart) part;
////						SlidePart slidePart = PresentationMLPackage.createSlidePart(pp, layoutPart, new PartName("/ppt/slides/slide"+ counter++ +".xml"));
//						SlidePart slidePart = sourcePackage.getMainPresentationPart().getSlide(0);
//						pp.addTargetPart(slidePart);
////						slidePart.setJaxbElement(slide.getJaxbElement());
//					}
//				}
			}
			
			targetPackage.save(targetFile);
			
//			PDDocument document = new PDDocument();
//			for(String actualKey : listKey)
//			{
//
//				PDDocument part = PDDocument.load(pdfRankFiles.get(actualKey));
//				System.out.println("merging "+pdfRankFiles.get(actualKey));
//				ArrayList<PDPage> list = (ArrayList<PDPage>)part.getDocumentCatalog().getAllPages();
//				for(PDPage page : list)
//				{
//					document.addPage(page);
//				}
//				
//			}
//			try
//			{
//				System.out.println("writing "+output);
//				document.save(output);
//			} catch (COSVisitorException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
//		catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
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
