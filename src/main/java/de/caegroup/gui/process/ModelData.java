package de.caegroup.gui.process;

//import java.util.ArrayList;
//import java.util.Collections;

public class ModelData extends ModelObject
{
	public String content = null;
	
	public ModelData()
	{
	}
	
	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		firePropertyChange("content", this.content, this.content = content);
	}
}
