package de.prozesskraft.testptest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.ptest.Dir;
import de.prozesskraft.ptest.File;

public class TestResult {

	@Before
	public void setUp()
	{
		
	}

	/**
	 * 
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws JAXBException
	 */
	@Test
	public void testCompare01() throws NullPointerException, IOException, JAXBException
	{
		Dir testdir01 = new Dir();
		testdir01.setBasepath("src/test/resources/testdir01");
		testdir01.genFingerprint(0f, false, new ArrayList<String>());
		testdir01.setRespectMd5Recursive(false);
		
		Dir fingerprint01 = new Dir();
		fingerprint01.setInfilexml("src/test/resources/testdir01.fpr");
		fingerprint01.readXml();
		fingerprint01.setRespectMd5Recursive(false);

		testdir01.runCheck(fingerprint01);
		System.out.println(testdir01.sprintSummaryAsCsv("all"));
		System.err.println("--- logging of the reference ---");
		System.err.println(testdir01.getLogAsStringRecursive());
		System.err.println("--- logging of the examinee ---");
		System.err.println(fingerprint01.getLogAsStringRecursive());
		assertEquals(true, fingerprint01.isMatchSuccessfullRecursive() && testdir01.isMatchSuccessfullRecursive());

		fingerprint01.runCheck(testdir01);
		System.out.println(fingerprint01.sprintSummaryAsCsv("all"));
		System.err.println(fingerprint01.getLogAsStringRecursive());
		assertEquals(true, testdir01.isMatchSuccessfullRecursive() && fingerprint01.isMatchSuccessfullRecursive());
		
	}

	@Test
	public void testCompare02() throws NullPointerException, IOException, JAXBException
	{
		Dir testdir01 = new Dir();
		testdir01.setBasepath("src/test/resources/testdir01");
		testdir01.genFingerprint(0f, false, new ArrayList<String>());

		Dir fingerprint02 = new Dir();
		fingerprint02.setInfilexml("src/test/resources/testdir02.fpr");
		fingerprint02.readXml();

		
		testdir01.runCheck(fingerprint02);
		System.out.println(fingerprint02.sprintSummaryAsCsv("error"));
		System.out.println(testdir01.sprintSummaryAsCsv("error"));
		
		assertEquals(false, fingerprint02.isMatchSuccessfullRecursive() && testdir01.isMatchSuccessfullRecursive());

//		fingerprint02.runCheck(testdir01);
//		assertEquals(false, testdir01.isMatchSuccessfull());
	}


}
