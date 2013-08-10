package de.caegroup.pmodel;

//import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.lang.Math;

import processing.core.PApplet;
//import org.apache.solr.common.util.NamedList;

import de.caegroup.process.Step;

public class PmodelViewStepSym
{
	/*----------------------------
	  structure
	----------------------------*/
//	private double maxspeed = 0;
	
	private Random generator = new Random();
	private String name = new String();
	private int[] color = {255,255,255};
	private int radius = 40;
	private float[] position = new float[3];
//	private float[] textposition = {this.position[0] + (this.radius / 2) + this.textdistance, this.position[1] + (this.textsize / 2), this.position[2]};

	private float[] speed = {0,0,0};
//	private float maxspeed = 50;
	private float mass = (float)1;
	private float attraction = (float)5;
	private float spring = 10;
	private double timestamp = 0.0;
	
	private int[] strokecolor = {0,0,0};
    private int strokethickness = 1;

	private int textdistance = 2;
//	private float[] textposition = new float[3];
	private int[] textcolor = {50,50,50};
//    private int textsize = (this.radius / 2);
//    private boolean isastartingpoint = false;
    private boolean nochvorhanden = true;	// bei jedem durchlauf wird geprueft ob fuer den stepcircle noch ein step existiert.
   
    private String rank = "";
	
    private PmodelViewPage parent;
    public Step step;
	/*----------------------------
	  constructors
	----------------------------*/
	public PmodelViewStepSym(PmodelViewPage p, Step s)
	{
		this.parent = p;
		this.step = s;
		this.name = s.getName();

		// festlegen der initialen position und rank
		if (p.rootstepname.equals(this.name))
		{
			this.setPosition(p.getWidth()*this.parent.einstellungen.getRootpositionratiox(), p.getHeight()*this.parent.einstellungen.getRootpositionratioy(), 0);
//			this.rank = "";
		}
//		else {this.setPosition(p.width/2+(this.generator.nextInt(10)+80), p.height/2+(this.generator.nextInt(160)-80), 0);}
		else
		{
			int initx = this.generator.nextInt(p.getWidth());
			int inity = this.generator.nextInt(p.getHeight());
			this.setPosition(initx, inity, 0);
		}
		
		this.rank = this.step.getParent().detStepRank(this.step.getName());
//		System.out.println("Step "+this.name+": "+this.position[0]+" "+this.position[1]+" "+this.position[2]);
		
//		this.textposition[0] = this.position[0] + (this.radius / 2) + this.textdistance;
//		this.textposition[1] = this.position[1] + (this.textsize / 2);
//		this.textposition[2] = this.position[2];
//		this.textcolor[0] = 0;
//		this.textcolor[1] = 0;
//		this.textcolor[2] = 0;

	}
	
	public PmodelViewStepSym()
	{
		
	}
	
//	public Stepcircle(Page p, String stepname)
//	{
//		this.parent = p;
//		this.name = stepname;
//		
//		// festlegen der initialen position
//		if (p.rootstepname.equals(this.name)) {this.setPosition(p.frame.getWidth()/2, p.frame.getHeight()/2, 0);}
////		else {this.setPosition(p.width/2+(this.generator.nextInt(10)+80), p.height/2+(this.generator.nextInt(160)-80), 0);}
//		else
//		{
//			int initx = this.generator.nextInt(p.frame.getWidth());
//			int inity = this.generator.nextInt(p.frame.getHeight());
//			this.setPosition(initx, inity, 0);
//			System.out.println("x="+initx);
//			System.out.println("y="+inity);
//		}
//		 
//	}
	

	/*----------------------------
	  methods
	----------------------------*/
	public void display()
	{
		// output of diverse data for debugging
//		System.out.println("Name: "+this.name+" Position: "+this.position[0]+" "+this.position[1]+" "+this.position[2]);
//		System.out.println("Speed: "+this.name+" "+this.speed[0]+" "+this.speed[1]+" "+this.speed[2]);
//		System.out.println(this.getColor1()+" "+this.getColor2()+" "+this.getColor3());

//		makeTimeStamp("5211");
		// festlegen der circle color in Abhaengigkeit des stepstatus
		if (this.step.getStatus().equals("waiting")) {this.setColor(200, 200, 200);}
		else if (this.step.getStatus().matches("initializing|initialized|committing|committed|fanning|fanned")) {this.setColor(155, 213, 255);}
		else if (this.step.getStatus().equals("working")) {this.setColor(165, 99, 66);}
		else if (this.step.getStatus().equals("worked")) {this.setColor(165, 99, 66);}
		else if (this.step.getStatus().equals("finished")) {this.setColor(155, 213, 141);}
		else if (this.step.getStatus().equals("canceled")) {this.setColor(240, 240, 240);}
		else if (this.step.getStatus().equals("error")) {this.setColor(220, 0, 0);}
		
//		makeTimeStamp("5212");
		
		// wenn der stepcircle gerade markiert ist, soll die komplimentaerfarbe gewaehlt werden
		float R = this.getColor1();
		float G = this.getColor2();
		float B = this.getColor3();
		float minRGB = PApplet.min(R,PApplet.min(G,B));
		float maxRGB = PApplet.max(R,PApplet.max(G,B));
		float minPlusMax = minRGB + maxRGB;
		this.setColor((int)(minPlusMax - R), (int)(minPlusMax - G), (int)(minPlusMax - B));
		
//		makeTimeStamp("5213");
//		System.out.println("name is: "+this.step.getName());
//		System.out.println("type is: "+this.step.getType());
//		System.out.println("is a multistep?: "+this.step.isAmultistep());

//		System.out.println(this.step.getName()+" is a multistep: "+this.step.isAmultistep());
		
		// zeichne stepsymbol
		
//		makeTimeStamp("5214");
		if ( this.step.getName().equals(this.step.getParent().getRootstepname()))
		{
			symbol_quadrat_mit_x(this.parent.bezugsgroesse, true);
//			System.out.println("symbol: quadrat mit x");
		}
		
		else if ( this.step.getType().equals("automatic") && !(this.step.isAmultistep()) )
		{
			symbol_circle(this.parent.bezugsgroesse, 0, 0, true);
//			System.out.println("symbol: kreis");
		}
		else if ( this.step.getType().equals("manual") && !(this.step.isAmultistep()) )
		{
			symbol_quadrat(this.parent.bezugsgroesse, 0 , 0, true);
//			System.out.println("symbol: quadrat");
		}
		
		else if ( this.step.getType().equals("automatic") && this.step.isAmultistep() )
		{
			symbol_multistep(this.parent.bezugsgroesse, "circle");
//			System.out.println("symbol: multi-kreis");
		}

		else if ( this.step.getType().equals("manual") && this.step.isAmultistep() )
		{
			symbol_multistep(this.parent.bezugsgroesse, "quadrat");
//			System.out.println("symbol: multi-quadrat");
		}

		
		// root
		else
		{
			symbol_quadrat_mit_x(this.parent.bezugsgroesse, true);
		}
		
//		makeTimeStamp("5215");
		// schreibe die ranknummer in das symbol
		parent.fill(this.getTextcolor1(), this.getTextcolor2(), this.getTextcolor3());
		float neue_rankgroesse = (this.getRadius()/2)*this.parent.bezugsgroesse*parent.einstellungen.getRanksize()/10;
		parent.textSize(neue_rankgroesse);
		if (!(rank.matches("0.1")))
		{ 
			parent.text(rank, this.getPosition1() - rank.length()*(neue_rankgroesse/6), this.getPosition2() + (neue_rankgroesse/3));
		}
		
//		makeTimeStamp("5216");
		// zeichne beschriftung
		parent.fill(this.getTextcolor1(), this.getTextcolor2(), this.getTextcolor3());
		float neue_textgroesse = (this.getRadius()/2)*this.parent.bezugsgroesse*parent.einstellungen.getLabelsize()/10;
		parent.textSize(neue_textgroesse);
		parent.text(this.getName(), this.getPosition1()+this.getTextdistance()+((this.getRadius()/2)*this.parent.bezugsgroesse), this.getPosition2() + (neue_textgroesse/3));
//		parent.text(this.getName(), this.getPosition1()+this.getTextdistance()+((this.getRadius()/2)*this.parent.bezugsgroesse), this.getPosition2()+this.getTextdistance()+((this.getRadius()/2)*this.parent.bezugsgroesse));
		parent.textSize(this.parent.textSize_gemerkt);

		
//		if (this.isastartingpoint)
//		{
////			System.out.println("A Starting Point!");
//			parent.fill(this.getTextcolor1(), this.getTextcolor2(), this.getTextcolor3());
//			parent.text("S", this.getPosition1()-4, this.getPosition2()+5);
//		}

//		makeTimeStamp("5217");
		// reposition for next display
		this.reposition(this.parent);
		if (this.position[2] > 0.)
		{
			System.err.println(this.name+": Achtung: z > Null!!!"+this.position[0]+" "+this.position[1]+" "+this.position[2]);
			System.exit(1);
		}

	}

	private void makeTimeStamp(String string)
	{
		System.err.println(string+": "+new Timestamp(System.currentTimeMillis()));
		// TODO Auto-generated method stub
		
	}

	private void symbol_circle(float scalierung, float x_offset, float y_offset, boolean fill)
	{
		if (fill)
		{
			parent.fill(this.getColor1(), this.getColor2(), this.getColor3());
		}
		else
		{
			parent.fill(255, 255, 255);
		}
		parent.strokeWeight(this.getStrokethickness());
		parent.stroke(getStrokecolor1(), getStrokecolor2(), getStrokecolor3());
		parent.ellipse(this.getPosition1() + x_offset, this.getPosition2() + y_offset, this.getRadius() * scalierung, this.getRadius() * scalierung);
	}
	
	private void symbol_quadrat(float scalierung, float x_offset, float y_offset, boolean fill)
	{
		if (fill)
		{
			parent.fill(this.getColor1(), this.getColor2(), this.getColor3());
		}
		else
		{
			parent.fill(255, 255, 255);
		}
		parent.strokeWeight(this.getStrokethickness() * scalierung);
		parent.stroke(getStrokecolor1(), getStrokecolor2(), getStrokecolor3());
		parent.rectMode(3);
		parent.rect(this.getPosition1(), this.getPosition2(), this.getRadius() * scalierung, this.getRadius() * scalierung);
	}

	private void symbol_quadrat_mit_x(float scalierung, boolean fill)
	{
//		parent.strokeWeight(this.getStrokethickness());
//		parent.stroke(getStrokecolor1(), getStrokecolor2(), getStrokecolor3());
//		parent.rect(this.getPosition1()-this.getRadius()/2, this.getPosition2()-this.getRadius()/2, this.getRadius(), this.getRadius());
		symbol_quadrat(scalierung, 0, 0, fill);
		parent.line(this.getPosition1()+(int)((this.getRadius()/2)*0.8*scalierung), this.getPosition2()-(int)((this.getRadius()/2)*0.8*scalierung), this.getPosition1()-(int)((this.getRadius()/2)*0.8*scalierung), this.getPosition2()+(int)((this.getRadius()/2)*0.8*scalierung));
		parent.line(this.getPosition1()+(int)((this.getRadius()/2)*0.8*scalierung), this.getPosition2()+(int)((this.getRadius()/2)*0.8*scalierung), this.getPosition1()-(int)((this.getRadius()/2)*0.8*scalierung), this.getPosition2()-(int)((this.getRadius()/2)*0.8*scalierung));
//		parent.line(this.getPosition1()-this.getRadius()/2, this.getPosition2()+this.getRadius()/2, this.getPosition1()+this.getRadius()/2, this.getPosition2()-this.getRadius()/2);
	}
	
	// multistep wird mit drei verkleinerten symbolen dargestellt
	private void symbol_multistep(float scalierung, String type)
	{
		int anzahl_symbole = 3;
//		double laenge_vektor = (double)((0.5 + (0.5 - (1.0 / anzahl_symbole))) * this.radius);
		double laenge_vektor = (double)(0.6 * this.radius);
		float verkl_faktor = (float)(0.45)*scalierung;

		//
		if (type.equals("circle"))
		{
			symbol_circle(scalierung, 0, 0, false);
		}
		else if (type.equals("quadrat"))
		{
			symbol_quadrat(scalierung, 0, 0, false);
		}
		
		
		for (int x = 0; x < anzahl_symbole; x++)
		{
			double x_offset = ( java.lang.Math.sin(((java.lang.Math.PI * 2) / anzahl_symbole) * x) * laenge_vektor * verkl_faktor );
//			System.out.println(((((java.lang.Math.PI * 2) / anzahl_symbole) * x)) * laenge_vektor);
			double y_offset = ( java.lang.Math.cos(((java.lang.Math.PI * 2) / anzahl_symbole) * x) * laenge_vektor * verkl_faktor ) * (-1);
			
			if (type.equals("circle"))
			{
				symbol_circle(verkl_faktor, (float)x_offset, (float)y_offset, true);
			}
			else if (type.equals("quadrat"))
			{
				symbol_quadrat(verkl_faktor, (float)x_offset, (float)y_offset, true);
			}
		}
	}
	
	private void reposition(PmodelViewPage p)
	{
		// wenn der aktuell betrachtete step der 'initstep' ist, und sich ausserhalb der anzeige befindet -> auf mitte der Anzeige positionieren
		if ((p.rootstepname.equals(this.name)) && !(p.stepcircle_clicked.getName().equals(this.name)) && ((this.getPosition1() > p.getWidth()) || (this.getPosition2() > p.getHeight()) || (this.getPosition1() < 0) || (this.getPosition2() < 0)))
		{
			this.setPosition(p.getWidth()*this.parent.einstellungen.getRootpositionratiox(), p.getHeight()*this.parent.einstellungen.getRootpositionratioy(), 0);
		}
//		// wenn der aktuell betrachtete step ein 'startingpoint' ist, und sich ausserhalb der anzeige befindet -> auf zufaellige position innerhalb des fensters
//		else if ((this.isastartingpoint) && !(p.clicked_stepcircle.getName().equals(this.name)) && ((this.getPosition1() > p.width) || (this.getPosition2() > p.height) || (this.getPosition1() < 0) || (this.getPosition2() < 0)))
//		{
//			this.setPosition(p.width/2+(this.generator.nextInt(200)-100), p.height/2+(this.generator.nextInt(200)-100), 0);
//		}


		// wenn das Stepcircle gerade geklickt wird, dann auf neupositionierung verzichten
		if (!(p.stepcircle_clicked.getName().equals(this.getName())))
		{
			// newPosition = oldPosition + (newSpeed * time)
			// newSpeed = (oldSpeed + speedDiff) * (1/damping)
			// speedDiff = ((antiGravity_impuls + connectorForce) / mass) * time
	//		double newtimestamp = (p.millis()/1000);
			double newtimestamp = (p.millis());
			double timestep = newtimestamp - this.timestamp;
			this.timestamp = newtimestamp;
	//		System.out.println("NewTimestamp: "+newtimestamp);
	//		System.out.println("Timestep: "+timestep);
	//		System.out.println("Mass: "+this.mass);

			float[] rejectionPuls = this.calRejectionPuls(p);
			float[] attractionPuls = this.calAttractionPuls(p);

			float[] speeddiff = new float[3];
			speeddiff[0] = (float)(((rejectionPuls[0] + attractionPuls[0]) / this.mass + this.parent.einstellungen.getGravx()));
			speeddiff[1] = (float)(((rejectionPuls[1] + attractionPuls[1]) / this.mass + this.parent.einstellungen.getGravy()));
			speeddiff[2] = (float)(((rejectionPuls[2] + attractionPuls[2]) / this.mass));

			float[] oldspeed = this.getSpeed();
			float[] newspeed = new float[3];
			newspeed[0] = (oldspeed[0] + speeddiff[0]) * (1-this.parent.getDamp());
			newspeed[1] = (oldspeed[1] + speeddiff[1]) * (1-this.parent.getDamp());
			newspeed[2] = (oldspeed[2] + speeddiff[2]) * (1-this.parent.getDamp());

			// wenn initialer step, dann festhalten
			if (this.name.equals(p.rootstepname))
			{
				newspeed[0] = 0;
				newspeed[1] = 0;
				newspeed[2] = 0;
			}

			this.speed = newspeed;

			float[] oldposition = this.getPosition();
			float[] newposition = new float[3];
			float[] repositionvektor = new float[3];
			
			repositionvektor[0] = (float)(newspeed[0] * timestep*0.001);
			repositionvektor[1] = (float)(newspeed[1] * timestep*0.001);
			repositionvektor[2] = (float)(newspeed[2] * timestep*0.001);
			
			newposition[0] = (float)(oldposition[0] + repositionvektor[0]);
			newposition[1] = (float)(oldposition[1] + repositionvektor[1]);
			newposition[2] = (float)(oldposition[2] + repositionvektor[2]);
	
			// die newposition darf im extremfall nicht zu stark vom fenster abweichen +- 1Fenstergrösse in jede Richtung			
			if (newposition[0] > this.parent.width*2) 		{newposition[0] = this.parent.width*2;}
			if (newposition[0] < this.parent.width*(-1)) 	{newposition[0] = this.parent.width*(-1);}
			if (newposition[1] > this.parent.height*2) 		{newposition[1] = this.parent.height*2;}
			if (newposition[1] < this.parent.height*(-1)) 	{newposition[1] = this.parent.height*(-1);}

			
			
			this.setPosition(newposition[0], newposition[1], newposition[2]);
//			System.out.println(this.getName()+" new position is: "+newposition[0]+" | "+newposition[1]);
		}
	}
	
	private float[] calRejectionPuls(PmodelViewPage p)
	{
		Iterator<PmodelViewStepSym> iter = p.getStepcircles().iterator();

		float[] rejectionPuls = new float[3];
		
		while(iter.hasNext())
		{
			PmodelViewStepSym stepcircle = iter.next();
			if (!(stepcircle.getName().equals(this.getName())))
			{
				// berechnung des abstands
				float distance = calDistance(stepcircle, this);
				// mindestabstand um extreme geschwindigkeiten zu vermeiden
				if (distance < 10) {distance = 10;}
				
				// berechnung einheitsrichtungsvektor von anderem circle zu diesem (soll die richtung der Kraft sein)
				float[] einheitsrichtungsvektor = calEinheitsrichtungsvektor(this, stepcircle);
				
				// antigravitation nimmt mit dem quadrat des abstands ab
				rejectionPuls[0] += (einheitsrichtungsvektor[0] / (Math.pow((distance/100),2))) * this.attraction * (this.parent.bezugsgroesse * 3);
				rejectionPuls[1] += (einheitsrichtungsvektor[1] / (Math.pow((distance/100),2))) * this.attraction * (this.parent.bezugsgroesse * 3);
				rejectionPuls[2] += (einheitsrichtungsvektor[2] / (Math.pow((distance/100),2))) * this.attraction * (this.parent.bezugsgroesse * 3);
			}
		}
//		System.out.println("	Antigravitypuls: "+this.name+" "+antigravitypuls[0]+" "+antigravitypuls[1]+" "+antigravitypuls[2]);
		return rejectionPuls;
	}
	
	private float[] calAttractionPuls(PmodelViewPage p)
	{
		Iterator<PmodelViewStepSym> iter1 = p.getStepcircles().iterator();
//		Iterator<String> iter1 = this.connectfrom.iterator();
//		Iterator<Stepcircle> iter2 = p.getStepcircles().iterator();
		
		float[] attractionPuls = new float[3];

		while(iter1.hasNext())
		{
			PmodelViewStepSym stepcircle = iter1.next();

			// wenn es sich um aktuellen stepcircle handelt oder um einen nicht verbundenen, dann ignorieren
//			System.out.println("Akt Stepcircle: "+this.getName()+"    testet Stepcircle: "+stepcircle.getName()+"     isconnected: "+this.isConnected(stepcircle.getName()));
			if ((!(stepcircle.getName().equals(this.getName()))) && (((this.isConnected(stepcircle.getName()))) || stepcircle.isConnected(this.getName())))
			{

//				System.out.println("Berechnung wird durchgefuehrt");
			// berechnung des abstands
				float distance = calDistance(stepcircle, this);

			// berechnung einheitsrichtungsvektor von diesem zum anderen (soll die richtung der Kraft sein)
				float[] einheitsrichtungsvektor = calEinheitsrichtungsvektor(stepcircle, this);
			
			// federkraft nimmt mit dem abstand zu 
				attractionPuls[0] += einheitsrichtungsvektor[0] * this.spring * (Math.pow((distance/100),2)) * (5*(1/this.parent.bezugsgroesse));
				attractionPuls[1] += einheitsrichtungsvektor[1] * this.spring * (Math.pow((distance/100),2)) * (5*(1/this.parent.bezugsgroesse));
				attractionPuls[2] += einheitsrichtungsvektor[2] * this.spring * (Math.pow((distance/100),2)) * (5*(1/this.parent.bezugsgroesse));
			}
		}
//		System.out.println("	Connectorpuls: "+this.name+" "+connectorpuls[0]+" "+connectorpuls[1]+" "+connectorpuls[2]);
		return attractionPuls;
	}
	
	private float[] calEinheitsrichtungsvektor(PmodelViewStepSym stepcircle1, PmodelViewStepSym stepcircle2)
	{
		float[] pos1 =  stepcircle1.getPosition();
		float[] pos2 =  stepcircle2.getPosition();
		
		float[] vekt = new float[3]; 
		vekt[0] = pos1[0] - pos2[0];
		vekt[1] = pos1[1] - pos2[1];
		vekt[2] = pos1[2] - pos2[2];
//		System.out.println("		Step1_Position: "+stepcircle1.name+" "+pos1[0]+" "+pos1[1]+" "+pos1[2]);
//		System.out.println("		Step2_Position: "+stepcircle2.name+" "+pos2[0]+" "+pos2[1]+" "+pos2[2]);
//		System.out.println("		Vektor_Step1->Step2: "+this.name+" "+vekt[0]+" "+vekt[1]+" "+vekt[2]);
		
		float distance = (float)Math.sqrt((double)((vekt[0] * vekt[0]) + (vekt[1] * vekt[1]) + (vekt[2] * vekt[2])));
//		System.out.println("			Distance: "+stepcircle1.name+"<-->"+stepcircle2.name+" "+distance);
		
		// verhindern, dass distance 0 wird
		if (distance < 0.001) {distance = (float)0.001;}
		
		float[] einvekt = new float[3];
		einvekt[0] = vekt[0] / distance;
		einvekt[1] = vekt[1] / distance;
		einvekt[2] = vekt[2] / distance;
		
//		System.out.println("		Einheitsvektor: "+this.name+" "+einvekt[0]+" "+einvekt[1]+" "+einvekt[2]);
		return einvekt;
	}

	private float calBetragvektor(float vekt1, float vekt2, float vekt3)
	{
		return (float)(Math.sqrt((vekt1 * vekt1) + (vekt2 * vekt2) + (vekt3 * vekt3)));
	}
	
	private float calDistance(PmodelViewStepSym stepcircle1, PmodelViewStepSym stepcircle2)
	{
		float[] pos1 =  stepcircle1.getPosition();
		float[] pos2 =  stepcircle2.getPosition();
		
		float[] vekt = new float[3]; 
		vekt[0] = pos1[0] - pos2[0];
		vekt[1] = pos1[1] - pos2[1];
		vekt[2] = pos1[2] - pos2[2];
		
		return (float)(Math.sqrt((vekt[0] * vekt[0]) + (vekt[1] * vekt[1]) + (vekt[2] * vekt[2])));
	}

	private boolean isConnected (String stepname)
	{
		if (this.getConnectfroms().contains(stepname))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	
	/*----------------------------
	  methods get
	----------------------------*/
	public String getName()
	{
		return this.name;
	}

	public int[] getColor()
	{
		return this.color;
	}

	public int getColor1()
	{
		return this.color[0];
	}

	public int getColor2()
	{
		return this.color[1];
	}

	public int getColor3()
	{
		return this.color[2];
	}

	public int getRadius()
	{
		return this.radius;
	}
	
	public PmodelViewPage getParent()
	{
		return this.parent;
	}

	public float[] getPosition()
	{
//		System.out.println("Stepname "+this.getName()+": Position: "+this.position[0]+" "+this.position[1]+" "+this.position[2]);
		return this.position;
	}
	
	public float getPosition1()
	{
		return this.position[0];
	}

	public float getPosition2()
	{
		return this.position[1];
	}

	public float getPosition3()
	{
		return this.position[2];
	}

	public float[] getSpeed()
	{
//		System.out.println("Stepname "+this.getName()+": Position: "+this.position[0]+" "+this.position[1]+" "+this.position[2]);
		return this.speed;
	}

	public int[] getStrokecolor()
	{
		return this.strokecolor;
	}
	
	public int getStrokecolor1()
	{
		return this.strokecolor[0];
	}

	public int getStrokecolor2()
	{
		return this.strokecolor[1];
	}

	public int getStrokecolor3()
	{
		return this.strokecolor[2];
	}

	public int getStrokethickness()
	{
		return this.strokethickness;
	}
	
	public int getTextdistance()
	{
		return this.textdistance;
	}

	public int[] getTextcolor()
	{
		return this.textcolor;
	}

	public int getTextcolor1()
	{
		return this.textcolor[0];
	}

	public int getTextcolor2()
	{
		return this.textcolor[1];
	}

	public int getTextcolor3()
	{
		return this.textcolor[2];
	}

	public Step getStep()
	{
		return this.step;
	}

	public boolean isNochvorhanden()
	{
		return this.nochvorhanden;
	}

	/**
	 *  @return
	 * liefert eine liste der stepcircles zurueck, mit denen eine verbindung besteht.
	 * aufgefaecherte steps werden beruecksichtigt,
	 * d.h. bestand eine Verbindung mit dem multistep, so besteht auch eine verbindung mit davon abgeleiteten steps
	 * wenn es explizit so gewollt ist, wird die root-verbindung immer beruecksichtigt.
	 * ansonsten nur wenn sie die einzige verbindung ist.
	 */
	public ArrayList<String> getConnectfroms()
	{
		ArrayList<String> connectfrom = new ArrayList<String>();
		boolean seenroot = false;
		// ermittle die 'fromsteps' des aktuellen steps
		ArrayList<Step> fromsteps = this.step.getFromsteps();
//		System.out.println("Name des aktuellen Steps: "+this.getStep().getName());
//		System.out.println("Anzahl der fromsteps: "+fromsteps.size());
		Iterator<Step> iterfromstep = fromsteps.iterator();
		while (iterfromstep.hasNext())
		{
			Step fromstep = iterfromstep.next();
//			System.out.println("fromstep:       "+fromstep.getName());
			// root ueberspringen, jedoch merken, wenn 'root' dabei ist.
			if (fromstep.getName().equals(this.getStep().getParent().getRootstepname()))
			{
//				System.out.println("ich habe bemerkt, dass aktueller fromstep 'root' ist.");
				seenroot = true;
			}
			else
			{
//				System.out.println("ich habe bemerkt, dass aktueller fromstep NICHT 'root' ist.");
				connectfrom.add(fromstep.getName());
			}
		}
//		System.out.println("Anzahl aller fromsteps (ohne root): "+connectfrom.size());
		
//		System.out.println("Jetzt wird entschieden was mit 'root' passieren soll.");
		// root sonderbehandlung
		if (seenroot)
		{
//			System.out.println("Ja - root kam in den fromsteps vor.");
			if ( (this.parent.rootstepfull) )
			{
//				System.out.println("die verbindung zu root soll immer beruecksichtigt werden.");
				connectfrom.add(this.getStep().getParent().getRootstepname());
			}
			else if ( (!(this.parent.rootstepfull)) && (connectfrom.size() == 0) )
			{
//				System.out.println("die verbindung zu root soll nur beruecksichtigt werden, wenn root der einzige fromstep ist und das ist hier der fall.");
				connectfrom.add(this.getStep().getParent().getRootstepname());
			}
			else
			{
//				System.out.println("aber es gab fromsteps ausser root, deshalb wird root nicht der liste hinzugefuegt");
			}
		}

//		System.out.println("Anzahl aller fromsteps (inkl. der sonderbehandlung von 'root'): "+connectfrom.size());
		return connectfrom;
	}
	
	
	/*----------------------------
	methods set
	----------------------------*/
	public void setName(String name)
	{
		this.name = name;
	}

	public void setColor(int color1, int color2, int color3)
	{
		this.color[0] = color1;
		this.color[1] = color2;
		this.color[2] = color3;
	}

	public void setColor1(int color1)
	{
		this.color[0] = color1;
	}

	public void setColor2(int color2)
	{
		this.color[1] = color2;
	}

	public void setColor3(int color3)
	{
		this.color[2] = color3;
	}

	public void setRadius(int radius)
	{
		this.radius = radius;
	}

	public void setPosition(float position1, float position2, float position3)
	{
		this.position[0] = position1;
		this.position[1] = position2;
		this.position[2] = position3;
//		this.textposition[0] =this.position[0] + (this.radius / 2) + this.textdistance;
//		this.textposition[1] = this.position[1] + (this.textsize / 2);
//		this.textposition[2] = this.position[2];
		
	}

	public void setSpeed(float speed1, float speed2, float speed3)
	{
		this.speed[0] = speed1;
		this.speed[1] = speed2;
		this.speed[2] = speed3;
	}

	public void setPosition1(float position1)
	{
		this.position[0] = position1;
//		this.textposition[0] =this.position[0] + (this.radius / 2) + this.textdistance;
	}

	public void setPosition2(float position2)
	{
		this.position[1] = position2;
//		this.textposition[1] = this.position[1] + (this.textsize / 2);
	}

	public void setPosition3(float position3)
	{
		this.position[2] = position3;
//		this.textposition[2] = this.position[2];
	}

	public void setStrokecolor(int color1, int color2, int color3)
	{
		this.strokecolor[0] = color1;
		this.strokecolor[1] = color2;
		this.strokecolor[2] = color3;
	}

	public void setStrokecolor1(int color1)
	{
		this.strokecolor[0] = color1;
	}

	public void setStrokecolor2(int color2)
	{
		this.strokecolor[1] = color2;
	}

	public void setStrokecolor3(int color3)
	{
		this.strokecolor[2] = color3;
	}

	public void setStrokethickness(int strokethickness)
	{
		this.strokethickness = strokethickness;
	}

	public void setTextdistance(int textdistance)
	{
		this.textdistance = textdistance;
	}

	public void setTextcolor(int color1, int color2, int color3)
	{
		this.textcolor[0] = color1;
		this.textcolor[1] = color2;
		this.textcolor[2] = color3;
	}

	public void setTextcolor1(int color1)
	{
		this.textcolor[0] = color1;
	}

	public void setTextcolor2(int color2)
	{
		this.textcolor[1] = color2;
	}

	public void setTextcolor3(int color3)
	{
		this.textcolor[2] = color3;
	}

	public void setStep(Step step)
	{
		this.step = step;
	}

	public void setParent(PmodelViewPage page)
	{
		this.parent = page;
	}

	public void setNochvorhanden(boolean nochvorhanden)
	{
		this.nochvorhanden = nochvorhanden;
	}
}
