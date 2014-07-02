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
				
		Option stop = OptionBuilder.withArgName("stop")
//				.hasArg()
				.withDescription("[optional] stops a running manager for given instance")
//				.isRequired()
				.create("stop");
				
		/*----------------------------
		  create options object
		----------------------------*/
		Options options = new Options();
		
		options.addOption( help );
		options.addOption( v );
		options.addOption( instance );
		options.addOption( stop );
		
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
			System.out.println("author:  info@prozesskraft.de");
			System.out.println("version: [% version %]");
			System.out.println("date:    [% date %]");
			System.exit(0);
		}
		
		else if (!( line.hasOption("instance") ))
		{
			exiter();
		}
		
		/*----------------------------
		  business logic
		----------------------------*/
		
		Process p1 = new Process();
		
		long loop_period_seconds = 20;
		
		double managerid = p1.genManagerid();

		java.io.File fileBinary = new java.io.File(line.getOptionValue("instance"));
		String pathBinary = "";
		
		if (fileBinary.exists())
		{
			pathBinary = fileBinary.getAbsolutePath();
		}
		else
		{
			System.out.println("file does not exist: "+fileBinary.getAbsolutePath());
		}
		
		if ( line.hasOption("stop") )
		{
			p1.setInfilebinary(pathBinary);
			Process p2 = p1.readBinary();
			p2.log("info", "stopping manager "+p2.getManagerid());
			p2.setManagerid(0);
			p2.setStatus("paused");
			p2.setOutfilebinary(pathBinary);
			p2.writeBinary();
			System.exit(0);
		}
		
		// prozessinstanz einlesen
		p1.setInfilebinary(pathBinary);
		
		Process p2;
		p2 = p1.readBinary();
		
		p2.setManagerid(managerid);
		p2.log("info", "manager "+managerid+": occupying instance.");
		p2.log("debug", "manager "+managerid+": setting new manager-id to signal other running managers that they are not longer needed.");

		p2.log("debug", "manager "+managerid+": setting binary file for input to: "+pathBinary);
//		System.out.println("setting binary file for input to: "+line.getOptionValue("instance"));

		p2.log("debug", "manager "+managerid+": reading binary file: "+pathBinary);

		p2.setInfilebinary(pathBinary);
		p2.setOutfilebinary(pathBinary);
		p2.log("debug", "manager "+managerid+": setting binary file for output: "+pathBinary);

		// instanz auf platte schreiben (um anderen managern zu signalisieren, dass sie nicht mehr gebraucht werden
//		System.out.println("setting manager-id to: "+managerid);

		p2.log("debug", "manager "+managerid+": writing process to binary file to occupy instance.");

		p2.writeBinary();

		boolean incharge = true;
		
		while(incharge)
		{
			// processinstanz frisch einlesen
			Process p3 = p2.readBinary();

			p3.log("debug", "manager "+managerid+": actual infilexml is: "+p3.getInfilexml());
			p3.log("debug", "manager "+managerid+": reading binary file: "+p2.getInfilebinary());
			
			// wenn der prozess den status 'finished' hat, soll dieses programm beendet werden
			if (p3.getStatus().equals("finished"))
			{
				p3.log("info", "manager "+managerid+": process instance is finished. goodbye from manager id "+p3.getManagerid());
				p3.writeBinary();
//				System.out.println("process instance is finished. goodbye from manager id "+managerid+".");
				System.exit(0);
			}
			
			// die manager-id mit eigener vergleichen. wenn nicht gleich, dann beenden.
			if (!(p3.getManagerid() == managerid))
			{
//				p3.log("warn", "manager "+managerid+": it appears that another manager (id: "+p3.getManagerid()+") took over. killing myself. bye.");
				System.out.println("it appears another instance of manager (id: "+p3.getManagerid()+") took over. so i'm (id: "+managerid+") not longer needed. killing myself. byebye.");
				System.exit(1);
			}

			Calendar time_entry = Calendar.getInstance();
			
			// jeden step durchgehen
//			for(Step actualStep : p3.getSteps())
			Iterator<Step> iterstep = p3.getSteps().iterator();
			boolean pradar =  (!(p3.isWrapper()));
//			System.out.println("Anzahl Steps: "+p3.getSteps().size());
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

				// if step is waiting or init failed
				if (step.getStatus().equals("waiting") || step.getStatus().equals("initialization failed"))
				{
					if (!(step.getName().equals("root")))
					{
						// versuchen zu initialisieren
						p3.log("debug", "manager "+managerid+": initializing step '"+step.getName()+"'");
						if (step.initialize())
						{
							p3.log("debug", "manager "+managerid+": initialisation of step '"+step.getName()+"' succesfull");
						}
						else
						{
							p3.log("debug", "manager "+managerid+": initialisation of step '"+step.getName()+"' failed");
						}
					}
					else
					{
						// versuchen zu comitten
						p3.log("debug", "manager "+managerid+": committing step '"+step.getName()+"'");
						try
						{
							if (step.commit())
							{
								p3.log("debug", "manager "+managerid+": commit of step '"+step.getName()+"' succesfull");
							}
							else
							{
								p3.log("debug", "manager "+managerid+": commit of step '"+step.getName()+"' failed");
							}
						}
						catch (IOException e)
						{
							p3.log("debug", "manager "+managerid+": caught an IOException.");
							e.printStackTrace();
						}
					}
					updateFile(p3);
				}

				if (step.getStatus().equals("initialized"))
				{
					// versuchen den step aufzufaechern
					p3.log("debug", "manager "+managerid+": fanning step '"+step.getName()+"'");
					if (step.fan())
					{
						p3.log("debug", "manager "+managerid+": fan-out of step '"+step.getName()+"' succesfull");
					}
					else
					{
						p3.log("debug", "manager "+managerid+": fan-out of step '"+step.getName()+"' failed");
					}
					updateFile(p3);
				}

				if (step.getStatus().equals("fanned"))
				{
					p3.log("debug", "manager "+managerid+": working step '"+step.getName()+"'");
					// versuchen alle works zu starten
					if (step.work())
					{
						p3.log("debug", "manager "+managerid+": launching work-program of step '"+step.getName()+"' succesfull");
					}
					else
					{
						p3.log("debug", "manager "+managerid+": launching work-program of step '"+step.getName()+"' failed");
					}
					updateFile(p3);
				}

				if (step.getStatus().equals("working"))
				{
					// ueberpruefen ob work noch laeuft
					p3.log("debug", "manager "+managerid+": check whether work-program of step '"+step.getName()+"' is still running");
					if (step.work())
					{
						p3.log("debug", "manager "+managerid+": work-program of step '"+step.getName()+"' finished");
					}
					else
					{
						p3.log("debug", "manager "+managerid+": work-program of step '"+step.getName()+"' is still running");
					}
					updateFile(p3);
				}

				if (step.getStatus().equals("worked"))
				{
					// versuchen zu comitten
					p3.log("debug", "manager "+managerid+": commit of step '"+step.getName()+"'");
					try
					{
						if (step.commit())
						{
							p3.log("debug", "manager "+managerid+": commit of step '"+step.getName()+"' succesfull");
						}
						else
						{
							p3.log("debug", "manager "+managerid+": commit of step '"+step.getName()+"' failed");
						}
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
//						System.out.println("problems with commit in step "+step.getName());
						p3.log("debug", "manager "+managerid+": caught an IOException.");
						e.printStackTrace();
					}
					updateFile(p3);
				}

				if (step.getStatus().equals("committing"))
				{
					// ueberpruefen ob work noch laeuft
					try {
						step.commit();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (step.getStatus().equals("committed"))
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
					updateFile(p3);
				}
				
			}
			
//			p3.printToc();
			updateFile(p3);
			
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

	private static void updateFile(Process process)
	{
		process.setDatetonow();
		process.touch();
		process.detStatus(); 
		process.writeBinary();
	}

	private static void exiter()
	{
		System.out.println("try -help for help.");
		System.exit(1);
	}



}
