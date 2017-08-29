package de.prozesskraft.gui.process;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.prozesskraft.pkraft.Commit;
import de.prozesskraft.pkraft.Step;
import de.prozesskraft.pkraft.Variable;

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
		
		addButtonIfNecessary();
		
//		// wenn schon die mindestanzahl erreicht ist aber die maximalzahl noch nicht, soll NUR ein button erstellt werden
//		if ( (this.variableoccurGui.size() >= variable.getMinoccur()) && (this.variableoccurGui.size() < variable.getMaxoccur()) )
//		{
//			addOnlyButton();
//		}
//		setBackground();
		parent_commitgui.parent.layout();
		parent_commitgui.parent_commitcreator.sc.setMinSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		parent_commitgui.parent_commitcreator.composite.layout();
	}
	
	public void add()
	{
		if (this.variableoccurGui.size() < variable.getMaxoccur())
		{
			VariableOccurGui variableoccur = new VariableOccurGui(this, composite, variable, variable.getKey(), variable.getFree(), false, true);
			variableoccurGui.add(variableoccur);
//			setBackground();
			parent_commitgui.parent.layout();
			parent_commitgui.parent_commitcreator.sc.setMinSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//			System.out.println("minHeight: "+parent_commitgui.parent_commitcreator.sc.getMinHeight());
//			parent_commitgui.parent_commitcreator.composite.setSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			parent_commitgui.parent_commitcreator.composite.layout();
		}
	}

	public void addButtonIfNecessary()
	{
		// ist die mindestanzahl der eintraege erreicht UND maximalanzahl der eintraege noch nicht erreicht, und existiert noch kein einzelnes button, soll ein einzelnes button erzeugt werden
		if ( (this.variableoccurGui.size() >= variable.getMinoccur()) && (this.variableoccurGui.size() < variable.getMaxoccur()) && (!(isVariableOccurWithOnlyAButtonPresent())))
		{
			VariableOccurGui variableoccur = new VariableOccurGui(this, composite, variable, variable.getKey(), variable.getFree(), false, true);
			variableoccurGui.add(variableoccur);
		}
	}
	
	public boolean isVariableOccurWithOnlyAButtonPresent()
	{
		boolean isPresent = false;
		for (VariableOccurGui v : this.variableoccurGui)
		{
			if ( (!(v.comboexist)) & v.buttonexist )
			{
				isPresent = true;
			}
		}
		return isPresent;
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
		
		addButtonIfNecessary();
		
//		else if ( ( ( variable.getMaxoccur() - this.variableoccurGui.size()) == 1 ) && this.variableoccurGui.get(this.variableoccurGui.size()-1).comboexist )
//		{
//			addOnlyButton();
//		}
//		System.out.println("comboexist in last variableOccur: "+this.variableoccurGui.get(this.variableoccurGui.size()-1).comboexist);

//		setBackground();
		parent_commitgui.parent.layout();
		parent_commitgui.parent_commitcreator.sc.setMinSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		System.out.println("minHeight: "+parent_commitgui.parent_commitcreator.sc.getMinHeight());

		//		parent_commitgui.parent_commitcreator.composite.setSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		parent_commitgui.parent_commitcreator.composite.layout();
	}
	
	/**
	 * get the content of the combo / textfield
	 */
	public Multimap<String,String> getContent ()
	{
		Multimap<String,String> content = HashMultimap.create();
		for(VariableOccurGui actualVariableoccurGui : variableoccurGui)
		{
			// die map aus VariableOccur holen
			Map<String,String> mapActualVariableoccurGui = actualVariableoccurGui.getContent();
			// iterieren ueber die liste und schon vorhandene schluessel mit -1 hochzaehlern
			for(String key : mapActualVariableoccurGui.keySet())
			{
				// die schluessel-werte paare ablegen
				content.put(key, mapActualVariableoccurGui.get(key));
			}

		}
		return content;
	}
	
	/**
	 * commit the content of the combo / textfield to ste step of process
	 */
	public void commit (Commit commit)
	{
		commit.log("debug", "Commit all occurances "+ variableoccurGui.size() +" of variable.");
		for(VariableOccurGui actualVariableoccurGui : variableoccurGui)
		{
			actualVariableoccurGui.commit(commit);
		}

		// entfernen der urspruenglichen variable (die aus dem process.xml)
		// sonst wird diese auch committed -> das fuehrt zu doppelten Eintraegen und Fehlern im Prozessablauf
//		if(!variableoccurGui.isEmpty())
//		{
			commit.removeVariable(variable);
//		}
	}

	/**
	 * checks the status of all Tests associated with this commitRoot (all Variables in this view)
	 */
	public boolean doAllTestsPass ()
	{
		for(VariableOccurGui actualVariableoccurGui : variableoccurGui)
		{
			if ( ! ( actualVariableoccurGui.doAllTestsPass() ) )
			{
				return false;
			}
		}
		return true;
	}
}
