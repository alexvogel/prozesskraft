package de.caegroup.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import de.caegroup.pradar.Db;
import de.caegroup.pradar.Entity;


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
	private Db db = new Db();
	
	/*----------------------------
	  constructors
	----------------------------*/
	public Client(Socket s) throws IOException
	{
		this.s = s;
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
				db.initDb();
			}
			else if (type.equals("checkin"))
			{
				String dbpath = (String) objectIn.readObject();
				System.out.println("setting dbpath to: "+dbpath);
				this.db.setDbfile(dbpath);

				Entity entity = (Entity) objectIn.readObject();
				
				System.out.println("checking in entity");
				db.checkinEntity(entity);
			}
			else if (type.equals("checkout"))
			{
				String dbpath = (String) objectIn.readObject();
				System.out.println("setting dbpath to: "+dbpath);
				this.db.setDbfile(dbpath);

				Entity entity = (Entity) objectIn.readObject();
				
				System.out.println("checking out entity");
				db.checkoutEntity(entity);
			}
			else if (type.equals("list"))
			{
				String dbpath = (String) objectIn.readObject();
				System.out.println("setting dbpath to: "+dbpath);
				this.db.setDbfile(dbpath);

				Entity entity = (Entity) objectIn.readObject();
				
				ArrayList<String> list = db.list(entity);
				objectOut.writeObject(list);
			}
			else if (type.equals("getall"))
			{
				ArrayList<Entity> allEntities = db.getAllEntities();
				objectOut.writeObject(allEntities);
			}
			else if (type.equals("stop"))
			{
				System.out.println("stopping server...");
				System.exit(0);
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
