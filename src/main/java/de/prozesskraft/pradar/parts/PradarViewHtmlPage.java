package de.prozesskraft.pradar.parts;

import java.sql.Timestamp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.prozesskraft.pradar.Entity;



public class PradarViewHtmlPage
{
	private PradarPartUi3 parentData;
	private CTabFolder folder;
	private CTabItem tabItem;
	private Browser browser;
	private Label labelInfo;
	private Entity entity;
	private String tabName;
	
	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarViewHtmlPage()
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
	public PradarViewHtmlPage(CTabFolder folder, PradarPartUi3 data, Entity entity, String tabName)
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

		Composite composite_tabItem_html = new Composite(folder, SWT.NONE);
		composite_tabItem_html.setLayout(new GridLayout(1, false));
		GridData gd_composite_html = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_composite_html.heightHint = 390;
		gd_composite_html.minimumWidth = 10;
		gd_composite_html.minimumHeight = 10;
		composite_tabItem_html.setLayoutData(gd_composite_html);
		
		tabItem.setControl(composite_tabItem_html);
		
		labelInfo = new Label(composite_tabItem_html, SWT.NONE);
		labelInfo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		browser = new Browser(composite_tabItem_html, SWT.WEBKIT);
		browser.setUrl(this.entity.getResource());
		
	}
	
	public void refresh()
	{
		String content;
		
		labelInfo.setText(new Timestamp(System.currentTimeMillis()).toString()+"  ->  "+entity.getResource());
		
		browser.refresh();
	}
	
	public String getTabName()
	{
		return this.tabName;
	}
}
