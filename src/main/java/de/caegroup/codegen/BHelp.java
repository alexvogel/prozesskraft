package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BHelp
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
	public BHelp(Script parent)
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
		block.add(this.parent.genBlockStart("help"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.add(this.parent.genBlockEnd("help"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("my $helptext;\n");
		content.add("\n");
		content.add("$helptext .= 'usage: $filename PARAMETER\\n';\n");
		content.add("$helptext .= '\\n';\n");
		content.add("$helptext .= 'Parameter\n';\n");
		content.add("\n");
		content.add("foreach(sort keys %OPTHELP)\n");
		content.add("{\n");
		content.add("\t$helptext .= sprintf (' --%s%s\\n', $_, ${$OPTHELP{$_}}{'text1'});\n");
		content.add("\n");
		content.add("\t# helptext nach ca. 60 Zeichen umbrechen\n");
		content.add("\t${$OPTHELP{$_}}{'text2'} =~ s/(.{60}[^\\s]*)\\s+/$1\\n/g;\n");
		content.add("\n");
		content.add("\tforeach(split ('\n', ${$OPTHELP{$_}}{'text2'}))\n");
		content.add("\t{\n");
		content.add("\t\t$helptext .= sprintf ('       %s\\n',  $_);\n");
		content.add("\t}\n");
		content.add("\t$helptext .= '\\n';\n");
		content.add("}\n");
		content.add("\n");
		content.add("$helptext .= '\\n';\n");
		content.add("$helptext .= 'example: $filename --scope model --submodel_vat f34_vat.nas --submodel_rk f34_rk.nas\n';\\n");
		content.add("$helptext .= '\\n';\n");
		content.add("$helptext .= 'author: alexander.vogel\\@caegroup.de | version: $version | date: $date\\n';\n");

		return content;
	}
}