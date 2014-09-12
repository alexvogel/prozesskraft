package de.caegroup.process;

import java.io.*;
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
	private String glob = "";
	private String filename = "";
	private String description = "";
	private String realposition = "";
	private ArrayList<Test> test = new ArrayList<Test>();
	private int minoccur = 0;
	private int maxoccur = 999999;

	private String status = "";	// waiting/finished/error

	private Step parent = null;
	
	private ArrayList<Log> log = new ArrayList<Log>();


	/*----------------------------
	  constructors
	----------------------------*/
	public File()
	{
		this.parent = new Step();
		log("info", "object created with an unknown parent");
	}

	public File( Step step)
	{
		this.parent = step;
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
		return SerializationUtils.clone(this);
	}
	
	public boolean match(Match match)
	{
		String fieldname = match.getField();
		String pattern = match.getPattern();
		
		String string_to_test = new String();
		if      (fieldname.equals("key")) 	{string_to_test = this.getKey();}
		else if (fieldname.equals("filename")) 	{string_to_test = this.getFilename();}
		else if (fieldname.equals("absfilename")) 	{string_to_test = this.getAbsfilename();}

		if (string_to_test.matches(".*"+pattern+".*")) { return true;	}
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
		}
		
		// ueberpruefen ob an beiden positionen das gleiche file ist, wenn nicht, dann soll es dorthin kopiert werden
		java.io.File quellFile = new java.io.File(this.getRealposition());
		java.io.File zielFile = new java.io.File(this.getAbsfilename());
		
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
						this.log("info", "files are not the same. will copy source="+quellFile.getAbsolutePath()+", destination="+zielFile.getAbsolutePath());
						FileUtils.copyFile(quellFile, zielFile, true);

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
					this.log("info", "destination file does not exists yet. will copy source="+quellFile.getAbsolutePath()+", destination="+zielFile.getAbsolutePath());
					FileUtils.copyFile(quellFile, zielFile, true);

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
		java.io.File myFile = new java.io.File(this.getAbsfilename());
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
		this.log.add(new Log("file-"+this.getKey(), loglevel, logmessage));
	}

/*----------------------------
	  methods get
	----------------------------*/
	public java.io.File asFile()
	{
		return new java.io.File(this.getAbsfilename());
	}
	
	public String getKey()
	{
		return this.key;
	}

	public String getGlob()
	{
		return this.glob;
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
		return this.getParent().getAbsdir() + "/" + this.getFilename();
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
		java.io.File file = new java.io.File(this.getAbsfilename());
		
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
	public void setRealposition(String realposition) {
		this.realposition = realposition;
	}

	/**
	 * @return the parent
	 */
	public Step getParent() {
		return parent;
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

}
