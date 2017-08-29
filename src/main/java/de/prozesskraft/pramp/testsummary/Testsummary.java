package de.prozesskraft.pramp.testsummary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

import de.prozesskraft.pramp.parts.PrampPartUi1;

public class Testsummary
{
	Shell shell = null;
	private Display display = Display.getCurrent();
	private String testlogDir = "";
	private PrampPartUi1 father = null;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Testsummary()
	{
		this.shell = new Shell();
		this.shell.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(shell, SWT.FILL | SWT.BORDER);
		composite.setLayout(new GridLayout(1, false));

		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}


	
	/**
	 * constructor
	 */
	public Testsummary(PrampPartUi1 prampPart, Shell parent, String testlogDir)
	{
		this.father = prampPart;
		this.shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
		this.testlogDir = testlogDir;

		try
		{
			shell.setText("testlog");
			shell.setSize(600, 625);
			
			shell.setLayout(new GridLayout(1, false));
			shell.setLocation(display.getCursorLocation());
			
			Composite composite = new Composite(shell, SWT.FILL | SWT.BORDER);
			composite.setLayout(new GridLayout(1, false));

			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			createControls(composite);

			try
			{
				shell.open();
	
				while (!shell.isDisposed())
				{
					if( ! display.readAndDispatch())
					{
						display.sleep();
					}
				}
	
			}
			finally
			{
				if (!shell.isDisposed())
				{
					shell.dispose();
				}
			}
			
		}
		// wenn display disposed wird, wird auch das aufrufende fenster gekillt
		finally
		{
	//		display.dispose();
		}
	}


	/**
	 * Create contents of the view part.
	 */
	public void createControls(Composite composite)
	{
		composite.setLayout(new GridLayout(1, false));
		
		// Filenamefilter fuer das callfiles erzeugen fuer spaetere filterung
		FilenameFilter testsummaryFileFilter = new FilenameFilter()
		{
			public boolean accept(File file, String name)
			{
				if(name.endsWith(".testsummary.txt"))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};

		// die erste zeile in ein composite setzen
		Composite compositeTestlogHeader = new Composite(composite, SWT.FILL | SWT.BORDER);
		
		GridLayout headerLayout = new GridLayout();
		headerLayout.numColumns = 3;
		headerLayout.makeColumnsEqualWidth = true;

		compositeTestlogHeader.setLayout(headerLayout);

		GridData headerData = new GridData();
		headerData.horizontalAlignment = GridData.FILL;
		headerData.verticalAlignment = GridData.FILL;
		
		compositeTestlogHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Label label_time = new Label(compositeTestlogHeader, SWT.FILL | SWT.BORDER);
		label_time.setText("time");
		
		Label label_number = new Label(compositeTestlogHeader, SWT.FILL | SWT.BORDER);
		label_number.setText("number of test instances");
		
		Label label_successRatio = new Label(compositeTestlogHeader, SWT.FILL | SWT.BORDER);
		label_successRatio.setText("success ratio");
		
		
		// ueber alle files in testlogDir iterieren und fuer jedes gefundene callfile ein TabItem erzeugen
//		Charset charset = Charset.forName("ISO-8859-1");
//		Charset charset = Charset.forName("UTF-32");
//		for(java.io.File actTestsummaryFile : new java.io.File(testlogDir).listFiles(testsummaryFileFilter))
//		{
//			if(actTestsummaryFile.isFile())
//			{
//				try
//				{
//					List<String> allLines = Files.readAllLines(actTestsummaryFile.toPath(), charset);
//				} catch (IOException e)
//				{
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				// die erste zeile in ein composite setzen
//				Composite compositeTestlogItem = new Composite(composite, SWT.FILL | SWT.BORDER);
//				composite.setLayout(new GridLayout(1, false));
//
//				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//				createControls(composite);
//
//			}
//		}
	}



	/**
	 * @return the father
	 */
	public PrampPartUi1 getFather() {
		return father;
	}



	/**
	 * @param father the father to set
	 */
	public void setFather(PrampPartUi1 father) {
		this.father = father;
	}



}

