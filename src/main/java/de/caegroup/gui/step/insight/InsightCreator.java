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

import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class InsightCreator
{
	Step step;
	Composite parent;

	Composite composite;
	ScrolledComposite sc;
	
//	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
//	
	public InsightCreator(Composite parent, Step step)
	{
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
		label.setText("stepname: "+step.getName());
		
		Label label2 = new Label(composite, SWT.NONE);
		label2.setText("status: "+step.getStatus());

		// tabFolder erzeugen
		CTabFolder tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.setTabPosition(SWT.TOP);
//		tabFolder.setTabHeight(30);
//		tabFolder.addSelectionListener(listener_tabFolder_selection);

		// ein tabItem fuer das log erzeugen
		CTabItem tabItem_log = new CTabItem(tabFolder, SWT.NONE);
		tabItem_log.setText("log");					
		
		// erstellen eines composites fuer 'log'
		Composite composite_tabItem_log = new Composite(tabFolder, SWT.NONE);
		composite_tabItem_log.setLayout(new GridLayout(1, false));
		GridData gd_composite_log = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_log.heightHint = 390;
		gd_composite_log.minimumWidth = 10;
		gd_composite_log.minimumHeight = 10;
		composite_tabItem_log.setLayoutData(gd_composite_log);

		// befuellen des composites fuer 'log'
		tabItem_log.setControl(composite_tabItem_log);
		new LogGui(composite_tabItem_log, step);

		// ein tabItem fuer 'files' erzeugen
		CTabItem tabItem_files = new CTabItem(tabFolder, SWT.NONE);
		tabItem_files.setText("files");

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
		new FileGui(composite_tabItem_files, step);

		// ein tabItem fuer 'variables' erzeugen
		CTabItem tabItem_variables = new CTabItem(tabFolder, SWT.NONE);
		tabItem_variables.setText("variables");

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
		new VariableGui(composite_tabItem_variables, step);

//		// wenn nicht 'root'
//		if (!(step.getName().equals(step.getParent().getRootstepname())))
//		{
//			// ein tabItem fuer 'variables' erzeugen
//			CTabItem tabItem_lists = new CTabItem(tabFolder, SWT.NONE);
//			tabItem_lists.setText("lists");
//		}

		tabFolder.setSelection(0);

		return parent;
	}

}
