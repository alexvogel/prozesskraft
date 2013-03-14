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
//import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
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
//import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.IStatus;

public class PradarView extends ModelObject
{
	private DataBindingContext bindingContext;
	private Text text_process;
	private Text text_user;
	private Text text_host;
	private Text text_active;
	private PradarViewModel filter = new PradarViewModel();
	private Composite compositepaint;
//	private Composite parent;
	private Entity filter_entity = new Entity();
	private Db db = new Db("/soft/deploy/data/pradar/pradar.db");
	PradarViewProcessing applet = new PradarViewProcessing(db, filter_entity);
	Display display;
	
	public PradarView()
	{
	}

	@Inject
	public PradarView(Composite composite)
	{
//		this.parent = composite;
		createControls(composite);
	}

	/**
	 * Create contents of the view part.
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
		
		Label lblProcess = new Label(parent, SWT.NONE);
		lblProcess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblProcess.setAlignment(SWT.CENTER);
		lblProcess.setText("process");
		
		Label lblUser = new Label(parent, SWT.NONE);
		lblUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblUser.setAlignment(SWT.CENTER);
		lblUser.setText("user");
		
		Label lblHost = new Label(parent, SWT.NONE);
		lblHost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblHost.setAlignment(SWT.CENTER);
		lblHost.setText("host");
		
		Label lblActive = new Label(parent, SWT.NONE);
		lblActive.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblActive.setAlignment(SWT.CENTER);
		lblActive.setText("active");
		
		text_process = new Text(parent, SWT.BORDER);
		text_process.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		text_user = new Text(parent, SWT.BORDER);
		text_user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		text_host = new Text(parent, SWT.BORDER);
		text_host.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		text_active = new Text(parent, SWT.BORDER);
		text_active.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
		bindingContext = initDataBindings();

		compositepaint = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		gd_composite.minimumWidth = 10;
		gd_composite.minimumHeight = 10;
		compositepaint.setLayoutData(gd_composite);

		Frame frame = SWT_AWT.new_Frame(compositepaint);

		frame.add(applet, BorderLayout.CENTER);
		applet.init();
		frame.pack();
		frame.setLocation(0, 0);
		frame.setVisible(true);

		updateUserInterface(filter);
		paint();
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
		compositepaint.setFocus();
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
			public void run() {
				Shell shell = new Shell(display);
				shell.setLayout(new FillLayout());
				Composite composite = new Composite(shell, SWT.NO_FOCUS);
				new PradarView(composite);
				shell.open();
				
				
				while (!shell.isDisposed())
				{
					if( ! display.readAndDispatch())
					{
						display.sleep();
					}
				}
				
			}
		});
		
		// TODO Auto-generated method stub

	}

	
}
