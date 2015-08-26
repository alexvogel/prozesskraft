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
	private String jrxml = null;
	private String jasper = null;
	private String jasperFilled = null;
	private String pdf = null;
	private String html = null;
	private String odt = null;
	private String docx = null;
	private String pptx = null;
	private String csv = null;
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

	public void printPlaceholder()
	{
		if (this.jrxml != null)
		{
			try {
				this.jasperReport = JasperCompileManager.compileReport(jrxml);
			} catch (JRException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println("to fully fill the template use this call:");
			System.out.println("reporter generate \\");
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
				System.err.println(zumSchlussAusgeben);
			}
			
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
		
//		System.err.println("Anzahl der zeilen ist: " + field.size());
		
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
	
	/**
	 * exports the Report to pptx
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToPptx() throws JRException, FileNotFoundException
	{
		if (pptx == null)
		{
			if (jasperFilled == null)
			{
				this.fillPReport();
			}
			pptx = this.jasperFilled + ".pptx";
		}
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilled));
		JRPptxExporter pptxExporter = new JRPptxExporter();
		pptxExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		pptxExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, pptx);

		pptxExporter.exportReport();
	}
	
	/**
	 * exports the Report to csv
	 * @throws JRException
	 * @throws FileNotFoundException 
	 */
	public void exportToCsv() throws JRException, FileNotFoundException
	{
		if (csv == null)
		{
			if (jasperFilled == null)
			{
				this.fillPReport();
			}
			csv = this.jasperFilled + ".csv";
		}
		JasperPrint jasperPrint = (JasperPrint) JRLoader.loadObject(new java.io.File(jasperFilled));
		JRCsvExporter csvExporter = new JRCsvExporter();
		csvExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		csvExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, csv);

		csvExporter.exportReport();
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
	
	public void setPptx (String pptx)
	{
		this.pptx = pptx;
	}

	public String getPptx ()
	{
		return this.pptx;
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
