package de.caegroup.pradar.parts;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Iterator;

import processing.core.PApplet;

import de.caegroup.pradar.Entity;

public class PradarViewProcessingEntity
{

	/*----------------------------
	  FIELDS
	----------------------------*/
	private String superid;
	private float checkin_radius;
	private float[] checkin_position = {0, 0};
	private float checkout_radius;
	private float[] checkout_position = {0, 0};
	private int[] color = {255,255,255};

	private Entity entity;
	
	private float speed = 0;
//	private float maxspeed = 50;
	private float mass = (float)1;
	private float gravity = (float)0.01;
	private float spring = 10;
	private float damp = (float)0.4;

	long jahrInMillis   = 14515200000L;
	long monatInMillis  = 2419200000L;
	long wocheInMillis  = 604800000;
	long tagInMillis    = 86400000;
	long stundeInMillis = 3600000;
	
	long lastTimePositionCalcInMillis = System.currentTimeMillis();
	
	float bogenlaenge = 0;
	
	private PradarViewProcessingPage parent;
	/*----------------------------
	  constructors
	----------------------------*/

	public PradarViewProcessingEntity(PradarViewProcessingPage p, Entity entity)
	{
		this.parent = p;
		this.entity = entity;
		this.superid = this.entity.getSuperid();
		this.setInitialPosition();
		this.detColor();
	}
	
//	public PradarViewProcessingEntity(Entity entity)
//	{
//		this.parent = new PradarViewProcessingPage();
//		this.entity = entity;
//		this.superid = this.entity.getSuperid();
//		this.setInitialPosition();
//		this.detColor();
//	}
//	
//	public PradarViewProcessingEntity()
//	{
//		this.parent = new PradarViewProcessingPage();
//		this.entity = new Entity();
//		this.superid = this.entity.getSuperid();
//		this.setInitialPosition();
//		this.detColor();
//	}

	/*----------------------------
	  METHODS
	----------------------------*/
	public void detColor()
	{
		if ( this.entity.getExitcode().matches("0") || this.entity.getExitcode().matches("") )
		{
			// dunkelgruen
			this.color[0] = 93;
			this.color[1] = 134;
			this.color[2] = 77;
		}
		else
		{
			// dunkelrot
			this.color[0] = 186;
			this.color[1] = 55;
			this.color[2] = 55;
		}
	}	
	
	public void detNewPosition()
	{
		Iterator<Entity> iterEntity = this.parent.matched_entities.iterator();
		while (iterEntity.hasNext())
		{
		
		}
	}
		
	public void setInitialPosition()
	{
		// Berechnen des ersten Punktes
		Calendar checkin = this.entity.getCheckin();
		Calendar checkout = this.entity.getCheckout();
		Calendar now = Calendar.getInstance();
		if (checkout.getTimeInMillis() == 0)
		{
			checkout = now;
		}

		// initiale bogenlaenge berechnen
		this.parent.randomSeed(checkin.getTimeInMillis());
		this.bogenlaenge = this.parent.random((float)0, (float)(2 * PApplet.PI) );

		// position berechnen
		calcPosition();
	}
	
	public void calcPosition()
	{
		this.checkin_radius = calcRadius(this.entity.getCheckin());
		System.out.println("Radius checkin: "+this.checkin_radius);
		this.checkin_position[0] = (this.parent.center_x) + PApplet.cos(this.bogenlaenge) * this.checkin_radius;
		this.checkin_position[1] = (this.parent.center_y) + PApplet.sin(this.bogenlaenge) * this.checkin_radius;
		this.checkout_radius = calcRadius(this.entity.getCheckout());
		System.out.println("Radius checkout: "+this.checkout_radius);
		this.checkout_position[0] = (this.parent.center_x) + PApplet.cos(this.bogenlaenge) * this.checkout_radius;
		this.checkout_position[1] = (this.parent.center_y) + PApplet.sin(this.bogenlaenge) * this.checkout_radius;
		System.out.println("1=>"+this.checkin_position[0]+" "+this.checkin_position[1]+" "+this.checkout_position[0]+" "+this.checkout_position[1]);
	}
	
	public void calcNewBogenlaenge()
	{
//		System.out.println("SuperId von ICH: "+this.getSuperid());
		long now = System.currentTimeMillis();
		long timediff = now - this.lastTimePositionCalcInMillis;
		
		double antiGravityPulsSum = 0;
		boolean theFirstHasAlreadyBeenSeen = false;
		
		Iterator<PradarViewProcessingEntity> iterpentity = this.parent.matched_processing_entities.iterator();
		while (iterpentity.hasNext())
		{
			PradarViewProcessingEntity pentity = iterpentity.next();
			if (!(theFirstHasAlreadyBeenSeen))
			{
				theFirstHasAlreadyBeenSeen = true;
				continue;
			}
			if (pentity.equals(this))
			{
				continue;
			}
			System.out.println("bogenlaenge ich: "+this.bogenlaenge);
			System.out.println("bogenlaenge pentity: "+pentity.bogenlaenge);
			float abstandRechtsdrehend1 = (this.bogenlaenge - pentity.bogenlaenge);
			float abstandRechtsdrehend2 = (float) (this.bogenlaenge - 2*Math.PI + pentity.bogenlaenge);
			
			System.out.println("abstand1: "+abstandRechtsdrehend1);
			System.out.println("abstand2: "+abstandRechtsdrehend2);
			
			if ( Math.abs(abstandRechtsdrehend1) < Math.abs(abstandRechtsdrehend2) )
			{
				antiGravityPulsSum = antiGravityPulsSum + calAntigravitypuls(abstandRechtsdrehend1);
			}
			else
			{
				antiGravityPulsSum = antiGravityPulsSum + calAntigravitypuls(abstandRechtsdrehend2);
			}
		}
		
		System.out.println("antiGravityPulsSum: "+antiGravityPulsSum);
		float speeddiff = (float) antiGravityPulsSum / this.mass;
		System.out.println("speeddiff: "+speeddiff);
		float oldspeed = this.speed;
		System.out.println("oldspeed: "+oldspeed);
		float newspeed = (oldspeed + speeddiff) * (1-this.damp);
		System.out.println("newspeed: "+newspeed);
		
		this.speed = newspeed;
		
		float repositionBogenlaenge = (float) (newspeed * timediff * 0.001);
		
		System.out.println("bogenlaenge bisher: "+this.bogenlaenge);
		this.bogenlaenge = this.bogenlaenge + repositionBogenlaenge;
//		this.bogenlaenge = (float) (this.bogenlaenge - ( (int)(this.bogenlaenge / Math.PI) ) * Math.PI);
		
		int faktor = (int) (this.bogenlaenge / (2*Math.PI));
		
		if (faktor > 0)
		{
			this.bogenlaenge = (float) (this.bogenlaenge - (faktor * (2*Math.PI)));
		}
		
		if (this.bogenlaenge < 0)
		{
			this.bogenlaenge = PApplet.map(this.bogenlaenge, (float)0, (float)-Math.PI, (float)(2*Math.PI), (float)0);
		}
		
		System.out.println("bogenlaenge neu: "+this.bogenlaenge);
	}
	
	private double calAntigravitypuls(double distance)
	{
		double antigravitypuls = 0;
		System.out.println("distance am Anfang: "+distance);
		
		// mindestabstand um extreme geschwindigkeiten zu vermeiden
		if		( (distance >= 0) && (distance < 0.001) ) {distance =  0.001;}
		else if ( (distance < 0)  && (distance > -0.001) ){distance = -0.001;}
		else if (distance > Math.PI) {distance = Math.PI;}
		else if (distance < -Math.PI) {distance = -Math.PI;}
		
		System.out.println("distance nach nachbehandlung: "+distance);
		// antigravitation nimmt mit dem quadrat des abstands ab
		
		antigravitypuls = (distance / (Math.pow((10*distance),3))) * this.gravity;
//			float antigravitypulsMap = PApplet.map((float)antigravitypuls, (float)-10, (float)10, (float)-0.01, (float)0.01);

		System.out.println("antigravitypuls: "+antigravitypuls);
		return antigravitypuls;

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
			radius = PApplet.map(zeitspanne, 0, this.stundeInMillis, 0, parent.radius_stunde);
		}
		else if ( (zeitspanne >= this.stundeInMillis) && (zeitspanne < this.tagInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.stundeInMillis, this.tagInMillis, parent.radius_stunde, parent.radius_tag );
		}
		else if ( (zeitspanne >= this.tagInMillis) && (zeitspanne < this.wocheInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.tagInMillis, this.wocheInMillis, parent.radius_tag, parent.radius_woche );
		}
		else if ( (zeitspanne >= this.wocheInMillis) && (zeitspanne < this.monatInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.wocheInMillis, this.monatInMillis, parent.radius_woche, parent.radius_monat );
		}
		else if ( (zeitspanne >= this.monatInMillis) && (zeitspanne < this.jahrInMillis) )
		{
			radius = PApplet.map(zeitspanne, this.monatInMillis, this.jahrInMillis, parent.radius_monat, parent.radius_jahr );
		}
		else
		{
			radius = parent.radius_jahr;
		}
		System.out.println("zeitspanne "+zeitspanne+" bedeutet radius "+radius+" bedeutet "+new Timestamp(zeitpunkt.getTimeInMillis()).toString());
		return radius;
	}
	
	float calcDistToMouse()
	{
		float abstand_mouse_zu_checkin = PApplet.dist(this.parent.mouseX, this.parent.mouseY, this.checkin_position[0], this.checkin_position[1]);
		float abstand_mouse_zu_checkout = PApplet.dist(this.parent.mouseX, this.parent.mouseY, this.checkout_position[0], this.checkout_position[1]);
		float abstand_checkin_zu_checkout = PApplet.dist(this.checkin_position[0], this.checkin_position[1], this.checkout_position[0], this.checkout_position[1]);
		
		float winkel_zw_mouse_und_checkin = PApplet.acos( (abstand_mouse_zu_checkin * abstand_mouse_zu_checkin - abstand_checkin_zu_checkout * abstand_checkin_zu_checkout - abstand_mouse_zu_checkout * abstand_mouse_zu_checkout)/(-2 * abstand_checkin_zu_checkout * abstand_mouse_zu_checkout));
		float winkel_zw_mouse_und_checkout = PApplet.acos( (abstand_mouse_zu_checkout * abstand_mouse_zu_checkout - abstand_checkin_zu_checkout * abstand_checkin_zu_checkout - abstand_mouse_zu_checkin * abstand_mouse_zu_checkin)/(-2 * abstand_checkin_zu_checkout * abstand_mouse_zu_checkin));
		
		
		float abstand_zu_strecke = abstand_mouse_zu_checkin * (PApplet.sin(winkel_zw_mouse_und_checkout));
		
		float abstand = PApplet.min(abstand_zu_strecke, abstand_mouse_zu_checkin, abstand_mouse_zu_checkout);
		
		// wenn einer der winkel groesser als 90grad ist, soll der abstand zur geraden nicht beruecksichtigt werden
		if ((winkel_zw_mouse_und_checkin > 0.5 * PApplet.PI) || (winkel_zw_mouse_und_checkout > 0.5 * PApplet.PI) )
		{
			abstand = PApplet.min(abstand_mouse_zu_checkin, abstand_mouse_zu_checkout);
		}
		return abstand;
	}
	
	public void draw()
	{
		this.parent.strokeWeight(this.parent.bezugsgroesse/300);
		this.parent.stroke(this.color[0], this.color[1], this.color[2]);
		this.parent.ellipse( this.checkin_position[0], this.checkin_position[1], (this.parent.bezugsgroesse/200), (this.parent.bezugsgroesse/200) );
		this.parent.ellipse( this.checkout_position[0], this.checkout_position[1], (this.parent.bezugsgroesse/200), (this.parent.bezugsgroesse/200) );
		this.parent.line(this.checkin_position[0], this.checkin_position[1], this.checkout_position[0], this.checkout_position[1]);
		this.parent.text(this.bogenlaenge, this.checkin_position[0], this.checkin_position[1]);
	}
	
	/*----------------------------
	  GETTER / SETTER
	----------------------------*/

	
	
	/**
	 * @return the superid
	 */
	public String getSuperid()
	{
		return this.superid;
	}

	/**
	 * @param superid the superid to set
	 */
	public void setSuperid(String superid)
	{
		this.superid = superid;
	}

	/**
	 * @return the checkin_radius
	 */
	public float getCheckin_radius()
	{
		return this.checkin_radius;
	}

	/**
	 * @param checkin_radius the checkin_radius to set
	 */
	public void setCheckin_radius(int checkin_radius)
	{
		this.checkin_radius = checkin_radius;
	}

	/**
	 * @param checkout_radius the checkout_radius to set
	 */
	public void setCheckout_radius(int checkout_radius)
	{
		this.checkout_radius = checkout_radius;
	}

	/**
	 * @return the color
	 */
	public int[] getColor()
	{
		return this.color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(int[] color)
	{
		this.color = color;
	}

	/**
	 * @return the entity
	 */
	public Entity getEntity()
	{
		return this.entity;
	}

	/**
	 * @param entity the entity to set
	 */
	public void setEntity(Entity entity)
	{
		this.entity = entity;
	}

	/**
	 * @return the parent
	 */
	public PradarViewProcessingPage getParent()
	{
		return this.parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(PradarViewProcessingPage parent)
	{
		this.parent = parent;
	}

	/**
	 * @return the bogenlaenge
	 */
	public float getBogenlaenge()
	{
		return this.bogenlaenge;
	}

	/**
	 * @param bogenlaenge the bogenlaenge to set
	 */
	public void setBogenlaenge(float bogenlaenge)
	{
		this.bogenlaenge = bogenlaenge;
	}
	

	
	
	
	
	
}
