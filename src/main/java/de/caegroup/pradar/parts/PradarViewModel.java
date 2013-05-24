package de.caegroup.pradar.parts;

import de.caegroup.pradar.Entity;

public class PradarViewModel extends ModelObject
{
	public String process = "";
	public String user = System.getProperty("user.name");
	public String host = "";
	public String active = "";
	public int period = 24;
	public boolean children = false;
	public boolean perspectiveRadar = true;
	public boolean perspectiveTree = false;
	public int zoom = 100;
	public int refresh = 600;
	
	public Entity entitySelected = null;
	
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

	public boolean getPerspectiveRadar()
	{
		return perspectiveRadar;
	}

	public boolean getPerspectiveTree()
	{
		return perspectiveTree;
	}

	public int getZoom()
	{
		return zoom;
	}

	public int getRefresh()
	{
		return refresh;
	}

	public Entity getEntitySelected()
	{
		return entitySelected;
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
		if (period < 1) {period = 1;}
		firePropertyChange("period", this.period, this.period = period);
	}

	public void setChildren(boolean children)
	{
		firePropertyChange("children", this.children, this.children = children);
	}

	public void setPerspectiveRadar(boolean perspectiveRadar)
	{
		firePropertyChange("perspectiveRadar", this.perspectiveRadar, this.perspectiveRadar = perspectiveRadar);
	}

	public void setPerspectiveTree(boolean perspectiveTree)
	{
		firePropertyChange("perspectiveTree", this.perspectiveTree, this.perspectiveTree = perspectiveTree);
	}

	public void setZoom(int zoom)
	{
		firePropertyChange("zoom", this.zoom, this.zoom = zoom);
	}

	public void setRefresh(int refresh)
	{
		firePropertyChange("refresh", this.refresh, this.refresh = refresh);
	}

	public void setEntitySelected(Entity entitySelected)
	{
		firePropertyChange("entitySelected", this.entitySelected, this.entitySelected = entitySelected);
	}
}
