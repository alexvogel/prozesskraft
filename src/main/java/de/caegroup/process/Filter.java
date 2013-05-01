package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;

public class Filter
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String field = new String();
	private String pattern = new String();

	/*----------------------------
	  constructors
	----------------------------*/
	public Filter()
	{

	}


	/*----------------------------
	  methods get
	----------------------------*/
	public String getField()
	{
		return this.field;
	}

	public String getPattern()
	{
		return this.pattern;
	}


	/*----------------------------
	methods set
	----------------------------*/
	public void setField(String field)
	{
		this.field = field;
	}

	public void setPattern(String pattern)
	{
		this.pattern = pattern;
	}


}
