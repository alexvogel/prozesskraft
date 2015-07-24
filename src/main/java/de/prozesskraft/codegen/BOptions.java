package de.prozesskraft.codegen;

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
	Block block = new Block();

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
		return this.block.getCode();
	}

	public void genCode(String type)
	{
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("process"))
		{
			content.add("my %OPT;");
			content.add("my %OPTHELP;");
			content.add("my %optionsall;");

			content.add("# defaultoptions");
			content.add("# hier koenne alle 'festen' optionen definiert werden");
			content.add("my %OPTIONS_TABLE = (");
			
			content.add("# 1 < reihenfolge <= 15");
			content.add("# definition der standardoptions");
			content.add("				'help' => {'reihenfolge' => '13', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'1', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'textfordefault' => '', 'text1' => '', 'text2' => 'prints this helptext'},");
			content.add("				'd'  => {'reihenfolge' => '14', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'1', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'textfordefault' => '', 'text1' => '', 'text2' => 'will also log debug-statements.'},");
			content.add("				'commitfiledummy'  => {'reihenfolge' => '15', 'minoccur' => '0', 'maxoccur' => '99', 'definition' => 'string', 'check'=>'1', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'textfordefault' => '', 'text1' => 'KEY=FILE; FILE', 'text2' => 'this parameter allows you to commit files into the process, which are not expected by the interface. use this parameter only in process development e.g. to accelerate tests by committing result files'},");
			content.add("				'commitvariabledummy'  => {'reihenfolge' => '16', 'minoccur' => '0', 'maxoccur' => '99', 'definition' => 'string', 'check'=>'1', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'textfordefault' => '', 'text1' => 'KEY=VALUE; VALUE', 'text2' => 'this parameter allows you to commit variables into the process, which are not expected by the interface. use this parameter only in process development e.g. to accelerate tests by committing result variables'},");
			
			content.add("");
			content.add("# definition der speziellen options");
			for(Option o: this.parent.option)
			{
				content.addAll(o.getContent());
			}
			
			content.add(");");

			content.add("");
			content.add("# die speziellen options in einem array sammeln");
			content.add("my @INPUT_OPTIONS;");
			for(Option o: this.parent.option)
			{
				content.add("push(@INPUT_OPTIONS, \"" + o.getName() + "\");");
			}
			content.add("");

			content.add("# dynamische standardoptions definieren");
			content.add("# wenn kein dokufile vorhanden ist, wird auch diese option nicht angelegt");
			content.add("if (stat $doc_path)");
			content.add("{");
			content.add("	$OPTIONS_TABLE{'doc'} = {'reihenfolge' => '20', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '', 'text2' => \"shows documentation\"},");
			content.add("}");
			content.add("# wenn kein config-file vorhanden ist, wird auch diese option nicht angelegt");
			content.add("if (stat $conf_path1)");
			content.add("{");
			content.add("	my %CONF_TMP = &getvars($conf_path1);");
			content.add("	$OPTIONS_TABLE{'conf'} = {'reihenfolge' => '21', 'minoccur' => '0', 'maxoccur' => '99', 'definition' => 'string', 'check'=>'^[^=]+=[^=]+$', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '=KEY=VALUE', 'text2' => 'for redefinition of configuration variables (instead of using the ones in '.Cwd::realpath(File::Spec->rel2abs($conf_path1)).'). (possible KEYs are: ' . join(\", \", sort keys %CONF_TMP) . ')'};");
			content.add("}");
			content.add("elsif (stat $conf_path2)");
			content.add("{");
			content.add("	my %CONF_TMP = &getvars($conf_path2);");
			content.add("	$OPTIONS_TABLE{'conf'} = {'reihenfolge' => '21', 'minoccur' => '0', 'maxoccur' => '99', 'definition' => 'string', 'check'=>'^[^=]+=[^=]+$', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '=KEY=VALUE', 'text2' => 'for redefinition of configuration variables (instead of using the ones in '.Cwd::realpath(File::Spec->rel2abs($conf_path2)).').\n(possible KEYs are: ' . join(\", \", sort keys %CONF_TMP) . ')'};");
			content.add("}");

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
			content.add("		# dem konfigurierten text1 noch [,STRING] anhaengen falls noetig");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'allowIntegratedListIfMultiOption'} eq \"true\")");
			content.add("		{");
			content.add("			# das fuehrende '=' in einer kopie entfernen");
			content.add("			my $tmp = ${$OPTIONS_TABLE{$par}}{'text1'};");
			content.add("			$tmp =~ s/^=//;");
			content.add("			");
			content.add("			# den text1 fuer die Usage erweitern");
			content.add("			$OPTHELP{$par}{'text1'} = $OPTHELP{$par}{'text1'} . \"[,$tmp]\";");
			content.add("		}");
			content.add("		");
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
			content.add("		");
			content.add("		if ( (exists ${$OPTIONS_TABLE{$par}}{'textfordefault'}) && (${$OPTIONS_TABLE{$par}}{'textfordefault'} =~ m/.+/) )");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'textfordefault'} = ${$OPTIONS_TABLE{$par}}{'textfordefault'};");
			content.add("		}");
			content.add("		");
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
			content.add("		if ( (exists ${$OPTIONS_TABLE{$par}}{'textfordefault'}) && (${$OPTIONS_TABLE{$par}}{'textfordefault'} =~ m/.+/) )");
			content.add("		{");
			content.add("			$OPTHELP{$par}{'textfordefault'} = ${$OPTIONS_TABLE{$par}}{'textfordefault'};");
			content.add("		}");
			content.add("		");
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
			content.add("# VALUES AN KOMMAS SPLITTEN, wenn es die option entsprechend definiert ist (maxoccur > 1, allowIntegratedListIfMultiOption == true)");
			content.add("foreach my $par (sort keys %OPTIONS_TABLE)");
			content.add("{");
			content.add("	if (ref($OPT{$par}) eq \"ARRAY\")");
			content.add("	{");
			content.add("		if(${$OPTIONS_TABLE{$par}}{'allowIntegratedListIfMultiOption'} eq \"true\")");
			content.add("		{");
			content.add("			my @newOptionsContent;");
			content.add("			if(&getOption($par))");
			content.add("			{");
			content.add("				foreach my $possiblyCommaSeparatedOption (&getOption($par))");
			content.add("				{");
			content.add("					my $count = ($possiblyCommaSeparatedOption =~ m/,/);");
			content.add("					");
			content.add("					if($count > 0)");
			content.add("					{");
			content.add("						logit(\"debug\", \"occurance of option --\".$par.\" has\" . ++$count . \" entries in form of a comma-separated list ($possiblyCommaSeparatedOption)\");");
			content.add("						logit(\"debug\", \"these occurances will be treated as they have been called with separate --$par\");");
			content.add("						push(@newOptionsContent, split(\",\", $possiblyCommaSeparatedOption));");
			content.add("					}");
			content.add("					else");
			content.add("					{");
			content.add("						push(@newOptionsContent, $possiblyCommaSeparatedOption);");
			content.add("					}");
			content.add("				}");
			content.add("				&setOption($par, @newOptionsContent);");
			content.add("			}");
			content.add("		}");
			content.add("	}");
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
			content.add("			if(${$OPTIONS_TABLE{$par}}{'definition'} eq \"flag\")");
			content.add("			{");
			content.add("				logit('info', 'setting flag --' . $par);");
			content.add("			}");
			content.add("			else");
			content.add("			{");
			content.add("				logit('info', 'setting default for --' . $par . '=' . ${$OPTIONS_TABLE{$par}}{'default'} );");
			content.add("			}");
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

		// default
		else
		{
			content.add("my %OPT;");
			content.add("my %OPTHELP;");
			content.add("my %optionsall;");

			content.add("# defaultoptions");
			content.add("# hier koenne alle 'festen' optionen definiert werden");
			content.add("my %OPTIONS_TABLE = (");
			
			content.add("# 1 < reihenfolge <= 15");
			content.add("# definition der standardoptions");
			content.add("				'help' => {'reihenfolge' => '13', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'1', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'textfordefault' => '', 'text1' => '', 'text2' => 'prints this helptext'},");
			content.add("				'd'  => {'reihenfolge' => '14', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'1', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'textfordefault' => '', 'text1' => '', 'text2' => 'will also log debug-statements.'},");
			
			content.add("");
			content.add("# definition der speziellen options");
			for(Option o: this.parent.option)
			{
				content.addAll(o.getContent());
			}
			
			content.add(");");

			content.add("");
			content.add("# die speziellen options in einem array sammeln");
			content.add("my @INPUT_OPTIONS;");
			for(Option o: this.parent.option)
			{
				content.add("push(@INPUT_OPTIONS, \"" + o.getName() + "\");");
			}
			content.add("");

			content.add("# dynamische standardoptions definieren");
			content.add("# wenn kein dokufile vorhanden ist, wird auch diese option nicht angelegt");
			content.add("if (stat $doc_path)");
			content.add("{");
			content.add("	#$OPTIONS_TABLE{'doc'} = {'reihenfolge' => '20', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '', 'text2' => \"shows documentation(\".Cwd::realpath(File::Spec->rel2abs($doc_path)).\")\"};");
			content.add("	$OPTIONS_TABLE{'doc'} = {'reihenfolge' => '20', 'minoccur' => '0', 'maxoccur' => '1', 'definition' => 'flag', 'check'=>'', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '', 'text2' => \"shows documentation\"};");
			content.add("}");
			content.add("# wenn kein config-file vorhanden ist, wird auch diese option nicht angelegt");
			content.add("if (stat $conf_path1)");
			content.add("{");
			content.add("	my %CONF_TMP = &getvars($conf_path1);");
			content.add("	$OPTIONS_TABLE{'conf'} = {'reihenfolge' => '21', 'minoccur' => '0', 'maxoccur' => '99', 'definition' => 'string', 'check'=>'^[^=]+=[^=]+$', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '=KEY=VALUE', 'text2' => 'for redefinition of configuration variables (possible KEYs are: ' . join(\", \", sort keys %CONF_TMP) . ')'};");
			content.add("}");
			content.add("elsif (stat $conf_path2)");
			content.add("{");
			content.add("	my %CONF_TMP = &getvars($conf_path2);");
			content.add("	$OPTIONS_TABLE{'conf'} = {'reihenfolge' => '21', 'minoccur' => '0', 'maxoccur' => '99', 'definition' => 'string', 'check'=>'^[^=]+=[^=]+$', 'default' => '', 'allowIntegratedListIfMultiOption' => 'false', 'text1' => '=KEY=VALUE', 'text2' => 'for redefinition of configuration variables (possible KEYs are: ' . join(\", \", sort keys %CONF_TMP) . ')'};");
			content.add("}");

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
			content.add("		# dem konfigurierten text1 noch [,STRING] anhaengen falls noetig");
			content.add("		if (${$OPTIONS_TABLE{$par}}{'allowIntegratedListIfMultiOption'} eq \"true\")");
			content.add("		{");
			content.add("			# das fuehrende '=' in einer kopie entfernen");
			content.add("			my $tmp = ${$OPTIONS_TABLE{$par}}{'text1'};");
			content.add("			$tmp =~ s/^=//;");
			content.add("			");
			content.add("			# den text1 fuer die Usage erweitern");
			content.add("			$OPTHELP{$par}{'text1'} = $OPTHELP{$par}{'text1'} . \"[,$tmp]\";");
			content.add("		}");
			content.add("		");
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
			content.add("# VALUES AN KOMMAS SPLITTEN, wenn es die option entsprechend definiert ist (maxoccur > 1, allowIntegratedListIfMultiOption == true)");
			content.add("foreach my $par (sort keys %OPTIONS_TABLE)");
			content.add("{");
			content.add("	if (ref($OPT{$par}) eq \"ARRAY\")");
			content.add("	{");
			content.add("		if(${$OPTIONS_TABLE{$par}}{'allowIntegratedListIfMultiOption'} eq \"true\")");
			content.add("		{");
			content.add("			my @newOptionsContent;");
			content.add("			if(&getOption($par))");
			content.add("			{");
			content.add("				foreach my $possiblyCommaSeparatedOption (&getOption($par))");
			content.add("				{");
			content.add("					my $count = ($possiblyCommaSeparatedOption =~ m/,/);");
			content.add("					");
			content.add("					if($count > 0)");
			content.add("					{");
			content.add("						logit(\"debug\", \"occurance of option --\".$par.\" has\" . ++$count . \" entries in form of a comma-separated list ($possiblyCommaSeparatedOption)\");");
			content.add("						logit(\"debug\", \"these occurances will be treated as they have been called with separate --$par\");");
			content.add("						push(@newOptionsContent, split(\",\", $possiblyCommaSeparatedOption));");
			content.add("					}");
			content.add("					else");
			content.add("					{");
			content.add("						push(@newOptionsContent, $possiblyCommaSeparatedOption);");
			content.add("					}");
			content.add("				}");
			content.add("				&setOption($par, @newOptionsContent);");
			content.add("			}");
			content.add("		}");
			content.add("	}");
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
			content.add("			if(${$OPTIONS_TABLE{$par}}{'definition'} eq \"flag\")");
			content.add("			{");
			content.add("				logit('info', 'setting flag --' . $par);");
			content.add("			}");
			content.add("			else");
			content.add("			{");
			content.add("				logit('info', 'setting default for --' . $par . '=' . ${$OPTIONS_TABLE{$par}}{'default'} );");
			content.add("			}");
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

		this.block.setOrigin("auto");
		this.block.setBlockname("options");
		this.block.setCode(content);
	}
}