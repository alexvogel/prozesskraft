package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;

import org.apache.commons.lang3.SerializationUtils;

public class Subprocess
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
	private String version = "noversion";

	private Step step = new Step("root");
	
//	private String status = "";	// waiting/finished/error

	private ArrayList<Log> log = new ArrayList<Log>();


	/*----------------------------
	  constructors
	----------------------------*/
	public Subprocess()
	{

	}

	public Subprocess(String name, String version)
	{
		this.name = name;
		this.version = version;
	}

	public Subprocess(String name, String version, Step step)
	{
		this.name = name;
		this.version = version;
		this.step = step;
	}

	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * clone
	 * returns a clone of this
	 * @return Variable
	 */
	@Override
	public Subprocess clone()
	{
		return SerializationUtils.clone(this);
	}
	
	/**
	 * reset this
	 */
	public void reset()
	{
		this.getLog().clear();
//		this.setStatus("");
	}
	

	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("subprocess-"+this.getName()+"-"+this.getVersion(), loglevel, logmessage));
	}

	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getVersion()
	{
		return this.version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public Step getStep()
	{
		return this.step;
	}

	public void setStep(Step step)
	{
		this.step = step;
	}

	public Step getRoot()
	{
		return this.step;
	}

	public void setRoot(Step step)
	{
		this.step = step;
	}
	
	private ArrayList<Log> getLog()
	{
		return this.log;
	}
}
