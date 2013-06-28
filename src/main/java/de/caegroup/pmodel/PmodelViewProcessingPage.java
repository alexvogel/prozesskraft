package de.caegroup.pmodel;

import java.util.*;
import java.awt.Toolkit;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.xerces.impl.xpath.regex.ParseException;
import processing.core.*;
import de.caegroup.process.Process;
import de.caegroup.process.Step;


public class PmodelViewProcessingPage extends PApplet
{
	/*----------------------------
	  structure
	----------------------------*/
	public Process process;
	public PmodelViewModel einstellungen;
	
	int textSize_gemerkt = 15;
	int zoomfaktor = 100;
	int zoomfaktor_min = 10;
	int zoomfaktor_max = 200;
	Calendar refresh_last = Calendar.getInstance();
	Calendar refresh_next = Calendar.getInstance();
	Calendar now = Calendar.getInstance();
	Calendar mousepressedlasttime = Calendar.getInstance();
	int refresh_interval = 300;
	int doubleclickperiod = 300;	// seconds
	boolean refresh_topology = false;
	Random generator = new Random();
	private ArrayList<PmodelViewProcessingStepcircle> stepcircles = new ArrayList<PmodelViewProcessingStepcircle>();
	private ArrayList<PmodelViewProcessingStepconnector> stepconnectors = new ArrayList<PmodelViewProcessingStepconnector>();
	public PmodelViewProcessingStepcircle stepcircle_clicked = new PmodelViewProcessingStepcircle();
	public PmodelViewProcessingStepcircle stepcircle_marked = null;
	public Process p;
	public String rootstepname = "root";
	public boolean rootstepfull = false;
	private float[] legendposition = {0,0,0};
	private int[] legendcolor = {0,0,0};
    private int legendsize = (10);
    private String legend_processname = new String();
    private String legend_processlastrevision = new String();
    private String legend_visualrefresh = new String();
    private String legend_processstatus = new String();
    private String legend_framerate = new String();
    private int mybackground = 255;
    private Map<String,String> statuscolors = new HashMap<String,String>();
//	private boolean initial_resized = false;	// ohne erstmaligen resize stellt der renderer 'JAVA2D' den fensterinhalt nicht korrekt dar.
	public int width = 1200;
	public int height = 800;
	public int framerate = 50;
	float bezugsgroesse = (float) 1.0;
//	public float damp = (float)0.2;


    
//    private int width = 600;
//    private int height = 400;
//	String textfont = new String("Univers.vlw");

// die stepcircles, die startingpoints repraesentieren, werden nach einer kurzen wartezeit 'gepinned', d.h. ihre position nicht mehr neu bestimmt
	public int timetopin = 2;
	
//    private PApplet parent;
	//	PApplet.main(new String[] {"de.caegroup.visuinstance.Page"});

	public PmodelViewProcessingPage()
	{
		this.einstellungen = new PmodelViewModel();
		this.process = new Process();
		this.p =  this.process;
	}
	
	public PmodelViewProcessingPage(PmodelViewModel einstellungen)
	{
		this.einstellungen = einstellungen;
		this.process = new Process();
		this.p =  this.process;
	}
	
	public PmodelViewProcessingPage(String pathToProcessFile, PmodelViewModel einstellungen)
	{
		this.einstellungen = einstellungen;
		this.process = new Process();
		this.process.setInfilexml(pathToProcessFile);
		this.p =  this.process;
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
//    	Image icon = Toolkit.getDefaultToolkit().getImage("/data/prog/workspace/larry/icon/process.png");
    	Image icon = Toolkit.getDefaultToolkit().getImage(PmodelViewProcessingPage.class.getClassLoader().getResource("process.png"));
		textSize(15);
		size(this.width, this.height, JAVA2D);
//  	size(this.width, this.height, OPENGL);
//    	size(this.width, this.height, P2D);
    	background(this.mybackground);
//    	frame.setTitle("unnamed");
//    	frame.setIconImage(icon);
//		frame.setResizable(true);
		
    	frameRate(50);
    	refresh_last.setTimeInMillis(0);
    	smooth();
//   	noLoop();
    }
    
	/*----------------------------
	  method draw Processing
	----------------------------*/
	public void draw()
	{
//    		System.out.println(this.millis());
//		System.out.println("width: "+frame.getWidth());

		background(255);
		this.bezugsgroesse = (float)((float)this.zoomfaktor / 100);
//		System.out.println("bezugsgroesse         : "+this.bezugsgroesse);
		
		this.now = Calendar.getInstance();
//		System.out.println("now         : "+this.now.toString());
//		System.out.println("next refresh: "+this.refresh_next.toString());
//		System.out.println("last refresh: "+this.refresh_last.toString());
//		System.out.println("difference between now and last in millis: "+(this.now.getTimeInMillis() - this.refresh_last.getTimeInMillis()));
//		wenn refresh-interval verstrichen ist seit letztem refresh oder wenn space (jedoch nicht vor mindestens 1 Sekunde) gedrueckt wurde
		if ((now.after(this.refresh_next)) || ((this.keyPressed) && (this.key == ' ') && ((this.now.getTimeInMillis() - this.refresh_last.getTimeInMillis()) > 1000)))
		{
			this.refresh();
			this.refresh_last = this.now;
			this.refresh_next = Calendar.getInstance();
			this.refresh_next.add(13, this.refresh_interval);
		}
		
		// wenn der letzte refresh in der letzten 0,5 sekunden passiert ist, dann nur noise darstellen
		else if ((this.now.getTimeInMillis() - this.refresh_last.getTimeInMillis()) < 100)
		{
//			this.effect_noise();
			this.effect_fade();
		}
		
		else
		{
//			System.out.println("refresh_topology: "+this.refresh_topology);

			if (this.refresh_topology) {draw_stepcircles();}
			if (this.refresh_topology) {draw_stepconnectors();}
			
			if (this.mousePressed) {mouse_pressed_action();}
		
			this.display();
		}
	

	}

	/*----------------------------
	  methods
	----------------------------*/
	
	private void mouse_pressed_action()
	{
		{
			for(int l=0; l<this.stepcircles.size(); l++)
			{
				if (PApplet.dist(mouseX, mouseY, stepcircles.get(l).getPosition1(), stepcircles.get(l).getPosition2()) < stepcircles.get(l).getRadius())
				{
					if (this.stepcircle_clicked.equals(stepcircles.get(l))) {break;}
					this.stepcircle_clicked = stepcircles.get(l);
					this.stepcircle_marked = stepcircles.get(l);
					
					this.einstellungen.setMarkedStepName(this.stepcircle_marked.getStep().getName());
					
					// wenn es ein doppelklick ist
					if ( ( Calendar.getInstance().getTimeInMillis() - this.mousepressedlasttime.getTimeInMillis() )  < this.doubleclickperiod )
					{
//						System.out.println("timeperiod "+(Calendar.getInstance().getTimeInMillis() - this.mousepressedlasttime.getTimeInMillis()));
						// feststellen des stdout/stderr des steps
						String stdout = this.stepcircle_clicked.step.getAbsstdout();
						String stderr = this.stepcircle_clicked.step.getAbsstderr();
						
						java.io.File stdoutfile = new java.io.File(stdout);
						java.io.File stderrfile = new java.io.File(stderr);
						
						String neditstring = new String();
						
						if (stdoutfile.exists())
						{
							neditstring = neditstring+" "+stdout;
						}
						
						if (stderrfile.exists())
						{
							neditstring = neditstring+" "+stderr;
						}
						
						if (!(neditstring.isEmpty()))
						// Aufruf taetigen
						try {
							java.lang.Process sysproc = Runtime.getRuntime().exec("nedit "+neditstring);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
//					break;
				}
				else
				{
				}
			}
			this.mousepressedlasttime = Calendar.getInstance();
		}
	}
	
	private void draw_stepcircles()
	{

		if (this.refresh_topology) {this.markAllstepcirclestodelete();}
		// erstellen eines Stepcircles fuer jeden vorkommenden Step in process, falls es ihn noch nicht gibt. stepcircles fuer die es keinen step mehr gibt, sollen geloescht werden
		Step[] steps = p.getSteps2();
		// den virtuellen step 'root' hinzufuegen
		// System.out.println("Anzahl der Steps: "+steps.length);
		for(int i=0; i<steps.length; i++)
		{
			
//				Step step = steps[i];
			PmodelViewProcessingStepcircle stepcircle = new PmodelViewProcessingStepcircle(this, steps[i]);
			// wenn es noch keinen stepcircle mit diesem namen gibt, dann hinzufuegen zur page
//			if ((!(this.isStepcirclepresent_by_name(stepcircle.getName()))) && (!(stepcircle.getName().equals(rootstepname))))
			if (!(this.isStepcirclepresent_by_name(stepcircle.getName())))
			{
				this.addStepcircle(stepcircle);
			}
			else
			{
				// den stepcircle feststellen, der diesen step bereits repraesentiert
				stepcircle = null;
				if (this.hasStepcircle(steps[i].getName()))
				{
					PmodelViewProcessingStepcircle oldstepcircle = this.getStepcircle(steps[i].getName());
					// und den step setzen (damit die daten aktualisiert werden)
					oldstepcircle.setStep(steps[i]);
					if (this.refresh_topology) {oldstepcircle.setNochvorhanden(true);}
				}
			}
		}
		if (this.refresh_topology) {this.expungeStepcircles();}
	}
	
	private void draw_stepconnectors()
	{
		
		if (this.refresh_topology) {this.markAllstepconnectorstodelete();}
		// erstellen eines Stepconnectors fuer jeden vorkommenden fromstep-eintrag
		PmodelViewProcessingStepcircle[] stepcircles = this.getStepcircles2();
		for(int i=0; i<stepcircles.length; i++)
		{
			ArrayList<String> stepcirclenames_connect_from = stepcircles[i].getConnectfroms();
			
			System.out.println("====\ndetermining fromsteps of stepcircle '"+stepcircles[i].getName()+"'\n----");
			Iterator<String> iterstring = stepcirclenames_connect_from.iterator();
			while(iterstring.hasNext())
			{
				String stepcirclename = iterstring.next();
				System.out.println(stepcirclename);
			}
			System.out.println("====");
			for(int j=0; j<stepcirclenames_connect_from.size(); j++)
			{
				Iterator<PmodelViewProcessingStepcircle> iterstepcircle = this.getStepcircles(stepcirclenames_connect_from.get(j)).iterator();
				while (iterstepcircle.hasNext())
				{
					PmodelViewProcessingStepcircle stepcircle = iterstepcircle.next();
					PmodelViewProcessingStepconnector stepconnector = new PmodelViewProcessingStepconnector(this, stepcircle, stepcircles[i]);
					// wenn es noch keinen stepconnector mit diesem Namen gibt, dann hinzufuegen zur Page
					if (!(this.isStepconnectorpresent_by_name(stepconnector.getName())))
					{
						this.addStepconnector(stepconnector);
					}
					// wenn es ihn schon auf der page gibt, dann markieren um ihn zu behalten
					else
					{
						PmodelViewProcessingStepconnector oldstepconnector = this.getStepconnector(stepconnector.getName());
						if (this.refresh_topology) {oldstepconnector.setNochvorhanden(true);}
					}
				}
			}
		}
		if (this.refresh_topology) {this.expungeStepconnectors();}
	}
	
	
	private boolean hasStepconnector(String name)
	{
		Iterator<PmodelViewProcessingStepconnector> iterstepconnector = this.getStepconnectors().iterator();
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
		Iterator<PmodelViewProcessingStepconnector> iterstepconnector = this.getStepconnectors().iterator();
		while ( iterstepconnector.hasNext() )
		{
			iterstepconnector.next().setNochvorhanden(false);
		}
	}

	private void expungeStepconnectors()
	{
		Iterator<PmodelViewProcessingStepconnector> iterstepconnector = this.getStepconnectors().iterator();
		while ( iterstepconnector.hasNext() )
		{
			PmodelViewProcessingStepconnector stepconnector = iterstepconnector.next();
			if ( !(stepconnector.isNochvorhanden()) )
			{
//				System.out.println("loesche connector: "+stepconnector.getName()+"    "+stepconnector.toString());
				iterstepconnector.remove();
			}
		}
		this.refresh_topology = false;
	}

	private void expungeStepcircles()
	{
		Iterator<PmodelViewProcessingStepcircle> iterstepcircle = this.getStepcircles().iterator();
		while ( iterstepcircle.hasNext() )
		{
			PmodelViewProcessingStepcircle stepcircle = iterstepcircle.next();
			if ( !(stepcircle.isNochvorhanden()) )
			{
//				System.out.println("loesche stepcircle: "+stepcircle.getName());
				iterstepcircle.remove();
			}
		}
	}

	private void markAllstepcirclestodelete()
	{
		Iterator<PmodelViewProcessingStepcircle> iterstepcircle = this.getStepcircles().iterator();
		while ( iterstepcircle.hasNext() )
		{
			iterstepcircle.next().setNochvorhanden(false);
		}
	}

	void setZoomfaktor (int zoomfaktor)
	{
		this.zoomfaktor = zoomfaktor;
		// den zoomfaktor im Model synchron halten, damit das scale-widget aktualisiert wird
		this.einstellungen.setZoom(zoomfaktor);
	}

	void setZoomfaktor ()
	{
		// den zoomfaktor im Model synchron halten, damit das scale-widget aktualisiert wird
		this.einstellungen.setZoom(100);
	}

	void autoscale ()
	{
		this.setZoomfaktor();
	}

	public void mouseDragged()
	{
		this.stepcircle_clicked.setPosition(mouseX, mouseY, 0);
	}
	
	public void mouseReleased()
	{
		this.stepcircle_clicked = new PmodelViewProcessingStepcircle();
	}
	
	public void addStepcircle(PmodelViewProcessingStepcircle stepcircle)
	{
		this.stepcircles.add(stepcircle);
	}
	
	public void addStepconnector(PmodelViewProcessingStepconnector stepconnector)
	{
		this.stepconnectors.add(stepconnector);
	}
	
	public boolean isStepcirclepresent_by_name(String stepcirclename)
	{
		boolean returnvalue = false;
		PmodelViewProcessingStepcircle[] stepcircles = this.getStepcircles2();
		for(int i=0; i<stepcircles.length; i++)
		{
			if(stepcircles[i].getName().equals(stepcirclename))
			{
				returnvalue = true;
			}
		}
		return returnvalue;
	}
	
	public boolean isStepconnectorpresent_by_name(String stepconnectorname)
	{
		boolean returnvalue = false;
		PmodelViewProcessingStepconnector[] stepconnectors = this.getStepconnectors2();
		for(int i=0; i<stepconnectors.length; i++)
		{
			if(stepconnectors[i].getName().equals(stepconnectorname))
			{
				returnvalue = true;
			}
		}
		return returnvalue;
	}
	
	public void display()
	{
		// legende ausgeben
		this.fill(this.legendcolor[0], this.legendcolor[1], this.legendcolor[2]);
		this.text(this.legend_processname, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+2*this.legendsize+0);
		this.text(this.legend_processlastrevision, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+4*this.legendsize+0);
		this.text(this.legend_processstatus, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+6*this.legendsize+0);
		this.text((int)(((this.refresh_next.getTimeInMillis() - this.now.getTimeInMillis())/1000)+1), this.legendsize/2, this.getHeight() -40);
		this.text(((int)(this.frameRate)), this.getWidth() - this.legendsize/2 - this.legendsize*3, this.getHeight() - 40);

		// Stepconnectoren ausgeben
		Iterator<PmodelViewProcessingStepconnector> iterconn = this.stepconnectors.iterator();
		while(iterconn.hasNext())
		{
			PmodelViewProcessingStepconnector stepconnector = iterconn.next();
			stepconnector.display();
		}
		// Stepcircles ausgeben
		Iterator<PmodelViewProcessingStepcircle> itercirc = this.stepcircles.iterator();
		while(itercirc.hasNext())
		{
			PmodelViewProcessingStepcircle stepcircle = itercirc.next();
			stepcircle.display();
		}
		
//		System.out.println("Anzahl der Stepcircles    : "+this.stepcircles.size());
//		System.out.println("Anzahl der Stepconnectoren: "+this.stepconnectors.size());
		
	}
	
	public boolean refresh()
	{
		boolean result = false;
		System.out.println("refreshing data from: "+this.process.getInfilexml());
		try
		{
			this.p = process.readXml();
	    	this.legend_processname = p.getName()+" v"+p.getVersion().toString();
	    	
	    	this.refresh_last = Calendar.getInstance();
	    	this.refresh_topology = true;
	    	
	    	result = true;
		} catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	return result;
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
	public ArrayList<PmodelViewProcessingStepcircle> getStepcircles()
	{
		return this.stepcircles;
	}

	public boolean hasStepcircle(String stepcirclename)
	{
		boolean exist = false;
		PmodelViewProcessingStepcircle[] stepcircles = this.getStepcircles2();
		for(int i=0; i<stepcircles.length; i++)
		{
			if(stepcircles[i].getName().equals(stepcirclename))
			{
				exist = true;
			}
		}
		return exist;
	}

	public PmodelViewProcessingStepcircle getStepcircle(String stepcirclename)
	{
		PmodelViewProcessingStepcircle stepcircle = new PmodelViewProcessingStepcircle();
		PmodelViewProcessingStepcircle[] stepcircles = this.getStepcircles2();
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
	public ArrayList<PmodelViewProcessingStepcircle> getStepcircles(String stepcirclename)
	{
		ArrayList<PmodelViewProcessingStepcircle> stepcircles = new ArrayList<PmodelViewProcessingStepcircle>();
		
		Iterator<PmodelViewProcessingStepcircle> iterstepcircle = this.getStepcircles().iterator();
		while(iterstepcircle.hasNext())
		{
			// den namen abgleichen und merken wenn uebereinstimmung
			PmodelViewProcessingStepcircle stepcircle = iterstepcircle.next();
			if ( (stepcircle.getName().equals(stepcirclename)) || (stepcircle.getName().matches("^"+stepcirclename+"@")) )
			{
				stepcircles.add(stepcircle);
			}
		}
		return stepcircles;
	}

	public PmodelViewProcessingStepcircle[] getStepcircles2()
	{
		PmodelViewProcessingStepcircle[] stepcircles = new PmodelViewProcessingStepcircle[this.getStepcircles().size()];
		for(int i=0; i<stepcircles.length; i++)
		{
			stepcircles[i] = this.stepcircles.get(i);
		}
		return stepcircles;
	}

	public PmodelViewProcessingStepconnector[] getStepconnectors2()
	{
		PmodelViewProcessingStepconnector[] stepconnectors = new PmodelViewProcessingStepconnector[this.getStepconnectors().size()];
		for(int i=0; i<stepconnectors.length; i++)
		{
			stepconnectors[i] = this.stepconnectors.get(i);
		}
		return stepconnectors;
	}

	public PmodelViewProcessingStepconnector getStepconnector(String stepconnectorname)
	{
		Iterator<PmodelViewProcessingStepconnector> iterstepconnector = this.getStepconnectors().iterator();
		while ( iterstepconnector.hasNext() )
		{
			PmodelViewProcessingStepconnector stepconnector = iterstepconnector.next();
			if (stepconnector.getName().equals(stepconnectorname))
			{
				return stepconnector;
			}
		}
		System.err.println("Error in method getStepcircle(String stepcirclename). please check.");
		System.err.println("Cannot find a stepcircle with name '"+stepconnectorname+"'. returning null :-P");
		
		return null;
	}
	
	public ArrayList<PmodelViewProcessingStepconnector> getStepconnectors()
	{
		return this.stepconnectors;
	}

	public float getDamp()
	{
		float damp = (float)((5 / this.frameRate));
		if (damp < 0.1) {damp = (float)0.1;}
		else if (damp > 0.9) {damp = (float)0.9;}
//		System.out.println("damp: "+damp);
		return damp;
	}

	/*----------------------------
	methods set
	----------------------------*/
	public void setRottstepfull(boolean rootstepfull)
	{
		this.rootstepfull = rootstepfull;
	}
	
}
