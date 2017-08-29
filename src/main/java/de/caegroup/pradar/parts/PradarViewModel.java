package de.caegroup.pradar.parts;

import de.caegroup.pradar.Entity;

public class PradarViewModel extends ModelObject
{
	public String[] processes = null;
	public String process = "";
	public String[] users = null;
	public String user = "";
	public String[] hosts = null;
	public String host = "";
	public String[] exitcodes = null;
	public String exitcode = "";
	public int period = 24;
	public boolean children = false;
	public boolean perspectiveRadar = true;
	public boolean perspectiveTree = false;
	public int zoom = 60;
	public int refresh = 600;
	
	public Entity entitySelected = null;
	
	public PradarViewModel()
	{
	}
	
	public String[] getProcesses()
	{
		return processes;
	}

	public String getProcess()
	{
		return process;
	}

	public String[] getUsers()
	{
		return users;
	}

	public String getUser()
	{
		return user;
	}

	public String[] getHosts()
	{
		return hosts;
	}

	public String getHost()
	{
		return host;
	}

	public String[] getExitcodes()
	{
		return exitcodes;
	}

	public String getExitcode()
	{
		return exitcode;
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

	public void setProcesses(String[] processes)
	{
		firePropertyChange("processes", this.processes, this.processes = processes);
	}

	public void setProcess(String process)
	{
		firePropertyChange("process", this.process, this.process = process);
	}

	public void setUsers(String[] users)
	{
		firePropertyChange("users", this.users, this.users = users);
	}

	public void setUser(String user)
	{
		firePropertyChange("user", this.user, this.user = user);
	}

	public void setHosts(String[] hosts)
	{
		firePropertyChange("hosts", this.hosts, this.hosts = hosts);
	}

	public void setHost(String host)
	{
		firePropertyChange("host", this.host, this.host = host);
	}

	public void setExitcodes(String[] exitcodes)
	{
		firePropertyChange("exitcodes", this.exitcodes, this.exitcodes = exitcodes);
	}

	public void setExitcode(String exitcode)
	{
		firePropertyChange("exitcode", this.exitcode, this.exitcode = exitcode);
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
