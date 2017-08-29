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
import de.prozesskraft.pkraft.Log;
import de.prozesskraft.pkraft.Param;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.pkraft.Work;

public class TestStringResolve {

	Process process = new Process();
	ArrayList<String> nameDatenFuerTest = new ArrayList<String>();
	ArrayList<String> splDatenFuerTest = new ArrayList<String>();
	ArrayList<String> callDatenFuerTest = new ArrayList<String>();
	ArrayList<String> resultDatenFuerTest = new ArrayList<String>();

	@Before
	public void setUp()
	{
		Step stepRoot = new Step();
		stepRoot.setName("root");
		process.addStep(stepRoot);
		List listRoot = new List();
		listRoot.setName("irgendEinListennamen");
		listRoot.addItem("ersterEintrag-bla");
		stepRoot.addList(listRoot);
		
		Step step1 = new Step();
		step1.setName("appshake");
		step1.setLoop("index(name)");
		step1.setType("process");
		step1.setDescription("startet den prozess appshake mit dem daten eines testlaufs");
		process.addStep(step1);

		List listName = new List(step1);
		listName.setName("name");
		listName.addItem("cb2nvh_makesimpackbset");
		listName.addItem("cb2nvh_makeadamsaset");
		listName.addItem("cb2nvh_makeadamssetdload");
		nameDatenFuerTest.add("cb2nvh_makesimpackbset");
		nameDatenFuerTest.add("cb2nvh_makeadamsaset");
		nameDatenFuerTest.add("cb2nvh_makeadamssetdload");

		List listSpl = new List(step1);
		listSpl.setName("spl");
		listSpl.addItem("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1");
		listSpl.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1");
		listSpl.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1");
		splDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1");
		splDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1");
		splDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1");

		List listCall = new List(step1);
		listCall.setName("call");
		listCall.addItem("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1/.call.a.txt");
		listCall.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1/.call.1.txt");
		listCall.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1/.call.1.txt");
		callDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1/.call.a.txt");
		callDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1/.call.1.txt");
		callDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1/.call.1.txt");

		List listResult = new List(step1);
		listResult.setName("result");
		listResult.addItem("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1/.result.a.fpr");
		listResult.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1/.result.1.fpr");
		listResult.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1/.result.1.fpr");
		resultDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1/.result.a.fpr");
		resultDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1/.result.1.fpr");
		resultDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1/.result.1.fpr");

		step1.fan();
	}

	@Test
	public void testResolving()
	{
//		System.out.println(process.getStep("appshake").getList("spl").getName());
//		System.out.println(process.getStep("appshake"));
		
//		for(Step actStep : process.getStep())
//		{
//			System.out.println("----");
//			System.out.println("stepname: "+actStep.getName());
//			System.out.println("loopvarstep: "+actStep.getLoopvar());
//			System.out.println("unresolvter string: {$spl[{$loopvarstep}]}");
//			System.out.println("fertig resolvter string: "+actStep.resolveString("{$spl[{$loopvarstep}]}"));
//		}

		assertEquals("bla_cb2nvh_makesimpackbset_cb2nvh_makeadamssetdload.inp", process.getStep("appshake@1").resolveString("bla_{$name}_{$name[2]}.inp"));

		assertEquals(nameDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$name}"));
		assertEquals(nameDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$name[0]}"));
		assertEquals(nameDatenFuerTest.get(1), process.getStep("appshake@1").resolveString("{$name[1]}"));
		assertEquals(nameDatenFuerTest.get(2), process.getStep("appshake@1").resolveString("{$name[2]}"));
		
		assertEquals(splDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$spl}"));
		assertEquals(splDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$spl[0]}"));
		assertEquals(splDatenFuerTest.get(1), process.getStep("appshake@1").resolveString("{$spl[1]}"));
		assertEquals(splDatenFuerTest.get(2), process.getStep("appshake@1").resolveString("{$spl[2]}"));

		assertEquals(callDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$call}"));
		assertEquals(callDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$call[0]}"));
		assertEquals(callDatenFuerTest.get(1), process.getStep("appshake@1").resolveString("{$call[1]}"));
		assertEquals(callDatenFuerTest.get(2), process.getStep("appshake@1").resolveString("{$call[2]}"));

		assertEquals(resultDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$result}"));
		assertEquals(resultDatenFuerTest.get(0), process.getStep("appshake@1").resolveString("{$result[0]}"));
		assertEquals(resultDatenFuerTest.get(1), process.getStep("appshake@1").resolveString("{$result[1]}"));
		assertEquals(resultDatenFuerTest.get(2), process.getStep("appshake@1").resolveString("{$result[2]}"));
	}
	
	@Test
	public void testResolving2()
	{
		for(Step actStep : process.getStep())
		{
			System.err.println("aktueller step: >" + actStep.getName() + "<");
		}
		
		assertEquals("ersterEintrag-bla", process.getStep("appshake@1").resolveString("{root:$irgendEinListennamen}"));
	}
}
