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
	
	/**
	 * getPerl()
	 * returns the whole step-logic in perlcode
	 * @return String
	 */
	
	public String getAll()
	{
		String perl = "#" + this.interpreter + "\n";
		
		BMeta meta = new BMeta(this);
		BModules modules = new BModules(this);
		BPath path = new BPath(this);
		BConfig config = new BConfig(this);
		
		
		perl += meta.getBlock() + modules.getBlock() + path.getBlock() + config.getBlock();
		
		return perl;
	}
	
	public String genBlockStart(String blockname)
	{
		String text = "";
		
		text += "#============================================================================\n";
		text += "# blockstart=" + blockname + "\n";
		
		return text;
	}

	public String genBlockEnd(String blockname)
	{
		String text = "";
		
		text   += "# blockend=" + blockname + "\n";
		text   += "# edit here if necessary\n";
		text   += "#\n";
		text   += "#============================================================================\n";
		
		return text;
	}
	
	public BigInteger genMd5(String content)
	{
		BigInteger bigInt = null;
		try {
			byte[] bytesOfContent = content.getBytes("UTF-8");
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