package de.caegroup.process;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
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
	private String listname = null;
	private String fromobjecttype = null;
	private String returnfield = null;
	private String fromstep = null;
	private String insertrule = "overwrite";  // append(initialisierte Eintraege werden zu bestehenden angefuegt) | unique (gleichlautende neue eintraege ueberschreiben bestehende = keine duplikate) | overwrite (evtl. vorher bestehende eintraege werden entfernt) 
	private ArrayList<Match> match = new ArrayList<Match>();
	private ArrayList<String> value = new ArrayList<String>();
	private int minoccur = 0;
	private int maxoccur = 999999;
	private String description = "";

	private String loop = new String();
	private String loopvar = new String();
	
	private ArrayList<Log> log = new ArrayList<Log>();

	private String status = new String();	// waiting/initializing/finished/error

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

	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	public ArrayList<Log> getLogRecursive()
	{
		ArrayList<Log> logRecursive = this.log;
		for(Match actMatch : this.match)
		{
			logRecursive.addAll(actMatch.getLog());
		}

		// sortieren nach Datum
		Collections.sort(logRecursive);

		return logRecursive;
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
	  methods
	----------------------------*/
	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("init "+this.getListname(), loglevel, logmessage));
	}
	
	/**
	 * initialisierung durchfuehren
	 */
	public void doIt()
	{
		// die Steps einsammeln, die im attribut 'fromstep' definiert sind
		// wenn es ein multistep ist, koennen hier auch mehr als 1 zurueckgeliefert werden
		// wenn kann multistep, so ist nur 1 fromstep in der liste
		ArrayList<Step> fromsteps = this.getParent().getParent().getSteps(this.getFromstep());

		// ueber alle fromsteps iterieren und die gefordterte liste (mit dem namen des inits) erstellen (nur EINE insgesamt, nicht eine PRO fromstep)
		for (Step actualFromstep : fromsteps)
		{
			log("debug", "looking for field '"+this.getReturnfield()+"' from a "+this.getFromobjecttype()+" of step '"+actualFromstep.getName()+"'");

			// wenn es ein file ist
			if (this.getFromobjecttype().equals("file"))
			{
				ArrayList<File> files_from_fromstep = actualFromstep.getFile();
				ArrayList<File> files_from_fromstep_which_matched = new ArrayList<File>();
				// wenn match-angaben vorhanden sind, wird die fileliste reduziert
				
				for(Match actualMatch : this.getMatch())
				{
					log("debug", "accepting only "+this.getFromobjecttype()+"(s) which match '"+actualMatch.getPattern()+"' and field '"+actualMatch.getField()+"'");
					// iteriere ueber alle Files der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
					for(File actualFile : files_from_fromstep)
					{
						if (actualFile.match(actualMatch))
						{
							files_from_fromstep_which_matched.add(actualFile);
						}
					}
				}

				// feststellen ob die gewuenschte anzahl gematched hat
				if( (files_from_fromstep_which_matched.size() < this.getMinoccur()) || (files_from_fromstep_which_matched.size() > this.getMaxoccur()) )
				{
					log("debug", "found "+files_from_fromstep_which_matched.size()+" items to add to the list, but minoccur="+this.getMinoccur()+", maxoccur="+this.getMaxoccur());
					setStatus("error");
					return;
				}

				// aus der reduzierten file-liste, das gewuenschte field (returnfield) extrahieren und in der list unter dem Namen ablegen
				// ist eine liste mit dem namen schon vorhanden, dann soll keine neue angelegt werden
				List list;
				if (this.getParent().getList(this.getListname()) != null)
				{
					list = this.getParent().getList(this.getListname());
				}
				// ansonsten eine anlegen und this hinzufuegen
				else
				{
					list = new List();
					list.setName(this.getListname());
					this.getParent().addList(list);
				}
				for (File actualFile : files_from_fromstep_which_matched)
				{
					list.addItem(actualFile.getField(this.getReturnfield()));
				}
				log("debug", "new list '"+list.getName()+"' with "+list.getItem().size()+" item(s).");
			}
			
			// wenn es ein variable ist
			else if (this.getFromobjecttype().equals("variable"))
			{
				ArrayList<Variable> variables_from_fromstep = actualFromstep.getVariable();
				ArrayList<Variable> variables_from_fromstep_which_matched = new ArrayList<Variable>();

				for (Match actualMatch : this.getMatch())
				{
					log("debug", "accepting only "+this.getFromobjecttype()+"(s) which match '"+actualMatch.getPattern()+"' and field '"+actualMatch.getField()+"'");
					// iteriere ueber alle Variablen der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
					for (Variable actualVariable : variables_from_fromstep)
					{
						if (actualVariable.match(actualMatch))
						{
							variables_from_fromstep_which_matched.add(actualVariable);
						}
					}
				}

				// feststellen ob die gewuenschte anzahl der variablen gematched hat
				if( (variables_from_fromstep_which_matched.size() < this.getMinoccur()) || (variables_from_fromstep_which_matched.size() > this.getMaxoccur()) )
				{
					log("debug", "found "+variables_from_fromstep_which_matched.size()+" items to add to the list, but minoccur="+this.getMinoccur()+", maxoccur="+this.getMaxoccur());
					this.setStatus("error");
					return;
				}

				// aus der reduzierten variablen-liste, das gewuenschte field (returnfield) extrahieren und in der initlist unter dem Namen ablegen
				// ist eine liste mit dem namen schon vorhanden, dann soll keine neue angelegt werden
				List list;
				if (this.getParent().getList(this.getListname()) != null)
				{
					list = this.getParent().getList(this.getListname());
				}
				// ansonsten eine anlegen und this hinzufuegen
				else
				{
					list = new List();
					list.setName(this.getListname());
					this.getParent().addList(list);
				}

				// hinzufuegen der listitems
				for(Variable actualVariable : variables_from_fromstep_which_matched)
				{
					list.addItem(actualVariable.getField(this.getReturnfield()));
				}
				
				log("debug", "new list '"+list.getName()+"' with "+list.getItem().size()+" item(s).");
			}
		}
		this.setStatus("finished");
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

		// check ob die angabe in fromstep auf einen existierenden step zeigt
		if( !(this.getParent().getParent().isStep(this.getFromstep())) )
		{
			result = false;
			this.log("error", "fromstep '"+this.getFromstep()+"' does not exist");
		}
		
		if( !(this.getFromobjecttype().matches("file|variable") ))
		{
			result = false;
			this.log("error", "fromobjecttype '"+this.getFromobjecttype()+"' does not match /file|variable/");
		}
		
		if( !(this.getReturnfield().matches(".+") ))
		{
			result = false;
			log("error", "returnfield '"+this.getReturnfield()+"' does not match /.+/");
		}
		
		return result;
	}


}
