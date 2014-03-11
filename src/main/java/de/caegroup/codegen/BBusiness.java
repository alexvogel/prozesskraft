package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BBusiness
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
	public BBusiness(Script parent)
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
		
		if(type.matches("process"))
		{
			content.add("# 1) copy all options which value stat as a path to a file or a directory --<key>=<path> as key=path to %FILE . Except for option like --submodel_<key>=<path> go as key=path to %FILE");
			content.add("# 2) copy all options which value does not stat as a path to a file or directory --<key>=<value> as key=value to %VARIABLE . Except for options like --variable_<key>=<value> go as key=value to %VARIABLE");
			content.add("				");
			content.add("my %FILE = getTypedOptions(\"file\");");
			content.add("my %VARIABLE = getTypedOptions(\"variable\");");
			content.add("");
		}
		
		// default
		else
		{
			content.add("#");
			content.add("# you may use getConfig(<key>), setConfig(<key>, <value>), getConfigKeys() to deal with data from the configfile if one exists.");
			content.add("# you may use getOption(<key>), setOption(<string>, <value>), getOptionKeys(), and addOption(<string>, <key>) to deal with data from the call-options.");
			content.add("# you may use @ARG for all unknown/unparsed options given. this makes writing wrapperscripts easier.");
			content.add("#");
			content.add("# place your business logic here.");
			content.add("#");
			content.add("\t&logit(\"info\", \"you are now in program $0\");");
			content.add("#");
		}

		this.block.setOrigin("auto");
		this.block.setBlockname("business");
		this.block.setCode(content);
	}
	
	public void addCode(ArrayList<String> code)
	{
		this.block.addCode(code);
	}
}