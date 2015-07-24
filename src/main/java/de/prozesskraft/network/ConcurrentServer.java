package de.prozesskraft.network;

import java.io.File;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;

import de.prozesskraft.commons.*;
import de.prozesskraft.pradar.Db;


public class ConcurrentServer implements Runnable
{
	/*----------------------------
	  fields
	----------------------------*/
	int port;
	Db db = new Db();
	String sshIdRelPath = null; 
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
		this.sshIdRelPath = WhereAmI.getDefaultSshIdRsa();
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
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * constructor
	 * @param String
	 */
	public ConcurrentServer(String sshIdRelPath)
	{
		this.sshIdRelPath = sshIdRelPath;
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
			e.printStackTrace();
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
	public ConcurrentServer(String sshIdRelPath, int portNumber)
	{
		this.sshIdRelPath = sshIdRelPath;
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
			e.printStackTrace();
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
	public ConcurrentServer(String sshIdRelPath, int portNumber, File dbfile)
	{
		this.sshIdRelPath = sshIdRelPath;
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
			e.printStackTrace();
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
				Socket socket = server.accept();
				new Server(socket, this).run();
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

	/**
	 * @return the port
	 */
	public int getPort()
	{
		return this.port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @return the db
	 */
	public Db getDb()
	{
		return this.db;
	}

	/**
	 * @param db the db to set
	 */
	public void setDb(Db db)
	{
		this.db = db;
	}

	/**
	 * @return the pathSshIdRsa
	 */
	public String getSshIdRelPath()
	{
		return this.sshIdRelPath;
	}

	/**
	 * @param pathSshIdRsa the pathSshIdRsa to set
	 */
	public void setSshIdRelPath(String sshIdRelPath)
	{
		this.sshIdRelPath = sshIdRelPath;
	}

	void log(String loglevel, String message)
	{
		String logstring = "["+new Timestamp(System.currentTimeMillis()) + "]:"+loglevel+":"+message;
		System.out.println(logstring);
	}

}
