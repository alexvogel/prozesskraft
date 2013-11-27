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
	ArrayList<Option> option = new ArrayList<Option>();
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
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
	public String getBlock()
	{
		
		String content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		return this.parent.genBlockStart("help") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("help");
	}
	
	private String getContent()
	{
		String content = "";

		content      += "my $helptext;\n";
		content      += "\n";
		content      += "$helptext .= 'usage: $filename PARAMETER\\n';\n";
		content      += "$helptext .= '\\n';\n";
		content      += "$helptext .= 'Parameter\n';\n";
		content      += "\n";
		content      += "foreach(sort keys %OPTHELP)\n";
		content      += "{\n";
		content      += "\t$helptext .= sprintf (' --%s%s\\n', $_, ${$OPTHELP{$_}}{'text1'});\n";
		content      += "\n";
		content      += "\t# helptext nach ca. 60 Zeichen umbrechen\n";
		content      += "\t${$OPTHELP{$_}}{'text2'} =~ s/(.{60}[^\\s]*)\\s+/$1\\n/g;\n";
		content      += "\n";
		content      += "\tforeach(split ('\n', ${$OPTHELP{$_}}{'text2'}))\n";
		content      += "\t{\n";
		content      += "\t\t$helptext .= sprintf ('       %s\\n',  $_);\n";
		content      += "\t}\n";
		content      += "\t$helptext .= '\\n';\n";
		content      += "}\n";
		content      += "\n";
		content      += "$helptext .= '\\n';\n";
		content      += "$helptext .= 'example: $filename --scope model --submodel_vat f34_vat.nas --submodel_rk f34_rk.nas\n';\\n";
		content      += "$helptext .= '\\n';\n";
		content      += "$helptext .= 'author: alexander.vogel\\@caegroup.de | version: $version | date: $date\\n';\n";

		return content;
	}
}