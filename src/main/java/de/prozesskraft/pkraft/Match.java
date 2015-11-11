package de.prozesskraft.pkraft;

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
	
	private String field = "";
	private String pattern = "";

	ArrayList<Log> log = new ArrayList<Log>();

	// don't clone parent when you clone this
	private Init parent = null;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public Match()
	{
//		Init dummyInit = new Init();
//		dummyInit.setListname("dummy");
//		this.parent = dummyInit;
	}

	public Match(Init parent)
	{
		this.parent = parent;
		this.parent.addMatch(this);
	}


	/*----------------------------
	  methods
	----------------------------*/
	public Match clone()
	{
		Match newMatch = new Match();
		
		newMatch.setField(this.getField());
		newMatch.setPattern(this.getPattern());
		
		for(Log actLog : this.getLog())
		{
			newMatch.addLog(actLog.clone());
		}
		
		return newMatch;
	}
	
	public void addLog(Log log)
	{
		log.setLabel("match (field="+this.getField()+", pattern="+this.getPattern());
		this.log.add(log);
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
		this.addLog(new Log(loglevel, logmessage));
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
