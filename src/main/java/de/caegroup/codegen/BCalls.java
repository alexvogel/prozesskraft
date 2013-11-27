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
	ArrayList<Option> option = new ArrayList<Option>();
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
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
		
		ArrayList<String> content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		
		ArrayList<String> block = new ArrayList<String>();
		block.addAll(this.parent.genBlockStart("calls"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("calls"));
		
		return block;
	}

	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("if (${$OPT{'help'}})");
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

		return content;
	}
}