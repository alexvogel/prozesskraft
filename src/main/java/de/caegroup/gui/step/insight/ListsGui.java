package de.caegroup.gui.step.insight;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.caegroup.process.File;
import de.caegroup.process.Log;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;

public class ListsGui
{
	private Composite parent;
	private Step step;
	
	TableViewer viewer;
//	Composite composite;

//	ArrayList<VariableGui> variableGui = new ArrayList<VariableGui>();
//	ArrayList<FileGui> fileGui = new ArrayList<FileGui>();

	public ListsGui(Composite parent, Step step)
	{
		this.parent = parent;
		this.step = step;
		Composite composite = new Composite(this.parent, SWT.BORDER);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));

		createControls(composite);
	}

	public void createControls(Composite composite)
	{
		// tabFolder erzeugen
		CTabFolder tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.setTabPosition(SWT.TOP);
//		tabFolder.setTabHeight(30);
//		tabFolder.addSelectionListener(listener_tabFolder_selection);

		// fuer jede liste ein tabItem erzeugen
		for(de.caegroup.process.List actualList : step.getList())
		{
			CTabItem tabItem_list = new CTabItem(tabFolder, SWT.NONE);
			tabItem_list.setText(actualList.getName());					
			
			// erstellen eines composites fuer die aktuelle Liste
			Composite composite_tabItem_list = new Composite(tabFolder, SWT.NONE);
			composite_tabItem_list.setLayout(new GridLayout(1, false));
			GridData gd_composite_list = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			gd_composite_list.heightHint = 390;
			gd_composite_list.minimumWidth = 10;
			gd_composite_list.minimumHeight = 10;
			composite_tabItem_list.setLayoutData(gd_composite_list);
	
			// befuellen des composites fuer 'list'
			tabItem_list.setControl(composite_tabItem_list);
			new ListGui(composite_tabItem_list, actualList);
		}
		
		tabFolder.setSelection(0);

	}	
}
