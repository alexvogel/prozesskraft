package de.caegroup.process;

import java.io.*;
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
//			System.err.println("actual Step: "+this.getParent().getName()+" | actual Commit: "+this.getName()+" | actual File is: "+actFile.getKey()+" | size of File-log: "+actFile.getLog().size());
			logRecursive.addAll(actFile.getLog());
		}

		// sortieren nach Datum
		Collections.sort(logRecursive);

		return logRecursive;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		log("info", "setting status to '"+status+"'");
		this.status = status;
	}

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

	// den inhalt eines ganzen directories in den aktuellen step committen
	public void commitdir(java.io.File dir)
	{
		this.log("info", "will commit directory "+dir.toString());
		this.log("info", "test whether it is a directory "+dir.toString());
		
		if (dir.isDirectory())
		{
			boolean all_commitfiles_ok = true;
			
			this.log("info", "it is really a directory");
			ArrayList<java.io.File> allfiles = new ArrayList<java.io.File>(Arrays.asList(dir.listFiles()));
			
			for(java.io.File actFile : allfiles)
			{
				this.log("info", "test whether it is a file "+file.toString());
				if (actFile.isFile())
				{
					this.log("info", "it is a file");
					this.commitFile("default", actFile);
				}
				else
				{
					this.log("info", "it is NOT a file - skipping");
				}
			}
		}
		else
		{
			this.log("info", "it is not a directory - skipping");
			this.setStatus("error");
		}
	}

	public void commitdir(String absfilepathdir)
	{
		java.io.File file = new java.io.File(absfilepathdir);
		commitdir(file);
	}

	// ein file in den aktuellen step committen
	public void commitFile(String key, java.io.File file)
	{
		if (file.canRead())
		{
			File newfile = new File();
			newfile.setAbsfilename(file.getPath());
			newfile.setKey(key);
			this.commitFile(newfile);
		}
		else
		{
			this.log("error", "file NOT committed (CANT READ!): "+file.getAbsolutePath());
			setStatus("error");
		}
	}

	public void commitFile(String key, String absfilepathdir)
	{
		java.io.File file = new java.io.File(absfilepathdir);
		commitFile(key, file);
	}

	/**
	 * das file wird in das step-verzeichnis (oder root) kopiert, falls es nicht schon dort liegt
	 * dieses file im step-verzeichnis (oder root) wird in die file-ablage des step-objects aufgenommen 
	 * @param file
	 * @return success
	 */
	public void commitFile(File file)
	{
		this.log("info", "commit: planning for file: "+file.getAbsfilename());

		// wenn der pfad des files NICHT identisch ist mit dem pfad des step-directories
		if (!(new java.io.File(file.getAbsfilename()).getParent().matches("^"+this.getAbsdir()+"$")))
		{
			log("info", "file is not in step-directory");

			// wenn sich das file nicht im step-verzeichnis gefunden wird, soll es dorthin kopiert werden
			java.io.File zielFile = new java.io.File(this.getAbsdir()+"/"+file.getFilename());
			if(!(zielFile.exists()))
			{
				log("info", "commit: copying file to step-directory.");
				try
				{
					// copy
					String aufruf = "cp "+file.getAbsfilename()+" "+zielFile.getAbsolutePath();
					log("info", "commit: call: "+aufruf);
					Runtime.getRuntime().exec(aufruf);

					// anpassen des pfads
					file.setAbsfilename(zielFile.getAbsolutePath());
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					log("error", "IOException while copying file.");
					e.printStackTrace();
					this.setStatus("error");
				}
			}
		}
		else
		{
			log("info", "file already in step-directory. no copy needed.");
		}

		log("info", "adding file to step object: "+file.getAbsfilename());
		this.getParent().addFile(file);
//		System.out.println("AMOUNT OF FILES ARE NOW: "+this.file.size());
		this.setStatus("finished");
	}

	/**
	 * commit einer variable aus zwei strings (name, value)
	 * @input key, value
	*/	
	public void commitVariable(String key, String value)
	{
		Variable variable = new Variable();
		variable.setKey(key);
		variable.setValue(value);
		this.commitVariable(variable);
	}

	/**
	 * commit einer variable an this
	 * @input variable
	*/	
	public void commitVariable(Variable variable)
	{
		this.getParent().addVariable(variable);
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
	public void commitvarfile(java.io.File file)
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
					try {
						while ((line = in.readLine()) != null)
						{
							if (line.matches("^[^#]([^= \n]+)=([^= \n]+) *\n?"))
							{
								String[] linelist = line.split("=", 2);
								Variable variable = new Variable();
								variable.setKey(linelist[0]);
								variable.setValue(linelist[1]);
								this.getParent().addVariable(variable);
								this.log("info", "variable committed from file: "+file.getAbsolutePath()+" name: "+variable.getKey()+" value: "+variable.getValue());
							}
						}
					}
					catch (IOException e)
					{
						this.log("error", "IOException");
						this.setStatus("error");
						e.printStackTrace();
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
						this.setStatus("error");
						e.printStackTrace();
					}
				}
			}
			else
			{
				this.log("info", "file is to big (>100kB) to commit content as variables: "+file.getAbsolutePath());
				this.setStatus("error");
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			this.log("info", "variables not committed (cannot read file): "+file.getAbsolutePath());
			this.setStatus("error");
		}
	}

	public void commitvarfile(String absfilepathdir) throws IOException
	{
		java.io.File file = new java.io.File(absfilepathdir);
		commitvarfile(file);
	}

	/**
	 * den commit durchfuehren
	 * @throws IOException
	 */
	public void doIt()
	{
		this.setStatus("committing");

		// und jetzt der konventionelle commit

		// wenn das zu committende objekt ein File ist...
		for(File actualFile : this.getFile())
		{
			this.log("info", "file key="+actualFile.getKey());
			// ausfuehren von evtl. vorhandenen globs in den files
			if(actualFile.getAbsfilename().equals(""))
			{
				if( ( actualFile.getGlob() != null)&& !(actualFile.getGlob().equals("")))
				{
					log("info", "globbing for files with'"+actualFile.getGlob()+"'");
					java.io.File dir = new java.io.File(this.getAbsdir());
					FileFilter fileFilter = new WildcardFileFilter(actualFile.getGlob());
					java.io.File[] files = dir.listFiles(fileFilter);
					if(files.length == 0)
					{
						log("info", "no file matched glob '"+actualFile.getGlob()+"'");
					}

					else
					{
						log("info", files.length+" file(s) matched glob '"+actualFile.getGlob()+"'");
					}

					// globeintrag im File loeschen, damit es nicht erneut geglobbt wird
					actualFile.setGlob("");

					// passt es bzgl. minoccur und maxoccur?
					log("info", "checking amount of occurances.");
					if (files.length < actualFile.getMinoccur())
					{
						log("error", "minoccur is "+actualFile.getMinoccur()+" but only "+files.length+" file(s) globbed.");
					}
					else if (files.length > actualFile.getMaxoccur())
					{
						log("error", "maxoccur is "+actualFile.getMaxoccur()+" but "+files.length+" file(s) globbed.");
					}
					else
					{
						for(int x = 0; x < files.length; x++)
						{
							// ist es der letzte glob? Dann soll das urspruengliche file object verwendet werden
							if((x+1) == files.length)
							{
								actualFile.setAbsfilename(files[x].getAbsolutePath());
								log("info", "setting absolute filename to: "+actualFile.getAbsfilename());
							}
							// alle anderen werden aus einem clon erstellt
							else
							{
								log("info", "cloning file-object and setting filename: "+actualFile.getAbsfilename());
								File pFile = actualFile.clone();
								pFile.setAbsfilename(files[x].getAbsolutePath());
								// das zusaetzliche file dem commit hinzufuegen
								this.addFile(pFile);
							}
						}
					}
				}
			}
		}
		// ueber alle files des commits iterieren und dem step hinzufuegen
		for(File actFile : this.getFile())
		{
			this.log("info", "commit: file "+actFile.getKey()+":"+actFile.getAbsfilename());
			commitFile(actFile);

			// falls nicht rootstep und wenn das File auch dem prozess committed werden soll...
			if (this.getToroot() && (!(this.getParent().isRoot())))
			{
				this.log("info", "this file also goes also to root: file "+actFile.getKey()+":"+actFile.getAbsfilename());

				// dem root-step committen
				this.getParent().getParent().getStep(this.getParent().getParent().getRootstepname()).addFile(actFile);
			}
		}
				
		// wenn das zu committende objekt eine Variable ist...
		for(Variable actVariable : this.getVariable())
		{
			this.log("info", "variable "+actVariable.getKey()+":"+actVariable.getValue());
			this.commitVariable(actVariable);

			// wenn die Variable auch dem prozess committed werden soll...
			if (this.getToroot() && (!(this.getName().equals(this.getParent().isRoot()))))
			{
				this.log("info", "this variable goes also to root: variable "+actVariable.getKey()+":"+actVariable.getValue());
				// dem root-step committen
				this.getParent().getParent().getStep(this.getParent().getParent().getRootstepname()).addVariable(actVariable);
			}
		}

		this.setStatus("finished");
	}
}
