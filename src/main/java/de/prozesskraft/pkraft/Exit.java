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

	// don't clone parent when you clone this
	private Work parent = null;
	transient private Work parentDummy = null;
	/*----------------------------
	  constructors
	----------------------------*/
	public Exit()
	{
		Work dummyWork = new Work();
		dummyWork.setName("dummy");
		this.parentDummy = dummyWork;
	}

	public Exit(Work parent)
	{
		this.parent = parent;
	}

	/*----------------------------
	  methods
	----------------------------*/
	public Exit clone()
	{
		Exit newExit = new Exit();
		newExit.setValue(this.getValue());
		newExit.setMsg(this.getMsg());
		
		return newExit;
	}
	
	/**
	 * deserialize not in a standard way
	 * @param stream
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException
	{
		stream.defaultReadObject();

		// erstellen eines parentDummies, falls notwendig
		if(parent == null)
		{
			parentDummy = new Work();
		}
	}
	
	/*----------------------------
	  methods getter / setter
	----------------------------*/
	
	/**
	 * @return the parent
	 */
	public Work getParent()
	{
		if(this.parent != null)
		{
			return this.parent;
		}
		else
		{
			return parentDummy;
		}
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
