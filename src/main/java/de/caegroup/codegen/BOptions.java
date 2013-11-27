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
	public String getBlock()
	{
		
		String content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		return this.parent.genBlockStart("options") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("options");
	}
	
	private String getContent()
	{
		String content = "";
		content      += "my %OPT;\n";
		content      += "my %OPTHELP;\n";
		content      += "my %optionsall;\n";

		content      += "# defaultoptions\n";
		content      += "# hier koenne alle 'festen' optionen definiert werden\n";
		content      += "my %options_fest = (\n";
		
		content      += "\t\t\t\t'help' => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'help|h', 'check'=>'', 'default' => undef, 'text1' => '', 'text2' => 'gibt diesen hilfetext aus'},\n";
		content      += "\t\t\t\t'doc'  => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'doc', 'check'=>'', 'default' => undef, 'text1' => '', 'text2' => 'falls eine dokumentation existiert, wird diese angezeigt'},\n";
		content      += "\t\t\t\t'log'  => {'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'log=s', 'check'=>'', 'default' => undef, 'text1' => '=PATH', 'text2' => 'statt auf STDERR koennen logging-ausgaben in eine datei umgeleitet werden'},\n";
		
		for(Option o: option)
		{
			content  += o.getContent();
		}
		
		content      += ");\n";

		content      += "# parsen und generieren eines hashs (%optionsall) zum erzeugen von optionen (Getopt::Long)\n";
		content      += "foreach my $optionname (keys %options_fest)\n";
		content      += "{\n";

		
		content      += "\tif ( ${$options_fest{$optionname}}{'maxoccur'} > 1 )\n";
		content      += "\t{\n";
		content      += "\t\tmy @tmp;\n";
		content      += "\t\t$optionsall{${$options_fest{$optionname}}{'definition'}} = \\@tmp;\n";
		content      += "\n";
			
		content      += "\t\t$OPT{$optionname} = \\@tmp;\n";
		content      += "\t\t$OPTHELP{$optionname} = {'text1' => ${$options_fest{$optionname}}{'text1'}, 'text2' => ${$options_fest{$optionname}}{'text2'}};\n";
		content      += "\t}\n";
		content      += "\telse\n";
		content      += "\t{\n";
		content      += "\t\tmy $tmp;\n";
		content      += "\t\t$optionsall{${$options_fest{$optionname}}{'definition'}} = \\$tmp;\n";
		content      += "\n";

		content      += "\t\t$OPT{$optionname} = \\$tmp;\n";
		content      += "\t\t$OPTHELP{$optionname} = {'text1' => ${$options_fest{$optionname}}{'text1'}, 'text2' => ${$options_fest{$optionname}}{'text2'}};\n";
		content      += "\t}\n";
		content      += "}\n";

		content      += "# das tatsaechliche erzeugen der options mit Getopt::Long\n";
		content      += "my $result = GetOptions(%optionsall);\n";

		content      += "unless($result)\n";
		content      += "{\n";
		content      += "	logit('warn', 'problem occured while reading commandline options. information given with unknown options will be ignored. this could be a problem.');\n";
		content      += "#	exit(1);\n";
		content      += "}\n";

		content      += "# OPTIONEN DEFAULTS ZUWEISEN\n";
		content      += "foreach(keys %options_fest)\n";
		content      += "{\n";
		content      += "	# wenn eine option nicht vorhanden ist, aber dafuer ein default-wert existiert, soll dieser gesetzt werden\n";
		content      += "	if (!(${$OPT{$_}}) && ${$options_fest{$_}}{'default'})\n";
		content      += "#	if ( ${$options_fest{$_}}{'default'})\n";
		content      += "	{\n";
		content      += "		logit('info', 'setting default for --' . $_ . '=' . ${$options_fest{$_}}{'default'} );\n";
		content      += "		${$OPT{$_}} = ${$options_fest{$_}}{'default'};\n";
		content      += "	}\n";
		content      += "}\n";

		return content;
	}
}