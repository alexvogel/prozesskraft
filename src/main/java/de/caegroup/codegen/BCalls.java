package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BCalls
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
	public BCalls(Script parent)
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

	public void genCode(String type) {
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("bla"))
		{
			
		}
		// default
		else
		{
			content.add("if (${$OPT{'h'}})");
			content.add("{");
			content.add("\tprint STDERR $helptext;");
			content.add("\texit(0);");
			content.add("}");
			content.add("if (${$OPT{'doc'}})");
			content.add("{");
			content.add("\tif (stat $doc_path)");
			content.add("\t{");
			content.add("\t\tlogit('info', 'showing documentation');");
			content.add("\t\texec 'acroread ' . $doc_path;");
			content.add("\t}");
			content.add("\telse");
			content.add("\t{");
			content.add("\t\tlogit('info', 'no documentation installed ('.$doc_path.')');");
			content.add("\t}");
			content.add("\texit(0);");
			content.add("}");
		}
		
		this.block.setCode(content);
	}
}