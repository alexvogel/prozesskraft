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

public class CommitCreator
{

	int abstandZeilen = 30;
	Font font_5;
	
	Step step;
	
	Composite parent;
	
	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
	
	public CommitCreator(Composite parent, Step step)
	{
		this.parent = parent;
		this.step = step;
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Composite createControls()
	{
		Iterator<Commit> iterCommit = step.getCommit().iterator();
		while(iterCommit.hasNext())
		{
			Commit actualCommit = iterCommit.next();
			commitGui.add(new CommitGui(this, parent, actualCommit));
		}
		return parent;
	}
}
