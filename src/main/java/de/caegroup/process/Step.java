package de.caegroup.process;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import de.caegroup.codegen.BBusiness;
import de.caegroup.codegen.Script;
import de.caegroup.process.Commit;

import org.apache.commons.io.filefilter.WildcardFileFilter;
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
	private String rank = "";
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
			
			for(String item : list.getItem())
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

		perlSnippet.add("\tmy $return = system(\"set -o pipefail; ($call | tee stdout.log) 3>&1 1>&2 2>&3 | tee stderr.log\");");
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
		perlSnippet.add("\t\tsystem(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (work) (exitcode=$return)\\\"\");");
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
					perlSnippet.add("\t\t\tsystem(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (variable="+actVariable.getKey()+") not right amount of variables: "+actVariable.getMinoccur()+" <= rightAmountOfVariables <= "+actVariable.getMaxoccur()+" (actualAmount=\" . scalar(@globbedFiles) . \"\\\"\");");
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
					if(actCommit.getToroot())
					{
						perlSnippet.add("");
						perlSnippet.add("\t\t\t\t# toroot = true");
						perlSnippet.add("\t\t\t&logit(\"info\", \"toroot: cp $globbedFile $pwd\");");
						perlSnippet.add("\t\t\t\tcopy($globbedFile, $pwd);");
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
					if(actCommit.getToroot())
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
					perlSnippet.add("\t\t\tsystem(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (variable="+actVariable.getKey()+") variable needs either a value or a glob definition\\\"\");");
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
					perlSnippet.add("\t\t&printHtmlOverview();");
					perlSnippet.add("\t\t\tsystem(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (file="+actFile.getKey()+") not right amount of files: "+actFile.getMinoccur()+" <= rightAmountOfFiles <= "+actFile.getMaxoccur()+" (actualAmount=\" . scalar(@globbedFiles) . \"\\\"\");");
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
					if(actCommit.getToroot())
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
					perlSnippet.add("\t\t\tsystem(\"pradar checkout -process "+this.getParent().getName()+" -id $id -exitcode \\\"fatal: (step="+this.getName()+") (commit="+actCommit.getName()+") (file="+actFile.getKey()+") file needs a glob definition\\\"\");");
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
		perlSnippet.add("system(\"pradar progress -process "+this.getParent().getName()+" -id $id -completed \" . ++$stepsCompleted);");
		perlSnippet.add("#-------------------");

		
		return perlSnippet;		
	}
	
	/**
	 * getStepAsPerlScript()
	 * generates a stub of a standalone perlscript that represents this process-step once it is finished by a programmer
	 * @return ArrayList<String> code
	 */
	public ArrayList<String> getStepAsPerlScript()
	{
		Script script = new Script();
		script.setType("step");
		script.genContent();
		
//		System.out.println(" options : "+callitem.size());

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
							else if(actMatch.getPattern().matches("^\\^[^\\\\+*?{}]+\\$$"))
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
				// wenn es eine liste mit dem gleichen namen gibt wie das init-element, sollen alle items (trennzeichen=%%) als default gesetzt werden 
				if(this.getList(actInit.getListname()) != null)
				{
					List list = this.getList(actInit.getListname());
					ArrayList<String> items = list.getItem();
					for(String item : items)
					{
						if(def.equals("")) {def = item;}
						else{def += "%%" + item;}
					}
				}
				
				// der hilfstext fuer diese option
				String text2 = "no description available";
				if(!(actInit.getDescription().matches("")))
				{
					text2 = actInit.getDescription();
					text2 = text2.replaceAll("'", "\\\\'");
				}
				
				script.addOption(name, minoccur, maxoccur, definition, check, def, text1, text2);
			}
//			System.out.println("addoption mit diesen parametern: "+name+minoccur+maxoccur+definition+check+def+text1+text2);
		}
		
		return script.getAll();
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
		
		this.getList().clear();
		this.setStatus("initializing");
		log("info", "setting status to 'initializing'");
		boolean initializing_success = true;
		// ueber alle inits iterieren
		for( Init actualInit : this.getInits())
		{
			// die init-angaben in lokale variablen uebernehmen
//			String fromobjecttype = actualInit.getFromobjecttype();
//			String name = actualInit.getName();
//			String returnfield = actualInit.getReturnfield();
			ArrayList<Step> fromsteps = parent.getSteps(actualInit.getFromstep());
			
			// ueber alle fromsteps gehen und die gefordterte liste erstellen (nur EINE insgesamt, nicht eine PRO fromstep)
			for (Step actualFromstep : fromsteps)
			{
				log("debug", "init '"+actualInit.getListname()+"': looking for field '"+actualInit.getReturnfield()+"' from a "+actualInit.getFromobjecttype()+" of step '"+actualFromstep.getName()+"'");
	
				ArrayList<Match> matchs = actualInit.getMatch();
			
				// wenn es ein file ist
				if (actualInit.getFromobjecttype().equals("file"))
				{
					ArrayList<File> files_from_fromstep = actualFromstep.getFile();
					ArrayList<File> files_from_fromstep_which_matched = new ArrayList<File>();
					// wenn match-angaben vorhanden sind, wird die fileliste reduziert
					
					for(Match actualMatch : matchs)
					{
						log("debug", "init '"+name+"': accepting only "+actualInit.getFromobjecttype()+"(s) which match '"+actualMatch.getPattern()+"' and field '"+actualMatch.getField()+"'");
						// iteriere ueber alle Files der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
						for(File actualFile : files_from_fromstep)
						{
							if (actualFile.match(actualMatch))
							{
								files_from_fromstep_which_matched.add(actualFile);
							}
						}
					}

					// feststellen ob die gewuenschte anzahl der variablen gematched hat
					if( (files_from_fromstep_which_matched.size() < actualInit.getMinoccur()) || (files_from_fromstep_which_matched.size() > actualInit.getMaxoccur()) )
					{
						log("debug", "init '"+name+"': found "+files_from_fromstep_which_matched.size()+" items to add to the list, but minoccur="+actualInit.getMinoccur()+", maxoccur="+actualInit.getMaxoccur());
						initializing_success = false;
					}

					// aus der reduzierten file-liste, das gewuenschte field (returnfield) extrahieren und in der list unter dem Namen ablegen
					// ist eine liste mit dem namen schon vorhanden, dann soll keine neue angelegt werden
					List list;
					if (this.getList(actualInit.getListname()) != null)
					{
						list = this.getList(actualInit.getListname());
					}
					// ansonsten eine anlegen und this hinzufuegen
					else
					{
						list = new List();
						list.setName(actualInit.getListname());
						this.addList(list);
					}
					for (File actualFile : files_from_fromstep_which_matched)
					{
						list.addItem(actualFile.getField(actualInit.getReturnfield()));
					}
					log("debug", "init '"+name+"': new list '"+list.getName()+"' with "+list.getItem().size()+" item(s).");
				}
				
				// wenn es ein variable ist
				else if (actualInit.getFromobjecttype().equals("variable"))
				{
					ArrayList<Variable> variables_from_fromstep = actualFromstep.getVariable();
					ArrayList<Variable> variables_from_fromstep_which_matched = new ArrayList<Variable>();
	
					for (Match actualMatch : matchs)
					{
						log("debug", "init '"+name+"': accepting only "+actualInit.getFromobjecttype()+"(s) which match '"+actualMatch.getPattern()+"' and field '"+actualMatch.getField()+"'");
						// iteriere ueber alle Variablen der (womoeglich bereits durch vorherige matchs reduzierte) liste und ueberpruefe ob sie matchen
						for (Variable actualVariable : variables_from_fromstep)
						{
							if (actualVariable.match(actualMatch))
							{
								variables_from_fromstep_which_matched.add(actualVariable);
							}
						}
					}

					// feststellen ob die gewuenschte anzahl der variablen gematched hat
					if( (variables_from_fromstep_which_matched.size() < actualInit.getMinoccur()) || (variables_from_fromstep_which_matched.size() > actualInit.getMaxoccur()) )
					{
						log("debug", "init '"+name+"': found "+variables_from_fromstep_which_matched.size()+" items to add to the list, but minoccur="+actualInit.getMinoccur()+", maxoccur="+actualInit.getMaxoccur());
						initializing_success = false;
					}

					// aus der reduzierten variablen-liste, das gewuenschte field (returnfield) extrahieren und in der initlist unter dem Namen ablegen
					// ist eine liste mit dem namen schon vorhanden, dann soll keine neue angelegt werden
					List list;
					if (this.getList(actualInit.getListname()) != null)
					{
						list = this.getList(actualInit.getListname());
					}
					// ansonsten eine anlegen und this hinzufuegen
					else
					{
						list = new List();
						list.setName(actualInit.getListname());
						this.addList(list);
					}
	
					// hinzufuegen der listitems
					for(Variable actualVariable : variables_from_fromstep_which_matched)
					{
						list.addItem(actualVariable.getField(actualInit.getReturnfield()));
					}
					
					log("debug", "init '"+name+"': new list '"+list.getName()+"' with "+list.getItem().size()+" item(s).");
				}
			}
		}
		
		// wenn alle initialisierung funktioniert habenn den status aendern
		if (initializing_success)
		{
			this.setStatus("initialized");
			log("info", "setting status to 'initialized'");
						
			return true;
		}
		else
		{
			this.setStatus("initialization failed");
			log("debug", "initialization failed.");
			return false;
		}
	}
	
	public boolean fan() throws CloneNotSupportedException
	{
		this.setStatus("fanning");
		log("info", "setting status to 'fanning'");
		
		if (this.loop!=null && !(this.loop.equals("")))
		{
			// wenn die loopliste mindestens 1 wert enthaelt, ueber dioe liste iterieren und fuer jeden wert den aktuellen step clonen
			if (this.getListItems(this.loop).size() > 0)
			{
				// cloner erstellen fuer einen deep-copy
				Cloner cloner = new Cloner();
				int x = 1;
				for(String loopVariable : this.getListItems(this.loop))
				{
					// einen neuen step erzeugen (klon von this)
					Step newstep = cloner.deepClone(this);
					newstep.setLoopvar(loopVariable);
					newstep.setLoop(null);
					newstep.setName(newstep.getName()+"@"+x);
					newstep.setStatus("fanned");
					newstep.log("info", "this step '"+newstep.getName()+"' was fanned out from step '"+this.getName()+"'");
					newstep.log("info", "setting status to 'fanned'");

					// eine liste mit dem namen 'loop' anlegen und darin die loopvar speichern
					List listLoop = new List();
					listLoop.setName("loopvar");
					listLoop.addItem(loopVariable);
					newstep.addList(listLoop);

					// den neuen step (klon von this) dem prozess hinzufuegen
					this.parent.addStep(newstep);
					x++;
				}
				
				// den urspruenglichen step (this) aus dem prozess entfernen
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

	public boolean work(String processSyscall)
	{
		boolean success = true;
		this.setStatus("working");
		log("info", "setting status to 'working'");

//			String call = work.generateCall(this.getListall());
		String call = this.getWork().getCall();

		// holen der zugehoerigen Systemprozess-ID - feststellen ob fuer diesen work schon ein aufruf getaetigt wurde
		log("info", "generated call for the work-phase (name='"+ this.getWork().getName() +"') is: "+call);
		
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
			else if (this.getParent().isWrapper())
			{
				log("info", "process work (script,program,..) already finished. pid="+pid);
				this.setStatus("finished");
				log("info", "setting status to 'finished' (no commit, because this is a wrapper-process)");
				success = true;
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
//			System.out.println("STEP IST NOCH NICHT GESTARTET");
			log("info", "process work (script,program,..) not lauched yet");
			log("info", "creating directory "+this.getAbsdir());
			
			// step-directory anlegen, falls es noch nicht existiert
			// falls es ein wrapper-process ist, gibt es das directory warsch. schon
			if(!(new java.io.File(this.getAbsdir()).exists()))
			{
				this.mkdir(this.getAbsdir());
			}
			
			try
			{
				String[] args_for_syscall = {processSyscall, call, this.getAbsstdout(), this.getAbsstderr(), this.getAbspid()};

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

				// wait 2 seconds for becoming the pid-file visible
				Thread.sleep(2000);
				
				log("info", "process work (script,program,..) launched. pid="+sysproc.hashCode());
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
	// es werden standardvariablen committet
	public boolean rootcommit() throws IOException
	{

		this.log("info", "special commit, because this step is root");

		//ueber alle initCommitDirs verzeichnisse iterieren
		this.log("info", "commit all initCommitDirs");
		for(java.io.File actualCommitDir : this.parent.getInitCommitDirs2())
		{
			this.commitdir(actualCommitDir);
			this.log("info", "committed dir "+actualCommitDir.getAbsolutePath());
		}

		//ueber alle commitvarfiles iterieren
		this.log("info", "commit all initCommitVarfiles");
		for(java.io.File actCommitVarfile : this.parent.getInitCommitVarfiles2())
		{
			this.commitvarfile(actCommitVarfile);
			this.log("info", "committed dir "+actCommitVarfile.getAbsolutePath());
		}
		
		this.log("info", "commit all standard entries");
		// das stepdir als variable ablegen
		commitVariable("dir", this.getAbsdir());
		this.log("info", "committed variable dir="+this.getAbsdir());
		
		this.setStatus("finished");
		
		this.log("info", "special commit of step 'root' ended");
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
					if (!(this.commitFile("default", file)))
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
	public boolean commitFile(String key, java.io.File file)
	{
		if (file.canRead())
		{
			File newfile = new File();
			newfile.setAbsfilename(file.getPath());
			newfile.setKey(key);
			return this.commitFile(newfile);
		}
		else
		{
			this.log("info", "file NOT committed (CANT READ!): "+file.getAbsolutePath());
			return false;
		}
	}

	public boolean commitFile(String key, String absfilepathdir)
	{
		java.io.File file = new java.io.File(absfilepathdir);
		return commitFile(key, file);
	}

	/**
	 * das file wird in das step-verzeichnis (oder root) kopiert, falls es nicht schon dort liegt
	 * dieses file im step-verzeichnis (oder root) wird in die file-ablage des step-objects aufgenommen 
	 * @param file
	 * @return success
	 */
	public boolean commitFile(File file)
	{
		this.log("info", "commit: planning for file: "+file.getAbsfilename());

		// wenn der pfad des files NICHT identisch ist mit dem pfad des step-directories
		this.log("debug", "step.commitFile(File): file.getAbsfilename():"+file.getAbsfilename());
		this.log("debug", "step.commitFile(File): step.getAbsfilename():"+this.getAbsdir());
		if (!(new java.io.File(file.getAbsfilename()).getParent().matches("^"+this.getAbsdir()+"$")))
		{
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
					return false;
				}
			}
		}
		else
		{
			log("info", "commit: file already found in step-directory. skipping copy.");
		}
		
		log("info", "commit: adding file to step object: "+file.getAbsfilename());
		this.addFile(file);
//		System.out.println("AMOUNT OF FILES ARE NOW: "+this.file.size());
		return true;
	}

	/**
	 * commit einer variable aus zwei strings (name, value)
	 * @input key, value
	 * @return success
	*/	
	public boolean commitVariable(String key, String value)
	{
		Variable variable = new Variable();
		variable.setKey(key);
		variable.setValue(value);
		return this.commitVariable(variable);
	}

	/**
	 * commit einer variable an this
	 * @input variable
	 * @return success
	*/	
	public boolean commitVariable(Variable variable)
	{
		this.addVariable(variable);
		return true;
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

		// wenn es sich um root handelt, wird vor dem eigentlichen commit noch etwas anderes committed
		if (this.getName().equals(this.parent.getRootstepname()))
		{
			success = this.rootcommit();
		}

		// nur wenn es nicht der root-step ist, soll auf konventionelle art committed werden
		else
		{
			// ueber alle commits iterieren
			for( Commit actualCommit : this.commit)
			{
				this.log("info", "commit name "+actualCommit.getName());
				
				// wenn das zu committende objekt ein File ist...
				for(File actualFile : actualCommit.getFile())
				{
					this.log("info", "actual file is key "+actualFile.getKey());
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
										actualCommit.addFile(pFile);
									}
								}
							}
						}
					}
				}

				// ueber alle files des commits iterieren und dem step hinzufuegen
				for(File actualFile : actualCommit.getFile())
				{
					this.log("info", "commit: file "+actualFile.getKey()+":"+actualFile.getAbsfilename());
					if(!(this.commitFile(actualFile)))
					{
						success = false;
					}
	
					this.log("debug", "rootstepname of this process is: " + this.getParent().getRootstepname() + " || this stepname is: " + this.getName());
					// falls nicht rootstep und wenn das File auch dem prozess committed werden soll...
					if (actualCommit.getToroot() && (!(this.getName().equals(this.getParent().getRootstepname()))))
					{
						this.log("info", "commit to root: file "+actualFile.getKey()+":"+actualFile.getAbsfilename());
						// dem root-step committen
						if (!(this.parent.getStep(this.parent.getRootstepname()).commitFile(actualFile)))
						{
							success = false;
						}
					}
				}
				
				// wenn das zu committende objekt eine Variable ist...
				for(Variable actualVariable : actualCommit.getVariable())
				{
					this.log("info", "commit: variable "+actualVariable.getKey()+":"+actualVariable.getValue());
					if (!(this.commitVariable(actualVariable)))
					{
						success = false;
					}
	
					// wenn die Variable auch dem prozess committed werden soll...
					if (actualCommit.getToroot() && (!(this.getName().equals(this.getParent().getRootstepname()))))
					{
						this.log("info", "commit to root: variable "+actualVariable.getKey()+":"+actualVariable.getValue());
						// dem root-step committen
						if (!(this.parent.getStep(this.parent.getRootstepname()).commitVariable(actualVariable)))
						{
							success = false;
						}
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
		for(Init actualInit : this.getInit())
		{
			actualInit.setParent(this);
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
		String absDir = "";

		if (this.getName().equals(this.parent.getRootstepname()))
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
	
}
