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
	Block block = new Block();

	ArrayList<String> code_logit = new ArrayList<String>();
	ArrayList<String> code_getvars = new ArrayList<String>();
	ArrayList<String> code_initlist = new ArrayList<String>();
	ArrayList<String> code_girlande = new ArrayList<String>();
	ArrayList<String> code_getsetoptionsconfigs = new ArrayList<String>();
	ArrayList<String> code_resolve = new ArrayList<String>();
	ArrayList<String> content = new ArrayList<String>();
	/*----------------------------
	  constructors
	----------------------------*/
	public BSubs(Script parent)
	{
		this.parent = parent;
		this.initCodeLogit();
		this.initCodeGetvars();
		this.initCodeInitlist();
		this.initCodeGirlande();
		this.initCodeGetsetoptionsconfigs();
		this.initCodeResolve();
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
			content.addAll(this.code_getvars);
			content.addAll(this.code_logit);
			content.addAll(this.code_initlist);
			content.addAll(this.code_girlande);
			content.addAll(this.code_getsetoptionsconfigs);
			content.addAll(this.code_resolve);
		}
		// default
		else
		{
			content.addAll(this.code_getvars);
			content.addAll(this.code_logit);
			content.addAll(this.code_getsetoptionsconfigs);
		}
		
		this.block.setOrigin("auto");
		this.block.setBlockname("subs");
		this.block.setCode(content);
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
		code.add("# wenn loglevel == debug, ABER --debug nicht gesetzt wurde, soll nicht geloggt werden");
		code.add("		if ( ($level eq \"debug\") &! getOption(\"debug\") )");
		code.add("		{");
		code.add("			return;");
		code.add("		}");
		code.add("");
		code.add("	my $timestamp = localtime(time);");
		code.add("	");
		code.add("	my $ausgabestring = '[' . $timestamp . ']:' . $level . ':' . $msg; ");
		code.add("	");
		code.add("	if (!($dest))");
		code.add("	{");
		code.add("		my $logfile = &getOption(\"log\");");
		code.add("		if ($logfile)");
		code.add("		{");
		code.add("			system \"echo \\\"$ausgabestring\\\" >> $logfile\";");
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
		code.add("    if (!open (CONF, \"<$fp_conf\")) {die \"cannot read $fp_conf $!\\n\";}");
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
		code.add("		&logit(\"fatal\", \"initlist needs exact 10 (not \".scalar(@_).\") parameters (1=type 2=returnfield 3=fromstep 4=insertrule 5=minoccur 6=maxoccur 7=refARRAYmatch 8=refARRAYlist 9=refARRAYvariable 10=refARRAYfile\");");
		code.add("		&logit(\"debug\", \"fromobjecttype=$_[0] returnfield=$_[1] fromstep=$_[2] insertrule=$_[3] minoccur=$_[4] maxoccur=$_[5] refARRAYmatch=$_[6] refARRAYlist=$_[7] refARRAYvariable=$_[8] refARRAYfile=$_[9]\");");
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
		code.add("	my $refa_variable = shift;");
		code.add("	my $refa_file = shift;");
		code.add("	");
		code.add("	logit(\"debug\", \"- initialisierung startet\");");
		code.add("	# wenns eine variable ist");
		code.add("	if ($fromobjecttype =~ m/^variable$/i)");
		code.add("	{");
		code.add("		");
		code.add("		logit(\"debug\", \"-- in der aktuellen initialisierung sollen nur variablen beruecksichtigt werden.\");");
		code.add("		# auf jedes key=value Paarung sollen alle matches angewendet werden");
		code.add("		foreach my $refa_optionPair (@$refa_variable)");
		code.add("		{");
		code.add("			my $key = $$refa_optionPair[0];");
		code.add("			my $value = $$refa_optionPair[1];");
		code.add("			");
		code.add("			logit(\"debug\", \"--- variable $key=$value soll auf alle matches passen\");");
		code.add("");
		code.add("			my $doesItMatch = 1;");
		code.add("			");
		code.add("			foreach my $refh_match (@$refa_match)");
		code.add("			{");
		code.add("				my %match = %$refh_match;");
		code.add("				");
		code.add("				if($match{'field'} eq \"key\")");
		code.add("				{");
		code.add("					logit(\"debug\", \"---- passt das? $key =~ m/$match{'pattern'}/\");");
		code.add("					if ($key =~ m/$match{'pattern'}/)");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- JA\");");
		code.add("					}");
		code.add("					else");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- NEIN, Abbruch der Matchueberpruefung fuer die aktuelle Variable\");");
		code.add("						$doesItMatch = 0;");
		code.add("						last;");
		code.add("					}");
		code.add("				}");
		code.add("				elsif($match{'field'} eq \"value\")");
		code.add("				{");
		code.add("					logit(\"debug\", \"---- passt das? $value =~ m/$match{'pattern'}/\");");
		code.add("					if ($value =~ m/$match{'pattern'}/)");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- JA\");");
		code.add("					}");
		code.add("					else");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- NEIN, Abbruch der Matchueberpruefung fuer die aktuelle Variable\");");
		code.add("						$doesItMatch = 0;");
		code.add("						last;");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("			");
		code.add("			if ($doesItMatch)");
		code.add("			{");
		code.add("			");
		code.add("				# wenn insertrule eq 'append'");
		code.add("				if ($insertrule eq \"append\")");
		code.add("				{");
		code.add("					# und returnfield eq 'key'");
		code.add("					if ($returnfield eq \"key\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt: \" . $key);");
		code.add("						push (@$refa_list, $key);");
		code.add("					}");
		code.add("					elsif ($returnfield eq \"value\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt: \" . $value);");
		code.add("						push (@$refa_list, $value);");
		code.add("					}");
		code.add("				}");
		code.add("				# wenn insertrule eq 'overwrite'");
		code.add("				elsif ($insertrule eq \"overwrite\")");
		code.add("				{");
		code.add("					# und returnfield eq 'key'");
		code.add("					if ($returnfield eq \"key\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt, weil er noch nicht vorhanden ist: \" . $key);");
		code.add("						push (@$refa_list, $key);");
		code.add("					}");
		code.add("					elsif ($returnfield eq \"value\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt, weil er noch nicht vorhanden ist: \" . $value);");
		code.add("						push (@$refa_list, $value);");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("		}");
		code.add("	}");
		code.add("		");
		code.add("	# wenns ein file ist");
		code.add("	elsif ($fromobjecttype =~ m/^file$/i)");
		code.add("	{");
		code.add("		");
		code.add("		logit(\"debug\", \"-- in der aktuellen initialisierung sollen nur files beruecksichtigt werden.\");");
		code.add("		# auf jedes key=value Paarung sollen alle matches angewendet werden");
		code.add("		foreach my $refa_optionPair (@$refa_file)");
		code.add("		{");
		code.add("			my $key = $$refa_optionPair[0];");
		code.add("			my $absfilename = $$refa_optionPair[1];");
		code.add("			");
		code.add("			logit(\"debug\", \"--- file $key=$absfilename soll auf alle matches passen\");");
		code.add("");
		code.add("			my $doesItMatch = 1;");
		code.add("			");
		code.add("			foreach my $refh_match (@$refa_match)");
		code.add("			{");
		code.add("				my %match = %$refh_match;");
		code.add("				");
		code.add("				if($match{'field'} eq \"key\")");
		code.add("				{");
		code.add("					logit(\"debug\", \"---- passt das? $key =~ m/$match{'pattern'}/\");");
		code.add("					if ($key =~ m/$match{'pattern'}/)");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- JA\");");
		code.add("					}");
		code.add("					else");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- NEIN, Abbruch der Matchueberpruefung fuer das aktuelle File\");");
		code.add("						$doesItMatch = 0;");
		code.add("						last;");
		code.add("					}");
		code.add("				}");
		code.add("				elsif($match{'field'} eq \"absfilename\")");
		code.add("				{");
		code.add("					logit(\"debug\", \"---- passt das? $absfilename =~ m/$match{'pattern'}/\");");
		code.add("					if ($absfilename =~ m/$match{'pattern'}/)");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- JA\");");
		code.add("					}");
		code.add("					else");
		code.add("					{");
		code.add("						logit(\"debug\", \"----- NEIN, Abbruch der Matchueberpruefung fuer das aktuelle File\");");
		code.add("						$doesItMatch = 0;");
		code.add("						last;");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("			");
		code.add("			if ($doesItMatch)");
		code.add("			{");
		code.add("			");
		code.add("				# wenn insertrule eq 'append'");
		code.add("				if ($insertrule eq \"append\")");
		code.add("				{");
		code.add("					# und returnfield eq 'key'");
		code.add("					if ($returnfield eq \"key\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt: \" . $key);");
		code.add("						push (@$refa_list, $key);");
		code.add("					}");
		code.add("					elsif ($returnfield eq \"absfilename\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt: \" . $absfilename);");
		code.add("						push (@$refa_list, $absfilename);");
		code.add("					}");
		code.add("				}");
		code.add("				# wenn insertrule eq 'overwrite'");
		code.add("				elsif ($insertrule eq \"overwrite\")");
		code.add("				{");
		code.add("					# und returnfield eq 'key'");
		code.add("					if ($returnfield eq \"key\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt, weil er noch nicht vorhanden ist: \" . $key);");
		code.add("						push (@$refa_list, $key);");
		code.add("					}");
		code.add("					elsif ($returnfield eq \"absfilename\")");
		code.add("					{");
		code.add("						logit(\"debug\", \"------ dieser string wird an die liste angehaengt, weil er noch nicht vorhanden ist: \" . $absfilename);");
		code.add("						push (@$refa_list, $absfilename);");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("		}");
		code.add("	}");
		code.add("	# ueberpruefen ob minoccur und maxoccur eingehalten wird");
		code.add("	my $anzahl_items = scalar(@$refa_list);");
		code.add("	if ($anzahl_items < $minoccur)");
		code.add("	{");
		code.add("		my $tmp;");
		code.add("		logit(\"fatal\", \"list initializes \" . $anzahl_items . \" items. that is less than the needed $minoccur\");");
		code.add("		logit(\"debug\", \"after: fromobjecttype=$fromobjecttype\");");
		code.add("		logit(\"debug\", \"after: returnfield=$returnfield\");");
		code.add("		logit(\"debug\", \"after: fromstep=$fromstep\");");
		code.add("		logit(\"debug\", \"after: insertrule=$insertrule\");");
		code.add("		logit(\"debug\", \"after: minoccur=$minoccur\");");
		code.add("		logit(\"debug\", \"after: maxoccur=$maxoccur\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_match) {$tmp = join(\", \", @$refa_match)};");
		code.add("		logit(\"debug\", \"after: refa_match=$refa_match (\" . $tmp . \")\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_list) {$tmp = join(\", \", @$refa_list)};");
		code.add("		logit(\"debug\", \"after: refa_list=$refa_list (\" . $tmp . \")\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_variable) {$tmp = join(\", \", @$refa_variable)};");
		code.add("		logit(\"debug\", \"after: refa_variable=$refa_variable (\" . $tmp . \")\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_file) {$tmp = join(\", \", @$refa_file)};");
		code.add("		logit(\"debug\", \"after: refa_file=$refa_file (\" . $tmp . \")\");");
		code.add("");
		code.add("		exit(1);");
		code.add("	}");
		code.add("	if ($anzahl_items > $maxoccur)");
		code.add("	{");
		code.add("		my $tmp;");
		code.add("		logit(\"fatal\", \"list initializes \" . $anzahl_items . \" items. that is more than the allowed $maxoccur\");");
		code.add("		logit(\"debug\", \"after: fromobjecttype=$fromobjecttype\");");
		code.add("		logit(\"debug\", \"after: returnfield=$returnfield\");");
		code.add("		logit(\"debug\", \"after: fromstep=$fromstep\");");
		code.add("		logit(\"debug\", \"after: insertrule=$insertrule\");");
		code.add("		logit(\"debug\", \"after: minoccur=$minoccur\");");
		code.add("		logit(\"debug\", \"after: maxoccur=$maxoccur\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_match) {$tmp = join(\", \", @$refa_match)};");
		code.add("		logit(\"debug\", \"after: refa_match=$refa_match (\" . $tmp . \")\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_list) {$tmp = join(\", \", @$refa_list)};");
		code.add("		logit(\"debug\", \"after: refa_list=$refa_list (\" . $tmp . \")\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_variable) {$tmp = join(\", \", @$refa_variable)};");
		code.add("		logit(\"debug\", \"after: refa_variable=$refa_variable (\" . $tmp . \")\");");
		code.add("");
		code.add("		$tmp = \"\";");
		code.add("		if(defined @$refa_file) {$tmp = join(\", \", @$refa_file)};");
		code.add("		logit(\"debug\", \"after: refa_file=$refa_file (\" . $tmp . \")\");");
		code.add("");
		code.add("		exit(1);");
		code.add("	}");
		code.add("}");

		this.code_initlist = code;
	}
	
	private void initCodeGirlande()
	{
		ArrayList<String> code = new ArrayList<String>();
		
		code.add("sub girlande_stepstart");
		code.add("{");
		code.add("\tmy $stepname = shift;");
		code.add("");
		code.add("\tlogit(\"info\", \"###############################################\");");
		code.add("\tlogit(\"info\", \" START PROCESS STEP: $stepname\");");
		code.add("\tlogit(\"info\", \"###############################################\");");
		code.add("}");
		code.add("sub girlande_stepend");
		code.add("{");
		code.add("\tmy $stepname = shift;");
		code.add("");
		code.add("\tlogit(\"info\", \"###############################################\");");
		code.add("\tlogit(\"info\", \"   END PROCESS STEP: $stepname\");");
		code.add("\tlogit(\"info\", \"###############################################\");");
		code.add("}");
		code.add("sub girlande_processend");
		code.add("{");
		code.add("\tlogit(\"info\", \"###############################################\");");
		code.add("\tlogit(\"info\", \"   END PROCESS\");");
		code.add("\tlogit(\"info\", \"###############################################\");");
		code.add("}");

		this.code_girlande = code;
	}
	
	private void initCodeGetsetoptionsconfigs()
	{
		ArrayList<String> code = new ArrayList<String>();
		
		code.add("sub getOptionKeys");
		code.add("{");
		code.add("	return keys %OPT;");
		code.add("}");
		code.add("");
		code.add("sub getOption");
		code.add("{");
		code.add("	my $key = shift;");
		code.add("	");
		code.add("	# wenn option nicht existiert oder die anzahl der values innerhalb der option = 0 ist");
		code.add("	if (!(defined $OPT{$key}))");
		code.add("	{");
		code.add("		return undef;");
		code.add("	}");
		code.add("	");
		code.add("	elsif (ref($OPT{$key}) eq \"SCALAR\")");
		code.add("	{");
		code.add("		if (!defined ${$OPT{$key}})");
		code.add("		{");
		code.add("			return undef;");
		code.add("		}");
		code.add("		else");
		code.add("		{");
		code.add("			return ${$OPT{$key}}");
		code.add("		}");
		code.add("	}");
		code.add("	");
		code.add("	elsif (ref($OPT{$key}) eq \"ARRAY\")");
		code.add("	{");
		code.add("		if ( (!defined @{$OPT{$key}}) || (scalar @{$OPT{$key}} == 0) )");
		code.add("		{");
		code.add("			return undef;");
		code.add("		}");
		code.add("		# wenn es genau eine value fuer die option gibt ");
		code.add("		elsif (scalar(@{$OPT{$key}}) == 1)");
		code.add("		{");
		code.add("			return ${$OPT{$key}}[0]");
		code.add("		}");
		code.add("		# wenn es mehrere values fuer die option gibt");
		code.add("		else");
		code.add("		{");
		code.add("			return @{$OPT{$key}}");
		code.add("		}");
		code.add("	}");
		code.add("}");
		code.add("");
		code.add("sub setOption");
		code.add("{");
		code.add("	my $key = shift;");
		code.add("	");
		code.add("	if (ref($OPT{$key}) eq \"SCALAR\")");
		code.add("	{");
		code.add("		if (@_ > 1)");
		code.add("		{");
		code.add("			&logit(\"error\", \"cannot apply a list (@_) to a scalar option ($key).\");");
		code.add("		}");
		code.add("		");
		code.add("		else");
		code.add("		{");
		code.add("			${$OPT{$key}} = $_[0];");
		code.add("		}");
		code.add("	}");
		code.add("	");
		code.add("	elsif (ref($OPT{$key}) eq \"ARRAY\")");
		code.add("	{");
		code.add("		@{$OPT{$key}} = @_;");
		code.add("	}");
		code.add("}");
		code.add("");
		code.add("sub addOption");
		code.add("{");
		code.add("	my $key = shift;");
		code.add("	");
		code.add("	if (ref($OPT{$key}) eq \"ARRAY\")");
		code.add("	{");
		code.add("		push @{$OPT{$key}}, @_;");
		code.add("	}");
		code.add("}");
		code.add("");
		code.add("sub getConfig");
		code.add("{");
		code.add("	my $key = shift;");
		code.add("	if (defined $CONF{$key})");
		code.add("	{");
		code.add("		return $CONF{$key};");
		code.add("	}");
		code.add("}");
		code.add("");
		code.add("sub setConfig");
		code.add("{");
		code.add("	my $key = shift;");
		code.add("	my $value = shift;");
		code.add("	");
		code.add("	$CONF{$key} = $value;");
		code.add("}");
		code.add("");
		code.add("sub getTypedOptions");
		code.add("{");
		code.add("");
		code.add("	my $type = shift;");
		code.add("	");
		code.add("	my %VARIABLE;");
		code.add("	my %FILE;");
		code.add("");
		code.add("	logit(\"debug\", \"##### START identifing whether given options are a \" . uc($type) . \" #####\");");
		code.add("	");
		code.add("	unless( $type =~ m/variable|file/ )");
		code.add("	{");
		code.add("		logit(\"fatal\", \"unknown type $type. only permitted one of these: variable, file, all\");");
		code.add("		exit(1);");
		code.add("	}");
		code.add("	");
		code.add("	foreach my $option (getOptionKeys())");
		code.add("	{");
		code.add("	");
		code.add("		my @allValuesOfACertainOption;");
		code.add("		if(getOption($option))");
		code.add("		{");
		code.add("			@allValuesOfACertainOption = getOption($option);");
		code.add("		}");
		code.add("	");
		code.add("		my $isFile = 1;	#erst mal 'true'");
		code.add("		foreach my $actValue (@allValuesOfACertainOption)");
		code.add("		{");
		code.add("			unless (stat $actValue) {$isFile = 0;}");
		code.add("		}");
		code.add("	");
		code.add("		if(scalar(@allValuesOfACertainOption))");
		code.add("		{");
		code.add("#			logit(\"debug\", \"option --\" . $option . \" exists\");");
		code.add("");
		code.add("			if($isFile && $type =~ m/file/i)");
		code.add("			{");
		code.add("		");
		code.add("				logit(\"debug\", \"option --\" . $option.\" is identified as a FILE-option\");");
		code.add("				if ($option =~ m/submodel_(.+)/i)");
		code.add("				{");
		code.add("					foreach (@allValuesOfACertainOption)");
		code.add("					{");
		code.add("						logit(\"debug\", \"memorizing as FILE: $1->$_\");");
		code.add("						push(@{$FILE{'root'}}, [$1, $_]);");
		code.add("					}");
		code.add("				}");
		code.add("				else");
		code.add("				{");
		code.add("					foreach (@allValuesOfACertainOption)");
		code.add("					{");
		code.add("						logit(\"debug\", \"memorizing as FILE: $option->$_\");");
		code.add("						push(@{$FILE{'root'}}, [$option, $_]);");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("			elsif($type =~ m/variable/i)");
		code.add("			{");
		code.add("				logit(\"debug\", \"option --\" . $option.\" is identified as a VARIABLE-option\");");
		code.add("				if ($option =~ m/variable_(.+)/i)");
		code.add("				{");
		code.add("					foreach (@allValuesOfACertainOption)");
		code.add("					{");
		code.add("						logit(\"debug\", \"memorizing as VARIABLE: $1->$_\");");
		code.add("						push(@{$VARIABLE{'root'}}, [$1, $_]);");
		code.add("					}");
		code.add("				}");
		code.add("				else");
		code.add("				{");
		code.add("					foreach (@allValuesOfACertainOption)");
		code.add("					{");
		code.add("						logit(\"debug\", \"memorizing as VARIABLE: $option->$_\");");
		code.add("						push(@{$VARIABLE{'root'}}, [$option, $_]);");
		code.add("					}");
		code.add("				}");
		code.add("			}");
		code.add("		}");
		code.add("	}");
		code.add("	");
		code.add("# falls keine eintraege in root existieren, soll eine leere annonyme liste angelegt werden");
		code.add("unless($FILE{'root'}) {$FILE{'root'} = [];}");
		code.add("unless($VARIABLE{'root'}) {$VARIABLE{'root'} = [];}");
		code.add("");
		code.add("	logit(\"debug\", \"##### END   identifing whether given options are a \" . uc($type) . \" #####\");");
		code.add("	if ($type =~ /file/i) {return %FILE;}");
		code.add("	elsif ($type =~ /variable/i) {return %VARIABLE;}");
		code.add("");
		code.add("}");
		
		
		this.code_getsetoptionsconfigs = code;
	}
	private void initCodeResolve()
	{
		ArrayList<String> code = new ArrayList<String>();
		
		code.add("sub resolve");
		code.add("{");
		code.add("	my $string = shift;");
		code.add("	my $refh_lists = shift;");
		code.add("	");
		code.add("	if($string =~ m/\\{\\$.+\\}/)");
		code.add("	{");
		code.add("		&logit(\"debug\", \"string has to be resolved because it contains list items ($string)\");");
		code.add("		my @mentionedLists = $string =~ /\\{\\$(.+?)\\}/g;");
		code.add("");
		code.add("		foreach my $list (@mentionedLists)");
		code.add("		{");
		code.add("#			&logit(\"debug\", \"placeholder for list '$list' will be replaced\");");
		code.add("			my $firstItemOfList = (${$$refh_lists{$list}}[0]);");
		code.add("");
		code.add("			if($string =~ s/\\{\\$$list\\}/$firstItemOfList/g)");
		code.add("			{");
		code.add("				&logit(\"debug\", \"---- substitution in call successfull: (/{\\$$list}/ => /$firstItemOfList/)\");");
		code.add("			}");
		code.add("			else");
		code.add("			{");
		code.add("				&logit(\"debug\", \"---- substitution in call NOT successfull - somethings wrong: (/{\\$$list}/ => /$firstItemOfList/)\");");
		code.add("			}");
		code.add("		}");
		code.add("	}");
		code.add("	&logit(\"debug\", \"string has been resolved to (string=$string)\");");
		code.add("");
		code.add("	return $string;");
		code.add("}");

		this.code_resolve = code;
	}
	
	
}
