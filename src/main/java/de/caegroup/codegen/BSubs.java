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
	ArrayList<Option> option = new ArrayList<Option>();
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BSubs(Script parent)
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
		block.addAll(this.parent.genBlockStart("subs"));
		block.add("# md5="+md5);
		block.addAll(content);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("subs"));
		
		return block;
	}
	
	private ArrayList<String> getContent()
	{
		ArrayList<String> content = new ArrayList<String>();

		content.add("# place your business logic here.\n");
		content.add("sub logit\n");
		content.add("{\n");
		content.add("	if (scalar(@_) < 2) {die \"wrong call on subroutine 'logit'\";}\n");
		content.add("	my $level = shift;\n");
		content.add("	my $msg = shift;\n");
		content.add("	my $dest;\n");
		content.add("	if (@_)\n");
		content.add("	{\n");
		content.add("		$dest = shift;\n");
		content.add("	}\n");
		content.add("	\n");
		content.add("	my $timestamp = localtime(time);\n");
		content.add("	\n");
		content.add("	my $ausgabestring = '[' . $timestamp . ']:' . $level . ':' . $msg; \n");
		content.add("	\n");
		content.add("	if (!($dest))\n");
		content.add("	{\n");
		content.add("		if (defined ${$OPT{'log'}})\n");
		content.add("		{\n");
		content.add("			system \"echo \\\"$ausgabestring\\\" >> ${$OPT{'log'}}\";\n");
		content.add("		}\n");
		content.add("		else\n");
		content.add("		{\n");
		content.add("			print STDERR $ausgabestring.'\n';\n");
		content.add("		}\n");
		content.add("	}\n");
		content.add("	elsif ($dest =~ m/^stderr$/i)\n");
		content.add("	{\n");
		content.add("		print STDERR $ausgabestring.'\n';\n");
		content.add("	}\n");
		content.add("	elsif ($dest =~ m/^stdout$/i)\n");
		content.add("	{\n");
		content.add("		print STDOUT $ausgabestring.'\n';\n");
		content.add("	}\n");
		content.add("	elsif (($dest) && (stat $dest))\n");
		content.add("	{\n");
		content.add("		system 'echo $ausgabestring >> $dest';\n");
		content.add("	}\n");
		content.add("	else\n");
		content.add("	{\n");
		content.add("		print STDERR 'unknown logging destination $dest (file does not exist)' . '. assuming stderr';\n");
		content.add("		print STDERR $ausgabestring.'\n';\n");
		content.add("	}\n");
		content.add("}\n");
		content.add("\n");
		content.add("sub getvars\n");
		content.add("{\n");
		content.add("    my $fp_conf = shift;\n");
		content.add("\n");
		content.add("    my %CONF;\n");
		content.add("    if (!open (CONF, '<$fp_conf')) {die 'cannot read $fp_conf $!\\n';}\n");
		content.add("    \n");
		content.add("    while(<CONF>)\n");
		content.add("    {\n");
		content.add("        if    ( $_ =~ m/^#/) {next}\n");
		content.add("        elsif ( $_ =~ m/^$/) {next}\n");
		content.add("		elsif ( $_ =~ m/^\\s*$/) {next}\n");
		content.add("        else\n");
		content.add("        {\n");
		content.add("            my @tmp = split('=', $_);\n");
		content.add("            \n");
		content.add("        	# falls unwahr, soll der parameter einen leeren string erhalten\n");
		content.add("        	unless ($tmp[1]) {$tmp[1] = '';}\n");
		content.add("        	\n");
		content.add("        	$tmp[0] =~ s/\\s$//g;\n");
		content.add("        	$tmp[0] =~ s/^\\s//g;\n");
		content.add("        	$tmp[1] =~ s/\\s$//g;\n");
		content.add("        	$tmp[1] =~ s/^\\s//g;\n");
		content.add("        			\n");
		content.add("            $CONF{$tmp[0]} = $tmp[1];\n");
		content.add("        }\n");
		content.add("    }\n");
		content.add("    return %CONF;\n");
		content.add("}\n");

		return content;
	}
}