package de.prozesskraft.gui.step.insight;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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

//import de.caegroup.pradar.Entity;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.gui.step.edit.EditVariable;
import de.prozesskraft.gui.step.edit.ShowLog;

public class SIVariableGui
{
	private SIInsightCreator father;
	private Composite parent;
	private Step step;
	
	private SIVariableGui This = this;
	
	private TableViewer viewer;

//	ArrayList<VariableGui> variableGui = new ArrayList<VariableGui>();
//	ArrayList<FileGui> fileGui = new ArrayList<FileGui>();

	public SIVariableGui(SIInsightCreator father, Composite parent, Step step)
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
		
		// zum setzen der ezilenhoehe
//		table.addListener(SWT.MeasureItem, new Listener()
//			{
//				public void handleEvent(Event event)
//				{
//					event.height = 25;
//				}
//			}
//		);

//		// column Delete
//		TableColumn colDelete = new TableColumn(table, SWT.LEFT);
//		colDelete.setText("");
//		colDelete.setToolTipText("delete");
//		colDelete.setWidth(30);
//		colDelete.setResizable(false);
//		TableViewerColumn colViewDelete = new TableViewerColumn(viewer, colDelete);
//		colViewDelete.setLabelProvider(new ColumnLabelProvider()
//			{
//				// make sure to dispose those button when viewer input changes
//				Map<Object,Button> buttons = new HashMap<Object,Button>();
//				
//				@Override
//				public void update(ViewerCell cell)
//				{
//					TableItem item = (TableItem) cell.getItem();
//					Button button;
//					if(buttons.containsKey(cell.getElement()))
//					{
//						button = buttons.get(cell.getElement());
//					}
//					else
//					{
//						button = new Button((Composite) cell.getViewerRow().getControl(), SWT.NONE);
//						button.setImage(imageDelete);
//						buttons.put(cell.getElement(), button);
//					}
//					button.addSelectionListener(listener_delete_row);
//					TableEditor editor = new TableEditor(item.getParent());
//					editor.grabHorizontal = true;
//					editor.grabVertical = true;
//					editor.setEditor(button, item, cell.getColumnIndex());
//					editor.layout();
//				}
//			}
//		);
		
		// column Key
		TableColumn colKey = new TableColumn(table, SWT.LEFT);
		colKey.setText("key");
		colKey.setToolTipText("key");
		colKey.setWidth(80);
		TableViewerColumn colViewKey = new TableViewerColumn(viewer, colKey);
		colViewKey.setLabelProvider(new ColumnLabelProvider()
			{
				@Override
				public String getText(final Object element)
				{
					Variable variable = (Variable) element;
					return variable.getKey();
				}
			}
		);

		// column Value
		TableColumn colValue = new TableColumn(table, SWT.LEFT);
		colValue.setText("value");
		colValue.setToolTipText("value");
		colValue.setWidth(150);
		TableViewerColumn colViewValue = new TableViewerColumn(viewer, colValue);
		colViewValue.setLabelProvider(new ColumnLabelProvider()
			{
				@Override
				public String getText(final Object element)
				{
					Variable variable = (Variable) element;
					return variable.getValue();
				}
			}
		);
		
		viewer.setContentProvider(new MyContentProvider());
		
		// alternative art einen tableviewer mit daten zu befuellen:
//		viewer.setLabelProvider(new MyLabelProvider());
		
		viewer.setInput(step.getVariable());
		
		viewer.addDoubleClickListener(listener_double_click);
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
				
				Variable variable = (Variable) thisselection.getFirstElement();
				EditVariable editor = new EditVariable(father.getShell(), This, step, variable);
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
//			switch(columnIndex)
//			{
//				case 0:
//					Image imageDelete = new Image(Display.getDefault(), "icons/delete.png");
//					return imageDelete;
//			}
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex)
		{
			Variable v = (Variable) element;
			
			switch(columnIndex)
			{
				case 1:
					return v.getKey();
					
				case 2:
					return v.getValue();
			}
			// should never get here
			return "";
		}
		
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
	
	
	
	
	
}
