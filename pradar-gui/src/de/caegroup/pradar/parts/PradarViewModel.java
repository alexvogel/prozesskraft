package de.caegroup.pradar.parts;

public class PradarViewModel extends ModelObject
{
	public String process = "";
	public String user = System.getProperty("user.name");
	public String host = "";
	public String active = "true";
	
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
}
