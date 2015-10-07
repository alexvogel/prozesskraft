package de.prozesskraft.pramp.parts;

public class PrampViewModel extends ModelObject
{
	public String[] domains = null;
	public String[] processes = null;
	public String[] versions = null;
	public String domain = null;
	public String process = null;
	public String version = null;
	public String baseDirectory = null;
	
	public PrampViewModel()
	{
	}
	
	/**
	 * setzen anonymer felder
	 * @param key
	 * @param value
	 */
	public void setField(String key, String value)
	{
		if(key.equals("baseDirectory"))
		{
			this.baseDirectory = value;
		}
	}
	
	public String[] getDomains()
	{
		return domains;
	}

	public void setDomains(String[] domains)
	{
		firePropertyChange("domains", this.domains, this.domains = domains);
	}

	public String[] getProcesses()
	{
		return processes;
	}

	public void setProcesses(String[] processes)
	{
		firePropertyChange("processes", this.processes, this.processes = processes);
	}

	public String[] getVersions()
	{
		return versions;
	}

	public void setVersions(String[] versions)
	{
		firePropertyChange("versions", this.versions, this.versions = versions);
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		firePropertyChange("domain", this.domain, this.domain = domain);
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

	public String getBaseDirectory()
	{
		return baseDirectory;
	}

	public void setBaseDirectory(String baseDirectory)
	{		
		String korrBaseDirectory = null;
		// pfadkorrektur fuer BMW
		if(baseDirectory.matches("^/net/[^/]+/[^/]/proj/.+$"))
		{
			korrBaseDirectory = baseDirectory.replaceFirst("^/net/[^/]+/[^/]/proj/", "/proj/");
		}
		else
		{
			korrBaseDirectory = baseDirectory;
		}

		firePropertyChange("baseDirectory", this.baseDirectory, this.baseDirectory = korrBaseDirectory);
	}

}
