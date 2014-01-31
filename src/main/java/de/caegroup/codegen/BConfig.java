package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BConfig
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
	public BConfig(Script parent)
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
		
		if(type.matches("bla"))
		{
			
		}
		// default
		else
		{
			content.add("my %CONF;");
			content.add("if (stat $conf_path)");
			content.add("{");
			content.add("	my %CONF_ORG = &getvars($conf_path);");
			content.add("# viele parameter im parameterfile enthalten pfade relativ zum installationsverzeichnis");
			content.add("# diese pfade sollen auf absolute pfade expandiert werden");
			content.add("	logit(\"info\", \"expanding config parameter to absolute path\");");
			content.add("	foreach my $param (sort keys %CONF_ORG)");
			content.add("	{");
			content.add("# gibts da ein file? Ja? Dann soll auf den absoluten Pfad expandiert werden");
			content.add("		if ((($CONF_ORG{$param} ne \"\") && (stat $bindir.\"/\".$CONF_ORG{$param})))");
			content.add("		{");
			content.add("			$CONF{$param} = File::Spec->rel2abs($bindir.\"/\".$CONF_ORG{$param});");
			content.add("			logit(\"info\", \"parameter $param (value=\" . $CONF_ORG{$param} .\") expanding to (new_value=\".$CONF{$param}.\")\");");
			content.add("		}");
			content.add("		else");
			content.add("		{");
			content.add("			$CONF{$param} = $CONF_ORG{$param};");
			content.add("		}");
			content.add("	}");
			content.add("}");
			content.add("else");
			content.add("{");
			content.add("	logit(\"warn\", \"cannot read $conf_path. builtin config-read capabilities cannot be used\");");
			content.add("}");
		}
		
		this.block.setOrigin("auto");
		this.block.setBlockname("config");
		this.block.setCode(content);
	}

}