package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BChecks
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	Script parent = null;
	ArrayList<Option> option = new ArrayList<Option>();
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BChecks(Script parent)
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
		block.addAll(this.parent.genBlockStart("checks"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("checks"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("my $error_param_mandatory;");
		content.add("my $error;");
		content.add("");
		content.add("if ($error)");
		content.add("{");
		content.add("\tlogit('info', 'while checking for existance of files at least one fatal error occured. exit.');");
		content.add("\texit(1);");
		content.add("}");

		return content;
	}
}