package de.caegroup.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

import de.caegroup.pradar.Db;
import de.caegroup.pradar.Entity;
import de.caegroup.commons.*;


public class Server
{
	/*----------------------------
	  fields
	----------------------------*/
	private Socket s;
	private ObjectOutputStream objectToClient;
	private ObjectInputStream objectFromClient;
//	private boolean typeUnknown = true;
	private ConcurrentServer parent;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public Server(Socket s, ConcurrentServer p) throws IOException
	{
		this.s = s;
		this.parent = p;
	}
	
	/*----------------------------
	  methods
	----------------------------*/
	public void run()
	{
		try
		{
			// streams erstellen
			log("debug", "outputStream erstellen");
			BufferedOutputStream streamToClient = new BufferedOutputStream(this.s.getOutputStream());

			log("debug", "objectOutputStream  erstellen");
			objectToClient = new ObjectOutputStream(streamToClient);

			log("debug", "objectOutputStream  flushen");
			streamToClient.flush();
			objectToClient.flush();
			
			log("debug", "inputStream erstellen");
			InputStream streamFromClient = this.s.getInputStream();

			log("debug", "objectInputStream  erstellen");
			objectFromClient  = new ObjectInputStream(streamFromClient);

			String type   = (String) objectFromClient.readObject();			
			if (type.equals("init"))
			{
				this.parent.db.initDb();
				log("info", "creating new dbfile");
			}
			else if (type.equals("initforce"))
			{
				log("info", "creating new dbfile with force");
				this.parent.db.initForceDb();
			}
			else if (type.equals("checkin"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "checking in entity id "+entity.getId());
				this.parent.db.checkinEntity(entity);
			}
			else if (type.equals("checkout"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "checking out entity id "+entity.getId());
				this.parent.db.checkoutEntity(entity);
			}
			else if (type.equals("list"))
			{
				// dieses entity enthaelt die filter informationen der auszugebenden liste
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "obtaining list about entities that pass the filter: "+entity.getId());
				ArrayList<String> list = this.parent.db.list(entity);
				objectToClient.writeObject(list);
			}
			else if (type.equals("getall"))
			{
				objectToClient.writeObject("Hi HO THERE!!!");
				log("info", "obtaining information about all entities");
				ArrayList<Entity> allEntities = this.parent.db.getAllEntities();
				log("info", allEntities.size()+"all entities written to objectOutputStream");
				objectToClient.writeObject(allEntities);
				objectToClient.flush();
				log("info", "all entities written to objectOutputStream");
			}
			else if (type.equals("stop"))
			{
				log("info", "stopping pradar-server");
				System.exit(0);
			}
			else if (type.equals("cleandb"))
			{
				log("info", "cleaning db for all users");
				this.parent.db.cleanDb(this.parent.getSshIdRelPath(), "");
			}
			else if (type.equals("cleandb_user"))
			{
				String user = (String) objectFromClient.readObject();
				log("info", "cleaning db on behalf of user "+user);
				this.parent.db.cleanDb(this.parent.getSshIdRelPath(), user);
			}
			else if (type.equals("delete"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				log("info", "deleting entity id "+entity.getId());
				this.parent.db.deleteEntity(entity);
			}
			else if (type.equals("progress"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				log("info", "updating progress for entity id "+entity.getId());
				this.parent.db.setProgress(entity);
			}
			else
			{
				log("info", "unknown type ("+type+"). don't know what to do");
			}
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void log(String loglevel, String message)
	{
		String logstring = "["+new Timestamp(System.currentTimeMillis()) + "]:"+loglevel+":"+message;
		System.out.println(logstring);
	}

}
