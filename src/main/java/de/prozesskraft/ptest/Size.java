package de.prozesskraft.ptest;

public class Size {

	private float tolerance = 0f;
	private String unit = "MB";
	private Long content = 0L;
	
	public Size()
	{
		
	}

	public String toString()
	{
		String sizeString = "";

		sizeString += "size: "+this.getContent()+", unit: "+this.getUnit()+", tolerance: "+this.getTolerance();
		
		return sizeString;
	}
	
	/**
	 * @return the tolerance
	 */
	public float getTolerance() {
		return tolerance;
	}

	/**
	 * @param tolerance the tolerance to set
	 */
	public void setTolerance(float tolerance) {
		this.tolerance = tolerance;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @return the content
	 */
	public Long getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(Long content) {
		this.content = content;
	}
	
}
