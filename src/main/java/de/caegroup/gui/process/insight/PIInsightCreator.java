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
		composite.setLayout(new GridLayout(1, false));
		
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
		Composite fieldComposite = new Composite(composite, SWT.NONE);
		fieldComposite.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		GridLayout gl_actualComposite = new GridLayout(2, false);
		fieldComposite.setLayout(gl_actualComposite);
		
		// processName
		Label label_processName1 = new Label(fieldComposite, SWT.NONE);
		label_processName1.setText("process: ");
		
		Label label_processName2 = new Label(fieldComposite, SWT.NONE);
		label_processName2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		label_processName2.setText(this.process.getName());
		
		// instanceDirectory
		Label label_instanceDirectory1 = new Label(fieldComposite, SWT.NONE);
		label_instanceDirectory1.setText("instance file: ");
		
		Label label_instanceDirectory2 = new Label(fieldComposite, SWT.NONE);
		label_instanceDirectory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if ((new java.io.File(this.process.getInfilebinary()).getParent()) != null)
		{
			label_instanceDirectory2.setText(new java.io.File(this.process.getInfilebinary()).getAbsolutePath());
		}
		else
		{
			label_instanceDirectory2.setText("unknown");
		}

		// definitionDirectory
		Label label_definitionDirectory1 = new Label(fieldComposite, SWT.NONE);
		label_definitionDirectory1.setText("definition directory: ");
		
		Label label_definitionDirectory2 = new Label(fieldComposite, SWT.NONE);
		label_definitionDirectory2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if ((new java.io.File(this.process.getInfilexml()).getParent()) != null)
		{
			label_definitionDirectory2.setText((new java.io.File(this.process.getInfilexml()).getParent()));
		}
		else
		{
			label_definitionDirectory2.setText("unknown");
		}
		
		// lastTouchByManager
		Label label_lastTouch1 = new Label(fieldComposite, SWT.NONE);
		label_lastTouch1.setText("last touch: ");
		
		label_lastTouch2 = new Label(fieldComposite, SWT.NONE);
		label_lastTouch2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		label_lastTouch2.setText("unknown");
		
		// processStatus
		Label label_processStatus1 = new Label(fieldComposite, SWT.NONE);
		label_processStatus1.setText("status: ");
		
		label_processStatus2 = new Label(fieldComposite, SWT.NONE);
		label_processStatus2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		label_processStatus2.setText("unknown");

		// erstellen eines table fuer das Prozess-Logging
		new PILogGui(composite, process);

		// databinding
		initDataBindingsProcess();
		
		return parent;
	}
	
	protected DataBindingContext initDataBindingsProcess()
	{
		DataBindingContext bindingContextProcess = new DataBindingContext();
		//
		IObservableValue targetObservableStatus = WidgetProperties.text().observe(label_processStatus2);
		IObservableValue modelObservableStatus = BeanProperties.value("status").observe(process);
		bindingContextProcess.bindValue(targetObservableStatus, modelObservableStatus, null, null);
		//
		IObservableValue targetObservableTouch = WidgetProperties.text().observe(label_lastTouch2);
		IObservableValue modelObservableTouch = BeanProperties.value("touch").observe(process);
		bindingContextProcess.bindValue(targetObservableTouch, modelObservableTouch, null, null);
		//
		return bindingContextProcess;
	}


}
