package de.caegroup.process;

import java.io.*;
import java.util.*;
//import java.util.HashMap;
//import java.util.Map;
import java.util.regex.*;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.solr.common.util.NamedList;

public class Callitem
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private Integer sequence = 0;
	private String par = "";
	private String del = "";
	private String val = "";
	private String loop = null;
	private String loopvar = null;
// loopvar = {$loopvarcallitem}
	
	private String status = "";	// waiting/initializing/working/committing/ finished/broken/cancelled

	private Work parent = null;
	private ArrayList<Log> log = new ArrayList<Log>();

	/*----------------------------
	  constructors
	----------------------------*/
	public Callitem()
	{
		this.parent = new Work();
	}

	public Callitem(Work work)
	{
		this.parent = work;
	}

	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * clone
	 * returns a clone of this
	 * @return Callitem
	 */
	@Override
	public Callitem clone()
	{
		return SerializationUtils.clone(this);
	}

	/**
	 * reset this
	 */
	public void reset()
	{
		this.log.clear();
		this.setStatus("");
	}
	
	/**
	 * resolve
	 * returns an ArrayList of Callitems with
	 * 1) looped by loop and resolved placeholder 'loopvarcallitem'
	 * 2) resolved par, del, val for all other placeholder
	 * @return ArrayList<Callitem>
	 */
	public ArrayList<Callitem> resolve()
	{
		ArrayList<Callitem> loopedThisToCallitems = new ArrayList<Callitem>();
		
		// wenn kein loop vorhanden, direkt die felder resolven
		if(this.getLoop() == null)
		{
			this.setPar(this.getParent().getParent().resolveString(this.getPar()));
			this.setDel(this.getParent().getParent().resolveString(this.getDel()));
			this.setVal(this.getParent().getParent().resolveString(this.getVal()));
			loopedThisToCallitems.add(this);
		}

		else
		{
			List loopList = this.parent.parent.getList(this.getLoop());
			
			if(loopList == null)
			{
				log("error", "list " + this.getLoop() + "does not exist");
			}
			
			else
			{
				
				log("debug", "loopliste '"+loopList.getName()+"' hat "+loopList.getItem().size()+" items.");
				for(String actItem : loopList.getItem())
				{
					// loopen
					Callitem clonedCallitem = this.clone();
					clonedCallitem.setLoop(null);
					clonedCallitem.setLoopvar(actItem);
					log("debug", "par="+this.getPar()+" | del="+this.getDel()+" | val="+this.getVal() + " | actItem="+actItem);
					// loopvarcallitems ersetzen
					clonedCallitem.setPar(this.getPar().replaceAll("\\{\\$loopvarcallitem\\}", clonedCallitem.getLoopvar()));
					clonedCallitem.setDel(this.getDel().replaceAll("\\{\\$loopvarcallitem\\}", clonedCallitem.getLoopvar()));
					clonedCallitem.setVal(this.getVal().replaceAll("\\{\\$loopvarcallitem\\}", clonedCallitem.getLoopvar()));
					// loopvarstep ersetzen
					clonedCallitem.setPar(this.getPar().replaceAll("\\{\\$loopvarstep\\}", clonedCallitem.getParent().getParent().getLoopvar()));
					clonedCallitem.setDel(this.getDel().replaceAll("\\{\\$loopvarstep\\}", clonedCallitem.getParent().getParent().getLoopvar()));
					clonedCallitem.setVal(this.getVal().replaceAll("\\{\\$loopvarstep\\}", clonedCallitem.getParent().getParent().getLoopvar()));

					this.log("debug", "val="+this.getVal());

					// placeholder, die auf steplisten referenzieren ersetzen
					clonedCallitem.setPar(this.getParent().getParent().resolveString(clonedCallitem.getPar()));
					clonedCallitem.setDel(this.getParent().getParent().resolveString(clonedCallitem.getDel()));
					clonedCallitem.setVal(this.getParent().getParent().resolveString(clonedCallitem.getVal()));
					
					loopedThisToCallitems.add(clonedCallitem);
				}
			}
		}
		return loopedThisToCallitems;
	}

	/**
	 * aufloesen eines strings und aller darin verschachtelter verweise auf listitems
	 * @param stringToResolve
	 * @return
	 */
	public String resolveString(String stringToResolve)
	{
		return this.getParent().getParent().resolveString(stringToResolve);
	}
	
//	/**
//	 * resolve
//	 * returns the string with resolved placeholders (only the ones for lists (not for loopvarcallitem))
//	 * @return String
//	 */
//	public String resolve(String stringToResolve, String loopvarcallitem)
//	{
//		String resolvedString = null;
//
//		String patt = "(\\{\\$(.+)\\})";
//		Pattern r = Pattern.compile(patt);
//		Matcher m = r.matcher(stringToResolve);
//
//		while(m.find())
//		{
//			String listname = m.group(1);
////			System.out.println("placeholder: "+listname);
//			// die liste ermitteln, die den Namen traegt wie das gematchte substring
//			List list = this.parent.parent.getList(listname);
//
//			if (list == null)
//			{
//				log("error", "list '"+listname+"' not found in step '"+this.parent.parent.getName()+"' but needed for resolving.");
//				System.out.println("list '"+listname+"' not found in step '"+this.parent.parent.getName()+"' but needed for resolving.");
//			}
//			
//			// das muster soll durch den ersten eintrag in der list ersetzt werden
//			if(list.itemCount() > 0)
//			{
//				resolvedString = m.replaceAll(list.getItem().get(0));
//				log("info", "resolved '{$"+listname+"}' to '"+resolvedString+"'");
//			}
//			else
//			{
//				resolvedString = m.replaceAll("");
//				log("error", "resolved '{$"+listname+"}' to '', because list "+list.getName()+" is empty. (perhaps init.minoccur == 0 and you use it in a callitem without loop="+list.getName()+")");
//			}
//		}
//		else
//		{
//			resolvedString = stringToResolve;
//		}
//		return resolvedString;
//	}
//	
	/*----------------------------
	  methods misc
	----------------------------*/
	/**
	 * stores a message for the process
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("callitem "+this.getPar(), loglevel, logmessage));
	}
	

	/*----------------------------
	  methods get
	----------------------------*/
	public Integer getSequence()
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

	public String getLoop()
	{
		return this.loop;
	}
	
	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setSequence(Integer sequence)
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

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setParent(Work work)
	{
		this.parent = work;
	}

	public Work getParent()
	{
		return this.parent;
	}

	/**
	 * @return the loopvar
	 */
	public String getLoopvar() {
		return loopvar;
	}

	/**
	 * @param loopvar the loopvar to set
	 */
	public void setLoopvar(String loopvar) {
		this.loopvar = loopvar;
	}


}
