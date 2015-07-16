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
	
	private Init parent = new Init();
	
	private String field = "";
	private String pattern = "";

	private ArrayList<Log> log = new ArrayList<Log>();

	/*----------------------------
	  constructors
	----------------------------*/
	public Match()
	{
	}

	public Match(Init parent)
	{
		this.parent = parent;
		this.parent.addMatch(this);
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
		return this.getParent().getParent().resolveString(this.pattern);
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
		this.log.add(new Log("match "+this.getField()+" ["+this.toString()+"]", loglevel, logmessage));
	}


	/**
	 * @return the parent
	 */
	public Init getParent() {
		return parent;
	}


	/**
	 * @param parent the parent to set
	 */
	public void setParent(Init parent) {
		this.parent = parent;
	}
	

}
