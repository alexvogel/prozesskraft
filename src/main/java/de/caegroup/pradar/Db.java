package de.caegroup.pradar;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.sql.*;
import de.caegroup.commons.*;

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
	 * constructor
	 * the dbfile is set to be at the default position
	 * default position is relative to installation position
	 * <installationdir>/../../../../data/pradar/pradar.db
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
	}

	/**
	 * constructor
	 * @param String
	 */
	public Db(String pathtofile)
	{
		setDbfile(new File(pathtofile));
	}

	/**
	 * constructor
	 * @param File
	 */
	public Db(File dbfile)
	{
		setDbfile(dbfile);
	}

	/*----------------------------
	  methods
	----------------------------*/
	
	/**
	 * creating in database a table with name 'radar'
	 * creating in table 'radar' a column for every field of object 'Entity'
	 */
	public void initForceDb()
	{
		this.sqlvoodoo();
		this.connection = null;
		try
		{
//			System.out.println("getting connection to: "+this.dbfile.getAbsolutePath());
			connection = DriverManager.getConnection("jdbc:sqlite:"+this.dbfile.getAbsolutePath());
			
			Statement statement = connection.createStatement();
			
			statement.setQueryTimeout(10);
			
			statement.executeUpdate("drop table if exists radar");
			statement.executeUpdate("create table radar (id TEXT, parentid TEXT, process TEXT, host TEXT, user TEXT, checkin TEXT, checkout TEXT, active TEXT, exitcode TEXT, resource TEXT)");
			
			this.connection.close();

		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * checking whether dbfile exists. if no, create directories and initForceDb()
	 * creating in database a table with name 'radar'
	 * creating in table 'radar' a column for every field of object 'Entity'
	 */
	public void initDb()
	{
		if (!(this.getDbfile().exists()))
		{
			this.getDbfile().mkdirs();
			initForceDb();
		}
	}

	/**
	 * creating in database in table 'radar' a row with content of Object 'Entity'
	 * setting column 'checkin' to currentTimeInMillis
	 * @param Entity
	 */
	public void checkinEntity(Entity entity)
	{
		this.sqlvoodoo();
		this.connection = null;
		try
		{
			this.getConnection();
			Statement statement = this.connection.createStatement();

			statement.setQueryTimeout(10);
			String sql = "INSERT INTO radar (id, parentid, process, host, user, checkin, checkout, active, exitcode, resource) VALUES ('"+entity.getId()+"', '"+entity.getParentid()+"', '"+entity.getProcess()+"', '"+entity.getHost()+"', '"+entity.getUser()+"', '"+entity.getCheckin().getTimeInMillis()+"', '0', '"+entity.getActive()+"', '"+entity.getExitcode()+"', '"+entity.getResource()+"')"; 
//			System.out.println(sql);
			statement.executeUpdate(sql);
			
			this.connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * setting in database in table 'radar' column 'checkout' to currentTimeInMillis
	 * @param Entity
	 */
	public void checkoutEntity(Entity entity)
	{
		this.sqlvoodoo();
		this.connection = null;
		try
		{
			this.getConnection();
			Statement statement = this.connection.createStatement();
			
			statement.setQueryTimeout(10);
			
			String sql = "UPDATE OR REPLACE radar SET checkout='"+System.currentTimeMillis()+"', active='false', exitcode='"+ entity.getExitcode() +"' WHERE id IS '"+entity.getId()+"' AND host IS '"+entity.getHost()+"' AND user IS '"+entity.getUser()+"' AND process IS '"+entity.getProcess()+"' AND active IS 'true'";
//			System.out.println(sql);
			statement.executeUpdate(sql);
			
			this.connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * matches an 'Entity' against the table 'radar' in database
	 * @param Entity
	 * @return ArrayList<Entity> of all rows of table 'radar' which match.
	 */
	public ArrayList<Entity> match(Entity entity)
	{
		
		// kalkulieren der grenzzeit in millisekunden
		long now = Calendar.getInstance().getTimeInMillis();
		long grenzzeitInMillis = now - entity.getPeriodInMillis();
		
		ArrayList<Entity> matches = new ArrayList<Entity>();
		this.sqlvoodoo();
		this.connection = null;
		try
		{
			this.getConnection();
			Statement statement = this.connection.createStatement();
			statement.setQueryTimeout(10);
			String sql = "SELECT * FROM radar WHERE id LIKE '"+entity.getIdSqlPattern()+"' AND parentid LIKE '"+entity.getParentidSqlPattern()+"' AND host LIKE '"+entity.getHostSqlPattern()+"' AND user LIKE '"+entity.getUserSqlPattern()+"' AND process LIKE '"+entity.getProcessSqlPattern()+"' AND active LIKE '"+entity.getActiveSqlPattern()+"' AND exitcode LIKE '"+entity.getExitcodeSqlPattern()+"' AND resource LIKE '"+entity.getResourceSqlPattern()+"' AND ( checkin > '"+grenzzeitInMillis+"' OR checkout > '"+grenzzeitInMillis+"')";
//			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
		
			while (rs.next())
			{
				Entity matched_entity = new Entity();
				matched_entity.setId(rs.getString("id"));
				matched_entity.setParentid(rs.getString("parentid"));
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
			this.connection.close();
		}
		catch (Exception e)
		{
			System.err.println("no processes.");
		}

		return matches;
	}
	
	/**
	 * get all rows of table 'radar'
	 * @return ArrayList<Entity> of all rows of table 'radar'.
	 */
	public ArrayList<Entity> getAllEntities()
	{
		ArrayList<Entity> allEntities = new ArrayList<Entity>();
		this.sqlvoodoo();
		this.connection = null;
		try
		{
			this.getConnection();
			Statement statement = this.connection.createStatement();

			statement.setQueryTimeout(10);
			String sql = "SELECT * FROM radar";
//			System.out.println(sql);
			ResultSet rs = statement.executeQuery(sql);
		
			while (rs.next())
			{
				Entity matched_entity = new Entity();

//				System.out.println(rs.getString("id"));
				matched_entity.setId(rs.getString("id"));
				
//				System.out.println(rs.getString("parentid"));
				matched_entity.setParentid(rs.getString("parentid"));
				
//				System.out.println(rs.getString("process"));
				matched_entity.setProcess(rs.getString("process"));
				
//				System.out.println(rs.getString("user"));
				matched_entity.setUser(rs.getString("user"));
				
//				System.out.println(rs.getString("host"));
				matched_entity.setHost(rs.getString("host"));
				
				String checkin = rs.getString("checkin");
				if (checkin.matches("")) {checkin = "0";}
//				System.out.println(rs.getString("checkin"));
				matched_entity.setCheckin(Long.valueOf(checkin).longValue());
				
				String checkout = rs.getString("checkout");
				if (checkout.matches("")) {checkout = "0";}
//				System.out.println(rs.getString("checkout"));
				matched_entity.setCheckout(Long.valueOf(checkout).longValue());

//				System.out.println(rs.getString("active"));
				matched_entity.setActive(rs.getString("active"));
				
//				System.out.println(rs.getString("exitcode"));
				matched_entity.setExitcode(rs.getString("exitcode"));
				
//				System.out.println(rs.getString("resource"));
				matched_entity.setResource(rs.getString("resource"));
				
				allEntities.add(matched_entity);
			}
			this.connection.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("no processes.");
		}

		return allEntities;
	}
	
	/**
	 * print all rows to stdout of table 'radar' which match the Entity
	 * @param Entity
	 */
	public ArrayList<String> list(Entity entity)
	{
		ArrayList<String> ausgabe = new ArrayList<String>();
		this.sqlvoodoo();
		this.connection = null;
		try
		{
			this.getConnection();

//			Statement statement = this.connection.createStatement();
//			statement.setQueryTimeout(10);

			
			ArrayList<Entity> matched_entities = new ArrayList<Entity>();
			try
			{
				matched_entities = this.match(entity);
			}
			catch (NullPointerException e)
			{
				System.out.println("result is empty!");
			}
			
			String formatstring = "|%-14s|%-14s|%-11s|%-7s|%-13s|%-6s|%-19s|%-19s|%-8s|";

			ausgabe.add(String.format(formatstring, "id", "parentid", "process", "user", "host", "active", "checkin", "checkout", "exitcode"));
			ausgabe.add(String.format(formatstring, "--------------", "--------------", "-----------", "-------", "-------------", "------", "-------------------", "-------------------", "--------"));

			Iterator<Entity> iterentity = matched_entities.iterator();
			
			while (iterentity.hasNext())
			{
				Entity actual_entity = iterentity.next();
				ausgabe.add(String.format(formatstring, actual_entity.getId(), actual_entity.getParentid(), actual_entity.getProcess(), actual_entity.getUser(), actual_entity.getHost(), actual_entity.getActive(), actual_entity.getCheckinAsString(), actual_entity.getCheckoutAsString(), actual_entity.getExitcode() ));
			}

			this.connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ausgabe;
	}

	/**
	 * cleans Db: checks out all active instances where
	 * 1) host is reachable, but pid is no longer active
	 * 2) host is unreachable and instance is older than 1 week
	 * 
	 */
	public void cleanDb(String sshIdRelPath)
	{
		this.sqlvoodoo();
		this.connection = null;
		try
		{
			this.getConnection();

			Statement statement = this.connection.createStatement();
			statement.setQueryTimeout(10);
			
			ArrayList<Entity> all_entities = new ArrayList<Entity>();
			try
			{
				all_entities = this.getAllEntities();
			}
			catch (NullPointerException e)
			{
				System.out.println("result is empty!");
			}
			
			Iterator<Entity> iterentity = all_entities.iterator();
			
			while (iterentity.hasNext())
			{
				Entity actualEntity = iterentity.next();
				if (actualEntity.isActive())
				{
					if (actualEntity.isHostReachable(sshIdRelPath))
					{
						System.out.println("host "+actualEntity.getHost()+" is reachable");
						
						if (!(actualEntity.isInstanceAlive(sshIdRelPath)))
						{
							System.out.println("instance appears to be vanished: pid "+actualEntity.getId());
							String sql = "UPDATE OR REPLACE radar SET checkout='"+System.currentTimeMillis()+"', active='false', exitcode='? (forced checkout)' WHERE id IS '"+actualEntity.getId()+"' AND host IS '"+actualEntity.getHost()+"' AND user IS '"+actualEntity.getUser()+"' AND process IS '"+actualEntity.getProcess()+"' AND active IS 'true'";
//							System.out.println(sql);
//							statement.execute(sql);
							statement.executeUpdate(sql);
						}
						else
						{
							System.out.println("instance "+actualEntity.getId()+" is still alive");
						}

					}
					// sonst wenn aelter als eine Woche -> forced checkout
					else if ( (actualEntity.getCheckinInMillis() - System.currentTimeMillis()) > 604800000 )
					{
						System.out.println("host is unreachable and instance is older than 1 week.");
						String sql = "UPDATE OR REPLACE radar SET checkout='"+System.currentTimeMillis()+"', active='false', exitcode='? (forced checkout)' WHERE id IS '"+actualEntity.getId()+"' AND host IS '"+actualEntity.getHost()+"' AND user IS '"+actualEntity.getUser()+"' AND process IS '"+actualEntity.getProcess()+"' AND active IS 'true'";
//						System.out.println(sql);
						statement.executeUpdate(sql);
					}
				}
			}

			this.connection.close();
		} catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sqlvoodoo()
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
	
	private void getConnection()
	{
		this.connection = null;
		try
		{
			if (!(this.dbfile.exists()))
			{
				System.err.println("database file does not exist: "+this.dbfile.getAbsolutePath());
//				System.exit(1);
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
//			System.exit(1);
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

	public void setDbfile(String path)
	{
		File dbfile = new File(path);
		this.dbfile = dbfile;
	}
}
