package de.caegroup.gui.step.edit;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
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

import de.caegroup.process.*;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class EditVariable
{
//	private Variable variable = null;
	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();
	
	private Text textKey = null;
	private Text textValue = null;
	
	private EditVariableModel einstellungen = null;
	
	private Step step = null;
	private Variable variable = null;
	/**
	 * @wbp.parser.entryPoint
	 */
	public EditVariable()
	{
		this.step = new Step();
		this.variable = new Variable();
		variable.setKey("");
		variable.setValue("");
		
		shell.setText("edit variable");
		shell.setSize(425, 162);
		shell.setLayout(new FormLayout());
		shell.setLocation(display.getCursorLocation());
		Composite composite = new Composite(shell, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(0, 129);
		fd_composite.right = new FormAttachment(0, 415);
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0);
		composite.setLayoutData(fd_composite);
		createControls(composite);
	}

	public EditVariable(Step step)
	{
		this.step = step;
		this.variable = new Variable();
		this.variable.setKey("");
		this.variable.setValue("");
		
		try
		{
			shell.setText("edit variable");
			shell.setSize(425, 162);
			shell.setLayout(new FormLayout());
			shell.setLocation(display.getCursorLocation());
			
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
	
	public EditVariable(Step step, Variable variable)
	{
		this.step = step;
		this.variable = variable;

		try
		{
			shell.setText("edit variable");
			shell.setSize(425, 162);
			shell.setLayout(new FormLayout());
			shell.setLocation(display.getCursorLocation());
			
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
		
		textKey = new Text(compositeEntries, SWT.BORDER);
		textKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textKey.setToolTipText("name of variable");
		textKey.setMessage("name of variable");

		Label variableValue = new Label(compositeEntries, SWT.NONE);
		variableValue.setAlignment(SWT.RIGHT);
		variableValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		variableValue.setToolTipText("value of variable");
		variableValue.setText("value");
		
		textValue = new Text(compositeEntries, SWT.BORDER);
		textValue.setToolTipText("value of variable");
		textValue.setMessage("value of variable");
		textValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		btnCancel.addSelectionListener(listenerButtonCancel);

		Button btnDelete = new Button(compositeBtn, SWT.NONE);
		btnDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnDelete.setText("delete");
		btnDelete.addSelectionListener(listenerButtonDelete);
		if(variable.getKey().equals("") && variable.getValue().equals(""))
		{
			btnDelete.setEnabled(false);
		}
			
		
		Button btnEnter = new Button(compositeBtn, SWT.NONE);
		btnEnter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnEnter.setText("enter");
		btnEnter.addSelectionListener(listenerButtonEnter);
		
		// binding
		bindingContextFelder();
		System.out.println("variable-key:   "+variable.getKey());
		System.out.println("variable-value: "+variable.getValue());
		
		// setzen der aktuellen werte der variable in die felder
		textKey.setText(variable.getKey());
		textValue.setText(variable.getValue());
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
			Shell messageShell = new Shell();
			MessageBox confirmation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			confirmation.setText("please confirm");
			
			String message = "do you really want to delete this variable from step "+step.getName()+"\n\n";
			message += "  key:\t\t"+variable.getKey()+"\n";
			message += "value:\t\t"+variable.getValue()+"\n";
			
			confirmation.setMessage(message);
			
			// open confirmation and wait for user selection
			int returnCode = confirmation.open();
//			System.out.println("returnCode is: "+returnCode);

			// ok == 32
			if (returnCode == 32)
			{
				
				ArrayList<Variable> newVariables = new ArrayList<Variable>();
				for(Variable actVariable : step.getVariable())
				{
					// alle beruecksichtigen, die nicht geloescht werden sollen
					if(actVariable != variable)
					{
						newVariables.add(actVariable);
					}
				}
				// neue (um das zu loeschende element) verkuerzte liste wieder in den step schreiben
				step.setVariable(newVariables);
				// auf platte schreiben
				step.getParent().writeBinary();
				messageShell.dispose();
			}
			shell.dispose();
		}
	};
	
	SelectionAdapter listenerButtonEnter = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			System.out.println("variable "+variable.getKey());
			
			boolean addAsNewVariable = true;
			
			// setzen der neuen werte in die variable
			variable.setKey(textKey.getText());
			variable.setValue(textValue.getText());
			
			// falls noch nicht in den variablen des steps existent, soll die variable hinzugefuegt werden
			for(Variable actVariable : step.getVariable())
			{
				// existiert die variable im step bereits, muss sie nicht mehr hinzugefuegt werden
				if(actVariable == variable)
				{
					addAsNewVariable = false;
				}
			}
			
			// wenn 
			if(addAsNewVariable)
			{
				step.addVariable(variable);
			}
			
			// schreiben des prozesse auf platte
			System.out.println("act file: "+step.getParent().getOutfilebinary());
			step.getParent().writeBinary();
			shell.dispose();
		}
	};
	
	protected DataBindingContext bindingContextFelder()
	{
		DataBindingContext bindingContextFelder = new DataBindingContext();
		//
		IObservableValue targetObservableSize = WidgetProperties.text().observe(textKey);
		IObservableValue modelObservableSize = BeanProperties.value("key").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableSize, modelObservableSize, null, null);
		//
		IObservableValue targetObservableFix = WidgetProperties.text().observe(textValue);
		IObservableValue modelObservableFix = BeanProperties.value("value").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableFix, modelObservableFix, null, null);
		//
		return bindingContextFelder;
	}

}
