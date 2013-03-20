package de.caegroup.pradar.parts;


import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
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

public class PradarViewProcessing extends PApplet
{
	/*----------------------------
	  structure
	----------------------------*/
	private PradarViewModel einstellungen;
	private Db db = new Db();
	@Inject
	private Entity entity_filter;
	Calendar refresh_last = Calendar.getInstance();
	Calendar refresh_next = Calendar.getInstance();
	int min_refresh_interval = 5000;
	Calendar now = Calendar.getInstance();
	Calendar mouse_last_pressed = Calendar.getInstance();
	int zoomfaktor = 100;
	int zoomfaktor_min = 50;
	int zoomfaktor_max = 500;
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
	Entity entity_mit_fahne = new Entity();
	float kleinster_abstand = 100000;
	float keine_fahne_ab_abstand_mehr_als = 20;
	private float[] legendposition = {0,0,0};
	private int[] legendcolor = {0,0,0};
    private int legendsize = (10);
	int once = 0;

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
		int bezugsgroesse = 10;
		if (width < height) { bezugsgroesse = (width*this.zoomfaktor/100); }
		else { bezugsgroesse = (height*this.zoomfaktor/100); }
		
		// strichfarbe festlegen
		stroke(185);

		// linien zeichnen
		noFill();
		stroke(240);
		line( (center_x-bezugsgroesse/2-20),	center_y,						(center_x+bezugsgroesse/2+20),	center_y);
		line(  center_x,						(center_y-bezugsgroesse/2-20),	center_x,						(center_y+bezugsgroesse/2-20));
		
		// kreise zeichnen

		// kreis 1w
		strokeWeight(1);
		stroke(100);
		ellipse(center_x, center_y, bezugsgroesse, bezugsgroesse);
		strokeWeight(1);
		stroke(240);
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.167))), (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.167))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.333))), (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.333))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.500))), (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.500))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.667))), (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.667))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.833))), (float) ((bezugsgroesse/3)*2+((bezugsgroesse/3)*(0.833))) );

		stroke(100);
		textSize(bezugsgroesse/60);
		fill(100);
		text("1w", (center_x+(bezugsgroesse/2)+2), (center_y)-2);
		noFill();

		// kreis 1d
		strokeWeight(1);
		stroke(100);
		ellipse(center_x, center_y, (bezugsgroesse/3)*2, (bezugsgroesse/3)*2);
		strokeWeight(1);
		stroke(240);
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.167))), (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.167))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.333))), (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.333))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.500))), (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.500))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.667))), (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.667))) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.833))), (float) ((bezugsgroesse/3)+((bezugsgroesse/3)*(0.833))) );

		stroke(100);
		textSize(bezugsgroesse/60);
		fill(100);
		text("1d", (center_x+(bezugsgroesse/3)+2), (center_y)-2);
		noFill();

		// kreis 1h
		noFill();
		strokeWeight(1);
		stroke(100);
		ellipse(center_x, center_y, bezugsgroesse/3, bezugsgroesse/3);
		strokeWeight(1);
		stroke(240);
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*(0.167)), (float) ((bezugsgroesse/3)*(0.167)) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*(0.333)), (float) ((bezugsgroesse/3)*(0.333)) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*(0.500)), (float) ((bezugsgroesse/3)*(0.500)) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*(0.667)), (float) ((bezugsgroesse/3)*(0.667)) );
		ellipse(center_x, center_y, (float) ((bezugsgroesse/3)*(0.833)), (float) ((bezugsgroesse/3)*(0.833)) );

		stroke(100);
		textSize(bezugsgroesse/60);
		fill(100);
		text("1h", (center_x+((bezugsgroesse/3)/2)+2), (center_y)-2);
		noFill();
		
		stroke(100);
		textSize(bezugsgroesse/60);
		fill(100);
		text("now", (center_x+2), (center_y)-2);
		noFill();

		// ueber alle gefilterten entities iterieren
		Iterator<Entity> iterentity = matched_entities.iterator();
//		System.out.println("Hello"+matched_entities.size());
		
		legend();
		
//		int anzahl_entities = matched_entities.size();
//		int zaehler = 1;
		kleinster_abstand = 100000;
		while (iterentity.hasNext())
		{
			Entity entity = iterentity.next();
			
			// Berechnen des ersten Punktes
			Calendar checkin = entity.getCheckin();
			Calendar checkout = entity.getCheckout();
			Calendar now = Calendar.getInstance();
			now.setTimeInMillis(System.currentTimeMillis());
			if (checkout.getTimeInMillis() == 0)
			{
				checkout = now;
			}
			
			long checkin_from_now_in_millis = now.getTimeInMillis() - checkin.getTimeInMillis();
			long checkout_from_now_in_millis = now.getTimeInMillis() - checkout.getTimeInMillis();
			
//			long faktor = (long) (bezugsgroesse/log(604800000));
			
			long woche  = 604800000;
			long tag    = 86400000;
			long stunde = 3600000;
			
			float radius_checkin;
			if ( (checkin_from_now_in_millis >= 0) && (checkin_from_now_in_millis < stunde) )
			{
				radius_checkin = map(checkin_from_now_in_millis, 0, stunde, 0, (bezugsgroesse/2)/3);
			}
			else if ( (checkin_from_now_in_millis >= stunde) && (checkin_from_now_in_millis <= tag) )
			{
				radius_checkin = map(checkin_from_now_in_millis, stunde, tag, (bezugsgroesse/2)/3, (bezugsgroesse/3) );
			}
			else if ( (checkin_from_now_in_millis >= tag) && (checkin_from_now_in_millis <= woche) )
			{
				radius_checkin = map(checkin_from_now_in_millis, tag, woche, (bezugsgroesse/3), (bezugsgroesse/2) );
			}
			else
			{
				radius_checkin = bezugsgroesse/2;
			}

			float radius_checkout;
			if ( (checkout_from_now_in_millis >= 0) && (checkout_from_now_in_millis < stunde) )
			{
				radius_checkout = map(checkout_from_now_in_millis, 0, stunde, 0, (bezugsgroesse/2)/3);
			}
			else if ( (checkout_from_now_in_millis >= stunde) && (checkout_from_now_in_millis <= 86400000) )
			{
				radius_checkout = map(checkout_from_now_in_millis, stunde, 86400000, (bezugsgroesse/2)/3, (bezugsgroesse/3) );
			}
			else if ( (checkout_from_now_in_millis >= 86400000) && (checkout_from_now_in_millis <= 604800000) )
			{
				radius_checkout = map(checkout_from_now_in_millis, 86400000, 604800000, (bezugsgroesse/3), (bezugsgroesse/2) );
			}
			else
			{
				radius_checkout = bezugsgroesse/2;
			}
			
//			System.out.println("faktor: "+faktor);
//
//			System.out.println("log(woche): "+log(604800000));
//			System.out.println("bezugsgroesse: "+bezugsgroesse);
//			System.out.println("checkin_from_now_in_millis: "+checkin_from_now_in_millis);
//			System.out.println("log(checkin_from_now): "+log(checkin_from_now_in_millis));
//			System.out.println("checkout_from_now_in_millis: "+checkout_from_now_in_millis);
//			System.out.println("radius_checkin: "+radius_checkin);
//			System.out.println("radius_checkout: "+radius_checkout);
			
			// instanzlinien einfaerben nach exitcode
			stroke(93, 134, 77);
			if ((entity.getExitcode().matches("")) || (entity.getExitcode().matches("0")) )
			{
				// dunkelgruen
				stroke(93, 134, 77);
			}
			else
			{
				// dunkelrot
				stroke(186, 55, 55);
			}
			
			// dunkelgrau
			fill(50);
			// dicke
			strokeWeight(bezugsgroesse/300);
			
			// x-koordinate berechnen
			randomSeed(checkin.getTimeInMillis());
			float zufall = random(0, 2*PI);
//			float zufall = map(zaehler, 0, anzahl_entities, 0, 2*PI);
//			zaehler++;
			float x_checkin = (center_x) + cos(zufall) * radius_checkin;
			float y_checkin = (center_y) + sin(zufall) * radius_checkin;
			float x_checkout = (center_x) + cos(zufall) * radius_checkout;
			float y_checkout = (center_y) + sin(zufall) * radius_checkout;
//			System.out.println("x_checkin: "+x_checkin);
//			System.out.println("y_checkin: "+y_checkin);
//			System.out.println("x_checkout: "+x_checkout);
//			System.out.println("y_checkout: "+y_checkout);
			
			ellipse(x_checkin, y_checkin, bezugsgroesse/200, bezugsgroesse/200);
			ellipse(x_checkout, y_checkout, bezugsgroesse/200, bezugsgroesse/200);
			line(x_checkin, y_checkin, x_checkout, y_checkout);

			// berechnen des aktuellen abstands von mauszeiger zur instanz
			float abstand = calc_abstand(x_checkin, y_checkin, x_checkout, y_checkout);
			if (abstand < kleinster_abstand)
			{
				kleinster_abstand = abstand;
				entity_mit_fahne = entity;
			}
			
		}
		
		// fahne zeichnen
		if ((kleinster_abstand < keine_fahne_ab_abstand_mehr_als) && (entity_mit_fahne != null))
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
		text("process:  "+entity_mit_fahne.getProcess(), mouseX+6, mouseY-60);
		text("user:     "+entity_mit_fahne.getUser(),    mouseX+6, mouseY-50);
		text("host:     "+entity_mit_fahne.getHost(),    mouseX+6, mouseY-40);
		text("checkin:  "+entity_mit_fahne.getCheckinAsString(), mouseX+6, mouseY-30);
		text("checkout: "+entity_mit_fahne.getCheckoutAsString(),mouseX+6, mouseY-20);
		text("exitcode: "+entity_mit_fahne.getExitcode(),mouseX+6, mouseY-10);
	}
	
	float calc_abstand(float x_checkin, float y_checkin, float x_checkout, float y_checkout)
	{
		float abstand_mouse_zu_checkin = dist(mouseX, mouseY, x_checkin, y_checkin);
		float abstand_mouse_zu_checkout = dist(mouseX, mouseY, x_checkout, y_checkout);
		float abstand_checkin_zu_checkout = dist(x_checkin, y_checkin, x_checkout, y_checkout);
		
		float winkel_zw_mouse_und_checkin = acos( (abstand_mouse_zu_checkin * abstand_mouse_zu_checkin - abstand_checkin_zu_checkout * abstand_checkin_zu_checkout - abstand_mouse_zu_checkout * abstand_mouse_zu_checkout)/(-2 * abstand_checkin_zu_checkout * abstand_mouse_zu_checkout));
		float winkel_zw_mouse_und_checkout = acos( (abstand_mouse_zu_checkout * abstand_mouse_zu_checkout - abstand_checkin_zu_checkout * abstand_checkin_zu_checkout - abstand_mouse_zu_checkin * abstand_mouse_zu_checkin)/(-2 * abstand_checkin_zu_checkout * abstand_mouse_zu_checkin));
		
		
		float abstand_zu_strecke = abstand_mouse_zu_checkin * (sin(winkel_zw_mouse_und_checkout));
		
		float abstand = min(abstand_zu_strecke, abstand_mouse_zu_checkin, abstand_mouse_zu_checkout);
		
		// wenn einer der winkel groesser als 90grad ist, soll der abstand zur geraden nicht beruecksichtigt werden
		if ((winkel_zw_mouse_und_checkin > 0.5 * PI) || (winkel_zw_mouse_und_checkout > 0.5 * PI) )
		{
			abstand = min(abstand_mouse_zu_checkin, abstand_mouse_zu_checkout);
		}
		
		return abstand;
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
			System.out.println("refresh interval must not be less than "+(this.min_refresh_interval/1000)+" seconds.");
			
		}
	}

	void filter(Entity entity_filter)
	{
		// daten holen aus db
//		System.out.println("filtering data...");
//		System.out.println("id: "+entity_filter.getId());
//		System.out.println("process: "+entity_filter.getProcess());
//		System.out.println("user: "+entity_filter.getUser());
//		System.out.println("host: "+entity_filter.getHost());
//		System.out.println("active: "+entity_filter.getActive());
//		System.out.println("----------------");
		this.matched_entities = entity_filter.getAllMatches(this.all_entities);
	}
	
	void legend()
	{
		stroke(100);
		textSize(13);
		fill(this.legendcolor[0], this.legendcolor[1], this.legendcolor[2]);
//		text(this.legend_processstatus, this.legendposition[0]+this.legendsize/2, this.legendposition[1]+6*this.legendsize+0);
//		text((int)(((this.refresh_next.getTimeInMillis() - this.now.getTimeInMillis())/1000)+1), this.legendsize/2, frame.getHeight() -40);
//		text(((int)(this.frameRate)), frame.getWidth() - this.legendsize/2 - this.legendsize*3, frame.getHeight() - 40);
		text((int)(((this.refresh_next.getTimeInMillis() - this.now.getTimeInMillis())/1000)), 5, height-5);
		text("automation@caegroup.de", width-180, height-5);
		noFill();
	}
	
	void setFilter (Entity entity_filter)
	{
		this.entity_filter = entity_filter;
		System.out.println("setting new filter");
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
		String aufruf = "nedit "+entity_mit_fahne.getResource();
//		System.out.println("entity_mit_fahne: "+entity_mit_fahne.getId());
		System.out.println("showing resource: "+aufruf);
		
		if (kleinster_abstand < keine_fahne_ab_abstand_mehr_als)
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
		mouse_last_pressed = now;
		mouse_pressed_x = mouseX;
		mouse_pressed_y = mouseY;
	}
	
	public void mouseDragged()
	{
//		System.out.println("Ja mouse dragged: mouse_pressed_x="+mouse_pressed_x+"  || mouseX="+mouseX+" || center_x="+center_x);
		int new_center_x = (int)(this.center_ratio_x * width) + (mouseX-mouse_pressed_x);
		int new_center_y = (int)(this.center_ratio_y * height) + (mouseY-mouse_pressed_y);
		
		double new_center_ratio_x = ((double) new_center_x / (double)width);
		double new_center_ratio_y = ((double) new_center_y / (double)height);
		
//		System.out.println("new_center_x= "+new_center_x+"    width="+width+"   || new_center_factor_x="+new_center_factor_x);
		
		if		(new_center_ratio_x < 0)		{this.center_ratio_x = 0;}
		else if	(new_center_ratio_x > 1)		{this.center_ratio_x = 1;}
		else									{this.center_ratio_x = new_center_ratio_x;}

		if		(new_center_ratio_y < 0)		{this.center_ratio_y = 0;}
		else if	(new_center_ratio_y > 1)		{this.center_ratio_y = 1;}
		else									{this.center_ratio_y = new_center_ratio_y;}

		mouse_pressed_x = mouseX;
		mouse_pressed_y = mouseY;
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

	public PradarViewProcessing()
	{
		this.entity_filter = new Entity();
		this.einstellungen = new PradarViewModel();
	}
	
	public PradarViewProcessing(Entity entity)
	{
		this.entity_filter = entity;
		this.einstellungen = new PradarViewModel();
	}

	public PradarViewProcessing(Entity entity, PradarViewModel einstellungen)
	{
		this.entity_filter = entity;
		this.einstellungen = einstellungen;
	}


}
