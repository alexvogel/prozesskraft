package de.caegroup.network;

import java.io.File;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import de.caegroup.pradar.Db;
import de.caegroup.commons.*;


public class ConcurrentServer implements Runnable
{
	/*----------------------------
	  fields
	----------------------------*/
	int port = 37888;
	Db db = new Db();
	ServerSocket server;
	Thread thread;
	
	/*----------------------------
	  constructors
	----------------------------*/
	/**
	 * constructor
	 * @param 
	 */
	public ConcurrentServer()
	{
		this.port = WhereAmI.getDefaultPortNumber();
		this.db.setDbfile(WhereAmI.getDefaultDbfile(this.getClass()));
		try
		{
			server = new ServerSocket(port);
			thread = new Thread(this);
			thread.start();
		}
		catch(BindException e)
		{
			System.out.println("Adress '"+port+"' is already in use.");
//			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * constructor
	 * @param int
	 */
	public ConcurrentServer(int portNumber)
	{
		this.port = portNumber;
		this.db.setDbfile(WhereAmI.getDefaultDbfile(this.getClass()));
		try
		{
			server = new ServerSocket(port);
			thread = new Thread(this);
			thread.start();
		}
		catch(BindException e)
		{
			System.out.println("Adress '"+port+"' is already in use.");
//			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * constructor
	 * @param int File
	 */
	public ConcurrentServer(int portNumber, File dbfile)
	{
		this.port = portNumber;
		this.db.setDbfile(dbfile);
		try
		{
			server = new ServerSocket(port);
			thread = new Thread(this);
			thread.start();
		}
		catch(BindException e)
		{
			System.out.println("Adress '"+port+"' is already in use.");
//			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/*----------------------------
	  methods
	----------------------------*/
	public void run()
	{
		try
		{
			while(true)
			{
				Socket client = server.accept();
				new Client(client, this).run();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void stop()
	{
		System.exit(0);
	}

}
