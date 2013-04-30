package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import java.util.HashMap;
//import java.util.Map;

//import org.apache.solr.common.util.NamedList;

public class Commit
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String id = new String();
	private boolean toroot = false;
	private String type = new String();
	private String name = new String();
	private String filename = new String();
	private String value = new String();
	private String loop = new String();
	private String loopvar = new String();
	
	private boolean success = false;
	private java.lang.Process proc;
	private int exitvalue;
	private Step parent;

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
	  methods get
	----------------------------*/

	// ein file in den aktuellen step committen
	public void commitfile(java.io.File file)
	{
		this.parent.commitfile(file);
	}

	public void commitvariable(String name, String value)
	{
		this.parent.commitvariable(name, value);
	}
	/*----------------------------
	  methods get
	----------------------------*/
	public String getId()
	{
		return this.id;
	}

	public boolean getToroot()
	{
		return this.toroot;
	}

	public String getType()
	{
		return this.type;
	}

	public String getName()
	{
		return this.name;
	}
	
	public String getFrompool()
	{
		return this.filename;
	}

	public String getFilename()
	{
		return this.filename;
	}

	public String getValue()
	{
		return this.value;
	}

	public String getLoop()
	{
		return this.loop;
	}
	
	public String getLoopvar()
	{
		return this.loopvar;
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
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setId(String id)
	{
		this.id = id;
	}

	public void setToroot(boolean toroot)
	{
		this.toroot = toroot;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setValue(String value)
	{
		this.value = value;
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

}
