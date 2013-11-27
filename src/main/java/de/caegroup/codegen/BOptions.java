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
	ArrayList<Option> option = new ArrayList<Option>();
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BOptions(Script parent)
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
		block.addAll(this.parent.genBlockStart("checks"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("checks"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("my %OPT;");
		content.add("my %OPTHELP;");
		content.add("my %optionsall;");

		content.add("# defaultoptions");
		content.add("# hier koenne alle 'festen' optionen definiert werden");
		content.add("my %options_fest = (");
		
		content.add("\t\t\t\t'help' => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'help|h', 'check'=>'', 'default' => undef, 'text1' => '', 'text2' => 'gibt diesen hilfetext aus'},");
		content.add("\t\t\t\t'doc'  => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'doc', 'check'=>'', 'default' => undef, 'text1' => '', 'text2' => 'falls eine dokumentation existiert, wird diese angezeigt'},");
		content.add("\t\t\t\t'log'  => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'log=s', 'check'=>'', 'default' => undef, 'text1' => '=PATH', 'text2' => 'statt auf STDERR koennen logging-ausgaben in eine datei umgeleitet werden'},");
		
		for(Option o: option)
		{
			content.addAll(o.getContent());
		}
		
		content.add(");");

		content.add("# parsen und generieren eines hashs (%optionsall) zum erzeugen von optionen (Getopt::Long)");
		content.add("foreach my $optionname (keys %options_fest)");
		content.add("{");

		
		content.add("\tif ( ${$options_fest{$optionname}}{'maxoccur'} > 1 )");
		content.add("\t{");
		content.add("\t\tmy @tmp;");
		content.add("\t\t$optionsall{${$options_fest{$optionname}}{'definition'}} = \\@tmp;");
		content.add("");
			
		content.add("\t\t$OPT{$optionname} = \\@tmp;");
		content.add("\t\t$OPTHELP{$optionname} = {'text1' => ${$options_fest{$optionname}}{'text1'}, 'text2' => ${$options_fest{$optionname}}{'text2'}};");
		content.add("\t}");
		content.add("\telse");
		content.add("\t{");
		content.add("\t\tmy $tmp;");
		content.add("\t\t$optionsall{${$options_fest{$optionname}}{'definition'}} = \\$tmp;");
		content.add("");

		content.add("\t\t$OPT{$optionname} = \\$tmp;");
		content.add("\t\t$OPTHELP{$optionname} = {'text1' => ${$options_fest{$optionname}}{'text1'}, 'text2' => ${$options_fest{$optionname}}{'text2'}};");
		content.add("\t}");
		content.add("}");

		content.add("# das tatsaechliche erzeugen der options mit Getopt::Long");
		content.add("my $result = GetOptions(%optionsall);");

		content.add("unless($result)");
		content.add("{");
		content.add("	logit('warn', 'problem occured while reading commandline options. information given with unknown options will be ignored. this could be a problem.');");
		content.add("#	exit(1);");
		content.add("}");

		content.add("# OPTIONEN DEFAULTS ZUWEISEN");
		content.add("foreach(keys %options_fest)");
		content.add("{");
		content.add("	# wenn eine option nicht vorhanden ist, aber dafuer ein default-wert existiert, soll dieser gesetzt werden");
		content.add("	if (!(${$OPT{$_}}) && ${$options_fest{$_}}{'default'})");
		content.add("#	if ( ${$options_fest{$_}}{'default'})");
		content.add("	{");
		content.add("		logit('info', 'setting default for --' . $_ . '=' . ${$options_fest{$_}}{'default'} );");
		content.add("		${$OPT{$_}} = ${$options_fest{$_}}{'default'};");
		content.add("	}");
		content.add("}");

		return content;
	}
}