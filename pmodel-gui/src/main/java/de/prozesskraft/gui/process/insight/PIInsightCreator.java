package de.prozesskraft.gui.process.insight;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.prozesskraft.pkraft.Process;
import de.prozesskraft.pmodel.PmodelPartUi1;

public class PIInsightCreator
{
	private Process process;
	private PmodelPartUi1 father;
	private Composite parent;
	
	private Composite composite;
	private ScrolledComposite sc;
	
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

		Button buttonDebug = new Button(compositeAction, SWT.NONE);
		buttonDebug.setText("debug");
		buttonDebug.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonDebug.setToolTipText("shows some internal data for debugging purposes");
		buttonDebug.addSelectionListener(listener_button_debug);
		buttonDebug.setEnabled(true);

		new Label(compositeAction, SWT.NONE);
		
		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

		new Label(compositeAction, SWT.NONE);

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
