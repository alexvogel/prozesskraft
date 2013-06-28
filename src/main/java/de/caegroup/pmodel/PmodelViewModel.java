package de.caegroup.pmodel;

public class PmodelViewModel extends ModelObject
{
	public int zoom = 100;
	private String markedStepName = "root";
	
	public PmodelViewModel()
	{
	}
	
	public int getZoom()
	{
		return zoom;
	}

	public String getMarkedStepName()
	{
		return this.markedStepName;
	}

	public void setZoom(int zoom)
	{
		firePropertyChange("zoom", this.zoom, this.zoom = zoom);
	}

	public void setMarkedStepName(String markedStepName)
	{
		firePropertyChange("markedStepName", this.markedStepName, this.markedStepName = markedStepName);
	}
	

}
