package de.caegroup.process;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.util.NamedList;

import de.caegroup.process.Callitem;

public class Work
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
	private String description = "no description";
	private int maxrun = 5; // minuten
	private String interpreter = "";
	private String command = "";
	private String logfile = "";
	private ArrayList<Callitem> callitem = new ArrayList<Callitem>();
	private ArrayList<Exit> exit = new ArrayList<Exit>();
//	private String loop = null; // kein loop in element 'work' -> dazu ist das element 'step' da!!

	private ArrayList<Log> log = new ArrayList<Log>();

	private String status = new String();	// waiting/working/finished/error/cancelled
	private int exitvalue;
	public Step parent;
	/*----------------------------
	  constructors
	----------------------------*/
	public Work()
	{
		this.parent = new Step();
	}

	public Work(Step step)
	{
		this.parent = step;
	}

	/*----------------------------
	  methods
	----------------------------*/
	public void addCallitem(Callitem callitem)
	{
		callitem.setParent(this);
		this.callitem.add(callitem);

	}

	/**
	 * removeCallitem
	 * remove a certain callitem from this
	 */
	public void removeCallitem(Callitem callitem)
	{
		ArrayList<Callitem> cleanedCallitem = new ArrayList<Callitem>();
		for(Callitem actCallitem : this.getCallitem())
		{
			if(!(actCallitem.equals(callitem)))
			{
				cleanedCallitem.add(actCallitem);
			}
		}

		this.callitem = cleanedCallitem;
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
		java.io.File binDir = new java.io.File(this.getParent().getParent().getInfilexml()).getParentFile();
		this.log("debug", "bin-directory for process-owned commands exists: "+binDir.getAbsolutePath());
		
		java.io.File evtlVorhScript = new java.io.File(binDir.getAbsolutePath() + "/" + this.command);
		this.log("debug", "looking for the command in process-owned bin: "+evtlVorhScript.getAbsolutePath());
		if(evtlVorhScript.exists() && !evtlVorhScript.isDirectory())
		{
			call = evtlVorhScript.getAbsolutePath();
			this.log("debug", "using the process-owned command: "+call);
		}
		else
		{
			call = this.command;
			this.log("debug", "command not found in process-owned bin-directory - will use command as a global command (must exist in $PATH of underlying shell): "+call);
		}
		
		this.log("debug", "constructing call a): "+call);

		this.log("debug", "there are "+this.getCallitem().size()+" unresolved callitems in this 'work'");

		// resolven aller callitems
		for(Callitem actCallitem : this.getCallitemssorted())
		{
			for(Callitem actResolvedCallitem : actCallitem.resolve())
			{
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
		return this.parent.getListItems(listname);
	}
	
	public Step getParent()
	{
		return this.parent;
	}
	
	public ArrayList<Log> getLog()
	{
		return this.log;
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
		this.log.add(new Log("work", loglevel, logmessage));
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
		this.setStatus("working");

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
}
