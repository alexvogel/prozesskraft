package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;

public class Variable
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = new String();
	private String value = new String();

	/*----------------------------
	  constructors
	----------------------------*/
	public Variable()
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
		return this.name;
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
		this.name = name;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

}
