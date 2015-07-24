package de.prozesskraft.codegen;

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
	int reihenfolge = 5;
	int minoccur = 0;
	int maxoccur = 0;
	String definition = "string";  // string | flag | integer | float
	String check = "";
	String def = ""; // default-value for this option
	String text1 = "";
	String text2 = "";
	Boolean allowIntegratedListIfMultiOption = false;     // wenn maxoccur > 1, dann darf einem parameter eine kommaseparierte liste mitgegeben werden
	
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
		content.add("				'" + this.name + "' => {'reihenfolge' =>'" + this.reihenfolge + "', 'minoccur' => '" + this.minoccur + "', 'maxoccur' => '" + this.maxoccur + "', 'definition' => '" + this.definition + "', 'check' => '" + this.check + "', 'default' => '" + this.def + "', 'allowIntegratedListIfMultiOption' => '" + this.allowIntegratedListIfMultiOption.toString() + "', 'text1' => \"" + this.text1 + "\", 'text2' => \"" + this.text2 + "\"},");
		return content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the reihenfolge
	 */
	public int getReihenfolge() {
		return reihenfolge;
	}

	/**
	 * @param reihenfolge the reihenfolge to set
	 */
	public void setReihenfolge(int reihenfolge) {
		this.reihenfolge = reihenfolge;
	}

	public int getMinoccur() {
		return minoccur;
	}

	public void setMinoccur(int minoccur) {
		this.minoccur = minoccur;
	}

	public int getMaxoccur() {
		return maxoccur;
	}

	public void setMaxoccur(int maxoccur) {
		this.maxoccur = maxoccur;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public String getDef() {
		return def;
	}

	public void setDef(String def) {
		this.def = def;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	/**
	 * @return the allowIntegratedListIfMultiOption
	 */
	public Boolean getAllowIntegratedListIfMultiOption() {
		return allowIntegratedListIfMultiOption;
	}

	/**
	 * @param allowIntegratedListIfMultiOption the allowIntegratedListIfMultiOption to set
	 */
	public void setAllowIntegratedListIfMultiOption(
			Boolean allowIntegratedListIfMultiOption) {
		this.allowIntegratedListIfMultiOption = allowIntegratedListIfMultiOption;
	}
	
	
}