package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BMeta
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	Script parent = null;
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 * constructs a BMeta
	 */
	public BMeta(Script parent)
	{
		this.parent = parent;
	}

	/*----------------------------
	  methods 
	----------------------------*/
	public ArrayList<String> getBlock()
	{
		
		ArrayList<String> content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		
		ArrayList<String> block = new ArrayList<String>();
		block.add(this.parent.genBlockStart("checks"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.add(this.parent.genBlockEnd("checks"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("my $version = \"[% version %]\");");
		content.add("my $date = \"[% date %]\");");
		
		return content;
	}
}