package de.prozesskraft.pramp.parts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.NotDirectoryException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
//import org.eclipse.jface.bindings.Binding;
//import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import org.eclipse.swt.widgets.Combo;

import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.jcraft.jsch.JSchException;
import com.license4j.License;
import com.license4j.LicenseValidator;

import de.prozesskraft.pkraft.Log;
import de.prozesskraft.pkraft.Process;
import de.prozesskraft.commons.*;
import de.prozesskraft.gui.process.CommitCreator;
import de.prozesskraft.pramp.testrun.Testrun;
import de.prozesskraft.pramp.testsummary.Testsummary;

public class PrampPartUi1 extends ModelObject
//public class PrampPartUi1
{
	private static Shell shell = null;
	
	static CommandLine line;
	private DataBindingContext bindingContextProcesses;
	private Button button_create = null;
	private Button button_open = null;
	private Button button_start = null;
	private Button button_testrun = null;
	private Button button_testsummary = null;
	private Button button_doc = null;
//	private Text text_logging = null;
	private StyledText text_logging = null;
	private Combo combo_domains = null;
	private Combo combo_processes = null;
	private Combo combo_versions = null;
//	private Combo combo_hosts = null;
	private String domainMainDir = null;
	private String processDefinitionPath = null;
	public Process process = null;
	private String iniFile = null;
	static private Ini ini = null;
//	private String userIniFile = null;
	
	private Map<String,Boolean> domainUserRights = new HashMap<String,Boolean>();
	
	private ArrayList<String> license_server_port_at_hostname = new ArrayList<String>();
	
//	private Ini userIni = null;
	private Text text_basedirectory = null;
	
	Composite composite_12;
	Shell shell_dummy_hinweis;
	Composite hinweisComposite;
	Shell shell_dummy_commitRoot;
	Composite commitRoot;
	Map<String,Composite> commitRootOld = new HashMap();
	Map<String,CommitCreator> commitCreatorOld = new HashMap();

	Display display;

	final Color colorLogError = new Color(new Shell().getDisplay(), 215, 165, 172);
	final Color colorLogWarn = new Color(new Shell().getDisplay(), 202, 191, 142);
	final Color colorLogInfo = new Color(new Shell().getDisplay(), 184, 210, 176);
	
	int logLineCount = 0;

	PrampPartUi1 This = this;
	
	public PrampViewModel einstellungen = new PrampViewModel();

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PrampPartUi1()
	{
		Shell shell = new Shell();
		shell.setSize(633, 688);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLocation(0, 0);
		setIni("../etc/pramp-gui.ini");
		readUserIni();
		loadIni();
//		checkLicense();
		detInstalledDomainNames();
		detDomainUserRights();
		reduceInstalledDomainNamesByRight();
//		setRandomRootdirectory();
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PrampPartUi1(Composite composite)
	{
		setIni("../etc/pramp-gui.ini");
		loadIni();
//		checkLicense();
		readUserIni();
		detInstalledDomainNames();
		detDomainUserRights();
		reduceInstalledDomainNamesByRight();
//		setRandomRootdirectory();
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Test
	 */
	@Inject
	public PrampPartUi1(String tmp)
	{
//		setIni("target/test-classes/etc/pramp-gui.ini");
		setIni("../etc/pramp-gui.ini");
		loadIni();
//		checkLicense();
		readUserIni();
//		getProcesses();
//		refresh();
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
		
		composite.addDisposeListener(new DisposeListener() {
		      public void widgetDisposed(DisposeEvent event) {
		          // When the composite gets disposed, the userIniFile should be updated
		          writeUserIni();
		        }
		      });
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(2, false);
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
		grpFilter.setText("select process");
		grpFilter.setLayout(new GridLayout(4, false));
		
		Label labelDomain = new Label(grpFilter, SWT.NONE);
		labelDomain.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		labelDomain.setText("domain");
		new Label(grpFilter, SWT.NONE);
		
		combo_domains = new Combo(grpFilter, SWT.NONE | SWT.READ_ONLY);
		combo_domains.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		combo_domains.addModifyListener(listener_domainselection);
		new Label(grpFilter, SWT.NONE);
		
		Label lblNewLabel = new Label(grpFilter, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		lblNewLabel.setText("process");
		new Label(grpFilter, SWT.NONE);
		
		combo_processes = new Combo(grpFilter, SWT.NONE | SWT.READ_ONLY);
		combo_processes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		combo_processes.addModifyListener(listener_processselection);
		new Label(grpFilter, SWT.NONE);
		
		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblNewLabel_1.setText("version");
		new Label(grpFilter, SWT.NONE);
		
		combo_versions = new Combo(grpFilter, SWT.NONE | SWT.READ_ONLY);
		combo_versions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		combo_versions.addModifyListener(listener_versionselection);
		
		Group grpVisual = new Group(composite_11, SWT.NONE);
		grpVisual.setText("base directory");
		GridData gd_grpVisual = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_grpVisual.widthHint = 139;
		grpVisual.setLayoutData(gd_grpVisual);
		grpVisual.setLayout(new GridLayout(4, false));
		
		text_basedirectory = new Text(grpVisual, SWT.BORDER);
		text_basedirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if(einstellungen.getBaseDirectory() == null)
		{
			einstellungen.setBaseDirectory(System.getProperty("user.dir"));
		}
		//		text_instancedirectory.addModifyListener(listener_text_instancedirectory);
		
//		Button btnA = new Button(grpVisual, SWT.NONE);
//		btnA.setToolTipText("generates random path in current working directory");
//		btnA.setText("A");
//		btnA.addSelectionListener(listener_randomdirectory_button);
		
		Button button = new Button(grpVisual, SWT.NONE);
		button.setToolTipText("select a directory");
		button.setText("...");
		button.addSelectionListener(listener_directory_button);

		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(1, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("functions");
		
		button_create = new Button(grpFunction, SWT.NONE);
		button_create.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_create.setText("create");
		button_create.setToolTipText("create instance");
		button_create.addSelectionListener(listener_create_button);
		
//		button_open = new Button(grpFunction, SWT.NONE);
//		button_open.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		button_open.setText("create and open");
//		button_open.setToolTipText("create instance and open with pmodel");
//		button_open.addSelectionListener(listener_open_button);
//		
//		button_start = new Button(grpFunction, SWT.NONE);
//		button_start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		button_start.setText("create and start");
//		button_start.setToolTipText("create instance and start");
//		button_start.addSelectionListener(listener_start_button);
		
		Group grpAdmin = new Group(composite_11, SWT.NONE);
		grpAdmin.setLayout(new GridLayout(1, false));
		grpAdmin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpAdmin.setText("admin");
		grpAdmin.setEnabled(this.domainUserRights.get("Admin"));

		button_doc = new Button(grpAdmin, SWT.NONE);
		button_doc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_doc.setText("doc");
		button_doc.setToolTipText("show documentation");;
		button_doc.addSelectionListener(listener_showdoc_button);
		button_doc.setEnabled(this.domainUserRights.get("Admin"));
		
		button_testrun = new Button(grpAdmin, SWT.NONE);
		button_testrun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_testrun.setText("testrun");
		button_testrun.setToolTipText("starts an instance of selected process with a sample dataset");;
		button_testrun.addSelectionListener(listener_testrun_button);
		button_testrun.setEnabled(this.domainUserRights.get("Admin"));
		
		button_testsummary = new Button(grpAdmin, SWT.NONE);
		button_testsummary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_testsummary.setText("testsummary");
		button_testsummary.setToolTipText("shows summary results of old testruns");;
		button_testsummary.addSelectionListener(listener_testsummary_button);
		button_testsummary.setEnabled(this.domainUserRights.get("Admin"));
		
		composite_12 = new Composite(composite_1, SWT.BORDER);
//		composite_12.setLayout(new GridLayout(1, false));
		composite_12.setLayout(new FillLayout());
		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		gd_composite_12.heightHint = 390;
//		gd_composite_12.minimumWidth = 10;
//		gd_composite_12.minimumHeight = 10;
		composite_12.setLayoutData(gd_composite_12);

		
		shell_dummy_hinweis = new Shell(display);
		hinweisComposite = new Composite(shell_dummy_hinweis, SWT.NONE);
//		hinweisComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_hinweisComposite = new GridLayout(1, false);
		hinweisComposite.setLayout(gl_hinweisComposite);
		Label label_hinweis = new Label(hinweisComposite, SWT.NONE);
		label_hinweis.setText("no process definition.");
		
		shell_dummy_commitRoot = new Shell(display);
		commitRoot = new Composite(shell_dummy_commitRoot, SWT.V_SCROLL);
		commitRoot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_commitRoot = new GridLayout(1, false);
		commitRoot.setLayout(gl_commitRoot);

		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_2.heightHint = 164;
		composite_2.setLayoutData(gd_composite_2);
		
//		text_logging = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
//		text_logging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		text_logging = new StyledText(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		text_logging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Datenbindung Domains-Combo
		initDataBindingsDomains();
		// Datenbindung Processes-Combo
		initDataBindingsProcesses();
		// Datenbindung Versions-Combo
		initDataBindingsVersions();
		// Datenbindung Domains-Combo-Selection
		initDataBindingsDomain();
		// Datenbindung Processes-Combo-Selection
		initDataBindingsProcess();
		// Datenbindung Versions-Combo-Selection
		initDataBindingsVersion();
		// Datenbindung instancedirectory textfeld
		initDataBindingsInstancedirectory();

		// auswahl der Domains-Combo auf das Element aus der UserIni setzen
		combo_domains.select(einstellungen.getDomainId());
		// auswahl der Processes-Combo auf das Element aus der UserIni setzen
		combo_processes.select(einstellungen.getProcessId());
		// auswahl der Processes-Combo auf das Element aus der UserIni setzen
		combo_versions.select(einstellungen.getVersionId());

		
		new Label(grpFilter, SWT.NONE);

		// setzen der random instancedirectory
//		setRandomInstancedirectory();

		new Label(grpVisual, SWT.NONE);

		

//		Frame frame = SWT_AWT.new_Frame(composite_12);
//		
//		frame.pack();
//		frame.setLocation(0, 0);
//		frame.setVisible(true);

	}

	@PreDestroy
	public void dispose()
	{
	}

	@Focus
	public void setFocus()
	{

	}
	
	void updateUiComboProcesses()
	{
		getInstalledProcessNames(combo_domains.getText());
		combo_processes.select(combo_processes.getItemCount()-1);
	}
	
	void updateUiComboVersions()
	{
		getInstalledVersionNames(combo_domains.getText(), combo_processes.getText());

		// deselect first
		combo_versions.deselect(combo_versions.getSelectionIndex());
		// then select the last item
		combo_versions.select(combo_versions.getItemCount()-1);
	}
	
	/**
	 * listener for Modifications/Selections in combobox 'domains'
	 */
	ModifyListener listener_domainselection = new ModifyListener()
	{
		public void modifyText(ModifyEvent arg0)
		{
        	log("info", "setting domain: "+combo_domains.getText());
			updateUiComboProcesses();
//			setRandomInstancedirectory();
		}
	};

	/**
	 * listener for Modifications/Selections in combobox 'processes'
	 */
	ModifyListener listener_processselection = new ModifyListener()
	{
		public void modifyText(ModifyEvent arg0)
		{
        	log("info", "setting process: "+combo_processes.getText());
			updateUiComboVersions();
//			setRandomInstancedirectory();
		}
	};

	/**
	 * listener for Modifications/Selections in combobox 'versions'
	 */
	ModifyListener listener_versionselection = new ModifyListener()
	{
		public void modifyText(ModifyEvent arg0)
		{
        	log("info", "setting version: "+combo_versions.getText());
//			setRandomInstancedirectory();
			getProcessDefinition();
			createControlsRootCommit(composite_12);
		}
	};

//	/**
//	 * listener for Modification in textfield 'instancedirectory'
//	 */
//	ModifyListener listener_text_instancedirectory = new ModifyListener()
//	{
//		public void modifyText(ModifyEvent arg0)
//		{
//			text_instancedirectory.setToolTipText(text_instancedirectory.getText());
//		}
//	};
//	
//	/**
//	 * listener for selection of 'A'-button
//	 */
//	SelectionAdapter listener_randomdirectory_button = new SelectionAdapter()
//	{
//		public void widgetSelected(SelectionEvent event)
//		{
//			setRandomInstancedirectory();
//		}
//	};
	
	/**
	 * listener for selection of "..."-button
	 * opens a shell to select a directory-path
	 * selection will be echoed in textfield for directory path
	 */
	SelectionAdapter listener_directory_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
	        DirectoryDialog dlg = new DirectoryDialog(new Shell());

	        // Set the initial filter path according
	        // to anything they've selected or typed in
	        dlg.setFilterPath(text_basedirectory.getText());

	        // Change the title bar text
	        dlg.setText("Instance Directory Dialog");

	        // Customizable message displayed in the dialog
	        dlg.setMessage("Select a directory");

	        // Calling open() will open and run the dialog.
	        // It will return the selected directory, or
	        // null if user cancels
	        String dir = dlg.open();
	        if (dir != null) {
	          // Set the text box to the new selection
	        	einstellungen.setBaseDirectory(dir);
	        	log("info", "setting instancedirectory: "+dir);
	        	
	        	// dieses Directory als filterPath in allen bereits bestehenden commitCreatorn setzen
	        	for(String key : commitCreatorOld.keySet())
	        	{
	        		commitCreatorOld.get(key).setFilterPath(dir);
	        	}
	        	
//	        	text_instancedirectory.setText(dir);
	        }
		}
	};
	
	/**
	 * listener for Selections in of button 'doc'
	 */
	SelectionAdapter listener_showdoc_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button doc wurde gedrueckt");
			showDoc();
		}
	};

	/**
	 * listener for Selections in of button 'create'
	 */
	SelectionAdapter listener_create_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			new CreateInstanceGui(shell, This);
		}
	};
	
	/**
	 * listener for Selections in of button 'start'
	 */
	SelectionAdapter listener_start_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button show wurde gedrueckt");
			createInstance();
			
			// kurz schlafen damit der schreibvorgang beendet werden kann
//			try
//			{
//				Thread.sleep(100);
//			} catch (InterruptedException e1)
//			{
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}

			startInstance();
		}
	};
	
	/**
	 * listener for Selections in of button 'testrun'
	 */
	SelectionAdapter listener_testrun_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button start wurde gedrueckt");
			testrun();
		}
	};
	
	/**
	 * listener for Selections in of button 'testsummary'
	 */
	SelectionAdapter listener_testsummary_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button start wurde gedrueckt");
			testsummary();
		}
	};
	
	/**
	 * pramp-button oeffnet die anwendung pradar-gui
	 **/
	SelectionAdapter listener_pradar_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			log("info", "starten von pradar");
			String aufruf = ini.get("apps", "pradar");
			
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
	
	/**
	 * binds array of domainnames to combo-box 'domains'
	 */
	protected DataBindingContext initDataBindingsDomains()
	{
		DataBindingContext bindingContextDomains = new DataBindingContext();
		//
		IObservableList targetObservableDomains = WidgetProperties.items().observe(combo_domains);
		IObservableList modelObservableDomains = BeanProperties.list("domains").observe(einstellungen);
		bindingContextDomains.bindList(targetObservableDomains, modelObservableDomains, null, null);
		//
		return bindingContextDomains;
	}
	
	/**
	 * binds selection of combo-box 'domains' to String domain
	 */
	protected DataBindingContext initDataBindingsDomain()
	{
		DataBindingContext bindingContextDomain = new DataBindingContext();
		//
		IObservableValue targetObservableDomain = WidgetProperties.text().observe(combo_domains);
		IObservableValue modelObservableDomain = BeanProperties.value("domain").observe(einstellungen);
		bindingContextDomain.bindValue(targetObservableDomain, modelObservableDomain, null, null);
		//
		return bindingContextDomain;
	}
	
	/**
	 * binds array of processnames to combo-box 'processes'
	 */
	protected DataBindingContext initDataBindingsProcesses()
	{
		DataBindingContext bindingContextProcesses = new DataBindingContext();
		//
		IObservableList targetObservableProcesses = WidgetProperties.items().observe(combo_processes);
		IObservableList modelObservableProcesses = BeanProperties.list("processes").observe(einstellungen);
		bindingContextProcesses.bindList(targetObservableProcesses, modelObservableProcesses, null, null);
		//
		return bindingContextProcesses;
	}
	
	/**
	 * binds selection of combo-box 'processes' to String process
	 */
	protected DataBindingContext initDataBindingsProcess()
	{
		DataBindingContext bindingContextProcess = new DataBindingContext();
		//
		IObservableValue targetObservableProcess = WidgetProperties.text().observe(combo_processes);
		IObservableValue modelObservableProcess = BeanProperties.value("process").observe(einstellungen);
		bindingContextProcess.bindValue(targetObservableProcess, modelObservableProcess, null, null);
		//
		return bindingContextProcess;
	}
	
	/**
	 * binds array of versions to combo-box 'versions'
	 */
	protected DataBindingContext initDataBindingsVersions()
	{
		DataBindingContext bindingContextVersions = new DataBindingContext();
		//
		IObservableList targetObservableVersions = WidgetProperties.items().observe(combo_versions);
		IObservableList modelObservableVersions = BeanProperties.list("versions").observe(einstellungen);
		bindingContextVersions.bindList(targetObservableVersions, modelObservableVersions, null, null);
		//
		return bindingContextVersions;
	}
	
	/**
	 * binds selection of combo-box 'versions' to String version
	 */
	protected DataBindingContext initDataBindingsVersion()
	{
		DataBindingContext bindingContextVersion = new DataBindingContext();
		//
		IObservableValue targetObservableVersion = WidgetProperties.text().observe(combo_versions);
		IObservableValue modelObservableVersion = BeanProperties.value("version").observe(einstellungen);
		bindingContextVersion.bindValue(targetObservableVersion, modelObservableVersion, null, null);
		//
		return bindingContextVersion;
	}
	
	/**
	 * binds content of textfield 'instancedirectory' to String instancedirectory
	 */
	protected DataBindingContext initDataBindingsInstancedirectory()
	{
		DataBindingContext bindingContextInstancedirectory = new DataBindingContext();
		//
		IObservableValue targetObservableInstancedirectory = WidgetProperties.text(SWT.Modify).observe(text_basedirectory);
		IObservableValue modelObservableInstancedirectory = BeanProperties.value("baseDirectory").observe(einstellungen);
		bindingContextInstancedirectory.bindValue(targetObservableInstancedirectory, modelObservableInstancedirectory, null, null);
		//
		IObservableValue targetObservableInstancedirectoryTooltip = WidgetProperties.tooltipText().observe(text_basedirectory);
		IObservableValue modelObservableInstancedirectoryTooltip = BeanProperties.value("baseDirectory").observe(einstellungen);
		bindingContextInstancedirectory.bindValue(targetObservableInstancedirectoryTooltip, modelObservableInstancedirectoryTooltip, null, null);
		//
		return bindingContextInstancedirectory;
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
			if (ini.get("process", "domain-installation-directory") != null )
			{
				this.domainMainDir = (ini.get("process", "domain-installation-directory"));
				
				// wenn es sich um einen relativen path handelt, soll dieser mit dem installationsverzeichnis ergaenzt werden
				if(!(this.domainMainDir.matches("^/.+")))
				{
					
					this.domainMainDir = WhereAmI.getInstallDirectoryAbsolutePath(this.getClass())+"/"+this.domainMainDir;
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

	/**
	 * determines the domain right of actual user
	 */
	public void detDomainUserRights()
	{
		// 1) alle domains auf true setzen
		for(String actDomain : einstellungen.getDomains())
		{
			this.domainUserRights.put(actDomain, true);
		}
		
		// 2) fuer alle domains aus der whitelist (ini)
		// wenn der aktuelle user sich nicht in der liste befindet, wird die entsprechende domain auf false gesetzt
		for( String actDomain : this.ini.get("domainWhitelist").keySet() )
		{
			// da eine whitelist fuer diese domain existiert, soll der aktuelle user fuer diese domain erstmal auf false gesetzt werden
			this.domainUserRights.put(actDomain, false);

			// die 'white' user fuer die aktuelle domain feststellen ((leerzeichen entfernen und an kommas splitten))
			String[] usersWhite = this.ini.get("domainWhitelist", actDomain).replaceAll(" ", "").split(",");

			// wird der aktuelle user gewhitet? dann domain auf true setzen
			for(String actUser : usersWhite)
			{
				if(System.getProperty("user.name").matches(actUser))
				{
					this.domainUserRights.put(actDomain, true);
				}
			}
		}
		
		// 3) fuer alle domains aus der blacklist (ini)
		// wenn der aktuelle user sich in der liste befindet, wird die entsprechende domain auf false gesetzt
		for( String actDomain : this.ini.get("domainBlacklist").keySet() )
		{
			// die 'white' user fuer die aktuelle domain feststellen ((leerzeichen entfernen und an kommas splitten))
			String[] usersBlack = this.ini.get("domainBlacklist", actDomain).replaceAll(" ", "").split(",");

			// wird der aktuelle user gewhitet? dann domain auf true setzen
			for(String actUser : usersBlack)
			{
				if(System.getProperty("user.name").matches(actUser))
				{
					this.domainUserRights.put(actDomain, false);
				}
			}
		}
	}
	
	/**
	 * reduces the installed domain list to the domains the actual user has rights
	 */
	private void reduceInstalledDomainNamesByRight()
	{
		ArrayList<String> filteredDomainNamesByRight = new ArrayList<String>();
		
		for(String actDomain : einstellungen.getDomains())
		{
			if(this.domainUserRights.get(actDomain))
			{
				filteredDomainNamesByRight.add(actDomain);
			}
		}

		// sortieren
		Collections.sort(filteredDomainNamesByRight);

		// zentrale daten setzen
		einstellungen.setDomains((String[]) filteredDomainNamesByRight.toArray(new String[filteredDomainNamesByRight.size()]));
	}
	
	/**
	 * determines all domains
	 * @return a list of all installed domains sorted in alphabetical order
	 */
	private ArrayList<String> detInstalledDomainNames()
	{
		ArrayList<String> domains = new ArrayList<String>();
		String directoryPath = this.domainMainDir;
		try
		{
			domains = getSubDirectories(directoryPath);
		}
		catch (NotDirectoryException e)
		{
			System.err.println("not a directory: "+directoryPath);
			e.printStackTrace();
		}

		ArrayList<String> domainsFiltered = new ArrayList<String>();
		// alle domains mit vorangestelltem "_" herausfiltern
		for(String actDomain : domains)
		{
			if(!actDomain.matches("^_.*$"))
			{
				System.out.println("kein fuehrendes _ bei "+actDomain);
				domainsFiltered.add(actDomain);
			}
		}
		
		// sortieren
		Collections.sort(domainsFiltered);

		// zentrale daten setzen
		einstellungen.setDomains((String[]) domainsFiltered.toArray(new String[domainsFiltered.size()]));
		
		return domainsFiltered;
	}
	
	/**
	 * determines all processes of a specific domain
	 * @return a list of all installed domains sorted in alphabetical order
	 */
	public ArrayList<String> getInstalledProcessNames(String domainName)
	{
		ArrayList<String> processes = new ArrayList<String>();
		String directoryPath = this.domainMainDir+"/"+domainName;
		try
		{
			processes = getSubDirectories(directoryPath);
		} catch (NotDirectoryException e)
		{
			System.err.println("not a directory: "+directoryPath);
			e.printStackTrace();
		}

		// sortieren
		Collections.sort(processes);
		
		// zentrale daten setzen
		einstellungen.setProcesses((String[]) processes.toArray(new String[processes.size()]));
		
//		// in combo_box einfuegen
//		combo_versions.setItems((String[]) versions.toArray(new String[versions.size()]));
//		combo_versions.select(versions.size()-1);
//		
		return processes;
	}
	
	/**
	 * determines all versions of a specific process
	 * @return a list of all installed processes sorted in alphabetical order
	 */
	public ArrayList<String> getInstalledVersionNames(String domainName, String processName)
	{
		ArrayList<String> versions = new ArrayList<String>();
		String directoryPath = this.domainMainDir+"/"+domainName+"/"+processName;
		try
		{
			versions = getSubDirectories(directoryPath);
		} catch (NotDirectoryException e)
		{
			System.err.println("not a directory: "+directoryPath);
//			e.printStackTrace();
		}

		// sortieren
		Collections.sort(versions);
		
		// zentrale daten setzen
		einstellungen.setVersions((String[]) versions.toArray(new String[versions.size()]));
		
//		// in combo_box einfuegen
//		combo_versions.setItems((String[]) versions.toArray(new String[versions.size()]));
//		combo_versions.select(versions.size()-1);
//		
		return versions;
	}
	
	/**
	 * determines all subdirectories of a specified directory
	 * @return a list of all subdirectory names (without path - only the last name)
	 * @throws NotDirectoryException 
	 */
	private ArrayList<String> getSubDirectories(String path) throws NotDirectoryException
	{
		ArrayList<String> subdirectories = new ArrayList<String>();
		File dir = new File(path);
		if (!(dir.isDirectory()))
		{
			throw new NotDirectoryException(path);
		}
		
		File[] inhaltDir = dir.listFiles();
		for(int x = 0; x < inhaltDir.length; x++)
		{
			if (inhaltDir[x].isDirectory())
			{
				subdirectories.add(inhaltDir[x].getName());
			}
		}
		// sortieren
//		Collections.sort(processes);
		return subdirectories;
	}
	
	/**
	 * determines the path to the process-definition-directory
	 * @return String path to process-definition-directory
	 * @return null if parts of path are not available or path does not point to a directory
	 */
	public String getProcessDirectory()
	{
		String processDefinitionDirectory = null;
		
		String domain  = this.combo_domains.getText();
		String process = this.combo_processes.getText();
		String version = this.combo_versions.getText();
		String installationPath = this.domainMainDir;
		
		if ((process != null) && (version != null) && (installationPath != null))
		{
			processDefinitionDirectory = installationPath+"/"+domain+"/"+process+"/"+version;
		}

		if ( (processDefinitionDirectory == null) || (!(new java.io.File(processDefinitionDirectory).exists()))  || (!(new java.io.File(processDefinitionDirectory).isDirectory())))
		{
	    	log("error", "process definition directory does not exist: "+processDefinitionDirectory);
	    	processDefinitionDirectory = null;
		}

		return processDefinitionDirectory;
	}
	
	/**
	 * determines the path to the process-definition-file
	 * @return String path to process-definition
	 * @return null if parts of path are not available or path does not point to a file
	 */
	public String getProcessDefinition()
	{
		String processDefinitionDirectory = this.getProcessDirectory();
		String processDefinition = null;
		
		if(processDefinitionDirectory == null)
		{
	    	log("error", "directory does not exist: "+processDefinitionDirectory);
		}
		
		else
		{
			processDefinition = processDefinitionDirectory + "/process.xml";
			String processDefinitionFuerLog = "";
			if(this.domainUserRights.get("Admin"))
			{
				processDefinitionFuerLog = processDefinition;
			}

	    	log("info", "setting process definition "+processDefinitionFuerLog);
	    	
	    	java.io.File fileProcess = new java.io.File(processDefinition);
	    	if(!(fileProcess.exists()))
	    	{
		    	log("error", "process definition file does not exist "+processDefinitionFuerLog);
		    	this.process = null;
		    	return null;
	    	}
	    	
	    	else
	    	{
				Process tmp = new Process();
				System.err.println("reading process definition " + processDefinition);
				tmp.setInfilexml(processDefinition);
				try
				{
				this.process = tmp.readXml();
				} catch (JAXBException e)
				{
					// TODO Auto-generated catch block
					log("error", "cannot unmarshal data from process definition.");
//						e.printStackTrace();
				}
				this.process.setInfilexml(processDefinition);
			
				this.processDefinitionPath = processDefinition;
				return processDefinition;
	    	}
		}
		return null;
	}

	/**
	 * every commitRoot page has a Name, such as "beulen@0.8.2"
	 * @return String actualCommitRootName
	 */
	public String getActualCommitRootName()
	{
		return combo_processes.getText()+"@"+combo_versions.getText();
	}
	
	
	/**
	 * creates the controls for RootCommit Area
	 */
	public void createControlsRootCommit(Composite parent)
	{

		if (this.process == null)
		{
			commitRoot.setParent(shell_dummy_commitRoot);
			commitRoot.setVisible(false);
			hinweisComposite.setParent(parent);
			hinweisComposite.setVisible(true);
			parent.layout(true);
		}
		
		else
		{
			hinweisComposite.setVisible(false);
			hinweisComposite.setParent(shell_dummy_hinweis);
			
			// wenn es fuer diese version schon einen composite gibt, dann diesen anzeigen
			if ( this.commitRootOld.containsKey(getActualCommitRootName()) )
			{
				
				// den bisher angezeigten prozess ausblenden
				commitRoot.setParent(shell_dummy_commitRoot);
				commitRoot.setVisible(false);

				// den schon vorhandenen prozess einblenden
//				Composite old = this.commitRootOld.get((combo_processes.getText()+combo_versions.getText()));
				commitRoot = this.commitRootOld.get(getActualCommitRootName());
				commitRoot.setParent(parent);
				commitRoot.setVisible(true);
				parent.layout(true);
				log("info", "reactivating an existent commitRoot page");
				
				// das feld aktuell halten
				this.process = this.commitCreatorOld.get(getActualCommitRootName()).getStep().getParent();
			}
			
			// ein neues composite erstellen
			else if (this.process.isStep("root"))
			{
				// den bisher angezeigten prozess ausblenden
				commitRoot.setParent(shell_dummy_commitRoot);
				commitRoot.setVisible(false);

				Composite actualComposite = new Composite(shell_dummy_commitRoot, SWT.NONE);
//				actualComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//				actualComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
//				GridLayout gl_actualComposite = new GridLayout(1, false);
				actualComposite.setLayout(new FillLayout());

				CommitCreator commitCreator = new CommitCreator(this, actualComposite, this.process.getRootStep());

				actualComposite = commitCreator.createControls();
//				actualComposite.pack();
				
				commitRoot = actualComposite;
				commitRoot.setParent(parent);
				commitRoot.setVisible(true);

				parent.layout(true);
				log("info", "creating a new commitRoot page");
				commitRootOld.put(getActualCommitRootName(), commitRoot);
				commitCreatorOld.put(getActualCommitRootName(), commitCreator);
			}
			
			else
			{
				log("error", "selected process definition does not contain a step 'root'");
				commitRoot.setParent(shell_dummy_commitRoot);
				commitRoot.setVisible(false);
				hinweisComposite.setParent(parent);
				hinweisComposite.setVisible(true);
				parent.layout(true);
			}
			
		}
	}
	
//	/**
//	 * determines a random path in cwd
//	 */
//	public void setRandomInstancedirectory()
//	{
//		String cwd = System.getProperty("user.dir");
//		Calendar now = Calendar.getInstance();
//		
//		int intYear  = now.get(Calendar.YEAR);
//		int intMonth = now.get(Calendar.MONTH) + 1;
//		int intDay   = now.get(Calendar.DAY_OF_MONTH);
//		int intHour  = now.get(Calendar.HOUR_OF_DAY);
//		int intMinute= now.get(Calendar.MINUTE);
//		int intSecond= now.get(Calendar.SECOND);
//		int intMilli = now.get(Calendar.MILLISECOND);
//		
//		String stringYear  = String.format("%04d", intYear);
//		String stringMonth = String.format("%02d", intMonth);
//		String stringDay   = String.format("%02d", intDay);
//		String stringHour  = String.format("%02d", intHour);
//		String stringMinute= String.format("%02d", intMinute);
//		String stringSecond= String.format("%02d", intSecond);
//		String stringMilli = String.format("%03d", intMilli);
//		
//		String stringProcess = null;
//		if (combo_processes.getText() != null)
//		{
//			stringProcess = combo_processes.getText();
//		}
//		String stringVersion = null;
//		if (combo_versions.getText() != null)
//		{
//			stringVersion = combo_versions.getText();
//			stringVersion = stringVersion.replaceAll("\\.", "");
//		}
//		
//		String path = cwd + "/" + stringProcess + "_v" + stringVersion + "_" + stringYear + stringMonth + stringDay + "_" + stringHour + stringMinute + stringSecond + "_" + stringMilli; 
//
//		einstellungen.setRootDirectory(path);
//    	log("info", "setting instancedirectory: "+path);
////		System.out.println("pfad sollte sein: "+path);
////		System.out.println("aktualisiere textfeld - einstellungen get.Instancedirectory: "+einstellungen.getInstancedirectory());
//	}
	
	
//	/**
//	 * prueft ob die parameter bereits verwendet wurden
//	 * TODO: erneuern auf Multimap
//	 * @return true if parameter are used the first time
//	 */
//	private boolean parameterAlreadyUsed(Multimap<String,String> content)
//	{
//		boolean result = false;
//
//		Ini userIni;
//		try
//		{
//			userIni = new Ini(new File(this.userIniFile));
//			int processZaehler = 1;
//			while(userIni.containsKey(this.process.getName()+"-"+processZaehler))
//			{
//				Boolean comparison = null;
//				
//				Map<String,String> actualMapIni = userIni.get(this.process.getName()+"-"+processZaehler);
//				
//				// die aktuellen input-daten vergleichen mit dem block aus dem inifile
//				for(String key : content.keySet())
//				{
////					System.out.println("mapIni:  "+actualMapIni.get(key));
////					System.out.println("content: "+content.get(key));
//					if ( (actualMapIni.get(key) != null) && (content.get(key) != null) && (!(content.get(key).equals(actualMapIni.get(key)))) )
//					{
////						log("info", "keine uebereinstimmung fuer section "+this.process.getName()+"-"+processZaehler+" key: "+key+" value-content: "+content.get(key)+" value-ini: "+actualMapIni.get(key));
//						comparison = false;
//						break;
//					}
//					else if (comparison == null)
//					{
//						comparison = true;
//					}
//				}
//				
//				// wenn beide datensaetze uebereinstimmen, dann
//				if (comparison)
//				{
//					result = true;
//					return result;
//				}
//				processZaehler++;
//			}
//
//			
//		} catch (InvalidFileFormatException e)
//		{
//			// TODO Auto-generated catch block
//			log("error", "invalid file format of user-ini file: "+this.userIniFile);
//			//	e.printStackTrace();
//		} catch (IOException e)
//		{
//			// TODO Auto-generated catch block
////			e.printStackTrace();
//			log("error", "problems with reading user-ini file: "+this.userIniFile);
//		}
//		return result;
//	}
	
	
	/**
	 * show documentation in a pdfreader if any exists
	 */
	private boolean showDoc()
	{
		String pathToDoc = this.getProcessDirectory() + "/process.pdf";
		
		java.io.File fileDoc = new java.io.File(pathToDoc);
		
		if(!(fileDoc.exists()))
		{
			String fileDocFuerLog = "";
			if(this.domainUserRights.get("Admin"))
			{
				fileDocFuerLog = fileDoc.getAbsolutePath();
			}

			log ("error", "no documentation found " + fileDocFuerLog);

			return false;
		}
		
		else
		{
			String pdfreader = ini.get("apps", "pdfreader");
			
			String fileDocFuerLog = "";
			if(this.domainUserRights.get("Admin"))
			{
				fileDocFuerLog = fileDoc.getAbsolutePath();
			}
			
			log ("info", "showing documentation with " +pdfreader +" " + fileDocFuerLog);
			String[] args_for_command = {pdfreader, fileDoc.getAbsolutePath()};
			ProcessBuilder pb = new ProcessBuilder(args_for_command);
			
			try
			{
				java.lang.Process p = pb.start();
				log ("debug", "hashCode="+p.hashCode());
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				log ("error", "IOException: problems with executing via ssh");
				e.printStackTrace();
			}
		
		return true;
		}
	}
	
	/**
	 * shows a selection of sample data and starts one of it
	 */
	private void testrun()
	{
		java.io.File splDir = new java.io.File(this.domainMainDir+"/"+this.einstellungen.getDomain()+"/"+this.einstellungen.getProcess()+"/"+this.einstellungen.getVersion()+"/"+this.ini.get("process", "sample-sub-directory"));
		
		if(splDir.exists())
		{
			if(splDir.isDirectory())
			{
				this.log("info", "showing available testruns of selected process");
				new Testrun(this, shell, splDir.getAbsolutePath());
			}
			else
			{
				this.log("error", "expected a directory: "+splDir.getAbsolutePath());
			}
		}
		else
		{
			this.log("error", "no sample data exists for selected process: "+splDir.getAbsolutePath());
		}
	}

	/**
	 * shows a the summary results of old testruns
	 */
	private void testsummary()
	{
		
		try
		{
			String domainTestsummaryDir = ini.get("process", "domain-testsummary-directory");
			java.io.File actualTestsummaryDir = new java.io.File(domainTestsummaryDir + "/"+this.einstellungen.getDomain()+"/"+this.einstellungen.getProcess()+"/"+this.einstellungen.getVersion());
		
			if(!actualTestsummaryDir.exists() || actualTestsummaryDir.isFile())
			{
				this.log("warn", "testsummary directory does not exist. no logs to show. " + actualTestsummaryDir.getAbsolutePath());
				return;
			}
			else if(actualTestsummaryDir.isDirectory())
			{
				this.log("info", "showing available testsummaries of selected process");
				new Testsummary(this, shell, actualTestsummaryDir.getAbsolutePath());
			}
			else
			{
				this.log("error", "i don't know");
			}
		}
		catch (Exception e)
		{
			this.log("error", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * commit all the defined data to the process
	 */
	Process createInstance()
	{

		if (this.commitCreatorOld.containsKey((getActualCommitRootName())))
		{
			// wurden alle Tests erfolgreich absolviert?
			if ( ! (this.commitCreatorOld.get(getActualCommitRootName()).doAllTestsPass()))
			{
				log ("error", "not all tests passed. commit refused. check input.");
				return null;
			}
			// ist prozess konsistent?
			else if(!(process.isProcessConsistent()))
			{
				log ("error", "process data appears to be inconsistent. check process definition.");
				ArrayList<Log> logOfProcess = this.process.getLog();
				for(Log actualLog : logOfProcess)
				{
					log (actualLog.getLevel(), actualLog.getMsg());
				}
				return null;
			}
			else
			{
				// holen aller user-angaben aus dem formular
				Multimap<String,String> content = this.commitCreatorOld.get(getActualCommitRootName()).getContent();

				if(false)
				{
					log ("warn", "recently started an instance with same input.");
					
					// bestaetigungsdialog
					Shell shell = new Shell();
					MessageBox confirmation = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
					confirmation.setText("please confirm");
					
					String message = "You recently started an instance with same input.\nDo you really want to start another one.";
					confirmation.setMessage(message);
					
					// open confirmation and wait for user selection
					int returnCode = confirmation.open();
//					System.out.println("returnCode is: "+returnCode);

					// ok == 32
					if (!(returnCode == 32))
					{
						return null;
					}
				}
				
				this.process.setBaseDir(this.einstellungen.getBaseDirectory());
				this.process.genRandomId();
				
				if (this.process.makeRootdir())
				{
					log ("info", "all tests passed. performing commit.");
	//				System.out.println("Anzahl der Files in Step root: "+this.process.getStep("root").getFile().size());
					
//					// eintragen der parameter ins ini
//					Ini userIni;
//					try
//					{
//						userIni = new Ini(new File(this.userIniFile));
//						int processZaehler = 1;
//						while(userIni.containsKey(this.process.getName()+"-"+processZaehler))
//						{
//							processZaehler++;
//						}
//						
//						for(String key : content.keySet())
//						{
//							userIni.put(this.process.getName()+"-"+processZaehler, key, content.get(key));
//						}
//						// userIni File schreiben
//						userIni.store();
//						
//					} catch (InvalidFileFormatException e)
//					{
//						// TODO Auto-generated catch block
//						log("error", "invalid file format of user-ini file: "+this.userIniFile);
//						//	e.printStackTrace();
//					} catch (IOException e)
//					{
//						// TODO Auto-generated catch block
////						e.printStackTrace();
//						log("error", "problems with reading user-ini file: "+this.userIniFile);
//					}

					// user input an den Prozess committen
					this.commitCreatorOld.get(getActualCommitRootName()).commitAll();

//					System.out.println("Anzahl der Files in Step root: "+this.process.getStep("root").getFile().size());
//					System.out.println("Id des Prozesses: "+process.getRandomId());
					
					process.setOutfilebinary(process.getRootdir()+"/process.pmb");
					process.setInfilebinary(process.getRootdir()+"/process.pmb");

					process.writeBinary();
					log ("info", "writing binary instance file to disk "+process.getOutfilebinary());
					
				}
				else
				{
					log ("error", "problems handling instance directory."+process.getRootdir()+". doing nothing.");
					return null;
				}
			}
		}
		else
		{
			log ("error", "nothing to commit.");
			return null;
		}
		
		return process;
	}


	/**
	 * start instance with pkraft-manager
	 */
	void startInstance()
	{
						
						String call = ini.get("apps", "pkraft-manager") +" -instance "+process.getOutfilebinary();
						
						log ("info", "calling: " + call);
						try
						{
							java.lang.Process sysproc = Runtime.getRuntime().exec(call);
							log ("debug", "hashCode="+sysproc.hashCode());
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							log ("error", "IOException: problems with execution");
							e.printStackTrace();
						}

	}

	void setDomainMainDir (String domainMainDir)
	{
		this.domainMainDir = domainMainDir;
	}
	
	String getDomainMainDir ()
	{
		return this.domainMainDir;
	}
	
	void setIni (String relPath)
	{
		this.iniFile = WhereAmI.getInstallDirectoryAbsolutePath(this.getClass()) + "/" + relPath;
	}
	
	void setIni ()
	{
		File file = WhereAmI.getDefaultInifile(this.getClass());
		this.iniFile = file.getAbsolutePath();
	}

	public String getIniAsString ()
	{
		return this.iniFile;
	}
	
	File getIniAsFile ()
	{
		return new File(this.iniFile);
	}
	
	public Ini getIni()
	{
		return this.ini;
	}
	
	void setProcess (String process)
	{
		this.einstellungen.setProcess(process);
	}
	
	String getProcess ()
	{
		return this.einstellungen.getProcess();
	}
	
	void setVersion (String version)
	{
		this.einstellungen.setVersion(version);
	}
	
	String getVersion ()
	{
		return this.einstellungen.getVersion();
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
		}
		else
		{
			System.err.println(logstring);
		}
	}

	/**
	 * reads user setting from file
	 *  ~/pkraft/pramp.user.ini
	 * @return void
	 */
	void readUserIni()
	{
		Ini userIni = new Ini();
		userIni.setFile(new java.io.File(System.getProperty("user.home") + "/pkraft/pramp.user.ini"));
		
		// userIni laden und die werte in das datenmodell uebertragen
		try
		{
			userIni.load();
			Section prampSection = userIni.get("pramp");
			for(String actKey : prampSection.keySet())
			{
				einstellungen.setField(actKey, prampSection.get(actKey));
			}
		}
		// existiert das file noch nicht? - macht nichts
		catch (IOException e)
		{
			System.err.println("userIni file does not exist yet.");
		}
		// existiert das file noch nicht? - macht nichts
		catch (NullPointerException e)
		{
			System.err.println("illegal content in file ~/pkraft/pramp.user.ini");
			e.printStackTrace();;
		}
	}
	
	/**
	 * writes some user settings to the user ini file in
	 *  ~/pkraft/pramp.user.ini
	 * @return void
	 */
	void writeUserIni()
	{
		Ini userIni = new Ini();

		// die settings festlegen
		userIni.add("pramp", "domain", einstellungen.getDomain());
		userIni.add("pramp", "process", einstellungen.getProcess());
		userIni.add("pramp", "version", einstellungen.getVersion());
		userIni.add("pramp", "baseDirectory", einstellungen.getBaseDirectory());
		
		// temporaeres das userIni-file festlegen
		java.io.File fileUserIni = new java.io.File(System.getProperty("user.home") + "/pkraft/pramp.user.ini~");
		
		// falls das verzeichnis noch nicht existiert, soll es erstellt werden
		if(!fileUserIni.getParentFile().exists())
		{
			fileUserIni.getParentFile().mkdirs();
		}

		// das iniFile schreiben
		userIni.setFile(fileUserIni);
		try
		{
			userIni.store();
		}
		catch
		(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// das temporaere userIni auf das richtige moven
		try {
			Files.move(new java.io.File(System.getProperty("user.home") + "/pkraft/pramp.user.ini~"), new java.io.File(System.getProperty("user.home") + "/pkraft/pramp.user.ini"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(PrampPartUi1.class) + "/" + "../etc/pramp-gui.ini");

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
			formatter.printHelp("pramp-gui", options);
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
		

		// gui
		final Display display = new Display();
		
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run()
			{
				try
				{
					shell = new Shell(display);
					shell.setText("pramp "+"[% version %]");

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
					shell.setSize(1000, 780);
					Composite composite = new Composite(shell, SWT.NO_FOCUS);
					GridLayout gl_composite = new GridLayout(2, false);
					gl_composite.marginWidth = 0;
					gl_composite.marginHeight = 0;
					new PrampPartUi1(composite);
					
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
	 * @param process the process to set
	 */
	public void setProcess(Process process) {
		this.process = process;
	}
}
