package de.caegroup.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Callitem;
import de.caegroup.process.File;
import de.caegroup.process.List;
import de.caegroup.process.Step;
import de.caegroup.process.Work;

public class TestFile {

	de.caegroup.process.Process process = new de.caegroup.process.Process();

	@Before
	public void setUp()
	{
		Step step = new Step("hello");
		step.setLoopvar("1");
		process.addStep(step);
		
		// definieren einer liste mit einem eintrag
		List listSpl = new List();
		listSpl.setName("spl");
		listSpl.addItem("mama");
		listSpl.addItem("papa");
		step.addList(listSpl);
		
		// definieren eines Files mit einem glob in dem sich platzhalter befinden
		File file = new File();
		file.setKey("testschluessel");
		file.setGlob("{$spl[{$loopvarstep}]}");
		step.addFile(file);
	}

	@Test
	public void testResolveGlob()
	{
		String resolvedGlobString = process.getStep("hello").getFile().get(0).getGlob();
		
		System.err.println("resolvedGlob: "+resolvedGlobString);
		assertEquals("papa", resolvedGlobString);
	}

}
