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
	private int maxrun = 5;
	private String interpreter = "";
	private String command = "";
	private String logfile = "";
	private ArrayList<Callitem> callitem = new ArrayList<Callitem>();
	private ArrayList<Exit> exit = new ArrayList<Exit>();
	private String loop = null;

	private ArrayList<Log> log = new ArrayList<Log>();

	private String status = new String();	// waiting/working/worked/finished/error/cancelled
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

	/*----------------------------
	  methods virtual get
	----------------------------*/
	public String getCall()
	{
//		this.parent.log("debug", "constructing call");
		String call = this.command;
		this.log("debug", "constructing call a): "+call);

		this.log("debug", "there are "+this.getCallitem().size()+" unresolved callitems in this 'work'");

		// resolven aller callitems
		for(Callitem actCallitem : this.getCallitemssorted())
		{
			for(Callitem actResolvedCallitem : actCallitem.resolveCallitem())
			{
				call = call + " ";
				call = call + actResolvedCallitem.getPar();
				call = call + actResolvedCallitem.getDel();
				call = call + actResolvedCallitem.getVal();
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
	
	public String getLoop()
	{
		return this.loop;
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

	public void setLoop(String loop)
	{
		this.loop = loop;
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
				this.getParent().mkdir(this.getParent().getAbsdir());
			}

			// das logfile des Syscalls (zum debuggen des programms "process syscall" gedacht)
			String AbsLogSyscallWrapper = new java.io.File(new java.io.File(this.getParent().getAbspid()).getParent()).getAbsolutePath()+"/log";

			try
			{
				String[] args_for_syscall = {processSyscall, "-call \""+call+"\"", "-stdout "+this.getParent().getAbsstdout(), "-stderr "+this.getParent().getAbsstderr(), "-pid "+this.getParent().getAbspid(), "-mylog "+AbsLogSyscallWrapper, "-maxrun "+this.maxrun};

//				// erstellen prozessbuilder
//				ProcessBuilder pb = new ProcessBuilder(args_for_syscall);
//
//				// erweitern des PATHs um den prozesseigenen path
//				Map<String,String> env = pb.environment();
//				String path = env.get("PATH");
//				log("info", "adding to path: "+this.parent.getAbsPath());
//				path = this.parent.getAbsPath()+":"+path;
//				env.put("PATH", path);
//				log("info", "path: "+path);
//				
//				// setzen der aktuellen directory
//				java.io.File directory = new java.io.File(this.getAbsdir());
//				pb.directory(directory);
//				
//				// starten des prozesses
//				java.lang.Process sysproc = pb.start();

				log ("info", "calling: " + StringUtils.join(args_for_syscall, " "));

//				alternativer aufruf
				java.lang.Process sysproc = Runtime.getRuntime().exec(StringUtils.join(args_for_syscall, " "));
				log("info", "call executed. pid="+sysproc.hashCode());

				// wait 2 seconds for becoming the pid-file visible
				Thread.sleep(2000);

			}
			catch (Exception e2)
			{
				log("error", "something went wrong. an exception...");
				log("info", "setting status to 'error'");
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
