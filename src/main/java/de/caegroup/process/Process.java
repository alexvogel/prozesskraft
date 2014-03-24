package de.caegroup.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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

import org.dozer.DozerBeanMapper;
//import org.dozer.Mapper;
//import org.dozer.loader.api.BeanMappingBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.caegroup.codegen.Script;
import de.caegroup.codegen.UnknownCodeBlockException;


public class Process extends ModelObject
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/
	static final long serialVersionUID = 1;

	private String name = new String();
	private String description = new String();
	private String path = new String();
	private String initCommitDir = new String();
	private String initCommitVarfile = new String();
	private String architectName = new String();
	private String architectCompany = new String();
	private String architectMail = new String();
	private String customerName = new String();
	private String customerCompany = new String();
	private String customerMail = new String();
	private String modelVersion = new String();
	private String version = new String();
	private boolean pradar = false;
//	private NamedList<Step> steps = new NamedList<Step>();
	private ArrayList<Step> step = new ArrayList<Step>();
//	private ArrayList<Init> inits = new ArrayList<Init>();
	
	private String status = new String();	// waiting/working/finished/broken/paused
	private String rootdir = "";
	private double managerid = -1;
	private Date date = new Date();
	private String infilebinary = "";
	private String infilexml = "";
	private String outfilebinary = "";
	private String outfilexml = "";
	private String outFileDoc = new String();
	private String filedoctemplateodf = new String();
	private String fileDocJrxml = new String();
	private String rootstepname = "root";
	private ArrayList<Log> log = new ArrayList<Log>();
	private int randomId = 0;  
	private String touchAsString = "";
	private long touchInMillis = 0;
	/*----------------------------
	  constructors
	----------------------------*/
	public Process()
	{
		name = "unnamed";
		description = "without description";
		status = "waiting";
		
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		randomId = generator.nextInt();
		
//		Step step = new Step(this);
//		step.setName(this.rootstepname);
//		this.addStep(step);
		
//		try
//		{
//			absdir = new java.io.File( "." ).getAbsolutePath();
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//		System.out.println("absdir von prozess ist: "+this.absdir);
	}

	/*----------------------------
	  methods
	----------------------------*/

	/**
	 * generates a new step with a random name and adds it to this.
	 */
	public void addStepp()
	{
		int zaehler = 1;
		String basename = "default_";
		while(this.isStep(basename+zaehler))
		{
			zaehler++;
		}
		this.addStep(basename+zaehler);
	}
	
	/**
	 * generates a new step with a certain name and adds it to this.
	 */
	public void addStep(String stepname)
	{
		Step step = new Step(this, stepname);
		this.addStep(step);
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
				System.err.println("stepname '"+stepnames[i]+"' already in use. step not added.");
				return false;
			}
		}
		step.setParent(this);
		this.step.add(step);
		return true;
	}

	public ArrayList<String> getProcessAsPerlScript()
	{
		Script script = new Script();
		script.setType("process");
		script.meta.setVersion(this.getVersion());
		script.genContent();
		
		// abpruefen ob die aufzurufenden programme aller steps verfuegbar sind
		// falls nein - abbrechen
		
		
		
		// script-OPTIONS generieren aus den commit-objekten des root-steps
		Step rootStep = this.getStep("root");
		for(Commit actCommitOfRootStep : rootStep.getCommit())
		{
			// alle Variablen
			for(Variable actVariable : actCommitOfRootStep.getVariable())
			{
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
				script.addOption (optionname, minoccur, maxoccur, definition, check, def, text1, text2);
			}
			
			// und fuer alle Files
			for(File actFile : actCommitOfRootStep.getFile())
			{
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
				script.addOption (optionname, minoccur, maxoccur, definition, check, def, text1, text2);
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

		return script.getAll();
	}
	

//	public void removeStep(String stepname)
//	{
//		this.steps.remove(stepname);
//	}

//	/**
//	 * schreibt die aktuelle Prozess-Definition als menschenlesbare Dokumentation im pdf-Format raus (basierend auf einem jasper-report)
//	 * @throws JRException 
//	 * @throws FileNotFoundException 
//	**/
//	public void writeDoc() throws FileNotFoundException
//	{
//		Report document = new Report();
//		
//		document.setJrxml(this.getFileDocJrxml());
//		
//		document.setParameter("processName", this.getName());
//		document.setParameter("processVersion", this.getVersion());
//		document.setParameter("processArchitect", this.getArchitect());
////		content.put("processAutomatic", this.isAutomatic());
//		document.setParameter("processStepCount", ""+this.getStep().size());
////		content.put("processParamCount", this.getParamCount());
//		document.setParameter("processDescription", this.getDescription());
////		document.setParameter("processTopologyImagePath", new FileInputStream("/austausch/avo/ampelmann_lauf.png"));
//		
//		document.compile();
//		document.fillPReport();
//		document.setPdf(this.getOutFileDoc());
//		document.exportToPdf();
//		
//	}
	
	/*----------------------------
	  method: 	liest eine Prozessdefinition aus einer xml-Datei in dieses Prozessobjekt ein.
	  			Alle bestehenden Definitionen gehen verloren. (Ausser infilebinary, infilexml, outfilebinary, outfilexml) 
	----------------------------*/
	public void writeXml()
	{
		try
		{
			FileOutputStream fs = new FileOutputStream(this.outfilexml);
			PrintWriter os = new PrintWriter(fs);
			
			StreamResult streamResult = new StreamResult(os);
			
			SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			
			// SAX2.0 ContentHandler
			TransformerHandler hd = tf.newTransformerHandler();

			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
//			serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "process.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			hd.setResult(streamResult);
			hd.startDocument();
			
			AttributesImpl atts = new AttributesImpl();
			
			hd.startElement("", "", "root", atts);
			
			// Definition der Attribute des 'process'-Elementes
			atts.addAttribute("", "", "name", "CDATA", this.name);
			atts.addAttribute("", "", "version", "CDATA", this.modelVersion);
			atts.addAttribute("", "", "description", "CDATA", this.description);
			atts.addAttribute("", "", "path", "CDATA", this.path);
			atts.addAttribute("", "", "initcommitdir", "CDATA", this.path);
			atts.addAttribute("", "", "initcommitvarfile", "CDATA", this.path);
			atts.addAttribute("", "", "architectMail", "CDATA", this.architectMail);
			
			hd.startElement("", "", "process", atts);

			// Fuer jeden Knoten 'step'
			for (int i=0; i<this.step.size();i++)
			{
				atts.clear();
				atts.addAttribute("", "", "name", "CDATA", this.step.get(i).getName());
				atts.addAttribute("", "", "type", "CDATA", this.step.get(i).getType());
				atts.addAttribute("", "", "description", "CDATA", this.step.get(i).getDescription());
				atts.addAttribute("", "", "loop", "CDATA", this.step.get(i).getLoop());
				atts.addAttribute("", "", "loopvar", "CDATA", this.step.get(i).getLoopvar());
				
				hd.startElement("", "", "step", atts);
				
				// Fuer jeden Knoten 'init'
				Init[] inits = this.step.get(i).getInits2();
				for (int j=0; j<inits.length; j++)
				{
					atts.clear();
					atts.addAttribute("", "", "name", "CDATA", inits[j].getListname());
					atts.addAttribute("", "", "fromobjecttype", "CDATA", inits[j].getFromobjecttype());
					atts.addAttribute("", "", "returnfield", "CDATA", inits[j].getReturnfield());
					atts.addAttribute("", "", "fromstep", "CDATA", inits[j].getFromstep());
					
					hd.startElement("", "", "init", atts);
				
					// Fuer jeden Knoten 'match'
					Match[] matchs = inits[j].getMatch2();
					for (int k=0; k<matchs.length; k++)
					{
						atts.clear();
						atts.addAttribute("", "", "field", "CDATA", matchs[k].getField());
						atts.addAttribute("", "", "pattern", "CDATA", matchs[k].getPattern());
						
						hd.startElement("", "", "match", atts);
						hd.endElement("", "", "match");
					}

					hd.endElement("", "", "init");

				}

				// Fuer den einen(!) Knoten 'work'
				Work work = this.step.get(i).getWork();
				atts.clear();
				atts.addAttribute("", "", "name", "CDATA", work.getName());
				atts.addAttribute("", "", "loop", "CDATA", work.getLoop());
				atts.addAttribute("", "", "loopvar", "CDATA", work.getLoopvar());
				atts.addAttribute("", "", "description", "CDATA", work.getDescription());
				atts.addAttribute("", "", "command", "CDATA", work.getCommand());
				
				hd.startElement("", "", "work", atts);
			
				// Fuer jeden Knoten 'callitem'
				Callitem[] callitems = work.getCallitems2();
				for (int k=0; k<callitems.length; k++)
				{
					atts.clear();
					atts.addAttribute("", "", "sequence", "CDATA", "" + callitems[k].getSequence());
					atts.addAttribute("", "", "loop", "CDATA", callitems[k].getLoop());
					atts.addAttribute("", "", "par", "CDATA", callitems[k].getPar());
					atts.addAttribute("", "", "del", "CDATA", callitems[k].getDel());
					atts.addAttribute("", "", "val", "CDATA", callitems[k].getVal());
					
					hd.startElement("", "", "callitem", atts);
					hd.endElement("", "", "callitem");
				}
				
				hd.endElement("", "", "work");
			
				// Fuer jeden Knoten 'commit'
				Commit[] commits = this.step.get(i).getCommits2();
				for (int j=0; j<commits.length; j++)
				{
					atts.clear();
					
						atts.addAttribute("", "", "id", "CDATA", commits[j].getName());
						atts.addAttribute("", "", "toroot", "CDATA", String.valueOf(commits[j].getToroot()));
					
					hd.startElement("", "", "commit", atts);
					hd.endElement("", "", "commit");
	
				}

				hd.endElement("", "", "step");
			}

			hd.endElement("", "", "process");
			hd.endElement("", "", "root");
			
			hd.endDocument();
		}
		catch (TransformerConfigurationException e)
		{
			System.out.println(e.toString());
		}
		catch (SAXException e)
		{
			System.out.println(e.toString());
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
		
	
	}
	
	public Process readXml() throws JAXBException
	{
		if (this.getInfilexml() == null)
		{
			throw new NullPointerException();
		}
		
		JAXBContext context = JAXBContext.newInstance(de.caegroup.jaxb.process.Process.class);
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
			de.caegroup.jaxb.process.Process xprocess = (de.caegroup.jaxb.process.Process) um.unmarshal(new java.io.File(this.getInfilexml()));
//			System.out.println("xprocess variable1 free = "+xprocess.getStep().get(0).getCommit().get(0).getVariable().get(0).isFree());
//			BeanMappingBuilder builder = new BeanMappingBuilder()
//			{
//				protected void configure()
//				{
//					mapping(de.caegroup.jaxb.process.Process.class, de.caegroup.process.Process.class, oneWay(), mapId("A"), mapNull(true));
//				}
//			};
			
//			System.out.println("processName1: "+this.getName());
			DozerBeanMapper mapper = new DozerBeanMapper();
//			destObject = mapper.map(xprocess, de.caegroup.process.Process.class);
			mapper.map(xprocess, this);
//			System.out.println("processName2: "+this.getName());

			// setzen der parenteintraege aller steps
			this.affiliate();
			// die jaxb-klassen mit den domain-klassen mappen
//			System.out.println("processName3: "+this.getName());
			
		}
		catch (javax.xml.bind.UnmarshalException e)
		{
			System.err.println("error: cannot unmarshall xml-file: "+this.getInfilexml());
			e.printStackTrace();
		}

		return this;
	}
	
	/*----------------------------
	  method: 	liest eine Prozessdefinition aus einer xml-Datei in dieses Prozessobjekt ein.
	  			Alle bestehenden Definitionen gehen verloren. (Ausser infilebinary, infilexml, outfilebinary, outfilexml) 
	----------------------------*/
	@SuppressWarnings("finally")
	public Process readXmlOld()
	{
		// neuen Prozess erzeugen
		final Process proc = new Process();

		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			org.w3c.dom.Document document = db.parse(this.infilexml);
			NodeList nodes_i = document.getDocumentElement().getChildNodes();

	//		int anzahl = nodes_i.getLength();

//			System.out.println("Jetzt wird gelesen");
			
			proc.setInfilexml(this.infilexml);
			proc.setInfilebinary(this.infilebinary);
			proc.setOutfilexml(this.outfilexml);
			proc.setOutfilebinary(this.outfilebinary);
			
			for (int i = 0; i < nodes_i.getLength(); i++)
			{
	
				Node node_i = nodes_i.item(i);
				if (node_i.getNodeType() == Node.ELEMENT_NODE && ((Element) node_i).getTagName().equals("process"))
				{
//					System.out.println("Node: process");
					// Lesen der xml-Informationen des Knotens 'process'
					Element lp = (Element) node_i;
					String processname = lp.getAttribute("name");
					String processversion = lp.getAttribute("version");
					String processdescription = lp.getAttribute("description");
					String processpath = lp.getAttribute("path");
					String processinitcommitdir = lp.getAttribute("initcommitdir");
					String processinitcommitvarfile = lp.getAttribute("initcommitvarfile");
					String processarchitect = lp.getAttribute("architectName");
	
					// Eintragen der gelesenen Daten in die Objektinstanz
					proc.setName(processname);
					proc.setModelVersion(processversion);
					proc.setDescription(processdescription);
					proc.setPath(processpath);
					proc.setInitCommitDir(processinitcommitdir);
					proc.setInitCommitVarfile(processinitcommitvarfile);
					proc.setArchitectName(processarchitect);
					
					// Ermitteln aller Childnodes
					NodeList nodes_j = lp.getChildNodes();
	
					for (int j = 0; j < nodes_j.getLength(); j++)
					{
						Node node_j = nodes_j.item(j);
						if (node_j.getNodeType() == Node.ELEMENT_NODE && ((Element) node_j).getTagName().equals("step"))
						{
//							System.out.println("Node: step");
							// Einspielen der xml-Informationen des Knotens 'step'
							Element ls = (Element) node_j;
							String stepname = ls.getAttribute("name");
							String steptype = ls.getAttribute("type");
							String stepdescription = ls.getAttribute("description");
							String steploop = ls.getAttribute("loop");
							String steploopvar = ls.getAttribute("loopvar");
							
							// Erstellen der Objektinstanz 'step' und einhaengen in den neuen Prozess
							Step step = new Step(this);
							proc.addStep(step);
							
							// Eintragen der gelesenen Daten in die Objektinstanz
							step.setName(stepname);
							step.setType(steptype);
							step.setDescription(stepdescription);
							step.setLoop(steploop);
							step.setLoopvar(steploopvar);
	
							// Ermitteln aller Childnodes
							NodeList nodes_k = ls.getChildNodes();
							
							for (int k = 0; k < nodes_k.getLength(); k++)
							{
								Node node_k = nodes_k.item(k);
								if (node_k.getNodeType() == Node.ELEMENT_NODE && ((Element) node_k).getTagName().equals("init"))
								{
//									System.out.println("Node: init");
									// Einspielen der xml-Informationen des Knotens 'init'
									Element lsu = (Element) node_k;
									String initname = lsu.getAttribute("name");
									String initfromobjecttype = lsu.getAttribute("fromobjecttype");
									String initreturnfield = lsu.getAttribute("returnfield");
									String initfromstep = lsu.getAttribute("fromstep");
									
									// Erstellen der Objektinstanz 'list' und einhaengen in den letzten step
									Init init = new Init();
									step.addInit(init);
									
									// Eintragen der gelesenen Daten in die Objektinstanz
									init.setListname(initname);
									init.setFromobjecttype(initfromobjecttype);
									init.setReturnfield(initreturnfield);
									init.setFromstep(initfromstep);
	
									// Ermitteln aller Childnodes
									NodeList nodes_l = lsu.getChildNodes();
									
									for (int l = 0; l < nodes_l.getLength(); l++)
									{
										Node node_l = nodes_l.item(l);
										if (node_l.getNodeType() == Node.ELEMENT_NODE && ((Element) node_l).getTagName().equals("match"))
										{
//											System.out.println("Node: match");
											// Einspielen der xml-Informationen des Knotens 'match'
											Element lm = (Element) node_l;
											String matchfield = lm.getAttribute("field");
											String matchpattern = lm.getAttribute("pattern");
	
											// Erstellen der Objektinstanz 'match' und einhaengen in den letzten list
											Match match = new Match();
											init.addMatch(match);
											
											// Eintragen der gelesenen Daten in die Objektinstanz
											match.setField(matchfield);
											match.setPattern(matchpattern);
										}
									}
									
								}
	
								if (node_k.getNodeType() == Node.ELEMENT_NODE && ((Element) node_k).getTagName().equals("work"))
								{
//									System.out.println("Node: work");
									// Einspielen der xml-Informationen des Knotens 'work'
									Element lw = (Element) node_k;
									String workname = lw.getAttribute("name");
									String workdescription = lw.getAttribute("description");
									String workcommand = lw.getAttribute("command");
									String workloop = lw.getAttribute("loop");
									String workloopvar = lw.getAttribute("loopvar");
									
									// Erstellen der Objektinstanz 'work' und einhaengen in den letzten step
									Work work = new Work();
									step.setWork(work);
									
									// Eintragen der gelesenen Daten in die Objektinstanz
									work.setName(workname);
									work.setLoop(workloop);
									work.setLoopvar(workloopvar);
									work.setDescription(workdescription);
									work.setCommand(workcommand);
	
									// Ermitteln aller Childnodes
									NodeList nodes_l = lw.getChildNodes();
									
									for (int l = 0; l < nodes_l.getLength(); l++)
									{
										Node node_l = nodes_l.item(l);
										if (node_l.getNodeType() == Node.ELEMENT_NODE && ((Element) node_l).getTagName().equals("callitem"))
										{
//											System.out.println("Node: callitem");
											// Einspielen der xml-Informationen des Knotens 'callitem'
											Element lc = (Element) node_l;
											String callitemsequence = lc.getAttribute("sequence");
											String callitemloop = lc.getAttribute("loop");
											String callitempar = lc.getAttribute("par");
											String callitemdel = lc.getAttribute("del");
											String callitemval = lc.getAttribute("val");
	
											// Erstellen der Objektinstanz 'match' und einhaengen in den letzten list
											Callitem callitem = new Callitem();
											work.addCallitem(callitem);
											
											// Eintragen der gelesenen Daten in die Objektinstanz
											callitem.setSequence(Integer.parseInt(callitemsequence));
											callitem.setLoop(callitemloop);
											callitem.setPar(callitempar);
											callitem.setDel(callitemdel);
											callitem.setVal(callitemval);
										}
									}
								}
								if (node_k.getNodeType() == Node.ELEMENT_NODE && ((Element) node_k).getTagName().equals("commit"))
								{
//									System.out.println("Node: commit");
									// Einspielen der xml-Informationen des Knotens 'commit'
									Element lco = (Element) node_k;
									String commitid = lco.getAttribute("id");
									String committoroot = lco.getAttribute("toroot");
									
									// Erstellen der Objektinstanz 'commit' und einhaengen in den letzten step
									Commit commit = new Commit(step);
									step.addCommit(commit);
									
									// Eintragen der gelesenen Daten in die Objektinstanz
									commit.setName(commitid);
									commit.setToroot(committoroot.equals("true"));
								}
	
							}
						}
					}
				}
			}
//			return proc;
		}
		catch (SAXException e)
		{
			System.out.println(e.toString());
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
		catch (ParserConfigurationException e)
		{
			System.out.println(e.toString());
		}
		finally
		{
			return proc;
		}
	}
	
	/*----------------------------
	  method: liest das Objekt inklusiver aller referenzierten Objekte aus einer binaeren Datei (deserialisiert)
	----------------------------*/
	public Process readBinary()
	{
//		Process proc1 = new Process();
		try
		{
			FileInputStream fs = new FileInputStream(this.infilebinary);
			ObjectInputStream is = new ObjectInputStream(fs);
			Process proc = (Process)is.readObject();
			// den parent eintrag aller steps auf das neue objekt erneuern, da die referenz noch die alte ist
			for(Step actualStep : proc.getStep())
			{				
				actualStep.setParent(proc);
			}
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
		catch (ClassNotFoundException e)
		{
			System.err.println(e.toString());
		}
		catch (FileNotFoundException e)
		{
			System.err.println(e.toString());
		}
		catch (IOException e)
		{
			System.err.println(e.toString());
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
		System.out.println("number of steps: "+this.getSteps().size());
		Iterator<Step> iterstep = this.getSteps().iterator();
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
	
	/*----------------------------
	  method: stellt den status des prozesses fest. dieser ist abhaengig vom status aller steps
	  alle steps=finished|cancelled => prozess=finished
	  step=working => prozess=working
  	  step=error => prozess=error
	----------------------------*/
	public void detStatus ()
	{
		String newstatus = "finished";
		Iterator<Step> iterstep = this.step.iterator();
		while(iterstep.hasNext())
		{
			Step step = iterstep.next();
			if ((step.getStatus().matches("waiting")) && (!(newstatus.matches("error|working"))))
			{
				newstatus = "waiting";
			}
			else if ((step.getStatus().matches("initializing|initialized|fanning|fanned|committing|comitted|working|worked")) && (!(newstatus.equals("error"))))
			{
				newstatus = "working";
			}
			else if (step.getStatus().matches("error"))
			{
				newstatus = "error";
			}
		}
		this.setStatus(newstatus);
	}

	/*----------------------------
	  method: entferne step aus prozess
	----------------------------*/
	public void removeStep (Step step)
	{
		this.step.remove(step);
	}
	
	/**
	 * stores a message for the process
	 * @param String loglevel, String logmessage
	 */
	public void log(String loglevel, String logmessage)
	{
		this.log.add(new Log(loglevel, logmessage));
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

	public ArrayList<String> getPaths()
	{
		String[] patharray = this.path.split(":");
		ArrayList<String> paths = new ArrayList<String>(Arrays.asList(patharray));
		return paths;
	}

	public ArrayList<java.io.File> getPaths2()
	{
		ArrayList<java.io.File> paths = new ArrayList<java.io.File>();
		for(String path : this.getPaths())
		{
			java.io.File dir = new java.io.File(new java.io.File(this.getInfilexml()).getParent()+"/"+path);
			
			if (dir.exists())
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
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines Strings "pfad:pfad:pfad" zurueck
	 * @return
	 * einen string, der die absoluten pfade aller 'initcommitdir' enthaelt. trennzeichen ist ':'
	 * relative pfade werden auf absolute pfade umgesetzt, wobei getInfileXml als Basisverzeichnis verwendet wird
	 */
	public String getInitCommitDir()
	{
		return this.initCommitDir;
	}
	
	/**
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines ArrayList<String> zurueck
	 * @return
	 * die absoluten pfadnamen aller 'initcommitdir' 
	 * relative pfade werden auf absolute pfade umgesetzt, wobei getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<String> getInitCommitDirs()
	{
		ArrayList<String> newDirarray = new ArrayList<String>();
		if ((this.initCommitDir != null) && (!(this.initCommitDir.equals(""))))
		{
			String[] dirarray = this.initCommitDir.split(":");
			
			for(String actualInitCommitDir : dirarray)
			{
				String newInitCommitDir = "";
				if (!(actualInitCommitDir.matches("^/")))
				{
					newInitCommitDir = new java.io.File(this.getInfilexml()).getParent() + "/" + actualInitCommitDir;
				}
				else
				{
					newInitCommitDir = actualInitCommitDir;
				}
				newDirarray.add(newInitCommitDir);
			}
			
		}
		return newDirarray;
	}

	/**
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines ArrayList<java.io.File> zurueck
	 * @return
	 * die directories aller 'initcommitdir'
	 * relative pfade werden auf absolute pfade umgesetzt, wobei getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<java.io.File> getInitCommitDirs2()
	{
		ArrayList<java.io.File> initcommitdir = new ArrayList<java.io.File>();
		for(String actualInitCommitDir : this.getInitCommitDirs())
		{
			initcommitdir.add(new java.io.File(actualInitCommitDir));
		}
		
		return initcommitdir;
	}

	public String getInitCommitVarfile()
	{
		return this.initCommitVarfile;
	}

	/**
	 * liefert alle files 'initcommitvarfiles' zurueck in Form
	 * eines ArrayList<String> zurueck
	 * @return
	 * die absoluten pfadnamen aller 'initcommitvarfiles' 
	 * relative pfade werden auf absolute pfade umgesetzt, wobei das directory von getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<String> getInitCommitVarfiles()
	{
		ArrayList<String> newinitcommitvarfiles = new ArrayList<String>();
		if ((this.initCommitVarfile != null) && (!(this.initCommitVarfile.equals(""))))
		{
			String[] filesarray = this.initCommitVarfile.split(":");

			for(String actualInitCommitVarfile : filesarray)
			{
				String newInitCommitVarfile = "";
				if (!(actualInitCommitVarfile.matches("^/")))
				{
					newInitCommitVarfile = new java.io.File(this.getInfilexml()).getParent() + "/" + actualInitCommitVarfile;
				}
				else
				{
					newInitCommitVarfile = actualInitCommitVarfile;
				}
				newinitcommitvarfiles.add(newInitCommitVarfile);
			}
		}
		return newinitcommitvarfiles;
	}

	/**
	 * liefert alle files 'initcommitvarfile' zurueck in Form
	 * eines ArrayList<java.io.File> zurueck
	 * @return
	 * die files aller 'initcommitvarfiles'
	 * relative pfade werden auf absolute pfade umgesetzt, wobei das directory von getInfileXml als Basisverzeichnis verwendet wird
	 */
	public ArrayList<java.io.File> getInitCommitVarfiles2()
	{
		ArrayList<java.io.File> initcommitvarfile = new ArrayList<java.io.File>();
		for(String actualInitCommitVarfile : this.getInitCommitVarfiles())
		{
			initcommitvarfile.add(new java.io.File(actualInitCommitVarfile));
		}
		
		return initcommitvarfile;
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

	public boolean getPradar()
	{
		return this.pradar;
	}
	
	public String getStatus()
	{
		return this.status;
	}

	/**
	 * @return
	 * returns the canonical path of rootdir.
	 * if rootdir is not set so far, a random rootdir will be set like "./<processname>_<processversion>_<randominteger>"
	 */
	public String getRootdir()
	{
		if (this.rootdir.equals(""))
		{
			java.io.File currentdir = new java.io.File (".");
			try
			{
				final Random generator = new Random();
				long time = System.currentTimeMillis();
				generator.setSeed(time);
				int randomnumber = generator.nextInt(9999999);
				
				this.rootdir = (currentdir.getCanonicalPath()+"/"+this.getName()+"_v"+this.getModelVersionPlain()+"_"+randomnumber);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this.rootdir;
	}

	public double getManagerid()
	{
		return this.managerid;
	}

	public Date getDate()
	{
		return this.date;
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
	public ArrayList<Step> getSteps()
	{
		return this.step;
	}

	public ArrayList<Step> getStep()
	{
		return this.step;
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
	 * liefert den step zurueck auf den der namen exakt passt
	 * @param stepname
	 * @return step
	 */
	public Step getStep(String stepname)
	{
		Iterator<Step> iterstep = this.getSteps().iterator();
		while(iterstep.hasNext())
		{
			// den namen abgleichen und merken wenn uebereinstimmung
			Step step = iterstep.next();
			if ( (step.getName().equals(stepname)) )
			{
				return step;
			}
		}
		return null;
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
		
		for(Step actualStep : this.getSteps())
		{
//			System.out.println("looking for "+stepname+" => "+actualStep.getName()+" does not match.");
			if ( (actualStep.getName().equals(stepname)) || (actualStep.getName().matches("^"+stepname+"@.+")) )
			{
				steps.add(actualStep);
			}
		}
		
		return steps;
	}

	public Step[] getSteps2()
	{
		Step[] steps = new Step[this.getSteps().size()];
		for(int i=0; i<steps.length; i++)
		{
			steps[i] = this.step.get(i);
		}
		return steps;
	}

	public String[] getStepnames()
	{
		String[] stepnames = new String[this.getSteps().size()];
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

	public int getRandomId()
	{
		return randomId;
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

	public void setInitCommitDir(String initcommitdir)
	{
		this.initCommitDir = initcommitdir;
	}

	public void setInitCommitVarfile(String initcommitvarfile)
	{
		this.initCommitVarfile = initcommitvarfile;
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

	public void setPradar(boolean pradar)
	{
		this.pradar = pradar;
	}

	public void setStatus(String status)
	{
		this.status = status;
		firePropertyChange("status", this.status, this.status = status);
	}

	public void setRootdir(String rootdir)
	{
		java.io.File dir = new java.io.File(rootdir);
		this.rootdir = dir.getAbsolutePath();
	}

	public void makeRootdir()
	{
		java.io.File dir = new java.io.File(this.getRootdir());
		if (!(dir.exists()))
		{
			dir.mkdirs();
		}
	}

	public void setManagerid(double managerid)
	{
		this.managerid = managerid;
	}

	public void setDatetonow()
	{
		
		this.date = new Date();
	}

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
}
