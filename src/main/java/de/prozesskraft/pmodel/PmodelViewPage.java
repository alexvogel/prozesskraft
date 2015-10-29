package de.prozesskraft.pmodel;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import processing.core.*;
import de.prozesskraft.pkraft.Step;

public class PmodelViewPage extends PApplet
{
	/*----------------------------
	  structure
	----------------------------*/
	public PmodelViewModel einstellungen;
	int textSize_gemerkt = 15;
	Calendar now = Calendar.getInstance();
	long startTimeMillis = System.currentTimeMillis();
	Calendar mousepressedlasttime = Calendar.getInstance();
//	int refresh_interval = 300;
	int doubleclickperiod = 300;	// seconds
//	boolean refresh_topology = false;
	Random generator = new Random();
	private ArrayList<PmodelViewStepSym> stepcircles = new ArrayList<PmodelViewStepSym>();
	private ArrayList<PmodelViewStepCon> stepconnectors = new ArrayList<PmodelViewStepCon>();
	public PmodelViewStepSym stepcircle_clicked = null;
	public PmodelViewStepSym stepcircle_marked = null;
//	public Process p;
	public String rootstepname = "root";
	public boolean rootstepfull = false;
	private float[] legendposition = {0,0,0};
	private int[] legendcolor = {0,0,0};
    private int legendsize = (10);
    private String legend_processname = new String();
    private String legend_processlastrevision = new String();
    private String legend_processstatus = new String();
    private int mybackground = 255;

	public int framerate = 50;
	float bezugsgroesse = (float) 1.0;
	boolean saveActualPic = false;
	String pathActualPic = "";
//	public float damp = (float)0.2;
	
	// um den hintergrund draagen zu koennen
	int deltax = 0;
	int deltay = 0;
	int mouse_pressed_x;
	int mouse_pressed_y;

	Float dampOverride = null;
    
	// die stepcircles, die startingpoints repraesentieren, werden nach einer kurzen wartezeit 'gepinned', d.h. ihre position nicht mehr neu bestimmt
	public int timetopin = 2;
	
//    private PApplet parent;
	//	PApplet.main(new String[] {"de.caegroup.visuinstance.Page"});

	// manipulationen von UI1 von hier aus fuehren immer zu einer Exception
	// dies muss mit einem thread o.ae. geloest werden
//	private PmodelPartUi1 father = null;
	
	public PmodelViewPage()
	{
		this.einstellungen = new PmodelViewModel();
	}
	
	public PmodelViewPage(PmodelViewModel einstellungen)
	{
		this.einstellungen = einstellungen;
	}

	public PmodelViewPage(String pathToProcessFile, PmodelViewModel einstellungen)
	{
		this.einstellungen = einstellungen;
	}
    
	/*----------------------------
	  method setup Processing
	----------------------------*/
	public void setup()
    {
		
//    	this.dataPath("/data/prog/workspace/larry");
//    	PFont font = this.loadFont("Univers45.vlw");
//    	PFont font = this.loadFont("AndaleMono-36.vlw");
     	PFont font = this.loadFont("TheSans-Plain-12.vlw");
    	textFont(font, 12);
//    	Image icon = Toolkit.getDefaultToolkit().getImage(PmodelViewPage.class.getClassLoader().getResource("process.png"));
		textSize(15);
		size(this.einstellungen.getWidth(), this.einstellungen.getHeight(), JAVA2D);
//		size(this.einstellungen.getWidth(), this.einstellungen.getHeight(), OPENGL);
//		size(this.einstellungen.getWidth(), this.einstellungen.getHeight(), P2D);
//  	size(this.width, this.height, OPENGL);
//    	size(this.width, this.height, P2D);
    	background(this.mybackground);
//    	frame.setTitle("unnamed");
//    	frame.setIconImage(icon);
		
    	frameRate(50);
//    	this.frame.setResizable(true);
//    	refresh_last.setTimeInMillis(0);
    	smooth();
//   	noLoop();
		addMouseWheelListener(listener_mousewheel);

		// wenn ein multistep auf mehr als 20 Stueck aufgefaechert wurde, soll das in einem feld markiert werden -> dies ermoeglicht eine erhoehte daempfung einzustellen
		ArrayList<Step> allSteps = this.einstellungen.getProcess().getStep();
		Map<String,Integer> stepBasename_count = new HashMap<String,Integer>();
		for(Step actStep : allSteps)
		{
			Pattern p = Pattern.compile("^([^@]+)(@\\d+)?$");
			Matcher m = p.matcher(actStep.getName());

			if(m.matches())
			{
//				System.err.println("found a step: "+m.group(1));
				if(stepBasename_count.containsKey(m.group(1)))
				{
					stepBasename_count.put(m.group(1), stepBasename_count.get(m.group(1)) + 1);
				}
				else
				{
					stepBasename_count.put(m.group(1), 1);
				}
			}
		}
		//
		for(String actStepName : stepBasename_count.keySet())
		{
//			System.err.println("anzahl des steps "+actStepName+": " + stepBasename_count.get(actStepName));
			if(stepBasename_count.get(actStepName) > 20)
			{
				System.err.println("because of the amount of a fanned out multistep the damping override will be set to: " + 0.97);
				dampOverride = 0.97f;
			}
		}
		
    }
    
	/*----------------------------
	  method draw Processing
	----------------------------*/
	public void draw()
	{

		if(einstellungen.getSleep())
		{
			frameRate(5);
			return;
		}
		else
		{
			frameRate(50);
		}
		
		// zoom from the center of the scetch
//		translate(width/2, height/2); // use translate around scale
//		scale((float)this.einstellungen.getZoom()/100);
//		translate(-width/2, -height/2);

		//		makeTimeStamp("1");

		background(255);
		this.bezugsgroesse = (float)((float)this.einstellungen.getSize() / 100);
		textSize(this.einstellungen.getTextsize());
//		System.out.println("bezugsgroesse         : "+this.bezugsgroesse);
		
		this.now = Calendar.getInstance();

//		// wieviel sekunden muesseen vom refresh-Countdown abgezogen werden?
//		long sekundenSeitLetztemRefreshCheck = ((System.currentTimeMillis()/1000) - this.einstellungen.getLastRefreshCheckSeconds());
//		
////		makeTimeStamp("2");
//		// refresh Countdown aktualisieren
//		if ( sekundenSeitLetztemRefreshCheck > 0)
//		{
//			this.einstellungen.setNextRefreshSeconds((int)(this.einstellungen.getNextRefreshSeconds()-sekundenSeitLetztemRefreshCheck));
//			this.einstellungen.setLastRefreshCheckSeconds((int)(System.currentTimeMillis()/1000));
////			System.out.println("refresh in "+this.einstellungen.getNextRefreshSeconds());
//		}
//		
////		makeTimeStamp("3");
//		// wenn refresh-Countdown < 0 ist, soll refresht werden
//		if (this.einstellungen.getNextRefreshSeconds() < 0)
//		{
//			refresh();
//		}
			
//		makeTimeStamp("4");
//		if (this.mousePressed) {mouse_pressed_action();}
	
//		makeTimeStamp("5");
		try
		{
			this.display();
		}
		catch (ConcurrentModificationException e)
		{
			System.err.println("data has been changed while drawing. drawing skipped.");
			
		}
		
		// wenn das bild fixiert werden soll, dann soll die geschwindigkeit aller stepsymbols auf 000 gesetzt werden
		if(this.einstellungen.getFix())
		{
			this.startTimeMillis = System.currentTimeMillis();
//			setStepsymbolSpeed(0f, 0f, 0f);
		}
		
		// wenn alles verschoben werden soll, alle stepsymbole translaten
		if((deltax != 0) || (deltay != 0))
		{
			translateStepsymbols(deltax, deltay);
			deltax = 0;
			deltay = 0;
		}
		
//		makeTimeStamp("6");
		
		
//		}
	
		if (this.saveActualPic)
		{
//			System.out.println("saving actual drawing to: "+this.pathActualPic);
			this.save(this.pathActualPic);
			this.saveActualPic = false;
		}

	}

	/*----------------------------
	  methods
	----------------------------*/
	
	private void draw_stepcircles()
	{
		// alle steps durchgehen und nur stepcircles erzeugen, wenn es noch keinen mit dem gleichen Namen gibt wie einen Step
		try
		{
//			ArrayList<Step> copyOfSteps =  this.einstellungen.getProcess().getStep();
			for(Step actualStep : this.einstellungen.getProcess().getStep())
			{
//				System.err.println("drawing stepcircle: "+actualStep.getName());
				// wenn es noch keinen stepcircle mit diesem namen gibt, dann einen erzeugen und hinzufuegen zur page
				if (!(this.hasStepcircle(actualStep.getName())))
				{
	//				System.out.println("Es muss ein stepcircle erzeugt werden fuer step "+actualStep.getName());
	//				makeTimeStamp("1");
					this.addStepcircle(new PmodelViewStepSym(this, actualStep));
	//				makeTimeStamp("2");
				}
				else
				{
					// den stepcircle feststellen, der diesen step bereits repraesentiert
					// das step-object darin ablegen (es koennte sein, dass es sich geaendert hat)
					PmodelViewStepSym oldstepcircle = this.getStepcircle(actualStep.getName());
					// und den step setzen (damit die daten aktualisiert werden)
					oldstepcircle.setStep(actualStep);
					oldstepcircle.setNochvorhanden(true);
				}
			}
		}
		// falls während des durchgehens der steps, die liste veraendert wird (hinzufuegen oder loeschen von steps), soll die sache nicht gleich abstuerzen
		catch(Exception e)
		{
			e.printStackTrace();;
		}
	}
	
	private void draw_stepconnectors()
	{
		// erstellen eines Stepconnectors fuer jeden vorkommenden fromstep-eintrag
		for(PmodelViewStepSym actualStepcircle : this.getStepcircles2())
		{
			for(String actualFromStepcircleName : actualStepcircle.getConnectfroms())
			{
				for(PmodelViewStepSym actualFromStepcircle : this.getStepcircles(actualFromStepcircleName))
				{
					// wenn es noch keinen stepconnector mit diesem Namen gibt, dann hinzufuegen zur Page
					if (!(this.hasStepconnector(actualFromStepcircle.getName()+actualStepcircle.getName())))
					{
						this.addStepconnector(new PmodelViewStepCon(this, actualFromStepcircle, actualStepcircle));
					}
					// wenn es ihn schon auf der page gibt, dann markieren um ihn zu behalten
					else
					{
						this.getStepconnector(actualFromStepcircle.getName()+actualStepcircle.getName()).setNochvorhanden(true);
					}
				}
			}
		}
	}
	
	private boolean hasStepconnector(String name)
	{
		Iterator<PmodelViewStepCon> iterstepconnector = this.getStepconnectors().iterator();
		while ( iterstepconnector.hasNext() )
		{
			if (iterstepconnector.next().getName().equals(name))
			{
				return true;
			}
		}
		return false;
	}

	private void markAllstepconnectorstodelete()
	{
		Iterator<PmodelViewStepCon> iterstepconnector = this.getStepconnectors().iterator();
		while ( iterstepconnector.hasNext() )
		{
			iterstepconnector.next().setNochvorhanden(false);
		}
	}

	/**
	 * es wird ueberprueft ob fuer jeden stepConnector beide steps im prozess existieren.
	 * falls einer der beiden steps nicht mehr existiert, wird der stepconnector geloescht.
	 */
	private void expungeStepconnectors()
	{
		try
		{
			ArrayList<PmodelViewStepCon> cleanedStepconnectors = new ArrayList<PmodelViewStepCon>();
			for(PmodelViewStepCon actualStepconnector : this.getStepconnectors())
			{
				if ( (this.einstellungen.getProcess().getStep(actualStepconnector.getStepcirclefrom().getName()) != null) && (this.einstellungen.getProcess().getStep(actualStepconnector.getStepcircleto().getName()) != null) )
				{
					cleanedStepconnectors.add(actualStepconnector);
				}
			}
			this.stepconnectors = cleanedStepconnectors;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * es wird ueberprueft ob fuer jeden stepcircle auch ein step im prozess existiert.
	 * falls der step nicht mehr existiert, wird der stepcircle geloescht.
	 */
	private void expungeStepcircles()
	{
		try
		{
			ArrayList<PmodelViewStepSym> cleanedStepcircles = new ArrayList<PmodelViewStepSym>();
			for(PmodelViewStepSym actualStepcircle : this.getStepcircles())
			{
				if ( this.einstellungen.getProcess().getStep(actualStepcircle.getName()) != null )
				{
					cleanedStepcircles.add(actualStepcircle);
				}
				else
				{
					// sind der markierte stepcircle in den cleanedStepcircles nicht mehr vorhanden, soll die markierung auf null gesetzt werden
					if(this.stepcircle_marked.equals(actualStepcircle))
					{
						this.stepcircle_marked = null;
					}
				}
			}
			

			
			this.stepcircles = cleanedStepcircles;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void markAllstepcirclestodelete()
	{
		Iterator<PmodelViewStepSym> iterstepcircle = this.getStepcircles().iterator();
		while ( iterstepcircle.hasNext() )
		{
			iterstepcircle.next().setNochvorhanden(false);
		}
	}

	public void mousePressed()
	{
		mouse_pressed_x = mouseX;
		mouse_pressed_y = mouseY;
//		System.out.println("mouseX: "+mouseX);

		for(int l=0; l<this.stepcircles.size(); l++)
		{
			if (PApplet.dist(mouseX, mouseY, stepcircles.get(l).getDrawPosition1(), stepcircles.get(l).getDrawPosition2()) < (stepcircles.get(l).getRadius() * this.einstellungen.getZoom()/100) * 0.9)
			{
//				if (this.stepcircle_clicked != null && this.stepcircle_clicked.equals(stepcircles.get(l))) {break;}
				this.stepcircle_clicked = stepcircles.get(l);
				this.stepcircle_marked = stepcircles.get(l);
				
				this.einstellungen.setMarkedStepName(this.stepcircle_marked.getStep().getName());
				
				// wenn es ein doppelklick ist
				if ( ( Calendar.getInstance().getTimeInMillis() - this.mousepressedlasttime.getTimeInMillis() )  < this.doubleclickperiod )
				{
					
					
					// konnte nicht realisiert werden, weil es immer einen "Exception in thread "Animation Thread" org.eclipse.swt.SWTException: Invalid thread access" gibt, wenn ich versuche
					// den father zu manipulieren
//					// wenn es ein step ist, der einen subprocess beherrbergt, soll der subprocess in einer eigenen pmodel sitzung geoeffnet werden
//					if( this.stepcircle_clicked.getStep().getType().equals("process") && this.stepcircle_clicked.getStep().getSubprocess() != null && this.stepcircle_clicked.getStep().getSubprocess().getProcess() != null)
//					{
//						Process subprocessProcess = this.stepcircle_clicked.getStep().getSubprocess().getProcess();
//						
////						java.io.File processBinaryFile = new java.io.File(step.getAbsdir() + "/process.pmb");
//						java.io.File processBinaryFile = new java.io.File(subprocessProcess.getOutfilebinary());
//						
//						if(processBinaryFile.exists())
//						{
//							// Aufruf taetigen
//							try
//							{
//								String aufruf = father.getIni().get("apps", "pmodel-gui")+" -instance "+processBinaryFile.getCanonicalPath();
//								father.log("info", "opening subprocess of step "+this.stepcircle_clicked.getName()+" with call: "+aufruf);
//								java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
//							}
//							catch (IOException e)
//							{
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//						else
//						{
//							father.log("warn", "no outfileBinary present for subprocess of step "+this.stepcircle_clicked.getName());
//							father.log("debug", "this binary has not been found: "+processBinaryFile.getAbsolutePath());
//						}
//					}
//					// ansonsten nedit mit den logfiles oeffnen
//					else
//					{
//	//					System.out.println("timeperiod "+(Calendar.getInstance().getTimeInMillis() - this.mousepressedlasttime.getTimeInMillis()));
//						// feststellen des stdout/stderr des steps
//						String stdout = this.stepcircle_clicked.step.getAbsstdout();
//						String stderr = this.stepcircle_clicked.step.getAbsstderr();
//						
//						java.io.File stdoutfile = new java.io.File(stdout);
//						java.io.File stderrfile = new java.io.File(stderr);
//						
//						String neditstring = new String();
//						
//						if (stdoutfile.exists())
//						{
//							neditstring = neditstring+" "+stdout;
//						}
//						
//						if (stderrfile.exists())
//						{
//							neditstring = neditstring+" "+stderr;
//						}
//						
//						if (!(neditstring.isEmpty()))
//						// Aufruf taetigen
//						try {
//							java.lang.Process sysproc = Runtime.getRuntime().exec("nedit "+neditstring);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
				}
//					break;
			}
			else
			{
			}
		}
		this.mousepressedlasttime = Calendar.getInstance();
	}

	public void mouseDragged()
	{
		// stepcircle umherziehen
		if (this.stepcircle_clicked != null)
		{
			this.stepcircle_clicked.setPosition((mouseX - this.width/2) * (float)1/((float)this.einstellungen.getZoom()/100) + this.width/2, (mouseY - this.height/2) * (float)1/((float)this.einstellungen.getZoom()/100) + this.height/2, 0);
		}
		
		// hintergrund umherziehen
		else
		{
			int newDeltax = mouseX-mouse_pressed_x;
			int newDeltay = mouseY-mouse_pressed_y;
			
			mouse_pressed_x = mouseX;
			mouse_pressed_y = mouseY;
			
			deltax += newDeltax * (float)1/((float)this.einstellungen.getZoom()/100);
			deltay += newDeltay * (float)1/((float)this.einstellungen.getZoom()/100);
			
			// wenn die flaeche gedraggt wurde, soll ab diesem moment der rootstep nicht mehr repositioniert werden.
			this.einstellungen.setRootReposition(false);
			
//			System.out.println("deltax="+deltax+": deltay="+deltay);
		}
	}
	
	public void mouseReleased()
	{
//		this.stepcircle_clicked = new PmodelViewStepSym();
		this.stepcircle_clicked = null;
	}
	
	public void addStepcircle(PmodelViewStepSym stepcircle)
	{
		this.stepcircles.add(stepcircle);
	}
	
	public void addStepconnector(PmodelViewStepCon stepconnector)
	{
		this.stepconnectors.add(stepconnector);
	}
	
	public boolean isStepconnectorpresent_by_name(String stepconnectorname)
	{
		boolean returnvalue = false;
		PmodelViewStepCon[] stepconnectors = this.getStepconnectors2();
		for(int i=0; i<stepconnectors.length; i++)
		{
			if(stepconnectors[i].getName().equals(stepconnectorname))
			{
				returnvalue = true;
			}
		}
		return returnvalue;
	}
	
	public void mouseWheel(int delta)
	{
//		System.out.println("mouse has moved by "+delta+" units");
		int newzoomfaktor = this.einstellungen.getZoom() - (delta * 20);
		newzoomfaktor = Math.min(200, newzoomfaktor);
		newzoomfaktor = Math.max(10, newzoomfaktor);
		
		// neuen zoomfaktor setzen
		this.einstellungen.setZoom(newzoomfaktor);
	}
	
	java.awt.event.MouseWheelListener listener_mousewheel = new MouseWheelListener()
	{
		public void mouseWheelMoved(MouseWheelEvent me)
		{
			mouseWheel(me.getWheelRotation());
		}
	};

	public void display()
	{
//		makeTimeStamp("1");
		// legende ausgeben
		this.fill(this.legendcolor[0], this.legendcolor[1], this.legendcolor[2]);
		this.text(this.legend_processname, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+2*this.legendsize+0);
		this.text(this.legend_processlastrevision, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+4*this.legendsize+0);
		this.text(this.legend_processstatus, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+6*this.legendsize+0);
//		this.text((int)(((this.refresh_next.getTimeInMillis() - this.now.getTimeInMillis())/1000)+1), this.legendsize/2, this.getHeight() -40);
		this.text(((int)(this.frameRate)), this.getWidth() - this.legendsize/2 - this.legendsize*3, this.getHeight() - 40);

//		makeTimeStamp("51");
		// Stepconnectoren ausgeben
		Iterator<PmodelViewStepCon> iterconn = this.stepconnectors.iterator();
		while(iterconn.hasNext())
		{
			PmodelViewStepCon stepconnector = iterconn.next();
			stepconnector.display();
//			makeTimeStamp("511");
		}
//		makeTimeStamp("52");
		// Stepcircles ausgeben
		Iterator<PmodelViewStepSym> itercirc = this.stepcircles.iterator();
		while(itercirc.hasNext())
		{
			PmodelViewStepSym stepcircle = itercirc.next();
			stepcircle.display();
//			makeTimeStamp("521");
		}
		
//		System.out.println("Anzahl der Stepcircles    : "+this.stepcircles.size());
//		System.out.println("Anzahl der Stepconnectoren: "+this.stepconnectors.size());
		
	}
	
	private void setStepsymbolSpeed(float speedX, float speedY, float speedZ)
	{
		for(PmodelViewStepSym actualStepsymbol : this.stepcircles)
		{
			actualStepsymbol.setSpeed(speedX, speedY, speedZ);
		}
	}
	
	private void translateStepsymbols(int deltax, int deltay)
	{
		for(PmodelViewStepSym actualStepsymbol : this.stepcircles)
		{
			actualStepsymbol.translate(deltax, deltay);
		}
	}
	
	private void makeTimeStamp(String string)
	{
		System.err.println(string+": "+new Timestamp(System.currentTimeMillis()));
		// TODO Auto-generated method stub
		
	}

	public void refresh()
	{
//		System.out.println("Es wird refresht");
//		System.out.println("refreshInterval: "+this.einstellungen.getRefreshInterval());
//		System.out.println("lastRefreshSeconds: "+this.einstellungen.getLastRefreshSeconds());
//		System.out.println("nextRefreshSeconds: "+this.einstellungen.getNextRefreshSeconds());

//		try
//		{
//			Thread.sleep(1000);
//		} catch (InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		this.markAllstepcirclestodelete();
//		this.markAllstepconnectorstodelete();
//		this.einstellungen.getProcess().setStepRanks();
		this.expungeStepcircles();
		this.expungeStepconnectors();
		draw_stepcircles();
		draw_stepconnectors();
		
    	this.einstellungen.setLastRefreshSeconds((int)(System.currentTimeMillis()/1000));
    	this.einstellungen.setNextRefreshSeconds(this.einstellungen.getRefreshInterval());
//		System.out.println("lastRefreshSeconds: "+this.einstellungen.getLastRefreshSeconds());
//		System.out.println("nextRefreshSeconds: "+this.einstellungen.getNextRefreshSeconds());
    	
 	}
	
	private void effect_noise()
	{
		for(int x=0; x < this.width; x+=5)
		{
			for(int y = 0; y < this.height; y +=5)
			{
				noStroke();
				fill(random(255));
				rect(x,y,5,5);
			}
		}
	}
	private void effect_fade()
	{
		background(255);
	}

	/*----------------------------
	  methods get
	----------------------------*/
	public ArrayList<PmodelViewStepSym> getStepcircles()
	{
		return this.stepcircles;
	}

	public boolean hasStepcircle(String stepcirclename)
	{
		boolean exist = false;
		PmodelViewStepSym[] stepcircles = this.getStepcircles2();
		for(int i=0; i<stepcircles.length; i++)
		{
			if(stepcircles[i].getName().equals(stepcirclename))
			{
				exist = true;
			}
		}
		return exist;
	}

	public PmodelViewStepSym getStepcircle(String stepcirclename)
	{
		PmodelViewStepSym stepcircle = new PmodelViewStepSym();
		PmodelViewStepSym[] stepcircles = this.getStepcircles2();
		for(int i=0; i<stepcircles.length; i++)
		{
			if(stepcircles[i].getName().equals(stepcirclename))
			{
				stepcircle = stepcircles[i];
				return stepcircle;
			}
		}
		System.err.println("Error in method getStepcircle(String stepcirclename). please check.");
		System.err.println("Cannot find a stepcircle with name '"+stepcirclename+"'. returning an empty one :-P");
		
		return stepcircle;
	}
	
	// gibt die stepcircles zurueck, die dem namen entsprechen (auch die aufgefaecherten)
	public ArrayList<PmodelViewStepSym> getStepcircles(String stepcirclename)
	{
		ArrayList<PmodelViewStepSym> stepcircles = new ArrayList<PmodelViewStepSym>();
		
		Iterator<PmodelViewStepSym> iterstepcircle = this.getStepcircles().iterator();
		while(iterstepcircle.hasNext())
		{
			// den namen abgleichen und merken wenn uebereinstimmung
			PmodelViewStepSym stepcircle = iterstepcircle.next();
			if ( (stepcircle.getName().equals(stepcirclename)) || (stepcircle.getName().matches("^"+stepcirclename+"@")) )
			{
				stepcircles.add(stepcircle);
			}
		}
		return stepcircles;
	}

	public PmodelViewStepSym[] getStepcircles2()
	{
		PmodelViewStepSym[] stepcircles = new PmodelViewStepSym[this.getStepcircles().size()];
		for(int i=0; i<stepcircles.length; i++)
		{
			stepcircles[i] = this.stepcircles.get(i);
		}
		return stepcircles;
	}

	public PmodelViewStepCon[] getStepconnectors2()
	{
		PmodelViewStepCon[] stepconnectors = new PmodelViewStepCon[this.getStepconnectors().size()];
		for(int i=0; i<stepconnectors.length; i++)
		{
			stepconnectors[i] = this.stepconnectors.get(i);
		}
		return stepconnectors;
	}

	public PmodelViewStepCon getStepconnector(String stepconnectorname)
	{
		Iterator<PmodelViewStepCon> iterstepconnector = this.getStepconnectors().iterator();
		while ( iterstepconnector.hasNext() )
		{
			PmodelViewStepCon stepconnector = iterstepconnector.next();
			if (stepconnector.getName().equals(stepconnectorname))
			{
				return stepconnector;
			}
		}
		System.err.println("Error in method getStepcircle(String stepcirclename). please check.");
		System.err.println("Cannot find a stepcircle with name '"+stepconnectorname+"'. returning null :-P");
		
		return null;
	}
	
	public ArrayList<PmodelViewStepCon> getStepconnectors()
	{
		return this.stepconnectors;
	}

	public float getDamp()
	{
		if(this.dampOverride != null)
		{
			return this.dampOverride;
		}
		
		float damp;
		
		// die ersten sekunden soll die daempfung immer sehr hoch sein
		long millisSeitStart = (now.getTimeInMillis() - startTimeMillis);
		
		// das maximum (abwaertsrampe die ersten 30 sekunden oder 5/frameRate)
		damp = Math.max((float)(1.0f - (millisSeitStart / 30000f)), (float)((-0.067 * this.frameRate) + 1) );
		
		// sind es weniger als 25 steps? (unterer wert: kleine modelle => 0.15, ab 25 steps aus einem multistep => 0.5, ab 50 steps aus einem multistep => 0,98 
		
		damp = Math.max(damp, 0.5f);
		damp = Math.min(damp, 1.0f);

//		System.out.println("damp: "+damp);
		return damp;
	}
	
	public void savePic(String pathToPic)
	{
		this.pathActualPic = pathToPic;
		this.saveActualPic = true;
	}
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setRootstepfull(boolean rootstepfull)
	{
		this.rootstepfull = rootstepfull;
	}
	
	public void setTextsize(int textsize)
	{
		this.einstellungen.setTextsize(textsize);
	}
	
	public void setSize(int size)
	{
		this.einstellungen.setSize(size);
	}
	
	public void setRootpositionratiox(float rootpositionratiox)
	{
		this.einstellungen.setRootpositionratiox(rootpositionratiox);
	}
}
