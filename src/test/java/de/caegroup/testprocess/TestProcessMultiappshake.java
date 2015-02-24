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
import de.caegroup.process.Subprocess;
import de.caegroup.process.Variable;
import de.caegroup.process.Work;
import de.caegroup.process.Callitem;

public class TestProcessMultiappshake {

	Process process = new Process();
	String pathToXml = "src/test/resources/definitions/Admin/multiappshake/0.0.1/process.xml";
	
	@Before
	public void setUp()
	{
		
		process.setInfilexml(pathToXml);

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
	public void testReadXml()
	{
		// testen der process-attribute, die durch das mapping gesetzt werden
		assertEquals(true, new java.io.File(pathToXml).isFile());
		assertEquals("multiappshake", process.getName());
		assertEquals("0.3", process.getModelVersion());
		assertEquals("alexander.vogel@caegroup.de", process.getArchitectMail());

		// testen des feldes, dass schon vorher gesetzt war
		assertEquals(pathToXml, process.getInfilexml());

		// testen der process-elemente
		assertEquals(3, process.getStep().size());

		// testen des subprocesses im step 'appshake'
		Subprocess subprocess = process.getStep("appshake").getSubprocess();
		
		assertEquals("Admin", subprocess.getDomain());
		assertEquals("appshake", subprocess.getName());
		assertEquals("0.0.1", subprocess.getVersion());
		assertEquals("root", subprocess.getStep().getName());
		
		assertEquals(3, subprocess.getStep().getCommit().size());
	}
	
	@Test
	public void testGenSubprocess()
	{
		Step step = process.getStep("appshake");
		
		List nameList = new List();
		nameList.setName("name");
		nameList.addItem("beulen");
		step.addList(nameList);
		
		List splList = new List();
		splList.setName("spl");
		splList.addItem("/irgendein/pfad/auf/das/spl/directory");
		step.addList(splList);
		
		List callList = new List();
		callList.setName("call");
		callList.addItem("/irgendein/pfad/auf/das/call/file");
		step.addList(callList);
		
		List resultList = new List();
		resultList.setName("result");
		resultList.addItem("/irgendein/pfad/auf/das/result/file");
		step.addList(resultList);
		
		step.setLoopvar("0");
		
		//
		Process childProcess = step.getSubprocess().genProcess("src/test/resources/definitions");
		assertEquals("appshake", childProcess.getName());
		
		// 4 listen aus multiappshake wurden in den childProcess 'appshake' uebertragen
		assertEquals(4, childProcess.getRootStep().getList().size());
		
		assertEquals("name", childProcess.getRootStep().getList("name").getName());
		assertEquals("spl", childProcess.getRootStep().getList("spl").getName());
		assertEquals("call", childProcess.getRootStep().getList("call").getName());
		assertEquals("result", childProcess.getRootStep().getList("result").getName());
		
		// 3 commits aus dem rootStep des Subprocesses + 1 automatisch erzeugter commit aus standardRootCommit fuer standardeintraege wie "_dir" etc.
		assertEquals(4, childProcess.getRootStep().getCommit().size());
		
		for(Log actLog : childProcess.getRootStep().getLog())
		{
			actLog.print();
		}
		System.out.println("-----");
		for(Commit actCommit : childProcess.getRootStep().getCommit())
		{
			for(Log actLog : actCommit.getLog())
			{
				actLog.print();
			}
			System.out.println("-----");
		}
	}
}
