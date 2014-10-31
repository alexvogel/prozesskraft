package de.prozesskraft.ptest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Spl {

	private String name = "unnamed";
	private java.io.File call = null;
	private java.io.File result = null;
	private ArrayList<java.io.File> input = new ArrayList<java.io.File>();
	private String altapp = null;

	public Spl()
	{

	}

	/**
	 * reads the call-file
	 * 1) the first line is the app-call
	 * 2) every following line is a parameter
	 * the callString is: all lines, joined together without "\newLines"
	 * @return the call
	 * @throws IOException 
	 */
	public String getCallAsString() throws IOException
	{
		// das call-file einlesen
		List<String> allLines = Files.readAllLines(this.getCall().toPath(), Charset.defaultCharset());

		// soll ein alternativer app-aufruf verwendet werden?
		if(this.getAltapp() != null)
		{
			allLines.set(0, this.getAltapp());
		}
		
		// alle lines zu einem string joinen
		// dabei am ende des string eventuelle " *\\" entfernen
		for(String actLine : allLines)
		{
			actLine.replaceAll(" *\\$", "");
		}
		
		String callAsString = "";
		
		// zu einem string joinen  (trennzeichen=" ")
		for(String actLine : allLines)
		{
			callAsString += actLine;
		}
		
		return callAsString;
	}

	/**
	 * @return the call
	 */
	public java.io.File getCall() {
		return call;
	}

	/**
	 * @param call the call to set
	 */
	public void setCall(java.io.File call) {
		this.call = call;
	}

	/**
	 * @return the result
	 */
	public java.io.File getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(java.io.File result) {
		this.result = result;
	}

	/**
	 * @return the input
	 */
	public ArrayList<java.io.File> getInput() {
		return input;
	}

	/**
	 * @param input the input to set
	 */
	public void setInput(ArrayList<java.io.File> input) {
		this.input = input;
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
	 * @return the altapp
	 */
	public String getAltapp() {
		return altapp;
	}

	/**
	 * @param altapp the altapp to set
	 */
	public void setAltapp(String altapp) {
		this.altapp = altapp;
	}

	
}
