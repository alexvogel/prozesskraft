package de.caegroup.gui.process.insight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.caegroup.pmodel.PmodelPartUi1;
import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class PIInsightCreator
{
	private Process process;
	private PmodelPartUi1 father;
	private Composite parent;
	
	private Composite composite;
	private ScrolledComposite sc;
	
//	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
//	
	public PIInsightCreator(PmodelPartUi1 father, Composite parent, Process process)
	{
		this.parent = parent;
		this.process = process;
		this.father = father;

		sc = new ScrolledComposite(this.parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
//		sc.setAlwaysShowScrollBars(true);
		
		composite = new Composite(sc, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		composite.setLayoutData(gd_composite);
		GridLayout gridLayoutProcessBereich = new GridLayout(2, true);
		gridLayoutProcessBereich.marginBottom = 0;
		gridLayoutProcessBereich.marginTop = 0;
		gridLayoutProcessBereich.marginLeft = 0;
		gridLayoutProcessBereich.marginRight = 0;
//		gridLayoutStepBereich.horizontalSpacing = 0;
//		gridLayoutStepBereich.verticalSpacing = 0;
		composite.setLayout(gridLayoutProcessBereich);
		
		sc.setContent(composite);
//		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		composite.layout();

		this.createControls(composite);
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Composite createControls(Composite composite)
	{
		Composite compositeInfoAction = new Composite(composite, SWT.NONE);
		GridData gd_compositeInfoAction = new GridData(SWT.FILL, SWT.NONE, true, false,2, 1);

		compositeInfoAction.setLayoutData(gd_compositeInfoAction);
		
		GridLayout gridLayout_InfoAction = new GridLayout(2, true);
		gridLayout_InfoAction.marginBottom = 0;
		gridLayout_InfoAction.marginTop = 0;
		gridLayout_InfoAction.marginLeft = 0;
		gridLayout_InfoAction.marginRight = 0;
		compositeInfoAction.setLayout(gridLayout_InfoAction);

		// oben-links stehen textinformationen
		Composite compositeInfo = new Composite(compositeInfoAction, SWT.NONE);
		GridData gd_compositeInfo = new GridData(SWT.FILL, SWT.FILL, true, true,1, 1);
		compositeInfo.setLayoutData(gd_compositeInfo);

		GridLayout gridLayout_Info = new GridLayout(1, true);
		gridLayout_Info.marginBottom = 0;
		gridLayout_Info.marginTop = 0;
		gridLayout_Info.marginLeft = 0;
		gridLayout_Info.marginRight = 0;
		compositeInfo.setLayout(gridLayout_Info);

		// processName
		Label label_processName1 = new Label(compositeInfo, SWT.NONE);
		label_processName1.setText("process: "+this.process.getName());
		label_processName1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		// lastTouchByManager
		Label label_lastTouch1 = new Label(compositeInfo, SWT.NONE);
		label_lastTouch1.setText("last touch: "+this.process.getTouchAsString());

		// processStatus
		Label label_processStatus1 = new Label(compositeInfo, SWT.NONE);
		label_processStatus1.setText("status: "+this.process.getStatus());

		// oben-rechts sind buttons angeordnet
		Composite compositeAction = new Composite(compositeInfoAction, SWT.NONE);
		GridData gd_compositeButtons = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		compositeAction.setLayoutData(gd_compositeButtons);

		GridLayout gridLayout_Action = new GridLayout(3, true);
		gridLayout_Action.marginBottom = 0;
		gridLayout_Action.marginTop = 0;
		gridLayout_Action.marginLeft = 0;
		gridLayout_Action.marginRight = 0;
		compositeAction.setLayout(gridLayout_Action);

		Button buttonFileBrowser = new Button(compositeAction, SWT.NONE);
		buttonFileBrowser.setText("browse");
		buttonFileBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonFileBrowser.setToolTipText("open process directory with a filebrowser");
		buttonFileBrowser.addSelectionListener(listener_button_browse);

		Button buttonReset = new Button(compositeAction, SWT.NONE);
		buttonReset.setText("reset");
		buttonReset.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonReset.setToolTipText("reset process to initial state");
		buttonReset.addSelectionListener(listener_button_reset);

		Button buttonClone = new Button(compositeAction, SWT.NONE);
		buttonClone.setText("clone");
		buttonClone.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonClone.setToolTipText("clone process");
		buttonClone.addSelectionListener(listener_button_clone);


		Label labelDummy3 = new Label(compositeAction, SWT.NONE);

		Label labelDummy4 = new Label(compositeAction, SWT.NONE);

		Label labelDummy5 = new Label(compositeAction, SWT.NONE);

		Label labelDummy6 = new Label(compositeAction, SWT.NONE);

		Label labelDummy7 = new Label(compositeAction, SWT.NONE);

		Label labelDummy8 = new Label(compositeAction, SWT.NONE);

//		// instanceFile
//		Label label_instanceDirectory1 = new Label(fieldComposite, SWT.NONE);
//		label_instanceDirectory1.setText("instance file: ");
//
//		Label label_instanceDirectory2 = new Label(fieldComposite, SWT.NONE);
//		label_instanceDirectory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		if ((new java.io.File(this.process.getInfilebinary()).getParent()) != null)
//		{
//			label_instanceDirectory2.setText(new java.io.File(this.process.getInfilebinary()).getAbsolutePath());
//		}
//		else
//		{
//			label_instanceDirectory2.setText("unknown");
//		}

//		// rootDirectory
//		Label label_rootDirectory1 = new Label(fieldComposite, SWT.NONE);
//		label_rootDirectory1.setText("root directory: ");
//		
//		Label label_rootDirectory2 = new Label(fieldComposite, SWT.NONE);
//		label_rootDirectory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		if ((new java.io.File(this.process.getRootdir()).getParent()) != null)
//		{
//			label_rootDirectory2.setText(new java.io.File(this.process.getRootdir()).getAbsolutePath());
//		}
//		else
//		{
//			label_rootDirectory2.setText("unknown");
//		}

//		// definitionDirectory
//		Label label_definitionDirectory1 = new Label(fieldComposite, SWT.NONE);
//		label_definitionDirectory1.setText("definition directory: ");
//		
//		Label label_definitionDirectory2 = new Label(fieldComposite, SWT.NONE);
//		label_definitionDirectory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		if ((new java.io.File(this.process.getInfilexml()).getParent()) != null)
//		{
//			label_definitionDirectory2.setText((new java.io.File(this.process.getInfilexml()).getParent()));
//		}
//		else
//		{
//			label_definitionDirectory2.setText("unknown");
//		}

		// erstellen eines table fuer das Prozess-Logging
//		new PILogGui(composite, process);

		// databinding
//		initDataBindingsProcess();

		return parent;
	}

	SelectionAdapter listener_button_browse = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			java.io.File stepDir = new java.io.File(process.getRootdir());
			if(!stepDir.exists())
			{
				process.log("error", "directory does not exist: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.isDirectory())
			{
				process.log("error", "is not a directory: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.canRead())
			{
				process.log("error", "cannot read directory: "+stepDir.getAbsolutePath());
			}
			
			else
			{
				String call = father.getIni().get("apps", "filebrowser") + " " + stepDir.getAbsolutePath(); 
				process.log("info", "calling: "+call);
				
				try
				{
					java.lang.Process sysproc = Runtime.getRuntime().exec(call);
				}
				catch (IOException e)
				{
					father.log("error", e.getMessage());
				}
			}

		}
	};

	SelectionAdapter listener_button_reset = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if(process.getStatus().equals("rolling"))
			{
				reset_decline();
			}
			else
			{
				reset_execute();
			}
		}
	};

	private void reset_decline()
	{
		Shell messageShell = new Shell();
		MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_CANCEL | SWT.CANCEL);
//		confirmation.setText("please confirm");
		String message = "";
		message += "you have to stop instance before resetting.\n";

		confirmation.setMessage(message);

		// open confirmation and wait for user selection
		confirmation.open();
//		System.out.println("returnCode is: "+returnCode);

		messageShell.dispose();
	}

	private void reset_execute()
	{
		Shell messageShell = new Shell();
		MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		confirmation.setText("please confirm");
		String message = "";

		message += "WARNING\n";
		message += "you are about to reset all steps of this instance.\n";
		message += "aggregated data (variables, files) will be deleted, all produced files will be erased from the filesystem.\n\n";
		message += "do you really want to reset all steps?";

		confirmation.setMessage(message);

		// open confirmation and wait for user selection
		int returnCode = confirmation.open();
//		System.out.println("returnCode is: "+returnCode);

		// ok == 32
		if (returnCode == 32)
		{
			// den step resetten und alle von diesem step abhaengigen steps
			process.resetStep(process.getRootstepname());

			process.writeBinary();
			
			// den update anstossen
			father.refreshAppletAndUi();

		}
		messageShell.dispose();
	}

	SelectionAdapter listener_button_clone = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			Shell messageShell = new Shell();
			MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			confirmation.setText("please confirm");
			String message = "";

			message += "you are about to clone this instance.\n";
			message += "a full copy of this process instance with all its nested instances and all associated files will be made.\n\n";
			message += "do you really want to clone?";

			confirmation.setMessage(message);

			// open confirmation and wait for user selection
			int returnCode = confirmation.open();
//			System.out.println("returnCode is: "+returnCode);

			// ok == 32
			if (returnCode == 32)
			{
				father.log("info", "cloning process");

				// bisherigen process klonen
				Process clonedProcess = process.clone();

				// den datenbaum umkopieren
				try
				{
					father.log("info", "copying directory tree: source="+process.getRootdir()+", target="+clonedProcess.getRootdir());
					FileUtils.copyDirectory(new java.io.File(process.getRootdir()), new java.io.File(clonedProcess.getRootdir()), true);

					// speichern des geklonten prozesses in das neue verzeichnis (dabei wird das alte pmb ueberschrieben)
					clonedProcess.setOutfilebinary(clonedProcess.getRootdir() + "/" + "process.pmb");
					clonedProcess.writeBinary();
					
					// den aufruf zusammenstellen und
					// starten von pmodel, mit der angabe des neuen pmb
					ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(father.getIni().get("apps", "pmodel-gui").split(" ")));

					// die sonstigen argumente hinzufuegen
					processSyscallWithArgs.add("-instance");
					processSyscallWithArgs.add(clonedProcess.getRootdir() + "/" + "process.pmb");

					father.log("info", "calling: "+StringUtils.join(processSyscallWithArgs, " "));
					
					try
					{
						java.lang.Process pqq = Runtime.getRuntime().exec(processSyscallWithArgs.toArray(new String[processSyscallWithArgs.size()]));
					}
					catch (IOException e)
					{
						father.log("error", e.getMessage());
					}

				}
				// falls directoryCopy schief laeuft, soll das clonen rueckabgewickelt werden
				catch (IOException e)
				{
					process.log("error", "copying of directory tree failed -> cloning failed. deleting all copied data.");
					process.log("error", e.getMessage()+"\n"+Arrays.toString(e.getStackTrace()));

//					try
//					{
						father.log("warn", "delete this directory by hand: "+clonedProcess.getRootdir());
						// FileUtils.deleteDirectory(new java.io.File(clonedProcess.getRootdir()));
//					}
//					catch (IOException e1)
//					{
//						process.log("error", "deleting of half copied directory tree failed -> chaos arises.");
//						process.log("error", e1.getMessage()+"\n"+Arrays.toString(e1.getStackTrace()));
//					}
				}

			}
		}
	};


	
	//	protected DataBindingContext initDataBindingsProcess()
//	{
//		DataBindingContext bindingContextProcess = new DataBindingContext();
//		//
//		IObservableValue targetObservableStatus = WidgetProperties.text().observe(label_processStatus2);
//		IObservableValue modelObservableStatus = BeanProperties.value("status").observe(process);
//		bindingContextProcess.bindValue(targetObservableStatus, modelObservableStatus, null, null);
//		//
//		IObservableValue targetObservableTouch = WidgetProperties.text().observe(label_lastTouch2);
//		IObservableValue modelObservableTouch = BeanProperties.value("touchAsString").observe(process);
//		bindingContextProcess.bindValue(targetObservableTouch, modelObservableTouch, null, null);
//		//
//		return bindingContextProcess;
//	}


}
