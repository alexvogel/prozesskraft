package de.prozesskraft.gui.step.insight;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import de.prozesskraft.pkraft.*;
import de.prozesskraft.gui.step.edit.EditFile;
import de.prozesskraft.gui.step.edit.EditVariable;

public class SIFileGui
{
	private SIInsightCreator father;
	private Composite parent;
	private Step step;
	
	private SIFileGui This = this;

	private TableViewer viewer;
//	Composite composite;

//	ArrayList<VariableGui> variableGui = new ArrayList<VariableGui>();
//	ArrayList<FileGui> fileGui = new ArrayList<FileGui>();

	public SIFileGui(SIInsightCreator father, Composite parent, Step step)
	{
		this.father = father;
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
		colKey.setWidth(80);
		
		TableColumn colSize = new TableColumn(table, SWT.LEFT);
		colSize.setText("size");
		colSize.setWidth(50);
		
		TableColumn colFilename = new TableColumn(table, SWT.LEFT);
		colFilename.setText("filename");
		colFilename.setWidth(150);

		TableColumn colPath = new TableColumn(table, SWT.LEFT);
		colPath.setText("path");
		colPath.setWidth(150);

		viewer.setContentProvider(new MyContentProvider());
		viewer.setLabelProvider(new MyLabelProvider());
		viewer.setInput(step.getFile());
		viewer.addDoubleClickListener(listener_double_click);

		System.out.println("Anzahl ist: "+step.getLog().size());
	}

	IDoubleClickListener listener_double_click = new IDoubleClickListener()
	{
		public void doubleClick(DoubleClickEvent event)
		{
			if(step.getParent().getStatus().equals("rolling"))
			{
				decline();
			}
			else
			{
				TableViewer viewer = (TableViewer) event.getSource();
				IStructuredSelection thisselection = (IStructuredSelection) viewer.getSelection();
				
				File file = (File) thisselection.getFirstElement();
				EditFile editor = new EditFile(father.getShell(), This, step, file);
			}
		}
	};

	private void decline()
	{
		Shell messageShell = new Shell();
		MessageBox confirmation = new MessageBox(messageShell, SWT.ICON_CANCEL | SWT.CANCEL);
//		confirmation.setText("please confirm");
		String message = "";
		message += "you have to stop instance before editing.\n";

		confirmation.setMessage(message);

		// open confirmation and wait for user selection
		confirmation.open();
//		System.out.println("returnCode is: "+returnCode);

		messageShell.dispose();
	}

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
			File f = (File) element;
			
			switch(columnIndex)
			{
				case 0:
					return f.getKey();
					
				case 1:
					return f.getSizeAsString();
					
				case 2:
					return f.getFilename();
					
				case 3:
					return f.getAbsfilename();
			}
			// should never get here
			return "";
		}
		
	}
	
	/**
	 * @return the father
	 */
	public SIInsightCreator getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(SIInsightCreator father) {
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
