package de.caegroup.process;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
		Option ov = new Option("v", "prints version and build-date");
		
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
		options.addOption( ov );
		options.addOption( odefinition );
		options.addOption( otype );
		options.addOption( oformat );
		options.addOption( ooutput );
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

		// mit open kann die page angezeigt werden
//		open();
		
		// 20 sekunden warten
		System.out.println("20 sekunden warten.");
		long jetzt5 = System.currentTimeMillis();
		while (System.currentTimeMillis() < jetzt5 + 20000)
		{
			
		}
		
		// VORBEREITUNG) bild speichern
		processTopologyImagePath = randomPathPng+"/processTopology.png";
		page.savePic(processTopologyImagePath);
		// zuerst 1 sekunde warten, dann autocrop
		long jetzt = System.currentTimeMillis();
		while (System.currentTimeMillis() < jetzt + 1000)
		{
			
		}
		try
		{
			new AutoCropBorder(processTopologyImagePath);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			try
			{
				new AutoCropBorder(stepImagePath);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			stepTopologyImagePath.put(actualStep.getName(), stepImagePath);
			
			// farbe wieder auf grau aendern
			actualStep.setStatus("waiting");
			
			System.out.println("fuer step: "+actualStep.getName());

			long jetzt2 = System.currentTimeMillis();
			while (System.currentTimeMillis() < jetzt2 + 1000)
			{
				
			}

		}
		
		page.destroy();

		
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
			pdfRankFiles.put("0.0.1", randomPathPdf+"/p1.pdf");
		}
		else
		{
			System.err.println("no entry 'p1' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getModelVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		
		// P1) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);

		// P1) report fuellen
		try
		{
			report.fillPReport();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// P1) pdf schreiben
		try
		{
			report.exportToPdf();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			pdfRankFiles.put("0.0.2", randomPathPdf+"/p2.pdf");
		}
		else
		{
			System.err.println("no entry 'p2' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getModelVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());
		
		// P2) bild an report melden
		report.setParameter("processTopologyImagePath", processTopologyImagePath);

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

			// wenn nicht der root-step, dann row eintragen
			if (!(actualStep.getName().equals(process.getRootstepname())))
			{
				report.addField(row);
			}
		}
		
		// P2) report fuellen
		try
		{
			report.fillPReport();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// P2) pdf schreiben
		try
		{
			report.exportToPdf();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
			pdfRankFiles.put("0.0.3", randomPathPdf+"/p3.pdf");
		}
		else
		{
			System.err.println("no entry 'p3' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getModelVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());

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

		// P3) report fuellen
		try
		{
			report.fillPReport();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// P3) pdf schreiben
		try
		{
			report.exportToPdf();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
			pdfRankFiles.put("0.0.4", randomPathPdf+"/p4.pdf");
		}
		else
		{
			System.err.println("no entry 'p4' found in ini file");
			System.exit(1);
		}
		
		report.setParameter("processName", process.getName());
		report.setParameter("processVersion", process.getModelVersion());
		report.setParameter("processArchitectCompany", process.getArchitectCompany());
		report.setParameter("processArchitectName", process.getArchitectName());
		report.setParameter("processArchitectMail", process.getArchitectMail());
		report.setParameter("processCustomerCompany", process.getCustomerCompany());
		report.setParameter("processCustomerName", process.getCustomerName());
		report.setParameter("processCustomerMail", process.getCustomerMail());

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
		
		// P4) report fuellen
		try
		{
			report.fillPReport();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// P4) pdf schreiben
		try
		{
			report.exportToPdf();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JRException e)
		{
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
				System.out.println("info: generating p51 for step "+process.detStepRank(actualStep.getName())+" => "+actualStep.getName());
				
				String stepRank = process.detStepRank(actualStep.getName());
				
				// P51x) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
				if (ini.get("process-createdoc", "p51") != null )
				{
					report.setJasper(ini.get("process-createdoc", "p51"));
					report.setJasperFilled(randomPathJasperFilled+"/p5."+stepRank+".1.jasperFilled");
					report.setPdf(randomPathPdf+"/p5."+stepRank+".1.pdf");
					pdfRankFiles.put(stepRank+".1", randomPathPdf+"/p5."+stepRank+".1.pdf");
				}
				else
				{
					System.err.println("no entry 'p51' found in ini file");
					System.exit(1);
				}
	
				report.setParameter("processName", process.getName());
				report.setParameter("processVersion", process.getModelVersion());
				report.setParameter("processArchitectCompany", process.getArchitectCompany());
				report.setParameter("processArchitectName", process.getArchitectName());
				report.setParameter("processArchitectMail", process.getArchitectMail());
				report.setParameter("processCustomerCompany", process.getCustomerCompany());
				report.setParameter("processCustomerName", process.getCustomerName());
				report.setParameter("processCustomerMail", process.getCustomerMail());

				report.setParameter("stepName", actualStep.getName());
				report.setParameter("stepRank", stepRank);
				// P51x) bild an report melden
				report.setParameter("stepTopologyImagePath", stepTopologyImagePath.get(actualStep.getName()));
	
			
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
					row.put("objectKey", actualInit.getName());
	
					report.addField(row);
				}
	
				// P51x) report fuellen
				try
				{
					report.fillPReport();
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
				// P51x) pdf schreiben
				try
				{
					report.exportToPdf();
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e)
				{
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
				System.out.println("info: generating p52 for step "+process.detStepRank(actualStep.getName())+" => "+actualStep.getName());
				
				String stepRank = process.detStepRank(actualStep.getName());
				
				// P52x) feststellen, welches jasperreports-template fuer den angeforderten typ verwendet werden soll
				if (ini.get("process-createdoc", "p52") != null )
				{
					report.setJasper(ini.get("process-createdoc", "p52"));
					report.setJasperFilled(randomPathJasperFilled+"/p5."+stepRank+".2.jasperFilled");
					report.setPdf(randomPathPdf+"/p5."+stepRank+".2.pdf");
					pdfRankFiles.put(stepRank+".2", randomPathPdf+"/p5."+stepRank+".2.pdf");
				}
				else
				{
					System.err.println("no entry 'p52' found in ini file");
					System.exit(1);
				}
				
				report.setParameter("processName", process.getName());
				report.setParameter("processVersion", process.getModelVersion());
				report.setParameter("processArchitectCompany", process.getArchitectCompany());
				report.setParameter("processArchitectName", process.getArchitectName());
				report.setParameter("processArchitectMail", process.getArchitectMail());
				report.setParameter("processCustomerCompany", process.getCustomerCompany());
				report.setParameter("processCustomerName", process.getCustomerName());
				report.setParameter("processCustomerMail", process.getCustomerMail());
				
				report.setParameter("stepName", actualStep.getName());
				report.setParameter("stepRank", stepRank);
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
				// P52x) report fuellen
				try
				{
					report.fillPReport();
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// P52x) pdf schreiben
				try
				{
					report.exportToPdf();
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JRException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				report = null;
			}
		}

		mergePdf(pdfRankFiles, output);

		
		
		
		
		
		
		
		
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
