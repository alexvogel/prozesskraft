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
	ArrayList<String> content = new ArrayList<String>();
	String type = "default";
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BHelp(Script parent)
	{
		this.parent = parent;
		this.genContent(type);
	}

	/*----------------------------
	  methods 
	----------------------------*/
	public ArrayList<String> getBlock()
	{
		ArrayList<String> content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		
		ArrayList<String> block = new ArrayList<String>();
		block.addAll(this.parent.genBlockStart("help", this.type, md5.toString()));
		block.addAll(content);
		block.addAll(this.parent.genBlockEnd("help"));
		
		return block;
	}
	
	public ArrayList<String> getContent() {
		return content;
	}

	public void setContent(ArrayList<String> content) {
		this.content = content;
		this.type = "manual";
	}
	
	public void addContent(ArrayList<String> content)
	{
		this.content.addAll(content);
		this.type = "manual";
	}
	
	public void genContent(String type) {
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("bla"))
		{
			
		}
		// default
		else
		{
			content.add("my $helptext;");
			content.add("");
			content.add("$helptext .= \"usage: $filename PARAMETER\\n\";");
			content.add("$helptext .= \"\\n\";");
			content.add("$helptext .= \"Parameter\\n\";");
			content.add("");
			content.add("for(my $x=0; $x<11; $x++)");
			content.add("{");
			content.add("	foreach(sort keys %OPTHELP)");
			content.add("	{	");
			content.add("		if ( ${$OPTIONS_TABLE{$_}}{'reihenfolge'} == $x )");
			content.add("		{");
			content.add("			$helptext .= sprintf (\" --%s%s\", $_, ${$OPTHELP{$_}}{'text1'});");
			content.add("	");
			content.add("			if (${$OPTHELP{$_}}{'mandatory'})");
			content.add("			{");
			content.add("				$helptext .= \" (mandatory\";");
			content.add("			}");
			content.add("			else");
			content.add("			{");
			content.add("				$helptext .= \" (optional\";");
			content.add("			}");
			content.add("	");
			content.add("			if (${$OPTHELP{$_}}{'default'})");
			content.add("			{");
			content.add("				$helptext .= \", default: \" . ${$OPTHELP{$_}}{'default'} . \")\";");
			content.add("			}");
			content.add("			else");
			content.add("			{");
			content.add("				$helptext .= \")\";");
			content.add("			}");
			content.add("			$helptext .= \"\\n\";");
			content.add("	");
			content.add("			# helptext nach ca. 66 Zeichen umbrechen");
			content.add("			${$OPTHELP{$_}}{'text2'} =~ s/(.{66}[^\\s]*)\\s+/$1\\n/g;");
			content.add("	");
			content.add("			foreach(split (\"\\n\", ${$OPTHELP{$_}}{'text2'}))");
			content.add("			{");
			content.add("				$helptext .= sprintf (\"       %s\\n\",  $_);");
			content.add("			}");
			content.add("			$helptext .= \"\\n\";");
			content.add("		}");
			content.add("	}");
			content.add("}");

			content.add("$helptext .= \"author: alexander.vogel\\@caegroup.de | version: $version | date: $date\\n\";");
		}
		
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		this.genContent(type);
	}
	

}