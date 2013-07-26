package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;

public class File
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String key = "";
//	private String filename = "";
	private String description = "";
	private String absfilename = "";
	private ArrayList<Test> test = new ArrayList<Test>();
	private int minoccur = 0;
	private int maxoccur = 999999;
	

	/*----------------------------
	  constructors
	----------------------------*/
	public File()
	{

	}


	/*----------------------------
	  methods
	----------------------------*/
	public boolean match(Match match)
	{
		String fieldname = match.getField();
		String pattern = match.getPattern();
		
		String string_to_test = new String();
		if      (fieldname.equals("filename")) 	{string_to_test = this.getFilename();}
		else if (fieldname.equals("absfilename")) 	{string_to_test = this.getAbsfilename();}

		if (string_to_test.matches(".*"+pattern+".*")) { return true;	}
//		if (string_to_test.matches(pattern)) { return true;	}
		else { return false; }
	}
	
	public void performAllTests()
	{
		System.out.println("performing "+this.test.size()+" file tests");
		for(Test t : this.test)
		{
			t.performTest(this);
			System.out.println("performing test "+t.getName());
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
		return new java.io.File(this.getAbsfilename()).exists();
	}
	/*----------------------------
	  methods get
	----------------------------*/
	public String getKey()
	{
		return this.key;
	}

	public String getFilename()
	{
		java.io.File f = new java.io.File(this.absfilename);
		return f.getName();
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getAbsfilename()
	{
		return this.absfilename;
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
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setKey(String key)
	{
		this.key = key;
	}

//	public void setFilename(String filename)
//	{
//		this.filename = filename;
//	}
//
	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setAbsfilename(String absfilename)
	{
		this.absfilename = absfilename;
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


}
