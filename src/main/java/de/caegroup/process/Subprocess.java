package de.caegroup.process;

import java.io.*;
//import java.util.*;
//import org.apache.solr.common.util.NamedList;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.SerializationUtils;

public class Subprocess
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
	private String version = "noversion";
	private int maxrun = 100;

	private Step step = new Step("root");

	private String status = "";	// waiting/finished/error

	private ArrayList<Log> log = new ArrayList<Log>();

	public Step parent;

	/*----------------------------
	  constructors
	----------------------------*/
	public Subprocess()
	{

	}

	public Subprocess(String name, String version)
	{
		this.name = name;
		this.version = version;
	}

	public Subprocess(String name, String version, Step step)
	{
		this.name = name;
		this.version = version;
		this.step = step;
	}

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
//		this.setStatus("");
	}
	

	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("subprocess-"+this.getName()+"-"+this.getVersion(), loglevel, logmessage));
	}

	/**
	 * does the work!
	 * 1) executes the call 
	 * 2) if the call already has been executed, then check whether it is still running
	 * 3) if it is not running anymore, the 
	 * 
	 * @param String loglevel, String logmessage
	 */
	public void doIt(String processSyscall, String processStartinstance)
	{
		this.setStatus("working");

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
			log("info", "subprocess not lauched yet");

			String call = this.getCall(processStartinstance);
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

	public String getCall(String processStartinstance)
	{
//		this.parent.log("debug", "constructing call");
		String call = processStartinstance;
		this.log("debug", "constructing call a): "+call);

		this.log("debug", "there are "+this.getStep().getCommit().size()+" commit(s) in this 'subprocess'");

		// resolven aller callitems
		for(Commit actCommit : this.getStep().getCommit())
		{
			for(File actFile : actCommit.getFile())
			{
				call += " ";
				call += "-commitfile "+actFile.getKey() + "=" + actFile.getGlob();
				this.log("debug", "constructing call b): "+call);
			}
			for(Variable actVariable : actCommit.getVariable())
			{
				call += " ";
				call += "-commitvariable " + actVariable.getKey() + "=" + actVariable.getValue();
				this.log("debug", "constructing call b): "+call);
			}
		}
		
		this.log("debug", "constructing call");
		return call;
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
	
}
