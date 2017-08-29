package de.prozesskraft.gui.process.insight;

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

import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pmodel.PmodelPartUi1;
import de.prozesskraft.gui.process.edit.*;;

public class PIInsightCreator
{
	private Process process;
	private PmodelPartUi1 father;
	private Composite parent;
	
	private Composite composite;
	private ScrolledComposite sc;
	
	private Shell shell = new Shell(Display.getCurrent());
	private PIInsightCreator This = this;
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

		// processId
		Label label_processId = new Label(compositeInfo, SWT.NONE);
		label_processId.setText("id: "+this.process.getId());

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

		Button buttonBrowse = new Button(compositeAction, SWT.NONE);
		buttonBrowse.setText("browse");
		buttonBrowse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonBrowse.setToolTipText("show instance-directory with a filebrowser");
		buttonBrowse.addSelectionListener(listener_button_browse);
		buttonBrowse.setEnabled(true);

		Button buttonDebug = new Button(compositeAction, SWT.NONE);
		buttonDebug.setText("debug");
		buttonDebug.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonDebug.setToolTipText("shows some internal data for debugging purposes");
		buttonDebug.addSelectionListener(listener_button_debug);
		buttonDebug.setEnabled(true);

		Label labelDummyB = new Label(compositeAction, SWT.NONE);
		
		Button buttonClone = new Button(compositeAction, SWT.NONE);
		buttonClone.setText("clone");
		buttonClone.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonClone.setToolTipText("clone process");
		buttonClone.addSelectionListener(listener_button_clone);
		if(process.getStatus().equals("working")) {buttonClone.setEnabled(false);}
		else{buttonClone.setEnabled(true);}

		Button buttonReset = new Button(compositeAction, SWT.NONE);
		buttonReset.setText("reset");
		buttonReset.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonReset.setToolTipText("reset this instance to initial state");
		buttonReset.addSelectionListener(listener_button_reset);
		if(process.getStatus().equals("working")) {buttonReset.setEnabled(false);}
		else{buttonReset.setEnabled(true);}

		Button buttonKill = new Button(compositeAction, SWT.NONE);
		buttonKill.setText("kill");
		buttonKill.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonKill.setToolTipText("kill all programs that has been started by this process instance");
		buttonKill.addSelectionListener(listener_button_kill);
		if(process.getStatus().equals("working")) {buttonKill.setEnabled(false);}
		else{buttonKill.setEnabled(true);}

		Button buttonMerge = new Button(compositeAction, SWT.NONE);
		buttonMerge.setText("merge");
		buttonMerge.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonMerge.setToolTipText("merge with another instance of the same process");
		buttonMerge.addSelectionListener(listener_button_merge);
		if(process.getStatus().equals("working")) {buttonMerge.setEnabled(false);}
		else{buttonMerge.setEnabled(true);}

		return parent;
	}

	SelectionAdapter listener_button_debug = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			Shell messageShell = new Shell();
			MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_WARNING | SWT.OK);
			confirmation.setText("please confirm");
			
			String message = "";
			message += "some fields of the instance\n";
			message += "id:\t" +process.getId() + "\n";
			message += "id2:\t" +process.getId2() + "\n";
			message += "parentId:\t" +process.getParentid() + "\n";
			message += "cloneGeneration:\t" +process.getCloneGeneration() + "\n";
			message += "cloneDescendant:\t" +process.getCloneDescendant() + "\n";
			message += "clonePerformed:\t" +process.getClonePerformed() + "\n";
			message += "\n";

			confirmation.setMessage(message);

			// open confirmation and wait for user selection
			int returnCode = confirmation.open();
//			System.out.println("returnCode is: "+returnCode);

			// ok == 32
			if (returnCode == 32)
			{
				messageShell.dispose();
			}
			messageShell.dispose();
		}
	};

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
				father.log("info", "calling: "+call);
				
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
				Process clonedProcess = process.cloneWithData(null, null);

				// originalProcess speichern, da auch hier derzaehler veraendert wurde
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
				
				// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
				String call2 = father.getIni().get("apps", "pradar-attend") + " -instance " + clonedProcess.getRootdir() + "/" + "process.pmb"; 
				father.log("info", "calling: "+call2);

				try
				{
					java.lang.Process sysproc = Runtime.getRuntime().exec(call2);
				}
				catch (IOException e)
				{
					father.log("error", e.getMessage());
				}

			}
		}
	};

	SelectionAdapter listener_button_kill = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			kill_execute();
		}
	};

	SelectionAdapter listener_button_merge = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			new MergeProcess(shell, This, process);
		}
	};

	
	
	private void kill_execute()
	{
		Shell messageShell = new Shell();
		MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		confirmation.setText("please confirm");
		
		String message = "";
		message += "WARNING\n";
		message += "you are about to kill all programs that has been started by this process if they are still running.\n";
		message += "the process will most probably run into an error.\n\n";
		message += "do you really want to proceed?";

		confirmation.setMessage(message);

		// open confirmation and wait for user selection
		int returnCode = confirmation.open();
//		System.out.println("returnCode is: "+returnCode);

		// ok == 32
		if (returnCode == 32)
		{
			String killString = process.kill();
			// den prozess killen und alle von diesem step abhaengigen steps
			father.log("info", killString);

		}
		messageShell.dispose();
	}

	/**
	 * @return the father
	 */
	public PmodelPartUi1 getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(PmodelPartUi1 father) {
		this.father = father;
	}


	
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
