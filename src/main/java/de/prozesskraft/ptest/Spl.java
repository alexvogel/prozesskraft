package de.prozesskraft.ptest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Spl {

	private String name = "unnamed";
	private String splDir = null;
	private java.io.File call = null;
	private java.io.File result = null;
	private ArrayList<java.io.File> input = new ArrayList<java.io.File>();
	private String altapp = null;
	private ArrayList<String> addopt = new ArrayList<String>();

	public Spl(String splDir)
	{
		this.splDir = splDir;
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
		// alle einzel strings in erster Zeile sollen ueberprueft werden ob es sich dabei um eine relative pfadangabe handelt und zu einem absoluten pfad gewandelt werden
		else
		{
			ArrayList<String> allLinesModified = new ArrayList<String>();
			// die erste nicht-kommentarzeile extrahieren und relative pfade expanden
			boolean firstLineSeen = false;
			for(String actLine : allLines)
			{
				// wenn es sich um die erste zeile handelt, dann sollen pfade expandiert werden
				if( (!firstLineSeen) && (!actLine.matches("^\\s*#.*$")) )
				{
					// splitten an whitespace
					String[] firstLine = actLine.split("\\s+");
					
					ArrayList<String> firstLineExpanded = new ArrayList<String>();
					// alle einzelstrings durchegehen und pfadangaben erweitern
					for(String actString : firstLine)
					{
						java.io.File testFile = new java.io.File(this.getCall().getParentFile().getCanonicalPath()+"/"+actString);

						System.err.println("debug: string of first line before: "+actString);

						// wenn es ein file/directory mit dem expandeten path gibt, soll dieser (statt dem bisherigen relativen Pfad) verwendet werden
						if(testFile.exists())
						{
							firstLineExpanded.add(testFile.getCanonicalPath());
							System.err.println("debug: string of first line after: "+testFile.getCanonicalPath());

						}
						else
						{
							firstLineExpanded.add(actString);
							System.err.println("debug: string of first line after: "+actString);
						}
					}
					
					// wieder zu einem string joinen
					String firstLineExpandedJoined = "";
					for(String actString : firstLineExpanded)
					{
						firstLineExpandedJoined += " " + actString;
					}
					// das fuehrende blank loeschen
					firstLineExpandedJoined = firstLineExpandedJoined.replaceFirst("^\\s+", "");
					System.err.println("debug: first line after expansion: '"+firstLineExpandedJoined+"'");
					
					allLinesModified.add(firstLineExpandedJoined);
					
					// vermerken, dass die erste zeile bereits besucht wurde
					firstLineSeen = true;
				}
				else
				{
					allLinesModified.add(actLine);
				}
			}
			allLines = allLinesModified;
		}
		
		String callAsString = "";

		// zu einem string joinen  (trennzeichen=" ")
		for(String actLine : allLines)
		{
			// wenn zeile kein commentar
			if(!actLine.matches("^\\s*#.+$"))
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
		}
		
		// die zusaetzlich angegebenen options auch hinzujoinen
		if(this.getAddopt().size()>0)
		{
			for(String actString : this.getAddopt())
			{
				callAsString += " " + actString;
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

	/**
	 * @return the addopt
	 */
	public ArrayList<String> getAddopt() {
		return addopt;
	}

	/**
	 * @param addopt the addopt to set
	 */
	public void setAddopt(ArrayList<String> addopt) {
		this.addopt = addopt;
	}

	/**
	 * @return the splDir
	 */
	public String getSplDir() {
		return splDir;
	}

	/**
	 * @param splDir the splDir to set
	 */
	public void setSplDir(String splDir) {
		this.splDir = splDir;
	}

}
