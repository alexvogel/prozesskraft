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
	ArrayList<String> code_logit = new ArrayList<String>();
	ArrayList<String> code_getvars = new ArrayList<String>();
	ArrayList<String> code_initlist = new ArrayList<String>();
	ArrayList<String> content = new ArrayList<String>();
	String type = "default";
	
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BSubs(Script parent)
	{
		this.parent = parent;
		this.initCodeLogit();
		this.initCodeGetvars();
		this.initCodeInitlist();
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
	
	public void genContent(String type)
	{
		ArrayList<String> content = new ArrayList<String>();
		
		if(type.matches("process"))
		{
			content.addAll(this.code_getvars);
			content.addAll(this.code_logit);
			content.addAll(this.code_initlist);
		}
		// default
		else
		{
			content.addAll(this.code_getvars);
			content.addAll(this.code_logit);
		}
		
		this.content = content;
	}

	public String getType() {
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
		this.genContent(type);
	}

	private void initCodeLogit()
	{
		ArrayList<String> code = new ArrayList<String>();
		
		code.add("sub logit");
		code.add("{");
		code.add("	if (scalar(@_) < 2) {die \"wrong call on subroutine 'logit'\";}");
		code.add("	my $level = shift;");
		code.add("	my $msg = shift;");
		code.add("	my $dest;");
		code.add("	if (@_)");
		code.add("	{");
		code.add("		$dest = shift;");
		code.add("	}");
		code.add("	");
		code.add("	my $timestamp = localtime(time);");
		code.add("	");
		code.add("	my $ausgabestring = '[' . $timestamp . ']:' . $level . ':' . $msg; ");
		code.add("	");
		code.add("	if (!($dest))");
		code.add("	{");
		code.add("		if (defined ${$OPT{'log'}})");
		code.add("		{");
		code.add("			system \"echo \\\"$ausgabestring\\\" >> ${$OPT{'log'}}\";");
		code.add("		}");
		code.add("		else");
		code.add("		{");
		code.add("			print STDERR $ausgabestring.\"\\n\";");
		code.add("		}");
		code.add("	}");
		code.add("	elsif ($dest =~ m/^stderr$/i)");
		code.add("	{");
		code.add("		print STDERR $ausgabestring.\"\\n\";");
		code.add("	}");
		code.add("	elsif ($dest =~ m/^stdout$/i)");
		code.add("	{");
		code.add("		print STDOUT $ausgabestring.\"\\n\";");
		code.add("	}");
		code.add("	elsif (($dest) && (stat $dest))");
		code.add("	{");
		code.add("		system 'echo $ausgabestring >> $dest';");
		code.add("	}");
		code.add("	else");
		code.add("	{");
		code.add("		print STDERR 'unknown logging destination $dest (file does not exist)' . '. assuming stderr';");
		code.add("		print STDERR $ausgabestring.\"\\n\";");
		code.add("	}");
		code.add("}");
		code.add("");

		this.code_logit = code;
	}

	private void initCodeGetvars()
	{
		ArrayList<String> code = new ArrayList<String>();
		
		code.add("sub getvars");
		code.add("{");
		code.add("    my $fp_conf = shift;");
		code.add("");
		code.add("    my %CONF;");
		code.add("    if (!open (CONF, '<$fp_conf')) {die 'cannot read $fp_conf $!\\n';}");
		code.add("    ");
		code.add("    while(<CONF>)");
		code.add("    {");
		code.add("        if    ( $_ =~ m/^#/) {next}");
		code.add("        elsif ( $_ =~ m/^$/) {next}");
		code.add("		elsif ( $_ =~ m/^\\s*$/) {next}");
		code.add("        else");
		code.add("        {");
		code.add("            my @tmp = split('=', $_);");
		code.add("            ");
		code.add("        	# falls unwahr, soll der parameter einen leeren string erhalten");
		code.add("        	unless ($tmp[1]) {$tmp[1] = '';}");
		code.add("        	");
		code.add("        	$tmp[0] =~ s/\\s$//g;");
		code.add("        	$tmp[0] =~ s/^\\s//g;");
		code.add("        	$tmp[1] =~ s/\\s$//g;");
		code.add("        	$tmp[1] =~ s/^\\s//g;");
		code.add("        			");
		code.add("            $CONF{$tmp[0]} = $tmp[1];");
		code.add("        }");
		code.add("    }");
		code.add("    return %CONF;");
		code.add("}");

		this.code_getvars = code;
	}

	private void initCodeInitlist()
	{
		ArrayList<String> code = new ArrayList<String>();
		
		code.add("sub initlist");
		code.add("{");
		code.add("	if(scalar(@_) != 10)");
		code.add("	{");
		code.add("		&logit(\"fatal\", \"initlist needs exact 10 (not \".scalar(@_).\") parameters (1=type 2=returnfield 3=fromstep 4=insertrule 5=minoccur 6=maxoccur 7=refARRAYmatch 8=refARRAYlist 9=refHASHvariable 10=refHASHfile\");");
		code.add("		&logit(\"debug\", \"fromobjecttype=$_[0] returnfield=$_[1] fromstep=$_[2] insertrule=$_[3] minoccur=$_[4] maxoccur=$_[5] refARRAYmatch=$_[6] refARRAYlist=$_[7] refHASHvariable=$_[8] refHASHfile=$_[9]\");");
		code.add("		exit(1);");
		code.add("	}");
		code.add("");
		code.add("	my $fromobjecttype = shift;");
		code.add("	my $returnfield = shift;");
		code.add("	my $fromstep = shift;");
		code.add("	my $insertrule = shift;");
		code.add("	my $minoccur = shift;");
		code.add("	my $maxoccur = shift;");
		code.add("	my $refa_match = shift;");
		code.add("	my $refa_list = shift;");
		code.add("	my $refh_variable = shift;");
		code.add("	my $refh_file = shift;");
		code.add("	");
		code.add("	# wenns eine variable ist");
		code.add("	if ($fromobjecttype =~ m/^variable$/i)");
		code.add("	{");
		code.add("		");
		code.add("		# jeden match anwenden");
		code.add("		foreach my $refh_match (@$refa_match)");
		code.add("		{");
		code.add("			my %match = %$refh_match;");
		code.add("			");
		code.add("			# soll nach key gematched werden?");
		code.add("			if ($match{'field'} eq \"key\")");
		code.add("			{");
		code.add("				my @tmp_keys_matched = grep {$_ =~ m/$match{'pattern'}/} keys %$refh_variable;");
		code.add("				foreach my $key (@tmp_keys_matched)");
		code.add("				{");
		code.add("					# wenn insertrule eq 'append'");
		code.add("					if ($insertrule eq \"append\")");
		code.add("					{");
		code.add("						# und returnfield eq 'key'");
		code.add("						if ($returnfield eq \"key\")");
		code.add("						{");
		code.add("							push (@$refa_list, $key);");
		code.add("						}");
		code.add("						elsif ($returnfield eq \"value\")");
		code.add("						{");
		code.add("							push (@$refa_list, $$refh_variable{$key});");
		code.add("						}");
		code.add("					}");
		code.add("					# wenn insertrule eq 'overwrite'");
		code.add("					elsif ($insertrule eq \"overwrite\")");
		code.add("					{");
		code.add("						# nur anfuegen, wenn noch nicht vorhanden");
		code.add("						unless (grep {$_ eq $key} @$refa_list)");
		code.add("						{");
		code.add("							# und returnfield eq 'key'");
		code.add("							if ($returnfield eq \"key\")");
		code.add("							{");
		code.add("								push (@$refa_list, $key);");
		code.add("							}");
		code.add("							elsif ($returnfield eq \"value\")");
		code.add("							{");
		code.add("								push (@$refa_list, $$refh_variable{$key});");
		code.add("							}");
		code.add("						}");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("			# soll nach value gematched werden?");
		code.add("			elsif ($match{'field'} eq \"value\")");
		code.add("			{");
		code.add("				foreach my $key (keys %$refh_variable)");
		code.add("				{");
		code.add("					if ($$refh_variable{$key} =~ m/$match{'pattern'}/)");
		code.add("					{");
		code.add("						# wenn insertrule eq 'append'");
		code.add("						if ($insertrule eq \"append\")");
		code.add("						{");
		code.add("							# und returnfield eq 'key'");
		code.add("							if ($returnfield eq \"key\")");
		code.add("							{");
		code.add("								push (@$refa_list, $key);");
		code.add("							}");
		code.add("							elsif ($returnfield eq \"value\")");
		code.add("							{");
		code.add("								push (@$refa_list, $$refh_variable{$key});");
		code.add("							}");
		code.add("						}");
		code.add("						# wenn insertrule eq 'overwrite'");
		code.add("						elsif ($insertrule eq \"overwrite\")");
		code.add("						{");
		code.add("							# nur anfuegen, wenn noch nicht vorhanden");
		code.add("							unless (grep {$_ eq $key} @$refa_list)");
		code.add("							{");
		code.add("								# und returnfield eq 'key'");
		code.add("								if ($returnfield eq \"key\")");
		code.add("								{");
		code.add("									push (@$refa_list, $key);");
		code.add("								}");
		code.add("								elsif ($returnfield eq \"value\")");
		code.add("								{");
		code.add("									push (@$refa_list, $$refh_variable{$key});");
		code.add("								}");
		code.add("							}");
		code.add("						}");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("		}");
		code.add("	}");
		code.add("	# wenns ein file ist");
		code.add("	elsif ($fromobjecttype =~ m/^file$/i)");
		code.add("	{");
		code.add("		# jeden match anwenden");
		code.add("		foreach my $refh_match (@$refa_match)");
		code.add("		{");
		code.add("			my %match = %$refh_match;");
		code.add("			");
		code.add("			# soll nach key gematched werden?");
		code.add("			if ($match{'field'} eq \"key\")");
		code.add("			{");
		code.add("				my @tmp_keys_matched = grep {$_ =~ m/$match{'pattern'}/} keys %$refh_file;");
		code.add("				foreach my $key (@tmp_keys_matched)");
		code.add("				{");
		code.add("					# wenn insertrule eq 'append'");
		code.add("					if ($insertrule eq \"append\")");
		code.add("					{");
		code.add("						# und returnfield eq 'key'");
		code.add("						if ($returnfield eq \"key\")");
		code.add("						{");
		code.add("							push (@$refa_list, $key);");
		code.add("						}");
		code.add("						elsif ($returnfield eq \"value\")");
		code.add("						{");
		code.add("							push (@$refa_list, $$refh_file{$key});");
		code.add("						}");
		code.add("					}");
		code.add("					# wenn insertrule eq 'overwrite'");
		code.add("					elsif ($insertrule eq \"overwrite\")");
		code.add("					{");
		code.add("						# nur anfuegen, wenn noch nicht vorhanden");
		code.add("						unless (grep {$_ eq $key} @$refa_list)");
		code.add("						{");
		code.add("							# und returnfield eq 'key'");
		code.add("							if ($returnfield eq \"key\")");
		code.add("							{");
		code.add("								push (@$refa_list, $key);");
		code.add("							}");
		code.add("							elsif ($returnfield eq \"value\")");
		code.add("							{");
		code.add("								push (@$refa_list, $$refh_file{$key});");
		code.add("							}");
		code.add("						}");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("			# soll nach absfilename gematched werden?");
		code.add("			elsif ($match{'field'} eq \"absfilename\")");
		code.add("			{");
		code.add("				foreach my $key (keys %$refh_file)");
		code.add("				{");
		code.add("					if ($$refh_variable{$key} =~ m/$match{'pattern'}/)");
		code.add("					{");
		code.add("						# wenn insertrule eq 'append'");
		code.add("						if ($insertrule eq \"append\")");
		code.add("						{");
		code.add("							# und returnfield eq 'key'");
		code.add("							if ($returnfield eq \"key\")");
		code.add("							{");
		code.add("								push (@$refa_list, $key);");
		code.add("							}");
		code.add("							elsif ($returnfield eq \"value\")");
		code.add("							{");
		code.add("								push (@$refa_list, $$refh_file{$key});");
		code.add("							}");
		code.add("						}");
		code.add("						# wenn insertrule eq 'overwrite'");
		code.add("						elsif ($insertrule eq \"overwrite\")");
		code.add("						{");
		code.add("							# nur anfuegen, wenn noch nicht vorhanden");
		code.add("							unless (grep {$_ eq $key} @$refa_list)");
		code.add("							{");
		code.add("								# und returnfield eq 'key'");
		code.add("								if ($returnfield eq \"key\")");
		code.add("								{");
		code.add("									push (@$refa_list, $key);");
		code.add("								}");
		code.add("								elsif ($returnfield eq \"value\")");
		code.add("								{");
		code.add("									push (@$refa_list, $$refh_file{$key});");
		code.add("								}");
		code.add("							}");
		code.add("						}");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("		}");
		code.add("	}");
		code.add("	# ueberpruefen ob minoccur und maxoccur eingehalten wird");
		code.add("	my $anzahl_items = scalar(@$refa_list);");
		code.add("	if ($anzahl_items < $minoccur)");
		code.add("	{");
		code.add("		logit(\"error\", \"list contains \" . $anzahl_items . \" items. that is less than the needed $minoccur\");");
		code.add("		exit(1);");
		code.add("	}");
		code.add("	if ($anzahl_items > $maxoccur)");
		code.add("	{");
		code.add("		logit(\"error\", \"list contains \" . $anzahl_items . \" items. that is more than the allowed $maxoccur\");");
		code.add("		exit(1);");
		code.add("	}");
		code.add("}");

		this.code_initlist = code;
	}
}
