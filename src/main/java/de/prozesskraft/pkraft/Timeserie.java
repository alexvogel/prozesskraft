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
