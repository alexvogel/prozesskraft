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
	public String getBlock()
	{
		
		String content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		return this.parent.genBlockStart("calls") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("calls");
	}
	
	private String getContent()
	{
		String content = "";

		content      += "my $helptext;\n";
		content      += "if (${$OPT{'help'}})\n";
		content      += "{\n";
		content      += "\tprint STDERR $helptext;\n";
		content      += "\texit(0);\n";
		content      += "}\n";
		content      += "if (${$OPT{'doc'}})\n";
		content      += "{\n";
		content      += "\tif (stat $doc_path)\n";
		content      += "\t{\n";
		content      += "\t\tlogit('info', 'showing documentation');\n";
		content      += "\t\texec 'acroread ' . $doc_path;\n";
		content      += "\t}\n";
		content      += "\telse\n";
		content      += "\t{\n";
		content      += "\t\tlogit('info', 'no documentation installed ('.$doc_path.')');\n";
		content      += "\t}\n";
		content      += "\texit(0);\n";
		content      += "}\n";

		return content;
	}
}