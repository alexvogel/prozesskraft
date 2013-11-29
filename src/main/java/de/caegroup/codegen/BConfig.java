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
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
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
		
		ArrayList<String> content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		
		ArrayList<String> block = new ArrayList<String>();
		block.addAll(this.parent.genBlockStart("config"));
		block.add("# md5="+md5);
		block.add(this.parent.trenner);
		block.addAll(content);
		block.add(this.parent.trenner);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("config"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("if (stat $conf_path)");
		content.add("{");
		content.add("	my %CONF_ORG = &getvars($conf_path);");
		content.add("	my %CONF;");
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
		content.add("	logit('warn', 'cannot read $conf_path. builtin config-read capabiliries cannot be used');");
		content.add("}");
		
		return content;
	}
}