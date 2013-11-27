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
		block.addAll(this.parent.genBlockStart("path"));
		block.add("# md5="+md5);
		block.add(this.parent.trenner);
		block.addAll(content);
		block.add(this.parent.trenner);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("path"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("# feststellen des installationsortes des scripts");
		content.add("my ($filename, $directories, $suffix);");
		content.add("BEGIN");
		content.add("{");
		content.add("    use File::Basename;");
		content.add("    ($filename, $directories, $suffix) = fileparse ($0);");
		content.add("}");
		content.add("");
		content.add("# autoflush");
		content.add("$|=1;");
		content.add("");
		content.add("# feststellen des installationsortes des programmes");
		content.add("$directories = File::Spec->rel2abs($directories);");
		content.add("my $installdir = $directories . \"/..\";");
		content.add("my $etcdir = $installdir . \"/etc\";");
		content.add("my $docdir = $installdir . \"/doc\";");
		content.add("my $bindir = $directories;");
		content.add("");
		content.add("# einbinden der avoge module");
		content.add("# zuerst, falls aus installationsverzeichnis gesucht wird");
		content.add("use lib $directories . \"../../../myperllib/master/lib\";");
		content.add("# falls aus eclipse gesucht wird");
		content.add("use lib $directories . \"../../myperllib/lib\";");
		content.add("# falls aus installationsverzeichnis das eigene lib gesucht wird (wird benoetigt falls die zentrale version nicht mehr passt)");
		content.add("use lib $directories . \"../myperllib/lib\";");
		content.add("# falls aus installationsverzeichnis cb2-scripts");
		content.add("use lib $directories . \"../../../../cb2common/lib/1.0\";");
		content.add("");
		content.add("# path to configurationfile");
		content.add("my $conf_path = $etcdir . \"/\" . $filename.\".conf\";");
		content.add("# path to documentation");
		content.add("my $doc_path = $docdir . \"/\" . $filename.\".pdf\";");
		
		return content;
	}
}