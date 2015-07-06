package de.caegroup.process;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardCopyOption.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.SerializationUtils;

//import org.apache.solr.common.util.NamedList;

public class Commit
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private static final CopyOption REPLACE_EXISTING = null;
	private String name = "";
	private String toroot = null;
	private String refactor = null;
	private String copyto = null;
	private String loop = "";
	private String loopvar = "";
	private ArrayList<Variable> variable = new ArrayList<Variable>();
	private ArrayList<File> file = new ArrayList<File>();
	
//	private String status = "waiting";	// waiting/committing/finished/error/cancelled

	private java.lang.Process proc;
	private Step parent;

	private ArrayList<Log> log = new ArrayList<Log>();
	
	// im jungfraeulichen zustand ist es null
	// eine leere loopedCommits gibt es dann, wenn ein loop 0 eintraege enthaelt
	private ArrayList<Commit> loopedCommits = null;
	private String statusOverwrite = null;
	/*----------------------------
	  constructors
	----------------------------*/
	public Commit()
	{
		parent = new Step();
	}

	public Commit(Step s)
	{
		parent = s;
		s.addCommit(this);
	}

	
	/*----------------------------
	  methods
	----------------------------*/

	/**
	 * clone
	 * returns a clone of this
	 * @return Commit
	 */
	@Override
	public Commit clone()
	{
		return SerializationUtils.clone(this);
	}
	
	/*----------------------------
	  methods
	----------------------------*/

	public void addFile(File file)
	{
		this.file.add(file);
	}

	public void addVariable(Variable variable)
	{
		this.variable.add(variable);
	}
	
	/**
	 * reset this
	 * 1) alle files:
	 *     log leeren
	 *     status auf "" setzen
	 * 2) alle variablen:
	 *    log leeren
	 *    den status auf "" setzen
	 * 3) log leeren
	 */
	public void reset()
	{
		for(File actFile : this.getFile())
		{
			actFile.getLog().clear();
			actFile.setStatus("");
		}
		for(Variable actVariable : this.getVariable())
		{
			actVariable.getLog().clear();
			actVariable.setStatus("");
		}
		this.loopedCommits = null;
		this.getLog().clear();
	}
	
	/**
	 * resolve
	 * resolves all the entries in the attributes
	 */
	public void resolve()
	{
		// den eintrag im attribut 'interpreter' resolven
		if(this.getCopyto() != null)
		{
			this.setCopyto(this.getCopyto().replaceAll("\\{\\$loopvarstep\\}", this.getParent().getLoopvar()));
			this.setCopyto(this.getCopyto().replaceAll("\\{\\$loopvarcommit\\}", this.getLoopvar()));
			this.setCopyto(this.getParent().resolveString(this.getCopyto()));
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

	public void printLog()
	{
		for(Log actLog : this.getLog())
		{
			actLog.print();
		}
	}
	
	/*----------------------------
	  methods getter/setter
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getToroot()
	{
		return this.toroot;
	}

	public boolean isTorootPresent()
	{
		if(this.toroot == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public void setToroot(String toroot)
	{
		this.toroot = toroot;
	}

	public String getLoop()
	{
		return this.loop;
	}
	
	public void setLoop(String loop)
	{
		this.loop = loop;
	}
	
	public String getLoopvar()
	{
		return this.loopvar;
	}

	public void setLoopvar(String loopvar)
	{
		this.loopvar = loopvar;
	}

	public ArrayList<Variable> getVariable()
	{
		return this.variable;
	}

	/**
	 * es werden alle Variables zurueckgegeben, die einen bestimmten schluessel haben
	 * @param key
	 * @return
	 */
	public ArrayList<Variable> getVariable(String key)
	{
		ArrayList<Variable> VariableWithCertainKey = new ArrayList<Variable>();
		for(Variable actVariable : this.variable)
		{
			if(actVariable.getKey().equals(key))
			{
				VariableWithCertainKey.add(actVariable);
			}
		}
		return VariableWithCertainKey;
	}

	public void setVariable(ArrayList<Variable> variable)
	{
		this.variable = variable;
	}

	public ArrayList<File> getFile()
	{
		return this.file;
	}

	/**
	 * es werden alle Files zurueckgegeben, die einen bestimmten schluessel haben
	 * @param key
	 * @return
	 */
	public ArrayList<File> getFile(String key)
	{
		ArrayList<File> FileWithCertainKey = new ArrayList<File>();
		for(File actFile : this.file)
		{
			if(actFile.getKey().equals(key))
			{
				FileWithCertainKey.add(actFile);
			}
		}
		return FileWithCertainKey;
	}

	public void setFile(ArrayList<File> file)
	{
		this.file = file;
	}

	public java.lang.Process getProc()
	{
		return this.proc;
	}
	
	public void setProc(java.lang.Process proc)
	{
		this.proc = proc;
	}

	public Step getParent()
	{
		return this.parent;
	}
	
	/**
	 * @param step the parent to set
	 */
	public void setParent(Step step)
	{
		this.parent = step;
	}

	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	public ArrayList<Log> getLogRecursive()
	{
		ArrayList<Log> logRecursive = this.log;

		// alle logs der geloopten commits hinzufuegen
		if(this.loopedCommits != null)
		{
			for(Commit actCommit : this.loopedCommits)
			{
				logRecursive.addAll(actCommit.getLogRecursive());
			}
		}
		
		// alle logs aller Variablen hinzufuegen
		for(Variable actVariable : this.variable)
		{
			logRecursive.addAll(actVariable.getLog());
		}
		// alle logs aller Files hinzufuegen
		for(File actFile : this.file)
		{
			//System.err.println("actual Step: "+this.getParent().getName()+" | actual Commit: "+this.getName()+" | actual File is: "+actFile.getKey()+" | size of File-log: "+actFile.getLog().size());
			logRecursive.addAll(actFile.getLog());
		}

		// sortieren nach Datum
		Collections.sort(logRecursive);

		return logRecursive;
	}

	public String getStatus()
	{
		if(this.statusOverwrite != null)
		{
			return this.statusOverwrite;
		}
		
		// wenn es ein loopedCommit ist, dann den status deropedCommits abfragen um den status von this bestimmen zu koennen
		if(this.loopedCommits != null)
		{
			String status = "unknown";
			Map<String,Integer> statusAnzahl = new HashMap<String,Integer>();

			for(Commit actLoopedCommit : this.loopedCommits)
			{
				String statusActLoopedCommit = actLoopedCommit.getStatus();
				// wenn es den status in dem map bereits gibt, soll der entsprechende zaehler erhoeht werden
				if(statusAnzahl.containsKey(statusActLoopedCommit))
				{
					statusAnzahl.put(statusActLoopedCommit, statusAnzahl.get(statusActLoopedCommit));
				}
				// wenn es den status noch nicht gibt, dann anlegen mit zaehler 0
				else
				{
					statusAnzahl.put(statusActLoopedCommit, 0);
				}
			}
			
			// aus den einzelstati der loopedCommit den endstatus bestimmen
			if(statusAnzahl.containsKey("error"))
			{
				return "error";
			}
			else if(statusAnzahl.containsKey("waiting"))
			{
				return "waiting";
			}
			else if(statusAnzahl.containsKey("finished"))
			{
				return "finished";
			}
			else
			{
				return "unknown";
			}
		}
		
		// wenn es keine looped Commits gibt
		else if(this.loopedCommits == null)
		{
			String status = "unknown";
	
			ArrayList<String> statusAllFilesVariables = new ArrayList<String>();
	
			for(File actFile : this.getFile())
			{
				statusAllFilesVariables.add(actFile.getStatus());
	//			System.err.println("status of commit "+this.getName()+": file "+actFile.getKey()+": "+actFile.getStatus());
			}
			for(Variable actVariable : this.getVariable())
			{
				statusAllFilesVariables.add(actVariable.getStatus());
	//			System.err.println("status of commit "+this.getName()+": variable "+actVariable.getKey()+": "+actVariable.getStatus());
			}
	
			// ist der status 'error' vorhanden? prozess=error
			if(statusAllFilesVariables.contains("error"))
			{
				status = "error";
				return status;
			}
	
			// wenn schluessel waiting nicht vorhanden sind, und nur finished vorhanden ist, dann ist commit finished
			else if(  statusAllFilesVariables.contains("waiting") )
			{
				status = "waiting";
				return status;
			}
	
			// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist prozess finished
			else if(  statusAllFilesVariables.contains("finished") )
			{
				status = "finished";
				return status;
			}
		}
		return "unknown";
	}

//	public void setStatus(String status)
//	{
//		log("info", "setting status to '"+status+"'");
//		this.status = status;
//	}

	public String getAbsdir()
	{
		return this.getParent().getAbsdir();
	}

	
	/**
	 * @return the copyto
	 */
	public String getCopyto() {
		return copyto;
	}

	/**
	 * @param copyto the copyto to set
	 */
	public void setCopyto(String copyto) {
		this.copyto = copyto;
	}

	/*----------------------------
	methods
	----------------------------*/
	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("commit "+this.getName(), loglevel, logmessage));
	}

//	// den inhalt eines ganzen directories in den aktuellen step committen
//	public void commitdir(java.io.File dir)
//	{
//		this.log("info", "will commit directory "+dir.toString());
//		this.log("info", "test whether it is a directory "+dir.toString());
//		
//		if (dir.isDirectory())
//		{
//			boolean all_commitfiles_ok = true;
//			
//			this.log("info", "it is really a directory");
//			ArrayList<java.io.File> allfiles = new ArrayList<java.io.File>(Arrays.asList(dir.listFiles()));
//			
//			for(java.io.File actFile : allfiles)
//			{
//				this.log("info", "test whether it is a file "+file.toString());
//				if (actFile.isFile())
//				{
//					this.log("info", "it is a file");
//					this.commitFile("default", actFile);
//				}
//				else
//				{
//					this.log("info", "it is NOT a file - skipping");
//				}
//			}
//		}
//		else
//		{
//			this.log("info", "it is not a directory - skipping");
//			this.setStatus("error");
//		}
//	}

//	// ein file in den aktuellen step committen
//	public void commitFile(String key, java.io.File file)
//	{
//		if (file.canRead())
//		{
//			File newfile = new File();
//			newfile.setAbsfilename(file.getPath());
//			newfile.setKey(key);
//			this.commitFile(newfile);
//		}
//		else
//		{
//			this.log("error", "file NOT committed (CANT READ!): "+file.getAbsolutePath());
//			setStatus("error");
//		}
//	}

//	public void commitFile(String key, String absfilepathdir)
//	{
//		java.io.File file = new java.io.File(absfilepathdir);
//		commitFile(key, file);
//	}

	/**
	 * commit von files
	 * 1) globben
	 * 2) falls step=root:
	 * 	2a) falls die files noch nicht im step-verzeichnis liegen, sollen sie dorthin kopiert werden
	 * 3) falls step != root
	 * 	3a) falls die files nicht im stepverzeichnis liegen => error
	 *	4) fuer jedes file: clone von master file, setzen des AbsPath
	 * 5) ueberpruefen minoccur/maxoccur
	 * 6) durchfuehren evtl. vorhandener tests
	 * 7) hinzufuegen zu step
	 * @param File
	*/	
	public void commitFile(File master)
	{
		log("info", "want to commit the file(s) (key=" +master.getKey()+")");
//		System.out.println("info: want to commit the file(s) (key=" +master.getKey()+")");

		ArrayList<File> filesToCommit = new ArrayList<File>();

		// wenn das file bereits einen absdir hat und das file existiert, dann muss dies nicht ueber globbing ermittelt werden
		if(new java.io.File(master.getAbsfilename()).exists())
		{
			log("debug", "file has already a absfilename, so no need to glob");
			filesToCommit.add(master);
		}

		// ansonsten muss mit dem glob festgestellt werden welche files gemeint sind
		// das Verzeichnis des Steps
		else if((master.getGlob()!=null) && (!master.getGlob().equals("")))
		{
			log("info", "file does not have a absfilename, so i need to glob it");

			// zuerst evtl. vorh. loopvar fuer {$loopvarcommit} einsetzen
			String resolvedGlob = master.getGlob();
			if(!this.getLoopvar().equals(""))
			{
				log("debug", "resolving glob internally in commit-object '"+master.getGlob()+"' to '"+resolvedGlob+"'");
				resolvedGlob = resolvedGlob.replaceAll("\\{\\$loopvarcommit\\}", this.getLoopvar());
			}
			
			// resolven des globeintrages
			resolvedGlob = this.getParent().resolveString(resolvedGlob);
			log("info", "resolving glob '"+master.getGlob()+"' to '"+resolvedGlob+"'");

			// ist der glob relativ?, dann den pfad um das stepdir erweitern
			java.io.File stepDir = new java.io.File(this.getAbsdir());
			if(!(resolvedGlob.matches("^/.+$")))
			{
				try
				{
					// wenn globDir == null ist, soll im stepDir geglobbed werden
					if(master.getGlobdir() == null)
					{
						resolvedGlob = stepDir.getCanonicalPath() +"/"+ resolvedGlob;
					}
					// ansonsten in dem explicit angegebenen globdir
					else
					{
						resolvedGlob = master.getGlobdir() +"/"+ resolvedGlob;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					log("error", e.getMessage());
				}
			}
			
			// verzeichnis des globs feststellen (entweder dass stepdir oder der pfadanteil des absoluten globs)
			java.io.File dirOfGlob = null;
			try
			{
				dirOfGlob = new java.io.File(resolvedGlob).getParentFile().getCanonicalFile();
				java.io.File fileResolvedGlob = new java.io.File(resolvedGlob);
				// evtl. wurden nicht vorhandene obsolete pfadbestandteile entfernt, deshalb soll der resolvedGlob aktualisiert werden
				resolvedGlob = dirOfGlob.getAbsolutePath() + "/" + fileResolvedGlob.getName() ;
			}
			catch (IOException e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			// logging
			log("debug", "directory to glob for files: "+dirOfGlob.getAbsolutePath());
			
			// alle eintraege des glob-directories feststellen
			java.io.File[] allEntriesOfDirectory = dirOfGlob.listFiles();
			if(allEntriesOfDirectory == null)
			{
				log("info", "glob-directory is empty: "+dirOfGlob.getAbsolutePath());
			}
			else
			{
				log("info", allEntriesOfDirectory.length+" entries in glob-directory "+dirOfGlob.getAbsolutePath() + " " + Arrays.toString(allEntriesOfDirectory));
	
				// nur die files des verzeichnisses
				ArrayList<java.io.File> allFilesOfDirectory = new ArrayList<java.io.File>();
				for(java.io.File actFile : allEntriesOfDirectory)
				{
					if(!actFile.isDirectory())
					{
						allFilesOfDirectory.add(actFile);
					}
				}
				// interpolieren aller files in einen String fuer die logging ausgabe
				String allFiles = "[";
				for(java.io.File actFile : allFilesOfDirectory)
				{
					allFiles += actFile.getAbsolutePath() +", ";
				}
				allFiles = allFiles.substring(0, allFiles.length()-3);
				allFiles += "]";
				
				log("info", allFilesOfDirectory.size()+" files in directory "+dirOfGlob.getAbsolutePath() + " " + allFiles);

				// nur die files auf die der glob passt
				log("info", "globbing: "+resolvedGlob);
				PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+resolvedGlob);
				ArrayList<java.io.File> allFilesThatGlob = new ArrayList<java.io.File>();
				for(java.io.File actFile :allFilesOfDirectory)
				{
					if(matcher.matches(actFile.toPath()))
					{
						allFilesThatGlob.add(actFile);
						log("debug", "glob DOES match: "+actFile.getAbsolutePath());
					}
					else
					{
						log("debug", "glob DOES NOT match: "+actFile.getAbsolutePath());
					}
				}
	
				// files aus den java.io.files generieren und jedes mal das vorliegende file clonen
				for(java.io.File actFile : allFilesThatGlob)
				{
					File clonedFile = master.clone();
					clonedFile.setGlob("");
					
					// evtl. vorhandenes refactor durchfuehren
					if(this.getRefactor() != null)
					{
						this.log("debug", "the object will be refactored just before the commit");
						String[] arg = this.getRefactor().split(":");
						// bei arg1==filename z.B. refactor="filename:effective_force.txt"
						if( (arg.length == 2) && (arg[0].equals("filename")) )
						{
							this.log("debug", "refactor=\""+ this.getRefactor()+"\" means the file will be copied with a new name before the commit");
							// das file kopieren
							// 0.5) den filenamen feststellen
							String newFilename = this.getParent().resolveString(arg[1]);
							// 1) das feld realposition auf das existierende file setzen
							clonedFile.setRealposition(actFile.getAbsolutePath());
							// 2) und den filenamen aendern
							this.log("debug", "setting new filename to "+newFilename);
							clonedFile.setFilename(newFilename);
							// 3) kopieren durchfuehren (realposition -> absfilename) [[ absfilename wird dynamisch ermittelt ueber die stepdir ]]
//							clonedFile.copyIfNeeded();
						}
					}
					// falls kein refactor existiert
					else
					{
						clonedFile.setRealposition(actFile.getAbsolutePath());
					}
					filesToCommit.add(clonedFile);
				}
			}
		}

		// DIE ERMITTELTEN FILES EVTL. KOPIEREN UND UEBERPRUEFEN

		// ueberpruefen ob Anzahl der ermittelten Variablen mit minoccur und maxoccur zusammen passt
		if(filesToCommit.size() < master.getMinoccur())
		{
			log("error", "amount of determined files is "+filesToCommit.size()+". this is lower than the needed "+master.getMinoccur()+" (minoccur)");
			master.setStatus("error");
		}
		else if (filesToCommit.size() > master.getMaxoccur())
		{
			log("error", "amount of determined files is "+filesToCommit.size()+". this is more than the maximum allowed amount "+master.getMaxoccur()+" (maxoccur)");
			master.setStatus("error");
		}
		else
		{
			log("info", "amount of determined files is "+filesToCommit.size()+". (minoccur="+master.getMinoccur()+", maxoccur="+master.getMaxoccur()+")");
		}

		// durchfuehren evtl. definierter tests
		for(File actFile : filesToCommit)
		{
			actFile.performAllTests();
			if(actFile.doAllTestsPass())
			{
				log("info", "file "+actFile.getKey()+": all tests passed successfully ("+actFile.getAllTestsFeedback()+")");
			}
			else
			{
				log("error", "file "+actFile.getKey()+": tests failed ("+actFile.getAllTestsFeedback()+")");
				master.setStatus("error");
			}
		}

		// wenn alles gut gegangen ist bisher, dann committen
		if(! master.getStatus().equals("error"))
		{
			log("debug", "adding "+filesToCommit.size()+" file(s) to step "+this.getParent().getName());

			// falls toRoot != null, soll bei den variablen die category auf denselben wert gesetzt werden
			for(File actFile : filesToCommit)
			{
			}
			
			// jedes file einzeln dem step hinzufuegen und sofort danach den 'key' und 'value' resolven
			for(File actFile : filesToCommit)
			{
				// wenn commit im root-Step ist, soll die category des files auf processInput gesetzt werden
				if(this.getParent().isRoot())
				{
					if(this.isTorootPresent())
					{
						actFile.setCategory("processInput"+"/"+this.getToroot());
					}
					else
					{
						actFile.setCategory("processInput");
					}
				}
				
				// committen
				log("debug", "committen des files in den step "+this.getParent().getName());
				this.getParent().addFile(actFile);
				log("debug", "dauerhaftes resolven des gerade committeten files von key "+actFile.getKey()+"->"+this.getParent().resolveString(actFile.getKey()));
				actFile.setKey(this.getParent().resolveString(actFile.getKey()));
				log("debug", "file: ("+actFile.getKey()+"=>"+actFile.getAbsfilename()+")");

				// soll auch 'toroot' committed werden? dann soll das file geklont und root committed werden mit entsprechend angegebener category
				if(!this.getParent().isRoot())
				{
					log("debug", "step of this commit is not root. is there a toRoot entry in actual commit?");
					if(this.isTorootPresent())
					{
						log("debug", "yes there is a toRoot entry in actual commit");
						File clonedFileToRoot = actFile.clone();
						clonedFileToRoot.setCategory("processOutput"+"/"+this.getToroot());
						log("debug", "adding file to root-Step because commit contains toRoot-instruction");
						this.getParent().getParent().getRootStep().addFile(clonedFileToRoot);
					}
					else
					{
						log("debug", "no there is NO toRoot entry in actual commit");
					}
				}
			
			}
			
			// und den master auf finished setzen, denn dieser ist in this abgelegt und wird beim ermitteln des status abgefragt
			master.setStatus("finished");
		}
	}

//	/**
//	 * commit einer variable aus zwei strings (name, value)
//	 * @input key, value
//	*/	
//	public void commitVariable(String key, String value)
//	{
//		Variable variable = new Variable();
//		variable.setKey(key);
//		variable.setValue(value);
//		this.commitVariable(variable);
//	}

	/**
	 * commit einer variable
	 * 1) globben
	 * 2) aus den geglobbten files die values extrahieren
	 * 3) fuer jeden value eine variable an step committen
	 * @input variable
	*/	
	public void commitVariable(Variable master)
	{
		log("info", "want to commit the variable(s) (key=" +master.getKey()+")");
//		System.out.println("info: want to commit the variable(s) (key=" +master.getKey()+")");

		ArrayList<Variable> variablesToCommit = new ArrayList<Variable>();

		// wenn die variable bereits einen value hat, dann muss dies nicht ueber globbing ermittelt werden
		log("info", "(value=" +master.getValue()+")");
		if((master.getValue()!=null) && (!master.getValue().equals("")))
		{
			master.setValue(this.getParent().resolveString(master.getValue()));
			log("info", "(value=" +this.getParent().resolveString(master.getValue())+")");
			variablesToCommit.add(master);
		}

		// ansonsten muss mit dem glob festgestellt werden welche files gemeint sind
		else if((master.getValue()==null) && ( master.getGlob()!=null && (!master.getGlob().equals(""))) )
		{
			log("debug", "variable does define a glob instead of a value. so the content of files / directories "+master.getGlob()+" have to be interpreted as a variables.");

			// den glob zu einem absoluten glob ueberfuehren ohne den eintrag im master zu veraendern (muss fuer evtl. reset unveraendert bleiben)
			String globAbsolute = "";
			// ist der glob relativ? Dann muss er um das stepdir erweitert werden
			if(!(master.getGlob().matches("^/.+$")))
			{
//				master.setGlob(this.getAbsdir()+"/"+master.getGlob());
				globAbsolute = this.getAbsdir()+"/"+master.getGlob();
				log("debug", "glob is relative - expanding with stepdir "+globAbsolute);
			}
			else
			{
				globAbsolute = master.getGlob();
			}

			// das Verzeichnis des globs feststellen (der glob selber koennte auch ein dir sein, aber das spielt vorerst keine rolle)
			java.io.File dirOfGlob = null;

			String glob = globAbsolute;
			String globParent = globAbsolute.replaceFirst("/[^/]+$", "");

			System.err.println("globabsolute: "+globAbsolute);
			
			// directory festlegen
			if(new java.io.File(glob).isDirectory())
			{
				dirOfGlob = new java.io.File(glob);
			}
			else if(new java.io.File(globParent).isDirectory())
			{
				dirOfGlob = new java.io.File(globParent);
			}

//			System.err.println("dir of glob "+dirOfGlob);
//			System.err.println("glob "+glob);

			// alle eintraege des Verzeichnisses
			ArrayList<java.io.File> allFilesOfDirectory = new ArrayList<java.io.File>();
			java.io.File[] allEntriesOfDirectory = dirOfGlob.listFiles();
			for(java.io.File actFile : allEntriesOfDirectory)
			{
				if(!actFile.isDirectory())
				{
					allFilesOfDirectory.add(actFile);
				}
			}
			log("debug", allFilesOfDirectory.size()+" files in directory "+dirOfGlob);

			// alle eintraege des verzeichnisses, auf die der glob matched
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+globAbsolute);
			ArrayList<java.io.File> allEntitiesThatGlob = new ArrayList<java.io.File>();
			for(java.io.File actFile : allFilesOfDirectory)
			{
				if(matcher.matches(actFile.toPath()))
				{
					allEntitiesThatGlob.add(actFile);
					log("info", "glob matches: "+actFile.getAbsolutePath());
				}
				else
				{
					log("info", "glob NOT matches: "+actFile.getAbsolutePath());
				}
			}
			
			// die geglobbten eintraege, die directories sind durch deren enthaltene files ersetzen
			ArrayList<java.io.File> allFilesThatContainVariables = allEntitiesThatGlob;
			boolean enthaeltNochDirs = true;
			
			// solange aufloesen bis nur noch files vorhanden sind
			while(enthaeltNochDirs)
			{
				ArrayList<java.io.File> tmp = new ArrayList<java.io.File>();
				for(java.io.File actFile : allFilesThatContainVariables)
				{
					// wenn es ein file ist, wird es uebernommen
					if(actFile.isFile())
					{
						tmp.add(actFile);
					}
					// wenn es ein directory ist, wird dessen inhalt uebernommen
					else
					{
						tmp.addAll(Arrays.asList(actFile.listFiles()));
					}
				}
				allFilesThatContainVariables = tmp;
				
				// sind noch directories vorhanden, die aufgeloest werden muessen?
				enthaeltNochDirs = false;
				for(java.io.File actFile : allFilesThatContainVariables)
				{
					if(actFile.isDirectory())
					{
						enthaeltNochDirs = true;
					}
				}
			}

			// interpolieren aller files in einen String fuer die logging ausgabe
			String allFiles = "[";
			for(java.io.File actFile : allFilesThatContainVariables)
			{
				allFiles += actFile.getAbsolutePath() +", ";
			}
			allFiles += "]";

			log("debug", "and "+allFilesThatContainVariables.size()+" of this file(s) match glob (files: "+allFiles+")");

			// variablen aus den files generieren und jedes mal die vorliegende variable clonen
			for(java.io.File actFile : allFilesThatContainVariables)
			{
				variablesToCommit.addAll(extractVariables(master, actFile));
			}
		}

		// ueberpruefen ob Anzahl der ermittelten Variablen mit minoccur und maxoccur zusammen passt
		if(variablesToCommit.size() < master.getMinoccur())
		{
			log("error", "amount of determined variables is "+variablesToCommit.size()+". this is lower than the needed "+master.getMinoccur()+" (minoccur)");
			master.setStatus("error");
		}
		else if (variablesToCommit.size() > master.getMaxoccur())
		{
			log("error", "amount of determined variables is "+variablesToCommit.size()+". this is more than the maximum allowed amount "+master.getMaxoccur()+" (maxoccur)");
			master.setStatus("error");
		}
		else
		{
			log("info", "amount of determined variables is "+variablesToCommit.size()+". (minoccur="+master.getMinoccur()+", maxoccur="+master.getMaxoccur()+")");
		}

		// durchfuehren evtl. definierter tests
		for(Variable actVariable : variablesToCommit)
		{
			actVariable.performAllTests();
			if(actVariable.doAllTestsPass())
			{
				log("info", "all tests passed successfully ("+actVariable.getAllTestsFeedback()+")");
			}
			else
			{
				log("error", "tests failed ("+actVariable.getAllTestsFeedback()+")");
				master.setStatus("error");
			}
		}

		// wenn alles gut gegangen ist bisher, dann committen
		if(! master.getStatus().equals("error"))
		{
			log("debug", "adding "+variablesToCommit.size()+" variable(s) to step "+this.getParent().getName());
			
			// jede variable einzeln dem step hinzufuegen und sofort danach den 'key' und 'value' resolven
			for(Variable actVar : variablesToCommit)
			{
				log("debug", "committen der variable in den step "+this.getParent().getName());
				this.getParent().addVariable(actVar);
				log("debug", "dauerhaftes resolven des gerade committeten variable von key "+actVar.getKey()+"->"+this.getParent().resolveString(actVar.getKey()));
				actVar.setKey(this.getParent().resolveString(actVar.getKey()));
				log("debug", "dauerhaftes resolven des gerade committeten variable von value "+actVar.getValue()+"->"+this.getParent().resolveString(actVar.getValue()));
				actVar.setValue(this.getParent().resolveString(actVar.getValue()));
				log("debug", "variable: ("+actVar.getKey()+"=>"+actVar.getValue()+")");
			}
			
			// falls toRoot != null, soll bei den variablen die category auf denselben wert gesetzt werden
			for(Variable actVar : variablesToCommit)
			{
				// im rootstep soll alles nach processInput
				if(this.getParent().isRoot())
				{
					actVar.setCategory("processInput");
				}
				// bei anderen steps soll bei toRoot ins Verzeichnis processOutput geschickt werden
				else
				{
					if(this.isTorootPresent())
					{
						actVar.setCategory("processOutput"+"/"+this.getToroot());
					}
//					else
//					{
//						actVar.setCategory("processOutput");
//					}
				}
			}
			
			// soll auch 'toroot' committed werden? Dann alle Variablen dem RootStep hinzufuegen (mit bereits resolvten Key/Value-Eintraegen)
			if(this.isTorootPresent() && !this.getParent().isRoot())
			{
				Step rootStep = this.getParent().getParent().getRootStep();
				log("debug", "adding "+variablesToCommit.size()+" file(s) to rootStep");
				rootStep.addVariable(variablesToCommit);

				//  variable in root auch in eine textdatei schreiben
				for(Variable actVar : variablesToCommit)
				{
					FileWriter writer;
					try
					{
						java.io.File fileForVariableValue = new java.io.File(rootStep.getAbsdir() + "/" + actVar.getCategory() + "/variable."+actVar.getKey());
						java.io.File directoryOfFileForVariableValue = fileForVariableValue.getParentFile();
						directoryOfFileForVariableValue.mkdirs();
						writer = new FileWriter(fileForVariableValue.getAbsolutePath());
						writer.write(actVar.getValue()+"\n");
						writer.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						log("error", e.getMessage());
						master.setStatus("error");
						return;
					}
				}
			}
			else if(this.isTorootPresent() && this.getParent().isRoot())
			{
				log("debug", "commit enthaelt toroot=true aber aktueller step ist bereits der rootStep -> verzicht auf erneutes commit to root");
			}
			master.setStatus("finished");
		}
	}

	/**
	 * Eine Variable wird aus einem File extrahiert
	 * 
	 * 1) ist ein file mit dem namen 'variables' vorhanden, soll der inhalt nach key=value definitionen durchgegangen werden (#=kommentar, leerzeilen ignorieren, value < 100kB)
	 *	2) bei files mit dem namens muster variable.<key> wird jede zeile vollstaendig als eigene variable interpretiert mit value=zeileninhalt (#=kommentar, leerzeilen ignorieren, value < 100kB)
	 * 3) bei allen anderen filenamen, soll der vollstaendige inhalt als value interpretiert werden
     *
	 * Ein clone der Variable wird mit einem Value bestueckt und als Teil der ArrayList zurueck gegeben.
	 *
	 * @param Variable, java.io.File, boolean
	 * @throws IOException 
	*/
	public ArrayList<Variable> extractVariables(Variable master, java.io.File fileToExtractFrom)
	{
		ArrayList<Variable> extractedVariables = new ArrayList<Variable>();

		// ueber den filenamen feststellen welche extraktionsmethode angewendet werden soll
		// methode 1)
		if(fileToExtractFrom.getName().matches("^variables$"))
		{
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(fileToExtractFrom)));
				String line = null;
				int lineNr = 0;
				while ((line = in.readLine()) != null)
				{
					lineNr++;
					// wenn zeile nicht mit '#' beginnt oder leer ist, oder nur whitespaces enthaelt, soll diese als variable betrachtet werden
					if( !line.matches("^#.+$") && !line.matches("^\\s*$") )
					{
						// entfernen von evtl. vorhandenen newlines
						line = line.replaceAll("(\\r|\\n)", "");
						
						log("debug", "extracting variable from file '"+fileToExtractFrom.getAbsolutePath()+"' line-nr "+lineNr);

						// am ersten '=' zerlegen
						String[] keyValue = line.split("=", 2);

						String key = "";
						String value = "";
						if(keyValue.length == 1)
						{
							key = keyValue[0];
							value = keyValue[0];
						}
						else if(keyValue.length > 1)
						{
							key = keyValue[0];
							value = keyValue[1];
						}

						// zeile zu lang?
						if(key.length() > 102400 || value.length() > 102400)
						{
							log("error", "cannot import as variable. line nr "+lineNr+" contains to much data (key or value >100kB).");
						}
						else
						{
							Variable newVariable = master.clone();
							newVariable.setGlob(null);
							newVariable.setKey(key);
							newVariable.setValue(value);
							extractedVariables.add(newVariable);
						}
					}
				}
			}
			catch (IOException e)
			{
				this.log("error", e.getMessage());
				master.setStatus("error");
			}
			finally
			{
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					this.log("error", e.getMessage());
					master.setStatus("error");
				}
			}

		}
		// methode 2)
		else if(fileToExtractFrom.getName().matches("^variable\\..+$"))
		{
			// den key aus dem filenamen extrahieren (variable.<key>)
			Pattern p = Pattern.compile("^variable\\.(.+)$", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(fileToExtractFrom.getName());
			
			String key = "";

			if(m.find())
			{
				key = m.group(1);
			}
			else
			{
				key = "default";
			}
			
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(fileToExtractFrom)));
				String line = null;
				int lineNr = 0;
				while ((line = in.readLine()) != null)
				{
					lineNr++;
					// wenn zeile mit nicht '#' beginnt oder leer ist, oder nur whitespaces enthaelt, soll diese als variable betrachtet werden
					if( !line.matches("^#.+$") && !line.matches("^\\s*$") )
					{
						// entfernen einen evtl. vorhandenen newlines
						line = line.replaceAll("(\\r|\\n)", "");
						
						log("debug", "extracting value '"+line+"' from file "+fileToExtractFrom.getAbsolutePath());

						// zeile zu lang?
						if(line.length() > 102400)
						{
							log("error", "cannot import as variable. line nr "+lineNr+" contains to much data (>100kB).");
						}
						else
						{
							Variable newVariable = master.clone();
							newVariable.setGlob(null);
							newVariable.setKey(key);
							newVariable.setValue(line);
							extractedVariables.add(newVariable);
						}
					}
				}
			}
			catch (IOException e)
			{
				this.log("error", e.getMessage());
				master.setStatus("error");
			}
			finally
			{
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					this.log("error", e.getMessage());
					master.setStatus("error");
				}
			}
		}
		// methode 3)
		else
//			else if(fileToExtractFrom.getName().matches("^.+$"))
		{
//			System.err.println("extracting the whole content as value from file " +fileToExtractFrom.getAbsolutePath());
			log("debug", "extracting the whole content as value from file "+fileToExtractFrom.getAbsolutePath());
			// den key verwenden, der in der variable bereits angegeben ist

			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(new FileInputStream(fileToExtractFrom)));
				String line = null;
				String value = "";
				
				// den glob nullen
				master.setGlob(null);
				
				// den gesamten inhalt so wier ist einlesen und nur das letzte \n entfernen
				while ((line = in.readLine()) != null)
				{
					value += line;
				}
				
				// entfernen aller newlines
				value = value.replace("(\\r|\\n)", "");

				// den master clonen
				Variable newVariable = master.clone();
				newVariable.setGlob(null);
				newVariable.setKey(master.getKey());
				newVariable.setValue(value);

				// und der variablen sammlung hinzufuegen
				extractedVariables.add(newVariable);
			}
			catch (IOException e)
			{
				this.log("error", e.getMessage());
				master.setStatus("error");
			}
			finally
			{
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					this.log("error", e.getMessage());
					master.setStatus("error");
				}
			}
		}

		return extractedVariables;
	}

	/**
	 * den commit durchfuehren
	 * @throws IOException
	 */
	public void doIt()
	{
		try
		{
			// und jetzt der konventionelle commit

			// wenn aktueller commit gelooped werden muss, dann werden die clone committed
			if( (this.getLoop() != null) && (!this.getLoop().equals("")) ) 
			{
				// die liste der looped Commits erstellen
				this.loopedCommits = new ArrayList<Commit>();
				
				this.log("info", "commit ("+this.getName()+") contains a loop-entry: "+this.getLoop());
				
				// gibt es die liste ueberhaupt ueber die geloopt werden soll?
				if(this.getParent().getList(this.getLoop()) == null)
				{
					this.log("error", "commit loop="+this.getLoop()+". such a list is not known in step " + this.getParent().getName());
					this.statusOverwrite = "error";
				}
				else
				{
					for(String loopVar : this.getParent().getList(this.getLoop()).getItem())
					{
						// den commit clonen und der loopedCommits hinzufuegen
						this.log("debug", "cloning commit: "+this.getName());
						Commit clonedCommit = this.clone();
						clonedCommit.setName(this.getName()+"(looped)");
						clonedCommit.loopedCommits = null;
						this.loopedCommits.add(clonedCommit);
						
						// und die werte fuer den clone setzen
						this.log("debug", "leeren des loops des clones");
						clonedCommit.setLoop("");
						this.log("debug", "setzen der loopVariable im clone auf "+loopVar);
						clonedCommit.setLoopvar(loopVar);
						
						// und den commit durchfuehren als ob es kein clone waere
						this.log("debug", "doing the commit of the clone");
						clonedCommit.doIt();
					}
				}
				// commit beenden, da das commit mit dem loopeintrag selber nicht committed wird, sondern nur die clone
				return;
			}
			
			// wenn kein loop vorhanden ist, wird 'this' committed
			else
			{
				// wenn das zu committende objekt ein File ist...
				for(File actualFile : this.getFile())
				{
					this.log("info", "file key="+actualFile.getKey());
					// ausfuehren von evtl. vorhandenen globs in den files
					commitFile(actualFile);
				}
	
				// wenn das zu committende objekt eine Variable ist...
				for(Variable actVariable : this.getVariable())
				{
					this.log("info", "variable "+actVariable.getKey());
					this.commitVariable(actVariable);
				}
			}
				
		}
		catch (Exception e)
		{
			log("fatal", e.getMessage() + "\n" +Arrays.toString(e.getStackTrace()));
		}
	}

	/**
	 * @return the refactor
	 */
	public String getRefactor() {
		return refactor;
	}

	/**
	 * @param refactor the refactor to set
	 */
	public void setRefactor(String refactor) {
		this.refactor = refactor;
	}
}
