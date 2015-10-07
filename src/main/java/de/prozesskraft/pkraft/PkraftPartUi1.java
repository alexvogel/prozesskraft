package de.prozesskraft.pkraft;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.CTabFolder;

//import de.caegroup.pradar.Init;
import de.prozesskraft.pradar.parts.PradarPartUi3;
import de.prozesskraft.pramp.parts.PrampPartUi1;
import de.prozesskraft.pmodel.PmodelPartUi1;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

public class PkraftPartUi1 implements de.prozesskraft.pradar.parts.IPkraftPartUi1, de.prozesskraft.gui.step.insight.IPkraftPartUi2
{
	static CommandLine line;
	
	Display display;

	CTabFolder tabFolder = null;

	Composite processInsight = null;
	Map<String,CTabItem> pmodel_id_item = new HashMap<String,CTabItem>();

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
		
		
	}

	/**
	 * opens a process.pmb in a new tabItem
	 * @param pathToInstance
	 */
	public void openInstance(String pathToInstance)
	{
		boolean ctabMussNeuErzeugtWerden = true;
		
		// gibt es den CTabItem bereits, soll dieser selektiert werden
		int counter = 2; // bereits 2 CTabItems vorhanden (pradar, pramp);
		for(String oldPath : pmodel_id_item.keySet())
		{
			if(oldPath.equals(pathToInstance))
			{
				System.err.println("instance already open");
				counter++;
				ctabMussNeuErzeugtWerden = false;
				tabFolder.setSelection(counter-1);
			}
		}

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
	
			PmodelPartUi1 pmodelUi = new PmodelPartUi1(compositePmodel, pathToInstance);
			pmodelUi.setPkraft(this);
			tabItemPmodel.setText(pmodelUi.getProcess().getName() + " " + pmodelUi.getProcess().getId2() + " " + pmodelUi.getProcess().getId());
			tabItemPmodel.setToolTipText(pmodelUi.getProcess().getName() + " - " + pmodelUi.getProcess().getVersion() + " - " + pathToInstance);
	
			tabItemPmodel.addDisposeListener(entferne_pmodel);
			
			// das neue tabItem dem tabFolder hinzufuegen
			tabItemPmodel.setControl(compositePmodel);
	
			// das neueste tabItem selektieren
			tabFolder.setSelection(tabFolder.getItemCount()-1);
	
			// das neue tabItem dem map der bekannten tabItems hinzufuegen
			this.pmodel_id_item.put(pathToInstance, tabItemPmodel);
		}
	}
	
	/**
	 * listener for Disposing a CtabItem
	 */
	DisposeListener entferne_pmodel = new DisposeListener()
	{
		public void widgetDisposed(DisposeEvent event)
		{
			CTabItem zuLoeschendesCTabItem = (CTabItem) event.widget;
			
			// die liste bekannter pmodel CTabItems durchgehen und den zu loeschenden eintrag entfernen
			Map<String,CTabItem> new_pmodel_id_item = new HashMap<String,CTabItem>();

			for(String actPathToInstance : pmodel_id_item.keySet())
			{
				if( ! zuLoeschendesCTabItem.equals(pmodel_id_item.get(actPathToInstance)))
				{
					new_pmodel_id_item.put(actPathToInstance, pmodel_id_item.get(actPathToInstance));
				}
			}
			
			pmodel_id_item = new_pmodel_id_item;
		}
	};
	
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
		  other things
		----------------------------*/
		// gui
		final Display display = new Display();

		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run()
			{
				try
				{
					Shell shell = new Shell(display);
//					shell.setSize(1200, 800);
					shell.setMaximized(true);
					shell.setText("pkraft "+"v[% version %]");

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
