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

public class TestProcessIO {

	Process process;
	String inFileXml = "src/test/resources/definitions/Exterior/beulen/0.1.0/process.xml";
	String outFileXml = "src/test/resources/beulen_out.xml";
	String inFileBinary = "src/test/resources/beulen.pmb";
	String outFileBinary = "src/test/resources/beulen.pmb";

	@Before
	public void setUp()
	{
		
		process = new Process();
		process.setInfilexml(inFileXml);
		try
		{
			process.readXml();
		} catch (JAXBException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		process.setName("hullahupp");
		process.setInfilexml(inFileXml);
		process.setOutfilexml(outFileXml);
		process.setInfilebinary(inFileBinary);
		process.setOutfilebinary(outFileBinary);
	}
	
	@Test
	/**
	 * dieser test soll einen prozess aus einem binary einlesen.
	 */
	public void testPersistanceBinary()
	{
		process.writeBinary();
//		process.writeXml();
		Process p2 = process.readBinary();
		assertEquals("hullahupp", p2.getName());
		assertEquals(inFileXml, p2.getInfilexml());
		assertEquals(outFileXml, p2.getOutfilexml());
		assertEquals(inFileBinary, p2.getInfilebinary());
		assertEquals(outFileBinary, p2.getOutfilebinary());
	}
	


}
