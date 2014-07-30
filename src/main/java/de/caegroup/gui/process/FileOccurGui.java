package de.caegroup.gui.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.FileDialog;

import de.caegroup.process.Commit;
import de.caegroup.process.File;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class FileOccurGui
{

//	int abstandZeilen = 30;
//	Font font_5;

	Composite parent;
	public FileGui parent_filegui;
	File file;
	
	String key;
	boolean textexist;
	boolean buttonexist;

	Composite composite;
	Label label;
	Text text = null;
	Button button = null;

	ModelData data = new ModelData();

	public FileOccurGui(FileGui parent_filegui, Composite parent, File file, String key, boolean textexist, boolean buttonexist)
	{
		this.parent_filegui = parent_filegui;
		this.parent = parent;
		this.file = file.clone();
		this.file.setKey(key);
		this.key = key;
		this.textexist = textexist;
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
		Label fileKey = new Label(composite, SWT.NONE);
		fileKey.setText(this.key);
		fileKey.setToolTipText(this.key + ": " + this.file.getDescription());

		FormData fd_fileKey = new FormData();
		fd_fileKey.top = new FormAttachment(0, 4);
		fd_fileKey.right = new FormAttachment(0,100);
		fileKey.setLayoutData(fd_fileKey);

		if (buttonexist)
		{
			createButton();
		}

		// erstellen der combobox falls noetig
		if (textexist)
		{
			createFileControls();
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
	private void createFileControls()
	{
		text = new Text(composite, SWT.BORDER);
		text.setToolTipText(this.file.getDescription());
		text.setMessage(this.file.getDescription());
		
		Button fileButton = new Button(composite, SWT.NONE);
		fileButton.setText("...");
		fileButton.addSelectionListener(listener_file_button);

		// Default 3: Der Wert, der beim letzten Mal gewählt wurde
		
		FormData fd_text_file = new FormData();
		fd_text_file.top = new FormAttachment(0, 0);
		fd_text_file.left = new FormAttachment(0, 120);
		fd_text_file.width = 190;
		textexist = true;
		text.setLayoutData(fd_text_file);
		
		FormData fd_file_button = new FormData();
		fd_file_button.top = new FormAttachment(0, 0);
		fd_file_button.left = new FormAttachment(0, 320);
		fd_file_button.width = 20;
		fileButton.setLayoutData(fd_file_button);
		
		composite.layout();
		DataBindingContext bindingContext = initDataBinding();
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
		fd_button.left = new FormAttachment(0, 340);
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
			createFileControls();
			parent_filegui.add();
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
	 * listener for selection of "..."-button
	 * opens a shell to select a directory-path
	 * selection will be echoed in textfield for directory path
	 */
	SelectionAdapter listener_file_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
	        FileDialog dlg = new FileDialog(new Shell());

	        // Set the initial filter path according
	        // to anything they've selected or typed in
	        String initialPathInDialog = "";
	        java.io.File dummyFile = new java.io.File(text.getText());
	        if(dummyFile.isDirectory())
	        {
	        	initialPathInDialog = dummyFile.getAbsolutePath();
	        }
	        else if(dummyFile.isFile())
	        {
	        	// setzen des pfades in dem sich das file befindet
	        	initialPathInDialog = dummyFile.getParent();
	        	// setzen der bereits getroffenen auswahl
		        dlg.setFileName(dummyFile.getAbsolutePath().substring(dummyFile.getAbsolutePath().lastIndexOf("/")));
	        }
	        else
	        {
	        	// was auch immer da schon drinsteht soll als filter versucht werden
	        	initialPathInDialog = parent_filegui.parent_commitgui.parent_commitcreator.filterPath;
	        }
	        dlg.setFilterPath(initialPathInDialog);

	        // Change the title bar text
	        dlg.setText("File Dialog");

	        // Customizable message displayed in the dialog
//	        dlg.setMessage("Select a file");

	        // Calling open() will open and run the dialog.
	        // It will return the selected directory, or
	        // null if user cancels
	        String path = dlg.open();
	        if (path != null) {
	          // Set the text box to the new selection
	        	text.setText(path);
	    		text.setToolTipText(path);
//	        	parent.log("info", "setting instancedirectory: "+dir);
//	        	text_instancedirectory.setText(dir);
	        }
		}
	};
	
	/**
	 * remove this
	 */
	private void remove()
	{
//		parent_filegui.remove(this);
		disposeAllWidgets();
	}
	
	/**
	 * remove this
	 */
	private void disposeAllWidgets()
	{
		if(this.label != null) {this.label.dispose();}
		if(this.text != null) {this.text.dispose();}
		if(this.button != null) {this.button.dispose();}
		if(this.composite != null) {this.composite.dispose();}

		parent_filegui.remove(this);
	}
	
	/**
	 * databinding
	 */
	protected DataBindingContext initDataBinding()
	{
		// Einrichten der ControlDecoration über dem combofeld
		final ControlDecoration controlDecorationCombo = new ControlDecoration(text, SWT.LEFT | SWT.TOP);
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
					file.setAbsfilename((String)value);
					System.out.println("ACTUAL FILENAME IS: "+file.getAbsfilename());
					file.performAllTests();
					if ( file.doAllTestsPass() )
					{
						controlDecorationCombo.hide();

//						// debug
//						controlDecorationCombo.show();
//						controlDecorationCombo.setDescriptionText( file.getAllTestsFeedback() );
//						
						return ValidationStatus.ok();

					}
				}
				controlDecorationCombo.setDescriptionText( file.getFirstFailedTestsFeedback() );
//				System.out.println(file.getFailedTestsFeedback());
				controlDecorationCombo.show();
				return ValidationStatus.error("at least one test failed");
			}
		};

		// UpdateStrategy ist: update der werte nur wenn validierung erfolgreich
		UpdateValueStrategy strategyTest = new UpdateValueStrategy();
//		strategyTest.setBeforeSetValidator(validatorTest);
		strategyTest.setBeforeSetValidator(validatorTest);

		DataBindingContext bindingContext = new DataBindingContext();

		IObservableValue targetObservableContent = WidgetProperties.text(SWT.Modify).observeDelayed(800, text);
		IObservableValue modelObservableContent = BeanProperties.value("content").observe(data);
		bindingContext.bindValue(targetObservableContent, modelObservableContent, strategyTest, null);

		IObservableValue targetObservableContentTooltip = WidgetProperties.tooltipText().observe(text);
		IObservableValue modelObservableInstancedirectoryTooltip = BeanProperties.value("content").observe(data);
		bindingContext.bindValue(targetObservableContentTooltip, modelObservableInstancedirectoryTooltip, null, null);
		//
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
	public void commit(Step step)
	{
		if ( this.data.getContent() != null && (!(this.data.getContent().matches("^$"))))
		{
			// setzen des pfades
			file.setAbsfilename(text.getText());
			step.log("debug", "FileOccurGui.commit: committing " + file.toString() + file.getAbsfilename());
			step.commitFile(file);
//			System.out.println("committingly "+file.getAbsfilename());
		}
		else
		{
			step.log("debug", "data of FileOccurGui.commit is null or ''. text is "+text.getText());
		}
	}

	
	public boolean doAllTestsPass()
	{
		if(this.textexist)
		{
//		System.out.println("testResult file '"+this.key+"' "+this.file.doAllTestsPass());
			return this.file.doAllTestsPass();
		}
		return true;
	}

}
