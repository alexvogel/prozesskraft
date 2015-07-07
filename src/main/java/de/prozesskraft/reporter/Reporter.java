package de.prozesskraft.reporter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
//import de.caegroup.process.Process;
import net.sf.jasperreports.engine.util.JRLoader;

public class Reporter
{
	/*----------------------------
	  structure
	----------------------------*/
//	private Process process = null;
	private String jrxml = null;
	private String jasper = null;
	private String jasperFilled = null;
	private String pdf = null;
	private String html = null;
	private String odt = null;
	private String docx = null;
	private JasperReport jasperReport = null;
	private OutputStream jasperReportOutputStream = null;
	private JasperPrint jasperPrint = null;
	private Map<String,Object> parameter = new HashMap<String,Object>();
	private List<Map<String,?>> field = new ArrayList<Map<String, ?>> ();
//	private ArrayList<HashMap> field = new ArrayList<HashMap>();
	private JRMapCollectionDataSource dataSource;

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
	public void compile() throws JRException
	{
		if (this.jrxml != null)
		{
			if (this.jasper == null)
			{
				this.jasper = this.jrxml + ".jasper";
			}
			JasperCompileManager.compileReportToFile(jrxml, jasper);
		}
		else
		{
//			throw new NullPointerException();
		}
		
	}
	
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
	public void fillPReport() throws FileNotFoundException, JRException
	{

		if (jasperFilled == null)
		{
			if (jasper == null)
			{
				this.compile();
			}
			jasperFilled = jasper + ".filled";
		}
		
//		System.out.println("jasper "+jasper);
//		System.out.println("jasperFilled "+jasperFilled);
//		System.out.println("parameter "+parameter);
//		System.out.println("jasper "+jasper);
		
		dataSource = new JRMapCollectionDataSource((Collection)field);
		
		JasperFillManager.fillReportToFile(jasper, jasperFilled, parameter, dataSource);
//		JasperFillManager.fillReportToFile(jasper, jasperFilled, new HashMap(), dataSource);
//		JasperFillManager.fillReportToFile(jasper, jasperFilled, parameter, new JREmptyDataSource());
		
//		jasperPrint = JasperFillManager.fillReport(jasperReport, parameter, new JREmptyDataSource());
	}
	
	/**
	 * exports the Report to pdf
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToPdf() throws JRException, FileNotFoundException
	{
		if (pdf == null)
		{
			if (jasperFilled == null)
			{
				this.fillPReport();
			}
			pdf = this.jasperFilled + ".pdf";
		}
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilled));
		JRPdfExporter pdfExporter = new JRPdfExporter();
		pdfExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		pdfExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, pdf);

		pdfExporter.exportReport();
	}
	
	/**
	 * exports the Report to html
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToHtml() throws JRException, FileNotFoundException
	{
		if (html == null)
		{
			if (jasperFilled == null)
			{
				this.fillPReport();
			}
			html = this.jasperFilled + ".html";
		}
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilled));
		JRHtmlExporter htmlExporter = new JRHtmlExporter();
		htmlExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		htmlExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, html);

		htmlExporter.exportReport();
	}
	
	/**
	 * exports the Report to odt
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToOdt() throws JRException, FileNotFoundException
	{
		if (odt == null)
		{
			if (jasperFilled == null)
			{
				this.fillPReport();
			}
			odt = this.jasperFilled + ".odt";
		}
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilled));
		JROdtExporter odtExporter = new JROdtExporter();
		odtExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		odtExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, odt);

		odtExporter.exportReport();
	}
	
	/**
	 * exports the Report to docx
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToDocx() throws JRException, FileNotFoundException
	{
		if (docx == null)
		{
			if (jasperFilled == null)
			{
				this.fillPReport();
			}
			docx = this.jasperFilled + ".docx";
		}
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilled));
		JRDocxExporter docxExporter = new JRDocxExporter();
		docxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		docxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, docx);

		docxExporter.exportReport();
	}
	
	public void setJrxml (String jrxml)
	{
		this.jrxml = jrxml;
	}

	public String getJrxml ()
	{
		return this.jrxml;
	}
	
	public void setJasper (String jasper)
	{
		this.jasper = jasper;
	}

	public String getJasper ()
	{
		return this.jasper;
	}

	public void setJasperFilled (String jasperFilled)
	{
		this.jasperFilled = jasperFilled;
	}

	public String getJasperFilled ()
	{
		return this.jasperFilled;
	}

	public void setPdf (String pdf)
	{
		this.pdf = pdf;
	}

	public String getPdf ()
	{
		return this.pdf;
	}
	
	public void setHtml (String html)
	{
		this.html = html;
	}

	public String getHtml ()
	{
		return this.html;
	}
	
	public void setOdt (String odt)
	{
		this.odt = odt;
	}

	public String getOdt ()
	{
		return this.odt;
	}
	
	public void setDocx (String docx)
	{
		this.docx = docx;
	}

	public String getDocx ()
	{
		return this.docx;
	}
	
}
