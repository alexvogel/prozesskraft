package de.prozesskraft.testptest;

import static org.junit.Assert.*;

import java.io.IOException;

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
		testdir01.genFingerprint();

		Dir fingerprint01 = new Dir();
		fingerprint01.setInfilexml("src/test/resources/testdir01.xml");
		fingerprint01.readXml();

		testdir01.runCheck(fingerprint01);
		assertEquals(true, fingerprint01.isMatchSuccessfull());
		assertEquals(true, testdir01.isMatchSuccessfull());

		fingerprint01.runCheck(testdir01);
		assertEquals(true, testdir01.isMatchSuccessfull());
		assertEquals(true, fingerprint01.isMatchSuccessfull());
	}

	@Test
	public void testCompare02() throws NullPointerException, IOException, JAXBException
	{
		Dir testdir01 = new Dir();
		testdir01.setBasepath("src/test/resources/testdir01");
		testdir01.genFingerprint();

		Dir fingerprint02 = new Dir();
		fingerprint02.setInfilexml("src/test/resources/testdir02.xml");
		fingerprint02.readXml();

		testdir01.runCheck(fingerprint02);
		assertEquals(false, fingerprint02.isMatchSuccessfull());

		fingerprint02.runCheck(testdir01);
		assertEquals(false, testdir01.isMatchSuccessfull());
	}


}
