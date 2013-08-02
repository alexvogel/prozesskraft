package de.caegroup.pmodel;

public class PmodelViewModel extends ModelObject
{
	public int width = 1200;
	public int height = 800;
	public int zoom = 100;
	public String zoomstring = "";
	public int labelsize = 10;
	public int textsize = 10;
	public int ranksize = 10;
	public float rootpositionratiox = (float)0.5;
	public float rootpositionratioy = (float)0.5;
	public int gravx = 0;
	public int gravy = 10;
	private String markedStepName = "root";
	
	public PmodelViewModel()
	{
	}
	
	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		firePropertyChange("width", this.width, this.width = width);
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		firePropertyChange("height", this.height, this.height = height);
	}

	public int getZoom()
	{
		return zoom;
	}

	public void setZoom(int zoom)
	{
		firePropertyChange("zoom", this.zoom, this.zoom = zoom);
		setZoomstring(""+zoom);
	}

	public String getZoomstring()
	{
		return ""+zoom;
	}

	public void setZoomstring(String zoomstring)
	{
		firePropertyChange("zoomstring", this.zoomstring, this.zoomstring = zoomstring);
	}

	public int getLabelsize()
	{
		return labelsize;
	}

	public void setLabelsize(int labelsize)
	{
		firePropertyChange("labelsize", this.labelsize, this.labelsize = labelsize);
	}

	public int getTextsize()
	{
		return textsize;
	}

	public void setTextsize(int textsize)
	{
		firePropertyChange("textsize", this.textsize, this.textsize = textsize);
	}

	public int getRanksize()
	{
		return ranksize;
	}

	public void setRanksize(int ranksize)
	{
		firePropertyChange("ranksize", this.ranksize, this.ranksize = ranksize);
	}

	public float getRootpositionratiox()
	{
		return rootpositionratiox;
	}

	public void setRootpositionratiox(float rootpositionratiox)
	{
		firePropertyChange("rootpositionratiox", this.rootpositionratiox, this.rootpositionratiox = rootpositionratiox);
	}

	public float getRootpositionratioy()
	{
		return rootpositionratioy;
	}

	public void setRootpositionratioy(float rootpositionratioy)
	{
		firePropertyChange("rootpositionratioy", this.rootpositionratioy, this.rootpositionratioy = rootpositionratioy);
	}

	public float getGravx()
	{
		return gravx;
	}

	public void setGravx(int gravx)
	{
		firePropertyChange("gravx", this.gravx, this.gravx = gravx);
	}

	public float getGravy()
	{
		return gravy;
	}

	public void setGravy(int gravy)
	{
		firePropertyChange("gravy", this.gravy, this.gravy = gravy);
	}

	public String getMarkedStepName()
	{
		return this.markedStepName;
	}

	public void setMarkedStepName(String markedStepName)
	{
		firePropertyChange("markedStepName", this.markedStepName, this.markedStepName = markedStepName);
	}
	
	

}
