package de.caegroup.gui.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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

public class VariableGui
{
	Variable variable;
	
	Composite parent;
	CommitGui parent_commitgui;
	
	Composite composite;

	ArrayList<VariableOccurGui> variableoccurGui = new ArrayList<VariableOccurGui>();
	
	public VariableGui(CommitGui parent_commitgui, Composite parent, Variable variable)
	{
		this.parent = parent;
		this.parent_commitgui = parent_commitgui;
		this.variable = variable;
		
		composite = new Composite(this.parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));

		addFirst();
	}

	
	/**
	 * adds the first time
	 */
	public void addFirst()
	{
		// solange welche benoetigt werden, (< minOccur) welche erstellen und keinen button anbieten
		while (variable.getMinoccur() > this.variableoccurGui.size())
		{
			VariableOccurGui variableoccur = new VariableOccurGui(this, composite, variable, variable.getKey(), variable.getFree(), true, false);
			variableoccurGui.add(variableoccur);
		}
		
//		if ((this.variableoccurGui.size() == variable.getMinoccur()) && (this.variableoccurGui.size() < variable.getMinoccur()))
//		{
//			VariableOccurGui variableoccur = new VariableOccurGui(this, composite, variable, variable.getKey(), variable.getFree(), false, true);
//			variableoccurGui.add(variableoccur);
//		}
//		
		// wenn schon die mindestanzahl erreicht ist aber die maximalzahl noch nicht, soll NUR ein button erstellt werden
		if ( (this.variableoccurGui.size() >= variable.getMinoccur()) && (this.variableoccurGui.size() < variable.getMaxoccur()) )
		{
			VariableOccurGui variableoccur = new VariableOccurGui(this, composite, variable, variable.getKey(), variable.getFree(), false, true);
			variableoccurGui.add(variableoccur);
		}
//		setBackground();
	}
	
	public void add()
	{
		if (this.variableoccurGui.size() < variable.getMaxoccur())
		{
			VariableOccurGui variableoccur = new VariableOccurGui(this, composite, variable, variable.getKey(), variable.getFree(), false, true);
			variableoccurGui.add(variableoccur);
//			setBackground();
			parent_commitgui.parent.layout();
		}
	}

//	private void setBackground()
//	{
//		if (this.variableoccurGui.size() > 1)
//		{
//			composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
//		}
//		else
//		{
//			composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
//		}
//		parent_commitgui.parent.layout();
//	}
//	
	/**
	 * removes
	 */
	public void remove(VariableOccurGui variableoccurgui)
	{
//		System.out.println("Anzahl vor remove: "+this.variableoccurGui.size());
		
		this.variableoccurGui.remove(variableoccurgui);
		
//		System.out.println("Anzahl nach remove: "+this.variableoccurGui.size());
		
		if (this.variableoccurGui.size() == 0)
		{
			addFirst();
//			System.out.println("Nochmal Ersterstellung durchlaufen und jetzt eine laenge von: "+this.variableoccurGui.size());
		}
//		setBackground();
		parent_commitgui.parent_commitcreator.parent.layout();
	}
}
