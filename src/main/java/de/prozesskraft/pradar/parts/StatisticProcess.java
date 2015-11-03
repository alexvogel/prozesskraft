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
		shell.setSize(800, 600);
		
		composite.setLayout(new GridLayout(1, false));
		
//		Composite compositeGantt = new Composite(composite, SWT.NONE);
//		compositeGantt.setLayout(new GridLayout(1, false));
////		gd_composite.minimumWidth = 10;
////		gd_composite.minimumHeight = 10;
//		compositeGantt.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));

		final IntervalCategoryDataset dataset = createDatasetGantt();  
		final JFreeChart chart = createChart(dataset);  
  
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

        
        
//        final TaskSeries s1 = new TaskSeries("Scheduled");
//        s1.add(new Task("Write Proposal",
//               new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
//                                    date(5, Calendar.APRIL, 2001))));
//        s1.add(new Task("Obtain Approval",
//               new SimpleTimePeriod(date(9, Calendar.APRIL, 2001),
//                                    date(9, Calendar.APRIL, 2001))));
//        s1.add(new Task("Requirements Analysis",
//               new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
//                                    date(5, Calendar.MAY, 2001))));
//        s1.add(new Task("Design Phase",
//               new SimpleTimePeriod(date(6, Calendar.MAY, 2001),
//                                    date(30, Calendar.MAY, 2001))));
//        s1.add(new Task("Design Signoff",
//               new SimpleTimePeriod(date(2, Calendar.JUNE, 2001),
//                                    date(2, Calendar.JUNE, 2001))));
//        s1.add(new Task("Alpha Implementation",
//               new SimpleTimePeriod(date(3, Calendar.JUNE, 2001),
//                                    date(31, Calendar.JULY, 2001))));
//        s1.add(new Task("Design Review",
//               new SimpleTimePeriod(date(1, Calendar.AUGUST, 2001),
//                                    date(8, Calendar.AUGUST, 2001))));
//        s1.add(new Task("Revised Design Signoff",
//               new SimpleTimePeriod(date(10, Calendar.AUGUST, 2001),
//                                    date(10, Calendar.AUGUST, 2001))));
//        s1.add(new Task("Beta Implementation",
//               new SimpleTimePeriod(date(12, Calendar.AUGUST, 2001),
//                                    date(12, Calendar.SEPTEMBER, 2001))));
//        s1.add(new Task("Testing",
//               new SimpleTimePeriod(date(13, Calendar.SEPTEMBER, 2001),
//                                    date(31, Calendar.OCTOBER, 2001))));
//        s1.add(new Task("Final Implementation",
//               new SimpleTimePeriod(date(1, Calendar.NOVEMBER, 2001),
//                                    date(15, Calendar.NOVEMBER, 2001))));
//        s1.add(new Task("Signoff",
//               new SimpleTimePeriod(date(28, Calendar.NOVEMBER, 2001),
//                                    date(30, Calendar.NOVEMBER, 2001))));
//
//        final TaskSeries s2 = new TaskSeries("Actual");
//        s2.add(new Task("Write Proposal",
//               new SimpleTimePeriod(date(1, Calendar.APRIL, 2001),
//                                    date(5, Calendar.APRIL, 2001))));
//        s2.add(new Task("Obtain Approval",
//               new SimpleTimePeriod(date(9, Calendar.APRIL, 2001),
//                                    date(9, Calendar.APRIL, 2001))));
//        s2.add(new Task("Requirements Analysis",
//               new SimpleTimePeriod(date(10, Calendar.APRIL, 2001),
//                                    date(15, Calendar.MAY, 2001))));
//        s2.add(new Task("Design Phase",
//               new SimpleTimePeriod(date(15, Calendar.MAY, 2001),
//                                    date(17, Calendar.JUNE, 2001))));
//        s2.add(new Task("Design Signoff",
//               new SimpleTimePeriod(date(30, Calendar.JUNE, 2001),
//                                    date(30, Calendar.JUNE, 2001))));
//        s2.add(new Task("Alpha Implementation",
//               new SimpleTimePeriod(date(1, Calendar.JULY, 2001),
//                                    date(12, Calendar.SEPTEMBER, 2001))));
//        s2.add(new Task("Design Review",
//               new SimpleTimePeriod(date(12, Calendar.SEPTEMBER, 2001),
//                                    date(22, Calendar.SEPTEMBER, 2001))));
//        s2.add(new Task("Revised Design Signoff",
//               new SimpleTimePeriod(date(25, Calendar.SEPTEMBER, 2001),
//                                    date(27, Calendar.SEPTEMBER, 2001))));
//        s2.add(new Task("Beta Implementation",
//               new SimpleTimePeriod(date(27, Calendar.SEPTEMBER, 2001),
//                                    date(30, Calendar.OCTOBER, 2001))));
//        s2.add(new Task("Testing",
//               new SimpleTimePeriod(date(31, Calendar.OCTOBER, 2001),
//                                    date(17, Calendar.NOVEMBER, 2001))));
//        s2.add(new Task("Final Implementation",
//               new SimpleTimePeriod(date(18, Calendar.NOVEMBER, 2001),
//                                    date(5, Calendar.DECEMBER, 2001))));
//        s2.add(new Task("Signoff",
//               new SimpleTimePeriod(date(10, Calendar.DECEMBER, 2001),
//                                    date(11, Calendar.DECEMBER, 2001))));

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
