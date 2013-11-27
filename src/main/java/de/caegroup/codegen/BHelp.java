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
		block.addAll(this.parent.genBlockStart("help"));
		block.add("# md5="+md5);
		block.add(this.parent.trenner);
		block.addAll(content);
		block.add(this.parent.trenner);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("help"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("my $helptext;");
		content.add("");
		content.add("$helptext .= 'usage: $filename PARAMETER\\n';");
		content.add("$helptext .= '\\n';");
		content.add("$helptext .= 'Parameter\n';");
		content.add("");
		content.add("foreach(sort keys %OPTHELP)");
		content.add("{");
		content.add("\t$helptext .= sprintf (' --%s%s\\n', $_, ${$OPTHELP{$_}}{'text1'});");
		content.add("");
		content.add("\t# helptext nach ca. 60 Zeichen umbrechen");
		content.add("\t${$OPTHELP{$_}}{'text2'} =~ s/(.{60}[^\\s]*)\\s+/$1\\n/g;");
		content.add("");
		content.add("\tforeach(split ('\n', ${$OPTHELP{$_}}{'text2'}))");
		content.add("\t{");
		content.add("\t\t$helptext .= sprintf ('       %s\\n',  $_);");
		content.add("\t}");
		content.add("\t$helptext .= '\n';");
		content.add("}");
		content.add("");
		content.add("$helptext .= '\n';");
		content.add("$helptext .= 'example: $filename --scope model --submodel_vat f34_vat.nas --submodel_rk f34_rk.nas';");
		content.add("$helptext .= '\n';");
		content.add("$helptext .= 'author: alexander.vogel\\@caegroup.de | version: $version | date: $date';");

		return content;
	}
}