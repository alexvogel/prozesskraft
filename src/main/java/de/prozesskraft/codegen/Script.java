package de.prozesskraft.codegen;

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
	private String authorMail = "";
//	private ArrayList<Option> option = new ArrayList<Option>();
	private String description = "";
	private String interpreter = "/usr/bin/perl";
	private String type = "default";	// process|default|user
	ArrayList<Option> option = new ArrayList<Option>();
//	public String trenner = "#----------------------------------------------------------------------------";
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
	public Script()
	{

	}

	/*----------------------------
	  methods 
	----------------------------*/
	public void addOption (Option option)
	{
		this.option.add(option);
	}
	
	public void addOption (String name, int reihenfolge, int minoccur, int maxoccur, String definition, String check, String def, String text1, String text2, Boolean allowIntegratedListIfMultiOption)
	{
		Option option = new Option();

		option.setName(name);
		option.setReihenfolge(reihenfolge);
		option.setMinoccur(minoccur);
		option.setMaxoccur(maxoccur);
		option.setDefinition(definition);
		option.setCheck(check);
		option.setDef(def);
		option.setText1(text1);
		option.setText2(text2);
		option.setAllowIntegratedListIfMultiOption(allowIntegratedListIfMultiOption);
		
		this.option.add(option);
		this.options.genCode(type);
	}

	/**
	 * getPerl()
	 * returns the whole script in perlcode
	 * @return String
	 */

	public ArrayList<String> getAll()
	{
		ArrayList<String> perl = new ArrayList<String>();
		perl.add("#!" + this.interpreter);
		
		perl.addAll(meta.getBlock());
		perl.addAll(modules.getBlock());
		perl.addAll(path.getBlock());
		perl.addAll(options.getBlock());
		perl.addAll(config.getBlock());
		perl.addAll(help.getBlock());
		perl.addAll(calls.getBlock());
		perl.addAll(checks.getBlock());
		perl.addAll(business.getBlock());
		perl.addAll(subs.getBlock());
		
		return perl;
	}
	
//	public ArrayList<String> genBlockStart(String blockname, String md5)
//	{
//		ArrayList<String> text = new ArrayList<String>();
//		
//		text.add("#============================================================================");
//		text.add("# processcraft:" + blockname + ":" + "begin" + ":" + this.type + ":" + md5);
//		text.add("#----------------------------------------------------------------------------");
//		
//		return text;
//	}
//
//	public ArrayList<String> genBlockEnd(String blockname)
//	{
//		ArrayList<String> text = new ArrayList<String>();
//		
//		text.add("#----------------------------------------------------------------------------");
//		text.add("# processcraft:" + blockname + ":" + "end");
//		text.add("#============================================================================");
//		
//		return text;
//	}
//	
	/**
	 * genContent()
	 * the content of all blocks will be generated
	 */
	public void genContent()
	{
		this.meta.genCode(this.type);
		this.modules.genCode(this.type);
		this.path.genCode(this.type);
		this.config.genCode(this.type);
		this.options.genCode(this.type);
		this.help.genCode(this.type);
		this.calls.genCode(this.type);
		this.checks.genCode(this.type);
		this.business.genCode(this.type);
		this.subs.genCode(this.type);
	}

	/**
	 * addCode()
	 * add code to a certain block
	 * @throws UnknownCodeBlockException 
	 */
	public void addCode(String blockname, ArrayList<String> code) throws UnknownCodeBlockException
	{
		if (blockname.matches("business"))
		{
			this.business.addCode(code);
		}
		else
		{
			throw new UnknownCodeBlockException(blockname);
		}
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the authorMail
	 */
	public String getAuthorMail() {
		return authorMail;
	}

	/**
	 * @param authorMail the authorMail to set
	 */
	public void setAuthorMail(String authorMail) {
		this.authorMail = authorMail;
	}

	/**
	 * @return the interpreter
	 */
	public String getInterpreter() {
		return interpreter;
	}

	/**
	 * @param interpreter the interpreter to set
	 */
	public void setInterpreter(String interpreter) {
		this.interpreter = interpreter;
	}
	
//	public void setContent(String block, ArrayList<String> content) throws UnknownCodeBlockException
//	{
//		if (block.matches("meta")) {this.meta.setContent(content);}
//		else if (block.matches("modules")) {this.modules.setContent(content);}
//		else if (block.matches("path")) {this.path.setContent(content);}
//		else if (block.matches("config")) {this.config.setContent(content);}
//		else if (block.matches("options")) {this.options.setContent(content);}
//		else if (block.matches("help")) {this.help.setContent(content);}
//		else if (block.matches("calls")) {this.calls.setContent(content);}
//		else if (block.matches("checks")) {this.checks.setContent(content);}
//		else if (block.matches("business")) {this.business.setContent(content);}
//		else if (block.matches("subs")) {this.subs.setContent(content);}
//		else {throw new UnknownCodeBlockException("unknown code block "+block);}
//	}
}