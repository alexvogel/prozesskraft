package de.caegroup.process;

import java.io.*;
import java.util.*;
import org.apache.solr.common.util.NamedList;

import de.caegroup.process.Callitem;

public class Work
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
	private String description = "no description";
	private String command = new String();
	private ArrayList<Callitem> callitem = new ArrayList<Callitem>();
	private String loop = new String();
	private String loopvar = new String();
	
	private String status = new String();	// waiting/initializing/working/committing/ finished/broken/cancelled
	private int exitvalue;
	private Step parent;
	/*----------------------------
	  constructors
	----------------------------*/
	public Work()
	{
		this.parent = new Step();
	}

	public Work(Step step)
	{
		this.parent = step;
	}

	/*----------------------------
	  methods
	----------------------------*/
	public void addCallitem(Callitem callitem)
	{
		this.callitem.add(callitem);
	}
	

	/*----------------------------
	  methods virtual get
	----------------------------*/
	public String getCall()
	{
		ArrayList<Callitem> callitems = this.getCallitemssorted();
		
		StringBuffer callbuffer = new StringBuffer(this.command);
	
		Iterator<Callitem> itercallitem = callitems.iterator();
		while (itercallitem.hasNext())
		{
			Callitem callitem = itercallitem.next();
			callbuffer.append(" ");
			callbuffer.append(callitem.getRespar());
			callbuffer.append(callitem.getResdel());
			callbuffer.append(callitem.getResval());
		}
		return callbuffer.toString();
	}
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getCommand()
	{
		return this.command;
	}

	public ArrayList<Callitem> getCallitems()
	{
		return this.callitem;
	}
	
	public ArrayList<Callitem> getCallitem()
	{
		return this.callitem;
	}
	
	public Callitem getCallitem(int index)
	{
		return this.callitem.get(index);
	}
	
	public ArrayList<Callitem> getCallitemssorted()
	{
		ArrayList<Integer> sequences = new ArrayList<Integer>();
		
		// aus den vorhandenen callitems die sequences in ein eigenes array extrahieren
		Iterator<Callitem> itercallitem = this.callitem.iterator();
		while (itercallitem.hasNext())
		{
			Callitem callitem = itercallitem.next();
			sequences.add(callitem.getSequence());
		}

		// das sequences-array sortieren
		Collections.sort(sequences);
		
		// ueber das sortierte sequences-array iterieren
		ArrayList<Callitem> callitems_sorted = new ArrayList<Callitem>();
		Iterator<Integer> itersequences = sequences.iterator();
		while (itersequences.hasNext())
		{
			int sequence = itersequences.next();
			// und das zugehoerige callitem rausfischen
			Iterator<Callitem> itercallitem2 = this.callitem.iterator();
			while (itercallitem2.hasNext())
			{
				Callitem callitem = itercallitem2.next();
				if (callitem.getSequence() == sequence)
				{
					callitems_sorted.add(callitem);
				}
			}
		}
		
		return callitems_sorted;
	}
	
	public Callitem[] getCallitems2()
	{
		Callitem[] callitems = new Callitem[this.callitem.size()];
		for (int i=0; i<this.callitem.size(); i++)
		{
			callitems[i] = this.callitem.get(i);
		}
		return callitems;
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

	public int getExitvalue()
	{
		return this.exitvalue;
	}
	
	public ArrayList<String> getListItems(String listname)
	{
		return this.parent.getListItems(listname);
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setCommand(String command)
	{
		this.command = command;
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

	public void setExitvalue(int exitvalue)
	{
		this.exitvalue = exitvalue;
	}

	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}

	/**
	 * @param ArrayList<Callitem> the callitem to set
	 */
	public void setCallitem(ArrayList<Callitem> callitem)
	{
		this.callitem = callitem;
	}

}
