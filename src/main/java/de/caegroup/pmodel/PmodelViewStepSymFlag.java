package de.caegroup.pmodel;

//import java.io.*;
import java.sql.Timestamp;
import java.util.*;
import java.lang.Math;

import processing.core.PApplet;
//import org.apache.solr.common.util.NamedList;

import processing.core.PFont;
import de.caegroup.process.Step;

public class PmodelViewStepSymFlag
{
	/*----------------------------
	  structure
	----------------------------*/
	private PmodelViewStepSym parent;
	private Step step;
	
	private int grundgroesse = 15;
	private ArrayList<String> wrappedDescription = new ArrayList<String>();
	PFont fontCourier;
	
	public PmodelViewStepSymFlag(PmodelViewStepSym p, Step s)
	{
		this.parent = p;
		this.step = s;

		wrappedDescription = wrapDescription(this.step.getDescription(), grundgroesse * 2);
		
    	this.fontCourier = this.parent.parent.loadFont("Courier-48.vlw");
	}

	public void display()
	{
		float zoom = (float)this.parent.getParent().einstellungen.getZoom()/100;
		System.out.println("zoom: "+zoom);

		float textgroesse = (grundgroesse) * zoom;
		System.out.println("textgroesse: "+textgroesse);

		float stiftstaerkeNormal = 1 * zoom;
		System.out.println("stiftstaerkeNormal: "+stiftstaerkeNormal);

		float puffer =  textgroesse/4 * zoom;
		System.out.println("puffer: "+puffer);
		
		float zeilenHoehe =  textgroesse + puffer;
		System.out.println("zeilenHoehe: "+zeilenHoehe);
//		int flagHoehe = grundgroesse *  this.parent.getParent().einstellungen.getZoom()/100;

		float flagHoehe = (wrappedDescription.size() + 1) * zeilenHoehe + puffer;
		System.out.println("flagHoehe: "+flagHoehe);

		float flagBreite = grundgroesse * 18 * zoom;
		System.out.println("flagBreite: "+flagBreite);

		float flagPositionX = this.parent.parent.mouseX + flagBreite/2;
		System.out.println("flagPositionX: "+flagPositionX);
//		int flagPositionY = this.parent.parent.mouseY - (grundgroesse/2 * this.parent.getParent().einstellungen.getZoom()/100);

		float flagPositionY = this.parent.parent.mouseY - flagHoehe / 2;
		System.out.println("flagPositionY: "+flagPositionY);

		float initSchreibPositionX = this.parent.parent.mouseX + (6 * zoom);
		System.out.println("initSchreibPositionX: "+initSchreibPositionX);

		float initSchreibPositionY = this.parent.parent.mouseY - flagHoehe;
		System.out.println("initSchreibPositionY: "+initSchreibPositionY);

		float flagEckRundung = 5 * zoom;

		System.out.println("");
		
		int zeile = 1;

		// weiss
		this.parent.parent.stroke(100);
		this.parent.parent.fill(255);
		// dicke
		this.parent.parent.strokeWeight(stiftstaerkeNormal);
		this.parent.parent.rect(flagPositionX, flagPositionY, flagBreite, flagHoehe, flagEckRundung);

		this.parent.parent.fill(100);
		this.parent.parent.textFont(fontCourier, textgroesse);

		this.parent.parent.text(this.step.getName(), initSchreibPositionX, initSchreibPositionY + (zeilenHoehe * zeile++));
		
		for(String actZeile : this.wrappedDescription)
		{
			this.parent.parent.text(actZeile, initSchreibPositionX, initSchreibPositionY + (zeilenHoehe * zeile++));
		}
	}
	
	/**
	* macht aus einem langen string eine ArrayList von mehreren Strings, deren jeweilige Laenge einen Wert nicht ueberschreitet
	 */
	private ArrayList<String> wrapDescription(String description, int lineLengthCharacters)
	{
		ArrayList<String> wrappedDescription = new ArrayList<String>();
		wrappedDescription.add("");

		if(description == null)
		{
			return wrappedDescription;
		}

		String[] woerter = description.split(" ");
		
		for(String actWort : woerter)
		{
			if( (wrappedDescription.get(wrappedDescription.size()-1).length() + actWort.length() ) < lineLengthCharacters)
			{
				wrappedDescription.set(wrappedDescription.size()-1, wrappedDescription.get(wrappedDescription.size()-1) + " " + actWort);
			}
			else
			{
				wrappedDescription.add(actWort);
			}
		}

		return wrappedDescription;
	}
	
//	private void drawProgressRect(int length, int posX, int posY)
//	{
//		this.parent.parent.stroke(100);
//		this.parent.parent.fill(this.parent.getColor1(), this.parent.getColor1(), this.parent.getColor1());
//		this.parent.parent.rect(posX+52, posY+1, length * this.step.getProgress(), -10, 1);
//		this.parent.parent.fill(255,255,255);
//		this.parent.parent.rect(posX+52+(length*this.step.getProgress()), posY+1, length * (1-this.step.getProgress()), -10, 1);
//		this.parent.parent.fill(100);
//	}
	
//	private String wrapText(int maxLength, String text)
//	{
//		if (text.length() > maxLength)
//		{
//			return (text.substring(0, maxLength - 3) + "..");
//		}
//		return text;
//	}
	

	public PmodelViewStepSym getParent()
	{
		return this.parent;
	}


}
