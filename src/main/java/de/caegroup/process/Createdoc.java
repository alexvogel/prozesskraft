package de.caegroup.process;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

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
import org.apache.xerces.impl.xpath.regex.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

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
	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException
	{

		Createdoc tmp = new Createdoc();
		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = WhereAmI.getDefaultInifile(tmp.getClass());

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
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option odefinition = OptionBuilder.withArgName("definition")
				.hasArg()
				.withDescription("[mandatory] process definition file")
				.isRequired()
				.create("definition");
		
		Option otype = OptionBuilder.withArgName("type")
				.hasArg()
				.withDescription("[optional, default=full] documentation type (full)")
				.create("type");
		
		Option oformat = OptionBuilder.withArgName("format")
				.hasArg()
				.withDescription("[optional, default=pdf] output format (pdf|odt|docx|html) ")
				.create("format");
		
		Option ooutput = OptionBuilder.withArgName("output")
				.hasArg()
				.withDescription("[mandatory] output file (pdf) with documentation of process definition")
				.isRequired()
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
		options.addOption( odefinition );
		options.addOption( otype );
		options.addOption( oformat );
		options.addOption( ooutput );
////		options.addOption( property );
		
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
			formatter.printHelp("createdoc", options);
			System.exit(0);
		}
		
		/*----------------------------
		  die variablen festlegen
		----------------------------*/
		int error = 0;
		String definition = null;
		String type = null;
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
		
		// festlegen von type
		if ( line.hasOption("type") )
		{
			type = line.getOptionValue("type");
		}
		else
		{
			type = "full";
		}
		
		// festlegen von format
		if ( line.hasOption("format") )
		{
			if (line.getOptionValue("format").matches("pdf|odt|docx|html"))
			{
				format = line.getOptionValue("format");
			}
			else
			{
				System.err.println("for -format use only pdf|odt|docx|html");
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
			output = "out."+type;
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
		
		new File(randomPathJasperFilled).mkdirs();
		new File(randomPathPng).mkdirs();
		new File(randomPathPdf).mkdirs();
		
//////////////////////////////////////////

		// erstellen der Bilder
		
		// konfigurieren der processing ansicht
//		PmodelViewPage page = new PmodelViewPage(process);
		PmodelViewPage page = new PmodelViewPage(process);
		page.einstellungen.setZoom(160);
		page.einstellungen.setLabelsize(0);
		page.einstellungen.setTextsize(0);
		page.einstellungen.setRanksize(7);
		page.einstellungen.setWidth(1500);
		page.einstellungen.setHeight(750);
		page.einstellungen.setGravx(20);
		page.einstellungen.setGravy(0);
		page.einstellungen.setRootpositionratiox((float)0.1);
		page.einstellungen.setRootpositionratioy((float)0.5);
	
		createContents(page);
		open();
		
		// VORBEREITUNG) bild speichern
		processTopologyImagePath = randomPathPng+"/processTopology.png";
		page.save(processTopologyImagePath);
		page.destroy();

		//  VORBEREITUNG) Autocrop all images
		new AutoCropBorder(processTopologyImagePath);
		
		
//////////////////////////////////////////
		 report = new Report();
		
		// P1) erstellen des p1
		System.out.println("info: generating p1.");
		
		// P1) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p1") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p1"));
			report.setJasperFilled(randomPathJasperFilled+"/p1.jasperFilled");
			report.setPdf(randomPathPdf+"/p1.pdf");
		}
		else
		{
			System.err.println("no entry 'p1' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitect", process.getArchitect());
		
		// P1) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);

		// P1) report fuellen
		report.fillPReport();

		// P1) pdf schreiben
		report.exportToPdf();

		report = null;
		
//		System.exit(0);
//////////////////////////////////////////
		
		report = new Report();
			
		// P2) erstellen des p2
		System.out.println("info: generating p2.");
		
		// P2) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p2") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p2"));
			report.setJasperFilled(randomPathJasperFilled+"/p2.jasperFilled");
			report.setPdf(randomPathPdf+"/p2.pdf");
		}
		else
		{
			System.err.println("no entry 'p2' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitect", process.getArchitect());
		
		// Tabelle erzeugen

		ArrayList<Step> steps = process.getStep();
		for(int x = 0; x < steps.size(); x++)
		{
			HashMap<String,Object> row = new HashMap<String,Object>();
			Step actualStep = steps.get(x);

			// erste Spalte ist 'rank'
			row.put("stepRank", process.detStepRank(actualStep.getName()));
			
			// zweite Spalte ist 'stepname'
			row.put("stepName", actualStep.getName());
//				System.out.println("stepName: "+actualStep.getName());

			// dritte Spalte ist 'Beschreibung'
			row.put("stepDescription", actualStep.getDescription());
//				System.out.println("stepRank: "+actualStep.getDescription());

			report.addField(row);
		}
		
		// P2) report fuellen
		report.fillPReport();

		// P2) pdf schreiben
		report.exportToPdf();
		
		report = null;
		
//////////////////////////////////////////
		
		report = new Report();
		
		// P3) erstellen des p3
		System.out.println("info: generating p3.");
		
		// P3) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p3") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p3"));
			report.setJasperFilled(randomPathJasperFilled+"/p3.jasperFilled");
			report.setPdf(randomPathPdf+"/p3.pdf");
		}
		else
		{
			System.err.println("no entry 'p3' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitect", process.getArchitect());
		// P3) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);
		
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
				row.put("origin", "extern");
				
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
				row.put("origin", "extern");
				
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

		// P3) report fuellen
		report.fillPReport();

		// P3) pdf schreiben
		report.exportToPdf();
		
		report = null;
		
//////////////////////////////////////////

		report = new Report();
		
		// P4) erstellen des p4
		System.out.println("info: generating p4.");
		
		// P4) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
		if (ini.get("process-createdoc", "p4") != null )
		{
			report.setJasper(ini.get("process-createdoc", "p4"));
			report.setJasperFilled(randomPathJasperFilled+"/p4.jasperFilled");
			report.setPdf(randomPathPdf+"/p4.pdf");
		}
		else
		{
			System.err.println("no entry 'p4' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getVersion());
		report.setParameter("processArchitect", process.getArchitect());
		// P4) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);
		
		// ueber alle steps iterieren (ausser root)
		for(Step actualStep : process.getStep())
		{
			
			// ueberspringen wenn es sich um root handelt
			if(actualStep.getName().equals(process.getRootstepname()))
			{
				break;
			}
		
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
						row.put("destination", "extern");
						
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
						row.put("destination", "extern");
						
	
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
		
		// P4) report fuellen
		report.fillPReport();

		// P4) pdf schreiben
		report.exportToPdf();
		
		report = null;
	

		
		
		System.out.println("info: generating process documentation ready.");
		System.exit(0);
	}
//////////////////////////////////////////

	
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
		shell.setSize(1500, 750);
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
	}

	

}
