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
	private ArrayList<String> choice = new ArrayList<String>();
	private ArrayList<Test> test = new ArrayList<Test>();
	private int minoccur = 0;
	private int maxoccur = 999999;
	

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
	public boolean match(Filter match)
	{
		String fieldname = match.getField();
		String pattern = match.getPattern();
		
		String string_to_test = new String();
		if 		(fieldname.equals("name")) 		{string_to_test = this.getName();}
		else if (fieldname.equals("value")) 	{string_to_test = this.getValue();}

		if (string_to_test.matches(pattern)) { return true;	}
		else { return false; }
	}
	

	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.key;
	}

	public String getValue()
	{
		return this.value;
	}

	public String getField(String fieldname)
	{
		String returnvalue = new String();
		if 		(fieldname.equals("name")) 		{returnvalue = this.getName();}
		else if (fieldname.equals("value")) 	{returnvalue = this.getValue();}
		else 	{returnvalue = "no field '"+fieldname+"' in Object 'Variable'";}
		return returnvalue;
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.key = name;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

}
