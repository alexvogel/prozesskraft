package de.prozesskraft.pkraft;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Timeserie
implements Serializable, Comparable

{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;

	private String label = "";
	
//	private ArrayList<Map<Long,String>> serie = new ArrayList<Map<Long,String>>();
	private ArrayList<Map<Long,String>> serie = new ArrayList<Map<Long,String>>();
//	private Map<Long,String> serie = new HashMap<Long,String>();

	/*----------------------------
	  constructors
	----------------------------*/
	public Timeserie()
	{
	}

	public Timeserie(String label)
	{
		this.setLabel(label);
	}

	/*----------------------------
	  methods
	----------------------------*/

	public Timeserie clone()
	{
		Timeserie clone = new Timeserie();
		clone.setLabel(this.getLabel());
		clone.setSerie(this.getSerie());
		
		return clone;
	}

	public void print()
	{
		System.out.println("# " + this.getLabel() );
		for(Map<Long,String> actPair : this.getSerie())
		{
			for(Long actTime : actPair.keySet())
			{
				System.out.println(new Timestamp(actTime)+","+actPair.get(actTime));
			}
		}
	}

	public String sprint()
	{
		String out = "# " + this.getLabel();
		for(Map<Long,String> actPair : this.getSerie())
		{
			for(Long actTime : actPair.keySet())
			{
				out += new Timestamp(actTime)+"],"+actPair.get(actTime);
			}
		}
		return out;
	}

	/**
	 * returns the last key-value-pair
	 * @return
	 */
	
	public Map<Long,String> getLastPair()
	{
		if(serie.size() > 0)
		{
			return serie.get(serie.size()-1);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * returns the first key-value-pair
	 * @return
	 */
	
	public Map<Long,String> getFirstPair()
	{
		if(serie.size() > 0)
		{
			return serie.get(0);
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * returns the first time point of time series
	 * if no next time point found -> null
	 * @param time
	 * @return time
	 */
	public Long getFirstTime()
	{
		if(this.getSerie().size()>0)
		{
			return (Long)this.getSerie().get(0).keySet().toArray()[0];
		}
		return null;
	}
	
	/**
	 * returns the next time point of given time point
	 * if no next time point found
	 * @param time
	 * @return time
	 */
	public Long getNextTime(Long time)
	{
		boolean flag = false;
		for(Map<Long,String> actPair : this.getSerie())
		{
			for(Long actTime : actPair.keySet())
			{
				if(flag)
				{
					return actTime;
				}
				if(time == actTime)
				{
					flag = true;
				}
			}
		}
		return null;
	}
	
	/**
	 * add only a new pair actualTimeInMillis -> value, if the value of the last pair is a different one
	 * @param value
	 */
	public void addValueIfDiffersFromLast(String value)
	{
		if(serie.size() > 0)
		{
			if(!(serie.get(serie.size()-1).containsValue(value)))
			{
				this.addValue(value);
			}
		}
		else
		{
			this.addValue(value);
		}
	}
	
	public void addValue(String value)
	{
		Map<Long,String> newPair = new HashMap<Long,String>();
		newPair.put(System.currentTimeMillis(), value);
		this.getSerie().add(newPair);
	}
	
	public void addPair(Long time, String value)
	{
		Map<Long,String> newPair = new HashMap<Long,String>();
		newPair.put(time, value);
		this.getSerie().add(newPair);
	}
	
	public void writeFile(String path) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		
		writer.println("# " + this.getLabel() );
		for(Map<Long,String> actPair : this.getSerie())
		{
			for(Long actTime : actPair.keySet())
			{
				writer.println(new Timestamp(actTime).toString().replace(" ", "T")+","+actPair.get(actTime));
			}
		}
		
		writer.close();
	}
	
	public ArrayList<String> getValues()
	{
		ArrayList<String> allValues = new ArrayList<String>();
		
		for(Map<Long,String> actPair : serie)
		{
			allValues.addAll(actPair.values());
		}

		return allValues;
	}
	
	/*----------------------------
	  methods getter & setter
	----------------------------*/

	/**
	 * @return the label of parent
	 */
	public String getLabel()
	{
		return this.label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the serie
	 */
	public ArrayList<Map<Long, String>> getSerie() {
		return serie;
	}

	/**
	 * @param serie the serie to set
	 */
	public void setSerie(ArrayList<Map<Long, String>> serie) {
		this.serie = serie;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
