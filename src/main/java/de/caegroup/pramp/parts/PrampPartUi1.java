package de.caegroup.pramp.parts;

import de.caegroup.commons.*;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;

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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.eclipse.swt.widgets.Combo;


public class PrampPartUi1 extends ModelObject
//public class PrampPartUi1
{
	static CommandLine line;
	private DataBindingContext bindingContextProcesses;
	private Button button_refresh = null;
	private Text text_logging = null;
	private Combo combo_processes = null;
	private Combo combo_versions = null;
	private String processMainDir = null;
	private String iniFile = null;
	ArrayList<String> processes = new ArrayList<String>();
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
		loadIni();
		getProcesses();
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
		getProcesses();
		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Test
	 */
	@Inject
	public PrampPartUi1(String tmp)
	{
		setIni();
		loadIni();
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
		
		Composite composite_11 = new Composite(composite_1, SWT.NONE);
		composite_11.setLayout(new GridLayout(1, false));
		GridData gd_composite_11 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_11.heightHint = 437;
		gd_composite_11.widthHint = 169;
		composite_11.setLayoutData(gd_composite_11);
		
		Group grpFilter = new Group(composite_11, SWT.NONE);
		grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpFilter.setText("which model?");
		grpFilter.setLayout(new GridLayout(4, false));
		
		Label lblNewLabel = new Label(grpFilter, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		lblNewLabel.setText("process");
		new Label(grpFilter, SWT.NONE);
		
		combo_processes = new Combo(grpFilter, SWT.NONE | SWT.READ_ONLY);
		combo_processes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		combo_processes.addModifyListener(listener_processselection);
		
		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblNewLabel_1.setText("version");
		new Label(grpFilter, SWT.NONE);
		
		combo_versions = new Combo(grpFilter, SWT.NONE | SWT.READ_ONLY);
		combo_versions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		Label lblHost = new Label(grpFilter, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
		lblHost.setText("instancedir");
		new Label(grpFilter, SWT.NONE);
		
		Group grpVisual = new Group(composite_11, SWT.NONE);
		grpVisual.setText("visual");
		grpVisual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpVisual.setLayout(new GridLayout(1, false));
		
		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(1, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("function");
		
		button_refresh = new Button(grpFunction, SWT.NONE);
		button_refresh.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		button_refresh.setText("refresh");
		button_refresh.addSelectionListener(listener_refresh_button);
		
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
		
		text_logging = new Text(composite_2, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.MULTI);
		text_logging.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// Datenbindung Processes-Combo
		initDataBindingsProcesses();
		// Datenbindung Versions-Combo
		initDataBindingsVersions();
		// Datenbindung Processes-Combo-Selection
		initDataBindingsProcess();
		// Datenbindung Versions-Combo-Selection
		initDataBindingsVersion();

		// auswahl auf das erste Element setzen
		combo_processes.select(0);
		
//		if (combo_processes.getItemCount() > 0)
//		{
//			updateUiComboVersions();
//		}
//		
		Frame frame = SWT_AWT.new_Frame(composite_12);

		frame.pack();
		frame.setLocation(0, 0);
		frame.setVisible(true);

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
		getVersions(combo_processes.getText());
		combo_versions.select(combo_versions.getItemCount()-1);
	}
	
	SelectionAdapter listener_refresh_button = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
//			System.out.println("button wurde gedrueckt");
			refresh();
		}
	};
	
	ModifyListener listener_processselection = new ModifyListener()
	{
		public void modifyText(ModifyEvent arg0)
		{
			updateUiComboVersions();
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
	
	void refresh()
	{
//		loadIni();
//		this.einstellungen.setProcesses(getProcesses());
	}

	/**
	 * loads an ini-file into the field ini
	 */
	void loadIni()
	{
		Ini ini;
		
		try
		{
			ini = new Ini(getIniAsFile());
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
	public ArrayList<String> getProcesses()
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
	public ArrayList<String> getVersions(String processName)
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
		Collections.sort(processes);
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
		
		String process = this.getProcess();
		String version = this.getVersion();
		String installationPath = this.processMainDir;
		
		if ((process != null) && (version != null) && (installationPath != null))
		{
			processDefinition = installationPath+"/"+process+"/"+version+"/process.xml";
		}
		
		return processDefinition;
	}

	void load()
	{
		// inifile parsen
		// directory fuer prozesse feststellen
		// erzeugen der prozessliste (directory parsen)
		// erzeugen der versionslisten (directory parsen)
		
		File inifile = new File(getIni());
		Ini ini;
		
		ArrayList<String> pradar_server_list = new ArrayList<String>();
		
		try
		{
			ini = new Ini(inifile);
			for(int x = 1; x <= 5; x++)
			{
				if (ini.get("pradar-server", "pradar-server-"+x) != null )
				{
					pradar_server_list.add(ini.get("pradar-server", "pradar-server-"+x));
				}
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
