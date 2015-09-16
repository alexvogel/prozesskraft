package de.prozesskraft.pradar.parts;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
//import java.beans.PropertyChangeSupport;
//import java.beans.PropertyChangeListener;
//import org.eclipse.jface.viewers.TableViewer;
//import org.eclipse.swt.widgets.Combo;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.layout.FormLayout;
//import org.eclipse.swt.layout.FormData;
//import org.eclipse.swt.layout.FormAttachment;
//import processing.core.PApplet;
//import org.eclipse.core.databinding.UpdateValueStrategy;
//import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
//import org.eclipse.core.databinding.beans.PojoProperties;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;
import de.prozesskraft.pradar.Entity;

import de.prozesskraft.pkraft.*;
import de.prozesskraft.pkraft.Process;

public class PradarPartUi3 extends ModelObject
{
	static CommandLine line;
	private DataBindingContext bindingContextFilter;
	private DataBindingContext bindingContextZoom;
	private Combo combo_processes;
	private Combo combo_users;
	private Combo combo_hosts;
	private Combo combo_exitcodes;
	private Spinner spinner_period;
	private Button btnChildren;
	private Button button_refresh = null;
	private Button button_log = null;
	private Button button_browse = null;
	private Button button_open = null;
	private Button button_clean = null;
	private Button button_clone = null;
	private Button button_delete = null;
	private Scale scale_zoom;
	private StyledText text_logging = null;
	private Frame frame_radar = null;
	PradarViewModel einstellungen = new PradarViewModel();

	public Ini ini = null;
	
	Entity entity_filter = new Entity();
	
	public ArrayList<Entity> entities_all = new ArrayList<Entity>();
	public ArrayList<Entity> entities_filtered = new ArrayList<Entity>();
//	public Entity entity_marked = null;
	
	private int refresh_min_interval = 2000;
	private int refresh_interval = 600000;
	private Calendar now = Calendar.getInstance();
	private Calendar refresh_last = Calendar.getInstance();
	Calendar refresh_next = Calendar.getInstance();
	
	PradarViewProcessingPage applet;
	PradarViewTreePage tree;
	
//	Shell shell_dummy_tree;
//	Composite composite_tree;
//	Shell shell_dummy_radar;
//	Composite composite_radar;
//	Composite composite_12;
	CTabFolder tabFolder_12;
	ArrayList<PradarViewLogPage> logPages = new ArrayList<PradarViewLogPage>();
	ArrayList<PradarViewHtmlPage> htmlPages = new ArrayList<PradarViewHtmlPage>();
	CTabItem tabItem_radar;
	CTabItem tabItem_tree;
	Composite composite_tabItem_radar;
	Composite composite_tabItem_tree;
	
	Display display;
	
	final Color colorLogFatal = new Color(new Shell().getDisplay(), 215, 40, 40);
	final Color colorLogError = new Color(new Shell().getDisplay(), 215, 165, 172);
	final Color colorLogWarn = new Color(new Shell().getDisplay(), 202, 191, 142);
	final Color colorLogInfo = new Color(new Shell().getDisplay(), 184, 210, 176);
	
	int logLineCount = 0;
	
	ArrayList<String> pradar_server_port_at_hostname = new ArrayList<String>();
	ArrayList<String> license_server_port_at_hostname = new ArrayList<String>();
//	License license = null;
//	boolean erster_license_check = true;
	
	private Text txtTesttext;
	private Composite composite_3;

	private boolean userAdmin = false;

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarPartUi3()
	{
		checkJavaVersion();
		loadIni();
//		checkLicense();
		Shell shell = new Shell();
		shell.setSize(450, 465);
//		shell.setSize(633, 767);
//		shell.setSize(633, 900);
		composite_3 = new Composite(shell, SWT.NONE);
		composite_3.setLocation(0, 0);
		createControls(composite_3);
//		applet = new PradarViewProcessingPage(this);
		refresh_last.setTimeInMillis(0);
		refresh();
//		filter();
//		applet.refresh();
		tree.refresh();
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PradarPartUi3(Composite composite)
	{
		checkJavaVersion();
		loadIni();
//		checkLicense();
		applet = new PradarViewProcessingPage(this);
		refresh_last.setTimeInMillis(0);
		refresh();
		createControls(composite);
//		applet.refresh();
//		filter();
		tree.refresh();
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite composite)
	{
//		composite.setSize(613, 738);
//		composite.setSize(613, 900);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(3, false);
		gl_composite_1.marginWidth = 0;
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.heightHint = 445;
		gd_composite_1.widthHint = 122;
		composite_1.setLayoutData(gd_composite_1);
		
		Composite composite_11 = new Composite(composite_1, SWT.BORDER);
		composite_11.setLayout(new GridLayout(1, false));
		GridData gd_composite_11 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_11.heightHint = 437;
		gd_composite_11.widthHint = 169;
		composite_11.setLayoutData(gd_composite_11);
		
		Group grpFilter = new Group(composite_11, SWT.NONE);
		grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpFilter.setText("filter");
		grpFilter.setLayout(new GridLayout(4, false));
		
		Label lblNewLabel = new Label(grpFilter, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		lblNewLabel.setText("process");
		new Label(grpFilter, SWT.NONE);
		
		combo_processes = new Combo(grpFilter, SWT.BORDER | SWT.READ_ONLY);
		combo_processes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblNewLabel_1.setText("user");
		new Label(grpFilter, SWT.NONE);
		
		combo_users = new Combo(grpFilter, SWT.BORDER | SWT.READ_ONLY);
		combo_users.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		if(!this.isUserAdmin())
		{
			combo_users.setEnabled(false);
		}
		
		Label lblHost = new Label(grpFilter, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblHost.setText("host");
		new Label(grpFilter, SWT.NONE);
		
		combo_hosts = new Combo(grpFilter, SWT.NONE | SWT.READ_ONLY);
		combo_hosts.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblActive = new Label(grpFilter, SWT.NONE);
		lblActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblActive.setText("exitcode");
		new Label(grpFilter, SWT.NONE);
		
		combo_exitcodes = new Combo(grpFilter, SWT.BORDER | SWT.READ_ONLY);
		combo_exitcodes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblPeriod = new Label(grpFilter, SWT.NONE);
		lblPeriod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblPeriod.setText("active in last");
		new Label(grpFilter, SWT.NONE);
		
		spinner_period = new Spinner(grpFilter, SWT.BORDER);
		spinner_period.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinner_period.setMaximum(8064);
		spinner_period.setSelection(168);
		spinner_period.setMinimum(1);
		new Label(grpFilter, SWT.NONE);
		
		Label lblNewLabel_3 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		lblNewLabel_3.setText("hours");
		
		btnChildren = new Button(grpFilter, SWT.CHECK);
		btnChildren.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		btnChildren.setText("children");
		new Label(grpFilter, SWT.NONE);
		
		// Group functions db
		Group grpFunctionDb = new Group(composite_11, SWT.NONE);
		grpFunctionDb.setLayout(new GridLayout(4, false));
		grpFunctionDb.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunctionDb.setText("db");
		
		button_refresh = new Button(grpFunctionDb, SWT.NONE);
		GridData gd_button_refresh = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_button_refresh.widthHint = 62;
		button_refresh.setLayoutData(gd_button_refresh);
		button_refresh.setText("refresh");
		button_refresh.setToolTipText("refresh status of entities from database");
		button_refresh.addSelectionListener(listener_refresh_button);
		
//		button_clean = new Button(grpFunctionDb, SWT.NONE);
//		button_clean.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
//		button_clean.setText("clean");
//		button_clean.setToolTipText("checks whether active instances are still alive - disappeared instances will be checked out");
//		button_clean.addSelectionListener(listener_clean_button);
		
		// Group functions instance
		Group grpFunctionInstance = new Group(composite_11, SWT.NONE);
		grpFunctionInstance.setLayout(new GridLayout(4, true)); // 4 spalten, gleich breit
		grpFunctionInstance.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunctionInstance.setText("instance");
		
//		button_log = new Button(grpFunction, SWT.NONE);
//		button_log.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//		button_log.setText("log");
//		button_log.setToolTipText("shows logfile of selected process instance");
//		button_log.addSelectionListener(listener_log_button);
		
		button_browse = new Button(grpFunctionInstance, SWT.NONE);
		button_browse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_browse.setText("browse");
		button_browse.setToolTipText("show instance directory with a filebrowser.");
		button_browse.addSelectionListener(listener_browse_button);

		button_open = new Button(grpFunctionInstance, SWT.NONE);
		button_open.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_open.setText("open");
		button_open.setToolTipText("open instance with pmodel");
		button_open.addSelectionListener(listener_open_button);

		button_clone = new Button(grpFunctionInstance, SWT.NONE);
		button_clone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_clone.setText("clone");
		button_clone.setToolTipText("copy the instance");
		button_clone.addSelectionListener(listener_clone_button);
		
		button_delete = new Button(grpFunctionInstance, SWT.NONE);
		button_delete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_delete.setText("delete");
		button_delete.setToolTipText("deletes a finished (already checked out) process instance from database. includes an implicit 'clean'");
		button_delete.addSelectionListener(listener_delete_button);
		
		// den button auf diese weise aktiv/deaktiv zu stellen funktioniert nicht.
		// muss ueber databinding realisiert werden
		//		if ( (einstellungen.entitySelected != null) && ( einstellungen.entitySelected.getParentid()).equals("") )
//		{
//			button_delete.setEnabled(true);
//		}
//		else
//		{
//			button_delete.setEnabled(false);
//		}
		
		// Group visual
		Group grpVisual = new Group(composite_11, SWT.NONE);
		grpVisual.setText("visual");
		GridData gd_grpVisual = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_grpVisual.widthHint = 152;
		grpVisual.setLayoutData(gd_grpVisual);
		grpVisual.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel_2 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblNewLabel_2.setText("zoom");
		
		scale_zoom = new Scale(grpVisual, SWT.NONE);
		GridData gd_scale_zoom = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_scale_zoom.widthHint = 144;
		scale_zoom.setLayoutData(gd_scale_zoom);
		scale_zoom.setMaximum(1000);
		scale_zoom.setMinimum(60);
		scale_zoom.setSelection(100);
		scale_zoom.addMouseWheelListener(listener_mousewheel);
		
		Button btnNewButton2 = new Button(grpVisual, SWT.NONE);
		GridData gd_btnNewButton2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_btnNewButton2.widthHint = 141;
		btnNewButton2.setLayoutData(gd_btnNewButton2);
		btnNewButton2.setText("autoscale");
		new Label(composite_1, SWT.NONE);
		btnNewButton2.addSelectionListener(listener_autoscale_button);

		// Group apps
		Group grpApps = new Group(composite_11, SWT.NONE);
		grpApps.setText("apps");
		GridData gd_grpApps = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_grpApps.widthHint = 152;
		grpApps.setLayoutData(gd_grpApps);
		grpApps.setLayout(new GridLayout(2, false));
		
		Button btnNewButton3 = new Button(grpApps, SWT.NONE);
		GridData gd_btnNewButton3 = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_btnNewButton3.widthHint = 141;
		btnNewButton3.setLayoutData(gd_btnNewButton3);
		btnNewButton3.setText("pRamp");
		btnNewButton3.addSelectionListener(listener_pramp_button);

		// tabFolder erzeugen
		tabFolder_12 = new CTabFolder(composite_1, SWT.BORDER);
		tabFolder_12.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder_12.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder_12.setTabPosition(SWT.TOP);
		tabFolder_12.setTabHeight(30);
		tabFolder_12.addSelectionListener(listener_tabFolder_selection);
//		tabFolder_12.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
//		tabFolder_12.setSimple(false);
										
		// ein tabItem fuer radar mit eingebetteten composite erzeugen
		tabItem_radar = new CTabItem(tabFolder_12, SWT.NONE);
		tabItem_radar.setText("radar");
										
		composite_tabItem_radar = new Composite(tabFolder_12, SWT.NO_BACKGROUND | SWT.EMBEDDED);
		composite_tabItem_radar.setLayout(new GridLayout(1, false));
		GridData gd_composite_radar = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_radar.heightHint = 390;
		gd_composite_radar.minimumWidth = 10;
		gd_composite_radar.minimumHeight = 10;
		composite_tabItem_radar.setLayoutData(gd_composite_radar);
												
		tabItem_radar.setControl(composite_tabItem_radar);
		// radar einbinden
		frame_radar = SWT_AWT.new_Frame(composite_tabItem_radar);

		// ein tabItem fuer tree mit eingebetteten composite erzeugen
		tabItem_tree = new CTabItem(tabFolder_12, SWT.NONE);
		tabItem_tree.setText("tree");
		
		composite_tabItem_tree = new Composite(tabFolder_12, SWT.NONE);
		composite_tabItem_tree.setLayout(new GridLayout(1, false));
		GridData gd_composite_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_tree.heightHint = 390;
		gd_composite_tree.minimumWidth = 10;
		gd_composite_tree.minimumHeight = 10;
		composite_tabItem_tree.setLayoutData(gd_composite_tree);

		tabItem_tree.setControl(composite_tabItem_tree);
		tree = new PradarViewTreePage(this.composite_tabItem_tree, this);
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_composite_2.heightHint = 164;
		composite_2.setLayoutData(gd_composite_2);
		
		text_logging = new StyledText(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		text_logging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// Datenbindung der Filter
		bindingContextFilter = initDataBindingsFilter();
		bindingContextZoom = initDataBindingsZoom();
		
		// Datenbindung Processes-Combo
		initDataBindingsComboItems();

		// select combo_user to 'aktueller user'
		combo_users.setText(System.getProperty("user.name"));
		
		// tree einbinden
		composite_tabItem_tree.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Label testlab = new Label(composite_tabItem_tree, SWT.NONE);

		frame_radar.add(applet, BorderLayout.CENTER);
		applet.init();
		frame_radar.pack();
		frame_radar.setLocation(0, 0);
		updateUserInterface(einstellungen);
		updateUserInterfaceProcessing(einstellungen);
		applet_paint_with_new_filter();
		tabFolder_12.setSelection(0);
		frame_radar.setVisible(true);
		tabFolder_12.layout(true);
		
	}

	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	public void applet_paint_with_new_filter()
	{
		entity_filter.setProcess(einstellungen.getProcess());
		entity_filter.setUser(einstellungen.getUser());
		entity_filter.setHost(einstellungen.getHost());
		entity_filter.setExitcode(einstellungen.getExitcode());
		entity_filter.setPeriodInHours(einstellungen.getPeriod());
		// nur entities, die keine eltern haben
		entity_filter.setParentid("0");
		filter();
	}
	public void applet_paint_with_new_zoom()
	{
		applet.setZoomfaktor(einstellungen.getZoom());
	}
//	public void applet_refresh()
//	{
//		applet.refresh();
//	}
	public void applet_autoscale()
	{
		applet.autoscale();
	}
	
	
	@PreDestroy
	public void dispose()
	{
	}

	@Focus
	public void setFocus()
	{
		txtTesttext.setFocus();
	}
	
	IChangeListener listener_filter = new IChangeListener()
	{
		public void handleChange(ChangeEvent event)
		{
			filter();
			applet_paint_with_new_filter();
			applet.refresh();
			tree.refresh();
		}
	};
	
	IChangeListener listener_zoom = new IChangeListener()
	{
		public void handleChange(ChangeEvent event)
		{
//			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
			applet_paint_with_new_zoom();
		}
	};
	
	SelectionAdapter listener_refresh_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button wurde gedrueckt");
			refresh();
			tree.refresh();
		}
	};
	
	SelectionAdapter listener_log_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button wurde gedrueckt");
			if (einstellungen.entitySelected != null && einstellungen.entitySelected.getResource().equals(""))
			{
				log("warn", "no logfile for entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && (!(new File(einstellungen.entitySelected.getResource()).canRead())) )
			{
				log("warn", "cannot read logfile of entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && (!(new File(einstellungen.entitySelected.getResource()).exists()) ) )
			{
				log("warn", "logfile of entity does not exist "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && ((einstellungen.entitySelected.getResource().matches("\\.log|\\.txt$"))))
			{
				log("info", "showing logfile "+einstellungen.entitySelected.getResource());
				showLogFile(einstellungen.entitySelected);
			}
			else if (einstellungen.entitySelected != null && ((einstellungen.entitySelected.getResource().matches(".+html"))))
			{
				log("info", "showing logfile "+einstellungen.entitySelected.getResource());
				showHtmlFile(einstellungen.entitySelected);
			}
			else
			{
				log("warn", "no entity marked.");
			}
		}
	};
	
	SelectionAdapter listener_browse_button_OLD = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if (einstellungen.entitySelected != null && (!(einstellungen.entitySelected.getResource().equals(""))))
			{
				String pathInstanceDir = new File(einstellungen.entitySelected.getResource()).getParent();
				log("info", "opening filebrowser for entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
				try
				{
					log("info", "nautilus --browser "+pathInstanceDir);
					java.lang.Process sysproc = Runtime.getRuntime().exec("nautilus --browser "+pathInstanceDir);
				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			else if (einstellungen.entitySelected != null && einstellungen.entitySelected.getResource().equals(""))
			{
				log("warn", "unknown instance directory for entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && (new File(einstellungen.entitySelected.getResource()).getParentFile().canRead() ) )
			{
				log("warn", "cannot read instance directory of entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && (!(new File(einstellungen.entitySelected.getResource()).getParentFile().exists()) ) )
			{
				log("warn", "instance directory of entity does not exist "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else
			{
				log("warn", "no instance selected.");
			}
		}
	};
	
	SelectionAdapter listener_browse_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if(einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
				return;
			}

			String pathInstanceDir = new File(einstellungen.entitySelected.getResource()).getParent();

			java.io.File stepDir = new java.io.File(pathInstanceDir);
			if(!stepDir.exists())
			{
				log("error", "directory does not exist: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.isDirectory())
			{
				log("error", "is not a directory: "+stepDir.getAbsolutePath());
			}
			else if(!stepDir.canRead())
			{
				log("error", "cannot read directory: "+stepDir.getAbsolutePath());
			}
			
			else
			{
				String call = ini.get("apps", "filebrowser") + " " + stepDir.getAbsolutePath(); 
				log("info", "calling: "+call);
				
				try
				{
					java.lang.Process sysproc = Runtime.getRuntime().exec(call);
				}
				catch (IOException e)
				{
					log("error", e.getMessage());
				}
			}
		}
	};

	SelectionAdapter listener_clean_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			Iterator<String> iterPradarServer = pradar_server_port_at_hostname.iterator();
			while(iterPradarServer.hasNext())
			{
				String portAtMachineAsString = iterPradarServer.next();
				String [] port_and_machine = portAtMachineAsString.split("@");
		
				int portNumber = Integer.parseInt(port_and_machine[0]);
				String machineName = port_and_machine[1];
				log("info", "want to clean database");
				log("info", "trying pradar-server "+portNumber+"@"+machineName);
				try
				{
					// socket einrichten und Out/Input-Streams setzen
					Socket server = new Socket(machineName, portNumber);
					OutputStream out = server.getOutputStream();
					InputStream in = server.getInputStream();
					ObjectOutputStream objectOut = new ObjectOutputStream(out);
					ObjectInputStream  objectIn  = new ObjectInputStream(in);
					
					// Objekte zum server uebertragen
					objectOut.writeObject("cleandb_user");
					objectOut.writeObject(System.getProperty("user.name"));
		
					// daten holen aus db
					log("info", "checking out active instances which appear to be disappeared");
					server.close();
				}
				catch (UnknownHostException e)
				{
					// TODO Auto-generated catch block
					log("warn", "unknown host "+machineName);
					pradar_server_port_at_hostname = null;
		//					e.printStackTrace();
				}
		//		catch (ConnectException e)
		//		{
		//			log("warn", "no pradar-server found at "+portNumber+"@"+machineName);
		////					e.printStackTrace();
		//		}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					log("warn", "input / output problems at "+portNumber+"@"+machineName);
							e.printStackTrace();
				}
			}
			
			// daten und anzeige refreshen
			refresh();
			tree.refresh();

		}
	};	
	
	SelectionAdapter listener_open_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if (einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
			}
			
			else
			{
				java.io.File pmbFile = new java.io.File(einstellungen.entitySelected.getResource());
				
				if(!(pmbFile.exists()))
				{
					log("error", "process-model-file does not exist: " + pmbFile.getAbsolutePath());
				}
				
				else
				{
					log("info", "opening process-model-file for inspection");
					String aufruf = ini.get("apps",  "pmodel") + " -instance "+pmbFile.getAbsolutePath();
					log("info", "calling " + aufruf);
					
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
			}
		}
	};	

	SelectionAdapter listener_clone_button = new SelectionAdapter()
	{
		
		public void widgetSelected(SelectionEvent event)
		{
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getUser().equals(System.getProperty("user.name")))) )
			{
				log("error", "you may only clone your instances (user "+System.getProperty("user.name")+")");
			}
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getParentid().equals("0"))) )
			{
				log("error", "you must not clone child instances");
			}

//			else if ( (einstellungen.entitySelected != null) && einstellungen.entitySelected.isActive() )
//			{
//				log("error", "you may only delete finished instances.");
//			}

			else if (einstellungen.entitySelected != null)
			{
				// bestaetigungsdialog
				Shell diaShell = new Shell();
				MessageBox confirmation = new MessageBox(diaShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				String message = "";

				message += "you are about to clone this instance.\n";
				message += "a full copy of this process instance with all its nested instances and all associated files will be made.\n\n";
				message += "do you really want to clone?";

				confirmation.setMessage(message);

				// open confirmation and wait for user selection
				int returnCode = confirmation.open();
//				System.out.println("returnCode is: "+returnCode);

				// ok == 32
				if (returnCode == 32)
				{
					// creating and setting a busy cursor
					diaShell.setCursor(new Cursor(display, SWT.CURSOR_WAIT));

					log("info", "cloning process");

					File fileResource = new File(einstellungen.entitySelected.getResource());

					if(!fileResource.exists())
					{
						log("error", "resource does not exist: "+fileResource.getAbsolutePath());
					}
					else if(!fileResource.isFile())
					{
						log("error", "resource is not a file: "+fileResource.getAbsolutePath());
					}
					else if(!fileResource.canRead())
					{
						log("error", "cannot read resource: "+fileResource.getAbsolutePath());
					}
					
					else
					{
						Process clonedProcess = this.cloneProcess(einstellungen.entitySelected, null);
						
						// falls children vorhanden, sollen diese auch geklont werden
						for(Entity possibleChild : entities_filtered)
						{
							// ist es ein child?
							if(possibleChild.getParentid().equals(einstellungen.entitySelected.getId()))
							{
								this.cloneProcess(possibleChild, clonedProcess);
							}
						}
						diaShell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
					}
				}
			}
			else
			{
				log("warn", "no instance selected");
			}
			
			// creating and setting an arrow cursor
		}

		/**
		 * clone Process mit Daten an Hand der pradar-Entity
		 * returns process-id
		 * @param entity
		 */
		public Process cloneProcess(Entity entity, Process parentProcess)
		{
			Process p1 = new Process();
			p1.setInfilebinary(entity.getResource());
			Process process = p1.readBinary();

			// klonen mit data
			Process clone = null;
			if(parentProcess == null)
			{
				clone = process.cloneWithData(null, null);
				log("info", "cloning instance: original=" + process.getRootdir() + "/process.pmb, clone=" + clone.getRootdir() + "/process.pmb");
			}
			else
			{
				clone = process.cloneWithData(parentProcess.getRootdir() + "/dir4step_" + process.getStepnameOfParent(), parentProcess.getId());
				log("debug", "stepname of parentProcess is: " + process.getStepnameOfParent());
				log("debug", "process.cloneWithData(" + parentProcess.getRootdir() + "/dir4step_" + process.getStepnameOfParent() + ", " + parentProcess.getId());
				log("info", "cloning instance as a child: original=" + process.getRootdir() + "/process.pmb, clone=" + clone.getRootdir() + "/process.pmb");
			}

//			// das original speichern, weil auch hier aenderungen vorhanden sind (zaehler fuer klone)
			process.setOutfilebinary(entity.getResource());
			process.writeBinary();

			// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
			String call2 = ini.get("apps", "pradar-attend") + " -instance " + clone.getRootdir() + "/process.pmb"; 
			log("info", "calling: "+call2);

			try
			{
				java.lang.Process sysproc = Runtime.getRuntime().exec(call2);
			}
			catch (IOException e)
			{
				log("error", e.getMessage());
			}
			
			// rueckgabe der id. kann beim klonen von childprozessen verwendet werden
			return clone;
		}
	};	
	
	SelectionAdapter listener_delete_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getUser().equals(System.getProperty("user.name")))) )
			{
				log("error", "you may only delete your instances (user "+System.getProperty("user.name")+")");
			}
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getParentid().equals("0"))) )
			{
				log("error", "you must not delete child instances");
			}

//			else if ( (einstellungen.entitySelected != null) && einstellungen.entitySelected.isActive() )
//			{
//				log("error", "you may only delete finished instances.");
//			}

			else if (einstellungen.entitySelected != null)
			{
				// bestaetigungsdialog
				Shell shell = new Shell();
				MessageBox confirmation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				confirmation.setText("please confirm");
				
				String message = "Do you really want to delete this entity with all the children and all its data from filesystem?\n\n";
				message += "id:\t\t\t"+einstellungen.entitySelected.getId() +"\n";
				message += "process:\t\t"+einstellungen.entitySelected.getProcess() +"\n";
				message += "user:\t\t"+einstellungen.entitySelected.getUser() +"\n";
				message += "host:\t\t"+einstellungen.entitySelected.getHost() +"\n";
				message += "checkin:\t\t"+einstellungen.entitySelected.getCheckinAsString() +"\n";
				message += "checkout:\t"+einstellungen.entitySelected.getCheckoutAsString() +"\n";
				message += "exitcode:\t\t"+einstellungen.entitySelected.getExitcode() +"\n";
				try
				{
					java.io.File resource = new java.io.File(einstellungen.entitySelected.getResource());
					message += "directory:\t\t"+resource.getParentFile().getCanonicalPath() +"\n";
				}
				catch (IOException e)
				{
						// TODO Auto-generated catch block
						log("error", e.getMessage());
				}
				catch (NullPointerException e)
				{
						// TODO Auto-generated catch block
					log("warn", "resource file not found");
				}

				confirmation.setMessage(message);
				
				// open confirmation and wait for user selection
				int returnCode = confirmation.open();
//				System.out.println("returnCode is: "+returnCode);

				// ok == 32
				if (returnCode == 32)
				{
					// 1) loeschen des entity in der pradar-db
					Iterator<String> iterPradarServer = pradar_server_port_at_hostname.iterator();
					while(iterPradarServer.hasNext())
					{
						String portAtMachineAsString = iterPradarServer.next();
						String [] port_and_machine = portAtMachineAsString.split("@");
				
						int portNumber = Integer.parseInt(port_and_machine[0]);
						String machineName = port_and_machine[1];
						log("info", "want to delete data from database");
						log("info", "trying pradar-server "+portNumber+"@"+machineName);
						try
						{
							// socket einrichten und Out/Input-Streams setzen
							Socket server = new Socket(machineName, portNumber);
							OutputStream out = server.getOutputStream();
							InputStream in = server.getInputStream();
							ObjectOutputStream objectOut = new ObjectOutputStream(out);
							ObjectInputStream  objectIn  = new ObjectInputStream(in);
							
							// Objekte zum server uebertragen
							objectOut.writeObject("delete");
							objectOut.writeObject(einstellungen.entitySelected);
				
							// daten holen aus db
							log("info", "deleting entity");
							server.close();
							
						}
						catch (UnknownHostException e)
						{
							// TODO Auto-generated catch block
							log("warn", "unknown host "+machineName);
							pradar_server_port_at_hostname = null;
							e.printStackTrace();
							log("error", e.getMessage());
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							log("warn", "input / output problems at "+portNumber+"@"+machineName);
							e.printStackTrace();
							log("error", e.getMessage());
						}
						
						
						// 2) loeschen der daten im filesystem
						try
						{
							java.io.File resource = new java.io.File(einstellungen.entitySelected.getResource());
							log("warn", "deleting directory "+resource.getParentFile().getCanonicalPath());
							FileUtils.deleteDirectory(resource.getParentFile());
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							log("error", e.getMessage());
						}
						catch (NullPointerException e)
						{
							log("warn", "data not deleted from filesystem, because no resource is defined");
						}

					}
				}

				
				// daten und anzeige refreshen
				refresh();
				tree.refresh();

			}
			
			else
			{
				log("warn", "no instance selected");
			}
			
		}
	};	
	
	
	SelectionAdapter listener_autoscale_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button wurde gedrueckt");
			applet_autoscale();
		}
	};

	/**
	 * pramp-button oeffnet die anwendung pramp-gui
	 **/
	SelectionAdapter listener_pramp_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			log("info", "starten von pramp");
			String aufruf = ini.get("apps", "pramp");
			
			try
			{
				log("info", "calling: " + aufruf);
				java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
	SelectionAdapter listener_tabFolder_selection = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if(tabFolder_12.getSelectionIndex() == 0)
			{
				einstellungen.setIsRadarVisible(true);
			}
			else
			{
				einstellungen.setIsRadarVisible(false);
			}
		}
	};
	
	MouseWheelListener listener_mousewheel = new MouseWheelListener()
	{
		public void mouseScrolled(MouseEvent me)
		{
			scale_zoom.setSelection(scale_zoom.getSelection() + (me.count*5));
		}
	};
	
	/**
	 * add change listener to all bindings
	 */
	private void updateUserInterface(PradarViewModel einstellungen)
	{
//		bindingContext.dispose();
		IObservableList bindings = bindingContextFilter.getValidationStatusProviders();

//		// Register the Listener for binding 'process'
//		Iterator iterbinding =  bindings.iterator();
//		while (iterbinding.hasNext())
//		{
//			Binding binding = (Binding)iterbinding.next();
//			binding.getModel().addChangeListener(listener);
//			System.out.println("bindingtype: "+binding.toString());
//		}
		
		// Register the Listener to all bindings
		for (Object o : bindings)
		{
			Binding b = (Binding) o;
			b.getModel().addChangeListener(listener_filter);
		}
	}

	/**
	 * add change listener for binding 'zoom'
	 */
	private void updateUserInterfaceProcessing(PradarViewModel zoom)
	{
//		bindingContext.dispose();
		IObservableList bindings = bindingContextZoom.getValidationStatusProviders();

		// Register the Listener for binding 'zoom'
		
		Binding b = (Binding) bindings.get(0);
		b.getModel().addChangeListener(listener_zoom);
	}

	protected DataBindingContext initDataBindingsZoom()
	{
		DataBindingContext bindingContextZoom = new DataBindingContext();
		//
		IObservableValue targetObservableZoom = WidgetProperties.selection().observe(scale_zoom);
		IObservableValue modelObservableZoom = BeanProperties.value("zoom").observe(einstellungen);
		bindingContextZoom.bindValue(targetObservableZoom, modelObservableZoom, null, null);
		//
		return bindingContextZoom;
	}
	
	protected DataBindingContext initDataBindingsFilter()
	{

//		// Einrichten der ControlDecoration über dem Textfeld 'active'
//		final ControlDecoration controlDecorationActive = new ControlDecoration(text_active, SWT.LEFT | SWT.TOP);
//		controlDecorationActive.setDescriptionText("use 'true', 'false' or leave field blank");
//		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
//		controlDecorationActive.setImage(fieldDecoration.getImage());
//
//		// Validator for 'active' mit Verbindung zur Controldecoration
//		IValidator validatorActive = new IValidator()
//		{
//			public IStatus validate(Object value)
//			{
//				if (value instanceof String)
//				{
//					if (((String) value).matches("true|false|all|"))
//					{
//						controlDecorationActive.hide();
//						return ValidationStatus.ok();
//						
//					}
//				}
//				controlDecorationActive.show();
//				return ValidationStatus.error("not a boolean or 'all'");
//			}
//		};
//
//		// UpdateStrategy fuer 'active' ist: update der werte nur wenn validierung erfolgreich
//		UpdateValueStrategy strategyActive = new UpdateValueStrategy();
//		strategyActive.setBeforeSetValidator(validatorActive);
//
//		IObservableValue targetObservableActive = WidgetProperties.text().observeDelayed(800, text_active);
//		IObservableValue modelObservableActive = BeanProperties.value("active").observe(einstellungen);
//		bindingContextFilter.bindValue(targetObservableActive, modelObservableActive, strategyActive, null);
		//
		//---------
		
		DataBindingContext bindingContextFilter = new DataBindingContext();
		//
		IObservableValue targetObservableProcess = WidgetProperties.text().observe(combo_processes);
		IObservableValue modelObservableProcess = BeanProperties.value("process").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableProcess, modelObservableProcess, null, null);
		//
		IObservableValue targetObservableUser = WidgetProperties.text().observe(combo_users);
		IObservableValue modelObservableUser = BeanProperties.value("user").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableUser, modelObservableUser, null, null);
		//
		IObservableValue targetObservableHost = WidgetProperties.text().observe(combo_hosts);
		IObservableValue modelObservableHost = BeanProperties.value("host").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableHost, modelObservableHost, null, null);
		//
		IObservableValue targetObservableExitcode = WidgetProperties.text().observe(combo_exitcodes);
		IObservableValue modelObservableExitcode = BeanProperties.value("exitcode").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableExitcode, modelObservableExitcode, null, null);
		//
		IObservableValue targetObservablePeriod = WidgetProperties.selection().observeDelayed(800, spinner_period);
		IObservableValue modelObservablePeriod = BeanProperties.value("period").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservablePeriod, modelObservablePeriod, null, null);
		//
		IObservableValue targetObservableChildren = WidgetProperties.selection().observe(btnChildren);
		IObservableValue modelObservableChildren = BeanProperties.value("children").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableChildren, modelObservableChildren, null, null);
		//
		return bindingContextFilter;
	}
	
	/**
	 * binds arrays to combo-boxes 'processes', 'users'
	 */
	protected DataBindingContext initDataBindingsComboItems()
	{
		DataBindingContext bindingContextComboItems = new DataBindingContext();
		//
		IObservableList targetObservableProcesses = WidgetProperties.items().observe(combo_processes);
		IObservableList modelObservableProcesses = BeanProperties.list("processes").observe(einstellungen);
		bindingContextComboItems.bindList(targetObservableProcesses, modelObservableProcesses, null, null);
		//
		IObservableList targetObservableUsers = WidgetProperties.items().observe(combo_users);
		IObservableList modelObservableUsers = BeanProperties.list("users").observe(einstellungen);
		bindingContextComboItems.bindList(targetObservableUsers, modelObservableUsers, null, null);
		//
		IObservableList targetObservableHosts = WidgetProperties.items().observe(combo_hosts);
		IObservableList modelObservableHosts = BeanProperties.list("hosts").observe(einstellungen);
		bindingContextComboItems.bindList(targetObservableHosts, modelObservableHosts, null, null);
		//
		IObservableList targetObservableExitcodes = WidgetProperties.items().observe(combo_exitcodes);
		IObservableList modelObservableExitcodes = BeanProperties.list("exitcodes").observe(einstellungen);
		bindingContextComboItems.bindList(targetObservableExitcodes, modelObservableExitcodes, null, null);
		//
		return bindingContextComboItems;
	}

	/**
	 * refreshes data (entities) from database
	 * @return void
	 */
	void refresh()
	{
		einstellungen.animation = false;
//		checkLicense();
		now = Calendar.getInstance();
		if ((now.getTimeInMillis() - refresh_last.getTimeInMillis()) > refresh_min_interval)
		{
			einstellungen.entitySelected = null;
//			entities_all.clear();
//			entities_filtered.clear();
			loadData();
			filter();
			applet.refresh();
			
			// jetzt alle logFiles refreshen
			// falls ein logFile mehrfach geoeffnet wurde, ist es in diesem Array mehrfach vorhanden und nur das letzte soll refresht werden

			ArrayList<PradarViewLogPage> logPagesReverse = this.logPages;
			Collections.reverse(logPagesReverse);
			
			for(PradarViewLogPage logPage : logPagesReverse)
			{
				if (this.isTabPresentByName(this.tabFolder_12, logPage.getTabName()))
				{
					logPage.refresh();
					log("info", "refreshing logfile "+logPage.getTabName());
					break;
				}
			}
			
			// die liste der moegliche process-auswahl feststellen
			Map<String, String> processNames = new HashMap<String, String>();
			processNames.put("", "");
			for (Entity entity : this.entities_all)
			{
				if (!(processNames.containsKey(entity.getProcess())) && entity.getParentid().equals("0"))
				{
					processNames.put(entity.getProcess(), "");
				}
			}
			einstellungen.processes = processNames.keySet().toArray(new String[processNames.size()]);
			
			// die liste der moegliche user-auswahl feststellen
			Map<String, String> userNames = new HashMap<String, String>();
			userNames.put("", "");
			for (Entity entity : this.entities_all)
			{
				if (!(userNames.containsKey(entity.getUser())) && entity.getParentid().equals("0"))
				{
					userNames.put(entity.getUser(), "");
				}
			}
			einstellungen.users = userNames.keySet().toArray(new String[userNames.size()]);
			
			// die liste der moegliche host-auswahl feststellen
			Map<String, String> hostNames = new HashMap<String, String>();
			hostNames.put("", "");
			for (Entity entity : this.entities_all)
			{
				if (!(hostNames.containsKey(entity.getUser())) && entity.getParentid().equals("0"))
				{
					hostNames.put(entity.getHost(), "");
				}
			}
			einstellungen.hosts = hostNames.keySet().toArray(new String[hostNames.size()]);
			
			// die liste der moeglichen exitcode-auswahl feststellen
			Map<String, String> exitcodes = new HashMap<String, String>();
			exitcodes.put("", "");
			for (Entity entity : this.entities_all)
			{
				if (!(exitcodes.containsKey(entity.getExitcode())) && entity.getParentid().equals("0"))
				{
					exitcodes.put(entity.getExitcode(), "");
				}
			}
			einstellungen.exitcodes = exitcodes.keySet().toArray(new String[exitcodes.size()]);

		}
		else
		{
			log("warn", "refresh interval must be at least "+(this.refresh_min_interval/1000)+" seconds.");
//			System.out.println("refresh interval must be at least "+(this.refresh_min_interval/1000)+" seconds.");
			
		}
		einstellungen.animation = true;
		
	}

	/**
	 * asks for entities from the first pradar-server that responds
	 * @return void
	 */
	void loadData()
	{
		Iterator<String> iterPradarServer = this.pradar_server_port_at_hostname.iterator();
		while(iterPradarServer.hasNext())
		{
			String portAtMachineAsString = iterPradarServer.next();
			String [] port_and_machine = portAtMachineAsString.split("@");
			
			int portNumber = Integer.parseInt(port_and_machine[0]);
			String machineName = port_and_machine[1];
			log("info", "want to load data from pradar-server");
			log("info", "trying pradar-server "+portNumber+"@"+machineName);
			try
			{

				// socket einrichten und Out/Input-Streams setzen
//				log("debug", "machineName="+machineName+" | portNumber="+portNumber);

//				log("debug", "server objekt erstellen");
				Socket connectToServerSocket = new Socket(machineName, portNumber);
				connectToServerSocket.setSoTimeout(20000);

//				log("debug", "outputStream erstellen");
				OutputStream streamToServer = connectToServerSocket.getOutputStream();

//				log("debug", "outputStream  flushen");
				streamToServer.flush();
				
//				log("debug", "objectOutputStream  erstellen");
				ObjectOutputStream objectToServer = new ObjectOutputStream(streamToServer);

//				log("debug", "objectOutputStream  flushen");
				objectToServer.flush();
				
				// Objekte zum server uebertragen
//				log("debug", "write: getall");
				objectToServer.writeObject("getall");

//				log("debug", "outputStream  flushen");
				streamToServer.flush();

//				log("debug", "objectOutputStream  flushen");
				objectToServer.flush();

				// sende-object zerstoeren - wird nicht mehr gebraucht
//				log("debug", "objectOutputStream schliessen");
//				objectToServer.close();

//				log("debug", "inputStream erstellen");
				InputStream streamFromServer = connectToServerSocket.getInputStream();

//				log("debug", "objectInputStream  erstellen");
				ObjectInputStream  objectFromServer = new ObjectInputStream(streamFromServer);

				// Antwort vom Server lesen - ein array aller Entities
				try
				{
//					log("debug", "reading");
					Object serverAnswer = objectFromServer.readObject();
//					log("debug", "reading done");

					// lese-object zerstoeren - wird nicht mehr gebraucht
					objectFromServer.close();
//					log("debug", "objectFromServer closed");

					ArrayList<Object> serverAnswer2 = null;
					if(serverAnswer instanceof ArrayList)
					{
//						log("debug", "serverAnswer is an ArrayList");
						serverAnswer2 = (ArrayList<Object>) serverAnswer;
					}

					this.entities_all.clear();
					for(Object actObject : serverAnswer2)
					{
						if(actObject instanceof Entity)
						{
//							log("debug", "item of ArrayList<Object> is an Entity  --->  adding to ArrayList<Entity>");
							this.entities_all.add((Entity) actObject);
						}
					}

//					log("debug", "reading done! closing ");
					objectFromServer.close();

//					log("debug", "read finished");
				}
				catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// daten holen aus db
				log("info", "refreshing data...");
				connectToServerSocket.close();
				
			}
			catch (UnknownHostException e)
			{
				log("warn", "unknown host "+machineName);
				this.pradar_server_port_at_hostname = null;
			}
			catch (IOException e)
			{
				log("warn", "input / output problems at "+portNumber+"@"+machineName);
						e.printStackTrace();
			} 
	
			this.refresh_last = Calendar.getInstance();
			this.refresh_next = Calendar.getInstance();
			this.refresh_next.add(13, this.refresh_interval);
		}
	}
	
	/**
	 * loads variables from ini-file, tests whether server are responding and saves the responding ones to fields
	 * @return void
	 */
	void loadIni()
	{
//		PradarViewProcessingPage tmp = new PradarViewProcessingPage(this);
		java.io.File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(this.getClass()) + "/" + "../etc/pradar-gui.ini");
			
		ArrayList<String> pradar_server_list = new ArrayList<String>();
		ArrayList<String> license_server_list = new ArrayList<String>();
			
		try
		{
			ini = new Ini(inifile);
			// einlesen der ini-section [pradar-server]
			for(int x = 1; x <= 5; x++)
			{
				if (ini.get("pradar-server", "pradar-server-"+x) != null )
				{
					pradar_server_list.add(ini.get("pradar-server", "pradar-server-"+x));
				}
			}
			// einlesen der ini-section [license-server]
			for(int x = 1; x <= 3; x++)
			{
				if (ini.get("license-server", "license-server-"+x) != null )
				{
					license_server_list.add(ini.get("license-server", "license-server-"+x));
				}
			}
			this.license_server_port_at_hostname = license_server_list;

			// feststelen ob aktueller user ein admin ist
			for(String iniUser : ini.get("roles", "admin").split(","))
			{
				if(iniUser.equals(System.getProperty("user.name")))
				{
					this.userAdmin = true;
				}
			}
		}
		catch (FileNotFoundException e)
		{
			log("fatal", "problems with configuration: file not found: "+inifile.getAbsolutePath());
			System.exit(1);
		}
		catch (InvalidFileFormatException e1)
		{
			log("fatal", "problems with configuration: invalid file format: "+inifile.getAbsolutePath());
			System.exit(1);
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			log("fatal", "problems with configuration: problems while reading file (IOException): "+inifile.getAbsolutePath());
			System.exit(1);
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		}
			
//		// einchecken in die DB
//		Socket serverSocket = null;
			
		boolean pradar_server_not_found = true;
			
		// ueber alle pradar-server aus ini-file iterieren und den ersten erfolgreichen merken fuer spaetere anfragen
		Iterator<String> iter_pradar_server = pradar_server_list.iterator();
		while(pradar_server_not_found && iter_pradar_server.hasNext() && (this.pradar_server_port_at_hostname.size() == 0))
		{
			String port_and_machine_as_string = iter_pradar_server.next();
			String [] port_and_machine = port_and_machine_as_string.split("@");

			int portNumber = Integer.parseInt(port_and_machine[0]);
			String machineName = port_and_machine[1];
			log("info", "determining pradar-server");
			log("info", "trying pradar-server "+portNumber+"@"+machineName);
			try
			{
				Socket serverSocket = new Socket(machineName, portNumber);
				
//				PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
//				BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
//				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				
				
				
//				// socket einrichten und Out/Input-Streams setzen
//				serverSocket = new Socket(machineName, portNumber);
//				log("debug", "server.getOutputStream()");
//				OutputStream out = serverSocket.getOutputStream();
//
//				log("debug", "server.getInputStream()");
//				InputStream in = serverSocket.getInputStream();
//
//				log("debug", "new ObjectOutputStream(...)");
//				ObjectOutputStream objectOut = new ObjectOutputStream(out);
//
//				log("debug", "new ObjectInputStream(...)");
//				ObjectInputStream  objectIn  = new ObjectInputStream(in);

//				log("debug", "streams fertig");

				serverSocket.close();
				
				// socket wurde erfolgreich mit dem server verbunden. pradar-server soll fuer weitere Anfragen gemerkt werden
				this.pradar_server_port_at_hostname.add(port_and_machine_as_string);
				pradar_server_not_found = false;
				log("info", "valid pradar-server found at "+portNumber+"@"+machineName);
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				log("warn", "unknown host "+machineName);
//					e.printStackTrace();
			}
			catch (ConnectException e)
			{
				log("warn", "no pradar-server found at "+portNumber+"@"+machineName);
//					e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				log("warn", "input / output problems at "+portNumber+"@"+machineName);
//					e.printStackTrace();
			}
		}
		if (pradar_server_not_found)
		{
			log("error", "no pradar-server found. talk to your administrator.");
//			System.exit(1);
		}

	}

	void log(String level, String logstring)
	{

		logstring = "["+new Timestamp(System.currentTimeMillis()) + "]:"+level+":"+logstring;

		if (text_logging != null)
		{
			text_logging.append(logstring+"\n");
//			if (level.equals("info"))		{	text_logging.setLineBackground(logLineCount, 1, colorLogWarn);}
			if (level.equals("warn"))	{	text_logging.setLineBackground(logLineCount, 1, colorLogWarn);}
			else if (level.equals("error"))	{	text_logging.setLineBackground(logLineCount, 1, colorLogError);}
			else if (level.equals("fatal"))	{	text_logging.setLineBackground(logLineCount, 1, colorLogFatal);}
			logLineCount = logLineCount+1;
			text_logging.setTopIndex(text_logging.getLineCount()-1);
			
			// alles ausser level=info soll auch auf der kommandozeile ausgegeben werden
			if (!(level.equals("info")))
			{
				System.err.println(logstring);
			}
		}
		
		else
		{
			System.err.println(logstring);
		}
	}
	
	/**
	 * checkout License from floatingLicenseServer
	 * @return void
	 */
	void checkLicense()
	{
		/*----------------------------
		  die lizenz ueberpruefen und ggf abbrechen
		----------------------------*/
	
		// check for valid license
		ArrayList<String> allPortAtHost = new ArrayList<String>();
		allPortAtHost.add(ini.get("license-server", "license-server-1"));
		allPortAtHost.add(ini.get("license-server", "license-server-2"));
		allPortAtHost.add(ini.get("license-server", "license-server-3"));
	
		MyLicense lic = new MyLicense(allPortAtHost, "1", "user-edition", "0.1");
	
		// lizenz-logging ausgeben
		for(String actLine : (ArrayList<String>) lic.getLog())
		{
			System.err.println(actLine);
		}
	
		// abbruch, wenn lizenz nicht valide
		if (!lic.isValid())
		{
			System.exit(1);
		}
	}

	void filter()
	{
//		System.out.println("children is: "+this.einstellungen.getChildren());
		this.entities_filtered = entity_filter.getAllMatches(this.entities_all);

		// falls auch children angezeigt werden sollen
		if (einstellungen.getChildren())
		{
			for(int x=0; x<this.entities_filtered.size(); x++)
			{
				Entity actualEntity = this.entities_filtered.get(x);
				for(int y=0; y<this.entities_all.size(); y++)
				{
					Entity actualEntityPossibleChild = this.entities_all.get(y);
//					if (possible_child.getParentid().equals(entity.getId()))
//					Entity possible_child = this.entities_all.get(y);
					if ( (actualEntityPossibleChild.getParentid().equals(actualEntity.getId())) && (actualEntityPossibleChild.getHost().equals(actualEntity.getHost())) && (actualEntityPossibleChild.getUser().equals(actualEntity.getUser())) && (actualEntityPossibleChild.getCheckinInMillis() > actualEntity.getCheckinInMillis()) )
//					if ( (actualEntityPossibleChild.getParentid().equals(actualEntity.getId())) && (actualEntityPossibleChild.getHost().equals(actualEntity.getHost())) && (actualEntityPossibleChild.getUser().equals(actualEntity.getUser()))  )
//					if (actualEntityPossibleChild.getParentid().equals(actualEntity.getId()) && actualEntityPossibleChild.getUser().equals(actualEntity.getUser()) )
//					if (actualEntityPossibleChild.getParentid().equals(actualEntity.getId())  )
					{
//						if(!(this.entities_filtered.contains(actualEntityPossibleChild)))
						this.entities_filtered.add(actualEntityPossibleChild);
//						System.out.println("another child found");
					}
				}
			}
		}
		log("info", "setting filter...");
		log("info", "total amount of entities: "+this.entities_all.size());
		log("info", "amount of entities passing filter: "+this.entities_filtered.size());
	}

	Entity getEntityBySuperId(String superId)
	{
		Entity entityWithSuperId = null;
		for(Entity actualEntity : entities_all)
		{
			if ( actualEntity.getSuperid().equals(superId) )
			{
				entityWithSuperId = actualEntity;
			}
		}
		return entityWithSuperId;
	}
	
	/**
	 * opens a seperate window and shows the content of a file
	 * @param String pathToFile
	 */
	void showTextFileShell(String pathToFile)
	{
		String content = "problems while reading from file";
		File file = new File(pathToFile);
		if (!(file.exists()))
		{
//			log("warn", "cannot read file: "+pathToFile);
			content = "cannot read file";
		}
		
		try
		{
			content = readFile(pathToFile);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// create the widget's shell
		Shell shell = new Shell(display);
		shell.setText(pathToFile);
		shell.setLayout(new FillLayout());
		shell.setSize(800, 400);
//		Display display = shell.getDisplay();

		// create the styled text widget
		StyledText widget = new StyledText(shell, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
		widget.setText(content);

		shell.open();
//		while (!shell.isDisposed())
//		if (!display.readAndDispatch()) display.sleep();

	}

	/**
	 * opens a seperate Tab and shows the content of a html file
	 * @param Entity entity
	 */
	void showHtmlFile(Entity entity)
	{
		File file = new File(entity.getResource());
		if (!(file.exists()))
		{
			log("warn", "cannot read file: "+entity.getResource());
			return;
		}

		log("info", "opening in external browser: "+entity.getResource());

		String[] args_for_syscall = {"firefox", entity.getResource()};
		ProcessBuilder pb = new ProcessBuilder(args_for_syscall);
		try {
			java.lang.Process p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log ("info", "calling: " + StringUtils.join(args_for_syscall, " "));

		// alternative: embedded browser
//		else
//		{
//			String tabName = entity.getProcess()+"<"+entity.getId()+">";
//			
//			// ueberpruefen ob es von diesem file schon ein tab gibt
//			if (isTabPresentByName(tabFolder_12, tabName))
//			{
//				tabFolder_12.setSelection(getTabIdByName(tabFolder_12, tabName));
//			}
//			
//			// tab mit logfile ansicht erzeugen
//			else
//			{
//				this.htmlPages.add(new PradarViewHtmlPage(this.tabFolder_12, this, entity, tabName));
//				// focus auf den neuen Tab
//				tabFolder_12.setSelection(tabFolder_12.getItemCount()-1);
//			}
//		}
	}	

	/**
	 * opens a seperate Tab and shows the content of a file
	 * @param Entity entity
	 */
	void showLogFile(Entity entity)
	{
		File file = new File(entity.getResource());
		if (!(file.exists()))
		{
			log("warn", "cannot read file: "+entity.getResource());
			return;
		}

		else
		{
			String tabName = entity.getProcess()+"<"+entity.getId()+">";
			
			// ueberpruefen ob es von diesem file schon ein tab gibt
			if (isTabPresentByName(tabFolder_12, tabName))
			{
				tabFolder_12.setSelection(getTabIdByName(tabFolder_12, tabName));
			}
			
			// tab mit logfile ansicht erzeugen
			else
			{
				this.logPages.add(new PradarViewLogPage(this.tabFolder_12, this, entity, tabName));
				// focus auf den neuen Tab
				tabFolder_12.setSelection(tabFolder_12.getItemCount()-1);
			}
		}
		
	}

		// tab mit logfile ansicht erzeugen
//		else
//		{
//			// ein tabItem fuer das textfile mit eingebetteten composite erzeugen
//			CTabItem tabItem_logtext = new CTabItem(tabFolder_12, SWT.CLOSE);
//			tabItem_logtext.setText(entity.getProcess()+" "+entity.getId());
//			tabItem_logtext.setToolTipText(entity.getResource());
//	
//			Composite composite_tabItem_logtext = new Composite(tabFolder_12, SWT.NONE);
//			composite_tabItem_logtext.setLayout(new GridLayout(1, false));
//			GridData gd_composite_logtext = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
//			gd_composite_logtext.heightHint = 390;
//			gd_composite_logtext.minimumWidth = 10;
//			gd_composite_logtext.minimumHeight = 10;
//			composite_tabItem_logtext.setLayoutData(gd_composite_logtext);
//			
//			tabItem_logtext.setControl(composite_tabItem_logtext);
//	
//			try
//			{
//				content = readFile(entity.getResource());
//			} catch (IOException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			String[] lines = content.split("\\n");
//			
//			// create the styled text widget
//			StyledText widget = new StyledText(composite_tabItem_logtext, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI);
//			widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//			
//			for (int y = 0; y < lines.length; y++)
//			{
//				widget.append(lines[y]+"\n");
//				if (lines[y].matches(".*([^\\w]|^)(warn|WARN)([^\\w]|$).*") || lines[y].matches(".*([^\\w]|^)(warning|WARNING)([^\\w]|$).*"))	{	widget.setLineBackground(y, 1, colorLogWarn);}
//				if (lines[y].matches(".*([^\\w]|^)(error|ERROR)([^\\w]|$).*"))	{	widget.setLineBackground(y, 1, colorLogError);}
//				if (lines[y].matches(".*([^\\w]|^)(fatal|FATAL)([^\\w]|$).*"))	{	widget.setLineBackground(y, 1, colorLogFatal);}
//			}
//			widget.setTopIndex(widget.getLineCount()-1);
//		
//			widget.addFocusListener(listener_tabItem_refresh);
//			
//			tabFolder_12.setSelection(tabFolder_12.getItemCount()-1);
//		}
		
	/**
	 * determines whether a Tab is present by Name
	 * @param CTabFolder folder, String TabItemName
	 * @return boolean isTabPresent
	 */
	private boolean isTabPresentByName(CTabFolder tabFolder, String name)
	{
		boolean isPresent = false;
		
		for(int x = 0; x < tabFolder.getItemCount(); x++)
		{
			CTabItem tabItem = tabFolder.getItem(x);
			if (tabItem.getText().equals(name))
			{
				return true;
			}
		}
		return isPresent;
	}
	
	/**
	 * determines the tabIndex by Name
	 * @param CTabFolder folder, String TabItemName
	 * @return int index
	 */
	private int getTabIdByName(CTabFolder tabFolder, String name)
	{
		int tabId = 0;
		
		for(int x = 0; x < tabFolder.getItemCount(); x++)
		{
			CTabItem tabItem = tabFolder.getItem(x);
			if (tabItem.getText().equals(name))
			{
				return x;
			}
		}
		return tabId;
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
	
	/**
	 * checks if java-version is newer than 1.6
	 */
	void checkJavaVersion()
	{
		String javaVersion = System.getProperty("java.version");
		char minor = javaVersion.charAt(2);
		
		if (minor < '6')
		{
			throw new RuntimeException("Java 1.6 or higher is required to run this app. (found version "+javaVersion+")");
		}
		
//		System.out.println("java.home:       "+System.getProperty("java.home"));
//		System.out.println("java.vendor:     "+System.getProperty("java.vendor"));
//		System.out.println("java.version:    "+System.getProperty("java.version"));
//		System.out.println("java.home:       "+System.getProperty("java.home"));
//		System.out.println("java.class.path: "+System.getProperty("java.class.path"));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option help = new Option("help", "print this message");
		Option v = new Option("v", "prints version and build-date");
		/*----------------------------
		  create argument options
		----------------------------*/
		Option dbfile = OptionBuilder.withArgName("dbfile")
				.hasArg()
				.withDescription("[optional] dbfile")
//				.isRequired()
				.create("dbfile");
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( dbfile );
				
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			line = parser.parse( options,  args );
		}
		catch ( Exception exp )
		{
			// oops, something went wrong
			System.err.println( "Parsing failed. Reason: "+ exp.getMessage());
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( line.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
//			formatter.printHelp("checkin --version [% version %]", options);
			formatter.printHelp("pradar-gui", options);
			System.exit(0);
		}
		
		if ( line.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@caegroup.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		

		// gui
		final Display display = new Display();
		
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run()
			{
				try
				{
					Shell shell = new Shell(display);
					shell.setText("pradar "+"[% version %]");

					// set an icon
					if(this.getClass().getResourceAsStream("/logoSymbol50Transp.png") != null)
					{
						shell.setImage(new Image(display, this.getClass().getResourceAsStream("/logoSymbol50Transp.png")));
					}
					else if((new java.io.File("logoSymbol50Transp.png")).exists())
					{
						shell.setImage(new Image(display, "logoSymbol50Transp.png"));
					}
					
					shell.setLayout(new FillLayout());
					shell.setSize(1500, 1000);
					Composite composite = new Composite(shell, SWT.NO_FOCUS);
					GridLayout gl_composite = new GridLayout(2, false);
					gl_composite.marginWidth = 0;
					gl_composite.marginHeight = 0;
					new PradarPartUi3(composite);
					
					try
					{
						shell.open();

						while (!shell.isDisposed())
						{
							if( ! display.readAndDispatch())
							{
								display.sleep();
							}
						}

					}
					finally
					{
						if (!shell.isDisposed())
						{
							shell.dispose();
						}
					}
					
				} finally
				{
					display.dispose();
				}
			}
		});
		System.exit(0);
	}

	/**
	 * @param userAdmin the userAdmin to set
	 */
	public void setUserAdmin(boolean userAdmin) {
		this.userAdmin = userAdmin;
	}

	/**
	 * @return the userAdmin
	 */
	public boolean isUserAdmin() {
		return userAdmin;
	}
}
