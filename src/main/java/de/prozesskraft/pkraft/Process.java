package de.prozesskraft.pkraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.dozer.DozerBeanMapper;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
//import org.dozer.Mapper;
//import org.dozer.loader.api.BeanMappingBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.prozesskraft.codegen.Script;
import de.prozesskraft.codegen.UnknownCodeBlockException;
import de.prozesskraft.commons.WhereAmI;


public class Process extends ModelObject
implements Serializable
{
	/*----------------------------pkraft-startinstance
	  structure
	----------------------------*/
	static final long serialVersionUID = 1;

	private String name = new String();
	private String description = new String();
	private String path = new String();
	private Integer maxSimultaneousSteps = null;
	private Integer stepStartDelayMinutes = null;
	private Double stepStartLoadAverageBelow = null;
	private String initCommitFile = new String();
	private String initCommitVariable = new String();
	private String architectName = new String();
	private String architectCompany = new String();
	private String architectMail = new String();
	private String customerName = new String();
	private String customerCompany = new String();
	private String customerMail = new String();
	private String modelVersion = new String();
	private String version = new String();
	private boolean wrapper = false;
	private String id2Variable = null;
//	private NamedList<Step> steps = new NamedList<Step>();
	private ArrayList<Step> step = new ArrayList<Step>();
//	private ArrayList<Init> inits = new ArrayList<Init>();
//	public ArrayList<Step> stepStorageForAdd = new ArrayList<Step>();
//	public ArrayList<Step> stepStorageForRemove = new ArrayList<Step>();
	
	public boolean run = false;
	private String status = new String();	// waiting/working/finished/broken/paused/abandoned
	private String baseDir = new java.io.File(".").getAbsolutePath();
	private double managerid = -1;
	private String infilebinary = "";
	private String infilexml = "";
	private String outfilebinary = "";
	private String outfilexml = "";
	private String outFileDoc = new String();
	private String filedoctemplateodf = new String();
	private String fileDocJrxml = new String();
	private String rootstepname = "root";
	ArrayList<Log> log = new ArrayList<Log>();
	private String idRumpf = "noId"; 
	private String touchAsString = "";
	public long touchInMillis = 0;

	public int cloneGeneration = 1;
	public int cloneDescendant = 1;
	public int clonePerformed = 0;
	
	private boolean subprocess = false;
	private String parentid = "0";
	public String stepnameOfParent = null;
	
	private boolean stepStatusChangedWhileLastDoIt = true;
	
	private Long timeOfLastStepStart = System.currentTimeMillis()-600000;
	private Long timeOfProcessCreated = System.currentTimeMillis();
	private Long timeOfProcessFinishedOrError = 0L;

	private Timeserie timeSerieLoadAverage = new Timeserie("load average of client machine");
	private Timeserie timeSerieBinarySize = new Timeserie("size of binary file in kB");
	private Timeserie timeSerieStepSize = new Timeserie("footprint of all steps");
	
	public int counterLoadAverageTooHigh = 0;

	/*----------------------------
	  constructors
	----------------------------*/
	public Process()
	{
		name = "unnamed";
		version = "unversioned";
		description = "without description";
		status = "waiting";

		this.genRandomId();
	}
	/*----------------------------
	  methods
	----------------------------*/

	/**
	 * clone
	 * returns a clone of this
	 * @return Process
	 */
	@Override
	public Process clone()
	{
		Process clone = new Process();
		clone.setName(this.getName());
		clone.setDescription(this.getDescription());
		clone.setPath(this.getPath());
		clone.setMaxSimultaneousSteps(this.getMaxSimultaneousSteps());
		clone.setStepStartDelayMinutes(this.getStepStartDelayMinutes());
		clone.setStepStartLoadAverageBelow(this.getStepStartLoadAverageBelow());
		clone.setInitCommitFile(this.getInitCommitFile());
		clone.setInitCommitVariable(this.getInitCommitVariable());
		clone.setArchitectName(this.getArchitectName());
		clone.setArchitectCompany(this.getArchitectCompany());
		clone.setArchitectMail(this.getArchitectMail());
		clone.setCustomerName(this.getCustomerName());
		clone.setCustomerCompany(this.getCustomerCompany());
		clone.setCustomerMail(this.getCustomerMail());
		clone.setModelVersion(this.getModelVersion());
		clone.setVersion(this.getVersion());
		clone.setWrapper(this.isWrapper());
		clone.setId2Variable(this.getId2Variable());
		clone.setRun(this.isRun());
		clone.setStatus(this.getStatus());
		clone.setBaseDir(this.getBaseDir());
		clone.setManagerid(this.getManagerid());
		clone.setInfilebinary(this.getInfilebinary());
		clone.setInfilexml(this.getInfilexml());
		clone.setOutfilebinary(this.getOutfilebinary());
		clone.setOutfilexml(this.getOutfilexml());
		clone.setOutFileDoc(this.getOutFileDoc());
		clone.setFiledoctemplateodf(this.getFiledoctemplateodf());
		clone.setFileDocJrxml(this.getFileDocJrxml());
		clone.setRootstepname(this.getRootstepname());
		clone.setIdRumpf(this.getIdRumpf());
		clone.setTouchAsString(this.getTouchAsString());
		clone.setTouchInMillis(this.getTouchInMillis());
		clone.setCloneGeneration(this.getCloneGeneration());
		clone.setCloneDescendant(this.getCloneDescendant());
		clone.setClonePerformed(this.getClonePerformed());
		clone.setSubprocess(this.isSubprocess());
		clone.setParentid(this.getParentid());
		if(this.getStepnameOfParent() != null)
		{
			clone.setStepnameOfParent(this.getStepnameOfParent());
		}
		clone.setTimeOfLastStepStart(this.getTimeOfLastStepStart());
		clone.setTimeOfProcessCreated(this.getTimeOfProcessCreated());
		clone.setTimeOfProcessFinishedOrError(this.getTimeOfProcessFinishedOrError());

		// zum clonen der steps wird this uebergeben: dies stellt sicher, dass beim nachfolgenden klonen der files bereits der prozess bekannt ist und
		// die files in das richtige verzeichnis kopiert werden
		for(Step actStep : this.getStep())
		{
			clone.addStep(actStep.clone());
		}
		for(Log actLog : this.getLog())
		{
			clone.addLog(actLog.clone());
		}

		clone.cloneGeneration++;
		this.clonePerformed++;
		clone.setCloneDescendant(this.clonePerformed);
		
		clone.setClonePerformed(0);
		
		return clone;
	}

	/**
	 * clones the process and makes a copy of all files on filesystem
	 * step-directories of subprocesses will not be copied
	 * if process is a child, the path to the (just beforehand cloned) parentId is needed to set the parentId.
	 * returns a clone of this
	 * @return Process
	 */
	public Process cloneWithData(String baseDir, String parentId)
	{
		// bisherigen process klonen
		Process clonedProcess = this.clone();
		System.err.println("debug: cloned: original process id="+this.getId()+", clone process id="+clonedProcess.getId());
		// kopieren der daten auf filesystem
		clonedProcess.log("debug", "cloned: original process id="+this.getId()+", clone process id="+clonedProcess.getId());

		// falls angegeben, soll das basedir auf einen bestimmten pfad geaendert werden
		// falls null, dann bleibt es wie vom Vatter beim klonen erhalten
		if(baseDir != null)
		{
			System.err.println("debug: setting baseDirectory of clone: "+baseDir);
			clonedProcess.log("debug", "setting baseDirectory of clone: "+baseDir);
			clonedProcess.setBaseDir(baseDir);
		}
		else
		{
			// bleibt beim gleichen basedir wie im original
			System.err.println("debug: lieving baseDirectory of clone: "+clonedProcess.getBaseDir());
			clonedProcess.log("debug", "lieving baseDirectory of clone: "+clonedProcess.getBaseDir());
		}

		// falls eine parentId uebergeben wurde soll diese im clonedProcess als parentId gesetzt werden
		if(parentId != null)
		{
			System.err.println("debug: setting new parentId in cloned process to: "+parentId);
			clonedProcess.setParentid(parentId);
		}
		else
		{
			System.err.println("debug: setting new parentId in cloned process to: 0");
			clonedProcess.setParentid("0");
		}

		// erstellen des zielverzeichnisses (rootDir des clonedProcesses)
		new java.io.File(clonedProcess.getRootdir()).mkdirs();

		// speichern des geklonten prozesses in das neue verzeichnis (dabei wird das alte pmb ueberschrieben)
		clonedProcess.setOutfilebinary(clonedProcess.getRootdir() + "/" + "process.pmb");
		clonedProcess.writeBinary();

		// kopieren der daten auf filesystem
		System.err.println("debug: copying directory tree: source="+this.getRootdir()+", target="+clonedProcess.getRootdir());
		clonedProcess.log("debug", "copying directory tree: source="+this.getRootdir()+", target="+clonedProcess.getRootdir() + " without directories of subprocesses");
		
		try
		{
			// kopieren des InputDirs des RootSteps
			if(new java.io.File(this.getRootdir() + "/processInput").exists())
			{
				System.err.println("debug: copying processInput: source="+this.getRootdir()+"/processInput , target="+clonedProcess.getRootdir() + "/processInput");
				clonedProcess.log("debug", "copying processInput: source="+this.getRootdir()+"/processInput , target="+clonedProcess.getRootdir() + "/processInput");
				FileUtils.copyDirectory(new java.io.File(this.getRootdir() + "/processInput"), new java.io.File(clonedProcess.getRootdir() + "/processInput"), true);
			}
			
			// kopieren des OutputDirs des RootSteps
			if(new java.io.File(this.getRootdir() + "/processOutput").exists())
			{
				System.err.println("debug: copying processOutput: source="+this.getRootdir()+"/processOutput , target="+clonedProcess.getRootdir() + "/processOutput");
				clonedProcess.log("debug", "copying processOutput: source="+this.getRootdir()+"/processOutput , target="+clonedProcess.getRootdir() + "/processOutput");
				FileUtils.copyDirectory(new java.io.File(this.getRootdir() + "/processOutput"), new java.io.File(clonedProcess.getRootdir() + "/processOutput"), true);
			}
			// kopieren aller Step-Directories, falls sie existieren (außer rootStep und SubprocessSteps)
			for(Step actStep : this.getStep())
			{
				if(!actStep.isRoot() && actStep.getSubprocess() == null)
				{
					if(new java.io.File(actStep.getAbsdir()).exists())
					{
						System.err.println("debug: copying stepDirectory "+actStep.getName() +": source="+actStep.getAbsdir() +", target="+clonedProcess.getStep(actStep.getName()).getAbsdir());
						clonedProcess.log("debug", "copying stepDirectory "+actStep.getName() +": source="+actStep.getAbsdir() +", target="+clonedProcess.getStep(actStep.getName()).getAbsdir());
						FileUtils.copyDirectory(new java.io.File(actStep.getAbsdir()), new java.io.File(clonedProcess.getStep(actStep.getName()).getAbsdir()), true);
					}
				}
			}
		}
		catch (IOException e)
		{
			this.log("error", "copying of directory tree failed -> cloning failed. deleting all copied data.");
			this.log("error", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));

			this.log("warn", "delete this directory by hand: "+clonedProcess.getRootdir());

			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return clonedProcess;
	}
	
	/**
	 * clone
	 * returns a clone of this
	 * @return Process
	 */
	public Process oldClone()
	{
		Process clonedProcess = SerializationUtils.clone(this);
		clonedProcess.cloneGeneration++;
		clonedProcess.clonePerformed = 0;
		this.clonePerformed++;
		clonedProcess.cloneDescendant = this.clonePerformed;
		return clonedProcess;
	}
	
	private void addLog(Log log)
	{
		log.setLabel("process "+this.getName());
		this.log.add(log);
	}
	
	/**
	 * reset a certain step and all subsequent steps (the steps who have an Init, that link to a fromstep that has been reset)
	 * @param Step
	 */
	public void resetStep(String stepname)
	{
		// zuerst alle dependent steps reseten
		ArrayList<Step> stepsToReset = this.getStepDependent(stepname);
		for(Step actStep : stepsToReset)
		{
			log("warn", "reset step (because of dependency): "+actStep.getName());
			actStep.resetBecauseOfDependency();
		}
		
		// erst dann den urspruenglichen step reseten
		Step stepToReset = this.getStep(stepname);
		stepToReset.reset();
		log("warn", "reset step: "+stepname);
	}
	
	/**
	 * reset only the commit of a certain step and perform a full reset for all subsequent steps (the steps who have an Init, that link to a fromstep that has been reset)
	 * @param Step
	 * @throws IOException 
	 */
	public void resetCommitStep(String stepname) throws IOException
	{
		// zuerst alle dependent steps reseten
		ArrayList<Step> stepsToReset = this.getStepDependent(stepname);
		for(Step actStep : stepsToReset)
		{
			log("warn", "reset step (because of dependency): "+actStep.getName());
			actStep.resetBecauseOfDependency();
		}
		
		// erst dann den urspruenglichen step reseten
		Step stepToReset = this.getStep(stepname);
		stepToReset.resetCommit();
		log("warn", "reset commit step: "+stepname);
	}
	
	/**
	 * kills all steps
	 */
	public String kill()
	{
		String returnStringInfoAboutKills = "";
		for(Step actStep : this.getStep())
		{
			returnStringInfoAboutKills += actStep.kill();
		}
		
		return returnStringInfoAboutKills;
	}
	
	/**
	 * kills all steps
	 */
	public void clearLogRecursive()
	{
		this.getLog().clear();
		
		for(Step actStep : this.getStep())
		{
			actStep.clearLogRecursive();
		}
	}
	
	/**
	 * adds a new step to this.
	 */
	public boolean addStep(Step step)
	{
		String[] stepnames = this.getStepnames();
		for(int i=0; i<stepnames.length; i++)
		{
			if (stepnames[i] == step.getName())
			{
				log("error", "stepname '"+stepnames[i]+"' already in use. step not added.");
//				System.err.println("stepname '"+stepnames[i]+"' already in use. step not added.");
				return false;
			}
		}
		step.setParent(this);
		this.step.add(step);
		return true;
	}

	/**
	 * integrates a step into a process
	 * this is only possible for a fanned out multistep
	 * 1) the name will be changed to avoid collision with existent fanned steps
	 * 2) if there is a datadirectory, this will also be copied and onthefly renamed
	 * @param step
	 * @return
	 */
	public boolean integrateStep(Step step)
	{
		boolean integrationErfolgreich = true;

		this.log("debug", "want to integrate step: " + step.getName());
		System.err.println("debug: want to integrate step: " + step.getName());

		// das alte datenverzeichnis feststellen
		java.io.File sourceStepDir = new java.io.File(step.getAbsdir());

		// feststellen des namensrumpfes
		Pattern p = Pattern.compile("^(.+)@(.+)$");
		Matcher m = p.matcher(step.getName());

		if(m.find())
		{
			String rumpf = m.group(1);
//			System.err.println("rumpfnamen: " + rumpf);
			int zaehler = Integer.parseInt(m.group(2));
//			System.err.println("zaehler: " + zaehler);

			// hochzaehlen
			zaehler++;

			// mit bekannten zaehler beginnen und so lange hochzaehlen bis kein step mit diesem namen gefunden wird
			while(this.getStep(rumpf+"@"+zaehler) != null)
			{
				zaehler++;
			}

			// den namen fuer den zu integrierenden step setzen
			step.setName(rumpf+"@"+zaehler);
			this.log("debug", "renaming step while integrating. new name is: " + step.getName());
			System.err.println("debug: renaming step while integrating. new name is: " + step.getName());
			
			// den neuen step dem process hinzufuegen
			this.addStep(step);
			
			// ein evtl. vorhandenes daten verzeichnis einkopieren
			java.io.File destStepDir = new java.io.File(step.getAbsdir());

			this.log("info", "debug: source directory that will be processed: " + sourceStepDir.getAbsolutePath());
			System.err.println("debug: source directory that will be processed: " + sourceStepDir.getAbsolutePath());
			// gibt es ueberhaupt ein source directory?
			if(sourceStepDir.exists() && sourceStepDir.isDirectory())
			{
				// gibt es bereits ein verzeichnis, dass den pfad des destination step directories traegt?
				if(destStepDir.exists())
				{
					this.log("error", "destination step directory does already exist -> no data integration possible.");
					System.err.println("error: destination step directory does already exist -> no data integration possible.");
					integrationErfolgreich = false;
				}
				else
				{
					// falls das integrierte step einen subprozess enthaelt
					// in diesem fall soll das basedir dieses subprocesses auf denselben wert gesetzt werden wie das neue stepdir lauten
					// und den step clonen
					if(step.getSubprocess() != null && step.getSubprocess().getProcess() != null)
					{
						this.log("debug", "step contains a process -> this process will be cloned");
						System.err.println("debug: step contains a process -> this process will be cloned");

						// der Process innerhalb des Supprocesses muss neu eingelesen werden
						// es koennte sein, dass Daten und Infilebinary-Pfad nicht mehr aktuell sind auf Grund vorangegangener klonierungen
						Process subprozessOriginal = step.getSubprocess().getProcess();
						subprozessOriginal.setInfilebinary(sourceStepDir + "/process.pmb");
						subprozessOriginal.setOutfilebinary(sourceStepDir + "/process.pmb");
						this.log("debug", "original process of subprocess will be reread from here: " + sourceStepDir + "/process.pmb");
						System.err.println("debug: original process of subprocess will be reread from here: " + sourceStepDir + "/process.pmb");
						subprozessOriginal = subprozessOriginal.readBinary();

						// den gerade eingelesenen Prozess klonen
						this.log("debug", "original process of subprocess will be cloned into this basedir: " + destStepDir.getAbsolutePath());
						System.err.println("debug: original process of subprocess will be cloned into this basedir: " + destStepDir.getAbsolutePath());
						System.err.println("debug: calling method cloneWithData(" + destStepDir.getAbsolutePath() + ", " + this.getParentid());
						Process subprozessClone = subprozessOriginal.cloneWithData(destStepDir.getAbsolutePath(), this.getId());

						// und das original schreiben, da generationszaehler veraendert wurden 
						this.log("debug", "original process of subprocess will be written, because of changed counters");
						System.err.println("debug: original process of subprocess will be written, because of changed counters");
						subprozessOriginal.writeBinary();

//						Process gemergteSubprocess = step.getSubprocess().getProcess();
//						gemergteSubprocess.setInfilebinary(destStepDir + "/process.pmb");
//						gemergteSubprocess.setOutfilebinary(destStepDir + "/process.pmb");
//						gemergteSubprocess.setBaseDir(destStepDir.getAbsolutePath());
//						gemergteSubprocess.writeBinary();
					}
					
					// dann handelt es sich um einen normalen step
					else
					{
						// das alte bestehende stepdirectory in den neuen prozess (this) kopieren und dabei den neuen stepnamen beruecksichtigen
						this.log("debug", "copying data for step integration. " + sourceStepDir.getAbsolutePath() + " => " + destStepDir.getAbsolutePath());
						System.err.println("debug: copying data for step integration. " + sourceStepDir.getAbsolutePath() + " => " + destStepDir.getAbsolutePath());
						try
						{
							FileUtils.copyDirectory(sourceStepDir, destStepDir, true);
						}
						catch (IOException e)
						{
							this.log("error", "copying of directory tree failed");
							System.err.println("error: copying of directory tree failed");
							// TODO Auto-generated catch block
							this.log("error", e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
			else
			{
				this.log("debug", "source step directory does not exist -> no data integration needed.");
				System.err.println("debug: source step directory does not exist -> no data integration needed.");
			}
			
		}
		else
		{
			this.log("info", "Pattern " + "^(.+)@(.+)$" + " did not match the stepname!!!!!!");
			System.err.println("Pattern " + "^(.+)@(.+)$" + " did not match the stepname!!!!!!");
		}

		return integrationErfolgreich;
	}
	
	/**
	 * triggers the refreshStatus of every present subprocess
	 * @throws IOException 
	 */
	public void refreshSubprocessStatus() throws IOException
	{
		for(Step actStep : this.getStep())
		{
			if(actStep.getSubprocess() != null)
			{
				actStep.getSubprocess().refreshStatus();
			}
		}
	}
	
	/**
	 * generates a ArrayList that represents a graph in the format 'dot'
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getProcessAsDotGraph()
	{
		ArrayList<String> dot = new ArrayList<String>();
	
		dot.add("digraph \""+this.getName()+"\" {");
		
		// alle bestehenden steps deklarieren
		for(Step actStep : this.getStep())
		{
			dot.add(actStep.getName() + ";");
		}
		
		// fuer jeden step die beziehung der fromsteps auffuehren
		for(Step actStep : this.getStep())
		{
			ArrayList<String> fromstep = new ArrayList<String>();
			for(Init actInit : actStep.getInit())
			{
				fromstep.add(actInit.getFromstep());
			}
			
			for(String actFromstep : fromstep)
			{
				dot.add(actFromstep + " -> " + actStep.getName());
			}
		}
		
		dot.add("}");

		return dot;
	}
	
	
	/**
	 * generates a new process as a wrapper-process to this
	 * this means:
	 * root stays root
	 * all other steps will be deleted
	 * will contain only 1 step that
	 * a) triggers a command "processname"
	 * b) all root/commits are translated to step/inits in step 'processname'
	 * c) all callitems are the same as meant in root
	 * d) all commits are the same as in all old steps 'toroot'
	 * e) the new inits with fromobjecttype=file of the generated step 'processname' will have definition returnfield="absfilename"
	 * f) the new inits with fromobjecttype=variable of the generated step 'processname' will have definition returnfield="value"
	 * @return Process
	 */
	public Process getProcessAsWrapper()
	{
		// process clonen
		Process wrapperProcess = this.clone();
		
		// und gleich alle steps wegschmeissen
		wrapperProcess.removeStepAll();
		
		// setzen des wrapper-flags
		wrapperProcess.setWrapper(true);
		
		// kopieren des root-steps und dem wrapperProcess hinzufuegen
		wrapperProcess.addStep(this.getStep(this.getRootstepname()).clone());
		
		// erstellen eines steps mit dem namen des prozesses und dem wrapperProcess hinzufuegen
//		Step wrapStep = new Step(this.getName());
//		wrapperProcess.addStep(new Step(this.getName()));
		Step step = new Step();
		step.setName(this.getName());
		step.setDescription(this.getDescription());
		wrapperProcess.addStep(step);

		// jeden commit aus 'root' durchgehen und daraus alle inits erzeugen
		for(Commit actCommit : wrapperProcess.getStep(this.getRootstepname()).getCommit())
		{
			// und aus jeder variable commit einen init fuer den wrapperStep erstellen
			for(Variable actVariable : actCommit.getVariable())
			{
				// wenn ein (default-)value eintrag in aktueller variable angegeben ist, soll eine liste mit diesem item initial erzeugt werden
				if(actVariable.getValue() != null)
				{
					List list = new List();
					list.setName(actVariable.getKey());
					list.addItem(actVariable.getValue());
					// und dem wrapperStep hinzufuegen
					wrapperProcess.getStep(this.getName()).addList(list);
				}
				
				Init init = new Init();
				init.setListname(actVariable.getKey());
				init.setDescription(actVariable.getDescription());
				init.setFromobjecttype("variable");
				init.setReturnfield("value");
				init.setFromstep(this.getRootstepname());
				init.setInsertrule("overwrite");
				init.setMinoccur(actVariable.getMinoccur());
				init.setMaxoccur(actVariable.getMaxoccur());
				
				Match match = new Match(init);
				match.setField("key");
				match.setPattern(actVariable.getKey());
				
				// den init dem wrapStep hinzufuegen
				wrapperProcess.getStep(this.getName()).addInit(init);
			}
			
			// und aus jedem file commit einen (evtl. list und) init fuer den wrapperStep erstellen
			for(File actFile : actCommit.getFile())
			{
				
				Init init = new Init();
				init.setListname(actFile.getKey());
				init.setDescription(actFile.getDescription());
				init.setFromobjecttype("file");
				init.setReturnfield("absfilename");
				init.setFromstep(this.getRootstepname());
				init.setInsertrule("overwrite");
				init.setMinoccur(actFile.getMinoccur());
				init.setMaxoccur(actFile.getMaxoccur());
				
				Match match = new Match(init);
				match.setField("key");
				match.setPattern(actFile.getKey());
				
				// den match zum neuen init hinzufuegen
//				init.addMatch(match);
				
				// den init dem wrapStep hinzufuegen
				wrapperProcess.getStep(this.getName()).addInit(init);
			}
			
		}
		
		// und einen zusaetzlichen init fuer das directory des steps root
		// das wird fuer den parameter --instancedir benoetigt (ein standard parameter fuer prozesse, die zu perl konvertiert wurden)
		Init init = new Init();
		init.setListname("instancedir");
		init.setDescription("Verzeichnis des steps root");
		init.setFromobjecttype("variable");
		init.setReturnfield("value");
		init.setFromstep(this.getRootstepname());
		init.setInsertrule("overwrite");
		init.setMinoccur(1);
		init.setMaxoccur(1);
		
		Match match = new Match(init);
		match.setField("key");
		match.setPattern("dir");
		
		// den init dem wrapStep hinzufuegen
		wrapperProcess.getStep(this.getName()).addInit(init);

		// ein work element erstellen und fuer jedes init ein callitem generieren
		Work work = new Work();
		work.setCommand(this.getName());

		// als erstes callitem den bmw-spezifischen aufruf --version erstellen
		Callitem callitemVersion = new Callitem();
		callitemVersion.setPar("--version");
		callitemVersion.setDel("=");
		callitemVersion.setVal(this.getVersion());
		work.addCallitem(callitemVersion);
		
		// fuer jedes init ein callitem
		for(Init actInit : wrapperProcess.getStep(this.getName()).getInit())
		{
			Callitem callitem = new Callitem();
			callitem.setLoop(actInit.getListname());
			callitem.setPar("--"+actInit.getListname());
			callitem.setDel("=");
			callitem.setVal("{$loopvarcallitem}");
			work.addCallitem(callitem);
		}
		
		// work zum wrapperProcess hinzufuegen
		wrapperProcess.getStep(this.getName()).setWork(work);
		
		// aus allen steps (auser root) aus this die commits in den wrap-step clonen
		for(Step actStep : this.getStep())
		{
			if(!(actStep.getName().matches("^root$")))
			{
				for(Commit actCommit : actStep.getCommit())
				{
					wrapperProcess.getStep(this.getName()).addCommit(actCommit);
				}
			}
		}
		
		// zurueckgeben des wrapperProzesses
		return wrapperProcess;
	}
	
	/**
	 * generates a perl script that resembles the process in scriptform
	 */
	public ArrayList<String> getProcessAsPerlScript(boolean nolist)
	{
		Boolean allowIntegratedListIfMultiOption = !nolist;
		
		Script script = new Script();
		script.setAuthorMail(this.getArchitectMail().replaceAll("@", "\\\\@"));
		script.setType("process");
		script.setName(this.getName());
		script.meta.setVersion(this.getVersion());
		script.genContent();
		
		// hinzufuegen des prozess meta infos
		script.business.addCode("# metadata from the processmodel");
		script.business.addCode("my $PROCESS_NAME = '" + this.getName() + "';");
		script.business.addCode("my $PROCESS_VERSION = '" + this.getVersion() + "';");
		script.business.addCode("my $PROCESS_DESCRIPTION = \"" + this.getDescription() + "\";");
		script.business.addCode("my $PROCESS_ARCHITECTNAME = '" + this.getArchitectName() + "';");
		script.business.addCode("my $PROCESS_ARCHITECTMAIL = '" + this.getArchitectMail() + "';");
		script.business.addCode("my $PROCESS_CUSTOMERNAME = '" + this.getCustomerName() + "';");
		script.business.addCode("my $PROCESS_CUSTOMERMAIL = '" + this.getCustomerMail() + "';");
		script.business.addCode("my $PROCESS_CUSTOMERCOMPANY = '" + this.getCustomerCompany() + "';");
		script.business.addCode("my $PROCESS_PATH = '" + this.getPath() + "';");
		script.business.addCode("");
		script.business.addCode("# temporaere prozessdaten");
		script.business.addCode("my $PROCESS_STATUS = 'running';");
		script.business.addCode("my $PROCESS_START = scalar(localtime());");
		script.business.addCode("my $PROCESS_STOP = '';");
		script.business.addCode("my @PROCESS_LOGGING;");
		script.business.addCode("");

		script.business.addCode("#-------------------");
		script.business.addCode("# checkin pradar");
		script.business.addCode("#system(\"pradar checkin -process "+this.getName()+" -id $id -id2 "+this.getName()+" -resource \" . $instancedir . '/README.html' . \" -pversion $version\");");
		script.business.addCode("#system(\"pradar progress -process "+this.getName()+" -id $id -completed 0 -stepcount "+(this.getStep().size() - 1)+"\");");
		script.business.addCode("#-------------------");
		script.business.addCode("");
		
		// befuellen von %FILE und %VARIABLE
		script.business.addCode("#-------------------");
		script.business.addCode("# die folgenden hashes haben diese struktur: stepname => [ [optionname, value], [optionname, value]]");
		script.business.addCode("# anlegen des files hashes (die uebergebenen optionen werden um den pfad erweitert)");
		script.business.addCode("my %FILE;");
		script.business.addCode("$FILE{'root'} = [];");
		script.business.addCode("# anlegen des variable hashes");
		script.business.addCode("my %VARIABLE;");
		script.business.addCode("$VARIABLE{'root'} = [];");
		script.business.addCode("");
		
		// alle commit des root-steps durchegehn und die entsprechenden hashes befuellen mit den uebergebenen informationen
		for(Commit actCommit : this.getRootStep().getCommit())
		{
			// alle files
			for(File actFile : actCommit.getFile())
			{
				script.business.addCode("# option "+actFile.getKey());
				script.business.addCode("&importOptionToHash('file', \\%FILE, '"+ actFile.getKey() +"');");
			}
			// alle variablen
			for(Variable actVariable : actCommit.getVariable())
			{
				script.business.addCode("# option "+actVariable.getKey());
				script.business.addCode("&importOptionToHash('variable', \\%VARIABLE, '"+ actVariable.getKey() +"');");
			}
		}
		
		script.business.addCode("# option commitfiledummy");
		script.business.addCode("&importOptionToHash('file', \\%FILE, 'commitfiledummy');");
		
		script.business.addCode("# option commitvariabledummy");
		script.business.addCode("&importOptionToHash('variable', \\%VARIABLE, 'commitvariabledummy');");
		
		script.business.addCode("#-------------------");
		script.business.addCode("");

		// standard-Variablen anlegen
		script.business.addCode("#-------------------");
		script.business.addCode("# standardVariablen in den Hash (fuer step root) aufnehmen");
		script.business.addCode("");
		script.business.addCode("# _processName");
		script.business.addCode("&logit('debug', 'initCommitStandardVariable: _processName=' . $PROCESS_NAME);");
		script.business.addCode("push(@{$VARIABLE{'root'}}, ['_processName', $PROCESS_NAME]);");
		script.business.addCode("");
		script.business.addCode("# _processVersion");
		script.business.addCode("&logit('debug', 'initCommitStandardVariable: _processVersion=' . $PROCESS_VERSION);");
		script.business.addCode("push(@{$VARIABLE{'root'}}, ['_processVersion', $PROCESS_VERSION]);");
		script.business.addCode("");
		script.business.addCode("# _processDescription");
		script.business.addCode("&logit('debug', 'initCommitStandardVariable: _processDescription=' . $PROCESS_DESCRIPTION);");
		script.business.addCode("push(@{$VARIABLE{'root'}}, ['_processDescription', $PROCESS_DESCRIPTION]);");
		script.business.addCode("#-------------------");
		script.business.addCode("");
		
		// initCommitVariable anlegen
		script.business.addCode("#-------------------");
		script.business.addCode("# importieren von initCommitVariable");
		script.business.addCode("&importInitCommitVariable(\\%VARIABLE, '" + this.getInitCommitVariable() + "');");
		script.business.addCode("#-------------------");
		script.business.addCode("");
		
		// initCommitFile anlegen
		script.business.addCode("#-------------------");
		script.business.addCode("# importieren von initCommitFile");
		script.business.addCode("&importInitCommitVariable(\\%FILE, '" + this.getInitCommitFile() + "');");
		script.business.addCode("#-------------------");
		script.business.addCode("");
		
		// Verzeichnis anlegen, usw
		script.business.addCode("#-------------------");
		script.business.addCode("# ein instanzverzeichnis anlegen");
		script.business.addCode("mkdir $instancedir;");
		script.business.addCode("");
		script.business.addCode("# das basisverzeichnis merken");
		script.business.addCode("my $basedir = cwd();");
		script.business.addCode("");
		script.business.addCode("# und hinein wechseln");
		script.business.addCode("chdir $instancedir;");
		script.business.addCode("");
		script.business.addCode("#-------------------");
		
		// anlegen der step-tabelle
		script.business.addCode("#-------------------");
		script.business.addCode("# anlegen der step-tabelle");
		script.business.addCode("my %STEPS_TABELLE = (");

		// setzen der Ranks aller vorhandenen Steps
		this.setStepRanks();
		
		for(Step actStep : this.getStep())
		{
			if(!(actStep.getName().matches("^root$")))
			{
				script.business.addCode("			'"+actStep.getRank()+"' => {'rang' => '"+actStep.getRank()+"', 'stepnamen' => '"+actStep.getName()+"', 'beschreibung' => \""+actStep.getDescription()+"\", 'aufruf' => '', 'dir' => '', 'status' => 'waiting', 'log' => ''},");
			}
		}
		
		script.business.addCode(");");
		script.business.addCode("");
		
		script.business.addCode("#-------------------");
		script.business.addCode("# initiale ausgabe des html");
		script.business.addCode("&printHtmlOverview();");
		script.business.addCode("#-------------------");
		script.business.addCode("");

		// script-OPTIONS generieren aus den commit-objekten des root-steps
		Step rootStep = this.getRootStep();
		if(rootStep != null)
		{
			int reihenfolge = 0;
			for(Commit actCommitOfRootStep : rootStep.getCommit())
			{
				// alle Variablen
				for(Variable actVariable : actCommitOfRootStep.getVariable())
				{
					reihenfolge++;
					// String name, int minoccur, int maxoccur, String definition, String check, String def, String text1, String text2)
					String optionname = actVariable.getKey();
					int minoccur = actVariable.getMinoccur();
					int maxoccur =  actVariable.getMaxoccur();
					String definition = actVariable.getType();
					String check = "";

					//integrieren des ersten tests 'matchPattern'
					for(Test actTest : actVariable.getTest())
					{
						if(actTest.getName().matches("^matchPattern$"))
						{
							check = actTest.getParam().get(0).getContent();
						}
					}
					
					// default, falls vorhanden und nicht null
					String def = "";
					if (actVariable.getValue() != null)
					{
						def = actVariable.getValue();
					}
	
					// text1
					String text1 = "";
					
					// wenn es kein flag-parameter ist, soll ein hinweis auf die erwarteten werte geliefert werden
					if (! definition.matches("flag"))
					{
						text1 = "=";
						// wenn es free=false, gibt es vorgegebene werte - aus diesen einen pattern fuer den text1 bilden "|"-getrennt
						if(! actVariable.getFree())
						{
							for(String actChoice : actVariable.getChoice())
							{
								text1 += actChoice + "|";
							}
							text1 = text1.substring(0, text1.length()-1);
						}
						else
						{
							text1 += actVariable.getType().toUpperCase();
						}
					}
					String text2 = actVariable.getDescription();
	//				System.out.println("text2 vor dem ersetzen: "+text2);
					text2 = text2.replaceAll("'", "\\\\'");
	//				System.out.println("text2 nach dem ersetzen: "+text2);
					if (text2.equals("")) {text2 = "no description available";}
					
					// erzeugen der option im script
					script.addOption (optionname, reihenfolge, minoccur, maxoccur, definition, check, def, text1, text2, allowIntegratedListIfMultiOption);
				}
				
				// und fuer alle Files
				for(File actFile : actCommitOfRootStep.getFile())
				{
					reihenfolge++;
					// String name, int minoccur, int maxoccur, String definition, String check, String def, String text1, String text2)
					String optionname = actFile.getKey();
					int minoccur = actFile.getMinoccur();
					int maxoccur =  actFile.getMaxoccur();
					String definition = "string"; // filepfad ist ein string
					String check = "";
					
					//integrieren des ersten tests 'matchPattern'
					for(Test actTest : actFile.getTest())
					{
						if(actTest.getName().matches("^matchPattern$"))
						{
							check = actTest.getParam().get(0).getContent();
						}
					}
					
					// default, falls vorhanden
					String def = "";
	
					// text1
					String text1 = "=FILE";
					
					String text2 = actFile.getDescription();
					text2 = text2.replaceAll("'", "\\\\'");
					if (text2.equals("")) {text2 = "no description available";}
					
					// erzeugen der option im script
					script.addOption (optionname, reihenfolge, minoccur, maxoccur, definition, check, def, text1, text2, allowIntegratedListIfMultiOption);
				}
			}
		}
		
		// fuer jeden step einen perl-codeblock erzeugen, der jeweils den kommandoaufruf des work-Elementes aufloest
		for(Step actStep : this.getStepsLinearized())
		{
			try {
				script.addCode("business", actStep.getCommandResolveAsPerlCode());
			} catch (UnknownCodeBlockException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// fuer jeden step den perl-codeblock erzeugen
		for(Step actStep : this.getStepsLinearized())
		{
			try {
				script.addCode("business", actStep.getStepAsPerlCodeBlock());
			} catch (UnknownCodeBlockException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		script.business.addCode("#-------------------");
		script.business.addCode("# checkout pradar");
		script.business.addCode("#system(\"pradar checkout -process "+this.getName()+" -id $id -exitcode 0\");");
		script.business.addCode("#-------------------");
		
		// ist der prozess bis hier gelaufen, dann ist wohl alles i.o.
		script.business.addCode("");
		script.business.addCode("$PROCESS_STATUS = 'exit=0';");
		script.business.addCode("$PROCESS_STOP = scalar(localtime());");
		script.business.addCode("&printHtmlOverview();");
		script.business.addCode("");
		
		return script.getAll();
	}
	
	/**
	 * schreiben des aktuellen prozesses in ein xml file
	 * @throws JAXBException 
	 * 
	 **/

	public void writeXml()
	{
		java.io.File file = new java.io.File(this.outfilexml);
		JAXBContext jaxbContext;
		try
		{
			jaxbContext = JAXBContext.newInstance(de.prozesskraft.jaxb.pkraft.Process.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		
			// die daten aus this in das jaxb objekt mappen
			de.prozesskraft.jaxb.pkraft.Process xprocess = new de.prozesskraft.jaxb.pkraft.Process();
			DozerBeanMapper mapper = new DozerBeanMapper();
//			mapper.map(xprocess, this);
			mapper.map(this, xprocess);
			
			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			jaxbMarshaller.marshal(xprocess, file);
		}
		catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public Process readXml() throws JAXBException
	{
		if (this.getInfilexml() == null)
		{
			throw new NullPointerException();
		}
		
		JAXBContext context = JAXBContext.newInstance(de.prozesskraft.jaxb.pkraft.Process.class);
		Unmarshaller um = context.createUnmarshaller();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try
		{
			// stream auf das schema oeffnen (liegt im root verezichnis des jars in das es gepack wird)
			InputStream schemaStream = this.getClass().getResourceAsStream("/process.xsd");
			BufferedReader schemaReader = new BufferedReader(new InputStreamReader(schemaStream));

			// und in dieses temp-verzeichnis schreiben
			java.io.File tmpFile = java.io.File.createTempFile("avoge2013", "process.xsd");
			FileWriter fstream = new FileWriter(tmpFile);
			BufferedWriter schemaWriter = new BufferedWriter(fstream);

			String thisLine;
			while((thisLine = schemaReader.readLine()) != null)
			{
//				System.out.println(thisLine);
				schemaWriter.append(thisLine);
			}
			schemaWriter.close();

			// das temporaere schemafile beim unmarshaller angeben, damit es zur validierung verwendet wird
			schema = sf.newSchema(tmpFile);
			um.setSchema(schema);
		}
		catch (SAXException e)
		{
			System.err.println("error: reading schema.");
			e.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NullPointerException e2)
		{
			System.err.println("error: xml schema file not found");
			e2.printStackTrace();
		}

//		Process destObject = null;
		// das aktuelle xml-file in die jaxb-klassen einlesen
		try
		{
			System.err.println("debug: reading xml from " + this.getInfilexml());
			de.prozesskraft.jaxb.pkraft.Process xprocess = (de.prozesskraft.jaxb.pkraft.Process) um.unmarshal(new java.io.File(this.getInfilexml()));

//			System.out.println("xprocess variable1 free = "+xprocess.getStep().get(0).getCommit().get(0).getVariable().get(0).isFree());
//			BeanMappingBuilder builder = new BeanMappingBuilder()
//			{
//				protected void configure()
//				{
//					mapping(de.prozesskraft.jaxb.pkraft.Process.class, de.prozesskraft.pkraft.Process.class, oneWay(), mapId("A"), mapNull(true));
//				}
//			};
			
//			System.out.println("processName1: "+this.getName());
			DozerBeanMapper mapper = new DozerBeanMapper();
//			destObject = mapper.map(xprocess, de.prozesskraft.pkraft.Process.class);

//			ArrayList<String> myMappingFiles = new ArrayList<String>();
//			myMappingFiles.add("dozermapping.xml");
//			mapper.setMappingFiles(myMappingFiles);

			// alle steps in this loeschen
			this.removeStepAll();
			
			mapper.map(xprocess, this);
//			System.out.println("processName2: "+this.getName());

			// setzen der parenteintraege aller steps
			this.affiliate();
			
			// setzen der step ranks, falls steps hinzugekommen sind
			// dieser aufruf wird nur fuer den editor benoetigt
			// setStepRanks muss umformuliert werden, da es viel zu lange laeuft (ab 30 Steps ~ 60 sekunden)
//			this.setStepRanks();

			// die jaxb-klassen mit den domain-klassen mappen
//			System.out.println("processName3: "+this.getName());
			
			// ueberpruefen ob der process consistent ist
//			if(this.isProcessConsistent())
//			{
//				this.log("info", "check process consistency successfull.");
//			}
//			else
//			{
//				this.log("info", "check process consistency NOT successfull.");
//			}
		}
		catch (javax.xml.bind.UnmarshalException e)
		{
			System.err.println("error: cannot unmarshall xml-file: "+this.getInfilexml());
			e.printStackTrace();
		}

		return this;
	}
	
	/*----------------------------
	  method: liest das Objekt inklusiver aller referenzierten Objekte aus einer binaeren Datei (deserialisiert)
	----------------------------*/
	public Process readBinary()
	{
		String actualVersion = "1";

		// bekannte Versionen von pkraft.core
		Map<String,Object> allPkraftCoreImplementations = new HashMap<String,Object>();
		
		allPkraftCoreImplementations.put("1", new de.prozesskraft.pkraft.Process());
//		allPkraftCoreImplementations.put("07", new de.prozesskraft.pkraft.old07.Process());

		ArrayList<String> sortiertNachAktualitaet = new ArrayList<String>();
		sortiertNachAktualitaet.add("1");
//		sortiertNachAktualitaet.add("07");

		for(String triedVersion : sortiertNachAktualitaet)
		{

	//		Process proc1 = new Process();
			try
			{
				FileInputStream fs = new FileInputStream(this.infilebinary);
				ObjectInputStream is = new ObjectInputStream(fs);

				de.prozesskraft.pkraft.Process proc = null;

				// ist es die aktuellste version?
				if(triedVersion.equals(actualVersion))
				{
//					System.err.println("debug: binary version is newest version: "+actualVersion);
					proc = (Process)is.readObject();
//					System.err.println("debug: reading done");
				}
				
				else if(triedVersion.equals("07"))
				{

					System.err.println("debug: binary version: 07");
//					de.prozesskraft.pkraft.old07.Process proc07 = (de.prozesskraft.pkraft.old07.Process)is.readObject();
//					
//					DozerBeanMapper mapper = new DozerBeanMapper();
//					mapper.map(proc07, proc);
					
				}
				
				// setzen der parenteintraege aller steps
				proc.affiliate();

//				Process proc = (Process)is.readObject();
//				// den parent eintrag aller steps auf das neue objekt erneuern, da die referenz noch die alte ist
//				for(Step actualStep : processImplementation.getStep())
//				{				
//					actualStep.setParent(processImplementation);
//				}

				is.close();
	//			proc1 = proc;
	//			System.out.println("NAMEN des Prozesses proc: "+proc.getName());
	//			System.out.println("NAMEN des Prozesses proc1: "+proc1.getName());
	
				// wenn der eingelesene prozess in den file-feldern inhalte hat, sollen diese beibehalten werden
				// ansonsten sollen die inhalte von 'this' uebernommen werden
				
				proc.setInfilebinary(this.getInfilebinary());
				proc.setOutfilebinary(this.getOutfilebinary());
				
				if (proc.getInfilexml().equals(""))
				{
					proc.setInfilexml(this.getInfilexml());
				}
				
				if (proc.getOutfilexml().equals(""))
				{
					proc.setOutfilexml(this.getOutfilexml());
				}
				
				return proc;
			}
			catch(InvalidClassException e)
			{
				System.err.println("not the tried version serialVersionUID");
//				System.err.println(e.toString());
			}
			catch(ClassNotFoundException e)
			{
				System.err.println(e.toString());
			}
			catch(FileNotFoundException e)
			{
				System.err.println(e.toString());
			}
			catch(IOException e)
			{
				System.err.println(e.toString());
			}
		}
//		System.out.println("NAMEN des Prozesses proc1: "+proc1.getName());
		return null;
	}

	/*----------------------------
	  method: schreibt das Objekt inklusiver aller referenzierten Objekte in eine binaere Datei (selialisiert)
	----------------------------*/
	public void writeBinary()
	{
		try
		{
			FileOutputStream fs = new FileOutputStream(this.getOutfilebinary());
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(this);
			os.flush();
			os.close();
		}
		catch (FileNotFoundException e)
		{
			System.err.println(e.toString());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.err.println(e.toString());
			e.printStackTrace();
		}
	}

	/*----------------------------
	  method: schreibt den inhalt des prozesses in die konsole
	----------------------------*/
	public void printToc()
	{
		System.out.println("---TOC---instance<"+this.getName()+">---TOC---");
		System.out.println("        version: "+this.getModelVersion());
		System.out.println("    description: "+this.getDescription());
		System.out.println("           path: "+this.getPath());
		System.out.println("      architect: "+this.getArchitectName());
		System.out.println("number of steps: "+this.getStep().size());
		Iterator<Step> iterstep = this.getStep().iterator();
		while(iterstep.hasNext())
		{
			Step step = iterstep.next();
			System.out.println("-----------step: "+step.getName());
			System.out.println("         status: "+step.getStatus());
			System.out.println("    description: "+step.getDescription());
			System.out.println("   amount files: "+step.getFile().size());
			Iterator<File> iterfile = step.getFile().iterator();
			while (iterfile.hasNext())
			{
				File file = iterfile.next();
				System.out.println("->    filename: "+file.getFilename());
				System.out.println("   absfilename: "+file.getAbsfilename());
			}
			System.out.println("    amount vars: "+step.getVariable().size());
			Iterator<Variable> itervariable = step.getVariable().iterator();
			while (itervariable.hasNext())
			{
				Variable variable = itervariable.next();
				System.out.println("->     varname: "+variable.getKey());
				System.out.println("         value: "+variable.getValue());
			}
			System.out.println("   amount inits: "+step.getInits().size());

			ArrayList<Init> inits = step.getInits();
			Iterator<Init> iterinit = inits.iterator();
			while (iterinit.hasNext())
			{
				Init init = iterinit.next();
				System.out.println("->     initname: "+init.getListname());
				System.out.println("       fromstep: "+init.getFromstep());
				System.out.println("amount of matchs: "+init.getMatch().size());

				ArrayList<Match> matchs = init.getMatch();
				Iterator<Match> itermatch = matchs.iterator();
				while (itermatch.hasNext())
				{
					Match match = itermatch.next();
					System.out.println("->        field: "+match.getField());
					System.out.println("        pattern: "+match.getPattern());
				}
			}

			ArrayList<String> listnames = step.getListnames();
			Iterator<String> iterstring = listnames.iterator();
			while (iterstring.hasNext())
			{
				String listname = iterstring.next();
				System.out.println("->      listname: "+listname);
				Iterator<String> iterlistitem = step.getListItems(listname).iterator();
				while (iterlistitem.hasNext())
				{
					String listitem = iterlistitem.next();
					System.out.println("->      listitem: "+listitem);
				}
			}
		}
	}

	/**
	 * prints the content of log to stdout
	 */
	public void printLog()
	{
		for(Log actualLog : this.getLog())
		{
			actualLog.print();
		}
	}
	
	/*----------------------------
	  method: generiert eine neue managerid. aus aktueller zeit + zufallszahl
	----------------------------*/
	public double genManagerid ()
	{
		final Random generator = new Random();
		long time = System.currentTimeMillis();
		generator.setSeed(time);
		return generator.nextDouble();
	}
	
//	/*----------------------------
//	  method: stellt den status des prozesses fest. dieser ist abhaengig vom status aller steps
//	  alle steps=finished|cancelled => prozess=finished
//	  step=working => prozess=working
//  	  step=error => prozess=error
//	----------------------------*/
//	public void detStatus()
//	{
//		String newstatus = "finished";
//		Iterator<Step> iterstep = this.step.iterator();
//		while(iterstep.hasNext())
//		{
//			Step step = iterstep.next();
//			if ((step.getStatus().matches("waiting")) && (!(newstatus.matches("error|working"))))
//			{
//				newstatus = "waiting";
//			}
//			else if ((step.getStatus().matches("initializing|initialized|initialization failed|fanning|fanned|committing|comitted|working|worked")) && (!(newstatus.equals("error"))))
//			{
//				newstatus = "working";
//			}
//			else if (step.getStatus().matches("error"))
//			{
//				newstatus = "error";
//			}
//		}
//		this.setStatus(newstatus);
//	}

	/*----------------------------
	  method: entferne step aus prozess
	----------------------------*/
	public void removeStep (Step step)
	{
		this.step.remove(step);
	}
	
	public void removeStepAll ()
	{
		this.step = new ArrayList<Step>();
	}
	
	/**
	 * stores a message for the process
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.addLog(new Log(loglevel, logmessage));
	}
	
	/**
	 * relocates the logmessages to files
	 * @param
	 */
	public void logRelocate()
	{
		for(Step actStep : this.getStep())
		{
			actStep.logRelocate();
		}
	}
	
	/**
	 * sets the parent of all dependents to this process
	 */
	public void affiliate()
	{
		for(Step actualStep : this.step)
		{
			actualStep.setParent(this);
			actualStep.affiliate();
		}
	}
	
	/**
	 * schiebe den process einen bearbeitungsschritt weiter
	 * gehe alle steps durch und versuche zu initialisieren, arbeiten, committen
	 * @return
	 */
	public void doIt(String aufrufProcessSyscall, String aufrufProcessManager, String domainInstallationDirectory)
	{
		this.setStepStatusChangedWhileLastDoIt(false);
		
		if(this.run)
		{
			if(this.getStatus().equals("error") || this.getStatus().equals("finished"))
			{
				this.run = false;
			}

			for(Step actStep : this.getStep().toArray(new Step[this.getStep().size()]))
			{
				// alle steps, die nicht aus gutem grund beendet sind, sollen angeschoben werden
				if(!(actStep.getStatus().equals("finished"))  ||  !(actStep.getStatus().equals("cancelled")) ||  !(actStep.getStatus().equals("error")))
				{
					actStep.doIt(aufrufProcessSyscall, aufrufProcessManager, domainInstallationDirectory);
					
					// aktualisieren des floags ob sich etwas veraendert hat im letzten durchlauf
					if(actStep.isStatusChangedWhileLastDoIt())
					{
						this.setStepStatusChangedWhileLastDoIt(true);
					}
				}
			}
		}
	}
	
	
	public int getMaxLevel()
	{
		int maxLevel = 0;
		for(Step actualStep : this.getStep())
		{
			int actualLevel = actualStep.getLevel();
			if(actualLevel > maxLevel)
			{
				maxLevel = actualLevel;
			}
		}
		return maxLevel;
	}
	
	public int getAmountStepsOfLevel(int level)
	{
		int amountSteps = 0;
		for(Step actualStep : this.getStep())
		{
			int actualLevel = actualStep.getLevel();
			if(actualLevel == level)
			{
				amountSteps++;
			}
		}
		return amountSteps;
	}
	
	/**
	 * sets the actual stepranks
	 * @return rank
	 */
	public void setStepRanks()
	{
//		System.out.println("IN: "+new Timestamp(System.currentTimeMillis()));
		Map<Step,Integer> mapStepLevel = new HashMap<Step,Integer>();
		Map<String,Step> mapStepnameStep = new HashMap<String,Step>();
		int maxLevel = 0;

		for(Step actualStep : this.getStep())
		{
			int actualStepLevel = actualStep.getLevel();
			mapStepLevel.put(actualStep, actualStepLevel);
			if (actualStepLevel > maxLevel)
			{
				maxLevel = actualStepLevel;
			}
			mapStepnameStep.put(actualStep.getName(), actualStep);
		}
		
//		System.out.println("1: "+new Timestamp(System.currentTimeMillis()));

		for(int x = 0; x <= maxLevel; x++)
		{
//			System.out.println("11: "+new Timestamp(System.currentTimeMillis()));
			ArrayList<String> allStepNamesOfLevelX = new ArrayList<String>();			
			// alle durchgehen und nur die des aktuellen Levels einsammeln
			for(Step actualStep : mapStepLevel.keySet())
			{
//				System.out.println("111: "+new Timestamp(System.currentTimeMillis()));
				if(mapStepLevel.get(actualStep) == x)
				{
					allStepNamesOfLevelX.add(actualStep.getName());
				}
			}

			// die Collection mit Namen sortieren
//			System.out.println("12: "+new Timestamp(System.currentTimeMillis()));
			Collections.sort(allStepNamesOfLevelX);
//			System.out.println("13: "+new Timestamp(System.currentTimeMillis()));

			// die ranks in den steps setzen
			for(int y = 0; y < allStepNamesOfLevelX.size(); y++)
			{
//				System.out.println("132: "+new Timestamp(System.currentTimeMillis()));
				mapStepnameStep.get(allStepNamesOfLevelX.get(y)).setRank(x+"."+(y+1));
//				System.out.println("133: "+new Timestamp(System.currentTimeMillis()));
			}
		}
//		System.out.println("OUT: "+new Timestamp(System.currentTimeMillis()));
	}
	
	/*----------------------------
	  methods getter / setter
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getModelVersion()
	{
		return this.modelVersion;
	}

	public void setModelVersion(String modelVersion)
	{
		this.modelVersion = modelVersion;
	}

	public String getVersion()
	{
		return this.version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getPath()
	{
		return this.path;
	}

	/**
	 * returns the $PATH in the form <path>:<path>:...
	 * 
	 */
	public String getAbsPath()
	{
		String absPath = "";
		for(java.io.File dir : getPaths2())
		{
			if (absPath.equals(""))
			{
				absPath = dir.getAbsolutePath();
			}
			else
			{
				absPath = absPath + ":" + dir.getAbsolutePath();
			}
		}
		return absPath;
	}

	/**
	 * es geht hierbei um das Element 'path' im Element 'process'
	 * returns the $PATH as ArrayList<String>
	 * @return ArrayList<String>
	 * 
	 */
	public ArrayList<String> getPaths()
	{
		String[] patharray = this.path.split(":");
		ArrayList<String> paths = new ArrayList<String>(Arrays.asList(patharray));
		return paths;
	}

	/**
	 * es geht hierbei um das Element 'path' im Element 'process'
	 * returns the $PATH as ArrayList<File>
	 * @return ArrayList<java.io.File>
	 */
	public ArrayList<java.io.File> getPaths2()
	{
		ArrayList<java.io.File> paths = new ArrayList<java.io.File>();
		for(String path : this.getPaths())
		{
			String fullPath = null;
			// wenn absoluter path, dann unveraendert uebernehmen
			if(path.matches("^/.+"))
			{
				fullPath = path;
			}
			else
			{
				fullPath = new java.io.File(this.getInfilexml()).getParent()+"/"+path;
			}
			
			// ein File erstellen
			java.io.File dir = new java.io.File(fullPath);
			
			if (dir.exists() && dir.isDirectory())
			{
				paths.add(dir.getAbsoluteFile());
			}
			else
			{
				log("error", "path "+path+" does not exist");
			}
		}
		return paths;
	}

	/**
	 * liefert den string 'initCommitFile' zurueck in Form "pfad:pfad:pfad" zurueck
	 * @return
	 * einen string, der die absoluten pfade aller 'initcommitdir' enthaelt. trennzeichen ist ':'
	 * relative pfade werden auf absolute pfade umgesetzt, wobei getInfileXml als Basisverzeichnis verwendet wird
	 */
	public String getInitCommitFile()
	{
		return this.initCommitFile;
	}

	/**
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines ArrayList<String> zurueck
	 * @return
	 * die absoluten pfadnamen aller 'initcommitdir' 
	 * relative pfade werden auf absolute pfade umgesetzt, wobei getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<String> getInitCommitFileDirectory()
	{
		ArrayList<String> newFileArray = new ArrayList<String>();
		if ((this.initCommitFile != null) && (!(this.initCommitFile.equals(""))))
		{
			// trennzeichen ist ":"
			for(String actualInitCommitFile : this.initCommitFile.split(":"))
			{
				String newInitCommitFile = "";
				
				// evtl. vorkommen von "~" durch das home directory ersetzen
				actualInitCommitFile = actualInitCommitFile.replaceAll("~", System.getProperty("user.home"));
				
				// wenn relativer pfad, dann um das verzeichnis des xml-Files (Installationsverzeichnis process.xml) erweitern
				if (!(actualInitCommitFile.matches("^/")))
				{
					newInitCommitFile = new java.io.File(this.getInfilexml()).getParent() + "/" + actualInitCommitFile;
				}
				// wenn absoluter pfad, dann der ergebnisliste hinzufuegen
				else
				{
					newInitCommitFile = actualInitCommitFile;
				}
				newFileArray.add(newInitCommitFile);
			}
			
		}
		return newFileArray;
	}

	/**
	 * liefert alle eintrage 'initCommitFile' zurueck in Form
	 * eines ArrayList<java.io.File> zurueck
	 * @return
	 * die directories aller 'initCommitFile'
	 * relative pfade werden auf absolute pfade umgesetzt, wobei getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<java.io.File> getInitCommitFileDirectoryAsFile()
	{
		ArrayList<java.io.File> initcommitfile = new ArrayList<java.io.File>();
		for(String actualInitCommitFile : this.getInitCommitFileDirectory())
		{
			java.io.File initCommitFileAsFile = new java.io.File(actualInitCommitFile);
			log("debug", "directory as file: "+initCommitFileAsFile.getAbsolutePath());
			initcommitfile.add(initCommitFileAsFile);
		}
		return initcommitfile;
	}

	/**
	 * liefert alle files, die sich in den directories 'initCommitFile' befinden in der form eines ArrayList<java.io.File> zurueck
	 * @return
	 */
	public ArrayList<java.io.File> getInitCommitFileAsFile()
	{
		ArrayList<java.io.File> allInitCommitFileAsFile = new ArrayList<java.io.File>();
		ArrayList<java.io.File> initCommitFileDirectory = this.getInitCommitFileDirectoryAsFile();

		for(java.io.File actDirectory : initCommitFileDirectory)
		{
			if(actDirectory.isDirectory())
			{
				for(java.io.File actFile : actDirectory.listFiles())
				{
					if(actFile.isFile())	
					{
						allInitCommitFileAsFile.add(actFile);
					}
				}
			}
		}
		return allInitCommitFileAsFile;
	}

	public String getInitCommitVariable()
	{
		return this.initCommitVariable;
	}

	/**
	 * liefert alle directories 'initCommitVariable' zurueck in Form eines ArrayList<String>
	 * @return
	 * die absoluten pfadnamen aller 'initcommitvarfiles' 
	 * relative pfade werden auf absolute pfade umgesetzt, wobei das directory von getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<String> getInitCommitVariableDirectory()
	{
		ArrayList<String> newinitcommitvariables = new ArrayList<String>();
		if ((this.initCommitVariable != null) && (!(this.initCommitVariable.equals(""))))
		{
			String[] filesarray = this.initCommitVariable.split(":");

			for(String actualInitCommitVariable : filesarray)
			{
				String newInitCommitVariable = "";
				
				// evtl. vorkommen von "~" durch das home directory ersetzen
				actualInitCommitVariable = actualInitCommitVariable.replaceAll("~", System.getProperty("user.home"));

				if (!(actualInitCommitVariable.matches("^/")))
				{
					newInitCommitVariable = new java.io.File(this.getInfilexml()).getParent() + "/" + actualInitCommitVariable;
				}
				else
				{
					newInitCommitVariable = actualInitCommitVariable;
				}
				newinitcommitvariables.add(newInitCommitVariable);
			}
		}
		return newinitcommitvariables;
	}

	/**
	 * liefert alle pfade aus feld 'initCommitVariable' zurueck in Form eines ArrayList<java.io.File> zurueck
	 * @return
	 * relative pfade werden auf absolute pfade umgesetzt, wobei das directory von getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<java.io.File> getInitCommitVariableDirectoryAsFile()
	{
		ArrayList<java.io.File> initcommitvariable = new ArrayList<java.io.File>();
		for(String actualInitCommitVariable : this.getInitCommitVariableDirectory())
		{
			initcommitvariable.add(new java.io.File(actualInitCommitVariable));
		}
		
		return initcommitvariable;
	}

	public String getArchitectName()
	{
		return this.architectName;
	}

	public String getArchitectCompany()
	{
		return this.architectCompany;
	}

	public String getArchitectMail()
	{
		return this.architectMail;
	}

	public String getCustomerName()
	{
		return this.customerName;
	}

	public String getCustomerCompany()
	{
		return this.customerCompany;
	}

	public String getCustomerMail()
	{
		return this.customerMail;
	}

	/**
	 * 
	 * @return versionsnummer ohne punkte
	 */
	public String getModelVersionPlain()
	{
		String versionplain = this.modelVersion;
		versionplain.replaceAll("\\.", "");
		return versionplain;
	}

	public String getStatus()
	{
		String status = "unknown";

		ArrayList<String> statusAllSteps = new ArrayList<String>();

		for(Step actStep : this.getStep())
		{
			statusAllSteps.add(actStep.getStatus());
		}

		// ist der status 'error' vorhanden? prozess=error
		if(statusAllSteps.contains("error"))
		{
			status = "error";
			this.setTimeOfProcessFinishedOrError(System.currentTimeMillis());
			return status;
		}

		// wenn schluessel waiting/initializing/working/committing nicht vorhanden sind, und nur finished vorhanden ist, dann ist prozess finished
		else if(  statusAllSteps.contains("initializing") || statusAllSteps.contains("initialized") || statusAllSteps.contains("working")  || statusAllSteps.contains("worked") || statusAllSteps.contains("committing")   )
		{
			if(this.run)
			{
				// wenn der Prozess seit 12 Minuten nicht mehr beruehrt wurde, dann soll status=error sein
				if( this.getTouchInMillis() > 0 && (System.currentTimeMillis() - this.getTouchInMillis()) > 720000)
				{
					status = "abandoned";
					this.log("error", "instance has been abandoned. last touch has been: " + new Timestamp(this.getTouchInMillis()).toString() + ". now is: " + new Timestamp(System.currentTimeMillis()).toString());
				}
				else
				{
					status = "working";
					this.setTimeOfProcessFinishedOrError(0L);
				}
				return status;
			}
			else
			{
				status = "paused";
				this.setTimeOfProcessFinishedOrError(0L);
				return status;
			}
		}

		// wenn schluessel waiting vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist prozess waiting
		else if(  statusAllSteps.contains("waiting") )
		{
			status = "waiting";
			this.setTimeOfProcessFinishedOrError(0L);
			return status;
		}

		// wenn schluessel finished vorhanden ist und die vorherigen optionen nicht in Frage kommen, dann ist prozess finished
		else if(  statusAllSteps.contains("finished") )
		{
			status = "finished";
			this.setTimeOfProcessFinishedOrError(System.currentTimeMillis());
			
			return status;
		}

		return status;
	}

	/**
	 * @return
	 * returns the canonical path of rootdir.
	 * if rootdir is not set so far, a random rootdir will be set like "<basedir>/<processname>_<processversion>_<randominteger>"
	 * if process is a subprocess, then rootdir == basedir
	 */
	public String getRootdir()
	{
		if(this.isSubprocess())
		{
			return this.getBaseDir();
		}
		else
		{
			return this.getBaseDir()+"/"+this.getName()+"_v"+this.getVersion()+"_"+this.getId();
		}
	}

	/**
	 * @return
	 * returns the canonical path of the statistics directory.
	 */
	public String getStatisticDir()
	{
		return this.getRootdir()+"/processStatistic";
	}

	/**
	 * @return the baseDir
	 */
	public String getBaseDir() {
		return baseDir;
	}

	/**
	 * @param baseDir the baseDir to set
	 */
	public void setBaseDir(String baseDir)
	{
		// pfadkorrektur fuer BMW
		this.baseDir = this.modPathForBmw(baseDir);
	}
	
	/**
	 * only for bmw infrastructure: some parts of the path must be cut away
	 * @return modifiedPath
	 */
	public String modPathForBmw(String orgpath)
	{
		String modpath = "";
		if(orgpath.matches("^/net/[^/]+/[^/]+/proj/.+$"))
		{
			modpath = orgpath.replaceFirst("^/net/[^/]+/[^/]+/proj/", "/proj/");
//			System.err.println("SETBASEDIR: " + this.baseDir);
		}
		else
		{
			modpath = orgpath;
//			System.err.println("SETBASEDIR: " + this.baseDir);
		}
		return modpath;
	}

	/**
	 * @return the id
	 */
	public void genRandomId()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		int randomInt = generator.nextInt(100000000);

		// assemble a random name for instanceDir
		Calendar now = Calendar.getInstance();
		DecimalFormat df2 = new DecimalFormat("00");
		this.idRumpf = now.get(Calendar.YEAR) + df2.format(now.get(Calendar.MONTH)+1) + df2.format(now.get(Calendar.DAY_OF_MONTH)) + "_" + randomInt;
	}
	
	/**
	 * @return the id
	 */
	public String getId()
	{
		return this.idRumpf + String.valueOf((char)(this.getCloneGeneration() +64)) + this.getCloneDescendant();
	}
	
	public double getManagerid()
	{
		return this.managerid;
	}

	public String getInfilebinary()
	{
		return this.infilebinary;
	}

	public String getOutfilebinary()
	{
		return this.outfilebinary;
	}

	public String getInfilexml()
	{
		return this.infilexml;
	}

	public String getOutfilexml()
	{
		return this.outfilexml;
	}

	public String getOutFileDoc()
	{
		return this.outFileDoc;
	}

	public String getFiledoctemplateodf()
	{
		return this.filedoctemplateodf;
	}

	public String getFileDocJrxml()
	{
		return this.fileDocJrxml;
	}

//	public Step getStep(String stepname)
//	{
//		for(int i=0; i<steps.size(); i++)
//		{
//			Step step = steps.get(i);
//			if (step.getName().endsWith(stepname))
//			{
//				return step;
//			}
//		}
//		System.out.println("no step with name "+stepname+" found - this should be implemented as an exception, shouldn't it");
//		return null;
//	}

	public Step getStep(int id)
	{
		return this.step.get(id);
	}

	public ArrayList<Step> getStep()
	{
		return this.step;
	}

	/**
	 * liefert alle steps, die erfolgreich verarbeitet wurden, zurueck
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepFinished()
	{
		ArrayList<Step> finishedStep = new ArrayList<Step>();
		for(Step actStep : this.getStep())
		{
			if(actStep.getStatus().equals("finished"))
			{
				finishedStep.add(actStep);
			}
		}
		return finishedStep;
	}
	
	/**
	 * liefert alle steps, die erfolgreich verarbeitet wurden, zurueck
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepFinishedOrCanceled()
	{
		ArrayList<Step> finishedOrCanceledStep = new ArrayList<Step>();
		for(Step actStep : this.getStep())
		{
			if(actStep.getStatus().equals("finished") || actStep.getStatus().equals("canceled"))
			{
				finishedOrCanceledStep.add(actStep);
			}
		}
		return finishedOrCanceledStep;
	}
	
	/**
	 * liefert alle steps, die momentan im status 'initialized' sind
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepInitialized()
	{
		ArrayList<Step> steps = new ArrayList<Step>();
		for(Step actStep : this.getStep())
		{
			if(actStep.getStatus().equals("initialized"))
			{
				steps.add(actStep);
			}
		}
		return steps;
	}
	
	/**
	 * liefert alle steps, die momentan im status 'working' sind
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepWorking()
	{
		ArrayList<Step> workingSteps = new ArrayList<Step>();
		for(Step actStep : this.getStep())
		{
			if(actStep.getStatus().equals("working"))
			{
				workingSteps.add(actStep);
			}
		}
		return workingSteps;
	}
	
	/**
	 * liefert alle steps, die fehler enthalten,, zurueck
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepError()
	{
		ArrayList<Step> errorStep = new ArrayList<Step>();
		for(Step actStep : this.getStep())
		{
			if(actStep.getStatus().equals("error"))
			{
				errorStep.add(actStep);
			}
		}
		return errorStep;
	}
	
	/**
	 * liefert alle steps, die noch verarbeitet werden muessen, zurueck
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepTogo()
	{
		ArrayList<Step> togoStep = new ArrayList<Step>();
		for(Step actStep : this.getStep())
		{
			if(!actStep.getStatus().equals("finished") || !actStep.getStatus().equals("cancelled"))
			{
				togoStep.add(actStep);
			}
		}
		return togoStep;
	}
	

	/**
	 * liefert alle steps, nach dem Rank sortiert (linearisiert), zurueck
	 * @param 
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepsLinearized()
	{
		ArrayList<Step> stepsSorted = new ArrayList<Step>();
		
		boolean weitermachen = true;
		int actLevel = 1;
		
		while(weitermachen)
		{
			ArrayList<Step> allStepsOfActLevel = new ArrayList<Step>();
			allStepsOfActLevel.addAll(this.getStepByLevel(actLevel));
			
			// wenn es keine mit diesem Level gibt, sind wir fertig
			if(allStepsOfActLevel.size() == 0)
			{
				weitermachen = false;
			}
			
			else
			{
				stepsSorted.addAll(allStepsOfActLevel);
				actLevel++;
			}
		}
		return stepsSorted;
	}

	/**
	 * liefert alle steps, die zu einem level gehoeren 
	 * @param int
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepByLevel(int level)
	{
		ArrayList<Step> stepOfLevel = new ArrayList<Step>();
		
		// von jedem Step den level ermitteln
		for(Step actStep : this.getStep())
		{
			if(level == actStep.getLevel())
			{
				stepOfLevel.add(actStep);
			}
		}
		return stepOfLevel;
	}

	/**
	 * liefert alle steps, die von einem bestimmten step abhaengig sind
	 * @param String
	 * @return ArrayList<Step>
	 */
	public ArrayList<Step> getStepDependent(String stepname)
	{
		String stumpfStepname = stepname;
		// falls der stepnamen ein multistep ist muss erst der stumpf des namens festgestellt werden
		if(stepname.matches("^[^@]+@.+$"))
		{
//			System.err.println("resetBecauseOfDependency: enthaelt ein @");
			Pattern p = Pattern.compile("^([^@]+)@.+$");
			Matcher m = p.matcher(stepname);
			
			if(m.find())
			{
//				System.err.println("resetBecauseOfDependency: enthaelt ein @ alter namen ist "+m.group(1));
				stumpfStepname = m.group(1);
			}
		}
		
//		System.err.println("stumpfnamen: "+stumpfStepname);
		
		ArrayList<Step> allDependentSteps = new ArrayList<Step>();
		
		// feststellen aller abhaengigen Steps von dem einen genannten Step
		for(Step actStep : this.getStep())
		{
			for(Init actInit : actStep.getInit())
			{
				if(actInit.getFromstep().equals(stumpfStepname))
				{
					// den tatsaechlichen step (inkl. evtl. @-extension) der gefundenen-liste hinzufuegen
					if(!allDependentSteps.contains(actStep))
					{
						allDependentSteps.add(actStep);
					}
				}
			}
		}

		boolean aenderungImLetztenLauf = true;
		// solange die abhaengigen Steps zunehmen, so lange nach neuen abhaengigen suchen
		while(aenderungImLetztenLauf)
		{
			ArrayList<Step> newDependentSteps = new ArrayList<Step>();
			// fuer alle bisher bekannten abhaengigen Steps
			for(Step actDependentStep : allDependentSteps)
			{
				String actDependentStepStumpfName = actDependentStep.getName();
				// evtl. den Namen auf den stumpf reduzieren
				if(actDependentStep.getName().matches("^.*@.*$"))
				{
//					System.err.println("resetBecauseOfDependency: enthaelt ein @");
					Pattern p = Pattern.compile("^([^@]+)@+$");
					Matcher m = p.matcher(actDependentStep.getName());
					
					if(m.find())
					{
//						System.err.println("resetBecauseOfDependency: enthaelt ein @ alter namen ist "+m.group(1));
						actDependentStepStumpfName = m.group(1);
					}
				}

				// alle Steps durchgehen
				for(Step actStep : this.getStep())
				{
					
					// alle Inits durchgehen
					for(Init actInit : actStep.getInit())
					{
						// wenn ein Init einen Fromstepverweis hat, der einem bekannten abhaengigen Step entspricht
						if(actInit.getFromstep().equals(actDependentStepStumpfName))
						{
							// und dieser noch nicht in der abhaengigen liste drin ist
							if(!allDependentSteps.contains(actStep))
							{
								// der liste hinzufuegen
								newDependentSteps.add(actStep);
							}
						}
					}
				}
			}
			// wenn beim letzten durchlauf kein neuer dependent step hinzugekommen ist, soll nicht mehr weiter gesucht werden
			if(newDependentSteps.isEmpty())
			{
				aenderungImLetztenLauf = false;
			}
			// sonst ja und weitersuchen
			else
			{
				// nur hinzufuegen falls noch nicht enthalten
				for(Step actNewDependantStep : newDependentSteps)
				{
					if(!allDependentSteps.contains(actNewDependantStep))
					{
						allDependentSteps.add(actNewDependantStep);
						aenderungImLetztenLauf = true;
					}
				}
			}
		}
		
		return allDependentSteps;
	}
	/**
	 * gibt alle steps zurueck, die mindestens 1 init haben, welches einen match auf den key der variable enthaelt
	 * @param type (String: variable|file)
	 * @param key
	 * @return
	 */
	public ArrayList<Step> getStepWhichNeedFromRoot(String type, String key)
	{
		ArrayList<Step> allStepsThatNeedSomething = new ArrayList<Step>();
		
		for(Step actStep : this.getStep())
		{
			for(Init actInit : actStep.getInit())
			{
				if( actInit.getFromobjecttype().equals("variable") && actInit.getFromstep().equals("root") )
				{
					for(Match actMatch : actInit.getMatch())
					{
						if(actMatch.getField().equals("key") && !actMatch.getPattern().matches("^.*\\$.*$") )
						{
							if(key.matches(actMatch.getPattern()))
							{
								allStepsThatNeedSomething.add(actStep);
								break;
							}
						}
					}
				}
				else if( actInit.getFromobjecttype().equals("file") && actInit.getFromstep().equals("root") )
				{
					for(Match actMatch : actInit.getMatch())
					{
						if(actMatch.getField().equals("key") && !actMatch.getPattern().matches("^.*\\$.*$") )
						{
							if(key.matches(actMatch.getPattern()))
							{
								allStepsThatNeedSomething.add(actStep);
								break;
							}
						}
					}
				}
			}
		}

		return allStepsThatNeedSomething;
	}
	
	/**
	 * liefert den step zurueck auf den der namen exakt passt
	 * @param stepname
	 * @return step
	 */
	public Step getStep(String stepname)
	{
		for(Step actStep : this.getStep())
		{
			// den namen abgleichen und merken wenn uebereinstimmung
			if ( (actStep.getName().equals(stepname)) )
			{
				return actStep;
			}
		}
		return null;
	}

	/**
	 * liefert den root Step zurueck
	 * @return
	 */
	public Step getRootStep()
	{
		return this.getStep(this.getRootstepname());
	}

	/**
	 * liefert die information ob ein step mit dem angegebenen namen existiert
	 * @param stepname
	 * @return exists
	 */
	public boolean isStep(String stepname)
	{
		boolean vorhanden = false;
		Step step = this.getStep(stepname);

		if (step != null)
		{
			vorhanden = true;
		}
		
		return vorhanden;
	}

	
	/**
	 * liefert den step zurueck auf den der namen exakt passt
	 * inkl. aller aufgefaecherter steps
	 * @param stepname
	 * @return steps
	 */
	public ArrayList<Step> getSteps(String stepname)
	{
		ArrayList<Step> steps = new ArrayList<Step>();
		
		if(stepname == null)
		{
			return steps;
		}
		
		
		for(Step actualStep : this.getStep())
		{
//			System.err.println("amount of steps in total: "+this.getStep().size());
//			System.err.println("name of actualStep "+actualStep.getName() + " | checking if " + stepname +" does match the actal step");
			if ( (actualStep.getName().matches("^"+stepname+"(@.+)?$")) )
			{
				steps.add(actualStep);
			}
		}
		
		return steps;
	}

	public Step[] getSteps2()
	{
		Step[] steps = new Step[this.getStep().size()];
		for(int i=0; i<steps.length; i++)
		{
			steps[i] = this.step.get(i);
		}
		return steps;
	}

	public String[] getStepnames()
	{
		String[] stepnames = new String[this.getStep().size()];
		for(int i=0; i<stepnames.length; i++)
		{
			stepnames[i] = this.step.get(i).getName();
		}
		return stepnames;
	}
	
	public String getRootstepname()
	{
		return rootstepname;
	}

	public ArrayList<Log> getLog()
	{
		return this.log;
	}

	public String getTouchAsString()
	{
		return touchAsString;
	}

	public long getTouchInMillis()
	{
		return touchInMillis;
	}

	public void setStep(ArrayList<Step> step)
	{
		this.step = step;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public void setInitCommitFile(String initcommitfile)
	{
		this.initCommitFile = initcommitfile;
	}

	public void setInitCommitVariable(String initcommitvariable)
	{
		this.initCommitVariable = initcommitvariable;
	}

	public void setArchitectName(String architectName)
	{
		this.architectName = architectName;
	}

	public void setArchitectCompany(String architectCompany)
	{
		this.architectCompany = architectCompany;
	}

	public void setArchitectMail(String architectMail)
	{
		this.architectMail = architectMail;
	}

	public void setCustomerName(String customerName)
	{
		this.customerName = customerName;
	}

	public void setCustomerCompany(String customerCompany)
	{
		this.customerCompany = customerCompany;
	}

	public void setCustomerMail(String customerMail)
	{
		this.customerMail = customerMail;
	}

	public boolean isWrapper()
	{
		return this.wrapper;
	}

	public void setWrapper(boolean wrapper)
	{
		this.wrapper = wrapper;
	}

	public void setStatus(String status)
	{
		this.status = status;
		firePropertyChange("status", this.status, this.status = status);
	}

	/**
	 * @return the cloneGeneration
	 */
	public int getCloneGeneration() {
		return cloneGeneration;
	}

	/**
	 * @param cloneGeneration the cloneGeneration to set
	 */
	public void setCloneGeneration(int cloneGeneration) {
		this.cloneGeneration = cloneGeneration;
	}

	/**
	 * @return the cloneDescendant
	 */
	public int getCloneDescendant() {
		return cloneDescendant;
	}

	/**
	 * @param cloneDescendant the cloneDescendant to set
	 */
	public void setCloneDescendant(int cloneDescendant) {
		this.cloneDescendant = cloneDescendant;
	}

	/**
	 * @return the clonePerformed
	 */
	public int getClonePerformed() {
		return clonePerformed;
	}

	/**
	 * @param clonePerformed the clonePerformed to set
	 */
	public void setClonePerformed(int clonePerformed) {
		this.clonePerformed = clonePerformed;
	}

	public boolean makeRootdir()
	{
		// wenn der prozess noch keinen namen hat, dann soll kein rootDir angelegt werden
		if(this.getName().equals("unnamed"))
		{
			return false;
		}
		else
		{
			java.io.File dir = new java.io.File(this.getRootdir());
			if (!(dir.exists()))
			{
				dir.mkdirs();
			}
			return true;
		}
	}

	public void setManagerid(double managerid)
	{
		this.managerid = managerid;
	}

//	public void setDatetonow()
//	{
//		
//		this.date = new Date();
//	}

	public void setInfilebinary(String file)
	{
		this.infilebinary = file;
	}

	public void setInfilexml(String file)
	{
		this.infilexml = file;
	}

	public void setOutfilebinary(String file)
	{
		this.outfilebinary = file;
	}

	public void setOutfilexml(String file)
	{
		this.outfilexml = file;
	}

	public void setOutFileDoc(String file)
	{
		this.outFileDoc = file;
	}

	public void setFiledoctemplateodf(String file)
	{
		this.filedoctemplateodf = file;
	}

	public void setFileDocJrxml(String file)
	{
		this.fileDocJrxml = file;
	}

	public void setRootstepname(String rootstepname)
	{
		this.rootstepname = rootstepname;
	}

	public void touch()
	{
		long now = System.currentTimeMillis();
		setTouchAsString(new Timestamp(now).toString());
		setTouchInMillis(now);
	}

	public void setTouchAsString(String touchAsString)
	{
		firePropertyChange("touchAsString", this.touchAsString, this.touchAsString = touchAsString);
	}

	public void setTouchInMillis(long touchInMillis)
	{
		this.touchInMillis = touchInMillis;
	}

	/*----------------------------
	  methods consistent
	----------------------------*/

	/**
	 * checks whether the content of field is consistent
	 * @return result
	 */
	public boolean isNameConsistent()
	{
		if(this.getName().equals("") || this.getName().matches("<|>|:|\"|/|\\|?|\\*|\\||\\)|\\(") )
		{
			this.log("error", "process/name contains at least one bad character ()<>:\"\\?*|");
			return false;
		}
		else
		{
			this.log("debug", "content of process/name is ok");
			return true;
		}
	}

	/**
	 * checks whether the content of field is consistent
	 * @return result
	 */
	public boolean isModelVersionConsistent()
	{
		if(this.getModelVersion().equals("") || this.getModelVersion().matches("<|>|:|\"|/|\\|?|\\*|\\||\\)|\\(") )
		{
			this.log("error", "process/modelVersion contains at least one bad character ()<>:\"\\?*|");
			return false;
		}
		else
		{
			this.log("debug", "content of process/modelVersion is ok");
			return true;
		}
	}

	/**
	 * checks whether the content of process is consistent
	 * @return result
	 */
	public boolean isProcessConsistent()
	{
		boolean result = true;

		// check the name
		if( !this.isNameConsistent() )			{result = false;	log("error", "error in process/name");}
		
		// check the modelVersion
		if( !this.isModelVersionConsistent() )	{result = false;	log("error", "error in process/modelVersion");}
		
		// gibt es einen root-step?
		if ( this.getStep(this.getRootstepname()) == null ) {result = false;	log("error", "there is no initial step with expected name "+this.getRootstepname());}
		
		// check all steps
		for (Step actualStep : this.getStep())
		{
			if ( !actualStep.isStepConsistent() ) {result = false;	log("error", "error in step '"+actualStep.getName()+"'");}
			else {log("debug", "content of step '"+actualStep.getName()+"' is ok");}
		}
		
		return result;
	}

	/**
	 * @return the subprocess
	 */
	public boolean isSubprocess() {
		return subprocess;
	}

	/**
	 * @param subprocess the subprocess to set
	 */
	public void setSubprocess(boolean subprocess) {
		this.subprocess = subprocess;
	}

	/**
	 * @return the parentid
	 */
	public String getParentid() {
		return parentid;
	}

	/**
	 * @param parentid the parentid to set
	 */
	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	/**
	 * @return the id2
	 */
	public String getId2Variable() {
		return id2Variable;
	}

	/**
	 * @param id2 the id2 to set
	 */
	public void setId2Variable(String id2) {
		this.id2Variable = id2;
	}

	/**
	 * feststellen der id2
	 * die erste Variable mit dem key id2Variable -> dessen value wird zurueckgegeben
	 */
	public String getId2()
	{
		if(this.getId2Variable() == null)
		{
			return "";
		}

		String id2 = "";
		for(Variable actVar : this.getRootStep().getVariable())
		{
			if(actVar.getKey().equals(this.getId2Variable()))
			{
				return actVar.getValue();
			}
//			else
//			{
//				id2 += "/"+ actVar.getKey() + ".equals(" + this.getId2Variable() + ")";
//			}
		}
		return id2;
	}

	/**
	 * @return the maxSimultaneousSteps
	 */
	public Integer getMaxSimultaneousSteps() {
		return maxSimultaneousSteps;
	}

	/**
	 * @param maxSimultaneousSteps the maxSimultaneousSteps to set
	 */
	public void setMaxSimultaneousSteps(Integer maxSimultaneousSteps) {
		this.maxSimultaneousSteps = maxSimultaneousSteps;
	}

	/**
	 * @return the stepStartDelayMinutesMinimumOfInitializedSteps
	 */
	public Integer stepStartDelayMinutesMinimumOfInitializedSteps()
	{
		ArrayList<Integer> allStepStartDelayMinutes = new ArrayList<Integer>();
		
		// aller steps, die noch verarbeitet werden muessen, falls vorhanden
		for(Step actStep : this.getStepInitialized())
		{
//			System.err.println("debug: this step is initialized: " + actStep.getName());
			if(actStep.getStepStartDelayMinutes() != null)
			{
				allStepStartDelayMinutes.add(actStep.getStepStartDelayMinutes());
			}
			else if(this.getStepStartDelayMinutes() != null)
			{
				allStepStartDelayMinutes.add(this.getStepStartDelayMinutes());
			}
			else
			{
				allStepStartDelayMinutes.add(0);
			}
		}
		
		// rueckgabe
		if(allStepStartDelayMinutes.size() == 0)
		{
			return null;
		}
		else
		{
			return Collections.min(allStepStartDelayMinutes);
		}
	}

	/**
	 * @return the stepStartDelayMinutes
	 */
	public Integer getStepStartDelayMinutes() {
		return stepStartDelayMinutes;
	}

	/**
	 * @param stepStartDelayMinutes the stepstartDelayMinutes to set
	 */
	public void setStepStartDelayMinutes(Integer stepStartDelayMinutes) {
		this.stepStartDelayMinutes = stepStartDelayMinutes;
	}

	/**
	 * @return the timeOfLastStepStart
	 */
	public Long getTimeOfLastStepStart() {
		return timeOfLastStepStart;
	}

	/**
	 * @param timeOfLastStepStart the timeOfLastStepStart to set
	 */
	public void setTimeOfLastStepStart(Long timeOfLastStepStart) {
		this.timeOfLastStepStart = timeOfLastStepStart;
	}

	/**
	 * @return the run
	 */
	public boolean isRun() {
		return run;
	}

	/**
	 * @param run the run to set
	 */
	public void setRun(boolean run) {
		this.run = run;
	}

	/**
	 * @return the idRumpf
	 */
	public String getIdRumpf() {
		return idRumpf;
	}

	/**
	 * @param idRumpf the idRumpf to set
	 */
	public void setIdRumpf(String idRumpf) {
		this.idRumpf = idRumpf;
	}

	/**
	 * @return the timeOfProcessCreated
	 */
	public Long getTimeOfProcessCreated() {
		return timeOfProcessCreated;
	}

	/**
	 * @param timeOfProcessCreated the timeOfProcessCreated to set
	 */
	public void setTimeOfProcessCreated(Long timeOfProcessCreated) {
		this.timeOfProcessCreated = timeOfProcessCreated;
	}

	/**
	 * @return the timeOfProcessFinishedOrError
	 */
	public Long getTimeOfProcessFinishedOrError() {
		return timeOfProcessFinishedOrError;
	}

	/**
	 * @param timeOfProcessFinishedOrError the timeOfProcessFinishedOrError to set
	 */
	public void setTimeOfProcessFinishedOrError(Long timeOfProcessFinishedOrError) {
		this.timeOfProcessFinishedOrError = timeOfProcessFinishedOrError;
	}

	/**
	 * @return the stepnameOfParent
	 */
	public String getStepnameOfParent() {
		return stepnameOfParent;
	}

	/**
	 * @param stepnameOfParent the stepnameOfParent to set
	 */
	public void setStepnameOfParent(String stepnameOfParent) {
		this.stepnameOfParent = stepnameOfParent;
	}

	/**
	 * @return the stepStartLoadAverageBelow
	 */
	public Double getStepStartLoadAverageBelow() {
		return stepStartLoadAverageBelow;
	}

	/**
	 * @param stepStartLoadAverageBelow the stepStartLoadAverageBelow to set
	 */
	public void setStepStartLoadAverageBelow(Double stepStartLoadAverageBelow) {
		this.stepStartLoadAverageBelow = stepStartLoadAverageBelow;
	}

	/**
	 * @return the timeSerieLoadAverage
	 */
	public Timeserie getTimeSerieLoadAverage() {
		return timeSerieLoadAverage;
	}

	/**
	 * @param timeSerieLoadAverage the timeSerieLoadAverage to set
	 */
	public void setTimeSerieLoadAverage(Timeserie timeSerieLoadAverage) {
		this.timeSerieLoadAverage = timeSerieLoadAverage;
	}

	/**
	 * @return the timeSerieBinarySize
	 */
	public Timeserie getTimeSerieBinarySize() {
		return timeSerieBinarySize;
	}

	/**
	 * @param timeSerieBinarySize the timeSerieBinarySize to set
	 */
	public void setTimeSerieBinarySize(Timeserie timeSerieBinarySize) {
		this.timeSerieBinarySize = timeSerieBinarySize;
	}

	/**
	 * @return the timeSerieStepSize
	 */
	public Timeserie getTimeSerieStepSize() {
		return timeSerieStepSize;
	}

	/**
	 * @param timeSerieStepSize the timeSerieStepSize to set
	 */
	public void setTimeSerieStepSize(Timeserie timeSerieStepSize) {
		this.timeSerieStepSize = timeSerieStepSize;
	}

	/**
	 * @return the stepStatusChangedWhileLastDoIt
	 */
	public boolean isStepStatusChangedWhileLastDoIt() {
		return stepStatusChangedWhileLastDoIt;
	}

	/**
	 * @param stepStatusChangedWhileLastDoIt the stepStatusChangedWhileLastDoIt to set
	 */
	public void setStepStatusChangedWhileLastDoIt(boolean stepStatusChangedWhileLastDoIt) {
		this.stepStatusChangedWhileLastDoIt = stepStatusChangedWhileLastDoIt;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
