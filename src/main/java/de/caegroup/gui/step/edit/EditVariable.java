package de.caegroup.gui.step.edit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.caegroup.process.*;

public class EditVariable
{
	private Variable variable = null;

	public EditVariable(Variable variable)
	{
		this.variable = variable;

		Display display = Display.getCurrent();
		try
		{
			Shell shell = new Shell(display);
			shell.setText("edit/delete variable");
			shell.setLayout(new FillLayout());
			Composite composite = new Composite(shell, SWT.NO_FOCUS);
			GridLayout gl_composite = new GridLayout(2, false);
			gl_composite.marginWidth = 0;
			gl_composite.marginHeight = 0;
			createControls(composite);
			
			try
			{
				shell.open();

				while (!shell.isDisposed())
				{
					if( ! display.readAndDispatch())
					{
						display.sleep();
					}
				}

			}
			finally
			{
				if (!shell.isDisposed())
				{
					shell.dispose();
				}
			}
			
		}
		// wenn display disposed wird, wird auch das aufrufende fenster gekillt
		finally
		{
//			display.dispose();
		}

	}
	
	/**
	 * Create contents of the view part.
	 */
	public void createControls(Composite composite)
	{

		composite.setSize(150, 150);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));

		Label variableKey = new Label(composite, SWT.NONE);
		variableKey.setText("key");
		variableKey.setToolTipText("name of variable");

		FormData fd_variableKey = new FormData();
		fd_variableKey.top = new FormAttachment(0, 4);
		fd_variableKey.right = new FormAttachment(0,100);
		variableKey.setLayoutData(fd_variableKey);

		Text text = new Text(composite, SWT.BORDER);
		text.setToolTipText("name of variable");
		text.setMessage("name of variable");

		FormData fd_text_file = new FormData();
		fd_text_file.top = new FormAttachment(0, 0);
		fd_text_file.left = new FormAttachment(0, 120);
		fd_text_file.width = 190;
		text.setLayoutData(fd_text_file);
	}

}
