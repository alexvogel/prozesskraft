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
	public ArrayList<String> getBlock()
	{
		
		ArrayList<String> content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		
		ArrayList<String> block = new ArrayList<String>();
		block.add(this.parent.genBlockStart("path"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.add(this.parent.genBlockEnd("path"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("\n");
		content.add("# feststellen des installationsortes des scripts\n");
		content.add("my ($filename, $directories, $suffix);\n");
		content.add("BEGIN\n");
		content.add("{\n");
		content.add("    use File::Basename;\n");
		content.add("    ($filename, $directories, $suffix) = fileparse ($0);\n");
		content.add("}\n");
		content.add("\n");
		content.add("# autoflush\n");
		content.add("$|=1;\n");
		content.add("\n");
		content.add("# feststellen des installationsortes des programmes\n");
		content.add("$directories = File::Spec->rel2abs($directories);\n");
		content.add("my $installdir = $directories . \"/..\");\n");
		content.add("my $etcdir = $installdir . \"/etc\");\n");
		content.add("my $docdir = $installdir . \"/doc\");\n");
		content.add("my $bindir = $directories;\n");
		content.add("\n");
		content.add("# einbinden der avoge module\n");
		content.add("# zuerst, falls aus installationsverzeichnis gesucht wird\n");
		content.add("use lib $directories . \"../../../myperllib/master/lib\");\n");
		content.add("# falls aus eclipse gesucht wird\n");
		content.add("use lib $directories . \"../../myperllib/lib\");\n");
		content.add("# falls aus installationsverzeichnis das eigene lib gesucht wird (wird benoetigt falls die zentrale version nicht mehr passt)\n");
		content.add("use lib $directories . \"../myperllib/lib\");\n");
		content.add("# falls aus installationsverzeichnis cb2-scripts\n");
		content.add("use lib $directories . \"../../../../cb2common/lib/1.0\");\n");
		content.add("\n");
		content.add("# path to configurationfile\n");
		content.add("my $conf_path = $etcdir . \"/\" . $filename.\".conf\");\n");
		content.add("# path to documentation\n");
		content.add("my $doc_path = $docdir . \"/\" . $filename.\".pdf\");\n");
		content.add("\n");
		
		return content;
	}
}