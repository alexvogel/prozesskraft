package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;

public class Variable
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String key = "";
	private String value = "";
	private String description = "";
	private String glob = "";
	private ArrayList<String> choice = new ArrayList<String>();
	private ArrayList<Test> test = new ArrayList<Test>();
	private int minoccur = 0;
	private int maxoccur = 999999;
	private Boolean free = null;
	

	/*----------------------------
	  constructors
	----------------------------*/
	public Variable()
	{

	}

	public Variable(String key)
	{
		this.key = key;
	}

	public Variable(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	/*----------------------------
	  methods
	----------------------------*/
	public boolean match(Match match)
	{
		String fieldname = match.getField();
		String pattern = match.getPattern();
		
		String string_to_test = new String();
		if 		(fieldname.equals("key")) 		{string_to_test = this.getKey();}
		else if (fieldname.equals("value")) 	{string_to_test = this.getValue();}

		if (string_to_test.matches(pattern)) { return true;	}
		else { return false; }
	}
	
	public void performAllTests()
	{
		for(Test t : this.test)
		{
			t.performTest(this);
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

	/*----------------------------
	  methods get
	----------------------------*/
	public String getKey()
	{
		return this.key;
	}

	public String getGlob()
	{
		return this.glob;
	}

	public String getValue()
	{
		return this.value;
	}

	public String getDescription()
	{
		return this.description;
	}

	public int getMinoccur()
	{
		return this.minoccur;
	}

	public int getMaxoccur()
	{
		return this.maxoccur;
	}

	public boolean getFree()
	{
		return this.free;
	}

	public boolean isFree()
	{
		return this.free;
	}

	public ArrayList<Test> getTest()
	{
		return this.test;
	}

	public ArrayList<String> getChoice()
	{
		return this.choice;
	}

	public String getField(String fieldname)
	{
		String returnvalue = new String();
		if 		(fieldname.equals("key")) 		{returnvalue = this.getKey();}
		else if (fieldname.equals("value")) 	{returnvalue = this.getValue();}
		else 	{returnvalue = "no field '"+fieldname+"' in Object 'Variable'";}
		return returnvalue;
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

	public void setValue(String value)
	{
		this.value = value;
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

	public void setFree(boolean free)
	{
		this.free = free;
	}

	public void setTest(ArrayList<Test> test)
	{
		this.test = test;
	}

	public void setChoice(ArrayList<String> choice)
	{
		this.choice = choice;
	}

}
