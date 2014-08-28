package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;

public class Match
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String field = new String();
	private String pattern = new String();

	private ArrayList<Log> log = new ArrayList<Log>();

	/*----------------------------
	  constructors
	----------------------------*/
	public Match()
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

	public ArrayList<Log> getLog()
	{
		return this.log;
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

	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log(this, loglevel, logmessage));
	}
	

}
