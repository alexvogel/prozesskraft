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

public class TestProcess {

	Process process = new Process();

	@Before
	public void setUp()
	{
		
	}
	
	@Test
	public void testGetInitcommitdirs()
	{
		process.setInitCommitDir("/home/qxb0117/etc");
		ArrayList<String> initcommitdirs = process.getInitcommitdirs();
		assertEquals(1, initcommitdirs.size());
	}
	
	@Test
	public void testGetInitcommitdirs2()
	{
		process.setInitCommitDir("/home/qxb0117/etc:/home/irgendwas");
		ArrayList<String> initcommitdirs = process.getInitcommitdirs();
		assertEquals(2, initcommitdirs.size());
	}

	@Test
	public void testGetInitcommitdirs3()
	{
		process.setInitCommitDir("/home/qxb0117/etc:/home/irgendwas::");
		ArrayList<String> initcommitdirs = process.getInitcommitdirs();
		assertEquals(2, initcommitdirs.size());
	}

	@Test
	public void testGetInitcommitvarfiles()
	{
		process.setInitCommitVarfile("/home/qxb0117/etc/bla");
		ArrayList<String> initcommitvarfiles = process.getInitcommitvarfiles();
		assertEquals(1, initcommitvarfiles.size());
	}
	
	@Test
	public void testGetInitcommitvarfiles2()
	{
		process.setInitCommitVarfile("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu");
		ArrayList<String> initcommitvarfiles = process.getInitcommitvarfiles();
		assertEquals(2, initcommitvarfiles.size());
		Iterator<String> iterstring = initcommitvarfiles.iterator();
//		while (iterstring.hasNext())
//		{
//			System.out.println("BLUB: "+iterstring.next());
//		}
	}

	@Test
	public void testGetInitcommitvarfiless3()
	{
		process.setInitCommitVarfile("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu::");
		ArrayList<String> initcommitvarfiles = process.getInitcommitvarfiles();
		assertEquals(2, initcommitvarfiles.size());
		Iterator<String> iterstring = initcommitvarfiles.iterator();
//		while (iterstring.hasNext())
//		{
//			System.out.println(iterstring.next());
//		}
	}

	@Test
	public void testGetInitcommitvarfiless4()
	{
		process.setInitCommitVarfile("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu::");
		ArrayList<java.io.File> initcommitvarfiles = process.getInitcommitvarfiles2();
		assertEquals(2, initcommitvarfiles.size());
		Iterator<java.io.File> iterfile = initcommitvarfiles.iterator();
//		while (iterfile.hasNext())
//		{
//			System.out.println(iterfile.next().getAbsolutePath());
//		}
	}

	@Test
	public void testReadXml()
	{
		String pathToXml = "src/test/resources/beulen.xml";
		process.setInfilexml(pathToXml);
		java.io.File file = new java.io.File(pathToXml);

		try
		{
			process.readXml();
		} catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// testen der process-attribute, die durch das mapping gesetzt werden
		assertEquals(true, file.isFile());
		assertEquals("beulen", process.getName());
		assertEquals("0.2", process.getModelVersion());
		assertEquals("Ermittelt die Beulsteifigkeit einer Struktur an einer bestimmten Stelle. Die Position wird mittels einer Knoten-ID definiert.", process.getDescription());
		assertEquals("alexander.vogel@caegroup.de", process.getArchitectMail());

		// testen des feldes, dass schon vorher gesetzt war
		assertEquals(pathToXml, process.getInfilexml());
		
		// testen der process-elemente
		assertEquals(2, process.getStep().size());

		//-----Step 'root' testen Anfang-----

		Step stepRoot = process.getStep("root");
		// testen der step-attribute
		assertEquals("root", stepRoot.getName());
		assertEquals(2, stepRoot.getCommit().size());

		Commit commit1 = stepRoot.getCommit("parameter");
		// testen der commit-elemente des steps root
		assertEquals(true, commit1.getToroot());

		Variable variable1 = commit1.getVariable().get(0);
		// testen der elemente des commits
		assertEquals("matdb", variable1.getKey());
		assertEquals("no", variable1.getValue());
		assertEquals(1, variable1.getMinoccur());
		assertEquals(1, variable1.getMaxoccur());
		assertEquals(false, variable1.getFree());

		ArrayList<String> choice = variable1.getChoice();
		// testen der elemente der variable
		assertEquals(3, choice.size());
		assertEquals("nlin", choice.get(2));

		de.caegroup.process.Test test0 = variable1.getTest().get(0);
		// testen der elemente des tests
		assertEquals("matchPattern", test0.getName());
//		assertEquals("", test0.getDescription());

		Param param1 = test0.getParam().get(0);
		// testen der elemente des params
		assertEquals(1, param1.getId());
		assertEquals("no|lin|nlin", param1.getContent());

		//-----Step 'root' testen Ende-----

		Step step1 = process.getStep("beulen");
		// testen der step-attribute
		assertEquals("beulen", step1.getName());
		assertEquals("automatic", step1.getType());
		assertEquals("Die FE-Struktur wird an 1 Position auf Beulsteifigkeit und Beulfestigkeit untersucht.", step1.getDescription());
		
		// testen der step-elemente
		assertEquals(7, step1.getInit().size());

		// testen der init0-attribute
		Init init0 = step1.getInit(0);
		assertEquals("inc", init0.getName());
		assertEquals("file", init0.getFromobjecttype());
		assertEquals("absfilename", init0.getReturnfield());
		assertEquals("root", init0.getFromstep());

		// testen der work-attribute
		Work work = step1.getWork();
		assertEquals("starte_prozesskette", work.getName());
		assertEquals("beulen", work.getCommand());
		assertEquals("irgendeine beschreibung", work.getDescription());

		// testen der callitem0-attribute
		Callitem callitem1 = work.getCallitem(1);
		assertEquals(2, callitem1.getSequence());
		assertEquals("--matdb", callitem1.getPar());
		assertEquals("=", callitem1.getDel());
		assertEquals("$matdb", callitem1.getVal());
	}


}
