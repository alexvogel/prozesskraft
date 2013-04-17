package de.caegroup.pramp.parts;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;

public class TestPrampPartUi1
{
	/*----------------------------
	  fields
	----------------------------*/
	PrampPartUi1 ui = new PrampPartUi1("irgendwas");
	
	/*----------------------------
	  setup
	----------------------------*/
	@Before
	public void setUp()
	{
		ui.setProcessMainDir("src/test/resources/processes");
	}

	/*----------------------------
	  tests
	----------------------------*/
	@Test
	public void testGetProcessList()
	{
		ArrayList<String> processNames = ui.getProcesses();
		// test ob die richtige Anzahl festgestellt wurde
		assertEquals(3, processNames.size());
		// test auf richtige sortierung innerhalb der liste
		assertEquals(processNames.get(0), "beulen");
		assertEquals(processNames.get(1), "modal");
		assertEquals(processNames.get(2), "multibeulen");
	}

	@Test
	public void testGetVersionList()
	{
		String processName = "beulen";
		ArrayList<String> versionNames = ui.getVersions(processName);
		assertEquals(4, versionNames.size());
	}
}
