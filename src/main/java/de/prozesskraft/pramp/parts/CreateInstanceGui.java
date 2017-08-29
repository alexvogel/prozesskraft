package de.prozesskraft.pramp.parts;

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

import de.prozesskraft.pkraft.*;
import de.prozesskraft.pkraft.Process;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class CreateInstanceGui
{
	private PrampPartUi1 father;
	public Shell shell = null;
//	public Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
//	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();

	Button buttonCreateOpen = null;
	Button buttonCreateStart = null;

	/**
	 * @wbp.parser.entryPoint
	 */
	public CreateInstanceGui()
	{
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);

		shell.setText("create instance");
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	/**
	 * Constructor fuer pkraft & Co.
	 */
	public CreateInstanceGui(Shell fatherShell, PrampPartUi1 father)
	{
		this.father = father;
		
//		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.ON_TOP);
		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try
		{
			shell.setText("create instance " + father.getProcess());
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
		shell.setSize(450, 180);
		
		composite.setLayout(new GridLayout(1, false));
		
		Composite compositeEntries = new Composite(composite, SWT.NONE);
		compositeEntries.setLayout(new GridLayout(1, false));
//		gd_composite.minimumWidth = 10;
//		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

	    Label labelIntro = new Label(compositeEntries, SWT.WRAP);
	    labelIntro.setText("you are about to create an instance of a process.\n\nThe root directory of this instance will be created in\n"+this.getFather().einstellungen.getBaseDirectory()+"\nDo not move, copy or delete instance data by hand.\nUse clone and delete of application 'pradar'.");

		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		btnCancel.addSelectionListener(listenerButtonCancel);

	    Button btnCreate = new Button(compositeBtn, SWT.NONE);
	    btnCreate.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
	    btnCreate.setText("create");
	    btnCreate.setToolTipText("create instance");
	    btnCreate.addSelectionListener(listenerButtonCreate);

	    Button btnCreateAndStart = new Button(compositeBtn, SWT.NONE);
		btnCreateAndStart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCreateAndStart.setText("create and start");
		btnCreateAndStart.setToolTipText("create instance and start it");
		btnCreateAndStart.addSelectionListener(listenerButtonCreateAndStart);

		composite.layout();
	}

	SelectionAdapter listenerButtonCancel = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			shell.dispose();
		}
	};	

	/**
	 * listener for Selections in of button 'create'
	 */
	SelectionAdapter listenerButtonCreate = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button show wurde gedrueckt");
			Process newProcess = father.createInstance();
			
			// pradar-attend starten
			String call = father.getIni().get("apps", "pradar-attend") + " -instance " + newProcess.getInfilebinary(); 
			father.log("info", "calling: "+call);
			
			try
			{
				java.lang.Process sysproc = Runtime.getRuntime().exec(call);
			}
			catch (IOException e)
			{
				father.log("error", e.getMessage());
			}

			shell.dispose();
		}
	};

	/**
	 * listener for Selections in of button 'create and start'
	 */
	SelectionAdapter listenerButtonCreateAndStart = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button show wurde gedrueckt");
			father.createInstance();
			
			// kurz schlafen damit der schreibvorgang beendet werden kann
//			try
//			{
//				Thread.sleep(100);
//			} catch (InterruptedException e1)
//			{
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

			father.startInstance();
			shell.dispose();
		}
	};

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
