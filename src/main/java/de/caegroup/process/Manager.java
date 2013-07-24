package de.caegroup.process;

import de.caegroup.process.Process;
import de.caegroup.process.Step;
import java.io.IOException;
//import de.caegroup.view.Stepconnector;
//import org.w3c.dom.*;
//import org.xml.sax.*;
//import javax.xml.parsers.*;
//import java.io.*;
//import java.io.NotSerializableException;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
//import java.util.Date;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
//import org.apache.xerces.impl.xpath.regex.ParseException;

public class Manager
{

	/*----------------------------
	  structure
	----------------------------*/
	static CommandLine line;
	

	
	/*----------------------------
	  constructors
	----------------------------*/
//	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException
	public static void main(String[] args) throws org.apache.commons.cli.ParseException, CloneNotSupportedException
	{

//		try
//		{
//			if (args.length != 1)
//			{
//				System.out.println("Please specify Inputfile and Outputfile (prozessinstanz.lri)");
//			}
//			
//		}
//		catch (ArrayIndexOutOfBoundsException e)
//		{
//			System.out.println("***ArrayIndexOutOfBoundsException: Please specify procesdefinition.lrd and processinstance.lri\n" + e.toString());
//		}
		
		/*----------------------------
		  create boolean options
		----------------------------*/
		Option help = new Option("help", "print this message");
		Option v = new Option("v", "prints version and build-date");
		
		/*----------------------------
		  create argument options
		----------------------------*/
		Option instance = OptionBuilder.withArgName("instance")
				.hasArg()
				.withDescription("[mandatory] process instance file")
//				.isRequired()
				.create("instance");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( instance );
		
		/*----------------------------
		  create the parser
		----------------------------*/
		CommandLineParser parser = new GnuParser();
		try
		{
			// parse the command line arguments
			line = parser.parse( options,  args );
		}
//		catch ( ParseException exp )
		catch ( Exception exp )
		{
			// oops, something went wrong
			System.err.println( "Parsing failed. Reason: "+ exp.getMessage());
			exiter();
		}
		
		/*----------------------------
		  usage/help
		----------------------------*/
		if ( line.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("manager", options);
			System.exit(0);
		}
		
		else if ( line.hasOption("v"))
		{
			System.out.println("author:  alexander.vogel@caegroup.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		else if (!( line.hasOption("instance") ))
		{
			exiter();
		}
		
		
		Process p1 = new Process();
		
		long loop_period_seconds = 20;
		
		// prozessinstanz einlesen
		p1.setInfilebinary(line.getOptionValue("instance"));
		p1.log("info", "setting binary file for input to: "+line.getOptionValue("instance"));
		System.out.println("setting binary file for input to: "+line.getOptionValue("instance"));

		Process p2 = p1.readBinary();
		p2.log("info", "reading binary file: "+line.getOptionValue("instance"));

		p2.setOutfilebinary(line.getOptionValue("instance"));
		p2.log("info", "setting binary file for output: "+line.getOptionValue("instance"));

		p2.setInfilebinary(line.getOptionValue("instance"));
		
		// neue manager id generieren und sofort in die instanz auf platte schreiben
		double managerid = p2.genManagerid();
		p2.setManagerid(managerid);
		p2.log("info", "setting manager-id to: "+managerid);
		System.out.println("setting manager-id to: "+managerid);

		p2.writeBinary();
		p2.log("info", "writing process to binary file: "+managerid);

		boolean incharge = true;
		
		while(incharge)
		{
			// processinstanz frisch einlesen
			Process p3 = p1.readBinary();
			p3.log("info", "reading binary file: "+p1.getInfilebinary());
			p3.setOutfilebinary(line.getOptionValue("instance"));
			
			// wenn der prozess den status 'finished' hat, soll dieses programm beendet werden
			if (p3.getStatus().equals("finished"))
			{
				p3.log("info", "process instance is finished. goodbye from manager id "+p3.getManagerid());
				System.out.println("process instance is finished. goodbye from manager id "+managerid+".");
				System.exit(0);
			}
			
			// die manager-id mit eigener vergleichen. wenn nicht gleich, dann beenden.
			if (!(p3.getManagerid() == managerid))
			{
				p3.log("warn", "it appears that another manager (id: "+p3.getManagerid()+") took over. killing myself. bye.");
				System.out.println("it appears another instance of manager (id: "+p3.getManagerid()+") took over. so i'm (id: "+managerid+") not longer needed. killing myself. byebye.");
				System.exit(1);
			}

			Calendar time_entry = Calendar.getInstance();
			
			// jeden step durchgehen
		//	for(Step actualStep : p3.getSteps())
			Iterator<Step> iterstep = p3.getSteps().iterator();
			boolean pradar =  p3.getPradar();
			System.out.println("Anzahl Steps: "+p3.getSteps().size());
			while(iterstep.hasNext())
			{
				Step step = new Step(p3);
				try
				{
					step = iterstep.next();
				}
				catch (ConcurrentModificationException e)
				{
					break;
				}
				String status = step.getStatus();
//				System.out.println("step: "+step.getName()+" | status: "+step.getStatus());
				// if step is waiting
				if (status.equals("waiting"))
				{
					if (!(step.getName().equals("root")))
					{
						// versuchen zu initialisieren
						System.out.println("step: "+step.getName()+" => start initializing");
						boolean erfolgreich = step.initialize();
						if (!(erfolgreich)) {System.out.println("step: "+step.getName()+" => start initializing not successfull");}
					}
					else
					{
						// versuchen zu comitten
						try
						{
							step.commit();
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							System.out.println("problems with commit in step "+step.getName());
							e.printStackTrace();
						}
//						catch (NullPointerException e)
//						{
//							// TODO Auto-generated catch block
//							System.out.println("problems with commit in step "+step.getName());
//							e.printStackTrace();
//						}
//						System.out.println("step: "+step.getName()+" => start committing");
					}
				}

				if (status.equals("initialized"))
				{
					// versuchen den step aufzufaechern
					step.fan();
				}

				if (status.equals("fanned"))
				{
					// versuchen alle works zu starten
					step.work();
				}

				if (status.equals("working"))
				{
					// ueberpruefen ob work noch laeuft
					step.work();
				}

				if (status.equals("worked"))
				{
					// versuchen zu comitten
					try
					{
						step.commit();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						System.out.println("problems with commit in step "+step.getName());
						e.printStackTrace();
					}
				}

				if (status.equals("committing"))
				{
					// ueberpruefen ob work noch laeuft
//					step.commit();
				}

				if (status.equals("committed"))
				{
					if (pradar)
					{
						// aufruf von pradar-progress
						try
						{
							java.lang.Process sysproc = Runtime.getRuntime().exec("pradar-progress ");
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
//					step.setStatus("finished");
				}
				
			}
			
			p3.printToc();
			p3.setDatetonow();
			p3.detStatus(); 
			p3.writeBinary();
			
			Calendar time_exit = Calendar.getInstance();
			long seconds_for_loop = (time_exit.getTimeInMillis() - time_entry.getTimeInMillis()) / 1000;
			
			long rest_looptime_seconds = loop_period_seconds - seconds_for_loop;
			
			if ((rest_looptime_seconds) > 0)
			{
//				Thread.currentThread();
				try {
					Thread.sleep(rest_looptime_seconds*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}



	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}



}
