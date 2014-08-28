package de.caegroup.process;

import java.io.*;
import java.util.Date;

public class Log
implements Serializable, Comparable

{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;

	private Object parent = null;
	private Date date = new Date();
	private String level = "default";
	private String msg = "";

	/*----------------------------
	  constructors
	----------------------------*/
	public Log(Object parent)
	{
		this.parent = parent;
	}

	public Log(Object parent, String msg)
	{
		this.parent = parent;
		this.msg = msg;
	}

	public Log(Object parent, String level, String msg)
	{
		this.parent = parent;
		this.level = level;
		this.msg = msg;
	}
	/*----------------------------
	  methods
	----------------------------*/

	public void print()
	{
		System.out.println("["+this.date.toString()+"]:"+this.getLevel()+":"+this.getMsg());
	}

	/*----------------------------
	  methods getter & setter
	----------------------------*/

	/**
	 * @return the time in Milliseconds
	 */
	public long getTime()
	{
		return this.date.getTime();
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time)
	{
		this.date.setTime(time);
	}

	/**
	 * @return the date
	 */
	public Date getDate()
	{
		return this.date;
	}

	/**
	 * @param set the date
	 */
	public void setDate(Date date)
	{
		this.date = date;
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
	 * @return the parentType
	 */
	public String getParentType()
	{
		return "" + this.parent.getClass();
	}

	public int compareTo(Object logToCompare)
	{
		return (this.getDate().compareTo(((Log)logToCompare).getDate()));
	}

}
