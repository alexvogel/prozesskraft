package de.prozesskraft.network;

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

import de.prozesskraft.commons.*;
import de.prozesskraft.pradar.Db;
import de.prozesskraft.pradar.Entity;


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
		
//		log("debug", "outputStream erstellen");
		OutputStream streamToClient = this.s.getOutputStream();
		
//		log("debug", "outputStream flushen");
		streamToClient.flush();
		
//		log("debug", "objectOutputStream erstellen");
		objectToClient = new ObjectOutputStream(streamToClient);

//		log("debug", "objectOutputStream flushen");
		objectToClient.flush();
	}
	
	/*----------------------------
	  methods
	----------------------------*/
	public void run()
	{
		try
		{

//			log("debug", "inputStream erstellen");
			InputStream streamFromClient = this.s.getInputStream();

//			log("debug", "objectInputStream erstellen");
			objectFromClient  = new ObjectInputStream(streamFromClient);

//			log("debug", "typ der anforderung von client empfangen");
			String type   = (String) objectFromClient.readObject();			

//			log("debug", "objectInputStream schliessen");
			// object zerstoeren - wird nicht mehr gebraucht
//			objectFromClient.close();

			// wenn der befehl vom client 'init' lautet, soll die datenbank initialisiert werden
			if (type.equals("init"))
			{
				this.parent.db.initDb();
				log("info", "creating new dbfile");
			}

			// wenn der befehl vom client 'initforce' lautet, soll die datenbank - initialisierung erzwungen werden
			else if (type.equals("initforce"))
			{
				log("info", "creating new dbfile with force");
				this.parent.db.initForceDb();
			}

			// wenn der befehl vom client 'attend' lautet, soll ein prozess eingecheckt oder upgedatet werden
			else if (type.equals("attend"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "attenting entity id "+entity.getId());
				this.parent.db.attendEntity(entity);
			}

			// wenn der befehl vom client 'checkin' lautet, soll ein prozess eingecheckt werden
			else if (type.equals("checkin"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "checking in entity id "+entity.getId());
				this.parent.db.checkinEntity(entity);
			}

			// wenn der befehl vom client 'checkout' lautet, soll ein prozess ausgecheckt werden
			else if (type.equals("checkout"))
			{
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "checking out entity id "+entity.getId());
				this.parent.db.checkoutEntity(entity);
			}

			// wenn der befehl vom client 'list' lautet, soll der DB-Inhalt gelistet werden
			else if (type.equals("list"))
			{
				// dieses entity enthaelt die filter informationen der auszugebenden liste
				Entity entity = (Entity) objectFromClient.readObject();
				
				log("info", "obtaining list about entities that pass the filter: "+entity.getId());
				ArrayList<String> list = this.parent.db.list(entity);
				objectToClient.writeObject(list);
			}

			// wenn der befehl vom client 'getall' lautet, sollen alle Entities der DB geliefert werden
			else if (type.equals("getall"))
			{
				log("debug", "anforderung von client lautet 'getall'");

				log("info", "obtaining information about all entities");
				ArrayList<Entity> allEntities = this.parent.db.getAllEntities();
				log("info", "plan" + allEntities.size()+" entities written to objectOutputStream");
				objectToClient.writeObject(allEntities);
				objectToClient.flush();
				log("info", "all entities written to objectOutputStream");
				
				log("debug", "objectOutputStream schliessen");
//				objectToClient.close();
			}

			// wenn der befehl vom client 'getallfromuser' lautet, sollen alle Entities eines bestimmten users aus der DB geliefert werden
			else if (type.equals("getallfromuser"))
			{
				log("debug", "anforderung von client lautet 'getallfromuser'");

				log("info", "obtaining information about all entities from a certain user");

				// dieser string enthaelt den user, dessen entities zurueckgegeben werden sollen
				String user = (String) objectFromClient.readObject();
				
				ArrayList<Entity> allEntitiesFromUser = this.parent.db.getAllEntitiesFromCertainUser(user);
				log("info", "plan" + allEntitiesFromUser.size()+" entities written to objectOutputStream");
				objectToClient.writeObject(allEntitiesFromUser);
				objectToClient.flush();
				log("info", "all entities written to objectOutputStream");
				
				log("debug", "objectOutputStream schliessen");
//				objectToClient.close();
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
