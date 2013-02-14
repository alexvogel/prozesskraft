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

	/**
	 * constructor without parameter
	 * dbfile is expected in default location relative to program installation
	 */
	public Db()
	{
//		File program = new File(System.getProperty("java.class.path"));
		this (new File (System.getProperty("java.class.path")).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath()+"/data/pradar/pradar.db");
	}

	/**
	 * constructor with given dbfile as String of path
	 */
	public Db(String pathtofile)
	{
		setDbfile(new File(pathtofile));
	}

	/*----------------------------
	  methods
	----------------------------*/
	public void initDb()
	{
		this.sqlvoodoo();

		Connection connection = null;
		try
		{
			System.out.println("getting connection to: "+this.dbfile.getAbsolutePath());
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(10);
			
			statement.executeUpdate("drop table if exists radar");
			statement.executeUpdate("create table radar (id UNIQUE, processname, host, user, checkin, checkout, restart)");
			
//			connection.commit();
			connection.close();

		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
// falls es automatisch von den feldern in entity abgeleitet werden soll
		Entity muster = new Entity();
		Class entityClass = muster.getClass();
		Field[] fields = entityClass.getFields();
		for (int i=0; i < fields.length; i++)
		{
			System.out.println("Public field found: " + fields[i].toString());
		}
	}

	public void checkinEntity(Entity entity)
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(30);
			
			statement.executeUpdate("INSERT INTO radar (id, processname, host, user, checkin, restart) VALUES ("+entity.getId()+", "+entity.getProcessname()+", "+entity.getHost()+", "+entity.getUser()+", "+entity.getCheckin()+", "+entity.isRestart()+")");
			
			connection.commit();
			connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void checkoutEntity(Entity entity)
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(30);
			
			statement.executeUpdate("INSERT INTO radar (checkout) VALUES ("+entity.getCheckout()+") WHERE (id="+entity.getId()+")");
			
			connection.commit();
			connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	
	public void sqlvoodoo()
	{
		try
		{
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
