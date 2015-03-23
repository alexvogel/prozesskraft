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
	
	@Before
	public void setUp()
	{

	}

	@Test
	public void testCommit()
	{
		
		// damit kein extra verzeichnis fuer prozess und step angelegt wird und das zu committende file reinkopiert wird
		process.setBaseDir("/tmp");
		// damit rootdirectory == basedir ist
//		process.setSubprocess(true);
		
		Step step = new Step();
		step.setName("root");
		process.addStep(step);
		
		Commit commit = new Commit();
		commit.setName("ergebnisse");
		step.addCommit(commit);
		
		File file = new File();
		file.setMinoccur(1);
		file.setKey("irgendEinFile");
		file.setGlob("call.1.txt");
		file.setGlobdir("src/test/resources");
		commit.addFile(file);

		// die stati abfragen
		assertEquals("unknown", commit.getStatus());
		assertEquals("unknown", step.getStatus());
		assertEquals(0, step.getFile().size());
		
		
		// commit durchfuehren
		commit.doIt();
//		commit.printLog();
		
		// die stati abfragen
		assertEquals("finished", commit.getStatus());
		assertEquals("finished", step.getStatus());
		assertEquals(1, step.getFile().size());
		assertEquals("irgendEinFile", step.getFile().get(0).getKey());
	}
	
	@Test
	public void testCommit2()
	{
		
		// damit kein extra verzeichnis fuer prozess und step angelegt wird und das zu committende file reinkopiert wird
		process.setBaseDir("/tmp");
		// damit rootdirectory == basedir ist
//		process.setSubprocess(true);
		
		Step step = new Step();
		step.setName("root");
		step.setLoopvar("0");
		process.addStep(step);

		Commit commit = new Commit(step);
		commit.setName("ergebnisse");
		commit.setToroot("pp");
		
		List listFile = new List();
		listFile.setName("files");
		listFile.addItem("nocheinFile");
		step.addList(listFile);

		List listVar = new List();
		listVar.setName("variablesTuTu");
		listVar.addItem("ergebnis");
		step.addList(listVar);

		Variable variable = new Variable();
		variable.setMinoccur(1);
		variable.setKey("{$variablesTuTu[{$loopvarstep}]}");
		variable.setValue("superB");
		commit.addVariable(variable);

		Variable variable2 = new Variable();
		variable2.setMinoccur(1);
		variable2.setKey("{root:$variablesTuTu[{$loopvarstep}]}");
		variable2.setValue("superB");
		commit.addVariable(variable2);

		File file = new File();
		file.setMinoccur(1);
		file.setKey("{$files[{$loopvarstep}]}");
		file.setGlob("call.1.txt");
		file.setGlobdir("src/test/resources");
		commit.addFile(file);

		// die stati abfragen
		assertEquals("unknown", commit.getStatus());
		assertEquals("unknown", step.getStatus());
		assertEquals(0, step.getFile().size());

		// commit durchfuehren
		commit.doIt();
		System.err.println("testCommit2");
		commit.printLog();
		
		// die stati abfragen
		assertEquals("finished", commit.getStatus());
		assertEquals("finished", step.getStatus());

		// was an file/variable im step angekommen ist
		assertEquals(1, step.getFile().size());
		assertEquals(2, step.getVariable().size());
		assertEquals("nocheinFile", step.getFile().get(0).getKey());

		// beide variablen sollen den gleichen key.
		assertEquals(step.getVariable().get(0).getKey(), step.getVariable().get(1).getKey());
		System.err.println("variable 1: "+step.getVariable().get(0).getKey());
		System.err.println("variable 2; "+step.getVariable().get(1).getKey());
		
	}
	
	@Test
	public void testCommit3()
	{
		
		// damit kein extra verzeichnis fuer prozess und step angelegt wird und das zu committende file reinkopiert wird
		process.setBaseDir("/tmp");
		
		Step stepRoot = new Step();
		stepRoot.setName("root");
		stepRoot.setLoopvar("0");
		process.addStep(stepRoot);

		Step stepAlpha = new Step();
		stepAlpha.setName("alpha");
		stepAlpha.setLoopvar("0");
		process.addStep(stepAlpha);

		Commit commit = new Commit(stepAlpha);
		commit.setName("ergebnisse");
		commit.setToroot("pp");
		
		File file = new File();
		file.setMinoccur(1);
		file.setKey("irgendEinFile");
		file.setGlob("call.1.txt");
		file.setGlobdir("src/test/resources");
		commit.addFile(file);

		// die stati abfragen
		assertEquals("unknown", commit.getStatus());
		assertEquals("unknown", stepAlpha.getStatus());
		assertEquals(0, stepAlpha.getFile().size());

		// commit durchfuehren
		commit.doIt();
		System.err.println("testCommit3");
		commit.printLog();
		
		// die stati abfragen
		assertEquals("finished", commit.getStatus());
		assertEquals("finished", stepAlpha.getStatus());

		// was an file/variable im step angekommen ist
		assertEquals(1, stepAlpha.getFile().size());
		assertEquals("irgendEinFile", stepAlpha.getFile().get(0).getKey());
		
		// was an file/variable im step Root angekommen ist
		assertEquals(1, stepRoot.getFile().size());
		assertEquals("irgendEinFile", stepRoot.getFile().get(0).getKey());

		assertTrue(stepRoot.getFile().get(0).getAbsfilename().matches(".+/processOutput/pp/call.1.txt"));
		assertTrue(stepAlpha.getFile().get(0).getAbsfilename().matches(".+/dir4step_alpha/call.1.txt"));
		
	}

}
