package de.caegroup.pradar.parts;

public class PradarViewModel extends ModelObject
{
	public String process = "";
	public String user = System.getProperty("user.name");
	public String host = "";
	public String active = "";
	public int period = 168;
	public boolean children = false;
	public int zoom = 100;
	
	public PradarViewModel()
	{
	}
	
	public String getProcess()
	{
		return process;
	}

	public String getUser()
	{
		return user;
	}

	public String getHost()
	{
		return host;
	}

	public String getActive()
	{
		return active;
	}

	public int getPeriod()
	{
		return period;
	}

	public boolean getChildren()
	{
		return children;
	}

	public int getZoom()
	{
		return zoom;
	}

	public void setProcess(String process)
	{
		firePropertyChange("process", this.process, this.process = process);
	}

	public void setUser(String user)
	{
		firePropertyChange("user", this.user, this.user = user);
	}

	public void setHost(String host)
	{
		firePropertyChange("host", this.host, this.host = host);
	}

	public void setActive(String active)
	{
		firePropertyChange("active", this.active, this.active = active);
	}

	public void setPeriod(int period)
	{
		firePropertyChange("period", this.period, this.period = period);
	}

	public void setChildren(boolean children)
	{
		firePropertyChange("children", this.children, this.children = children);
	}

	public void setZoom(int zoom)
	{
		firePropertyChange("zoom", this.zoom, this.zoom = zoom);
	}
}
