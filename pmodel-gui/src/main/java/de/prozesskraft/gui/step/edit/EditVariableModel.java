package de.prozesskraft.gui.step.edit;

import de.prozesskraft.pkraft.*;

public class EditVariableModel extends ModelObject
{
	public String key = null;
	public String value = null;

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
