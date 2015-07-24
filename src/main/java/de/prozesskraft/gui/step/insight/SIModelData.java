package de.prozesskraft.gui.step.insight;

import java.util.ArrayList;
import java.util.Collections;

public class SIModelData extends SIModelObject
{
	public String content = null;
	
	public SIModelData()
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
