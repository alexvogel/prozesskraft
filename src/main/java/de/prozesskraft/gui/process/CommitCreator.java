package de.prozesskraft.gui.process;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.File;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;
import de.prozesskraft.pramp.parts.*;

public class CommitCreator
{
	int abstandZeilen = 30;
	Font font_5;
	
	Step step;

	int maxBreiteDerSchluessel = 5;
	
	String filterPath = "";
	
	Composite parent;
	PrampPartUi1 parent_prampgui;
	Composite composite;
	ScrolledComposite sc;
	
	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
	
	public CommitCreator(PrampPartUi1 parentPrampGui, Composite parent, Step step)
	{
		this.parent_prampgui = parentPrampGui;
		this.parent = parent;
		this.step = step;

		sc = new ScrolledComposite(this.parent, SWT.V_SCROLL | SWT.H_SCROLL);
//		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
//		sc.setAlwaysShowScrollBars(true);

		composite = new Composite(sc, SWT.NONE);
//		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
//		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		sc.setContent(composite);
//		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
//		// ermitteln wie breit die erste spalte sein muss um alle schluessel vollstaendig darstellen zu koennen
//		for(Commit actCommit : step.getCommit())
//		{
//			// alle Variablen im Commit durchgehen und die maximale Size ermitteln und festhalten
//			for(Variable actVariable : actCommit.getVariable())
//			{
////				System.out.println("variable.length "+actVariable.getKey().length());
//				if(actVariable.getKey().length() > maxBreiteDerSchluessel) {maxBreiteDerSchluessel = actVariable.getKey().length();}
//			}
//			// alle Files im Commit durchgehen und die maximale Size ermitteln und festhalten
//			for(File actFile : actCommit.getFile())
//			{
////				System.out.println("variable.length "+actFile.getKey().length());
//				if(actFile.getKey().length() > maxBreiteDerSchluessel) {maxBreiteDerSchluessel = actFile.getKey().length();}
//			}
//		}
//		maxBreiteDerSchluessel = maxBreiteDerSchluessel * 10;
		maxBreiteDerSchluessel = 150;
//		System.out.println("max Breite = "+maxBreiteDerSchluessel);
		
		
		composite.layout();
		
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Composite createControls()
	{
		Composite actualComposite = new Composite(composite, SWT.NONE);
		actualComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_actualComposite = new GridLayout(1, false);
		actualComposite.setLayout(gl_actualComposite);

		for (Commit actualCommit : step.getCommit())
		{
			commitGui.add(new CommitGui(this, actualComposite, actualCommit));
		}
		return parent;
	}
	
	public Multimap<String,String> getContent()
	{
		Multimap<String,String> content = HashMultimap.create();
		for (CommitGui actualCommitGui : commitGui)
		{
			content.putAll(actualCommitGui.getContent());
		}
		return content;
	}
	
	public void commitAll()
	{
		// alles bestehende aus letztem start loeschen
		step.getVariable().clear();
		step.getFile().clear();
		step.log("debug", "CommitCreator: work on all commits");

		for (CommitGui actualCommitGui : commitGui)
		{
			actualCommitGui.commit(step);
		}
		
		// die commits durchfuehren
		step.log("debug", "performing commits");
		step.commit();

		// alle files/variablen aller commits des aktuellen steps sollen auf 'finished' gesetzt werden

		for(Commit actCommit : step.getCommit())
		{
			for(File actFile : actCommit.getFile())
			{
				actFile.setStatus("finished");
			}
			for(Variable actVariable : actCommit.getVariable())
			{
				actVariable.setStatus("finished");
			}
		}
	}
	
	public boolean doAllTestsPass()
	{
		for (CommitGui actualCommitGui : commitGui)
		{
			if (! (actualCommitGui.doAllTestsPass()) )
			{
				return false;
			}
		}
		
		return true;
	}
	
	public Step getStep()
	{
		return this.step;
	}

	/**
	 * @return the filterPath
	 */
	public String getFilterPath() {
		return filterPath;
	}

	/**
	 * @param filterPath the filterPath to set
	 */
	public void setFilterPath(String filterPath) {
		this.filterPath = filterPath;
	}
}
