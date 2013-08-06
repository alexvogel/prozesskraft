package de.caegroup.process;

import java.io.*;
import java.util.*;

import de.caegroup.process.Commit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Step
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = new String();
	private String clip = null;
	private String type = new String();
	private String description = new String();
	private ArrayList<List> list = new ArrayList<List>();
	private ArrayList<Init> init = new ArrayList<Init>();
	private Work work = null;
	private ArrayList<Commit> commit = new ArrayList<Commit>();
	private String loop = null;
	private String loopvar = null;

	private Process parent = null;
//	private String dir = new String();
//	private String absdir = new String();
//	private String abspid = new String();
//	private String absstdout = new String();
//	private String absstderr = new String();
	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Variable> variable = new ArrayList<Variable>();
	private String status = "waiting";	// waiting/initializing/working/committing/ finished/broken/cancelled
	
	private ArrayList<Log> log = new ArrayList<Log>();
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 * constructs a step with
	 * a given parent
	 * a random name
	 */
	public Step(Process p)
	{
		this.setParent(p);
		this.setName(this.genName());
	}

	/**
	 * constructs a step with
	 * a new parent
	 * a given name
	 */
	public Step(String stepname)
	{
//		this.setParent(new Process());
		this.setName(stepname);
	}

	/**
	 * constructs a step with
	 * a given parent
	 * a given name
	 */

	public Step(Process p, String stepname)
	{
		this.setParent(p);
		this.setName(stepname);
	}

	/**
	 * constructs a step with
	 * a new parent
	 * a random name
	 */
	public Step()
	{
//		this.setParent(new Process());
		this.setName(this.genName());
	}

	/*----------------------------
	  methods 
	----------------------------*/
	@Override
	public Step clone()
	{
		try
		{
			return (Step) super.clone();
		}
		catch ( CloneNotSupportedException e )
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean areFromstepsfinished()
	{
		boolean allfinished = true;
		// alle fromsteps feststellen
		Iterator<Step> iterfromstep = this.getFromsteps().iterator();
		while (iterfromstep.hasNext())
		{
			Step fromstep = iterfromstep.next();
			// wenn nur einer nicht 'finished' ist, den status auf 'false'
			if (!(fromstep.getStatus().matches("finished")))
			{
				allfinished = false;
				return allfinished;
			}
		}
		return allfinished;
	}
	
	public boolean initialize()
	{
		// wenn nicht alle fromsteps den status 'finished' haben, wird nichts initialisiert
		if (!(this.areFromstepsfinished()))
		{
			return false;
		}
		
		this.setStatus("initializing");
		log("info", "setting status to 'initializing'");
		boolean initializing_success = true;
		// ueber alle inits iterieren
		for( Init actualInit : this.getInits())
		{
			// die init-angaben in lokale variablen uebernehmen
			String fromobjecttype = actualInit.getFromobjecttype();
			String name = actualInit.getName();
			String returnfield = actualInit.getReturnfield();
			Step fromstep = parent.getStep(actualInit.getFromstep());
			
			log("debug", "init '"+name+"': looking for field '"+returnfield+"' from a "+fromobjecttype+" of step '"+fromstep.getName()+"'");

			ArrayList<Match> matchs = actualInit.getMatch();
		
			// wenn es ein file ist
			if (fromobjecttype.equals("file"))
			{
				ArrayList<File> files_from_fromstep = fromstep.getFile();
				ArrayList<File> files_from_fromstep_which_matched = new ArrayList<File>();
				// wenn match-angaben vorhanden sind, wird die fileliste reduziert
				
				for(Match actualMatch : matchs)
				{
					log("debug", "init '"+name+"': accepting only "+fromobjecttype+"(s) which match '"+actualMatch.getPattern()+"' and field '"+actualMatch.getField()+"'");
					// iteriere ueber alle Files der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
					for(File actualFile : files_from_fromstep)
					{
						if (actualFile.match(actualMatch))
						{
							files_from_fromstep_which_matched.add(actualFile);
						}
					}
				}
				// wenn die fileliste leer ist, dann ist initialisierung fehlgeschlagen
				if (files_from_fromstep_which_matched.size() == 0) {initializing_success = false;}
				// aus der reduzierten file-liste, das gewuenschte field (returnfield) extrahieren und in der list unter dem Namen ablegen
				List liste = new List();

				// hinzufuegen der listitems
				for (File actualFile : files_from_fromstep_which_matched)
				{
					liste.addItem(actualFile.getField(returnfield));
				}
				
				this.addList(liste);
				liste.setName(actualInit.getName());

				log("debug", "init '"+name+"': new list '"+liste.getName()+"' with "+liste.getItem().size()+" item(s).");
				
			}
			// wenn es ein variable ist
			else if (fromobjecttype.equals("variable"))
			{
				ArrayList<Variable> variables_from_fromstep = fromstep.getVariable();
				ArrayList<Variable> variables_from_fromstep_which_matched = new ArrayList<Variable>();

				for (Match actualMatch : matchs)
				{
					log("debug", "init '"+name+"': accepting only "+fromobjecttype+"(s) which match '"+actualMatch.getPattern()+"' and field '"+actualMatch.getField()+"'");
					// iteriere ueber alle Variablen der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
					for (Variable actualVariable : variables_from_fromstep)
					{
						if (actualVariable.match(actualMatch))
						{
							variables_from_fromstep_which_matched.add(actualVariable);
						}
					}
				}
				if (variables_from_fromstep_which_matched.size() == 0) {initializing_success = false;}
				// aus der reduzierten variablen-liste, das gewuenschte field (returnfield) extrahieren und in der initlist unter dem Namen ablegen
				List liste = new List();

				for(Variable actualVariable : variables_from_fromstep_which_matched)
				{
					liste.addItem(actualVariable.getField(returnfield));
				}
				
				this.addList(liste);
				liste.setName(actualInit.getName());

				log("debug", "init '"+name+"': new list '"+liste.getName()+"' with "+liste.getItem().size()+" item(s).");
			}
		}
		// wenn alle initialisierung funktioniert habenn den status aendern
		// wenn eine loop gefordert ist, soll der step aufgefaechert werden.
		if (initializing_success)
		{
			this.setStatus("initialized");
			log("info", "setting status to 'initialized'");
						
			return true;
		}
		else
		{
			log("debug", "initialization failed.");
			return false;
		}
	}
	
	public boolean fan() throws CloneNotSupportedException
	{
		boolean success = false;
		this.setStatus("fanning");
		log("info", "setting status to 'fanning'");
		
		if (this.loop!=null && !(this.loop.equals("")))
		{
			// wenn die loopliste mindestens 1 wert enthaelt, ueber dioe liste iterieren und fuer jeden wert den aktuellen step clonen
			if (this.getListItems(this.loop).size() > 0)
			{
				Iterator<String> itervalue = this.getListItems(this.loop).iterator();
				int x = 1;
				while (itervalue.hasNext())
				{
					String value = itervalue.next();
					Step newstep = this.clone();
					newstep.setLoopvar(value);
					newstep.setLoop("");
					newstep.setName(newstep.getName()+"@"+x);
					newstep.setStatus("fanned");
					newstep.log("info", "this step '"+newstep.getName()+"' was fanned out from step '"+this.getName()+"'");
					newstep.log("info", "setting status to 'fanned'");
					this.parent.addStep(newstep);
					x++;
				}
				this.parent.removeStep(this);
				return true;
			}
		}
		
		// falls kein loop, soll der status trotzdem gesetzt werden
		this.setStatus("fanned");
		log("info", "setting status to 'fanned'");
		return true;
//		System.out.println("anzahl der Steps im Prozess nach dem fanning: "+this.parent.getSteps().size());
	}

	public boolean work()
	{
		boolean success = true;
		this.setStatus("working");
		log("info", "setting status to 'working'");

		Work work = this.work;
//			String call = work.generateCall(this.getListall());
		String call = work.getCall();

		// holen der zugehoerigen Systemprozess-ID - feststellen ob fuer diesen work schon ein aufruf getaetigt wurde
		
		
		// wenn schritt schon gestartet wurde
		if (this.isPidfileexistent())
		{
			String pid = this.getPid();	// aus der absdir des 'work' soll aus der Datei '.pid' die pid ermittelt werden. wenn noch keine existiert, wurde der schritt noch nicht gestartet
			log("info", "process work (script,program,..) already launched. pid="+pid);
//			System.out.println("PROCESS-STEP BEREITS GESTARTET: "+pid);
			
			if (Step.isPidalive(pid))
			{
				log("info", "process work (script,program,..) still running. pid="+pid);
				success = false;
//				System.out.println("PROCESS-STEP LAEUFT NOCH: "+pid);
			}
			else
			{
				log("info", "process work (script,program,..) already finished. pid="+pid);
//				System.out.println("PROCESS-STEP LAEUFT NICHT MEHR: "+pid);
				this.setStatus("worked");
				log("info", "setting status to 'worked'");
				success = true;
			}
		}
		// wenn schritt noch nicht gestartet wurde
		else 
		{
//			System.out.println("PROCESS-STEP IST NOCH NICHT GESTARTET");
			log("info", "process work (script,program,..) not lauched yet");
			log("info", "creating directory "+this.getAbsdir());
			success = this.mkdir(this.getAbsdir());
			
			try
			{
//					System.out.println("AUFRUF: /bin/bash /home/avo/bin/procsyscall "+call+" "+this.getAbsstdout()+" "+this.getAbsstderr()+" "+this.getAbspid());
//					String[] args_for_syscall = {"/bin/bash", "/home/avo/bin/procsyscall", call, this.getAbsstdout(), this.getAbsstderr(), this.getAbspid()};
//				System.out.println("AUFRUF: processsyscall "+call+" "+this.getAbsstdout()+" "+this.getAbsstderr()+" "+this.getAbspid());
				String[] args_for_syscall = {"process-syscall", call, this.getAbsstdout(), this.getAbsstderr(), this.getAbspid()};
				ProcessBuilder pb = new ProcessBuilder(args_for_syscall);
//				log("info", "constructing the systemcall to: processsyscall "+call+" "+this.getAbsstdout()+" "+this.getAbsstderr()+" "+this.getAbspid());

				// erweitern des PATHs um den prozesseigenen path
				Map<String,String> env = pb.environment();
				String path = env.get("PATH");
				log("info", "adding to path: "+this.parent.getPath());
				path = this.parent.getAbsPath()+":"+path;
				env.put("PATH", path);
				log("info", "path: "+path);
//				System.out.println("new PATH: "+path);
				
				java.io.File directory = new java.io.File(this.getAbsdir());
				pb.directory(directory);
				java.io.File checkdirectory = pb.directory().getAbsoluteFile();
//				System.out.println("ALS AKTUELLES VERZEICHNIS WIRD GESETZT+GECHECKT: "+checkdirectory);
				log("info", "calling: process-syscall "+call+" "+this.getAbsstdout()+" "+this.getAbsstderr()+" "+this.getAbspid());
				java.lang.Process p = pb.start();
//					Map<String,String> env = pb.environment();
//					String cwd = env.get("PWD");
//					String cwd = "/data/prog/workspace/prozesse/dummy/inst";
//					java.io.File directory = new java.io.File(cwd);
//					System.out.println("DIRECTORY SETZEN AUF "+directory.getAbsolutePath());
//					pb.directory(directory);
//					java.lang.Process p = pb.start();
//					java.lang.Process p = Runtime.getRuntime().exec(args_for_syscall);
//				System.out.println("PROCESS: "+p.hashCode());
				log("info", "process work (script,program,..) lauched. pid="+p.hashCode());
				success = true;
			}			
			catch (Exception e2)
			{
//				e2.printStackTrace();
				
				log("error", "something went wrong. an exception...");
				success = false;
			}
		}
		return success;
	}
	

	// eine extra methode fuer den step 'root'. es werden alle files/variablen aus 'path' committet
	public boolean rootcommit() throws IOException
	{

		//ueber alle initCommitDirs verzeichnisse iterieren
		for(java.io.File actualCommitDir : this.parent.getInitCommitDirs2())
		{
			this.commitdir(actualCommitDir);
		}

		// alle varfiles, die committed werden sollen zusammensuchen
		ArrayList<java.io.File> allcommitvarfiles = this.parent.getInitCommitVarfiles2();

		//ueber alle varfiles iterieren
		Iterator<java.io.File> itercommitvarfile = allcommitvarfiles.iterator();
		while (itercommitvarfile.hasNext())
		{
			java.io.File commitvarfile = itercommitvarfile.next();
			this.commitvarfile(commitvarfile);
		}
		this.setStatus("finished");
		
		return true;
	}
	
	// den inhalt eines ganzen directories in den aktuellen step committen
	public boolean commitdir(java.io.File dir)
	{
		this.log("info", "will commit directory "+dir.toString());
		this.log("info", "test whether it is a directory "+dir.toString());
		
		if (dir.isDirectory())
		{
			boolean all_commitfiles_ok = true;
			
			this.log("info", "it is really a directory");
			ArrayList<java.io.File> allfiles = new ArrayList<java.io.File>(Arrays.asList(dir.listFiles()));
			Iterator<java.io.File> iterfile = allfiles.iterator();
			while (iterfile.hasNext())
			{
				java.io.File file = iterfile.next();
				this.log("info", "test whether it is a file "+file.toString());
				if (file.isFile())
				{
					this.log("info", "it is a file");
					if (!(this.commitFile(file)))
					{
						all_commitfiles_ok = false;
					}
				}
				else
				{
					this.log("info", "it is NOT a file - skipping");
				}
			}
			return all_commitfiles_ok;
		}
		else
		{
			this.log("info", "it is not a directory - skipping");
			return false;
		}
	}

	public boolean commitdir(String absfilepathdir)
	{
		java.io.File file = new java.io.File(absfilepathdir);
		return commitdir(file);
	}

	// ein file in den aktuellen step committen
	public boolean commitFile(java.io.File file)
	{
		if (file.canRead())
		{
			File newfile = new File();
			newfile.setAbsfilename(file.getPath());
			this.addFile(newfile);
			this.log("info", "file committed: "+newfile.getAbsfilename());
//			System.out.println("AMOUNT OF FILES ARE NOW: "+this.file.size());
			return true;
		}
		else
		{
			this.log("info", "file NOT committed (CANT READ!): "+file.getAbsolutePath());
			return false;
		}
	}

	public boolean commitFile(String absfilepathdir)
	{
		java.io.File file = new java.io.File(absfilepathdir);
		return commitFile(file);
	}

	public void commitFile(File file)
	{
		this.log("info", "file committed: "+file.getAbsfilename());
		addFile(file);
	}

	/**
	 * commit einer variable aus zwei strings (name, value)
	 *
	 * der erste string wird als 'name' verwendet
	 * der zweite wert wird als 'value' verwendet
	*/	
	public boolean commitvariable(String name, String value)
	{
		Variable variable = new Variable();
		variable.setKey(name);
		variable.setValue(value);
		this.addVariable(variable);
		this.log("info", "variable committed as (name=value): "+variable.getKey()+"="+variable.getValue());
		return true;
	}

	/**
	 * commit einer variable aus einem string des musters (name=wert)
	 *
	 * der linke wert wird als 'name' verwendet
	 * der rechte wert wird als 'value' verwendet
	*/	
	public void commitvariable(String namevalue)
	{
		if (namevalue.matches("^[^#]([^= \n]+)=([^= \n]+) *\n?"))
		{
			String[] linelist = namevalue.split("=", 2);
			this.commitvariable(linelist[0], linelist[1]);
		}
	}
	
	/**
	 * commit von variablen aus einem file
	 * Eingabe ist ein Objekt des Typs java.io.File
	 * jede zeile des files, auf die das Muster "^[^#]([^=\s\n]+)=([^=\s\n]+)\s*\n?" passt
	 * wird zu einer variable geparst (split an "=")
	 *
	 * der linke wert wird als 'name' verwendet
	 * der rechte wert wird als 'value' verwendet
	 * @throws IOException 
	*/
	public boolean commitvarfile(java.io.File file) throws IOException
	{
		try
		{
			// wenn das file nicht zu gross ist (<100kB)
			if (file.length() < 102400.)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				
				try
				{
					String line;
					while ((line = in.readLine()) != null)
					{
						if (line.matches("^[^#]([^= \n]+)=([^= \n]+) *\n?"))
						{
							String[] linelist = line.split("=", 2);
							Variable variable = new Variable();
							variable.setKey(linelist[0]);
							variable.setValue(linelist[1]);
							this.addVariable(variable);
							this.log("info", "variable committed from file: "+file.getAbsolutePath()+" name: "+variable.getKey()+" value: "+variable.getValue());
						}
					}
				}
				finally
				{
					in.close();
				}
				return true;
			}
			else
			{
				this.log("info", "file is to big (>100kB) to commit content as variables: "+file.getAbsolutePath());
				return false;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			this.log("info", "variables not committed (cannot read file): "+file.getAbsolutePath());
			return false;
		}
	}

	public boolean commitvarfile(String absfilepathdir) throws IOException
	{
		java.io.File file = new java.io.File(absfilepathdir);
		return commitvarfile(file);
	}

	public boolean commit() throws IOException
	{
		boolean success = true;

		this.log("info", "will try to commit...");
		this.setStatus("committing");
		this.log("info", "status is set to "+this.getStatus());

		// wenn es sich um root handelt, wird besonders committed
		if (this.getName().equals(this.parent.getRootstepname()))
		{
			success = this.rootcommit();
		}
		
		// wenn es sich nicht um root handelt
		else
		{
			// ueber alle commits iterieren
			for( Commit actualCommit : this.commit)
			{
				this.log("info", "commit name "+actualCommit.getName());
				
				// wenn das zu committende objekt ein File ist...
				for(File actualFile : actualCommit.getFile())
				{
					this.log("info", "file id "+actualFile.getAbsfilename());
					java.io.File fsfile = new java.io.File(actualFile.getAbsfilename());
					if (this.commitFile(fsfile))
					{
						// wenn das File auch dem prozess committed werden soll, dann soll es ins instanzverzeichnis kopiert werden
						if (actualCommit.getToroot())
						{
							try
							{
								Runtime.getRuntime().exec("cp "+fsfile.getAbsolutePath()+" "+this.parent.getRootdir()+"/"+fsfile.getName());
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
						}
					}
					else
					{
						success = false;
						this.log("info", "commit(file) id NOT successfull");
					}
				}
				
				// wenn das zu committende objekt eine Variable ist...
				for(Variable actualVariable : actualCommit.getVariable())
				{
					if (this.commitvariable(actualVariable.getKey(), actualVariable.getValue()))
					{
						this.log("info", "commit(variable) id successfull");
					}
					else
					{
						success = false;
						this.log("info", "commit(variable) id NOT successfull");
					}
				}
				
				actualCommit.setSuccess(success);
			}
			if (this.areAllcommitssuccessfull())
			{
				this.setStatus("finished");
			}
		}
		return success;
	}

	public boolean areAllcommitssuccessfull()
	{
		boolean allcommitssuccessfull = true;
		// ueber alle commits iterieren
		Iterator<Commit> itercommit = this.getCommits().iterator();
		while (itercommit.hasNext())
		{
			Commit commit = itercommit.next();
			if (!(commit.getSuccess())) {allcommitssuccessfull = false;}
		}
		return allcommitssuccessfull;
	}
	
	public boolean mkdir(String directory)
	{
		java.io.File dir = new java.io.File(directory);
		// wenn directory existiert, dann die darin befindlichen files loeschen
		if (dir.exists())
		{
			log("debug", "directory already exists. deleting all content of "+directory);
			java.io.File[] files = dir.listFiles();
			for(int i=0; i<files.length; i++)
			{
				log("debug", "deleting file "+files[i].getAbsolutePath());
				files[i].delete();
			}
		}
		// ansonsten ein directory anlegen
		else
		{
			log("debug", "directory does not exist. creating directory "+directory);
			dir.mkdir();
		}
		
		// zum schluss testen ob es existiert und beschreibbar ist
		if ((dir.exists()) && (dir.canWrite()))
		{
			log("debug", "directory exists and writable "+directory);
			return true;
		}
		else
		{
			log("debug", "directory does not exist or is not writable "+directory);
			return false;
		}
	}
	
	/**
	 * sets the parent of all dependents to this instance
	 */
	public void affiliate()
	{
		for(Init actualInit : this.init)
		{
			actualInit.setParent(this);
		}
		for(Commit actualCommit : this.commit)
		{
			actualCommit.setParent(this);
		}
		if(this.work != null)
		{
			this.work.setParent(this);
		}
	}

	/*----------------------------
	  methods add
	----------------------------*/
	public void addInit(Init init)
	{
		this.init.add(init);
	}

	public void addCommit(Commit commit)
	{
		this.commit.add(commit);
	}

	public void addFile(File file)
	{
		this.file.add(file);
//		System.out.println("NOW FILES AMOUNT: "+this.files.size());
	}

	public void addVariable(Variable variable)
	{
		this.variable.add(variable);
	}
	
	public String genName()
	{
		final Random generator = new Random();
		long time = System.currentTimeMillis();
		generator.setSeed(time);
		return ""+generator.nextInt(9999999);
	}

	/**
	 * ermittelt den level eines steps.
	 * Hat ein Step einen Fromstep, so ist der level des Steps um 1 hoeher als der des Fromsteps
	 * @return level
	 */
	public int getLevel()
	{
		boolean run = true;
		ArrayList<Step> allFromsteps = this.getFromsteps();
		// einsammeln aller fromsteps
		while(run)
		{
			run = false;

			ArrayList<Step> newFromsteps = new ArrayList<Step>();
			
			for(Step actualStep : allFromsteps)
			{
				ArrayList<Step> fromstepsOfActualStep = actualStep.getFromsteps();
				for(Step actualStep2 : fromstepsOfActualStep)
				{
					if (!(allFromsteps.contains(actualStep2)))
					{
						newFromsteps.add(actualStep2);
					}
				}
			}
			
			// wenn neue fromsteps gefunden werden, soll erneut durchlaufen werden
			if (newFromsteps.size() > 0) {run = true;}
			allFromsteps.addAll(newFromsteps);
		}

		int level = 0;
		// den hoechsten level aller fromsteps ermitteln
		for(Step actualStep : allFromsteps)
		{
			int rankActualStep = actualStep.getLevel();
			if (rankActualStep > level)
			{
				level = rankActualStep;
			}
		}
		
		// der eigene level ist um 1 hoeher als der hoechste aller fromstep-level (auser beim rootstep)
		if (!(this.parent.getRootstepname().equals(this.getName())))
		{
			level++;
		}
		return level;
	}
	
	/**
	 * stores a message for the process
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log(loglevel, logmessage));
	}
	

	/*----------------------------
	  methods get
	----------------------------*/
	public boolean isListPresent(String listname)
	{
		boolean listIsPresent = false;
		Iterator<List> iterList = this.list.iterator();
		while(iterList.hasNext())
		{
			if (iterList.next().getName().equals(listname))
			{
				listIsPresent = true;
				return listIsPresent;
			}
		}
		return listIsPresent;
	}

	public String getName()
	{
		return this.name;
	}

	public String getClip()
	{
		return this.clip;
	}

	public String getAbsstdout()
	{
		return this.getAbsdir()+"/stdout.txt";
	}
	
	public String getAbsstderr()
	{
		return this.getAbsdir()+"/stderr.txt";
	}
	
	public String getAbspid()
	{
		return this.getAbsdir()+"/pid";
	}
	
	public boolean isPidfileexistent()
	{
		java.io.File pidfile = new java.io.File(this.getAbspid());
		if (pidfile.canRead())
		{
			System.out.println("PIDFILE GEFUNDEN: "+this.getAbspid());
			return true;
		}
		else
		{
			System.out.println("PIDFILE NICHT GEFUNDEN: "+this.getAbspid());
			return false;
		}
	}
	
	public String getPid()
	{
		String abspidpath = this.getAbspid();
		String pid = new String();
		try
		{
			System.out.println("PROCESS_ID FESTSTELLEN IN FILE: "+abspidpath);
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
	
	public String getType()
	{
		return this.type;
	}

	public String getDescription()
	{
		return this.description;
	}

	public Init getInit(String initname)
	{
		for(int i=0; i<init.size(); i++)
		{
			Init actualInit = init.get(i);
			if (actualInit.getName() == initname)
			{
				return actualInit;
			}
		}
		return null;
	}
	
	public ArrayList<Init> getInits()
	{
		return this.init;
	}

	public ArrayList<Init> getInit()
	{
		return this.init;
	}

	public Init getInit(int id)
	{
		return this.init.get(id);
	}

	public Init[] getInits2()
	{
		Init[] inits = new Init[this.init.size()];
		for (int i=0; i<this.init.size(); i++)
		{
			inits[i] = this.init.get(i);
		}
		return inits;
	}

	public String[] getInitnames()
	{
		String[] initnames = new String[this.init.size()];
		for (int i=0; i<this.init.size(); i++)
		{
			initnames[i] = this.init.get(i).getName(); 
		}
		return initnames;
	}
	
//	public ArrayList<Work> getWorks()
//	{
//		return this.work;
//	}
//
//	public Work[] getWorks2()
//	{
//		Work[] works = new Work[this.work.size()];
//		for (int i=0; i<this.work.size(); i++)
//		{
//			works[i] = this.work.get(i);
//		}
//		return works;
//	}
//
	public ArrayList<Commit> getCommits()
	{
		return this.commit;
	}

	public ArrayList<Commit> getCommit()
	{
		return this.commit;
	}

	public Commit getCommit(String name)
	{
		Iterator<Commit> iterCommit = this.commit.iterator();
		while(iterCommit.hasNext())
		{
			Commit actualCommit = iterCommit.next();
			if (actualCommit.getName().equals(name))
			{
				return actualCommit;
			}
		}
		return null;
	}

	public Commit[] getCommits2()
	{
		Commit[] commits = new Commit[this.commit.size()];
		for (int i=0; i<this.commit.size(); i++)
		{
			commits[i] = this.commit.get(i);
		}
		return commits;
	}

	public String getLoop()
	{
		return this.loop;
	}
	
	public String getLoopvar()
	{
		return this.loopvar;
	}

	public String getStatus()
	{
		return this.status;
	}

	public Process getParent()
	{
		return this.parent;
	}

	public String getAbsdir()
	{
		return this.parent.getRootdir()+"/STEP_"+this.getName();
	}

	public ArrayList<String> getListnames()
	{
		ArrayList<String> listnames = new ArrayList<String>();
		Iterator<List> iterList = this.list.iterator();
		while (iterList.hasNext())
		{
			listnames.add(iterList.next().getName());
		}
		
		return listnames;
	}

	public List getList(String listname) throws ListNotFoundException
	{
		List list = null;
		
		if (!(this.isListPresent(listname)))
		{
			throw new ListNotFoundException();
		}
		
		Iterator<List> iterList = this.list.iterator();
		while(iterList.hasNext())
		{
			List actualList = iterList.next();
			if (actualList.getName().equals(listname))
			{
				list = actualList;
				return list;
			}
		}
		return list;
	}
	
	public List getList(int index)
	{
		return list.get(index);
	}
	
	public ArrayList<List> getList()
	{
		return this.list;
	}
	
	public ArrayList<String> getListItems(String listname)
	{
		List list = null;
		try
		{
			list = this.getList(listname);
		} catch (ListNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list.getItem();
	}

	public Work getWork()
	{
		return this.work;
	}
	
	/**
	 * 
	 * @return step
	 */
	public ArrayList<Step> getFromsteps()
	{
		ArrayList<Step> fromsteps = new ArrayList<Step>();
		// jeden init durchgehen
//		System.out.println("stepname: "+this.getName()+" || anzahlInits: "+this.getInit().size());
		for(Init actualInit : this.getInit())
		{
			// und diese steps in einem arraylist aufsammeln
//			System.out.println("anzahl aller steps im aktuellen prozess: "+this.parent.getStep().size());
			ArrayList<Step> steps = this.parent.getSteps(actualInit.getFromstep());
//			System.out.println("my parent is: "+this.parent.toString());
//			System.out.println("anzahl der fromsteps im aktuellen init: "+steps.size());
//			System.out.println("actualInit: "+actualInit.getName()+" || fromStep: "+actualInit.getFromstep());
			// nur die noch nicht als fromstep erkannten steps der suchliste hinzufuegen
			for(Step actualStep : steps)
			{
				if (!(fromsteps.contains(actualStep)))
				{
					fromsteps.add(actualStep);
				}
			}
		}
//		System.out.println("anzahl der fromsteps2: "+fromsteps.size());
		return fromsteps;
	}

	public ArrayList<File> getFile()
	{
		return this.file;
	}
		
	public ArrayList<Variable> getVariable()
	{
		return this.variable;
	}
		
	public void addList(List list)
	{
		this.list.add(list);
	}
		
	public boolean isAmultistep()
	{
		boolean isamultistep = false;
		if ( (this.getLoop() != null) )
		{
			isamultistep = true;
		}
		return isamultistep;
	}
	
	public ArrayList<Log> getLog()
	{
		return this.log;
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setClip(String clip)
	{
		this.clip = clip;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setLoop(String loop)
	{
		this.loop = loop;
	}
	
	public void setLoopvar(String loopvar)
	{
		this.loopvar = loopvar;
	}

	public void setList(ArrayList<List> list)
	{
		this.list = list;
		Iterator<List> iterList = this.list.iterator();
		while(iterList.hasNext())
		{
			iterList.next().setParent(this);
		}

	}
	
	public void setInit(ArrayList<Init> init)
	{
		this.init = init;
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public void setParent(Process process)
	{
		this.parent = process;
	}
	
	public void setWork(Work work)
	{
		this.work = work;
	}

	public void setCommit(ArrayList<Commit> commit)
	{
		this.commit = commit;
		Iterator<Commit> iterCommit = this.commit.iterator();
		while(iterCommit.hasNext())
		{
			iterCommit.next().setParent(this);
		}
	}
	
}
