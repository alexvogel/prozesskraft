package de.caegroup.pradar.parts;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.MouseWheelEvent;
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

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
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.caegroup.pradar.Db;
import de.caegroup.pradar.Entity;
import de.caegroup.commons.WhereAmI;

import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Spinner;
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
import org.eclipse.wb.swt.SWTResourceManager;

import com.license4j.License;
import com.license4j.LicenseValidator;
import com.license4j.util.FileUtils;

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
	private Button button_clean = null;
	private Button button_delete = null;
	private Scale scale_zoom;
	private StyledText text_logging = null;
	private Frame frame_radar = null;
	PradarViewModel einstellungen = new PradarViewModel();

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
	boolean erster_license_check = true;
	
	private Text txtTesttext;
	private Composite composite_3;

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
		
		Label lblHost = new Label(grpFilter, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblHost.setText("host");
		new Label(grpFilter, SWT.NONE);
		
		combo_hosts = new Combo(grpFilter, SWT.BORDER | SWT.READ_ONLY);
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
		
		// Group function
		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(4, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("functions");
		
		button_refresh = new Button(grpFunction, SWT.NONE);
		GridData gd_button_refresh = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
		gd_button_refresh.widthHint = 62;
		button_refresh.setLayoutData(gd_button_refresh);
		button_refresh.setText("refresh");
		button_refresh.setToolTipText("refresh status of entities from database");
		button_refresh.addSelectionListener(listener_refresh_button);
		
		button_log = new Button(grpFunction, SWT.NONE);
		button_log.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_log.setText("log");
		button_log.setToolTipText("shows logfile of selected process instance");
		button_log.addSelectionListener(listener_log_button);
		
		button_browse = new Button(grpFunction, SWT.NONE);
		button_browse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		button_browse.setText("browse");
		button_browse.setToolTipText("browse instance files.");
		button_browse.addSelectionListener(listener_browse_button);
		
		button_clean = new Button(grpFunction, SWT.NONE);
		button_clean.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_clean.setText("clean");
		button_clean.setToolTipText("checks whether active instances are still alive - disappeared instances will be checked out");
		button_clean.addSelectionListener(listener_clean_button);
		
		button_delete = new Button(grpFunction, SWT.NONE);
		button_delete.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		button_delete.setText("delete");
		button_delete.setToolTipText("deletes a finished (already checked out) process instance from database. includes an implicit 'clean'");
		button_delete.addSelectionListener(listener_delete_button);
		
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
		GridData gd_scale_zoom = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
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
			if (einstellungen.entitySelected != null && (!(einstellungen.entitySelected.getResource().equals(""))))
			{
				log("info", "showing logfile "+einstellungen.entitySelected.getResource());
				showLogFile(einstellungen.entitySelected);
			}
			else if (einstellungen.entitySelected != null && einstellungen.entitySelected.getResource().equals(""))
			{
				log("warn", "no logfile for entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && (new File(einstellungen.entitySelected.getResource()).canRead() ) )
			{
				log("warn", "cannot read logfile of entity "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else if (einstellungen.entitySelected != null && (!(new File(einstellungen.entitySelected.getResource()).exists()) ) )
			{
				log("warn", "logfile of entity does not exist "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
			}
			else
			{
				log("warn", "no entity marked.");
			}
		}
	};
	
	SelectionAdapter listener_browse_button = new SelectionAdapter()
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
				log("warn", "no entity marked.");
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
	
	SelectionAdapter listener_delete_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			if ( (einstellungen.entitySelected != null) && (!(einstellungen.entitySelected.getUser().equals(System.getProperty("user.name")))) )
			{
				log("error", "you may only delete your instances (user "+System.getProperty("user.name")+")");
			}
			
			else if ( (einstellungen.entitySelected != null) && einstellungen.entitySelected.isActive() )
			{
				log("error", "you may only delete finished instances.");
			}
			
			else if (einstellungen.entitySelected != null)
			{
				// bestaetigungsdialog
				Shell shell = new Shell();
				MessageBox confirmation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
				confirmation.setText("please confirm");
				
				String message = "Do you really want to delete this entity from pradar database?\n\n";
				message += "id:\t\t\t"+einstellungen.entitySelected.getId() +"\n";
				message += "process:\t\t"+einstellungen.entitySelected.getProcess() +"\n";
				message += "user:\t\t"+einstellungen.entitySelected.getUser() +"\n";
				message += "host:\t\t"+einstellungen.entitySelected.getHost() +"\n";
				message += "checkin:\t\t"+einstellungen.entitySelected.getCheckinAsString() +"\n";
				message += "checkout:\t"+einstellungen.entitySelected.getCheckoutAsString() +"\n";
				message += "exitcode:\t\t"+einstellungen.entitySelected.getExitcode() +"\n";
				
				confirmation.setMessage(message);
				
				// open confirmation and wait for user selection
				int returnCode = confirmation.open();
//				System.out.println("returnCode is: "+returnCode);

				// ok == 32
				if (returnCode == 32)
				
				{
					Iterator<String> iterPradarServer = pradar_server_port_at_hostname.iterator();
					while(iterPradarServer.hasNext())
					{
						String portAtMachineAsString = iterPradarServer.next();
						String [] port_and_machine = portAtMachineAsString.split("@");
				
						int portNumber = Integer.parseInt(port_and_machine[0]);
						String machineName = port_and_machine[1];
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
				}
				
				// daten und anzeige refreshen
				refresh();
				tree.refresh();

			}
			
			else
			{
				log("warn", "no entity marked.");
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
		checkLicense();
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
				objectOut.writeObject("getall");
	
				// Antwort vom Server lesen. (Liste bereits Druckfertig aufbereitet)
				try
				{
					this.entities_all = (ArrayList<Entity>) objectIn.readObject();
				}
				catch (ClassNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// daten holen aus db
				log("info", "refreshing data...");
				server.close();
				
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				log("warn", "unknown host "+machineName);
				this.pradar_server_port_at_hostname = null;
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
		PradarViewProcessingPage tmp = new PradarViewProcessingPage(this);
		File inifile = WhereAmI.getDefaultInifile(tmp.getClass());
			
		Ini ini;
			
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
			
		// einchecken in die DB
		Socket server = null;
			
		boolean pradar_server_not_found = true;
			
		// ueber alle pradar-server aus ini-file iterieren und den ersten erfolgreichen merken fuer spaetere anfragen
		Iterator<String> iter_pradar_server = pradar_server_list.iterator();
		while(pradar_server_not_found && iter_pradar_server.hasNext() && (this.pradar_server_port_at_hostname.size() == 0))
		{
			String port_and_machine_as_string = iter_pradar_server.next();
			String [] port_and_machine = port_and_machine_as_string.split("@");

			int portNumber = Integer.parseInt(port_and_machine[0]);
			String machineName = port_and_machine[1];
			log("info", "trying pradar-server "+portNumber+"@"+machineName);
			try
			{
				// socket einrichten und Out/Input-Streams setzen
				server = new Socket(machineName, portNumber);
				OutputStream out = server.getOutputStream();
				InputStream in = server.getInputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(out);
				ObjectInputStream  objectIn  = new ObjectInputStream(in);
				
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
		String publicKey =	"30819f300d06092a864886f70d010101050003818d003081893032301006"
							+ "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
							+ "004b460a863476ce8d60591192b45e656da25433f85feb56f0911f79c69G"
							+ "02818100b89d68e21006ec20808c60ba29d992bf3fc519c2109cb7f85f24"
							+ "07bbbd0ba620cf5b40148a4a5ba61e67e2423b528cb73e7db95013405d01"
							+ "a5e083a519fc5ebb5861aa51e785df6e9e2afd7c9dc89b9cbd4edde24278"
							+ "0f52dc58c07f8259c7d803RSA4102413SHA512withRSA5645cb91606642d"
							+ "1d00b916fbde2ebb7954dfe2531abdb5174835b5c09413a6f0203010001";

		boolean license_valid = false;		
		
		for(String portAtHost : this.license_server_port_at_hostname)
		{
			String[] port_and_host = portAtHost.split("@");
			InetAddress inetAddressHost;
			try
			{
				inetAddressHost = InetAddress.getByName(port_and_host[1]);

				License license = LicenseValidator.validate(publicKey, "1", "user-edition", "0.1", null, null, inetAddressHost, Integer.parseInt(port_and_host[0]), null, null, null);

				// logging nur beim ersten mal
				if (erster_license_check)
				{
					log("info", "trying license-server "+portAtHost);
					log("info", "license validation returns "+license.getValidationStatus().toString());
					log("info", "license issued for "+license.getLicenseText().getUserEMail()+ " expires in "+license.getLicenseText().getLicenseExpireDaysRemaining(null)+" day(s).");
					erster_license_check = false;
				}
				
				switch(license.getValidationStatus())
				{
					case LICENSE_VALID:
						license_valid = true;
						break;
					default:
						license_valid = false;
				}
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				log("warn", "unknown host "+port_and_host[1]);
	//			e.printStackTrace();
			}
			
			if (license_valid)
			{
				break;
			}
		}
		
		if (!(license_valid))
		{
			log("fatal", "no valid license found. forcing exit.");
			try
			{
				Thread.sleep(10000);
				System.exit(1);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
//			log("info", "license issued for "+license.getLicenseText().getUserEMail()+ " expires in "+license.getLicenseText().getLicenseExpireDaysRemaining(null)+" day(s).");
		}
	}

//	/**
//	 * check if license is still valid
//	 * @return void
//	 */
//	void checkLicense()
//	{
//		log("info", license.getValidationStatus().toString());
//	}
	
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
	 * opens a seperate Tab and shows the content of a file
	 * @param String pathToFile
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
		
	}

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
					shell.setText("pradar-gui "+"v[% version %]");
					shell.setLayout(new FillLayout());
					shell.setSize(1300, 800);
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
}
