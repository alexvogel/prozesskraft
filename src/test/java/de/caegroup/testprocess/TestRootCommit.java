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

public class TestRootCommit {

	Process process = new Process();

	@Before
	public void setUp()
	{
		process.setInfilexml("src/test/resources/multiappshake.xml");
		process.setInitCommitVarfile("etc/name:etc/spl:etc/call:etc/result");

		Step rootStep = new Step("root");
		process.addStep(rootStep);

	}

	@Test
	public void testResolving()
	{
		Step rootStep = process.getRootStep();
		
		// zuerst existieren keine listen im rootStep
		assertEquals(0, rootStep.getList().size());
		
		// rootCommit durchfuehren
		process.getRootStep().rootCommit();
		
		// jetzt muessten 4 listen existieren
		assertEquals(4, rootStep.getList().size());
		
	}
}
