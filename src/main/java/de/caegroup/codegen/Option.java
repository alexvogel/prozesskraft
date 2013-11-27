package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class Option
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/
	static final long serialVersionUID = 1;
	String name = "";
	int minoccur = 0;
	int maxoccur = 0;
	String definition = "string";  // string | flag
	String check = "";
	String def = null;
	String text1 = "";
	String text2 = "";
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 * constructs an Option
	 */
	public Option()
	{
	}

	/*----------------------------
	  methods 
	----------------------------*/
	public ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();
		content.add("				'" + this.name + "' => {'minoccur' => '" + this.minoccur + ", 'maxoccur' => '" + this.maxoccur + ", 'definition' => '" + this.definition + ", 'check' => '" + this.check + ", 'default' => '" + this.def + ", 'text1' => '" + this.text1 + ", 'text2' => '" + this.text2 + "'},");
		return content;
	}
}