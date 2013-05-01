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

}
