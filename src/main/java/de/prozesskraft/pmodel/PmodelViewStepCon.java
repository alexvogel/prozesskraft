package de.prozesskraft.pmodel;

import processing.core.*;

public class PmodelViewStepCon
{
	/*----------------------------
	  structure
	----------------------------*/

	private String name = new String();
	private int[] color = {0,0,0};
	private float thickness = 0;
	private float[] positionfrom = new float[3];
	private float[] positionto = new float[3];
	private double arrowpos = 0.50;
	
	private PmodelViewStepSym stepcirclefrom = new PmodelViewStepSym();
	private PmodelViewStepSym stepcircleto = new PmodelViewStepSym();
	
    private PmodelViewPage parent;
    private boolean nochvorhanden = true;	// bei jedem durchlauf wird geprueft ob fuer den stepcircle noch ein step existiert.


	/*----------------------------
	  constructors
	----------------------------*/
	public PmodelViewStepCon(PmodelViewPage p, PmodelViewStepSym stepcircle_from, PmodelViewStepSym stepcircle_to)
	{
		this.parent = p;
		
		this.stepcirclefrom = stepcircle_from;
		this.stepcircleto = stepcircle_to;
		
		this.thickness = (float)1.0;

		this.setPositionfrom(stepcirclefrom.getPosition());
		this.setPositionto(stepcircleto.getPosition());
		
		this.setName(stepcirclefrom.getName()+stepcircleto.getName());
		
	
	}


	/*----------------------------
	  methods
	----------------------------*/
	public void display()
	{
//		parent.strokeWeight(this.getThickness());
//		parent.line(this.getPositionfrom1(), this.getPositionfrom2(), this.getPositionto1(), this.getPositionto2());

		
		parent.strokeWeight(this.getThickness());
		parent.stroke(getColor1(), getColor2(), getColor3());
		parent.fill(this.getColor1(), this.getColor2(), this.getColor3());
		this.arrowLine(this.getDrawPositionfrom1(), this.getDrawPositionfrom2(), this.getDrawPositionto1(), this.getDrawPositionto2(), 0, (float)0.5, true);
//		System.out.println("line "+this.positionfrom[0]+" "+this.positionfrom[1]+" "+this.positionto[0]+" "+this.positionto[1]);
//		System.out.println("line "+this.getPositionfrom1()+" "+this.getPositionfrom2()+" "+this.getPositionto1()+" "+this.getPositionto2());
//		System.exit(0);

		this.setPositionfrom(stepcirclefrom.getPosition());
		this.setPositionto(stepcircleto.getPosition());
	}
	
	/*
	 * Draws a lines with arrows of the given angles at the ends.
	 * x0 - starting x-coordinate of line
	 * y0 - starting y-coordinate of line
	 * x1 - ending x-coordinate of line
	 * y1 - ending y-coordinate of line
	 * startAngle - angle of arrow at start of line (in radians)
	 * endAngle - angle of arrow at end of line (in radians)
	 * solid - true for a solid arrow; false for an "open" arrow
	 */
	void arrowLine(float x0, float y0, float x1, float y1, float startAngle, float endAngle, boolean solid)
	{
	  parent.line(x0, y0, x1, y1);
	  
	  float p0x = (float)((x1-x0)*this.arrowpos)+x0;
	  float p0y = (float)((y1-y0)*this.arrowpos)+y0;
	  float p1x = x1-(float)((x1-x0)*this.arrowpos);
	  float p1y = y1-(float)((y1-y0)*this.arrowpos);
	  
	  
	  
	  if (startAngle != 0)
	  {
	    arrowhead(p0x, p0y, PApplet.atan2(y1 - y0, x1 - x0), startAngle, solid, (float)this.parent.einstellungen.getZoom()/100);
	  }
	  if (endAngle != 0)
	  {
	    arrowhead(p1x, p1y, PApplet.atan2(y0 - y1, x0 - x1), endAngle, solid, (float)this.parent.einstellungen.getZoom()/100);
	  }
	}
	 
	/*
	 * Draws an arrow head at given location
	 * x0 - arrow vertex x-coordinate
	 * y0 - arrow vertex y-coordinate
	 * lineAngle - angle of line leading to vertex (radians)
	 * arrowAngle - angle between arrow and line (radians)
	 * solid - true for a solid arrow, false for an "open" arrow
	 */
	void arrowhead(float x0, float y0, float lineAngle, float arrowAngle, boolean solid, float zoom)
	{
//		float phi;
		
		
		float x2;
		float y2;
		float x3;
		float y3;
		final float SIZE = 8 * this.parent.bezugsgroesse * zoom;
	   
		x2 = (float)(x0 + SIZE * PApplet.cos(lineAngle + arrowAngle));
		y2 = (float)(y0 + SIZE * PApplet.sin(lineAngle + arrowAngle));
		x3 = (float)(x0 + SIZE * PApplet.cos(lineAngle - arrowAngle));
		y3 = (float)(y0 + SIZE * PApplet.sin(lineAngle - arrowAngle));
		if (solid)
		{
			parent.triangle(x0, y0, x2, y2, x3, y3);
		}
		else
		{
			parent.line(x0, y0, x2, y2);
			parent.line(x0, y0, x3, y3);
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

	public float getThickness()
	{
		return this.thickness;
	}

	public float[] getPositionfrom()
	{
		return this.positionfrom;
	}

	public float getPositionfrom1()
	{
		return this.positionfrom[0];
	}

	public float getPositionfrom2()
	{
		return this.positionfrom[1];
	}

	public float getPositionfrom3()
	{
		return this.positionfrom[2];
	}

	public float getDrawPositionfrom1()
	{
		return (this.positionfrom[0] - this.parent.getWidth()/2) * ((float)this.parent.einstellungen.getZoom()/100) + this.parent.getWidth()/2;
	}

	public float getDrawPositionfrom2()
	{
		return (this.positionfrom[1] - this.parent.getHeight()/2) * ((float)this.parent.einstellungen.getZoom()/100) + this.parent.getHeight()/2;
	}

	public float[] getPositionto()
	{
		return this.positionto;
	}

	public float getPositionto1()
	{
		return this.positionto[0];
	}

	public float getPositionto2()
	{
		return this.positionto[1];
	}

	public float getPositionto3()
	{
		return this.positionto[2];
	}

	public float getDrawPositionto1()
	{
		return (this.positionto[0] - this.parent.getWidth()/2) * ((float)this.parent.einstellungen.getZoom()/100) + this.parent.getWidth()/2;
	}

	public float getDrawPositionto2()
	{
		return (this.positionto[1] - this.parent.getHeight()/2) * ((float)this.parent.einstellungen.getZoom()/100) + this.parent.getHeight()/2;
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

	public void setPositionfrom(float[] position)
	{
		this.positionfrom[0] = position[0];
		this.positionfrom[1] = position[1];
		this.positionfrom[2] = position[2];
	}

	public void setPositionfrom(float position1, float position2, float position3)
	{
		this.positionfrom[0] = position1;
		this.positionfrom[1] = position2;
		this.positionfrom[2] = position2;
	}

	public void setPositionfrom1(float position1)
	{
		this.positionfrom[0] = position1;
	}

	public void setPositionfrom2(float position2)
	{
		this.positionfrom[1] = position2;
	}

	public void setPositionfrom3(float position3)
	{
		this.positionfrom[2] = position3;
	}

	public void setPositionto(float[] position)
	{
		this.positionto[0] = position[0];
		this.positionto[1] = position[1];
		this.positionto[2] = position[2];
	}

	public void setPositionto(float position1, float position2, float position3)
	{
		this.positionto[0] = position1;
		this.positionto[1] = position2;
		this.positionto[2] = position2;
	}

	public void setPositionto1(float position1)
	{
		this.positionto[0] = position1;
	}

	public void setPositionto2(float position2)
	{
		this.positionto[1] = position2;
	}

	public void setPositionto3(float position3)
	{
		this.positionto[2] = position3;
	}

	public boolean isNochvorhanden()
	{
		return nochvorhanden;
	}

	public void setNochvorhanden(boolean nochvorhanden)
	{
		this.nochvorhanden = nochvorhanden;
	}


	/**
	 * @return the stepcirclefrom
	 */
	public PmodelViewStepSym getStepcirclefrom()
	{
		return this.stepcirclefrom;
	}


	/**
	 * @return the stepcircleto
	 */
	public PmodelViewStepSym getStepcircleto()
	{
		return this.stepcircleto;
	}


	/**
	 * @param stepcirclefrom the stepcirclefrom to set
	 */
	public void setStepcirclefrom(PmodelViewStepSym stepcirclefrom)
	{
		this.stepcirclefrom = stepcirclefrom;
	}


	/**
	 * @param stepcircleto the stepcircleto to set
	 */
	public void setStepcircleto(PmodelViewStepSym stepcircleto)
	{
		this.stepcircleto = stepcircleto;
	}


}
