package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BOptions
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
	public BOptions(Script parent)
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
		block.addAll(this.parent.genBlockStart("options"));
		block.add("# type="+type);
		block.add("# md5="+md5);
		block.add(this.parent.trenner);
		block.addAll(content);
		block.add(this.parent.trenner);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("options"));
		
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
			content.add("my %OPT;");
			content.add("my %OPTHELP;");
			content.add("my %optionsall;");

			content.add("# defaultoptions");
			content.add("# hier koenne alle 'festen' optionen definiert werden");
			content.add("my %OPTIONS_TABLE = (");
			
			content.add("				'help' => {'reihenfolge' => '9', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'', 'default' => '', 'text1' => '', 'text2' => 'gibt diesen hilfetext aus'},");
			content.add("				'doc'  => {'reihenfolge' => '8', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'', 'default' => '', 'text1' => '', 'text2' => 'falls eine dokumentation existiert, wird diese angezeigt'},");
			content.add("				'log'  => {'reihenfolge' => '7', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'string', 'check'=>'', 'default' => '', 'text1' => '=FILE', 'text2' => 'statt auf STDERR koennen logging-ausgaben in eine datei umgeleitet werden'},");
			
			for(Option o: this.parent.option)
			{
				content.addAll(o.getContent());
			}
			
			content.add(");");

			content.add("# parsen und generieren eines hashs (%optionsall) zum erzeugen von optionen (Getopt::Long)");
			content.add("foreach my $par (keys %OPTIONS_TABLE)");
			content.add("{");
			content.add("	if ( ${$OPTIONS_TABLE{$par}}{'maxoccur'} > 1 )");
			content.add("	{");
			content.add("		my @tmp;");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'definition'} eq \"flag\")");
			content.add("		{");
			content.add("			$optionsall{$par} = \\@tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"string\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=s\"} = \\@tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"integer\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=i\"} = \\@tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"float\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=f\"} = \\@tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"file\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=s\"} = \\@tmp;");
			content.add("		}");
			content.add("		");
			content.add("		$OPT{$par} = \\@tmp;");
			content.add("		$OPTHELP{$par} = {'text1' => ${$OPTIONS_TABLE{$par}}{'text1'}, 'text2' => ${$OPTIONS_TABLE{$par}}{'text2'}};");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'minoccur'} > 0)");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'mandatory'} = 1;");
			content.add("		}");
			content.add("		else");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'mandatory'} = 0;");
			content.add("		}");
			content.add("		");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'default'} =~ m/.+/)");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'default'} = ${$OPTIONS_TABLE{$par}}{'default'};");
			content.add("		}");
			content.add("		else");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'default'} = 0;");
			content.add("		}");
			content.add("	}");
			content.add("	else");
			content.add("	{");
			content.add("		my $tmp;");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'definition'} eq \"flag\")");
			content.add("		{");
			content.add("			$optionsall{$par} = \\$tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"string\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=s\"} = \\$tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"integer\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=i\"} = \\$tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"float\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=f\"} = \\$tmp;");
			content.add("		}");
			content.add("		elsif (${$OPTIONS_TABLE{$par}}{'definition'} eq \"file\")");
			content.add("		{");
			content.add("			$optionsall{$par . \"=s\"} = \\$tmp;");
			content.add("		}");
			content.add("		");
			content.add("		$OPT{$par} = \\$tmp;");
			content.add("		$OPTHELP{$par} = {'text1' => ${$OPTIONS_TABLE{$par}}{'text1'}, 'text2' => ${$OPTIONS_TABLE{$par}}{'text2'}};");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'minoccur'} > 0)");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'mandatory'} = 1;");
			content.add("		}");
			content.add("		else");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'mandatory'} = 0;");
			content.add("		}");
			content.add("		");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'default'} =~ m/.+/)");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'default'} = ${$OPTIONS_TABLE{$par}}{'default'};");
			content.add("		}");
			content.add("		else");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'default'} = 0;");
			content.add("		}");
			content.add("	}");
			content.add("}");
			content.add("# das tatsaechliche erzeugen der options mit Getopt::Long");
			content.add("");
			content.add("my $result = GetOptions(%optionsall);");
			content.add("unless($result)");
			content.add("{");
			content.add("	logit('warn', 'problem occured while reading commandline options. information given with unknown options will be ignored. this could be a problem.');");
			content.add("#	exit(1);");
			content.add("}");
			content.add("");
			content.add("# OPTIONEN DEFAULTS ZUWEISEN");
			content.add("foreach my $par (sort keys %OPTIONS_TABLE)");
			content.add("{");
			content.add("	# wenn eine option vom user nicht gesetzt wurde, aber dafuer ein default-wert existiert, soll dieser gesetzt werden");
			content.add("	");
			content.add("# wenn es ein SCALAR ist");
			content.add("	if (ref($OPT{$par}) eq \"SCALAR\")");
			content.add("	{");
			content.add("		if (!(${$OPT{$par}}) && ${$OPTIONS_TABLE{$par}}{'default'} && ${$OPTIONS_TABLE{$par}}{'default'} ne \"\")");
			content.add("		{");
			content.add("			logit('info', 'setting default for --' . $par . '=' . ${$OPTIONS_TABLE{$par}}{'default'} );");
			content.add("			my @tmp_defaults = split (\"%%\", ${$OPTIONS_TABLE{$par}}{'default'});");
			content.add("			${$OPT{$par}} = $tmp_defaults[0];");
			content.add("		}");
			content.add("	}");
			content.add("	");
			content.add("	elsif (ref($OPT{$par}) eq \"ARRAY\")");
			content.add("	{");
			content.add("		if (!(${$OPT{$par}}[0]) && ${$OPTIONS_TABLE{$par}}{'default'} && ${$OPTIONS_TABLE{$par}}{'default'} ne \"\")");
			content.add("		{");
			content.add("			logit('info', 'setting default for --' . $par . '=' . ${$OPTIONS_TABLE{$par}}{'default'} );");
			content.add("			@{$OPT{$par}} = split (\"%%\", ${$OPTIONS_TABLE{$par}}{'default'});");
			content.add("		}");
			content.add("	}");
			content.add("	");
			content.add("}");
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