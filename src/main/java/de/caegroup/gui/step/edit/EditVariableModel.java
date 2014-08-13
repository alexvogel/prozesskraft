package de.caegroup.gui.step.edit;

import de.caegroup.process.*;
import de.caegroup.process.Process;

public class EditVariableModel extends ModelObject
{
	private String key = null;
	private String value = null;

	/**
	 * constructors
	 */
	public EditVariableModel()
	{
	}
	
	/**
	 * getter / setter
	 */
	public String getKey()
	{
		return this.key;
	}

	public void setKey(String key)
	{
		firePropertyChange("key", this.key, this.key = key);
	}

	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		firePropertyChange("value", this.value, this.value = value);
	}

}
