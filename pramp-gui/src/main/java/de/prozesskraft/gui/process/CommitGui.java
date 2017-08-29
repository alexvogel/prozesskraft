package de.prozesskraft.gui.process;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.prozesskraft.pkraft.File;
import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;

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
//		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, true, 2, 1);
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
		GridData gd_group = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		
		
		// Layout ist ab hier --> FormLayout!
		group.setLayoutData(gd_group);
		group.setLayout(new GridLayout(1, false));

		for(Variable actVariable : commit.getVariable())
		{
			variableGui.add(new VariableGui(this, group, actVariable));
		}

		for(File actFile : commit.getFile())
		{
			fileGui.add(new FileGui(this, group, actFile));
		}
	}
	
	/**
	 * einsammeln der maps aus variables und files und zusammenfuehren in einem map
	 * @return
	 */
	public Multimap<String,String> getContent()
	{
		Multimap<String,String> content = HashMultimap.create();
		
		// getContent aller variables
		for(VariableGui actualVariableGui : variableGui)
		{
			content.putAll(actualVariableGui.getContent());
		}
		// getContent aller files
		for(FileGui actualFileGui : fileGui)
		{
			content.putAll(actualFileGui.getContent());
		}
		return content;
	}

	public void commit(Step step)
	{
		// commit aller variables
		for(VariableGui actualVariableGui : variableGui)
		{
			step.log("debug", "Commit: its a variable");
			actualVariableGui.commit(this.commit);
		}
		// commit aller files
		for(FileGui actualFileGui : fileGui)
		{
			step.log("debug", "Commit: its a file");
			actualFileGui.commit(this.commit);
		}
		
	}

	public boolean doAllTestsPass()
	{
		// check ob alle Tests der Variablen gut (true) gelaufen sind
		for(VariableGui actualVariableGui : variableGui)
		{
			if ( ! (actualVariableGui.doAllTestsPass()) )
			{
				return false;
			}
		}
		// check ob alle Tests der Files gut (true) gelaufen sind
		for(FileGui actualFileGui : fileGui)
		{
			if ( ! (actualFileGui.doAllTestsPass()) )
			{
				return false;
			}
		}
		
		// kein Tests 'false' gelaufen? -> dann treu zurueckmelden
		return true;
	}
}
