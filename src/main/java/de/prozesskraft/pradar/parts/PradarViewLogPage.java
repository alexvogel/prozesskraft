package de.prozesskraft.pradar.parts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import de.prozesskraft.pradar.Entity;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.core.databinding.beans.PojoProperties;


public class PradarViewLogPage
{
	private PradarPartUi3 parentData;
	private CTabFolder folder;
	private CTabItem tabItem;
	private StyledText textWidget;
	private Label labelInfo;
	private Entity entity;
	private String tabName;
	private int lineCount = 0;
	
	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarViewLogPage()
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
		folder = new CTabFolder(composite, SWT.BORDER);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		folder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		createControls(folder);
	}

	/**
	 * constructor als EntryPoint fuer PradarPartUi3
	 */
	public PradarViewLogPage(CTabFolder folder, PradarPartUi3 data, Entity entity, String tabName)
	{
		this.parentData = data;
		this.folder = folder;
		this.entity = entity;
		this.tabName = tabName;
		createControls(this.folder);
		refresh();
	}
	
	private void createControls(CTabFolder folder)
	{
		// ein tabItem fuer das textfile mit eingebetteten composite erzeugen
		tabItem = new CTabItem(folder, SWT.CLOSE);
		tabItem.setText(this.tabName);
		tabItem.setToolTipText(entity.getResource());

		Composite composite_tabItem_logtext = new Composite(folder, SWT.NONE);
		composite_tabItem_logtext.setLayout(new GridLayout(1, false));
		GridData gd_composite_logtext = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_composite_logtext.heightHint = 390;
		gd_composite_logtext.minimumWidth = 10;
		gd_composite_logtext.minimumHeight = 10;
		composite_tabItem_logtext.setLayoutData(gd_composite_logtext);
		
		tabItem.setControl(composite_tabItem_logtext);
		
		labelInfo = new Label(composite_tabItem_logtext, SWT.NONE);
		labelInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		textWidget = new StyledText(composite_tabItem_logtext, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		textWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

	}
	
	public void refresh()
	{
		String content;
		
		labelInfo.setText(new Timestamp(System.currentTimeMillis()).toString()+"  ->  "+entity.getResource());
		
		try
		{
			content = readFile(this.entity.getResource());
			String[] lines = content.split("\\n");
			
			// falls das logfile gekuerzt wurde, soll es komplett neu eingelesen werden
			if (lineCount > lines.length)
			{
				lineCount = 0;
			}
			
			for (; lineCount < lines.length; lineCount++)
			{
				textWidget.append(lines[lineCount]+"\n");
				if (lines[lineCount].matches(".*([^\\w]|^)(warn|WARN)([^\\w]|$).*") || lines[lineCount].matches(".*([^\\w]|^)(warning|WARNING)([^\\w]|$).*"))	{	textWidget.setLineBackground(lineCount, 1, parentData.colorLogWarn);}
				if (lines[lineCount].matches(".*([^\\w]|^)(error|ERROR)([^\\w]|$).*"))	{	textWidget.setLineBackground(lineCount, 1, parentData.colorLogError);}
				if (lines[lineCount].matches(".*([^\\w]|^)(fatal|FATAL)([^\\w]|$).*"))	{	textWidget.setLineBackground(lineCount, 1, parentData.colorLogFatal);}
//				if (lines[y].matches(".*([^\\w]|^)(info|INFO)([^\\w]|$).*"))	{	widget.setLineBackground(y, 1, colorLogInfo);}
//				System.out.println("zeilennummer: "+lineCount);
			}
			
			textWidget.setTopIndex(textWidget.getLineCount()-1);

		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * reads the content of a file
	 * @param String pathToFile
	 * @return String contentOfFile
	 */
	public String readFile(String pathToFile) throws IOException
	{
		BufferedReader reader = new BufferedReader( new FileReader (pathToFile));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		
		while ( ( line = reader.readLine()) != null)
		{
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		
		reader.close();
		
		return stringBuilder.toString();
	}
	
	public String getTabName()
	{
		return this.tabName;
	}
}
