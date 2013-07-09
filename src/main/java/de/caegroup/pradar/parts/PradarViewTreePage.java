package de.caegroup.pradar.parts;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import de.caegroup.pradar.Entity;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.core.databinding.beans.PojoProperties;


public class PradarViewTreePage
{
	private DataBindingContext m_bindingContext;
	private static class Sorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			Object item1 = e1;
			Object item2 = e2;
			return 0;
		}
	}
	
	private Tree entityTree;
	private TreeViewer myTreeViewer;
	private PradarPartUi3 parentData;
	private Composite parent;

	private Image img_ampel_lauf_gruen;
	private Image img_ampel_lauf_rot;
	private Image img_ampel_steh_gruen;
	private Image img_ampel_steh_rot;
	
	Entity entity = new Entity();
	
	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarViewTreePage()
	{
		prepareImages();
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
		
		prepareImages();
		
	}
	
	public void createControls(Composite parent)
	{
		entityTree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE);
		entityTree.setSize(1000, 1000);
		entityTree.setLayout(new GridLayout(1, false));
//		GridData gd_entityTree = new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1);
//		gd_entityTree.heightHint = 1000;
//		gd_entityTree.widthHint = 1000;
		entityTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		entityTree.setHeaderVisible(true);
		entityTree.addSelectionListener(listener_selection);
		
		entityTree.setLinesVisible(true);
		FontData[] fD = entityTree.getFont().getFontData();
		fD[0].setHeight(8);
		entityTree.setFont(new Font(entityTree.getDisplay(), fD[0]));

		TreeColumn columnId = new TreeColumn(entityTree, SWT.LEFT);
		columnId.setAlignment(SWT.LEFT);
		columnId.setText("id");
		columnId.setWidth(20);

		TreeColumn columnId2 = new TreeColumn(entityTree, SWT.LEFT);
		columnId2.setAlignment(SWT.LEFT);
		columnId2.setText("id2");
		columnId2.setWidth(220);
		
		TreeColumn columnProcess = new TreeColumn(entityTree, SWT.RIGHT);
		columnProcess.setAlignment(SWT.LEFT);
		columnProcess.setText("process");
		columnProcess.setWidth(120);
		
		TreeColumn columnActive = new TreeColumn(entityTree, SWT.RIGHT);
		columnActive.setAlignment(SWT.LEFT);
		columnActive.setText("activity");
		columnActive.setToolTipText("activity status");
		columnActive.setWidth(20);
		
		TreeColumn columnProgress = new TreeColumn(entityTree, SWT.RIGHT);
		columnProgress.setAlignment(SWT.LEFT);
		columnProgress.setText("progress");
		columnProgress.setWidth(120);
		
		TreeColumn columnUser = new TreeColumn(entityTree, SWT.RIGHT);
		columnUser.setAlignment(SWT.LEFT);
		columnUser.setText("user");
		columnUser.setWidth(80);
		
		TreeColumn columnHost = new TreeColumn(entityTree, SWT.RIGHT);
		columnHost.setAlignment(SWT.LEFT);
		columnHost.setText("host");
		columnHost.setWidth(95);
		
		TreeColumn columnCheckin = new TreeColumn(entityTree, SWT.RIGHT);
		columnCheckin.setAlignment(SWT.LEFT);
		columnCheckin.setText("checkin");
		columnCheckin.setWidth(125);
		
		TreeColumn columnCheckout = new TreeColumn(entityTree, SWT.RIGHT);
		columnCheckout.setAlignment(SWT.LEFT);
		columnCheckout.setText("checkout");
		columnCheckout.setWidth(125);
		
		TreeColumn columnExitcode = new TreeColumn(entityTree, SWT.RIGHT);
		columnExitcode.setAlignment(SWT.LEFT);
		columnExitcode.setText("exitcode");
		columnExitcode.setWidth(160);
		
//		entityTree.addListener(SWT.PaintItem, new Listener()
//			{
//				public void handleEvent(Event event)
//				{
//					TreeItem item = (TreeItem)event.item;
//					Image trailingImage = (Image)item.getData();
//					if (trailingImage != null)
//					{
//						int x = event.x + event.width + 1;
//						int itemHeigth = entityTree.getItemHeight();
//						int imageHeight = trailingImage.getBounds().height;
//						int y = event.y + (itemHeigth - imageHeight) / 2;
//						event.gc.drawImage(trailingImage, x , y);
//					}
//				}
//			});
		
		
		myTreeViewer = new TreeViewer(entityTree);
		myTreeViewer.setSorter(new Sorter());
		ColumnViewerToolTipSupport.enableFor(myTreeViewer);
		
		myTreeViewer.setContentProvider(new EntityContentProvider());
		myTreeViewer.setLabelProvider(new TableLabelProvider());
//		myTreeViewer.setLabelProvider(new ColumnLabelProvider());

		List<Entity> entities = (List<Entity>) parentData.entities_filtered;
//		entities.add(new Entity());
		myTreeViewer.setInput(entities);
//		myTreeViewer.expandAll();

		myTreeViewer.addDoubleClickListener(listener_double_click);
		
		
		if (parentData.einstellungen.entitySelected != null)
		{
			myTreeViewer.setSelection(new StructuredSelection(parentData.einstellungen.entitySelected), true);
		}
	}
	
	/**
	 * wenn eine zeile markiert wird, soll das entity markiert werden
	 */
	SelectionAdapter listener_selection = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent e)
		{
			TreeItem item = (TreeItem) e.item;
			Entity entity = (Entity)item.getData();
			parentData.einstellungen.entitySelected = entity;
			if (item.getItemCount() > 0)
			{
//				item.setExpanded(true);
			}
		}
	};
	
	IDoubleClickListener listener_double_click = new IDoubleClickListener()
	{
		public void doubleClick(DoubleClickEvent event)
		{
//			TreeViewer viewer = (TreeViewer) event.getSource();
//			IStructuredSelection thisselection = (IStructuredSelection) viewer.getSelection();
//			
//			Entity entity = (Entity) thisselection.getFirstElement();
//
//			String aufruf = "nedit "+entity.getResource();
//			try
//			{
//				java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
//			}
//			catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

		}
	};
	
	// es wird beim selektieren nur ein baum mit einer (1) Unterhierarchie korrekt markiert
	void refresh()
	{
		// alle treeitems durchgehen und den, der mit dem entitySelected uebereinstimmt markieren
		TreeItem[] treeItem = entityTree.getItems();
		for (int x = 0; x < treeItem.length; x++)
		{
			if (treeItem[x].getData().equals(this.parentData.einstellungen.entitySelected))
			{
//				entityTree.setSelection(treeItem[x]);
				entityTree.setSelection(entityTree.getItem(x));
			}
//			else if (treeItem[x].getItemCount() > 0)
//			{
//				System.out.println("itemCount ist: "+treeItem[x].getItemCount());
//				Entity tmp = (Entity)treeItem[x].getData();
//				System.out.println("ProcessName ist: "+tmp.getProcess());
//				TreeItem[] treeItem2 = treeItem[x].getItems();
//				for (int y = 0; y < treeItem2.length; y++)
//				{
//					Entity tmp2 = (Entity)treeItem2[y].getData();
//					System.out.println("is instance of entity? "+ ( tmp2 instanceof Entity ));
//					System.out.println("is instance of: "+ ( tmp2.getClass().toString() ));
//					System.out.println("ProzessName ist: "+tmp2.getProcess());
//					if (treeItem2[y].getData().equals(this.parentData.einstellungen.entitySelected))
//					{
//						entityTree.setSelection(treeItem2[y]);
//					}
//				}
//			}
		}
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
				
				Object parentEntity = null;
				if (filter_entity.getAllMatches(parentData.entities_filtered).size() > 0)
				{
					parentEntity = filter_entity.getAllMatches(parentData.entities_filtered).get(0);
				}
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
			Entity entity = ((Entity) element);
			switch (columnIndex)
			{
				case 3:
				{
					// Das Ampelmaennchen erstellen
					Image img_ampelmaennchen = null;
					if (entity.isActive())
					{
						img_ampelmaennchen = img_ampel_lauf_gruen;
					}
					else
					{
						if (entity.getExitcode().equals("0"))
						{
							img_ampelmaennchen = img_ampel_steh_gruen;
						}
						else
						{
							img_ampelmaennchen = img_ampel_steh_rot;
						}
					}
					return img_ampelmaennchen;
				}	

				
				
				case 4:
				{
					// den progressbalken erstellen
					int breite = 50;
					int hoehe = 10; // of balken

					Image img_balken = new Image(entityTree.getDisplay(), breite, hoehe);
					
					GC gc = new GC(img_balken);
					gc.setBackground(entityTree.getDisplay().getSystemColor(SWT.COLOR_WHITE));
					gc.setForeground(entityTree.getDisplay().getSystemColor(SWT.COLOR_BLACK));
					gc.drawRectangle(0, 0, breite-1, hoehe-1);
					
					float progress = entity.getProgress();
					if (progress >= 0)
					{
						if ( entity.getExitcode().equals("0") || entity.getExitcode().equals("") )
						{
							
							
							gc.setBackground(entityTree.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		//					gc.setForeground(entityTree.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		//					Rectangle rect = new Rectangle(0, 0, (int)(width * entity.getProgress()), height-1);
		//					gc.drawRectangle(rect);
							gc.fillRectangle(1, 1, (int)((breite-2) * entity.getProgress()), hoehe-2);
						}
						else
						{
							gc.setBackground(entityTree.getDisplay().getSystemColor(SWT.COLOR_RED));
							gc.fillRectangle(1, 1, (int)((breite-2) * entity.getProgress()), hoehe-2);
						}
					}

//					Image resultImage = mergeImageHorizontally(img_ampelmaennchen, img_balken);


					return img_balken;
				}
			}			
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex)
		{
			Entity entity = ((Entity) element);
			switch (columnIndex)
			{
				case 0: return entity.getId();
				case 1: return entity.getId2();
				case 2: return entity.getProcess();
//				case 3: return entity.getActive();
				case 4: return entity.getProgressAsString();
				case 5: return entity.getUser();
				case 6: return entity.getHost();
				case 7: return entity.getCheckinAsString();
				case 8: return entity.getCheckoutAsString();
				case 9: return entity.getExitcode();
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
	
	private void prepareImages()
	{
		// skalieren und faerben der Ampelmaennchen
		Image img_ampel_lauf_gruen_org = new Image(entityTree.getDisplay(), "jar:file:ampelmann_lauf_gruen_mc.png");
		Image img_ampel_lauf_rot_org = new Image(entityTree.getDisplay(), "jar:file:ampelmann_lauf_rot_mc.png");
		Image img_ampel_steh_gruen_org = new Image(entityTree.getDisplay(), "jar:file:ampelmann_steh_gruen_mc.png");
		Image img_ampel_steh_rot_org = new Image(entityTree.getDisplay(), "jar:file:ampelmann_steh_rot_mc.png");
		
		int width = 15;
		int height = 15;

		img_ampel_lauf_gruen = setWhiteToTransparent(fillPixelsFromRight(scaleImage(img_ampel_lauf_gruen_org, width, height), 35));
		img_ampel_lauf_rot   = setWhiteToTransparent(fillPixelsFromRight(scaleImage(img_ampel_lauf_rot_org,   width, height), 35));
		img_ampel_steh_gruen = setWhiteToTransparent(fillPixelsFromRight(scaleImage(img_ampel_steh_gruen_org, width, height), 35));
		img_ampel_steh_rot   = setWhiteToTransparent(fillPixelsFromRight(scaleImage(img_ampel_steh_rot_org,   width, height), 35));

	}

	private Image scaleImage(Image image, int width, int height)
	{
		// skalieren das laufmaennchen
		Image scaled = new Image(entityTree.getDisplay(), width, height);
//		System.out.println("param_width: "+width+"   param_height: "+height);
//		System.out.println("org_width: "+image.getBounds().width+"   org_height: "+image.getBounds().height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		gc.dispose();
//		System.out.println("scaled_width: "+scaled.getBounds().width+"   scaled_height: "+scaled.getBounds().height);
		
		return scaled;
	}
	
	private Image fillPixelsFromRight(Image image, int addPixel)
	{
		ImageData imageData = image.getImageData();
		
		ImageData newData = new ImageData(imageData.width + addPixel, imageData.height, imageData.depth, imageData.palette);
		
		for (int i = imageData.x; i < imageData.width + addPixel; i++)
		{
			int j = imageData.y;
			for (; j < imageData.height; j++)
			{
				if (i >= imageData.width)
				{
					newData.setPixel(i, j, 0xFFFFFF);
				}
				else
				{
					newData.setPixel(i, j, imageData.getPixel(i, j));
				}
			}
		}
		
//		
//		
//		Color color = new Color(entityTree.getDisplay(), 255, 255, 255);
		Image newImage = new Image(entityTree.getDisplay(), newData);
//		newImage.setBackground(color);
//		
		return newImage;
	}
	
	private Image setWhiteToTransparent(Image image)
	{
		ImageData iD = image.getImageData();
		int whitePixel = iD.palette.getPixel(new RGB(255, 255, 255));
		iD.transparentPixel = whitePixel;

		return (new Image(entityTree.getDisplay(), iD));
	}
	
	private Image mergeImageHorizontally(Image left, Image right)
	{
		ImageData leftData = left.getImageData();
		ImageData rightData = right.getImageData();
		
		ImageData targetData = new ImageData(leftData.width + rightData.width, Math.max(leftData.height, rightData.height), rightData.depth, rightData.palette);

		int i;
		i = leftData.x;

		for (; i < leftData.width; i++)
		{
			int j = leftData.y;
			for (; j < leftData.height; j++)
			{
				targetData.setPixel(i, j, leftData.getPixel(i, j));
			}
		}
		
		i = leftData.width;
		for (; i < leftData.width + rightData.width; i++)
		{
			int j = rightData.y;
			for (; j < rightData.height; j++)
			{
				targetData.setPixel(i, j, rightData.getPixel(i, j));
			}
		}
		
		return (new Image(entityTree.getDisplay(), targetData));
//		return left;
	}

}
