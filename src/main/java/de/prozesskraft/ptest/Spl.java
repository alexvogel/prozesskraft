package de.prozesskraft.ptest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * the callString in the spl file is: all lines, joined together without "\newLines"
	 * @return call
	 * @throws IOException 
	 */
	public ArrayList<String> getCallAsArrayList() throws IOException
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
						System.err.println("debug: test if there is a file or directory with this path "+testFile.getCanonicalPath());
						if(testFile.exists())
						{
							System.err.println("debug: yes - there is a file/directory, so this option will be expanded to its full path");
							firstLineExpanded.add(testFile.getCanonicalPath());
							System.err.println("debug: string of first line after: "+testFile.getCanonicalPath());

						}
						else
						{
							System.err.println("debug: no - there is no file/directory, so this option will be left unchanged");
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
		
		ArrayList<String> callAsArrayList = new ArrayList<String>();

		// zu einem arraylist umgestalten
		// 1) trennen der ersten zeile an allen " "
		boolean ersteZeileGesehen = false;
		for(String actLine : allLines)
		{
			// kommentare ignorieren
			if(actLine.matches("^\\s*#.+$"))
			{
				// ignorieren
			}
			// erste zeile an whitespaces splitten
			else if(!ersteZeileGesehen)
			{
				callAsArrayList.addAll(new ArrayList<String>(Arrays.asList(actLine.split(" +"))));
				ersteZeileGesehen = true;
			}
			// alle weiteren zeilen an erstem whitespace splitten
			else
			{
				callAsArrayList.addAll(new ArrayList<String>(Arrays.asList(actLine.split(" +", 2))));
			}
		}
		
		// die zusaetzlich angegebenen options auch hinzujoinen
		if(this.getAddopt().size()>0)
		{
			for(String actString : this.getAddopt())
			{
				callAsArrayList.add(actString);
			}
		}
		
//		System.err.println("this call is returned to caller ---- start ----:");
//		for(String actString : callAsArrayList)
//		{
//			System.err.println(actString +" \\");
//		}
//		System.err.println("this call is returned to caller ---- end ----:");
		
		return callAsArrayList;
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
			try
			{
				Path pathOfSpl = Paths.get(this.getSplDir().getCanonicalPath());
				System.err.println("info: pathCanonical of sample data is: "+pathOfSpl.toString());
			
				for(java.io.File actInputFile : this.getInput())
				{
//					System.err.println("info: bearbeite file " + actInputFile.getAbsolutePath());
					// abspfad file source
					Path pathOfActInputFile = Paths.get(actInputFile.getCanonicalPath());
					System.err.println("debug: pathOfActInputFileAbsolute: " + pathOfActInputFile.toString());
					
					// relpfad file source
					Path pathOfActInputFileRelativeToSpl = pathOfSpl.relativize(pathOfActInputFile);
					System.err.println("debug: pathOfActInputFileRelativeToSpl: " + pathOfActInputFileRelativeToSpl.toString());
					
					// abspfad target (mit erhaltenen unterverzeichnissen)
					java.io.File targetFile = new java.io.File(target.getCanonicalPath() + "/" + pathOfActInputFileRelativeToSpl);
					System.err.println("debug: pathOftargetFile: " + targetFile.getCanonicalPath());
					
					// erstellen der unterverzeichnisse, falls notwendig
					if(! targetFile.getParentFile().exists())
					{
//						System.err.println("info: creating target directory "+targetFile.getParent());
						targetFile.getParentFile().mkdirs();
					}
					
					System.err.println("info: copy sample file to instance directory: "+actInputFile.getCanonicalPath() +" => " +targetFile.getCanonicalPath());
					System.err.println("debug: start copy at: " + new Timestamp(System.currentTimeMillis()));
					
					try
					{
						Files.copy(actInputFile.toPath(), targetFile.toPath());
					}
					catch (FileAlreadyExistsException e)
					{
						System.err.println("warn: file already exists. " + e.getMessage());
					}
					System.err.println("debug: end copy at: " + new Timestamp(System.currentTimeMillis()));
				}
			
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
