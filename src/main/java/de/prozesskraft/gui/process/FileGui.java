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
import de.prozesskraft.pkraft.File;
import de.prozesskraft.pkraft.Step;

public class FileGui
{
	File file = null;
	
	Composite parent;
	CommitGui parent_commitgui;
	
	Composite composite;

	ArrayList<FileOccurGui> fileoccurGui = new ArrayList<FileOccurGui>();
	
	public FileGui(CommitGui parent_commitgui, Composite parent, File file)
	{
		this.parent = parent;
		this.parent_commitgui = parent_commitgui;
		this.file = file;
		
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
		while (file.getMinoccur() > this.fileoccurGui.size())
		{
			FileOccurGui fileoccur = new FileOccurGui(this, composite, file, file.getKey(), true, false);
			fileoccurGui.add(fileoccur);
		}
		
		addButtonIfNecessary();

		parent_commitgui.parent.layout();
		parent_commitgui.parent_commitcreator.sc.setMinSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		parent_commitgui.parent_commitcreator.composite.layout();
		
}
	
	public void add()
	{
		if (this.fileoccurGui.size() < file.getMaxoccur())
		{
			FileOccurGui fileoccur = new FileOccurGui(this, composite, file, file.getKey(), false, true);
			fileoccurGui.add(fileoccur);

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
		if ( (this.fileoccurGui.size() >= file.getMinoccur()) && (this.fileoccurGui.size() < file.getMaxoccur()) && (!(isFileOccurWithOnlyAButtonPresent())))
		{
			FileOccurGui fileoccur = new FileOccurGui(this, composite, file, file.getKey(), false, true);
			fileoccurGui.add(fileoccur);
		}
	}
	
	public boolean isFileOccurWithOnlyAButtonPresent()
	{
		boolean isPresent = false;
		for (FileOccurGui v : this.fileoccurGui)
		{
			if ( (!(v.textexist)) & v.buttonexist )
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
	public void remove(FileOccurGui fileoccurgui)
	{
		this.parent_commitgui.parent_commitcreator.parent_prampgui.log("debug", "Anzahl vor remove: "+this.fileoccurGui.size());
		
		this.fileoccurGui.remove(fileoccurgui);
		
		this.parent_commitgui.parent_commitcreator.parent_prampgui.log("debug", "Anzahl nach remove: "+this.fileoccurGui.size());
		
		if (this.fileoccurGui.size() == 0)
		{
			addFirst();
//			System.out.println("Nochmal Ersterstellung durchlaufen und jetzt eine laenge von: "+this.variableoccurGui.size());
		}

		addButtonIfNecessary();
		
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
		for(FileOccurGui actualFileoccurGui : fileoccurGui)
		{
			// die map aus FileOccur holen
			Map<String,String> mapActualFileoccurGui = actualFileoccurGui.getContent();
			// iterieren ueber die map und schon vorhandene schluessel mit -1 hochzaehlern
			for(String key : mapActualFileoccurGui.keySet())
			{
				// die schluessel-werte paare ablegen
				content.put(key, mapActualFileoccurGui.get(key));
			}

		}
		return content;
	}
	
	/**
	 * commit the content of the combo / textfield to ste step of process
	 */
	public void commit (Commit commit)
	{
		commit.log("debug", "Commit all occurances "+ fileoccurGui.size() +" of file.");
		for(FileOccurGui actualFileoccurGui : fileoccurGui)
		{
			actualFileoccurGui.commit(commit);
		}
		
		// entfernen der urspruenglichen file (die aus dem process.xml)
		// sonst wird diese auch committed -> das fuehrt zu doppelten Eintraegen und Fehlern im Prozessablauf
//		if(!variableoccurGui.isEmpty())
//		{
			commit.removeFile(file);
//		}

	}
	
	/**
	 * checks the status of all Tests associated with this commitRoot (all Files in this view)
	 */
	public boolean doAllTestsPass ()
	{
		for(FileOccurGui actualFileoccurGui : fileoccurGui)
		{
			if ( ! ( actualFileoccurGui.doAllTestsPass() ) )
			{
				return false;
			}
		}
		return true;
	}


}
