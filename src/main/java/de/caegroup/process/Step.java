package de.caegroup.process;

import java.io.*;
import java.util.*;

import org.apache.solr.common.util.NamedList;
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
	private ArrayList<Init> inits = new ArrayList<Init>();
	private ArrayList<Work> works = new ArrayList<Work>();
	private ArrayList<Commit> commits = new ArrayList<Commit>();
	private String loop = new String();
	private String loopvar = new String();

	private Process parent;
//	private String dir = new String();
//	private String absdir = new String();
//	private String abspid = new String();
//	private String absstdout = new String();
//	private String absstderr = new String();
	private NamedList<String> list = new NamedList<String>();
	private ArrayList<File> files = new ArrayList<File>();
	private ArrayList<Variable> variables = new ArrayList<Variable>();
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
		this.parent = p;
	}

	/**
	 * constructs a step with
	 * a new parent
	 * a given name
	 */
	public Step(String stepname)
	{
		this.parent = new Process();
		this.name = stepname;
	}

	/**
	 * constructs a step with
	 * a given parent
	 * a given name
	 */

	public Step(Process p, String stepname)
	{
		this.parent = p;
		this.name = stepname;
	}

	/**
	 * constructs a step with
	 * a new parent
	 * a random name
	 */
	public Step()
	{
		this.parent = new Process();
		this.name = this.genName();
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
		Iterator<Init> iterinit = this.getInits().iterator();
		while (iterinit.hasNext())
		{
			Init init = iterinit.next();
			// die init-angaben in lokale variablen uebernehmen
			String fromobjecttype = init.getFromobjecttype();
			String name = init.getName();
			String returnfield = init.getReturnfield();
			ArrayList<Step> fromsteps = parent.getSteps(init.getFromstep());
			
			Iterator<Step> iterstep = fromsteps.iterator();
			while (iterstep.hasNext())
			{
				Step fromstep = iterstep.next();
				jlog.log(Level.INFO, "init ("+name+") wants the returnfield ("+returnfield+") from a ("+fromobjecttype+") from step ("+fromstep.getName()+")");
				ArrayList<Match> matchs = init.getMatchs();
			
				// wenn es ein file ist
				if (fromobjecttype.equals("file"))
				{
					ArrayList<File> files_from_fromstep = fromstep.getFiles();
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
					Iterator<File> iterfile = files_from_fromstep_which_matched.iterator();
					while (iterfile.hasNext())
					{
						File file = iterfile.next();
						this.list.add(name, file.getField(returnfield));
					}
				}

				// wenn es ein variable ist
				else if (fromobjecttype.equals("variable"))
				{
					ArrayList<Variable> variables_from_fromstep = fromstep.getVariables();
					Iterator<Match> itermatch = matchs.iterator();
					// iteriere ueber matchs
					while (itermatch.hasNext())
					{
						Match match = itermatch.next();
						// iteriere ueber alle Files der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
						Iterator<Variable> itervariable = variables_from_fromstep.iterator();
						while (itervariable.hasNext())
						{
							Variable variable = itervariable.next();
							if (!(variable.match(match)))
							{
								variables_from_fromstep.remove(variable);
							}
						}
					}
					if (variables_from_fromstep.size() == 0) {initializing_success = false;}
					// aus der reduzierten variablen-liste, das gewuenschte field (returnfield) extrahieren und in der initlist unter dem Namen ablegen
					Iterator<Variable> itervariable = variables_from_fromstep.iterator();
					while (itervariable.hasNext())
					{
						Variable variable = itervariable.next();
						this.list.add(name, variable.getField(returnfield));
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
			if (this.getListitems(this.loop).size() > 0)
			{
				Iterator<String> itervalue = this.getListitems(this.loop).iterator();
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

		// ueber alle works iterieren
		Iterator<Work> iterwork = this.getWorks().iterator();
		while (iterwork.hasNext())
		{
			Work work = iterwork.next();
			String call = work.generateCall(this.getListall());

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
					if (!(this.commitfile(file)))
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
	public boolean commitfile(java.io.File file)
	{
		if (file.canRead())
		{
			File newfile = new File();
			newfile.setFilename(file.getName());
			newfile.setAbsfilename(file.getPath());
			this.addFile(newfile);
			jlog.log(Level.INFO, "file committed: "+newfile.getAbsfilename());
			System.out.println("AMOUNT OF FILES ARE NOW: "+this.files.size());
			return true;
		}
		else
		{
			jlog.log(Level.INFO, "file NOT committed (CANT READ!): "+file.getAbsolutePath());
			return false;
		}
	}
	
	public boolean commitfile(String absfilepathdir)
	{
		java.io.File file = new java.io.File(absfilepathdir);
		return commitfile(file);
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
		variable.setName(name);
		variable.setValue(value);
		this.addVariable(variable);
		jlog.log(Level.INFO, "variable committed as (name=value): "+variable.getName()+"="+variable.getValue());
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
							variable.setName(linelist[0]);
							variable.setValue(linelist[1]);
							this.addVariable(variable);
							jlog.log(Level.INFO, "variable committed from file: "+file.getAbsolutePath()+" name: "+variable.getName()+" value: "+variable.getValue());
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
			// ueber alle commits iterieren
			Iterator<Commit> itercommit = this.getCommits().iterator();
			while (itercommit.hasNext())
			{
				Commit commit = itercommit.next();
				String type = commit.getType();
				jlog.log(Level.INFO, "commit id "+commit.getId()+" is a ...");
				
				// wenn das zu committende objekt ein File ist...
				if (type.equals("file"))
				{
					jlog.log(Level.INFO, "commit id "+commit.getId()+" is a "+type+" "+this.getAbsdir()+"/"+commit.getFilename());
					java.io.File fsfile = new java.io.File(this.getAbsdir()+"/"+commit.getFilename());
					if (this.commitfile(fsfile))
					{
						commit.setSuccess(true);
						jlog.log(Level.INFO, "commit(file) id successfull");

						// wenn das File auch dem prozess committed werden soll, dann soll es ins instanzverzeichnis kopiert werden
						if (commit.getToroot())
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
						jlog.log(Level.INFO, "commit(file) id NOT successfull");
					}
				}
				
				// wenn das zu committende objekt eine Variable ist...
				else if (type.equals("variable"))
				{
					jlog.log(Level.INFO, "commit id "+commit.getId()+" is a "+type);
					this.commitvariable(commit.getName(), commit.getValue());
					commit.setSuccess(true);
					jlog.log(Level.INFO, "commit(variable) id successfull");
				}
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
		this.inits.add(init);
	}

	public void addWork(Work work)
	{
		this.works.add(work);
	}

	public void addCommit(Commit commit)
	{
		this.commits.add(commit);
	}

	public void addFile(File file)
	{
		this.files.add(file);
//		System.out.println("NOW FILES AMOUNT: "+this.files.size());
	}

	public void addVariable(Variable variable)
	{
		this.variables.add(variable);
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
		for(int i=0; i<inits.size(); i++)
		{
			Init init = inits.get(i);
			if (init.getName() == initname)
			{
				return init;
			}
		}
		return null;
	}
	
	public ArrayList<Init> getInits()
	{
		return this.inits;
	}

	public Init[] getInits2()
	{
		Init[] inits = new Init[this.inits.size()];
		for (int i=0; i<this.inits.size(); i++)
		{
			inits[i] = this.inits.get(i);
		}
		return inits;
	}

	public String[] getInitnames()
	{
		String[] initnames = new String[this.inits.size()];
		for (int i=0; i<this.inits.size(); i++)
		{
			initnames[i] = this.inits.get(i).getName(); 
		}
		return initnames;
	}
	
	public ArrayList<Work> getWorks()
	{
		return this.works;
	}

	public Work[] getWorks2()
	{
		Work[] works = new Work[this.works.size()];
		for (int i=0; i<this.works.size(); i++)
		{
			works[i] = this.works.get(i);
		}
		return works;
	}

	public ArrayList<Commit> getCommits()
	{
		return this.commits;
	}

	public Commit[] getCommits2()
	{
		Commit[] commits = new Commit[this.commits.size()];
		for (int i=0; i<this.commits.size(); i++)
		{
			commits[i] = this.commits.get(i);
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

	public NamedList<String> getListall()
	{
		return this.list;
	}

	public ArrayList<String> getListnames()
	{
		ArrayList<String> listnames = new ArrayList<String>();
		for(int i=0; i<this.list.size(); i++)
		{
			if(!(listnames.contains(this.list.getName(i))))
			{
				listnames.add(this.list.getName(i));
			}
		}
		return listnames;
	}

	public ArrayList<String> getListitems(String listname)
	{
		ArrayList<String> listitems = new ArrayList<String>();
		for(int i=0; i<this.list.size(); i++)
		{
			if (this.list.getName(i).equals(listname))
			{
				listitems.add(this.list.getVal(i));
			}
		}
		return listitems;
	}

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
			Process process = this.parent;
			// und diese steps in einem arraylist aufsammeln
			ArrayList<Step> steps = process.getSteps(fromstep);
			
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

	public ArrayList<File> getFiles()
	{
		return this.files;
	}
		
	public ArrayList<Variable> getVariables()
	{
		return this.variables;
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

	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public void setParent(Process process)
	{
		this.parent = process;
	}
}
