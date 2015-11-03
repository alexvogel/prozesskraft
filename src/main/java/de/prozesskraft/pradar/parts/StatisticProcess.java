package de.prozesskraft.pradar.parts;

import java.awt.Frame;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.prozesskraft.pkraft.*;
import de.prozesskraft.pkraft.Process;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;  

public class StatisticProcess
{
	private Object father;
	public Shell shell = null;
//	public Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM & (~SWT.RESIZE));
//	public Shell shell = new Shell(Display.getCurrent());
	private Display display = Display.getCurrent();

	private Process process = null;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public StatisticProcess()
	{
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM);
		this.process = new Process();
		this.process.setName("undefined");

		shell.setText("statistic of process " + this.process.getName() + " instance " + this.process.getId());
		shell.setLocation(display.getCursorLocation());
		shell.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, true, 1, 1));
		createControls(composite);
	}

	/**
	 * Constructor fuer pkraft & Co.
	 */
	public StatisticProcess(Shell fatherShell, Object father, Process process)
	{
		this.father = father;
		this.process = process;

		shell = new Shell(fatherShell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
		try
		{
			shell.setText("statistic of process " + this.process.getName() + " instance " + this.process.getId());
			shell.setLayout(new GridLayout());
			shell.setLocation(display.getCursorLocation());
			
			Composite composite = new Composite(shell, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

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
		// breite, hoehe
		shell.setSize(1200, 900);
		
		composite.setLayout(new GridLayout(1, false));
		
//		Composite compositeGantt = new Composite(composite, SWT.NONE);
//		compositeGantt.setLayout(new GridLayout(1, false));
////		gd_composite.minimumWidth = 10;
////		gd_composite.minimumHeight = 10;
//		compositeGantt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		final IntervalCategoryDataset dataset = createDatasetGantt();  
		final JFreeChart chart = createChart(dataset);  
		chart.setTitle("Process History");
  
		Composite embeddedComposite = new Composite(composite, SWT.EMBEDDED);
		embeddedComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL));
		Frame fileTableFrame = SWT_AWT.new_Frame(embeddedComposite);
		ChartPanel panel = new ChartPanel(chart);
		panel.setPopupMenu(null);
		fileTableFrame.add(panel);
		
		// Ok Button
		Composite compositeBtn = new Composite(composite, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout sss = new GridLayout(3, true);
		compositeBtn.setLayout(sss);
		
		Label dummyLabel = new Label(compositeBtn, SWT.NONE);

		Button btnOk = new Button(compositeBtn, SWT.NONE);
		btnOk.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		btnOk.setText("Ok");
		btnOk.addSelectionListener(listenerButtonOk);

		composite.layout();

	}

	SelectionAdapter listenerButtonOk = new SelectionAdapter()
	{
		public void widgetSelected(SelectionEvent event)
		{
			shell.dispose();
		}
	};	

	
	/**
     * Creates a sample dataset for a Gantt chart.
     *
     * @return The dataset.
     */
    public IntervalCategoryDataset createDatasetGantt() {

    	// erstellen einer ArrayList firstTeit und eine ArrayList mit den steps (gleiche reihenfolge)
    	ArrayList<Long> firstTimePoints = new ArrayList<Long>();
    	ArrayList<Step> stepsUnordered = new ArrayList<Step>();
    	Map<Step,Long> stepFirstTimePoints = new HashMap<Step,Long>();
    	for(Step actStep : process.getStep())
    	{
    		Long firstTimePoint = actStep.getTimeSerieStatus().getFirstTime();
    		if(firstTimePoint != null)
    		{
        		firstTimePoints.add(firstTimePoint);
        		stepsUnordered.add(actStep);
        		stepFirstTimePoints.put(actStep, firstTimePoint);
    		}
    	}
    	
    	// sortieren der erstEintraege nach ihrem auftreten
    	Collections.sort(firstTimePoints);
    	
    	// sortieren der steps nach der gleichen ordnung
    	ArrayList<Step> stepsOrdered = new ArrayList<Step>();
    	for(Long actTimePoint : firstTimePoints)
    	{
    		for(Step actStep : stepsUnordered)
    		{
    			if(stepFirstTimePoints.get(actStep) == actTimePoint)
    			{
    				stepsOrdered.add(actStep);
    			}
    		}
    	}

    	// ermitteln aller moeglichen stati im prozess
    	Map<String,TaskSeries> statusTaskseries = new HashMap<String,TaskSeries>();
    	for(Step actStep : stepsOrdered)
    	{
			if(actStep.isRoot())
			{
				// wenn root, dann mit naechster iteration weitermachen
				continue;
			}
			for(Map<Long,String> actPair : actStep.getTimeSerieStatus().getSerie())
			{
				for(Long timeInMillis : actPair.keySet())
				{
					String status = actPair.get(timeInMillis);
    				
					// wenn status waiting|finished, dann auch mit naechster iteration weitermachen
					if(status.equals("waiting") || status.equals("finished"))
					{
						continue;
					}

					if(!statusTaskseries.containsKey(status))
					{
						statusTaskseries.put(status, new TaskSeries(status));
					}
    				
    				// hinzufuegen eines neuen tasks
					Long endTime = actStep.getTimeSerieStatus().getNextTime(timeInMillis);
    				// falls es noch keine endZeit fuer den aktuellen status gibt, soll die aktuelle zeit gesetzt werden
					if(endTime == null)
					{
						endTime = System.currentTimeMillis();
					}
    				
					statusTaskseries.get(status).add(new Task(actStep.getName(), new SimpleTimePeriod(date(timeInMillis), date(endTime))));
    		
				}
			}
    	}
    	
    	// alle timeSeries einer collection hinzufuegen
    	final TaskSeriesCollection collection = new TaskSeriesCollection();
    	for(TaskSeries actTaskSerie : statusTaskseries.values())
    	{
    		collection.add(actTaskSerie);
    	}
        return collection;

     }

    /**
     * Utility method for creating <code>Date</code> objects.
     *
     * @param day  the date.
     * @param month  the month.
     * @param year  the year.
     *
     * @return a date.
     */
    private static Date date(long timeInMillis) {

        final Calendar calendar = Calendar.getInstance();
        
        calendar.setTimeInMillis(timeInMillis);
        final Date result = calendar.getTime();
        return result;

    }

    /**
     * Creates a chart.
     * 
     * @param dataset  the dataset.
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final IntervalCategoryDataset dataset) {
        final JFreeChart chart = ChartFactory.createGanttChart(
            "Gantt Chart Demo",  // chart title
            "Task",              // domain axis label
            "Date",              // range axis label
            dataset,             // data
            true,                // include legend
            true,                // tooltips
            false                // urls
        );    
//        chart.getCategoryPlot().getDomainAxis().setMaxCategoryLabelWidthRatio(10.0f);
        return chart;    
    }
	
	/**
	 * @return the father
	 */
	public Object getFather() {
		return father;
	}

	/**
	 * @param father the father to set
	 */
	public void setFather(Object father) {
		this.father = father;
	}

	
}
