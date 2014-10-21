package de.caegroup.commons;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Log
implements Serializable, Comparable

{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;

	private String label = null;
	
	private long time = System.currentTimeMillis();
	private String level = null;
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

	public Log(String label, String level, String msg)
	{
		this.label = label;
		this.level = level;
		this.msg = msg;
	}
	/*----------------------------
	  methods
	----------------------------*/

	public String sprint()
	{
		String myOutput = "["+this.getTimestamp()+"]";
		
		if(this.getLabel() != null)
		{
			myOutput += ":"+this.getLabel();
		}
		if(this.getLevel() != null)
		{
			myOutput += ":"+this.getLevel();
		}
		myOutput += ":"+this.getMsg();
		
		return myOutput;
	}

	public void print()
	{
		System.out.println(this.sprint());
	}

	/*----------------------------
	 static  methods
	----------------------------*/
	static public String sprintWholeLog(ArrayList<Log> bigLog)
	{
		String logBook = "";
		for(Log actLog : bigLog)
		{
			if(logBook.equals(""))
			{
				logBook += actLog.sprint();
			}
		}
		return logBook;
	}
	
	/*----------------------------
	  methods getter & setter
	----------------------------*/

	/**
	 * @return the time in Milliseconds
	 */
	public long getTime()
	{
		return this.time;
	}

	/**
	 * @return the time as a Timestamp-String
	 */
	public String getTimestamp()
	{
//		return "unbekannt";
		return (new Timestamp(this.time)).toString();
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time)
	{
		this.time = time;
	}

	/**
	 * @return the level
	 */
	public String getLevel()
	{
		return this.level;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(String level)
	{
		this.level = level;
	}

	/**
	 * @return the msg
	 */
	public String getMsg()
	{
		return this.msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * @return the label of parent
	 */
	public String getLabel()
	{
		return this.label;
	}

	public int compareTo(Object logToCompare)
	{
		return (int)(this.time - ((Log)logToCompare).getTime());
	}

	

}
