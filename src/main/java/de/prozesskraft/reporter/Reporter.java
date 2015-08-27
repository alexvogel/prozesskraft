package de.prozesskraft.reporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
//import de.caegroup.process.Process;
import net.sf.jasperreports.engine.util.JRLoader;

public class Reporter
{
	/*----------------------------
	  structure
	----------------------------*/
//	private Process process = null;
	private String name = null;	// hauptreport.name = main

	private String jrxml = null;
//	private String jasper = null;
//	private String jasperFilled = null;
//	private String pdf = null;
//	private String html = null;
//	private String odt = null;
//	private String docx = null;
//	private String pptx = null;

	private JasperReport jasperReport = null;
//	private OutputStream jasperReportOutputStream = null;
	private JasperPrint jasperPrint = null;

	// parameter
	private Map<String,Object> parameter = new HashMap<String,Object>();
	// field (dataconnection)
	private List<Map<String,?>> field = new ArrayList<Map<String, ?>> ();
//	private String fieldCsvPath = null;

	private Map<String,Reporter> subreports = new HashMap<String,Reporter>();

	/*----------------------------
	  constructors
	----------------------------*/
	public Reporter()
	{
//		this.process = process;
//		this.jrxml = jrxml;
//		this.jasperReport = JasperCompileManager.compileReport(jrxml);
	}
	
	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * compiles jrxml to internal jasperReport
	 * @throws JRException
	 */
	public void compile() throws JRException
	{
		this.jasperReport = JasperCompileManager.compileReport(jrxml);
	}

	public static void compileFileToFile(String jrxmlPath, String jasperPath) throws JRException
	{
		// file2file compilen
		JasperCompileManager.compileReportToFile(jrxmlPath, jasperPath);
	}

	public void printPlaceholder()
	{
//		if (this.jrxml != null)
//		{
//			try {
//				this.jasperReport = JasperCompileManager.compileReport(jrxml);
//			} catch (JRException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			System.out.println("to fill the template use this example call:");
			System.out.println("reporter generate --template " + this.jrxml + " \\");
			JRParameter[] parameter = this.jasperReport.getParameters();
			
			String zumSchlussAusgeben = "";
			
			for(JRParameter actParameter : parameter)
			{
				// wenn parameter mit mit 2 grossbuchstaben anfaengt, soll er auf stderr ausgegeben werden
				if(actParameter.getName().matches("^[A-Z]{2}.*$"))
				{
					zumSchlussAusgeben += "# skipping -parameter " + actParameter.getName() + "=" + actParameter.getValueClassName() + " \\\n";
				}
				else
				{
					System.out.println("-parameter " + actParameter.getName() + "=" + actParameter.getValueClassName() + " \\");
				}
			}

			System.err.println(zumSchlussAusgeben);

			// gibt es fields? dann auch ausgeben
			if(this.jasperReport.getFields() != null)
			{
				JRField[] field = this.jasperReport.getFields();
				for(JRField actField : field)
				{
					System.out.println("-field " + actField.getName() + "=" + actField.getValueClassName() + " \\");
				}
			}
		}
//	}
	
	public void setParameter(String paramKey, Object paramValue)
	{
		parameter.put(paramKey, paramValue);
	}
	
	public void addField(HashMap map)
	{
		field.add(map);
	}
	
	/**
	 * fills the report with data from the given process
	 * @throws FileNotFoundException
	 * @throws JRException
	 */
	public void fillReport() throws FileNotFoundException, JRException
	{

		JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource((Collection)field);
		
		this.jasperPrint = JasperFillManager.fillReport(this.jasperReport, this.parameter, dataSource);
//		jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, new JREmptyDataSource());
	}
	
	/**
	 * fills the report with data from the given process and writes a jasper binary with filled conten
	 * @throws FileNotFoundException
	 * @throws JRException
	 */
	public void fillReportFileToFile(String jasperPath, String jasperFilledPath) throws FileNotFoundException, JRException
	{

//		System.out.println("jasper "+jasper);
//		System.out.println("jasperFilled "+jasperFilled);
//		System.out.println("parameter "+parameter);
//		System.out.println("jasper "+jasper);
		
//		System.err.println("Anzahl der zeilen ist: " + field.size());
		
		JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource((Collection)field);
		
		JasperFillManager.fillReportToFile(jasperPath, jasperFilledPath, parameter, dataSource);
//		JasperFillManager.fillReportToFile(jasper, jasperFilled, new HashMap(), dataSource);
//		JasperFillManager.fillReportToFile(jasper, jasperFilled, parameter, new JREmptyDataSource());
		
//		jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, new JREmptyDataSource());
	}
	
	/**
	 * converts a filled JasperReportFile to pdf
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public static void convertFileToPdf(String jasperFilledPath, String outPath) throws JRException, FileNotFoundException
	{
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilledPath));
		JRPdfExporter pdfExporter = new JRPdfExporter();
		pdfExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		pdfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		pdfExporter.exportReport();
	}

	/**
	 * exports JasperReportFile to pdf
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToPdf(String outPath) throws JRException, FileNotFoundException
	{
		JRPdfExporter pdfExporter = new JRPdfExporter();
		pdfExporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
		pdfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		pdfExporter.exportReport();
	}

	/**
	 * converts a filled JasperReportFile to html
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public static void convertFileToHtml(String jasperFilledPath, String outPath) throws JRException, FileNotFoundException
	{

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilledPath));
		JRHtmlExporter htmlExporter = new JRHtmlExporter();
		htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		htmlExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		htmlExporter.exportReport();
	}
	
	/**
	 * exports JasperReport to html
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToHtml(String outPath) throws JRException, FileNotFoundException
	{

		JRHtmlExporter htmlExporter = new JRHtmlExporter();
		htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
		htmlExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		htmlExporter.exportReport();
	}
	
	/**
	 * converts a filled JasperReportFile to odt
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public static void convertFileToOdt(String jasperFilledPath, String outPath) throws JRException, FileNotFoundException
	{

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilledPath));
		JROdtExporter odtExporter = new JROdtExporter();
		odtExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		odtExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		odtExporter.exportReport();
	}
	
	/**
	 * exports JasperReport to odt
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToOdt(String outPath) throws JRException, FileNotFoundException
	{

		JROdtExporter odtExporter = new JROdtExporter();
		odtExporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
		odtExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		odtExporter.exportReport();
	}
	
	/**
	 * converts a filled JasperReportFile to docx
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public static void convertFileToDocx(String jasperFilledPath, String outPath) throws JRException, FileNotFoundException
	{

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilledPath));
		JRDocxExporter docxExporter = new JRDocxExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		docxExporter.exportReport();
	}
	
	/**
	 * exports JasperReport to docx
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToDocx(String outPath) throws JRException, FileNotFoundException
	{

		JRDocxExporter docxExporter = new JRDocxExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		docxExporter.exportReport();
	}
	
	/**
	 * converts a filled JasperReportFile to pptx
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public static void convertFileToPptx(String jasperFilledPath, String outPath) throws JRException, FileNotFoundException
	{

		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilledPath));
		JRPptxExporter pptxExporter = new JRPptxExporter();
		pptxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		pptxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		pptxExporter.exportReport();
	}
	
	/**
	 * exports JasperReport to pptx
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToPptx(String outPath) throws JRException, FileNotFoundException
	{

		JRPptxExporter pptxExporter = new JRPptxExporter();
		pptxExporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
		pptxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		pptxExporter.exportReport();
	}
	
	/**
	 * converts a filled JasperReportFile to csv
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public static void convertFileToCsv(String jasperFilledPath, String outPath) throws JRException, FileNotFoundException
	{
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilledPath));
		JRCsvExporter csvExporter = new JRCsvExporter();
		csvExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		csvExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		csvExporter.exportReport();
	}

	/**
	 * exports JasperReport to csv
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToCsv(String outPath) throws JRException, FileNotFoundException
	{
		JRCsvExporter csvExporter = new JRCsvExporter();
		csvExporter.setParameter(JRExporterParameter.JASPER_PRINT, this.jasperPrint);
		csvExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outPath);

		csvExporter.exportReport();
	}

	/**
	 * exports the Parameter to a conf-file
	 */
	public void exportParametersToFile(String outpath)
	{
		try
		{
			// anlegen printwriter
			PrintWriter writer = new PrintWriter(outpath, "UTF-8");

			// ueber die properties iterieren und im conf-format rausschreiben
			for(JRParameter actParameter : this.jasperReport.getParameters())
			{
				writer.println(actParameter.getName());
				
				writer.println("Description");
				writer.println(actParameter.getDescription());

				if(actParameter.getDefaultValueExpression().getText() != null)
				{
					writer.println(".getDefaultValueExpression().getText()");
					writer.println(actParameter.getDefaultValueExpression().getText());
				}
				
				writer.println(".getDefaultValueExpression().getId()");
				writer.println(actParameter.getDefaultValueExpression().getId());
				
				writer.println(".getValueClassName()");
				writer.println(actParameter.getValueClassName());
				
				writer.println(".getClass().toString()");
				writer.println(actParameter.getClass().toString());
				
				writer.println(".toString()");
				writer.println(actParameter.toString());
				
				
				for(String actPropertyName : actParameter.getPropertiesMap().getPropertyNames())
				{
					writer.println(".getPropertiesMap().getPropertyNames");
					writer.println(actParameter.getName() + "=" + actPropertyName);
				}

			}

			// file schliessen
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	public void exportFieldsToFile(String path)
//	{
//		// wenn keine fields vorhanden => return
//		if(this.jasperReport.getFields() == null)
//		{
//			return;
//		}
//
//		// anlegen printwriter
//		try
//		{
//			PrintWriter writer = new PrintWriter(path, "UTF-8");
//
//			// ueber die properties iterieren und im conf-format rausschreiben
//			for(JRField actField : this.jasperReport.getFields())
//			{
//				writer.println(actField.getName() + "=" + actField.toString());
//			}
//			
//			// file schliessen
//			writer.close();
//		}
//		catch (FileNotFoundException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		catch (UnsupportedEncodingException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	public void setJrxml (String jrxml)
	{
		this.jrxml = jrxml;
	}

	public String getJrxml ()
	{
		return this.jrxml;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
