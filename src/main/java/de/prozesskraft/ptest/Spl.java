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

		// aus allen zeilen " *\\" entfernen
		for(int id = 0; id < allLines.size(); id++)
		{
			allLines.set(id, allLines.get(id).replaceAll("\\s*\\\\$", ""));
		}

		// soll ein alternativer app-aufruf verwendet werden?
		if(this.getAltapp() != null)
		{
			allLines.set(0, this.getAltapp());
		}
		// wenn es sich beim ersten String in der Zeile um einen relativen pfad handelt (beginnt mit "..", soll der zu einem absoluten verlaengert werden
		else if(allLines.get(0).matches("^\\s*\\.\\..*$"))
		{
			String newPathToApp = this.getCall().getParentFile().getCanonicalPath() + "/" + allLines.get(0);

			System.err.println("debug: this is the call: " + this.getCall().getCanonicalPath());
			System.err.println("debug: this is the dir of call: " + this.getCall().getParentFile().getCanonicalPath());
			System.err.println("debug: this is the new Path to App: " + newPathToApp);
			
			java.io.File appFile = new java.io.File(newPathToApp);
			
			if(appFile.isFile())
			{
				allLines.set(0, appFile.getCanonicalPath());
			}
			else
			{
				System.err.println("error: this is not a file: " + appFile.getCanonicalPath());
				System.exit(1);
			}
		}

		String callAsString = "";

		// zu einem string joinen  (trennzeichen=" ")
		for(String actLine : allLines)
		{
			if(callAsString.equals(""))
			{
				callAsString += actLine;
			}
			else
			{
				callAsString += " " + actLine;
			}
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
