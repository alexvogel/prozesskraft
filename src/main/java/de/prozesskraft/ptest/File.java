package de.prozesskraft.ptest;

import java.util.Random;

public class File {

	private Integer id = null;
	private int minoccur = 0;
	private int maxoccur = 99;
	private String path = null;
	private String size = null;
	private Float sizetolerance = null;
	private String extension = null;

	public File()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);
		
		minoccur = 0;
		maxoccur = 99;
	}

	public String toString()
	{
		String entityString = "";
		
		entityString += "id="+this.getId()+", minoccur: "+this.getMinoccur()+", maxoccur: "+this.getMaxoccur()+", path: "+this.getPath()+", extension: "+this.getExtension()+", size: "+this.getSize()+", sizetolerance: "+this.getSizetolerance();
		
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
	public int getMinoccur() {
		return minoccur;
	}

	/**
	 * @param minoccur the minoccur to set
	 */
	public void setMinoccur(int minoccur) {
		this.minoccur = minoccur;
	}

	/**
	 * @return the maxoccur
	 */
	public int getMaxoccur() {
		return maxoccur;
	}

	/**
	 * @param maxoccur the maxoccur to set
	 */
	public void setMaxoccur(int maxoccur) {
		this.maxoccur = maxoccur;
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
	public String getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * @return the sizetolerance
	 */
	public Float getSizetolerance() {
		return sizetolerance;
	}

	/**
	 * @param sizetolerance the sizetolerance to set
	 */
	public void setSizetolerance(Float sizetolerance) {
		this.sizetolerance = sizetolerance;
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

}
