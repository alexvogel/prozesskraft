package de.caegroup.process;

//import de.caegroup.process.Step;
//import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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
import org.dozer.Mapper;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions.FontStyle;
import org.odftoolkit.simple.style.StyleTypeDefinitions.HorizontalAlignmentType;
import org.odftoolkit.simple.text.Paragraph;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.caegroup.commons.*;

public class Process
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/
	static final long serialVersionUID = 1;

	private String name = new String();
	private String description = new String();
	private String path = new String();
	private String initcommitdir = new String();
	private String initcommitvarfile = new String();
	private String architect = new String();
	private String version = new String();
//	private NamedList<Step> steps = new NamedList<Step>();
	private ArrayList<Step> step = new ArrayList<Step>();
//	private ArrayList<Init> inits = new ArrayList<Init>();
	
	private String status = new String();	// waiting/working/ finished/broken
	private String rootdir = new String();
	private double managerid = -1;
	private Date date = new Date();
	private String infilebinary = new String();
	private String infilexml = new String();
	private String outfilebinary = new String();
	private String outfilexml = new String();
	private String outfiledoc = new String();
	private String filedoctemplate = new String();
	private String rootstepname = "root";
	/*----------------------------
	  constructors
	----------------------------*/
	public Process()
	{
		name = "unnamed";
		description = "without description";
		status = "waiting";
		
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

//	public void removeStep(String stepname)
//	{
//		this.steps.remove(stepname);
//	}

	/*----------------------------
	  method: 	schreibt die aktuelle process-Definition als officefile im odf-Format.
	----------------------------*/
	public void writeDoc()
	{
		try
		{
			java.io.File template = new java.io.File(this.filedoctemplate);
			
			TextDocument document;
			if (template.exists())
			{
				document = TextDocument.loadDocument("/data/prog/workspace/larry/prozessdefinitionen/template_beulen.ott");
			}
			else
			{
				document = TextDocument.newTextDocument();
			}
			
			// erstelle eines headers
//			Header docheader = document.getHeader();
//			Table tabheader = docheader.addTable(1, 2);
//			tabheader.getCellByPosition(0,0).setStringValue("caegroup");
//			tabheader.getCellByPosition(1,0).setStringValue("alexander.vogel@caegroup.de");
			
			// erster Absatz ist ueberschrift
			Paragraph para1 = document.addParagraph("process '"+this.getName()+"'");
//			HorizontalAlignmentType align = para1.getHorizontalAlignment();
			para1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
			Font fontbold = para1.getFont();
			fontbold.setSize(20);
			fontbold.setFontStyle(FontStyle.BOLD);
			para1.setFont(fontbold);
			
			// neuer Absatz
			document.addParagraph("");	// leere Zeile
			Paragraph para2 = document.addParagraph("process-name:");	// ueberschrift2
//			para2.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
			Font fontbold2 = para1.getFont();
			fontbold2.setSize(12);
			fontbold2.setFontStyle(FontStyle.BOLD);
			para2.setFont(fontbold2);
			document.addParagraph(this.getName());	// inhalt
			
			// neuer Absatz
			document.addParagraph("");	// leere Zeile
			para2 = document.addParagraph("process-version:");	// ueberschrift2
			para2.setFont(fontbold2);
			document.addParagraph(this.getVersion());	// inhalt

			// neuer Absatz
			document.addParagraph("");	// leere Zeile
			para2 = document.addParagraph("process-architect:");	// ueberschrift2
			para2.setFont(fontbold2);
			document.addParagraph(this.getArchitect());	// leere Zeile
			
			// neuer Absatz
			document.addParagraph("");	// leere Zeile
			para2 = document.addParagraph("process-decription:");	// ueberschrift2
			para2.setFont(fontbold2);
			document.addParagraph(this.getDescription());	// inhalt

			// neuer Absatz
			document.addParagraph("");	// leere Zeile
			para2 = document.addParagraph("process-topology:");	// ueberschrift2
			para2.setFont(fontbold2);
			
			// die vorhandenen steps ermitteln und fuer jeden step durchfuehren (seite hinzufuegen und beschreibung)
			for (int i=0; i<this.step.size();i++)
			{
				document.addPageBreak();
			
				Step actualStep = step.get(i);
				
				// erster Absatz ist ueberschrift
				Paragraph spara1 = document.addParagraph("step '"+actualStep.getName()+"'");
				spara1.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
				Font sfontbold = spara1.getFont();
				sfontbold.setSize(20);
				sfontbold.setFontStyle(FontStyle.BOLD);
				spara1.setFont(fontbold);
				
				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				Paragraph spara2 = document.addParagraph("step-name:");	// ueberschrift2
//				para2.setHorizontalAlignment(HorizontalAlignmentType.LEFT);
				Font sfontbold2 = para1.getFont();
				sfontbold2.setSize(12);
				sfontbold2.setFontStyle(FontStyle.BOLD);
				spara2.setFont(sfontbold2);
				document.addParagraph(actualStep.getName());	// inhalt
				
				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				spara2 = document.addParagraph("step-type:");	// ueberschrift2
				spara2.setFont(fontbold2);
				document.addParagraph(actualStep.getType());	// leere Zeile
				
				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				spara2 = document.addParagraph("step-description:");	// ueberschrift2
				spara2.setFont(fontbold2);
				document.addParagraph(actualStep.getDescription());	// leere Zeile
				
				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				spara2 = document.addParagraph("step-input:");	// ueberschrift2
				spara2.setFont(fontbold2);

				ArrayList<Init> sinits = actualStep.getInits();
				for (int j=0; j<sinits.size();j++)
				{
					Init init = sinits.get(j);
					document.addParagraph(init.getFromobjecttype()+" '"+init.getName()+"' from step '"+init.getFromstep()+"'");	// ueberschrift2
				}

				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				spara2 = document.addParagraph("step-work:");	// ueberschrift2
				spara2.setFont(fontbold2);

				Work swork = actualStep.getWork();
				document.addParagraph("work '"+swork.getName()+"' will be done with command '"+swork.getCommand()+"'");	// ueberschrift2

				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				spara2 = document.addParagraph("step-output:");	// ueberschrift2
				spara2.setFont(fontbold2);

				ArrayList<Commit> scommits = actualStep.getCommits();
				for (int j=0; j<scommits.size();j++)
				{
					Commit commit = scommits.get(j);
//					document.addParagraph(commit.getType()+" '"+commit.getName()+"' will get stored temporarily (while instance is alive).");
				}

				// neuer Absatz
				document.addParagraph("");	// leere Zeile
				spara2 = document.addParagraph("step-output-to-root:");	// ueberschrift2
				spara2.setFont(fontbold2);

//				ArrayList<Commit> scommits = step.getCommits();
				for (int j=0; j<scommits.size();j++)
				{
					Commit commit = scommits.get(j);
					if (commit.getToroot())
					{
//						document.addParagraph(commit.getType()+" '"+commit.getName()+"' will get stored permanently.");
					}
				}

			}
			
			document.save(this.outfiledoc);

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
		catch (Exception e)
		{
			System.out.println(e.toString());
		}
		
	
	}

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
			atts.addAttribute("", "", "version", "CDATA", this.version);
			atts.addAttribute("", "", "description", "CDATA", this.description);
			atts.addAttribute("", "", "path", "CDATA", this.path);
			atts.addAttribute("", "", "initcommitdir", "CDATA", this.path);
			atts.addAttribute("", "", "initcommitvarfile", "CDATA", this.path);
			atts.addAttribute("", "", "architect", "CDATA", this.architect);
			
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
					atts.addAttribute("", "", "name", "CDATA", inits[j].getName());
					atts.addAttribute("", "", "fromobjecttype", "CDATA", inits[j].getFromobjecttype());
					atts.addAttribute("", "", "returnfield", "CDATA", inits[j].getReturnfield());
					atts.addAttribute("", "", "fromstep", "CDATA", inits[j].getFromstep());
					
					hd.startElement("", "", "init", atts);
				
					// Fuer jeden Knoten 'match'
					Filter[] matchs = inits[j].getMatchs2();
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
		JAXBContext context = JAXBContext.newInstance(de.caegroup.jaxb.process.Process.class);
		Unmarshaller um = context.createUnmarshaller();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try
		{
			URL inst1 = this.getClass().getClassLoader().getResource("");
			URI uri_inst1 = inst1.toURI();
			java.io.File process_schema1 = new java.io.File(uri_inst1.getPath()+"process.xsd");
			System.out.println("version1 "+process_schema1.getAbsolutePath()+" it does exist?: "+process_schema1.exists());

			URL inst2 = this.getClass().getResource("");
			URI uri_inst2 = inst2.toURI();
			java.io.File process_schema2 = new java.io.File(uri_inst2.getPath()+"process.xsd");
			System.out.println("version2 "+process_schema2.getAbsolutePath()+" it does exist?: "+process_schema2.exists());

			java.io.File classpathXSD = new java.io.File(WhereAmI.WhereAmI(this.getClass()).getAbsoluteFile()+"/process.xsd");
			System.out.println("version3: "+classpathXSD.getAbsolutePath()+" it does exist?: "+classpathXSD.exists());
			schema = sf.newSchema((classpathXSD));
			um.setSchema(schema);
		} catch (SAXException e)
		{
			System.err.println("reading schema throws an exception.");
			e.printStackTrace();
		} catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		de.caegroup.jaxb.process.Process xprocess = (de.caegroup.jaxb.process.Process) um.unmarshal(new java.io.File(this.getInfilexml()));

		DozerBeanMapper mapper = new DozerBeanMapper();
		Process destObject = mapper.map(xprocess, de.caegroup.process.Process.class);
		return destObject;
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
					String processarchitect = lp.getAttribute("architect");
	
					// Eintragen der gelesenen Daten in die Objektinstanz
					proc.setName(processname);
					proc.setVersion(processversion);
					proc.setDescription(processdescription);
					proc.setPath(processpath);
					proc.setInitcommitdir(processinitcommitdir);
					proc.setInitcommitvarfile(processinitcommitvarfile);
					proc.setArchitect(processarchitect);
					
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
									init.setName(initname);
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
											Filter match = new Filter();
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
			Iterator<Step> iterstep = proc.getSteps().iterator();
			while(iterstep.hasNext())
			{
				Step step = iterstep.next();
				step.setParent(proc);
			}
			is.close();
//			proc1 = proc;
//			System.out.println("NAMEN des Prozesses proc: "+proc.getName());
//			System.out.println("NAMEN des Prozesses proc1: "+proc1.getName());
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
		System.out.println("        version: "+this.getVersion());
		System.out.println("    description: "+this.getDescription());
		System.out.println("           path: "+this.getPath());
		System.out.println("      architect: "+this.getArchitect());
		System.out.println("number of steps: "+this.getSteps().size());
		Iterator<Step> iterstep = this.getSteps().iterator();
		while(iterstep.hasNext())
		{
			Step step = iterstep.next();
			System.out.println("-----------step: "+step.getName());
			System.out.println("         status: "+step.getStatus());
			System.out.println("    description: "+step.getDescription());
			System.out.println("   amount files: "+step.getFiles().size());
			Iterator<File> iterfile = step.getFiles().iterator();
			while (iterfile.hasNext())
			{
				File file = iterfile.next();
				System.out.println("->    filename: "+file.getFilename());
				System.out.println("   absfilename: "+file.getAbsfilename());
			}
			System.out.println("    amount vars: "+step.getVariables().size());
			Iterator<Variable> itervariable = step.getVariables().iterator();
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
				System.out.println("->     initname: "+init.getName());
				System.out.println("       fromstep: "+init.getFromstep());
				System.out.println("amount of matchs: "+init.getMatchs().size());

				ArrayList<Filter> matchs = init.getMatchs();
				Iterator<Filter> itermatch = matchs.iterator();
				while (itermatch.hasNext())
				{
					Filter match = itermatch.next();
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
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}

	public String getPath()
	{
		return this.path;
	}

	public ArrayList<String> getPaths()
	{
		String[] patharray = this.path.split(":");
		ArrayList<String> paths = new ArrayList<String>(Arrays.asList(patharray));
		return paths;
	}

	public ArrayList<java.io.File> getPaths2()
	{
		ArrayList<java.io.File> paths2 = new ArrayList<java.io.File>();
		ArrayList<String> paths = this.getPaths();
		Iterator<String> iterpath = paths.iterator();
		while (iterpath.hasNext())
		{
			String path = iterpath.next();
			java.io.File dir = new java.io.File(path);
			paths2.add(dir);
		}
		
		return paths2;
	}

	/**
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines Strings "pfad:pfad:pfad" zurueck
	 * @return
	 * einen string, der die absoluten pfade aller 'initcommitdir' enthaelt. trennzeichen ist ':'
	 */
	public String getInitcommitdir()
	{
		return this.initcommitdir;
	}
	
	/**
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines ArrayList<String> zurueck
	 * @return
	 * die absoluten pfadnamen aller 'initcommitdir' 
	 */
	public ArrayList<String> getInitcommitdirs()
	{
		ArrayList<String> initcommitdirs = new ArrayList<String>();
		if (!(this.initcommitdir.equals("")))
		{
			String[] dirharray = this.initcommitdir.split(":");
			initcommitdirs = new ArrayList<String>(Arrays.asList(dirharray));
		}
		return initcommitdirs;
	}

	/**
	 * liefert alle directories 'initcommitdir' zurueck in Form
	 * eines ArrayList<java.io.File> zurueck
	 * @return
	 * die directories aller 'initcommitdir'
	 */
	public ArrayList<java.io.File> getInitcommitdirs2()
	{
		ArrayList<java.io.File> initcommitdirs = new ArrayList<java.io.File>();
		Iterator<String> iterinitcommitdir = this.getInitcommitdirs().iterator();
		while(iterinitcommitdir.hasNext())
		{
			initcommitdirs.add(new java.io.File(iterinitcommitdir.next()));
		}
		return initcommitdirs;
	}

	public String getInitcommitvarfile()
	{
		return this.initcommitvarfile;
	}

	public ArrayList<String> getInitcommitvarfiles()
	{
		ArrayList<String> initcommitvarfiles = new ArrayList<String>();
		if (!(this.initcommitvarfile.isEmpty()))
		{
			String[] filesarray = this.initcommitvarfile.split(":");
			initcommitvarfiles = new ArrayList<String>(Arrays.asList(filesarray));
		}
		return initcommitvarfiles;
	}

	/**
	 * liefert alle files 'initcommitvarfile' zurueck in Form
	 * eines ArrayList<java.io.File>
	 * @return
	 * die files aller 'initcommitvarfile'
	 */
	public ArrayList<java.io.File> getInitcommitvarfiles2()
	{
		ArrayList<java.io.File> initcommitvarfiles = new ArrayList<java.io.File>();
//		System.out.println("HELLO");
		Iterator<String> iterinitcommitvarfile = this.getInitcommitvarfiles().iterator();
		while(iterinitcommitvarfile.hasNext())
		{
			String initcommitvarfile = iterinitcommitvarfile.next();
			java.io.File initcommitvarfile_file = new java.io.File(initcommitvarfile);
//			System.out.println("commitvarfile: "+initcommitvarfile_file.getAbsolutePath());
			initcommitvarfiles.add(initcommitvarfile_file);
		}
		return initcommitvarfiles;
	}

	public String getArchitect()
	{
		return this.architect;
	}

	public String getVersion()
	{
		return this.version;
	}
	
	/**
	 * 
	 * @return versionsnummer ohne punkte
	 */
	public String getVersionplain()
	{
		String versionplain = this.version;
		versionplain.replaceAll("\\.", "");
		return versionplain;
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
		if (this.rootdir.isEmpty())
		{
			java.io.File currentdir = new java.io.File (".");
			try
			{
				final Random generator = new Random();
				long time = System.currentTimeMillis();
				generator.setSeed(time);
				int randomnumber = generator.nextInt(9999999);
				
				this.rootdir = (currentdir.getCanonicalPath()+"/"+this.getName()+"_v"+this.getVersionplain()+"_"+randomnumber);
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
		if (this.outfilebinary.isEmpty())
		{
			this.outfilebinary = this.getRootdir()+"/"+this.getName()+".lri";
		}
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

	public String getOutfiledoc()
	{
		return this.outfiledoc;
	}

	public String getFiledoctemplate()
	{
		return this.filedoctemplate;
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

	// liefert nur den step zurueck dessen namen exakt passt
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

	// liefert nur den step zurueck dessen namen exakt passt
	public boolean isStep(String stepname)
	{
		boolean vorhanden = false;
		Step step = this.getStep(stepname);
		
		if (!(step.equals(null)))
		{
			vorhanden = true;
		}
		
		return vorhanden;
	}

	
	// liefert die steps zurueck auf die der namen passt (inkl. aufgefaecherte steps mit der namenserweiterung @...)
	public ArrayList<Step> getSteps(String stepname)
	{
		ArrayList<Step> steps = new ArrayList<Step>();
		
		Iterator<Step> iterstep = this.getSteps().iterator();
		while(iterstep.hasNext())
		{
			// den namen abgleichen und merken wenn uebereinstimmung
			Step step = iterstep.next();
			if ( (step.getName().equals(stepname)) || (step.getName().matches("^"+stepname+"@.+")) )
			{
				steps.add(step);
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

	/*----------------------------
	  methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setStep(ArrayList<Step> step)
	{
		this.step = step;
		Iterator<Step> iterStep = this.step.iterator();
		while(iterStep.hasNext())
		{
			iterStep.next().setParent(this);
		}
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public void setInitcommitdir(String initcommitdir)
	{
		this.initcommitdir = initcommitdir;
	}

	public void setInitcommitvarfile(String initcommitvarfile)
	{
		this.initcommitvarfile = initcommitvarfile;
	}

	public void setArchitect(String architect)
	{
		this.architect = architect;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public void setStatus(String status)
	{
		this.status = status;
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

	public void setOutfiledoc(String file)
	{
		this.outfiledoc = file;
	}

	public void setFiledoctemplate(String file)
	{
		this.filedoctemplate = file;
	}

	public void setRootstepname(String rootstepname)
	{
		this.rootstepname = rootstepname;
	}
}
