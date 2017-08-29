package de.prozesskraft.gui.step.edit;

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
import org.eclipse.swt.graphics.Point;
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

import de.prozesskraft.pkraft.*;
import de.prozesskraft.gui.step.insight.SIInsightCreator;
import de.prozesskraft.gui.step.insight.SIVariableGui;

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
	private Shell parentShell = null;
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public ShowLog()
	{
		this.log = new Log();
		
		shell.setText("log");
//		shell.setSize(600, 225);
		shell.setLayout(new GridLayout(1, false));
		shell.setLocation(display.getCursorLocation());
		Composite composite = new Composite(shell, SWT.NONE);

		composite.setLayoutData(new GridData(2, 1));
		createControls(composite);
		
	}

	public ShowLog(Object father, Log log)
	{
		this.father = father;
		this.log = log;
		
		this.parentShell = shell;
		
		try
		{
			shell.setText("log");
			shell.setSize(600, 625);
			
			shell.setLayout(new GridLayout(1, false));
			shell.setLocation(display.getCursorLocation());
			
			Composite composite = new Composite(shell, SWT.FILL | SWT.BORDER);

			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			createControls(composite);
			
//			composite.layout();
//			shell.layout(true, true);
//			final Point newSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//			shell.setSize(newSize);
			//shell.pack(true);
//			shell.layout();
			
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
		composite.setLayout(new GridLayout(1, false));

		Composite compositeEntries = new Composite(composite, SWT.NONE);

		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(gd_composite);

		compositeEntries.setLayout(new GridLayout(2, false));

		// time
		Label time = new Label(compositeEntries, SWT.NONE);
		time.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		time.setText("time");
		time.setToolTipText("when was this log created?");
		
		Text timeText = new Text(compositeEntries, SWT.READ_ONLY);
		timeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		timeText.setText(this.log.getTimestamp());
		
		// object
		Label object = new Label(compositeEntries, SWT.NONE);
		object.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		object.setText("object");
		object.setToolTipText("which object created this log?");
		
		Text objectText = new Text(compositeEntries, SWT.READ_ONLY);
		objectText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		objectText.setText(this.log.getLabel());
			
		// log-level
		Label level = new Label(compositeEntries, SWT.NONE);
		level.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		level.setText("level");
		level.setToolTipText("what is the level of this log?");
		
		Text levelText = new Text(compositeEntries, SWT.READ_ONLY);
		levelText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		levelText.setText(this.log.getLevel());

		// log-message
		Label message = new Label(compositeEntries, SWT.NONE ) ;
		message.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
//		message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		message.setText("message");
		message.setToolTipText("what is the message of this log?");
		
		Text messageText = new Text(compositeEntries, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		messageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		messageText.setText(this.log.getMsg());
		
		// button
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
