package de.caegroup.pradar.parts;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.MouseWheelEvent;

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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

public class PradarPartUi3 extends ModelObject
{
	static CommandLine line;
	private DataBindingContext bindingContextFilter;
	private DataBindingContext bindingContextZoom;
//	private DataBindingContext bindingContext;
	private Text text_process;
	private Text text_user;
	private Text text_host;
	private Text text_active;
	private Spinner spinner_period;
	private Button button_children;
	private Scale scale_zoom;
	private Table table;
	private PradarViewModel einstellungen = new PradarViewModel();
	private Entity filter_entity = new Entity();
	PradarViewProcessingPage applet;
	Display display;

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PradarPartUi3()
	{
		Shell shell = new Shell();
		shell.setSize(633, 688);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLocation(0, 0);
		createControls(composite);
		applet = new PradarViewProcessingPage(filter_entity, einstellungen);
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PradarPartUi3(Composite composite)
	{
		createControls(composite);
		applet = new PradarViewProcessingPage(filter_entity, einstellungen);
	}

	/**
	 * constructor als EntryPoint fuer Main falls das dbfile mitgeliefert wird
	 */
	@Inject
	public PradarPartUi3(Composite composite, String pathdbfile)
	{
		applet = new PradarViewProcessingPage(pathdbfile, filter_entity, einstellungen);
		createControls(composite);
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite composite)
	{

		composite.setSize(613, 649);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.marginWidth = 0;
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite_1.heightHint = 445;
		gd_composite_1.widthHint = 122;
		composite_1.setLayoutData(gd_composite_1);
		
		Composite composite_11 = new Composite(composite_1, SWT.NONE);
		composite_11.setLayout(new GridLayout(1, false));
		GridData gd_composite_11 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_11.heightHint = 437;
		gd_composite_11.widthHint = 169;
		composite_11.setLayoutData(gd_composite_11);
		
		Group grpFilter = new Group(composite_11, SWT.NONE);
		grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpFilter.setText("filter");
		grpFilter.setLayout(new GridLayout(3, false));
		
		Label lblNewLabel = new Label(grpFilter, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		lblNewLabel.setText("process");
		
		text_process = new Text(grpFilter, SWT.BORDER);
		text_process.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblNewLabel_1.setText("user");
		
		text_user = new Text(grpFilter, SWT.BORDER);
		text_user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblHost = new Label(grpFilter, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblHost.setText("host");
		
		text_host = new Text(grpFilter, SWT.BORDER);
		text_host.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblActive = new Label(grpFilter, SWT.NONE);
		lblActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblActive.setText("active");
		
		text_active = new Text(grpFilter, SWT.BORDER);
		text_active.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		
		Label lblPeriod = new Label(grpFilter, SWT.NONE);
		lblPeriod.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblPeriod.setText("active in last");
		
		spinner_period = new Spinner(grpFilter, SWT.BORDER);
		spinner_period.setMaximum(8064);
		spinner_period.setSelection(168);
		new Label(grpFilter, SWT.NONE);
		
		Label lblNewLabel_3 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_3.setText("hours");
		
		button_children = new Button(grpFilter, SWT.CHECK);
		button_children.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		button_children.setText("show children");
		
		Group grpVisual = new Group(composite_11, SWT.NONE);
		grpVisual.setText("visual");
		grpVisual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpVisual.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel_2 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_2.setText("zoom");
		
		scale_zoom = new Scale(grpVisual, SWT.NONE);
		scale_zoom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale_zoom.setMaximum(1000);
		scale_zoom.setMinimum(50);
		scale_zoom.setSelection(100);
		
		Button btnNewButton2 = new Button(grpVisual, SWT.NONE);
		btnNewButton2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnNewButton2.setText("autoscale");
		btnNewButton2.addSelectionListener(listener_autoscale_button);
		scale_zoom.addMouseWheelListener(listener_mousewheel);
		
		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(1, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("function");
		
		Button btnNewButton = new Button(grpFunction, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnNewButton.setText("refresh");
		btnNewButton.addSelectionListener(listener_refresh_button);
		
		Composite composite_12 = new Composite(composite_1, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite_12.setLayout(new GridLayout(1, false));
		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_12.heightHint = 390;
		gd_composite_12.minimumWidth = 10;
		gd_composite_12.minimumHeight = 10;
		composite_12.setLayoutData(gd_composite_12);
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_2.heightHint = 164;
		composite_2.setLayoutData(gd_composite_2);
		
		CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(composite_2, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		checkboxTableViewer.setContentProvider(new ContentProvider());
		
		bindingContextFilter = initDataBindingsFilter();
		bindingContextZoom = initDataBindingsZoom();

		Frame frame = SWT_AWT.new_Frame(composite_12);

		frame.add(applet, BorderLayout.CENTER);
		applet.init();
		frame.pack();
		frame.setLocation(0, 0);
		frame.setVisible(true);

		updateUserInterface(einstellungen);
		updateUserInterfaceProcessing(einstellungen);
		applet_paint_with_new_filter();
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
		filter_entity.setProcess(einstellungen.getProcess());
		filter_entity.setUser(einstellungen.getUser());
		filter_entity.setHost(einstellungen.getHost());
		filter_entity.setActive(einstellungen.getActive());
		filter_entity.setPeriodInHours(einstellungen.getPeriod());
		filter_entity.setParentidAsBoolean(einstellungen.getChildren());
//		System.out.println("period aus einstellungen in stunden: "+einstellungen.getPeriod());
//		System.out.println("period aus filter_entity in Stunden: "+filter_entity.getPeriodInHours());
//		System.out.println("period aus filter_entity in Millis: "+filter_entity.getPeriodInMillis());
		
		applet.setFilter(filter_entity);
	}
	public void applet_paint_with_new_zoom()
	{
		applet.setZoomfaktor(einstellungen.getZoom());
	}
	public void applet_refresh()
	{
		applet.refresh();
	}
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
		table.setFocus();
	}
	
	IChangeListener listener = new IChangeListener()
	{
		public void handleChange(ChangeEvent event)
		{
			applet_paint_with_new_filter();
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
			applet_refresh();
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
			b.getModel().addChangeListener(listener);
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

		// Einrichten der ControlDecoration über dem Textfeld 'active'
		final ControlDecoration controlDecorationActive = new ControlDecoration(text_active, SWT.LEFT | SWT.TOP);
		controlDecorationActive.setDescriptionText("use 'true', 'false', 'all' or leave field blank");
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
		IObservableValue targetObservableChildren = WidgetProperties.selection().observeDelayed(800, button_children);
		IObservableValue modelObservableChildren = BeanProperties.value("children").observe(einstellungen);
		bindingContextFilter.bindValue(targetObservableChildren, modelObservableChildren, null, null);
		//
		return bindingContextFilter;
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
					if (line.hasOption("dbfile"))
					{
						new PradarPartUi3(composite, line.getOptionValue("dbfile"));
					}
					else
					{
						new PradarPartUi3(composite);
					}
					
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
