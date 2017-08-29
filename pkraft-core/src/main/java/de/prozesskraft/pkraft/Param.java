package de.prozesskraft.pkraft;

import java.io.*;

public class Param
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private int id = 0;
	private String content = "";

	/*----------------------------
	  constructors
	----------------------------*/
	public Param()
	{

	}

	public Param(int id)
	{
		this.id = id;
	}

	public Param(int id, String content)
	{
		this.id = id;
		this.content = content;
	}

	/*----------------------------
	  methods
	----------------------------*/
	public Param clone()
	{
		Param clone = new Param();
		clone.setId(this.getId());
		clone.setContent(this.getContent());
		
		return clone;
	}
	
	
	/*----------------------------
	  methods get
	----------------------------*/
	public int getId()
	{
		return this.id;
	}

	public String getContent()
	{
		return this.content;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setId(int id)
	{
		this.id = id;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

}
