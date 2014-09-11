package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
	public void reset()
	{
		this.setTestFeedback(null);
		this.setTestResult(false);
	}
	
	/**
	 * tests the variable
	 * @param variableToTest
	 * @return boolean testResult
	 */
	public void performTest(Variable variableToTest)
	{
		Boolean result = null;

		// in die einzelnen Tests abzweigen
		if (this.name.equals(""))
		{
			result = false;
		}

		else if (this.name.equals("matchPattern"))
		{
			result = this.testVariableMatchPattern(variableToTest, this.getParameterList());
		}
		
		else if (this.name.equals("lessThan"))
		{
			result = this.testVariableLessThan(variableToTest, this.getParameterList());
		}
		
		else if (this.name.equals("moreThan"))
		{
			result = this.testVariableMoreThan(variableToTest, this.getParameterList());
		}
		
		else
		{
			result = false;
			setTestFeedback("variable test with name '"+this.name+"' does not exist. please check process definition.");
		}
		
		setTestResult(result);
	}
	
	/**
	 * tests the variable
	 * @param variableToTest
	 * @return boolean testResult
	 */
	public void performTest(File fileToTest)
	{
		Boolean result = null;

		// in die einzelnen Tests abzweigen
		if (this.name.equals(""))
		{
			result = false;
		}

		else if (this.name.equals("sizeGreaterThan"))
		{
			result = this.testFileSizeGreaterThan(fileToTest, this.getParameterList());
		}
		
		else if (this.name.equals("doesExist"))
		{
			result = this.testFileDoesExist(fileToTest, this.getParameterList());
		}
		
		else if (this.name.equals("matchPattern"))
		{
			result = this.testFileMatchPattern(fileToTest, this.getParameterList());
		}
		
		else
		{
			result = false;
			setTestFeedback("file test with name '"+this.name+"' does not exist. please check process definition.");
		}
		
		setTestResult(result);
	}
	
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
	  test file methods
	----------------------------*/

	/**
	 * tests whether the fileSize is greater than a given treshhold
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean testFileSizeGreaterThan(File testFile, ArrayList<String> param)
	{
		boolean result = true;
		
		if (param.size() != 2)
		{
			setTestFeedback("error in test definition for files. test sizeGreaterThan need exact 2 param.");
			result = false;
			return result;
		}
		
		String sizeScale = param.get(0);
		long sizeTreshold = Integer.parseInt(param.get(1));
		
		if (sizeScale.matches("^byte$|^kilobyte$|^megabyte$|^gigabyte$"))
		{
			if (testFile.getSize(sizeScale) <= sizeTreshold)
			{
				setTestFeedback("size of file '"+testFile.getAbsfilename()+"' is not greater than '"+sizeTreshold+"' "+sizeScale);
				result = false;
			}
			else
			{
				setTestFeedback("size of file '"+testFile.getAbsfilename()+"' is greater than '"+sizeTreshold+"' "+sizeScale);
			}
		}
		else
		{
			setTestFeedback("unknown sizeScale '"+sizeScale+"'. check process definition.");
			result = false;
		}
		return result;
	}

	/**
	 * tests whether the file does exist
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean testFileDoesExist(File testFile, ArrayList<String> param)
	{
		boolean result = true;
		
		if (param.size() > 0)
		{
			setTestFeedback("error in test definition for files. test doesExist need exact 0 param.");
			result = false;
			return result;
		}
		
		if (!(testFile.doesExist()))
		{
			setTestFeedback("file '"+testFile.getAbsfilename()+"' does not exist.");
			result = false;
		}
		else if(testFile.doesExist())
		{
			setTestFeedback("file '"+testFile.getAbsfilename()+"' does exist");
		}
		return result;
	}

	/**
	 * VariableTest: tests whether the Variable-value matches all patterns
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean testFileMatchPattern(File testFile, ArrayList<String> param)
	{
		boolean result = false;

		if (param.size() != 1)
		{
			setTestFeedback("error in test definition for files. test matchPattern needs exact 1 param.");
			result = false;
			return result;
		}
		
		String pattern = param.get(0);
		
		// einlesen der datei
		BufferedReader reader;
		try
		{
			reader = new BufferedReader( new FileReader (testFile.getAbsfilename()));
		
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			String ls = System.getProperty("line.separator");

			while ( ( line = reader.readLine()) != null)
			{
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			
			reader.close();

			String content = stringBuilder.toString();
			String[] lines = content.split("\\n");
			
			// ueber alle zeilen iterieren bis das muster einmal gefunden wurde
			for (String l : lines)
			{
				if (l.matches(".*"+Pattern.quote(pattern)+".*"))
				{
					setTestFeedback("content of file '"+testFile.getAbsfilename()+"' matches pattern '"+pattern+"'");
					return true;
				}
			}
			setTestFeedback("content of file '"+testFile.getAbsfilename()+"' does not match pattern '"+pattern+"'");

		
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			setTestFeedback("content of file '"+testFile.getAbsfilename()+"' does not match pattern '"+pattern+"', because file does not exist.");
//			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			
//				e.printStackTrace();
		}
		return result;
		
	}
	

	/*----------------------------
	  test variable methods
	----------------------------*/
	
	/**
	 * VariableTest: tests whether the Variable-value matches all patterns
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean testVariableMatchPattern(Variable testVariable, ArrayList<String> param)
	{
		boolean result = true;

		if (param.size() != 1)
		{
			setTestFeedback("error in test definition for variables. test matchPattern needs exact 1 param.");
			result = false;
			return result;
		}
		
		String pattern = param.get(0);
		
		if (!(testVariable.getValue().matches(pattern)))
		{
			setTestFeedback("value '"+testVariable.getValue()+"' does not match pattern '"+pattern+"'");
			result = false;
		}
		else
		{
			setTestFeedback("value '"+testVariable.getValue()+"' matches pattern '"+pattern+"'");
		}
		return result;
	}
	
	/**
	 * tests whether the Integer is less than a given treshhold
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean testVariableLessThan(Variable testVariable, ArrayList<String> param)
	{
		boolean result = true;
		
		if (param.size() != 1)
		{
			setTestFeedback("error in test definition for variables. test lessThan needs exact 1 param.");
			result = false;
			return result;
		}
		
		int grenzwert_integer = Integer.parseInt(param.get(0));
		
		if (!( Integer.parseInt(testVariable.getValue()) < grenzwert_integer ))
		{
			setTestFeedback("value '"+testVariable.getValue()+"' is not < "+grenzwert_integer);
			result = false;
		}
		else
		{
			setTestFeedback("value '"+testVariable.getValue()+"' is < "+grenzwert_integer);
		}

		return result;
	}
	
	/**
	 * tests whether the Integer is greater than a given treshhold
	 * @param Variable testVariable, ArrayList<String> pattern
	 * @return boolean testResult
	 */
	private boolean testVariableMoreThan(Variable testVariable, ArrayList<String> param)
	{
		boolean result = true;

		if (param.size() != 1)
		{
			setTestFeedback("error in test definition for variables. test moreThan needs exact 1 param.");
			result = false;
			return result;
		}
		
		int grenzwert_integer = Integer.parseInt(param.get(0));
		
		if (!( Integer.parseInt(testVariable.getValue()) > grenzwert_integer ))
		{
			setTestFeedback("value '"+testVariable.getValue()+"' is not > "+grenzwert_integer);
			result = false;
		}
		else
		{
			setTestFeedback("value '"+testVariable.getValue()+"' is > "+grenzwert_integer);
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
