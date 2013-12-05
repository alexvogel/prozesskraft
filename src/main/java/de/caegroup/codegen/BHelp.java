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
		content.add("$helptext .= \"usage: $filename PARAMETER\\n\";");
		content.add("$helptext .= \"\\n\";");
		content.add("$helptext .= \"Parameter\\n\";");
		content.add("");
		content.add("foreach(sort keys %OPTHELP)");
		content.add("{");
		content.add("	$helptext .= sprintf (\" --%s%s\", $_, ${$OPTHELP{$_}}{'text1'});");
		content.add("	");
		content.add("	if (${$OPTHELP{$_}}{'mandatory'})");
		content.add("	{");
		content.add("		$helptext .= \" (mandatory\";");
		content.add("	}");
		content.add("	else");
		content.add("	{");
		content.add("		$helptext .= \" (optional\";");
		content.add("	}");
		content.add("	");
		content.add("	if (${$OPTHELP{$_}}{'default'})");
		content.add("	{");
		content.add("		$helptext .= \", default: \" . ${$OPTHELP{$_}}{'default'} . \")\";");
		content.add("	}");
		content.add("	else");
		content.add("	{");
		content.add("		$helptext .= \")\";");
		content.add("	}");
		content.add("	$helptext .= \"\\n\";");
		content.add("	");
		content.add("	# helptext nach ca. 60 Zeichen umbrechen");
		content.add("	${$OPTHELP{$_}}{'text2'} =~ s/(.{60}[^\\s]*)\\s+/$1\\n/g;");
		content.add("	");
		content.add("	foreach(split (\"\\n\", ${$OPTHELP{$_}}{'text2'}))");
		content.add("	{");
		content.add("		$helptext .= sprintf (\"       %s\\n\",  $_);");
		content.add("	}");
		content.add("	$helptext .= \"\\n\";");
		content.add("}");

		content.add("$helptext .= \"author: alexander.vogel\\@caegroup.de | version: $version | date: $date\\n\";");

		return content;
	}
}