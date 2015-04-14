package de.caegroup.pramp.testrun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TestrunItem {

	private Testrun father = null;
	private String name = "unnamed";
	private File callFile = null;
	private File splDir = null;
	private CTabFolder tabfolder = null;
	private String comment = "-";
	private String call = "-";

	public TestrunItem(Testrun father, String name, File callFile, File splDir, CTabFolder tabfolder)
	{
		this.father = father;
		this.name = name;
		this.callFile = callFile;
		this.tabfolder = tabfolder;
		this.splDir = splDir;

		// die Daten aus dem spl-call-file ermitteln
		this.detDataFromCallFile();
		
		CTabItem tabItem_testcase = new CTabItem(this.tabfolder, SWT.NONE);
		tabItem_testcase.setText(this.name);
		tabItem_testcase.setToolTipText("testrun: "+this.callFile.getAbsolutePath());

		Composite composite = new Composite(this.tabfolder, SWT.FILL | SWT.BORDER);

		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabItem_testcase.setControl(composite);
		
		createControls(composite);

	}
	
	/**
	 * extract data from call-file
	 */
	private void detDataFromCallFile()
	{
		// die datei in eine ArrayList einlesen
		ArrayList<String> lines = new ArrayList<String>();
		try
		{
			FileReader fileReader = new FileReader(this.callFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			
			String line = null;
			
			while((line = bufferedReader.readLine()) != null)
			{
				lines.add(line);
			}
			bufferedReader.close();
			fileReader.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// den inhalt der datei in die strings comment / call einfuellen
		String newComment = "";
		String newCall = "";
		for(String actLine : lines)
		{
			if(actLine.matches("^#.+$"))
			{
				newComment += actLine+"\n";
			}
			else
			{
				newCall += actLine+"\n";
			}
		}
		
		// wenn inhalt gelesen wurde, dann in die zentralen variablen kopieren
		if(newComment.length()>1)
		{
			this.comment = newComment.substring(0, newComment.length()-2);;
		}
		if(newCall.length()>1)
		{
			this.call = newCall.substring(0, newCall.length()-2);
		}
	}
	
	/**
	 * Create contents of the view part.
	 */
	private void createControls(Composite composite)
	{
		composite.setLayout(new GridLayout(1, false));

		Composite compositeEntries = new Composite(composite, SWT.NONE);

		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
//		gd_composite.minimumWidth = 1000;
//		gd_composite.minimumHeight = 1000;
		compositeEntries.setLayoutData(gd_composite);

//		Device device = Display.getCurrent();
//		Color red = new Color(device, 255, 0, 0);
//		compositeEntries.setBackground(red);
		compositeEntries.setLayout(new GridLayout(2, false));

		// comment
		Label comment = new Label(compositeEntries, SWT.NONE);
		comment.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		comment.setSize(300,30);
		comment.setText("comment");
		comment.setToolTipText("a small description");
		
		Text commentText = new Text(compositeEntries, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		commentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		commentText.setText(this.comment);

		// call
		Label call = new Label(compositeEntries, SWT.NONE);
		call.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		call.setText("call");
		call.setToolTipText("full call");
		
		Text callText = new Text(compositeEntries, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		callText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		callText.setText(this.call);

		compositeEntries.layout();
		
		// button
		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Label dummyLabel = new Label(compositeBtn, SWT.NONE);
		dummyLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnCancel = new Button(compositeBtn, SWT.NONE);
		btnCancel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnCancel.setText("cancel");
		btnCancel.addSelectionListener(listenerButtonCancel);
		
		Button btnRun = new Button(compositeBtn, SWT.NONE);
		btnRun.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnRun.setText("run test");
		btnRun.addSelectionListener(listenerButtonRun);
		
		compositeBtn.layout();
	}

	SelectionAdapter listenerButtonRun = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{

//			father.getFather().process.setBaseDir(father.getFather().einstellungen.getBaseDirectory());
//			father.getFather().process.genRandomId();
//			father.getFather().process.makeRootdir();

			de.caegroup.process.Process dummyProcess = new de.caegroup.process.Process();
			dummyProcess.setName("testrun-"+name);
			dummyProcess.setVersion("Intern");
			dummyProcess.setBaseDir(father.getFather().einstellungen.getBaseDirectory());
			dummyProcess.makeRootdir();

			String schalterPmodelLaunch = father.getFather().getIni().get("start", "pmodel");
			String schalterManagerLaunch = father.getFather().getIni().get("start", "process-manager");

			String instanceDir = dummyProcess.getRootdir();
			String syscall = father.getFather().getIni().get("apps", "process-syscall");

			try
			{
				// den Aufrufstring fuer die externe App (process syscall --version 0.6.0)) splitten
				// beim aufruf muss das erste argument im path zu finden sein, sonst gibt die fehlermeldung 'no such file or directory'
				ArrayList<String> processSyscallWithArgs = new ArrayList<String>(Arrays.asList(syscall.split(" ")));

				// die sonstigen argumente hinzufuegen
				processSyscallWithArgs.add("-call");
				if(schalterManagerLaunch.equals("true"))
				{
					processSyscallWithArgs.add(father.getFather().getIni().get("apps", "ptest-launch") + " -spl "+getSplDir().getAbsolutePath()+" -call "+callFile+" -instancedir "+instanceDir);
				}
				else
				{
					processSyscallWithArgs.add(father.getFather().getIni().get("apps", "ptest-launch") + " -spl "+getSplDir().getAbsolutePath()+" -call "+callFile+" -instancedir "+instanceDir + " -addopt '-nostart'");
				}
				processSyscallWithArgs.add("-stdout");
				processSyscallWithArgs.add(instanceDir+"/.stdout.ptest-launch.txt");
				processSyscallWithArgs.add("-stderr");
				processSyscallWithArgs.add(instanceDir+"/.stderr.ptest-launch.txt");
				processSyscallWithArgs.add("-pid");
				processSyscallWithArgs.add(instanceDir+"/.pid.ptest-launch");
				processSyscallWithArgs.add("-mylog");
				processSyscallWithArgs.add(instanceDir+"/.log.ptest-launch");
				processSyscallWithArgs.add("-maxrun");
				// ~2 Tage
				processSyscallWithArgs.add("3000");

				// erstellen prozessbuilder
				ProcessBuilder pb = new ProcessBuilder(processSyscallWithArgs);

				// erweitern des PATHs um den prozesseigenen path
//				Map<String,String> env = pb.environment();
//				String path = env.get("PATH");
//				log("debug", "$PATH="+path);
//				path = this.parent.getAbsPath()+":"+path;
//				env.put("PATH", path);
//				log("info", "path: "+path);
				
				// setzen der aktuellen directory
				java.io.File directory = new java.io.File(instanceDir);
				father.getFather().log("info", "setting execution directory to: "+directory.getAbsolutePath());
				pb.directory(directory);

				// zum debuggen ein paar ausgaben
//				java.lang.Process p1 = Runtime.getRuntime().exec("date >> ~/tmp.debug.work.txt");
//				p1.waitFor();
//				java.lang.Process p2 = Runtime.getRuntime().exec("ls -la "+this.getParent().getAbsdir()+" >> ~/tmp.debug.work.txt");
//				p2.waitFor();
//				java.lang.Process pro = Runtime.getRuntime().exec("nautilus");
//				java.lang.Process superpro = Runtime.getRuntime().exec(processSyscallWithArgs.toArray(new String[processSyscallWithArgs.size()]));
//				p3.waitFor();
				
				father.getFather().log("info", "calling: " + pb.command());

				// starten des prozesses
				java.lang.Process sysproc = pb.start();
				
				// falls pmodel gestartet werden soll
				if(schalterPmodelLaunch.equals("true"))
				{
					int maxWait = 10;
					int actualWait = 0;
					// a bisserl schlafen bis process-startinstance das unterverzeichnis mit process.pmb angelegt hat
					father.getFather().log("info", "sleeping 2 seconds - waiting for the process.pmb of executed testrun to become available on disk");
					Thread.sleep(2000);
					actualWait += 2;
					
					// da an dieser stelle das genaue verzeichnis des prozesses nicht bekannt ist (das wird mit process-startinstance erstellt) muss das erst herausgefunden werden
					// das rootDir von ptest-launch ist das basedir des prozesses, der mit startinstance angeschoben wird
					java.io.File baseDirOfStartinstance = new java.io.File(dummyProcess.getRootdir());

					ArrayList<File> allFoundProcessBinaries = new ArrayList<File>();
					
					boolean processBinaryNichtVorhanden = true;
					
					while(processBinaryNichtVorhanden && (actualWait<maxWait))
					{
						
						// loeschen evtl. eintraege
						allFoundProcessBinaries.clear();
						
						// alle process.pmb in unterverzeichnissen finden
						for(java.io.File actFile : baseDirOfStartinstance.listFiles())
						{
	//						System.err.println("seeing entry: "+actFile.getCanonicalPath());
							if(actFile.isDirectory())
							{
	//							System.err.println("its a directory - going in...");
								for(java.io.File actFileFile : actFile.listFiles())
								{
	//								System.err.println("seeing file: (name="+actFileFile.getName()+") "+actFileFile.getCanonicalPath());
									if(actFileFile.getName().equals("process.pmb"))
									{
										allFoundProcessBinaries.add(actFileFile);
									}
								}
							}
						}
						
						if(allFoundProcessBinaries.size() == 0)
						{
							father.getFather().log("warn", "process-binary still not found.");
							Thread.sleep(2000);
							actualWait += 2;
						}
						else
						{
							processBinaryNichtVorhanden = false;
						}
						
					}
					
					if(allFoundProcessBinaries.size() > 1)
					{
						father.getFather().log("error", "cannot open pmodel-gui because more than 1 process.pmb found in subdirectories of "+baseDirOfStartinstance);
						for(java.io.File actFile : allFoundProcessBinaries)
						{
							father.getFather().log("debug", "this is a process.pmb: "+actFile.getCanonicalPath());
						}
					}
					else
					{
						String pmodelCall = father.getFather().getIni().get("apps", "pmodel") + " -instance " + allFoundProcessBinaries.get(0).getCanonicalPath();
						ArrayList<String> pmodelCallAsArray = new ArrayList<String>(Arrays.asList(pmodelCall.split(" ")));
						ProcessBuilder pb2 = new ProcessBuilder(pmodelCallAsArray);
						pb2.directory(new java.io.File(instanceDir));
						father.getFather().log("info", "calling: " + pb2.command());
						// starten des pmodel
						java.lang.Process sysproc2 = pb2.start();
					}
				}
				
			}
			catch (Exception e)
			{
				father.getFather().log("error", e.getMessage());
			}
			getFather().shell.dispose();
		}
	};

	SelectionAdapter listenerButtonCancel = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			getFather().shell.dispose();
		}
	};

	/**
	 * @return the father
	 */
	public Testrun getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(Testrun father) {
		this.father = father;
	}

	/**
	 * @return the splDir
	 */
	public File getSplDir() {
		return splDir;
	}

	/**
	 * @param splDir the splDir to set
	 */
	public void setSplDir(File splDir) {
		this.splDir = splDir;
	}	

}
