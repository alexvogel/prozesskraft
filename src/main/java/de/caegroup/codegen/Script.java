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
	ArrayList<Option> option = new ArrayList<Option>();
	public String trenner = "#----------------------------------------------------------------------------";
	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 * constructs a step with
	 * a new parent
	 * a random name
	 */
	public Script()
	{

	}

	/*----------------------------
	  methods 
	----------------------------*/
//	@Override
//	public Step clone()
//	{
//		try
//		{
//			return (Step) super.clone();
//		}
//		catch ( CloneNotSupportedException e )
//		{
//			e.printStackTrace();
//		}
//		return null;
//	}
	
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
		
		BMeta meta = new BMeta(this);
		BModules modules = new BModules(this);
		BPath path = new BPath(this);
		BConfig config = new BConfig(this);
		BOptions options = new BOptions(this);
		BHelp help = new BHelp(this);
		BCalls calls = new BCalls(this);
		BChecks checks = new BChecks(this);
		BBusiness business = new BBusiness(this);
		BSubs subs = new BSubs(this);
		
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

}