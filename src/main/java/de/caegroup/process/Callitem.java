package de.caegroup.process;

import java.io.*;
import java.util.*;
//import java.util.HashMap;
//import java.util.Map;
import java.util.regex.*;
import org.apache.solr.common.util.NamedList;

public class Callitem
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private int sequence;
	private String par = "";
	private String del = "";
	private String val = "";
	private String loop = new String();
	private String loopvar = new String();
	
	private String status = new String();	// waiting/initializing/working/committing/ finished/broken/cancelled

	private Work parent = null;

	/*----------------------------
	  constructors
	----------------------------*/
	public Callitem()
	{
		this.parent = new Work();
	}

	public Callitem(Work parent)
	{
		this.parent = parent;
	}

	/*----------------------------
	  methods resolve
	----------------------------*/

	private String resolve(String stringToResolve)
	{
		String resolvedString = null;

		String patt = "list\\((.+)\\)";
		Pattern r = Pattern.compile(patt);
		Matcher m = r.matcher(stringToResolve);
		
		if (m.find())
		{
			String substitution = new String();
//			this.parent.getList(m.group(1));
			ArrayList<String> listItems = (this.parent.getListItems(m.group(1)));
			Iterator<String> iterListItem = listItems.iterator();
			while(iterListItem.hasNext())
			{
				substitution = substitution + iterListItem.next();
			}
			resolvedString = m.replaceAll(substitution);
		}
		return resolvedString;
	}
	/*----------------------------
	  methods get
	----------------------------*/
	public int getSequence()
	{
		return this.sequence;
	}

	public String getPar()
	{
		return this.par;
	}

	public String getDel()
	{
		return this.del;
	}

	public String getVal()
	{
		return this.val;
	}
	
	public String getStatus()
	{
		return this.status;
	}

	public String getRespar()
	{
		return resolve(this.getPar());
	}

	public String getResdel()
	{
		return resolve(this.getDel());
	}

	public String getResval()
	{
		return resolve(this.getVal());
	}
	
	public String getLoop()
	{
		return this.loop;
	}
	
	public String getLoopvar()
	{
		return this.loopvar;
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}

	public void setPar(String par)
	{
		this.par = par;
	}

	public void setDel(String del)
	{
		this.del = del;
	}

	public void setVal(String val)
	{
		this.val = val;
	}

	public void setLoop(String loop)
	{
		this.loop = loop;
	}

	public void setLoopvar(String loopvar)
	{
		this.loopvar = loopvar;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

}
