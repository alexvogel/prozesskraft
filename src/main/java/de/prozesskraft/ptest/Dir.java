package de.prozesskraft.ptest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FilenameUtils;
import org.dozer.DozerBeanMapper;
import org.xml.sax.SAXException;

import de.caegroup.commons.Log;

public class Dir {

	private Integer id = null;
	private int minOccur = 0;
	private int maxOccur = 99;
	private String path = null;

	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Dir> dir = new ArrayList<Dir>();

	private String infilexml = null;
	private String outfilexml = null;
	private Path basepath = null;

	private int runningId = 0;

	public Dir thisObj = null;

	// ist es ein template? , dann wird in match alle passenden ids aus dem pruefling gesammelt.
	// ist es ein pruefling?, dann wird in match die id des passenden eintrages aus dem template eingetragen (in diesem fall kann es nur 1 stk sein)
	private ArrayList<Dir> matchedDir = new ArrayList<Dir>();

	private ArrayList<Log> log = new ArrayList<Log>();

	public Dir()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);

		minOccur = 0;
		maxOccur = 99;

		this.thisObj = this;
	}

	public String toString()
	{
		String entityString = "";

		entityString += "id="+this.getId()+", minoccur: "+this.getMinOccur()+", maxoccur: "+this.getMaxOccur()+", path: "+this.getPath();

		return entityString;
	}

	/**
	 * every dir and file of the template has to find the given occurance from the examinee
	 * @return
	 */
	public boolean checkDir(Dir examineeDir)
	{
		// loeschen bestehender match-eintraege in this
		this.clearMatchRecursive();
		examineeDir.clearMatchRecursive();

		// alle im verzeichnis befindlichen verzeichnis mit vollstaendigen examineeDir abgleichen
		for(Dir actDir : this.getDir())
		{
			actDir.match(examineeDir);
		}

		// alle im verzeichnis befindlichen files mit vollstaendigen examineeDir abgleichen
		for(File actFile : this.getFile())
		{
			actFile.match(examineeDir);
		}

		// den abgleich auswerten
		// 1) gibt es ein Dir | File im template, dessen match den minoccur unterschreitet?, dann false
		return this.summary();
	}

	/**
	 * alle Dirs und Files durchgehen
	 * 1) gibt es ein Dir | File im template, dessen match den minoccur unterschreitet?
	 * ...
	 */
	private boolean summary()
	{
		boolean result = true;
		
		for(Dir actDir : this.getDir())
		{
			if(actDir.getMatchedDir().size() < actDir.getMinOccur())
			{
				actDir.log.add(new Log("error", "directory (id="+actDir.getId()+", path="+actDir.getPath()+") needed to be there at least "+actDir.getMinOccur()+" time(s), but was only found "+actDir.getMatchedDir().size()+" time(s)"));
				result = false;
			}
			if(actDir.getMatchedDir().size() > actDir.getMaxOccur())
			{
				actDir.log.add(new Log("error", "directory (id="+actDir.getId()+", path="+actDir.getPath()+") needed to be there at maximum "+actDir.getMinOccur()+" time(s), but was found "+actDir.getMatchedDir().size()+" time(s)"));
				result = false;
			}
			// ausgeben des gesamten loggings des aktuellen entity auf STDOUT
			System.out.println("id="+actDir.getId()+", path="+actDir.getPath());
			System.out.println(Log.sprintWholeLog(actDir.log));
		}
		for(File actFile : this.getFile())
		{
			if(actFile.getMatchedFile().size() < actFile.getMinOccur())
			{
				actFile.log.add(new Log("error", "file (id="+actFile.getId()+", path="+actFile.getPath()+") needed to be there at least "+actFile.getMinOccur()+" time(s), but was only found "+actFile.getMatchedFile().size()+" time(s)"));
				result = false;
			}
			if(actFile.getMatchedFile().size() > actFile.getMaxOccur())
			{
				actFile.log.add(new Log("error", "file (id="+actFile.getId()+", path="+actFile.getPath()+") needed to be there at maximum "+actFile.getMinOccur()+" time(s), but was found "+actFile.getMatchedFile().size()+" time(s)"));
				result = false;
			}
			// ausgeben des gesamten loggings des aktuellen entity auf STDOUT
			System.out.println("id="+actFile.getId()+", path="+actFile.getPath());
			System.out.println(Log.sprintWholeLog(actFile.log));
		}
		return result;
	}
	
	/**
	 * geht den vollstaendigen baum durch und prueft ob es zu this passende eintraege gibt
	 * passende eintraege werden auf beiden seiten (template-baum <-> examinee-baum) vermerkt
	 * es passt, wenn
	 * 1) die pfad-angabe im examinee auf das pfad-pattern im template (this) matcht
	 * @param examineeDir
	 * @return allMatchingDirByPath
	 */
	public void match(Dir examineeDir)
	{
		// den pfad vom examinee gegen den template-pfad matchen
		if(examineeDir.getPath().matches("^"+this.getPath()+"$"))
		{
			// passen beide vergleichspartner? Dann soll dies in beiden vermerkt werden
			examineeDir.getMatchedDir().add(this);
			examineeDir.log.add(new Log("debug", "path matched with (id="+this.getId()+", path="+this.getPath()+")"));

			this.addDir(examineeDir);
			this.log.add(new Log("debug", "path matched with (id="+examineeDir.getId()+", path="+examineeDir.getPath()+")"));
		}
		else
		{
			examineeDir.log.add(new Log("debug", "path did NOT match with (id="+this.getId()+", path="+this.getPath()+")"));
			this.log.add(new Log("debug", "path did NOT match with (id="+examineeDir.getId()+", path="+examineeDir.getPath()+")"));
		}

		// auch fuer alle enthaltenen Dirs ausfuehren
		for(Dir actDir : examineeDir.getDir())
		{
			this.match(actDir);
		}
	}
	
	/**
	 * loescht die match-Eintraege auch aller enthaltener Dirs und Files
	 */
	private void clearMatchRecursive()
	{
		this.getMatchedDir().clear();
		for(Dir actDir : this.getDir())
		{
			actDir.clearMatchRecursive();
		}
		for(File actFile : this.getFile())
		{
			actFile.getMatchedFile().clear();
		}
	}
	
	/**
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public void genFingerprint() throws NullPointerException, IOException
	{
		if(basepath == null)
		{
			throw new NullPointerException();
		}

		// den directory-baum durchgehen und fuer jeden eintrag ein entity erstellen
		Files.walkFileTree(this.getBasepath(), new FileVisitor<Path>()
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
				// relativen Pfad (zur Basis basepath) feststellen
				String pathString = walkingDir.getParent() + "/"+walkingDir.getFileName();
				String relPathString = getBasepath().relativize(walkingDir).toString();
				
				// Dir erstellen
				Dir dir = new Dir();

				// wenn der relative path ein leerer string ist, ist this das directory
				if(relPathString.equals(""))
				{
					System.out.println("THIS DIR IS ROOT: "+walkingDir.getFileName());
					dir = thisObj;
				}
				else
				{
					addDir(dir);
				}

				System.err.println("before visit directory (abs): "+pathString);
				System.err.println("before visit directory (rel): "+relPathString);

				// die Daten setzen
				dir.setId(runningId++);

				dir.setPath(relPathString);
				dir.setMinOccur(1);
				dir.setMaxOccur(1);

				System.out.println(dir.toString());
				
				return FileVisitResult.CONTINUE;
			}

			// called for each file visited. the basic file attributes of the file are also available
			public FileVisitResult visitFile(Path walkingFile, BasicFileAttributes attrs) throws IOException
			{
				// relativen Pfad (zur Basis basepath) feststellen
				String pathString = walkingFile.getParent() + "/"+walkingFile.getFileName();
				String relPathString = getBasepath().relativize(walkingFile).toString();

				System.err.println("visit file (abs): "+pathString);
				System.err.println("visit file (rel): "+relPathString);

				// new File erstellen
				File file = new File();
				file.setId(runningId++);

				file.setPath(relPathString);
				file.setMinOccur(1);
				file.setMaxOccur(1);

				file.setSize((float)attrs.size());
				try {
					file.setSizeUnit("B");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				file.setSizeTolerance(0F);

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

	public void addMatchedDir(Dir dir)
	{
		this.getMatchedDir().add(dir);
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
	 * @return the minOccur
	 */
	public int getMinOccur() {
		return minOccur;
	}

	/**
	 * @param minOccur the minOccur to set
	 */
	public void setMinOccur(int minOccur) {
		this.minOccur = minOccur;
	}

	/**
	 * @return the maxOccur
	 */
	public int getMaxOccur() {
		return maxOccur;
	}

	/**
	 * @param maxOccur the maxOccur to set
	 */
	public void setMaxOccur(int maxOccur) {
		this.maxOccur = maxOccur;
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
	public Path getBasepath() {
		return basepath;
	}

	/**
	 * @return the basedir
	 */
	public String getBasepathAsString() {
		return basepath.toString();
	}

	/**
	 * @param basedir the basedir to set
	 */
	public void setBasepath(String basedir) {
		this.basepath = Paths.get(basedir);
	}

	/**
	 * @param basedir the basedir to set
	 */
	public void setBasepath(Path basepath) {
		this.basepath = basepath;
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

	/**
	 * @return the matchedDir
	 */
	public ArrayList<Dir> getMatchedDir() {
		return matchedDir;
	}

	/**
	 * @param matchedDir the matchedDir to set
	 */
	public void setMatchedDir(ArrayList<Dir> matchedDir) {
		this.matchedDir = matchedDir;
	}

}
