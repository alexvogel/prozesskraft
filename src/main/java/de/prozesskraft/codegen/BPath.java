package de.prozesskraft.codegen;

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
	Block block = new Block();

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
		return this.block.getCode();
	}

	public void genCode(String type)
	{
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("process"))
		{
			content.add("my $FULLCALL = join(' ', $0, @ARGV);");
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
			content.add("my $installdir = File::Spec->rel2abs($directories);");
			content.add("my $etcdir = $INSTALLDIR . \"/etc\";");
			content.add("my $docdir = $INSTALLDIR . \"/doc\";");
			content.add("my $bindir = $INSTALLDIR . \"/bin\";");
			content.add("");
			content.add("# feststellen des domain installations verzeichnisses");
			content.add("my $domainInstallationDirectory = $installdir . \"/../../..\";");
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
			content.add("# falls aus installationsverzeichnis gesucht wird zuerst das lokale lib verwenden");
			content.add("use lib $directories . \"lib\";");
			content.add("");
			content.add("# path to configurationfile");
			content.add("my $conf_path1 = $installdir . \"/\" . $filename.\".conf\";");
			content.add("my $conf_path2 = $etcdir . \"/\" . $filename.\".conf\";");
			content.add("");
			content.add("my $baseFilename = $filename;");
			content.add("$baseFilename =~ s/\\..+$//; # entfernen der extension");
			content.add("my $conf_path3 = $etcdir . \"/\" . $baseFilename.\".conf\";");
			content.add("");
			content.add("# path to documentation");
			content.add("my $doc_path = $docdir . \"/\" . $filename.\".pdf\";");
			content.add("");
			content.add("# das aufrufverzeichnis (basedir)");
			content.add("my $_basedir = cwd();");
		}
		// default
		else
		{
			content.add("my $FULLCALL = join(' ', $0, @ARGV);");
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
			content.add("my $INSTALLDIR = File::Spec->rel2abs($directories);");
			content.add("my $etcdir = $INSTALLDIR . \"/../etc\";");
			content.add("my $docdir = $INSTALLDIR . \"/../doc\";");
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
			content.add("# falls aus installationsverzeichnis gesucht wird zuerst das lokale lib verwenden");
			content.add("use lib $directories . \"../lib\";");
			content.add("");
			content.add("# path to configurationfile");
			content.add("my $conf_path1 = $directories . \"/\" . $filename.\".conf\";");
			content.add("my $conf_path2 = $etcdir . \"/\" . $filename.\".conf\";");
			content.add("");
			content.add("my $baseFilename = $filename;");
			content.add("$baseFilename =~ s/\\..+$//; # entfernen der extension");
			content.add("my $conf_path3 = $etcdir . \"/\" . $baseFilename.\".conf\";");
			content.add("");
			content.add("# path to documentation");
			content.add("my $doc_path = $docdir . \"/\" . $filename.\".pdf\";");
			content.add("");
			content.add("# das aufrufverzeichnis (basedir)");
			content.add("my $_basedir = cwd();");
			content.add("");
			content.add("# das aktuelle configfile");
			content.add("my %ALLCONFS;");
			content.add("my $SELECTED_CONF_LABEL = 'default';");
			content.add("my %SELECTED_CONF;");

		}
		
		this.block.setOrigin("auto");
		this.block.setBlockname("path");
		this.block.setCode(content);
	}
}