package de.prozesskraft.ptest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Spl {

	private String name = "unnamed";
	private java.io.File splDir = null;
	private java.io.File call = null;
	private java.io.File result = null;
	private ArrayList<java.io.File> input = new ArrayList<java.io.File>();
	private String altapp = null;
	private ArrayList<String> addopt = new ArrayList<String>();

	public Spl(java.io.File splDir)
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
	 * exportiert alle inputfiles in ein verzeichnis
	 * unterverzeichnisse werden beruecksichtigt und bleiben erhalten
	 * zielverzeichnis muss existieren
	 */
	public void exportInput(java.io.File target)
	{
		System.err.println("info: exporting input samplefiles");
		if(!target.exists())
		{
			System.err.println("error: target directory does not exist: "+target.getAbsolutePath());
		}
		else if(target.exists())
		{
			// namen des targetfiles festlegen / dabei sollen unterverzeichnisse, die relativ zum quellverzeichnis existieren erhalten bleiben
			// abspath basisverzeichnis
			Path pathOfSpl = Paths.get(this.getSplDir().getAbsolutePath());
			System.err.println("info: path of sample data is: "+pathOfSpl.toString());

			for(java.io.File actInputFile : this.getInput())
			{
//				System.err.println("info: bearbeite file " + actInputFile.getAbsolutePath());
				// abspfad file source
				Path pathOfActInputFile = Paths.get(actInputFile.getAbsolutePath());

				// relpfad file source
				Path pathOfActInputFileRelativeToSpl = pathOfSpl.relativize(pathOfActInputFile);
				
				// abspfad target (mit erhaltenen unterverzeichnissen)
				java.io.File targetFile = new java.io.File(target.getAbsolutePath() + "/" + pathOfActInputFileRelativeToSpl);
				
				// erstellen der unterverzeichnisse, falls notwendig
				if(! targetFile.getParentFile().exists())
				{
//					System.err.println("info: creating target directory "+targetFile.getParent());
					targetFile.getParentFile().mkdirs();
				}
				
				// input file in das instancedir kopieren
				try
				{
					System.err.println("info: copy sample file to instance directory: "+actInputFile.getAbsolutePath() +" => " +targetFile.getAbsolutePath());
					System.err.println("debug: start copy at: " + new Timestamp(System.currentTimeMillis()));
					
					Files.copy(actInputFile.toPath(), targetFile.toPath());
					System.err.println("debug: end copy at: " + new Timestamp(System.currentTimeMillis()));
				}
				catch (FileAlreadyExistsException e)
				{
					System.err.println(e.getMessage());
				}
				catch (IOException e)
				{
					System.err.println(e.getMessage());
				}
			}
		}
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
	public java.io.File getSplDir() {
		return splDir;
	}

	/**
	 * @param splDir the splDir to set
	 */
	public void setSplDir(java.io.File splDir) {
		this.splDir = splDir;
	}

}
