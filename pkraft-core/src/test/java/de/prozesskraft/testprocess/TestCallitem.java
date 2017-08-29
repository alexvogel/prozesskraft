package de.prozesskraft.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.pkraft.Callitem;
import de.prozesskraft.pkraft.List;
import de.prozesskraft.pkraft.Log;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Work;

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
		
		for(Log actLog : step1.getLog())
		{
			actLog.print();
		}
		
		assertEquals("eiapupajaICHBINGROSSeijoas", resolvedString);
	}

	@Test
	public void testConstructCall()
	{
		de.prozesskraft.pkraft.Process process = new de.prozesskraft.pkraft.Process();
		process.setInfilexml("/irgendetwas");
		
		Step step1 = new Step(process);
		step1.setName("ich_bin_ein_step");
		
		Work work = new Work(step1);
		
		work.setCommand("animator");
		
		Callitem item1 = new Callitem(work);
		item1.setSequence(1);
		item1.setPar("--version");
		item1.setDel(" ");
		item1.setVal("6.7");
		
		Callitem item2 = new Callitem(work);
		item2.setSequence(2);
		item2.setPar("-FG");
		
		Callitem item3 = new Callitem(work);
		item3.setSequence(3);
		item3.setPar("-b");

		Callitem item4 = new Callitem(work);
		item4.setSequence(4);
		item4.setPar("-s");
		item4.setDel(" ");
		item4.setVal("genview.ses");

		
		assertEquals("animator --version 6.7 -FG -b -s genview.ses", work.getCall());
		
//		for(Log actLog : step1.getLogRecursive())
//		{
//			actLog.print();
//		}

	}
}
