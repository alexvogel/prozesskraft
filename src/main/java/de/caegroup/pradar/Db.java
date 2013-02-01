package de.caegroup.pradar;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.sql.*;

public class Db
{
	/*----------------------------
	  structure
	----------------------------*/
	
	private File dbfile;
//	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	/*----------------------------
	  constructors
	----------------------------*/

	public Db(File file)
	{
		setDbfile(file);

		// JDBC-Treiber einbinden
		try
		{
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Db(String pathtofile)
	{
		File file = new File(pathtofile);
		new Db(file);
	}

	/*----------------------------
	  methods
	----------------------------*/
	public void initDb()
	{
		Connection connection = null;
		Entity muster = new Entity();
		Class entityClass = muster.getClass();
		Field[] fields = entityClass.getFields();
		for (int i=0; i < fields.length; i++)
		{
			System.out.println("Public field found: " + fields[i].toString());
		}
	}

	public void checkinEntity(Entity entity) throws ClassNotFoundException
	{
		Class.forName("org.sqlite.JDBC");
	}

	public void getEntityById(String id) throws ClassNotFoundException
	{
		Class.forName("org.sqlite.JDBC");
		Connection connection = null;
//		try
//		{
//			// create a database connection
//			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile);
//		}
	}
	
	
	
	/*----------------------------
	  methods getter/setter
	----------------------------*/

	public File getDbfile()
	{
		return dbfile;
	}

	public void setDbfile(File dbfile)
	{
		this.dbfile = dbfile;
	}

}
