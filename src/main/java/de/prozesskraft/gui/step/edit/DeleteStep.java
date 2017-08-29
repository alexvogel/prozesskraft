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

public class DeleteStep
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
	public DeleteStep()
	{
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);
		this.step = new Step();
		this.step.setName("bb");

		shell.setText("delete step " + this.step.getName());
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	/**
	 * Constructor fuer pkraft & Co.
	 */
	public DeleteStep(Shell fatherShell, Object father, Step step)
	{
		this.father = father;
		this.step = step;

		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try
		{
			shell.setText("delete step " + this.step.getName());
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
	    labelIntro.setText("WARNING\nyou are about to delete the step.\nall aggregated data and all generated files will erased from the filesystem. for all downstream steps a full reset will be performed.");

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
		btnDelete.setText("delete");
		btnDelete.addSelectionListener(listenerButtonDelete);

		composite.layout();
		
	}

	SelectionAdapter listenerButtonCancel = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			shell.dispose();
		}
	};	

	SelectionAdapter listenerButtonDelete = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{

			// 1) falls der zu loeschende step einen subprocess enthaelt, soll dieser aus pradar geloescht werden
			if(step.getSubprocess() != null)
			{
				String call = ((SIInsightCreator)father).getFather().getIni().get("apps", "pradar-delete") + " -instance " + step.getAbsdir() + "/process.pmb"; 
				((SIInsightCreator)father).getFather().log("info", "calling: "+call);

				try
				{
					java.lang.Process sysproc = Runtime.getRuntime().exec(call);
				}
				catch (IOException e)
				{
					((SIInsightCreator)father).getFather().log("error", e.getMessage());
					return;
				}
			}

			// 2) einen reset durchfuehren. damit werden Daten geloescht und alle downstream steps anstossen resettet.
			step.getParent().resetStep(step.getName());

			// 3) den step aus prozess entfernen
			step.getParent().removeStep(step);

			// 4) die veraenderte instanz auf platte schreiben
			step.getParent().writeBinary();

			// 5) nachfragefenster schliessen
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
