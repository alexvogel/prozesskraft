package de.caegroup.testprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Commit;
import de.caegroup.process.Param;
import de.caegroup.process.Process;
import de.caegroup.process.Step;
import de.caegroup.process.List;
import de.caegroup.process.Init;
import de.caegroup.process.Variable;
import de.caegroup.process.Work;
import de.caegroup.process.Callitem;

public class TestParentalStatus {

	Process process = new Process();

	@Before
	public void setUp()
	{
		process.setInfilexml("src/test/resources/beulen.xml");
		try
		{
			process.readXml();
		} catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testParentStatusOfStep()
	{
		for(Step step : process.getStep())
		{
			assertNotNull(step.getParent());
			assertSame(step.getParent(), process);
		}
	}
	
	@Test
	public void testParentStatusOfInit()
	{
		for(Step actualStep : process.getStep())
		{
			for(Init actualInit : actualStep.getInit())
			{
				assertNotNull(actualInit.getParent());
				assertSame(actualInit.getParent(), actualStep);
			}
		}
	}
	
	@Test
	public void testParentStatusOfWork()
	{
		for(Step actualStep : process.getStep())
		{
			if(actualStep.getWork() != null)
			{
				Work actualWork = actualStep.getWork();
				assertNotNull(actualWork.getParent());
				assertSame(actualWork.getParent(), actualStep);
			}
		}
	}
	
	@Test
	public void testParentStatusOfCommit()
	{
		for(Step actualStep : process.getStep())
		{
			for(Commit actualCommit : actualStep.getCommit())
			{
				assertNotNull(actualCommit.getParent());
				assertSame(actualCommit.getParent(), actualStep);
			}
		}
	}
	


}
