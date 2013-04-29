package de.caegroup.testprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Init;
import de.caegroup.process.Process;
import de.caegroup.process.Step;

public class TestProcess {

	Process process = new Process();

	@Before
	public void setUp()
	{
	}
	
	@Test
	public void testGetInitcommitdirs()
	{
		process.setInitcommitdir("/home/qxb0117/etc");
		ArrayList<String> initcommitdirs = process.getInitcommitdirs();
		assertEquals(1, initcommitdirs.size());
	}
	
	@Test
	public void testGetInitcommitdirs2()
	{
		process.setInitcommitdir("/home/qxb0117/etc:/home/irgendwas");
		ArrayList<String> initcommitdirs = process.getInitcommitdirs();
		assertEquals(2, initcommitdirs.size());
	}

	@Test
	public void testGetInitcommitdirs3()
	{
		process.setInitcommitdir("/home/qxb0117/etc:/home/irgendwas::");
		ArrayList<String> initcommitdirs = process.getInitcommitdirs();
		assertEquals(2, initcommitdirs.size());
	}

	@Test
	public void testGetInitcommitvarfiles()
	{
		process.setInitcommitvarfile("/home/qxb0117/etc/bla");
		ArrayList<String> initcommitvarfiles = process.getInitcommitvarfiles();
		assertEquals(1, initcommitvarfiles.size());
	}
	
	@Test
	public void testGetInitcommitvarfiles2()
	{
		process.setInitcommitvarfile("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu");
		ArrayList<String> initcommitvarfiles = process.getInitcommitvarfiles();
		assertEquals(2, initcommitvarfiles.size());
		Iterator<String> iterstring = initcommitvarfiles.iterator();
		while (iterstring.hasNext())
		{
			System.out.println("BLUB: "+iterstring.next());
		}
	}

	@Test
	public void testGetInitcommitvarfiless3()
	{
		process.setInitcommitvarfile("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu::");
		ArrayList<String> initcommitvarfiles = process.getInitcommitvarfiles();
		assertEquals(2, initcommitvarfiles.size());
		Iterator<String> iterstring = initcommitvarfiles.iterator();
		while (iterstring.hasNext())
		{
			System.out.println(iterstring.next());
		}
	}

	@Test
	public void testGetInitcommitvarfiless4()
	{
		process.setInitcommitvarfile("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu::");
		ArrayList<java.io.File> initcommitvarfiles = process.getInitcommitvarfiles2();
		assertEquals(2, initcommitvarfiles.size());
		Iterator<java.io.File> iterfile = initcommitvarfiles.iterator();
		while (iterfile.hasNext())
		{
			System.out.println(iterfile.next().getAbsolutePath());
		}
	}


}
