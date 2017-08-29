package de.prozesskraft.pkraft;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Timestamp;

import static java.nio.file.FileVisitResult.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.prozesskraft.codegen.Script;
import de.prozesskraft.pkraft.Process;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.io.FileUtils;

public class Step
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = new String();
	private String clip = null;
	private String type = "automatic";
	private String description = new String();
	private Integer stepStartDelayMinutes = null;
	private ArrayList<List> list = new ArrayList<List>();
	private ArrayList<List> defaultlist = new ArrayList<List>();

	private ArrayList<Init> init = new ArrayList<Init>();
	private Work work = null;
	private Subprocess subprocess = null;
	private ArrayList<Commit> commit = new ArrayList<Commit>();
	private String loop = null;
	private String loopOld = null;
	private String loopvar = null;

	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Variable> variable = new ArrayList<Variable>();
	
	// diese Variablen und Files wurden ueber 'toroot' in den rootStep committed.
	// bei einem step.reset() sollen diese committments auch aus dem rootStep entfernt werden
	private ArrayList<File> fileCommittedToRoot = new ArrayList<File>();
	private ArrayList<Variable> variableCommittedToRoot = new ArrayList<Variable>();
	
//	private String status = "waiting";	// waiting/initializing/working/committing/ finished/error/cancelled
	private ArrayList<Log> log = new ArrayList<Log>();
	public String statusOverwrite = null;
	private String rank = "";
	private int reset = 0;

	private int level = 0;
	private long levelCalctime = 0;

//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");

	private boolean statusChangedWhileLastDoIt = false;
	
	private Timeserie timeSerieStatus = new Timeserie("status");

	// don't clone parent when cloning this
	private Process parent = null;
	transient private Process parentDummy = null;

	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 * constructs a step with
	 * a new parent
	 * a random name
	 */
	public Step()
	{
		Process dummyProcess = new Process();
		dummyProcess.setName("dummy");
		this.parentDummy = dummyProcess;

		this.setName(this.genName());
	}

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

	/*----------------------------
	  methods 
	----------------------------*/
	/**
	 * clone
	 * returns a clone of this
	 * @return Step
	 */
	@Override
	public Step clone()
	{
		Step newStep = new Step();
		newStep.setName(this.getName());
		newStep.setClip(this.getClip());
		newStep.setType(this.getType());
		newStep.setStepStartDelayMinutes(this.getStepStartDelayMinutes());
		newStep.setDescription(this.getDescription());
		
		for(List actList : this.getList())
		{
			newStep.addList(actList.clone());
		}
		for(List actList : this.getDefaultlist())
		{
			newStep.addDefaultlist(actList.clone());
		}
		for(Init actInit : this.getInit())
		{
			newStep.addInit(actInit.clone());
		}
		if(this.getWork() != null)
		{
			newStep.setWork(this.getWork().clone());
		}
		if(this.getSubprocess() != null)
		{
			newStep.setSubprocess(this.getSubprocess().clone());
		}
		for(Commit actCommit : this.getCommit())
		{
			newStep.addCommit(actCommit.clone());
		}
		newStep.setLoop(this.getLoop());
		newStep.setLoopOld(this.getLoopOld());
		newStep.setLoopvar(this.getLoopvar());
		for(File actFile : this.getFile())
		{
			newStep.addFile(actFile.clone());
		}
		for(Variable actVariable : this.getVariable())
		{
			newStep.addVariable(actVariable.clone());
		}
		for(Log actLog : this.getLog())
		{
			newStep.addLog(actLog.clone());
		}
		newStep.setStatusOverwrite(this.getStatusOverwrite());
		newStep.setStatusChangedWhileLastDoIt(this.isStatusChangedWhileLastDoIt());
		newStep.setRank(this.getRank());
		newStep.setReset(this.getReset());
		newStep.setLevel(this.getLevel());
		newStep.setLevelCalctime(this.getLevelCalctime());

		return newStep;
	}

	/**
	 * clones the step,
	 * integrates the clone into process
	 * clones the stepdirectory on filesystem
	 * writes prozess to file
	 * returns the cloned step
	 * @return Step
	 */
	public Step cloneAndIntegrateWithData()
	{
		// bisherigen step klonen
		Step clonedStep = this.clone();

		// den geklonten step in den Prozess integrieren
		if(!this.getParent().integrateStep(clonedStep))
		{
			this.log("error", "integrating step into process failed");
			return null;
		}

		// kopieren der daten auf filesystem
		clonedStep.log("info", "copying step directory: source="+this.getAbsdir()+", target="+clonedStep.getAbsdir());
		try
		{
			FileUtils.copyDirectory(new java.io.File(this.getAbsdir()), new java.io.File(clonedStep.getAbsdir()), true);
		}
		catch (IOException e)
		{
			this.log("error", "copying of directory tree failed -> cloning failed. deleting all copied data.");
			this.log("error", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));

			this.log("warn", "delete this directory by hand: "+clonedStep.getAbsdir());

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return clonedStep;
	}
	
	/**
	 * clone
	 * returns a clone of this
	 * @return Step
	 */
	public Step oldClone()
	{
		return SerializationUtils.clone(this);
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
			parentDummy = new Process();
		}
	}
	
	/**
	 * add a log
	 * @param log
	 */
	public void addLog(Log log)
	{
		log.setLabel("step "+this.getName());
		this.log.add(log);
	}

	/**
	 * getCommandResolveAsPerlCode()
	 * generates perlcode for resolving a command
	 * @return ArrayList<String> code
	 */
	public ArrayList<String> getCommandResolveAsPerlCode()
	{
		ArrayList<String> perlSnippet = new ArrayList<String>();
		
		perlSnippet.add("#-------------------");
		perlSnippet.add("# Welches Kommando soll fuer Step '" + this.getName() + "' aufgerufen werden?");
		perlSnippet.add("");

		// gibts ein work? dann soll das kommando aus der workdefinition uebernommen werden
		if(this.getWork() != null)
		{
			perlSnippet.add("if (!($COMMAND{'" + this.getName() + "'} = &commandResolve(\"" + this.getWork().getCommand() + "\")))");
			perlSnippet.add("{");
			perlSnippet.add("	&logit(\"fatal\", \"cannot determine what program to call for step '" + this.getName() + "'. " + this.getWork().getCommand() + " neither found in <installdir>/bin nor by calling 'which'.\");");
			perlSnippet.add("	my $PROCESS_STOP = scalar(localtime());");
			perlSnippet.add("	exit(1);");
			perlSnippet.add("}");
			perlSnippet.add("");
			perlSnippet.add("&logit(\"debug\", \"command for step '" + this.getName() + "' is: $COMMAND{'" + this.getName() + "'}\");");
			perlSnippet.add("");
		}
		// gibts ein subprocess? Dann soll der Aufruf auf das entsprechende prozesskommando gehen
		else if(this.getSubprocess() != null)
		{
			perlSnippet.add("$COMMAND{'" + this.getName() + "'} = $domainInstallationDirectory . \"/" + this.getSubprocess().getDomain() + "/" +this.getSubprocess().getName() + "/" +this.getSubprocess().getVersion() + "/"+this.getSubprocess().getName()+"\";");
			perlSnippet.add("");
			perlSnippet.add("if(!stat $COMMAND{'" + this.getName() + "'})");
			perlSnippet.add("{");
			perlSnippet.add("	&logit(\"fatal\", \"cannot determine what program to call for subprocess in step '" + this.getName() + "'. command not found: \" . $COMMAND{'" + this.getName() + "'});");
			perlSnippet.add("	my $PROCESS_STOP = scalar(localtime());");
			perlSnippet.add("	exit(1);");
			perlSnippet.add("}");
			perlSnippet.add("");
			perlSnippet.add("&logit(\"debug\", \"command for subprocess in step '" + this.getName() + "' is: \" . $COMMAND{'" + this.getName() + "'});");
			perlSnippet.add("");
			
		}
		
		return perlSnippet;
	}
	
	/**
	 * getStepAsPerlCodeBlock()
	 * generates perlcode for starting this process-step from a perlscript
	 * @return ArrayList<String> code
	 */
	public ArrayList<String> getStepAsPerlCodeBlock()
	{
		ArrayList<String> perlSnippet = new ArrayList<String>();
		
		perlSnippet.add("#-------------------");
		perlSnippet.add("# Prozessschritt wird gestartet '" + this.getName() + "' -- " + this.getDescription());

		perlSnippet.add(this.getName() + ":");
		perlSnippet.add("{");
		perlSnippet.add("\tmy $stepname = \""+this.getName()+"\";");
		perlSnippet.add("\tmy $stepdescription = \""+this.getDescription()+"\";");
		perlSnippet.add("\tmy $steprank = \""+this.getRank()+"\";");
		perlSnippet.add("\t&girlande_stepstart($stepname);");
		perlSnippet.add("");
		perlSnippet.add("\tmy %allLists;");

		perlSnippet.add("");
		perlSnippet.add("\t# generate lists with initial values");

		// generate festverdrahtete listen
		for(List list : this.getList())
		{
			String line = "\tmy @"+list.getName()+" = (";
			
			for(String item : list.getDefaultitem())
			{
				line += "\""+item+"\"";
			}
			
			line += ");";
			
			perlSnippet.add(line);
			perlSnippet.add("\t$allLists{'"+list.getName()+"'} = \\@"+list.getName()+";");
			perlSnippet.add("");
		}
	
		// initialisierung noch nicht bestehender listen bzw. anreichern bestehender listen
		for(Init actInit : this.getInit())
		{
			perlSnippet.add("\t{");
				
			perlSnippet.add("\t\t&logit(\"debug\", \"populating list '"+actInit.getListname()+"'\");");

			// falls liste noch nicht existiert, soll eine leere liste erzeugt werden
			if (this.getList(actInit.getListname()) == null)
			{
				perlSnippet.add("");
				perlSnippet.add("\t\t# new list");
				perlSnippet.add("\t\tmy @list;");
				perlSnippet.add("\t\t$allLists{'"+actInit.getListname()+"'} = \\@list;");
			}
				
			// ein array of hashes mit allen matches anlegen
			perlSnippet.add("");
			perlSnippet.add("\t\t# create an array of hashes with all matches");
			perlSnippet.add("\t\tmy @matches_list;");
			ArrayList<Match> matchesOfInit = actInit.getMatch();
			for(int x=0; x < matchesOfInit.size(); x++)
			{
				perlSnippet.add("\t\tmy %match_list_"+x+";");
				perlSnippet.add("\t\t$match_list_"+x+"{'field'} = \'"+matchesOfInit.get(x).getField()+"\';");
				perlSnippet.add("\t\t$match_list_"+x+"{'pattern'} = \'"+matchesOfInit.get(x).getPattern()+"\';");
				perlSnippet.add("\t\tpush @matches_list, \\%match_list_"+x+";");
			}
			
			// liste initialisieren / anreichern
			perlSnippet.add("");
			perlSnippet.add("\t\t# initialize list");
			perlSnippet.add("\t\t# initlist (1=fromobjecttype 2=returnfield 3=fromstep 4=insertrule 5=minoccur 6=maxoccur 7=refARRAYmatch 8=refARRAYlist 9=refHASHvariable 10=refHASHfile)");
			perlSnippet.add("\t\t&logit(\"info\", \"step '" + this.getName() + "' initializes list '"+actInit.getListname()+"' with data from step '"+actInit.getFromstep()+"'\");");
			perlSnippet.add("\t\t&initlist('"+actInit.getFromobjecttype()+"', '"+actInit.getReturnfield()+"', '"+actInit.getFromstep()+"', '"+actInit.getInsertrule()+"', "+actInit.getMinoccur()+", "+actInit.getMaxoccur()+", \\@matches_list, $allLists{'"+actInit.getListname()+"'}, $VARIABLE{'"+actInit.getFromstep()+"'}, $FILE{'"+actInit.getFromstep()+"'});");
			
			perlSnippet.add("\t}");
		}
		
		// call erzeugen
		perlSnippet.add("");
		perlSnippet.add("\t# create call for command");
		perlSnippet.add("\tmy $call = $COMMAND{'" + this.getName() + "'};");
		perlSnippet.add("");
		
		// falls ein work existiert, sol das kommando aus den callitems zusammengesetzt werden
		if(this.getWork() != null)
		{
			for(Callitem actCallitem : this.getWork().getCallitem())
			{
				// wenn callitem geloopt werden soll?
				if(!(actCallitem.getLoop() == null))
				{
					perlSnippet.add("\tforeach my $loopvar (@{$allLists{'"+actCallitem.getLoop()+"'}})");
					perlSnippet.add("\t{");
					String tmpString = actCallitem.getPar()+actCallitem.getDel()+actCallitem.getVal();
					String tmpReplace = tmpString.replaceAll("\\$", "\\\\\\$");
					perlSnippet.add("\t\tmy $tmpString = \""+tmpReplace+"\";");
					perlSnippet.add("\t\t$tmpString =~ s/\\{\\$loopvarcallitem\\}/$loopvar/g;");
					
					perlSnippet.add("\t\t$call .= \" \" . $tmpString;");
					perlSnippet.add("\t}");
				}
				// wenn callitem nicht geloopt werden soll
				else
				{
					String tmpString = actCallitem.getPar()+actCallitem.getDel()+actCallitem.getVal();
					String tmpReplace = tmpString.replaceAll("\\$", "\\\\\\$");
					perlSnippet.add("\t$call .= \" \" . \""+tmpReplace+"\";");
				}
			}
		}
		// falls es ein Subprocess ist, dann soll ueber die commits seines rootsteps geloopt und daraus aufrufparameter erzeugt werden
		else if(this.getSubprocess() != null)
		{
			// fuer jedes commit des rootSteps
			for(Commit actCommit : this.getSubprocess().getStep().getCommit())
			{
				// fuer jedes file des commits
				for(File actFile : actCommit.getFile())
				{
					String tmpString = actFile.getKey()+" "+actFile.getGlob();
					String tmpReplace = tmpString.replaceAll("\\$", "\\\\\\$");
					perlSnippet.add("\t$call .= \" \" . \""+tmpReplace+"\";");
				}
			}
		}

		// aufloesen der platzhalter inerhalb des calls
		perlSnippet.add("\t$call = &resolve($call, \\%allLists);");
		perlSnippet.add("\t&logit(\"debug\", \"--- call will be: $call\");");
		perlSnippet.add("");
		
		// directory fuer den step anlegen
		perlSnippet.add("\t# create step-owned directory");
		perlSnippet.add("\tmy $stepdir = \"dir4step_"+this.getName()+"\";");
		perlSnippet.add("\tmy $pwd = cwd();");
		perlSnippet.add("\tmy $steppathdir = $pwd . \"/\" . \"dir4step_"+this.getName()+"\";");
		perlSnippet.add("\t&logit(\"debug\", \"create directory for actual step '"+this.getName()+"': 'dir4step_"+this.getName()+"'\");");
		perlSnippet.add("\tmkdir($steppathdir);");
		perlSnippet.add("");

		// in das step-verzeichnis wechseln
		perlSnippet.add("\t# change into step-owned directory");
		perlSnippet.add("\t&logit(\"debug\", \"changing into step-directory: 'dir4step_"+this.getName()+"'\");");
		perlSnippet.add("\tchdir($steppathdir);");
		perlSnippet.add("");
		
		// executieren des calls
		perlSnippet.add("\t# execute the call");
		perlSnippet.add("\t&logit(\"info\", \"executing program for step '"+this.getName()+"': $call\");");
		perlSnippet.add("\t&logit(\"info\", \">>>>>>>>>> subsequent logging comes from step '"+this.getName()+"' >>>>>>>>>>\");");
		perlSnippet.add("");
		perlSnippet.add("\t# aktualisieren der steptabelle im html");
		perlSnippet.add("\t$STEPS_TABELLE{$steprank}{'aufruf'} = \"$call\";");
		perlSnippet.add("\t$STEPS_TABELLE{$steprank}{'dir'} = \"<a href=\\\"./$stepdir\\\" target=\\\"_blank\\\">$stepdir</a>\";");
		perlSnippet.add("\t$STEPS_TABELLE{$steprank}{'status'} = 'running';");
		perlSnippet.add("\t$STEPS_TABELLE{$steprank}{'log'} = \"<a href=\\\"./$stepdir/stdout.log\\\">stdout.log</a> <a href=\\\"./$stepdir/stderr.log\\\">stderr.log</a>\";");
		perlSnippet.add("\t&printHtmlOverview();");
		perlSnippet.add("");

		perlSnippet.add("\tmy $return = system(\"set -o pipefail; (($call | tee stdout.log) 3>&1 1>&2 2>&3 | tee stderr.log) 3>&1 1>&2 2>&3\");");
		perlSnippet.add("");
		perlSnippet.add("\t# aktualisieren der steptabelle im html");
		perlSnippet.add("\t$STEPS_TABELLE{$steprank}{'status'} = \"exit=$return\";");
		perlSnippet.add("\t&printHtmlOverview();");
		perlSnippet.add("");

		perlSnippet.add("\t&logit(\"info\", \"<<<<<<<<<< previous logging came from step '"+this.getName()+"' <<<<<<<<<<\");");
		perlSnippet.add("\tif($return)");
		perlSnippet.add("\t{");
		perlSnippet.add("\t\t&logit(\"fatal\", \"step '"+this.getName()+"' exited with an error (exitcode=$return)\");");
		perlSnippet.add("\t\t$PROCESS_STOP = scalar(localtime());");
		perlSnippet.add("\t\t$PROCESS_STATUS = 'error';");
		perlSnippet.add("\t\tforeach(sort keys %STEPS_TABELLE)");
		perlSnippet.add("\t\t{");
		perlSnippet.add("\t\t\tif($steprank < $_)");
		perlSnippet.add("\t\t\t{");
		perlSnippet.add("\t\t\t\t$STEPS_TABELLE{$_}{'status'} = 'cancelled';");
		perlSnippet.add("\t\t\t}");
		perlSnippet.add("\t\t}");
		perlSnippet.add("\t\t&printHtmlOverview();");
		perlSnippet.add("\t\t#system(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (work) (exitcode=$return)\\\"\");");
		perlSnippet.add("\t\texit(1);");
		perlSnippet.add("\t}");
		perlSnippet.add("\t&logit(\"info\", \"step '"+this.getName()+"' exited properly\");");
		perlSnippet.add("");
		
		// committen
		perlSnippet.add("\t#commits;");
		for(Commit actCommit : this.getCommit())
		{
			perlSnippet.add("\t&logit(\"info\", \"preparing to commit '"+actCommit.getName()+"'\");");
			
			for(Variable actVariable : actCommit.getVariable())
			{
				perlSnippet.add("\t&logit(\"info\", \"committing variable '"+actVariable.getKey()+"'\");");
				perlSnippet.add("\t{");
				// wenn ein value angegeben wird
				if(!(actVariable.getValue() == null))
				{
					perlSnippet.add("\t\tmy $value = '"+actVariable.getValue()+"';");
					perlSnippet.add("\t\t$value = &resolve($value, \\%allLists);");
					perlSnippet.add("\t\tpush (@{$VARIABLE{'"+this.getName()+"'}}, [\""+actVariable.getKey()+"\", $value]);");
					perlSnippet.add("\t\t&logit(\"info\", \""+actVariable.getKey()+"=$value\");");
				}
				else if(!(actVariable.getGlob() == null))
				{
					String tmpString = actVariable.getGlob();
					String tmpReplace = tmpString.replaceAll("\\$", "\\\\\\$");
					perlSnippet.add("\t\tmy $glob = '"+tmpReplace+"';");
					perlSnippet.add("\t\t$glob = &resolve($glob, \\%allLists);");
					perlSnippet.add("\t\t&logit(\"debug\", \"--- modified glob is: $glob\");");
					perlSnippet.add("\t\t&logit(\"debug\", \"---- globbing for files with glob '$glob'\");");
					perlSnippet.add("");
					perlSnippet.add("\t\t# feststellen ob files vorhanden sind");
					perlSnippet.add("\t\topendir(DIRSTEP, cwd());");
					perlSnippet.add("\t\tmy @globbedFiles = grep{ $_ =~ m/$glob/ } readdir(DIRSTEP);");
					perlSnippet.add("\t\t&logit(\"debug\", \"----- \" . scalar(@globbedFiles) . \" file(s) globbed\");");
					perlSnippet.add("");
					perlSnippet.add("\t\tif((@globbedFiles < "+actVariable.getMinoccur()+") || (@globbedFiles > "+actVariable.getMaxoccur()+"))");
					perlSnippet.add("\t\t{");
					perlSnippet.add("\t\t\t&logit(\"debug\", \"------ step '"+this.getName()+"' did not produce the right amount of variables with pattern (glob=$glob).\");");
					perlSnippet.add("\t\t\t&logit(\"error\", \""+actVariable.getMinoccur()+" <= rightAmountOfVariables <= "+actVariable.getMaxoccur()+" (actualAmount=\" . scalar(@globbedFiles) . \")\");");
					perlSnippet.add("\t\t\t&logit(\"fatal\", \"committing variable '"+actVariable.getKey()+"' failed\");");
					perlSnippet.add("\t\t\t$PROCESS_STOP = scalar(localtime());");
					perlSnippet.add("\t\t\t#system(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (variable="+actVariable.getKey()+") not right amount of variables: "+actVariable.getMinoccur()+" <= rightAmountOfVariables <= "+actVariable.getMaxoccur()+" (actualAmount=\" . scalar(@globbedFiles) . \"\\\"\");");
					perlSnippet.add("\t\t\texit(1);");
					perlSnippet.add("\t\t}");
					perlSnippet.add("\t\tmy @variableList;");
					perlSnippet.add("\t\tforeach my $globbedFile (@globbedFiles)");
					perlSnippet.add("\t\t{");
					perlSnippet.add("\t\t\t$globbedFile = File::Spec->rel2abs($globbedFile);");
					perlSnippet.add("\t\t\t&logit(\"debug\", \"------ globbed File: \" . $globbedFile);");
					perlSnippet.add("\t\t\tif(open(VARFILE, '<'.$globbedFile))");
					perlSnippet.add("\t\t\t{");
					// wenn variable toroot=true ist, dann soll auch in das root-verzeichnis kopiert werden (und auch in %FILE{'root}
					if(actCommit.isTorootPresent())
					{
						perlSnippet.add("");
						perlSnippet.add("\t\t\t\t# toroot = true");
						perlSnippet.add("\t\t\t&logit(\"info\", \"toroot: cp $globbedFile $pwd\");");
						perlSnippet.add("\t\t\t\tcopy($globbedFile, $pwd);");
					}
					// wenn variable copyto=basedir ist, dann soll das auch in das angegebene verzeichnis kopiert werden
					if(actCommit.getCopyto() != null)
					{
						if(actCommit.getCopyto().equalsIgnoreCase("basedir"))
						{
							perlSnippet.add("");
							perlSnippet.add("\t\t\t\t# copyto = basedir");
							perlSnippet.add("\t\t\t&logit(\"info\", \"copyto: cp $globbedFile $_basedir\");");
							perlSnippet.add("\t\t\t\tcopy($globbedFile, $_basedir);");
						}
					}
					perlSnippet.add("\t\t\t\twhile(<VARFILE>)");
					perlSnippet.add("\t\t\t\t{");
					perlSnippet.add("\t\t\t\t\tmy $zeile = $_;");
					perlSnippet.add("\t\t\t\t\tchomp $zeile;");
					perlSnippet.add("\t\t\t\t\t&logit(\"debug\", \"------- adding first line of file to the variable repository (step="+this.getName()+", key="+actVariable.getKey()+", value=$zeile)\");");
					perlSnippet.add("\t\t\t\t\tpush (@variableList, $zeile);");
					perlSnippet.add("\t\t\t\t\tlast;");
					perlSnippet.add("\t\t\t\t}");
					perlSnippet.add("\t\t\t}");
					perlSnippet.add("\t\t}");
					perlSnippet.add("");
					perlSnippet.add("\t\tforeach my $tmp (@variableList)");
					perlSnippet.add("\t\t{");
					perlSnippet.add("\t\t\tpush (@{$VARIABLE{'"+this.getName()+"'}}, [\""+actVariable.getKey()+"\", $tmp]);");
					perlSnippet.add("\t\t\t&logit(\"info\", \""+actVariable.getKey()+"=$tmp\");");
					// wenn variable toroot=true ist, dann soll auch in das root-verzeichnis kopiert werden (und auch in %FILE{'root}
					if(actCommit.isTorootPresent())
					{
						perlSnippet.add("");
						perlSnippet.add("\t\t\t# toroot = true");
						perlSnippet.add("\t\t\tpush (@{$VARIABLE{'root'}}, [\""+actVariable.getKey()+"\", $tmp]);");
						perlSnippet.add("");
						perlSnippet.add("\t\t\t# toroot = true, deshalb auch in die tabelle des htmls mit aufnehemen");
						perlSnippet.add("\t\t\tmy @outputRowForHtml = ($stepname, 'wert', '"+actVariable.getKey()+"', \""+actVariable.getDescription()+"\", \"$tmp\");");
						perlSnippet.add("\t\t\tpush(@OUTPUT_TABELLE, \\@outputRowForHtml);");
						perlSnippet.add("\t\t\t&printHtmlOverview();");
					}
					perlSnippet.add("\t\t}");
				}
				else
				{
					perlSnippet.add("\t\t&logit(\"error\", \"step '"+this.getName()+"', commit '"+actCommit.getName()+"', variable '"+actVariable.getKey()+"' needs either a value or a glob definition.\");");
					perlSnippet.add("\t\t&logit(\"fatal\", \"committing variable '"+actVariable.getKey()+"' failed\");");
					perlSnippet.add("\t\t\tmy $PROCESS_STOP = scalar(localtime());");
					perlSnippet.add("\t\t\tmy $PROCESS_STATUS = 'error';");
					perlSnippet.add("\t\t\t#system(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (variable="+actVariable.getKey()+") variable needs either a value or a glob definition\\\"\");");
					perlSnippet.add("\t\t\texit(1);");
				}
				perlSnippet.add("\t}");
			}
			for(File actFile : actCommit.getFile())
			{
				perlSnippet.add("\t&logit(\"info\", \"committing file '"+actFile.getKey()+"'\");");
				perlSnippet.add("\t{");
				if(!(actFile.getGlob() == null))
				{
					String tmpString = actFile.getGlob();
					String tmpReplace = tmpString.replaceAll("\\$", "\\\\\\$");
					perlSnippet.add("\t\tmy $glob = \""+tmpReplace+"\";");
					perlSnippet.add("\t\t$glob = &resolve($glob, \\%allLists);");
					perlSnippet.add("\t\t&logit(\"debug\", \"--- modified glob is: $glob\");");
					perlSnippet.add("\t\t&logit(\"debug\", \"---- globbing for files with glob '$glob'\");");
					perlSnippet.add("");
					perlSnippet.add("\t\t# feststellen ob files vorhanden sind");
					perlSnippet.add("\t\topendir(DIRSTEP, cwd());");
					perlSnippet.add("\t\tmy @globbedFiles = grep{ $_ =~ m/$glob/ } readdir(DIRSTEP);");
					perlSnippet.add("\t\t&logit(\"debug\", \"----- \" . scalar(@globbedFiles) . \" file(s) globbed\");");
					perlSnippet.add("");
					perlSnippet.add("\t\tif((@globbedFiles < "+actFile.getMinoccur()+") || (@globbedFiles > "+actFile.getMaxoccur()+"))");
					perlSnippet.add("\t\t{");
					perlSnippet.add("\t\t\t&logit(\"debug\", \"------ step '"+this.getName()+"' did not produce the right amount of files with pattern (glob=$glob).\");");
					perlSnippet.add("\t\t\t&logit(\"error\", \""+actFile.getMinoccur()+" <= rightAmountOfFiles <= "+actFile.getMaxoccur()+" (actualAmount=\" . scalar(@globbedFiles) . \")\");");
					perlSnippet.add("\t\t\t&logit(\"fatal\", \"committing file '"+actFile.getKey()+"' failed\");");
					perlSnippet.add("\t\t\t$PROCESS_STOP = scalar(localtime());");
					perlSnippet.add("\t\t\t$PROCESS_STATUS = 'error';");
					perlSnippet.add("\t\t\tforeach(sort keys %STEPS_TABELLE)");
					perlSnippet.add("\t\t\t{");
					perlSnippet.add("\t\t\t\tif($steprank < $_)");
					perlSnippet.add("\t\t\t\t{");
					perlSnippet.add("\t\t\t\t\t$STEPS_TABELLE{$_}{'status'} = 'cancelled';");
					perlSnippet.add("\t\t\t\t}");
					perlSnippet.add("\t\t\t}");
					perlSnippet.add("\t\t\t&printHtmlOverview();");
					perlSnippet.add("\t\t\t#system(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (file="+actFile.getKey()+") not right amount of files: "+actFile.getMinoccur()+" <= rightAmountOfFiles <= "+actFile.getMaxoccur()+" (actualAmount=\" . scalar(@globbedFiles) . \"\\\"\");");
					perlSnippet.add("\t\t\texit(1);");
					perlSnippet.add("\t\t}");
					perlSnippet.add("\t\tmy @fileList;");
					
					perlSnippet.add("\t\tforeach my $globbedFile (@globbedFiles)");
					perlSnippet.add("\t\t{");
					perlSnippet.add("\t\t\t$globbedFile = File::Spec->rel2abs($globbedFile);");
					perlSnippet.add("\t\t\t&logit(\"debug\", \"------ globbed File: \" . $globbedFile);");
					perlSnippet.add("\t\t\t&logit(\"debug\", \"------- adding file to file repository (step="+this.getName()+", key="+actFile.getKey()+", absfilename=$globbedFile)\");");
					perlSnippet.add("\t\t\tpush (@fileList, $globbedFile);");
					perlSnippet.add("\t\t}");
					perlSnippet.add("");
					perlSnippet.add("\t\tforeach my $tmp (@fileList)");
					perlSnippet.add("\t\t{");
					perlSnippet.add("\t\t\tpush (@{$FILE{'"+this.getName()+"'}}, [\""+actFile.getKey()+"\", $tmp]);");
					perlSnippet.add("\t\t\t&logit(\"info\", \""+actFile.getKey()+"=$tmp\");");
					// wenn file toroot=true ist, dann soll auch in das root-verzeichnis kopiert werden (und auch in %FILE{'root}
					if(actCommit.isTorootPresent())
					{
						perlSnippet.add("");
						perlSnippet.add("\t\t\t# toroot = true");
						perlSnippet.add("\t\t\tpush (@{$FILE{'root'}}, [\""+actFile.getKey()+"\", $tmp]);");
						perlSnippet.add("\t\t\t&logit(\"info\", \"toroot: cp $tmp $pwd\");");
						perlSnippet.add("\t\t\tcopy($tmp, $pwd);");
						perlSnippet.add("");
						perlSnippet.add("\t\t\t# toroot = true, deshalb auch in die tabelle des htmls mit aufnehemen");
						perlSnippet.add("\t\t\t(my $filename, my $dirs, my $suf) = fileparse($tmp);");
						perlSnippet.add("\t\t\tmy @outputRowForHtml = ($stepname, 'datei', '"+actFile.getKey()+"', \""+actFile.getDescription()+"\", \"<a href=\\\"\" . File::Spec->abs2rel($tmp) . \"\\\">$filename</a>\");");
						perlSnippet.add("\t\t\tpush(@OUTPUT_TABELLE, \\@outputRowForHtml);");
						perlSnippet.add("\t\t\t&printHtmlOverview();");
					}
					// wenn file copyto=basedir ist, dann soll das auch in das angegebene verzeichnis kopiert werden
					if(actCommit.getCopyto() != null)
					{
						if(actCommit.getCopyto().equalsIgnoreCase("basedir"))
						{
							perlSnippet.add("");
							perlSnippet.add("\t\t\t# copyto = basedir");
							perlSnippet.add("\t\t\t&logit(\"info\", \"copyto: cp $tmp $_basedir\");");
							perlSnippet.add("\t\t\tcopy($tmp, $_basedir);");
						}
					}
					perlSnippet.add("\t\t}");
					
				}
				else
				{
					perlSnippet.add("\t\t&logit(\"error\", \"------ step '"+this.getName()+"', commit '"+actCommit.getName()+"', file '"+actFile.getKey()+"' needs a glob definition.\");");
					perlSnippet.add("\t\t&logit(\"fatal\", \"committing file '"+actFile.getKey()+"' failed\");");
					perlSnippet.add("\t\t\t$PROCESS_STOP = scalar(localtime());");
					perlSnippet.add("\t\t\t$PROCESS_STATUS = 'error';");
					perlSnippet.add("\t\t\tforeach(sort keys %STEPS_TABELLE)");
					perlSnippet.add("\t\t\t{");
					perlSnippet.add("\t\t\t\tif($steprank < $_)");
					perlSnippet.add("\t\t\t\t{");
					perlSnippet.add("\t\t\t\t\t$STEPS_TABELLE{$_}{'status'} = 'cancelled';");
					perlSnippet.add("\t\t\t\t}");
					perlSnippet.add("\t\t\t}");
					perlSnippet.add("\t\t&printHtmlOverview();");
					perlSnippet.add("\t\t\t#system(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (file="+actFile.getKey()+") file needs a glob definition\\\"\");");
					perlSnippet.add("\t\t\texit(1);");
				}
				perlSnippet.add("\t}");
			}

		}
		
		// zurueckwechseln in das root verzeichnis
		perlSnippet.add("");
		perlSnippet.add("\t&logit(\"debug\", \"changing back to root directory: $pwd\");");
		perlSnippet.add("\tchdir($pwd);");
		
		perlSnippet.add("\t&girlande_stepend($stepname);");
		
		perlSnippet.add("}");

		perlSnippet.add("#-------------------");
		perlSnippet.add("# pradar progress");
		perlSnippet.add("#system(\"pradar progress -process "+this.getParent().getName()+" -id $id -completed \" . ++$stepsCompleted);");
		perlSnippet.add("#-------------------");

		
		return perlSnippet;		
	}
	
	/**
	 * getStepAsPerlScript()
	 * generates a stub of a standalone perlscript that represents this process-step once it is finished by a programmer
	 * @return ArrayList<String> code
	 */
	public ArrayList<String> getStepAsPerlScript(boolean nolist)
	{
		if(this.getWork() == null)
		{
			return new ArrayList<String>();
		}
		
		Boolean allowIntegratedListIfMultiOption = !nolist;
		
		Script script = new Script();
		script.setAuthorMail(this.getParent().getArchitectMail().replaceAll("@", "\\\\@"));
		script.setType("step");
		if(this.getWork() != null)
		{
			script.setDescription(this.getWork().getDescription());
		}
		else
		{
			script.setDescription("no description in 'work' found");
		}
		script.genContent();
		
		// interpreter setzen, falls in work vorhanden
		if((this.getWork() != null) && (this.getWork().getInterpreter() != null) )
		{
			script.setInterpreter(this.getWork().getInterpreter());
		}
		
//		System.out.println(" options : "+callitem.size());

		// 1) Optionen aus Inits, fuer die ein gleichnamiges Callitem existiert. (Inits ohne Callitem werden ignoriert) 
		int reihenfolge = 0;
		ArrayList<String> optionenAusInits = new ArrayList<String>();
		for(Init actInit : this.getInit())
		{
			// daten aus der init-definition
			String name = actInit.getListname();
			int minoccur = actInit.getMinoccur();
			int maxoccur = actInit.getMaxoccur();
			
			// wenn der listname innerhalb der callitems keine erwaehnung finden sollen sie bei der erzeugung
			// von aufrufparametern fuer das perlscript ignoriert werden
			boolean beruecksichtigen = false;
			for(Callitem actCallitem : this.getWork().getCallitem())
			{
				if((actCallitem.getLoop() != null) && (actCallitem.getLoop().matches(actInit.getListname())))
				{
					beruecksichtigen = true;
				}
				if((actCallitem.getPar() != null) && (actCallitem.getPar().matches("^.*\\{\\$" + actInit.getListname() + ".*$"))) {beruecksichtigen = true;}
				if((actCallitem.getDel() != null) && (actCallitem.getDel().matches("^.*\\{\\$" + actInit.getListname() + ".*$"))) {beruecksichtigen = true;}
				if((actCallitem.getVal() != null) && (actCallitem.getVal().matches("^.*\\{\\$" + actInit.getListname() + ".*$"))) {beruecksichtigen = true;}
			}
			// actInit soll im Script NICHT als Aufrufparameter vorhanden sein
			if(!(beruecksichtigen))
			{
//				System.out.println("DIE AKTUELLE LISTE "+ actInit.getListname()  +"SOLL NICHT ALS PARAMETER VERARBEITET WERDEN");
			}

			// actInit soll im Script als Aufrufparameter vorhanden sein
			if (beruecksichtigen)
			{
//				System.out.println("DIE AKTUELLE LISTE "+ actInit.getListname()  +"SOLL ALS PARAMETER VERARBEITET WERDEN");
				String definition = "string";
				// check soll aus den match-elementen des init-elementes extrahiert werden
				// aus der ERSTEN match, dass field=value|absfilename definiert ist, soll das pattern zum checken verwendet werden
				// falls es mehere matches gibt auf die das kriterium zutrifft werden diese ignoriert (wäre noch zu implementieren)
				String check = "";
				// der erste erklaerungstext fuer diese option (z.B. "=FILE")
				// evtl. 
				String text1 = "";
				for(Match actMatch : actInit.getMatch())
				{
					if(actInit.getFromobjecttype().equals("file"))
					{
						text1 = "=FILE";
						definition = "file";
					}
					
					else if(actInit.getFromobjecttype().equals("variable"))
					{
						if(actMatch.getField().equals("value"))
						{
//							// wenn als value konkrete alternativen moeglich sind (mit dem zeichen '|'), sollen diese uebernommen werden fuer string1
//							if(actMatch.getPattern().matches("^[\\w|]+$"))
//							{
//								text1 = actMatch.getPattern();
//								definition = "string";
//
//								// und als check aufnehmen
//								if(check.equals(""))
//								{
//									check = actMatch.getPattern();
//								}
//							}
							// wenn "^\d+$", dann sollen die werte offensichtlich integer sein
							if(actMatch.getPattern().matches("^\\^\\[-\\+\\]\\?\\\\d\\+?\\$$"))
							{
								text1 = "=INTEGER";
								definition = "integer";

								// als check aufnehmen
								if(check.equals(""))
								{
									check = actMatch.getPattern();
								}
							}
	
							// wenn "^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$", dann sollen die werte offensichtlich float sein
							else if(actMatch.getPattern().matches("^\\^\\[-\\+\\]\\?\\[0-9\\]\\*\\\\\\.\\?\\[0-9\\]\\+\\(\\[eE\\]\\[-\\+\\]\\?\\[0-9\\]\\+\\)\\?\\$$"))
							{
								text1 = "=FLOAT";
								definition = "float";

								// als check aufnehmen
								if(check.equals(""))
								{
									check = actMatch.getPattern();
								}
							}
							
							// wenn "^.+$", dann sollen die werte offensichtlich string sein
							else if(actMatch.getPattern().matches("^\\^\\.\\+\\$$"))
							{
								text1 = "=STRING";
								definition = "string";

								// als check aufnehmen
								if(check.equals(""))
								{
									check = actMatch.getPattern();
								}
							}
							
							// wenn ^[^\\+*?{}]+$  Muster ohne quantifier oder metazeichen gefunden wird bsplw. bei "node|element", soll das direkt als text1 verwendet werden
							else if(actMatch.getPattern().matches("^\\^?.*[^\\\\+*?{}].*\\$?$"))
							{
								text1 = "=" + actMatch.getPattern().replace("^", "").replace("$", "");
								definition = "string";
								
								// als check aufnehmen
								if(check.equals(""))
								{
									check = actMatch.getPattern();
								}
							}
							
							// wenn es keiner der bekannten muster ist und es noch keinen check gibt, soll dieser string zum checken verwendet werden
							else if(check.equals(""))
							{
								check = actMatch.getPattern();
							}
	
						}
					}
				}
				
				// wenn in einem callitem ein Par existiert, dass genauso heisst wie dieses init, wird
				// falls ein val existiert 'string' gesetzt, falls kein val existiert 'flag' gesetzt
				for(Callitem act_callitem : this.getWork().getCallitem())
				{
					if(act_callitem.getSequence() != null && !act_callitem.getSequence().equals(""))
					{
						reihenfolge = act_callitem.getSequence();
					}
					
					String par = act_callitem.getPar().replaceAll("^-+", "");
					if(par.equals(name))
					{
						if(act_callitem.getVal() == null || !(act_callitem.getVal().matches(".+")))
						{
							definition = "flag";
						}
						break;
					}
				}
				
				// der default wert fuer diese option
				// aus den items der gleichnamigen listen extrahieren, die hart im xml definiert wurden
				String def = "";
				// wenn es eine liste mit dem gleichen namen gibt wie das init-element, sollen alle defaultitems (trennzeichen=%%) als default gesetzt werden 
				if(this.getList(actInit.getListname()) != null)
				{
					List list = this.getList(actInit.getListname());
					for(String actDefaultitem : list.getDefaultitem())
					{
						if(def.equals("")) {def = actDefaultitem;}
						else{def += "%%" + actDefaultitem;}
					}
				}

				// der hilfstext fuer diese option
				String text2 = "no description available";
				if(!(actInit.getDescription().matches("")))
				{
					text2 = actInit.getDescription();
					text2 = text2.replaceAll("'", "\\\\'");
				}
				
				script.addOption(name, reihenfolge, minoccur, maxoccur, definition, check, def, text1, text2, allowIntegratedListIfMultiOption);
				optionenAusInits.add(name);
			}
//			System.out.println("addoption mit diesen parametern: "+name+minoccur+maxoccur+definition+check+def+text1+text2);
		}
		
		// 2) Optionen aus callitems, fuer die kein gleichnamiges Init existiert
		for(Callitem actCallitem : this.getWork().getCallitem())
		{
			if(!optionenAusInits.contains(actCallitem.getPar().replaceAll("^-+", "")))
			{
				String definition = "";
				int minoccur = 0;
				int maxoccur = 1;
				String check = "";
				String def = ""; // default value
				String text1 = "";
				String text2 = "";
				
				// gibts ein Val, dann ist string (vereinfacht)
				if(actCallitem.getVal() == null)
				{
					definition = "flag";
				}
				// ansonsten ein flag
				else
				{
					definition = "string";
				}
				
				// gibts einen loop, ist maxoccur = 99
				if( (actCallitem.getLoop() != null) && (!actCallitem.getLoop().equals("")) )
				{
					maxoccur = 99;
				}
				script.addOption(actCallitem.getPar().replaceAll("-", ""), ++reihenfolge, minoccur, maxoccur, definition, check, def, text1, text2, allowIntegratedListIfMultiOption);
			}
		}

		return script.getAll();
	}
	
	public boolean areFromstepsFinishedOrCanceled()
	{
		boolean allfinishedOrCanceled = true;
		// alle fromsteps feststellen
		
		for(Step actFromstep : this.getFromsteps())
		{
			// wenn nur einer nicht 'finished' ist, den status auf 'false'
			if (!(actFromstep.getStatus().matches("finished|canceled")))
			{
				allfinishedOrCanceled = false;
				return allfinishedOrCanceled;
			}
		}
		return allfinishedOrCanceled;
	}
	
	/**
	 * tue was auch immer als naechstes getan werden muss
	 */
	public void doIt(String aufrufProcessSyscall, String aufrufProcessStartinstance, String domainInstallationDirectory)
	{
		// status merken
		String statusAtBeginOfDoIt = this.getStatus();
		
		// den flag fuer aenderungen zuruecksetzen
		this.setStatusChangedWhileLastDoIt(false);
		
		if(this.getStatus().equals("finished"))
		{
			return;
		}

		else if(this.getStatus().equals("waiting") || this.getStatus().equals("fanned"))
		{
			// wenn nicht alle fromsteps den status 'finished' haben, wird nichts initialisiert
			if (!(this.areFromstepsFinishedOrCanceled()))
			{
				log("debug", "predecessor step(s) not finished or canceled. initialization postponed.");
				return;
			}

			/**
			 *  wenn root, dann committen
			 */
			if(this.isRoot())
			{
				this.commit();
			}
			// wenn nicht root, dann initialisieren
			else
			{
//				this.mkdir(this.getAbsdir());
				this.initialize();
			}
		}

		// wenn alle fromsteps den status 'finished' haben wird evtl. zuerst 'gefanned'
		else if (this.getStatus().equals("initialized") && this.loop!=null && !(this.loop.equals("")))
		{
			log("debug", "there is a loop -> fanning multistep");
			// flag setzen, dass sich im step etwas geaendert hat
			// dies wird normalerweise automatisch am Ende des this.doIt() erledigt
			// AUSNAHME bei fan, weil der eigentliche step (this) beim fan() aus dem prozess entfernt wird
			// und der Prozess damit nicht erfahren wuerde, dass sich ein stepstatus geaendert hat
			this.setStatusChangedWhileLastDoIt(true);
			
			this.fan();
			return;
		}

		else if(this.getStatus().equals("initialized") && (this.loop==null || this.loop.equals("")))
		{
			if(this.getType().equals("automatic") && (this.getWork() != null))
			{
				log("debug", "step is automatic -> starting work");
				this.work(aufrufProcessSyscall);
			}
			else if(this.getType().equals("process") && (this.getSubprocess() != null))
			{
				log("debug", "step is a process -> calling subprocess");
				this.subprocess(aufrufProcessSyscall, aufrufProcessStartinstance, domainInstallationDirectory);
			}
		}

		else if(this.getStatus().equals("working") && this.getType().equals("automatic"))
		{
			this.work(aufrufProcessSyscall);
		}
		else if(this.getStatus().equals("working") && this.getType().equals("process"))
		{
			this.subprocess(aufrufProcessSyscall, aufrufProcessStartinstance, domainInstallationDirectory);
		}

		if(this.getStatus().equals("worked"))
		{
			this.commit();
		}

		// falls sich nach diesem doIt() der status geaendert hat, soll dies geflaggt werden
		if(!this.getStatus().equals(statusAtBeginOfDoIt))
		{
			this.setStatusChangedWhileLastDoIt(true);
		}
	}

	public void initialize()
	{
		
		this.createStandardEntries();

		// alle listen leeren (damit machen wir den letzten initialization versuch rueckgaengig)
		for(List actList : this.getList())
		{
			actList.clear();
			actList.addItem(actList.getDefaultitem());
		}
		
		// ueber alle inits iterieren und ausfuehren
		for( Init actualInit : this.getInits())
		{
			actualInit.doIt();
		}

	}

	/**
	 * loescht evtl vorhandene Standardenries und erzeugt sie neu
	 * z.B. eine Variable _dir haelt das verzeichnis des steps
	 */
	private void createStandardEntries()
	{
		// standardvariablen setzen
		this.log("info", "create standard variables");

		// ist _dir variable bereits vorhanden?, dann soll sie geloescht werden
		if(this.getVariable("_dir") != null)
		{
			this.removeVariable(this.getVariable("_dir"));
		}

		// das stepdir als variable "_dir" ablegen
		Variable var = new Variable();
		var.setKey("_dir");
		var.setValue(this.getAbsdir());
		this.addVariable(var);
		this.log("info", "create standard variable _dir=" + var.getValue());

		this.log("info", "creation of standard entries finished");
	}
	
	public void fan()
	{
		// lokale liste zur zwischenspeicherung der items
		List looplist = new List();
		
		// den eintrag in loop in eine loopliste ueberfuehren
		// ist da die index-funktion?
		Pattern p = Pattern.compile("^index\\((.+)\\)");
		Matcher m = p.matcher(this.loop);
		if(m.matches())
		{
			String listname = m.group(1);
			this.log("debug", "listname in index(*) is "+listname);
			System.err.println("step-"+this.getName()+": listname in loop=index(<listname>) is "+listname);
			looplist.addItem(this.getIndexesOfListItems(listname));
		}
		// loop enthaelt keine funktion, sondern direkt den namen einer liste
		else
		{
			System.err.println("step-"+this.getName()+": listname in loop="+this.loop);
			looplist.addItem(this.getListItems(this.loop));
		}

		// wenn die loopliste mindestens 1 wert enthaelt, ueber die liste iterieren und fuer jeden wert den aktuellen step clonen
		if (looplist.size() > 0)
		{
			System.err.println("size of looplist: "+looplist.size());
			// cloner erstellen fuer einen deep-copy
//			Cloner cloner = new Cloner();

			int x = 1;
			for(String loopVariable : looplist.getItem())
			{
				System.err.println("fanning for item "+x+": " + loopVariable);
				// einen neuen step erzeugen (klon von this)
				
				// this clonen, allerdings mit moeglichst geringen aufwand
//				Step newstep = cloner.deepClone(this);
				Step newstep = this.clone();

				// setzen der standardentries (die setzung aus der initialisierung ist nicht mehr gueltig,
				// da beim fannen eines steps sich der wert von _dir entsprechend aendern muss
				this.createStandardEntries();
				
				newstep.setLoopvar(loopVariable);
				// den loop fuer einen evtl. spaeteren reset merken
				newstep.setLoopOld(newstep.getLoop());
				newstep.setLoop(null);

				newstep.setName(newstep.getName()+"@"+x);
//				System.err.println("this step '"+newstep.getName()+"' was fanned out from step '"+this.getName()+"'");
				newstep.log("info", "this step '"+newstep.getName()+"' was fanned out from step '"+this.getName()+"'");

				// eine liste mit dem namen 'loop' anlegen und darin die loopvar speichern
				List listLoop = new List();
				listLoop.setName("loopvar");
				listLoop.addItem(loopVariable);
				newstep.addList(listLoop);

				// den neuen step (klon von this) dem prozess hinzufuegen
				this.getParent().addStep(newstep);
				x++;
			}

			// den urspruenglichen step (this) aus dem prozess entfernen
			this.getParent().removeStep(this);

			// parents neu setzen
			this.getParent().affiliate();
			
			// die ranks neu setzen
			this.getParent().setStepRanks();
			
		}

//		System.out.println("anzahl der Steps im Prozess nach dem fanning: "+this.parent.getSteps().size());
	}
	/**
	 * work!
	 * @param aufrufProcessSyscall
	 */
	public void work(String aufrufProcessSyscall)
	{
		// work ausfuehren
		this.getWork().doIt(aufrufProcessSyscall);
	}

	/**
	 * subprocess!
	 * @param aufrufProcessSyscall
	 */
	public void subprocess(String aufrufProcessSyscall, String aufrufProcessManager, String domainInstallationDirectory)
	{
		// subprocess ausfuehren
		try
		{
			this.getSubprocess().doIt(aufrufProcessSyscall, aufrufProcessManager, domainInstallationDirectory);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			log("fatal", e.getMessage());
		}
	}

	/**
	 * commit!
	 */
	public void commit()
	{

		// wenn der step der rootstep ist, dann soll ein spezielles commit durchgefuehrt werden
		if(this.isRoot())
		{
			this.rootCommit();
		}

		// alle commits durchfuehren
		for(Commit actCommit : this.getCommit())
		{
			this.log("debug", "performing commit "+actCommit.getName());
			actCommit.doIt();
		}
	}

	// eine extra methode fuer den step 'root'. es werden alle files/variablen aus 'path' committet
	// es werden standardvariablen committet
	public void rootCommit()
	{
		this.log("info", "special commit, because this step is root");

		// einen commit fuer die initCommitFile und initCommitVariable und standardEintraege, die ProcessInfos beinhalten anlegen
		this.removeCommit("rootCommit");
		this.log("debug", "creating a commit 'rootCommit' and adding to step "+this.getParent().getRootStep().getName());
		Commit rootCommit = new Commit(this);
		rootCommit.setName("rootCommit");
		
		// variable _dir
		Variable variableRootDirectory = new Variable();
		variableRootDirectory.setKey("_dir");
		variableRootDirectory.setValue(this.getAbsdir());
		rootCommit.addVariable(variableRootDirectory);

		// variable _processName
		Variable variableProcessName = new Variable();
		variableProcessName.setKey("_processName");
		variableProcessName.setValue(this.getParent().getName());
		rootCommit.addVariable(variableProcessName);

		// variable _processVersion
		Variable variableProcessVersion = new Variable();
		variableProcessVersion.setKey("_processVersion");
		variableProcessVersion.setValue(this.getParent().getVersion());
		rootCommit.addVariable(variableProcessVersion);

		// variable _processDescription
		Variable variableProcessDescription = new Variable();
		variableProcessDescription.setKey("_processDescription");
		variableProcessDescription.setValue(this.getParent().getDescription());
		rootCommit.addVariable(variableProcessDescription);

		// alle initCommitFile dem rootCommit hinzufuegen
		this.log("info", "start resolving all entries of initCommitFile and adding to the "+rootCommit.getName());
		for(java.io.File actFile : this.getParent().getInitCommitFileAsFile())
		{
			log("debug", "initCommitFile: committing file "+actFile.getAbsolutePath());
				
			File file = new File();
			// als schluessel soll der filenamen verwendet werden
			file.setCategory("processInput");
			file.setKey(actFile.getName());
			file.setGlob(actFile.getAbsolutePath());
			// das file soll nicht in den step importiert werden
			file.setPreservePosition(true);
			rootCommit.addFile(file);
		}
		this.log("info", "end resolving all entries of initCommitFile and adding to the "+rootCommit.getName());

		//ueber alle CommitVariable iterieren
		this.log("info", "resolving all entries of initCommitVariable and adding to the "+rootCommit.getName());

		// alle CommitVariable committen (darf ein directory-pfad oder ein filepfad oder ein glob sein
		for(java.io.File actCommitVariable : this.getParent().getInitCommitVariableDirectoryAsFile())
		{
			Variable variable = new Variable();

			variable.setKey("toBeDetermined");
			variable.setCategory("processInput");

			if(new java.io.File(actCommitVariable.getAbsolutePath()).isDirectory())
			{
				variable.setGlob(actCommitVariable.getAbsolutePath() + "/*");
			}
			else
			{
				variable.setGlob(actCommitVariable.getAbsolutePath());
			}

			this.log("debug", "adding a variable to commit '"+rootCommit.getName()+"' with glob '"+variable.getGlob()+"'" );
			rootCommit.addVariable(variable);
		}
		this.log("info", "end resolving all entries of initCommitVariable and adding to the "+rootCommit.getName());


		// commits durchfuehren

//		this.log("info", "commit standard entries");
//		// das stepdir als variable ablegen
//		Variable var = new Variable();
//		var.setKey("_dir");
//		var.setValue(this.getAbsdir());
//		this.addVariable(var);

		this.log("info", "special commit of step 'root' ended.");
	}

	public boolean areAllCommitsSuccessfull()
	{
		boolean allcommitssuccessfull = true;
		// ueber alle commits iterieren
		Iterator<Commit> itercommit = this.getCommits().iterator();
		while (itercommit.hasNext())
		{
			Commit commit = itercommit.next();
			if (!(commit.getStatus().equals("finished"))) {allcommitssuccessfull = false;}
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
		for(Init actualInit : this.getInit())
		{
			actualInit.setParent(this);
			actualInit.affiliate();
		}
		for(Commit actualCommit : this.getCommit())
		{
			actualCommit.setParent(this);
		}
		for(List actualList : this.getList())
		{
			actualList.setParent(this);
		}
		if(this.getWork() != null)
		{
			this.getWork().setParent(this);
			this.getWork().affiliate();
		}
		if(this.getSubprocess() != null)
		{
			this.getSubprocess().setParent(this);
		}
	}

	/**
	 * resolve
	 * resolves all the entries in the attributes
	 */
	public void resolve()
	{
		// den eintrag im attribut 'description' resolven
		if(this.getDescription() != null && this.getLoopvar() != null)
		{
			this.setDescription(this.getDescription().replaceAll("\\{\\$loopvarstep\\}", this.getLoopvar()));
			this.setDescription(this.resolveString(this.getDescription()));
		}
	}

	/**
	 * resolves all the placeholders and gives back the resolved string
	 */
	public String resolveString(String stringToResolve)
	{
		// ist der string null? return null
		if(stringToResolve == null) {return null;}

		// wenn kein $-Zeichen im string enthalten ist, wird er direkt zurueckgegeben
		if(!stringToResolve.contains("$"))
		{
			log("debug", "nothing to resolve in string '" + stringToResolve + "'");
			return stringToResolve;
		}

		boolean fehler = false;
		String fehlerGrund = "";
		
		log("debug", "1: resolving string "+stringToResolve);

		if(this.getLoopvar() != null)
		{
			log("debug", "loopvar of actual step is: "+this.getLoopvar());
			stringToResolve = stringToResolve.replaceAll("\\{\\$loopvarstep\\}", this.getLoopvar());
			log("debug", "2: resolving string "+stringToResolve);
		}
//		String resolvedString = stringToResolve;

		Pattern p = Pattern.compile("\\{(\\w+)?:?\\$([^${}\\[\\]]+)(\\[([^${}]+)\\])?\\}");
		Matcher m = p.matcher(stringToResolve);

		// group 0 = {root:$x[5]}
		// group 1 = root ODER null
		// group 2 = x
		// group 3 = [5] ODER null
		// group 4 = 5 ODER null
		
		while(m.find())
		{
			// extrahieren des listnamen incl. evtl. index aus dem match
//			System.err.println("full string: "+stringToResolve);
//			System.err.println("group 0: "+m.group(0));
//			System.err.println("group 1: "+m.group(1));
//			System.err.println("group 2: "+m.group(2));
//			System.err.println("group 3: "+m.group(3));
//			System.err.println("group 4: "+m.group(4));

			String vollstaendigerMatch = m.group(0);
			String stepname = m.group(1);
			String listname = m.group(2);
			Integer index = null;
			
			// default index = 0
			if(m.group(4) != null)
			{
				index = Integer.parseInt(this.resolveString(m.group(4)));
			}
			else
			{
				index = 0;
			}

			log("debug", "index="+index);
//			System.err.println("index ist: "+index);

			// die liste raussuchen, die angefordert wurde
			List list = null;
			if(stepname != null)
			{
				Step step = this.getParent().getStep(stepname);
				if(step == null)
				{
					log("error", "step "+stepname+" not found in process ");
//					System.err.println("error: step "+stepname+" not found in process ");
					fehler = true;
					fehlerGrund = "step "+stepname+" not found in process ";
					return stringToResolve;
				}

				list = this.getParent().getStep(stepname).getList(listname);

				if(list == null)
				{
					log("error", "list "+listname+"(in step "+stepname+") not found");
//					System.err.println("error: list "+listname+"(in step "+stepname+") not found");
					fehler = true;
					fehlerGrund = "list "+listname+"(in step "+stepname+") not found";
					return stringToResolve;
				}
			}
			// wenn stepnamen == null ist, gehen wir nicht ueber den umweg prozess
			else
			{
				list = this.getList(listname);
			}
			// wenn wir hier sind, hat bislang alles geklappt
			if (list != null)
			{
				// den platzhalter durch das item ersetzen
				try
				{
					stringToResolve = stringToResolve.replace(m.group(0), list.getItem().get(index));
				}
				catch(IndexOutOfBoundsException e)
				{
					this.log("fatal", "cannot deliver item nr "+index+" from list '"+list.getName()+"'");
//					System.err.println("fatal: cannot deliver item nr "+index+" from list '"+list.getName()+"'");
					this.log("fatal", e.getMessage());
//					System.err.println("fatal: "+ e.getMessage());
					fehler = true;
					fehlerGrund = "cannot deliver item nr "+index+" from list '"+list.getName()+"'";
//					System.exit(1);
				}
			}
			else
			{
				this.log("error", "list '"+listname+"' not found in step '"+this.getName()+"' but needed for resolving.");
//				System.err.println("error: list '"+listname+"' not found in step '"+this.getName()+"' but needed for resolving.");
				fehler = true;
				fehlerGrund = "list '"+listname+"' not found in step '"+this.getName()+"' but needed for resolving.";
//				System.exit(1);
			}
//			System.err.println("fehler? "+fehler);
//			System.err.println("fehlerGrund? "+fehlerGrund);
		}
		
		if(fehler)
		{
			System.err.println(fehlerGrund);
		}
		
		return stringToResolve;
		
//		// wenn fehlerfrei und noch unaufgeloeste eintrage, dann soll weiter resolved werden
//		if( !fehler && stringToResolve.matches("^.*\\{\\$.+\\}.*$") )
//		{
//			return this.resolveString(stringToResolve);
//		}
//		// wenn fehler oder nichts mehr zu resolven, dann zurueckgeben
//		else
//		{
//			return stringToResolve;
//		}
	}

	/**
	 * resets the step because it is dependent from another step that has been resettet
	 * this means that fanned-out steps has to be reduced to their former multistep
	 * 
	 * 1) has another sibling already resetted, then simply delete this
	 * 2) reset all loopdata, name and make a usual reset
	 */
	public void resetBecauseOfDependency()
	{
//		System.err.println("resetBecauseOfDependency");
		// enthaelt der Namen ein '@' so handelt es sich um einen multistep
		// gibt es im prozess einen step, der genauso heisst wie this ohne '@', so hat bereits ein reset stattgefunden und dieser step kann aus prozess entfernt werden
		if(this.getName().matches("^[^@]+@.+$"))
		{
//			System.err.println("resetBecauseOfDependency: enthaelt ein @");
			Pattern p = Pattern.compile("^([^@]+)@.+$");
			Matcher m = p.matcher(this.getName());
			
			String oldName = "unknown";
			
			if(m.find())
			{
//				System.err.println("resetBecauseOfDependency: enthaelt ein @ alter namen ist "+m.group(1));
				oldName = m.group(1);
			}
			
			// wenn es schon einen gibt mit dem urspruenglichen namen
			if(this.getParent().getStep(oldName) != null)
			{
//				System.err.println("resetBecauseOfDependency: enthaelt ein @ alter namen ist "+m.group(1)+" Und den gibt es schon im prozess");
				// es gibt bereits wieder einen step mit Namen 'vor dem fan'
				// raus mit this
				this.reset();
				this.getParent().removeStep(this);
			}
			// wenn es noch keinen gibt mit dem urspruenglichen namen,
			// dann die loopdaten, stepnamen auf alte werte setzen und den ueblichen reset durchfuehren
			else
			{
//				System.err.println("resetBecauseOfDependency: enthaelt ein @ alter namen ist "+m.group(1)+" Und den gibt es NICHT im prozess");
				this.reset();
				this.setLoop(this.getLoopOld());
				this.setLoopOld(null);
				this.setLoopvar(null);
				this.setName(oldName);
			}
		}
		else
		{
//			System.err.println("resetBecauseOfDependency: wird normal resettet: "+this.getName());
			this.reset();
		}
	}

	/**
	 * resets the commits of a step (full reset)
	 * 1) clear variables
	 * 2) clear files
	 * 3) reset commits
	 * @throws IOException 
	 */
	public void resetCommit() throws IOException
	{
		this.log("info", "performing a resetCommit");
		// variablen leeren
		this.getVariable().clear();

		// und alle toRoot committeten aus dem rootStep entfernen
		this.getParent().getRootStep().removeVariable(this.getVariableCommittedToRoot());
		this.getVariableCommittedToRoot().clear();
		
		// files leeren
		this.getFile().clear();

		// und alle toRoot committeten aus dem rootStep entfernen
		this.getParent().getRootStep().removeFile(this.getFileCommittedToRoot());
		this.getFileCommittedToRoot().clear();
		
		// commits reseten
		for(Commit actCommit : this.getCommit())
		{
			actCommit.reset();
		}
		
		// wenn es einen subProcess gibt, dann soll dessen status frisch eingelesen werden
		if(this.getSubprocess() != null)
		{
			this.getSubprocess().getStatus();
		}
	}

	/**
	 * resets the step (full reset)
	 * 1) clear log
	 * 2) clear lists
	 * 3) clear variables
	 * 4) clear files
	 * 5) delete step-directory
	 * 6) reset inits
	 * 7) reset work
	 * 8) reset subprocess
	 * 9) reset commits
	 * 10) hochzaehlen des resetzaehlers
	 */
	public void reset()
	{
		this.log("info", "performing a full reset");
		// root reset ist ausschließlich die daten innerhalb des rootdirs loeschen
		// variablen und files verbleiben im rootStep
		if(this.isRoot())
		{
			// directory, welches gesaeubert werden soll
			Path dir = Paths.get(this.getAbsdir());

			// verzeichnis, welches verschont werden soll
			final Path dirSpare = Paths.get(this.getAbsdir() + "/processInput");
			
			// file, welches verschont werden soll
			final Path fileSpare = Paths.get(this.getAbsdir() + "/process.pmb");
			
			
			try
			{
				Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
					{
						// flag ob das aktuelle verzeichnis geschont werden soll
						boolean flagDirectorySpare = false;

						// called before a directory visit
						public FileVisitResult preVisitDirectory(Path walkingDir, BasicFileAttributes attrs) throws IOException
						{
							// setzen des flags ob aktuelles verzeichnis gesaeubert werden soll oder nicht
							if(walkingDir.equals(dirSpare))
							{
								flagDirectorySpare = true;
							}
							else
							{
								flagDirectorySpare = false;
							}
							return CONTINUE;
						}
						
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
						{
							if(!(file.equals(fileSpare)) &&  !(flagDirectorySpare))
							{
								Files.delete(file);
							}
							return CONTINUE;
						}
					}
				);
			}
			catch (Exception e)
			{
				log("warn", e.getMessage()+"\n"+"problems with deleting old step directory or parts of it: "+this.getAbsdir()+"\n"+Arrays.toString(e.getStackTrace()));
			}
			return;
		}
		else
		{
			// log leeren
			this.getLog().clear();

			// alle listen loeschen, die keine defaultitems enthalten
			// listen mit defaultitems leeren.
			ArrayList<List> toPreserve = new ArrayList<List>();
			log("debug", "listCount before reset-bla: "+this.getList().size());
			for(List actList : this.getList())
			{
				if(!actList.getDefaultitem().isEmpty())
				{
					log("debug", "list: "+actList.getName()+": amount defaultitems: "+actList.getDefaultitem().size());
					actList.clear();
					toPreserve.add(actList);
				}
				else
				{
					log("debug", "list: "+actList.getName()+": no defaultitems");
					actList.clear();
				}
			}
			this.setList(toPreserve);
			log("debug", "listCount after reset-bla: "+this.getList().size());
	
			// variablen leeren
			this.getVariable().clear();
	
			// und alle toRoot committeten aus dem rootStep entfernen
			this.getParent().getRootStep().removeVariable(this.getVariableCommittedToRoot());
			this.getVariableCommittedToRoot().clear();
			
			// files leeren
			this.getFile().clear();
	
			// und alle toRoot committeten aus dem rootStep entfernen
			this.getParent().getRootStep().removeFile(this.getFileCommittedToRoot());
			this.getFileCommittedToRoot().clear();
			
			// inits reseten
			for(Init actInit : this.getInit())
			{
				actInit.reset();
			}
	
			if(this.getWork() != null)
			{
				this.getWork().reset();
			}
	
			if(this.getSubprocess() != null)
			{
				this.getSubprocess().reset();
			}
	
			// commits reseten
			for(Commit actCommit : this.getCommit())
			{
				actCommit.reset();
			}
			
			// directory, welches gesaeubert werden soll
			Path dir = Paths.get(this.getAbsdir());
			
			try
			{
				Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
						{
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
							{
								Files.delete(file);
								return CONTINUE;
							}
							
							public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
							{
								if(exc == null)
								{
									Files.delete(dir);
									return CONTINUE;
								}
								else
								{
									throw exc;
								}
							}
						}
						);
			}
			catch (Exception e)
			{
				log("warn", e.getMessage()+"\n"+"problems with deleting old step directory or parts of it: "+this.getAbsdir()+"\n"+Arrays.toString(e.getStackTrace()));
			}

			this.reset++;
		}
	}

	public String kill()
	{
		String returnStringInfoAboutKills = "";
		
		// wenn es einen subprocess gibt, soll auf dieser gekillt werden
		if(this.subprocess != null)
		{
			this.subprocess.kill();
		}

		// ein kill wird durch 2 aufeinanderfolgende kills ausgefuehrt

		// KILL 1
		// wenn es ein explizites killcommand im work-object gibt, soll dieses ausgefuehrt werden
		// dies wird benoetigt um ein kill programm zu triggern, dass bsplw. eine analyse auf dem hpc abschiesst
		// <killcommand> <killpid>
		if((this.getWork() != null) && (this.getWork().getKillcommand() != null))
		{
			for(Variable actKillPid : this.getVariable("killpid"))
			{
				ArrayList<String> callToKill = new ArrayList<String>();
				callToKill.add(this.getWork().getKillcommand());
				callToKill.add(actKillPid.getValue());

				// log
				this.log("info", "call killcommand: " + StringUtils.join(callToKill, " "));
				returnStringInfoAboutKills += StringUtils.join(callToKill, " ");
				
				// erstellen prozessbuilder
				ProcessBuilder pb = new ProcessBuilder(callToKill);

				// Aufruf taetigen
				try {
					final java.lang.Process sysproc = pb.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					this.log("error", e.getMessage());
					e.printStackTrace();
				}
				
			}
		}
		
		// KILL 2
		// das gestartete programm killen
		// die pid des gestarteten programms feststellen (inhalt des .pid-files)
		java.util.List<String> allLines = null;
		
		java.io.File pidFile = new java.io.File(this.getAbspid());
		
		// wenn pid-file existiert, soll ein kill darauf erzeugt werden
		if(pidFile.exists())
		{
			try
			{
				allLines = org.apache.commons.io.FileUtils.readLines(new java.io.File(this.getAbspid()));
			}
			catch (IOException e1)
			{
				this.log("error", e1.getMessage());
				e1.printStackTrace();
			}
			
			// kill aufruf erzeugen (kill <pid>)
			// array das den aufruf beherbergt
			if(allLines != null && allLines.size() > 0)
			{
				ArrayList<String> callToKill = new ArrayList<String>();
				callToKill.add("kill");
				callToKill.add("-9");
				callToKill.add(allLines.get(0));
				
				// log
				this.log("info", "killing program that has been launched by this step");
				this.log("info", "call: " + StringUtils.join(callToKill, " "));
				returnStringInfoAboutKills += ", " + StringUtils.join(callToKill, " ");
	
				// erstellen prozessbuilder
				ProcessBuilder pb = new ProcessBuilder(callToKill);
	
				// Aufruf taetigen
				try {
					final java.lang.Process sysproc = pb.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					this.log("error", e.getMessage());
					e.printStackTrace();
				}
			}
			else
			{
				this.log("error", "tried to kill, but file does not have the expected content: "+this.getAbspid());
				returnStringInfoAboutKills += ", kill failed";
			}
		}
		
		return returnStringInfoAboutKills;
	}

	/*----------------------------
	  methods add / remove
	----------------------------*/
	public void addInit(Init init)
	{
		this.log("debug", "adding Init "+init.getListname());
		this.init.add(init);
		init.setParent(this);
	}

	public void addCommit(Commit commit)
	{
		this.log("debug", "adding Commit "+commit.getName());
		this.commit.add(commit);
		commit.setParent(this);
	}

	public void removeCommit(Commit commit)
	{
		ArrayList<Commit> commitsWithoutTheRemovedOne = new ArrayList<Commit>();
		for(Commit actCommit : this.getCommit())
		{
			if(!(actCommit.equals(commit)))
			{
				commitsWithoutTheRemovedOne.add(actCommit);
			}
		}
		this.setCommit(commitsWithoutTheRemovedOne);
		this.log("debug", "removing Commit "+commit.getName());
	}

	public void removeCommit(String commitName)
	{
		ArrayList<Commit> commitsWithoutTheRemovedOne = new ArrayList<Commit>();
		for(Commit actCommit : this.getCommit())
		{
			if(!(actCommit.getName().equals(commitName)))
			{
				commitsWithoutTheRemovedOne.add(actCommit);
			}
		}
		this.setCommit(commitsWithoutTheRemovedOne);
		this.log("debug", "removing Commit "+commitName);
	}

	/**
	 * fuegt ein File diesem Step hinzu. Debei wird im File dieser STep als Parent gesetzt
	 * @param file
	 */
	public void addFile(File file)
	{
		file.setParent(this);
		
		// falls das file ueber ein glob definiert ist, soll dieser aufgeloest und das file entsprechend des globs geclont werden
		file.copyIfNeeded();
		this.log("debug", "adding File (key="+file.getKey()+", glob="+file.getGlob()+", filename="+file.getFilename()+", path="+file.getRealposition()+")");
		this.file.add(file);
	}

	/**
	 * hinzufuegen einer ganzen liste an files
	 * @param file
	 */
	public void addFile(ArrayList<File> file)
	{
		for(File actFile : file)
		{
			this.addFile(actFile);
		}
	}

	/**
	 * entfernen eines files
	 * @param file
	 */
	public void removeFile(File fileToRemove)
	{
		this.getFile().remove(fileToRemove);
		try {
			Files.delete(Paths.get(fileToRemove.getAbsfilename()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * entfernen mehrerer files
	 * @param ArrayList<File>
	 */
	public void removeFile(ArrayList<File> filesToRemove)
	{
		for(File actFileToRemove : filesToRemove)
		{
			this.removeFile(actFileToRemove);
		}
	}

	public void addVariable(Variable variable)
	{
		variable.setParent(this);
		this.log("debug", "adding Variable (key="+variable.getKey()+", value="+variable.getValue()+")");
		this.variable.add(variable);
	}
	
	public void addVariable(ArrayList<Variable> variable)
	{
		for(Variable actVariable : variable)
		{
			this.log("debug", "adding Variable (key="+actVariable.getKey()+", value="+actVariable.getValue()+")");
			this.variable.add(actVariable);
		}
	}
	
	/**
	 * entfernen einer variable
	 * @param variable
	 */
	public void removeVariable(Variable variableToRemove)
	{
		this.getVariable().remove(variableToRemove);
	}

	/**
	 * entfernen mehrerer variablen
	 * @param ArrayList<Variable>
	 */
	public void removeVariable(ArrayList<Variable> variablesToRemove)
	{
		this.getVariable().removeAll(variablesToRemove);
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
		// gibt es eine kuerzlich berechneten level? dann soll dieser ausgegeben werden
		if((System.currentTimeMillis() - this.levelCalctime) < 5000)
		{
			return this.level;
		}

		// ansonsten neu berechnen
		ArrayList<Step> fromstepsToExamine = this.getFromsteps();

		int highestLevelOfFromsteps = 0;

		// durchgehen aller fromsteps und den laengsten weg bis root feststellen
		for(Step actualFromstep : fromstepsToExamine)
		{
			Integer level = null;

			level = actualFromstep.getLevel();

			if(level > highestLevelOfFromsteps)
			{
				highestLevelOfFromsteps = level;
			}
		}

		if(this.isRoot())
		{
			System.err.println("debug: step "+this.getName() + " has level: "+0);
			this.level = 0;
			this.levelCalctime = System.currentTimeMillis();
			return 0;
		}
		else
		{
			System.err.println("debug: step "+this.getName() + " has level: "+(highestLevelOfFromsteps+1));
			this.level = (highestLevelOfFromsteps+1);
			this.levelCalctime = System.currentTimeMillis();
			return (highestLevelOfFromsteps+1);
		}
		
		
//		int level = 0;
//		// den hoechsten level aller fromsteps ermitteln
//		for(Step actualStep : allFromsteps)
//		{
//			int rankActualStep = actualStep.getLevel();
//			if (rankActualStep > level)
//			{
//				level = rankActualStep;
//			}
//		}
//		
		// der eigene level ist um 1 hoeher als der hoechste aller fromstep-level (auser beim rootstep)
//		if (!this.isRoot()) 
//		{
//			level++;
//		}

//		return level;
	}
	
	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.addLog(new Log(loglevel, logmessage));
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
		return this.getAbsdir()+"/.stdout.txt";
	}
	
	public String getAbsstderr()
	{
		return this.getAbsdir()+"/.stderr.txt";
	}
	
	public String getAbspid()
	{
		return this.getAbsdir()+"/.pid";
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
			if (actualInit.getListname().equals(initname))
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
			initnames[i] = this.init.get(i).getListname(); 
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
		for(Commit actCommit : this.getCommit())
		{
			if (actCommit.getName().equals(name))
			{
				return actCommit;
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

	/**
	 * @return the statusOverwrite
	 */
	public String getStatusOverwrite() {
		return statusOverwrite;
	}

	/**
	 * @param statusOverwrite the statusOverwrite to set
	 */
	public void setStatusOverwrite(String statusOverwrite) {
		this.statusOverwrite = statusOverwrite;
	}

	public String getStatus()
	{
		if((this.getStatusOverwrite() != null) && (!this.getStatusOverwrite().equals("")))
		{
			this.log("debug", "actual status (because of statusOverwrite) is: "+this.statusOverwrite);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(this.statusOverwrite);
			return this.statusOverwrite;
		}
		
		String status = "unknown";
		
		// bevor unter-objekte nach ihrem status befragt werden, soll festgestellt werden ob aktueller step gecancelled werden soll
		List cancelList = this.getList("_cancel");
		if(cancelList != null && cancelList.itemCount() > 0)
		{
			status = "canceled";
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}

		// Die Inits untersuchen
		ArrayList<String> statusAllInits = new ArrayList<String>(); //waiting/initializing/finished/error
		
		for(Init actInit : this.getInit())
		{
			statusAllInits.add(actInit.getStatus());
		}

		// ist der status 'error' vorhanden? prozess=error
		if(statusAllInits.contains("error"))
		{
			status = "error";
//			this.log("debug", "actual status is: "+status);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}

		// wenn schluessel initializing vorhanden ist, dann gilt 'initializing'
		else if(  statusAllInits.contains("initializing")  )
		{
			status = "initializing";
//			this.log("debug", "actual status is: "+status);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}

		// wenn schluessel waiting vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'waiting'
		else if(  statusAllInits.contains("waiting") )
		{
			status = "waiting";
//			this.log("debug", "actual status is: "+status);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}
		
		// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'finished'
		else if(  statusAllInits.contains("finished") )
		{
			status = "initialized";
		}

		// Work untersuchen, falls vorhanden
		if(this.work != null)
		{
			// ist der status 'error' vorhanden? prozess=error
			if(this.work.getStatus().equals("error"))
			{
				status = "error";
//				this.log("debug", "actual status is: "+status);
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}
	
			// wenn schluessel working vorhanden ist, dann gilt 'working'
			else if( this.work.getStatus().equals("working")  )
			{
				status = "working";
//				this.log("debug", "actual status is: "+status);
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}

			// wenn schluessel waiting vorhanden ist, dann gilt 'waiting'
			else if(  this.work.getStatus().equals("waiting") && statusAllInits.isEmpty())
			{
				status = "waiting";
//				this.log("debug", "actual status is: "+status);
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}

			// wenn work finished ist, aber noch Commits vorhanden sind => worked
			else if(  this.work.getStatus().equals("finished") && !this.getCommit().isEmpty() )
			{
				status = "worked";
			}

			// wenn work finished ist, und keine Commits vorhanden sind => finished
			else if(  this.work.getStatus().equals("finished") && this.getCommit().isEmpty() )
			{
				status = "finished";
			}
		}

		// Subprocess untersuchen, falls vorhanden
		if(this.subprocess != null)
		{
			// ist der status 'error' vorhanden? prozess=error
			if(this.subprocess.getStatus().equals("error"))
			{
				status = "error";
//				this.log("debug", "actual status is: "+status);
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}
	
			// wenn schluessel working vorhanden ist, dann gilt 'working'
			else if( this.subprocess.getStatus().equals("working")  )
			{
				status = "working";
//				this.log("debug", "actual status is: "+status);
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}

			// wenn schluessel waiting vorhanden ist, dann gilt 'waiting'
			else if(  this.subprocess.getStatus().equals("waiting") && statusAllInits.isEmpty())
			{
				status = "waiting";
//				this.log("debug", "actual status is: "+status);
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}

			// wenn subprocess finished und noch Commits vorhanden sind => worked
			else if(  this.subprocess.getStatus().equals("finished") && !this.getCommit().isEmpty())
			{
				status = "worked";
				// kein return, da der status der commits den stepStatus noch beeinflussen koennen
			}

			// wenn subprocess finished und keine Commits vorhanden sind => finished
			else if(  this.subprocess.getStatus().equals("finished") && this.getCommit().isEmpty())
			{
				status = "finished";
				this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
				return status;
			}
			
		}

		// Die Commits untersuchen
		ArrayList<String> statusAllCommits = new ArrayList<String>(); //waiting/initializing/finished/error

		for(Commit actCommit : this.getCommit())
		{
			statusAllCommits.add(actCommit.getStatus());
		}

		// ist der status 'error' vorhanden, dann gilt 'error'
		if(statusAllCommits.contains("error"))
		{
			status = "error";
//			this.log("debug", "actual status is: "+status);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}

		// wenn schluessel committing vorhanden ist, dann gilt 'committing'
		else if(  statusAllCommits.contains("committing")  )
		{
			status = "committing";
//			this.log("debug", "actual status is: "+status);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}

		// wenn schluessel waiting vorhanden ist, dann gilt 'waiting'
		else if(  statusAllCommits.contains("waiting")  && statusAllInits.isEmpty()  &&  (this.work == null && this.subprocess ==null))
		{
			status = "waiting";
//			this.log("debug", "actual status is: "+status);
			this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
			return status;
		}

		// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'finished'
		else if(  statusAllCommits.contains("finished") )
		{
			status = "finished";
		}

//		this.log("debug", "actual status is: "+status);

		// in der timeSerie festhalten, falls status sich veraendert hat
		this.getTimeSerieStatus().addValueIfDiffersFromLast(status);
		return status;
	}

	/**
	 * @return the parent
	 */
	public Process getParent()
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

	public String getAbsdir()
	{
		String absDir = "";

		if (this.isRoot())
		{
			absDir = this.getParent().getRootdir();
		}
		else if (this.getParent().isWrapper())
		{
			absDir = this.getParent().getRootdir();
		}
		else
		{
			absDir = this.getParent().getRootdir()+"/dir4step_"+this.getName();
			if(this.reset > 0)
			{
				absDir += "_r" + this.reset;
			}
		}
		return absDir;
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

	public List getList(String listname)
	{
		for(List actualList : this.list)
		{
			if (actualList.getName().equals(listname))
			{
				return actualList;
			}
		}
		return null;
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
		if(this.getList(listname) != null)
		{
			return this.getList(listname).getItem();
		}
		else
		{
			return null;
		}
	}

	/**
	 * 
	 * @param listname
	 * @return
	 */
	public ArrayList<String> getIndexesOfListItems(String listname)
	{
		ArrayList<String> indexesOfListItems = new ArrayList<String>();
		if(this.getList(listname) != null)
		{
			ArrayList<String> allItemsOfList = this.getList(listname).getItem();
			for(String actItem : allItemsOfList)
				{
					indexesOfListItems.add(""+allItemsOfList.indexOf(actItem));
				}
			
			return indexesOfListItems;
		}
		else
		{
			return null;
		}
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
			ArrayList<Step> steps = this.getParent().getSteps(actualInit.getFromstep());
//			System.out.println("my parent is: "+this.parent.toString());
//			System.out.println("anzahl der fromsteps im aktuellen init: "+steps.size());
//			System.out.println("actualInit: "+actualInit.getName()+" || fromStep: "+actualInit.getFromstep());
			// nur die noch nicht als fromstep erkannten steps der suchliste hinzufuegen
			for(Step actualStep : steps)
			{
				// wenn die sammelliste den actualStep noch nicht enthaelt und er auch nicht this ist, dann hinzufuegen 
				if (!(fromsteps.contains(actualStep)) && actualStep != this)
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
	
	/**
	 * returns all Files that have a certain key
	 * @param key
	 * @return
	 */
	public ArrayList<File> getFile(String key)
	{
		ArrayList<File> fileOfCertainKey = new ArrayList<File>();
		for(File actFile : this.file)
		{
			if(actFile.getKey().equals(key))
			{
				fileOfCertainKey.add(actFile);
			}
		}
		return fileOfCertainKey;
	}
		
	public void setFile(ArrayList<File> file)
	{
		this.file = file;
	}
	
	/**
	 * returns all existent keys of all Variables stored in this step
	 * duplicates are possible
	 * @return
	 */
	public ArrayList<String> getVariableKeys()
	{
		ArrayList<String> existKeysInVariables = new ArrayList<String>();
		for(Variable actVariable : this.getVariable())
		{
			existKeysInVariables.add(actVariable.getKey());
		}

		return existKeysInVariables;
	}
		
	public ArrayList<Variable> getVariable()
	{
		return this.variable;
	}

	/**
	 * returns all Variables that have a certain key
	 * @param key
	 * @return
	 */
	public ArrayList<Variable> getVariable(String key)
	{
		ArrayList<Variable> variableOfCertainKey = new ArrayList<Variable>();
		for(Variable actVariable : this.variable)
		{
			if(actVariable.getKey().equals(key))
			{
				variableOfCertainKey.add(actVariable);
			}
		}
		return variableOfCertainKey;
	}

	public void setVariable(ArrayList<Variable> variable)
	{
		this.variable = variable;
	}
		
	public void addDefaultlist(List list)
	{
		this.defaultlist.add(list);
	}
		
	public void addList(List list)
	{
		this.list.add(list);
		list.setParent(this);
	}
		
	public void removeList(List list)
	{
		this.getList().remove(list);
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
	
	public boolean isAFannedMultistep()
	{
		if(this.getName().matches("^.+@.+$"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * is this a fanned multistep?
	 * and is it the only one?
	 * both yes? -> then true
	 * @return
	 */
	public boolean isAFannedMultistepLast()
	{
		Pattern p = Pattern.compile("^([^@]+)@.+$");
		Matcher m = p.matcher(this.getName());

		if(m.matches())
		{
			// alle verfuegbaren steps durchgehen, falls es noch einen gibt mit dem gleichen basename -> return false
			for(Step actStep : this.getParent().getStep())
			{
				if(!actStep.equals(this))
				{
					Matcher m2 = p.matcher(actStep.getName());
					if(m2.matches())
					{
						if(m.group(1).equals(m2.group(1)))
						{
							return false;
						}
					}
				}
			}
			
			// gab keinen mit gleichem namen -> return true
			return true;
		}
		// kein fanned multistep -> return false
		else
		{
			return false;
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
		// in diesem step die logs entfernen
		this.getLog().clear();
		
		// alle logs aller Variablen clearen
		for(Variable actVariable : this.variable)
		{
			actVariable.getLog().clear();
		}
		// alle logs aller Files clearen
		for(File actFile : this.file)
		{
			actFile.getLog().clear();
		}

		// die logs aller Inits clearen
		for(Init actInit : this.getInit())
		{
			actInit.clearLogRecursive();
		}

		// die logs des Work clearen
		if( this.work != null )
		{
			work.clearLogRecursive();
		}

		// die logs des Subprocess clearen
		if( this.subprocess != null )
		{
			subprocess.getLog().clear();
		}

		// die logs aller Commits clearen
		for(Commit actCommit : this.getCommit())
		{
			actCommit.clearLogRecursive();
		}

	}	
	
	/**
	 * relocates the logmessages to files if stepdirectory already exists
	 * @param
	 */
	public void logRelocate()
	{
		java.io.File stepDir = new java.io.File(this.getAbsdir());
		if(!stepDir.exists())
		{
			return;
		}
				
		try
		{
			// Filewriter initialisieren
			FileWriter logWriter =new FileWriter(this.getAbsdir() + "/.debug", true);
			
			// alle logs aller unterobjekte extrahieren
			ArrayList<Log> allLogs = this.getLogRecursive();
			
			// jedes log schreiben
			for(Log actLog : allLogs)
			{
				logWriter.write(actLog.sprint() + "\n");
			}
			
			// Filewriter schliessen
			logWriter.close();
			
			// die logs recursiv aus allen objekten entfernen
			// bei root zusaetzlich das processlogging
			if(this.isRoot())
			{
				this.getParent().getLog().clear();
			}
			this.clearLogRecursive();
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * returns all logs from all elements sorted by time
	 * if deleteLogsInObjects==true the logs will be emptied
	 * @return
	 */
	public ArrayList<Log> getLogRecursive()
	{
		// zuerst das eigene log kopieren
		ArrayList<Log> logRecursive = this.getLog();
		
		// alle logs aller Variablen hinzufuegen
		for(Variable actVariable : this.variable)
		{
			logRecursive.addAll(actVariable.getLog());
		}
		// alle logs aller Files hinzufuegen
		for(File actFile : this.file)
		{
			logRecursive.addAll(actFile.getLog());
		}

		// wenn this root ist, soll das logging von process mitgenommen werden
		if(this.isRoot())
		{
			logRecursive.addAll(this.getParent().getLog());
		}
		
		// die logs aller Inits in die Sammlung uebernehmen
		for(Init actInit : this.getInit())
		{
			logRecursive.addAll(actInit.getLogRecursive());
		}

		// die logs des Work in die Sammlung uebernehmen
		if( this.work != null )
		{
			logRecursive.addAll(work.getLogRecursive());
		}

		// die logs des Subprocess in die Sammlung uebernehmen
		if( this.subprocess != null )
		{
			logRecursive.addAll(subprocess.getLog());
		}

		// die logs aller Commits in die Sammlung uebernehmen
		for(Commit actCommit : this.getCommit())
		{
			logRecursive.addAll(actCommit.getLogRecursive());
		}

		// sortierte KeyListe erstellen
		java.util.Collections.sort(logRecursive);
		
		return logRecursive;
	}
	
	public String getRank()
	{
		return this.rank;
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
	
//	public void setStatus(String status)
//	{
//		log("info", "setting status to '"+status+"'");
//		this.status = status;
//	}
	
	public void setParent(Process process)
	{
		this.parent = process;
	}
	
	public void setWork(Work work)
	{
		this.work = work;
		work.setParent(this);
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
	
	public void setRank(String rank)
	{
		this.rank = rank;
	}

	public boolean isRoot()
	{
		boolean iAmRoot = false;
		if(this.getParent().getRootstepname().equals(this.getName()))
		{
			iAmRoot = true;
		}
		return iAmRoot;
	}

	/**
	 * @return the defaultlist
	 */
	public ArrayList<List> getDefaultlist() {
		return defaultlist;
	}

	/**
	 * @param defaultlist the defaultlist to set
	 */
	public void setDefaultlist(ArrayList<List> defaultlist) {
		this.defaultlist = defaultlist;
	}

	public float getProgress()
	{
		String status = this.getStatus();
		if(status.equals("waiting"))
		{
			return 0f;
		}
		else if((status.equals("initializing")) || (status.equals("initialized")) || (status.equals("fanning")) || (status.equals("fanned")))
		{
			return 0.3f;
		}
		else if((!status.equals("working")) || (!status.equals("worked")))
		{
			return 0.6f;
		}
		else if(status.equals("finished"))
		{
			return 1f;
		}
		else if(status.equals("error"))
		{
			return 0f;
		}
		return 1.1f;
	}
	/*----------------------------
	  methods consistent
	----------------------------*/

	/**
	 * checks whether the content of step is consistent
	 * @return result
	 */
	public boolean isStepConsistent()
	{
		boolean result = true;

		// check all inits
		for (Init actualInit : this.getInit())
		{
			if ( !actualInit.isInitConsistent() ) {result = false;	this.getParent().log("error", "error in init '"+actualInit.getListname()+"'");}
		}
		
		return result;
	}

	/**
	 * @return the subprocess
	 */
	public Subprocess getSubprocess() {
		return subprocess;
	}

	/**
	 * @param subprocess the subprocess to set
	 */
	public void setSubprocess(Subprocess subprocess) {
		this.subprocess = subprocess;
		subprocess.setParent(this);
	}

	/**
	 * @return the loopOld
	 */
	public String getLoopOld() {
		return loopOld;
	}

	/**
	 * @param loopOld the loopOld to set
	 */
	public void setLoopOld(String loopOld) {
		this.loopOld = loopOld;
	}

	/**
	 * @return the reset
	 */
	public int getReset() {
		return reset;
	}

	/**
	 * @param reset the reset to set
	 */
	public void setReset(int reset) {
		this.reset = reset;
	}

	/**
	 * @return the levelCalctime
	 */
	public long getLevelCalctime() {
		return levelCalctime;
	}

	/**
	 * @param levelCalctime the levelCalctime to set
	 */
	public void setLevelCalctime(long levelCalctime) {
		this.levelCalctime = levelCalctime;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the fileCommittedToRoot
	 */
	public ArrayList<File> getFileCommittedToRoot() {
		return fileCommittedToRoot;
	}

	/**
	 * @param fileCommittedToRoot the fileCommittedToRoot to set
	 */
	public void setFileCommittedToRoot(ArrayList<File> fileCommittedToRoot) {
		this.fileCommittedToRoot = fileCommittedToRoot;
	}

	/**
	 * @return the variableCommittedToRoot
	 */
	public ArrayList<Variable> getVariableCommittedToRoot() {
		return variableCommittedToRoot;
	}

	/**
	 * @param variableCommittedToRoot the variableCommittedToRoot to set
	 */
	public void setVariableCommittedToRoot(ArrayList<Variable> variableCommittedToRoot) {
		this.variableCommittedToRoot = variableCommittedToRoot;
	}

	/**
	 * @return the timeSerieStatus
	 */
	public Timeserie getTimeSerieStatus() {
		return timeSerieStatus;
	}

	/**
	 * @param timeSerieStatus the timeSerieStatus to set
	 */
	public void setTimeSerieStatus(Timeserie timeSerieStatus) {
		this.timeSerieStatus = timeSerieStatus;
	}

	/**
	 * @return the statusChangedWhileLastDoIt
	 */
	public boolean isStatusChangedWhileLastDoIt() {
		return statusChangedWhileLastDoIt;
	}

	/**
	 * @param statusChangedWhileLastDoIt the statusChangedWhileLastDoIt to set
	 */
	public void setStatusChangedWhileLastDoIt(boolean statusChangedWhileLastDoIt) {
		this.statusChangedWhileLastDoIt = statusChangedWhileLastDoIt;
	}

	/**
	 * @return the stepStartDelayMinutes
	 */
	public Integer getStepStartDelayMinutes() {
		return stepStartDelayMinutes;
	}

	/**
	 * @param stepStartDelayMinutes the stepStartDelayMinutes to set
	 */
	public void setStepStartDelayMinutes(Integer stepStartDelayMinutes) {
		this.stepStartDelayMinutes = stepStartDelayMinutes;
	}

}
