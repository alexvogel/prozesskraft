package de.caegroup.testprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Commit;
import de.caegroup.process.Log;
import de.caegroup.process.Param;
import de.caegroup.process.Process;
import de.caegroup.process.Step;
import de.caegroup.process.List;
import de.caegroup.process.Init;
import de.caegroup.process.Variable;
import de.caegroup.process.Work;
import de.caegroup.process.Callitem;

public class TestRootCommit {

	Process process = new Process();

	@Before
	public void setUp()
	{

	}

	@Test
	public void testRootCommit()
	{
		process.setInfilexml("src/test/resources/definitions/Admin/multiappshake/0.0.1/process.xml");

		Step rootStep = new Step("root");
		process.addStep(rootStep);
		process.setInitCommitVariable("etc/variable.name:etc/variable.spl:etc/variable.call:etc/variable.result");

		// zuerst existieren keine listen im rootStep
		assertEquals(0, rootStep.getList().size());

		// rootCommit durchfuehren
		rootStep.commit();

		for(Log actLog : rootStep.getLogRecursive())
		{
			System.err.println(actLog.sprint());
		}

		// jetzt muss 1 commit existieren (der automatisch angelegte 'rootCommit')
		assertEquals(1, rootStep.getCommit().size());

		for(Variable actVariable : rootStep.getVariable())
		{
			System.err.println(actVariable.getKey() + "=" + actVariable.getValue());
		}
		
		// jetzt muessten 13 variablen existieren (3*name, 3*spl, 3*call, 3*result, _dir)
		assertEquals(13, rootStep.getVariable().size());

	}
	
	@Test
	public void testRootCommit2()
	{
		process.setInfilexml("src/test/resources/definitions/Admin/multiappshake/0.0.1/process.xml");

		Step rootStep = new Step("root");
		process.addStep(rootStep);

		process.setInitCommitVariable("etc");

		System.err.println("Infiledirectory: "+process.getInfilexml());
		
		// zuerst existieren keine listen im rootStep
		assertEquals(0, rootStep.getList().size());

		// rootCommit durchfuehren
		rootStep.commit();

		for(Log actLog : rootStep.getLogRecursive())
		{
			System.err.println(actLog.sprint());
		}

		// jetzt muss 1 commit existieren (der automatisch angelegte 'rootCommit')
		assertEquals(1, rootStep.getCommit().size());

		for(Variable actVariable : rootStep.getVariable())
		{
			System.err.println(actVariable.getKey() + "=" + actVariable.getValue());
		}
		
		// jetzt muessten 13 variablen existieren (3*name, 3*spl, 3*call, 3*result, _dir, 2*variables)
		assertEquals(16, rootStep.getVariable().size());

	}

}
