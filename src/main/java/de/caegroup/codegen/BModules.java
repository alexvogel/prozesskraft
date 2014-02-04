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
	Block block = new Block();

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
	public ArrayList<String> getBlock()
	{
		return this.block.getCode();
	}

	public void genCode(String type)
	{
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("bla"))
		{
			
		}
		// default
		else
		{
			content.add("use warnings;");
			content.add("use strict;");
			content.add("use Getopt::Long;");
			content.add("Getopt::Long::Configure(\"pass_through\");");
			content.add("use File::Spec;");
			content.add("use Cwd;");
		}
		
		this.block.setOrigin("auto");
		this.block.setBlockname("modules");
		this.block.setCode(content);
	}
}