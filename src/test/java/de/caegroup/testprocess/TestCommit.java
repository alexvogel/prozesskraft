package de.caegroup.testprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Commit;
import de.caegroup.process.File;
import de.caegroup.process.Log;
import de.caegroup.process.Param;
import de.caegroup.process.Process;
import de.caegroup.process.Step;
import de.caegroup.process.List;
import de.caegroup.process.Init;
import de.caegroup.process.Variable;
import de.caegroup.process.Work;
import de.caegroup.process.Callitem;

public class TestCommit {

	Process process = new Process();
	Step step = null;
	Commit commit = null;
	File file = null;
	
	@Before
	public void setUp()
	{
		step = new Step();
		step.setName("mystep");
		process.addStep(step);
		
		commit = new Commit();
		commit.setName("ergebnisse");
		step.addCommit(commit);
		
		file = new File();
		file.setMinoccur(1);
		file.setKey("irgendEinFile");
		file.setGlob("../../src/test/resources/call.1.txt");
		commit.addFile(file);

	}

	@Test
	public void testCommit()
	{
		
		// die stati abfragen
		assertEquals("unknown", commit.getStatus());
		assertEquals("unknown", step.getStatus());
		assertEquals(0, step.getFile().size());
		
		
		// commit durchfuehren
		commit.doIt();
		commit.printLog();
		
		// die stati abfragen
		assertEquals("finished", commit.getStatus());
		assertEquals("finished", step.getStatus());
		assertEquals(1, step.getFile().size());
		assertEquals("irgendEinFile", step.getFile().get(0).getKey());
	}
}
