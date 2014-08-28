package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import java.util.HashMap;
//import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.lang3.SerializationUtils;

//import org.apache.solr.common.util.NamedList;

public class Commit
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "";
	private boolean toroot = false;
	private String loop = "";
	private String loopvar = "";
	private ArrayList<Variable> variable = new ArrayList<Variable>();
	private ArrayList<File> file = new ArrayList<File>();
	
	private boolean success = false;
	private java.lang.Process proc;
	private int exitvalue;
	private Step parent;

	private ArrayList<Log> log = new ArrayList<Log>();
	/*----------------------------
	  constructors
	----------------------------*/
	public Commit()
	{
		parent = new Step();
	}

	public Commit(Step s)
	{
		parent = s;
	}

	
	/*----------------------------
	  methods
	----------------------------*/

	/**
	 * clone
	 * returns a clone of this
	 * @return Commit
	 */
	@Override
	public Commit clone()
	{
		return SerializationUtils.clone(this);
	}
	
	/*----------------------------
	  methods get
	----------------------------*/

	// ein file in den aktuellen step committen
	public void commitFile(java.io.File file)
	{
		this.parent.commitFile("default", file);
	}

	public void commitVariable(String name, String value)
	{
		this.parent.commitVariable(name, value);
	}

	public void addFile(File file)
	{
		this.file.add(file);
//		System.out.println("NOW FILES AMOUNT: "+this.files.size());
	}

	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public boolean getToroot()
	{
		return this.toroot;
	}

	public String getLoop()
	{
		return this.loop;
	}
	
	public String getLoopvar()
	{
		return this.loopvar;
	}

	public ArrayList<Variable> getVariable()
	{
		return this.variable;
	}

	public ArrayList<File> getFile()
	{
		return this.file;
	}

	public boolean getSuccess()
	{
		return this.success;
	}

	public java.lang.Process getProc()
	{
		return this.proc;
	}
	
	public int getExitvalue()
	{
		return this.exitvalue;
	}
	
	public Step getParent()
	{
		return this.parent;
	}
	
	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	public ArrayList<Log> getLogRecursive()
	{
		ArrayList<Log> logRecursive = this.log;

// alle logs aller Variablen hinzufuegen
		for(Variable actVariable : this.variable)
		{
			logRecursive.addAll(actVariable.getLog());
		}
// alle logs aller Files hinzufuegen
		for(File actFile : this.file)
		{
			logRecursive.addAll(actFile.getLog());
		}

		// sortieren nach Datum
		Collections.sort(logRecursive);

		return logRecursive;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setToroot(boolean toroot)
	{
		this.toroot = toroot;
	}

	public void setVariable(ArrayList<Variable> variable)
	{
		this.variable = variable;
	}

	public void setFile(ArrayList<File> file)
	{
		this.file = file;
	}

	public void setLoop(String loop)
	{
		this.loop = loop;
	}
	
	public void setLoopvar(String loopvar)
	{
		this.loopvar = loopvar;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public void setProc(java.lang.Process proc)
	{
		this.proc = proc;
	}

	public void setExitvalue(int exitvalue)
	{
		this.exitvalue = exitvalue;
	}

	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}

	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log(this, loglevel, logmessage));
	}

}
