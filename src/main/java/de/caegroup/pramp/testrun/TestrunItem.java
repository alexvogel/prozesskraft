package de.caegroup.pramp.testrun;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TestrunItem {

	private Testrun father = null;
	private String name = "unnamed";
	private File callFile = null;
	private CTabFolder tabfolder = null;
	private String comment = "myComment";
	private String call = "myCall";

	public TestrunItem(Testrun father, String name, File callFile, CTabFolder tabfolder)
	{
		this.father = father;
		this.name = name;
		this.callFile = callFile;
		this.tabfolder = tabfolder;

		CTabItem tabItem_testcase = new CTabItem(this.tabfolder, SWT.NONE);
		tabItem_testcase.setText(this.name);
		tabItem_testcase.setToolTipText("testrun: "+this.callFile.getAbsolutePath());

		Composite composite = new Composite(this.tabfolder, SWT.FILL | SWT.BORDER);

		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		createControls(composite);

	}
	
	/**
	 * Create contents of the view part.
	 */
	public void createControls(Composite composite)
	{
		composite.setLayout(new GridLayout(1, false));

		Composite compositeEntries = new Composite(composite, SWT.NONE);

		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(gd_composite);

		compositeEntries.setLayout(new GridLayout(2, false));

		// comment
		Label comment = new Label(compositeEntries, SWT.NONE);
		comment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		comment.setText("comment");
		comment.setToolTipText("a small description");
		
		Text commentText = new Text(compositeEntries, SWT.READ_ONLY);
		commentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		commentText.setText(this.comment);

		// call
		Label call = new Label(compositeEntries, SWT.NONE);
		call.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		call.setText("call");
		call.setToolTipText("full call");
		
		Text callText = new Text(compositeEntries, SWT.READ_ONLY);
		callText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		callText.setText(this.call);

		// button
		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Label dummyLabel = new Label(compositeBtn, SWT.NONE);
		dummyLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		btnCancel.addSelectionListener(listenerButtonCancel);
		
		Button btnRun = new Button(compositeBtn, SWT.NONE);
		btnRun.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnRun.setText("run test");
		btnRun.addSelectionListener(listenerButtonRun);
		
	}

	SelectionAdapter listenerButtonRun = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			getFather().shell.dispose();
		}
	};

	SelectionAdapter listenerButtonCancel = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			getFather().shell.dispose();
		}
	};

	/**
	 * @return the father
	 */
	public Testrun getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(Testrun father) {
		this.father = father;
	}	

}
