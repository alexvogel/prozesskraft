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
	ArrayList<String> content = new ArrayList<String>();
	String type = "default";
	
//	private static Logger jlog = Logger.getLogger("de.caegroup.process.step");
	/*----------------------------
	  constructors
	----------------------------*/
	public BBusiness(Script parent)
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
		block.addAll(this.parent.genBlockStart("business"));
		block.add("# type="+type);
		block.add("# md5="+md5);
		block.add(this.parent.trenner);
		block.addAll(content);
		block.add(this.parent.trenner);
		block.add("# md5="+md5);
		block.addAll(this.parent.genBlockEnd("business"));
		
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
		
		if(type.matches("process"))
		{
			content.add("# 1) copy all known options with pattern =~ /--submodel_<key>=<path>/ as key=path to %FILE "+type);
			content.add("# 2) copy all known options with pattern !~ /--submodel_<key>=<value>/ to optionname=value to %VARIABLE "+type);
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
		}
		// default
		else
		{
			content.add("# place your business logic here.");
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