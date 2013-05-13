package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;

public class File
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String key = "";
	private String filename = "";
	private String description = "";
	private String absfilename = "";
	private int minoccur = 0;
	private int maxoccur = 999999;
	

	/*----------------------------
	  constructors
	----------------------------*/
	public File()
	{

	}


	/*----------------------------
	  methods
	----------------------------*/
	public boolean match(Filter match)
	{
		String fieldname = match.getField();
		String pattern = match.getPattern();
		
		String string_to_test = new String();
		if      (fieldname.equals("filename")) 	{string_to_test = this.getFilename();}
		else if (fieldname.equals("absfilename")) 	{string_to_test = this.getAbsfilename();}

		if (string_to_test.matches(".*"+pattern+".*")) { return true;	}
//		if (string_to_test.matches(pattern)) { return true;	}
		else { return false; }
	}
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getKey()
	{
		return this.key;
	}

	public String getFilename()
	{
		return this.filename;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getAbsfilename()
	{
		return this.absfilename;
	}

	public int getMinoccur()
	{
		return this.minoccur;
	}

	public int getMaxoccur()
	{
		return this.maxoccur;
	}

	public String getField(String fieldname)
	{
		String returnvalue = new String();
		if      (fieldname.equals("filename")) 	{returnvalue = this.getFilename();}
		else if (fieldname.equals("absfilename")) 	{returnvalue = this.getAbsfilename();}
		else 	{returnvalue = "no field '"+fieldname+"' in Object 'File'";}
		return returnvalue;
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setKey(String key)
	{
		this.key = key;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setAbsfilename(String absfilename)
	{
		this.absfilename = absfilename;
	}

	public void setMinoccur(int minoccur)
	{
		this.minoccur = minoccur;
	}

	public void setMaxoccur(int maxoccur)
	{
		this.maxoccur = maxoccur;
	}


}
