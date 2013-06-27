package de.caegroup.process;

import java.io.*;
import java.util.*;

import org.apache.solr.common.util.NamedList;

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
	private String type = new String();
	private String description = new String();
	private ArrayList<List> list = new ArrayList<List>();
	private ArrayList<Init> init = new ArrayList<Init>();
	private Work work = null;
	private ArrayList<Commit> commit = new ArrayList<Commit>();
	private String loop = new String();
	private String loopvar = new String();

	private Process parent;
//	private String dir = new String();
//	private String absdir = new String();
//	private String abspid = new String();
//	private String absstdout = new String();
//	private String absstderr = new String();
	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Variable> variable = new ArrayList<Variable>();
	private String status = "waiting";	// waiting/initializing/working/committing/ finished/broken/cancelled
	
	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
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
		this.setParent(new Process());
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
		this.setParent(new Process());
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
		boolean initializing_success = true;
		// ueber alle inits iterieren
		for( Init actualInit : this.getInits())
		{
			// die init-angaben in lokale variablen uebernehmen
			String fromobjecttype = actualInit.getFromobjecttype();
			String name = actualInit.getName();
			String returnfield = actualInit.getReturnfield();
			ArrayList<Step> fromsteps = parent.getSteps(actualInit.getFromstep());
			
			Iterator<Step> iterstep = fromsteps.iterator();
			while (iterstep.hasNext())
			{
				Step fromstep = iterstep.next();
				jlog.log(Level.INFO, "init ("+name+") wants the returnfield ("+returnfield+") from a ("+fromobjecttype+") from step ("+fromstep.getName()+")");
				ArrayList<Match> matchs = actualInit.getMatch();
			
				// wenn es ein file ist
				if (fromobjecttype.equals("file"))
				{
					ArrayList<File> files_from_fromstep = fromstep.getFile();
					ArrayList<File> files_from_fromstep_which_matched = new ArrayList<File>();
					// wenn match-angaben vorhanden sind, wird die fileliste reduziert
					Iterator<Match> itermatch = matchs.iterator();
					// iteriere ueber matchs
					while (itermatch.hasNext())
					{
						Match match = itermatch.next();
						// iteriere ueber alle Files der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
						Iterator<File> iterfile = files_from_fromstep.iterator();
						while (iterfile.hasNext())
						{
							File file = iterfile.next();
							if (file.match(match))
							{
								files_from_fromstep_which_matched.add(file);
							}
						}
					}
					// wenn die fileliste leer ist, dann ist initialisierung fehlgeschlagen
					if (files_from_fromstep_which_matched.size() == 0) {initializing_success = false;}
					// aus der reduzierten file-liste, das gewuenschte field (returnfield) extrahieren und in der list unter dem Namen ablegen
					List liste = new List();
					this.addList(liste);
					
					for (File actualFile : files_from_fromstep_which_matched)
					{
						liste.addItem(actualFile.getField(returnfield));
					}
				}

				// wenn es ein variable ist
				else if (fromobjecttype.equals("variable"))
				{
					ArrayList<Variable> variables_from_fromstep = fromstep.getVariable();

					for (Match actualMatch : matchs)
					{
						// iteriere ueber alle Files der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
						for (Variable actualVariable : variables_from_fromstep)
						{
							if (!(actualVariable.match(actualMatch)))
							{
								variables_from_fromstep.remove(actualVariable);
							}
						}
					}
					if (variables_from_fromstep.size() == 0) {initializing_success = false;}
					// aus der reduzierten variablen-liste, das gewuenschte field (returnfield) extrahieren und in der initlist unter dem Namen ablegen
					List liste = new List();
					this.addList(liste);
					Iterator<Variable> itervariable = variables_from_fromstep.iterator();
					while (itervariable.hasNext())
					{
						Variable variable = itervariable.next();
						liste.addItem(variable.getField(returnfield));
					}
				}
			}
		}
		// wenn alle initialisierung funktioniert habenn den status aendern
		// wenn eine loop gefordert ist, soll der step aufgefaechert werden.
		if (initializing_success)
		{
			this.setStatus("initialized");
						
			return true;
		};
		return false;
	}
	
	public void fan() throws CloneNotSupportedException
	{
		this.setStatus("fanning");
		
		System.out.println("anzahl der Steps im Prozess vor dem fanning: "+this.parent.getSteps().size());
		
		if (!(this.loop.equals("")))
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
					this.parent.addStep(newstep);
					x++;
				}
				this.parent.removeStep(this);
			}
		}
		this.setStatus("fanned");
		System.out.println("anzahl der Steps im Prozess nach dem fanning: "+this.parent.getSteps().size());
	}

	public void work()
	{
		this.setStatus("working");

		Work work = this.work;
//			String call = work.generateCall(this.getListall());
		String call = work.getCall();

		// holen der zugehoerigen Systemprozess-ID - feststellen ob fuer diesen work schon ein aufruf getaetigt wurde
		
		
		// wenn schritt schon gestartet wurde
		if (this.isPidfileexistent())
		{
			String pid = this.getPid();	// aus der absdir des 'work' soll aus der Datei '.pid' die pid ermittelt werden. wenn noch keine existiert, wurde der schritt noch nicht gestartet
			System.out.println("PROCESS-STEP BEREITS GESTARTET: "+pid);
			
			if (Step.isPidalive(pid))
			{
				System.out.println("PROCESS-STEP LAEUFT NOCH: "+pid);
			}
			else
			{
				System.out.println("PROCESS-STEP LAEUFT NICHT MEHR: "+pid);
				this.setStatus("worked");
			}
		}
		// wenn schritt noch nicht gestartet wurde
		else 
		{
			System.out.println("PROCESS-STEP IST NOCH NICHT GESTARTET");
			this.mkdir(this.getAbsdir());
			
			try
			{
//					System.out.println("AUFRUF: /bin/bash /home/avo/bin/procsyscall "+call+" "+this.getAbsstdout()+" "+this.getAbsstderr()+" "+this.getAbspid());
//					String[] args_for_syscall = {"/bin/bash", "/home/avo/bin/procsyscall", call, this.getAbsstdout(), this.getAbsstderr(), this.getAbspid()};
				System.out.println("AUFRUF: processsyscall "+call+" "+this.getAbsstdout()+" "+this.getAbsstderr()+" "+this.getAbspid());
				String[] args_for_syscall = {"processsyscall", call, this.getAbsstdout(), this.getAbsstderr(), this.getAbspid()};
				ProcessBuilder pb = new ProcessBuilder(args_for_syscall);

				// erweitern des PATHs um den prozesseigenen path
				Map<String,String> env = pb.environment();
				String path = env.get("PATH");
				path = this.parent.getPath()+":"+path;
				env.put("PATH", path);
				System.out.println("new PATH: "+path);
				
				java.io.File directory = new java.io.File(this.getAbsdir());
				pb.directory(directory);
				java.io.File checkdirectory = pb.directory().getAbsoluteFile();
				System.out.println("ALS AKTUELLES VERZEICHNIS WIRD GESETZT+GECHECKT: "+checkdirectory);
				java.lang.Process p = pb.start();
//					Map<String,String> env = pb.environment();
//					String cwd = env.get("PWD");
//					String cwd = "/data/prog/workspace/prozesse/dummy/inst";
//					java.io.File directory = new java.io.File(cwd);
//					System.out.println("DIRECTORY SETZEN AUF "+directory.getAbsolutePath());
//					pb.directory(directory);
//					java.lang.Process p = pb.start();
//					java.lang.Process p = Runtime.getRuntime().exec(args_for_syscall);
				System.out.println("PROCESS: "+p.hashCode());
			}			
			catch (Exception e2)
			{
				e2.printStackTrace();
			}
		}
	}
	

	// eine extra methode fuer den step 'root'. es werden alle files/variablen aus 'path' committet
	public void rootcommit() throws IOException
	{
		jlog.log(Level.INFO, "will commit everything in root directory and all directories of process-path");
		// alle verzeichnisse, die committed werden sollen zusammensuchen
		ArrayList<java.io.File> allcommitdirs = this.parent.getInitcommitdirs2();
		
		//ueber alle verzeichnisse iterieren
		Iterator<java.io.File> itercommitdir = allcommitdirs.iterator();
		while (itercommitdir.hasNext())
		{
			java.io.File commitdir = itercommitdir.next();
			this.commitdir(commitdir);
		}

		// alle varfiles, die committed werden sollen zusammensuchen
		ArrayList<java.io.File> allcommitvarfiles = this.parent.getInitcommitvarfiles2();

		//ueber alle varfiles iterieren
		Iterator<java.io.File> itercommitvarfile = allcommitvarfiles.iterator();
		while (itercommitvarfile.hasNext())
		{
			java.io.File commitvarfile = itercommitvarfile.next();
			this.commitvarfile(commitvarfile);
		}
		this.setStatus("finished");
	}
	
	// den inhalt eines ganzen directories in den aktuellen step committen
	public boolean commitdir(java.io.File dir)
	{
		jlog.log(Level.INFO, "will commit directory "+dir.toString());
		jlog.log(Level.INFO, "test whether it is a directory "+dir.toString());
		
		if (dir.isDirectory())
		{
			boolean all_commitfiles_ok = true;
			
			jlog.log(Level.INFO, "it is really a directory");
			ArrayList<java.io.File> allfiles = new ArrayList<java.io.File>(Arrays.asList(dir.listFiles()));
			Iterator<java.io.File> iterfile = allfiles.iterator();
			while (iterfile.hasNext())
			{
				java.io.File file = iterfile.next();
				jlog.log(Level.INFO, "test whether it is a file "+file.toString());
				if (file.isFile())
				{
					jlog.log(Level.INFO, "it is a file");
					if (!(this.commitFile(file)))
					{
						all_commitfiles_ok = false;
					}
				}
				else
				{
					jlog.log(Level.INFO, "it is NOT a file - skipping");
				}
			}
			return all_commitfiles_ok;
		}
		else
		{
			jlog.log(Level.INFO, "it is not a directory - skipping");
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
			newfile.setFilename(file.getName());
			newfile.setAbsfilename(file.getPath());
			this.addFile(newfile);
			jlog.log(Level.INFO, "file committed: "+newfile.getAbsfilename());
			System.out.println("AMOUNT OF FILES ARE NOW: "+this.file.size());
			return true;
		}
		else
		{
			jlog.log(Level.INFO, "file NOT committed (CANT READ!): "+file.getAbsolutePath());
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
		jlog.log(Level.INFO, "variable committed as (name=value): "+variable.getKey()+"="+variable.getValue());
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
							jlog.log(Level.INFO, "variable committed from file: "+file.getAbsolutePath()+" name: "+variable.getKey()+" value: "+variable.getValue());
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
				jlog.log(Level.INFO, "file is to big (>100kB) to commit content as variables: "+file.getAbsolutePath());
				return false;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			jlog.log(Level.INFO, "variables not committed (cannot read file): "+file.getAbsolutePath());
			return false;
		}
	}

	public boolean commitvarfile(String absfilepathdir) throws IOException
	{
		java.io.File file = new java.io.File(absfilepathdir);
		return commitvarfile(file);
	}

	public void commit() throws IOException
	{
		jlog.log(Level.INFO, "will try to commit...");
		this.setStatus("committing");
		jlog.log(Level.INFO, "status is set to "+this.getStatus());

		// wenn es sich um root handelt, wird besonders committed
		if (this.getName().equals(this.parent.getRootstepname()))
		{
			this.rootcommit();
		}
		
		// wenn es sich nicht um root handelt
		else
		{
			boolean success = true;
			// ueber alle commits iterieren
			for( Commit actualCommit : this.commit)
			{
				jlog.log(Level.INFO, "commit name "+actualCommit.getName());
				
				// wenn das zu committende objekt ein File ist...
				for(File actualFile : actualCommit.getFile())
				{
					jlog.log(Level.INFO, "file id "+actualFile.getAbsfilename());
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
						jlog.log(Level.INFO, "commit(file) id NOT successfull");
					}
				}
				
				// wenn das zu committende objekt eine Variable ist...
				for(Variable actualVariable : actualCommit.getVariable())
				{
					if (this.commitvariable(actualVariable.getKey(), actualVariable.getValue()))
					{
						jlog.log(Level.INFO, "commit(variable) id successfull");
					}
					else
					{
						success = false;
						jlog.log(Level.INFO, "commit(variable) id NOT successfull");
					}
				}
				
				actualCommit.setSuccess(success);
			}
			if (this.areAllcommitssuccessfull())
			{
				this.setStatus("finished");
			}
		}
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
	
	public void mkdir(String directory)
	{
		java.io.File dir = new java.io.File(directory);
		// wenn directory existiert, dann die darin befindlichen files loeschen
		if (dir.exists())
		{
			java.io.File[] files = dir.listFiles();
			for(int i=0; i<files.length; i++)
			{
				files[i].delete();
			}
		}
		// ansonsten ein directory anlegen
		else
		{
			dir.mkdir();
		}
		
		// zum schluss testen ob es existiert und beschreibbar ist
//		if ((dir.exists()) && (dir.canWrite()))	{return true;}
//		else {return false;}
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
		Iterator<Init> iterinit = this.getInits().iterator();
		while(iterinit.hasNext())
		{
			// die Namen der vorlaeufersteps feststellen
			Init init = iterinit.next();
			String fromstep = init.getFromstep();
//			System.out.println("fromstep: "+fromstep);

			// und diese steps in einem arraylist aufsammeln
			ArrayList<Step> steps = this.parent.getSteps(fromstep);
			
			// nur die noch nicht als fromstep erkannten steps der suchliste hinzufuegen
			Iterator<Step> iterstep = steps.iterator();
			while (iterstep.hasNext())
			{
				Step newstep = iterstep.next();
				if (!(fromsteps.contains(newstep)))
				{
					fromsteps.add(newstep);
				}
			}
		}
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
		if ( !(this.getLoop().equals("")) )
		{
			isamultistep = true;
		}
		return isamultistep;
	}
	
		
	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
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
		Iterator<Init> iterInit = this.init.iterator();
		while(iterInit.hasNext())
		{
			iterInit.next().setParent(this);
		}
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
		this.work.setParent(this);
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
