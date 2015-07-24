package de.prozesskraft.testprocess;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.pkraft.Init;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pkraft.Step;

public class TestRank {

	Step aStep = new Step();
	Step bStep = new Step();
	Step cStep = new Step();
	Step dStep = new Step();
	Process process = new Process();

	@Before
	public void setUp()
	{
		Init init1 = new Init();
		init1.setFromstep("root");

		aStep.setName("aStep");
		aStep.addInit(init1);
		
		
		Init init2 = new Init();
		init2.setFromstep("aStep");

		bStep.setName("bStep");
		bStep.addInit(init2);
		
		
		Init init3 = new Init();
		init3.setFromstep("bStep");

		cStep.setName("cStep");
		cStep.addInit(init3);
		
		process = new Process();
		process.addStep(aStep);
		process.addStep(bStep);
		process.addStep(cStep);
	}
	
	@Test
	public void testGetRank()
	{
		process.setStepRanks();
		
		assertEquals(1, aStep.getLevel());
		assertEquals(2, bStep.getLevel());
		assertEquals(3, cStep.getLevel());

		assertEquals("1.1", aStep.getRank());
		assertEquals("2.1", bStep.getRank());
		assertEquals("3.1", cStep.getRank());
		
		assertEquals(3, process.getStep().size());
	}
	

}
