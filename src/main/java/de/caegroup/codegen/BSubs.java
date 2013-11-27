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
	public String getBlock()
	{
		
		String content = getContent();
		BigInteger md5 = this.parent.genMd5(content);
		return this.parent.genBlockStart("subs") + "# md5="+md5+"\n" + content + "# md5="+md5+"\n" + this.parent.genBlockEnd("subs");
	}
	
	private String getContent()
	{
		String content = "";

		content      += "# place your business logic here.\n";
		content      += "sub logit\n";
		content      += "{\n";
		content      += "	if (scalar(@_) < 2) {die \"wrong call on subroutine 'logit'\";}\n";
		content      += "	my $level = shift;\n";
		content      += "	my $msg = shift;\n";
		content      += "	my $dest;\n";
		content      += "	if (@_)\n";
		content      += "	{\n";
		content      += "		$dest = shift;\n";
		content      += "	}\n";
		content      += "	\n";
		content      += "	my $timestamp = localtime(time);\n";
		content      += "	\n";
		content      += "	my $ausgabestring = '[' . $timestamp . ']:' . $level . ':' . $msg; \n";
		content      += "	\n";
		content      += "	if (!($dest))\n";
		content      += "	{\n";
		content      += "		if (defined ${$OPT{'log'}})\n";
		content      += "		{\n";
		content      += "			system \"echo \\\"$ausgabestring\\\" >> ${$OPT{'log'}}\";\n";
		content      += "		}\n";
		content      += "		else\n";
		content      += "		{\n";
		content      += "			print STDERR $ausgabestring.'\n';\n";
		content      += "		}\n";
		content      += "	}\n";
		content      += "	elsif ($dest =~ m/^stderr$/i)\n";
		content      += "	{\n";
		content      += "		print STDERR $ausgabestring.'\n';\n";
		content      += "	}\n";
		content      += "	elsif ($dest =~ m/^stdout$/i)\n";
		content      += "	{\n";
		content      += "		print STDOUT $ausgabestring.'\n';\n";
		content      += "	}\n";
		content      += "	elsif (($dest) && (stat $dest))\n";
		content      += "	{\n";
		content      += "		system 'echo $ausgabestring >> $dest';\n";
		content      += "	}\n";
		content      += "	else\n";
		content      += "	{\n";
		content      += "		print STDERR 'unknown logging destination $dest (file does not exist)' . '. assuming stderr';\n";
		content      += "		print STDERR $ausgabestring.'\n';\n";
		content      += "	}\n";
		content      += "}\n";
		content      += "\n";
		content      += "sub getvars\n";
		content      += "{\n";
		content      += "    my $fp_conf = shift;\n";
		content      += "\n";
		content      += "    my %CONF;\n";
		content      += "    if (!open (CONF, '<$fp_conf')) {die 'cannot read $fp_conf $!\\n';}\n";
		content      += "    \n";
		content      += "    while(<CONF>)\n";
		content      += "    {\n";
		content      += "        if    ( $_ =~ m/^#/) {next}\n";
		content      += "        elsif ( $_ =~ m/^$/) {next}\n";
		content      += "		elsif ( $_ =~ m/^\\s*$/) {next}\n";
		content      += "        else\n";
		content      += "        {\n";
		content      += "            my @tmp = split('=', $_);\n";
		content      += "            \n";
		content      += "        	# falls unwahr, soll der parameter einen leeren string erhalten\n";
		content      += "        	unless ($tmp[1]) {$tmp[1] = '';}\n";
		content      += "        	\n";
		content      += "        	$tmp[0] =~ s/\\s$//g;\n";
		content      += "        	$tmp[0] =~ s/^\\s//g;\n";
		content      += "        	$tmp[1] =~ s/\\s$//g;\n";
		content      += "        	$tmp[1] =~ s/^\\s//g;\n";
		content      += "        			\n";
		content      += "            $CONF{$tmp[0]} = $tmp[1];\n";
		content      += "        }\n";
		content      += "    }\n";
		content      += "    return %CONF;\n";
		content      += "}\n";

		return content;
	}
}