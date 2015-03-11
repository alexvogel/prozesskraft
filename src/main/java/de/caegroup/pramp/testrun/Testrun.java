package de.caegroup.pramp.testrun;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Testrun
{
	Shell shell = null;
	private Display display = Display.getCurrent();
	private String splDir = "";

	/**
	 * @wbp.parser.entryPoint
	 */
	public Testrun()
	{
		this.shell = new Shell();
		
		Composite composite = new Composite(shell, SWT.NONE);

		composite.setLayoutData(new GridData(2, 1));
		createControls(composite);
	}


	
	/**
	 * constructor
	 */
	public Testrun(Shell parent, String splDir)
	{
		this.shell = new Shell(parent);
		this.splDir = splDir;

		try
		{
			shell.setText("testrun");
			shell.setSize(600, 625);
			
			shell.setLayout(new GridLayout(1, false));
			shell.setLocation(display.getCursorLocation());
			
			Composite composite = new Composite(shell, SWT.FILL | SWT.BORDER);
	
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
		FilenameFilter callFilter = new FilenameFilter()
		{
			public boolean accept(File file, String name)
			{
				if(name.startsWith(".call."))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
		};

		// Pattern-Matching zum extrahieren der id aus dem call-File vorbereiten
		Pattern p = Pattern.compile("^\\.call\\.(.+)\\..+$");
		
//		// tabFolder erzeugen
//		CTabFolder tabFolder = new CTabFolder(composite, SWT.BORDER);
//		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
//		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
//		tabFolder.setTabPosition(SWT.TOP);
//		tabFolder.setTabHeight(30);

		// ueber alle unterordner in splDir iterieren und fuer jedes gefundene callfile ein TabItem erzeugen
		for(java.io.File actSplSubDir : new java.io.File(splDir).listFiles())
		{
			if(actSplSubDir.isDirectory())
			{
				String idPart1 = actSplSubDir.getName();
				String idPart2 = "unknown";
				// das directory durchgehen und alle ".call."-files feststellen
				for(java.io.File actFile : actSplSubDir.listFiles(callFilter))
				{
					Matcher m = p.matcher(actFile.getName());
					if(m.matches())
					{
						idPart2 = m.group(1); 
					}
					
//					TestrunItem tabItem_testcase = new TestrunItem(this, idPart1+"-"+idPart2, actFile, tabFolder);
					
//					CTabItem tabItem_testcase = new CTabItem(tabFolder, SWT.NONE);
//					tabItem_testcase.setText(idPart1+"-"+idPart2);
//					tabItem_testcase.setToolTipText("testrunner: "+actFile.getAbsolutePath());
					
					Composite composite2 = new Composite(composite, SWT.FILL | SWT.BORDER);

					composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
					
					// comment
					Label comment = new Label(composite2, SWT.NONE);
					comment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
					comment.setText("comment");
					comment.setToolTipText("a small description");
					
			
			}
			}
		}
		
	}
	
	
}

