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
			content.add("# 2) copy all known options with pattern !~ /--submodel_<key>=<value>/ to optionname=value to %VARIABLE ");
			content.add("");
			content.add("my %VARIABLE;");
			content.add("my %FILE;");
			content.add("");
			content.add("foreach my $option (keys %OPT)");
			content.add("{");
			content.add("	if ($option =~ m/submodel_(.+)/i)");
			content.add("	{");
			content.add("		$FILE{$1} = $OPT{$option};");
			content.add("	}");
			content.add("	else");
			content.add("	{");
			content.add("		$VARIABLE{$option} = $OPT{$option};");
			content.add("	}");
			content.add("}");
			content.add("");
			content.add("unless(defined $VARIABLE{'root'}) { $VARIABLE{'root'} = [];}");
			content.add("unless(defined $FILE{'root'}) { $FILE{'root'} = [];}");

		}
		// default
		else
		{
			content.add("# place your business logic here.");
		}

		this.block.setCode(content);
	}
	
}