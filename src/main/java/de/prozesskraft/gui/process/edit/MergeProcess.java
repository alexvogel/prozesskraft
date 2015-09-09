package de.prozesskraft.gui.process.edit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.prozesskraft.gui.process.insight.PIInsightCreator;
import de.prozesskraft.gui.step.insight.SIFileGui;
import de.prozesskraft.gui.step.insight.SIInsightCreator;
import de.prozesskraft.gui.step.insight.SIVariableGui;
import de.prozesskraft.pkraft.*;
import de.prozesskraft.pkraft.Process;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class MergeProcess
{
	private PIInsightCreator father;
	public Shell shell = null;
//	public Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
//	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();

	private Process process = null;

	Text text = null;

	/**
	 * @wbp.parser.entryPoint
	 */
	public MergeProcess()
	{
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);
		this.process = new Process();

		shell.setText("merge this instance with another instance of the process " + this.process.getName());
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	/**
	 * Constructor fuer pkraft & Co.
	 */
	public MergeProcess(Shell fatherShell, PIInsightCreator father, Process process)
	{
		this.father = father;
		this.process = process;

		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try
		{
			shell.setText("merge this instance with another instance of the process " + this.process.getName());
			shell.setLocation(display.getCursorLocation());
			shell.setLayout(new GridLayout());
			
			Composite composite = new Composite(shell, SWT.NONE);
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
//			display.dispose();
		}
	}

	/**
	 * Create contents of the view part.
	 */
	public void createControls(Composite composite)
	{
		shell.setSize(400, 200);

		composite.setLayout(new GridLayout(1, false));
		
		Composite compositeEntries = new Composite(composite, SWT.NONE);
		compositeEntries.setLayout(new GridLayout(2, false));
//		gd_composite.minimumWidth = 10;
//		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		// label mit hinweistext
	    Label labelIntro = new Label(compositeEntries, SWT.WRAP);
	    GridData gd_labelIntro = new GridData(SWT.LEFT, SWT.TOP, true, true, 2, 1);
	    labelIntro.setLayoutData(gd_labelIntro);
	    labelIntro.setText("please choose instance you want to merge into this instance.");

		text = new Text(compositeEntries, SWT.BORDER);
		text.setToolTipText("choose instance by picking its process.pmb");
		text.setMessage("choose instance by picking its process.pmb");
		
		Button fileButton = new Button(compositeEntries, SWT.NONE);
		fileButton.setText("...");
		fileButton.addSelectionListener(listener_file_button);

		// griddata
		GridData gd_textFile = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		text.setLayoutData(gd_textFile);

		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		btnCancel.addSelectionListener(listenerButtonCancel);

		Label dummyLabel = new Label(compositeBtn, SWT.NONE);

		Button btnDelete = new Button(compositeBtn, SWT.NONE);
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnDelete.setText("merge");
		btnDelete.addSelectionListener(listenerButtonMerge);

		composite.layout();
		
		
		
//	    labelWrap.setBounds(120, 10, 100, 100);

	    
//		Label fileKey = new Label(compositeEntries, SWT.NONE);
//		fileKey.setText("key");
//		fileKey.setToolTipText("key/label of file");
//		
//		FormData fd_fileKey = new FormData();
//		fd_fileKey.top = new FormAttachment(0, 10);
//		fd_fileKey.left = new FormAttachment(0, 8);
//		fileKey.setLayoutData(fd_fileKey);
//
//		textKey = new Text(compositeEntries, SWT.BORDER);
//		textKey.setToolTipText("key/label of file");
//		textKey.setMessage("key/label of file");
//
//		FormData fd_textKey = new FormData();
//		fd_textKey.top = new FormAttachment(0, 5);
//		fd_textKey.left = new FormAttachment(0, 50);
//		fd_textKey.right = new FormAttachment(100, -8);
//		fd_textKey.width = 190;
//		textKey.setLayoutData(fd_textKey);
//		
//		Label filePath = new Label(compositeEntries, SWT.NONE);
//		filePath.setText("path");
//		filePath.setToolTipText("path of file");
//		
//		FormData fd_filePath = new FormData();
//		fd_filePath.top = new FormAttachment(0, 45);
//		fd_filePath.left = new FormAttachment(0, 8);
//		filePath.setLayoutData(fd_filePath);
//
//		textPath = new Text(compositeEntries, SWT.BORDER);
//		textPath.setToolTipText("full path of file");
//		textPath.setMessage("path");
//
//		FormData fd_textPath = new FormData();
//		fd_textPath.top = new FormAttachment(0, 40);
//		fd_textPath.left = new FormAttachment(0, 50);
//		fd_textPath.right = new FormAttachment(100, -35);
//		textPath.setLayoutData(fd_textPath);
//		
//		Button fileButton = new Button(compositeEntries, SWT.NONE);
//		fileButton.setText("...");
//		fileButton.addSelectionListener(listener_file_button);
//
//		FormData fd_file_button = new FormData();
//		fd_file_button.top = new FormAttachment(0, 40);
//		fd_file_button.left = new FormAttachment(textPath, 0, SWT.RIGHT);
//		fd_file_button.width = 27;
//		fd_file_button.height = 27;
//		fileButton.setLayoutData(fd_file_button);
//		
//		Composite compositeBtn = new Composite(composite, SWT.NONE);
//		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		GridLayout sss = new GridLayout(3, true);
//		compositeBtn.setLayout(sss);
//		
//		Button btnCancel = new Button(compositeBtn, SWT.NONE);
//		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		btnCancel.setText("cancel");
//		btnCancel.addSelectionListener(listenerButtonCancel);
//
//		Button btnDelete = new Button(compositeBtn, SWT.NONE);
//		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		btnDelete.setText("delete");
//		btnDelete.addSelectionListener(listenerButtonDelete);
//		if(this.entertype.equals("add"))
//		{
//			btnDelete.setEnabled(false);
//		}
//		
//		btnEnter = new Button(compositeBtn, SWT.NONE);
//		btnEnter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		btnEnter.setText("enter");
//		btnEnter.addSelectionListener(listenerButtonEnter);
//		
//		// setzen der aktuellen werte der variable in die felder
//		textKey.setText(file.getKey());
//		textPath.setText(file.getAbsfilename());
//		einstellungen.setKey(file.getKey());
//		einstellungen.setPath(file.getAbsfilename());
		
//		 //binding
//		bindingContextFelder();
	}

	SelectionAdapter listenerButtonCancel = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			shell.dispose();
		}
	};	

	SelectionAdapter listenerButtonMerge = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// testen ob angegebener pfadein file existiert
			java.io.File fileInstance = new java.io.File(text.getText());
			
			if(!fileInstance.exists() || fileInstance.isDirectory())
			{
				father.getFather().log("error", "instance binary does not exist: " + fileInstance.getAbsolutePath());
				shell.dispose();
			}

			// feststellen ob beide instanzen vom selben typ sind (process, processversion)
			Process p1 = new Process();
			p1.setInfilebinary(fileInstance.getAbsolutePath());

			Process guestProcess = p1.readBinary();

			// sind sie vom gleichen typ
			if(!process.getName().equals(guestProcess.getName()))
			{
				father.getFather().log("error", "instances are not from the same type (" + process.getName() + "!=" + guestProcess.getName());
				shell.dispose();
			}
			
			// sind sie vom gleichen version
			if(!process.getVersion().equals(guestProcess.getVersion()))
			{
				father.getFather().log("error", "instances are not from the same version (" + process.getVersion() + "!=" + guestProcess.getVersion());
				shell.dispose();
			}

			// merge durchfuehren
			// alle fanned steps (ehemalige multisteps) des zu mergenden prozesses in die fanned multisteps des bestehenden prozesses integrieren
			for(Step actStep : guestProcess.getStep())
			{
				if(actStep.isAFannedMultistep())
				{
					father.getFather().log("info", "merging from external instance step " + actStep.getName());
					if(process.integrateStep(actStep))
					{
						father.getFather().log("info", "merging step successfully.");
					}
					else
					{
						father.getFather().log("error", "merging step failed.");
					}
				}
			}

			shell.dispose();
		}
	};	

	/**
	 * listener for selection of "..."-button
	 * opens a shell to select a directory-path
	 * selection will be echoed in textfield for directory path
	 */
	SelectionAdapter listener_file_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
	        FileDialog dlg = new FileDialog(new Shell());

	        // Set the initial filter path according
	        // to anything they've selected or typed in
	        String initialPathInDialog = "";
	        java.io.File dummyFile = new java.io.File(text.getText());
	        if(dummyFile.isDirectory())
	        {
	        	initialPathInDialog = dummyFile.getAbsolutePath();
	        }
	        else if(dummyFile.isFile())
	        {
	        	// setzen des pfades in dem sich das file befindet
	        	initialPathInDialog = dummyFile.getParent();
	        	// setzen der bereits getroffenen auswahl
		        dlg.setFileName(dummyFile.getAbsolutePath().substring(dummyFile.getAbsolutePath().lastIndexOf("/")));
	        }
	        else
	        {
	        	// der ort des prozesses soll vorgewaehlt werden
	        	initialPathInDialog = process.getBaseDir();
	        }
	        dlg.setFilterPath(initialPathInDialog);

	        // Change the title bar text
	        dlg.setText("File Dialog");

	        // Customizable message displayed in the dialog
//	        dlg.setMessage("Select a file");

	        // Calling open() will open and run the dialog.
	        // It will return the selected directory, or
	        // null if user cancels
	        String path = dlg.open();
	        if (path != null) {
	          // Set the text box to the new selection
	        	text.setText(path);
	    		text.setToolTipText(path);
//	        	parent.log("info", "setting instancedirectory: "+dir);
//	        	text_instancedirectory.setText(dir);
	        }
		}
	};
	

	/**
	 * @return the father
	 */
	public PIInsightCreator getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(PIInsightCreator father) {
		this.father = father;
	}

	
}
