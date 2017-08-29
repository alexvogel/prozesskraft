package de.prozesskraft.testptest;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

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
	 */
	@Test
	public void testCreateSpl()
	{
		Splset splset = new Splset("src/test/resources/spl/1");
		assertEquals("1", splset.getSplDir().getName());
//		System.err.println(splset.getSplDir().getAbsolutePath());
		
//		for(Spl actSpl : splset.getSpl())
//		{
//			System.err.println("----- spl -----");
//			System.err.println(actSpl.getSplDir().getAbsolutePath());
//			for(java.io.File actFile : actSpl.getInput())
//			{
//				System.err.println("input: " + actFile.getAbsolutePath());
//			}
//			
//			java.io.File target = new java.io.File("src/test/resources/spl/1_tmpCopy");
//			target.mkdir();
//			
//			actSpl.exportInput(target);
//			
//		}
		
		
		
//		fingerprint02.runCheck(testdir01);
	}


}
