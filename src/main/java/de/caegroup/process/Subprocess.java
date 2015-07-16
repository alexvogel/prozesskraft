package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.SerializationUtils;

public class Subprocess
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String domain = "unknown";
	private String name = "unnamed";
	private String version = "noversion";
	private int maxrun = 100;

	// das process object
	private Process process = null;
	
	private Step step = new Step("root");

	private String status = "";	// waiting/finished/error

	private ArrayList<Log> log = new ArrayList<Log>();

	public Step parent;

	/*----------------------------
	  constructors
	----------------------------*/
	public Subprocess()
	{
		this.parent = new Step();
	}

//	public Subprocess(Step parent)
//	{
//		this.parent = parent;
//	}

//	public Subprocess(String name, String version)
//	{
//		this.name = name;
//		this.version = version;
//	}

//	public Subprocess(String name, String version, Step step)
//	{
//		this.name = name;
//		this.version = version;
//		this.step = step;
//	}

	/*----------------------------
	  methods
	----------------------------*/
	/**
	 * clone
	 * returns a clone of this
	 * @return Variable
	 */
	@Override
	public Subprocess clone()
	{
		return SerializationUtils.clone(this);
	}
	
	/**
	 * reset this
	 */
	public void reset()
	{
		this.getLog().clear();
		this.setStatus("");
	}
	

	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("subprocess-"+this.getName()+"-"+this.getVersion()+"["+this.toString()+"]", loglevel, logmessage));
	}

	/**
	 * does the work!
	 * 1) executes the call 
	 * 2) if the call already has been executed, then check whether it is still running
	 * 3) if it is not running anymore, the 
	 * 
	 * @param String loglevel, String logmessage
	 * @throws IOException 
	 */
	public void doIt(String processSyscall, String aufrufProcessManager, String domainInstallationDirectory) throws IOException
	{

//		this.resolve();

		// wenn schritt schon gestartet wurde
		if (this.isPidfileexistent())
		{
			String pid = this.getPid();	// aus der absdir des steps soll aus der Datei '.pid' die pid ermittelt werden. wenn noch keine existiert, wurde der schritt noch nicht gestartet
			log("info", "subprocess already executed pid="+pid);
//			System.out.println("PROCESS-STEP BEREITS GESTARTET: "+pid);

			if (isPidalive(pid))
			{
				log("info", "subprocess still running. pid="+pid);
//				System.out.println("PROCESS-STEP LAEUFT NOCH: "+pid);
				this.setStatus("working");
			}
			else
			{
				log("info", "subprocess already finished. pid="+pid);
				log("info", "setting status to 'finished'");
				this.setStatus("finished");
			}
		}

		// wenn schritt noch nicht gestartet wurde
		else 
		{
			// den aufruf erstellen
			log("info", "subprocess not created yet");

			// das stepverzeichnis
			java.io.File stepDir = new java.io.File(this.getParent().getAbsdir());
			
			// step-directory (=neuerProzess-Directory) anlegen, falls es noch nicht existiert
			if(!(stepDir.exists()))
			{
				log("info", "creating step directory "+stepDir.getCanonicalPath());
				if(!this.getParent().mkdir(stepDir.getCanonicalPath()))
				{
					log("error", "could not create directory: "+stepDir.getCanonicalPath());
					this.setStatus("error");
				}
			}

			// aus der hinterlegten Definition einen Prozess erzeugen
			Process newProcess = this.genProcess(domainInstallationDirectory);

			// die commits durchfuehren. erst jetzt werden die files in das stepeigene verzeichnis kopiert und die
			// entsprechenden pfadangaben im file-objekt angepasst
			newProcess.getRootStep().commit();
			
			// und ins step-verzeichnis das binaere file schreiben
			String processInstance = stepDir.getCanonicalPath() + "/process.pmb";
			newProcess.setOutfilebinary(processInstance);
			newProcess.setInfilebinary(processInstance);

			newProcess.writeBinary();
			
			// und das process object in subprocess ablegen
			this.setProcess(newProcess);

			// das logfile des Syscalls (zum debuggen des programms "process syscall" gedacht)
			String AbsLogSyscallWrapper = new java.io.File(new java.io.File(this.getParent().getAbspid()).getParent()).getAbsolutePath()+"/.log";

			try
			{
				// den Aufrufstring fuer die externe App (process syscall --version 0.6.0)) splitten
				// beim aufruf muss das erste argument im path zu finden sein, sonst gibt die fehlermeldung 'no such file or directory'
				ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(processSyscall.split(" ")));

				// die sonstigen argumente hinzufuegen
				processSyscallWithArgs.add("-call");
				processSyscallWithArgs.add(aufrufProcessManager + " -instance " + processInstance);
//				processSyscallWithArgs.add("\""+processSyscall+"\"");
				processSyscallWithArgs.add("-stdout");
				processSyscallWithArgs.add(this.getParent().getAbsstdout());
				processSyscallWithArgs.add("-stderr");
				processSyscallWithArgs.add(this.getParent().getAbsstderr());
				processSyscallWithArgs.add("-pid");
				processSyscallWithArgs.add(this.getParent().getAbspid());
				processSyscallWithArgs.add("-mylog");
				processSyscallWithArgs.add(AbsLogSyscallWrapper);
				processSyscallWithArgs.add("-maxrun");
				processSyscallWithArgs.add(""+this.maxrun);

				// erstellen prozessbuilder
				ProcessBuilder pb = new ProcessBuilder(processSyscallWithArgs);

				// erweitern des PATHs um den prozesseigenen path
//				Map<String,String> env = pb.environment();
//				String path = env.get("PATH");
//				log("debug", "$PATH="+path);
//				path = this.parent.getAbsPath()+":"+path;
//				env.put("PATH", path);
//				log("info", "path: "+path);
				
				// setzen der aktuellen directory
				java.io.File directory = new java.io.File(this.getParent().getAbsdir());
				log("info", "setting execution directory to: "+directory.getAbsolutePath());
				pb.directory(directory);

				// zum debuggen ein paar ausgaben
//				java.lang.Process p1 = Runtime.getRuntime().exec("date >> ~/tmp.debug.work.txt");
//				p1.waitFor();
//				java.lang.Process p2 = Runtime.getRuntime().exec("ls -la "+this.getParent().getAbsdir()+" >> ~/tmp.debug.work.txt");
//				p2.waitFor();
//				java.lang.Process pro = Runtime.getRuntime().exec("nautilus");
//				java.lang.Process superpro = Runtime.getRuntime().exec(processSyscallWithArgs.toArray(new String[processSyscallWithArgs.size()]));
//				p3.waitFor();
				
				log ("info", "calling: " + pb.command());

				// starten des prozesses
				java.lang.Process sysproc = pb.start();


				
//				alternativer aufruf
//				java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(args_for_syscall, " "));
				
//				log("info", "call executed. pid="+sysproc.hashCode());

				// wait 2 seconds for becoming the pid-file visible
				Thread.sleep(2000);
			}
			catch (Exception e2)
			{
				log("error", e2.getMessage());
				this.setStatus("error");
			}
			this.setStatus("working");
		}
	}

	public boolean isPidfileexistent()
	{
		java.io.File pidfile = new java.io.File(this.getParent().getAbspid());
		if (pidfile.canRead())
		{
			log("debug", "pidfile found: "+pidfile.getAbsolutePath());
			return true;
		}
		else
		{
			log("debug", "pidfile NOT found: "+pidfile.getAbsolutePath());
			return false;
		}
	}
	
	public static boolean isPidalive(String pid)
	{
		try
		{
			java.lang.Process ps = Runtime.getRuntime().exec("ps "+pid);
			InputStream is_stdout = ps.getInputStream();
			InputStreamReader isr_stdout = new InputStreamReader(is_stdout);
			BufferedReader br_stdout = new BufferedReader(isr_stdout);
//			String line1 = br_stdout.readLine();	// die ueberschriftenzeile lesen
			br_stdout.readLine();	// die ueberschriftenzeile lesen
			String line2 = br_stdout.readLine();	// die zeile mit den processinfos lesen, falls vorhanden
			if (line2 != null)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
//			e.printStackTrace();
			return false;
		}
	}

	public String getPid()
	{
		String abspidpath = this.getParent().getAbspid();
		String pid = new String();
		try
		{
			log("debug", "pid ermitteln aus file: "+abspidpath);
			FileReader fr = new FileReader(abspidpath);
			BufferedReader br = new BufferedReader(fr);
			pid = br.readLine();
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "no";
		}
		return pid;
	}

	public Process genProcess(String domainInstallationDirectory)
	{
		// check ob es das domain verzeichnis ueberhaupt gibt
		java.io.File domainDir = new java.io.File(domainInstallationDirectory + "/" + this.getDomain());
		if(domainDir.exists() && domainDir.isDirectory())
		{
			log("debug", "domain-installation-directory exists: "+domainDir);
		}
		else
		{
			log("error", "domain-installation-directory does NOT exist: "+domainDir);
			this.setStatus("error");
			return null;
		}
		
		// check ob es das process verzeichnis ueberhaupt gibt
		java.io.File processDir = new java.io.File(domainInstallationDirectory + "/" + this.getDomain() + "/" + this.getName());
		if(processDir.exists() && processDir.isDirectory())
		{
			log("debug", "process-installation-directory exists: "+processDir);
		}
		else
		{
			log("error", "process-installation-directory does not exist: "+processDir);
			this.setStatus("error");
			return null;
		}
		
		// check ob es das process-versions verzeichnis ueberhaupt gibt
		java.io.File versionDir = new java.io.File(domainInstallationDirectory + "/" + this.getDomain() + "/" + this.getName() + "/" + this.getVersion());
		if(versionDir.exists() && versionDir.isDirectory())
		{
			log("debug", "processversion-installation-directory exists: "+versionDir);
		}
		else
		{
			log("error", "processversion-installation-directory does not exist: "+versionDir);
			this.setStatus("error");
			return null;
		}
		
		// check ob es das process.xml ueberhaupt gibt
		java.io.File processDef = new java.io.File(domainInstallationDirectory + "/" + this.getDomain() + "/" + this.getName() + "/" + this.getVersion() + "/process.xml");
		if(processDef.exists() && !processDef.isDirectory())
		{
			log("debug", "process.xml exists: "+processDef);
		}
		else
		{
			log("error", "process.xml does not exist: "+processDef);
			this.setStatus("error");
			return null;
		}
		
		log("info", "creating process");
		// einen neuen Process erstellen und den rootStep aus subprocess ruebernehmen
		de.caegroup.process.Process newProcess = new Process();
		try
		{
			newProcess.setInfilexml(processDef.getCanonicalPath());
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log("error", e1.getMessage());
		}
		de.caegroup.process.Process newProcess2 = null;
		try
		{
			newProcess2 = newProcess.readXml();
		}
		catch (JAXBException e)
		{
			e.printStackTrace();
			log("fatal", e.getMessage());
		}

		// dem root-Step des neuenProcesses, die listen aus parent-Step des Parent-Prozesses uebergeben (damit resolving mit platzhaltern funktioniert)
		newProcess2.getRootStep().setList(this.getParent().getList());
		
		// die loopvar des parentsteps auf den rootStep des neuenProzesses ueberschreiben, damit das resolving funktioniert
		newProcess2.getRootStep().setLoopvar(this.getParent().getLoopvar());
		
		// alle commits aus subprocess in die des neuenProzesses ueberschreiben inkl. aller noch nicht resolvter eintraege
		newProcess2.getRootStep().setCommit(this.getStep().getCommit());
		newProcess2.getRootStep().affiliate();
		
		// das Basedirectory des neuen prozesses soll das stepdir des parentsteps sein
		newProcess2.setBaseDir(this.getParent().getAbsdir());
		
		// der neue Process ist ein Subprocess (das hat auswirkungen auf die pfade fuer die daten
		newProcess2.setSubprocess(true);
		
		// dem root-Step des neuenProzesses alle commits ausfuehren
//		newProcess2.getRootStep().commit();

		// alle listen wieder loeschen
//		newProcess2.getRootStep().getList().clear();

		return newProcess2;
	}
	
	public void refreshProcess()
	{
		// wenn es einen Process gibt, dann den Status von this entsprechend des status des Processes updaten
		if(this.getProcess() != null)
		{
			this.log("debug", "refreshing Process from file: " + this.getProcess().getInfilebinary());

			// einen neuen Process erstellen und aus file einlesen
			Process updatedProcess = this.getProcess().readBinary();
			
			// status setzen
			// ist Process == finished => status=worked
			// ist Process == error => status=error
			if(updatedProcess.getStatus().equals("finished"))
			{
				this.setStatus("finished");
			}
			else if(updatedProcess.getStatus().equals("error"))
			{
				this.setStatus("error");
			}
			else if(updatedProcess.getStatus().equals("rolling"))
			{
				this.setStatus("working");
			}
			
			this.log("debug", "setting step status to " + this.getStatus() + ", because subprocess status is " + updatedProcess.getStatus());
			
			// die binaerfiles setzen
			updatedProcess.setInfilebinary(this.getProcess().getInfilebinary());
			updatedProcess.setOutfilebinary(this.getProcess().getOutfilebinary());
			
			this.setProcess(updatedProcess);
		}
	}

	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getVersion()
	{
		return this.version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public Step getStep()
	{
		return this.step;
	}

	public void setStep(Step step)
	{
		this.step = step;
	}

	public Step getRoot()
	{
		return this.step;
	}

	public void setRoot(Step step)
	{
		this.step = step;
	}

	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		
		// den eingebetteten process nach fehler abfragen und evtl. den status updaten
		this.refreshProcess();
		
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the parent
	 */
	public Step getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Step parent) {
		this.parent = parent;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the process
	 */
	public Process getProcess() {
		return process;
	}

	/**
	 * @param process the process to set
	 */
	public void setProcess(Process process) {
		this.process = process;
	}
	
}
