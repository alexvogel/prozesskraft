package de.caegroup.gui.step.insight;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;


//import de.caegroup.pradar.Entity;
import de.caegroup.process.File;
import de.caegroup.process.Log;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;

public class SIVariableGui
{
	private Composite parent;
	private Step step;
	
	TableViewer viewer;
//	Composite composite;

//	ArrayList<VariableGui> variableGui = new ArrayList<VariableGui>();
//	ArrayList<FileGui> fileGui = new ArrayList<FileGui>();

	public SIVariableGui(Composite parent, Step step)
	{
		this.parent = parent;
		this.step = step;
		Composite composite = new Composite(this.parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));

		createControls(composite);
	}

	public void createControls(Composite composite)
	{
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		FontData[] fD = table.getFont().getFontData();
		fD[0].setHeight(8);
		table.setFont(new Font(table.getDisplay(), fD[0]));

		TableColumn colKey = new TableColumn(table, SWT.LEFT);
		colKey.setText("key");
		colKey.setToolTipText("key");
		colKey.setWidth(80);
		
		TableColumn colLevel = new TableColumn(table, SWT.LEFT);
		colLevel.setText("value");
		colLevel.setToolTipText("value");
		colLevel.setWidth(150);

		
		viewer.setContentProvider(new MyContentProvider());
		viewer.setLabelProvider(new MyLabelProvider());
		viewer.setInput(step.getVariable());
		
		viewer.addDoubleClickListener(listener_double_click);

		System.out.println("Anzahl ist: "+step.getLog().size());
	}
	
	IDoubleClickListener listener_double_click = new IDoubleClickListener()
	{
		public void doubleClick(DoubleClickEvent event)
		{
			TableViewer viewer = (TableViewer) event.getSource();
			IStructuredSelection thisselection = (IStructuredSelection) viewer.getSelection();
			
			Variable variable = (Variable) thisselection.getFirstElement();

			de.caegroup.gui.step.edit.EditVariable editor = new de.caegroup.gui.step.edit.EditVariable(variable);
		}
	};
	

	
	public class MyContentProvider implements IStructuredContentProvider
	{
		public Object[] getElements(Object inputElement)
		{
			return ((List) inputElement).toArray();
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			// do nothing
		}

		public void dispose()
		{
			// do nothing
		}

	}
	
	public class MyLabelProvider implements ITableLabelProvider
	{
		List listeners = new ArrayList();

		public void addListener(ILabelProviderListener listener)
		{
			// add the listener to my list
			listeners.add(listener);
		}

		public void dispose()
		{
			// nothing to dispose
			
		}

		public boolean isLabelProperty(Object element, String property)
		{
			// standard
			return false;
		}

		public void removeListener(ILabelProviderListener listener)
		{
			// remove the listener from my list
			listeners.remove(listener);
		}

		public Image getColumnImage(Object element, int columnIndex)
		{
			// return nothing
			return null;
		}

		public String getColumnText(Object element, int columnIndex)
		{
			Variable v = (Variable) element;
			
			switch(columnIndex)
			{
				case 0:
					return v.getKey();
					
				case 1:
					return v.getValue();
			}
			// should never get here
			return "";
		}
		
	}
	
}
