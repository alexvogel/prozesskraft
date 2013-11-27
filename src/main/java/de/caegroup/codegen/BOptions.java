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

		content.add("my %OPT;\n");
		content.add("my %OPTHELP;\n");
		content.add("my %optionsall;\n");

		content.add("# defaultoptions\n");
		content.add("# hier koenne alle 'festen' optionen definiert werden\n");
		content.add("my %options_fest = (\n");
		
		content.add("\t\t\t\t'help' => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'help|h', 'check'=>'', 'default' => undef, 'text1' => '', 'text2' => 'gibt diesen hilfetext aus'},\n");
		content.add("\t\t\t\t'doc'  => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'doc', 'check'=>'', 'default' => undef, 'text1' => '', 'text2' => 'falls eine dokumentation existiert, wird diese angezeigt'},\n");
		content.add("\t\t\t\t'log'  => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'log=s', 'check'=>'', 'default' => undef, 'text1' => '=PATH', 'text2' => 'statt auf STDERR koennen logging-ausgaben in eine datei umgeleitet werden'},\n");
		
		for(Option o: option)
		{
			content.addAll(o.getContent());
		}
		
		content.add(");\n");

		content.add("# parsen und generieren eines hashs (%optionsall) zum erzeugen von optionen (Getopt::Long)\n");
		content.add("foreach my $optionname (keys %options_fest)\n");
		content.add("{\n");

		
		content.add("\tif ( ${$options_fest{$optionname}}{'maxoccur'} > 1 )\n");
		content.add("\t{\n");
		content.add("\t\tmy @tmp;\n");
		content.add("\t\t$optionsall{${$options_fest{$optionname}}{'definition'}} = \\@tmp;\n");
		content.add("\n");
			
		content.add("\t\t$OPT{$optionname} = \\@tmp;\n");
		content.add("\t\t$OPTHELP{$optionname} = {'text1' => ${$options_fest{$optionname}}{'text1'}, 'text2' => ${$options_fest{$optionname}}{'text2'}};\n");
		content.add("\t}\n");
		content.add("\telse\n");
		content.add("\t{\n");
		content.add("\t\tmy $tmp;\n");
		content.add("\t\t$optionsall{${$options_fest{$optionname}}{'definition'}} = \\$tmp;\n");
		content.add("\n");

		content.add("\t\t$OPT{$optionname} = \\$tmp;\n");
		content.add("\t\t$OPTHELP{$optionname} = {'text1' => ${$options_fest{$optionname}}{'text1'}, 'text2' => ${$options_fest{$optionname}}{'text2'}};\n");
		content.add("\t}\n");
		content.add("}\n");

		content.add("# das tatsaechliche erzeugen der options mit Getopt::Long\n");
		content.add("my $result = GetOptions(%optionsall);\n");

		content.add("unless($result)\n");
		content.add("{\n");
		content.add("	logit('warn', 'problem occured while reading commandline options. information given with unknown options will be ignored. this could be a problem.');\n");
		content.add("#	exit(1);\n");
		content.add("}\n");

		content.add("# OPTIONEN DEFAULTS ZUWEISEN\n");
		content.add("foreach(keys %options_fest)\n");
		content.add("{\n");
		content.add("	# wenn eine option nicht vorhanden ist, aber dafuer ein default-wert existiert, soll dieser gesetzt werden\n");
		content.add("	if (!(${$OPT{$_}}) && ${$options_fest{$_}}{'default'})\n");
		content.add("#	if ( ${$options_fest{$_}}{'default'})\n");
		content.add("	{\n");
		content.add("		logit('info', 'setting default for --' . $_ . '=' . ${$options_fest{$_}}{'default'} );\n");
		content.add("		${$OPT{$_}} = ${$options_fest{$_}}{'default'};\n");
		content.add("	}\n");
		content.add("}\n");

		return content;
	}
}