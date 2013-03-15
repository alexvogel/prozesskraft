package de.caegroup.pradar.parts;

import java.awt.BorderLayout;
import java.awt.Frame;
//import java.beans.PropertyChangeSupport;
//import java.beans.PropertyChangeListener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
//import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
//import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.layout.FormLayout;
//import org.eclipse.swt.layout.FormData;
//import org.eclipse.swt.layout.FormAttachment;

import de.caegroup.pradar.*;

//import processing.core.PApplet;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
//import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.jface.databinding.swt.SWTObservables;
//import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
//import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.IStatus;

public class PradarPartUi extends ModelObject
{
	private DataBindingContext bindingContext;
	private Text text_process;
	private Text text_user;
	private Text text_host;
	private Text text_active;
	private Table table;
	private PradarViewModel filter = new PradarViewModel();
	private Entity filter_entity = new Entity();
	PradarViewProcessing applet;
	Display display;
	
	public PradarPartUi()
	{
	}

	@Inject
	public PradarPartUi(Composite composite)
	{
//		this.parent = composite;
		createControls(composite);
	}

	/**
	 * Create contents of the view part.
	 * @wbp.parser.entryPoint
	 */
	@PostConstruct
	public void createControls(Composite parent)
//	public void createControls()
	{
		parent.setSize(10, 10);
		parent.setEnabled(true);
		GridLayout gl_parent = new GridLayout(4, false);
		gl_parent.horizontalSpacing = 10;
		parent.setLayout(gl_parent);
		
		Composite composite = parent;
//		Composite composite = new Composite(parent, SWT.NONE);

		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		composite.setLayoutData(gd_composite);
		composite.setLayout(new GridLayout(1, false));
		
		Composite composite_1 = new Composite(composite, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(2, false);
		gl_composite_1.marginWidth = 0;
		gl_composite_1.marginHeight = 0;
		composite_1.setLayout(gl_composite_1);
		GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_1.heightHint = 516;
		gd_composite_1.widthHint = 122;
		composite_1.setLayoutData(gd_composite_1);
		
		Composite composite_11 = new Composite(composite_1, SWT.NONE);
		composite_11.setLayout(new GridLayout(1, false));
		GridData gd_composite_11 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_11.widthHint = 169;
		composite_11.setLayoutData(gd_composite_11);
		
		Group grpFilter = new Group(composite_11, SWT.NONE);
		grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		grpFilter.setText("filter");
		grpFilter.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel = new Label(grpFilter, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblNewLabel.setText("process");
		
		text_process = new Text(grpFilter, SWT.BORDER);
		text_process.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel_1 = new Label(grpFilter, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("user");
		
		text_user = new Text(grpFilter, SWT.BORDER);
		text_user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblHost = new Label(grpFilter, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblHost.setText("host");
		
		text_host = new Text(grpFilter, SWT.BORDER);
		text_host.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblActive = new Label(grpFilter, SWT.NONE);
		lblActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblActive.setText("active");
		
		text_active = new Text(grpFilter, SWT.BORDER);
		text_active.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group grpVisual = new Group(composite_11, SWT.NONE);
		grpVisual.setText("visual");
		grpVisual.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		grpVisual.setLayout(new GridLayout(1, false));
		
		Label lblNewLabel_2 = new Label(grpVisual, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel_2.setText("zoom");
		
		Scale scale = new Scale(grpVisual, SWT.NONE);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		scale.setMaximum(3);
		scale.setMinimum(1);
		scale.setSelection(1);
		
		Group grpFunction = new Group(composite_11, SWT.NONE);
		grpFunction.setLayout(new GridLayout(1, false));
		grpFunction.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		grpFunction.setText("function");
		
		Button btnNewButton = new Button(grpFunction, SWT.NONE);
		btnNewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnNewButton.setText("refresh");
		
		Composite composite_12 = new Composite(composite_1, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		composite_12.setLayout(new GridLayout(1, false));
		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite_12.heightHint = 390;
		gd_composite_12.minimumWidth = 10;
		gd_composite_12.minimumHeight = 10;
		composite_12.setLayoutData(gd_composite_12);
		
		Composite composite_2 = new Composite(composite, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1);
		gd_composite_2.heightHint = 225;
		composite_2.setLayoutData(gd_composite_2);
		
		CheckboxTableViewer checkboxTableViewer = CheckboxTableViewer.newCheckList(composite_2, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		checkboxTableViewer.setContentProvider(new ContentProvider());
				
		bindingContext = initDataBindings();

		Frame frame = SWT_AWT.new_Frame(composite_12);

		applet = new PradarViewProcessing(filter_entity);
		frame.add(applet, BorderLayout.CENTER);
		applet.init();
		frame.pack();
		frame.setLocation(0, 0);
		frame.setVisible(true);

		updateUserInterface(filter);
		paint();
	}

	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return new Object[0];
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	
	public void paint()
	{
//		System.out.println("Es wird neu aufgebaut!");

//		Db db = new Db("/soft/deploy/data/pradar/pradar.db");

		filter_entity.setProcess(filter.getProcess());
		filter_entity.setUser(filter.getUser());
		filter_entity.setHost(filter.getHost());
		filter_entity.setActive(filter.getActive());
	
//		PradarViewProcessing applet = new PradarViewProcessing(db, filter_entity);

//		System.out.println("Active ist im Filter auf: "+filter.getActive());
		applet.setFilter(filter_entity);
	}
	
	
	@PreDestroy
	public void dispose()
	{
	}

	@Focus
	public void setFocus()
	{
		table.setFocus();
	}
	
	IChangeListener listener = new IChangeListener()
	{
		public void handleChange(ChangeEvent event)
		{
//			System.out.println("Active ist im Filter (abgefragt aus dem listener heraus): "+filter.getActive());
			paint();
		}
	};
	
	// define a change listener
	private void updateUserInterface(PradarViewModel filter)
	{
//		bindingContext.dispose();
		IObservableList bindings = bindingContext.getValidationStatusProviders();

		// Register the Listener to all bindings
		for (Object o : bindings)
		{
			Binding b = (Binding) o;
			b.getModel().addChangeListener(listener);
		}
	}


	/**
	 * @wbp.parser.entryPoint
	 */
	protected DataBindingContext initDataBindings()
	{

		// Einrichten der ControlDecoration über dem Textfeld 'active'
		final ControlDecoration controlDecorationActive = new ControlDecoration(text_active, SWT.LEFT | SWT.TOP);
		controlDecorationActive.setDescriptionText("use 'true', 'false', 'all' or leave field blank");
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecorationActive.setImage(fieldDecoration.getImage());

		// Validator for 'active' mit Verbindung zur Controldecoration
		IValidator validatorActive = new IValidator()
		{
			public IStatus validate(Object value)
			{
				if (value instanceof String)
				{
					if (((String) value).matches("true|false|all|"))
					{
						controlDecorationActive.hide();
						return ValidationStatus.ok();
						
					}
				}
				controlDecorationActive.show();
				return ValidationStatus.error("not a boolean or 'all'");
			}
		};

		// UpdateStrategy fuer 'active' ist: update der werte nur wenn validierung erfolgreich
		UpdateValueStrategy strategyActive = new UpdateValueStrategy();
		strategyActive.setBeforeSetValidator(validatorActive);
		//---------
		
		DataBindingContext bindingContext = new DataBindingContext();
		//
		IObservableValue targetObservableProcess = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_process);
		IObservableValue modelObservableProcess = BeanProperties.value("process").observe(filter);
		bindingContext.bindValue(targetObservableProcess, modelObservableProcess, null, null);
		//
		IObservableValue targetObservableUser = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_user);
		IObservableValue modelObservableUser = BeanProperties.value("user").observe(filter);
		bindingContext.bindValue(targetObservableUser, modelObservableUser, null, null);
		//
		IObservableValue targetObservableHost = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_host);
		IObservableValue modelObservableHost = BeanProperties.value("host").observe(filter);
		bindingContext.bindValue(targetObservableHost, modelObservableHost, null, null);
		//
		IObservableValue targetObservableActive = WidgetProperties.text(SWT.Modify).observeDelayed(800, text_active);
		IObservableValue modelObservableActive = BeanProperties.value("active").observe(filter);
		bindingContext.bindValue(targetObservableActive, modelObservableActive, strategyActive, null);
		//
		return bindingContext;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final Display display = new Display();
		
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run()
			{
				try
				{
					Shell shell = new Shell(display);
					shell.setLayout(new FillLayout());
					Composite composite = new Composite(shell, SWT.NO_FOCUS);
					GridLayout gl_composite = new GridLayout(2, false);
					gl_composite.marginWidth = 0;
					gl_composite.marginHeight = 0;
					new PradarPartUi(composite);
					
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
					
				} finally
				{
					display.dispose();
				}
			}
		});
		System.exit(0);
	}
//
//	
}
