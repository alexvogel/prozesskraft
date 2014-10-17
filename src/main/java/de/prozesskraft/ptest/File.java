package de.prozesskraft.ptest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class File {

	private Integer id = null;
	private int minOccur = 0;
	private int maxOccur = 0;
	private String path = null;
	private Float size = null;
	private String sizeUnit = "B";
	private Float sizeTolerance = 0F;
	private String md5 = null;

	private Dir bestFit = null;
	private Map<String, Boolean> bestChecks = new HashMap<String, Boolean>();
	private Map<String, Boolean> actChecks = new HashMap<String, Boolean>();

	// ist es ein template? , dann wird in match alle passenden ids aus dem pruefling gesammelt.
	// ist es ein pruefling?, dann wird in match die id des passenden eintrages aus dem template eingetragen (in diesem fall kann es nur 1 stk sein)
	private ArrayList<File> match = new ArrayList<File>();

	public File()
	{
		Random generator = new Random();
		generator.setSeed(System.currentTimeMillis());
		id = generator.nextInt(100000000);

		minOccur = 0;
		maxOccur = 99999;
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
		// alle files im examineeDir durchgehen und ueberpruefen ob sie matchen
		for(File actFile : examineeDir.getFile())
		{
			// den pfad vom examinee gegen den template-pfad matchen
			if(actFile.getPath().matches("^"+this.getPath()+"$"))
			{
				// die groesse vergleichen
				if(actFile.doesSizeMatch(this))
				{
					// passen beide vergleichspartner? Dann soll dies in beiden vermerkt werden
					actFile.match.add(this);
					this.match.add(actFile);
				}
			}
		}
		
		// und fuer alle enthaltenen Dirs in examineeDir ebenfalls ausfuehren
		for(Dir actDir : examineeDir.getDir())
		{
			this.match(actDir);
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

		return result;
	}
	
	public String toString()
	{
		String entityString = "";
		
		entityString += "id="+this.getId()+", minOccur: "+this.getMinOccur()+", maxOccur: "+this.getMaxOccur()+", path: "+this.getPath()+", size: "+this.getSize()+", sizetolerance: "+this.getSizeTolerance()+", md5: "+this.getMd5();
		
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
	 * @return the bestFit
	 */
	public Dir getBestFit() {
		return bestFit;
	}

	/**
	 * @param bestFit the bestFit to set
	 */
	public void setBestFit(Dir bestFit) {
		this.bestFit = bestFit;
	}

	/**
	 * @return the bestChecks
	 */
	public Map<String, Boolean> getBestChecks() {
		return bestChecks;
	}

	/**
	 * @param bestChecks the bestChecks to set
	 */
	public void setBestChecks(Map<String, Boolean> bestChecks) {
		this.bestChecks = bestChecks;
	}

	/**
	 * @return the actChecks
	 */
	public Map<String, Boolean> getActChecks() {
		return actChecks;
	}

	/**
	 * @param actChecks the actChecks to set
	 */
	public void setActChecks(Map<String, Boolean> actChecks) {
		this.actChecks = actChecks;
	}

	/**
	 * @return the match
	 */
	public ArrayList<File> getMatch() {
		return match;
	}

	/**
	 * @param match the match to set
	 */
	public void setMatch(ArrayList<File> match) {
		this.match = match;
	}

}
