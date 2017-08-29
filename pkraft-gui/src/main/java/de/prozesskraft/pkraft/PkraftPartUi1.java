package de.prozesskraft.pkraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.license4j.LicenseValidator;

import org.eclipse.swt.custom.CTabFolder;

//import de.caegroup.pradar.Init;
import de.prozesskraft.pradar.parts.PradarPartUi3;
import de.prozesskraft.pramp.parts.PrampPartUi1;
import de.prozesskraft.commons.MyLicense;
import de.prozesskraft.commons.WhereAmI;
import de.prozesskraft.pmodel.PmodelPartUi1;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class PkraftPartUi1 implements de.prozesskraft.pradar.parts.IPkraftPartUi1, de.prozesskraft.gui.step.insight.IPkraftPartUi2
{
	static Ini ini;
	static CommandLine line;
	
	Display display;

	CTabFolder tabFolder = null;

	Composite processInsight = null;

	ArrayList<Map<String,CTabItem>> processId_CTabItem = new ArrayList<Map<String,CTabItem>>();
	Map<CTabItem,PmodelPartUi1> CTabItem_pModel = new HashMap<CTabItem,PmodelPartUi1>();

	/**
	 * constructor als EntryPoint fuer WindowBuilder
	 * @wbp.parser.entryPoint
	 */
	public PkraftPartUi1()
	{
		Shell shell = new Shell();
		shell.setSize(1200, 800);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLocation(0, 0);

		createControls(composite);
	}

	/**
	 * constructor als EntryPoint fuer Main oder RCP
	 */
	@Inject
	public PkraftPartUi1(Composite composite) 
	{
		createControls(composite);
	}


	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite composite)
	{

		composite.setSize(1200, 800);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, true));
		
	
//		Composite composite_1 = new Composite(composite, SWT.NONE);
//		GridLayout gl_composite_1 = new GridLayout(1, false);
//		gl_composite_1.marginWidth = 0;
//		gl_composite_1.marginHeight = 0;
//		composite_1.setLayout(gl_composite_1);
//		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		gd_composite_1.heightHint = 445;
//		gd_composite_1.widthHint = 122;
//		composite_1.setLayoutData(gd_composite_1);
		
		tabFolder = new CTabFolder(composite, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

		// einen selectionListener einrichten um nur das pmodel des aktiven tabs zu aktivieren
		tabFolder.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent event)
			{
				// alle CTabItems, die eine pmodel Animation enthalten sollen auf sleep gesetzt werden
				for(CTabItem actCTabItem : CTabItem_pModel.keySet())
				{
					CTabItem_pModel.get(actCTabItem).einstellungen.setSleep(true);
				}
				
				// das pmodel im selectierten CTabitem soll aufgeweckt werden
				CTabItem selektiertesCTabItem = tabFolder.getSelection();
				if(CTabItem_pModel.containsKey(selektiertesCTabItem))
				{
					CTabItem_pModel.get(tabFolder.getSelection()).einstellungen.setSleep(false);
				}
			}
		});
		
		// erstellen des items fuer pramp
		CTabItem tabItemPramp = new CTabItem(tabFolder, SWT.NONE);
		tabItemPramp.setText("pramp");
		tabItemPramp.setToolTipText("launch");

		Composite compositePramp = new Composite(tabFolder, SWT.NONE);
		GridLayout gl_compositePramp = new GridLayout(1, false);
		gl_compositePramp.marginWidth = 0;
		gl_compositePramp.marginHeight = 0;
		compositePramp.setLayout(gl_compositePramp);

		// pramp erstellen
		PrampPartUi1 prampUi = new PrampPartUi1(compositePramp);
//		prampUi.setPkraft(this);
		
		// das tabItem dem tabfolder hinzufuegen
		tabItemPramp.setControl(compositePramp);
		
		// erstellen des items fuer pradar
		CTabItem tabItemPradar = new CTabItem(tabFolder, SWT.NONE);
		tabItemPradar.setText("pradar");
		tabItemPradar.setToolTipText("observe");

		Composite compositePradar = new Composite(tabFolder, SWT.NONE);
		GridLayout gl_compositePradar = new GridLayout(1, false);
		gl_compositePradar.marginWidth = 0;
		gl_compositePradar.marginHeight = 0;
		compositePradar.setLayout(gl_compositePradar);

		// pradar erstellen
		PradarPartUi3 pradarUi = new PradarPartUi3(compositePradar);
		pradarUi.setPkraft(this);

		// das item platzieren
		tabItemPradar.setControl(compositePradar);

		// auf pradar selektieren
		tabFolder.setSelection(1);
	}

	/**
	 * opens a process.pmb in a new tabItem
	 * @param pathToInstance
	 */
	public void openInstance(String pathToInstance) throws IOException
	{
		boolean ctabMussNeuErzeugtWerden = true;
		
		// einlesen der instanz
		Process p1 = new Process();
		p1.setInfilebinary(pathToInstance);
		Process p2 = p1.readBinary();
		
		// gibt es den CTabItem bereits, soll dieser selektiert werden
		int counter = 2; // bereits 2 CTabItems vorhanden (pradar, pramp);
		
		// jeden eintrag der liste durchgehen und sehen ob das schon der gesuchte Map mit der id -> CTabItem ist
		for(Map<String,CTabItem> actIdCTabItem : processId_CTabItem)
		{
			for(String oneExistentId : actIdCTabItem.keySet())
			{
				if(oneExistentId.equals(p2.getId()))
				{
					ctabMussNeuErzeugtWerden = false;
					tabFolder.setSelection(counter);
				}
			}
			counter++;
		}
//			String pathToInstance2 = new java.io.File(pathToInstance).getCanonicalPath();
//			
//			// gibt es den CTabItem bereits, soll dieser selektiert werden
//			int counter = 2; // bereits 2 CTabItems vorhanden (pradar, pramp);
//			for(String oldPath : pmodel_id_item.keySet())
//			{
//				if(oldPath.equals(pathToInstance2))
//				{
//					System.err.println("instance already open: "+pathToInstance2);
//					counter++;
//					ctabMussNeuErzeugtWerden = false;
//					tabFolder.setSelection(counter-1);
//				}
//				else
//				{
//					System.err.println("opening instance: " + pathToInstance2);
//				}
//			}

		// falls eine ansicht neu erzeugt werden muss
		if(ctabMussNeuErzeugtWerden)
		{
			// erstellen des items fuer pmodel
			CTabItem tabItemPmodel = new CTabItem(tabFolder, SWT.NONE);
			tabItemPmodel.setShowClose(true);

			Composite compositePmodel = new Composite(tabFolder, SWT.NONE);
			GridLayout gl_compositePmodel = new GridLayout(1, false);
			gl_compositePmodel.marginWidth = 0;
			gl_compositePmodel.marginHeight = 0;
			compositePmodel.setLayout(gl_compositePmodel);
			
			// falls es fehlschlaegt, kurz warten und nochmal versuchen
			PmodelPartUi1 pmodelUi = null;
			try
			{
				// Versuch 1
				pmodelUi = new PmodelPartUi1(compositePmodel, pathToInstance);
			}
			catch (NullPointerException e)
			{
				try
				{
					// kurz warten und nochmal versuchen
					Thread.sleep(200);
				}
				catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// Versuch 2
				pmodelUi = new PmodelPartUi1(compositePmodel, pathToInstance);
			}
			
			//
			pmodelUi.setPkraft(this);
			tabItemPmodel.setText(pmodelUi.getProcess().getName() + " " + pmodelUi.getProcess().getId2() + " " + pmodelUi.getProcess().getId());
			tabItemPmodel.setToolTipText(pmodelUi.getProcess().getName() + " - " + pmodelUi.getProcess().getVersion() + " - " + pathToInstance);

			tabItemPmodel.addDisposeListener(entferne_ctabitem);

			// das neue tabItem dem tabFolder hinzufuegen
			tabItemPmodel.setControl(compositePmodel);

			// das neueste tabItem selektieren
			tabFolder.setSelection(tabFolder.getItemCount()-1);

			// das neue tabItem dem map der bekannten tabItems hinzufuegen
			Map<String,CTabItem> newPmodel = new HashMap<String,CTabItem>();
			newPmodel.put(p2.getId(), tabItemPmodel);

			// der liste der bekannten CTabItems hinzufuegen
			this.processId_CTabItem.add(newPmodel);
			
			// das pmodel der liste der geoeffneten pmodels hinzufuegen
			this.CTabItem_pModel.put(tabItemPmodel, pmodelUi);
		}
		
	}
	
	/**
	 * listener for Disposing a CtabItem
	 */
	DisposeListener entferne_ctabitem = new DisposeListener()
	{
		
//		ArrayList<Map<String,CTabItem>> pmodel_id_item = new ArrayList<HashMap<String,CTabItem>();

		public void widgetDisposed(DisposeEvent event)
		{
			CTabItem zuLoeschendesCTabItem = (CTabItem) event.widget;

			// zuerst aus dem map entfernen
			CTabItem_pModel.remove(zuLoeschendesCTabItem);

			// neues
			ArrayList<Map<String,CTabItem>> newPmodel_id_item = new ArrayList<Map<String,CTabItem>>();

			// jeden eintrag der liste durchgehen und sehen ob das schon der gesuchte Map mit der id -> CTabItem ist
			for(Map<String,CTabItem> actIdCTabItem : processId_CTabItem)
			{
				for(CTabItem oneExistentCTabItem : actIdCTabItem.values())
				{
					if(!oneExistentCTabItem.equals(zuLoeschendesCTabItem))
					{
						newPmodel_id_item.add(actIdCTabItem);
					}
				}
			}

			processId_CTabItem = newPmodel_id_item;
			
		}
	};
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		/*----------------------------
		  get options from ini-file
		----------------------------*/
		File inifile = new java.io.File(WhereAmI.getInstallDirectoryAbsolutePath(PkraftPartUi1.class) + "/" + "../etc/pkraft-gui.ini");

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

		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
				
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
			formatter.printHelp("pkraft-gui", options);
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
		  die lizenz ueberpruefen und ggf abbrechen
		----------------------------*/

		// check for valid license
		ArrayList<String> allPortAtHost = new ArrayList<String>();
		allPortAtHost.add(ini.get("license-server", "license-server-1"));
		allPortAtHost.add(ini.get("license-server", "license-server-2"));
		allPortAtHost.add(ini.get("license-server", "license-server-3"));
		
		// muss final sein - wird sonst beim installieren mit maven angemeckert (nicht so aus eclipse heraus)
		final MyLicense lic = new MyLicense(allPortAtHost, "1", "user-edition", "0.1");
		
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
					// SPLASHSCREEN
					
//				    final Image image = new Image(display, 300, 300);
				    Image image = null;

					// set an image. die 2 versionen sind dazu da um von eclipse aus und in der installierten version zu funktionieren
					if(this.getClass().getResourceAsStream("/logo_beschnitten_transparent_small.png") != null)
					{
						image = new Image(display, this.getClass().getResourceAsStream("/logo_beschnitten_transparent_small.png"));
					}
					else if((new java.io.File("logo_beschnitten_transparent_small.png")).exists())
					{
						image = new Image(display, "logo_beschnitten_transparent_small.png");
					}

				    final Shell splash = new Shell(SWT.ON_TOP);
				    splash.setLayout(new GridLayout(1, false));
				    splash.setSize(300, 300);
				    splash.setBackground(new Color(display, 255, 255, 255)); // Weiss
				    
				    Label labelImage = new Label(splash, SWT.NONE);
				    labelImage.setImage(image);
//				    labelImage.setLayout(new GridLayout(1, false));
					GridData gd_labelImage = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_labelImage.widthHint = 300;
					gd_labelImage.minimumWidth = 300;
//					gd_labelImage.minimumHeight = 10;
					labelImage.setLayoutData(gd_labelImage);

				    Label labelZeile1 = new Label(splash, SWT.NONE | SWT.BORDER | SWT.CENTER);
				    String text = "version [% version %]";
				    text += "\nlicense status: " + lic.getLicense().getValidationStatus();
				    
					switch(lic.getLicense().getValidationStatus())
					{
						case LICENSE_VALID:
							
							text += "\nlicensee: "+lic.getLicense().getLicenseText().getUserEMail();
							text += "\nexpires in: "+lic.getLicense().getLicenseText().getLicenseExpireDaysRemaining(null)+" day(s).";
							break;
						case LICENSE_INVALID:
							break;
						default:
							text += "\nno valid license found";
					}
				    text += "\nsupport: support@prozesskraft.de";

					Button buttonOk = new Button(splash, SWT.NONE);
					GridData gd_buttonOk = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
					gd_buttonOk.widthHint = 62;
					buttonOk.setLayoutData(gd_buttonOk);
					buttonOk.setText("Ok");
					buttonOk.addSelectionListener(new SelectionAdapter()
					{
						public void widgetSelected(SelectionEvent event)
						{
							splash.close();
						}
					});
					
				    labelZeile1.setText(text);
//				    labelImage.setLayout(new GridLayout(1, false));
					GridData gd_labelZeile1 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
					gd_labelZeile1.horizontalAlignment = SWT.CENTER;
					gd_labelZeile1.widthHint = 300;
					gd_labelZeile1.minimumWidth = 300;
//					gd_labelImage.minimumHeight = 10;
					labelZeile1.setLayoutData(gd_labelZeile1);
				    
				    splash.pack();
				    Rectangle splashRect = splash.getBounds();
				    Rectangle displayRect = display.getBounds();
				    int x = (displayRect.width - splashRect.width) / 2;
				    int y = (displayRect.height - splashRect.height) / 2;
				    splash.setLocation(x, y);
				    splash.open();
					
					// DAS HAUPTFENSTER
				    
					Shell shell = new Shell(display);
//					shell.setSize(1200, 800);
					shell.setMaximized(true);
					shell.setText("pkraft "+"v[% version %]");

					// set an icon. die 2 versionen sind dazu da um von eclipse aus und in der installierten version zu funktionieren
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
					new PkraftPartUi1(composite);

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
