package de.caegroup.codegen;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Block
implements Serializable, Cloneable
{
	/*----------------------------
	  structure
	----------------------------*/

	static final long serialVersionUID = 1;

	private ArrayList<String> code = new ArrayList<String>();
	private String origin = "unknown";	// auto|user
	private String blockname = "unknown";
	private BigInteger md5 = null;
	private BigInteger md5Parsed = null;

	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 */
	public Block()
	{

	}

	/*----------------------------
	  methods 
	----------------------------*/

	/**
	 * setCode(String origin, ArrayList<String> code)
	 * sets the code for the content and its origin
	 */
	public void setCode (ArrayList<String> code)
	{
		
		// parsen und entfernen der oeffnenden und schliessenden blockzeilen
		
		ArrayList<String> codeOhneHeader = new  ArrayList<String>();
		// die oeffnenden Zeilen entfernen, falls sie existieren zuerst
		if((code.get(0).matches("^#====")) && (code.get(1).matches("^# processcraft:(.+):begin:(.+):(.+)")) && (code.get(2).matches("^#----")))
		{
			//processcraft:blockname:begin:origin:md5
			Pattern p = Pattern.compile("^# processcraft:(.+):begin:(.+):(.+)");
			Matcher m = p.matcher(code.get(1));
			this.blockname = m.group(1);
			this.origin = m.group(2);
			this.md5Parsed = new BigInteger(m.group(3));
			
			for(int x = 3; x < code.size(); x++)
			{
				codeOhneHeader.add(code.get(x));
			}
		}
		else
		{
			codeOhneHeader = code;
		}
		
		ArrayList<String> codeOhneHeaderOhneFooter = new  ArrayList<String>();
		// die letzten 3 zeilen checken ob sie dem muster eines schliessenden blocks entsprechen
		if((codeOhneHeader.get(-3).matches("^#----")) && (codeOhneHeader.get(-2).matches("^# processcraft:(.+):end")) && (codeOhneHeader.get(-1).matches("^#====")))
		{

			for(int x = 0; x < (codeOhneHeader.size() -3); x++)
			{
				codeOhneHeaderOhneFooter.add(codeOhneHeader.get(x));
			}
		}
		else
		{
			codeOhneHeaderOhneFooter = codeOhneHeader;
		}
		
		this.code = codeOhneHeaderOhneFooter;
		this.md5 = genMd5();
	}

	/**
	* addCode()
	* adds code to already existing code
	*/
	public void addCode(ArrayList<String> code)
	{
		this.code.addAll(code);
		this.origin = "user";
		this.md5 = genMd5();
	}
	
	/**
	* getCode()
	* returns the code
	* @return ArrayList<String>
	*/
	public ArrayList<String> getCode()
	{
		ArrayList<String> content = new ArrayList<String>();
		
		content.addAll(this.genBlockStart());
		content.addAll(this.code);
		content.addAll(this.genBlockEnd());
		
		return content;
	}

	/**
	* genMd5()
	* calculates and sets the md5 of the code
	*/
	public BigInteger genMd5()
	{
		BigInteger bigInt = null;
		try {
			
			// den gesamten content in einen String einfuegen
			String contentString = "";
			
			for(String line : this.code)
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

	/**
	* genBlockStart()
	* generates the opening infolines
	* @return ArrayList<String> lines
	*/
	public ArrayList<String> genBlockStart()
	{
		ArrayList<String> text = new ArrayList<String>();
		
		text.add("#============================================================================");
		text.add("# processcraft:" + this.blockname + ":" + "begin" + ":" + this.origin + ":" + this.md5);
		text.add("#----------------------------------------------------------------------------");
		
		return text;
	}

	/**
	* genBlockEnd()
	* generates the closing infolines
	* @return ArrayList<String> lines
	*/
	public ArrayList<String> genBlockEnd()
	{
		ArrayList<String> text = new ArrayList<String>();
		
		text.add("#----------------------------------------------------------------------------");
		text.add("# processcraft:" + this.blockname + ":" + "end");
		text.add("#============================================================================");
		
		return text;
	}

	/**
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(String origin) {
		this.origin = origin;
	}

	/**
	 * @return the blockname
	 */
	public String getBlockname() {
		return blockname;
	}

	/**
	 * @param blockname the blockname to set
	 */
	public void setBlockname(String blockname) {
		this.blockname = blockname;
	}
}