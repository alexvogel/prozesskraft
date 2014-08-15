package de.caegroup.gui.step.edit;

import de.caegroup.process.*;
import de.caegroup.process.Process;

public class EditFileModel extends ModelObject
{
	public String key = null;
	public String path = null;

	/**
	 * constructors
	 */
	public EditFileModel()
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

	public String getPath()
	{
		return this.path;
	}

	public void setPath(String path)
	{
		firePropertyChange("path", this.path, this.path = path);
	}

}
