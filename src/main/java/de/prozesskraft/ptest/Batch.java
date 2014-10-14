package de.prozesskraft.ptest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.dozer.DozerBeanMapper;
import org.xml.sax.SAXException;

public class Batch {

	private ArrayList<Entity> batch = new ArrayList<Entity>();
	private String infilexml = null;
	private String outfilexml = null;

	public Batch()
	{
		
	}

	public Batch readXml() throws JAXBException
	{
		if (this.getInfilexml() == null)
		{
			throw new NullPointerException();
		}

		JAXBContext context = JAXBContext.newInstance(de.prozesskraft.jaxb.ptest.Batch.class);
		Unmarshaller um = context.createUnmarshaller();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try
		{
			// stream auf das schema oeffnen (liegt im root verezichnis des jars in das es gepack wird)
			InputStream schemaStream = this.getClass().getResourceAsStream("/ptest.xsd");
			BufferedReader schemaReader = new BufferedReader(new InputStreamReader(schemaStream));

			// und in dieses temp-verzeichnis schreiben
			java.io.File tmpFile = java.io.File.createTempFile("avoge2013", "ptest.xsd");
			FileWriter fstream = new FileWriter(tmpFile);
			BufferedWriter schemaWriter = new BufferedWriter(fstream);

			String thisLine;
			while((thisLine = schemaReader.readLine()) != null)
			{
//				System.out.println(thisLine);
				schemaWriter.append(thisLine);
			}
			schemaWriter.close();

			// das temporaere schemafile beim unmarshaller angeben, damit es zur validierung verwendet wird
			schema = sf.newSchema(tmpFile);
			um.setSchema(schema);
		}
		catch (SAXException e)
		{
			System.err.println("error: reading schema.");
			e.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NullPointerException e2)
		{
			System.err.println("error: xml schema file not found");
			e2.printStackTrace();
		}

//		Process destObject = null;
		// das aktuelle xml-file in die jaxb-klassen einlesen
		try
		{
			de.prozesskraft.jaxb.ptest.Batch xptest = (de.prozesskraft.jaxb.ptest.Batch) um.unmarshal(new java.io.File(this.getInfilexml()));
//			System.out.println("xprocess variable1 free = "+xprocess.getStep().get(0).getCommit().get(0).getVariable().get(0).isFree());
//			BeanMappingBuilder builder = new BeanMappingBuilder()
//			{
//				protected void configure()
//				{
//					mapping(de.caegroup.jaxb.process.Process.class, de.caegroup.process.Process.class, oneWay(), mapId("A"), mapNull(true));
//				}
//			};
			
//			System.out.println("processName1: "+this.getName());
			DozerBeanMapper mapper = new DozerBeanMapper();
//			destObject = mapper.map(xprocess, de.caegroup.process.Process.class);
			mapper.map(xptest, this);
//			System.out.println("processName2: "+this.getName());

			// setzen der parenteintraege aller steps
//			this.affiliate();
			// die jaxb-klassen mit den domain-klassen mappen
//			System.out.println("processName3: "+this.getName());
			
			// ueberpruefen ob der process consistent ist
//			if(this.isProcessConsistent())
//			{
//				this.log("info", "check process consistency successfull.");
//			}
//			else
//			{
//				this.log("info", "check process consistency NOT successfull.");
//			}
		}
		catch (javax.xml.bind.UnmarshalException e)
		{
			System.err.println("error: cannot unmarshall xml-file: "+this.getInfilexml());
			e.printStackTrace();
		}

		return this;
	}

	/**
	 * schreiben des aktuellen prozesses in ein xml file
	 * @throws JAXBException 
	 * 
	 **/

	public void writeXml()
	{
		java.io.File file = new java.io.File(this.outfilexml);
		JAXBContext jaxbContext;
		try
		{
			jaxbContext = JAXBContext.newInstance(de.prozesskraft.jaxb.ptest.Batch.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
			// die daten aus this in das jaxb objekt mappen
			de.prozesskraft.jaxb.ptest.Batch xptest = new de.prozesskraft.jaxb.ptest.Batch();
			DozerBeanMapper mapper = new DozerBeanMapper();
//			mapper.map(xprocess, this);
			mapper.map(this, xptest);
			
			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			jaxbMarshaller.marshal(xptest, file);
		}
		catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void addEntity(Entity entity)
	{
		this.batch.add(entity);
	}
	
	/**
	 * @return the batch
	 */
	public ArrayList<Entity> getBatch() {
		return batch;
	}

	/**
	 * @param batch the batch to set
	 */
	public void setBatch(ArrayList<Entity> batch) {
		this.batch = batch;
	}

	/**
	 * @return the infilexml
	 */
	public String getInfilexml() {
		return infilexml;
	}

	/**
	 * @param infilexml the infilexml to set
	 */
	public void setInfilexml(String infilexml) {
		this.infilexml = infilexml;
	}

	/**
	 * @return the outfilexml
	 */
	public String getOutfilexml() {
		return outfilexml;
	}

	/**
	 * @param outfilexml the outfilexml to set
	 */
	public void setOutfilexml(String outfilexml) {
		this.outfilexml = outfilexml;
	}

	
}
