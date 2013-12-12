package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BSubs
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	Script parent = null;
	ArrayList<String> content = new ArrayList<String>();
	String type = "default";
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BSubs(Script parent)
	{
		this.parent = parent;
		this.genContent(type);
	}

	/*----------------------------
	  methods 
	----------------------------*/
	public ArrayList<String> getBlock()
	{
		
		ArrayList<String> content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		
		ArrayList<String> block = new ArrayList<String>();
		block.addAll(this.parent.genBlockStart("subs"));
		block.add("# type="+type);
		block.add("# md5="+md5);
		block.add(this.parent.trenner);
		block.addAll(content);
		block.add(this.parent.trenner);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("subs"));
		
		return block;
	}
	
	public ArrayList<String> getContent() {
		return content;
	}

	public void setContent(ArrayList<String> content) {
		this.content = content;
		this.type = "manual";
	}
	
	public void addContent(ArrayList<String> content)
	{
		this.content.addAll(content);
		this.type = "manual";
	}
	
	public void genContent(String type) {
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("bla"))
		{
			
		}
		// default
		else
		{
			content.add("sub logit");
			content.add("{");
			content.add("	if (scalar(@_) < 2) {die \"wrong call on subroutine 'logit'\";}");
			content.add("	my $level = shift;");
			content.add("	my $msg = shift;");
			content.add("	my $dest;");
			content.add("	if (@_)");
			content.add("	{");
			content.add("		$dest = shift;");
			content.add("	}");
			content.add("	");
			content.add("	my $timestamp = localtime(time);");
			content.add("	");
			content.add("	my $ausgabestring = '[' . $timestamp . ']:' . $level . ':' . $msg; ");
			content.add("	");
			content.add("	if (!($dest))");
			content.add("	{");
			content.add("		if (defined ${$OPT{'log'}})");
			content.add("		{");
			content.add("			system \"echo \\\"$ausgabestring\\\" >> ${$OPT{'log'}}\";");
			content.add("		}");
			content.add("		else");
			content.add("		{");
			content.add("			print STDERR $ausgabestring.'\n';");
			content.add("		}");
			content.add("	}");
			content.add("	elsif ($dest =~ m/^stderr$/i)");
			content.add("	{");
			content.add("		print STDERR $ausgabestring.'\n';");
			content.add("	}");
			content.add("	elsif ($dest =~ m/^stdout$/i)");
			content.add("	{");
			content.add("		print STDOUT $ausgabestring.'\n';");
			content.add("	}");
			content.add("	elsif (($dest) && (stat $dest))");
			content.add("	{");
			content.add("		system 'echo $ausgabestring >> $dest';");
			content.add("	}");
			content.add("	else");
			content.add("	{");
			content.add("		print STDERR 'unknown logging destination $dest (file does not exist)' . '. assuming stderr';");
			content.add("		print STDERR $ausgabestring.'\n';");
			content.add("	}");
			content.add("}");
			content.add("");
			content.add("sub getvars");
			content.add("{");
			content.add("    my $fp_conf = shift;");
			content.add("");
			content.add("    my %CONF;");
			content.add("    if (!open (CONF, '<$fp_conf')) {die 'cannot read $fp_conf $!\\n';}");
			content.add("    ");
			content.add("    while(<CONF>)");
			content.add("    {");
			content.add("        if    ( $_ =~ m/^#/) {next}");
			content.add("        elsif ( $_ =~ m/^$/) {next}");
			content.add("		elsif ( $_ =~ m/^\\s*$/) {next}");
			content.add("        else");
			content.add("        {");
			content.add("            my @tmp = split('=', $_);");
			content.add("            ");
			content.add("        	# falls unwahr, soll der parameter einen leeren string erhalten");
			content.add("        	unless ($tmp[1]) {$tmp[1] = '';}");
			content.add("        	");
			content.add("        	$tmp[0] =~ s/\\s$//g;");
			content.add("        	$tmp[0] =~ s/^\\s//g;");
			content.add("        	$tmp[1] =~ s/\\s$//g;");
			content.add("        	$tmp[1] =~ s/^\\s//g;");
			content.add("        			");
			content.add("            $CONF{$tmp[0]} = $tmp[1];");
			content.add("        }");
			content.add("    }");
			content.add("    return %CONF;");
			content.add("}");
		}
		
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		this.genContent(type);
	}

}