package de.prozesskraft.pmodel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

//import de.caegroup.pradar.Init;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;
import de.prozesskraft.gui.process.insight.PIInsightCreator;
import de.prozesskraft.gui.step.insight.SIInsightCreator;

public class PmodelPartUi1 extends ModelObject
{
	Shell shell = null;
	
	static CommandLine line;
	private DataBindingContext bindingContextVisual;
	private DataBindingContext bindingContextMarked;
	private DataBindingContext bindingContextRefresh;
	private Scale scale_size;
	private Scale scale_zoom;
	private Spinner spinner_textsize;

	private Button button_fix;
	private Button button_refresh;
	private Button button_startmanager;
	private Button button_stopmanager;

	private String iniFile = null;
	private static Ini ini = null;
	private ArrayList<String> license_server_port_at_hostname = new ArrayList<String>();
	
	private Label label_marked = null;
	
	public PmodelViewModel einstellungen = new PmodelViewModel();
	private StyledText text_logging = null;
	PmodelViewPage applet;
	Display display;

	Shell shell_dummy_insight;
	Composite actualStepInsight = null;
	CTabFolder tabFolder_12;
	SIInsightCreator sIInsightCreator = null;
	
	Composite processInsight = null;
	Map<String,Composite> stepInsight = new HashMap<String,Composite>();
	private Composite composite_131;
	private Composite composite_132;

	final Color colorLogError = new Color(new Shell().getDisplay(), 215, 165, 172);
	final Color colorLogWarn = new Color(new Shell().getDisplay(), 202, 191, 142);
	final Color colorLogInfo = new Color(new Shell().getDisplay(), 184, 210, 176);

	int logLineCount = 0;

	// wird pmodel innerhalb einer groesseren application geoeffnet, wird das beherbergende object hier abgelegt
	private Object pkraft = null;

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PmodelPartUi1()
	{
		shell = new Shell();
		shell.setSize(633, 688);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLocation(0, 0);
		setIni();
		loadIni();
//		checkLicense();
		createControls(composite);
		this.einstellungen.getProcess().setStepRanks();
		applet = new PmodelViewPage(einstellungen);
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PmodelPartUi1(Composite composite)
	{
		shell = composite.getShell();
		setIni();
		loadIni();
//		checkLicense();
		this.einstellungen.getProcess().setStepRanks();
		applet = new PmodelViewPage(einstellungen);
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Main falls das dbfile mitgeliefert wird
	 */
	@Inject
	public PmodelPartUi1(Composite composite, String pathToProcessFile)
	{
		shell = composite.getShell();
		
		java.io.File inFile = new java.io.File(pathToProcessFile);
		
		if (inFile.exists())
		{
			// binary file einlesen
			if (pathToProcessFile.matches(".+\\.pmb$"))
			{
				log("warn", "assuming binary format.");
//				System.out.println(inFile.getAbsolutePath());
				
				Process p1 = new Process();
				p1.setInfilebinary(inFile.getAbsolutePath());
				p1.setOutfilebinary(inFile.getAbsolutePath());
				
				this.einstellungen.setProcess(p1.readBinary());
			}
		
			// xml-format einlesen
			else if(pathToProcessFile.matches(".+\\.xml$|.+\\.pmx$"))
			{
				log("warn", "assuming xml format.");
				this.einstellungen.getProcess().setInfilexml(pathToProcessFile);
				try
				{
					this.einstellungen.setProcess(this.einstellungen.getProcess().readXml());
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
		}
		
		else
		{
			System.out.println("file does not exist: "+inFile.getAbsolutePath());
		}

		setIni();
		loadIni();
//		checkLicense();

//		einstellungen.setRootpositionratiox((float)0.5);
//		einstellungen.setRootpositionratioy((float)0.1);

		if(this.einstellungen.getProcess() != null)
		{
			this.einstellungen.getProcess().setStepRanks();
		}
//		applet = new PmodelViewPage(this, einstellungen);
		applet = new PmodelViewPage(einstellungen);
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
		
//		ExpandBar barVisual = new ExpandBar(composite_11, SWT.V_SCROLL);
//		Composite grpVisual = new Composite(barVisual, SWT.NONE);
//		ExpandItem item0 = new ExpandItem(barVisual, SWT.NONE, 0);
//		item0.setText("visual settings");
//		item0.setHeight(grpVisual.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
//		item0.setControl(grpVisual);
//		item0.setExpanded(true);
//		barVisual.setSpacing(8);

		Label lblNewLabel_2 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_2.setText("size");
		
		scale_size = new Scale(grpVisual, SWT.NONE);
		scale_size.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale_size.setMaximum(200);
		scale_size.setMinimum(10);
		scale_size.setSelection(100);
		scale_size.addMouseWheelListener(listener_mousewheel_size);
		
		Label lblNewLabel_21 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_21.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_21.setText("zoom");
		
		scale_zoom = new Scale(grpVisual, SWT.NONE);
		scale_zoom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale_zoom.setMaximum(200);
		scale_zoom.setMinimum(10);
		scale_zoom.setSelection(100);
		scale_zoom.addMouseWheelListener(listener_mousewheel_zoom);
		
//		Label lblNewLabel_3 = new Label(grpVisual, SWT.NONE);
//		lblNewLabel_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		lblNewLabel_3.setText("label");
		
//		spinner_labelsize = new Spinner(grpVisual, SWT.BORDER);
//		spinner_labelsize.setMaximum(20);
//		spinner_labelsize.setSelection(10);
//		spinner_labelsize.setMinimum(0);
		
		Label lblNewLabel_4 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_4.setText("text");
		
		spinner_textsize = new Spinner(grpVisual, SWT.BORDER);
		spinner_textsize.setMaximum(20);
		spinner_textsize.setSelection(10);
		spinner_textsize.setMinimum(0);
				
		button_fix = new Button(grpVisual, SWT.NONE | SWT.TOGGLE);
		button_fix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_fix.setText("fix");
		
		Button btnNewButton2 = new Button(grpVisual, SWT.NONE);
		btnNewButton2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		btnNewButton2.setText("autoscale");
		btnNewButton2.addSelectionListener(listener_autoscale_button);
		
		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(2, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("function");
		
		button_refresh = new Button(grpFunction, SWT.NONE | SWT.PUSH);
		button_refresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_refresh.setText("refresh");
		button_refresh.addListener(SWT.Selection, (Listener) listener_refresh_button);
		button_refresh.setEnabled(true);
		
//		button_refresh.getDisplay().asyncExec( new Runnable()
//		{
//			public void run()
//			{
//				// das Prozess Binary File ueberwachen
//				// Wenn modifiziert wurde? dann soll Enable=true gesetzt werden
//		//		Path processRootDir = Paths.get(einstellungen.getProcess().getRootdir());
//				Path processRootDir = Paths.get("/localhome/avoge/Desktop");
//		
//				System.err.println("watching directory " + processRootDir);
//				
//				try {
//					// Watch Service erstellen
//					WatchService service = FileSystems.getDefault().newWatchService();
//		
//					// Watch key erstellen
//					WatchKey key = processRootDir.register(service, ENTRY_MODIFY);
//					
//					while(true)
//					{
//						try {
//							Thread.sleep(500);
//						} catch (InterruptedException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						WatchKey key1;
//						try
//						{
//							key1 = service.take();
//						}
//						catch (InterruptedException x)
//						{
//							break;
//						}
//						
//						for(WatchEvent<?> event: key1.pollEvents())
//						{
//							WatchEvent.Kind<?> kind = event.kind();
//							
//							if(kind == OVERFLOW)
//							{
//								continue;
//							}
//							
//							WatchEvent<Path> ev = (WatchEvent<Path>) event;
//							Path filename = ev.context();
//							Path child = processRootDir.resolve(filename);
//							log("debug", "directory modified");
//		//					if(child.equals(Paths.get(einstellungen.getProcess().getRootdir() + "/process.pmb")))
////							if(child.equals(Paths.get("/localhome/avoge/Desktop/testfileNotification")))
//							{
//								button_refresh.setEnabled(true);
//								log("debug", "binary modified");
//							}
//						}
//					}
//		
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});

		
		button_startmanager = new Button(grpFunction, SWT.NONE);
		button_startmanager.setSelection(true);
		GridData gd_btnNewButton = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_btnNewButton.widthHint = 69;
		button_startmanager.setLayoutData(gd_btnNewButton);
		button_startmanager.setText("start new manager");
		button_startmanager.addSelectionListener(listener_startmanager_button);

		button_stopmanager = new Button(grpFunction, SWT.NONE);
		button_stopmanager.setSelection(true);
		GridData gd_button_stopmanager = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_button_stopmanager.widthHint = 69;
		button_stopmanager.setLayoutData(gd_button_stopmanager);
		button_stopmanager.setText("stop manager");
		button_stopmanager.addSelectionListener(listener_stopmanager_button);
		
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
		
		Composite composite_13 = new Composite(sashForm, SWT.NONE);
		composite_13.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_composite_13 = new GridLayout(1, false);
		gl_composite_13.marginWidth = 0;
		gl_composite_13.marginHeight = 0;
		composite_13.setLayout(gl_composite_13);
//		tabFolder_13.addSelectionListener(listener_tabFolder_selection);
		new Label(composite_1, SWT.NONE);

//		SashForm sashForm_13 = new SashForm(composite_13, SWT.SMOOTH | SWT.VERTICAL);
//		composite_13.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		composite_131 = new Composite(composite_13, SWT.BORDER);
		composite_131.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_composite_131 = new GridLayout(1, false);
		gl_composite_131.marginWidth = 0;
		gl_composite_131.marginHeight = 0;
		composite_131.setLayout(gl_composite_131);

		composite_132 = new Composite(composite_13, SWT.BORDER);
		composite_132.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_composite_132 = new GridLayout(1, false);
		gl_composite_132.marginWidth = 0;
		gl_composite_132.marginHeight = 0;
		composite_132.setLayout(gl_composite_132);

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
		
		
		bindingContextVisual = initDataBindingsVisual();
		bindingContextMarked = initDataBindingsMarked();
		bindingContextRefresh = initDataBindingsRefresh();
		
		// erzeugen der 'step-insight' listeners beim wechsel der markierung
		generateNewInsight(einstellungen);

		// erzeugen den insight fuer den Prozess
		createControlsProcessInsight(composite_131);
		new Label(composite_131, SWT.NONE);
		
		// erzeugen der ersten insight ansicht fuer den aktuell markierten step (root)
		createControlsStepInsight(composite_132);
		
		// die processing darstellung refreshen
		applet_refresh();

	}

	/**
	 * erzeugen der 'insight'-Ansicht fuer den aktuellen prozess
	 * @param parent
	 */
	public void createControlsProcessInsight(Composite composite)
	{
		log("info", "showing details for process "+this.einstellungen.getProcess().getName());
		
		processInsight = new Composite(composite, SWT.NONE);
		processInsight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_processInsight = new GridLayout(2, false);
		gl_processInsight.marginWidth = 0;
		gl_processInsight.marginHeight = 0;
		processInsight.setLayout(gl_processInsight);

		// erstellen der Prozess-Insight-Ansicht
		new PIInsightCreator(this, processInsight, this.einstellungen.getProcess());
		
		composite.layout(true);
	}
	
	/**
	 * erzeugen der 'insight'-Ansicht fuer den aktuell markierten step
	 * @param parent
	 */
	public void createControlsStepInsight(Composite composite)
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
			actualStepInsight.setParent(composite);
			log("debug", "reactivating existing controls");
			
			actualStepInsight.setVisible(true);
			composite.layout(true);
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

			sIInsightCreator = new SIInsightCreator(this, actualStepInsight, this.einstellungen.getProcess().getStep(einstellungen.getMarkedStepName()));
			
			// im stapel ablegen fuer spaeter
			this.stepInsight.put(einstellungen.getMarkedStepName(), actualStepInsight);
			
			// und auf die sichtbare flaeche legen
			actualStepInsight.setParent(composite);

			actualStepInsight.setVisible(true);
			composite.layout(true);
		}
		
	}
	
	public void applet_paint_with_new_visual()
	{
//		applet.setSizefaktor(einstellungen.getSize());
	}
	public void applet_refresh()
	{
		applet.refresh();
	}
	public void applet_autoscale()
	{
		this.einstellungen.setSize(100);
		this.einstellungen.setZoom(100);
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
	
	Listener listener_refresh_button = new Listener()
	{
		public void handleEvent(Event e)
		{
			refreshAppletAndUi();
		}
	};
	
	public void refreshAppletAndUi()
	{
		String inputFormat = "";
		
		log("info", "refreshing data.");
		// process frisch einlesen
		Process p = new Process();
		
		// wenn das pmb-file existiert, dann soll es eingelesen werden
		if(new java.io.File(this.einstellungen.process.getInfilebinary()).exists())
		{
			log("info", "process binary file does exist: "+this.einstellungen.process.getInfilebinary());
			p = this.einstellungen.getProcess().readBinary();

			// refreshen aller evtl vorhandenen subprocess stati, da diese nicht direkt ueber die getStatus() funktion erneuert werden
			try
			{
				p.refreshSubprocessStatus();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				log("warn", "failed to refresh status of min. one subprocesses. " + e.getMessage());
				e.printStackTrace();
			}
			inputFormat = "binary";
		}
		// wenn das xml-file existiert, dann soll das eingelesen werden
		else if(new java.io.File(this.einstellungen.process.getInfilexml()).exists())
		{
			log("info", "process definition file does exist: "+this.einstellungen.process.getInfilexml());
			try {
				System.out.println("Vor dem xml-einlesen: Anzahl der steps ist: "+p.getStep().size());
				p = this.einstellungen.getProcess().readXml();
				System.out.println("Nach dem xml-einlesen: Anzahl der steps ist: "+p.getStep().size());
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inputFormat = "xml";
		}
		// wenn keines von beiden existiert soll nichts gemacht werden
		else
		{
			log("error", "no process binary or definition file exists");
			return;
		}
		
		int zaehler = 0;
		while(p == null && zaehler < 5)
		{
			log("error", "problems while reading process object. trying once again.");
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(inputFormat.equals("binary"))
			{
				p = this.einstellungen.getProcess().readBinary();
			}
			else if(inputFormat.equals("xml"))
			{
				try {
					p = this.einstellungen.getProcess().readXml();
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			zaehler ++;
		}
		
		if(p == null)
		{
			log("error", "cannot read the process binary file: "+this.einstellungen.getProcess().getInfilebinary());
			return;
		}

		// wenn das neu eingelesene Object 'gut' ist, wird es uebernommen
		this.einstellungen.setProcess(p);
		
		// die prozessdarstellung zerstoeren und neu erstellen lassen
		processInsight.dispose();
		processInsight = null;
		createControlsProcessInsight(composite_131);

		// die stepdarstellungen disposen und den aktuellen neu erstellen lassen
		for(Composite actualStepInsight : stepInsight.values())
		{
			actualStepInsight.dispose();
		}
		stepInsight = new HashMap();
		this.actualStepInsight = null;
		createControlsStepInsight(composite_132);
		
		// wenn der step noch existiert (z.B. wurde ein multistep in der zwischenzeit gefanned, dann ist der urspruenglich evtl. markierte step nicht mehr vorhanden)
		try
		{
			// feststellen welche karteikarte im aktuellen step markiert ist
			int indexTabItemSelected = sIInsightCreator.tabFolder.getSelectionIndex();
			// und die gleiche karteikarte selektieren
			sIInsightCreator.tabFolder.setSelection(indexTabItemSelected);
		}
		catch (NullPointerException e)
		{
			System.err.println("error: step disappered.");
			System.err.println(e.getMessage());
		}
		
		// die processing darstellung refreshen
		applet_refresh();

	}

	SelectionAdapter listener_startmanager_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			managerActivate();
		}
	};
	
	public void managerActivate()
	{
		// wenn das pmb-file nicht existiert, dann soll nichts gemacht werden
		if(!(new java.io.File(this.einstellungen.process.getInfilebinary()).exists()))
		{
			log("error", "process binary file does not exist: "+this.einstellungen.process.getInfilebinary());
			return;
		}
		
		String aufruf = ini.get("apps", "pkraft-manager")+" -instance "+this.einstellungen.process.getInfilebinary();
		try
		{
			log("info", aufruf);
			java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);
			
//			try
//			{
//				log("info", "waiting 3 second for process become available on disk");
//				Thread.sleep(3000);
//			} catch (InterruptedException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
//			// update der daten und UI
//			refreshAppletAndUi();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	SelectionAdapter listener_stopmanager_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			managerDeactivate();
		}
	};
	
	public void managerDeactivate()
	{
		// wenn das pmb-file nicht existiert, dann soll nichts gemacht werden
		if(!(new java.io.File(this.einstellungen.process.getInfilebinary()).exists()))
		{
			log("error", "process binary file does not exist: "+this.einstellungen.process.getInfilebinary());
			return;
		}
		
		String aufruf =  ini.get("apps", "pkraft-manager")+" -stop -instance "+this.einstellungen.process.getInfilebinary();
		try
		{
			log("info", aufruf);
			java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);

//			try
//			{
//				log("info", "waiting 3 seconds for process become available on disk");
//				Thread.sleep(3000);
//			} catch (InterruptedException e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			//			// update der daten und UI
//			refreshAppletAndUi();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	SelectionAdapter listener_autoscale_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button wurde gedrueckt");
			applet_autoscale();
		}
	};
	
	MouseWheelListener listener_mousewheel_size = new MouseWheelListener()
	{
		public void mouseScrolled(MouseEvent me)
		{
			scale_size.setSelection(scale_size.getSelection() + (me.count*5));
		}
	};
	
	MouseWheelListener listener_mousewheel_zoom = new MouseWheelListener()
	{
		public void mouseScrolled(MouseEvent me)
		{
			scale_zoom.setSelection(scale_zoom.getSelection() + (me.count*5));
		}
	};
	
	IChangeListener listener_marked = new IChangeListener()
	{
		public void handleChange(ChangeEvent event)
		{
//			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
			createControlsStepInsight(composite_132);
		}
	};
	
	/**
	 * add change listener for binding 'marked'
	 */
	private void generateNewInsight(PmodelViewModel einstellungen)
	{
//		bindingContext.dispose();
		IObservableList bindings = bindingContextMarked.getValidationStatusProviders();

		// Register the Listener for binding 'size'
		
		Binding b = (Binding) bindings.get(0);
		b.getModel().addChangeListener(listener_marked);
	}


	protected DataBindingContext initDataBindingsVisual()
	{
		DataBindingContext bindingContextVisual = new DataBindingContext();
		//
		IObservableValue targetObservableSize = WidgetProperties.selection().observe(scale_size);
		IObservableValue modelObservableSize = BeanProperties.value("size").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableSize, modelObservableSize, null, null);
		//
		IObservableValue targetObservableSizeTooltip = WidgetProperties.tooltipText().observe(scale_size);
		IObservableValue modelObservableSizeTooltip = BeanProperties.value("sizestring").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableSizeTooltip, modelObservableSizeTooltip, null, null);
		//
		IObservableValue targetObservableZoom = WidgetProperties.selection().observe(scale_zoom);
		IObservableValue modelObservableZoom = BeanProperties.value("zoom").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableZoom, modelObservableZoom, null, null);
		//
		IObservableValue targetObservableZoomTooltip = WidgetProperties.tooltipText().observe(scale_zoom);
		IObservableValue modelObservableZoomTooltip = BeanProperties.value("sizestring").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableZoomTooltip, modelObservableZoomTooltip, null, null);
		//
//		IObservableValue targetObservableLabelsize = WidgetProperties.selection().observe(spinner_labelsize);
//		IObservableValue modelObservableLabelsize = BeanProperties.value("labelsize").observe(einstellungen);
//		bindingContextVisual.bindValue(targetObservableLabelsize, modelObservableLabelsize, null, null);
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
		IObservableValue targetObservableFix = WidgetProperties.selection().observe(button_fix);
		IObservableValue modelObservableFix = BeanProperties.value("fix").observe(einstellungen);
		bindingContextVisual.bindValue(targetObservableFix, modelObservableFix, null, null);
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
	
	protected DataBindingContext initDataBindingsRefresh()
	{
		DataBindingContext bindingContextRefresh = new DataBindingContext();
		//
//		IObservableValue targetObservableRefresh = WidgetProperties.text().observe(button_refresh);
//		IObservableValue modelObservableRefresh = BeanProperties.value("nextRefreshSecondsText").observe(einstellungen);
//		bindingContextRefresh.bindValue(targetObservableRefresh, modelObservableRefresh, null, null);
		//
//		IObservableValue targetObservableRefreshInterval = WidgetProperties.selection().observe(spinner_refreshinterval);
//		IObservableValue modelObservableRefreshInterval = BeanProperties.value("refreshInterval").observe(einstellungen);
//		bindingContextRefresh.bindValue(targetObservableRefreshInterval, modelObservableRefreshInterval, null, null);
//		//
//		IObservableValue targetObservableRefreshHit = WidgetProperties.selection().observe(button_refresh);
		IObservableValue targetObservableRefreshHit = SWTObservables.observeSelection(button_refresh);
//		IObservableValue modelObservableRefreshHit = BeansObservables.observeValue(einstellungen, "refreshNow");
		IObservableValue modelObservableRefreshHit = BeanProperties.value("refreshNow").observe(einstellungen);
		bindingContextRefresh.bindValue(targetObservableRefreshHit, modelObservableRefreshHit, null, null);
		//
		return bindingContextRefresh;
	}
	
//	protected DataBindingContext initDataBindingsManager()
//	{
//		DataBindingContext bindingContextManager = new DataBindingContext();
//		//
//		IObservableValue targetObservableManager = WidgetProperties.text().observe(button_manager);
//		IObservableValue modelObservableManager = BeanProperties.value("buttonManagerText").observe(einstellungen);
//		bindingContextManager.bindValue(targetObservableManager, modelObservableManager, null, null);
//		//
//		IObservableValue targetObservableProcessStatus = WidgetProperties.selection().observe(button_manager);
//		IObservableValue modelObservableProcessStatus = BeanProperties.value("managerActive").observe(einstellungen.process);
//		bindingContextManager.bindValue(targetObservableProcessStatus, modelObservableProcessStatus, null, null);
//		//
//		return bindingContextManager;
//	}
	
	void setIni ()
	{
		java.io.File file = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(this.getClass()) + "/" + "../etc/pmodel-gui.ini");
//		File file = WhereAmI.getDefaultInifile(this.getClass());
		this.iniFile = file.getAbsolutePath();
	}

	public String getInifile ()
	{
		return this.iniFile;
	}
	
	File getIniAsFile ()
	{
		return new File(this.iniFile);
	}
	
	public Ini getIni ()
	{
		return this.ini;
	}
	
	/**
	 * loads an ini-file
	 */
	void loadIni()
	{
		ArrayList<String> license_server_list = new ArrayList<String>();

		try
		{
			ini = new Ini(getIniAsFile());

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
			log("fatal", "problems with configuration: file not found: "+this.iniFile);
			System.exit(1);
		}
		catch (InvalidFileFormatException e1)
		{
			log("fatal", "problems with configuration: invalid file format: "+this.iniFile);
			System.exit(1);
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			log("fatal", "problems with configuration: problems while reading file (IOException): "+this.iniFile);
			System.exit(1);
			// TODO Auto-generated catch block
//			e1.printStackTrace();
		}
	}

	public void log(String level, String logstring)
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
			text_logging.update();
		}
		else
		{
			System.out.println(logstring);
		}
	}
	
	public Process getProcess()
	{
		return this.einstellungen.getProcess();
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
		options.addOption( instance );
				
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
			System.err.println("author:  [% email %]");
			System.err.println("version: [% version %]");
			System.err.println("date:    [% date %]");
			System.exit(0);
		}
		
		/*----------------------------
		  other things
		----------------------------*/
		if (!( line.hasOption("definition")) && !( line.hasOption("instance")))
		{
			System.err.println("either -definition or -instance needed.");
			System.err.println("either -definition or -instance needed. call -help for help.");
			System.exit(0);
		}

		// gui
		final Display display = new Display();
		
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable()
		{
			public void run()
			{
				try
				{
					Shell shell = new Shell(display);
					shell.setText("pmodel-gui "+"v[% version %]");

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
					Composite composite = new Composite(shell, SWT.NO_FOCUS);
					GridLayout gl_composite = new GridLayout(2, false);
					gl_composite.marginWidth = 0;
					gl_composite.marginHeight = 0;
					if (line.hasOption("definition"))
					{
						System.err.println("definition is "+line.getOptionValue("definition"));
						new PmodelPartUi1(composite, line.getOptionValue("definition"));
					}
					else if (line.hasOption("instance"))
					{
						System.err.println("instance is "+line.getOptionValue("instance"));
						new PmodelPartUi1(composite, line.getOptionValue("instance"));
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

	/**
	 * @param ini the ini to set
	 */
	public void setIni(Ini ini) {
		this.ini = ini;
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
