package de.prozesskraft.pmodel;

import de.prozesskraft.pkraft.Process;

public class PmodelViewModel extends ModelObject
{
	public Process process = new Process();
	public int width = 1200;
	public int height = 800;
	public int size = 100;
	public int zoom = 100;
	public int damp = 15;
	public String zoomstring = "";

	public int textsize = 10;
	public int ranksize = 10;
	public float rootpositionratiox = 0.25f;
	public float rootpositionratioy = 0.1f;
	public int gravx = 0;
	public int gravy = 6;
	private String markedStepName = "root";
	private int refreshInterval = 10;
	private boolean refreshNow = false;
	private int nextRefreshSeconds = refreshInterval;
	private String nextRefreshSecondsText = "refresh ("+nextRefreshSeconds+")";
	private int lastRefreshSeconds = 0;
	private int lastRefreshCheckSeconds = 0;
	private String buttonManagerText = "unknown";
//	private Boolean managerActive = null;
	private boolean rootReposition = true;
	private boolean fix = false;
	
	// soll animation schlafen?
	private boolean sleep = false;
	
	public PmodelViewModel()
	{
	}
	
	public Process getProcess()
	{
		return process;
	}

	public void setProcess(Process process)
	{
		firePropertyChange("process", this.process, this.process = process);
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

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		firePropertyChange("size", this.size, this.size = size);
		setZoomstring(""+size);
	}

	public String getSizestring()
	{
		return ""+size;
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
	
	public int getRefreshInterval()
	{
		return this.refreshInterval;
	}

	public void setRefreshInterval(int refreshInterval)
	{
		firePropertyChange("refreshInterval", this.refreshInterval, this.refreshInterval = refreshInterval);
	}
	
	public boolean getRefreshNow()
	{
		return this.refreshNow;
	}

	public void setRefreshNow(boolean refreshNow)
	{
		firePropertyChange("refreshNow", this.refreshNow, this.refreshNow = refreshNow);
	}
	
	public int getNextRefreshSeconds()
	{
		return this.nextRefreshSeconds;
	}

	public void setNextRefreshSeconds(int nextRefreshSeconds)
	{
		firePropertyChange("nextRefreshSeconds", this.nextRefreshSeconds, this.nextRefreshSeconds = nextRefreshSeconds);
		setNextRefreshSecondsText("refresh("+this.nextRefreshSeconds+")");
		if(nextRefreshSeconds == 0)
		{
			setRefreshNow(true);
		}
		else
		{
			setRefreshNow(false);
		}
	}
	
	public String getNextRefreshSecondsText()
	{
		return this.nextRefreshSecondsText;
	}

	public void setNextRefreshSecondsText(String nextRefreshSecondsText)
	{
		firePropertyChange("nextRefreshSecondsText", this.nextRefreshSecondsText, this.nextRefreshSecondsText = nextRefreshSecondsText);
	}
	
	public int getLastRefreshSeconds()
	{
		return this.lastRefreshSeconds;
	}

	public void setLastRefreshSeconds(int lastRefreshSeconds)
	{
		firePropertyChange("lastRefreshSeconds", this.lastRefreshSeconds, this.lastRefreshSeconds = lastRefreshSeconds);
	}
	
	public int getLastRefreshCheckSeconds()
	{
		return this.lastRefreshCheckSeconds;
	}

	public void setLastRefreshCheckSeconds(int lastRefreshCheckSeconds)
	{
		firePropertyChange("lastRefreshCheckSeconds", this.lastRefreshCheckSeconds, this.lastRefreshCheckSeconds = lastRefreshCheckSeconds);
	}
	
	public String getButtonManagerText()
	{
		return this.buttonManagerText;
	}

	public void setButtonManagerText(String buttonManagerText)
	{
		firePropertyChange("buttonManagerText", this.buttonManagerText, this.buttonManagerText = buttonManagerText);
	}

	public boolean getRootReposition()
	{
		return this.rootReposition;
	}

	public void setRootReposition(boolean rootReposition)
	{
		firePropertyChange("rootReposition", this.rootReposition, this.rootReposition = rootReposition);
	}

	public boolean getFix()
	{
		return this.fix;
	}

	public void setFix(boolean fix)
	{
		firePropertyChange("fix", this.fix, this.fix = fix);
	}

	public boolean getSleep()
	{
		return this.sleep;
	}

	public void setSleep(boolean sleep)
	{
		firePropertyChange("sleep", this.sleep, this.sleep = sleep);
	}
}
