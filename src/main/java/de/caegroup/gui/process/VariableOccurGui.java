package de.caegroup.gui.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

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

	public VariableOccurGui(VariableGui parent_variablegui, Composite parent, Variable variable, String key, boolean free, boolean comboexist, boolean buttonexist)
	{
		this.parent_variablegui = parent_variablegui;
		this.parent = parent;
		this.variable = variable;
		this.key = key;
		this.free = free;
		this.comboexist = comboexist;
		this.buttonexist = buttonexist;
		
		composite = new Composite(this.parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new FormLayout());
		
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
		Label variableKey = new Label(composite, SWT.NONE);
		variableKey.setText(this.key);

		FormData fd_variableKey = new FormData();
		fd_variableKey.top = new FormAttachment(0, 0);
		fd_variableKey.right = new FormAttachment(0,100);
		variableKey.setLayoutData(fd_variableKey);

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
		
		combo.setItems(variable.getChoice().toArray(new String[variable.getChoice().size()]));
		combo.select(0);
		
		FormData fd_combo_variable = new FormData();
		fd_combo_variable.top = new FormAttachment(0, 0);
		fd_combo_variable.left = new FormAttachment(0, 130);
		fd_combo_variable.width = 200;

		combo.setLayoutData(fd_combo_variable);
		
		composite.layout();
	}
	
	/**
	 * creates a neutral button
	 */
	private void createButton()
	{
		button = new Button(composite, SWT.NONE);
//		button_plus.setFont(font_5);
		
		FormData fd_button = new FormData();
		fd_button.top = new FormAttachment(0, 0);
		fd_button.left = new FormAttachment(0, 350);
		fd_button.width = 25;
		button.setLayoutData(fd_button);
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
	
}
