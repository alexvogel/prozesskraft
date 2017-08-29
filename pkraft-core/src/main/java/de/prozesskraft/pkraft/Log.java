package de.prozesskraft.pkraft;

import java.io.*;
import java.sql.Timestamp;

public class Log
implements Serializable, Comparable

{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;

	private String label = "";
	
	private long time = System.currentTimeMillis();
	private String level = "default";
	private String msg = "";
	
	/*----------------------------
	  constructors
	----------------------------*/
	public Log()
	{

	}

	public Log(String level, String msg)
	{
		this.level = level;
		this.msg = msg;
	}
	/*----------------------------
	  methods
	----------------------------*/

	public void print()
	{
		System.out.println("["+this.getTimestamp()+"]:"+this.getLabel()+":"+this.getLevel()+":"+this.getMsg());
	}

	public String sprint()
	{
		return "["+this.getTimestamp()+"]:"+this.getLabel()+":"+this.getLevel()+":"+this.getMsg();
	}

	public Log clone()
	{
		Log newLog = new Log();
		newLog.setLabel(this.getLabel());
		newLog.setLevel(this.getLevel());
		newLog.setMsg(this.getMsg());
		newLog.setTime(this.getTime());
		
		return newLog;
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

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

}
