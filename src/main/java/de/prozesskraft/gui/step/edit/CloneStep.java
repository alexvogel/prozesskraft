package de.prozesskraft.gui.step.edit;

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

import de.prozesskraft.gui.step.insight.SIFileGui;
import de.prozesskraft.gui.step.insight.SIInsightCreator;
import de.prozesskraft.gui.step.insight.SIVariableGui;
import de.prozesskraft.pkraft.*;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class CloneStep
{
	private SIInsightCreator father;
	public Shell shell = null;
//	public Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
//	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();

	private Step step = null;

	/**
	 * @wbp.parser.entryPoint
	 */
	public CloneStep()
	{
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);
		this.step = new Step();
		this.step.setName("bb");

		shell.setText("clone step " + this.step.getName());
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	/**
	 * Constructor fuer pkraft & Co.
	 */
	public CloneStep(Shell fatherShell, SIInsightCreator father, Step step)
	{
		this.father = father;
		this.step = step;

		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try
		{
			shell.setText("clone step " + this.step.getName());
			shell.setLayout(new GridLayout());
			shell.setLocation(display.getCursorLocation());
			
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
		shell.setSize(400, 400);
		
		composite.setLayout(new GridLayout(1, false));
		
		Composite compositeEntries = new Composite(composite, SWT.NONE);
		compositeEntries.setLayout(new GridLayout(1, false));
//		gd_composite.minimumWidth = 10;
//		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		// label mit hinweistext
	    Label labelIntro = new Label(compositeEntries, SWT.WRAP);
	    GridData gd_labelIntro = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
	    labelIntro.setLayoutData(gd_labelIntro);
	    labelIntro.setText("you really want to clone the step?");

		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		btnCancel.addSelectionListener(listenerButtonCancel);

		Label dummyLabel = new Label(compositeBtn, SWT.NONE);

		Button btnClone = new Button(compositeBtn, SWT.NONE);
		btnClone.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnClone.setText("clone");
		btnClone.addSelectionListener(listenerButtonClone);

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

	SelectionAdapter listenerButtonClone = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// den step clonen und in den prozess integrieren
			Step clone = step.cloneAndIntegrateWithData();

			// prozess schreiben
			if(clone != null)
			{
				father.getFather().log("info", "step successfully cloned to " + clone.getName());
				step.getParent().writeBinary();
			}
			else
			{
				father.getFather().log("error", "cloning step failed");
			}

			// nachfragefenster schliessen
			shell.dispose();
		}
	};

	/**
	 * @return the father
	 */
	public SIInsightCreator getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(SIInsightCreator father) {
		this.father = father;
	}

	
}
