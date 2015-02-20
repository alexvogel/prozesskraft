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
import de.caegroup.process.Subprocess;
import de.caegroup.process.Variable;
import de.caegroup.process.Work;
import de.caegroup.process.Callitem;

public class TestProcessMultiappshake {

	Process process = new Process();

	@Before
	public void setUp()
	{
		
	}

	@Test
	public void testReadXml()
	{
		String pathToXml = "src/test/resources/multiappshake.xml";
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
}
