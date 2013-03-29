package de.caegroup.pradar.parts;


import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;

//import org.eclipse.swt.events.MouseEvent;
//import org.eclipse.swt.events.MouseWheelListener;

import processing.core.PApplet;
import processing.core.PFont;
import de.caegroup.pradar.Db;
import de.caegroup.pradar.Entity;
//import java.lang.management.ManagementFactory;

public class PradarViewProcessingPage extends PApplet
{
	/*----------------------------
	  structure
	----------------------------*/
	private PradarViewModel einstellungen;
	private Db db;
	@Inject
	private Entity entity_filter;
	Calendar refresh_last = Calendar.getInstance();
	Calendar refresh_next = Calendar.getInstance();
	int min_refresh_interval = 5000;
	Calendar now = Calendar.getInstance();
	Calendar mouse_last_pressed = Calendar.getInstance();
	int zoomfaktor = 100;
	int zoomfaktor_min = 50;
	int zoomfaktor_max = 1000;
	int center_x;
	int center_y;
	double center_ratio_x = 0.5;
	double center_ratio_y = 0.5;
	int mouse_pressed_x;
	int mouse_pressed_y;
	float doubleClickTimeSpan = 400;
	int refresh_interval = 600;
	ArrayList<Entity> all_entities = new ArrayList<Entity>();
	ArrayList<Entity> matched_entities = new ArrayList<Entity>();
	ArrayList<PradarViewProcessingEntity> matched_processing_entities = new ArrayList<PradarViewProcessingEntity>();
	Entity entity_nahe_maus = null;
	PradarViewProcessingEntity pentity_nahe_maus = null;
	Entity entity_mit_kleinstem_abstand_mouse = new Entity();
	float distanceToMouse = 100000;
	float keine_fahne_ab_abstand_mehr_als = 20;
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
		refresh_last.setTimeInMillis(0);
		mouse_last_pressed.setTimeInMillis(0);
		
		addMouseWheelListener(listener_mousewheel);
		
		// initiales Daten abholen aus DB
		this.refresh();
  }
  
	/*----------------------------
	  method draw Processing
	----------------------------*/
	public void draw()
	{
		// do this only once at start but AFTER setup
		if (once == 0)
		{
			center_x = (int) (this.center_ratio_x * width);
			center_y = (int) (this.center_ratio_y * height);
			once = 1;
		}
		
		center_x = (int) (this.center_ratio_x * width);
		center_y = (int) (this.center_ratio_y * height);
		
		this.now = Calendar.getInstance();
		if ((now.after(this.refresh_next)) || ((this.keyPressed) && (this.key == ' ') && ((this.now.getTimeInMillis() - this.refresh_last.getTimeInMillis()) > this.min_refresh_interval)))
//		if ((now.after(this.refresh_next)) || ((this.key == ' ') && ((this.now.getTimeInMillis() - this.refresh_last.getTimeInMillis()) > 1000)))
		{
			this.refresh();
			this.filter(entity_filter);
		}
		
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
		period.setTimeInMillis(System.currentTimeMillis()-this.entity_filter.getPeriodInMillis());
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
		text((int)this.entity_filter.getPeriodInHours()+"h", (center_x-radius_period+2), (center_y)-2);

		
		// legende schreiben
		legend();
		
		// ueber alle ProcessingEntities, die angezeigt werden sollen iterieren und die visualisierung zeichnen
		Iterator<PradarViewProcessingEntity> iterpentity = matched_processing_entities.iterator();
		while (iterpentity.hasNext())
		{
			PradarViewProcessingEntity pentity = iterpentity.next();
			pentity.calcNewBogenlaenge();
			pentity.calcPosition();
			pentity.draw();
		}

		detMouseAndEntity();
		
		// fahne zeichnen, falls bedingungen erfuellt
		if (this.entity_nahe_maus != null)
		{
			draw_flag();
		}
		
	}

	void draw_flag()
	{
		// weiss
		stroke(100);
		fill(255);
		// dicke
		strokeWeight(1);
		rect(mouseX+3, mouseY-5, 165, -65, 5);

		fill(100);
		textSize(9);
		text("process:  "+this.entity_nahe_maus.getProcess(), mouseX+6, mouseY-60);
		text("user:     "+this.entity_nahe_maus.getUser(),    mouseX+6, mouseY-50);
		text("host:     "+this.entity_nahe_maus.getHost(),    mouseX+6, mouseY-40);
		text("checkin:  "+this.entity_nahe_maus.getCheckinAsString(), mouseX+6, mouseY-30);
		text("checkout: "+this.entity_nahe_maus.getCheckoutAsString(),mouseX+6, mouseY-20);
		text("exitcode: "+this.entity_nahe_maus.getExitcode(),mouseX+6, mouseY-10);
	}
	
	void detMouseAndEntity()
	{
		this.distanceToMouse = 1000000;
		this.entity_nahe_maus = null;
		this.pentity_nahe_maus = null;
		this.entity_mit_kleinstem_abstand_mouse = null;
		
		// feststellen der ententy, die den kleinsten abstand zur mouse hat
		Iterator<PradarViewProcessingEntity> iterpentity = this.matched_processing_entities.iterator();
		while (iterpentity.hasNext())
		{
			PradarViewProcessingEntity pentity = iterpentity.next();
			float actualDistanceToMouse = pentity.calcDistToMouse();
			if (actualDistanceToMouse < this.distanceToMouse)
			{
				this.distanceToMouse = actualDistanceToMouse;
				entity_mit_kleinstem_abstand_mouse = this.getEntityBySuperId(pentity.getSuperid());
			}
		}

		// feststellen ob der kleinste abstand kleiner grenzabstand ist
		if (this.distanceToMouse < this.keine_fahne_ab_abstand_mehr_als)
		{
			this.entity_nahe_maus = this.entity_mit_kleinstem_abstand_mouse;
		}
	}
	
	void refresh()
	{
		this.now = Calendar.getInstance();
		if ((this.now.getTimeInMillis() - this.refresh_last.getTimeInMillis()) > this.min_refresh_interval)
		{
			// daten holen aus db
			this.all_entities = db.getAllEntities();
			System.out.println("refreshing data...");
			this.refresh_last = Calendar.getInstance();
			this.refresh_next = Calendar.getInstance();
			this.refresh_next.add(13, this.refresh_interval);
			this.filter(entity_filter);
		}
		else
		{
			System.out.println("refresh interval must be at least "+(this.min_refresh_interval/1000)+" seconds.");
			
		}
	}

	void filter(Entity entity_filter)
	{
		this.matched_entities = entity_filter.getAllMatches(this.all_entities);

		// ueber alle entities, die angezeigt werden sollen iterieren und noch nicht vorhandene erstellen
		// fuer entities, die noch keine entsprechung bei den ProcessingEntities haben, soll eine erstellt werden
		Iterator<Entity> iterentity = this.matched_entities.iterator();
		while (iterentity.hasNext())
		{
			Entity entity = iterentity.next();
			
			if ( !(isProcessingEntityPresent(entity)) )
			{
				PradarViewProcessingEntity newProcessingEntity = new PradarViewProcessingEntity(this, entity);
				this.matched_processing_entities.add(newProcessingEntity);
//				System.out.println("Erstellen eines neues pentity superId: "+newProcessingEntity.getSuperid());
			}
		}
		
		// ueber alle Processingentities, die existieren, soll iteriert werden und
		// - nicht mehr matchende sollen entfernt werden.
		// - bei matchenden soll das checkout-datum von entity gesetzt werden (fuer den fall, dass sich das geaendert hat)
		ArrayList<PradarViewProcessingEntity> new_matched_processing_entities = new ArrayList<PradarViewProcessingEntity>();
		for (int x = 0; x<this.matched_processing_entities.size(); x++)
		{
			PradarViewProcessingEntity pentity = this.matched_processing_entities.get(x);
			if (isEntityPresent(pentity))
			{
				new_matched_processing_entities.add(pentity);
			}
		}
		this.matched_processing_entities = new_matched_processing_entities;
		
		// ueber alle pentities iterieren und nur beim ersten die fixPosition auf true setzen
		// setzen des ersten auf fixPosition
		for (int x = 0; x<this.matched_processing_entities.size(); x++)
		{
			PradarViewProcessingEntity pentity = this.matched_processing_entities.get(x);
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
	
	Entity getEntityBySuperId(String superId)
	{
		Entity entityWithSuperId = null;
		Iterator<Entity> iterentity = this.all_entities.iterator();
		while(iterentity.hasNext())
		{
			Entity entity = iterentity.next();
			if ( entity.getSuperid().equals(superId) )
			{
				entityWithSuperId = entity;
			}
		}
		return entityWithSuperId;
	}
	
//	void calcNewPosition()
//	{
//		// ueber alle ProcessingEntities, die angezeigt werden sollen iterieren und neue Position berechnen
//		Iterator<PradarViewProcessingEntity> iterpentity = matched_processing_entities.iterator();
//		while (iterpentity.hasNext())
//		{
//			PradarViewProcessingEntity pentity = iterpentity.next();
////			pentity.calcNewPosition();
//		}
//	}
	
	float calcBogenlaengeFromPosition(float x, float y)
	{
		float bogenlaenge = (float) ( Math.atan((y - this.center_y) / (x - this.center_x)) );
		return bogenlaenge;
	}
	
	boolean isProcessingEntityPresent(Entity entity)
	{
		boolean isPresent = false;
		Iterator<PradarViewProcessingEntity> iterpentity = this.matched_processing_entities.iterator();
		while (iterpentity.hasNext())
		{
			PradarViewProcessingEntity pentity = iterpentity.next();
			if ( pentity.getSuperid().equals(entity.getSuperid()) )
			{
				isPresent = true;
				return isPresent;
			}
		}
		return isPresent;
	}
	
	boolean isEntityPresent(PradarViewProcessingEntity pentity)
	{
		boolean isPresent = false;
		Iterator<Entity> iterentity = this.matched_entities.iterator();
		while (iterentity.hasNext())
		{
			Entity entity = iterentity.next();
			if ( entity.getSuperid().equals(pentity.getSuperid()) )
			{
				isPresent = true;
				return isPresent;
			}
		}
		return isPresent;
	}
	
	void legend()
	{
		stroke(100);
		textSize(13);
		fill(this.legendcolor[0], this.legendcolor[1], this.legendcolor[2]);
		text((int)(((this.refresh_next.getTimeInMillis() - this.now.getTimeInMillis())/1000)), 5, height-5);
		text("automation@caegroup.de", width-180, height-5);
		noFill();
	}
	
	void setFilter (Entity entity_filter)
	{
		this.entity_filter = entity_filter;
		System.out.println("setting new filter");
//		System.out.println("period is now: "+this.entity_filter.getPeriodInMillis());
		this.filter(this.entity_filter);
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
		this.center_ratio_x = 0.5;
		this.center_ratio_y = 0.5;
		this.setZoomfaktor();
	}

	void doubleClick ()
	{
		
		// Aufruf taetigen
		String aufruf = "nedit "+this.entity_nahe_maus.getResource();
//		System.out.println("entity_mit_fahne: "+entity_mit_fahne.getId());
		System.out.println("showing resource: "+aufruf);
		
		if (this.distanceToMouse < this.keine_fahne_ab_abstand_mehr_als)
		{
			try
			{
				java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			this.pentity_nahe_maus = getPentityBySuperId(this.entity_nahe_maus.getSuperid());
		}
		else
		{
			this.pentity_nahe_maus = null;
		}
		
		mouse_last_pressed = now;
		mouse_pressed_x = mouseX;
		mouse_pressed_y = mouseY;
	}
	
	public PradarViewProcessingEntity getPentityBySuperId(String superid)
	{
		PradarViewProcessingEntity matching_pentity = null;
		Iterator<PradarViewProcessingEntity> iterpentity = this.matched_processing_entities.iterator();
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
		
		if ( (zeitspanne >= 0) && (zeitspanne < this.stundeInMillis) )
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
	
	public void mouseDragged()
	{
		
		if ( this.pentity_nahe_maus != null )
		{
			this.pentity_nahe_maus.setBogenlaenge(calcBogenlaengeFromPosition(mouseX, mouseY));
		}
		
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

//	protected DataBindingContext initDataBindingsZoom()
//	{
//		DataBindingContext bindingContextZoom = new DataBindingContext();
//		//
//		IObservableValue targetObservableZoom = BeanProperties.value("zoomfaktor").observe(this);
//		IObservableValue modelObservableZoom = BeanProperties.value("zoom").observe(einstellungen);
//		bindingContextZoom.bindValue(targetObservableZoom, modelObservableZoom, null, null);
//		//
//		return bindingContextZoom;
//	}
	public void setDbfile(String pathToFile)
	{
		this.db.setDbfile(pathToFile);
	}
	
	public PradarViewProcessingPage()
	{
		this.entity_filter = new Entity();
		this.einstellungen = new PradarViewModel();
		this.db = new Db();
	}
	
	public PradarViewProcessingPage(String pathToDbfile)
	{
		this.entity_filter = new Entity();
		this.einstellungen = new PradarViewModel();
		this.db = new Db(pathToDbfile);
	}
	
	public PradarViewProcessingPage(Entity entity)
	{
		this.entity_filter = entity;
		this.einstellungen = new PradarViewModel();
		this.db = new Db();
	}

	public PradarViewProcessingPage(Entity entity, PradarViewModel einstellungen)
	{
		this.entity_filter = entity;
		this.einstellungen = einstellungen;
		this.db = new Db();
	}

	public PradarViewProcessingPage(String dbfile, Entity entity, PradarViewModel einstellungen)
	{
		this.entity_filter = entity;
		this.einstellungen = einstellungen;
		this.db = new Db(dbfile);
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
