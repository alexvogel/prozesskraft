package de.prozesskraft.testprocess;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.pkraft.Callitem;
import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.File;
import de.prozesskraft.pkraft.List;
import de.prozesskraft.pkraft.Log;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Work;

public class TestFile {

	de.prozesskraft.pkraft.Process process = new de.prozesskraft.pkraft.Process();

	@Before
	public void setUp()
	{
		Step step = new Step();
		step.setName("root");
		step.setLoopvar("1");

	//	process.setBaseDir(System.getProperty("user.dir")+"/src/test/resources");
		process.setBaseDir("/tmp/testBase");
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
