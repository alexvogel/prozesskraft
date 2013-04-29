package de.caegroup.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Init;
import de.caegroup.process.Process;
import de.caegroup.process.Step;

public class TestStep {

	Step step = new Step();

	@Before
	public void setUp()
	{
		Init init1 = new Init();
		init1.setFromstep("root");
		
		Init init2 = new Init();
		init2.setFromstep("somestep");

		Init init3 = new Init();
		init3.setFromstep("anotherstep@1");

		Init init4 = new Init();
		init4.setFromstep("anotherstep@2");
		
		step.setName("myStep");
		step.addInit(init1);
		step.addInit(init2);
		step.addInit(init3);
		step.addInit(init4);
		
		Process process = new Process();
		process.addStep(step);
		process.addStep("somestep");
		process.addStep("anotherstep@1");
		process.addStep("anotherstep@2");
	}
	
	@Test
	public void testGetFromsteps()
	{
		assertEquals(4, step.getFromsteps().size());
	}
	
	@Test
	public void testGetFromsteps2()
	{
		Init init = new Init();
		init.setFromstep("root");
		step.addInit(init);
		
		assertEquals(4, step.getFromsteps().size());
	}

}
