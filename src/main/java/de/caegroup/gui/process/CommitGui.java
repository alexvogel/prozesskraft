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

import de.caegroup.process.File;
import de.caegroup.process.Commit;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class CommitGui
{
	CommitCreator parent_commitcreator; 
	public Composite parent;
	Commit commit;
	
	Composite composite;

	ArrayList<VariableGui> variableGui = new ArrayList<VariableGui>();
	ArrayList<FileGui> fileGui = new ArrayList<FileGui>();
	
	public CommitGui(CommitCreator parent_commitcreator, Composite parent, Commit commit)
	{
		this.parent_commitcreator = parent_commitcreator;
		this.parent = parent;
		this.commit = commit;

		composite = new Composite(this.parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		createControls(composite);
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void createControls(Composite composite)
	{
		// fuer jedes commit soll eine Gruppe erstellt werden
		Group group = new Group(composite, SWT.NONE);
		group.setText(commit.getName());
		GridData gd_group = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);

		// Layout ist ab hier --> FormLayout!
		group.setLayoutData(gd_group);
		group.setLayout(new GridLayout(1, false));
		
		Iterator<Variable> iterVariable = commit.getVariable().iterator();
		while(iterVariable.hasNext())
		{
			Variable actualVariable = iterVariable.next();
			variableGui.add(new VariableGui(this, group, actualVariable));
		}
		
		Iterator<File> iterFile = commit.getFile().iterator();
		while(iterFile.hasNext())
		{
			File actualFile = iterFile.next();
			fileGui.add(new FileGui(this, group, actualFile));
		}
	}
}
