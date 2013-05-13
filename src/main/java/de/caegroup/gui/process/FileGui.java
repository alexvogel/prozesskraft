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
import de.caegroup.process.File;
import de.caegroup.process.Step;
import de.caegroup.process.Variable;
import de.caegroup.process.Process;

public class FileGui
{
	File file;
	
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
		
		// wenn schon die mindestanzahl erreicht ist aber die maximalzahl noch nicht, soll NUR ein button erstellt werden
		if ( (this.fileoccurGui.size() >= file.getMinoccur()) && (this.fileoccurGui.size() < file.getMaxoccur()) )
		{
			FileOccurGui fileoccur = new FileOccurGui(this, composite, file, file.getKey(), false, true);
			fileoccurGui.add(fileoccur);
		}

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
//		System.out.println("Anzahl vor remove: "+this.variableoccurGui.size());
		
		this.fileoccurGui.remove(fileoccurgui);
		
//		System.out.println("Anzahl nach remove: "+this.variableoccurGui.size());
		
		if (this.fileoccurGui.size() == 0)
		{
			addFirst();
//			System.out.println("Nochmal Ersterstellung durchlaufen und jetzt eine laenge von: "+this.variableoccurGui.size());
		}
//		setBackground();
		parent_commitgui.parent.layout();
		parent_commitgui.parent_commitcreator.sc.setMinSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
//		System.out.println("minHeight: "+parent_commitgui.parent_commitcreator.sc.getMinHeight());

		//		parent_commitgui.parent_commitcreator.composite.setSize(parent_commitgui.parent_commitcreator.composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		parent_commitgui.parent_commitcreator.composite.layout();
	}
}
