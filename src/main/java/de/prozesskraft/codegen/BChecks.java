package de.prozesskraft.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BChecks
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
	public BChecks(Script parent)
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
			content.add("my $error_patternchecks = 0;");
			content.add("my $error_anzahl = 0;");
			content.add("");
			content.add("# checken der anzahl der aufrufoptionen");
			content.add("foreach my $key (sort keys %OPT)");
			content.add("{");
			content.add("	my $anzahl = 0;");
			content.add("	if    ( (ref($OPT{$key}) eq \"SCALAR\") && ( defined(${$OPT{$key}}) ) ) {$anzahl = 1}");
			content.add("	elsif (ref($OPT{$key}) eq \"ARRAY\") {$anzahl = scalar(@{$OPT{$key}});}");
			content.add("	");
			content.add("	# minoccur checken");
			content.add("	if ( ${$OPTIONS_TABLE{$key}}{'minoccur'} > $anzahl )");
			content.add("	{");
			content.add("		if ( ${$OPTIONS_TABLE{$key}}{'minoccur'} == 1 && ${$OPTIONS_TABLE{$key}}{'maxoccur'} == 1)");
			content.add("		{");
			content.add("			logit('error', 'option --'.$key.' needed.');");
			content.add("		}");
			content.add("		elsif ( ${$OPTIONS_TABLE{$key}}{'minoccur'} < ${$OPTIONS_TABLE{$key}}{'maxoccur'})");
			content.add("		{");
			content.add("			logit('error', 'option --'.$key.' needed at least '.${$OPTIONS_TABLE{$key}}{'minoccur'}.' times.');");
			content.add("		}");
			content.add("		$error_anzahl++;");
			content.add("	}");
			content.add("	# maxoccur checken");
			content.add("	if ( ${$OPTIONS_TABLE{$key}}{'maxoccur'} < $anzahl )");
			content.add("	{");
			content.add("		logit('error', 'option --'.$key.' may only be used '.${$OPTIONS_TABLE{$key}}{'maxoccur'}.' times.');");
			content.add("		$error_anzahl++;");
			content.add("	}");
			content.add("}");
			content.add("");
			content.add("# checken des inhalts der aufrufoptionen");
			content.add("foreach my $key (sort keys %OPT)");
			content.add("{");
			content.add("	my $anzahl = 0;");
			content.add("	if ( (ref($OPT{$key}) eq \"SCALAR\") && ( defined(${$OPT{$key}}) ) )");
			content.add("	{");
			content.add("		unless (${$OPT{$key}} =~ m/${$OPTIONS_TABLE{$key}}{'check'}/)");
			content.add("		{");
			content.add("			logit('error', 'option --'.$key.'='.${$OPT{$key}}.' is not a ' . ${$OPTIONS_TABLE{$key}}{'definition'}.  ' (does not match the expected pattern /'.${$OPTIONS_TABLE{$key}}{'check'}.'/).');");
			content.add("			$error_patternchecks++;");
			content.add("		}");
			content.add("	}");
			content.add("	");
			content.add("	elsif ( (ref($OPT{$key}) eq \"ARRAY\") && (scalar(@{$OPT{$key}})) )");
			content.add("	{");
			content.add("		foreach my $value (@{$OPT{$key}})");
			content.add("		{");
			content.add("			unless ($value =~ m/${$OPTIONS_TABLE{$key}}{'check'}/)");
			content.add("			{");
			content.add("				logit('error', 'option --'.$key.'='.$value.' is not a ' . ${$OPTIONS_TABLE{$key}}{'definition'}.  ' (does not match the expected pattern /'.${$OPTIONS_TABLE{$key}}{'check'}.'/).');");
			content.add("				$error_patternchecks++;");
			content.add("			}");
			content.add("		}");
			content.add("	}");
			content.add("}");
			content.add("");
			content.add("");
			content.add("if ($error_anzahl + $error_patternchecks)");
			content.add("{");
			content.add("	logit('fatal', $error_anzahl + $error_patternchecks .' error(s) found in call. call --help for help. exit.');");
			content.add("	exit(1);");
			content.add("}");
		}

		this.block.setOrigin("auto");
		this.block.setBlockname("checks");
		this.block.setCode(content);
	}
}