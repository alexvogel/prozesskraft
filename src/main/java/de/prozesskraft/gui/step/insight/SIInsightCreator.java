package de.prozesskraft.gui.step.insight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import de.prozesskraft.pkraft.File;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.gui.step.edit.CloneStep;
import de.prozesskraft.gui.step.edit.DeleteStep;
import de.prozesskraft.gui.step.edit.EditFile;
import de.prozesskraft.gui.step.edit.EditVariable;
import de.prozesskraft.gui.step.edit.ResetStep;
import de.prozesskraft.pmodel.PmodelPartUi1;

public class SIInsightCreator
{
	private Step step;
	private PmodelPartUi1 father;
	public Shell shell = new Shell(Display.getCurrent());
	private Composite parent;
	public CTabFolder tabFolder;

	private SIInsightCreator This = this;
	
	private Composite composite;
	private ScrolledComposite sc;
	
//	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
//	
	public SIInsightCreator(PmodelPartUi1 father, Composite parent, Step step)
	{
		this.father = father;
		this.parent = parent;
		this.step = step;

		sc = new ScrolledComposite(this.parent, SWT.V_SCROLL | SWT.H_SCROLL);
//		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
//		sc.setAlwaysShowScrollBars(true);
		
		composite = new Composite(sc, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		composite.setLayoutData(gd_composite);

		// alternativ mit FillLayout
//		FillLayout fillLayoutStepBereich = new FillLayout();
//		fillLayoutStepBereich.type = SWT.VERTICAL;
//		composite.setLayout(fillLayoutStepBereich);

		// alternativ mit GridLayout
		GridLayout gridLayoutStepBereich = new GridLayout(2, true);
		gridLayoutStepBereich.marginBottom = 0;
		gridLayoutStepBereich.marginTop = 0;
		gridLayoutStepBereich.marginLeft = 0;
		gridLayoutStepBereich.marginRight = 0;
//		gridLayoutStepBereich.horizontalSpacing = 0;
//		gridLayoutStepBereich.verticalSpacing = 0;
		composite.setLayout(gridLayoutStepBereich);
		
		sc.setContent(composite);
//		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		composite.layout();

		if(step == null)
		{
			Label labelMessage = new Label(composite, SWT.NONE);
			labelMessage.setText("marked Step is null");
		}
		else
		{
			this.createControls(composite);
		}

	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Composite createControls(Composite composite)
	{

		// oberer Bereich fuer die stepdaten
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

		Label labelName = new Label(compositeInfo, SWT.NONE);
		labelName.setText("step: "+step.getName());

		Label labelStatus = new Label(compositeInfo, SWT.NONE);
		labelStatus.setText("status: "+step.getStatus());

		Label labelLoopvar = new Label(compositeInfo, SWT.NONE);
		if(step.getLoopvar() != null)
		{
			labelLoopvar.setText("loopvar: "+step.getLoopvar());
		}
		else
		{
			labelLoopvar.setText("loopvar: <null>");
		}

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
		buttonFileBrowser.setToolTipText("show step-directory with a filebrowser");
		buttonFileBrowser.addSelectionListener(listener_button_browse);

		Button buttonOpen = new Button(compositeAction, SWT.NONE);
		buttonOpen.setText("open");
		buttonOpen.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonOpen.setToolTipText("open subprocess with pmodel");
		buttonOpen.addSelectionListener(listener_button_open);
		if(this.step.getType().equals("process"))
		{
			buttonOpen.setEnabled(true);
		}
		else
		{
			buttonOpen.setEnabled(false);
		}

		Button buttonLog = new Button(compositeAction, SWT.NONE);
		buttonLog.setText(".log");
		buttonLog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonLog.setToolTipText("opens .log (contains stdout/stderr of work command) and .debug (contains internal logging) with an editor");
		buttonLog.addSelectionListener(listener_button_log);
//		if(this.step.getType().equals("process"))
//		{
//			buttonLog.setEnabled(false);
//		}
//		else
//		{
//			buttonLog.setEnabled(true);
//		}

//		Label labelDummy1 = new Label(compositeAction, SWT.NONE);
		Button buttonDelete = new Button(compositeAction, SWT.NONE);
		buttonDelete.setText("delete");
		buttonDelete.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonDelete.setToolTipText("delete this step");
		buttonDelete.addSelectionListener(listener_button_delete);
		if(step.isAFannedMultistep() && !step.getParent().getStatus().equals("working"))
		{
			buttonDelete.setEnabled(true);
		}
		else
		{
			buttonDelete.setEnabled(false);
		}

//		Button buttonClone = new Button(compositeAction, SWT.NONE);
//		buttonClone.setText("clone");
//		buttonClone.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		buttonClone.setToolTipText("clone this step");
//		buttonClone.addSelectionListener(listener_button_clone);
//		if(step.isAFannedMultistep() && !step.getParent().getStatus().equals("working"))
//		{
//			buttonClone.setEnabled(true);
//		}
//		else
//		{
//			buttonClone.setEnabled(false);
//		}

		Button buttonReset = new Button(compositeAction, SWT.NONE);
		buttonReset.setText("reset");
		buttonReset.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonReset.setToolTipText("reset this step to initial state");
		buttonReset.addSelectionListener(listener_button_reset);
		if(step.isRoot()) {buttonReset.setEnabled(false);}
		else if (step.getParent().getStatus().equals("working")) {buttonReset.setEnabled(false);}
		else {buttonReset.setEnabled(true);}

		Button buttonKill = new Button(compositeAction, SWT.NONE);
		buttonKill.setText("kill");
		buttonKill.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonKill.setToolTipText("kill the program that has been started by this step");
		buttonKill.addSelectionListener(listener_button_kill);
		if(step.getParent().getStatus().equals("waiting") || step.isRoot() || step.getSubprocess() != null) {buttonKill.setEnabled(false);}
		else {buttonKill.setEnabled(true);}

		Label labelDummy2 = new Label(compositeAction, SWT.NONE);

		Label labelDummy3 = new Label(compositeAction, SWT.NONE);

		Label labelDummy4 = new Label(compositeAction, SWT.NONE);

		Label labelDummy5 = new Label(compositeAction, SWT.NONE);

		// tabFolder erzeugen
		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.setTabPosition(SWT.TOP);
		tabFolder.setTabHeight(30);
//		tabFolder.addSelectionListener(listener_tabFolder_selection);

		// ein tabItem fuer 'lists' erzeugen
		CTabItem tabItem_lists = new CTabItem(tabFolder, SWT.NONE);
		tabItem_lists.setText("lists");
		tabItem_lists.setToolTipText("lists that have been initialized in step "+step.getName());

		// erstellen eines composites fuer 'lists'
		Composite composite_tabItem_lists = new Composite(tabFolder, SWT.NONE);
		composite_tabItem_lists.setLayout(new GridLayout(1, false));
		GridData gd_composite_lists = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_lists.heightHint = 390;
		gd_composite_lists.minimumWidth = 10;
		gd_composite_lists.minimumHeight = 10;
		composite_tabItem_lists.setLayoutData(gd_composite_lists);

		// befuellen des composites fuer 'lists'
		tabItem_lists.setControl(composite_tabItem_lists);
		new SIListsGui(composite_tabItem_lists, step);

		// ein tabItem fuer 'files' erzeugen
		CTabItem tabItem_files = new CTabItem(tabFolder, SWT.NONE);
		tabItem_files.setText("files");
		tabItem_files.setToolTipText("files which have been committed to step "+step.getName());

		// erstellen eines composites fuer 'files'
		Composite composite_tabItem_files = new Composite(tabFolder, SWT.NONE);
		composite_tabItem_files.setLayout(new GridLayout(1, false));
		GridData gd_composite_files = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_files.heightHint = 390;
		gd_composite_files.minimumWidth = 10;
		gd_composite_files.minimumHeight = 10;
		composite_tabItem_files.setLayoutData(gd_composite_files);

		// befuellen des composites fuer 'files'
		tabItem_files.setControl(composite_tabItem_files);
		new SIFileGui(this, composite_tabItem_files, step);

		// erstellen eines buttons zum hinzufuegen von variables
		Button buttonAddFile = new Button(composite_tabItem_files, SWT.NONE);
		buttonAddFile.setSelection(true);
		GridData gd_btnAddFile = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_btnAddFile.widthHint = 69;
		buttonAddFile.setLayoutData(gd_btnAddFile);
		buttonAddFile.setText("add");
		buttonAddFile.addSelectionListener(listener_button_add_file);
		if(step.getParent().getStatus().equals("rolling"))
		{
			buttonAddFile.setEnabled(false);
		}

	// ein tabItem fuer 'variables' erzeugen
		CTabItem tabItem_variables = new CTabItem(tabFolder, SWT.NONE);
		tabItem_variables.setText("variables");
		tabItem_variables.setToolTipText("variables which have been committed to step "+step.getName());

		// erstellen eines composites fuer 'variables'
		Composite composite_tabItem_variables = new Composite(tabFolder, SWT.NONE);
		composite_tabItem_variables.setLayout(new GridLayout(1, false));
		GridData gd_composite_variables = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_variables.heightHint = 390;
		gd_composite_variables.minimumWidth = 10;
		gd_composite_variables.minimumHeight = 10;
		composite_tabItem_variables.setLayoutData(gd_composite_variables);

		// befuellen des composites fuer 'variables'
		tabItem_variables.setControl(composite_tabItem_variables);
		new SIVariableGui(this, composite_tabItem_variables, step);

		// erstellen eines buttons zum hinzufuegen von variables
		Button buttonAddVariable = new Button(composite_tabItem_variables, SWT.NONE);
		buttonAddVariable.setSelection(true);
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_btnNewButton.widthHint = 69;
		buttonAddVariable.setLayoutData(gd_btnNewButton);
		buttonAddVariable.setText("add");
		buttonAddVariable.addSelectionListener(listener_button_add_variable);
		if(step.getParent().getStatus().equals("rolling"))
		{
			buttonAddVariable.setEnabled(false);
		}

		// ein tabItem fuer das debug erzeugen
		CTabItem tabItem_debug = new CTabItem(tabFolder, SWT.NONE);
		tabItem_debug.setText("debug");					
		tabItem_debug.setToolTipText("the logging in step "+step.getName());
		
		// erstellen eines composites fuer 'debug'
		Composite composite_tabItem_debug = new Composite(tabFolder, SWT.NONE);
		composite_tabItem_debug.setLayout(new GridLayout(1, false));
		GridData gd_composite_debug = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_debug.heightHint = 390;
		gd_composite_debug.minimumWidth = 10;
		gd_composite_debug.minimumHeight = 10;
		composite_tabItem_debug.setLayoutData(gd_composite_debug);

		// befuellen des composites fuer 'debug'
		tabItem_debug.setControl(composite_tabItem_debug);
		new SIDebugGui(composite_tabItem_debug, step);

		tabFolder.setSelection(0);

		return parent;
	}

	SelectionAdapter listener_button_add_variable = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			new EditVariable(shell, This, step);
		}
	};

	SelectionAdapter listener_button_add_file = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			new EditFile(shell, This, step);
		}
	};

	SelectionAdapter listener_button_browse = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			java.io.File stepDir = new java.io.File(step.getAbsdir());
			if(!stepDir.exists())
			{
				father.log("error", "directory does not exist: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.isDirectory())
			{
				father.log("error", "is not a directory: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.canRead())
			{
				father.log("error", "cannot read directory: "+stepDir.getAbsolutePath());
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

	SelectionAdapter listener_button_open = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			java.io.File stepDir = new java.io.File(step.getAbsdir());
			if(!stepDir.exists())
			{
				father.log("error", "directory does not exist: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.isDirectory())
			{
				father.log("error", "is not a directory: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.canRead())
			{
				father.log("error", "cannot read directory: "+stepDir.getAbsolutePath());
			}
			
			else
			{
				// ist der step ein unterprozess, so soll der prozess in einem eigenen pmodelfenster geoeffnet werden
				if(step.getType().equals("process"))
				{
					java.io.File processBinaryFile = new java.io.File(step.getAbsdir() + "/process.pmb");
					
					if(processBinaryFile.exists())
					{
						// ist das pmodel im kontext pkraft geoeffnet?, dann auch das neue pmodel im selben kontext oeffnen
						if(father.getPkraft() != null)
						{
							openInstance(processBinaryFile.getAbsolutePath());
						}
						// ansonsten ein standalone pmodel oeffnen
						else
						{
							// Aufruf taetigen
							try
							{
								String aufruf = father.getIni().get("apps", "pmodel-gui")+" -instance "+processBinaryFile.getCanonicalPath();
								father.log("info", "opening subprocess of step "+step.getName()+" with call: "+aufruf);
								java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
							}
							catch (IOException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else
					{
						father.log("info", "no process.pmb file present for subprocess of step "+step.getName());
					}
				}
			}
		}
	};

	/**
	 * oeffnen einer instance mit pmodel
	 */
	public void openInstance(String pathToInstance)
	{
		// wurde pradar-gui im kontext der gesamtapplication geoeffnet?, dann soll pmodel auch dort geoeffnet werden
		if(this.getFather().getPkraft() != null)
		{
			IPkraftPartUi2 lulu = (IPkraftPartUi2)this.getFather().getPkraft();
			try
			{
				lulu.openInstance(pathToInstance);
			}
			catch(IOException e)
			{
				father.log("error", "error when reading instance file "+pathToInstance);
				father.log("fatal", e.getMessage());
			}
		}

		// wurde pradar standalone geoeffnet, soll pmodel auch standalone geoeffnet werden
		else
		{
			this.getFather().log("debug", "pkraft == null");
			this.getFather().log("info", "opening instance file for inspection");
			String aufruf = this.getFather().getIni().get("apps",  "pmodel") + " -instance "+pathToInstance;
			this.getFather().log("info", "calling " + aufruf);
			
			try
			{
				java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	SelectionAdapter listener_button_log = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			java.io.File logFile = new java.io.File(step.getAbsdir() + "/.log");
			java.io.File debugFile = new java.io.File(step.getAbsdir() + "/.debug");
			String pathLogFile = ""; 
			String pathDebugFile = ""; 
			boolean einesVonBeidenFilesVorhanden = false; 
					
			// verfuegbarkeit des .log files uebrpruefen
			if(!logFile.exists())
			{
				father.log("error", ".log file does not exist: "+logFile.getAbsolutePath());
			}
			else if(!logFile.isFile())
			{
				father.log("error", "is not a file: "+logFile.getAbsolutePath());
			}
			else if(!logFile.canRead())
			{
				father.log("error", "cannot read .log file: "+logFile.getAbsolutePath());
			}
			else
			{
				pathLogFile = logFile.getAbsolutePath();
				einesVonBeidenFilesVorhanden = true;
			}
			
			// verfuegbarkeit des .debug files uebrpruefen
			if(!debugFile.exists())
			{
				father.log("warn", ".debug file does not exist: "+debugFile.getAbsolutePath());
			}
			else if(!debugFile.isFile())
			{
				father.log("warn", "is not a file: "+debugFile.getAbsolutePath());
			}
			else if(!debugFile.canRead())
			{
				father.log("warn", "cannot read .debug file: "+debugFile.getAbsolutePath());
			}
			else
			{
				pathDebugFile = debugFile.getAbsolutePath();
				einesVonBeidenFilesVorhanden = true;
			}
			
			
			
			if(einesVonBeidenFilesVorhanden)
			{
				// ist der step ein unterprozess, so soll der prozess in einem eigenen pmodelfenster geoeffnet werden
				if(step.getType().equals("process"))
				{
					father.log("warn", "step is a subprocess - there is no .log file to open.");
				}
				// ist step kein unterprozess
				else
				{
					String call = father.getIni().get("apps", "editor") + " " + pathDebugFile + " " + pathLogFile; 
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
		}
	};

	SelectionAdapter listener_button_delete = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// die prozess daten aktualisieren
			This.getFather().refreshAppletAndUi();

			// aussteigen falls was net stimmt
			if(step.getParent().getStatus().equals("rolling"))
				//|| !step.isAFannedMultistep() || step.isAFannedMultistepLast())
			{
				Shell messageShell = new Shell();
				MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_CANCEL | SWT.CANCEL);
//				confirmation.setText("please confirm");
				String message = "";
				message += "you have to stop instance before resetting.\n";

				confirmation.setMessage(message);

				// open confirmation and wait for user selection
				confirmation.open();
//				System.out.println("returnCode is: "+returnCode);

				messageShell.dispose();
			}
			else if(!step.isAFannedMultistep())
			{
				Shell messageShell = new Shell();
				MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_CANCEL | SWT.CANCEL);
//				confirmation.setText("please confirm");
				String message = "";
				message += "you only may delete multisteps\n";

				confirmation.setMessage(message);

				// open confirmation and wait for user selection
				confirmation.open();
				messageShell.dispose();
			}
			else if(step.isAFannedMultistepLast())
			{
				Shell messageShell = new Shell();
				MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_CANCEL | SWT.CANCEL);
//				confirmation.setText("please confirm");
				String message = "";
				message += "you must not delete the last multisteps\n";

				confirmation.setMessage(message);

				// open confirmation and wait for user selection
				confirmation.open();
				messageShell.dispose();
			}
			else
			{
//				delete_execute();
				new DeleteStep(shell, This, step);
			}
		}
	};

	SelectionAdapter listener_button_reset = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// den gui update anstossen
			This.getFather().refreshAppletAndUi();

			if(step.getParent().getStatus().equals("rolling"))
			{
				reset_decline();
			}
			else
			{
//				reset_execute();
				new ResetStep(shell, This, step);
			}
		}
	};

	SelectionAdapter listener_button_clone = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			new CloneStep(shell, This, step);
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

//	private void reset_execute()
//	{
//		Shell messageShell = new Shell();
//		MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
//		confirmation.setText("please confirm");
//		String message = "";
//		if(step.isRoot())
//		{
//			message += "WARNING\n";
//			message += "you are about to reset all steps of this instance.\n";
//			message += "aggregated data (variables, files) will be deleted, all produced files will be erased from the filesystem.\n\n";
//			message += "do you really want to reset all steps?";
//		}
//		else
//		{
//			message += "WARNING\n";
//			message += "you are about to reset step "+step.getName()+" and all steps which depend on it.\n";
//			message += "aggregated data (variables, files) will be deleted, all produced files will be erased from the filesystem.\n\n";
//			message += "do you really want to reset step "+step.getName()+" and all its dependencies?";
//		}
//
//		confirmation.setMessage(message);
//
//		// open confirmation and wait for user selection
//		int returnCode = confirmation.open();
////		System.out.println("returnCode is: "+returnCode);
//
//		// ok == 32
//		if (returnCode == 32)
//		{
//			// den step resetten und alle von diesem step abhaengigen steps
//			step.getParent().resetStep(step.getName());
//
//			step.getParent().writeBinary();
//			
//			// den update anstossen
//			father.refreshAppletAndUi();
//
//		}
//		messageShell.dispose();
//	}

	SelectionAdapter listener_button_kill = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			kill_execute();
		}
	};

	private void kill_execute()
	{
		Shell messageShell = new Shell();
		MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_WARNING | SWT.OK | SWT.CANCEL);
		confirmation.setText("please confirm");
		
		String message = "";
		message += "WARNING\n";
		message += "you are about to kill the program that has been started by this step if it is still running.\n";
		message += "the process will most probably run into an error.\n\n";
		message += "do you really want to proceed?";

		confirmation.setMessage(message);

		// open confirmation and wait for user selection
		int returnCode = confirmation.open();
//		System.out.println("returnCode is: "+returnCode);

		// ok == 32
		if (returnCode == 32)
		{
			// den step killen und alle von diesem step abhaengigen steps
			String killString = step.kill();
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

	/**
	 * @return the parent
	 */
	public Composite getParent() {
		return parent;
	}

	/**
	 * @param parent the parent to set
	 */
	public void setParent(Composite parent) {
		this.parent = parent;
	}

	/**
	 * @return the shell
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * @param shell the shell to set
	 */
	public void setShell(Shell shell) {
		this.shell = shell;
	}

	
}
