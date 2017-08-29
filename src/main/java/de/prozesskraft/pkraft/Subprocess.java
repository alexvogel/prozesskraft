package de.prozesskraft.pkraft;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	private int maxrun = 10000;

	private String status = "waiting";	// waiting/finished/error

	private boolean subprocessNeverStartet = true;
	
	ArrayList<Log> log = new ArrayList<Log>();

	// der step, der in subprocess eingebettet ist und dessen commits auf den rootStep des neuen Processes gemapped werden sollen
	// dieser step wird automatisch initialisiert durch die daten im xml
	private Step step = null;
	
	// don't clone parent when you clone this
	public Step parent = null;
	transient private Step parentDummy = null;
	/*----------------------------
	  constructors
	----------------------------*/
	public Subprocess()
	{
		Step dummyStep = new Step();
		dummyStep.setName("dummy");
		this.parentDummy = dummyStep;
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
		Subprocess newSubprocess = new Subprocess();
		newSubprocess.setDomain(this.getDomain());
		newSubprocess.setName(this.getName());
		newSubprocess.setVersion(this.getVersion());
		newSubprocess.setMaxrun(this.getMaxrun());
//		newSubprocess.setStatus(this.getStatus());
		for(Log actLog : this.getLog())
		{
			newSubprocess.addLog(actLog.clone());
		}
		if(this.getStep() != null)
		{
			newSubprocess.setStep(this.getStep().clone());
		}

		return newSubprocess;
	}
	
	/**
	 * oldClone
	 * returns a clone of this
	 * @return Variable
	 */
	public Subprocess oldClone()
	{
		return SerializationUtils.clone(this);
	}
	
	/**
	 * deserialize not in a standard way
	 * @param stream
	 * @throws java.io.IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException
	{
		stream.defaultReadObject();

		// erstellen eines parentDummies, falls notwendig
		if(parent == null)
		{
			parentDummy = new Step();
		}
	}
	
	/**
	 * reset this
	 */
	public void reset()
	{
		this.getLog().clear();
//		this.setStatus("");
	}
	
	/**
	 * kill the process inside this
	 */
	public void kill()
	{
		Process p1 = new Process();
		p1.setInfilebinary(this.getParent().getAbsdir() + "/process.pmb");
		Process p2 = p1.readBinary();
		p2.kill();
	}
	
	public void addLog(Log log)
	{
		log.setLabel("subprocess "+this.getName());
		this.log.add(log);
	}

	
	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.addLog(new Log(loglevel, logmessage));
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

		// wenn schritt schon gestartet wurde, muss das Prozessbinary vorhanden sein
		if (this.isProcessBinaryFileExistent())
		{
			// den status des subprocesses feststellen
			
			this.refreshStatus();
		}

		// wenn schritt noch nicht gestartet wurde
		else 
		{
			// soll der Schritt ueberhaupt gestartet werden?
			boolean schrittStarten = true;
			
			// nicht starten, wenn aktuell laufendeSteps >= simultaneousSteps(max=?)
			// nicht starten, wenn der zeitpunkt des letzten Stepstarts kuerzer zurueckliegt als simultaneousSteps(delay=?)
			
			// 1) max. erlaubte laufende stepanzahl bereits erreicht?
			Integer workingSteps =  this.getParent().getParent().getStepWorking().size();
			Integer maxWorkingSteps =  this.getParent().getParent().getMaxSimultaneousSteps();
			log("debug", "amount of working Steps is: " + workingSteps + " maxSimultaneousSteps=" + maxWorkingSteps);
			if(this.getParent().getParent().getMaxSimultaneousSteps() <= workingSteps)
			{
				log("info", "starting of new steps is not allowed at the moment by reference of maxSimultaniousSteps");
				schrittStarten = false;
			}
			else
			{
				log("info", "starting of new steps is allowed at the moment by reference of maxSimultaniousSteps");
			}

			log("debug", "now in milliseconds: " + System.currentTimeMillis());
			Integer minutesSinceLastStepStart = (int)((long)(System.currentTimeMillis() - this.getParent().getParent().getTimeOfLastStepStart()) / 60000);
			log("debug", "time since last step start in minutes: " + minutesSinceLastStepStart + " stepStartDelayMinutes="+this.getParent().getParent().getStepStartDelayMinutes());
			// 2) der zeitpunkt an dem der letzte schritt gestartet wurde ist weniger minuten her als der mindest-Delay fuer Stepstarts vorschreibt
			Integer stepStartDelayMinutes = null;
			
			// feststellen welche stepStartDelayMinutes -Angabe gilt, falls ueberhaupt vorhanden
			if(this.getParent().getStepStartDelayMinutes() != null)
			{
				stepStartDelayMinutes = this.getParent().getStepStartDelayMinutes();
				log("debug", "stepStartDelayMinutes from stepScope " + stepStartDelayMinutes);
			}
			else if(this.getParent().getParent().getStepStartDelayMinutes() != null)
			{
				stepStartDelayMinutes = this.getParent().getParent().getStepStartDelayMinutes();
				log("debug", "stepStartDelayMinutes from processScope " + stepStartDelayMinutes);
			}
			// und damit entscheiden, ob der schritt gestartet werden soll
			if( (stepStartDelayMinutes != null) && (minutesSinceLastStepStart < stepStartDelayMinutes) )
			{
				log("info", "starting of new steps is not allowed at the moment by reference of stepStartDelayMinutes");
				schrittStarten = false;
			}
			else
			{
				log("info", "starting of new steps is allowed at the moment by reference of stepStartDelayMinutes");
			}

			// wenn schritt noch nicht gestartet wurde
			if(schrittStarten) 
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
					}
				}
	
				// aus der hinterlegten Definition einen Prozess erzeugen
				log("info", "committing the rootStep of subprocess...");
				Process newProcess = this.genProcess(domainInstallationDirectory);
				
				// die id des Prozesses als parentid des subprocesses setzen
				log("info", "setting the parentid of subprocess...");
				newProcess.setParentid(this.getParent().getParent().getId());
				
				// den stepnameOfParent setzen
				log("info", "setting the stepnameOfParent of subprocess..." + this.getParent().getName());
				newProcess.setStepnameOfParent(this.getParent().getName());

				// die commits durchfuehren. erst jetzt werden die files in das stepeigene verzeichnis kopiert und die
				// entsprechenden pfadangaben im file-objekt angepasst
				log("info", "committing the rootStep of subprocess...");
				newProcess.getRootStep().commit();
				
				// und ins step-verzeichnis das binaere file schreiben
				String processInstance = stepDir.getCanonicalPath() + "/process.pmb";
				newProcess.setOutfilebinary(processInstance);
				newProcess.setInfilebinary(processInstance);
	
				log("info", "writing the binary-instance-file of subprocess...");
				newProcess.writeBinary();
				
//				// und das process object in subprocess ablegen
//				this.setProcess(newProcess);
	
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
	
					// flag markiert, dass dieser subprocess schon mal gestartet wurde
					subprocessNeverStartet = false;
					
					// den zeitpunkt des starts festhalten
					this.getParent().getParent().setTimeOfLastStepStart(System.currentTimeMillis());

//				alternativer aufruf
	//				java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(args_for_syscall, " "));
					
	//				log("info", "call executed. pid="+sysproc.hashCode());
	
					// wait 2 seconds for becoming the pid-file visible
//					Thread.sleep(2000);
				}
				catch (Exception e2)
				{
					log("error", e2.getMessage());
				}
			}
		}
	}

//	public boolean isPidfileexistent()
//	{
//		java.io.File pidfile = new java.io.File(this.getParent().getAbspid());
//		if (pidfile.canRead())
//		{
//			log("debug", "pidfile found: "+pidfile.getAbsolutePath());
//			return true;
//		}
//		else
//		{
//			log("debug", "pidfile NOT found: "+pidfile.getAbsolutePath());
//			return false;
//		}
//	}
	
	public boolean isProcessBinaryFileExistent()
	{
		java.io.File processBinaryFile = new java.io.File(this.getParent().getAbsdir() + "/process.pmb");
		if (processBinaryFile.canRead())
		{
			log("debug", "processBinaryFile found: "+processBinaryFile.getAbsolutePath());
			return true;
		}
		else
		{
			log("debug", "processBinaryFile NOT found: "+processBinaryFile.getAbsolutePath());
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
			return null;
		}
		
		log("debug", "creating process");
		// einen neuen Process erstellen und den rootStep aus subprocess ruebernehmen
		de.prozesskraft.pkraft.Process newProcess = new Process();
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
		de.prozesskraft.pkraft.Process newProcess2 = null;
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
		log("debug", "comitting all lists of step to the rootStep of subprocess");
		newProcess2.getRootStep().setList(this.getParent().getList());
		
		// die loopvar des parentsteps auf den rootStep des neuenProzesses ueberschreiben, damit das resolving funktioniert
		log("debug", "mapping the loopVar of step to the rootStep of subprocess");
		newProcess2.getRootStep().setLoopvar(this.getParent().getLoopvar());
		
//		// den wert simultaneousSteps auf max=1 setzen unabhaengig was im xml definiert ist
//		log("debug", "setting attribute of subprocess maxSimultaneousSteps=1");
//		newProcess2.setMaxSimultaneousSteps(1);

		// alle commits aus subprocess in die des neuenProzesses ueberschreiben inkl. aller noch nicht resolvter eintraege
		log("debug", "setting all commits of embedded rootStep to the rootStep of new process");
		newProcess2.getRootStep().setCommit(this.getStep().getCommit());
		Step rootStepOfNewProcess = newProcess2.getRootStep();
		for(Commit actCommit : rootStepOfNewProcess.getCommit())
		{
			actCommit.setParent(rootStepOfNewProcess);
		}
		newProcess2.affiliate();
		
		// das Basedirectory des neuen prozesses soll das stepdir des parentsteps sein
		log("debug", "setting baseDir of subprocess to the same value like stepDir of step");
		newProcess2.setBaseDir(this.getParent().getAbsdir());
		
		// der neue Process ist ein Subprocess (das hat auswirkungen auf die pfade fuer die daten
		log("debug", "setting a flag to indicate that the subprocess is a subprocess");
		newProcess2.setSubprocess(true);
		
		// dem root-Step des neuenProzesses alle commits ausfuehren
//		newProcess2.getRootStep().commit();

		// alle listen wieder loeschen
//		newProcess2.getRootStep().getList().clear();

		return newProcess2;
	}
	
//	public void refreshProcess()
//	{
//		// wenn es einen Process gibt, dann den Status von this entsprechend des status des Processes updaten
//		if(this.getProcess() != null)
//		{
//			// neues setzen des binary pfads (falls die daten verschoben wurden)
//			this.getProcess().setInfilebinary(this.getProcess().getRootdir() + "/process.pmb");
//			this.log("debug", "refreshing Process from file: " + this.getProcess().getInfilebinary());
//
//			// einen neuen Process erstellen und aus file einlesen
//			Process updatedProcess = this.getProcess().readBinary();
//			
//			// status setzen
//			// ist Process == finished => status=worked
//			// ist Process == error => status=error
//			this.log("debug", "status of the process (triggered by subprocess): " + updatedProcess.getStatus());
//			
//			if(updatedProcess.getStatus().equals("finished"))
//			{
//				this.setStatus("finished");
//			}
//			else if(updatedProcess.getStatus().equals("error"))
//			{
//				this.setStatus("error");
//			}
//			else if(updatedProcess.getStatus().equals("rolling"))
//			{
//				this.setStatus("working");
//			}
//			
//			this.log("debug", "status of subprocess: " + updatedProcess.getStatus());
//			
//			// die binaerfiles setzen
//			updatedProcess.setInfilebinary(this.getProcess().getInfilebinary());
//			updatedProcess.setOutfilebinary(this.getProcess().getOutfilebinary());
//			
//			this.setProcess(updatedProcess);
//		}
//	}

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

//	public Step getStep()
//	{
//		return this.step;
//	}
//
//	public void setStep(Step step)
//	{
//		this.step = step;
//	}
//
//	public Step getRoot()
//	{
//		return this.step;
//	}
//
//	public void setRoot(Step step)
//	{
//		this.step = step;
//	}

	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	void refreshStatus() throws IOException
	{
		if(this.subprocessNeverStartet)
		{
			return;
		}
		
		// 1) alte methode: durch einlesen des prozessmodells
		//    diese methode kommt wieder in Mode, falls es gewuenscht ist, einen ganzen prozessbaum aktuell zu halten
		//    und dafuer langsamkeit in kauf nimmt
//		Process p1 = new Process();
//		p1.setInfilebinary(this.getParent().getAbsdir() + "/process.pmb");
//		Process processReread = p1.readBinary();
//		this.setDelayedStatus(processReread.getStatus());
//		this.setLastTimeDelayedStatusSet(System.currentTimeMillis());
//		log("info", "setting status to " + this.getStatus());

		java.io.File statusFile = new java.io.File(this.getParent().getAbsdir() + "/.status");
		
		if(statusFile.exists())
		{
			try
			{
				java.util.List<String> statusInhalt = Files.readAllLines(statusFile.toPath(), Charset.defaultCharset());
				if(statusInhalt.size() > 0)
				{
					this.setStatus(statusInhalt.get(0));
					log("info", "setting status to " + this.status);
				}
			}
			catch (ExceptionInInitializerError e)
			{
				System.err.println("trying to write file: " + this.getParent().getAbsdir() + "/.status");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return the status
	 * @throws IOException 
	 */
	public void setStatus(String newStatus)
	{
		this.status = newStatus;
	}

	/**
	 * @return the status
	 * @throws IOException 
	 */
	public String getStatus()
	{
		return this.status;
	}

	/**
	 * @return the parent
	 */
	public Step getParent()
	{
		if(this.parent != null)
		{
			return this.parent;
		}
		else
		{
			return parentDummy;
		}
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
	public Process getProcess()
	{
		Process p1 = new Process();
		p1.setInfilebinary(this.getParent().getAbsdir() + "/process.pmb");
		Process p2 = p1.readBinary();

		return p2; 
	}

//	/**
//	 * @param process the process to set
//	 */
//	public void setProcess(Process process) {
//		this.process = process;
//	}

	/**
	 * @return the maxrun
	 */
	public int getMaxrun() {
		return maxrun;
	}

	/**
	 * @param maxrun the maxrun to set
	 */
	public void setMaxrun(int maxrun) {
		this.maxrun = maxrun;
	}

	/**
	 * @return the step
	 */
	public Step getStep() {
		return step;
	}

	/**
	 * @param step the step to set
	 */
	public void setStep(Step step) {
		this.step = step;
	}

}
