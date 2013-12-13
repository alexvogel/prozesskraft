package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Script
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;
	private String name = "unnamed";
//	private ArrayList<Option> option = new ArrayList<Option>();
	private String description = "";
	private String interpreter = "/usr/bin/perl";
	private String type = "default";
	ArrayList<Option> option = new ArrayList<Option>();
	public String trenner = "#----------------------------------------------------------------------------";
	public BBusiness business = new BBusiness(this);
	public BCalls calls = new BCalls(this);
	public BChecks checks = new BChecks(this);
	public BConfig config = new BConfig(this);
	public BHelp help = new BHelp(this);
	public BMeta meta = new BMeta(this);
	public BModules modules = new BModules(this);
	public BOptions options = new BOptions(this);
	public BPath path = new BPath(this);
	public BSubs subs = new BSubs(this);
	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 */
	public Script(String type)
	{
		this.setType(type);
	}
	/**
	 */
	public Script()
	{
		this.setType("default");
	}

	/*----------------------------
	  methods 
	----------------------------*/
	public void addOption (Option option)
	{
		this.option.add(option);
	}
	
	public void addOption (String name, int minoccur, int maxoccur, String definition, String check, String def, String text1, String text2)
	{
		Option option = new Option();

		option.setName(name);
		option.setMinoccur(minoccur);
		option.setMaxoccur(maxoccur);
		option.setDefinition(definition);
		option.setCheck(check);
		option.setDef(def);
		option.setText1(text1);
		option.setText2(text2);
		
		this.option.add(option);
		this.options.genContent(type);
	}
	
	/**
	 * getPerl()
	 * returns the whole step-logic in perlcode
	 * @return String
	 */
	
	public ArrayList<String> getAll()
	{
		ArrayList<String> perl = new ArrayList<String>();
		perl.add("#!" + this.interpreter);
		
		perl.addAll(meta.getBlock());
		perl.addAll(modules.getBlock());
		perl.addAll(path.getBlock());
		perl.addAll(config.getBlock());
		perl.addAll(options.getBlock());
		perl.addAll(help.getBlock());
		perl.addAll(calls.getBlock());
		perl.addAll(checks.getBlock());
		perl.addAll(business.getBlock());
		perl.addAll(subs.getBlock());
		
		return perl;
	}
	
	public ArrayList<String> genBlockStart(String blockname)
	{
		ArrayList<String> text = new ArrayList<String>();
		
		text.add("#============================================================================");
		text.add("# blockstart=" + blockname);
		
		return text;
	}

	public ArrayList<String> genBlockEnd(String blockname)
	{
		ArrayList<String> text = new ArrayList<String>();
		
		text.add("# blockend=" + blockname);
		text.add("# edit here if necessary");
		text.add("#");
		text.add("#============================================================================");
		
		return text;
	}
	
	public BigInteger genMd5(ArrayList<String> content)
	{
		BigInteger bigInt = null;
		try {
			
			// den gesamten content in einen String einfuegen
			String contentString = "";
			
			for(String line : content)
			{
				if( !( (line.matches("^#")) || (line.matches("^$")) ) )
				contentString += line;
			}
			
			// und dann md5 erzeugen
			byte[] bytesOfContent = contentString.getBytes("UTF-8");
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfContent);
			bigInt = new BigInteger(1, thedigest);
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bigInt;
	}

	public void setContent(String block, ArrayList<String> content) throws UnknownCodeBlockException
	{
		if (block.matches("meta")) {this.meta.setContent(content);}
		else if (block.matches("modules")) {this.modules.setContent(content);}
		else if (block.matches("path")) {this.path.setContent(content);}
		else if (block.matches("config")) {this.config.setContent(content);}
		else if (block.matches("options")) {this.options.setContent(content);}
		else if (block.matches("help")) {this.help.setContent(content);}
		else if (block.matches("calls")) {this.calls.setContent(content);}
		else if (block.matches("checks")) {this.checks.setContent(content);}
		else if (block.matches("business")) {this.business.setContent(content);}
		else if (block.matches("subs")) {this.subs.setContent(content);}
		else {throw new UnknownCodeBlockException("unknown code block "+block);}
	}

	public void addContent(String block, ArrayList<String> content) throws UnknownCodeBlockException
	{
		if (block.matches("meta")) {this.meta.addContent(content);}
		else if (block.matches("modules")) {this.modules.addContent(content);}
		else if (block.matches("path")) {this.path.addContent(content);}
		else if (block.matches("config")) {this.config.addContent(content);}
		else if (block.matches("options")) {this.options.addContent(content);}
		else if (block.matches("help")) {this.help.addContent(content);}
		else if (block.matches("calls")) {this.calls.addContent(content);}
		else if (block.matches("checks")) {this.checks.addContent(content);}
		else if (block.matches("business")) {this.business.addContent(content);}
		else if (block.matches("subs")) {this.subs.addContent(content);}
		else {throw new UnknownCodeBlockException("unknown code block "+block);}
	}

	private void setType(String type)
	{
		this.type = type;
		// nur wenn type != default, soll der content neu generiert werden
		if (!(type.matches("default")))
		{
			this.meta.setType(type);
			this.modules.setType(type);
			this.path.setType(type);
			this.config.setType(type);
			this.options.setType(type);
			this.help.setType(type);
			this.calls.setType(type);
			this.checks.setType(type);
			this.business.setType(type);
			this.subs.setType(type);
		}
	}


}