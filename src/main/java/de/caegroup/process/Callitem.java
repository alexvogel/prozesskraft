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
	private String sequence = new String();
	private String par = new String();
	private String del = new String();
	private String val = new String();
	private String loop = new String();
	private String loopvar = new String();
	
	private String status = new String();	// waiting/initializing/working/committing/ finished/broken/cancelled
	private String respar = null;
	private String resdel = null;
	private String resval = null;

	/*----------------------------
	  constructors
	----------------------------*/
	public Callitem()
	{

	}

	/*----------------------------
	  methods resolve
	----------------------------*/
	public void resolvePar(NamedList<String> lists)
	{
		this.respar = "";
		String patt = "list\\((.+)\\)";
		Pattern r = Pattern.compile(patt);
		Matcher m = r.matcher(this.par);
		if (m.find())
		{
			System.out.println("PATTERN "+patt+" GEFUNDEN in "+this.par);
			String substitution = new String();
			ArrayList<String> items = new ArrayList<String>(lists.getAll(m.group(1)));
			Iterator<String> iterstring = items.iterator();
			while(iterstring.hasNext())
			{
				substitution = substitution+iterstring.next();
			}
			System.out.println("PATTERN: "+patt+" will be substituted by "+substitution);
			this.respar = m.replaceAll(substitution);
			
		}
		else
		{
			System.out.println("PATTERN "+patt+" NICHT GEFUNDEN in "+this.par);
		}
	}
	
	public void resolveDel(NamedList<String> lists)
	{
		this.resdel = "";
		String patt = "list\\((.+)\\)";
		Pattern r = Pattern.compile(patt);
		Matcher m = r.matcher(this.del);
		if (m.find())
		{
			System.out.println("PATTERN "+patt+" GEFUNDEN in "+this.del);
			String substitution = new String();
			ArrayList<String> items = new ArrayList<String>(lists.getAll(m.group(1)));
			Iterator<String> iterstring = items.iterator();
			while(iterstring.hasNext())
			{
				substitution = substitution+iterstring.next();
			}
			System.out.println("PATTERN: "+patt+" will be substituted by "+substitution);
			this.resdel = m.replaceAll(substitution);
			
		}
		else
		{
			System.out.println("PATTERN "+patt+" NICHT GEFUNDEN in "+this.del);
		}
	}
	
	public void resolveVal(NamedList<String> lists)
	{
		this.resval = "";
		String patt = "list\\((.+)\\)";
		Pattern r = Pattern.compile(patt);
		Matcher m = r.matcher(this.val);
		if (m.find())
		{
			System.out.println("PATTERN "+patt+" GEFUNDEN in "+this.val);
			String substitution = new String();
			ArrayList<String> items = new ArrayList<String>(lists.getAll(m.group(1)));
			Iterator<String> iterstring = items.iterator();
			while(iterstring.hasNext())
			{
				substitution = substitution+iterstring.next();
			}
			System.out.println("PATTERN: "+patt+" will be substituted by "+substitution);
			this.resval = m.replaceAll(substitution);
			System.out.println("VAL: "+this.val+" RESVAL: "+this.resval);

		}
		else
		{
			System.out.println("PATTERN "+patt+" NICHT GEFUNDEN in "+this.val);
		}
	}
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getSequence()
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
	
	public String getLoop()
	{
		return this.loop;
	}
	
	public String getLoopvar()
	{
		return this.loopvar;
	}

	public String getStatus()
	{
		return this.status;
	}

	public String getRespar(NamedList<String> lists)
	{
		if (this.respar == null)
		{
			this.resolvePar(lists);
		}
		return this.respar;
	}

	public String getResdel(NamedList<String> lists)
	{
		if (this.resdel == null)
		{
			this.resolveDel(lists);
		}
		return this.resdel;
	}

	public String getResval(NamedList<String> lists)
	{
		if (this.resval == null)
		{
			this.resolveVal(lists);
		}
		return this.resval;
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setSequence(String sequence)
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
