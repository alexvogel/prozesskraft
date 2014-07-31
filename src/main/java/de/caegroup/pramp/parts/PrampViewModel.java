package de.caegroup.pramp.parts;

public class PrampViewModel extends ModelObject
{
	public String[] processes = null;
	public String[] versions = null;
	public String[] hosts = null;
	public String process = null;
	public String version = null;
	public String host = null;
	public String baseDirectory = null;
	
	public PrampViewModel()
	{
	}
	
	public String[] getProcesses()
	{
		return processes;
	}

	public void setProcesses(String[] processes)
	{
		firePropertyChange("processes", this.processes, this.processes = processes);
	}

	public String[] getHosts()
	{
		return hosts;
	}

	public void setHosts(String[] hosts)
	{
		firePropertyChange("hosts", this.hosts, this.hosts = hosts);
	}

	public String[] getVersions()
	{
		return versions;
	}

	public void setVersions(String[] versions)
	{
		firePropertyChange("versions", this.versions, this.versions = versions);
	}

	public String getProcess()
	{
		return process;
	}

	public void setProcess(String process)
	{
		firePropertyChange("process", this.process, this.process = process);
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		firePropertyChange("version", this.version, this.version = version);
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		firePropertyChange("host", this.host, this.host = host);
	}

	public String getBaseDirectory()
	{
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory)
	{
		firePropertyChange("baseDirectory", this.baseDirectory, this.baseDirectory = baseDirectory);
	}

}
