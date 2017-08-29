package de.prozesskraft.ptest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.prozesskraft.commons.Log;

public class File {

	private Integer id = null;
	private int minOccur = 0;
	private int maxOccur = 0;
	private String path = null;
	private Float size = null;
	private String sizeUnit = "B";
	private Float sizeTolerance = 0F;
	private String md5 = null;

	// ist es ein template? , dann wird in match alle passenden ids aus dem pruefling gesammelt.
	// ist es ein pruefling?, dann wird in match die id des passenden eintrages aus dem template eingetragen (in diesem fall kann es nur 1 stk sein)
	private ArrayList<File> matchedFile = new ArrayList<File>();

	public ArrayList<Log> log = new ArrayList<Log>();

	private boolean respectMd5 = true;
	
	private boolean flagMd5Matched = false;
	private boolean flagPathMatched = false;
	private boolean flagSizeMatched = false;
	private boolean flagOccuranceMatched = false;
	private boolean flagFuzzyReference = false;

	private String actRole = null; // ref|exam

	public File()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);

		minOccur = 0;
		maxOccur = 99999;
	}

	/**
	 * @return the whole log of this as a String
	 */
	public String getLogAsString()
	{
		return Log.sprintWholeLog(this.log);
	}
	
	/**
	 * erzeugt eine csv-zeile mit allen relevanten daten, nur wenn die occurance-angaben ueber- oder unterschritten werden
	 * @return
	 */
	public String getReferenceSummaryAsCsvLine(String scope)
	{
		boolean error = false;
		
		String csvLine = "ref";

		csvLine += ";" + this.getId();

		csvLine += ";" + "file";
		csvLine += ";" + this.getPathWithoutQuotes();

		// gesamtergebnis
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
		if(this.isFlagSizeMatched())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}

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

		if(this.isFlagMd5Matched())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}

		String note = "";

		if(this.getMatchedFile().size() < this.getMinOccur())
		{
			note = "error: occurance of matched files: at least "+this.getMinOccur()+" matches are needed (minOccur), but only "+this.getMatchedFile().size()+" files matched "+this.getMatchedFile().toString();
		}
		else if(this.getMatchedFile().size() > this.getMaxOccur())
		{
			note = "error: occurance of matched files: max "+this.getMaxOccur()+" matches are allowed (maxOccur), but "+this.getMatchedFile().size()+" files matched "+this.getMatchedFile().toString();
		}

		csvLine += ";" + note;

		// rueckgabe in abhaengigkeit von scope und error
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

	/**
	 * erzeugt eine csv-zeile mit allen relevanten daten
	 * @return
	 */
	public String getExamineeSummaryAsCsvLine(String scope)
	{
		boolean error = false;
		
		String csvLine = "exam";

		csvLine += ";" + this.getId();
		
		csvLine += ";" + "file";
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
		
		if(this.isFlagSizeMatched())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}
		
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
		
		if(this.isFlagMd5Matched())
		{
			csvLine += ";" + "x";
		}
		else
		{
			csvLine += ";" + "o";
		}
		
		// rueckgabe in abhaengigkeit von scope und error
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
	
	/**
	 * gibt zurueck ob alle verglichenen eigenschaften zufriedenstellend zusammengepasst haben
	 * @param
	 */
	public boolean isMatchSuccessfull()
	{
		// wenn ueber md5 gematched werden soll
		if(this.respectMd5)
		{
//			System.out.println(this.actRole+" file respectMD5: "+this.getPath());
			if(this.flagMd5Matched)
			{
				return true;
			}
		}
		// sonst ueber alle anderen angaben matchen
		else
		{
//			System.out.println(this.actRole+"file DOES NOT respectMD5: "+this.getPath());
			if(this.flagPathMatched && this.flagSizeMatched && this.flagOccuranceMatched && !this.flagFuzzyReference)	
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * das reference file wird durchgegangen und festgestellt ob es einheiten gibt, deren minoccur unterschritten oder maxoccur ueberschritten wurde wurde
	 */
	public void detOccuranceReference()
	{
		if(this.getMatchedFile().size() < this.getMinOccur())
		{
			this.log.add(new Log("error", "(ref) this file has "+ this.getMatchedFile().size() +" matched Files. this is less than the minoccur "+this.getMinOccur()+" matches."));
		}
		else if(this.getMatchedFile().size() > this.getMaxOccur())
		{
			this.log.add(new Log("error", "(ref) this file has "+ this.getMatchedFile().size() +" matched Files. this is more than the maxoccur "+this.getMaxOccur()+" matches."));
		}
		else
		{
			this.setFlagOccuranceMatched(true);
		}
	}

	/**
	 * alle Dirs und Files durchgehen
	 * 1) gibt es ein Dir | File im template, dessen match den minoccur unterschreitet?
	 * ...
	 */
	public void detOccuranceExaminee()
	{

		// wenn das examinee-File nicht genau 1 match aufweist, so liegt eine unschaerfe im referenz-fingerprint vor
		if(this.getMatchedFile().size() > 1)
		{
			this.setFlagFuzzyReference(true);
			this.log.add(new Log("error", "(exam) this file has more than 1 matches. this indicates a fuzzyness in the patterns of the reference."));
		}
		else if(this.getMatchedFile().size() < 1)
		{
			this.log.add(new Log("debug", "(exam) this dir path has 0 matches. this is not a problem."));
		}

		// ermitteln ob es in reference-files entsprechend der ocurance-angaben gematched wurde
		for(File actMatchedFile : this.getMatchedFile())
		{
			if((actMatchedFile.getMatchedFile().size() < actMatchedFile.getMinOccur()) || (actMatchedFile.getMatchedFile().size() > actMatchedFile.getMaxOccur()) )
			{
				this.log.add(new Log("debug", "(exam) this file path does not fit in the occurance-definition of the reference."));
			}
			else
			{
				this.flagOccuranceMatched = true;
			}
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
	public void match(Dir examineeDir)
	{
		this.setActRole("ref");
		
		// wenn pfadTiefe von (examineeDir+1) != der pfadtiefe dieses files ist,
		// soll abgebrochen werden
		if((examineeDir.getPathDepth() + 1) != this.getPathDepth())
		{
			return;
		}
		// wenn die pfadtiefe von examDir+1 == der pfadTiefe des aktuellen files ist
		// sollen die files darin untersucht werden
		else if((examineeDir.getPathDepth() + 1) == this.getPathDepth())
		{
			// alle files im examineeDir durchgehen und ueberpruefen ob sie matchen
			for(File actFile : examineeDir.getFile())
			{
				actFile.setActRole("exam");
	
				// wenn this nicht schon als matchend erkannt wurde, soll matchueberpruefung stattfinden
				if((!actFile.getMatchedFile().contains(this)))
				{
					// wenn beide files einen md5-eintarg enthalten, dann den vergleich darueber fuehren
					if((actFile.getMd5() != null) && (this.getMd5() != null))
					{
//						System.out.println("-----------------");
//						System.out.println("beide vergleichspartner haben eine md5");
						if(actFile.getMd5().matches("^" + this.getMd5() + "$"))
						{
//							System.out.println("MD5 passen! " + actFile.getMd5());
//							System.out.println("exam: " + actFile.getPathWithoutQuotes());
//							System.out.println("ref:  " + this.getPathWithoutQuotes());
//							System.out.println("-----------------");

							actFile.setFlagMd5Matched(true);
							actFile.log.add(new Log("debug", "(exam) file ("+actFile.getPathWithoutQuotes()+") md5 matched with (id="+this.getId()+", path="+this.getPathWithoutQuotes()+")"));
							this.setFlagMd5Matched(true);
							this.log.add(new Log("debug", "(ref) file ("+this.getPathWithoutQuotes()+") md5 matched with (id="+actFile.getId()+", path="+actFile.getPathWithoutQuotes()+")"));
						}
//						else
//						{
//							System.out.println("MD5 passen NICHT! " + actFile.getMd5());
//							System.out.println("exam: " + actFile.getPathWithoutQuotes());
//							System.out.println("ref:  " + this.getPathWithoutQuotes());
//							System.out.println("-----------------");
//						}
					}
//					else
//					{
//						System.out.println("-----------------");
//						System.out.println("nicht beide vergleichspartner haben eine md5");
//						System.out.println("exam: MD5: " + actFile.getMd5());
//						System.out.println("ref: MD5: " + this.getMd5());
//						System.out.println("exam: " + actFile.getPathWithoutQuotes());
//						System.out.println("ref:  " + this.getPathWithoutQuotes());
//						System.out.println("-----------------");
//					}

					// den pfad vom examinee gegen den template-pfad matchen
					if(actFile.getPathWithoutQuotes().matches("^"+this.getPath()+"$"))
					{
						actFile.setFlagPathMatched(true);
						actFile.log.add(new Log("debug", "(exam) file ("+actFile.getPathWithoutQuotes()+") path matched with (id="+this.getId()+", path="+this.getPathWithoutQuotes()+")"));
						this.setFlagPathMatched(true);
						this.log.add(new Log("debug", "(ref) file ("+this.getPathWithoutQuotes()+") path matched with (id="+actFile.getId()+", path="+actFile.getPathWithoutQuotes()+")"));
		
						// die groesse vergleichen
						if(this.doesSizeMatch(actFile))
						{
							// vermerken, dass die groesse gepasst hat
							actFile.setFlagSizeMatched(true);
							// passen beide vergleichspartner? Dann soll das korrespondierende file abgelegt werden
							actFile.getMatchedFile().add(this);
							actFile.log.add(new Log("debug", "(exam) file size ("+actFile.getSize()+actFile.getSizeUnit()+") matched with (id="+this.getId()+", path="+this.getPathWithoutQuotes()+", size="+this.getSize()+this.getSizeUnit()+")"));
		
							this.setFlagSizeMatched(true);
							this.getMatchedFile().add(actFile);
							this.log.add(new Log("debug", "(ref) file size ("+this.getSize()+this.getSizeUnit()+") matched with (id="+actFile.getId()+", path="+actFile.getPathWithoutQuotes()+", size="+actFile.getSize()+actFile.getSizeUnit()+")"));
						}
						else
						{
		//					actFile.log.add(new Log("debug", "(exam) file size ("+actFile.getSize()+actFile.getSizeUnit()+") did NOT match with (id="+this.getId()+", path="+this.getPath()+", size="+this.getSize()+this.getSizeUnit()+")"));
		//					this.log.add(new Log("debug", "(ref) file size ("+this.getSize()+this.getSizeUnit()+") did NOT match with (id="+actFile.getId()+", path="+actFile.getPath()+", size="+actFile.getSize()+actFile.getSizeUnit()+")"));
						}
					}
					else
					{
		//				actFile.log.add(new Log("debug", "(exam) file path ("+actFile.getPath()+") did NOT match with (id="+this.getId()+", path="+this.getPath()+")"));
		//				this.log.add(new Log("debug", "(ref) file path ("+this.getPath()+") did NOT match with (id="+actFile.getId()+", path="+actFile.getPath()+")"));
					}
				}
			}
		}
	}

	/**
	 * ueberprueft ob die groessenangaben des examinneFiles in die Angaben des templates (this) passen
	 * @param examineeFile
	 * @return boolean
	 */
	private boolean doesSizeMatch(File examineeFile)
	{
		boolean result = false;
		
		float lowerBound = this.getSizeInByte() * (1 - this.getSizeTolerance());
		float upperBound = this.getSizeInByte() * (1 + this.getSizeTolerance());
		
		// ueberpruefen ob sich die groessenangabe innerhalb der toleranz befindet
		if( (examineeFile.getSizeInByte() >= lowerBound) && (examineeFile.getSizeInByte() <= upperBound) )
		{
			result = true;
		}
		else
		{
			this.log.add(new Log("debug", "sizeTolerance="+this.getSizeTolerance()+" | "+lowerBound+" < x < "+upperBound));
		}
		
		return result;
	}
	
	public String toString()
	{
		String entityString = "";
		
		entityString += "id="+this.getId()+", minOccur: "+this.getMinOccur()+", maxOccur: "+this.getMaxOccur()+", path: "+this.getPath()+", size: "+this.getSize()+", sizetolerance: "+this.getSizeTolerance();
		
		return entityString;
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
	 * @return the minoccur
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
	 * @return the size in Byte
	 */
	public Float getSizeInByte() {
		if(this.getSizeUnit().equalsIgnoreCase("GB"))
		{
			return(size*1024*1024*1024);
		}
		else if(this.getSizeUnit().equalsIgnoreCase("MB"))
		{
			return(size*1024*1024);
		}
		else if(this.getSizeUnit().equalsIgnoreCase("KB"))
		{
			return(size*1024);
		}
		else
		{
			return size;
		}
	}

	/**
	 * @return the size
	 */
	public Float getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(Float size) {
		this.size = size;
	}

	/**
	 * @return the sizeTolerance
	 */
	public Float getSizeTolerance() {
		return sizeTolerance;
	}

	/**
	 * @param sizeTolerance the sizeTolerance to set
	 */
	public void setSizeTolerance(Float sizeTolerance) {
		this.sizeTolerance = sizeTolerance;
	}

	/**
	 * @return the sizeUnit
	 */
	public String getSizeUnit() {
		return sizeUnit;
	}

	/**
	 * @param sizeUnit the sizeUnit to set
	 * @throws Exception 
	 */
	public void setSizeUnit(String sizeUnit) throws Exception {
		if( (sizeUnit.equalsIgnoreCase("B")) || (sizeUnit.equalsIgnoreCase("KB")) || (sizeUnit.equalsIgnoreCase("MB")) || (sizeUnit.equalsIgnoreCase("GB")) )
		{
			this.sizeUnit = sizeUnit;
		}
		else
		{
			throw illegalSizeUnitException();
		}
	}

	private Exception illegalSizeUnitException() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the md5
	 */
	public String getMd5() {
		return md5;
	}

	/**
	 * @param md5 the md5 to set
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}

	/**
	 * @return the matchedFile
	 */
	public ArrayList<File> getMatchedFile() {
		return matchedFile;
	}

	/**
	 * @param matchedFile the matchedFile to set
	 */
	public void setMatchedFile(ArrayList<File> matchedFile) {
		this.matchedFile = matchedFile;
	}

	/**
	 * @return the flagMd5Matched
	 */
	public boolean isFlagMd5Matched() {
		return flagMd5Matched;
	}

	/**
	 * @param flagMd5Matched the flagMd5Matched to set
	 */
	public void setFlagMd5Matched(boolean flagMd5Matched) {
		this.flagMd5Matched = flagMd5Matched;
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
	 * @return the flagSizeMatched
	 */
	public boolean isFlagSizeMatched() {
		return flagSizeMatched;
	}

	/**
	 * @param flagSizeMatched the flagSizeMatched to set
	 */
	public void setFlagSizeMatched(boolean flagSizeMatched) {
		this.flagSizeMatched = flagSizeMatched;
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
	 * 0 bei pfaden wie "hello.txt"
	 * 0 bei ""
	 * 1 bei "data/kitty.txt"
	 * 2 bei "data/special/kitty"
	 * @return
	 */
	public int getPathDepth()
	{
		String[] splittedPath = this.getPath().split("/");
		return(splittedPath.length);
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
	
}
