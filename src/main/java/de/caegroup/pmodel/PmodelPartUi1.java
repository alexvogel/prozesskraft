package de.caegroup.pmodel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.MouseWheelEvent;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;

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
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;

import de.caegroup.gui.step.insight.InsightCreator;
import de.caegroup.process.Process;

public class PmodelPartUi1 extends ModelObject
{
	static CommandLine line;
	private DataBindingContext bindingContextVisual;
	private DataBindingContext bindingContextMarked;
	private Scale scale_zoom;
	private Spinner spinner_textsize;
	private Spinner spinner_labelsize;
	private Scale scale_gravx;
	private Scale scale_gravy;

	private Process process = new Process();
	
	private Label label_marked = null;
	public PmodelViewModel einstellungen = new PmodelViewModel();
	private StyledText text_logging = null;
	PmodelViewPage applet;
	Display display;

	Shell shell_dummy_insight;
	Composite actualStepInsight = null;
	CTabFolder tabFolder_12;
	Map<String,Composite> stepInsight = new HashMap();

	final Color colorLogError = new Color(new Shell().getDisplay(), 215, 165, 172);
	final Color colorLogWarn = new Color(new Shell().getDisplay(), 202, 191, 142);
	final Color colorLogInfo = new Color(new Shell().getDisplay(), 184, 210, 176);

	int logLineCount = 0;

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PmodelPartUi1()
	{
		Shell shell = new Shell();
		shell.setSize(633, 688);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLocation(0, 0);
		createControls(composite);
		applet = new PmodelViewPage(einstellungen);
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PmodelPartUi1(Composite composite)
	{
		applet = new PmodelViewPage(einstellungen);
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Main falls das dbfile mitgeliefert wird
	 */
	@Inject
	public PmodelPartUi1(Composite composite, String pathToProcessFile)
	{
		// binary file einlesen
		if (pathToProcessFile.matches(".+\\.pmb$"))
		{
			log("warn", "assuming binary format.");
			this.process.setInfilebinary(pathToProcessFile);
			this.process = this.process.readBinary();
		}
		
		// xml-format einlesen
		else if(pathToProcessFile.matches(".+\\.xml$|.+\\.pmx$"))
		{
			log("warn", "assuming xml format.");
			this.process.setInfilexml(pathToProcessFile);
			try
			{
				this.process = process.readXml();
			} catch (JAXBException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else
		{
			log("fatal", "unknown file extension. please use only 'pmb', 'pmx' or 'xml'.");
		}
		
		einstellungen.setRootpositionratiox((float)0.5);
		einstellungen.setRootpositionratioy((float)0.1);
		applet = new PmodelViewPage(process, einstellungen);
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
		GridLayout gl_composite_1 = new GridLayout(4, false);
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
		
		Group grpVisual = new Group(composite_11, SWT.NONE);
		grpVisual.setText("visual");
		grpVisual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpVisual.setLayout(new GridLayout(2, false));
		
		Label lblNewLabel_2 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_2.setText("zoom");
		
		scale_zoom = new Scale(grpVisual, SWT.NONE);
		scale_zoom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale_zoom.setMaximum(200);
		scale_zoom.setMinimum(10);
		scale_zoom.setSelection(100);
		scale_zoom.addMouseWheelListener(listener_mousewheel);
		
		Label lblNewLabel_3 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_3.setText("label");
		
		spinner_labelsize = new Spinner(grpVisual, SWT.BORDER);
		spinner_labelsize.setMaximum(20);
		spinner_labelsize.setSelection(10);
		spinner_labelsize.setMinimum(0);
//		new Label(grpVisual, SWT.NONE);
		
		Label lblNewLabel_4 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_4.setText("text");
		
		spinner_textsize = new Spinner(grpVisual, SWT.BORDER);
		spinner_textsize.setMaximum(20);
		spinner_textsize.setSelection(10);
		spinner_textsize.setMinimum(0);
		
		scale_gravx = new Scale(grpVisual, SWT.NONE);
		scale_gravx.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale_gravx.setMaximum(100);
		scale_gravx.setMinimum(0);
		scale_gravx.setSelection(1);
		
		Label lblNewLabel_5 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_5.setText("gravX");
		
		scale_gravy = new Scale(grpVisual, SWT.NONE);
		scale_gravy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale_gravy.setMaximum(100);
		scale_gravy.setMinimum(0);
		scale_gravy.setSelection(10);
//		scale_gravx.addMouseWheelListener(listener_mousewheel);
		
		Label lblNewLabel_6 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_6.setText("gravX");
		//		scale_gravx.addMouseWheelListener(listener_mousewheel);
				
		Button btnNewButton2 = new Button(grpVisual, SWT.NONE);
		btnNewButton2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		btnNewButton2.setText("autoscale");
		btnNewButton2.addSelectionListener(listener_autoscale_button);
		
		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(1, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("function");
		
		Button btnNewButton = new Button(grpFunction, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnNewButton.setText("refresh");
		btnNewButton.addSelectionListener(listener_refresh_button);
		
		label_marked = new Label(composite_11, SWT.NONE);
		label_marked.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		label_marked.setText("New Label");
		
		SashForm sashForm = new SashForm(composite_1, SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		new Label(composite_1, SWT.NONE);

		Composite composite_12 = new Composite(sashForm, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite_12.setLayout(new GridLayout(1, false));
		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_12.heightHint = 390;
		gd_composite_12.minimumWidth = 10;
		gd_composite_12.minimumHeight = 10;
		composite_12.setLayoutData(gd_composite_12);
		
		Frame frame = SWT_AWT.new_Frame(composite_12);
		
		frame.add(applet, BorderLayout.CENTER);
		applet.init();
		frame.pack();
		frame.setLocation(0, 0);
		frame.setVisible(true);

		Composite composite_13 = new Composite(sashForm, SWT.BORDER);
		composite_13.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_composite_13 = new GridLayout(1, false);
		gl_composite_13.marginWidth = 0;
		gl_composite_13.marginHeight = 0;
		composite_13.setLayout(gl_composite_13);
//		tabFolder_13.addSelectionListener(listener_tabFolder_selection);
		new Label(composite_1, SWT.NONE);
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_2.heightHint = 164;
		composite_2.setLayoutData(gd_composite_2);
		
//		text_logging = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
//		text_logging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		text_logging = new StyledText(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		text_logging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// virtuelle ablage fuer gerade nicht anzuzeigende composites
		shell_dummy_insight = new Shell(display);
//		shell_dummy_insight.setLayout(new FillLayout());
		
		// erzeugen der 'step-insight'-Ansicht
		createControlsInsight(composite_13);
		
		bindingContextVisual = initDataBindingsVisual();
		bindingContextMarked = initDataBindingsMarked();
		
		
	}

	/**
	 * erzeugen der 'insight'-Ansicht fuer den aktuell markierten step
	 * @param parent
	 */
	public void createControlsInsight(Composite parent)
	{

		log("info", "showing details for step "+einstellungen.getMarkedStepName());
		
		// wenn es fuer diese ansicht schon einen composite gibt, dann diesen anzeigen
		if ( this.stepInsight.containsKey(einstellungen.getMarkedStepName()) )
		{
			if (actualStepInsight != null)
			{
				// das aktuell sichtbare composite 'stepInsight' wegnehmen
				actualStepInsight.setParent(shell_dummy_insight);
				actualStepInsight.setVisible(false);
			}
			
			// um es spaeter wieder umlegen zu koennen im field ablegen
			actualStepInsight = this.stepInsight.get(einstellungen.getMarkedStepName());
			
			// das bestehende composite auf das parent setzen
			actualStepInsight.setParent(parent);
			log("debug", "reactivating existing controls");
		}

		else
		{
			// ein eventuell aktuell angezeigtes composite wegnehemn
			if (actualStepInsight != null)
			{
				// das aktuell sichtbare composite 'stepInsight' wegnehmen
				actualStepInsight.setParent(shell_dummy_insight);
				actualStepInsight.setVisible(false);
			}
			
			// ein neues composite erzeugen und mit inhalt befuellen
			actualStepInsight = new Composite(shell_dummy_insight, SWT.NONE);
			actualStepInsight.setLayout(new FillLayout());
			GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			actualStepInsight.setLayoutData(gd_composite);

			new InsightCreator(actualStepInsight, process.getStep(einstellungen.getMarkedStepName()));
			
			// im stapel ablegen fuer spaeter
			this.stepInsight.put(einstellungen.getMarkedStepName(), actualStepInsight);
			
			// und auf die sichtbare flaeche legen
			actualStepInsight.setParent(parent);
		}

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

	public void applet_paint_with_new_visual()
	{
//		applet.setZoomfaktor(einstellungen.getZoom());
	}
	public void applet_refresh()
	{
		applet.refresh();
	}
	public void applet_autoscale()
	{
		this.einstellungen.setZoom(100);
		//		applet.autoscale();
	}
	
	
	@PreDestroy
	public void dispose()
	{
	}

	@Focus
	public void setFocus()
	{
//		table.setFocus();
	}
	
//	IChangeListener listener_visual = new IChangeListener()
//	{
//		public void handleChange(ChangeEvent event)
//		{
////			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
//			applet_paint_with_new_visual();
//		}
//	};
	
	SelectionAdapter listener_refresh_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			log("info", "refreshing data.");
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
	
//	/**
//	 * add change listener for binding 'zoom'
//	 */
//	private void updateUserInterfaceProcessing(PmodelViewModel zoom)
//	{
////		bindingContext.dispose();
//		IObservableList bindings = bindingContextVisual.getValidationStatusProviders();
//
//		// Register the Listener for binding 'zoom'
//		
//		Binding b = (Binding) bindings.get(0);
//		b.getModel().addChangeListener(listener_visual);
//	}


	protected DataBindingContext initDataBindingsVisual()
	{
		DataBindingContext bindingContextVisual = new DataBindingContext();
		//
		IObservableValue targetObservableZoom = WidgetProperties.selection().observe(scale_zoom);
		IObservableValue modelObservableZoom = BeanProperties.value("zoom").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableZoom, modelObservableZoom, null, null);
		//
		IObservableValue targetObservableZoomTooltip = WidgetProperties.tooltipText().observe(scale_zoom);
		IObservableValue modelObservableZoomTooltip = BeanProperties.value("zoomstring").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableZoomTooltip, modelObservableZoomTooltip, null, null);
		//
		IObservableValue targetObservableLabelsize = WidgetProperties.selection().observe(spinner_labelsize);
		IObservableValue modelObservableLabelsize = BeanProperties.value("labelsize").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableLabelsize, modelObservableLabelsize, null, null);
		//
		IObservableValue targetObservableTextsize = WidgetProperties.selection().observe(spinner_textsize);
		IObservableValue modelObservableTextsize = BeanProperties.value("textsize").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableTextsize, modelObservableTextsize, null, null);
		//
//		IObservableValue targetObservableGravx = WidgetProperties.selection().observe(scale_gravx);
//		IObservableValue modelObservableGravx = BeanProperties.value("gravx").observe(einstellungen);
//		bindingContextVisual.bindValue(targetObservableGravx, modelObservableGravx, null, null);
		//
//		IObservableValue targetObservableGravy = WidgetProperties.selection().observe(scale_gravy);
//		IObservableValue modelObservableGravy = BeanProperties.value("gravy").observe(einstellungen);
//		bindingContextVisual.bindValue(targetObservableGravy, modelObservableGravy, null, null);
		//
		return bindingContextVisual;
	}
	
	protected DataBindingContext initDataBindingsMarked()
	{
		DataBindingContext bindingContextMarked = new DataBindingContext();
		//
		IObservableValue targetObservableMarked = WidgetProperties.text().observe(label_marked);
		IObservableValue modelObservableMarked = BeanProperties.value("markedStepName").observe(einstellungen);
		bindingContextMarked.bindValue(targetObservableMarked, modelObservableMarked, null, null);
		//
		return bindingContextMarked;
	}
	
		
	void log(String level, String logstring)
	{
//		text_logging.setText(text_logging.getText()+logstring+"\n");
		logstring = "["+new Timestamp(System.currentTimeMillis()) + "]:"+level+":"+logstring;
		if (text_logging != null)
		{
			text_logging.append(logstring+"\n");
//			if (level.equals("info"))		{	text_logging.setLineBackground(logLineCount, 1, colorLogWarn);}
			if (level.equals("warn"))	{	text_logging.setLineBackground(logLineCount, 1, colorLogWarn);}
			else if (level.equals("error"))	{	text_logging.setLineBackground(logLineCount, 1, colorLogError);}
			logLineCount = logLineCount+1;
			text_logging.setTopIndex(text_logging.getLineCount()-1);
		}
		else
		{
			System.out.println(logstring);
		}
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
		Option definition = OptionBuilder.withArgName("definition")
				.hasArg()
				.withDescription("[optional] process definition file")
//				.isRequired()
				.create("definition");

		Option instance = OptionBuilder.withArgName("instance")
				.hasArg()
				.withDescription("[optional] process instance file")
//				.isRequired()
				.create("instance");
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( definition );
				
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
			formatter.printHelp("pmodel-gui", options);
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
					shell.setText("pmodel-gui "+"v[% version %]");
					shell.setLayout(new FillLayout());
					Composite composite = new Composite(shell, SWT.NO_FOCUS);
					GridLayout gl_composite = new GridLayout(2, false);
					gl_composite.marginWidth = 0;
					gl_composite.marginHeight = 0;
					if (line.hasOption("definition"))
					{
						System.out.println("definition is "+line.getOptionValue("definition"));
						new PmodelPartUi1(composite, line.getOptionValue("definition"));
					}
					else
					{
						new PmodelPartUi1(composite);
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
