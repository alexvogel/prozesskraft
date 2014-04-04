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
	private String listname = new String();
	private String fromobjecttype = new String();
	private String returnfield = new String();
	private String fromstep = new String();
	private String insertrule = "overwrite";  // append(initialisierte Eintraege werden zu bestehenden angefuegt) | unique (gleichlautende neue eintraege ueberschreiben bestehende = keine duplikate) | overwrite (evtl. vorher bestehende eintraege werden entfernt) 
	private ArrayList<Match> match = new ArrayList<Match>();
	private ArrayList<String> value = new ArrayList<String>();
	private int minoccur = 0;
	private int maxoccur = 999999;
	private String description = "";

	private String loop = new String();
	private String loopvar = new String();
	
	private String status = new String();	// waiting/initializing/working/committing/ finished/broken/cancelled

	private Step parent = null;
	/*----------------------------
	  constructors
	----------------------------*/
	public Init()
	{
		listname = "unnamed";
	}


	/*----------------------------
	  methods
	----------------------------*/
	public void addMatch(Match match)
	{
		this.match.add(match);
	}
	
	public void addValue(String value)
	{
		this.value.add(value);
	}

	
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getListname()
	{
		return this.listname;
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
	
	public String getInsertrule()
	{
		return this.insertrule;
	}
	
	public ArrayList<Match> getMatch()
	{
		return this.match;
	}

	public Match[] getMatch2()
	{
		Match[] matchs = new Match[this.match.size()];
		for(int i=0; i<matchs.length; i++)
		{
			matchs[i] = this.match.get(i);
		}
		return matchs;
	}

	public ArrayList<String> getValue()
	{
		return this.value;
	}

	public String[] getValues2()
	{
		String[] values = new String[this.value.size()];
		for(int i=0; i<values.length; i++)
		{
			values[i] = this.value.get(i);
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

	public int getMinoccur()
	{
		return this.minoccur;
	}

	public int getMaxoccur()
	{
		return this.maxoccur;
	}

	public Step getParent()
	{
		return this.parent;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setListname(String listname)
	{
		this.listname = listname;
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

	public void setInsertrule(String insertrule)
	{
		this.insertrule = insertrule;
	}

	public void setMatch(ArrayList<Match> match)
	{
		this.match = match;
	}

	public void setValue(ArrayList<String> value)
	{
		this.value = value;
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

	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}

	public void setMinoccur(int minoccur)
	{
		this.minoccur = minoccur;
	}

	public void setMaxoccur(int maxoccur)
	{
		this.maxoccur = maxoccur;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/*----------------------------
	  methods consistent
	----------------------------*/

	/**
	 * checks whether the content of init is consistent
	 * @return result
	 */
	public boolean isInitConsistent()
	{
		boolean result = true;

		// check fromstep
		if( !(this.getParent().getParent().isStep(this.getFromstep())) ) {result = false; this.getParent().getParent().log("error", "error in step '"+this.getParent().getName()+"' init '"+this.getListname()+"': fromstep '"+this.getFromstep()+"' does not exist in processModel");}
		
		return result;
	}


}
