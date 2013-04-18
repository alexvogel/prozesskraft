package de.caegroup.pramp.parts;

import java.util.ArrayList;
import java.util.Collections;

public class PrampViewModel extends ModelObject
{
	public String[] processes = null;
	public String[] versions = null;
	public String process = null;
	public String version = null;
	
	public PrampViewModel()
	{
	}
	
//	public ArrayList<String> getProcesses()
//	{
//		return processes;
//	}
//
	public String[] getProcesses()
	{
		return processes;
	}

//	public ArrayList<String> getVersions()
//	{
//		return versions;
//	}
//
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

	public void setProcesses(String[] processes)
	{
		firePropertyChange("processes", this.processes, this.processes = processes);
	}

	public void setVersions(String[] versions)
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
