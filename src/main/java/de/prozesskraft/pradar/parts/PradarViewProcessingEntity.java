package de.prozesskraft.pradar.parts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;

import processing.core.PApplet;

import de.prozesskraft.pradar.Entity;

public class PradarViewProcessingEntity
{

	/*----------------------------
	  FIELDS
	----------------------------*/
	private final double gravityConst = 6.673 * Math.pow(10, -11);

	private String id;
	private String superid;
	private String parent_superid = null;
	private float checkin_radius;
	private float[] checkin_position = {0, 0};
	private float checkout_radius;
	private float[] checkout_position = {0, 0};
//	private int[] color = {255,255,255};

	public ArrayList<PradarViewProcessingEntity> children_pentities = new ArrayList<PradarViewProcessingEntity>();
	
	private double speed = 0;
//	private float maxspeed = 50;
	private double mass;
	private float gravity = (float)0.01;
	private float spring = 10;
	private float damp = (float)0.15;
	
	private boolean fixPosition = false;

//	long jahrInMillis   = 14515200000L;
//	long monatInMillis  = 2419200000L;
//	long wocheInMillis  = 604800000;
//	long tagInMillis    = 86400000;
//	long stundeInMillis = 3600000;
//	
	long lastTimePositionCalcInMillis = System.currentTimeMillis();
	
	double bogenlaenge = 0;
	float repositionBogenlaenge = 0;
	
	private PradarViewProcessingPage parent;
	private PradarViewProcessingEntity pentity_parent = null;
	
	Random generator = new Random();
	private Entity entity;
	/*----------------------------
	  constructors
	----------------------------*/

	public PradarViewProcessingEntity(PradarViewProcessingPage page_parent, Entity entity)
	{
		this.parent = page_parent;
		this.superid = entity.getSuperid();
		this.id = entity.getId();
		
		this.entity = entity;

		// falls es einen eintrag im parent-id gibt, soll das parent mit abgelegt werden
		if (!( (entity.getParentid().equals("")) || (entity.getParentid().equals("0")) ))
		{
			this.pentity_parent = getParentPentity(this, this.parent.pentities_filtered);
			this.mass = 100;
		}
		
		else
		{
			this.pentity_parent = null;
			this.mass = 1000;
		}

		this.setInitialPosition();
	}
	
	/*----------------------------
	  METHODS
	----------------------------*/
	public int getColor(String string)
	{
		int[] color = {255,255,255};
		if ( this.parent.parent.getEntityBySuperId(this.superid).getExitcode().matches("0") )
		{
			// dunkelgruen
			color[0] = 93;
			color[1] = 135;
			color[2] = 77;
		}
		else if ( this.parent.parent.getEntityBySuperId(this.superid).getExitcode().matches("") )
		{
//			// orange
//			color[0] = 215;
//			color[1] = 135;
//			color[2] = 0;
			// dunkelgruen
			color[0] = 93;
			color[1] = 135;
			color[2] = 77;
		}
		else
		{
			// dunkelrot
			color[0] = 186;
			color[1] = 55;
			color[2] = 55;
		}
		
		if (string.equals("r"))
		{
			return color[0];
		}
		else if (string.equals("g"))
		{
			return color[1];
		}
		return color[2];
	}	
	
	public PradarViewProcessingEntity getPentityById(String id, ArrayList<PradarViewProcessingEntity> pentities)
	{
		PradarViewProcessingEntity pentity = null;
		for(PradarViewProcessingEntity actualPentity : pentities)
		{
			if (actualPentity.getId().equals(id))
			{
				return actualPentity;
			}
		}
		return pentity;
	}
	
	public PradarViewProcessingEntity getPentityBySuperId(String superid, ArrayList<PradarViewProcessingEntity> pentities)
	{
		PradarViewProcessingEntity pentity = null;
		for(PradarViewProcessingEntity actualPentity : pentities)
		{
			if (actualPentity.getSuperid().equals(superid))
			{
				return actualPentity;
			}
		}
		return pentity;
	}
	
	private PradarViewProcessingEntity getParentPentity(PradarViewProcessingEntity child, ArrayList<PradarViewProcessingEntity> pentities)
	{
		PradarViewProcessingEntity pentity = null;
		for(PradarViewProcessingEntity actualPentity : pentities)
		{
			if ((actualPentity.getEntity().getId().equals(child.getEntity().getParentid())) && (actualPentity.getEntity().getHost().equals(child.getEntity().getHost()))  && (actualPentity.getEntity().getUser().equals(child.getEntity().getUser())) )
			{
				return actualPentity;
			}
		}
		return pentity;
	}
	
	public void setInitialPosition()
	{
		// wenn es KEINEN parent gibt, so soll die initiale position per Zufall (mit checkin-Zeit als Seed) bestimmt werden
		if (this.pentity_parent == null)
		{
			// Berechnen des ersten Punktes
			Calendar checkin = this.parent.parent.getEntityBySuperId(this.superid).getCheckin();
			Calendar checkout = this.parent.parent.getEntityBySuperId(this.superid).getCheckout();
			Calendar now = Calendar.getInstance();
			if (checkout.getTimeInMillis() == 0)
			{
				checkout = now;
			}
	
			// initiale bogenlaenge berechnen
			this.parent.randomSeed(checkin.getTimeInMillis());
			this.bogenlaenge = this.parent.random((float)0, (float)(2 * PApplet.PI) );
		}
		
		// wenn es EINEN parent gibt, so soll die initiale position die gleiche sein, wie die des parents
		else
		{
			int faktor = generator.nextInt();
			if (faktor < 0)
			{
				this.bogenlaenge =  this.pentity_parent.bogenlaenge - 0.01;
			}
			else
			{
				this.bogenlaenge =  this.pentity_parent.bogenlaenge + 0.01;
			}
		}

		// position berechnen
		calcPosition();
	}
	
	public void calcPosition()
	{
		// wenn es diese nicht gibt
		Entity entity = this.parent.parent.getEntityBySuperId(this.superid);
		if (entity == null) {return;}
		
		this.checkin_radius = this.parent.calcRadius(this.parent.parent.getEntityBySuperId(this.superid).getCheckin());
//		System.out.println("Radius checkin: "+this.checkin_radius);
		this.checkin_position[0] = (this.parent.center_x) + PApplet.cos((float)this.bogenlaenge) * this.checkin_radius;
		this.checkin_position[1] = (this.parent.center_y) + PApplet.sin((float)this.bogenlaenge) * this.checkin_radius;
		this.checkout_radius = this.parent.calcRadius(this.parent.parent.getEntityBySuperId(this.superid).getCheckout());
//		System.out.println("Radius checkout: "+this.checkout_radius);
		this.checkout_position[0] = (this.parent.center_x) + PApplet.cos((float)this.bogenlaenge) * this.checkout_radius;
		this.checkout_position[1] = (this.parent.center_y) + PApplet.sin((float)this.bogenlaenge) * this.checkout_radius;
//		System.out.println("1=>"+this.checkin_position[0]+" "+this.checkin_position[1]+" "+this.checkout_position[0]+" "+this.checkout_position[1]);
	}
	
	public void calcNewBogenlaenge()
	{
		if ( (this.fixPosition)  )
		{
			// mache nix
		}
		
		else
		{
			long now = System.currentTimeMillis();
			long timediff = now - this.lastTimePositionCalcInMillis;
			this.lastTimePositionCalcInMillis = now;
			
			// alle pentities durchgehen und die gravity/antigravity, die durch diese erzeugt wird aufaddieren
			double forceSum = 0;
//			System.out.println("Ich bin Pentity: "+this.getId());
			for(int x=0; x<this.parent.pentities_filtered.size(); x++)
			{
				PradarViewProcessingEntity pentity = this.parent.pentities_filtered.get(x);
//				System.out.println("--------------------------------------");
//				System.out.println("Berechnen der Kraefte zum Pentity: "+pentity.getId());
				
				// wenn es sich um sich selber handelt -> ueberspringen
				if (pentity.equals(this)) {break;}
				
				double forceZugFederRechtsdrehend = 0;
				double forceZugFederLinksdrehend  = 0;
				double forceStossFederRechtsdrehend = 0;
				double forceStossFederLinksdrehend  = 0;
				
//				System.out.println("Abstand rechtsDrehend: "+this.abstandRechtsdrehend(pentity));
//				System.out.println("Abstand linksDrehend : "+this.abstandLinksdrehend(pentity));
				// wenn es der parent ist, dann soll gravity berechnet werden
				if (pentity.equals(this.pentity_parent))
				{
//					System.out.println("Its my parent: "+pentity.getId()+" == "+this.pentity_parent.getId());
					forceZugFederRechtsdrehend = forceZugFeder(this.mass, pentity.mass, this.abstandLinksdrehend(pentity));
					forceZugFederLinksdrehend  = forceZugFeder(this.mass, pentity.mass, this.abstandRechtsdrehend(pentity));
//					if (forceZugFederRechtsdrehend > forceZugFederLinksdrehend) {forceZugFederLinksdrehend = 0;}
//					else {forceZugFederRechtsdrehend = 0;}
				}
				
				// bei jedem pentity, dass sich mit dem aktuellen ueberschneidet, soll antiGravity berechnet werden
				if (
						(
								(pentity.getEntity().getCheckinInMillis() >= this.getEntity().getCheckinInMillis())   &&
								(pentity.getEntity().getCheckinInMillis() <= this.getEntity().getCheckoutInMillis())
						)  ||
						(
								(pentity.getEntity().getCheckoutInMillis() <= this.getEntity().getCheckoutInMillis())  &&
								(pentity.getEntity().getCheckoutInMillis() >= this.getEntity().getCheckinInMillis())
						)  ||
						(
								(pentity.getEntity().getCheckoutInMillis() == this.getEntity().getCheckoutInMillis())
						)
					)
				{
					forceStossFederRechtsdrehend = forceStossFeder(this.mass, pentity.mass, this.abstandRechtsdrehend(pentity));
					forceStossFederLinksdrehend  = forceStossFeder(this.mass, pentity.mass, this.abstandLinksdrehend(pentity));
//					if (forceStossFederRechtsdrehend > forceStossFederLinksdrehend) {forceStossFederLinksdrehend = 0;}
//					else {forceStossFederRechtsdrehend = 0;}
//					System.out.println("forceZugFederRechtsdrehend  : "+forceZugFederRechtsdrehend);
//					System.out.println("forceZugFederLinksdrehend   : "+forceZugFederLinksdrehend);
//					System.out.println("forceStossFederRechtsdrehend: "+forceStossFederRechtsdrehend);
//					System.out.println("forceStossFederLinksdrehend : "+forceStossFederLinksdrehend);
//					System.out.println("--------------------------------------");
				}
				
				// summe aller Gravities bilden (rechts=positiv, links=negativ, anti=negativ)
				double forceRechtsDrehend = forceZugFederRechtsdrehend + forceStossFederLinksdrehend;
				double forceLinksDrehend  = forceZugFederLinksdrehend  + forceStossFederRechtsdrehend;
				forceSum += forceRechtsDrehend - forceLinksDrehend;
//				System.out.println("SUMME der FORCES: "+forceSum);
			}
			
//			System.out.println("SUMME ALLER GRAVITIES: "+forceSum);
			
//			System.out.println("antiGravityPulsSum: "+antiGravityPulsSum);
			double speeddiff = (forceSum / this.mass);
//			System.out.println("speeddiff: "+speeddiff);
			double oldspeed = this.speed;
//			System.out.println("oldspeed: "+oldspeed);
			double newspeed = (oldspeed + speeddiff) * (1-this.damp);
//			System.out.println("newspeed: "+newspeed);
						
			this.speed = newspeed;
			if (this.speed < -0.05) {this.speed = -0.05;}
			else if (this.speed > 0.05) {this.speed = 0.05;}
//			System.out.println("speed is "+this.speed);
			
			this.bogenlaenge += this.speed;
			
			
//			System.out.println("SPEED: "+this.speed);
//			System.out.println("BOGENLAENGE: "+this.bogenlaenge);
//			System.out.println("========================================");
			
		}
	}
	private double abstandLinksdrehend(PradarViewProcessingEntity pentity)
	{
		double abstand = this.bogenlaenge - pentity.bogenlaenge;
		int anzahl_der_vollkreise = Math.abs((int) (abstand / (Math.PI * 2)));
		if (anzahl_der_vollkreise > 0)
		{
			abstand = abstand - (anzahl_der_vollkreise *  (Math.PI * 2));
		}

		if (abstand < 0)
		{
			abstand = (Math.PI * 2) + abstand;
		}
		return abstand;
	}

	private double abstandRechtsdrehend(PradarViewProcessingEntity pentity)
	{
		return (Math.PI * 2) - abstandLinksdrehend(pentity);
	}

	
//	private double calAntigravitypuls(double distance)
//	{
//		double antigravitypuls = 0;
////		System.out.println("distance am Anfang: "+distance);
//		
//		// mindestabstand um extreme geschwindigkeiten zu vermeiden
//		if		( (distance >= 0) && (distance < 0.001) ) {distance =  0.001;}
//		else if ( (distance < 0)  && (distance > -0.001) ){distance = -0.001;}
//		else if (distance > Math.PI) {distance = Math.PI;}
//		else if (distance < -Math.PI) {distance = -Math.PI;}
//		
////		System.out.println("distance nach nachbehandlung: "+distance);
//		// antigravitation nimmt mit dem quadrat des abstands ab
//		
//		antigravitypuls = (distance / (Math.pow((10*distance),2))) * this.gravity;
////			float antigravitypulsMap = PApplet.map((float)antigravitypuls, (float)-10, (float)10, (float)-0.01, (float)0.01);
//
//		if (Double.isNaN(antigravitypuls)) {antigravitypuls = 0.1;}
//		System.out.println("antigravitypuls: "+antigravitypuls);
//		return antigravitypuls;
//	}
//	

	private double forceGravity(double mass1, double mass2, double distance)
	{
		double gravity = gravityConst * ((mass1 * mass2) / Math.pow(distance, 2)); 
		return gravity;
	}
	
	private double forceZugFeder(double mass1, double mass2, double distance)
	{
		if (distance < 0.1) { distance = 0.1; }
		double force = 0.0000001 * (mass1 * mass2) * Math.pow(distance, 2); 
		return (force);
	}
	
	private double forceStossFeder(double mass1, double mass2, double distance)
	{
		if (distance < 0.01) { distance = 0.01; }
		double force = 0.00000001 * (mass1 * mass2) * Math.pow((1/distance), 2);
		return (force);
	}
	
//	private double calGravitypuls(double distance)
//	{
//		double gravityPuls = (-1) * 0.1/calAntigravitypuls(distance);
////		System.out.println("GRAVITYPULS: "+gravityPuls);
//		return gravityPuls;
//	}
	
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
	
//	public void detVisible()
//	{
//		if (this.pentity_parent == null)
//		{
//			this.visible = true;
//		}
//		else if ((this.pentity_parent != null) && this.parent.parent.einstellungen.getChildren() )
//		{
//			// falls es bisher unsichtbar war, soll die position beim einschalten auf die von parent gesetzt werden
//			if (!(this.visible))
//			{
//				this.bogenlaenge = this.pentity_parent.bogenlaenge+(float)0.01;
//				this.speed = 0;
//				this.calcPosition();
//			}
//			this.visible = true;
//		}
//		else
//		{
//			this.visible = false;
//		}		
//	}
//	
	public void draw()
	{
//		detVisible();
//		System.out.println("Schalter fuer children: "+this.parent.parent.einstellungen.getChildren());
//		System.out.println("SuperId   :             "+this.superid);
//		System.out.println("Visibility:             "+this.visible);
//		System.out.println("Pentity "+this.getId()+"  |  bogenlaenge:             "+this.bogenlaenge);
		
		if (true)
		{
			int groesse;
			if ((this.parent.bezugsgroesse/300) > 4) {groesse = 4;}
			else {groesse = (this.parent.bezugsgroesse/300);}
			this.parent.strokeWeight(groesse);

			this.parent.stroke(getColor("r"), getColor("g"), getColor("b"));
			this.parent.fill(getColor("r"), getColor("g"), getColor("b"));

			// wenn es die markierte entity ist, soll die groesse neu definiert werden
			if (this.entity.equals(this.parent.parent.einstellungen.entitySelected))
			{
//				this.parent.strokeWeight(groesse/2);
				this.parent.fill(255,255,255);
			}
			
			// kreis soll fein umrandet sein und weiss gefuellt
			this.parent.fill(255,255,255);
			this.parent.strokeWeight(1);

			// wenn kreis markiert ist, soll er mit seiner farbe gefuellt sein
			if (this.entity.equals(this.parent.parent.einstellungen.entitySelected))
			{
				this.parent.fill(getColor("r"), getColor("g"), getColor("b"));
			}
			
			this.parent.ellipse( this.checkin_position[0], this.checkin_position[1], (float)(groesse*3), (float)(groesse*3) );
			this.parent.ellipse( this.checkout_position[0], this.checkout_position[1], (float)(groesse*3), (float)(groesse*3) );
			
			// linie soll sie duenn sein
			this.parent.strokeWeight(groesse/2);
			// wenn linie markiert ist, soll sie doppelt so dick sein
			if (this.entity.equals(this.parent.parent.einstellungen.entitySelected))
			{
				this.parent.strokeWeight(groesse);
			}
			this.parent.line(this.checkin_position[0], this.checkin_position[1], this.checkout_position[0], this.checkout_position[1]);

			// wenn es einen parent gibt, sollen die verbinder zu ihm gezeichnet werden
			if (this.pentity_parent != null)
			{
	//			System.out.println("children are drawn");
				this.parent.strokeWeight(1);
				this.parent.stroke(100);
				this.parent.line((this.pentity_parent.checkin_position[0]+this.pentity_parent.checkout_position[0])/2, (this.pentity_parent.checkin_position[1]+this.pentity_parent.checkout_position[1])/2, this.checkin_position[0], this.checkin_position[1]);
	//			System.out.println(this.pentity_parent.checkin_position[0]+" | "+this.pentity_parent.checkin_position[1]+"  |  "+this.checkin_position[0]+"  |  "+this.checkin_position[1]);
				this.parent.line((this.pentity_parent.checkin_position[0]+this.pentity_parent.checkout_position[0])/2, (this.pentity_parent.checkin_position[1]+this.pentity_parent.checkout_position[1])/2, this.checkout_position[0], this.checkout_position[1]);
			}

		}
	}
	
	/*----------------------------
	  GETTER / SETTER
	----------------------------*/

	
	
	/**
	 * @return the id
	 */
	public String getId()
	{
		return this.id;
	}

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

//	/**
//	 * @return the color
//	 */
//	public int[] getColor()
//	{
//		return this.color;
//	}
//
//	/**
//	 * @param color the color to set
//	 */
//	public void setColor(int[] color)
//	{
//		this.color = color;
//	}
//

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
	public double getBogenlaenge()
	{
		return this.bogenlaenge;
	}

	/**
	 * @return the entity
	 */
	public Entity getEntity()
	{
		return this.entity;
	}

	/**
	 * @return the children_pentities
	 */
	public ArrayList<PradarViewProcessingEntity> getChildrenPentities()
	{
		return this.children_pentities;
	}

	/**
	 * @param bogenlaenge the bogenlaenge to set
	 */
	public void setBogenlaenge(float bogenlaenge)
	{
		this.bogenlaenge = bogenlaenge;
	}

	/**
	 * @return the fixPosition
	 */
	public boolean isFixPosition()
	{
		return this.fixPosition;
	}

	/**
	 * @param fixPosition the fixPosition to set
	 */
	public void setFixPosition(boolean fixPosition)
	{
		this.fixPosition = fixPosition;
	}

	/**
	 * @return the pentity_parent
	 */
	public PradarViewProcessingEntity getPentity_parent() {
		return pentity_parent;
	}

	/**
	 * @param pentity_parent the pentity_parent to set
	 */
	public void setPentity_parent(PradarViewProcessingEntity pentity_parent) {
		this.pentity_parent = pentity_parent;
	}
	

	
	
	
	
	
}
