package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BModules
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
	public BModules(Script parent)
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
		return this.parent.genBlockStart("modules") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("modules");
	}
	
	private String getContent()
	{
		String content = "";
		content      += "use warnings;";
		content      += "use strict;";
		content      += "use Getopt::Long;";
		content      += "use File::Spec;";
		content      += "use Cwd;";
		
		return content;
	}
}