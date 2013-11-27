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
	public String getBlock()
	{
		
		String content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		return this.parent.genBlockStart("checks") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("checks");
	}
	
	private String getContent()
	{
		String content = "";

		content      += "my $helptext;\n";
		content      += "my $error_param_mandatory;\n";
		content      += "my $error;\n";
		content      += "\n";
		content      += "foreach my $submodel ($FILES->getSubmodels())\n";
		content      += "{\n";
		content      += "\tforeach my $submodelitem (@{$OPT{$submodel}})\n";
		content      += "\t{\n";
		content      += "\t\tif (!(stat $submodelitem))\n";
		content      += "\t\t{\n";
		content      += "\t\t\tlogit('fatal', 'file not readable: ' . $submodelitem);\n";
		content      += "\t\t\t$error++;\n";
		content      += "\t\t}\n";
		content      += "\t}\n";
		content      += "}\n";
		content      += "\n";
		content      += "if ($error)\n";
		content      += "{\n";
		content      += "\tlogit('info', 'while checking for existance of files at least one fatal error occured. exit.');\n";
		content      += "\texit(1);\n";
		content      += "}\n";

		return content;
	}
}