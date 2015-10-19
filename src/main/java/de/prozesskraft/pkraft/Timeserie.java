package de.prozesskraft.pkraft;

import java.io.*;
import java.sql.Timestamp;
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
	
	private Map<Long,String> serie = new HashMap<Long,String>();

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
		for(Long actTime : this.getSerie().keySet())
		{
			System.out.println("["+new Timestamp(actTime)+"],"+this.getSerie().get(actTime));
		}
	}

	public String sprint()
	{
		String out = "# " + this.getLabel();
		for(Long actTime : this.getSerie().keySet())
		{
			out += "["+new Timestamp(actTime)+"],"+this.getSerie().get(actTime);
		}
		return out;
	}

	public void addValue(String value)
	{
		this.getSerie().put(System.currentTimeMillis(), value);
//		System.err.println("Timeserie: " + System.currentTimeMillis() + ", " + value);
	}
	
	public void addPair(Long time, String value)
	{
		this.getSerie().put(time, value);
	}
	
	public void writeFile(String path) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		
		writer.println("# " + this.getLabel() );
		for(Long actTime : this.getSerie().keySet())
		{
			writer.println(new Timestamp(actTime).toString().replace(" ", "T")+","+this.getSerie().get(actTime));
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
	public Map<Long, String> getSerie() {
		return serie;
	}

	/**
	 * @param serie the serie to set
	 */
	public void setSerie(Map<Long, String> serie) {
		this.serie = serie;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
