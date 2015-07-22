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
	Block block = new Block();

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
			content.add("my $helptext;");
			content.add("");
			content.add("$helptext .= \"Description: "+ this.parent.getDescription() +"\\n\";");
			content.add("$helptext .= \"\\n\";");
			content.add("$helptext .= \"Usage: $filename PARAMETER\\n\";");
			content.add("$helptext .= \"\\n\";");
			content.add("$helptext .= \"Parameter\\n\";");
			content.add("");
			content.add("for(my $x=0; $x<30; $x++)");
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
			content.add("			# wenn der default==true und es sich um einen flag-parameter handelt");
			content.add("			if ( (${$OPTHELP{$_}}{'default'} eq \"1\") && (${$OPTIONS_TABLE{$_}}{'definition'} eq \"flag\") )");
			content.add("			{");
			content.add("				$helptext .= \", default: on)\";");
			content.add("			}");
			content.add("			# wenn der default==false und es sich um einen flag-parameter handelt");
			content.add("			elsif ( (${$OPTHELP{$_}}{'default'} eq \"0\") && (${$OPTIONS_TABLE{$_}}{'definition'} eq \"flag\") )");
			content.add("			{");
			content.add("				$helptext .= \", default: off)\";");
			content.add("			}");
			content.add("			# wenn es einen default gibt");
			content.add("			elsif ((exists ${$OPTHELP{$_}}{'textfordefault'}) && (${$OPTHELP{$_}}{'textfordefault'}))");
			content.add("			{");
			content.add("				$helptext .= \", default: \" . ${$OPTHELP{$_}}{'textfordefault'} . \")\";");
			content.add("			}");
			content.add("			elsif (${$OPTHELP{$_}}{'default'})");
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

			content.add("$helptext .= \"Author : "+this.parent.getAuthorMail()+"\\n\";");
			content.add("$helptext .= \"Date   : $date\\n\";");
			content.add("");
			content.add("if(&getOption('d'))");
			content.add("{");
			content.add("	$helptext .= \"Version: $version\\n\";");
			content.add("}");
			content.add("");
			content.add("$helptext .= \"\\n\";");
		}
		
		this.block.setOrigin("auto");
		this.block.setBlockname("help");
		this.block.setCode(content);
	}
}