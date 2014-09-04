package de.caegroup.gui.step.insight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

import de.caegroup.gui.step.edit.EditFile;
import de.caegroup.gui.step.edit.EditVariable;
import de.caegroup.pmodel.PmodelPartUi1;
import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class SIInsightCreator
{
	private Step step;
	private PmodelPartUi1 father;
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
		GridData gd_composite = new GridData();
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
//		Composite actualComposite = new Composite(composite, SWT.NONE);
//		actualComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//		GridLayout gl_actualComposite = new GridLayout(1, false);
//		actualComposite.setLayout(gl_actualComposite);

		Label label = new Label(composite, SWT.NONE);
		label.setText("step: "+step.getName());

		Label label2 = new Label(composite, SWT.NONE);
		label2.setText("status: "+step.getStatus());
		
		// tabFolder erzeugen
		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.setTabPosition(SWT.TOP);
//		tabFolder.setTabHeight(30);
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
		Button buttonAdd = new Button(composite_tabItem_variables, SWT.NONE);
		buttonAdd.setSelection(true);
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_btnNewButton.widthHint = 69;
		buttonAdd.setLayoutData(gd_btnNewButton);
		buttonAdd.setText("add");
		buttonAdd.addSelectionListener(listener_button_add_variable);

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
			new EditVariable(This, step);
		}
	};

	SelectionAdapter listener_button_add_file = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			new EditFile(This, step);
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

	
}
