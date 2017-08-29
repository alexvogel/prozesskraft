package de.prozesskraft.pradar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
	public String id2 = "";
	public String pid = "";
	public String parentid = "";
	public Calendar checkin = Calendar.getInstance();
	public Calendar checkout = Calendar.getInstance();
	public String process = "";
	public String version = "";
	public String host = "";
	public String user = "";
	public String active = "";
	public String stepcount = "";
	public String stepcountcompleted = "";
	public String exitcode = "";
	public String resource = "";
	public String serialVersionUID = "";

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

		for(Entity actEntity : allEntities)
		{
			if (this.doesItMatch(actEntity))
			{
				allMatches.add(actEntity);
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
//		System.out.println("tested entity id:"+assessedEntity.getId());
		boolean matchStatus = true;

		if (!(this.id.equals("") ) && (!(assessedEntity.getId().matches(Pattern.quote(this.id)))) )
		{
//			System.out.println("id does not match");
			matchStatus = false;
		}
		
		if (!(this.id2.equals("") ) && (!(assessedEntity.getId().matches(Pattern.quote(this.id2)))) )
		{
//			System.out.println("id2 does not match");
			matchStatus = false;
		}
		
		if (!(this.process.equals("") ) && (!(assessedEntity.getProcess().matches(Pattern.quote(this.process)))) )
		{
			matchStatus = false;
//			System.out.println("process does not match");
		}
		
		if (!(this.host.equals("") ) && (!(assessedEntity.getHost().matches(Pattern.quote(this.host)))) )
		{
			matchStatus = false;
		}
		
		if (!(this.user.equals("") ) && (!(assessedEntity.getUser().matches(Pattern.quote(this.user)))) )
		{
			matchStatus = false;
//			System.out.println("user does not match");
		}
		
		if (!(this.active.equals("") ) && (!(assessedEntity.getActive().matches(Pattern.quote(this.active)))) )
		{
			matchStatus = false;
//			System.out.println("active does not match");
		}
		
		if (!(this.exitcode.equals("") ) && (!(assessedEntity.getExitcode().matches(Pattern.quote(this.exitcode)))) )
		{
			matchStatus = false;
//			System.out.println("exitcode does not match");
		}
		
		if (!(this.resource.equals("") ) && (!(assessedEntity.getResource().matches(Pattern.quote(this.resource)))) )
		{
			matchStatus = false;
//			System.out.println("resource does not match");
		}
		
		if (!(this.serialVersionUID.equals("") ) &&
			(!(assessedEntity.getSerialVersionUID().matches(Pattern.quote(this.serialVersionUID)))) )
		{
			matchStatus = false;
//			System.out.println("resource does not match");
		}
		
		// wenn es dem pattern nicht entspricht UND nicht "" ist (leer)
		if ( (!(assessedEntity.getParentid().matches(this.parentid))) && (!(this.parentid.equals(""))) )
		{
			matchStatus = false;
//			System.out.println("parentid does not match");
		}
		
		if ( (this.getPeriodInMillis() < (Calendar.getInstance().getTimeInMillis() - assessedEntity.getCheckinInMillis())) && (this.getPeriodInMillis() < (Calendar.getInstance().getTimeInMillis() - assessedEntity.getCheckoutInMillis()) && (assessedEntity.getCheckoutInMillis() != 0) ) )
		{
//			System.out.println("CheckinInMillis="+assessedEntity.getCheckinInMillis()+" < (now="+Calendar.getInstance().getTimeInMillis()+" MINUS timePeriod="+this.getPeriodInMillis()+")" );
//			System.out.println("time does not match");
			matchStatus = false;
		}
//		System.out.println("matchStatus is: "+matchStatus);
		
		return matchStatus;
	}
	
	/**
	 * prints all fields to stdout
	 */
	public void print()
	{
		this.toString();
		System.out.println("id:      "+this.id);
		System.out.println("id2:     "+this.id2);
		System.out.println("parentid:"+this.parentid);
		System.out.println("process: "+this.process);
		System.out.println("user:    "+this.user);
		System.out.println("host:    "+this.host);
		System.out.println("active:  "+this.active);
		System.out.println("checkin: "+this.getCheckinAsString());
		System.out.println("checkout:"+this.getCheckoutAsString());
		System.out.println("stepcount:"+this.getStepcount());
		System.out.println("stepcountcompleted:"+this.getStepcountcompleted());
		System.out.println("exitcode:"+this.exitcode);
		System.out.println("resource:"+this.resource);
		System.out.println("serialVersionUID:"+this.serialVersionUID);
		System.out.println("period:  "+this.resource);
	}

	/**
	 * determines whether host is reachable via ssh
	 * @return boolean
	 */
	public boolean isHostReachable(String sshIdRelPath)
	{
		boolean reachable = false;
		
		String sshIdAbsPath = System.getProperty("user.home")+"/"+sshIdRelPath;
		System.out.println("using ssh-id-rsa: "+sshIdAbsPath);
		
		try
		{
			JSch jsch = new JSch();
			jsch.addIdentity(sshIdAbsPath);
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
	public boolean isInstanceAlive(String sshIdRelPath)
	{
		String sshIdAbsPath = System.getProperty("user.home")+"/"+sshIdRelPath;
		System.out.println("using ssh-id-rsa: "+sshIdAbsPath);

		boolean alive = false;
//		Pattern patternPsLinux = Pattern.compile("^ *(\\d+) +[^ ]+ +[^ ]+ +(.+)$");
		Pattern patternPsLinux = Pattern.compile("^ *(\\d+) +.+$");

		try
		{
			JSch jsch = new JSch();

			jsch.addIdentity(sshIdAbsPath);

			Session session = jsch.getSession(System.getProperty("user.name"), this.host, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			
			ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

			InputStream in = channelExec.getInputStream();
			String command = "ps -p "+this.getPid();
			
			channelExec.setCommand(command);

//			System.out.println("setting command to: "+command);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			String line;
			
			while((line = reader.readLine()) != null)
			{
				System.out.println(line);
				Matcher matcher = patternPsLinux.matcher(line);
				while(matcher.find())
				{
					System.err.println("PID gesucht: " + this.getPid());
					System.err.println("PID gefunden: " + matcher.group(1));
					
					if (matcher.group(1).equals(this.getPid()))
					{
						System.out.println("PID gefunden: "+matcher.group(1));
						alive = true;
						System.out.println("Prozess mit id " + this.getPid() + " auf maschine " + this.getHost() + " gefunden! ");
//						if (matcher.group(2).matches(this.getProcess()+".+"))
//						{
//							alive = true;
//							System.out.println("Prozessname '"+matcher.group(2)+"' stimmt auch weitgehend (ideal waehre '"+this.getProcess()+"'. jetzt gilt er als erkannt!");
//						}
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

	public String getId2()
	{
		return id2;
	}

	public String getId2SqlPattern()
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
	
	public void setId2(String id2)
	{
		this.id2 = id2;
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
	
	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getVersion()
	{
		return version;
	}

	public String getVersionSqlPattern()
	{
		if ((this.version.matches("")) || (this.version.matches("all")) )
		{
			return "%";
		}
		else
		{
			return "%"+this.version+"%";
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

	public String getStepcount()
	{
		return this.stepcount;
	}

	public void setStepcount(String stepcount)
	{
		this.stepcount = stepcount;
	}

	public String getStepcountcompleted()
	{
		return this.stepcountcompleted;
	}

	public void setStepcountcompleted(String stepcountcompleted)
	{
		this.stepcountcompleted = stepcountcompleted;
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
	
	public float getProgress()
	{
		if(this.stepcount.equals("") ||  this.stepcountcompleted.equals(""))
		{
			return -1;
		}
		else
		{
			return ((float)Integer.parseInt(this.stepcountcompleted) / (float)Integer.parseInt(this.stepcount));
		}
	}

	public String getProgressAsString()
	{
		float progress = getProgress();
		if(progress < 0)
		{
			return "unknown";
		}
		else
		{
			return ((int)(progress * 100)) + "% ("+this.stepcountcompleted+"/"+this.stepcount+")";
		}
	}

	/**
	 * @return the pid
	 */
	public String getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	/**
	 * @return the serialVersionUID
	 */
	public String getSerialVersionUID() {
		return serialVersionUID;
	}

	/**
	 * @param serialVersionUID the serialVersionUID to set
	 */
	public void setSerialVersionUID(String serialVersionUID) {
		this.serialVersionUID = serialVersionUID;
	}

	public String getSerialVersionUIDSqlPattern()
	{
		if (this.serialVersionUID.matches("all") || this.serialVersionUID.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.serialVersionUID+"%";
		}
	}

}
