package de.caegroup.gui.process.insight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class PIInsightCreator
{
	private Process process;
	private Composite parent;
	private Label label_processStatus2;
	private Label label_lastTouch2;
	
	private Composite composite;
	private ScrolledComposite sc;
	
//	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
//	
	public PIInsightCreator(Composite parent, Process process)
	{
		this.parent = parent;
		this.process = process;

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

		GridLayout gridLayout_Action = new GridLayout(2, true);
		gridLayout_Action.marginBottom = 0;
		gridLayout_Action.marginTop = 0;
		gridLayout_Action.marginLeft = 0;
		gridLayout_Action.marginRight = 0;
		compositeAction.setLayout(gridLayout_Action);

		Button buttonFileBrowser = new Button(compositeAction, SWT.NONE);
		buttonFileBrowser.setText("browse");
		buttonFileBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonFileBrowser.setToolTipText("open step directory with a filebrowser");
//		buttonFileBrowser.addSelectionListener(listener_button_browse);

		Button buttonReset = new Button(compositeAction, SWT.NONE);
		buttonReset.setText("reset");
		buttonReset.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		buttonReset.setToolTipText("reset this step to initial state");
//		buttonReset.addSelectionListener(listener_button_reset);

		Label labelDummy2 = new Label(compositeAction, SWT.NONE);

		Label labelDummy3 = new Label(compositeAction, SWT.NONE);

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
