package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.Calendar;

public class Log
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private long time = System.currentTimeMillis();
	private String level = "";
	private String msg = "";
	
	/*----------------------------
	  constructors
	----------------------------*/
	public Log()
	{

	}

	public Log(String msg)
	{
		this.msg = msg;
	}

	public Log(String level, String msg)
	{
		this.level = level;
		this.msg = msg;
	}
	/*----------------------------
	  methods
	----------------------------*/


	/*----------------------------
	  methods getter & setter
	----------------------------*/

	/**
	 * @return the time
	 */
	public long getTime()
	{
		return this.time;
	}

	/**
	 * @return the level
	 */
	public String getLevel()
	{
		return this.level;
	}

	/**
	 * @return the msg
	 */
	public String getMsg()
	{
		return this.msg;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time)
	{
		this.time = time;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(String level)
	{
		this.level = level;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

}
