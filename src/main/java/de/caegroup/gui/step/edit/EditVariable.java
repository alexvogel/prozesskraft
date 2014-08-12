package de.caegroup.gui.step.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.caegroup.pmodel.PmodelViewPage;
import de.caegroup.process.*;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class EditVariable
{
	private Variable variable = null;
	Shell shell = new Shell(Display.getCurrent());
	Display display = Display.getCurrent();
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public EditVariable()
	{
		shell.setText("edit/delete variable");
		shell.setSize(425, 162);
		shell.setLayout(new FormLayout());
		Composite composite = new Composite(shell, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(0, 129);
		fd_composite.right = new FormAttachment(0, 415);
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0);
		composite.setLayoutData(fd_composite);
		createControls(composite);
		
	}

	public EditVariable(Variable variable)
	{
		this.variable = variable;

		try
		{
			shell.setSize(425, 162);
			shell.setLayout(new FormLayout());
			Composite composite = new Composite(shell, SWT.NONE);
			FormData fd_composite = new FormData();
			fd_composite.bottom = new FormAttachment(0, 129);
			fd_composite.right = new FormAttachment(0, 415);
			fd_composite.top = new FormAttachment(0);
			fd_composite.left = new FormAttachment(0);
			composite.setLayoutData(fd_composite);
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
		composite.setLayout(new GridLayout(1, false));
		
		Composite compositeEntries = new Composite(composite, SWT.NONE);
		compositeEntries.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		GridLayout entriesLayout = new GridLayout(2, false);
		compositeEntries.setLayout(entriesLayout);
		
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(gd_composite);

		Label variableKey = new Label(compositeEntries, SWT.NONE);
		variableKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		variableKey.setAlignment(SWT.RIGHT);
		variableKey.setText("key");
		variableKey.setToolTipText("name of variable");
		
		Text textKey = new Text(compositeEntries, SWT.BORDER);
		textKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textKey.setToolTipText("name of variable");
		textKey.setMessage("name of variable");

		Label variableValue = new Label(compositeEntries, SWT.NONE);
		variableValue.setAlignment(SWT.RIGHT);
		variableValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		variableValue.setToolTipText("value of variable");
		variableValue.setText("value");
		
		Text textValue = new Text(compositeEntries, SWT.BORDER);
		textValue.setToolTipText("value of variable");
		textValue.setMessage("value of variable");
		textValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(5, true);
		compositeBtn.setLayout(sss);
		
		Button btnEnter = new Button(compositeBtn, SWT.NONE);
		btnEnter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnEnter.setText("enter");
		new Label(compositeBtn, SWT.NONE);

		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		new Label(compositeBtn, SWT.NONE);
		btnCancel.addSelectionListener(listenerButtonCancel);
		
		Button btnDelete = new Button(compositeBtn, SWT.NONE);
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnDelete.setText("delete");
		
	}
	
	
	SelectionAdapter listenerButtonCancel = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			shell.dispose();
		}
	};	
	
}
