package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BPath
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
	public BPath(Script parent)
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
		content      += "\n";
		content      += "# feststellen des installationsortes des scripts\n";
		content      += "my ($filename, $directories, $suffix);\n";
		content      += "BEGIN\n";
		content      += "{\n";
		content      += "    use File::Basename;\n";
		content      += "    ($filename, $directories, $suffix) = fileparse ($0);\n";
		content      += "}\n";
		content      += "\n";
		content      += "# autoflush\n";
		content      += "$|=1;\n";
		content      += "\n";
		content      += "# feststellen des installationsortes des programmes\n";
		content      += "$directories = File::Spec->rel2abs($directories);\n";
		content      += "my $installdir = $directories . \"/..\";\n";
		content      += "my $etcdir = $installdir . \"/etc\";\n";
		content      += "my $docdir = $installdir . \"/doc\";\n";
		content      += "my $bindir = $directories;\n";
		content      += "\n";
		content      += "# einbinden der avoge module\n";
		content      += "# zuerst, falls aus installationsverzeichnis gesucht wird\n";
		content      += "use lib $directories . \"../../../myperllib/master/lib\";\n";
		content      += "# falls aus eclipse gesucht wird\n";
		content      += "use lib $directories . \"../../myperllib/lib\";\n";
		content      += "# falls aus installationsverzeichnis das eigene lib gesucht wird (wird benoetigt falls die zentrale version nicht mehr passt)\n";
		content      += "use lib $directories . \"../myperllib/lib\";\n";
		content      += "# falls aus installationsverzeichnis cb2-scripts\n";
		content      += "use lib $directories . \"../../../../cb2common/lib/1.0\";\n";
		content      += "\n";
		content      += "# path to configurationfile\n";
		content      += "my $conf_path = $etcdir . \"/\" . $filename.\".conf\";\n";
		content      += "# path to documentation\n";
		content      += "my $doc_path = $docdir . \"/\" . $filename.\".pdf\";\n";
		content      += "\n";
		
		return content;
	}
}