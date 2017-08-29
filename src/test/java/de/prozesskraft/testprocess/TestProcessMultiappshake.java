//package de.prozesskraft.testprocess;
//
//import static org.junit.Assert.*;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//
//import javax.xml.bind.JAXBException;
//
//import org.junit.Test;
//import org.junit.Before;
//
//import de.prozesskraft.pkraft.Callitem;
//import de.prozesskraft.pkraft.Commit;
//import de.prozesskraft.pkraft.Init;
//import de.prozesskraft.pkraft.List;
//import de.prozesskraft.pkraft.Log;
//import de.prozesskraft.pkraft.Param;
//import de.prozesskraft.pkraft.Process;
//import de.prozesskraft.pkraft.Step;
//import de.prozesskraft.pkraft.Subprocess;
//import de.prozesskraft.pkraft.Variable;
//import de.prozesskraft.pkraft.Work;
//
//public class TestProcessMultiappshake {
//
//	Process process = new Process();
//	String pathToXml = "src/test/resources/definitions/Admin/multiappshake/0.0.1/process.xml";
//	
//	@Before
//	public void setUp()
//	{
//		
//		process.setInfilexml(pathToXml);
//
//		try
//		{
//			process.readXml();
//		} catch (JAXBException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	@Test
//	public void testReadXml()
//	{		
//		// testen der process-attribute, die durch das mapping gesetzt werden
//		assertEquals(true, new java.io.File(pathToXml).isFile());
//		assertEquals("multiappshake", process.getName());
//		assertEquals("0.3", process.getModelVersion());
//		assertEquals("alexander.vogel@caegroup.de", process.getArchitectMail());
//
//		// testen des feldes, dass schon vorher gesetzt war
//		assertEquals(pathToXml, process.getInfilexml());
//
//		// testen der process-elemente
//		assertEquals(3, process.getStep().size());
//
//		// testen des subprocesses im step 'appshake'
//		Subprocess subprocess = process.getStep("appshake").getSubprocess();
//		
//		assertEquals("Admin", subprocess.getDomain());
//		assertEquals("appshake", subprocess.getName());
//		assertEquals("0.0.1", subprocess.getVersion());
//		assertEquals("root", subprocess.getStep().getName());
//		
//		assertEquals(3, subprocess.getStep().getCommit().size());
//	}
//	
//	@Test
//	public void testGenSubprocess()
//	{
//		Step step = process.getStep("appshake");
//		
//		List nameList = new List();
//		nameList.setName("name");
//		nameList.addItem("beulen");
//		step.addList(nameList);
//		
//		List splList = new List();
//		splList.setName("spl");
//		splList.addItem("/irgendein/pfad/auf/das/spl/directory");
//		step.addList(splList);
//		
//		List callList = new List();
//		callList.setName("call");
//		callList.addItem("/data/prog/workspaces/workspace_privat/process-core/src/test/resources/call.1.txt");
//		step.addList(callList);
//		
//		List resultList = new List();
//		resultList.setName("result");
//		resultList.addItem("/data/prog/workspaces/workspace_privat/process-core/src/test/resources/result.1.fpr");
//		step.addList(resultList);
//		
//		step.setLoopvar("0");
//		
//		//
//		Process childProcess = step.getSubprocess().genProcess("src/test/resources/definitions");
//		childProcess.setBaseDir("/tmp");
//		childProcess.getRootStep().commit();
//
////		for(Log actLog : childProcess.getRootStep().getLogRecursive())
////		{
////			actLog.print();
////		}
//	
//		
////System.exit(0);
////		for(Log actLog : step.getSubprocess().getLog())
////		{
////			actLog.print();
////		}
//		
//		
//		
//		assertEquals("appshake", childProcess.getName());
//		
//		// 4 listen aus multiappshake wurden in den childProcess 'appshake' uebertragen
//		assertEquals(4, childProcess.getRootStep().getList().size());
//		
//		assertEquals("name", childProcess.getRootStep().getList("name").getName());
//		assertEquals("spl", childProcess.getRootStep().getList("spl").getName());
//		assertEquals("call", childProcess.getRootStep().getList("call").getName());
//		assertEquals("result", childProcess.getRootStep().getList("result").getName());
//		
//		// 3 commits aus dem rootStep des Subprocesses + 1 automatisch erzeugter commit aus standardRootCommit fuer standardeintraege wie "_dir" etc.
//		assertEquals(4, childProcess.getRootStep().getCommit().size());
////		System.err.println("stepName: "+childProcess.getRootStep().getName());
////		for(Commit actCommit : childProcess.getRootStep().getCommit())
////		{
////			System.err.println("commitName: "+actCommit.getName());
////		}
//
//		// ueberpruefen ob die 3 commits, die aus dem subprocess von multiappshake kamen auch zu entsprechenden variablen und files im prozess appshake gefuehrt haben
//		// in summe 2 files (call+result)
//		assertEquals(2, childProcess.getRootStep().getFile().size());
//		// in summe 2 variable (spl, _dir, processName, _processVarsion, _processDescription)
//		assertEquals(5, childProcess.getRootStep().getVariable().size());
////		for(Variable actVariable : childProcess.getRootStep().getVariable())
////		{
////			System.err.println("variableName: "+actVariable.getKey());
////		}
//
//		// 1 variable mit key=spl
//		assertEquals(1, childProcess.getRootStep().getVariable("spl").size());
//		assertEquals("/irgendein/pfad/auf/das/spl/directory", childProcess.getRootStep().getVariable("spl").get(0).getValue());
//		// 1 file mit key=call
//		assertEquals(1, childProcess.getRootStep().getFile("call").size());
//		assertEquals(childProcess.getRootStep().getAbsdir()+"/processInput/"+childProcess.getRootStep().getFile("call").get(0).getFilename(), childProcess.getRootStep().getFile("call").get(0).getAbsfilename());
//		// 1 file mit key=result
//		assertEquals(1, childProcess.getRootStep().getFile("result").size());
//		assertEquals(childProcess.getRootStep().getAbsdir()+"/processInput/"+childProcess.getRootStep().getFile("result").get(0).getFilename(), childProcess.getRootStep().getFile("result").get(0).getAbsfilename());
//		
////		for(Log actLog : childProcess.getRootStep().getLog())
////		{
////			actLog.print();
////		}
////		System.out.println("-----");
////		for(Commit actCommit : childProcess.getRootStep().getCommit())
////		{
////			for(Log actLog : actCommit.getLog())
////			{
////				actLog.print();
////			}
////			System.out.println("-----");
////		}
//	}
//}
