package de.prozesskraft.pkraft;

import java.io.*;
import java.util.*;
//import java.util.HashMap;
//import java.util.Map;
import java.util.regex.*;
import org.apache.solr.common.util.NamedList;

public class Exit
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private int value = 0;
	private String msg = "";

	private Work parent = null;

	/*----------------------------
	  constructors
	----------------------------*/
	public Exit()
	{
		this.parent = new Work();
	}

	public Exit(Work parent)
	{
		this.parent = parent;
	}

	/*----------------------------
	  methods
	----------------------------*/

	/*----------------------------
	  methods getter / setter
	----------------------------*/
	
	public Work getParent()
	{
		return this.parent;
	}

	public void setParent(Work work)
	{
		this.parent = work;
	}

	public int getValue()
	{
		return this.value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}

	public String getMsg()
	{
		return this.msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}


}
