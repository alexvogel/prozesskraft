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

public class CopyOfCommitCreator
{

	int abstandZeilen = 30;
	Font font_5;
	Composite parent;
	Process process;
	String stepname;
	
	ArrayList<ArrayList<Object>>  zeilen  = new ArrayList<ArrayList<Object>>();
	
	ArrayList<CommitGui> commitGui = new ArrayList<CommitGui>();
	
//	ArrayList<ArrayList<Label>>  col1  = new ArrayList<ArrayList<Label>>();
//	Map<Object, Button> col2  = new HashMap<Object, Button>();
//	Map<Object, Combo>  col3  = new HashMap<Object, Combo>();
//	Map<Object, Button> col4  = new HashMap<Object, Button>();
	
	public CopyOfCommitCreator(Composite parent, Process process, String stepname)
	{
		this.parent = parent;
		this.process = process;
		this.stepname = stepname;
	}
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public Composite createControls()
	{
		Composite composite = new Composite(parent, SWT.V_SCROLL);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		FontData[] fD = new Label(parent, 0).getFont().getFontData();
		fD[0].setHeight(5);
		font_5 = new Font(parent.getDisplay(), fD[0]);
		
		Iterator<Commit> iterCommit = process.getStep(stepname).getCommit().iterator();
		while (iterCommit.hasNext())
		{
			// fuer jedes commit soll eine Gruppe erstellt werden
			Commit actualCommit = iterCommit.next();
			Group group = new Group(composite, SWT.NONE);
			group.setText(actualCommit.getName());
			GridData gd_group = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);

			// Layout ist ab hier --> FormLayout!
			group.setLayoutData(gd_group);
			group.setLayout(new FormLayout());

			System.out.println("name: "+actualCommit.getName());
			
			int anzahlZeilen = 0;
			Label lastLabel = null;
			
			
			// fuer jede variable soll mindest eine Zeile mit mindestens 1 Label erstellt werden
			Iterator<Variable> iterVariable = actualCommit.getVariable().iterator();
			while(iterVariable.hasNext())
			{
				Variable actualVariable = iterVariable.next();
								
				for(int x = 0; x <= actualVariable.getMinoccur(); x++)
				{
					// hinzufuegen einer neuen zeile
					ArrayList<Object> zeile = new ArrayList<Object>();
					zeilen.add(zeile);

					// erstellen eines Label fuer den 'key' der Variable
					Label variableKey = new Label(group, SWT.NONE);
					variableKey.setText(actualVariable.getKey());

					FormData fd_variableKey = new FormData();
					fd_variableKey.top = new FormAttachment(2, anzahlZeilen * abstandZeilen);
					fd_variableKey.right = new FormAttachment(0,100);
					variableKey.setLayoutData(fd_variableKey);
					
					// erste spalte = namen der variable
					zeile.add(0, variableKey);
					
					// falls maxoccur - minoccur > 0 ist, soll ein +-Button neben dem Label erscheinen
					if ((actualVariable.getMaxoccur()-actualVariable.getMinoccur()) > 0)
					{
						
						Button button_plus = new Button(group, SWT.NONE);
						
						// dritte spalte = plus-button
						zeile.add(2, button_plus);
						button_plus.setText("+");
//						button_plus.setFont(font_5);
						
						FormData fd_button_plus = new FormData();
						fd_button_plus.top = new FormAttachment(0, anzahlZeilen * abstandZeilen);
						fd_button_plus.left = new FormAttachment(lastLabel, 5);
						button_plus.setLayoutData(fd_button_plus);
						
						button_plus.addSelectionListener(new SelectionAdapter()
						{
							public void widgetSelected(SelectionEvent e)
							{
								
							}
						});
					}
					
					
					
					
				}
				
				
				// entsprechend der anzahl minoccur eintragefelder erstellen
				boolean positioned = false;
				for(int x = 0; x < actualVariable.getMinoccur(); x++)
				{
					Combo combo_variable;
					if (!(actualVariable.getFree()))
					{
						combo_variable = new Combo(group, SWT.NONE | SWT.READ_ONLY);
					}
					else
					{
						combo_variable = new Combo(group, SWT.NONE);
					}
	
					positioned = true;
					
					combo_variable.setItems((String[]) actualVariable.getChoice().toArray(new String[actualVariable.getChoice().size()]));
					combo_variable.select(0);
					
					FormData fd_combo_variable = new FormData();
					fd_combo_variable.top = new FormAttachment(0, anzahlZeilen * abstandZeilen);
					fd_combo_variable.left = new FormAttachment(0, 130);
					fd_combo_variable.width = 200;

					combo_variable.setLayoutData(fd_combo_variable);
				}
				
				anzahlZeilen++;
				lastLabel = variableKey;
				
			}
			
		}

		
		return parent;
	}
}
