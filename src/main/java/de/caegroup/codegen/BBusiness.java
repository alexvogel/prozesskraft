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
			content.add("# 1) copy all known options with pattern =~ /--submodel_<key>=<path>/ as key=path to %FILE ");
			content.add("# 1) copy all options which value stat as a path to a file or a directory --<key>=<path> as key=path to %FILE . Except for option like --submodel_<key>=<path> go as key=path to %FILE");
			content.add("		# 2) copy all options which value does not stat as a path to a file or directory --<key>=<value> as key=value to %VARIABLE . Except for options like --variable_<key>=<value> go as key=value to %VARIABLE");
			content.add("				");
			content.add("		my %VARIABLE;");
			content.add("		my %FILE;");
			content.add("		my %ALL;");
			content.add("		");
			content.add("		logit(\"debug\", \"##### START identifing whether given options are a FILE or a VARIABLE #####\");");
			content.add("		foreach my $option (getOptionKeys())");
			content.add("		{");
			content.add("			logit(\"debug\", \"option --\" . $option . \" exists\");");
			content.add("			");
			content.add("			my @allValuesOfACertainOption;");
			content.add("			if(getOption($option))");
			content.add("			{");
			content.add("			@allValuesOfACertainOption = getOption($option);");
			content.add("			}");
			content.add("			");
			content.add("			my $isFile = 1;");
			content.add("			foreach my $actValue (@allValuesOfACertainOption)");
			content.add("			{");
			content.add("				unless (stat $actValue) {$isFile = 0;}");
			content.add("			}");
			content.add("			");
			content.add("			if($isFile)");
			content.add("			{");
			content.add("				");
			content.add("				logit(\"debug\", \"option --\" . $option.\" is identified as a FILE-option\");");
			content.add("				if ($option =~ m/submodel_(.+)/i)");
			content.add("				{");
			content.add("					foreach (@allValuesOfACertainOption)");
			content.add("					{");
			content.add("						logit(\"debug\", \"memorizing as FILE: $1->$_\");");
			content.add("						push(@{$FILE{'root'}->{$1}}, $_);");
			content.add("						push(@{$ALL{'root'}->{$1}}, $_);");
			content.add("					}");
			content.add("				}");
			content.add("				else");
			content.add("				{");
			content.add("					foreach (@allValuesOfACertainOption)");
			content.add("					{");
			content.add("						logit(\"debug\", \"memorizing as FILE: $option->$_\");");
			content.add("						push(@{$FILE{'root'}->{$option}}, $_);");
			content.add("						push(@{$ALL{'root'}->{$option}}, $_);");
			content.add("					}");
			content.add("				}");
			content.add("			}");
			content.add("			else");
			content.add("			{");
			content.add("				logit(\"debug\", \"option --\" . $option.\" is identified as a VARIABLE-option\");");
			content.add("				if ($option =~ m/variable_(.+)/i)");
			content.add("				{");
			content.add("					foreach (@allValuesOfACertainOption)");
			content.add("					{");
			content.add("						logit(\"debug\", \"memorizing as VARIABLE: $1->$_\");");
			content.add("						push(@{$VARIABLE{'root'}->{$1}}, $_);");
			content.add("						push(@{$ALL{'root'}->{$1}}, $_);");
			content.add("					}");
			content.add("				}");
			content.add("				else");
			content.add("				{");
			content.add("					foreach (@allValuesOfACertainOption)");
			content.add("					{");
			content.add("						logit(\"debug\", \"memorizing as VARIABLE: $option->$_\");");
			content.add("						push(@{$VARIABLE{'root'}->{$option}}, $_);");
			content.add("						push(@{$ALL{'root'}->{$option}}, $_);");
			content.add("					}");
			content.add("				}");
			content.add("			}");
			content.add("		}");
			content.add("		logit(\"debug\", \"##### END identifing whether given options are a FILE or a VARIABLE #####\");");
			content.add("");
		}
		
		// default
		else
		{
			content.add("#");
			content.add("# you may use getConfig(<string>), setConfig(<string>) to deal with data from the configfile if one exists.");
			content.add("# you may use getOption(<string>), setOption(<string>) and addOption(<string>) to deal with data from the call-options.");
			content.add("# you may use @ARG for all unknown/unparsed options given. this makes writing wrapperscripts easier.");
			content.add("#");
			content.add("# place your business logic here.");
			content.add("#");
			content.add("print \"you are now in program $0\";");
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
}