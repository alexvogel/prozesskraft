package de.caegroup.pramp.parts;

import java.util.ArrayList;
import java.util.Collections;

public class PrampViewModel extends ModelObject
{
	public String[] processes = null;
	public String[] versions = null;
	public String[] hosts = null;
	public String process = null;
	public String version = null;
	public String host = null;
	public String instancedirectory = null;
	
	public PrampViewModel()
	{
	}
	
	public String[] getProcesses()
	{
		return processes;
	}

	public String[] getHosts()
	{
		return hosts;
	}

	public String[] getVersions()
	{
		return versions;
	}

	public String getProcess()
	{
		return process;
	}

	public String getVersion()
	{
		return version;
	}

	public String getHost()
	{
		return host;
	}

	public String getInstancedirectory()
	{
		return instancedirectory;
	}

	public void setProcesses(String[] processes)
	{
		firePropertyChange("processes", this.processes, this.processes = processes);
	}

	public void setVersions(String[] versions)
	{
		firePropertyChange("versions", this.versions, this.versions = versions);
	}

	public void setHosts(String[] hosts)
	{
		firePropertyChange("hosts", this.hosts, this.hosts = hosts);
	}

	public void setProcess(String process)
	{
		firePropertyChange("process", this.process, this.process = process);
	}

	public void setVersion(String version)
	{
		firePropertyChange("version", this.version, this.version = version);
	}

	public void setHost(String host)
	{
		firePropertyChange("host", this.host, this.host = host);
	}

	public void setInstancedirectory(String instancedirectory)
	{
		firePropertyChange("instancedirectory", this.instancedirectory, this.instancedirectory = instancedirectory);
	}

}
