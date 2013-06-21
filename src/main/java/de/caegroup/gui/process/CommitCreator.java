package de.caegroup.gui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

import de.caegroup.pramp.parts.*;

public class CommitCreator
{
	int abstandZeilen = 30;
	Font font_5;
	
	Step step;
	
	Composite parent;
	Composite composite;
	ScrolledComposite sc;
	
	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
	
	public CommitCreator(Composite parent, Step step)
	{
		this.parent = parent;
		this.step = step;

		sc = new ScrolledComposite(this.parent, SWT.V_SCROLL | SWT.H_SCROLL);
//		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
//		sc.setAlwaysShowScrollBars(true);
		
		composite = new Composite(sc, SWT.NONE);
//		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
//		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		sc.setContent(composite);
//		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		composite.layout();
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Composite createControls()
	{
		Composite actualComposite = new Composite(composite, SWT.NONE);
		actualComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_actualComposite = new GridLayout(1, false);
		actualComposite.setLayout(gl_actualComposite);

		for (Commit actualCommit : step.getCommit())
		{
			commitGui.add(new CommitGui(this, actualComposite, actualCommit));
		}
		return parent;
	}
	
	public void commitAll()
	{
		for (CommitGui actualCommitGui : commitGui)
		{
			actualCommitGui.commit(step);
		}
	}
}
