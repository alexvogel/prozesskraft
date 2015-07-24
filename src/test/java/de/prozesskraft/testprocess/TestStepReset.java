package de.prozesskraft.testprocess;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.pkraft.Init;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pkraft.Step;

public class TestStepReset {

	Process process = new Process();

	@Before
	public void setUp()
	{
		Init init1 = new Init();
		init1.setFromstep("root");

		Init init2 = new Init();
		init2.setFromstep("somestep");

		Init init3 = new Init();
		init3.setFromstep("anotherstep");

		Step step = new Step();
		step.setName("myStep");
		step.addInit(init1);
		step.addInit(init2);
		step.addInit(init3);
		
		process.addStep(step);
		process.addStep("root");
		process.addStep("somestep");
		process.addStep("anotherstep@1");
		process.addStep("anotherstep@2");
		process.addStep("tuvalu");
		
		
	}
	
	@Test
	public void testGetStepDependent()
	{
		assertEquals(1, process.getStepDependent("somestep").size());
		assertEquals(1, process.getStepDependent("anotherstep@1").size());
		assertEquals("myStep", process.getStepDependent("anotherstep@1").get(0).getName());
	}
	
}
