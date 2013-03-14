package de.caegroup.pradar;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.*;

public class Db
{
	/*----------------------------
	  structure
	----------------------------*/
	
	private File dbfile;
	private Connection connection = null;
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
		File installationdirectory = WhereAmI.WhereAmI(this.getClass());
		
//		System.out.println("installationdirectory is: "+installationdirectory.getAbsolutePath());
		File file = installationdirectory;
		
		for (int x=0; x<4; x++)
		{
			try
			{
				file = file.getParentFile();
			}
			catch (Exception e)
			{
				System.err.println("fatal: default position of databasefile cannot be determined.");
			}
		}
		
		try
		{
			File dbfile = new File(file.getAbsoluteFile()+"/data/pradar/pradar.db");
			setDbfile(dbfile);
		}
		catch (NullPointerException e)
		{
			System.err.println("fatal: default position of databasefile cannot be determined.");
			System.exit(1);
		}
		
//		String path_dbfile = installationdirectory.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath()+"/data/pradar/pradar.db";
//		System.out.println("default dbfile is: "+dbfile.getAbsolutePath());
		
//		System.exit(0);
//		File program = new File(System.getProperty("java.class.path"));
//		String javaclasspath = (new File (System.getProperty("java.class.path")).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath());
//		String install = (new File (System.getProperty("java.class.path")).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getAbsolutePath());
//		String default_dbfilepath = install+"/data/pradar/pradar.db";
//		System.out.println("javaclasspath: "+javaclasspath);
//		System.out.println("installdir: "+install);
//		System.out.println("using dbfile: "+default_dbfilepath);
		
		
//		setDbfile(new File (default_dbfilepath));
//		setDbfile(new File("/data/pradar/pradar.db"));
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
			statement.executeUpdate("create table radar (id, process, host, user, checkin, checkout, active, exitcode, resource)");
			
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
			String sql = "INSERT INTO radar (id, process, host, user, checkin, checkout, active, exitcode, resource) VALUES ('"+entity.getId()+"', '"+entity.getProcess()+"', '"+entity.getHost()+"', '"+entity.getUser()+"', '"+entity.getCheckin().getTimeInMillis()+"', '0', '"+entity.getActive()+"', '"+entity.getExitcode()+"', '"+entity.getResource()+"')"; 
//			System.out.println(sql);
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
			
			String sql = "UPDATE OR REPLACE radar SET checkout='"+entity.getTimeInMillisOfNow()+"', active='false', exitcode='"+ entity.getExitcode() +"' WHERE id IS '"+entity.getId()+"' AND host IS '"+entity.getHost()+"' AND user IS '"+entity.getUser()+"' AND process IS '"+entity.getProcess()+"' AND active IS 'true'";
//			System.out.println(sql);
			statement.executeUpdate(sql);
			
			connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<Entity> match(Entity entity)
	{
		ArrayList<Entity> matches = new ArrayList<Entity>();
		this.sqlvoodoo();
		try
		{
			this.getConnection();
			Statement statement = this.connection.createStatement();
			
			statement.setQueryTimeout(10);
			String sql = "SELECT * FROM radar WHERE id LIKE '"+entity.getIdSqlPattern()+"' AND host LIKE '"+entity.getHostSqlPattern()+"' AND user LIKE '"+entity.getUserSqlPattern()+"' AND process LIKE '"+entity.getProcessSqlPattern()+"' AND active LIKE '"+entity.getActiveSqlPattern()+"' AND exitcode LIKE '"+entity.getExitcodeSqlPattern()+"' AND resource LIKE '"+entity.getResourceSqlPattern()+"'";
//			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
		
			while (rs.next())
			{
				Entity matched_entity = new Entity();
				matched_entity.setId(rs.getString("id"));
				matched_entity.setProcess(rs.getString("process"));
				matched_entity.setUser(rs.getString("user"));
				matched_entity.setHost(rs.getString("host"));
				matched_entity.setCheckin(Long.valueOf(rs.getString("checkin")).longValue());
				matched_entity.setCheckout(Long.valueOf(rs.getString("checkout")).longValue());
				matched_entity.setActive(rs.getString("active"));
				matched_entity.setExitcode(rs.getString("exitcode"));
				matched_entity.setResource(rs.getString("resource"));
				matches.add(matched_entity);
			}
			
			} catch (SQLException e)
		{
			// TODO Auto-generated catch block
//			System.err.println("cannot talk to database.");
			e.printStackTrace();
		}
		return matches;
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
			
			ArrayList<Entity> matched_entities = new ArrayList<Entity>();
			try
			{
				matched_entities = this.match(entity);
			}
			catch (NullPointerException e)
			{
				System.out.println("result is empty!");
			}
			
			String formatstring = "|%-14s|%-11s|%-7s|%-13s|%-6s|%-19s|%-19s|%-8s|\n";
			System.out.format(formatstring, "id", "process", "user", "host", "active", "checkin", "checkout", "exitcode");
			System.out.format(formatstring, "--------------", "-----------", "-------", "-------------", "------", "-------------------", "-------------------", "--------");

			Iterator<Entity> iterentity = matched_entities.iterator();
			
			while (iterentity.hasNext())
			{
				Entity actual_entity = iterentity.next();
				System.out.format(formatstring, actual_entity.getId(), actual_entity.getProcess(), actual_entity.getUser(), actual_entity.getHost(), actual_entity.getActive(), actual_entity.getCheckinAsString(), actual_entity.getCheckoutAsString(), actual_entity.getExitcode() );
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
	
	public void getConnection() throws SQLException
	{
		this.connection = null;
		try
		{
			if (!(this.dbfile.exists()))
			{
				System.err.println("database file does not exist: "+this.dbfile.getAbsolutePath());
				System.exit(1);
			}
		}
		catch (Exception e)
		{
			System.err.println("database file does not exist.");
		}

		try
		{
			this.connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
		}
		catch (SQLException e)
		{
			System.err.println("cannot connect to database: "+this.dbfile.getAbsolutePath());
			System.exit(1);
//				e.printStackTrace();
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
