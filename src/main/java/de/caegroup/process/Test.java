package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;

public class Test
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "";
	private String description = "";
	private ArrayList<Param> param = new ArrayList<Param>();

	// nicht an xml gebundene felder
	private String testFeedback = null;
	private boolean testResult = false;

	/*----------------------------
	  constructors
	----------------------------*/
	public Test()
	{

	}

	public Test(String name)
	{
		this.name = name;
	}

	/*----------------------------
	  methods
	----------------------------*/
	
	/**
	 * tests the variable
	 * @param variableToTest
	 * @return boolean testResult
	 */
	public boolean doesTestPass(Variable variableToTest)
	{
		Boolean result = null;

		// in die einzelnen Tests abzweigen
		if (this.name.equals(""))
		{
			result = false;
		}

		else if (this.name.equals("matchPattern"))
		{
			result = this.matchPattern(variableToTest, this.getParameterList());
		}
		
		else if (this.name.equals("lessThan"))
		{
			result = this.lessThan(variableToTest, this.getParameterList());
		}
		setTestResult(result);
		return result;
	}
	
//	/**
//	 * tests the file
//	 * @param fileToTest
//	 * @return boolean testResult
//	 */
//	public boolean doesTestPass(File fileToTest)
//	{
//		Boolean result = null;
//
//		// in die einzelnen Tests abzweigen
//		if (this.name.equals(""))
//		{
//			result = false;
//		}
//
//		else if (this.name.equals("matchPattern"))
//		{
//			result = this.matchPattern(fileToTest, this.getParameterList());
//		}
//		
//		return result;
//	}
	
	private ArrayList<String> getParameterList()
	{
		ArrayList<String> parameter = new ArrayList<String>();
		// eine ParameterListe erzeugen
		for( Param p : this.param)
		{
			parameter.add(p.getContent());
		}
		return parameter;
	}
	
	/*----------------------------
	  test methods
	----------------------------*/
	
	/**
	 * tests whether the Variable-value matches all patterns
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean matchPattern(Variable testVariable, ArrayList<String> pattern)
	{
		boolean result = true;
		this.testFeedback = "";
		for (String p : pattern)
		{
			if (!(testVariable.getValue().matches(p)))
			{
				addTestFeedback("value "+testVariable.getValue()+" does not match pattern '"+p+"'");
				result = false;
			}
			else
			{
				addTestFeedback("value "+testVariable.getValue()+" matches pattern '"+p+"'");
			}
		}
		return result;
	}
	
	/**
	 * tests whether the Integer is less than a given treshhold
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean lessThan(Variable testVariable, ArrayList<String> grenzwert)
	{
		boolean result = true;
		this.testFeedback = "";
		for (String g : grenzwert)
		{
			int grenzwert_integer = Integer.parseInt(g);
			
			if (!( Integer.parseInt(testVariable.getValue()) < grenzwert_integer ))
			{
				addTestFeedback("value "+testVariable.getValue()+" is not < "+grenzwert_integer);
				result = false;
			}
			else
			{
				addTestFeedback("value "+testVariable.getValue()+" is < "+grenzwert_integer);
			}
		}
		return result;
	}
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public ArrayList<Param> getParam()
	{
		return this.param;
	}

	public String getTestFeedback()
	{
		return this.testFeedback;
	}

	public boolean getTestResult()
	{
		return this.testResult;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setParam(ArrayList<Param> param)
	{
		this.param = param;
	}

	public void setTestFeedback(String testFeedback)
	{
		this.testFeedback = testFeedback;
	}

	public void addTestFeedback(String testFeedback)
	{
		if (!(testFeedback != null))
		{
			this.testFeedback = this.testFeedback + "\n" + testFeedback;
		}
		else
		{
			this.testFeedback = testFeedback;
		}
	}

	public void setTestResult(boolean testResult)
	{
		this.testResult = testResult;
	}

}
