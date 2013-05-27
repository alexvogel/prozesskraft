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
import java.util.Iterator;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
	private Text text_process;
	private Text text_user;
	private Text text_host;
	private Text text_active;
	private Spinner spinner_period;
	private Button btnChildren;
	private Button button_refresh = null;
	private Button button_showlog = null;
//	private Button button_radar = null;
//	private Button button_tree = null;
	private Scale scale_zoom;
	private StyledText text_logging = null;
	private Frame frame_radar = null;
	PradarViewModel einstellungen = new PradarViewModel();

	Entity entity_filter = new Entity();
	
	public ArrayList<Entity> entities_all = new ArrayList<Entity>();
	public ArrayList<Entity> entities_filtered;
//	public Entity entity_marked = null;
	
	private int refresh_min_interval = 5000;
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
	License license = null;

	private Text txtTesttext;
	private Composite composite_3;

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarPartUi3()
	{
		loadIni();
//		checkLicense();
		Shell shell = new Shell();
		shell.setSize(633, 767);
		composite_3 = new Composite(shell, SWT.NONE);
		composite_3.setLocation(0, 0);
		createControls(composite_3);
//		applet = new PradarViewProcessingPage(this);
		refresh_last.setTimeInMillis(0);
		refresh();
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PradarPartUi3(Composite composite)
	{
		loadIni();
//		checkLicense();
		applet = new PradarViewProcessingPage(this);
		refresh_last.setTimeInMillis(0);
		refresh();
		createControls(composite);
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite composite)
	{
		composite.setSize(613, 738);
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
		
		text_process = new Text(grpFilter, SWT.BORDER);
		text_process.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblNewLabel_1.setText("user");
		new Label(grpFilter, SWT.NONE);
		
		text_user = new Text(grpFilter, SWT.BORDER);
		text_user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblHost = new Label(grpFilter, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblHost.setText("host");
		new Label(grpFilter, SWT.NONE);
		
		text_host = new Text(grpFilter, SWT.BORDER);
		text_host.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblActive = new Label(grpFilter, SWT.NONE);
		lblActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblActive.setText("active");
		new Label(grpFilter, SWT.NONE);
		
		text_active = new Text(grpFilter, SWT.BORDER);
		text_active.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
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
		grpFunction.setLayout(new GridLayout(2, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("function");
		
		button_refresh = new Button(grpFunction, SWT.NONE);
		button_refresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_refresh.setText("refresh");
		button_refresh.addSelectionListener(listener_refresh_button);
		
		button_showlog = new Button(grpFunction, SWT.NONE);
		button_showlog.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_showlog.setText("showlog");
		button_showlog.addSelectionListener(listener_showlog_button);
		
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
		scale_zoom.setMinimum(50);
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
		
		bindingContextFilter = initDataBindingsFilter();
		bindingContextZoom = initDataBindingsZoom();
//		initDataBindingsPerspective();

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
		entity_filter.setActive(einstellungen.getActive());
		entity_filter.setPeriodInHours(einstellungen.getPeriod());
		// nur entities, die keine eltern haben
		entity_filter.setParentid("0");
		filter();
		applet.refresh();
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
			applet_paint_with_new_filter();
			tree.refresh();
		}
	};
	
//	SelectionAdapter listener_radar_button = new SelectionAdapter()
//	{
//		public void widgetSelected(SelectionEvent event)
//		{
////			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
//			button_tree.setSelection(!button_radar.getSelection());
//			
//			if (button_radar.getSelection())
//			{
//				set_perspective_to_radar();
//			}
//			else
//			{
//				set_perspective_to_tree();
//			}
//		}
//	};
//	
//	SelectionAdapter listener_tree_button = new SelectionAdapter()
//	{
//		public void widgetSelected(SelectionEvent event)
//		{
////			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
//			button_radar.setSelection(!button_tree.getSelection());
//
//			if (button_tree.getSelection())
//			{
//				set_perspective_to_tree();
//			}
//			else
//			{
//				set_perspective_to_radar();
//			}
//		}
//	};
//	
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
		}
	};
	
	SelectionAdapter listener_showlog_button = new SelectionAdapter()
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
			else if (einstellungen.entitySelected != null && (new File(einstellungen.entitySelected.getResource()).exists() ) )
			{
				log("warn", "logfile of entity does not exist "+einstellungen.entitySelected.getId()+" (instance of process '"+einstellungen.entitySelected.getProcess()+"')");
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
	
	MouseWheelListener listener_mousewheel = new MouseWheelListener()
	{
		public void mouseScrolled(MouseEvent me)
		{
			scale_zoom.setSelection(scale_zoom.getSelection() + (me.count*5));
		}
	};
	
	FocusListener listener_tabItem_refresh = new FocusListener()
	{
		public void focusGained(FocusEvent event)
		{
			StyledText widgetFocused = (StyledText) event.getSource();
			int logLineCount = widgetFocused.getLineCount();
		}
		
		public void focusLost(FocusEvent event)
		{
			
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

//	protected DataBindingContext initDataBindingsPerspective()
//	{
//		DataBindingContext bindingContextPerspective = new DataBindingContext();
//		//
//		IObservableValue targetObservableRadar = WidgetProperties.selection().observe(button_radar);
//		IObservableValue modelObservableRadar = BeanProperties.value("perspectiveRadar").observe(einstellungen);
//		bindingContextPerspective.bindValue(targetObservableRadar, modelObservableRadar, null, null);
//		//
//		IObservableValue targetObservableTree = WidgetProperties.selection().observe(button_tree);
//		IObservableValue modelObservableTree = BeanProperties.value("perspectiveTree").observe(einstellungen);
//		bindingContextPerspective.bindValue(targetObservableTree, modelObservableTree, null, null);
//		//
//		return bindingContextPerspective;
//	}
	
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
	
	protected DataBindingContext initDataBindingsRefresh()
	{
		DataBindingContext bindingContextRefresh = new DataBindingContext();
		//
		IObservableValue targetObservableRefresh = WidgetProperties.selection().observe(button_refresh);
		IObservableValue modelObservableRefresh = BeanProperties.value("refresh").observe(einstellungen);
		bindingContextRefresh.bindValue(targetObservableRefresh, modelObservableRefresh, null, null);
		//
		return bindingContextRefresh;
	}
	
	protected DataBindingContext initDataBindingsFilter()
	{

		// Einrichten der ControlDecoration über dem Textfeld 'active'
		final ControlDecoration controlDecorationActive = new ControlDecoration(text_active, SWT.LEFT | SWT.TOP);
		controlDecorationActive.setDescriptionText("use 'true', 'false' or leave field blank");
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecorationActive.setImage(fieldDecoration.getImage());

		// Validator for 'active' mit Verbindung zur Controldecoration
		IValidator validatorActive = new IValidator()
		{
			public IStatus validate(Object value)
			{
				if (value instanceof String)
				{
					if (((String) value).matches("true|false|all|"))
					{
						controlDecorationActive.hide();
						return ValidationStatus.ok();
						
					}
				}
				controlDecorationActive.show();
				return ValidationStatus.error("not a boolean or 'all'");
			}
		};

		// UpdateStrategy fuer 'active' ist: update der werte nur wenn validierung erfolgreich
		UpdateValueStrategy strategyActive = new UpdateValueStrategy();
		strategyActive.setBeforeSetValidator(validatorActive);
		//---------
		
		DataBindingContext bindingContextFilter = new DataBindingContext();
		//
		IObservableValue targetObservableProcess = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_process);
		IObservableValue modelObservableProcess = BeanProperties.value("process").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableProcess, modelObservableProcess, null, null);
		//
		IObservableValue targetObservableUser = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_user);
		IObservableValue modelObservableUser = BeanProperties.value("user").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableUser, modelObservableUser, null, null);
		//
		IObservableValue targetObservableHost = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_host);
		IObservableValue modelObservableHost = BeanProperties.value("host").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableHost, modelObservableHost, null, null);
		//
		IObservableValue targetObservableActive = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_active);
		IObservableValue modelObservableActive = BeanProperties.value("active").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableActive, modelObservableActive, strategyActive, null);
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
	 * refreshes data (entities) from database
	 * @return void
	 */
	void refresh()
	{
		checkLicense();
		now = Calendar.getInstance();
		if ((now.getTimeInMillis() - refresh_last.getTimeInMillis()) > refresh_min_interval)
		{
			loadData();
			filter();
			applet.refresh();
			
			for(PradarViewLogPage logPage : this.logPages)
			{
				if (this.isTabPresentByName(this.tabFolder_12, logPage.getTabName()))
				{
					logPage.refresh();
					log("info", "refreshing logfile "+logPage.getTabName());
				}
			}
		}
		else
		{
			log("warn", "refresh interval must be at least "+(this.refresh_min_interval/1000)+" seconds.");
//			System.out.println("refresh interval must be at least "+(this.refresh_min_interval/1000)+" seconds.");
			
		}
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
		catch (InvalidFileFormatException e1)
		{
			log("error", "invalid fileformat of inifile: "+inifile);
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
							+ "004b46c75f6fe31c9721cb3d37bcd3ca6e80beb6309c43816b6551641a5G"
							+ "02818100979bb432406483b286aa994af0141b619ae38c8b9b82c0766adc"
							+ "d13179e2f6a393a38685f524cd01b382e2ebc215d1dd9d13c05f7f898c1a"
							+ "36df447c282f25d1e04a20988a8ef91dd1fde2af0bb4fa242df3df8070bd"
							+ "d04bc83f4266202a73f303RSA4102413SHA512withRSA9c26a4d464229e9"
							+ "5b40df68620efd5bc408f0d8bb8d99499c465811c498080ad0203010001";

		boolean license_valid = false;		
		boolean das_erste_mal = false;
		if (license == null)
		{
			das_erste_mal = true;
		}
		
		Iterator<String> iterLicenseServerAsPortAtHostname = this.license_server_port_at_hostname.iterator();
		while(iterLicenseServerAsPortAtHostname.hasNext() && (!(license_valid)))
		{
			String portAtHost = iterLicenseServerAsPortAtHostname.next();
			String[] port_and_host = portAtHost.split("@");
			InetAddress inetAddressHost;
			try
			{
				inetAddressHost = InetAddress.getByName(port_and_host[1]);

				license = LicenseValidator.validate(publicKey, "1", "user-edition", "0.1", null, null, inetAddressHost, Integer.parseInt(port_and_host[0]), null, null, null);

				// logging nur beim ersten mal
				if (das_erste_mal)
				{
					log("info", "trying license-server "+portAtHost);
					log("info", "license issued for "+license.getLicenseText().getUserEMail()+ " expires in "+license.getLicenseText().getLicenseExpireDaysRemaining(null)+" day(s).");
					log("info", "license validation returns "+license.getValidationStatus().toString());
				}
				
				switch(license.getValidationStatus())
				{
					case LICENSE_VALID:
						license_valid = true;
					break;
				}
			}
			catch (UnknownHostException e)
			{
				// TODO Auto-generated catch block
				log("warn", "unknown host "+port_and_host[1]);
	//			e.printStackTrace();
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
		this.entities_filtered = entity_filter.getAllMatches(this.entities_all);

		// falls auch children angezeigt werden sollen
		if (einstellungen.getChildren())
		{
			for(int x=0; x<this.entities_filtered.size(); x++)
			{
				Entity entity = this.entities_filtered.get(x);
				for(int y=0; y<this.entities_all.size(); y++)
				{
					Entity possible_child = this.entities_all.get(y);
					if (possible_child.getParentid().equals(entity.getId()))
					{
						this.entities_filtered.add(possible_child);
					}
				}
			}
		}
		log("info", "setting filter...");
		log("info", "amount of entities passing filter: "+this.entities_filtered.size());
	}

	Entity getEntityBySuperId(String superId)
	{
		Entity entityWithSuperId = null;
		Iterator<Entity> iterentity = this.entities_all.iterator();
		while(iterentity.hasNext())
		{
			Entity entity = iterentity.next();
			if ( entity.getSuperid().equals(superId) )
			{
				entityWithSuperId = entity;
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
