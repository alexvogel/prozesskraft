package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;

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
	
	private Step parent = null;

	/*----------------------------
	  constructors
	----------------------------*/
	public List()
	{
		this.parent = new Step();
	}

	public List(String listname)
	{
		this.parent = new Step();
		this.name = listname;
	}

	public List(Step step, String listname)
	{
		this.parent = step;
		this.name = listname;
	}
	/*----------------------------
	  methods
	----------------------------*/


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
	 * @param item the item to add
	 */
	public void addItem(String item)
	{
		this.item.add(item);
	}

	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}
	

}
