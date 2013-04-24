package de.caegroup.pradar.parts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import de.caegroup.pradar.Entity;
import org.eclipse.jface.viewers.ViewerSorter;


public class PradarViewTreePage
{
	private static class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			Object item1 = e1;
			Object item2 = e2;
			return 0;
		}
	}
	private TreeViewer myTreeViewer;
	private PradarPartUi3 parentData;
	private Composite parent;
	
	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarViewTreePage()
	{
		Shell shell = new Shell();
		shell.setSize(633, 767);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.heightHint = 390;
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLocation(0, 0);
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer PradarPartUi3
	 */
	public PradarViewTreePage(Composite p, PradarPartUi3 data)
	{
		parent = p;
		parentData = data;
		createControls(parent);
	}
	
	public void createControls(Composite parent)
	{
		Tree entityTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		entityTree.setSize(1000, 1000);
		entityTree.setLayout(new GridLayout(1, false));
		GridData gd_entityTree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		GridData gd_entityTree = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
//		gd_entityTree.heightHint = 1000;
//		gd_entityTree.widthHint = 1000;
		entityTree.setLayoutData(gd_entityTree);

		entityTree.setHeaderVisible(true);
		myTreeViewer = new TreeViewer(entityTree);
		myTreeViewer.setSorter(new Sorter());
		
		
		entityTree.setLinesVisible(true);

		TreeColumn column1 = new TreeColumn(entityTree, SWT.LEFT);
		column1.setAlignment(SWT.LEFT);
		column1.setText("id");
		column1.setWidth(160);
		TreeColumn column2 = new TreeColumn(entityTree, SWT.RIGHT);
		column2.setAlignment(SWT.LEFT);
		column2.setText("process");
		column2.setWidth(150);
		TreeColumn column3 = new TreeColumn(entityTree, SWT.RIGHT);
		column3.setAlignment(SWT.LEFT);
		column3.setText("user");
		column3.setWidth(100);
		TreeColumn column4 = new TreeColumn(entityTree, SWT.RIGHT);
		column4.setAlignment(SWT.LEFT);
		column4.setText("host");
		column4.setWidth(120);
		TreeColumn column5 = new TreeColumn(entityTree, SWT.RIGHT);
		column5.setAlignment(SWT.LEFT);
		column5.setText("checkin");
		column5.setWidth(160);
		TreeColumn column6 = new TreeColumn(entityTree, SWT.RIGHT);
		column6.setAlignment(SWT.LEFT);
		column6.setText("checkout");
		column6.setWidth(160);
		TreeColumn column7 = new TreeColumn(entityTree, SWT.RIGHT);
		column7.setAlignment(SWT.LEFT);
		column7.setText("exitcode");
		column7.setWidth(160);
		
		myTreeViewer.setContentProvider(new EntityContentProvider());
		myTreeViewer.setLabelProvider(new TableLabelProvider());

		List<Entity> entities = (List<Entity>) parentData.entities_filtered;
//		entities.add(new Entity());
		myTreeViewer.setInput(entities);
		myTreeViewer.expandAll();

		myTreeViewer.addDoubleClickListener(listener_double_click);
		
	}
	
	IDoubleClickListener listener_double_click = new IDoubleClickListener()
	{
		public void doubleClick(DoubleClickEvent event)
		{
//			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
			TreeViewer viewer = (TreeViewer) event.getSource();
			IStructuredSelection thisselection = (IStructuredSelection) viewer.getSelection();
			
			Entity entity = (Entity) thisselection.getFirstElement();

			String aufruf = "nedit "+entity.getResource();
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
	};
	

	
	void refresh()
	{
		myTreeViewer.refresh();
	}
	
	
	class EntityContentProvider implements ITreeContentProvider
	{
		
		public Object[] getChildren(Object parentElement)
		{
			if (parentElement instanceof List)
			{
				return ((List<?>) parentElement).toArray();
			}
			if (parentElement instanceof Entity)
			{
				Entity parentEntity = ((Entity) parentElement);
				
				Entity filter_entity = new Entity();
				filter_entity.setParentid(parentEntity.getId());
				
				Object[] entities = filter_entity.getAllMatches(parentData.entities_filtered).toArray();
//				Object[] objects = filter_entity.getAllMatches(parent.entities_filtered).toArray();
				return entities;
			}
			return new Object[0];
		}
		
		public Object getParent(Object element)
		{
			if (element instanceof Entity)
			{
				Entity entity = ((Entity) element);
				Entity filter_entity = new Entity();
				filter_entity.setId(entity.getParentid());
				
				Object parentEntity = filter_entity.getAllMatches(parentData.entities_filtered).get(0);
				return parentEntity;
			}
			return null;
		}
		
		public boolean hasChildren(Object element)
		{
			if (element instanceof Entity)
			{
				Entity entity = ((Entity) element);
				Entity filter_entity = new Entity();
				filter_entity.setParentid(entity.getId());
				
				int amountChildren = filter_entity.getAllMatches(parentData.entities_filtered).size();
				return amountChildren > 0;
			}
			return false;
		}
		
		public Object[] getElements(Object entities)
		{
//			Object[] objects = (Object[]) parentData.entities_filtered.toArray();
			Entity filter_entity = new Entity();
			filter_entity.setParentidAsBoolean(false);
			Object[] objects = filter_entity.getAllMatches(parentData.entities_filtered).toArray();
			return objects;
		}
		
		public void dispose()
		{
			
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{
			
		}
	}
	
	
	class TableLabelProvider implements ITableLabelProvider
	{
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex)
		{
			Entity entity = ((Entity) element);
			switch (columnIndex)
			{
				case 0: return entity.getId();
				case 1: return entity.getProcess();
				case 2: return entity.getUser();
				case 3: return entity.getHost();
				case 4: return entity.getCheckinAsString();
				case 5: return entity.getCheckoutAsString();
				case 6: return entity.getExitcode();
			}
			return null;
		}
		
		public void addListener(ILabelProviderListener listener)
		{
			
		}

		public void dispose()
		{
			
		}
		
		public boolean isLabelProperty(Object element, String property)
		{
			return false;
		}
		
		public void removeListener(ILabelProviderListener listener)
		{
			
		}
		
	}
	
	
}
