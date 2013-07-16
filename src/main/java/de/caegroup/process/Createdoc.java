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
		Report report = new Report();

		process.setInfilexml(definition);
		
		System.out.println("info: reading process definition "+definition);

		try
		{
			System.out.println("Anzahl der Steps ist: "+process.getStep().size());
			process.readXml();
			System.out.println("Anzahl der Steps ist: "+process.getStep().size());

			// feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
			if (ini.get("process-createdoc", type+"-jrxml") != null )
			{
				report.setJrxml(ini.get("process-createdoc", type+"-jrxml"));
			}
			else
			{
				System.err.println("no entry '"+type+"-jrxml' found in ini file");
				System.exit(1);
			}
			
			report.setParameter("processName", process.getName());
			report.setParameter("processVersion", process.getVersion());
			report.setParameter("processArchitect", process.getArchitect());
			report.setParameter("processStepCount", process.getStep().size());
			report.setParameter("processDescription", process.getDescription());
			
			// konfigurieren der processing ansicht
			PmodelViewPage page = new PmodelViewPage(process);
			page.einstellungen.setZoom(50);
			page.einstellungen.setLabelsize(0);
			page.einstellungen.setTextsize(0);
			page.einstellungen.setRanksize(10);
			page.einstellungen.setWidth(350);
			page.einstellungen.setHeight(375);
			page.einstellungen.setGravx(0);
			page.einstellungen.setGravy(5);
			page.einstellungen.setRootpositionratiox((float)0.5);
			page.einstellungen.setRootpositionratioy((float)0.1);

			createContents(page);
//			open();

			System.out.println("Anzahl der Steps ist: "+process.getStep().size());
			// verzoegerung, damit die symbole sich gut ausrichten koennen
			long start = System.currentTimeMillis();
			System.out.println("sleeping for 3 seconds.");

			while(System.currentTimeMillis() < (start + 3000))
			{
//				try
//				{
//					System.out.println("sleeping.");
//					Thread.sleep(1000);
//					page.einstellungen.setGravy(5);
//				} catch (InterruptedException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
			
			// bild speichern
			String randomPath = "/tmp/"+System.currentTimeMillis()+".jpg";
			page.save(randomPath);
			page.destroy();
			
			// Tabelle erzeugen

			System.out.println("Anzahl der Steps ist: "+process.getStep().size());
			ArrayList<Step> steps = process.getStep();
			System.out.println("Anzahl der Steps ist: "+process.getStep().size());
//			System.exit(1);
			for(int x = 0; x < steps.size(); x++)
			{
				HashMap<String,Object> row = new HashMap<String,Object>();
				Step actualStep = steps.get(x);

				// erste Spalte ist 'rank'
				row.put("stepRank", process.detStepRank(actualStep.getName()));
				System.out.println("stepRank: "+process.detStepRank(actualStep.getName()));
				
				// zweite Spalte ist 'stepname'
				row.put("stepName", actualStep.getName());
				System.out.println("stepName: "+actualStep.getName());

				// dritte Spalte ist 'Beschreibung'
				row.put("stepDescription", actualStep.getDescription());
				System.out.println("stepRank: "+actualStep.getDescription());

				report.addField(row);
			}
			
			// fenster schliessen
//			shell.close();
//			shell.dispose();
//			display.dispose();

			report.setParameter("processTopologyImagePath", randomPath);

			report.setJasper("/home/avo/jasper.jasper");
			report.setJasperFilled("/home/avo/jasperFilled.jasper");
			
			report.compile();
			report.fillPReport();
			
			// mit dem ensprechenden exporter den report rausschreiben
			if (format.equals("pdf"))
			{
				report.setPdf(output);
				report.exportToPdf();
			}
			if (format.equals("odt"))
			{
				report.setOdt(output);
				report.exportToOdt();
			}
			if (format.equals("docx"))
			{
				report.setDocx(output);
				report.exportToDocx();
			}
			if (format.equals("html"))
			{
				report.setHtml(output);
				report.exportToHtml();
			}
			
		}
		catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("info: writing process documentation "+output);
		System.exit(0);
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
		shell.setSize(1200, 800);
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
