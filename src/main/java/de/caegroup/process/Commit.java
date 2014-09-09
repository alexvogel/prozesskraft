package de.caegroup.process;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
//import java.util.*;
//import java.util.HashMap;
//import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

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
	private String name = "";
	private boolean toroot = false;
	private String loop = "";
	private String loopvar = "";
	private ArrayList<Variable> variable = new ArrayList<Variable>();
	private ArrayList<File> file = new ArrayList<File>();
	
	private String status = "";	// waiting/committing/finished/error/cancelled

	private java.lang.Process proc;
	private int exitvalue;
	private Step parent;

	private ArrayList<Log> log = new ArrayList<Log>();
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

	public boolean getToroot()
	{
		return this.toroot;
	}

	public void setToroot(boolean toroot)
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

	public void setVariable(ArrayList<Variable> variable)
	{
		this.variable = variable;
	}

	public ArrayList<File> getFile()
	{
		return this.file;
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

	public int getExitvalue()
	{
		return this.exitvalue;
	}
	
	public void setExitvalue(int exitvalue)
	{
		this.exitvalue = exitvalue;
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
		String status = "unknown";

		ArrayList<String> statusAllFilesVariables = new ArrayList<String>();

		for(File actFile : this.getFile())
		{
			statusAllFilesVariables.add(actFile.getStatus());
		}
		for(Variable actVariable : this.getVariable())
		{
			statusAllFilesVariables.add(actVariable.getStatus());
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

		return status;
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
		log("info", "want to commit the file(s) (key=" +master.getKey()+") that glob "+master.getGlob());

		ArrayList<File> filesToCommit = new ArrayList<File>();

		// wenn das file bereits einen absdir hat und das file existiert, dann muss dies nicht ueber globbing ermittelt werden
		if(new java.io.File(master.getAbsfilename()).exists())
		{
			filesToCommit.add(master);
		}

		// ansonsten muss mit dem glob festgestellt werden welche files gemeint sind
		// das Verzeichnis des Steps
		else if((master.getGlob()!=null) && (!master.getGlob().equals("")))
		{
			java.io.File stepDir = new java.io.File(this.getAbsdir());
	
			// alle eintraege des Verzeichnisses
			java.io.File[] allEntriesOfDirectory = stepDir.listFiles();
			log("info", allEntriesOfDirectory.length+" entries in directory "+stepDir.getAbsolutePath());
	
			// nur die files des verzeichnisses
			ArrayList<java.io.File> allFilesOfDirectory = new ArrayList<java.io.File>();
			for(java.io.File actFile : allEntriesOfDirectory)
			{
				if(!actFile.isDirectory())
				{
					allFilesOfDirectory.add(actFile);
				}
			}
			log("info", allFilesOfDirectory.size()+" files in directory "+stepDir.getAbsolutePath());
	
			// nur die files auf die der glob passt
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+master.getGlob());
			ArrayList<java.io.File> allFilesThatGlob = new ArrayList<java.io.File>();
			for(java.io.File actFile :allFilesOfDirectory)
			{
				if(matcher.matches(actFile.toPath()))
				{
					allFilesThatGlob.add(actFile);
				}
			}
	
			// files aus den java.io.files generieren und jedes mal das vorliegende file clonen
			for(java.io.File actFile : allFilesThatGlob)
			{
				File clonedFile = master.clone();
				clonedFile.setGlob("");
				clonedFile.setAbsfilename(actFile.getAbsolutePath());
				filesToCommit.add(clonedFile);
			}
		}

		// DIE ERMITTELTEN FILES UEBERPRUEFEN

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
			if(actFile.doAllTestsPass())
			{
				log("info", "all tests passed successfully ("+actFile.getAllTestsFeedback()+")");
			}
			else
			{
				log("error", "tests failed ("+actFile.getAllTestsFeedback()+")");
				master.setStatus("error");
			}
		}

		// wenn alles gut gegangen ist bisher, dann committen
		if(! master.getStatus().equals("error"))
		{
			this.getParent().addFile(filesToCommit);
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
		log("info", "want to commit the variable(s) (key=" +master.getKey()+") which value is expected to be in file(s) that glob "+master.getGlob());

		ArrayList<Variable> variablesToCommit = new ArrayList<Variable>();

		// wenn die variable bereits einen value hat, dann muss dies nicht ueber globbing ermittelt werden
		if((!master.getValue().equals("")) && (master.getValue()!=null))
		{
			variablesToCommit.add(master);
		}

		// ansonsten muss mit dem glob festgestellt werden welche files gemeint sind
		else if((master.getValue()!=null) && (!master.getGlob().equals("")))
		{
			// das Verzeichnis des Steps
			java.io.File stepDir = new java.io.File(this.getAbsdir());
			
			// alle eintraege des Verzeichnisses
			java.io.File[] allEntriesOfDirectory = stepDir.listFiles();
			log("info", allEntriesOfDirectory.length+" entries in directory "+stepDir.getAbsolutePath());
			
			// nur die files des verzeichnisses
			ArrayList<java.io.File> allFilesOfDirectory = new ArrayList<java.io.File>();
			for(java.io.File actFile : allEntriesOfDirectory)
			{
				if(!actFile.isDirectory())
				{
					allFilesOfDirectory.add(actFile);
				}
			}
			log("info", allFilesOfDirectory.size()+" files in directory "+stepDir.getAbsolutePath());
			
			// nur die files auf die der glob passt
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:"+master.getGlob());
			ArrayList<java.io.File> allFilesThatGlob = new ArrayList<java.io.File>();
			for(java.io.File actFile :allFilesOfDirectory)
			{
				if(matcher.matches(actFile.toPath()))
				{
					allFilesThatGlob.add(actFile);
				}
			}
	
			// variablen aus den files generieren und jedes mal die vorliegende variable clonen
			for(java.io.File actFile : allFilesThatGlob)
			{
				variablesToCommit.addAll(extractVariables(master, actFile, true));
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
			this.getParent().addVariable(variablesToCommit);
			master.setStatus("finished");
		}
	}
	
	/**
	 * 1) Das File wird eingelesen, wenn groesse < 100kB
	 * 2a) Wenn boolean==false, dann soll jede zeile als jeweils 1 Value interpretiert werden
	 * 2b) Wenn boolean==true, dann soll nur die erste Zeile als 1 Variable interpretiert werden (alle weiteren Zeilen werden ignoriert)
	 * 3) Ein clone der Variable wird mit einem Value bestueckt und als Teiol der ArrayList zurueck gegeben.
	 *
	 * @param Variable, java.io.File, boolean
	 * @throws IOException 
	*/
	public ArrayList<Variable> extractVariables(Variable master, java.io.File fileToExtractFrom, boolean onlyFirstLine)
	{
		ArrayList<Variable> extractedVariables = new ArrayList<Variable>();
		try
		{
			// wenn das file nicht zu gross ist (<100kB)
			if (fileToExtractFrom.length() < 102400.)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileToExtractFrom)));

				try
				{
					String line = null;
					try {
						while ((line = in.readLine()) != null)
						{
							Variable newVariable = master.clone();
							newVariable.setValue(line);
							extractedVariables.add(newVariable);
							
							if(onlyFirstLine)
							{
								break;
							}
						}
					}
					catch (IOException e)
					{
						this.log("error", e.getMessage());
						master.setStatus("error");
					}
				}
				finally
				{
					try {
						in.close();
					}
					catch (IOException e)
					{
						this.log("error", "IOException");
						master.setStatus("error");
						e.printStackTrace();
					}
				}
			}
			else
			{
				this.log("info", "file is to big (>100kB) to commit content as variables: "+fileToExtractFrom.getAbsolutePath());
				master.setStatus("error");
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			this.log("info", "variables not committed (cannot read file): "+fileToExtractFrom.getAbsolutePath());
			master.setStatus("error");
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
		catch (Exception e)
		{
			log("fatal", e.getMessage() + "\n" +Arrays.toString(e.getStackTrace()));
		}
	}
}
