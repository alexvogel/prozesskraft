package de.caegroup.process;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.caegroup.codegen.Script;
import de.caegroup.process.Commit;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import com.rits.cloning.Cloner;

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
	private ArrayList<List> list = new ArrayList<List>();
	private ArrayList<List> defaultlist = new ArrayList<List>();

	private ArrayList<Init> init = new ArrayList<Init>();
	private Work work = null;
	private Subprocess subprocess = null;
	private ArrayList<Commit> commit = new ArrayList<Commit>();
	private String loop = null;
	private String loopOld = null;
	private String loopvar = null;

	private Process parent = null;
//	private String dir = new String();
//	private String absdir = new String();
//	private String abspid = new String();
//	private String absstdout = new String();
//	private String absstderr = new String();
	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Variable> variable = new ArrayList<Variable>();
//	private String status = "waiting";	// waiting/initializing/working/committing/ finished/error/cancelled

	public String statusOverwrite = null;

	private ArrayList<Log> log = new ArrayList<Log>();
	private String rank = "";
	private int reset = 0;
	
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
		this.setParent(new Process());
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
		return SerializationUtils.clone(this);
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
		perlSnippet.add("if (!($COMMAND{'" + this.getName() + "'} = &commandResolve(\"" + this.getWork().getCommand() + "\")))");
		perlSnippet.add("{");
		perlSnippet.add("	&logit(\"fatal\", \"cannot determine what program to call for step '" + this.getName() + "'. " + this.getWork().getCommand() + " neither found in <installdir>/bin nor by calling 'which'.\");");
		perlSnippet.add("	my $PROCESS_STOP = scalar(localtime());");
		perlSnippet.add("	exit(1);");
		perlSnippet.add("}");
		perlSnippet.add("");
		perlSnippet.add("&logit(\"debug\", \"command for step '" + this.getName() + "' is: $COMMAND{'" + this.getName() + "'}\");");
		perlSnippet.add("");
		
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
				perlSnippet.add("\t\tmy @"+actInit.getListname()+";");
				perlSnippet.add("\t\t$allLists{'"+actInit.getListname()+"'} = \\@"+actInit.getListname()+";");
			}
				
			// ein array of hashes mit allen matches anlegen
			perlSnippet.add("");
			perlSnippet.add("\t\t# create an array of hashes with all matches");
			perlSnippet.add("\t\tmy @matches_"+actInit.getListname()+";");
			ArrayList<Match> matchesOfInit = actInit.getMatch();
			for(int x=0; x < matchesOfInit.size(); x++)
			{
				perlSnippet.add("\t\tmy %match_"+actInit.getListname()+"_"+x+";");
				perlSnippet.add("\t\t$match_"+actInit.getListname()+"_"+x+"{'field'} = \'"+matchesOfInit.get(x).getField()+"\';");
				perlSnippet.add("\t\t$match_"+actInit.getListname()+"_"+x+"{'pattern'} = \'"+matchesOfInit.get(x).getPattern()+"\';");
				perlSnippet.add("\t\tpush @matches_"+actInit.getListname()+", \\%match_"+actInit.getListname()+"_"+x+";");
			}
			
			// liste initialisieren / anreichern
			perlSnippet.add("");
			perlSnippet.add("\t\t# initialize list");
			perlSnippet.add("\t\t# initlist (1=fromobjecttype 2=returnfield 3=fromstep 4=insertrule 5=minoccur 6=maxoccur 7=refARRAYmatch 8=refARRAYlist 9=refHASHvariable 10=refHASHfile)");
			perlSnippet.add("\t\t&logit(\"info\", \"step '$stepname' initializes list '"+actInit.getListname()+"' with data from step '"+actInit.getFromstep()+"'\");");
			perlSnippet.add("\t\t&initlist('"+actInit.getFromobjecttype()+"', '"+actInit.getReturnfield()+"', '"+actInit.getFromstep()+"', '"+actInit.getInsertrule()+"', "+actInit.getMinoccur()+", "+actInit.getMaxoccur()+", \\@matches_"+actInit.getListname()+", \\@"+actInit.getListname()+", $VARIABLE{'"+actInit.getFromstep()+"'}, $FILE{'"+actInit.getFromstep()+"'});");
			
			perlSnippet.add("\t}");
		}
		
		// call erzeugen
		perlSnippet.add("");
		perlSnippet.add("\t# create call for command");
		perlSnippet.add("\tmy $call = $COMMAND{'" + this.getName() + "'};");
		perlSnippet.add("");
		
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
					perlSnippet.add("\t\tmy $value = \""+actVariable.getValue()+"\";");
					perlSnippet.add("\t\t$value = &resolve($value, \\%allLists);");
					perlSnippet.add("\t\tpush (@{$VARIABLE{'"+this.getName()+"'}}, [\""+actVariable.getKey()+"\", $value]);");
					perlSnippet.add("\t\t&logit(\"info\", \""+actVariable.getKey()+"=$value\");");
				}
				else if(!(actVariable.getGlob() == null))
				{
					String tmpString = actVariable.getGlob();
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
		Boolean allowIntegratedListIfMultiOption = !nolist;
		
		Script script = new Script();
		script.setType("step");
		script.genContent();
		
//		System.out.println(" options : "+callitem.size());

		int reihenfolge = 0;
		for(Init actInit : this.getInit())
		{
			reihenfolge++;
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
				if(actCallitem.getDel().matches("^.*\\{\\$" + actInit.getListname() + ".*$")) {beruecksichtigen = true;}
				if(actCallitem.getVal().matches("^.*\\{\\$" + actInit.getListname() + ".*$")) {beruecksichtigen = true;}
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
							// wenn "^\d+$", dann sollen die werte offensichtlich integer sein
							if(actMatch.getPattern().matches("^\\^\\\\d\\+?\\$$"))
							{
								text1 = "=INTEGER";
								definition = "integer";
							}
	
							// wenn "^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?$", dann sollen die werte offensichtlich float sein
							else if(actMatch.getPattern().matches("^\\^\\[-\\+\\]\\?\\[0-9\\]\\*\\\\\\.\\?\\[0-9\\]\\+\\(\\[eE\\]\\[-\\+\\]\\?\\[0-9\\]\\+\\)\\?\\$$"))
							{
								text1 = "=FLOAT";
								definition = "float";
							}
							
							// wenn "^.+$", dann sollen die werte offensichtlich string sein
							else if(actMatch.getPattern().matches("^\\^\\.\\+\\$$"))
							{
								text1 = "=STRING";
								definition = "string";
							}
							
							// wenn ^[^\\+*?{}]+$  Muster ohne quantifier oder metazeichen gefunden wird bsplw. bei "node|element", soll das direkt als text1 verwendet werden
							else if(actMatch.getPattern().matches("^\\^.*[^\\\\+*?{}].*\\$$"))
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
				for(Callitem act_callitem : this.work.getCallitem())
				{
					String par = act_callitem.getPar().replaceAll("-", "");
					if(par.equals(name))
					{
						if(!(act_callitem.getVal().matches(".+")))
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
			}
//			System.out.println("addoption mit diesen parametern: "+name+minoccur+maxoccur+definition+check+def+text1+text2);
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
			this.fan();
		}

		else if(this.getStatus().equals("initialized") && (this.loop==null || this.loop.equals("")))
		{
			if(this.getType().equals("automatic") && (this.getWork() != null))
			{
				this.work(aufrufProcessSyscall);
			}
			else if(this.getType().equals("process") && (this.getSubprocess() != null))
			{
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

	}

	public void initialize()
	{
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
//			System.err.println("step-"+this.getName()+": listname in loop=index(<listname>) is "+listname);
			looplist.addItem(this.getIndexesOfListItems(listname));
		}
		// loop enthaelt keine funktion, sondern direkt den namen einer liste
		else
		{
			looplist.addItem(this.getListItems(this.loop));
		}
		
		// wenn die loopliste mindestens 1 wert enthaelt, ueber dioe liste iterieren und fuer jeden wert den aktuellen step clonen
		if (looplist.size() > 0)
		{
			// cloner erstellen fuer einen deep-copy
			Cloner cloner = new Cloner();
			int x = 1;
			for(String loopVariable : looplist.getItem())
			{
				// einen neuen step erzeugen (klon von this)
				Step newstep = cloner.deepClone(this);
				newstep.setLoopvar(loopVariable);
				// den loop fuer einen evtl. spaeteren reset merken
				newstep.setLoopOld(newstep.getLoop());
				newstep.setLoop(null);

				newstep.setName(newstep.getName()+"@"+x);
				newstep.log("info", "this step '"+newstep.getName()+"' was fanned out from step '"+this.getName()+"'");

				// eine liste mit dem namen 'loop' anlegen und darin die loopvar speichern
				List listLoop = new List();
				listLoop.setName("loopvar");
				listLoop.addItem(loopVariable);
				newstep.addList(listLoop);

				// den neuen step (klon von this) dem prozess hinzufuegen
				this.parent.getStep().add(newstep);
				x++;
			}
			
			// den urspruenglichen step (this) aus dem prozess entfernen
			this.parent.getStep().remove(this);
			
			// die ranks neu setzen
			this.parent.setStepRanks();
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

		this.log("info", "commit standard entries");

		ArrayList<String> existKeysInVariables = this.getVariableKeys();
		// das stepdir als variable "_dir" ablegen, falls noch nicht vorhanden
		if(!existKeysInVariables.contains("_dir"))
		{
			Variable var = new Variable();
			var.setKey("_dir");
			var.setValue(this.getAbsdir());
			this.addVariable(var);
		}

		this.log("info", "commit standard entries finished");

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

		// einen commit fuer die initCommitFile und initCommitVariable anlegen
		this.removeCommit("rootCommit");
		this.log("debug", "creating a commit 'rootCommit' and adding to step "+this.getParent().getRootStep().getName());
		Commit rootCommit = new Commit(this);
		rootCommit.setName("rootCommit");
		
		//ueber alle commitFile iterieren
		this.log("info", "start resolving all entries of initCommitFile and adding to the "+rootCommit.getName());
		for(java.io.File actInitCommitFile : this.getParent().getInitCommitFiles2())
		{
			if(actInitCommitFile.isDirectory())
			{
				log("info", "committing all files in directory: "+actInitCommitFile.getAbsolutePath());
				
				for(java.io.File actFile : actInitCommitFile.listFiles())
				{
					if(actFile.isDirectory())
					{
						log("info", "skipping because it is a directory "+actFile.getAbsolutePath());
					}
					else
					{
						File file = new File();
						// als schluessel soll der filenamen verwendet werden
						file.setCategory("processInput");
						file.setKey(actFile.getName());
						file.setGlob(actFile.getAbsolutePath());
						rootCommit.addFile(file);
					}
				}
			}
		}
		this.log("info", "end resolving all entries of initCommitFile and adding to the "+rootCommit.getName());

		//ueber alle CommitVariable iterieren
		this.log("info", "resolving all entries of initCommitVariable and adding to the "+rootCommit.getName());

		// alle CommitVariable committen (darf ein directory-pfad oder ein filepfad oder ein glob sein
		for(java.io.File actCommitVariable : this.getParent().getInitCommitVariables2())
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
	 * resets the step because it is dependent from another step that has been resetete
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
	 * resets the step
	 * 1) clear log
	 * 2) clear lists
	 * 3) clear variables
	 * 4) clear files
	 * 5) delete step-directory
	 * 6) hochzaehlen des resetzaehlers
	 */
	public void reset()
	{
		// root reset ist ausschließlich die daten innerhalb des rootdirs loeschen
		if(this.isRoot())
		{
			// delete stepdirectory
			Path dir = Paths.get(this.getAbsdir());
//			Path pathProcessFile = Paths.get(this.getAbsdir()+"/process.pmb");
			try
			{
				Files.walkFileTree(dir, new SimpleFileVisitor<Path>()
						{
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
							{
								if(!(file.endsWith("process.pmb")))
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
				}
			}
			this.setList(toPreserve);
			log("debug", "listCount after reset-bla: "+this.getList().size());
	
			// variablen leeren
			this.getVariable().clear();
	
			// files leeren
			this.getFile().clear();
	
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
			
			// delete stepdirectory
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

//	/**
//	 * resets the this step and all dependent steps
//	 */
//	public void resetIncDependent()
//	{
//		this.getParent().resetStep(this.getName());
//	}
	/*----------------------------
	  methods add / remove
	----------------------------*/
	public void addInit(Init init)
	{
		this.log("debug", "adding Init "+init.getListname());
		this.init.add(init);
	}

	public void addCommit(Commit commit)
	{
		this.commit.add(commit);
		commit.setParent(this);
		this.log("debug", "adding Commit "+commit.getName());
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
			
			for(Step actualFromstep : allFromsteps)
			{
				ArrayList<Step> fromstepsOfActualStep = actualFromstep.getFromsteps();
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
		if (!this.isRoot()) 
		{
			level++;
		}
		return level;
	}
	
	/**
	 * stores a message in the object log
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log("step-"+this.getName(), loglevel, logmessage));
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
			return this.statusOverwrite;
		}
		
		String status = "unknown";
		
		// bevor unter-objekte nach ihrem status befragt werden, soll festgestellt werden ob aktueller step gecancelled werden soll
		List cancelList = this.getList("_cancel");
		if(cancelList != null && cancelList.itemCount() > 0)
		{
			return "canceled";
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
			return status;
		}

		// wenn schluessel initializing vorhanden ist, dann gilt 'initializing'
		else if(  statusAllInits.contains("initializing")  )
		{
			status = "initializing";
//			this.log("debug", "actual status is: "+status);
			return status;
		}

		// wenn schluessel waiting vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'waiting'
		else if(  statusAllInits.contains("waiting") )
		{
			status = "waiting";
//			this.log("debug", "actual status is: "+status);
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
				return status;
			}
	
			// wenn schluessel working vorhanden ist, dann gilt 'working'
			else if( this.work.getStatus().equals("working")  )
			{
				status = "working";
//				this.log("debug", "actual status is: "+status);
				return status;
			}

			// wenn schluessel waiting vorhanden ist, dann gilt 'waiting'
			else if(  this.work.getStatus().equals("waiting") && statusAllInits.isEmpty())
			{
				status = "waiting";
//				this.log("debug", "actual status is: "+status);
				return status;
			}

			// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'finished'
			else if(  this.work.getStatus().equals("finished") )
			{
				status = "worked";
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
				return status;
			}
	
			// wenn schluessel working vorhanden ist, dann gilt 'working'
			else if( this.subprocess.getStatus().equals("working")  )
			{
				status = "working";
//				this.log("debug", "actual status is: "+status);
				return status;
			}

			// wenn schluessel waiting vorhanden ist, dann gilt 'waiting'
			else if(  this.subprocess.getStatus().equals("waiting") && statusAllInits.isEmpty())
			{
				status = "waiting";
//				this.log("debug", "actual status is: "+status);
				return status;
			}

			// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'finished'
			else if(  this.subprocess.getStatus().equals("finished") )
			{
				status = "worked";
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
			return status;
		}

		// wenn schluessel committing vorhanden ist, dann gilt 'committing'
		else if(  statusAllCommits.contains("committing")  )
		{
			status = "committing";
//			this.log("debug", "actual status is: "+status);
			return status;
		}

		// wenn schluessel waiting vorhanden ist, dann gilt 'waiting'
		else if(  statusAllCommits.contains("waiting")  && statusAllInits.isEmpty()  &&  (this.work == null && this.subprocess ==null))
		{
			status = "waiting";
//			this.log("debug", "actual status is: "+status);
			return status;
		}

		// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist 'finished'
		else if(  statusAllCommits.contains("finished") )
		{
			status = "finished";
		}

//		this.log("debug", "actual status is: "+status);
		return status;
	}

	public Process getParent()
	{
		return this.parent;
	}

	public String getAbsdir()
	{
		String absDir = "";

		if (this.isRoot())
		{
			absDir = this.parent.getRootdir();
		}
		else if (this.getParent().isWrapper())
		{
			absDir = this.parent.getRootdir();
		}
		else
		{
			absDir = this.parent.getRootdir()+"/dir4step_"+this.getName();
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
			ArrayList<Step> steps = this.parent.getSteps(actualInit.getFromstep());
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
		
	public void addList(List list)
	{
		this.list.add(list);
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
	
	public ArrayList<Log> getLog()
	{
		return this.log;
	}
	
	public ArrayList<Log> getLogRecursive()
	{
		
		// zuerst das eigene log kopieren
		ArrayList<Log> logRecursive = this.getLog();
		
		// wenn this root ist, soll das logging von process mitgenommen werden
		if(this.getParent().getRootstepname().equals(this.getName()))
		{
			logRecursive.addAll(this.getParent().getLog());
		}
		
		// die logs aller Inits in die Sammlung uebernehmen
		for(Init actInit : this.getInit())
		{
			logRecursive.addAll(actInit.getLogRecursive());
		}

		// die logs des Work in die Sammlung uebernehmen
		if(!(this.work == null))
		{
			logRecursive.addAll(work.getLogRecursive());
		}

		// die logs des Subprocess in die Sammlung uebernehmen
		if(!(this.subprocess == null))
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
			if ( !actualInit.isInitConsistent() ) {result = false;	this.parent.log("error", "error in init '"+actualInit.getListname()+"'");}
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
	
}
