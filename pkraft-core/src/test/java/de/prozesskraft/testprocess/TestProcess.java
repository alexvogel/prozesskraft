package de.prozesskraft.testprocess;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.junit.Before;

import de.prozesskraft.pkraft.Callitem;
import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.Init;
import de.prozesskraft.pkraft.List;
import de.prozesskraft.pkraft.Param;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.pkraft.Work;

public class TestProcess {

	Process process = new Process();

	@Before
	public void setUp()
	{
		
	}
	
	@Test
	public void testGetInitcommitdirs()
	{
		process.setInitCommitFile("/home/qxb0117/etc");
		ArrayList<String> initcommitdirs = process.getInitCommitFileDirectory();
		assertEquals(1, initcommitdirs.size());
	}
	
	@Test
	public void testGetInitcommitdirs2()
	{
		process.setInitCommitFile("/home/qxb0117/etc:/home/irgendwas");
		ArrayList<String> initcommitdirs = process.getInitCommitFileDirectory();
		assertEquals(2, initcommitdirs.size());
	}

	@Test
	public void testGetInitcommitdirs3()
	{
		process.setInitCommitFile("/home/qxb0117/etc:/home/irgendwas::");
		ArrayList<String> initcommitdirs = process.getInitCommitFileDirectory();
		assertEquals(2, initcommitdirs.size());
	}

	@Test
	public void testGetInitcommitvarfiles()
	{
		process.setInitCommitVariable("/home/qxb0117/etc/bla");
		ArrayList<String> initcommitvarfiles = process.getInitCommitVariableDirectory();
		assertEquals(1, initcommitvarfiles.size());
	}
	
	@Test
	public void testGetInitcommitvarfiles2()
	{
		process.setInitCommitVariable("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu");
		ArrayList<String> initcommitvarfiles = process.getInitCommitVariableDirectory();
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
		process.setInitCommitVariable("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu::");
		ArrayList<String> initcommitvarfiles = process.getInitCommitVariableDirectory();
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
		process.setInitCommitVariable("/home/qxb0117/etc/bla:/home/qxb0117/etc/blu::");
		ArrayList<java.io.File> initcommitvarfiles = process.getInitCommitVariableDirectoryAsFile();
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
		String pathToXml = "src/test/resources/definitions/Exterior/beulen/0.1.0/process.xml";
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

		Step stepRoot = process.getRootStep();
		// testen der step-attribute
		assertEquals("root", stepRoot.getName());
		assertEquals(2, stepRoot.getCommit().size());

		Commit commit1 = stepRoot.getCommit("parameter");
		commit1.setToroot("lulu");
		// testen der commit-elemente des steps root
		assertEquals("lulu", commit1.getToroot());

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

		de.prozesskraft.pkraft.Test test0 = variable1.getTest().get(0);
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
		assertEquals("inc", init0.getListname());
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
		assertEquals("2", callitem1.getSequence().toString());
		assertEquals("--matdb", callitem1.getPar());
		assertEquals("=", callitem1.getDel());
		assertEquals("{$matdb}", callitem1.getVal());
		
	}

	@Test
	public void resolveCall()
	{
		String pathToXml = "src/test/resources/definitions/Exterior/beulen/0.1.0/process.xml";
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
		
		assertEquals("starte_prozesskette", process.getStep("beulen").getWork().getName());
		assertEquals(7, process.getStep("beulen").getWork().getCallitem().size());

		// erstellen der listen mit testdaten
		List listArea = new List();
		listArea.setName("area");
		listArea.addItem("area_3");
		process.getStep("beulen").addList(listArea);

		List listBasename = new List();
		listBasename.setName("basename");
		listBasename.addItem("F30_SuperWagon");
		process.getStep("beulen").addList(listBasename);

		List listMatdb = new List();
		listMatdb.setName("matdb");
		listMatdb.addItem("/irgendein/pfad/ins/nirgendwo.db");
		process.getStep("beulen").addList(listMatdb);

		List listNid = new List();
		listNid.setName("nid");
		listNid.addItem("777263");
		process.getStep("beulen").addList(listNid);

		List listForce = new List();
		listForce.setName("force");
		listForce.addItem("150");
		process.getStep("beulen").addList(listForce);

		List listParttype = new List();
		listParttype.setName("parttype");
		listParttype.addItem("fkl");
		process.getStep("beulen").addList(listParttype);

		List listInc = new List();
		listInc.setName("inc");
		listInc.addItem("/my/pyth/toInclude.inc");
		listInc.addItem("/my/pyth/toInclude2.inc");
		process.getStep("beulen").addList(listInc);

		assertEquals("area", process.getStep("beulen").getList("area").getName());
		assertEquals(1, process.getStep("beulen").getList("area").getItem().size());
		assertEquals("area_3", process.getStep("beulen").getList("area").getItem().get(0));

		System.out.println("Step beulen - the name of the work-phase is: "+process.getStep("beulen").getWork().getName());
		
		for(Callitem actCallitem : process.getStep("beulen").getWork().getCallitem())
		{
			System.out.println("unresolved callitem with sequence="+actCallitem.getSequence() + " | par=" + actCallitem.getPar()+ " | del=" + actCallitem.getDel()+ " | val=" + actCallitem.getVal());
		}
		

		for(Callitem actCallitem : process.getStep("beulen").getWork().getCallitem())
		{
			for(Callitem actResolvedCallitem : actCallitem.resolve())
			{
				System.out.println("resolved callitem with sequence="+actResolvedCallitem.getSequence() + " | par=" + actResolvedCallitem.getPar()+ " | del=" + actResolvedCallitem.getDel()+ " | val=" + actResolvedCallitem.getVal());
			}
		}

		// testen des erzeugen des aufrufstrings
		String call = process.getStep("beulen").getWork().getCall();
		System.out.println(call);

	}
}
