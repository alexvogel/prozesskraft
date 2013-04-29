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
	private String name = new String();
	private String description = new String();
	private String command = new String();
	private ArrayList<Callitem> callitems = new ArrayList<Callitem>();
	private String loop = new String();
	private String loopvar = new String();
	
	private String status = new String();	// waiting/initializing/working/committing/ finished/broken/cancelled
	private int exitvalue;
	private String call = new String();
	/*----------------------------
	  constructors
	----------------------------*/
	public Work()
	{
		name = "unnamed";
		description = "no description";
	}


	/*----------------------------
	  methods
	----------------------------*/
	public void addCallitem(Callitem callitem)
	{
		this.callitems.add(callitem);
	}
	

	/*----------------------------
	  methods virtual get
	----------------------------*/
	public String getCall()
	{
		return this.call;
	}
	
	public String generateCall(NamedList<String> lists)
	{
		ArrayList<Callitem> callitems = this.getCallitemssorted();
		
		StringBuffer callbuffer = new StringBuffer(this.command);
	
		Iterator<Callitem> itercallitem = callitems.iterator();
		while (itercallitem.hasNext())
		{
			Callitem callitem = itercallitem.next();
			callbuffer.append(" ");
			callbuffer.append(callitem.getRespar(lists));
			callbuffer.append(callitem.getResdel(lists));
			callbuffer.append(callitem.getResval(lists));
		}
		this.call = callbuffer.toString();
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
		return this.callitems;
	}
	
	public ArrayList<Callitem> getCallitemssorted()
	{
		ArrayList<String> sequences = new ArrayList<String>();
		
		// aus den vorhandenen callitems die sequences in ein eigenes array extrahieren
		Iterator<Callitem> itercallitem = this.callitems.iterator();
		while (itercallitem.hasNext())
		{
			Callitem callitem = itercallitem.next();
			sequences.add(callitem.getSequence());
		}

		// das sequences-array sortieren
		Collections.sort(sequences);
		
		// ueber das sortierte sequences-array iterieren
		ArrayList<Callitem> callitems_sorted = new ArrayList<Callitem>();
		Iterator<String> itersequences = sequences.iterator();
		while (itersequences.hasNext())
		{
			String sequence = itersequences.next();
			// und das zugehoerige callitem rausfischen
			Iterator<Callitem> itercallitem2 = this.callitems.iterator();
			while (itercallitem2.hasNext())
			{
				Callitem callitem = itercallitem2.next();
				if (callitem.getSequence().equals(sequence))
				{
					callitems_sorted.add(callitem);
				}
			}
		}
		
		return callitems_sorted;
	}
	
	public Callitem[] getCallitems2()
	{
		Callitem[] callitems = new Callitem[this.callitems.size()];
		for (int i=0; i<this.callitems.size(); i++)
		{
			callitems[i] = this.callitems.get(i);
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

}
