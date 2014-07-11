package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.security.*;

public class BBusiness
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
	public BBusiness(Script parent)
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
			content.add("# 1) find out if a command exists in <installdir>/bin or globally installed \"which\" for every step and put the call in %COMMAND{$stepname}");
			content.add("# 2) copy all options which value stat as a path to a file or a directory --<key>=<path> as key=path to %FILE . Except for option like --submodel_<key>=<path> go as key=path to %FILE");
			content.add("# 3) copy all options which value does not stat as a path to a file or directory --<key>=<value> as key=value to %VARIABLE . Except for options like --variable_<key>=<value> go as key=value to %VARIABLE");
			content.add("");
			content.add("# Module aus lokalen (mitgelieferten) libs einbinden");
			content.add("# dabei handelt es sich um module, die nicht zur core-distro gehoeren");
			content.add("use HTML::Table;");
			content.add("");
			content.add("&expandPathInOptions();");
			content.add("");
			content.add("my %FILE = getTypedOptions(\"file\");");
			content.add("my %VARIABLE = getTypedOptions(\"variable\");");
			content.add("my $stepsCompleted = 0;");
			content.add("my $id = 'noId';");
			content.add("");
			content.add("addFilesEtc(\\%FILE);");
			content.add("");
			content.add("if(!(getOption('instancedir')))");
			content.add("{");
			content.add("	#-------------------");
			content.add("	# zeitpunkt des aufrufs feststellen");
			content.add("	my ($sec, $min, $hour, $day, $month, $year) = (localtime)[0,1,2,3,4,5];");
			content.add("	$month++;");
			content.add("	$month = '0'.$month if ($month<10);");
			content.add("	$day = '0'.$day if ($day<10);");
			content.add("	$year = $year+1900;");
			content.add("	my $datum = $year.$month.$day;");
			content.add("	my $moment = $hour.$min.$sec;");
			content.add("	setOption('instancedir', getcwd . '/"+this.parent.getName()+"_' . $version . '_' . $datum . '_' . $$);");
			content.add("	#-------------------");
			content.add("}");
			content.add("");
			content.add("elsif(getOption('instancedir') !~ m/^\\//)");
			content.add("{");
			content.add("	setOption('instancedir', getcwd . getOption('instancedir'));");
			content.add("}");
			content.add("");
			content.add("# aus dem instanzverzeichnis eine eindeutige id erstellen");
			content.add("$id = getOption('instancedir');");
			content.add("$id =~ s/^.+(\\d+_\\d+)$/$1/;");
			content.add("");
			content.add("unless($id =~ m/.{8,}/)");
			content.add("{");
			content.add("	$id = sprintf(\"%11-0o\", int(rand(99999999999)));");
			content.add("}");
			content.add("");
			content.add("# ein instanzverzeichnis anlegen");
			content.add("mkdir getOption('instancedir');");
			content.add("");
			content.add("# das basisverzeichnis merken");
			content.add("my $basedir = cwd();");
			content.add("");
			content.add("# und hinein wechseln");
			content.add("chdir getOption('instancedir');");
			content.add("");
			content.add("#-------------------");
			content.add("# erstellen der tabelle");
			content.add("my @INPUT_TABELLE;");
			content.add("");
			content.add("foreach my $actOption (sort @INPUT_OPTIONS)");
			content.add("{");
			content.add("	my @entries = &getOption($actOption);");
			content.add("	");
			content.add("	foreach my $entry (@entries)");
			content.add("	{");
			content.add("		my @row;");
			content.add("		");
			content.add("		# woher");
			content.add("		push(@row, 'user/cb2');");
			content.add("		my $displayEntry;");
			content.add("		");
			content.add("		# existiert und ist ein file?");
			content.add("		if (($entry) && stat $entry)");
			content.add("		{");
			content.add("			push(@row, 'datei');");
			content.add("    		(my $filenam, my $dirs, my $suf) = fileparse ($entry);");
			content.add("    		");
			content.add("    		$displayEntry = \"<a href=\\\"\" . File::Spec->abs2rel($entry) . \"\\\">$filenam</a>\";");
			content.add("		}");
			content.add("		# existiert und ist kein file?");
			content.add("		elsif(($entry))");
			content.add("		{");
			content.add("			push(@row, 'wert');");
			content.add("			$displayEntry = $entry;");
			content.add("		}");
			content.add("		# existiert nicht?");
			content.add("		else");
			content.add("		{");
			content.add("			next;");
			content.add("		}");
			content.add("		");
			content.add("		# label");
			content.add("		push(@row, $actOption);");
			content.add("		");
			content.add("		# beschreibung");
			content.add("		push(@row, $OPTIONS_TABLE{$actOption}->{'text2'});");
			content.add("		");
			content.add("		# inhalt/link");
			content.add("		push(@row, $displayEntry);");
			content.add("		");
			content.add("		# zeile zu tabelle hinzufuegen");
			content.add("		push(@INPUT_TABELLE, \\@row);");
			content.add("	}");
			content.add("}");
			content.add("");
			content.add("my @OUTPUT_TABELLE;");
			content.add("");
			content.add("");
			content.add("# anlegen eines command-hashes");
			content.add("my %COMMAND;");
			content.add("");


		}
		
		// default
		else
		{
			content.add("#");
			content.add("# you may use getConfig(<key>), setConfig(<key>, <value>), getConfigKeys() to deal with data from the configfile if one exists.");
			content.add("# you may use getOption(<key>), setOption(<string>, <value>), getOptionKeys(), and addOption(<string>, <key>) to deal with data from the call-options.");
			content.add("# you may use @ARG for all unknown/unparsed options given. this makes writing wrapperscripts easier.");
			content.add("#");
			content.add("# place your business logic here.");
			content.add("#");
			content.add("\t&logit(\"info\", \"you are now in program $0\");");
			content.add("#");
		}

		this.block.setOrigin("auto");
		this.block.setBlockname("business");
		this.block.setCode(content);
	}
	
	public void addCode(ArrayList<String> code)
	{
		this.block.addCode(code);
	}
	public void addCode(String codeLine)
	{
		this.block.addCode(codeLine);
	}
}