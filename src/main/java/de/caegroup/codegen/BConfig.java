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
	public String getBlock()
	{
		
		String content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		return this.parent.genBlockStart("path") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("path");
	}
	
	private String getContent()
	{
		String content = "";
		content      += "my %CONF_ORG = &getvars($conf_path);\n";
		content      += "my %CONF;\n";
		content      += "# viele parameter im parameterfile enthalten pfade relativ zum installationsverzeichnis\n";
		content      += "# diese pfade sollen auf absolute pfade expandiert werden\n";
		content      += "logit(\"info\", \"expanding config parameter to absolute path\");\n";
		content      += "foreach my $param (sort keys %CONF_ORG)\n";
		content      += "{\n";
		content      += "# gibts da ein file? Ja? Dann soll auf den absoluten Pfad expandiert werden";
		content      += "	if ((($CONF_ORG{$param} ne \"\") && (stat $bindir.\"/\".$CONF_ORG{$param})))";
		content      += "	{";
		content      += "		$CONF{$param} = File::Spec->rel2abs($bindir.\"/\".$CONF_ORG{$param});";
		content      += "		logit(\"info\", \"parameter $param (value=\" . $CONF_ORG{$param} .\") expanding to (new_value=\".$CONF{$param}.\")\");";
		content      += "	}";
		content      += "	else";
		content      += "	{";
		content      += "		$CONF{$param} = $CONF_ORG{$param};";
		content      += "	}";
		content      += "}";
		
		return content;
	}
}