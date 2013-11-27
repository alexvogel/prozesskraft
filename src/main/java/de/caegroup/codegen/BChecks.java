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

		content.add("my $helptext;\n");
		content.add("my $error_param_mandatory;\n");
		content.add("my $error;\n");
		content.add("\n");
		content.add("foreach my $submodel ($FILES->getSubmodels())\n");
		content.add("{\n");
		content.add("\tforeach my $submodelitem (@{$OPT{$submodel}})\n");
		content.add("\t{\n");
		content.add("\t\tif (!(stat $submodelitem))\n");
		content.add("\t\t{\n");
		content.add("\t\t\tlogit('fatal', 'file not readable: ' . $submodelitem);\n");
		content.add("\t\t\t$error++;\n");
		content.add("\t\t}\n");
		content.add("\t}\n");
		content.add("}\n");
		content.add("\n");
		content.add("if ($error)\n");
		content.add("{\n");
		content.add("\tlogit('info', 'while checking for existance of files at least one fatal error occured. exit.');\n");
		content.add("\texit(1);\n");
		content.add("}\n");

		return content;
	}
}