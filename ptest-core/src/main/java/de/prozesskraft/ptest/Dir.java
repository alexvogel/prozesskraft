package de.prozesskraft.ptest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FilenameUtils;
import org.dozer.DozerBeanMapper;
import org.xml.sax.SAXException;

import de.prozesskraft.commons.Log;
import de.prozesskraft.commons.Md5Checksum;

public class Dir {

	private Integer id = null;
	private int minOccur = 0;
	private int maxOccur = 99;
	private String path = null;

	private ArrayList<File> file = new ArrayList<File>();
	private ArrayList<Dir> dir = new ArrayList<Dir>();

	private String infilexml = null;
	private String outfilexml = null;
	private Path basepath = null;

	private int runningId = 0;

	public Dir thisObj = null;

	private boolean flagPathMatched = false;
	private boolean flagOccuranceMatched = false;
	private boolean flagFuzzyReference = false;

	private String actRole = null;  // ref|exam

	// ist es ein template? , dann wird in match alle passenden ids aus dem pruefling gesammelt.
	// ist es ein pruefling?, dann wird in match die id des passenden eintrages aus dem template eingetragen (in diesem fall kann es nur 1 stk sein)
	private ArrayList<Dir> matchedDir = new ArrayList<Dir>();

	private ArrayList<Log> log = new ArrayList<Log>();

	// merker fuer die file/dir walker
	private ArrayList<Dir> directoryPath = new ArrayList<Dir>();

	float sizeToleranceDefault = 0F;
	boolean respectMd5 = true;
	
	/*
	 * constructor
	 */
	public Dir()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);

		minOccur = 0;
		maxOccur = 99;

		this.thisObj = this;
	}

	public String toString()
	{
		String entityString = "";

		entityString += "id="+this.getId()+", minoccur: "+this.getMinOccur()+", maxoccur: "+this.getMaxOccur()+", path: "+this.getPath();

		return entityString;
	}

	/**
	 * @return the whole log of this and all dependencies as a String
	 */
	public String getLogAsStringRecursive()
	{
		String fullLog = this.getLogAsString();
		
		for(File actFile : this.getFile())
		{
			fullLog += actFile.getLogAsString();
		}
		
		for(Dir actDir : this.getDir())
		{
			fullLog += actDir.getLogAsString();
		}
		
		return fullLog;
	}

	/**
	 * @return the whole log of this as a String
	 */
	public String getLogAsString()
	{
		return Log.sprintWholeLog(this.log);
	}

	/**
	 * erzeugt einen string mit der summary
	 * @param scope
	 * @return
	 */
	public String sprintSummaryAsCsv(String scope)
	{
		String summaryString = this.getCsvHeader() + "\n";

		// wenn role == null
		if(this.actRole == null)
		{
			return "there is no summary available. first you have to runCheck.";
		}

		// wenn role == exam
		else if(this.actRole.equals("exam"))
		{
			ArrayList<String> allLines = this.getExamineeSummaryAsCsv(scope);
			
			// wenn kein inhalt und scope == error
			if( (allLines.size() == 0) && (scope.equals("error")) )
			{
				summaryString += "(exam) no errors in result table";
			}
			// wenn kein inhalt und scope != error
			else if(allLines.size() == 0)
			{
				summaryString += "(exam) no entries in result table";
			}
			// wenn mit inhalt
			else
			{
				for(String actLine : allLines)
				{
					summaryString += actLine + "\n";
				}
			}
			return summaryString;
		}

		// wenn role == ref
		else if(this.actRole.equals("ref"))
		{
			ArrayList<String> allLines = this.getReferenceSummaryAsCsv(scope);
			
			// wenn kein inhalt und scope == error
			if( (allLines.size() == 0) && (scope.equals("error")) )
			{
				summaryString += "(ref) no errors in result table";
			}
			// wenn kein inhalt und scope != error
			else if(allLines.size() == 0)
			{
				summaryString += "(ref) no entries in result table";
			}
			// wenn mit inhalt
			else
			{
				for(String actLine : allLines)
				{
					summaryString += actLine + "\n";
				}
			}
			return summaryString;
		}
		
		else
		{
			return "error: unknown role "+this.actRole;
		}
	}

	/**
	 * erzeugt eine csv-zeile mit allen relevanten daten
	 * @return
	 */
	private String getExamineeSummaryAsCsvLine(String scope)
	{
		boolean error = false;
		
		String csvLine = "exam";

		csvLine += ";" + this.getId();

		csvLine += ";" + "dir";
		csvLine += ";" + this.getPathWithoutQuotes();

		if(this.isMatchSuccessfull())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
			error = true;
		}

		if(this.isFlagPathMatched())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}

		// fuer size
		csvLine += ";" + "-";
		
		if(this.isFlagOccuranceMatched())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}

		if(!this.isFlagFuzzyReference())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}

		// letzte spalte ist md5 (keine relevanz bei directory)
		csvLine += ";" + "-";
		
		if(scope.equals("error") && error)
		{
			return csvLine;
		}
		else if(scope.equals("error") && !error)
		{
			return null;
		}

		return csvLine;
		
	}

	private String getCsvHeader()
	{
		return ("origin;id;type;path;result;pathMatched;sizeMatched;occuranceMatched;noFuzzyReference;md5Matched;note");
	}

	/**
	 * alle Dirs und Files durchgehen
	 * 1) gibt es ein Dir | File im template, dessen match den minoccur unterschreitet?
	 * ...
	 */
	private ArrayList<String> getExamineeSummaryAsCsv(String scope)
	{
		ArrayList<String> allResultsAsCsv = new ArrayList<String>();

		// 1) das directory
		String lineThis = this.getExamineeSummaryAsCsvLine(scope);
		if(lineThis != null)
		{
			allResultsAsCsv.add(lineThis);
		}

		// 2) alle darin enthaltenen files
		for(File actFile : this.getFile())
		{
			String lineFile = actFile.getExamineeSummaryAsCsvLine(scope);
			if(lineFile != null)
			{
				allResultsAsCsv.add(lineFile);
			}
		}

		// 2) alle darin enthaltenen directories
		for(Dir actDir : this.getDir())
		{
			allResultsAsCsv.addAll(actDir.getExamineeSummaryAsCsv(scope));
		}

		return allResultsAsCsv;
	}

	/**
	 * es werden die ergenisse ausgegeben
	 * @param der umfang der ausgabe all|error
	 * scope=all: es wird die zeile ausgegeben unabhaengig ob die entity einen erfolgreichen match hat oder nicht
	 * scope=error: es werden nur die zeilen ausgegeben, die einen erfolglosen match anzeigen
	 * @return
	 */
	private String getReferenceSummaryAsCsvLine(String scope)
	{
		if(scope.equals("error") && this.flagOccuranceMatched)
		{
			return null;
		}
		else
		{
			String csvLine = "ref";

			csvLine += ";" + this.getId();

			csvLine += ";" + "dir";
			csvLine += ";" + this.getPathWithoutQuotes();

			if(this.isMatchSuccessfull())
			{
				csvLine += ";" + "x";
			}
			else
			{
				csvLine += ";" + "o";
			}

			if(this.isFlagPathMatched())
			{
				csvLine += ";" + "x";
			}
			else
			{
				csvLine += ";" + "o";
			}

			// fuer size
			csvLine += ";" + "-";
			
			if(this.isFlagOccuranceMatched())
			{
				csvLine += ";" + "x";
			}
			else
			{
				csvLine += ";" + "o";
			}

			if(!this.isFlagFuzzyReference())
			{
				csvLine += ";" + "x";
			}
			else
			{
				csvLine += ";" + "o";
			}

			// letzte spalte ist md5 => keine relevanz bei directory
			csvLine += ";" + "-";

			String note = "";

			if(this.getMatchedDir().size() < this.getMinOccur())
			{
				note = "error: occurance of matched directories: at least "+this.getMinOccur()+" matches are needed (minOccur), but only "+this.getMatchedDir().size()+" directories matched "+this.getMatchedDir().toString();
			}
			else if(this.getMatchedDir().size() > this.getMaxOccur())
			{
				note = "error: occurance of matched directories: max "+this.getMaxOccur()+" matches are allowed (maxOccur), but "+this.getMatchedDir().size()+" directories matched "+this.getMatchedDir().toString();
			}
			
			csvLine += ";" + note;
			return csvLine;
		}
	}

	/**
	 * alle Dirs und Files durchgehen
	 * 1) gibt es ein Dir | File im template, dessen match den minoccur unterschreitet?
	 * ...
	 */
	private ArrayList<String> getReferenceSummaryAsCsv(String scope)
	{
		ArrayList<String> referenceSummaryAsCsv = new ArrayList<String>();

		// 1) das directory
		if(this.getReferenceSummaryAsCsvLine(scope) != null)
		{
			referenceSummaryAsCsv.add(this.getReferenceSummaryAsCsvLine(scope));
		}
		
		// 2) alle darin enthaltenen files
		for(File actFile : this.getFile())
		{
			if(actFile.getReferenceSummaryAsCsvLine(scope) != null)
			{
				referenceSummaryAsCsv.add(actFile.getReferenceSummaryAsCsvLine(scope));
			}
		}

		// 2) alle darin enthaltenen directories
		for(Dir actDir : this.getDir())
		{
			referenceSummaryAsCsv.addAll(actDir.getReferenceSummaryAsCsv(scope));
		}

		return referenceSummaryAsCsv;
	}

	/**
	 * gibt recursiv zurueck ob alle verglichenen eigenschaften zufriedenstellend zusammengepasst haben
	 * @param
	 */
	public boolean isMatchSuccessfullRecursive()
	{
		boolean matchSuccess = true;
		
		if(!this.isMatchSuccessfull())
		{
			return false;
		}
		
		// fuer alle files aufrufen
		for(File actFile : this.getFile())
		{
			if(! actFile.isMatchSuccessfull())
			{
				return false;
			}
		}

		// fuer alle unterverzeichnisse recursiv aufrufen
		for(Dir actDir : this.getDir())
		{
			if(! actDir.isMatchSuccessfullRecursive())
			{
				return false;
			}
		}

		return matchSuccess;
	}

	/**
	 * gibt zurueck ob alle verglichenen eigenschaften zufriedenstellend zusammengepasst haben
	 * @param
	 */
	private boolean isMatchSuccessfull()
	{
		if(this.flagPathMatched && this.flagOccuranceMatched && !this.flagFuzzyReference)
		{
			return true;
		}
		return false;
	}

	/**
	 * setzt den flagOccuranceMatched false|true
	 */
	private void detOccurance()
	{
		if(this.getActRole().equals("exam"))
		{
			detOccuranceExaminee();
		}
		else if(this.getActRole().equals("ref"))
		{
			detOccuranceReference();
		}
		else
		{
			System.err.println("actRole not known: "+this.getActRole());
			System.exit(1);
		}
	}

	/**
	 * das reference dir wird durchgegangen und festgestellt ob es einheiten gibt, deren minoccur unterschritten wurde
	 */
	private void detOccuranceReference()
	{
		if(this.getMatchedDir().size() < this.getMinOccur())
		{
			this.log.add(new Log("error", "(ref) this dir has "+ this.getMatchedDir().size() +" matched Dirs. this is less than the minoccur "+this.getMinOccur()+" matches."));
		}
		else if(this.getMatchedDir().size() > this.getMaxOccur())
		{
			this.log.add(new Log("error", "(ref) this dir has "+ this.getMatchedDir().size() +" matched Dirs. this is more than the maxoccur "+this.getMaxOccur()+" matches."));
		}
		else
		{
			this.setFlagOccuranceMatched(true);
		}

		for(File actFile : this.getFile())
		{
			actFile.detOccuranceReference();
		}
		
		for(Dir actDir : this.getDir())
		{
			actDir.detOccuranceReference();
		}
	}
	
	/**
	 * das examinee dir wird durchgegangen und entsprechend der gematchten Einheiten in reference dir wird festgestellt ob die occurance matcht
	 */
	private void detOccuranceExaminee()
	{

		// wenn das examinee-Directory nicht genau 1 match aufweist, so liegt eine unschaerfe im referenz-fingerprint vor
		if(this.getMatchedDir().size() > 1)
		{
			this.setFlagFuzzyReference(true);
			this.log.add(new Log("error", "(exam) this dir has more than 1 matches. this indicates a fuzzyness in the pathpatterns of the reference."));
		}
		else if(this.getMatchedDir().size() < 1)
		{
			this.log.add(new Log("debug", "(exam) this dir path has 0 matches. this is not a problem"));
		}

		// ermitteln ob es im reference-directory entsprechend der ocurance-angaben gematched wurde
		for(Dir actMatchedDir : this.getMatchedDir())
		{
			if( (actMatchedDir.getMatchedDir().size() < actMatchedDir.getMinOccur()) || (actMatchedDir.getMatchedDir().size() > actMatchedDir.getMaxOccur()) )
			{
				this.log.add(new Log("debug", "(exam) this dir path does not fit in the occurance-definition of the reference."));
			}
			else
			{
				this.flagOccuranceMatched = true;
			}
		}

		// alle enthaltenen Files auch matchOccurance
		for(File actFile : this.getFile())
		{
			actFile.detOccuranceExaminee();
		}
		
		// fuer alle enthaltenen dirs auch die occurance matchen
		for(Dir actDir : this.getDir())
		{
			actDir.detOccuranceExaminee();
		}

	}

	/**
	 * geht den vollstaendigen baum durch und prueft ob es zu this passende eintraege gibt
	 * passende eintraege werden auf beiden seiten (template-baum <-> examinee-baum) vermerkt
	 * es passt, wenn
	 * 1) die pfad-angabe im examinee auf das pfad-pattern im template (this) matcht
	 * @param examineeDir
	 * @return allMatchingDirByPath
	 */
	private void match(Dir examineeDir)
	{
		this.setActRole("ref");
		examineeDir.setActRole("exam");
		
		// alle enthaltenen directories sollen ebenfalls gematched werden
		for(Dir actDir : this.getDir())
		{
//			this.log.add(new Log("debug", "match also for the subdir "+actDir.getPath()+" (exam Directory: "+examineeDir.getPath()));
			actDir.match(examineeDir);
		}
		
		// wenn beide die gleiche pfadtiefe aufweisen und
		// wenn das ref nicht schon als matchend vermerkt wurde
		// nur dann soll ueberprueft werden
		if(examineeDir.getPathDepth() == this.getPathDepth())
		{
			examineeDir.log.add(new Log("debug", "(exam) this dir path ("+examineeDir.getPathWithoutQuotes()+") has the same PathDepth ("+examineeDir.getPathDepth()+"="+this.getPathDepth()+") like dir (id="+this.getId()+", path="+this.getPathWithoutQuotes()+")"));
			this.log.add(new Log("debug", "(ref) this dir path ("+this.getPathWithoutQuotes()+") has the same pathDepth ("+this.getPathDepth()+"="+examineeDir.getPathDepth()+") like (id="+examineeDir.getId()+", path="+examineeDir.getPathWithoutQuotes()+")"));
			if(!examineeDir.getMatchedDir().contains(this))
			{
				if( examineeDir.getPathWithoutQuotes().matches("^"+this.getPath()+"$")) 
				{
					// passen beide vergleichspartner? Dann soll das passende gegenstueck im jeweils anderen abgelegt werden
					// und die flags fuer die checks gesetzt werden
					examineeDir.getMatchedDir().add(this);
					examineeDir.setFlagPathMatched(true);
					examineeDir.log.add(new Log("debug", "(exam) this dir path ("+examineeDir.getPathWithoutQuotes()+") matched with dir (id="+this.getId()+", path="+this.getPathWithoutQuotes()+")"));
	
					this.addMatchedDir(examineeDir);
					this.setFlagPathMatched(true);
					this.log.add(new Log("debug", "(ref) this dir path ("+this.getPathWithoutQuotes()+") matched with dir (id="+examineeDir.getId()+", path="+examineeDir.getPathWithoutQuotes()+")"));
				}
				else
				{
	//				examineeDir.log.add(new Log("debug", "(exam) this dir path ("+examineeDir.getPath()+") did NOT match with dir (id="+this.getId()+", path="+this.getPath()+")"));
					this.log.add(new Log("debug", "(ref) this dir path ("+this.getPathWithoutQuotes()+") did NOT match with dir (id="+examineeDir.getId()+", path="+examineeDir.getPathWithoutQuotes()+")"));
				}
				
				// alle im verzeichnis befindlichen files mit vollstaendigen examineeDir abgleichen
				for(File actFile : this.getFile())
				{
//					this.log.add(new Log("debug", "match also for the file "+actFile.getPath()+" (exam Directory: "+examineeDir.getPath()+")"));
					actFile.match(examineeDir);
				}

				// an dieser stelle kann abgebrochen werden,
				// da darin liegende pfade eine hoehere pfadtiefe aufweisen als 'this'
			}
		}
		
		// wenn examinee eine geringere pfadtiefe aufweisst
		// sollen alle darin befindlichen directories gematcht werden
		else if(examineeDir.getPathDepth() < this.getPathDepth())
		{
			// auch fuer alle in examineeDir enthaltenen Dirs ausfuehren
			for(Dir actExamineeDir : examineeDir.getDir())
			{
				actExamineeDir.setActRole("exam");
				this.match(actExamineeDir);
			}
		}
		
	}

	/**
	 * every dir and file of the template has to find the given occurance from the examinee
	 * @return
	 */
	public void runCheck(Dir examineeDir)
	{
		// loeschen bestehender match-eintraege in this
		this.clearMatchRecursive();
		examineeDir.clearMatchRecursive();
		
		this.match(examineeDir);

		examineeDir.detOccurance();
		this.detOccurance();
	}

	/**
	 * loescht die match-Eintraege auch aller enthaltener Dirs und Files
	 */
	private void clearMatchRecursive()
	{
		this.getMatchedDir().clear();
		this.getLog().clear();
		this.setFlagPathMatched(false);
		this.setFlagOccuranceMatched(false);
		this.setFlagFuzzyReference(false);
		this.setActRole(null);

		for(Dir actDir : this.getDir())
		{
			actDir.clearMatchRecursive();
		}
		for(File actFile : this.getFile())
		{
			actFile.getMatchedFile().clear();
			actFile.getLog().clear();
			actFile.setFlagPathMatched(false);
			actFile.setFlagPathMatched(false);
			actFile.setFlagSizeMatched(false);
			actFile.setFlagOccuranceMatched(false);
			actFile.setFlagFuzzyReference(false);
			actFile.setActRole(null);
		}
	}

	/**
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public void genFingerprint(float sizeToleranceDef, final boolean respectMd5, final ArrayList<String> ignoreLines) throws NullPointerException, IOException
	{
		directoryPath.clear();
		this.sizeToleranceDefault = sizeToleranceDef;

//		this.setRespectMd5Recursive(respectMd5);
		
		if(basepath == null)
		{
			System.err.println("error: no basepath given. cannot generate a fingerprint without a basepath.");
			throw new NullPointerException();
		}

		// den directory-baum durchgehen und fuer jeden eintrag ein entity erstellen
		Files.walkFileTree(this.getBasepath(), new FileVisitor<Path>()
		{
			// called after a directory visit is complete
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
			{
//				String pathString = dir.getParent() + "/"+dir.getFileName();
//				System.out.println("after visit directory: "+pathString);
				// das letzte directory aus dem pfad entfernen
				directoryPath.remove(directoryPath.size()-1);
				return FileVisitResult.CONTINUE;
			}

			// called before a directory visit
			public FileVisitResult preVisitDirectory(Path walkingDir, BasicFileAttributes attrs) throws IOException
			{
				// alle ignore-eintraege durchgehen und feststellen ob das aktuell besuchte file ignoriert werden soll
				FileSystem fileSystem = FileSystems.getDefault();
				for(String actPattern : ignoreLines)
				{
//					System.out.println("debug: globbing for "+"glob:" + basepath.toFile().getCanonicalPath() + "/" + actPattern);
					PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + basepath.toFile().getCanonicalPath() + "/" + actPattern);
//					System.out.println("debug1: vergleichen: "+pathMatcher.toString());
//					System.out.println("debug2: vergleichen: "+walkingDir.getFileName());
					if (pathMatcher.matches(walkingDir.toFile().getCanonicalFile().toPath()))
					{
//						System.out.println("debug: ignoring directory "+walkingDir.getFileName());
						// dummy-maessig ein verzeichnis hinzufuegen (dieses trackt die bewegung durch das filesystem
						// im postVisitDirectory wird dieser eintrag wieder entfernt
						directoryPath.add(new Dir());
						return FileVisitResult.CONTINUE;
					}
				}

				// relativen Pfad (zur Basis basepath) feststellen
//				String pathString = walkingDir.getParent() + "/"+walkingDir.getFileName();
				String relPathString = getBasepath().relativize(walkingDir).toString();

				// wenn der relative path ein leerer string ist, ist this das directory
				if(relPathString.equals(""))
				{
//					System.out.println("THIS DIR IS ROOT: "+walkingDir.getFileName());
					directoryPath.add(thisObj);
				}
				else
				{
					// und das gleich zu betretende verzeichnis der besuchten directories hinzufuegen
					Dir newDir = new Dir();
					directoryPath.get(directoryPath.size()-1).addDir(newDir);	// das neue verz. dem letzten verzeichnis hinzufuegen
					directoryPath.add(newDir); // das neue verzeichnis im path ablegen
				}

//				System.err.println("before visit directory (abs): "+pathString);
//				System.err.println("before visit directory (rel): "+relPathString);

				// die Daten setzen
				directoryPath.get(directoryPath.size()-1).setId(runningId++);

				// der pfad wird bei vergleichen als pattern verwendet / bzw. kann vom user manuel zu einer pattern veraendert werden
				// deshalb sollen besondere zeichen beim erstellen eines fingerprints escaped werden
//				String relPathStringWithEscapedMetaCaracters = relPathString.replaceAll("([\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|])", "\\\\$1");

				directoryPath.get(directoryPath.size()-1).setPath(Pattern.quote(relPathString));
				directoryPath.get(directoryPath.size()-1).setMinOccur(1);
				directoryPath.get(directoryPath.size()-1).setMaxOccur(1);

//				System.out.println(dir.toString());

				return FileVisitResult.CONTINUE;
			}

			// called for each file visited. the basic file attributes of the file are also available
			public FileVisitResult visitFile(Path walkingFile, BasicFileAttributes attrs) throws IOException
			{
				// TODO: handling mit symbolic links implementieren
				// wenn es sich um einen symbolischen link handelt, soll nichts gemacht werden
				if(Files.isSymbolicLink(walkingFile))
				{
					return FileVisitResult.CONTINUE;
				}

				// alle ignore-eintraege durchgehen und feststellen ob das aktuell besuchte file ignoriert werden soll
				FileSystem fileSystem = FileSystems.getDefault();
				for(String actPattern : ignoreLines)
				{
//					System.out.println("debug: globbing for "+"glob:" + basepath.toFile().getCanonicalPath() + "/" + actPattern);
					PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + basepath.toFile().getCanonicalPath() + "/" + actPattern);
//					System.out.println("debug1: vergleichen: "+pathMatcher.toString());
//					System.out.println("debug2: vergleichen: "+walkingFile.getFileName());
					if (pathMatcher.matches(walkingFile.toFile().getCanonicalFile().toPath()))
					{
//						System.out.println("debug: ignoring file "+walkingFile.getFileName());
						return FileVisitResult.CONTINUE;
					}
				}

				// relativen Pfad (zur Basis basepath) feststellen
				String pathString = walkingFile.getParent() + "/"+walkingFile.getFileName();
				String relPathString = getBasepath().relativize(walkingFile).toString();

//				System.err.println("visit file (abs): "+pathString);
//				System.err.println("visit file (rel): "+relPathString);

				// new File erstellen
				File file = new File();
				file.setId(runningId++);
				
				// md5 feststellen
				if(respectMd5)
				{
					file.setRespectMd5(true);
					try
					{
						file.setMd5(Md5Checksum.getMd5Checksum(pathString));
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// der pfad wird bei vergleichen als pattern verwendet / bzw. kann vom user manuel zu einer pattern veraendert werden
				// deshalb sollen besondere zeichen beim erstellen eines fingerprints escaped werden
//				String relPathStringWithEscapedMetaCaracters = relPathString.replaceAll("([\\\\\\.\\[\\{\\(\\*\\+\\?\\^\\$\\|])", "\\\\$1");
				
				file.setPath(Pattern.quote(relPathString));
				file.setMinOccur(1);
				file.setMaxOccur(1);

				file.setSize((float)attrs.size());
				try {
					file.setSizeUnit("B");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				file.setSizeTolerance(sizeToleranceDefault);

				directoryPath.get(directoryPath.size()-1).addFile(file);

//				System.out.println(file.toString());

				return FileVisitResult.CONTINUE;
			}

			// called for each file if the visit failed
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
			{
				System.out.println("visit file FAILED: "+file.getFileName());
				return FileVisitResult.CONTINUE;
			}

		});

	}

	public Dir readXml() throws JAXBException
	{
		if (this.getInfilexml() == null)
		{
			throw new NullPointerException();
		}

		JAXBContext context = JAXBContext.newInstance(de.prozesskraft.jaxb.ptest.Dir.class);
		Unmarshaller um = context.createUnmarshaller();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try
		{
			// stream auf das schema oeffnen (liegt im root verezichnis des jars in das es gepack wird)
			InputStream schemaStream = this.getClass().getResourceAsStream("/ptest.xsd");
			BufferedReader schemaReader = new BufferedReader(new InputStreamReader(schemaStream));

			// und in dieses temp-verzeichnis schreiben
			java.io.File tmpFile = java.io.File.createTempFile("avoge2013", "ptest.xsd");
			FileWriter fstream = new FileWriter(tmpFile);
			BufferedWriter schemaWriter = new BufferedWriter(fstream);

			String thisLine;
			while((thisLine = schemaReader.readLine()) != null)
			{
//				System.out.println(thisLine);
				schemaWriter.append(thisLine);
			}
			schemaWriter.close();

			// das temporaere schemafile beim unmarshaller angeben, damit es zur validierung verwendet wird
			schema = sf.newSchema(tmpFile);
			um.setSchema(schema);
		}
		catch (SAXException e)
		{
			System.err.println("error: reading schema.");
			e.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (NullPointerException e2)
		{
			System.err.println("error: xml schema file not found");
			e2.printStackTrace();
		}

//		Process destObject = null;
		// das aktuelle xml-file in die jaxb-klassen einlesen
		try
		{
			de.prozesskraft.jaxb.ptest.Dir xptest = (de.prozesskraft.jaxb.ptest.Dir) um.unmarshal(new java.io.File(this.getInfilexml()));
//			System.out.println("xprocess variable1 free = "+xprocess.getStep().get(0).getCommit().get(0).getVariable().get(0).isFree());
//			BeanMappingBuilder builder = new BeanMappingBuilder()
//			{
//				protected void configure()
//				{
//					mapping(de.caegroup.jaxb.process.Process.class, de.caegroup.process.Process.class, oneWay(), mapId("A"), mapNull(true));
//				}
//			};

//			System.out.println("processName1: "+this.getName());
			DozerBeanMapper mapper = new DozerBeanMapper();
//			destObject = mapper.map(xprocess, de.caegroup.process.Process.class);
			mapper.map(xptest, this);
//			System.out.println("processName2: "+this.getName());

			// setzen der parenteintraege aller steps
//			this.affiliate();
			// die jaxb-klassen mit den domain-klassen mappen
//			System.out.println("processName3: "+this.getName());
			
			// ueberpruefen ob der process consistent ist
//			if(this.isProcessConsistent())
//			{
//				this.log("info", "check process consistency successfull.");
//			}
//			else
//			{
//				this.log("info", "check process consistency NOT successfull.");
//			}
		}
		catch (javax.xml.bind.UnmarshalException e)
		{
			System.err.println("error: cannot unmarshall xml-file: "+this.getInfilexml());
			e.printStackTrace();
		}

		return this;
	}

	/**
	 * schreiben des aktuellen prozesses in ein xml file
	 * @throws JAXBException 
	 * 
	 **/
	public void writeXml()
	{

		JAXBContext jaxbContext;
		try
		{
			jaxbContext = JAXBContext.newInstance(de.prozesskraft.jaxb.ptest.Dir.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// die daten aus this in das jaxb objekt mappen
			de.prozesskraft.jaxb.ptest.Dir xptest = new de.prozesskraft.jaxb.ptest.Dir();
			DozerBeanMapper mapper = new DozerBeanMapper();
//			mapper.map(xprocess, this);
			mapper.map(this, xptest);

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			// wenn ein oufilexml existiert, dann ins file schreiben
			if(this.outfilexml != null)
			{
				jaxbMarshaller.marshal(xptest, new java.io.File(this.outfilexml));
			}
			else
			{
				jaxbMarshaller.marshal(xptest, System.out);
			}
			
		}
		catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addMatchedDir(Dir dir)
	{
		this.getMatchedDir().add(dir);
	}
	
	public void addDir(Dir dir)
	{
		this.dir.add(dir);
	}
	
	public void addFile(File file)
	{
		this.file.add(file);
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the minOccur
	 */
	public int getMinOccur() {
		return minOccur;
	}

	/**
	 * @param minOccur the minOccur to set
	 */
	public void setMinOccur(int minOccur) {
		this.minOccur = minOccur;
	}

	/**
	 * @return the maxOccur
	 */
	public int getMaxOccur() {
		return maxOccur;
	}

	/**
	 * @param maxOccur the maxOccur to set
	 */
	public void setMaxOccur(int maxOccur) {
		this.maxOccur = maxOccur;
	}

	/**
	 * @return the pathWithoutQuotes
	 */
	public String getPathWithoutQuotes()
	{
		return path.replaceAll("^\\\\Q|\\\\E$", "");
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the file
	 */
	public ArrayList<File> getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(ArrayList<File> file) {
		this.file = file;
	}

	/**
	 * @return the dir
	 */
	public ArrayList<Dir> getDir() {
		return dir;
	}

	/**
	 * @param dir the dir to set
	 */
	public void setDir(ArrayList<Dir> dir) {
		this.dir = dir;
	}

	/**
	 * @return the infilexml
	 */
	public String getInfilexml() {
		return infilexml;
	}

	/**
	 * @param infilexml the infilexml to set
	 */
	public void setInfilexml(String infilexml) {
		this.infilexml = infilexml;
	}

	/**
	 * @return the outfilexml
	 */
	public String getOutfilexml() {
		return outfilexml;
	}

	/**
	 * @param outfilexml the outfilexml to set
	 */
	public void setOutfilexml(String outfilexml) {
		this.outfilexml = outfilexml;
	}

	/**
	 * @return the basedir
	 */
	public Path getBasepath() {
		return basepath;
	}

	/**
	 * @return the basedir
	 */
	public String getBasepathAsString() {
		return basepath.toString();
	}

	/**
	 * @param basedir the basedir to set
	 */
	public void setBasepath(String basedir) {
		this.basepath = Paths.get(basedir);
	}

	/**
	 * @param basedir the basedir to set
	 */
	public void setBasepath(Path basepath) {
		this.basepath = basepath;
	}

	/**
	 * @return the runningId
	 */
	public int getRunningId() {
		return runningId;
	}

	/**
	 * @param runningId the runningId to set
	 */
	public void setRunningId(int runningId) {
		this.runningId = runningId;
	}

	/**
	 * @return the matchedDir
	 */
	public ArrayList<Dir> getMatchedDir() {
		return matchedDir;
	}

	/**
	 * @param matchedDir the matchedDir to set
	 */
	public void setMatchedDir(ArrayList<Dir> matchedDir) {
		this.matchedDir = matchedDir;
	}

	/**
	 * @return the flagPathMatched
	 */
	public boolean isFlagPathMatched() {
		return flagPathMatched;
	}

	/**
	 * @param flagPathMatched the flagPathMatched to set
	 */
	public void setFlagPathMatched(boolean flagPathMatched) {
		this.flagPathMatched = flagPathMatched;
	}

	/**
	 * @return the flagOccuranceMatched
	 */
	public boolean isFlagOccuranceMatched() {
		return flagOccuranceMatched;
	}

	/**
	 * @param flagOccuranceMatched the flagOccuranceMatched to set
	 */
	public void setFlagOccuranceMatched(boolean flagOccuranceMatched) {
		this.flagOccuranceMatched = flagOccuranceMatched;
	}

	/**
	 * @return the flagFuzzyReference
	 */
	public boolean isFlagFuzzyReference() {
		return flagFuzzyReference;
	}

	/**
	 * @param flagFuzzyReference the flagFuzzyReference to set
	 */
	public void setFlagFuzzyReference(boolean flagFuzzyReference) {
		this.flagFuzzyReference = flagFuzzyReference;
	}

	/**
	 * @return the log
	 */
	public ArrayList<Log> getLog() {
		return log;
	}

	/**
	 * @param log the log to set
	 */
	public void setLog(ArrayList<Log> log) {
		this.log = log;
	}

	/**
	 * @return the actRole
	 */
	public String getActRole() {
		return actRole;
	}

	/**
	 * @param actRole the actRole to set
	 */
	public void setActRole(String actRole) {
		this.actRole = actRole;
	}

	/**
	 * liefert die Tiefe des Pfades zurueck
	 * 0 bei pfaden wie "data"
	 * 0 bei ""
	 * 1 bei "data/special"
	 * 2 bei "data/special/kitty"
	 * @return
	 */
	public int getPathDepth()
	{
		if(this.getPath().equals("\\Q\\E"))
		{
			return 0;
		}
		else
		{
			String[] splittedPath = this.getPath().split("/");
			return(splittedPath.length);
		}
	}

	/**
	 * @return the respectMd5
	 */
	public boolean isRespectMd5() {
		return respectMd5;
	}

	/**
	 * @param respectMd5 the respectMd5 to set
	 */
	public void setRespectMd5(boolean respectMd5) {
		this.respectMd5 = respectMd5;
	}
	
	/**
	 * @param respectMd5 the respectMd5 to set
	 */
	public void setRespectMd5Recursive(boolean respectMd5) {
		
		this.respectMd5 = respectMd5;
	
		// fuer alle files setzen
		for(File actFile : this.getFile())
		{
			actFile.setRespectMd5(respectMd5);
		}

		// fuer alle unterverzeichnisse recursiv aufrufen
		for(Dir actDir : this.getDir())
		{
			actDir.setRespectMd5Recursive(respectMd5);
		}

		this.respectMd5 = respectMd5;
	}
	
}
