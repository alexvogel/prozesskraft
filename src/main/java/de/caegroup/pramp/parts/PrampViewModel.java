package de.caegroup.pramp.parts;

import java.util.ArrayList;

public class PrampViewModel extends ModelObject
{
	public ArrayList<String> processes = new ArrayList<String>();
	public ArrayList<String> versions = new ArrayList<String>();
	public String process = "";
	public String version = "";
	
	public PrampViewModel()
	{
	}
	
	public ArrayList<String> getProcesses()
	{
		return processes;
	}

	public ArrayList<String> getVersions()
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

	public void setProcesses(ArrayList<String> processes)
	{
		firePropertyChange("processes", this.processes, this.processes = processes);
	}

	public void setVersions(ArrayList<String> versions)
	{
		firePropertyChange("versions", this.versions, this.versions = versions);
	}

	public void setProcess(String process)
	{
		firePropertyChange("process", this.process, this.process = process);
	}

	public void setVersion(String version)
	{
		firePropertyChange("version", this.version, this.version = version);
	}
}
