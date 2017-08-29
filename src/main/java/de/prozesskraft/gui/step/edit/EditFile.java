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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;

public class EditFile
{
	private Object father;
//	public Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
//	public Shell shell = new Shell(SWT.PRIMARY_MODAL | SWT.SHELL_TRIM & (~SWT.RESIZE));
	public Shell shell = null;
	private Display display = Display.getCurrent();
	
	private Text textKey = null;
	private Text textPath = null;
	private Button btnEnter = null;
	
	private boolean pathOk = true;
	private boolean keyOk = true;
	private String entertype = "add";
	
	private EditFileModel einstellungen = new EditFileModel();
	
	private Step step = null;
	private File file = null;
	/**
	 * @wbp.parser.entryPoint
	 */
	public EditFile()
	{
		this.step = new Step();
		this.file = new File();
		file.setKey("");
		file.setRealposition("");

		shell = new Shell(Display.getCurrent());
		shell.setText("edit file");
		shell.setSize(425, 162);
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	public EditFile(Shell fatherShell, Object father, Step step)
	{
		this.father = father;
		this.step = step;
		this.file = new File();
		this.file.setKey("");
		this.file.setRealposition("");
		this.entertype = "add";
				
		try
		{
			shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
//			if(father instanceof SIInsightCreator)
//			{
//				shell = new Shell(((SIInsightCreator)father).getParent().getShell(), SWT.PRIMARY_MODAL);
//			}
//			else if(father instanceof SIFileGui)
//			{
//				shell = new Shell(((SIFileGui)father).getParent().getShell(), SWT.PRIMARY_MODAL);
//			}
			shell.setText("edit file");
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
	
	public EditFile(Shell fatherShell, SIFileGui father, Step step, File file)
	{
		this.father = father;
		this.step = step;
		this.file = file;
		this.entertype = "modify";

		try
		{
			shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
			shell.setText("edit file");
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

		Label fileKey = new Label(compositeEntries, SWT.NONE);
		fileKey.setText("key");
		fileKey.setToolTipText("key/label of file");
		
		FormData fd_fileKey = new FormData();
		fd_fileKey.top = new FormAttachment(0, 10);
		fd_fileKey.left = new FormAttachment(0, 8);
		fileKey.setLayoutData(fd_fileKey);

		textKey = new Text(compositeEntries, SWT.BORDER);
		textKey.setToolTipText("key/label of file");
		textKey.setMessage("key/label of file");

		FormData fd_textKey = new FormData();
		fd_textKey.top = new FormAttachment(0, 5);
		fd_textKey.left = new FormAttachment(0, 50);
		fd_textKey.right = new FormAttachment(100, -8);
		fd_textKey.width = 190;
		textKey.setLayoutData(fd_textKey);
		
		Label filePath = new Label(compositeEntries, SWT.NONE);
		filePath.setText("path");
		filePath.setToolTipText("path of file");
		
		FormData fd_filePath = new FormData();
		fd_filePath.top = new FormAttachment(0, 45);
		fd_filePath.left = new FormAttachment(0, 8);
		filePath.setLayoutData(fd_filePath);

		textPath = new Text(compositeEntries, SWT.BORDER);
		textPath.setToolTipText("full path of file");
		textPath.setMessage("path");

		FormData fd_textPath = new FormData();
		fd_textPath.top = new FormAttachment(0, 40);
		fd_textPath.left = new FormAttachment(0, 50);
		fd_textPath.right = new FormAttachment(100, -35);
		textPath.setLayoutData(fd_textPath);
		
		Button fileButton = new Button(compositeEntries, SWT.NONE);
		fileButton.setText("...");
		fileButton.addSelectionListener(listener_file_button);

		FormData fd_file_button = new FormData();
		fd_file_button.top = new FormAttachment(0, 40);
		fd_file_button.left = new FormAttachment(textPath, 0, SWT.RIGHT);
		fd_file_button.width = 27;
		fd_file_button.height = 27;
		fileButton.setLayoutData(fd_file_button);
		
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
		if(this.entertype.equals("add"))
		{
			btnDelete.setEnabled(false);
		}
		
		btnEnter = new Button(compositeBtn, SWT.NONE);
		btnEnter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnEnter.setText("enter");
		btnEnter.addSelectionListener(listenerButtonEnter);
		
		// setzen der aktuellen werte der variable in die felder
		textKey.setText(file.getKey());
		textPath.setText(file.getAbsfilename());
		einstellungen.setKey(file.getKey());
		einstellungen.setPath(file.getAbsfilename());
		
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
			deleteEntry();
			shell.dispose();
		}
	};

	SelectionAdapter listenerButtonEnter = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			enterEntry();
			shell.dispose();
		}
	};

	/**
	 * den aktuellen eintrag loeschen
	 * die gui refrefhen
	 */
	private void deleteEntry()
	{
		Shell messageShell = new Shell();
		MessageBox confirmation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		confirmation.setText("please confirm");
		
		String message = "do you really want to delete this file from step "+step.getName()+"\n";
		message += "and from filesystem?\n\n";
		message += " key:\t\t"+file.getKey()+"\n";
		message += "path:\t\t"+file.getAbsfilename()+"\n";
		
		confirmation.setMessage(message);
		
		// open confirmation and wait for user selection
		int returnCode = confirmation.open();

		// ok == 32
		if (returnCode == 32)
		{
			// das zu loeschende file vom filesystem entfernen
			if(new java.io.File(file.getAbsfilename()).delete())
			{
				// alle nicht zu loeschenden files umkopieren in ein anderes array
				ArrayList<File> newFiles = new ArrayList<File>();
				for(File actFile : step.getFile())
				{
					// alle, die nicht geloescht werden sollen in einer neuen liste sammeln
					if(actFile != file)
					{
						newFiles.add(actFile);
					}
				}
				
				// neue (um das zu loeschende element) verkuerzte liste wieder in den step schreiben
				step.setFile(newFiles);
				// auf platte schreiben
				step.getParent().writeBinary();

				// loggen und den update anstossen
				if(father instanceof SIFileGui)
				{
					((SIFileGui) father).getFather().getFather().log("info", "file deleted from filesystem: "+file.getAbsfilename());
					((SIFileGui) father).getFather().getFather().refreshAppletAndUi();
				}
				else if(father instanceof SIInsightCreator)
				{
					((SIInsightCreator) father).getFather().log("info", "file deleted from filesystem: "+file.getAbsfilename());
					((SIInsightCreator) father).getFather().refreshAppletAndUi();
				}

			}
			else
			{
				// loggen und den update anstossen
				if(father instanceof SIFileGui)
				{
					((SIFileGui) father).getFather().getFather().log("error", "file not deleted from filesystem: "+file.getAbsfilename());
				}
				else if(father instanceof SIInsightCreator)
				{
					((SIInsightCreator) father).getFather().log("error", "file not deleted from filesystem: "+file.getAbsfilename());
				}
			}
			messageShell.dispose();
		}
	}
	
	/**
	 * den aktuellen eintrag loeschen
	 * den neuen eintragen
	 * die gui refrefhen
	 */
	private void enterEntry()
	{
		System.out.println("enterType="+entertype);
		
		// modify bedeutet: das alte file loeschen und das neue file hineinkopieren
		if(entertype.equals("modify"))
		{
			// bei einem modify muss zuerst das file, das bereits definiert ist, geloescht werden
			deleteEntry();
		}

		// setzen des schluessels
		file.setKey(textKey.getText());
		file.setRealposition(textPath.getText());
		
		// dem step hinzufuegen (weil 'add')
		step.addFile(file);

		// schreiben des prozesse auf platte
		step.getParent().writeBinary();

		// das update der GUI anstossen
		if(father instanceof SIFileGui)
		{
			((SIFileGui) father).getFather().getFather().refreshAppletAndUi();
		}
		else if(father instanceof SIInsightCreator)
		{
			((SIInsightCreator) father).getFather().refreshAppletAndUi();
		}
	}
	
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
//					file.setKey((String)value);

					if ( textKey.getText().matches("^.+") )
					{
						keyOk = true;
						controlDecorationKey.hide();
						if(pathOk)
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
				controlDecorationKey.setDescriptionText( "type at least one character" );
				controlDecorationKey.show();
				return ValidationStatus.error("type at least one character");
			}
		};

		// Einrichten der ControlDecoration über dem textfeld 'path'
		final ControlDecoration controlDecorationPath = new ControlDecoration(textPath, SWT.LEFT | SWT.TOP);
		FieldDecoration fieldDecorationPathError = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecorationPath.setImage(fieldDecorationPathError.getImage());

		// Validator mit Verbindung zur Controldecoration
		IValidator validatorDoesPathExist = new IValidator()
		{
			public IStatus validate(Object value)
			{
				if (value instanceof String)
				{
					File tmpFile = new File();
					tmpFile.setRealposition((String)value);
					Test testFileExist = new Test("doesExist");
					tmpFile.addTest(testFileExist);

					// file.setAbsfilename((String)value);
					// System.out.println("ACTUAL FILENAME IS: "+file.getAbsfilename());
					tmpFile.performAllTests();
					if ( tmpFile.doAllTestsPass() )
					{
						pathOk = true;
						controlDecorationPath.hide();
						if(keyOk)
						{
							btnEnter.setEnabled(true);
						}
						return ValidationStatus.ok();
					}
					else
					{
						pathOk = false;
						btnEnter.setEnabled(false);
					}
				}
				controlDecorationPath.setDescriptionText( file.getFirstFailedTestsFeedback() );
//				System.out.println(file.getFailedTestsFeedback());
				controlDecorationPath.show();
				return ValidationStatus.error("at least one test failed");
			}
		};

		// UpdateStrategy ist: nur nach erfolgreicher validierung des key feldes
		UpdateValueStrategy strategyTestKeyNotBlank = new UpdateValueStrategy();
		strategyTestKeyNotBlank.setBeforeSetValidator(validatorKeyNotBlank);

		// UpdateStrategy ist: nur nach erfolgreicher validierung des path feldes
		UpdateValueStrategy strategyTestDoesPathExist = new UpdateValueStrategy();
		strategyTestDoesPathExist.setBeforeSetValidator(validatorDoesPathExist);

		DataBindingContext bindingContextFelder = new DataBindingContext();
		//
		IObservableValue targetObservableSize = WidgetProperties.text(SWT.Modify).observeDelayed(800, textKey);
		IObservableValue modelObservableSize = BeanProperties.value("key").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableSize, modelObservableSize, strategyTestKeyNotBlank, null);
		//
		IObservableValue targetObservableFix = WidgetProperties.text(SWT.Modify).observeDelayed(800, textPath);
		IObservableValue modelObservableFix = BeanProperties.value("path").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableFix, modelObservableFix, strategyTestDoesPathExist, null);
		//
		IObservableValue targetObservableKeyTooltip = WidgetProperties.tooltipText().observe(textKey);
		IObservableValue modelObservableKeyTooltip = BeanProperties.value("key").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservableKeyTooltip, modelObservableKeyTooltip, null, null);
		//
		IObservableValue targetObservablePathTooltip = WidgetProperties.tooltipText().observe(textPath);
		IObservableValue modelObservablePathTooltip = BeanProperties.value("path").observe(einstellungen);
		bindingContextFelder.bindValue(targetObservablePathTooltip, modelObservablePathTooltip, null, null);

		return bindingContextFelder;
	}

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
	        // auf den pfad des bisherigen files filtern
	        dlg.setFilterPath(new java.io.File(textPath.getText()).getParent());

	        //eigentlich sollte die vormarkierung des bereits gewaehlten files funktionieren - aber geht neta
	        //dlg.setFilterPath(einstellungen.getPath());
	        
	        // Change the title bar text
	        dlg.setText("File Dialog");

	        // Customizable message displayed in the dialog
//	        dlg.setMessage("Select a file");

	        // Calling open() will open and run the dialog.
	        // It will return the selected file, or
	        // null if user cancels
	        String path = dlg.open();
	        if (path != null) {
	          // Set the text box to the new selection
	        	textPath.setText(path);
	    		textPath.setToolTipText(path);
	        }
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
