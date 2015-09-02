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

public class EditVariable
{
	private Object father;
//	public Shell shell = new Shell(Display.getCurrent());
	public Shell shell = null;
	private Display display = Display.getCurrent();
	
	private Button btnEnter = null;
	boolean keyOk = true;
	boolean valueOk = true;
	
	private Text textKey = null;
	private Text textValue = null;
	
	private EditVariableModel einstellungen = new EditVariableModel();
	
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
		
		shell = new Shell(Display.getCurrent());
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

	public EditVariable(Shell fatherShell, Object father, Step step)
	{
		this.father = father;
		this.step = step;
		this.variable = new Variable();
		this.variable.setKey("");
		this.variable.setValue("");
		
		try
		{
			shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
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
	
	public EditVariable(Shell fatherShell, SIVariableGui father, Step step, Variable variable)
	{
		this.father = father;
		this.step = step;
		this.variable = variable;

		try
		{
			shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
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
		compositeEntries.setLayout(new FormLayout());

		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		compositeEntries.setLayoutData(gd_composite);

		Label variableKey = new Label(compositeEntries, SWT.NONE);
		variableKey.setText("key");
		variableKey.setToolTipText("label of variable");
		
		FormData fd_variableKey = new FormData();
		fd_variableKey.top = new FormAttachment(0, 10);
		fd_variableKey.left = new FormAttachment(0, 8);
		variableKey.setLayoutData(fd_variableKey);

		textKey = new Text(compositeEntries, SWT.BORDER);
		textKey.setToolTipText("key/label of variable");
		textKey.setMessage("key/label of variable");

		FormData fd_textKey = new FormData();
		fd_textKey.top = new FormAttachment(0, 5);
		fd_textKey.left = new FormAttachment(0, 50);
		fd_textKey.right = new FormAttachment(100, -8);
		fd_textKey.width = 190;
		textKey.setLayoutData(fd_textKey);
		
		Label variableValue = new Label(compositeEntries, SWT.NONE);
		variableValue.setText("value");
		variableValue.setToolTipText("value of variable");
		
		FormData fd_variableValue = new FormData();
		fd_variableValue.top = new FormAttachment(0, 45);
		fd_variableValue.left = new FormAttachment(0, 8);
		variableValue.setLayoutData(fd_variableValue);

		textValue = new Text(compositeEntries, SWT.BORDER);
		textValue.setToolTipText("value of variable");
		textValue.setMessage("value of variable");

		FormData fd_textValue = new FormData();
		fd_textValue.top = new FormAttachment(0, 40);
		fd_textValue.left = new FormAttachment(0, 50);
		fd_textValue.right = new FormAttachment(100, -8);
		textValue.setLayoutData(fd_textValue);
		
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
			
		
		btnEnter = new Button(compositeBtn, SWT.NONE);
		btnEnter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnEnter.setText("enter");
		btnEnter.addSelectionListener(listenerButtonEnter);
		
//		System.out.println("variable-key:   "+variable.getKey());
//		System.out.println("variable-value: "+variable.getValue());
		
		// setzen der aktuellen werte der variable in die felder
		textKey.setText(variable.getKey());
		textValue.setText(variable.getValue());
		einstellungen.setKey(variable.getKey());
		einstellungen.setValue(variable.getValue());

		// binding
		bindingContextFelder();
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

				// den update anstossen
				if(father instanceof SIVariableGui)
				{
					((SIVariableGui) father).getFather().getFather().refreshAppletAndUi();
				}
				else if(father instanceof SIInsightCreator)
				{
					((SIInsightCreator) father).getFather().refreshAppletAndUi();
				}

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

			// den update anstossen
			if(father instanceof SIVariableGui)
			{
				((SIVariableGui) father).getFather().getFather().refreshAppletAndUi();
			}
			else if(father instanceof SIInsightCreator)
			{
				((SIInsightCreator) father).getFather().refreshAppletAndUi();
			}
			
			// das kleine fenster schliessen
			shell.dispose();

		}
	};
	
	protected DataBindingContext bindingContextFelder()
	{
		// Einrichten der ControlDecoration über dem textfeld 'key'
		final ControlDecoration controlDecorationKey = new ControlDecoration(textKey, SWT.LEFT | SWT.TOP);
		FieldDecoration fieldDecorationKeyError = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecorationKey.setImage(fieldDecorationKeyError.getImage());

		// Validator mit Verbindung zur Controldecoration
		IValidator validatorKeyNotBlank = new IValidator()
		{
			public IStatus validate(Object value)
			{
				if (value instanceof String)
				{
					variable.setKey((String)value);

					if ( variable.getKey().matches("^.+") )
					{
						keyOk = true;
						controlDecorationKey.hide();
						if(valueOk)
						{
							btnEnter.setEnabled(true);
						}
						return ValidationStatus.ok();
					}
					else
					{
						keyOk = false;
						btnEnter.setEnabled(false);
					}
				}
				controlDecorationKey.setDescriptionText( "type at least one character (a-Z0-9_)" );
				controlDecorationKey.show();
				return ValidationStatus.error("type at least one character (a-Z0-9_)");
			}
		};

		// Einrichten der ControlDecoration über dem textfeld 'value'
		final ControlDecoration controlDecorationValue = new ControlDecoration(textValue, SWT.LEFT | SWT.TOP);
		FieldDecoration fieldDecorationValueError = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecorationValue.setImage(fieldDecorationValueError.getImage());

		// Validator mit Verbindung zur Controldecoration
		IValidator validatorValueNotBlank = new IValidator()
		{
			public IStatus validate(Object value)
			{
				if (value instanceof String)
				{
					variable.setValue((String)value);

					if ( variable.getValue().matches("^.+") )
					{
						valueOk = true;
						controlDecorationValue.hide();
						if(keyOk)
						{
							btnEnter.setEnabled(true);
						}
						return ValidationStatus.ok();
					}
					else
					{
						valueOk = false;
						btnEnter.setEnabled(false);
					}
				}
				controlDecorationKey.setDescriptionText( "type at least one character (a-Z0-9_)" );
				controlDecorationKey.show();
				return ValidationStatus.error("type at least one character (a-Z0-9_)");
			}
		};

		// UpdateStrategy ist: nur nach erfolgreicher validierung des key feldes
		UpdateValueStrategy strategyTestKeyNotBlank = new UpdateValueStrategy();
		strategyTestKeyNotBlank.setBeforeSetValidator(validatorKeyNotBlank);

		// UpdateStrategy ist: nur nach erfolgreicher validierung des path feldes
		UpdateValueStrategy strategyTestValueNotBlank = new UpdateValueStrategy();
		strategyTestValueNotBlank.setBeforeSetValidator(validatorValueNotBlank);

		DataBindingContext bindingContextFelder = new DataBindingContext();
		//
		IObservableValue targetObservableSize = WidgetProperties.text(SWT.Modify).observeDelayed(800, textKey);
		IObservableValue modelObservableSize = BeanProperties.value("key").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableSize, modelObservableSize, strategyTestKeyNotBlank, null);
		//
		IObservableValue targetObservableFix = WidgetProperties.text(SWT.Modify).observeDelayed(800, textValue);
		IObservableValue modelObservableFix = BeanProperties.value("value").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableFix, modelObservableFix, strategyTestValueNotBlank, null);
		//
		IObservableValue targetObservableKeyTooltip = WidgetProperties.tooltipText().observe(textKey);
		IObservableValue modelObservableKeyTooltip = BeanProperties.value("key").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableKeyTooltip, modelObservableKeyTooltip, null, null);
		//
		IObservableValue targetObservableValueTooltip = WidgetProperties.tooltipText().observe(textValue);
		IObservableValue modelObservableValueTooltip = BeanProperties.value("value").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableValueTooltip, modelObservableValueTooltip, null, null);
		//
		return bindingContextFelder;
	}

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
