package de.caegroup.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.caegroup.process.Callitem;
import de.caegroup.process.Commit;
import de.caegroup.process.File;
import de.caegroup.process.List;
import de.caegroup.process.Log;
import de.caegroup.process.Step;
import de.caegroup.process.Work;

public class TestFile {

	de.caegroup.process.Process process = new de.caegroup.process.Process();

	@Before
	public void setUp()
	{
		Step step = new Step("root");
		step.setLoopvar("1");

		process.setBaseDir(System.getProperty("user.dir")+"/src/test/resources");
		// damit rootdir = basedir gesehen wird
		process.setSubprocess(true);
		process.addStep(step);
		
		// definieren einer liste mit einem eintrag
		List listSpl = new List();
		listSpl.setName("spl");
		listSpl.addItem("mama");
		listSpl.addItem("call.1.txt");
		step.addList(listSpl);
		
		// definieren eines Files mit einem glob in dem sich platzhalter befinden
		File file = new File();
		file.setKey("testschluessel");
		file.setGlob("{$spl[{$loopvarstep}]}");
		file.setGlobdir(System.getProperty("user.dir")+"/src/test/resources");

		Commit commit = new Commit(step);
		commit.addFile(file);
		
		commit.doIt();

//		for(Log actLog : file.getLog())
//		{
//			actLog.print();
//		}
	}

	@Test
	public void testResolveGlob()
	{
		assertEquals("call.1.txt", process.getStep("root").getFile().get(0).getFilename());
	}

}
