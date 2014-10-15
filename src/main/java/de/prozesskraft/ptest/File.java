package de.prozesskraft.ptest;

import java.util.Random;

public class File {

	private Integer id = null;
	private int minOccur = 0;
	private int maxOccur = 0;
	private String path = null;
	private Float size = null;
	private String sizeUnit = "B";
	private Float sizeTolerance = 0F;
	private String extension = null;

	public File()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);
		
		minOccur = 0;
		maxOccur = 99999;
	}

	public String toString()
	{
		String entityString = "";
		
		entityString += "id="+this.getId()+", minOccur: "+this.getMinOccur()+", maxOccur: "+this.getMaxOccur()+", path: "+this.getPath()+", extension: "+this.getExtension()+", size: "+this.getSize()+", sizetolerance: "+this.getSizeTolerance();
		
		return entityString;
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the minoccur
	 */
	public int getMinOccur() {
		return minOccur;
	}

	/**
	 * @param minOccur the minOccur to set
	 */
	public void setMinOccur(int minOccur) {
		this.minOccur = minOccur;
	}

	/**
	 * @return the maxOccur
	 */
	public int getMaxOccur() {
		return maxOccur;
	}

	/**
	 * @param maxOccur the maxOccur to set
	 */
	public void setMaxOccur(int maxOccur) {
		this.maxOccur = maxOccur;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the size
	 */
	public Float getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Float size) {
		this.size = size;
	}

	/**
	 * @return the sizeTolerance
	 */
	public Float getSizeTolerance() {
		return sizeTolerance;
	}

	/**
	 * @param sizeTolerance the sizeTolerance to set
	 */
	public void setSizeTolerance(Float sizeTolerance) {
		this.sizeTolerance = sizeTolerance;
	}

	/**
	 * @return the extension
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * @return the sizeUnit
	 */
	public String getSizeUnit() {
		return sizeUnit;
	}

	/**
	 * @param sizeUnit the sizeUnit to set
	 */
	public void setSizeUnit(String sizeUnit) {
		this.sizeUnit = sizeUnit;
	}

}
