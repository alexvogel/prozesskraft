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
import de.caegroup.process.Variable;
import de.caegroup.process.Work;
import de.caegroup.process.Callitem;

public class TestStringResolve {

	Process process = new Process();
	ArrayList<String> nameDatenFuerTest = new ArrayList<String>();
	ArrayList<String> splDatenFuerTest = new ArrayList<String>();
	ArrayList<String> callDatenFuerTest = new ArrayList<String>();
	ArrayList<String> resultDatenFuerTest = new ArrayList<String>();

	@Before
	public void setUp()
	{
		
		Step step1 = new Step("appshake");
		step1.setLoop("index(name)");
		step1.setType("process");
		step1.setDescription("startet den prozess appshake mit dem daten eines testlaufs");
		process.addStep(step1);

		List listName = new List(step1, "name");
		listName.addItem("cb2nvh_makesimpackbset");
		listName.addItem("cb2nvh_makeadamsaset");
		listName.addItem("cb2nvh_makeadamssetdload");
		nameDatenFuerTest.add("cb2nvh_makesimpackbset");
		nameDatenFuerTest.add("cb2nvh_makeadamsaset");
		nameDatenFuerTest.add("cb2nvh_makeadamssetdload");

		List listSpl = new List(step1, "spl");
		listSpl.addItem("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1");
		listSpl.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1");
		listSpl.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1");
		splDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1");
		splDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1");
		splDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1");

		List listCall = new List(step1, "call");
		listCall.addItem("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1/.call.a.txt");
		listCall.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1/.call.1.txt");
		listCall.addItem("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1/.call.1.txt");
		callDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makesimpackbset/3.1.0/spl/1/.call.a.txt");
		callDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamsaset/1.1.0/spl/1/.call.1.txt");
		callDatenFuerTest.add("/data/localsoft/deploy/install/cb2nvh_makeadamssetdload/2.0.1/spl/1/.call.1.txt");

		List listResult = new List(step1, "result");
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
}
