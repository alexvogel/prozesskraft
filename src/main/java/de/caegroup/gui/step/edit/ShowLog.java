package de.caegroup.gui.step.edit;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.caegroup.gui.step.insight.SIInsightCreator;
import de.caegroup.gui.step.insight.SIVariableGui;
import de.caegroup.process.*;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class ShowLog
{
	private Object father;
	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();
	
	boolean keyOk = true;
	boolean valueOk = true;
	
	private Log log = null;
	private Text textKey = null;
	private Text textValue = null;
	
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public ShowLog()
	{
		this.log = new Log();
		
		shell.setText("log");
		shell.setSize(425, 162);
		shell.setLayout(new FormLayout());
		shell.setLocation(display.getCursorLocation());
		Composite composite = new Composite(shell, SWT.NONE);

		composite.setLayoutData(new GridData(2, 1));
		createControls(composite);
	}

	public ShowLog(Object father, Log log)
	{
		this.father = father;
		this.log = log;
		
		try
		{
			shell.setText("log");
			shell.setSize(425, 162);
			shell.setLayout(new FormLayout());
			shell.setLocation(display.getCursorLocation());
			
			Composite composite = new Composite(shell, SWT.NONE);

			composite.setLayoutData(new GridData(2, 1));
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
		composite.setLayout(new GridLayout(2, false));
		
		Composite compositeEntries = new Composite(composite, SWT.NONE);
		compositeEntries.setLayout(new FormLayout());

		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(gd_composite);

		// time
		Label time = new Label(compositeEntries, SWT.NONE);
		time.setLayoutData(new GridData(1, 1));
		time.setText("time");
		time.setToolTipText("creation time of log");
		
		Text timeText = new Text(compositeEntries, SWT.NONE);
		timeText.setLayoutData(new GridData(1, 1));
		timeText.setText(this.log.getTimestamp());
		
		// object
		
		
		// log-level


		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Label dummyLabel = new Label(compositeBtn, SWT.NONE);
		dummyLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnClose = new Button(compositeBtn, SWT.NONE);
		btnClose.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnClose.setText("close");
		btnClose.addSelectionListener(listenerButtonClose);
		
	}
	
	SelectionAdapter listenerButtonClose = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
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
