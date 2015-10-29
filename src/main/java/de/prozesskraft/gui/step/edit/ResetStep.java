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
import de.prozesskraft.pmodel.PmodelPartUi1;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class ResetStep
{
	private Object father;
	public Shell shell = null;
//	public Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
//	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();

	private Step step = null;

	Button buttonFullReset = null;
	Button buttonCommitReset = null;

	/**
	 * @wbp.parser.entryPoint
	 */
	public ResetStep()
	{
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);
		this.step = new Step();
		this.step.setName("bb");

		shell.setText("reset step " + this.step.getName());
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	/**
	 * Constructor fuer pkraft & Co.
	 */
	public ResetStep(Shell fatherShell, Object father, Step step)
	{
		this.father = father;
		this.step = step;

		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try
		{
			shell.setText("reset step " + this.step.getName());
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

		boolean fullReset = true;

	    if(step.isRoot())
//		if(false)
		{
		    Label labelIntro = new Label(compositeEntries, SWT.WRAP);
		    labelIntro.setText("WARNING\nyou are about to fully reset all steps of this instance.\naggregated data (variables, files) will be deleted, all\nproduced files will be erased from the filesystem.\n\ndo you really want to reset all steps?");

		    fullReset = true;
		}
		else
		{
			// label mit hinweistext
		    Label labelIntro = new Label(compositeEntries, SWT.WRAP);
		    GridData gd_labelIntro = new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1);
//		    gd_labelIntro.widthHint = 426;
		    labelIntro.setLayoutData(gd_labelIntro);
//		    labelIntro.setSize(20,50);
		    labelIntro.setText("WARNING\nyou are about to reset the step.\nplease choose the type of reset:");

			// Radio button fuer vollen resets des aktuellen stps und aller abhaengigen steps
			buttonFullReset = new Button(compositeEntries, SWT.RADIO);
			buttonFullReset.setText("full");
			buttonFullReset.setSelection(true);

			// label mit hinweistext
		    Label labelFullReset = new Label(compositeEntries, SWT.WRAP);
		    labelFullReset.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
//		    labelFullReset.setSize(420,30);
		    labelFullReset.setText("aggregated data (inits, variables, files) will be deleted, all produced files will be erased from the filesystem. the same applies to all downstream steps.");

			// Radio button fuer reset des commits des aktuellen steps und eines vollen resets aller abhaengigen steps
			buttonCommitReset = new Button(compositeEntries, SWT.RADIO);
			buttonCommitReset.setText("commit");
			buttonCommitReset.setSelection(false);

			// label mit hinweistext
		    Label labelCommitReset = new Label(compositeEntries, SWT.WRAP);
		    labelCommitReset.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
		    labelCommitReset.setText("for the mentioned step only the commits will be resetted. computed data remains untouched. for all downstream steps a full reset will be performed.");
		}

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
		btnDelete.setText("reset");
		btnDelete.addSelectionListener(listenerButtonReset);

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

	SelectionAdapter listenerButtonReset = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("selection of full: " + buttonFullReset.getSelection());
//			System.out.println("selection of commit: " + buttonCommitReset.getSelection());
			if(buttonFullReset.getSelection() && !buttonCommitReset.getSelection())
			{
				System.err.println("performing a full reset on step " + step.getName() + " and all downstream steps");

				// den step resetten und alle von diesem step abhaengigen steps
				step.getParent().resetStep(step.getName());
			}
			else if(!buttonFullReset.getSelection() && buttonCommitReset.getSelection())
			{
				System.err.println("performing a resetCommit on step " + step.getName() + " and afull reset on all downstream steps");

				// den step resetten und alle von diesem step abhaengigen steps
				try
				{
					step.getParent().resetCommitStep(step.getName());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else
			{
				System.err.println("error: no reset performed");
			}

			// die veraenderte instanz auf platte schreiben
			step.getParent().writeBinary();

			// in pradar aktualisieren
			String call = ((SIInsightCreator)father).getFather().getIni().get("apps", "pradar-attend") + " -instance " + step.getParent().getInfilebinary(); 
			((SIInsightCreator)father).getFather().log("info", "calling: "+call);
			
			try
			{
				java.lang.Process sysproc = Runtime.getRuntime().exec(call);
			}
			catch (IOException e)
			{
				((SIInsightCreator)father).getFather().log("error", e.getMessage());
			}
			
			// nachfragefenster schliessen
			shell.dispose();
		}
	};

	/**
	 * @return the father
	 */
	public Object getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(Object father) {
		this.father = father;
	}

	
}
