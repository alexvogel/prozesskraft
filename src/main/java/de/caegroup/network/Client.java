package de.caegroup.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import de.caegroup.pradar.Db;
import de.caegroup.pradar.Entity;
import de.caegroup.commons.*;


public class Client
{
	/*----------------------------
	  fields
	----------------------------*/
	private Socket s;
	private OutputStream out;
	private InputStream in;
	private ObjectOutputStream objectOut;
	private ObjectInputStream objectIn;
	private boolean typeUnknown = true;
	private ConcurrentServer parent;
	
	/*----------------------------
	  constructors
	----------------------------*/
	public Client(Socket s, ConcurrentServer p) throws IOException
	{
		this.s = s;
		this.parent = p;
		out = s.getOutputStream();
		in = s.getInputStream();
		objectOut = new ObjectOutputStream(out);
		objectIn  = new ObjectInputStream(in);
	}
	
	/*----------------------------
	  methods
	----------------------------*/
	public void run()
	{
		try
		{
			String type   = (String) objectIn.readObject();			
			System.out.println("setting type to: "+type);
			if (type.equals("init"))
			{
				this.parent.db.initDb();
			}
			else if (type.equals("initforce"))
			{
				this.parent.db.initForceDb();
			}
			else if (type.equals("checkin"))
			{
				Entity entity = (Entity) objectIn.readObject();
				
				System.out.println("checking in entity");
				this.parent.db.checkinEntity(entity);
			}
			else if (type.equals("checkout"))
			{
				Entity entity = (Entity) objectIn.readObject();
				
				System.out.println("checking out entity");
				this.parent.db.checkoutEntity(entity);
			}
			else if (type.equals("list"))
			{
				Entity entity = (Entity) objectIn.readObject();
				
				ArrayList<String> list = this.parent.db.list(entity);
				objectOut.writeObject(list);
			}
			else if (type.equals("getall"))
			{
				ArrayList<Entity> allEntities = this.parent.db.getAllEntities();
				objectOut.writeObject(allEntities);
			}
			else if (type.equals("stop"))
			{
				System.exit(0);
			}
			else if (type.equals("cleandb"))
			{
				this.parent.db.cleanDb();
				System.out.println("cleaning db.");
			}
			else
			{
				System.out.println("dont know what you want: type="+type);
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

}
