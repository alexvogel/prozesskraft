package de.caegroup.pradar.parts;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import processing.core.PApplet;

import de.caegroup.pradar.Entity;

public class PradarViewProcessingEntity
{

	/*----------------------------
	  FIELDS
	----------------------------*/
	private final double gravityConst = 6.673 * Math.pow(10, -11);

	private String superid;
	private String parent_superid = null;
	private float checkin_radius;
	private float[] checkin_position = {0, 0};
	private float checkout_radius;
	private float[] checkout_position = {0, 0};
//	private int[] color = {255,255,255};

	public ArrayList<PradarViewProcessingEntity> children_pentities = new ArrayList<PradarViewProcessingEntity>();
	
	private float speed = 0;
//	private float maxspeed = 50;
	private float mass = (float)1;
	private float gravity = (float)0.01;
	private float spring = 10;
	private float damp = (float)0.1;
	
	private boolean fixPosition = false;

//	long jahrInMillis   = 14515200000L;
//	long monatInMillis  = 2419200000L;
//	long wocheInMillis  = 604800000;
//	long tagInMillis    = 86400000;
//	long stundeInMillis = 3600000;
//	
	long lastTimePositionCalcInMillis = System.currentTimeMillis();
	
	float bogenlaenge = 0;
	float repositionBogenlaenge = 0;
	
	private PradarViewProcessingPage parent;
	private PradarViewProcessingEntity pentity_parent;
	private Entity entity;
	/*----------------------------
	  constructors
	----------------------------*/

	public PradarViewProcessingEntity(PradarViewProcessingPage page_parent, PradarViewProcessingEntity pentity_parent, Entity entity)
	{
		this.parent = page_parent;
		this.pentity_parent = pentity_parent;
		this.superid = entity.getSuperid();
		this.entity = entity;
		Entity tmp_entity_filter = new Entity();
		this.setInitialPosition();

		// aus allen entities die kinder entities des vorliegenden entity feststellen
		tmp_entity_filter.setParentid(this.entity.getId());
		ArrayList<Entity> children_entities = tmp_entity_filter.getAllMatches(this.parent.parent.entities_all);

		// fuer jede der kinder-entity eine pentity erzeugen und unter children_pentities ablegen
		Iterator<Entity> iter_entity = children_entities.iterator();
		while(iter_entity.hasNext())
		{
			Entity children_entity = iter_entity.next();
			PradarViewProcessingEntity newProcessingEntity = new PradarViewProcessingEntity(this.parent, this, children_entity);
			this.children_pentities.add(newProcessingEntity);
		}

		
//		System.out.println("superId: "+this.superid+"  |  bogenlaenge: "+this.bogenlaenge);
//		this.detColor();
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
			// dunkelgruen
			color[0] = 215;
			color[1] = 135;
			color[2] = 0;
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
	
//	public void detNewPosition()
//	{
//		Iterator<Entity> iterEntity = this.parent.matched_parent_entities.iterator();
//		while (iterEntity.hasNext())
//		{
//		
//		}
//	}
	
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
			this.bogenlaenge = this.pentity_parent.bogenlaenge;
		}

		// position berechnen
		calcPosition();
	}
	
	public void calcPosition()
	{
		this.checkin_radius = this.parent.calcRadius(this.parent.parent.getEntityBySuperId(this.superid).getCheckin());
//		System.out.println("Radius checkin: "+this.checkin_radius);
		this.checkin_position[0] = (this.parent.center_x) + PApplet.cos(this.bogenlaenge) * this.checkin_radius;
		this.checkin_position[1] = (this.parent.center_y) + PApplet.sin(this.bogenlaenge) * this.checkin_radius;
		this.checkout_radius = this.parent.calcRadius(this.parent.parent.getEntityBySuperId(this.superid).getCheckout());
//		System.out.println("Radius checkout: "+this.checkout_radius);
		this.checkout_position[0] = (this.parent.center_x) + PApplet.cos(this.bogenlaenge) * this.checkout_radius;
		this.checkout_position[1] = (this.parent.center_y) + PApplet.sin(this.bogenlaenge) * this.checkout_radius;
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
			
			// eine liste mit allen parent-pentities und all ihren children anlegen
			ArrayList<PradarViewProcessingEntity> alle_pentities_parents_und_children = this.parent.pentities_filtered;
			
			for(int x=0; x<this.parent.pentities_filtered.size(); x++)
			{
				PradarViewProcessingEntity pentity_nur_parent = this.parent.pentities_filtered.get(x);
				for(int y=0; y<pentity_nur_parent.children_pentities.size(); y++)
				{
					PradarViewProcessingEntity children_des_parent = pentity_nur_parent.children_pentities.get(y);
					alle_pentities_parents_und_children.add(children_des_parent);
				}
			}
			
			double GravityPulsSum = 0;
			
			Iterator<PradarViewProcessingEntity> iter_alle_pentities_parents_und_children = alle_pentities_parents_und_children.iterator();
			while (iter_alle_pentities_parents_und_children.hasNext())
			{
				PradarViewProcessingEntity pentity = iter_alle_pentities_parents_und_children.next();

				// nicht beruecksichtigen, wenn du selber
				if (pentity.equals(this))
				{
					continue;
				}

				// Gravity berechnen, falls es das parent ist
				if (pentity.equals(this.pentity_parent))
				{
					float abstandRechtsdrehend1 = (this.bogenlaenge - pentity.bogenlaenge);
					float abstandRechtsdrehend2 = (float) (((2*Math.PI) - Math.abs(abstandRechtsdrehend1)) * (abstandRechtsdrehend1/Math.abs(abstandRechtsdrehend1) * (-1)));
					
//					System.out.println("abstand1: "+abstandRechtsdrehend1);
//					System.out.println("abstand2: "+abstandRechtsdrehend2);
					
					if ( Math.abs(abstandRechtsdrehend1) < Math.abs(abstandRechtsdrehend2) )
					{
						GravityPulsSum = GravityPulsSum + calGravitypuls(abstandRechtsdrehend1);
					}
					else
					{
						GravityPulsSum = GravityPulsSum + calGravitypuls(abstandRechtsdrehend2);
					}
				}

				// auf jeden Fall die Abstossung berechnen
				float abstandRechtsdrehend1 = (this.bogenlaenge - pentity.bogenlaenge);
				float abstandRechtsdrehend2 = (float) (((2*Math.PI) - Math.abs(abstandRechtsdrehend1)) * (abstandRechtsdrehend1/Math.abs(abstandRechtsdrehend1) * (-1)));
				
				if ( Math.abs(abstandRechtsdrehend1) < Math.abs(abstandRechtsdrehend2) )
				{
					GravityPulsSum = GravityPulsSum + calAntigravitypuls(abstandRechtsdrehend1);
				}
				else
				{
					GravityPulsSum = GravityPulsSum + calAntigravitypuls(abstandRechtsdrehend2);
				}
			}
			
//			System.out.println("antiGravityPulsSum: "+antiGravityPulsSum);
			float speeddiff = (float) GravityPulsSum / this.mass;
//			System.out.println("speeddiff: "+speeddiff);
			float oldspeed = this.speed;
//			System.out.println("oldspeed: "+oldspeed);
			float newspeed = (oldspeed + speeddiff) * (1-this.damp);
//			System.out.println("newspeed: "+newspeed);
			
			this.speed = newspeed;
			
			float repositionBogenlaenge = (float) (newspeed * timediff * 0.001);
			
			this.repositionBogenlaenge = repositionBogenlaenge;
			
//			System.out.println("bogenlaenge bisher: "+this.bogenlaenge);
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
	}
	
	private double calAntigravitypuls(double distance)
	{
		double antigravitypuls = 0;
//		System.out.println("distance am Anfang: "+distance);
		
		// mindestabstand um extreme geschwindigkeiten zu vermeiden
		if		( (distance >= 0) && (distance < 0.001) ) {distance =  0.001;}
		else if ( (distance < 0)  && (distance > -0.001) ){distance = -0.001;}
		else if (distance > Math.PI) {distance = Math.PI;}
		else if (distance < -Math.PI) {distance = -Math.PI;}
		
//		System.out.println("distance nach nachbehandlung: "+distance);
		// antigravitation nimmt mit dem quadrat des abstands ab
		
		antigravitypuls = (distance / (Math.pow((10*distance),2))) * this.gravity;
//			float antigravitypulsMap = PApplet.map((float)antigravitypuls, (float)-10, (float)10, (float)-0.01, (float)0.01);

		if (Double.isNaN(antigravitypuls)) {antigravitypuls = 0.1;}
		System.out.println("antigravitypuls: "+antigravitypuls);
		return antigravitypuls;
	}
	
	private double gravity(float mass1, float mass2, float distance)
	{
		double gravity = gravityConst * ((mass1 * mass2) / Math.pow(distance, 2)); 
		return gravity;
	}
	
	private double antiGravity(float mass1, float mass2, float distance)
	{
		double antiGravity = (-1) * gravity(mass1, mass2, distance);
		return antiGravity;
	}
	
	private double calGravitypuls(double distance)
	{
		double gravityPuls = (-1) * 0.1/calAntigravitypuls(distance);
//		System.out.println("GRAVITYPULS: "+gravityPuls);
		return gravityPuls;
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
		if (true)
		{
			this.parent.strokeWeight(this.parent.bezugsgroesse/300);
			this.parent.stroke(getColor("r"), getColor("g"), getColor("b"));
			this.parent.ellipse( this.checkin_position[0], this.checkin_position[1], (this.parent.bezugsgroesse/200), (this.parent.bezugsgroesse/200) );
			this.parent.ellipse( this.checkout_position[0], this.checkout_position[1], (this.parent.bezugsgroesse/200), (this.parent.bezugsgroesse/200) );
			this.parent.line(this.checkin_position[0], this.checkin_position[1], this.checkout_position[0], this.checkout_position[1]);

			// wenn es einen parent gibt, sollen die verbinder zu ihm gezeichnet werden
			if (this.pentity_parent != null)
			{
	//			System.out.println("children are drawn");
				this.parent.strokeWeight(1);
				this.parent.stroke(100);
				this.parent.line(this.pentity_parent.checkin_position[0], this.pentity_parent.checkin_position[1], this.checkin_position[0], this.checkin_position[1]);
	//			System.out.println(this.pentity_parent.checkin_position[0]+" | "+this.pentity_parent.checkin_position[1]+"  |  "+this.checkin_position[0]+"  |  "+this.checkin_position[1]);
				this.parent.line(this.pentity_parent.checkout_position[0], this.pentity_parent.checkout_position[1], this.checkout_position[0], this.checkout_position[1]);
			}

		}
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
	public float getBogenlaenge()
	{
		return this.bogenlaenge;
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
	

	
	
	
	
	
}
