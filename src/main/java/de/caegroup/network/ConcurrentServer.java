package de.caegroup.network;

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;


public class ConcurrentServer implements Runnable
{
	/*----------------------------
	  fields
	----------------------------*/
	int port = 37888;
	ServerSocket server;
	Thread thread;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public ConcurrentServer()
	{
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

	public ConcurrentServer(int portNumber)
	{
		this.port = portNumber;
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
				new Client(client).run();
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
