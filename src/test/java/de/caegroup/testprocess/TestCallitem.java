package de.caegroup.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Callitem;

public class TestCallitem {

	@Before
	public void setUp()
	{
		
	}
	
	@Test
	public void testResolvePar()
	{
		Callitem callitem1 = new Callitem();
		callitem1.setPar("{$irgendwas}");
		fail("Not yet implemented");
	}

}
