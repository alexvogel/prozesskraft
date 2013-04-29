package de.caegroup.process;

import java.io.*;
import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;

//import org.apache.solr.common.util.NamedList;

public class Init
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = new String();
	private String fromobjecttype = new String();
	private String returnfield = new String();
	private String fromstep = new String();
	private ArrayList<Match> matchs = new ArrayList<Match>();
	private ArrayList<String> values = new ArrayList<String>();
	private String loop = new String();
	private String loopvar = new String();
	
	private String status = new String();	// waiting/initializing/working/committing/ finished/broken/cancelled

	/*----------------------------
	  constructors
	----------------------------*/
	public Init()
	{
		name = "unnamed";
	}


	/*----------------------------
	  methods
	----------------------------*/
	public void addMatch(Match match)
	{
		this.matchs.add(match);
	}
	
	public void addValue(String value)
	{
		this.values.add(value);
	}

	
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public String getFromobjecttype()
	{
		return this.fromobjecttype;
	}
	
	public String getReturnfield()
	{
		return this.returnfield;
	}
	
	public String getFromstep()
	{
		return this.fromstep;
	}
	
	public ArrayList<Match> getMatchs()
	{
		return this.matchs;
	}

	public Match[] getMatchs2()
	{
		Match[] matchs = new Match[this.matchs.size()];
		for(int i=0; i<matchs.length; i++)
		{
			matchs[i] = this.matchs.get(i);
		}
		return matchs;
	}

	public ArrayList<String> getValues()
	{
		return this.values;
	}

	public String[] getValues2()
	{
		String[] values = new String[this.values.size()];
		for(int i=0; i<values.length; i++)
		{
			values[i] = this.values.get(i);
		}
		return values;
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

	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setFromobjecttype(String fromobjecttype)
	{
		this.fromobjecttype = fromobjecttype;
	}

	public void setReturnfield(String returnfield)
	{
		this.returnfield = returnfield;
	}

	public void setFromstep(String fromstep)
	{
		this.fromstep = fromstep;
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
