package de.caegroup.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Callitem;
import de.caegroup.process.List;
import de.caegroup.process.Step;
import de.caegroup.process.Work;

public class TestCallitem {

	@Before
	public void setUp()
	{
		
	}
	
	@Test
	public void testResolvePar()
	{
		Step step1 = new Step();
		step1.setName("ich_bin_ein_namen");
		
		List list1 = new List();
		list1.setName("HallO");
		list1.addItem("ICHBINGROSS");
		
		step1.addList(list1);
		
		Work work1 = new Work();
		
		Callitem callitem1 = new Callitem();
		work1.addCallitem(callitem1);
		step1.setWork(work1);
		
		
		String resolvedString = callitem1.resolveString("eiapupaja{$HallO}eijoas");
		
		assertEquals("eiapupajaICHBINGROSSeijoas", resolvedString);
	}

}
