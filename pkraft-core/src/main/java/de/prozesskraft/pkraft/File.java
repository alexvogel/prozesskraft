package de.prozesskraft.pkraft;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;

public class File
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String key = "default";
	private String subprocesskey = null;	// beim commit eines subprocess steps kann statt einem glob mit dieser angabe direkt das value aus dem subprocess geholt werden
	private String glob = "";
	private String globdir = null;
	private String filename = "";
	private String description = "";
	private String realposition = "";
	private ArrayList<Test> test = new ArrayList<Test>();
	private int minoccur = 0;
	private int maxoccur = 999999;

	private String category = null;
	private boolean preservePosition = false;
	private String status = "";	// waiting/finished/error

	ArrayList<Log> log = new ArrayList<Log>();

	boolean linkInsteadOfCopy = false;
	
	// don't clone parent when cloning this
	private Step parent = null;
	transient private Step parentDummy = null;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public File()
	{
		Step dummyStep = new Step();
		dummyStep.setName("dummy");
		this.parentDummy = dummyStep;
		log("info", "object created with an unknown parent");
	}

	public File(Step step)
	{
		this.setParent(step);
		log("info", "object created with parent step="+step.getName());
	}

	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * clone
	 * returns a clone of this
	 * @return File
	 */
	@Override
	public File clone()
	{
		File clone = new File();
		clone.setKey(this.getKey());
		clone.setSubprocesskey(this.getSubprocesskey());
		clone.setGlob(this.getGlob());
		clone.setGlobdir(this.getGlobdir());
		clone.setFilename(this.getFilename());
		clone.setDescription(this.getDescription());
		clone.setRealposition(this.getRealposition());
		clone.setMinoccur(this.getMinoccur());
		clone.setMaxoccur(this.getMaxoccur());
		clone.setCategory(this.getCategory());
		clone.setPreservePosition(this.isPreservePosition());
		clone.setStatus(this.getStatus());

		for(Test actTest : this.getTest())
		{
			clone.addTest(actTest.clone());
		}
		for(Log actLog : this.getLog())
		{
			clone.addLog(actLog.clone());
		}

		return clone;
	}
	
	/**
	 * clone
	 * returns a clone of this
	 * @return File
	 */
	public File oldClone()
	{
		return SerializationUtils.clone(this);
	}
	
	/**
	 * deserialize not in a standard way
	 * @param stream
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException
	{
		stream.defaultReadObject();

		// erstellen eines parentDummies, falls notwendig
		if(parent == null)
		{
			parentDummy = new Step();
		}
	}
	
	public boolean match(Match match)
	{
		String fieldname = match.getField();
		String pattern = match.getPattern();
		
		String string_to_test = new String();
		if      (fieldname.equals("key")) 	{string_to_test = this.getKey();}
		else if (fieldname.equals("filename")) 	{string_to_test = this.getFilename();}
		else if (fieldname.equals("absfilename")) 	{string_to_test = this.getAbsfilename();}

		if (string_to_test.matches("^"+pattern+"$")) { return true;	}
//		if (string_to_test.matches(pattern)) { return true;	}
		else { return false; }
	}
	
	/**
	 * reset this
	 */
	public void reset()
	{
		this.getLog().clear();
		this.setStatus("");
		for(Test actTest : this.getTest())
		{
			actTest.reset();
		}
	}
	
	private void addLog(Log log)
	{
		log.setLabel("file (key="+this.getKey() + ")");
		this.log.add(log);
	}
	
	/**
	 * es wird ueberprueft ob das file unter getAbsfilename() existiert und genau das gleiche ist wie unter getRealposition()
	 * bei bedarf wird es von getRealposition() nach getAbsfilename() kopiert.
	 * @return
	 */
	public boolean copyIfNeeded()
	{
		boolean success = false;

		this.log("info", "copy files if needed");

		if(this.getRealposition().equals(this.getAbsfilename()))
		{
			this.log("info", "source and destination are the same. no copy needed");
			return true;
		}
		
		// ueberpruefen ob an beiden positionen das gleiche file ist, wenn nicht, dann soll es dorthin kopiert werden
		java.io.File quellFile = new java.io.File(this.getRealposition());
		java.io.File zielFile = new java.io.File(this.getAbsfilename());

		// fuer BMW: pfade haben oft einen fuehrenden teil, der weggeschnitten gehoert
		
		
		this.log("debug", "source: " + quellFile.toPath().normalize());
		this.log("debug", "destination: " + zielFile.toPath().normalize());
		
		if(quellFile.exists())
		{
			this.log("info", "source file exists: " + quellFile.getAbsolutePath());
			if(zielFile.exists())
			{
				this.log("info", "destination file already exists: " + zielFile.getAbsolutePath());
				try
				{
					if(FileUtils.contentEquals(quellFile, zielFile))
					{
						this.log("info", "both files have equal content. no copy needed.");
						success = true;
					}
					else
					{

						// soll soft gelinkt werden?
						if(this.isLinkInsteadOfCopy())
						{
							zielFile.getParentFile().mkdirs();
							this.log("info", "files are not the same. will link="+zielFile.toPath().normalize()+", destination="+zielFile.getParentFile().toPath().normalize().relativize(quellFile.toPath().normalize()));
							Files.createSymbolicLink(zielFile.toPath().normalize(), zielFile.getParentFile().toPath().normalize().relativize(quellFile.toPath().normalize()));
						}
						// oder soll kopiert werden?
						else
						{
							this.log("info", "files are not the same. will copy source="+quellFile.getAbsolutePath()+", destination="+zielFile.getAbsolutePath());
							FileUtils.copyFile(quellFile, zielFile, true);
						}

						// 1b) alternativ einen hardlink (geringerer aufwand)
						// funktioniert nicht zwischen files auf verschiedenen filesystemen
						// z.B. beim committen der hinterlegten Files nach rootStep werden die files nicht ins rootdir kopiert, sondern an ihrem ort belassen
						// diese files befinden sich im installationsverzeichnis //share/ams/..... dies ist ein anderes filesystem!
						// Problem: beim clonen einer Instanz wird aus den hardlinks wieder normale files und der platzvorteil verpufft
//						zielFile.getParentFile().mkdirs();
//						Files.createLink(zielFile.toPath(), quellFile.toPath());
						
						// 1c) alternativ mit softlinks
//						try
//						{
//							Files.createSymbolicLink(quellFile.toPath(), zielFile.toPath());
//						}
//						catch (IOException x)
//						{
//							System.err.println(x);
//						}
//						catch (UnsupportedOperationException x)
//						{
//							// Some file systems do not support symbolic links.
//							System.err.println(x);
//						}
						
						// vermerken der neuen position als echte fileposition
						this.setRealposition(this.getAbsfilename());
						success = true;
					}
				}
				catch (IOException e)
				{
					log("error", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));
					this.setStatus("error");
					success = false;
				}
			}
			else
			{
				try
				{
					// soll soft gelinkt werden?
					if(this.isLinkInsteadOfCopy())
					{
						zielFile.getParentFile().mkdirs();
						this.log("info", "file does not exist yet. will link="+zielFile.toPath().normalize()+", destination="+zielFile.getParentFile().toPath().normalize().relativize(quellFile.toPath().normalize()));
						Files.createSymbolicLink(zielFile.toPath().normalize(), zielFile.getParentFile().toPath().normalize().relativize(quellFile.toPath().normalize()));
					}
					// oder soll kopiert werden?
					else
					{
						this.log("info", "file does not exist yet. will copy source="+quellFile.getAbsolutePath()+", destination="+zielFile.getAbsolutePath());
						FileUtils.copyFile(quellFile, zielFile, true);
					}

					// vermerken der neuen position als echte fileposition
					this.setRealposition(this.getAbsfilename());
					success = true;
				}
				catch (IOException e)
				{
					log("error", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));
					this.setStatus("error");
					success = false;
				}
			}
		}
		return success;
	}
	
//	public ArrayList<File> glob(String dir)
//	{
//		ArrayList<File> globbedFile = new ArrayList<File>();
//		String glob = this.getGlob();
//		this.log("debug","glob for "+glob);
//		if(!glob.equals(""))
//		{
//			this.log("debug","trying to glob for "+glob+" in directory "+dir);
//			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+dir+"/"+glob);
//			java.io.File dirFile = new java.io.File(dir);
//			if(dirFile.exists() && dirFile.isDirectory())
//			{
//				this.log("debug","directory exists "+dir);
//				for(String actFilename : new java.io.File(dir).list())
//				{
//					Path path = FileSystems.getDefault().getPath(dir, actFilename);
//					// wenn ein file dem glob-matcher entspricht, soll this geklont werden, glob resettet und realpath auf dieses gefundene file gesetzt werden 
//					if(matcher.matches(path))
//					{
//						this.log("debug","glob successfull for file "+dir+"/"+actFilename);
//						File newFile = this.clone();
//						newFile.setGlob("");
//						newFile.setRealposition(dir + "/" + actFilename);
//						globbedFile.add(newFile);
//					}
//					else
//					{
//						this.log("debug","glob NOT successfull for file "+dir+"/"+actFilename);
//					}
//				}
//			}
//			else
//			{
//				this.log("error","cannot glob for file because directory does not exist: "+dir);
//			}
//		}
//		else
//		{
//			if(new java.io.File(this.getRealposition()).exists())
//			{
//				globbedFile.add(this);
//			}
//			else
//			{
//				this.log("error","file does not exist in "+this.getRealposition());
//			}
//		}
//		
//		return globbedFile;
//	}

	public void performAllTests()
	{
//		System.out.println("performing "+this.test.size()+" file tests");
		for(Test t : this.test)
		{
			t.performTest(this);
//			System.out.println("performing test "+t.getName());
		}
	}
	
	public boolean doAllTestsPass()
	{
//		System.out.println("Variable "+this.getKey());
		for(Test t : this.test)
		{
//			System.out.println("testName "+t.getName()+" parameter: "+t.getParam().get(0).getContent());
			if (!(t.getTestResult()))
			{
//				System.out.println("test "+t.getName()+" does not pass with this call: value="+this.getValue()+" pattern="+t.getParam().get(0).getContent());
				return false;
			}
		}
//		System.out.println("all tests pass.");
		return true;
	}
	
	public String getAllTestsFeedback()
	{
		String testsFeedback = "";
		for(Test t : this.test)
		{
			if (!(testsFeedback.equals("")))
			{
				testsFeedback = testsFeedback + "\n\n";
			}
			testsFeedback = testsFeedback + "testName: " + t.getName() + "\n";
			testsFeedback = testsFeedback + "testDescription: " + t.getDescription() + "\n";
			testsFeedback = testsFeedback + "testResult: " + t.getTestResult() + "\n";
			testsFeedback = testsFeedback + "testFeedback: " + t.getTestFeedback();
		}
		return testsFeedback;
	}

	public String getFailedTestsFeedback()
	{
		String testsFeedback = "";
		for(Test t : this.test)
		{
			if (!(t.getTestResult()))
			{
				if (!(testsFeedback.equals("")))
				{
					testsFeedback = testsFeedback + "\n\n";
				}
				testsFeedback = testsFeedback + "testName: " + t.getName() + "\n";
				testsFeedback = testsFeedback + "testDescription: " + t.getDescription() + "\n";
				testsFeedback = testsFeedback + "testResult: " + t.getTestResult() + "\n";
				testsFeedback = testsFeedback + "testFeedback: " + t.getTestFeedback();
			}
		}
		return testsFeedback;
	}

	public String getFirstFailedTestsFeedback()
	{
		String testsFeedback = "";
		for(Test t : this.test)
		{
			if (!(t.getTestResult()))
			{
				if (!(testsFeedback.equals("")))
				{
					testsFeedback = testsFeedback + "\n\n";
				}
				testsFeedback = testsFeedback + "testName: " + t.getName() + "\n";
				testsFeedback = testsFeedback + "testDescription: " + t.getDescription() + "\n";
				testsFeedback = testsFeedback + "testResult: " + t.getTestResult() + "\n";
				testsFeedback = testsFeedback + "testFeedback: " + t.getTestFeedback();
				
				return testsFeedback;
			}
		}
		return testsFeedback;
	}

	public boolean doesExist()
	{
		java.io.File myFile = new java.io.File(this.getRealposition());
		if (myFile.exists() && !myFile.isDirectory())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.addLog(new Log(loglevel, logmessage));
	}

/*----------------------------
	  methods get
	----------------------------*/
	public java.io.File asFile()
	{
		return new java.io.File(this.getRealposition());
	}
	
	public String getKey()
	{
		return this.key;
	}

	public String getGlob()
	{
		return this.getParent().resolveString(this.glob);
	}

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getAbsfilename()
	{
		String absfilename = null;
		// wenn das file in den step integriert werden soll (defaultverhalten)
		if(!this.preservePosition)
		{
			if(category != null)
			{
				absfilename = this.getParent().getAbsdir() + "/" + this.category +"/"+ this.getFilename();
			}
			else
			{
				absfilename = this.getParent().getAbsdir() + "/" + this.getFilename();
			}
		}
		// wenn das file an seiner urspruenglichen position belassen werden soll (bei zentral in prozessdefinition hinterlegten daten der fall)
		else
		{
			if(this.getRealposition() != null)
			{
				absfilename = this.getRealposition();
			}
			else
			{
				absfilename = null;
			}
		}
		
		return this.getParent().getParent().modPathForBmw(absfilename);
	}

	public int getMinoccur()
	{
		return this.minoccur;
	}

	public int getMaxoccur()
	{
		return this.maxoccur;
	}

	public ArrayList<Test> getTest()
	{
		return this.test;
	}

	public String getField(String fieldname)
	{
		String returnvalue = new String();
		if      (fieldname.equals("filename")) 	{returnvalue = this.getFilename();}
		else if (fieldname.equals("absfilename")) 	{returnvalue = this.getAbsfilename();}
		else 	{returnvalue = "no field '"+fieldname+"' in Object 'File'";}
		return returnvalue;
	}
	
	public long getSizeInBytes()
	{
		java.io.File file = new java.io.File(this.getRealposition());

		if (file.exists())
		{
			return file.length();
		}
		return 0;
	}
	
	public long getSizeInKilobytes()
	{
		return (this.getSizeInBytes() / 1024);
	}
	
	public long getSizeInMegabytes()
	{
		return (this.getSizeInBytes() / 1048576);
	}
	
	public long getSizeInGigabytes()
	{
		return (this.getSizeInBytes() / 1073741824);
	}
	
	/**
	 * returns the size of the file in desired scale
	 * @param String scale (byte|kilobyte|megabyte|gigabyte)
	 * @return Long size
	 */
	public Long getSize(String scale)
	{
		Long size = null;
		if (scale.equals("byte"))
		{
			size = getSizeInBytes();
		}
		else if (scale.equals("kilobyte"))
		{
			size = getSizeInKilobytes();
		}
		else if (scale.equals("megabyte"))
		{
			size = getSizeInMegabytes();
		}
		else if (scale.equals("gigabyte"))
		{
			size = getSizeInGigabytes();
		}
		return size;
	}
	
	public String getSizeAsString()
	{
		long size = getSizeInBytes();
		String sizeString = size + " B";
		
		if(size > 1024)
		{
			size = getSizeInKilobytes();
			sizeString = size + " KB";
		}
		if(size > 1024)
		{
			size = getSizeInMegabytes();
			sizeString = size + " MB";
		}
		if(size > 1024)
		{
			size = getSizeInGigabytes();
			sizeString = size + " GB";
		}
		return sizeString;
	}
	
	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setKey(String key)
	{
		this.key = key;
	}

	public void setGlob(String glob)
	{
		this.glob = glob;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setMinoccur(int minoccur)
	{
		this.minoccur = minoccur;
	}

	public void setMaxoccur(int maxoccur)
	{
		this.maxoccur = maxoccur;
	}

	public void setTest(ArrayList<Test> test)
	{
		this.test = test;
	}

	public void addTest(Test test)
	{
		this.test.add(test);
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		log("info", "setting status to '"+status+"'");
		this.status = status;
	}

	/**
	 * @return the absposition
	 */
	public String getRealposition() {
		return realposition;
	}

	/**
	 * @param absposition the absposition to set
	 */
	public void setRealposition(String realposition)
	{
		this.realposition = this.getParent().getParent().modPathForBmw(realposition);
		this.setFilename(new java.io.File(realposition).getName());
	}

	/**
	 * @return the parent
	 */
	public Step getParent()
	{
		if(this.parent != null)
		{
			return this.parent;
		}
		else
		{
			return parentDummy;
		}
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Step parent) {
		this.parent = parent;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(ArrayList<Log> log) {
		this.log = log;
	}

	/**
	 * @return the globdir
	 */
	public String getGlobdir() {
		return globdir;
	}

	/**
	 * @param globdir the globdir to set
	 */
	public void setGlobdir(String globdir) {
		this.globdir = globdir;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the preservePosition
	 */
	public boolean isPreservePosition() {
		return preservePosition;
	}

	/**
	 * @param preservePosition the preservePosition to set
	 */
	public void setPreservePosition(boolean preservePosition) {
		this.preservePosition = preservePosition;
	}

	/**
	 * @return the subprocesskey
	 */
	public String getSubprocesskey() {
		return subprocesskey;
	}

	/**
	 * @param subprocesskey the subprocesskey to set
	 */
	public void setSubprocesskey(String subprocesskey) {
		this.subprocesskey = subprocesskey;
	}

	/**
	 * @return the linkInsteadOfCopy
	 */
	public boolean isLinkInsteadOfCopy() {
		return linkInsteadOfCopy;
	}

	/**
	 * @param linkInsteadOfCopy the linkInsteadOfCopy to set
	 */
	public void setLinkInsteadOfCopy(boolean linkInsteadOfCopy) {
		this.linkInsteadOfCopy = linkInsteadOfCopy;
	}

}
