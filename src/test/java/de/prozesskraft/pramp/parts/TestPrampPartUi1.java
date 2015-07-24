package de.prozesskraft.pramp.parts;
//package de.prozesskraft.pramp.parts;
//
//import static org.junit.Assert.*;
//
//import java.util.ArrayList;
//
//import org.junit.Test;
//import org.junit.Before;
//import org.junit.Ignore;
//
//public class TestPrampPartUi1
//{
//	/*----------------------------
//	  fields
//	----------------------------*/
//	PrampPartUi1 ui = new PrampPartUi1("irgendwas");
//	String processDir = "target/test-classes/processes";
////	String processDir = "src/test/resources/processes";
//	String iniFile = "target/etc/default.ini";
//	
//	/*----------------------------
//	  setup
//	----------------------------*/
//	@Before
//	public void setUp()
//	{
////		ui.setProcessMainDir(processDir);
//		ui.setIni(iniFile);
//	}
//
//	/*----------------------------
//	  tests
//	----------------------------*/
//	@Test
//	public void testSetIniToDefault()
//	{
//		ui.setIni();
//		assertNotNull(ui.getIni());
//	}
//
//	@Test
//	public void testLoadIni()
//	{
//		ui.loadIni();
//		assertEquals("/data/prog/workspace/pramp-gui/src/test/resources/processes", ui.getProcessMainDir());
//	}
//
//	@Test
//	public void testGetProcesses()
//	{
//		ui.loadIni();
//		ArrayList<String> processNames = ui.getInstalledProcessNames();
//		// test ob die richtige Anzahl festgestellt wurde
//		assertEquals(3, processNames.size());
//		// test auf richtige sortierung innerhalb der liste
//		assertEquals(processNames.get(0), "beulen");
//		assertEquals(processNames.get(1), "modal");
//		assertEquals(processNames.get(2), "multibeulen");
//	}
//
//	@Test
//	public void testGetVersions()
//	{
//		ui.loadIni();
//		String processName = "beulen";
//		ArrayList<String> versionNames = ui.getInstalledVersionNames(processName);
//		assertEquals(2, versionNames.size());
//	}

//	@Test
//	public void testGetProcessDefinition()
//	{
//		ui.loadIni();
//		String processName = "beulen";
//		String version = "0.8.1";
//		ui.setProcess(processName);
//		ui.setVersion(version);
//		String processDefinition = ui.getProcessDefinition();
//		System.out.println(processDefinition);
//		assertEquals(processDir+"/"+processName+"/"+version+"/process.xml", processDefinition);
//	}
//	
//}
