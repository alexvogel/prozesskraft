package de.prozesskraft.pradar.parts;


import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Display;


//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.events.MouseWheelListener;



import processing.core.PApplet;
import processing.core.PFont;
import de.prozesskraft.pradar.Entity;
//import java.lang.management.ManagementFactory;

public class PradarViewProcessingPage extends PApplet
{
	/*----------------------------
	  structure
	----------------------------*/
//	PradarViewModel einstellungen;

	PradarPartUi3 parent;
	
	@Inject
//	private Entity entity_filter;
	Calendar now = Calendar.getInstance();
	Calendar mouse_last_pressed = Calendar.getInstance();
	int zoomfaktor = 150;
	int zoomfaktor_min = 60;
	int zoomfaktor_max = 1000;
	int center_x;
	int center_y;
	double center_ratio_x = 0.5;
	double center_ratio_y = 0.5;
	int mouse_pressed_x;
	int mouse_pressed_y;
	float doubleClickTimeSpan = 400;
	int refresh_interval = 600;
//	ArrayList<Entity> all_entities = new ArrayList<Entity>();
//	ArrayList<Entity> matched_parent_entities = new ArrayList<Entity>();
	ArrayList<PradarViewProcessingEntity> pentities_filtered = new ArrayList<PradarViewProcessingEntity>();
	Entity entity_nahe_maus = null;
	PradarViewProcessingEntity pentity_nahe_maus = null;
	boolean period_kreis_folgt_der_maus = false;
	Entity entity_mit_kleinstem_abstand_mouse = new Entity();
	float distanceToMouse = 100000;
	float maus_toleranz_pentity = 20;
	float maus_toleranz_period = 5;
//	int periodKreis = 24;
	private float[] legendposition = {0,0,0};
	private int[] legendcolor = {0,0,0};
    private int legendsize = (10);
	int once = 0;
	int bezugsgroesse = 10;

	int radius_basis;
	int durchmesser_basis;
	
	int radius_stunde;
	int radius_tag;
	int radius_woche;
	int radius_monat;
	int radius_jahr;
	int radius_period;

	int durchmesser_stunde;
	int durchmesser_tag;
	int durchmesser_woche;
	int durchmesser_monat;
	int durchmesser_jahr;
	int durchmesser_period;
	
	long jahrInMillis   = 29030400000L;
	long monatInMillis  = 2419200000L;
	long wocheInMillis  = 604800000L;
	long tagInMillis    = 86400000;
	long stundeInMillis = 3600000;
	
	/*----------------------------
	  method setup Processing
	----------------------------*/
	public void setup()
  {
		size(10, 10);
//		background(255);
//		noLoop();
		frameRate(10);
		PFont font = loadFont("AndaleMono-36.vlw");
//    	PFont font = this.loadFont("TheSans-Plain-12.vlw");
		textFont(font, 12);
		center_x = width/2;
		mouse_last_pressed.setTimeInMillis(0);
		refresh();
		addMouseWheelListener(listener_mousewheel);
		
		// initiales Daten abholen aus DB
  }
  
	/*----------------------------
	  method draw Processing
	----------------------------*/
	public void draw()
	{
		if (!(parent.einstellungen.animation))
		{
			return;
		}
		
		// frameRate reduzieren, falls das radar gar nicht angezeigt wird
		if (this.parent.einstellungen.getIsRadarVisible())
		{
			frameRate(10);
		}
		else
		{
			frameRate(1);
		}
		
		// do this only once at start but AFTER setup
		if (once == 0)
		{
			center_x = (int) (this.center_ratio_x * width);
			center_y = (int) (this.center_ratio_y * height);
			once = 1;
		}
		
		center_x = (int) (this.center_ratio_x * width);
		center_y = (int) (this.center_ratio_y * height);
		
		background(255);
		if (width < height) { this.bezugsgroesse = (width*this.zoomfaktor/100); }
		else { this.bezugsgroesse = (height*this.zoomfaktor/100); }

		this.radius_basis = ((this.bezugsgroesse/3)*1)/2;
		this.durchmesser_basis = this.radius_basis * 2;
		
		this.radius_stunde= this.radius_basis;
		this.radius_tag   = this.radius_basis * 2;
		this.radius_woche = this.radius_basis * 3;
		this.radius_monat = this.radius_basis * 4;
		this.radius_jahr = this.radius_basis * 5;
		Calendar period = Calendar.getInstance();
		period.setTimeInMillis(System.currentTimeMillis()-(long)(this.parent.einstellungen.getPeriod()) * 3600000);
//		period.setTimeInMillis(System.currentTimeMillis()-(long)(this.periodKreis) * 3600000);
		this.radius_period = (int) this.calcRadius(period);
		
		this.durchmesser_stunde= this.radius_stunde * 2;
		this.durchmesser_tag   = this.radius_tag * 2;
		this.durchmesser_woche = this.radius_woche * 2;
		this.durchmesser_monat = this.radius_monat * 2;
		this.durchmesser_jahr = this.radius_jahr * 2;
		this.durchmesser_period = (int) (this.radius_period * 2);
		
		
		// strichfarbe festlegen
		stroke(185);

		// zeitbereiche zeichnen

		//////////////////
		// stunde
		//////////////////
		
		// waagrechte und senkrechte linien verlaengern
		noFill();
		stroke(240);
		line( (center_x-radius_jahr-20),	center_y,					(center_x+radius_jahr+20),	center_y);
		line(  center_x,					(center_y-radius_jahr-20),	center_x,					(center_y+radius_jahr+20));
		
		// kreise fuer bruchteile einer stunde zeichnen
		strokeWeight(1);
		stroke(240);
		for(int viertelstunde=1; viertelstunde<=3; viertelstunde++)
		{
			ellipse(center_x, center_y, (float) ( durchmesser_stunde - (durchmesser_basis * ((float)viertelstunde/(float)4) ) ), (float) ( durchmesser_stunde - (durchmesser_basis * ((float)viertelstunde/(float)4) ) ) );
		}

		//////////////////
		// tag
		//////////////////
		
		// kreise fuer bruchteile eines tages zeichnen
		strokeWeight(1);
		stroke(240);

		if (zoomfaktor < 300)
		{
			for(int viertelDesTags=1; viertelDesTags<=4; viertelDesTags++)
			{
				ellipse(center_x, center_y, (float) ( durchmesser_tag - (durchmesser_basis * ((float)viertelDesTags/(float)4) ) ), (float) ( durchmesser_tag - (durchmesser_basis * ((float)viertelDesTags/(float)4) ) ) );
			}
		}
		else
		{
			for(int stundeDesTags=1; stundeDesTags<=23; stundeDesTags++)
			{
				ellipse(center_x, center_y, (float) ( durchmesser_tag - (durchmesser_basis * ((float)stundeDesTags/(float)23) ) ), (float) ( durchmesser_tag - (durchmesser_basis * ((float)stundeDesTags/(float)23) ) ) );
			}
		}

		//////////////////
		// woche
		//////////////////
		
		// kreise fuer bruchteile einer woche zeichnen
		strokeWeight(1);
		stroke(240);
		for(int tagDerWoche=1; tagDerWoche<=6; tagDerWoche++)
		{
			ellipse(center_x, center_y, (float) ( durchmesser_woche - (durchmesser_basis * ((float)tagDerWoche/(float)6) ) ), (float) ( durchmesser_woche - (durchmesser_basis * ((float)tagDerWoche/(float)6) ) ) );
		}

		//////////////////
		// monat
		//////////////////
		
		// kreise fuer bruchteile eines monats zeichnen
		strokeWeight(1);
		stroke(240);
		for(int wocheDesMonats=1; wocheDesMonats<=3; wocheDesMonats++)
		{
			ellipse(center_x, center_y, (float) ( durchmesser_monat - (durchmesser_basis * ((float)wocheDesMonats/(float)3) ) ), (float) ( durchmesser_monat - (durchmesser_basis * ((float)wocheDesMonats/(float)3) ) ) );
		}

		//////////////////
		// jahr
		//////////////////
		
		// kreise fuer bruchteile eines jahrs zeichnen
		strokeWeight(1);
		stroke(240);
		if (zoomfaktor < 300)
		{
			for(int viertelDesJahrs=1; viertelDesJahrs<=4; viertelDesJahrs++)
			{
				ellipse(center_x, center_y, (float) ( durchmesser_jahr - (durchmesser_basis * ((float)viertelDesJahrs/(float)4) ) ), (float) ( durchmesser_jahr - (durchmesser_basis * ((float)viertelDesJahrs/(float)4) ) ) );
			}
		}
		else
		{
			for(int monatDesJahrs=1; monatDesJahrs<=11; monatDesJahrs++)
			{
				ellipse(center_x, center_y, (float) ( durchmesser_jahr - (durchmesser_basis * ((float)monatDesJahrs/(float)11) ) ), (float) ( durchmesser_jahr - (durchmesser_basis * ((float)monatDesJahrs/(float)11) ) ) );
			}
		}
		
		//////////////////
		// hauptkreise zeichnen
		//////////////////
				
		// kreise fuer 1h, 1d, 1w, 1m zeichnen
		noFill();
		strokeWeight(1);
		stroke(100);
		ellipse(center_x, center_y, durchmesser_stunde, durchmesser_stunde);
		ellipse(center_x, center_y, durchmesser_tag, durchmesser_tag);
		ellipse(center_x, center_y, durchmesser_woche, durchmesser_woche);
		ellipse(center_x, center_y, durchmesser_monat, durchmesser_monat);
		ellipse(center_x, center_y, durchmesser_jahr, durchmesser_jahr);
		
		//////////////////
		// beschriftung der kreise
		//////////////////
		
		// texte schreiben
		stroke(100);
		textSize(bezugsgroesse/60);
		fill(100);
		text("now", (center_x+2), (center_y)-2);
		text("1h", (center_x+radius_stunde+2), (center_y)-2);
		text("1h", (center_x+radius_stunde+2), (center_y)-2);
		text("1d", (center_x+radius_tag+2), (center_y)-2);
		text("1w", (center_x+radius_woche+2), (center_y)-2);
		text("1m", (center_x+radius_monat+2), (center_y)-2);
		text("1y", (center_x+radius_jahr+2), (center_y)-2);
		noFill();

		//////////////////
		// filterkreis zeichnen mit beschriftung
		//////////////////
		stroke(0, 140, 200);
		ellipse(center_x, center_y, durchmesser_period, durchmesser_period);
		fill(0, 140, 200);
		text((int)this.parent.einstellungen.getPeriod()+"h", (center_x-radius_period+2), (center_y)-2);

		
		// legende schreiben
		legend();
		
		// ueber alle ProcessingEntities (nur parents) die angezeigt werden sollen iterieren und die visualisierung zeichnen
//		System.out.println("Anzahl der Entities : "+this.parent.entities_filtered.size());
//		System.out.println("Anzahl der Pentities: "+this.pentities_filtered.size());
		
		try
		{
			for (PradarViewProcessingEntity actualPentity : pentities_filtered)
			{
				Entity lulu = this.parent.getEntityBySuperId(actualPentity.getSuperid());
//				if (this.parent.getEntityBySuperId(actualPentity.getSuperid()) != null)
				if (lulu != null && lulu.getParentid().equals("0"))
				{
					actualPentity.calcNewBogenlaenge();
					actualPentity.calcPosition();
					actualPentity.draw();
				}
			}
			detMouseAndEntity();
		}
		catch (ConcurrentModificationException e)
		{
			System.out.println("warn: filter function altered the data while drawing. skipping draw.");
		}
		
		// fahne zeichnen, falls bedingungen erfuellt
		if (this.entity_nahe_maus != null)
		{
			draw_flag();
		}
		
	}

	void refresh()
	{
		syncPentities();
		fixPosition();
	}
	
	void fixPosition()
	{
		// ueber alle pentities iterieren und nur beim ersten die fixPosition auf true setzen
		// setzen des ersten auf fixPosition
		for (int x = 0; x<this.pentities_filtered.size(); x++)
		{
			PradarViewProcessingEntity pentity = this.pentities_filtered.get(x);
			if (x == 0)
			{
				pentity.setFixPosition(true);
			}
			else
			{
				pentity.setFixPosition(false);
			}
		}
	}
	
	void syncPentities()
	{
		this.parent.einstellungen.setAnimation(false);
		// zuerst alle bekannten pentities loeschen
//		pentities_filtered.removeAll(pentities_filtered);
		
		// ueber alle entities, die angezeigt werden sollen iterieren
		// - fuer diejenigen, die noch keine entsprechung (pentity) haben, soll eine erstellt werden
		for(Entity actEntity : this.parent.idEntities_filtered.values())
		{
			if ( !(isPentityPresent(actEntity)) )
			{
				PradarViewProcessingEntity newProcessingEntity = new PradarViewProcessingEntity(this, actEntity);
				this.pentities_filtered.add(newProcessingEntity);
			}
		}

		// ueber alle Processingentities, die existieren, soll iteriert werden und
		// - nicht mehr matchende sollen entfernt werden.
		
		ArrayList<PradarViewProcessingEntity> new_pentities_filtered = new ArrayList<PradarViewProcessingEntity>();
		for(PradarViewProcessingEntity actualPentity : pentities_filtered)
		{
			if (isEntityPresent(actualPentity))
			{
				new_pentities_filtered.add(actualPentity);
			}
		}
		this.pentities_filtered = new_pentities_filtered;
//		System.out.println("Anzahl der Entities : "+this.parent.entities_filtered);
//		System.out.println("Anzahl der Pentities: "+this.pentities_filtered);
		this.parent.einstellungen.setAnimation(true);
	}
	
	boolean isPentityPresent(Entity entity)
	{
		boolean isPresent = false;
	
		Iterator<PradarViewProcessingEntity> iterpentity = this.pentities_filtered.iterator();
		while (iterpentity.hasNext())
		{
			PradarViewProcessingEntity pentity = iterpentity.next();
			if ( pentity.getSuperid().equals(entity.getSuperid()) )
			{
				isPresent = true;
//				System.out.println("Entity "+entity.getId()+" is present as Pentity "+pentity.getId());
				return isPresent;
			}
		}
		return isPresent;
	}

	boolean isEntityPresent(PradarViewProcessingEntity pentity)
	{
		boolean isPresent = false;
	
		for(Entity actEntity : this.parent.idEntities_filtered.values())
		{
			if ( actEntity.getSuperid().equals(pentity.getSuperid()) )
			{
				isPresent = true;
				return isPresent;
			}
		}
		return isPresent;
	}

	void draw_flag()
	{
		// weiss
		stroke(100);
		fill(255);
		// dicke
		strokeWeight(1);
		rect(mouseX+3, mouseY-5, 165, -85, 5);

		fill(100);
		textSize(9);
		text("process:  "+this.entity_nahe_maus.getProcess(), mouseX+6, mouseY-80);
//		text("id:       "+this.entity_nahe_maus.getId(), mouseX+6, mouseY-80);
		text(wrapText(30,"id2:      "+this.entity_nahe_maus.getId2()), mouseX+6, mouseY-70);
		text("user:     "+this.entity_nahe_maus.getUser(),    mouseX+6, mouseY-60);
		text("host:     "+this.entity_nahe_maus.getHost(),    mouseX+6, mouseY-50);
		text("checkin:  "+this.entity_nahe_maus.getCheckinAsString(), mouseX+6, mouseY-40);
		text("checkout: "+this.entity_nahe_maus.getCheckoutAsString(),mouseX+6, mouseY-30);
		text(wrapText(30,"exitcode: "+this.entity_nahe_maus.getExitcode()),mouseX+6, mouseY-20);
		drawProgressRect(105, mouseX+6, mouseY-10);
		text("progress:     "+this.entity_nahe_maus.getProgressAsString(), mouseX+6, mouseY-10);
	}
	
	void drawProgressRect(int length, int posX, int posY)
	{
		stroke(100);
		if (this.pentity_nahe_maus != null)
		{
			if ( (this.entity_nahe_maus != null) && (this.entity_nahe_maus.getProgress() >= 0 ))
			{
				fill(this.pentity_nahe_maus.getColor("r")+100, this.pentity_nahe_maus.getColor("g")+100, this.pentity_nahe_maus.getColor("b")+100);
				rect(posX+52, posY+1, length * this.entity_nahe_maus.getProgress(), -10, 1);
				fill(255,255,255);
				rect(posX+52+(length*this.entity_nahe_maus.getProgress()), posY+1, length * (1-this.entity_nahe_maus.getProgress()), -10, 1);
			}
				
		}
		fill(100);
	}
	
	private String wrapText(int maxLength, String text)
	{
		if (text.length() > maxLength)
		{
			return (text.substring(0, maxLength - 3) + "..");
		}
		return text;
	}
	
	void detMouseAndEntity()
	{
		this.distanceToMouse = 1000000;
		this.entity_nahe_maus = null;
		this.pentity_nahe_maus = null;
		this.entity_mit_kleinstem_abstand_mouse = null;
		
		// feststellen der entity, die den kleinsten abstand zur mouse hat
		for(PradarViewProcessingEntity actualPentity : this.pentities_filtered)
		{
			float actualDistanceToMouse = actualPentity.calcDistToMouse();
			if (actualDistanceToMouse < this.distanceToMouse)
			{
				this.distanceToMouse = actualDistanceToMouse;
				entity_mit_kleinstem_abstand_mouse = this.parent.getEntityBySuperId(actualPentity.getSuperid());
			}
		}

		// feststellen ob der kleinste abstand kleiner grenzabstand ist
		if (this.distanceToMouse < this.maus_toleranz_pentity)
		{
			this.entity_nahe_maus = this.entity_mit_kleinstem_abstand_mouse;
			this.pentity_nahe_maus = getPentityBySuperId(this.entity_nahe_maus.getSuperid());
		}
	}
	
	float calcBogenlaengeFromPosition(float x, float y)
	{
		int quad;
		if ( (((this.center_y - y) * (-1)) >= 0)  &&  ((x - this.center_x) >= 0) ) {quad = 1;}
		else if ( (((this.center_y - y) * (-1)) >= 0)  &&  ((x - this.center_x) <= 0) ) {quad = 2;}
		else if ( (((this.center_y - y) * (-1)) <= 0)  &&  ((x - this.center_x) <= 0) ) {quad = 3;}
		else {quad = 4;}
		
		float bogenlaenge = 0;
		if (quad == 1)
		{
			bogenlaenge = (float) ( Math.atan(((this.center_y - y) * (-1)) / (x - this.center_x)) );
		}
		
		else if (quad == 2)
		{
			bogenlaenge = (float) (Math.PI + ( Math.atan(((this.center_y - y) * (-1)) / (x - this.center_x))) );
		}
		
		else if (quad == 3)
		{
			bogenlaenge = (float) (Math.PI + ( Math.atan(((this.center_y - y) * (-1)) / (x - this.center_x))) );
		}
		
		else
		{
			bogenlaenge = (float) (2*Math.PI + ( Math.atan(((this.center_y - y) * (-1)) / (x - this.center_x))) );
		}
		
		return bogenlaenge;
	}
	
	float calcRadiusFromPosition(float x, float y)
	{
		float bogenlaenge = calcBogenlaengeFromPosition(x,y);
		float radius = (float) (1 / Math.cos(bogenlaenge)) * (x - this.center_x);
		
		return radius;
	}
	
	void legend()
	{
		stroke(100);
		textSize(13);
		fill(this.legendcolor[0], this.legendcolor[1], this.legendcolor[2]);
		
		Timestamp timestamp = new Timestamp(calcTimeFromRadius(calcRadiusFromPosition(mouseX, mouseY)).getTimeInMillis());
		String beschnittener_string_timestamp = timestamp.toString().substring(0, 16);
		text(beschnittener_string_timestamp, 5, height-5);
//		text("prozesskraft.de", width-125, height-5);
//		if (this.parent.einstellungen.entitySelected != null)
//		{
//			text(this.parent.einstellungen.entitySelected.getId(), width/3, height-5);
//		}
//		text(this.entity_filter.getPeriodInMillis(), 50, height-5);
		noFill();
	}
	
	void setZoomfaktor (int zoomfaktor)
	{
		this.zoomfaktor = zoomfaktor;
		// den zoomfaktor im Model synchron halten, damit das scale-widget aktualisiert wird
		this.parent.einstellungen.setZoom(zoomfaktor);
	}

	void setZoomfaktor ()
	{
		// den zoomfaktor im Model synchron halten, damit das scale-widget aktualisiert wird
		this.parent.einstellungen.setZoom(150);
	}

	void autoscale ()
	{
		this.center_ratio_x = 0.5;
		this.center_ratio_y = 0.5;
		this.setZoomfaktor();
	}

	void doubleClick ()
	{
		
		if (this.entity_nahe_maus != null)
			// Aufruf taetigen
		{
			if (this.distanceToMouse < this.maus_toleranz_pentity)
			{
				java.io.File pmbFile = new java.io.File((new java.io.File(this.entity_nahe_maus.getResource()).getParent()+"/"+this.entity_nahe_maus.getProcess()+".pmb"));
				
				if(pmbFile.exists())
				{
					String aufruf = this.parent.ini.get("apps",  "pmodel") + " -instance "+pmbFile.getAbsolutePath();
					
					try
					{
						java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void mousePressed()
	{
		now = Calendar.getInstance();
//		System.out.println("now: "+now.getTimeInMillis()+" | mouse_last_pressed: "+mouse_last_pressed.getTimeInMillis());
		if ( (now.getTimeInMillis() - mouse_last_pressed.getTimeInMillis()) < doubleClickTimeSpan)
		{
			doubleClick();
		}
		
		if ( this.entity_nahe_maus != null )
		{
//			this.pentity_nahe_maus = getPentityBySuperId(this.entity_nahe_maus.getSuperid());
			this.parent.einstellungen.entitySelected = this.entity_nahe_maus;
		}
		// period-kreis umherziehen
		else if ( Math.abs((calcRadiusFromPosition(mouseX, mouseY) - this.radius_period)) < this.maus_toleranz_period)
		{
			this.period_kreis_folgt_der_maus = true;
		}
		
		else if ( this.entity_nahe_maus == null)
		{
			this.parent.einstellungen.entitySelected = null;
		}
		
		mouse_last_pressed = now;
		mouse_pressed_x = mouseX;
		mouse_pressed_y = mouseY;
	}
	
	public void mouseReleased()
	{
		this.pentity_nahe_maus = null;
		if (this.period_kreis_folgt_der_maus)
		{
			this.period_kreis_folgt_der_maus = false;
//			this.parent.einstellungen.setPeriod(this.periodKreis);
		}
	}
	
	public void mouseDragged()
		{
			
			// pentity umherziehen
			if ( this.pentity_nahe_maus != null )
			{
				this.pentity_nahe_maus.setBogenlaenge(calcBogenlaengeFromPosition(mouseX, mouseY));
			}
			
			// period-kreis umherziehen
			else if (this.period_kreis_folgt_der_maus)
			{
//				periodKreis = (calcPeriodFromTime(calcTimeFromRadius(calcRadiusFromPosition(mouseX, mouseY))));
				this.parent.einstellungen.setPeriod(calcPeriodFromTime(calcTimeFromRadius(calcRadiusFromPosition(mouseX, mouseY))));
			}
			
			// hintergrund umherziehen
			else
			{
				//		System.out.println("Ja mouse dragged: mouse_pressed_x="+mouse_pressed_x+"  || mouseX="+mouseX+" || center_x="+center_x);
				int new_center_x = (int)(this.center_ratio_x * width) + (mouseX-mouse_pressed_x);
				int new_center_y = (int)(this.center_ratio_y * height) + (mouseY-mouse_pressed_y);
				
				double new_center_ratio_x = ((double) new_center_x / (double)width);
				double new_center_ratio_y = ((double) new_center_y / (double)height);
				
				this.center_ratio_x = new_center_ratio_x;
				this.center_ratio_y = new_center_ratio_y;
				
				mouse_pressed_x = mouseX;
				mouse_pressed_y = mouseY;
			}
		}

	public PradarViewProcessingEntity getPentityBySuperId(String superid)
	{
		PradarViewProcessingEntity matching_pentity = null;
		Iterator<PradarViewProcessingEntity> iterpentity = this.pentities_filtered.iterator();
		while (iterpentity.hasNext())
		{
			PradarViewProcessingEntity pentity = iterpentity.next();
			if (pentity.getSuperid().equals(superid))
			{
				matching_pentity = pentity;
			}
		}
		return matching_pentity;
	}
	
	public Calendar calcTimeFromRadius(float radius)
	{
		Calendar time = Calendar.getInstance();
		long zeitspanne = 0;
		if (radius < 0)
		{
			return time;
		}
		else if ( (radius >= 0) && (radius < this.radius_stunde) )
		{
			zeitspanne = (long)PApplet.map(radius, 0, this.radius_stunde, 0, this.stundeInMillis);
		}
		else if ( (radius >= this.radius_stunde) && (radius < this.radius_tag) )
		{
			zeitspanne = (long)PApplet.map(radius, this.radius_stunde, this.radius_tag, this.stundeInMillis, this.tagInMillis);
		}
		else if ( (radius >= this.radius_tag) && (radius < this.radius_woche) )
		{
			zeitspanne = (long)PApplet.map(radius, this.radius_tag, this.radius_woche, this.tagInMillis, this.wocheInMillis);
		}
		else if ( (radius >= this.radius_woche) && (radius < this.radius_monat) )
		{
			zeitspanne = (long)PApplet.map(radius, this.radius_woche, this.radius_monat, this.wocheInMillis, this.monatInMillis);
		}
		else if ( (radius >= this.radius_monat) && (radius < this.radius_jahr) )
		{
			zeitspanne = (long)PApplet.map(radius, this.radius_monat, this.radius_jahr, this.monatInMillis, this.jahrInMillis);
		}
		else
		{
			zeitspanne = this.jahrInMillis;
		}
		
		time.setTimeInMillis((time.getTimeInMillis() - zeitspanne));
		return time;
	}

	public int calcPeriodFromTime(Calendar calendar)
	{
		int period = (int)((System.currentTimeMillis() - calendar.getTimeInMillis()) / 3600000);
		return period;
	}

	public float calcRadius(Calendar zeitpunkt)
	{
		
		if (zeitpunkt.getTimeInMillis() == 0)
		{
			zeitpunkt = Calendar.getInstance();
			return 0;
		}
		
		float radius;
		long zeitpunktInMillis = zeitpunkt.getTimeInMillis();
		long zeitspanne = System.currentTimeMillis() - zeitpunktInMillis;
		
		if (zeitspanne < 0)
		{
			return 0;
		}
		
		else if ( (zeitspanne >= 0) && (zeitspanne < this.stundeInMillis) )
		{
			radius = PApplet.map(zeitspanne, 0, this.stundeInMillis, 0, this.radius_stunde);
		}
		else if ( (zeitspanne >= this.stundeInMillis) && (zeitspanne < this.tagInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.stundeInMillis, this.tagInMillis, this.radius_stunde, this.radius_tag );
		}
		else if ( (zeitspanne >= this.tagInMillis) && (zeitspanne < this.wocheInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.tagInMillis, this.wocheInMillis, this.radius_tag, this.radius_woche );
		}
		else if ( (zeitspanne >= this.wocheInMillis) && (zeitspanne < this.monatInMillis) )
		{
			radius = PApplet.map((long)zeitspanne, (long)this.wocheInMillis, (long)this.monatInMillis, (long)this.radius_woche, (long)this.radius_monat );
		}
		else if ( (zeitspanne >= this.monatInMillis) && (zeitspanne < this.jahrInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.monatInMillis, this.jahrInMillis, this.radius_monat, this.radius_jahr );
		}
		else
		{
			radius = this.radius_jahr;
		}
//		System.out.println("zeitspanne "+zeitspanne+" bedeutet radius "+radius+" bedeutet "+new Timestamp(zeitpunkt.getTimeInMillis()).toString());
		return radius;
	}
	
	public void mouseWheel(int delta)
	{
//		System.out.println("mouse has moved by "+delta+" units");
		int newzoomfaktor = this.zoomfaktor - (delta*20);
		
		if (newzoomfaktor < this.zoomfaktor_min)
		{
			newzoomfaktor = this.zoomfaktor_min;
		}
		else if (newzoomfaktor > this.zoomfaktor_max)
		{
			newzoomfaktor = this.zoomfaktor_max;
		}
		
		// wie veraendert sich der zoomfaktor?
		double delta_zoomfaktor = (double)newzoomfaktor / (double)this.zoomfaktor;
		
		// feststellen an welcher verhaeltnis-position sich der mauszeiger gerade befindet
		double mouse_ratio_x = ((double)mouseX / (double)width);
		double mouse_ratio_y = ((double)mouseY / (double)height);
		
		// Abstand zwischen mouse_ratio und center_ratio
		double delta_center_ratio_minus_mouse_ratio_x = this.center_ratio_x - mouse_ratio_x;
		double delta_center_ratio_minus_mouse_ratio_y = this.center_ratio_y - mouse_ratio_y;
		
		// entsprechend muss das center bzgl. der aktuellen mausposition verschoben werden
		this.center_ratio_x = mouse_ratio_x + (delta_zoomfaktor * delta_center_ratio_minus_mouse_ratio_x);
		this.center_ratio_y = mouse_ratio_y + (delta_zoomfaktor * delta_center_ratio_minus_mouse_ratio_y);
		
		// neuen zoomfaktor setzen
		this.setZoomfaktor(newzoomfaktor);
		
	}
	
	java.awt.event.MouseWheelListener listener_mousewheel = new MouseWheelListener()
	{
		public void mouseWheelMoved(MouseWheelEvent me)
		{
			mouseWheel(me.getWheelRotation());
		}
	};

//	public PradarViewProcessingPage()
//	{
//		this.parent = new PradarPartUi3();
//	}
//	
	public PradarViewProcessingPage(PradarPartUi3 p)
	{
		this.parent = p;
	}

	/**
	 * @return the bezugsgroesse
	 */
	public int getBezugsgroesse()
	{
		return this.bezugsgroesse;
	}

	/**
	 * @param bezugsgroesse the bezugsgroesse to set
	 */
	public void setBezugsgroesse(int bezugsgroesse)
	{
		this.bezugsgroesse = bezugsgroesse;
	}

}
