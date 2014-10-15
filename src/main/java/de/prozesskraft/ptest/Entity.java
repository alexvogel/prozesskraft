package de.prozesskraft.ptest;

import java.util.Random;

public class Entity {

	private Integer id = null;
	private String type = null;
	private int minoccur = 0;
	private int maxoccur = 99;
	private String path = null;
	private Size size = null;
	private Integer parent = null;

	public Entity()
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
		
		entityString += "id="+this.getId()+", type: "+this.getType()+", minoccur: "+this.getMinoccur()+", maxoccur: "+this.getMaxoccur()+", path: "+this.getPath()+", parent: "+this.getParent();
		if(this.size != null){entityString += ", "+this.size.toString();}
		
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
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
	public Size getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Size size) {
		this.size = size;
	}

	/**
	 * @return the parent
	 */
	public Integer getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Integer parent) {
		this.parent = parent;
	}
	
}
