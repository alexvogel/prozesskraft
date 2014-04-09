package de.caegroup.pramp.parts;

import de.caegroup.commons.*;
import de.caegroup.gui.process.CommitCreator;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.NotDirectoryException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.core.databinding.UpdateListStrategy;
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
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
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
import org.eclipse.swt.widgets.Combo;



//import com.jcraft.jsch.ChannelExec;
//import com.jcraft.jsch.JSch;
//import com.jcraft.jsch.Session;
import com.license4j.License;
import com.license4j.LicenseValidator;

import de.caegroup.commons.*;
import de.caegroup.process.Commit;
import de.caegroup.process.Process;
import de.caegroup.process.Variable;

public class PrampPartUi1 extends ModelObject
//public class PrampPartUi1
{
	static CommandLine line;
	private DataBindingContext bindingContextProcesses;
	private Button button_start = null;
//	private Text text_logging = null;
	private StyledText text_logging = null;
	private Combo combo_processes = null;
	private Combo combo_versions = null;
	private Combo combo_hosts = null;
	private String processMainDir = null;
	private String processDefinitionPath = null;
	private Process process = null;
	private String iniFile = null;
	private String userIniFile = null;
	
	private ArrayList<String> license_server_port_at_hostname = new ArrayList<String>();
	
	private boolean erster_license_check = true;
//	private Ini userIni = null;
	private Text text_rootdirectory = null;
	
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
	
	PrampViewModel einstellungen = new PrampViewModel();

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
		setIni();
		setUserIni();
		loadIni();
		checkLicense();
		getInstalledProcessNames();
		getHosts();
//		setRandomRootdirectory();
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PrampPartUi1(Composite composite)
	{
		setIni();
		loadIni();
		checkLicense();
		setUserIni();
		getInstalledProcessNames();
		getHosts();
//		setRandomRootdirectory();
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Test
	 */
	@Inject
	public PrampPartUi1(String tmp)
	{
		setIni("target/test-classes/etc/default.ini");
		loadIni();
		checkLicense();
		setUserIni();
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
		grpFilter.setText("select definition");
		grpFilter.setLayout(new GridLayout(4, false));
		
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
		grpVisual.setText("location");
		GridData gd_grpVisual = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_grpVisual.widthHint = 139;
		grpVisual.setLayoutData(gd_grpVisual);
		grpVisual.setLayout(new GridLayout(4, false));
		
		Label lblNewLabel_2 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_2.setToolTipText("host to run instance");
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		lblNewLabel_2.setText("host");
		new Label(grpVisual, SWT.NONE);
		
		combo_hosts = new Combo(grpVisual, SWT.NONE);
		GridData gd_combo_hosts = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
		gd_combo_hosts.widthHint = 127;
		combo_hosts.setLayoutData(gd_combo_hosts);
		new Label(grpVisual, SWT.NONE);
		
		Label lblRootdirectory = new Label(grpVisual, SWT.NONE);
		lblRootdirectory.setToolTipText("directory for instance data");
		lblRootdirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		lblRootdirectory.setText("base directory");
		new Label(grpVisual, SWT.NONE);
		
		text_rootdirectory = new Text(grpVisual, SWT.BORDER);
		text_rootdirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_rootdirectory.setText(System.getProperty("user.dir"));
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
		grpFunction.setText("function");
		
		button_start = new Button(grpFunction, SWT.NONE);
		button_start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_start.setText("start");
		button_start.addSelectionListener(listener_startinstance_button);
		
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

		// Datenbindung Processes-Combo
		initDataBindingsProcesses();
		// Datenbindung Versions-Combo
		initDataBindingsVersions();
		// Datenbindung Hosts-Combo
		initDataBindingsHosts();
		// Datenbindung Processes-Combo-Selection
		initDataBindingsProcess();
		// Datenbindung Versions-Combo-Selection
		initDataBindingsVersion();
		// Datenbindung Hosts-Combo-Selection
		initDataBindingsHost();
		// Datenbindung instancedirectory textfeld
		initDataBindingsInstancedirectory();

		// auswahl der Processes-Combo auf das erste Element setzen
		combo_processes.select(0);
		new Label(grpFilter, SWT.NONE);

		// auswahl der Hosts-Combo auf das erste Element setzen
		combo_hosts.select(0);

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
	
	void updateUiComboVersions()
	{
		getInstalledVersionNames(combo_processes.getText());
		combo_versions.select(combo_versions.getItemCount()-1);
	}
	
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
	        dlg.setFilterPath(text_rootdirectory.getText());

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
	        	einstellungen.setRootDirectory(dir);
	        	log("info", "setting instancedirectory: "+dir);
//	        	text_instancedirectory.setText(dir);
	        }
		}
	};
	
	/**
	 * listener for Selections in of button 'refresh'
	 */
	SelectionAdapter listener_startinstance_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button wurde gedrueckt");
			startInstance();
		}
	};
	
//	ModifyListener listener_versionselection = new ModifyListener()
//	{
//		public void modifyText(ModifyEvent arg0)
//		{
//			updateUiParameterAbfrage();
//		}
//	};	
//	
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
	 * binds array of versions to combo-box 'hosts'
	 */
	protected DataBindingContext initDataBindingsHosts()
	{
		DataBindingContext bindingContextHosts = new DataBindingContext();
		//
		IObservableList targetObservableHosts = WidgetProperties.items().observe(combo_hosts);
		IObservableList modelObservableHosts = BeanProperties.list("hosts").observe(einstellungen);
		bindingContextHosts.bindList(targetObservableHosts, modelObservableHosts, null, null);
		//
		return bindingContextHosts;
	}
	
	/**
	 * binds selection of combo-box 'host' to String host
	 */
	protected DataBindingContext initDataBindingsHost()
	{
		DataBindingContext bindingContextHost = new DataBindingContext();
		//
		IObservableValue targetObservableHost = WidgetProperties.text().observe(combo_hosts);
		IObservableValue modelObservableHost = BeanProperties.value("host").observe(einstellungen);
		bindingContextHost.bindValue(targetObservableHost, modelObservableHost, null, null);
		//
		return bindingContextHost;
	}
	
	/**
	 * binds content of textfield 'instancedirectory' to String instancedirectory
	 */
	protected DataBindingContext initDataBindingsInstancedirectory()
	{
		DataBindingContext bindingContextInstancedirectory = new DataBindingContext();
		//
		IObservableValue targetObservableInstancedirectory = WidgetProperties.text(SWT.Modify).observe(text_rootdirectory);
		IObservableValue modelObservableInstancedirectory = BeanProperties.value("rootDirectory").observe(einstellungen);
		bindingContextInstancedirectory.bindValue(targetObservableInstancedirectory, modelObservableInstancedirectory, null, null);
		//
		IObservableValue targetObservableInstancedirectoryTooltip = WidgetProperties.tooltipText().observe(text_rootdirectory);
		IObservableValue modelObservableInstancedirectoryTooltip = BeanProperties.value("rootDirectory").observe(einstellungen);
		bindingContextInstancedirectory.bindValue(targetObservableInstancedirectoryTooltip, modelObservableInstancedirectoryTooltip, null, null);
		//
		return bindingContextInstancedirectory;
	}
	
	/**
	 * loads an ini-file
	 */
	void loadIni()
	{
		Ini ini;
		ArrayList<String> license_server_list = new ArrayList<String>();

		try
		{
			ini = new Ini(getIniAsFile());
			if (ini.get("process", "process-installation-directory") != null )
			{
				this.processMainDir = (ini.get("process", "process-installation-directory"));
				
				// wenn es sich um einen relativen path handelt, soll dieser mit dem installationsverzeichnis ergaenzt werden
				if(!(this.processMainDir.matches("^/.+")))
				{
					
					this.processMainDir = WhereAmI.getInstallDirectoryAbsolutePath(this.getClass())+"/"+this.processMainDir;
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
	 * loads an user-ini-file into the field userIni
	 */
	void loadUserIni()
	{
		Ini ini;
		try
		{
			ini = new Ini(getUserIniAsFile());
			if (ini.get("process", "process-installation-directory") != null )
			{
				this.processMainDir = (ini.get("process", "process-installation-directory"));
			}
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

	/**
	 * determines all processes of a specific process-installation-directory
	 * @return a list of all installed processes sorted in alphabetical order
	 */
	public ArrayList<String> getInstalledProcessNames()
	{
		ArrayList<String> processes = new ArrayList<String>();
		try
		{
			processes = getSubDirectories(this.processMainDir);
		} catch (NotDirectoryException e)
		{
			System.err.println("not a directory: "+this.processMainDir);
//			e.printStackTrace();
		}

		// sortieren
		Collections.sort(processes);

		// zentrale daten setzen
		einstellungen.setProcesses((String[]) processes.toArray(new String[processes.size()]));
		
		return processes;
	}

	/**
	 * determines all versions of a specific process
	 * @return a list of all installed processes sorted in alphabetical order
	 */
	public ArrayList<String> getInstalledVersionNames(String processName)
	{
		ArrayList<String> versions = new ArrayList<String>();
		String directoryPath = this.processMainDir+"/"+processName;
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
	 * determines all available hosts which are known by name
	 * @return a list of all available hosts
	 */
	public ArrayList<String> getHosts()
	{
		ArrayList<String> hosts = new ArrayList<String>();
		
		// feststellen des lokalen hosts
		try
		{
			hosts.add(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sortieren
		Collections.sort(hosts);
		
		// zentrale daten setzen
		einstellungen.setHosts((String[]) hosts.toArray(new String[hosts.size()]));
		
//		// in combo_box einfuegen
//		combo_versions.setItems((String[]) versions.toArray(new String[versions.size()]));
//		combo_versions.select(versions.size()-1);
//		
		return hosts;
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
	 * determines the path to the process-definition-file
	 * @return String path to process-definition
	 * @return null if parts of path are not available or path does not point to a file
	 */
	public String getProcessDefinition()
	{
		String processDefinition = null;
		
		String process = this.combo_processes.getText();
		String version = this.combo_versions.getText();
		String installationPath = this.processMainDir;
		
		if ((process != null) && (version != null) && (installationPath != null))
		{
			processDefinition = installationPath+"/"+process+"/"+version+"/process.xml";
		}
		
		if ( (processDefinition == null) || (!(new java.io.File(processDefinition).exists())) )
		{
	    	log("error", "process definition does not exist: "+processDefinition);
			processDefinition = null;
			this.process = null;
		}
		else
		{
	    	log("info", "setting process definition: "+processDefinition);
			Process tmp = new Process();
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
			
		}
		this.processDefinitionPath = processDefinition;
		return processDefinition;
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

				CommitCreator commitCreator = new CommitCreator(actualComposite, this.process.getStep("root"));

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
	
	/**
	 * creates an instance directory if it does not exist
	 * if directory already exists and is empty, returns true
	 * if directory already exists and contains files or directories, returns false
	 * @return true if after this method an empty directory exists
	 */
	private boolean createInstanceDir()
	{
		java.io.File instanceDir = new java.io.File(this.einstellungen.getRootDirectory());
		
		boolean result = false;
		
		if (instanceDir.exists())
		{
			log("warn", "instance directory exists: "+instanceDir.getAbsoluteFile());
			if (instanceDir.listFiles().length > 0)
			{
				log("error", "instance directory not empty: "+instanceDir.getAbsoluteFile());
				log("info", "choose an empty or nonexistent instance directory.");
				result = false;
			}
			else
			{
				log("info", "instance directory is empty - thats good..");
			}
		}
		else
		{
			result = instanceDir.mkdirs();
		}
		return result;
	}
	
	private boolean parameterAlreadyUsed(Map<String,String> content)
	{
		boolean result = false;

		Ini userIni;
		try
		{
			userIni = new Ini(new File(this.userIniFile));
			int processZaehler = 1;
			while(userIni.containsKey(this.process.getName()+"-"+processZaehler))
			{
				Boolean comparison = null;
				
				Map<String,String> actualMapIni = userIni.get(this.process.getName()+"-"+processZaehler);
				
				// die aktuellen input-daten vergleichen mit dem block aus dem inifile
				for(String key : content.keySet())
				{
//					System.out.println("mapIni:  "+actualMapIni.get(key));
//					System.out.println("content: "+content.get(key));
					if ( (actualMapIni.get(key) != null) && (content.get(key) != null) && (!(content.get(key).equals(actualMapIni.get(key)))) )
					{
//						log("info", "keine uebereinstimmung fuer section "+this.process.getName()+"-"+processZaehler+" key: "+key+" value-content: "+content.get(key)+" value-ini: "+actualMapIni.get(key));
						comparison = false;
						break;
					}
					else if (comparison == null)
					{
						comparison = true;
					}
				}
				
				// wenn beide datensaetze uebereinstimmen, dann
				if (comparison)
				{
					result = true;
					return result;
				}
				processZaehler++;
			}

			
		} catch (InvalidFileFormatException e)
		{
			// TODO Auto-generated catch block
			log("error", "invalid file format of user-ini file: "+this.userIniFile);
			//	e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
//			e.printStackTrace();
			log("error", "problems with reading user-ini file: "+this.userIniFile);
		}
		return result;
	}
	
	/**
	 * commit all the defined data to the process
	 */
	private boolean startInstance()
	{
//		System.out.println("button commit");

		if (this.commitCreatorOld.containsKey((getActualCommitRootName())))
		{
			if ( ! (this.commitCreatorOld.get(getActualCommitRootName()).doAllTestsPass()))
			{
				log ("error", "not all tests passed. commit refused. check input.");
				return false;
			}
			else if(!(process.isProcessConsistent()))
			{
				log ("error", "process data appears to be inconsistent. check process definition.");
				return false;
			}
			else
			{
				// holen aller user-angaben aus dem formular
				Map<String,String> content = this.commitCreatorOld.get(getActualCommitRootName()).getContent();

				if(parameterAlreadyUsed(content))
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
						return false;
					}
				}
				
				if (createInstanceDir())
				{
					log ("info", "all tests passed. performing commit.");
	//				System.out.println("Anzahl der Files in Step root: "+this.process.getStep("root").getFile().size());
					
					// eintragen der parameter ins ini
					Ini userIni;
					try
					{
						userIni = new Ini(new File(this.userIniFile));
						int processZaehler = 1;
						while(userIni.containsKey(this.process.getName()+"-"+processZaehler))
						{
							processZaehler++;
						}
						
						for(String key : content.keySet())
						{
							userIni.put(this.process.getName()+"-"+processZaehler, key, content.get(key));
						}
						// userIni File schreiben
						userIni.store();
						
					} catch (InvalidFileFormatException e)
					{
						// TODO Auto-generated catch block
						log("error", "invalid file format of user-ini file: "+this.userIniFile);
						//	e.printStackTrace();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
//						e.printStackTrace();
						log("error", "problems with reading user-ini file: "+this.userIniFile);
					}

					// user input an den Prozess committen
					this.commitCreatorOld.get(getActualCommitRootName()).commitAll();
//					System.out.println("Anzahl der Files in Step root: "+this.process.getStep("root").getFile().size());
//					System.out.println("Id des Prozesses: "+process.getRandomId());
					
					process.setOutfilebinary(this.einstellungen.getRootDirectory()+"/"+this.einstellungen.getProcess()+".pmb");
					process.setInfilebinary(this.einstellungen.getRootDirectory()+"/"+this.einstellungen.getProcess()+".pmb");
					process.setRootdir(this.einstellungen.getRootDirectory());
					
					process.writeBinary();
					log ("info", "writing binary instance file to disk "+process.getOutfilebinary());
					
					// kurz schlafen damit der schreibvorgang beendet werden kann
					try
					{
						Thread.sleep(1000);
					} catch (InterruptedException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					// starten des process-manager remote
					// ....
					log ("info", "launching process instance over ssh on "+System.getProperty("user.name")+"@"+combo_hosts.getText());


					String[] args_for_command = {"ssh", System.getProperty("user.name")+"@"+combo_hosts.getText(), "\\\"process-manager -instance "+process.getOutfilebinary()+"\\\""};

					ProcessBuilder pb = new ProcessBuilder(args_for_command);
					
					try
					{
						java.lang.Process p = pb.start();
						log ("info", "calling: "+"ssh"+" "+System.getProperty("user.name")+"@"+combo_hosts.getText()+" "+"\"process-manager -instance "+process.getOutfilebinary()+"\"");
						log ("debug", "hashCode="+p.hashCode());
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						log ("error", "IOException: problems with executing via ssh");
						e.printStackTrace();
					}
					return true;

				}
				else
				{
					log ("error", "problems handling instance directory. doing nothing.");
					return false;
				}
			}
		}
		else
		{
			log ("error", "nothing to commit.");
			return false;
		}
	}
	
//	void load()
//	{
//		// inifile parsen
//		// directory fuer prozesse feststellen
//		// erzeugen der prozessliste (directory parsen)
//		// erzeugen der versionslisten (directory parsen)
//		
//		File inifile = new File(getIni());
//		Ini ini;
//		
//		ArrayList<String> pradar_server_list = new ArrayList<String>();
//		
//		try
//		{
//			ini = new Ini(inifile);
//			for(int x = 1; x <= 5; x++)
//			{
//				if (ini.get("pradar-server", "pradar-server-"+x) != null )
//				{
//					pradar_server_list.add(ini.get("pradar-server", "pradar-server-"+x));
//				}
//			}
//
//		}
//		catch (InvalidFileFormatException e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		catch (IOException e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	}

	void setProcessMainDir (String processMainDir)
	{
		this.processMainDir = processMainDir;
	}
	
	String getProcessMainDir ()
	{
		return this.processMainDir;
	}
	
	void setIni (String pathIniFile)
	{
		this.iniFile = pathIniFile;
	}
	
	void setIni ()
	{
		File file = WhereAmI.getDefaultInifile(this.getClass());
		this.iniFile = file.getAbsolutePath();
	}

	String getIni ()
	{
		return this.iniFile;
	}
	
	File getIniAsFile ()
	{
		return new File(this.iniFile);
	}
	
	void setUserIni ()
	{
		File file = new File((System.getProperty("user.home")+"/.process/pramp.ini"));
		if(!(file.exists()))
		{
			log("warn", "create user config directory: "+file.getParentFile().getAbsolutePath());
			file.getParentFile().mkdirs();
			try
			{
				log("warn", "create user ini file: "+file.getAbsolutePath());
				Ini ini = new Ini();
//				log("warn", "storing create user ini file: "+file.getAbsolutePath());
				ini.store(file);
				
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				log("error", "cannot create user ini file: "+file.getAbsolutePath());
				//				e.printStackTrace();
			}
		}
		this.userIniFile = file.getAbsolutePath();
	}

	String getUserIni ()
	{
		return this.userIniFile;
	}

	File getUserIniAsFile ()
	{
		return new File(this.userIniFile);
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
		
		if (this.license_server_port_at_hostname.size() == 0)
		{
			log("error", "no license server defined. check configuration.");
		}
		
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
					shell.setText("pramp-gui "+"v[% version %]");
					shell.setLayout(new FillLayout());
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
}
