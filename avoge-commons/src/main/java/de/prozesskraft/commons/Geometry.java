package de.prozesskraft.commons;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;

public class Geometry

{
	/*----------------------------
	  structure
	----------------------------*/


	/*----------------------------
	  constructors
	----------------------------*/
	public Geometry()
	{

	}

	/*----------------------------
	  methods
	----------------------------*/

	/*----------------------------
	 static  methods
	----------------------------*/
	
	static public Double areaTriangle(Double x1, Double y1, Double z1, Double x2, Double y2, Double z2, Double x3, Double y3, Double z3)
	{
		//berechnunge der seitenlaengen a=[12], b=[23], c=[31]
		Double a = Math.sqrt(Math.pow((x2-x1),2) + Math.pow((y2-y1),2) + Math.pow((z2-z1),2));
		Double b = Math.sqrt(Math.pow((x3-x2),2) + Math.pow((y3-y2),2) + Math.pow((z3-z2),2));
		Double c = Math.sqrt(Math.pow((x1-x3),2) + Math.pow((y1-y3),2) + Math.pow((z1-z3),2));

		//s=halber Umfang
		Double s = (a+b+c)/2;

		//Flaeche
		Double flaeche = Math.sqrt(s*(s-a)*(s-b)*(s-c));

	    return flaeche;
	}
	
	static public Double distance(Double x1, Double y1, Double z1, Double x2, Double y2, Double z2)
	{
		Double dx = x1 - x2;
		Double dy = y1 - y2;
		Double dz = z1 - z2;
		
		return(Math.sqrt((dx * dx) + (dy * dy) + (dz * dz)));
	}
	
	/**
	 * returns the arithmetical average of Double values
	 * @param values
	 * @return
	 */
	static public Double average(Double[] values)
	{
		Double sum = 0d;
		
		for(Double actValue : values)
		{
			sum += actValue;
		}
		
		return sum/values.length;
	}
	
	/**
	 * returns the volume of a tetra
	 * @param values
	 * @return
	 */
	static public Double volumeTetra(Double x1, Double y1, Double z1, Double x2, Double y2, Double z2, Double x3, Double y3, Double z3, Double x4, Double y4, Double z4)
	{
		return (Math.abs(determinant_3x3(vectorSubtract(x1, y1, z1, x2, y2, z2), vectorSubtract(x2, y2, z2, x3, y3, z3), vectorSubtract(x3, y3, z3, x4, y4, z4))) / 6);
	}
	
	static public Double[] vectorSubtract(Double x1, Double y1, Double z1, Double x2, Double y2, Double z2)
	{
		Double[] subtract = new Double[3];
		
		subtract[0] = x1 - x2;
		subtract[1] = y1 - y2;
		subtract[2] = z1 - z2;
		
		return subtract;
	}

	static public Double determinant_3x3(Double[] zero, Double[] one, Double[] two)
	{
		return (zero[0] * (one[1] * two[2] - one[2] * two[1]) -
				one[0] * (zero[1] * two[2] - zero[2] * two[1]) +
				two[0] * (zero[1] * one[2] - zero[2] * one[1]));
	}
	
	
	/*----------------------------
	  methods getter & setter
	----------------------------*/


}
