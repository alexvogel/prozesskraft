package de.prozesskraft.testprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.pkraft.Callitem;
import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.Init;
import de.prozesskraft.pkraft.List;
import de.prozesskraft.pkraft.Param;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.pkraft.Work;

public class TestParentalStatus {

	Process process = new Process();

	@Before
	public void setUp()
	{
		process.setInfilexml("src/test/resources/definitions/Exterior/beulen/0.1.0/process.xml");
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
