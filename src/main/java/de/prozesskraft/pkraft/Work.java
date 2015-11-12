package de.prozesskraft.pkraft;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.util.NamedList;

import de.prozesskraft.pkraft.Callitem;

public class Work
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
	private String description = "no description";
	private Integer maxrun = 5; // minuten
	private String precall = null; // dieser aufruf wird unmittelbar vor dem eigentlichen work-command abgesetzt. dient z.B. zum starten von Xvfb o.ae.
	private String env = null; // environment definitionen getrennt durch diese zeichenfolge [trenner]. z.B. "DISPLAY=:77[trenner]HOME=/home/avoge"
	private String interpreter = "";
	private String command = "";
	private String logfile = "";
	private String killcommand = null; // dieser aufruf wird bei einem kill zusaetzlich aufgerufen - gefolgt von der variable 'killpid'
	private String status = "";	// waiting/working/finished/error/cancelled
	//	private String loop = null; // kein loop in element 'work' -> dazu ist das element 'step' da!!

	private ArrayList<Callitem> callitem = new ArrayList<Callitem>();
	private ArrayList<Exit> exit = new ArrayList<Exit>();
	private ArrayList<Log> log = new ArrayList<Log>();

	private int exitvalue;
	
	// don't clone parent when you clone this
	public Step parent = null;
	transient private Step parentDummy = null;
	/*----------------------------
	  constructors
	----------------------------*/
	public Work()
	{
		Step dummyStep = new Step();
		dummyStep.setName("dummy");
		this.parentDummy = dummyStep;
	}

	public Work(Step step)
	{
		this.parent = step;
		step.setWork(this);
	}

	/*----------------------------
	  methods
	----------------------------*/
	
	public Work clone()
	{
		Work newWork = new Work();
		newWork.setName(this.getName());
		newWork.setDescription(this.getDescription());
		newWork.setMaxrun(this.getMaxrun());
		newWork.setPrecall(this.getPrecall());
		newWork.setEnv(this.getEnv());
		newWork.setInterpreter(this.getInterpreter());
		newWork.setCommand(this.getCommand());
		newWork.setLogfile(this.getLogfile());
		newWork.setKillcommand(this.getKillcommand());
		newWork.setStatus(this.getStatus());
		for(Callitem actCallitem : this.getCallitem())
		{
			newWork.addCallitem(actCallitem.clone());
		}
		for(Exit actExit : this.getExit())
		{
			newWork.addExit(actExit.clone());
		}
		for(Log actLog : this.getLog())
		{
			newWork.addLog(actLog.clone());
		}
		newWork.setExitvalue(this.getExitvalue());
		
		return newWork;
		
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
	
	public void addExit(Exit exit)
	{
		this.exit.add(exit);
	}
	
	public void addLog(Log log)
	{
		this.log.add(log);
	}
	
	public void addCallitem(Callitem callitem)
	{
		callitem.setParent(this);
		if(!this.callitem.contains(callitem))
		{
			this.callitem.add(callitem);
		}
	}

	public void addCallitem(ArrayList<Callitem> callitems)
	{
		for(Callitem actCallitem : callitems)
		{
			this.addCallitem(actCallitem);
		}
	}

	
	
	/**
	 * removeCallitem
	 * remove a certain callitem from this
	 */
	public void removeCallitem(Callitem callitem)
	{
		this.callitem.remove(callitem);
	}

	/**
	 * reset this
	 */
	public void reset()
	{
		this.log.clear();
		this.setStatus("waiting");
		for(Callitem actCallitem : this.getCallitem())
		{
			actCallitem.reset();
		}
	}
	
	/**
	 * resolve
	 * resolves all the entries in the attributes
	 */
	public void resolve()
	{
		// den eintrag im attribut 'interpreter' resolven
		if(this.getInterpreter()!=null)
		{
			this.setInterpreter(this.getInterpreter().replaceAll("\\{\\$loopvarstep\\}", this.getParent().getLoopvar()));
			this.setInterpreter(this.getParent().resolveString(this.getInterpreter()));
		}

		// den eintrag im attribut 'command' resolven
		if(this.getCommand()!=null)
		{
			this.setCommand(this.getCommand().replaceAll("\\{\\$loopvarstep\\}", this.getParent().getLoopvar()));
			this.setCommand(this.getParent().resolveString(this.getCommand()));
		}
		
		// den eintrag im attribut 'description' resolven
		if(this.getDescription()!=null)
		{
			this.setDescription(this.getDescription().replaceAll("\\{\\$loopvarstep\\}", this.getParent().getLoopvar()));
			this.setDescription(this.getParent().resolveString(this.getDescription()));
		}
		
		// den eintrag im attribut 'name' resolven
		if(this.getName()!=null)
		{
			this.setName(this.getName().replaceAll("\\{\\$loopvarstep\\}", this.getParent().getLoopvar()));
			this.setName(this.getParent().resolveString(this.getName()));
		}
		
		// den eintrag im attribut 'logfile' resolven
		if(this.getLogfile()!=null)
		{
			this.setLogfile(this.getLogfile().replaceAll("\\{\\$loopvarstep\\}", this.getParent().getLoopvar()));
			this.setLogfile(this.getParent().resolveString(this.getLogfile()));
		}
	}

	/**
	 * aufloesen eines strings und aller darin verschachtelter verweise auf listitems
	 * @param stringToResolve
	 * @return
	 */
	public String resolveString(String stringToResolve)
	{
		return this.getParent().resolveString(stringToResolve);
	}
	
	/*----------------------------
	  methods virtual get
	----------------------------*/
	public String getCall()
	{
//		this.parent.log("debug", "constructing call");
		String call = null;
		
		
		// wenn command im prozess-eigenen bin-verzeichnis zu finden ist, soll das command auf absoluten pfad erweitert werden
		java.io.File definitionDir = new java.io.File(new java.io.File(this.getParent().getParent().getInfilexml()).getParentFile().getAbsolutePath());
		
		// alle directories durchgehen, die im prozessPath angegeben sind
		for(java.io.File actPathFile : this.getParent().getParent().getPaths2())
		{
			// fuer jedes file in einem pathdirectory ueberpruefen ob es das gesuchte script ist
			// falls es mehrere pfade gibt, sollen alle durchgegangen werden. der letzte match ist ausschlaggebend
			for(java.io.File evtlScript : actPathFile.listFiles())
			{
				if(evtlScript.getName().equals(this.command))
				{
					call = evtlScript.getAbsolutePath();
					this.log("debug", "using the process-owned command: "+call);
				}
			}
		}

		// falls nicht in prozesseigenem path, dann davon ausgehen, dass im systemeigenen path vorhanden ist
		if(call == null)
		{
			call = this.command;
			this.log("debug", "command not found in process-owned bin-directory - will use command as a global command (must exist in $PATH of underlying shell): "+call);
		}
		
		this.log("debug", "constructing call a): "+call);

		this.log("debug", "there are "+this.getCallitem().size()+" unresolved callitems in this 'work'");

		// resolven aller callitems
		for(Callitem actCallitem : this.getCallitemssorted())
		{
			// die beim resolven erzeugten callitems werden nicht in work abgelegt - sie existieren nur beim resolven (clone)
			// wg evtl. loops entstehen beim resolven eines callitems evtl. mehr oder weniger callitems
			ArrayList<Callitem> actCallitemResolved = actCallitem.resolve();
			
			// die resolvten Callitems werden dem aktuellen work wieder hinzugefuegt
//			this.addCallitem(actCallitemResolved);
			
			log("debug", "the xml-callitem sequence " + actCallitem.getSequence() + " has been resolved to " +actCallitemResolved.size() +" callitems");
			for(Callitem actResolvedCallitem : actCallitemResolved)
			{
				log("debug", "resolved callitem " + actCallitem.getSequence() + ": " + actResolvedCallitem.getPar() + actResolvedCallitem.getDel() + actResolvedCallitem.getVal());
	//			log("debug", "step of resolved callitem is: "+actResolvedCallitem.getParent().getParent().toString());
				call += " ";
				call += actResolvedCallitem.getPar();
				call += actResolvedCallitem.getDel();
				call += actResolvedCallitem.getVal();
				this.log("debug", "constructing call b): "+call);
			}
		}
		
		this.log("debug", "constructing call");
		return call;
	}
	
	/**
	 * sets the parent of all dependents to this instance
	 */
	public void affiliate()
	{
		for(Callitem actualCallitem : this.getCallitem())
		{
			actualCallitem.setParent(this);
		}
	}

	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getInterpreter()
	{
		return this.interpreter;
	}

	public String getCommand()
	{
		return this.command;
	}

	public String getLogfile()
	{
		return this.logfile;
	}

	public ArrayList<Callitem> getCallitems()
	{
		return this.callitem;
	}
	
	public ArrayList<Callitem> getCallitem()
	{
		return this.callitem;
	}
	
	public Callitem getCallitem(int index)
	{
		return this.callitem.get(index);
	}
	
	/**
	 * getCallitemssorted
	 * returns all callitems of this sorted by the field 'sequence'
	 * @return ArrayList<Callitem>
	 */
	public ArrayList<Callitem> getCallitemssorted()
	{
		ArrayList<Integer> sequences = new ArrayList<Integer>();
		
		// aus den vorhandenen callitems die sequences in ein eigenes array extrahieren
		for(Callitem actCallitem : this.callitem)
		{
			sequences.add(actCallitem.getSequence());
		}

		// das sequences-array sortieren
		Collections.sort(sequences);

		// ueber das sortierte sequences-array iterieren
		ArrayList<Callitem> callitems_sorted = new ArrayList<Callitem>();
		for(Integer actSequence : sequences)
		{
			// und das zugehoerige callitem rausfischen
			for(Callitem actCallitem : this.getCallitem())
			{
				if (actCallitem.getSequence() == actSequence)
				{
					callitems_sorted.add(actCallitem);
				}
			}
		}
		return callitems_sorted;
	}
	
	public Callitem[] getCallitems2()
	{
		Callitem[] callitems = new Callitem[this.callitem.size()];
		for (int i=0; i<this.callitem.size(); i++)
		{
			callitems[i] = this.callitem.get(i);
		}
		return callitems;
	}

	public ArrayList<Exit> getExit()
	{
		return this.exit;
	}
	
	public String getStatus()
	{
		return this.status;
	}

	public int getExitvalue()
	{
		return this.exitvalue;
	}
	
	public ArrayList<String> getListItems(String listname)
	{
		return this.getParent().getListItems(listname);
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

	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	/**
	 * clears all the logs from the object and subobjects
	 * @param
	 */
	public void clearLogRecursive()
	{
		// in diesem object die logs entfernen
		this.getLog().clear();
		
		for(Callitem actCallitem : this.callitem)
		{
			actCallitem.getLog().clear();
		}
	}	

	public ArrayList<Log> getLogRecursive()
	{
		ArrayList<Log> logRecursive = this.log;

		for(Callitem actCallitem : this.callitem)
		{
			logRecursive.addAll(actCallitem.getLog());
		}

		// sortieren nach Datum
		Collections.sort(logRecursive);

		return logRecursive;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setInterpreter(String interpreter)
	{
		this.interpreter = interpreter;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public void setLogfile(String logfile)
	{
		this.logfile = logfile;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public void setExitvalue(int exitvalue)
	{
		this.exitvalue = exitvalue;
	}

	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}

	/**
	 * @param ArrayList<Callitem> the callitem to set
	 */
	public void setCallitem(ArrayList<Callitem> callitem)
	{
		this.callitem = callitem;
	}

	/**
	 * @param ArrayList<Callitem> the callitem to set
	 */
	public void setExit(ArrayList<Exit> exit)
	{
		this.exit = exit;
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
	 */
	public void doIt(String processSyscall)
	{

		this.resolve();

		// wenn schritt schon gestartet wurde
		if (this.isPidfileexistent())
		{
			String pid = this.getPid();	// aus der absdir des steps soll aus der Datei '.pid' die pid ermittelt werden. wenn noch keine existiert, wurde der schritt noch nicht gestartet
			log("info", "call already executed pid="+pid);
//			System.out.println("PROCESS-STEP BEREITS GESTARTET: "+pid);

			if (isPidalive(pid))
			{
				log("info", "program still running. pid="+pid);
//				System.out.println("PROCESS-STEP LAEUFT NOCH: "+pid);
			}
			else
			{
				log("info", "program already finished. pid="+pid);
				log("info", "setting status to 'finished'");
				this.setStatus("finished");
			}
		}

		// wenn schritt noch nicht gestartet wurde
		else 
		{
			// soll der Schritt ueberhaupt gestartet werden?
			boolean schrittStarten = true;
			
			// nicht starten, wenn aktuell laufendeSteps >= simultaneousSteps(max=?)
			// nicht starten, wenn der zeitpunkt des letzten Stepstarts kuerzer zurueckliegt als simultaneousSteps(delay=?)
			// nicht starten, wenn stepStartLoadAverageBelow niedriger ist als das aktuelle loadAverage
			
			// 1) max. erlaubte laufende stepanzahl bereits erreicht?
			Integer workingSteps =  this.getParent().getParent().getStepWorking().size();
			log("debug", "amount of working Steps is: " + workingSteps);
			if( (this.getParent().getParent().getMaxSimultaneousSteps() != null) && (this.getParent().getParent().getMaxSimultaneousSteps() <= workingSteps) )
			{
				log("info", "starting of new steps is not allowed at the moment by reference of maxSimultaniousSteps (" + this.getParent().getParent().getMaxSimultaneousSteps() + ")");
				schrittStarten = false;
				
				// merken
				this.getParent().getParent().counterLoadAverageTooHigh++;
			}
			else
			{
				log("info", "starting of new steps is allowed at the moment by reference of maxSimultaniousSteps (" + this.getParent().getParent().getMaxSimultaneousSteps() + ")");
				this.getParent().getParent().counterLoadAverageTooHigh = 0;
			}

			log("debug", "now in milliseconds: " + System.currentTimeMillis());
			Integer minutesSinceLastStepStart = (int)((long)(System.currentTimeMillis() - this.getParent().getParent().getTimeOfLastStepStart()) / 60000);
			log("debug", "time since last step start in minutes: " + minutesSinceLastStepStart);

			// 2) der zeitpunkt an dem der letzte schritt gestartet wurde ist weniger minuten her als der mindest-Delay fuer Stepstarts vorschreibt
			Integer stepStartDelayMinutes = null;
			
			// feststellen welche stepStartDelayMinutes -Angabe gilt, falls ueberhaupt vorhanden
			if(this.getParent().getStepStartDelayMinutes() != null)
			{
				stepStartDelayMinutes = this.getParent().getStepStartDelayMinutes();
			}
			else if(this.getParent().getParent().getStepStartDelayMinutes() != null)
			{
				stepStartDelayMinutes = this.getParent().getParent().getStepStartDelayMinutes();
			}
			// und damit entscheiden, ob der schritt gestartet werden soll
			if( (stepStartDelayMinutes != null) && (minutesSinceLastStepStart < stepStartDelayMinutes) )
			{
				log("info", "starting of new steps is not allowed at the moment by reference of stepStartDelayMinutes (" + this.getParent().getParent().getStepStartDelayMinutes() + ")");
				schrittStarten = false;
			}
			else
			{
				log("info", "starting of new steps is allowed at the moment by reference of stepStartDelayMinutes (" + this.getParent().getParent().getStepStartDelayMinutes() + ")");
			}

			double actLoadAverage = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
			log("debug", "the present load average is " + actLoadAverage);
//			log("debug", "all the load averages till now are " + this.getParent().getParent().getTimeSerieLoadAverage().sprint());

			// 3) das load average unterhalb eines definierten wertes
			if( (this.getParent().getParent().getStepStartLoadAverageBelow() != null) && (actLoadAverage > this.getParent().getParent().getStepStartLoadAverageBelow()) )
			{
				log("info", "starting of new steps is not allowed at the moment by reference of stepStartLoadAverageBelow (" + this.getParent().getParent().getStepStartLoadAverageBelow() + ")");
				schrittStarten = false;
			}
			else
			{
				log("info", "starting of new steps is allowed at the moment by reference of stepStartLoadAverageBelow (" + this.getParent().getParent().getStepStartLoadAverageBelow() + ")");
			}
			
			// wenn alle voraussetzungen zum starten eines neuen steps erfuellt sind
			if(schrittStarten)
			{
				this.setStatus("working");

				// den aufruf erstellen
				log("info", "program not lauched yet");
	
				String call = this.getCall();
				log("info", "generating call: "+call);
	
				// step-directory anlegen, falls es noch nicht existiert
				// falls es ein wrapper-process ist, gibt es das directory warsch. schon
				if(!(new java.io.File(this.getParent().getAbsdir()).exists()))
				{
					log("info", "creating step directory "+this.getParent().getAbsdir());
					if(!this.getParent().mkdir(this.getParent().getAbsdir()))
					{
						log("error", "could not create directory: "+this.getParent().getAbsdir());
						this.setStatus("error");
					}
				}
	
				// das logfile des Syscalls (zum debuggen des programms "process syscall" gedacht)
				String AbsLogSyscallWrapper = new java.io.File(new java.io.File(this.getParent().getAbspid()).getParent()).getAbsolutePath()+"/.log";
	
				try
				{
					// den Aufrufstring fuer die externe App (process syscall --version 0.6.0)) splitten
					// beim aufruf muss das erste argument im path zu finden sein, sonst gibt die fehlermeldung 'no such file or directory'
					ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(processSyscall.split(" ")));
	
					// die sonstigen argumente hinzufuegen
					processSyscallWithArgs.add("-call");
					processSyscallWithArgs.add(call);
	//				processSyscallWithArgs.add("\""+call+"\"");
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
	
					// sind env-angaben im work-element?, dann soll das bestehende environment um diese erweitert bzw. umdefiniert werden
					if(this.getEnv() != null)
					{
						Map<String,String> zusEnv = this.getEnvAsMap();

						// env loggen
						String logString = "tweaking environment variables\n";
						for(String actKey : zusEnv.keySet())
						{
							logString += actKey + "=" + zusEnv.get(actKey);
						}
						log("info", logString);
						
						// env definieren
						pb.environment().putAll(zusEnv);
					}
					else
					{
						log("debug", "no change in environment variables");
					}

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
					
					// aufruf des precalls
					if(this.getPrecall() != null)
					{
							// erstellen prozessbuilder fuer precall
						ProcessBuilder pbPrecall = new ProcessBuilder(this.getPrecall().split(" "));
						log ("info", "calling preCall: " + pbPrecall.command());
						pbPrecall.start();
					}

					log ("info", "calling: " + pb.command());
					
					// starten des prozesses
					java.lang.Process sysproc = pb.start();
	
	//				alternativer aufruf
	//				java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(args_for_syscall, " "));
					
	//				log("info", "call executed. pid="+sysproc.hashCode());
	
					// den zeitpunkt des starts festhalten
					this.getParent().getParent().setTimeOfLastStepStart(System.currentTimeMillis());

					// wait 2 seconds for becoming the pid-file visible
					Thread.sleep(2000);
				}
				catch (Exception e2)
				{
					log("error", e2.getMessage());
					this.setStatus("error");
				}
			}
		}
	}
	
	
	public boolean isPidfileexistent()
	{
		java.io.File pidfile = new java.io.File(this.getParent().getAbspid());
		if (pidfile.canRead())
		{
			System.out.println("PIDFILE GEFUNDEN: "+pidfile.getAbsolutePath());
			return true;
		}
		else
		{
			System.out.println("PIDFILE NICHT GEFUNDEN: "+pidfile.getAbsolutePath());
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

	/**
	 * @return the maxrun
	 */
	public Integer getMaxrun() {
		return maxrun;
	}

	/**
	 * @param maxrun the maxrun to set
	 */
	public void setMaxrun(Integer maxrun) {
		this.maxrun = maxrun;
	}

	/**
	 * @return the killcommand
	 */
	public String getKillcommand() {
		return killcommand;
	}

	/**
	 * @param killcommand the killcommand to set
	 */
	public void setKillcommand(String killcommand) {
		this.killcommand = killcommand;
	}

	/**
	 * der environment string wird in einen Map umgewandelt und zurueckgegeben
	 * @return the envAsMap
	 */
	public Map<String,String> getEnvAsMap()
	{
		Map<String,String> environment = new HashMap<String,String>();
		
		if(this.getEnv() != null)
		{
			
			for(String oneEnvDefinition : this.getEnv().split("\\[trenner\\]"))
			{
				String[] oneEnv = oneEnvDefinition.split("=", 2);
				if(oneEnv.length == 2)
				{
					environment.put(this.getParent().resolveString(oneEnv[0]), this.getParent().resolveString(oneEnv[1]));
				}
			}
		}
		
		return environment;
	}

	/**
	 * @return the env
	 */
	public String getEnv() {
		return env;
	}

	/**
	 * @param env the env to set
	 */
	public void setEnv(String env) {
		this.env = env;
	}

	/**
	 * @return the precall
	 */
	public String getPrecall() {
		return precall;
	}

	/**
	 * @param precall the precall to set
	 */
	public void setPrecall(String precall) {
		this.precall = precall;
	}

}
