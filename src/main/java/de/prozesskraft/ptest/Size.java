package de.prozesskraft.ptest;

public class Size {

	private float tolerance = 0f;
	private String unit = "MB";
	private float content = 0f;
	
	public Size()
	{
		
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
	public float getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(float content) {
		this.content = content;
	}
	
}
