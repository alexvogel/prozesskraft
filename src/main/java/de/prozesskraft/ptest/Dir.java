package de.prozesskraft.ptest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.dozer.DozerBeanMapper;
import org.xml.sax.SAXException;

public class Dir {

	private Integer id = null;
	private int minoccur = 0;
	private int maxoccur = 99;
	private String path = null;

	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Dir> dir = new ArrayList<Dir>();

	private String infilexml = null;
	private String outfilexml = null;
	private String basedir = null;
	
	private int runningId = 0;

	public Dir()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);
		
		minoccur = 0;
		maxoccur = 99;
	}

	public String toString()
	{
		String entityString = "";
		
		entityString += "id="+this.getId()+", minoccur: "+this.getMinoccur()+", maxoccur: "+this.getMaxoccur()+", path: "+this.getPath();
		
		return entityString;
	}

	public void genFingerprint() throws NullPointerException, IOException
	{
		if(basedir == null)
		{
			throw new NullPointerException();
		}
		
		// den directory-baum durchgehen und fuer jeden eintrag ein entity erstellen
		Files.walkFileTree(FileSystems.getDefault().getPath(this.getBasedir()), new FileVisitor<Path>()
		{
			// called after a directory visit is complete
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				String pathString = dir.getParent() + "/"+dir.getFileName();
				System.out.println("after visit directory: "+pathString);
				return FileVisitResult.CONTINUE;
			}

			// called before a directory visit
			public FileVisitResult preVisitDirectory(Path walkingDir, BasicFileAttributes attrs) throws IOException
			{
//				// wenn der parent == null, handelt es sich um "." Das soll ignoriert werden
//				if(dir.getParent() == null)
//				{
//					return FileVisitResult.CONTINUE;
//				}
				
				String pathString = walkingDir.getParent() + "/"+walkingDir.getFileName();
				System.out.println("before visit directory: "+pathString);

				// Dir erstellen
				Dir dir = new Dir();
				dir.setId(runningId++);

				dir.setPath(pathString);
				dir.setMinoccur(1);
				dir.setMaxoccur(1);

				addDir(dir);

				System.out.println(dir.toString());
				
				return FileVisitResult.CONTINUE;
			}

			// called for each file visited. the basic file attributes of the file are also available
			public FileVisitResult visitFile(Path walkingFile, BasicFileAttributes attrs) throws IOException
			{
				String pathString = walkingFile.getParent() + "/"+walkingFile.getFileName();
				System.out.println("visit file: "+pathString);
				
				File file = new File();
				file.setId(runningId++);

				file.setPath(pathString);
				file.setMinoccur(1);
				file.setMaxoccur(1);

				file.setSize(attrs.size()+"B");
				addFile(file);

				System.out.println(file.toString());

				return FileVisitResult.CONTINUE;
			}

			// called for each file if the visit failed
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				System.out.println("visit file FAILED: "+file.getFileName());
				return FileVisitResult.CONTINUE;
			}

		});
		
	}

	public Dir readXml() throws JAXBException
	{
		if (this.getInfilexml() == null)
		{
			throw new NullPointerException();
		}

		JAXBContext context = JAXBContext.newInstance(de.prozesskraft.jaxb.ptest.Dir.class);
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
			de.prozesskraft.jaxb.ptest.Dir xptest = (de.prozesskraft.jaxb.ptest.Dir) um.unmarshal(new java.io.File(this.getInfilexml()));
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
			jaxbContext = JAXBContext.newInstance(de.prozesskraft.jaxb.ptest.Dir.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
			// die daten aus this in das jaxb objekt mappen
			de.prozesskraft.jaxb.ptest.Dir xptest = new de.prozesskraft.jaxb.ptest.Dir();
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

	public void addDir(Dir dir)
	{
		this.dir.add(dir);
	}
	
	public void addFile(File file)
	{
		this.file.add(file);
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the minoccur
	 */
	public int getMinoccur() {
		return minoccur;
	}

	/**
	 * @param minoccur the minoccur to set
	 */
	public void setMinoccur(int minoccur) {
		this.minoccur = minoccur;
	}

	/**
	 * @return the maxoccur
	 */
	public int getMaxoccur() {
		return maxoccur;
	}

	/**
	 * @param maxoccur the maxoccur to set
	 */
	public void setMaxoccur(int maxoccur) {
		this.maxoccur = maxoccur;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the file
	 */
	public ArrayList<File> getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(ArrayList<File> file) {
		this.file = file;
	}

	/**
	 * @return the dir
	 */
	public ArrayList<Dir> getDir() {
		return dir;
	}

	/**
	 * @param dir the dir to set
	 */
	public void setDir(ArrayList<Dir> dir) {
		this.dir = dir;
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

	/**
	 * @return the basedir
	 */
	public String getBasedir() {
		return basedir;
	}

	/**
	 * @param basedir the basedir to set
	 */
	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	/**
	 * @return the runningId
	 */
	public int getRunningId() {
		return runningId;
	}

	/**
	 * @param runningId the runningId to set
	 */
	public void setRunningId(int runningId) {
		this.runningId = runningId;
	}

}
