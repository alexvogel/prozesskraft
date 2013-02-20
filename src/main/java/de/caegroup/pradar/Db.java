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
//			System.out.println("getting connection to: "+this.dbfile.getAbsolutePath());
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(10);
			
			statement.executeUpdate("drop table if exists radar");
			statement.executeUpdate("create table radar (id, process, host, user, checkin, checkout, active, exitcode)");
			
			connection.close();

		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//// falls es automatisch von den feldern in entity abgeleitet werden soll
//		Entity muster = new Entity();
//		Class entityClass = muster.getClass();
//		Field[] fields = entityClass.getFields();
//		for (int i=0; i < fields.length; i++)
//		{
//			System.out.println("Public field found: " + fields[i].toString());
//		}
	}

	public void checkinEntity(Entity entity)
	{
		this.sqlvoodoo();
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			Statement statement = connection.createStatement();

			statement.setQueryTimeout(10);
			String sql = "INSERT INTO radar (id, process, host, user, checkin, checkout, active, exitcode) VALUES ('"+entity.getId()+"', '"+entity.getProcess()+"', '"+entity.getHost()+"', '"+entity.getUser()+"', '"+entity.getCheckin().getTimeInMillis()+"', ' ', '"+entity.getActive()+"', '"+entity.getExitcode()+"')"; 
			System.out.println(sql);
			statement.executeUpdate(sql);
			
			connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void checkoutEntity(Entity entity)
	{
		this.sqlvoodoo();
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(10);
			
			String sql = "UPDATE OR REPLACE radar SET checkout='"+entity.getCheckout().getTimeInMillis()+"', active='false', exitcode='"+ entity.getExitcode() +"' WHERE id IS '"+entity.getId()+"' AND host IS '"+entity.getHost()+"' AND user IS '"+entity.getUser()+"' AND process IS '"+entity.getProcess()+"' AND active IS 'true'";
//			System.out.println(sql);
			statement.executeUpdate(sql);
			
			connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void list(Entity entity)
	{
		this.sqlvoodoo();
		Connection connection = null;
		try
		{
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(10);
			
			String sql = "SELECT * FROM radar WHERE id LIKE '"+entity.getId()+"' AND host LIKE '"+entity.getHost()+"' AND user LIKE '"+entity.getUser()+"' AND process LIKE '"+entity.getProcess()+"' AND active LIKE '"+entity.getActive()+"' AND exitcode LIKE '"+entity.getExitcode()+"'";
			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
			
			String formatstring = "|%-11s|%-11s|%-7s|%-13s|%-6s|%-23s|%-23s|%-8s|\n";
			System.out.format(formatstring, "id", "process", "user", "host", "active", "checkin", "checkout", "exitcode");
			System.out.format(formatstring, "-----------", "-----------", "-------", "-------------", "------", "-----------------------", "-----------------------", "--------");

			while (rs.next())
			{
				// millis in calendar umwandeln
//				Calendar cal_checkin = Calendar.getInstance();
//				Calendar cal_checkout = Calendar.getInstance();
//				cal_checkin.setTimeInMillis(new Long(rs.getString("checkin")));
//				cal_checkout.setTimeInMillis(new Long(rs.getString("checkin")));
				
				String tst_str_checkin = new String();
				Timestamp tst_checkin = new Timestamp(0);
				if (!(rs.getString("checkin").matches(" ")))
				{
					tst_checkin = new Timestamp(new Long(rs.getString("checkin")));
					tst_str_checkin = tst_checkin.toString();
				}
				else
				{
					tst_str_checkin = tst_checkin.toString();
				}
				
				String tst_str_checkout = new String();
				Timestamp tst_checkout = new Timestamp(0);
				if (!(rs.getString("checkout").matches(" ")))
				{
					tst_checkout = new Timestamp(new Long(rs.getString("checkout")));
					tst_str_checkout = tst_checkout.toString();
				}
				else
				{
					tst_str_checkout = tst_checkout.toString();
				}
				
				System.out.format(formatstring, rs.getString("id"), rs.getString("process"), rs.getString("user"), rs.getString("host"), rs.getString("active"), tst_str_checkin, tst_str_checkout, rs.getString("exitcode") );
			}
			
			connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
