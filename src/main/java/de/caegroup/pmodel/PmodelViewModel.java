package de.caegroup.pmodel;

public class PmodelViewModel extends ModelObject
{
	public int zoom = 100;
	
	public PmodelViewModel()
	{
	}
	
	public int getZoom()
	{
		return zoom;
	}

	public void setZoom(int zoom)
	{
		firePropertyChange("zoom", this.zoom, this.zoom = zoom);
	}
}
