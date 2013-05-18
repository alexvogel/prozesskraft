package de.caegroup.pradar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class Entity
implements Serializable
{
	/*----------------------------
	  structure
	----------------------------*/

	public String id = "";
	public String parentid = "0";
	public Calendar checkin = Calendar.getInstance();
	public Calendar checkout = Calendar.getInstance();
	public String process = "";
	public String host = "";
	public String user = "";
	public String active = "";
	public String exitcode = "";
	public String resource = "";
	public long period = 9999999999999L;

	/*----------------------------
	  constructors
	----------------------------*/

	public Entity()
	{

	}

	public Entity(String id)
	{
		this.id = id;
	}

	/*----------------------------
	  methods
	----------------------------*/

	/**
	 * generates an id (=currentTimeInMillis) 
	 */
	public void genId()
	{
		long time = System.currentTimeMillis();
//		String time_as_string = "" + time;
		this.setId(""+time);
	}

	/**
	 * matches itself (this) against every item of the given ArrayList<Entity>
	 * @param ArrayList<Entity>
	 * @return ArrayList<Entity> of matched Entities
	 */
	public ArrayList<Entity> getAllMatches(ArrayList<Entity> allEntities)
	{
		ArrayList<Entity> allMatches = new ArrayList<Entity>();
		
		Iterator<Entity> iterEntity = allEntities.iterator();
		while (iterEntity.hasNext())
		{
			Entity entity = iterEntity.next();
			if (this.doesItMatch(entity))
			{
				allMatches.add(entity);
			}
		}

		return allMatches;
	}
	
	/**
	 * matches itself (this) against the given Entity (assessedEntity)
	 * @param Entity
	 * @return 'true' if all fields match
	 * 'false' if one or more fields do not match
	 */
	private boolean doesItMatch(Entity assessedEntity)
	{
		boolean matchStatus = true;
		if (!(assessedEntity.getId().matches(".*"+this.id+".*")))
		{
			matchStatus = false;
		}
		
		if (!(assessedEntity.getProcess().matches(".*"+this.process+".*")))
		{
			matchStatus = false;
		}
		
		if (!(assessedEntity.getHost().matches(".*"+this.host+".*")))
		{
			matchStatus = false;
		}
		
		if (!(assessedEntity.getUser().matches(".*"+this.user+".*")))
		{
			matchStatus = false;
		}
		
		if (!(assessedEntity.getActive().matches(".*"+this.active+".*")))
		{
			matchStatus = false;
		}
		
		if (!(assessedEntity.getExitcode().matches(".*"+this.exitcode+".*")))
		{
			matchStatus = false;
		}
		
		if (!(assessedEntity.getResource().matches(".*"+this.resource+".*")))
		{
			matchStatus = false;
		}
		
		// wenn es dem pattern nicht entspricht UND nicht "" ist (leer)
		if ( (!(assessedEntity.getParentid().matches(this.parentid))) && (!(this.parentid.equals(""))) )
		{
			matchStatus = false;
		}
		
		if ( (this.getPeriodInMillis() < (Calendar.getInstance().getTimeInMillis() - assessedEntity.getCheckinInMillis())) && (this.getPeriodInMillis() < (Calendar.getInstance().getTimeInMillis() - assessedEntity.getCheckoutInMillis()) && (assessedEntity.getCheckoutInMillis() != 0) ) )
		{
//			System.out.println("CheckinInMillis="+assessedEntity.getCheckinInMillis()+" < (now="+Calendar.getInstance().getTimeInMillis()+" MINUS timePeriod="+this.getPeriodInMillis()+")" );
			matchStatus = false;
		}
		
		return matchStatus;
	}
	
	/**
	 * prints all fields to stdout
	 */
	public void print()
	{
		this.toString();
		System.out.println("id:      "+this.id);
		System.out.println("parentid:"+this.parentid);
		System.out.println("process: "+this.process);
		System.out.println("user:    "+this.user);
		System.out.println("host:    "+this.host);
		System.out.println("active:  "+this.active);
		System.out.println("checkin: "+this.getCheckinAsString());
		System.out.println("checkout:"+this.getCheckoutAsString());
		System.out.println("exitcode:"+this.exitcode);
		System.out.println("resource:"+this.resource);
		System.out.println("period:  "+this.resource);
	}

	/**
	 * determines whether host is reachable via ssh
	 * @return boolean
	 */
	public boolean isHostReachable()
	{
		boolean reachable = false;
		
		try
		{
			JSch jsch = new JSch();
			jsch.addIdentity(".ssh/id_rsa");
			Session session = jsch.getSession(System.getProperty("user.name"), this.host, 22);
//			session.setPassword("salutner1");
//			System.out.println("establishing connection...");

			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();

//			System.out.println("connection established");
			
//			Channel channel = session.openChannel("exec");
//			channel.connect();

//			System.out.println("channel 'exec' connection established");

			reachable = true;
			session.disconnect();

		} catch (JSchException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("host "+this.getHost()+" not reachable");
		}
		
		return reachable;
	}

	/**
	 * determines whether PID is alive on host
	 * @return boolean
	 */
	public boolean isInstanceAlive()
	{
		boolean alive = false;
		Pattern patternPsLinux = Pattern.compile("^ *(\\d+) +[^ ]+ +[^ ]+ +(.+)$");

		try
		{
			JSch jsch = new JSch();

			jsch.addIdentity(".ssh/id_rsa");

			Session session = jsch.getSession(System.getProperty("user.name"), this.host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream in = channelExec.getInputStream();
			String command = "ps -p "+this.getId();
			
			channelExec.setCommand(command);

//			System.out.println("setting command to: "+command);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			String line;
			
			while((line = reader.readLine()) != null)
			{
//				System.out.println(line);
				Matcher matcher = patternPsLinux.matcher(line);
				while(matcher.find())
				{
					if (matcher.group(1).equals(this.getId()))
					{
//						System.out.println("PID gefunden: "+matcher.group(1));
						if (matcher.group(2).matches(this.getProcess()+".+"))
						{
							alive = true;
//							System.out.println("Prozessname '"+matcher.group(2)+"' stimmt auch weitgehend (ideal waehre '"+this.getProcess()+"'. jetzt gilt er als erkannt!");
						}
//						else
//						{
//							System.out.println("Prozessname'"+matcher.group(2)+"' stimmt nicht mit gesuchtem  '"+this.getProcess()+"'-> weitersuchen");
//						}
					}
				}
			}

//			int exitStatus = channelExec.getExitStatus();
//			System.out.println("exitstatus of jsch-connection: "+exitStatus);

			channelExec.disconnect();
			session.disconnect();
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return alive;
	}

	/*----------------------------
	  methods getter/setter
	----------------------------*/

	public String getActualuser()
	{
		return System.getProperty("user.name");
	}

	public String getSuperid()
	{
		return (this.id + this.checkin.getTimeInMillis());
	}

	public String getId()
	{
		return id;
	}

	public String getParentid()
	{
		return this.parentid;
	}

	public void setParentid(String parentid)
	{
		this.parentid = parentid;
	}

	public void setParentidAsBoolean(boolean children)
	{
		if (children)
		{
			this.parentid = "";
		}
		else
		{
			this.parentid = "0";
		}
	}

	public String getParentidSqlPattern()
	{
		if (this.parentid.matches("all") || this.host.matches(""))
		{
			return "%";
		}
		else
		{
			return this.parentid;
		}
	}
	
	public String getIdSqlPattern()
	{
		if (!(this.id.isEmpty()))
		{
			return "%"+this.id+"%";
		}
		else
		{
			return "%";
		}
	}
	
	public void setId(String id)
	{
		this.id = id;
	}

	public Calendar getCheckin()
	{
		return checkin;
	}

	public long getCheckinInMillis()
	{
		return this.checkin.getTimeInMillis();
	}

	public String getCheckinAsString()
	{
		Timestamp timestamp = new Timestamp(this.checkin.getTimeInMillis());
		String string_timestamp = timestamp.toString();
		String beschnittener_string_timestamp = string_timestamp.substring(0, 19);
		return beschnittener_string_timestamp;
	}

	public void setCheckin(Calendar checkin)
	{
		this.checkin = checkin;
	}

	public void setCheckin(long timeInMillis)
	{
		this.checkin.setTimeInMillis(timeInMillis);
	}

	public Calendar getCheckout()
	{
		return this.checkout;
	}

	public long getCheckoutInMillis()
	{
		return this.checkout.getTimeInMillis();
	}

	public String getCheckoutAsString()
	{
		if (this.checkout.getTimeInMillis() == 0) {return "";}
		else
		{
			Timestamp timestamp = new Timestamp(this.checkout.getTimeInMillis());
			String string_timestamp = timestamp.toString();
			String beschnittener_string_timestamp = string_timestamp.substring(0, 19);
			return beschnittener_string_timestamp;
		}
	}

	public void setCheckout(Calendar checkout)
	{
		this.checkout = checkout;
	}

	public void setCheckout(long timeInMillis)
	{
		this.checkout.setTimeInMillis(timeInMillis);
	}

	public String getProcess()
	{
		return process;
	}

	public String getProcessSqlPattern()
	{
		if ((this.process.matches("")) || (this.process.matches("all")) )
		{
			return "%";
		}
		else
		{
			return "%"+this.process+"%";
		}
	}
	
	public void setProcess(String process)
	{
		this.process = process;
	}

	public String getHost()
	{
		return host;
	}

	public String getHostSqlPattern()
	{
		if (this.host.matches("all") || this.host.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.host+"%";
		}
	}
	
	public void setHost(String host)
	{
		this.host = host;
	}

	public String getUser()
	{
		return user;
	}

	public String getUserSqlPattern()
	{
		if (this.user.matches("all") || this.user.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.user+"%";
		}
	}
	
	public void setUser(String user)
	{
		this.user = user;
	}

	public String getActive()
	{
		return active;
	}

	public boolean isActive()
	{
		if (this.active.equals("true"))
		{
			return true;
		}
		return false;
	}

	public String getActiveSqlPattern()
	{
		if (this.active.matches("all") || this.active.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.active+"%";
		}
	}

	public void setActive(String active)
	{
		this.active = active;
	}

	public void setActive(boolean active)
	{
		if (active)
		{
			this.active = "true";
		}
		else
		{
			this.active = "false";
		}
	}

	public String getExitcode()
	{
		return this.exitcode;
	}

	public String getExitcodeSqlPattern()
	{
		if (this.exitcode.matches("all") || this.exitcode.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.exitcode+"%";
		}
	}

	public void setExitcode(String exitcode)
	{
		this.exitcode = exitcode;
	}

	public String getResource()
	{
		return this.resource;
	}

	public String getResourceSqlPattern()
	{
		if (this.resource.matches("all") || this.resource.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.resource+"%";
		}
	}

	public void setResource(String resource)
	{
		this.resource = resource;
	}

	public long getPeriodInMillis()
	{
		return this.period;
	}

	public int getPeriodInHours()
	{
		 return (int)(this.period/3600000);
	}

	public void setPeriodInMillis(long millis)
	{
		this.period = millis;
	}

	public void setPeriodInHours(int hours)
	{
		this.period = (long)((long)hours*3600000);
	}
	
	
}
