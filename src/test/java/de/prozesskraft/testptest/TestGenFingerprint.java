package de.prozesskraft.testptest;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.ptest.Dir;
import de.prozesskraft.ptest.File;

public class TestGenFingerprint {

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
	public void testGenFingerprint01() throws NullPointerException, IOException, JAXBException
	{
		Dir testdir01 = new Dir();
		testdir01.setBasepath("src/test/resources/testdir01");
		testdir01.genFingerprint(0f, false, new ArrayList<String>());

		testdir01.setOutfilexml("src/test/resources/testdir01.fpr");
		testdir01.writeXml();
	}

	@Test
	public void testGenFingerprint02() throws NullPointerException, IOException, JAXBException
	{
		Dir testdir02 = new Dir();
		testdir02.setBasepath("src/test/resources/testdir02");
		testdir02.genFingerprint(0f, false, new ArrayList<String>());

		testdir02.setOutfilexml("src/test/resources/testdir02.fpr");
		testdir02.writeXml();
	}
}
