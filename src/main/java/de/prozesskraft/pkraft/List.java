package de.prozesskraft.pkraft;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class List
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
	private int min = 0;
	private int max = 99999;
	private ArrayList<String> item = new ArrayList<String>();
	private ArrayList<String> defaultitem = new ArrayList<String>();

	// don't clone this, when cloning this List
	private Step parent = null;

	/*----------------------------
	  constructors
	----------------------------*/
	public List()
	{
		Step dummyStep = new Step();
		dummyStep.setName("dummy");
		this.parent = dummyStep;
	}

	public List(Step step)
	{
		step.addList(this);
		this.parent = step;
	}
	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * clone the list
	 */
	public List clone()
	{
		List newList = new List();
		newList.setName(this.getName());
		newList.setMin(this.getMin());
		newList.setMax(this.getMax());
		for(String actItem : this.getItem())
		{
			newList.addItem(actItem);
		}
		for(String actDefaultitem : this.getDefaultitem())
		{
			newList.addDefaultitem(actDefaultitem);
		}
		
		return newList;
	}

	/**
	 * clears the list
	 */
	public void clear()
	{
		this.item.clear();
	}

	/**
	 * remove doubles
	 */
	public void removeDoubles()
	{
		Map<String,String> itemMap = new HashMap<String,String>();
		for(String actItem : this.getItem())
		{
			itemMap.put(actItem, "dummy");
		}
		
		ArrayList<String> uniqueItems = new ArrayList<String>();
		uniqueItems = new ArrayList<String>(itemMap.keySet());
		this.setItem(uniqueItems);
	}

	/*----------------------------
	  methods getter & setter
	----------------------------*/

	/**
	 * @return the name
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the min
	 */
	public int getMin()
	{
		return this.min;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(int min)
	{
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public int getMax()
	{
		return this.max;
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(int max)
	{
		this.max = max;
	}

	/**
	 * @return the item
	 */
	public ArrayList<String> getItem()
	{
		return this.item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(ArrayList<String> item)
	{
		this.item = item;
	}
	
	/**
	 * @param add defaultitem
	 */
	public void addDefaultitem(String item)
	{
		this.defaultitem.add(item);
	}

	/**
	 * @param item the item to add
	 */
	public void addItem(String item)
	{
		this.item.add(item);
	}

	/**
	 * @param item the item to add
	 */
	public void addItem(ArrayList<String> item)
	{
		this.item.addAll(item);
	}

	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}
	

	/**
	 * @param itemcount
	 */
	public int itemCount()
	{
		return this.item.size();
	}

	/**
	 * @return the defaultitem
	 */
	public ArrayList<String> getDefaultitem() {
		return defaultitem;
	}

	/**
	 * @param defaultitem the defaultitem to set
	 */
	public void setDefaultitem(ArrayList<String> defaultitem) {
		this.defaultitem = defaultitem;
	}
	
	/**
	 * @param
	 * @return size of itemlist
	 */
	public int size()
	{
		return this.item.size();
	}
	
}
