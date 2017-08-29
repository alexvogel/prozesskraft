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
	Shell shell = null;
	
	static CommandLine line;
	private DataBindingContext bindingContextFilter;
	private DataBindingContext bindingContextZoom;
	private Combo combo_processes;
//	private Combo combo_users;
	private Combo combo_exitcodes;
	private Spinner spinner_period;
	private Button btnChildren;
	private Button button_refresh = null;
	private Button button_log = null;
	private Button button_run = null;
	private Button button_stop = null;
	private Button button_kill = null;
	private Button button_browse = null;
	private Button button_open = null;
	private Button button_clean = null;
	private Button button_clone = null;
	private Button button_merge = null;
	private Button button_attend = null;
	private Button button_delete = null;
	private Button button_statistic = null;
	private Scale scale_zoom;
	private StyledText text_logging = null;
	private Frame frame_radar = null;
	PradarViewModel einstellungen = new PradarViewModel();

	public static Ini ini = null;
	
	Entity entity_filter = new Entity();
	
//	public ArrayList<Entity> entities_all = new ArrayList<Entity>();

	public ArrayList<Entity> entities_all = new ArrayList<Entity>();
	public ArrayList<Entity> entities_filtered = new ArrayList<Entity>();
	public Map<String,Entity> idEntities_filtered = new HashMap<String,Entity>();
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

	private PradarPartUi3 This = this;
	
	// wird pradar innerhalb einer groesseren application geoeffnet, wird das beherbergende object hier abgelegt
	private Object pkraft = null;

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarPartUi3()
	{
		checkJavaVersion();
		loadIni();
//		checkLicense();
		shell = new Shell();
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
		shell = composite.getShell();
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
		
//		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
//		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
//		lblNewLabel_1.setText("user");
//		new Label(grpFilter, SWT.NONE);
//		
//		combo_users = new Combo(grpFilter, SWT.BORDER | SWT.READ_ONLY);
//		combo_users.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
//		if(!this.isUserAdmin())
//		{
//			combo_users.setEnabled(false);
//		}
		
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
		
		button_run = new Button(grpFunctionInstance, SWT.NONE);
		button_run.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_run.setText("run");
		button_run.setToolTipText("starts a new manager for the selected instance");
		button_run.addSelectionListener(listener_run_button);

		button_stop = new Button(grpFunctionInstance, SWT.NONE);
		button_stop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_stop.setText("stop");
		button_stop.setToolTipText("stops the manager for the selected instance. already running apps, started by its steps, remain active");
		button_stop.addSelectionListener(listener_stop_button);

		button_kill = new Button(grpFunctionInstance, SWT.NONE);
		button_kill.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_kill.setText("kill");
		button_kill.setToolTipText("stops the manager for the selected instance and kills all apps started by its steps");
		button_kill.addSelectionListener(listener_kill_button);

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
		
		button_merge = new Button(grpFunctionInstance, SWT.NONE);
		button_merge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_merge.setText("merge");
		button_merge.setToolTipText("merge several instances to a new instance. steps downstream of mergepoints will be resettet, content of upstream steps is taken from the first selected entity");
		button_merge.addSelectionListener(listener_merge_button);
		
		button_attend = new Button(grpFunctionInstance, SWT.NONE);
		button_attend.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_attend.setText("attend");
		button_attend.setToolTipText("renew status");
		button_attend.addSelectionListener(listener_attend_button);
		
		button_delete = new Button(grpFunctionInstance, SWT.NONE);
		button_delete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_delete.setText("delete");
		button_delete.setToolTipText("deletes an instance with all its data");
		button_delete.addSelectionListener(listener_delete_button);
		
		button_statistic = new Button(grpFunctionInstance, SWT.NONE);
		button_statistic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_statistic.setText("statistic");
		button_statistic.setToolTipText("shows details about the run");
		button_statistic.addSelectionListener(listener_statistic_button);
		
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

//		// select combo_user to 'aktueller user'
//		combo_users.setText(System.getProperty("user.name"));

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
		
		// mit einen selectionListener die ansicht 'radar' erst initialisieren, wenn sie zum ersten mal selektiert wird
//		tabFolder_12.addSelectionListener(listener_tabitem_selected);
		
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
		entity_filter.setExitcode(einstellungen.getExitcode());
		entity_filter.setPeriodInHours(einstellungen.getPeriod());
		// nur entities, die keine eltern haben
		entity_filter.setParentid("0");
		// nur entities, deren serailVersionUID kleiner oder gleich ist als 1
		entity_filter.setSerialVersionUID("1");
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

	SelectionAdapter listener_run_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if(einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
				return;
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 20)
			{
				log("warn", "run allows max 20 entities at a time");
				return;
			}

			// fuer das markierte entity inen pkraft-manager starten
			for(Entity actEntity : einstellungen.entitiesSelected)
			{
				String pathInstanceDir = new File(actEntity.getResource()).getParent();
	
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
					// pkraft-manager starten
					String call = ini.get("apps", "pkraft-manager") + " -instance " + actEntity.getResource(); 
					log("info", "calling: "+call);
					
					// pradar-attend starten
					String call2 = ini.get("apps", "pradar-attend") + " -instance " + actEntity.getResource(); 
					log("info", "calling: "+call2);
					
					try
					{
						java.lang.Process sysproc = Runtime.getRuntime().exec(call);
						java.lang.Process sysproc2 = Runtime.getRuntime().exec(call2);
					}
					catch (IOException e)
					{
						log("error", e.getMessage());
					}
					
					// daten und anzeige refreshen
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					refresh();
					tree.refresh();

				}
			}
		}
	};

	SelectionAdapter listener_stop_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if(einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
				return;
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 20)
			{
				log("warn", "stop allows max 20 entities at a time");
				return;
			}
			
			// fuer jedes markierte entity einen manager starten
			for(Entity actEntity : einstellungen.entitiesSelected)
			{
				String pathInstanceDir = new File(actEntity.getResource()).getParent();
	
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
					// den pkraft-manager stoppen
					String call = ini.get("apps", "pkraft-manager") + " -stop -instance " + actEntity.getResource(); 
					log("info", "calling: "+call);
					
					// und die daten aktualisieren
					String call2 = ini.get("apps", "pradar-attend") + " -instance " + actEntity.getResource(); 
					log("info", "calling: "+call2);
					try
					{
						java.lang.Process sysproc = Runtime.getRuntime().exec(call);
						java.lang.Process sysproc2 = Runtime.getRuntime().exec(call2);
					}
					catch (IOException e)
					{
						log("error", e.getMessage());
					}
					
					// daten und anzeige refreshen
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					refresh();
					tree.refresh();

				}
			}
		}
	};

	SelectionAdapter listener_kill_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if(einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
				return;
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 20)
			{
				log("warn", "stop allows max 20 entities at a time");
				return;
			}
			
			else if (einstellungen.entitySelected != null)
			{
				// bestaetigungsdialog
				Shell diaShell = new Shell();
				MessageBox confirmation = new MessageBox(diaShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				String message = "";

				message += "you are about to kill selected instance(s).\n\n";
				message += "this will kill\n";
				message += "- running programs that have been launched by this instance(s)\n";
				message += "- running calculations on an HPC that are related to this instance(s)";

				confirmation.setMessage(message);

				// open confirmation and wait for user selection
				int returnCode = confirmation.open();
//					System.out.println("returnCode is: "+returnCode);

				// ok == 32
				if (returnCode == 32)
				{
			
					// fuer jedes markierte entity einen manager starten
					for(Entity actEntity : einstellungen.entitiesSelected)
					{
						String pathInstanceDir = new File(actEntity.getResource()).getParent();
			
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
							// den pkraft-manager stoppen
							String call = ini.get("apps", "pkraft-manager") + " -stop -kill -instance " + actEntity.getResource(); 
							log("info", "calling: "+call);
							
							// und die daten aktualisieren
							String call2 = ini.get("apps", "pradar-attend") + " -instance " + actEntity.getResource(); 
							log("info", "calling: "+call2);
							try
							{
								java.lang.Process sysproc = Runtime.getRuntime().exec(call);
								java.lang.Process sysproc2 = Runtime.getRuntime().exec(call2);
							}
							catch (IOException e)
							{
								log("error", e.getMessage());
							}
							
							// daten und anzeige refreshen
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							refresh();
							tree.refresh();
		
						}
					}
				}
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

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 5)
			{
				log("warn", "browse allows max 5 entities at a time");
				return;
			}

			// fuer jedes markierte entity einen filebrowser oeffnen
			for(Entity actEntity : einstellungen.entitiesSelected)
			{
				String pathInstanceDir = new File(actEntity.getResource()).getParent();
	
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
		}
	};

	
	SelectionAdapter listener_open_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// ist ueberhaupt etwas markiert?
			if (einstellungen.entitiesSelected == null || einstellungen.entitiesSelected.size() == 0)
			{
				log("warn", "no instance selected");
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 5)
			{
				log("warn", "open allows max 5 entities at a time");
				return;
			}

			// wenn mit der markierung alles in Ordnung ist
			else
			{
				// fuer jedes markierte entity ein pmodel oeffnen
				for(Entity actEntity : einstellungen.entitiesSelected)
				{
					java.io.File pmbFile = new java.io.File(actEntity.getResource());
					
					if(!(pmbFile.exists()))
					{
						log("error", "process-model-file does not exist: " + pmbFile.getAbsolutePath());
					}
					
					else
					{
						openInstance(actEntity);
					}
					
				}
			}
		}
	};	

	/**
	 * oeffnen einer instance mit pmodel
	 */
	public void openInstance(Entity entity)
	{
		// wurde pradar-gui im kontext der gesamtapplication geoeffnet?, dann soll pmodel auch dort geoeffnet werden
		if(pkraft != null)
		{
			log("info", "opening instance file for inspection in new tab");
			IPkraftPartUi1 lulu = (IPkraftPartUi1)pkraft;
			try
			{
				lulu.openInstance(entity.getResource());
			}
			catch(IOException e)
			{
				log("error", "error when reading instance file "+entity.getResource());
				log("fatal", e.getMessage());
			}
		}

		// wurde pradar standalone geoeffnet, soll pmodel auch standalone geoeffnet werden
		else
		{
			log("info", "opening instance file for inspection");
			String aufruf = ini.get("apps",  "pmodel") + " -instance "+entity.getResource();
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

	
	SelectionAdapter listener_clone_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// ist ueberhaupt etwas markiert?
			if (einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 1)
			{
				log("warn", "clone allows max 1 entity at a time");
				return;
			}
			
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getUser().equals(System.getProperty("user.name")))) )
			{
				log("warn", "you may only clone your instances (user "+System.getProperty("user.name")+")");
			}
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getParentid().equals("0"))) )
			{
				log("warn", "you must not clone child instances");
			}

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
//					System.out.println("returnCode is: "+returnCode);

				// ok == 32
				if (returnCode == 32)
				{
					// creating and setting a busy cursor
					shell.setCursor(new Cursor(display, SWT.CURSOR_WAIT));

					// clone ausfuehren
					execute_clone(einstellungen.entitySelected);
					
					// creating and setting an arrow cursor
					shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
				}

				// daten und anzeige refreshen
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				refresh();
				tree.refresh();
			}
		}
	};	

	/**
	 * klont einen Prozess
	 * 
	 * @param entity
	 * @return String pathToResourceOfClone
	 */
	public String execute_clone(Entity entity)
	{
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
			Process p1 = new Process();
			p1.setInfilebinary(entity.getResource());
			Process process = p1.readBinary();
			
			log("info", "cloning instance " + process.getId2() + ", " + process.getId() + ", " + process.getInfilebinary());
			Process clone = this.cloneProcess(process, null);

			// falls children vorhanden, sollen diese auch geklont werden
			for(Entity possibleChild : entities_filtered)
			{
				// ist es ein child?
				if(possibleChild.getParentid().equals(entity.getId()))
				{
					// Process Object einlesen und clonen
					Process processChild1 = new Process();
					processChild1.setInfilebinary(possibleChild.getResource());
					Process processChild = processChild1.readBinary();
					log("info", "cloning child instance " + processChild.getId2() + ", " + processChild.getId() + ", " + processChild.getInfilebinary());
					this.cloneProcess(processChild, clone);
				}
			}
			
			return clone.getRootdir() + "/process.pmb";
		}
		
		return null;
	}
	
	/**
	 * clone Process mit Daten
	 * returns process-id
	 * @param entity
	 */
	public Process cloneProcess(Process process, Process parentProcess)
	{

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

//		// das original speichern, weil auch hier aenderungen vorhanden sind (zaehler fuer klone)
		process.setOutfilebinary(process.getInfilebinary());
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

	/**
	 * mergen mehrerer instanzen
	 */
	SelectionAdapter listener_merge_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// ist ueberhaupt etwas markiert?
			if (einstellungen.entitiesSelected == null || einstellungen.entitiesSelected.size() == 0)
			{
				log("error", "no instance selected");
			}

			// ist weniger als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() < 2)
			{
				log("error", "merge needs min 2 entities");
				return;
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 5)
			{
				log("error", "merge allows max 5 entities at a time");
				return;
			}

			else if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getUser().equals(System.getProperty("user.name")))) )
			{
				log("error", "you may only merge your instances (user "+System.getProperty("user.name")+")");
				return;
			}
			else if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getParentid().equals("0"))) )
			{
				log("error", "you must not merge child instances");
				return;
			}
			else
			{
				// sind alle selected instances vom selben prozess und version
				String lastProcessName = null;
				String lastProcessVersion = null;
				for(Entity actEntity : einstellungen.entitiesSelected)
				{
					if(lastProcessName == null)
					{
						lastProcessName = actEntity.getProcess();
					}
					else
					{
						if(!lastProcessName.equals(actEntity.getProcess()))
						{
							log("error", "you may only merge instances of same process");
							return;
						}
					}
					if(lastProcessVersion == null)
					{
						lastProcessVersion = actEntity.getVersion();
					}
					else
					{
						if(!lastProcessVersion.equals(actEntity.getVersion()))
						{
							log("error", "you may only merge instances of same version");
							return;
						}
					}
					
					java.io.File fileResource = new java.io.File(actEntity.getResource());
					// testen .. dass die daten existieren
					if(!fileResource.exists())
					{
						log("error", "resource does not exist: "+fileResource.getAbsolutePath());
						return;
					}
					else if(!fileResource.isFile())
					{
						log("error", "resource is not a file: "+fileResource.getAbsolutePath());
						return;
					}
					else if(!fileResource.canRead())
					{
						log("error", "cannot read resource: "+fileResource.getAbsolutePath());
						return;
					}
				}

				// bestaetigungsdialog
				Shell diaShell = new Shell();
				MessageBox confirmation = new MessageBox(diaShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				String message = "";

				message += "you are about to merge several instances into a new created instance.\n";
				message += "a clone of the first entity in your selection sequence will be used as the base instance.\n";
				message += "the other instances will be merged into the base instance.\n";
				message += "now existing instances remain unchanged.\n";
				message += "depending on the amount of data the merging can take some time.\n\n";
				message += "do you really want to proceed?";

				confirmation.setMessage(message);

				// open confirmation and wait for user selection
				int returnCode = confirmation.open();
//				System.out.println("returnCode is: "+returnCode);

				// ok == 32
				if (returnCode == 32)
				{
					// creating and setting a busy cursor
					shell.setCursor(new Cursor(event.display, SWT.CURSOR_WAIT));

					// alle selectionen durcharbeiten und sicherstellen...
					Process cloneOfBaseInstance = null;
					
					// alle dependent steps der zielinstanz einsammeln
					// dies wird zum resetten benoetigt, damit steps nicht doppelt resettet werden
					Map<Step,String> dependentSteps = new HashMap<Step,String>();

					// den main-prozess einlesen um subprozesse extrahieren zu koennen (obwohl das cloning weiter unten stattfindet)
					Process p3 = new Process();
					p3.setInfilebinary(einstellungen.entitiesSelected.get(0).getResource());
					Process baseProcess = p3.readBinary();

					for(Entity actEntity : einstellungen.entitiesSelected)
					{
						// die instanz der ersten selection vollstaendig klonen (baseProcess)
						if(cloneOfBaseInstance == null)
						{
							log("info", "start cloning first selected instance as base instance: " + actEntity.getResource());

							// clone ausfuehren fuer die erste selection der zu mergenden entities
							String pathToResourceOfClone = execute_clone(actEntity);

							Process p33 = new Process();
							p33.setInfilebinary(pathToResourceOfClone);
							cloneOfBaseInstance = p33.readBinary();
							log("info", "end cloning first selected instance as base instance: " + pathToResourceOfClone );
						}
						// die instanzen aller anderen selektionen sollen in die instanz der ersten selektion gemergt werden
						else
						{
							log("info", "merging guest process " + actEntity.getResource());

							Process p1 = new Process();
							p1.setInfilebinary(actEntity.getResource());
							Process actGuestProcess = p1.readBinary();

							// merge durchfuehren
							// alle fanned steps (ehemalige multisteps) des zu mergenden prozesses in die fanned multisteps des bestehenden prozesses integrieren
							for(Step actStep : actGuestProcess.getStep())
							{
								if(actStep.isAFannedMultistep())
								{
									log("info", "merging into base instance the step " + actStep.getName() + " from guest instance " + actGuestProcess.getInfilebinary());
									if(cloneOfBaseInstance.integrateStep(actStep))
									{
										log("info", "merging step successfully.");
										// die downstream steps vom merge-punkt merken
										for(Step actStepToResetBecauseOfDependency : cloneOfBaseInstance.getStepDependent(actStep.getName()))
										{
											dependentSteps.put(actStepToResetBecauseOfDependency, "dummy");
										}

										// der step einen subprocess enthaelt muss der subprocess nach der integration bei pradar gemeldet werden
										// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
										if(actStep.getSubprocess() != null && actStep.getSubprocess().getProcess() != null)
										{
											String call5 = ini.get("apps", "pradar-attend") + " -instance " + actStep.getAbsdir() + "/process.pmb"; 
											System.err.println("info: calling: "+call5);
											try
											{
												java.lang.Process sysproc = Runtime.getRuntime().exec(call5);
											}
											catch (IOException e)
											{
												System.err.println("error: " + e.getMessage());
												log("error", e.getMessage());
											}
										}
										
									}
									else
									{
										log("error", "merging step failed.");
									}
								}
								else
								{
									System.err.println("debug: because it's not a multistep, ignoring from external instance step " + actStep.getName());
								}
							}
						}
					}

					// alle steps downstream der merge-positionen resetten
					for(Step actStep : dependentSteps.keySet())
					{
						actStep.resetBecauseOfDependency();
					}

					// speichern der ergebnis instanz
					cloneOfBaseInstance.setOutfilebinary(cloneOfBaseInstance.getRootdir() + "/process.pmb");
					cloneOfBaseInstance.writeBinary();

					// den prozess in pradar anmelden durch aufruf des tools: pradar-attend
					String call2 = ini.get("apps", "pradar-attend") + " -instance " + cloneOfBaseInstance.getRootdir() + "/process.pmb"; 
					log("info", "calling: "+call2);

					try
					{
						java.lang.Process sysproc = Runtime.getRuntime().exec(call2);
					}
					catch (IOException e)
					{
						log("error", e.getMessage());
					}
				}
			}
			
			// creating and setting an arrow cursor
			shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
			
			// daten und anzeige refreshen
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refresh();
			tree.refresh();
		}

	};	
	
	
	SelectionAdapter listener_attend_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// ist ueberhaupt etwas markiert?
			if (einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
				return;
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 100)
			{
				log("warn", "attend allows max 100 entity at a time");
				return;
			}

			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getUser().equals(System.getProperty("user.name")))) )
			{
				log("error", "you may only attend your instances (user "+System.getProperty("user.name")+")");
			}

			// den aufruf zusammenstellen
			String aufruf = ini.get("apps",  "pradar-attend");
			for(Entity actEntity : einstellungen.entitiesSelected)
			{
				aufruf += " -instance " + actEntity.getResource();
			}

			log("info", "attending instance file: " + aufruf);
	
			try
			{
				java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// daten und anzeige refreshen
			refresh();
			tree.refresh();

		}
		
	};	


	SelectionAdapter listener_statistic_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// ist ueberhaupt etwas markiert?
			if (einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 1)
			{
				log("warn", "statistic allows max 1 entity at a time");
				return;
			}

			else if (einstellungen.entitySelected != null)
			{
				// Prozess einladen
				java.io.File processBinary = new java.io.File(einstellungen.entitySelected.getResource());
				if(!processBinary.exists() || processBinary.isDirectory())
				{
					log("error", "process instance file does not exist: " + processBinary.getAbsolutePath());
					return;
				}
				
				Process p1 = new Process();
				p1.setInfilebinary(processBinary.getAbsolutePath());
				Process p2 = p1.readBinary();
				
				new StatisticProcess(shell, This, p2);
			}
			
		}
	};	
	
	
	SelectionAdapter listener_delete_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			// ist ueberhaupt etwas markiert?
			if (einstellungen.entitySelected == null)
			{
				log("warn", "no instance selected");
			}

			// ist mehr als eine bestimmte zahl markiert
			else if(einstellungen.entitiesSelected != null && einstellungen.entitiesSelected.size() > 1)
			{
				log("warn", "delete allows max 1 entity at a time");
				return;
			}
			
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

	
	protected DataBindingContext initDataBindingsFilter()
	{

		
		DataBindingContext bindingContextFilter = new DataBindingContext();
		//
		IObservableValue targetObservableProcess = WidgetProperties.text().observe(combo_processes);
		IObservableValue modelObservableProcess = BeanProperties.value("process").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableProcess, modelObservableProcess, null, null);
		//
//		IObservableValue targetObservableUser = WidgetProperties.text().observe(combo_users);
//		IObservableValue modelObservableUser = BeanProperties.value("user").observe(einstellungen);
//		bindingContextFilter.bindValue(targetObservableUser, modelObservableUser, null, null);
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
//		IObservableList targetObservableUsers = WidgetProperties.items().observe(combo_users);
//		IObservableList modelObservableUsers = BeanProperties.list("users").observe(einstellungen);
//		bindingContextComboItems.bindList(targetObservableUsers, modelObservableUsers, null, null);
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
		for(String portAtMachineAsString : this.pradar_server_port_at_hostname)
		{
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
//				log("debug", "write: getallfromuser");
				objectToServer.writeObject("getallfromuser");
				objectToServer.writeObject(System.getProperty("user.name"));

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

					// alle existierenden entities loeschen
					this.entities_all.clear();
					
					// die neuen entities casten und in einem map unterbringen id->Entities erstellen um die children bei ihren parents einsortieren zu koennen
					Map<String,Entity> entities_all = new HashMap<String,Entity>();
					for(Object actObject : serverAnswer2)
					{
						// 
						if(actObject instanceof Entity)
						{
//							log("debug", "item of ArrayList<Object> is an Entity  --->  adding to ArrayList<Entity>");
							Entity newEntity = (Entity) actObject;
							this.entities_all.add(newEntity);
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
		// die entities die streng nach dem filter ausgesiebt werden
		this.entities_filtered = entity_filter.getAllMatches(this.entities_all);

		// falls auch children angezeigt werden sollen
		if (einstellungen.getChildren())
		{
			ArrayList<Entity> newEntities_filtered = new ArrayList<Entity>();
			for(Entity actEntity : this.entities_filtered)
			{
				// die bisherigen nicht vergessen
				newEntities_filtered.add(actEntity);

				// und alle ihre kinder
				for(Entity actEntityPossibleChild : this.entities_all)
				{
//					if (possible_child.getParentid().equals(entity.getId()))
//					Entity possible_child = this.entities_all.get(y);
					if(actEntityPossibleChild.getParentid().equals(actEntity.getId()) )
//					if ( (actualEntityPossibleChild.getParentid().equals(actualEntity.getId())) && (actualEntityPossibleChild.getHost().equals(actualEntity.getHost())) && (actualEntityPossibleChild.getUser().equals(actualEntity.getUser()))  )
//					if (actualEntityPossibleChild.getParentid().equals(actualEntity.getId()) && actualEntityPossibleChild.getUser().equals(actualEntity.getUser()) )
//					if (actualEntityPossibleChild.getParentid().equals(actualEntity.getId())  )
					{
//						if(!(this.entities_filtered.contains(actualEntityPossibleChild)))
						newEntities_filtered.add(actEntityPossibleChild);
//						System.out.println("another child found");
					}
				}
			}
			this.entities_filtered = newEntities_filtered;
			
			// einen map fuer schnellen zugriff erstellen
			for(Entity actEntity : this.entities_filtered)
			{
				this.idEntities_filtered.put(actEntity.getId(), actEntity);
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
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(PradarPartUi3.class) + "/" + "../etc/pradar-gui.ini");

		if (inifile.exists())
		{
			try
			{
				ini = new Ini(inifile);
			}
			catch (InvalidFileFormatException e1)
			{
			// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IOException e1)
			{
			// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else
		{
			System.err.println("ini file does not exist: "+inifile.getAbsolutePath());
			System.exit(1);
		}

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
		
		/*----------------------------
		  other things
		----------------------------*/

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
//					shell.setSize(1500, 1000);
					shell.setMaximized(true);
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

	/**
	 * @return the pkraft
	 */
	public Object getPkraft() {
		return pkraft;
	}

	/**
	 * @param pkraft the pkraft to set
	 */
	public void setPkraft(Object pkraft) {
		this.pkraft = pkraft;
	}
}
