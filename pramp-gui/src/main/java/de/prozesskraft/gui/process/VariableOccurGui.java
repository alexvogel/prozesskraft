package de.prozesskraft.gui.process;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;

public class VariableOccurGui
{

//	int abstandZeilen = 30;
//	Font font_5;

	Composite parent;
	VariableGui parent_variablegui;
	Variable variable;
	
	String key;
	boolean free;
	boolean comboexist;
	boolean buttonexist;

	Composite composite;
	Label label;
	Combo combo = null;
	Button button = null;
	
	ModelData data = new ModelData();
	
	public VariableOccurGui(VariableGui parent_variablegui, Composite parent, Variable variable, String key, boolean free, boolean comboexist, boolean buttonexist)
	{
		this.parent_variablegui = parent_variablegui;
		this.parent = parent;
		this.variable = variable.clone();
		this.variable.setKey(key);
		this.key = key;
		this.free = free;
		this.comboexist = comboexist;
		this.buttonexist = buttonexist;
		
		composite = new Composite(this.parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		composite.setLayoutData(gd_composite);

		// alternative mit griddata
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.horizontalSpacing = 10;
		composite.setLayout(gridLayout);
		
//		composite.setLayout(new FormLayout());

		createControls();
	}
	
	/**
	 * creates the controls
	 */
	public void createControls()
	{
		
//		FontData[] fD = new Label(parent, 0).getFont().getFontData();
//		fD[0].setHeight(5);
//		font_5 = new Font(parent.getDisplay(), fD[0]);
		
		// erstellen eines Label fuer den 'key' der Variable
		Label variableKey = new Label(composite, SWT.RIGHT);
		variableKey.setText(this.key);
		variableKey.setToolTipText(this.key + ": " + this.variable.getDescription());

		// alternative mit griddata
		GridData gd_variableKey = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_variableKey.widthHint = this.parent_variablegui.parent_commitgui.parent_commitcreator.maxBreiteDerSchluessel;
		variableKey.setLayoutData(gd_variableKey);

//		FormData fd_variableKey = new FormData();
//		fd_variableKey.top = new FormAttachment(0, 4);
//		fd_variableKey.right = new FormAttachment(0,this.parent_variablegui.parent_commitgui.parent_commitcreator.maxBreiteDerSchluessel);
////		fd_variableKey.right = new FormAttachment(0,100);
//		variableKey.setLayoutData(fd_variableKey);

		if (buttonexist)
		{
			createButton();
		}
		
		// erstellen der combobox falls noetig
		if (comboexist)
		{
			createCombo();
			if (button != null) {setButtonMinus();}
		}
		
		else
		{
			if (button != null) {setButtonPlus();}
		}

	}
	
	/**
	 * creates a combo
	 */
	private void createCombo()
	{
		if (this.free)
		{
			combo = new Combo(composite, SWT.NONE);
		}
		else
		{
			combo = new Combo(composite, SWT.NONE | SWT.READ_ONLY);
		}

		// databinding nur, falls auch tatsaechlich eine combo box erstellt wird
		// und bevor programmatisch ein wert gesetzt wird
//		DataBindingContext bindingContext = initDataBinding();
		initDataBinding();

		combo.setItems(variable.getChoice().toArray(new String[variable.getChoice().size()]));
		
		combo.setToolTipText(this.variable.getDescription());
//		combo.setMessage(this.variable.getDescription());

		// Default 1: Der erste Wert der Liste
		combo.select(0);
		
		// Default 2: Der Wert, der als Value definiert ist
		combo.select(variable.getChoice().indexOf(variable.getValue()));
		
		// Default 3: Der Wert, der als beim letzten Mal gewählt wurde
		
		
		// mit griddata
		GridData gd_comboVariable;
		if(this.buttonexist) {gd_comboVariable = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);}
		else {gd_comboVariable = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);}
		
		combo.setLayoutData(gd_comboVariable);

//		FormData fd_combo_variable = new FormData();
//		fd_combo_variable.top = new FormAttachment(0, 0);
//		fd_combo_variable.left = new FormAttachment(0, (this.parent_variablegui.parent_commitgui.parent_commitcreator.maxBreiteDerSchluessel)+20);
////		fd_combo_variable.left = new FormAttachment(0, 120);
//		fd_combo_variable.width = 220;
//		combo.setLayoutData(fd_combo_variable);
		
		composite.layout();
		
		comboexist = true;
		
	}
	
	/**
	 * creates a neutral button
	 */
	private void createButton()
	{
		button = new Button(composite, SWT.NONE);

		// alternative mit griddata
		GridData gd_button = new GridData(SWT.FILL, SWT.TOP, false, false, 2, 1);
		button.setLayoutData(gd_button);

//		FormData fd_button = new FormData();
//		fd_button.top = new FormAttachment(0, 0);
//		fd_button.left = new FormAttachment(0, (this.parent_variablegui.parent_commitgui.parent_commitcreator.maxBreiteDerSchluessel)+20+220);
////		fd_button.left = new FormAttachment(0, 340);
//		fd_button.width = 25;
//		button.setLayoutData(fd_button);
	}
	
	/**
	 * sets existent button to 'minus'
	 */
	private void setButtonMinus()
	{
		button.setText("-");
		button.removeSelectionListener(listener_plus);
		button.addSelectionListener(listener_minus);
	}

	/**
	 * sets existent button to 'plus'
	 */
	private void setButtonPlus()
	{
		button.setText("+");
		button.removeSelectionListener(listener_minus);
		button.addSelectionListener(listener_plus);
	}

//	/**
//	 * deletes the combobox
//	 */
//	public void deleteCombo()
//	{
//		combo.dispose();
//	}
//	
	/**
	 * listener for Selections of button '+'
	 */
	SelectionAdapter listener_plus = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent e)
		{
			createCombo();
			parent_variablegui.add();
			setButtonMinus();
			
		}
	};

	/**
	 * listener for Selections of button '-'
	 */
	SelectionAdapter listener_minus = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent e)
		{
//			deleteCombo();
			remove();
//			setButtonPlus();
		}
	};

	/**
	 * remove this
	 */
	private void remove()
	{
		parent_variablegui.remove(this);
		disposeAllWidgets();
	}

	/**
	 * remove this
	 */
	private void disposeAllWidgets()
	{
		if(this.label != null) {this.label.dispose();}
		if(this.combo != null) {this.combo.dispose();}
		if(this.button != null) {this.button.dispose();}
		if(this.composite != null) {this.composite.dispose();}

		parent_variablegui.remove(this);
	}

	/**
	 * databinding
	 */
	protected DataBindingContext initDataBinding()
	{
		// Einrichten der ControlDecoration über dem combofeld
		final ControlDecoration controlDecorationCombo = new ControlDecoration(combo, SWT.LEFT | SWT.TOP);
//		controlDecorationCombo.setDescriptionText("test failed");
		FieldDecoration fieldDecorationError = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecorationCombo.setImage(fieldDecorationError.getImage());
//		FieldDecoration fieldDecorationInfo = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION);
//		controlDecorationCombo.setImage(fieldDecorationInfo.getImage());
		
		// Validator mit Verbindung zur Controldecoration
		IValidator validatorTest = new IValidator()
		{
			public IStatus validate(Object value)
			{
				if (value instanceof String)
				{
					variable.setValue((String)value);
					variable.performAllTests();
					if ( variable.doAllTestsPass() )
					{
						controlDecorationCombo.hide();
						return ValidationStatus.ok();
					}
				}
				controlDecorationCombo.setDescriptionText( variable.getFirstFailedTestsFeedback() );
				controlDecorationCombo.show();
				return ValidationStatus.error("at least one test failed");
			}
		};

		// UpdateStrategy ist: update der werte nur wenn validierung erfolgreich
		UpdateValueStrategy strategyTest = new UpdateValueStrategy();
		strategyTest.setBeforeSetValidator(validatorTest);

		DataBindingContext bindingContext = new DataBindingContext();

		IObservableValue targetObservableContent = WidgetProperties.text().observeDelayed(800, combo);
		IObservableValue modelObservableContent = BeanProperties.value("content").observe(data);
		bindingContext.bindValue(targetObservableContent, modelObservableContent, strategyTest, null);

		return bindingContext;
	}

	/**
	 * gets the actual content of input field
	 * @return Map<String,String> keyValue
	 */
	public Map<String,String> getContent()
	{
		Map<String,String> keyValue = new HashMap<String,String>();
		keyValue.put(this.key, this.data.getContent());
		return keyValue;
	}
	
	/**
	 * commits the actual content of input field to process-object
	 * @param Step step
	 */
	public void commit(Commit commit)
	{
		// committen, wenn sichtbar (unsichtbare gibts bei optionalen parametern)
		if ( comboexist )
		{
			commit.log("debug", "setting the value for variable ("+variable.getKey()+") to the pramp-entry: "+data.getContent());

			Variable newVariable = variable.clone();
			newVariable.setValue(this.data.getContent());

			commit.getParent().addVariable(newVariable);
		}
	}

	public boolean doAllTestsPass()
	{
//		System.out.println("testResult variable '"+this.key+"' "+this.variable.doAllTestsPass());
		if(this.comboexist)
		{
			return this.variable.doAllTestsPass();
		}
		return true;
	}
}
