package de.prozesskraft.testptest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.ptest.Dir;
import de.prozesskraft.ptest.File;
import de.prozesskraft.ptest.Spl;
import de.prozesskraft.ptest.Splset;

public class TestSpl {

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
	public void testSplset01() throws IOException
	{
		Splset mySplSet = new Splset("src/test/resources/spl/1");
		
		assertEquals(2, mySplSet.getSpl().size());
	}
	
	@Test
	public void testSplset02() throws IOException
	{
		Splset mySplSet = new Splset("src/test/resources/spl/1");
		
		Spl mySpl = mySplSet.getSpl("irgendeiner");
		
		assertEquals("/data/prog/workspaces/workspace_privat/ptest-core/src/main/java/de/prozesskraft/ptest/Spl.java --inc file1 --inc file2 --inc file3 --scheiss true", mySpl.getCallAsString());
	}
}
