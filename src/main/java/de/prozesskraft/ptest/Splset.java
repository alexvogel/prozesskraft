package de.prozesskraft.ptest;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Splset {

	java.io.File splDir = null;
	ArrayList<Spl> spl = new ArrayList<Spl>();

	// das splDir ist direkt das verzeichnis in dem die beispieldaten liegen und ueblicherweise (jedoch nicht zwingend) auch .call und .result
	public Splset(String splDir)
	{
		this.splDir = new java.io.File(splDir);
		
		try
		{
			this.genSpl();
		}
		catch (NullPointerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * erzeugt fuer jedes gefundene .call-file im inputDir ein Spl-Objekt.
	 * Das Spl-Objekt enthält den call, den result und alle input-files
	 * @throws NullPointerException
	 * @throws IOException
	 */
	private void genSpl() throws NullPointerException, IOException
	{
		if(this.splDir == null)
		{
			System.err.println("error: no inputDir given. cannot identify entries without an inputDir.");
			throw new NullPointerException();
		}

		// patterns
		final Pattern patternCall = Pattern.compile("^\\.call\\.(.+)\\.txt$");
		final Pattern patternResult = Pattern.compile("^\\.result\\.(.+)\\.txt$");

		// die gefundenen Files werden vorerst in Arrays abgelegt
		final ArrayList<java.io.File> input = new ArrayList<java.io.File>();
		final Map<String,java.io.File> call = new HashMap<String,java.io.File>();
		final Map<String,java.io.File> result = new HashMap<String,java.io.File>();

		// den directory-baum durchgehen und fuer jeden eintrag ein entity erstellen
		Files.walkFileTree(Paths.get(this.getSplDir().getCanonicalPath()), new FileVisitor<Path>()
		{
			// called after a directory visit is complete
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
				return FileVisitResult.CONTINUE;
			}

			// called before a directory visit
			public FileVisitResult preVisitDirectory(Path walkingDir, BasicFileAttributes attrs) throws IOException
			{
				return FileVisitResult.CONTINUE;
			}

			// called for each file visited. the basic file attributes of the file are also available
			public FileVisitResult visitFile(Path walkingFile, BasicFileAttributes attrs) throws IOException
			{
				// relativen Pfad (zur Basis basepath) feststellen
				String pathString = walkingFile.getParent() + "/"+walkingFile.getFileName();
//				String relPathString = getBasepath().relativize(walkingFile).toString();

				boolean isCallerOrResult = false;

				// ist es ein ".call*"?, dann entsprechend merken
				Matcher matcherCall = patternCall.matcher(walkingFile.getFileName().toString());
				if(matcherCall.matches())
				{
					// den caller merken und als schluessel den extrahierten namen verwenden
					call.put(matcherCall.group(1), new java.io.File(pathString));
					isCallerOrResult = true;
				}

				// ist es ein ".result*"?, dann entsprechend merken
				Matcher matcherResult = patternResult.matcher(walkingFile.getFileName().toString());
				if(matcherResult.matches())
				{
					// den resultFingerprint merken und als schluessel den extrahierten namen verwenden
					result.put(matcherResult.group(1), new java.io.File(pathString));
					isCallerOrResult = true;
				}

				// ansonsten ist es ein inputfile
				if(!isCallerOrResult)
				{
					input.add(new java.io.File(pathString));
				}

				return FileVisitResult.CONTINUE;
			}

			// called for each file if the visit failed
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				System.out.println("visit file FAILED: "+file.getFileName());
				return FileVisitResult.CONTINUE;
			}

		});

		// nachdem alle files durchlaufen sind, sollen fuer jedes callfile ein spl-Objekt erzeugt werden
		// den call zuweisen
		// evtl. vorhandenen result zuweisen
		// alle input-files zuweisen
		// den Spl this zuweisen
		for(String actCallName : call.keySet())
		{
			Spl newSpl = new Spl(this.getSplDir());
			newSpl.setName(actCallName);
			newSpl.setCall(call.get(actCallName));
			
			// evtl. vorhandenen result zuweisen
			if(result.containsKey(actCallName))
			{
				newSpl.setResult(result.get(actCallName));
			}
			
			// alle input files zuweisen
			newSpl.setInput(input);
			
			// den neuen spl this hinzufuegen
			this.getSpl().add(newSpl);
		}
		
		// falls kein erzeugt wurde (weil kein call-file vorhanden, soll trotzdem ein spl nur mit input-files erstellt werden)
		if(spl.size() == 0)
		{
			Spl newSpl = new Spl(this.getSplDir());
			newSpl.setInput(input);
			this.getSpl().add(newSpl);
		}
		

	}

	/**
	 * @return the inputDir
	 */
	public java.io.File getSplDir() {
		return splDir;
	}

	/**
	 * @param inputDir the inputDir to set
	 */
	public void setSplDir(java.io.File inputDir) {
		this.splDir = inputDir;
	}

	/**
	 * @return the spl
	 */
	public Spl getSpl(String name)
	{
		for(Spl actSpl : this.getSpl())
		{
			if(actSpl.getName().equals(name))
			{
				return actSpl;
			}
		}
		return null;
	}

	/**
	 * @return the spl
	 */
	public ArrayList<Spl> getSpl() {
		return spl;
	}

	/**
	 * @param spl the spl to set
	 */
	public void setSpl(ArrayList<Spl> spl) {
		this.spl = spl;
	}

	
}
