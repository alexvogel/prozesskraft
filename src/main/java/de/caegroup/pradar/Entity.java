package de.caegroup.pradar;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class Entity
{
	/*----------------------------
	  structure
	----------------------------*/

	public String id = "";
	public String parentid = "";
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

	public void genId()
	{
		long time = System.currentTimeMillis();
//		String time_as_string = "" + time;
		this.setId(""+time);
	}

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
	
	public boolean doesItMatch(Entity assessedEntity)
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
		
		if ( (this.getPeriodInMillis() < (Calendar.getInstance().getTimeInMillis() - assessedEntity.getCheckinInMillis())) && (this.getPeriodInMillis() < (Calendar.getInstance().getTimeInMillis() - assessedEntity.getCheckoutInMillis()) && (assessedEntity.getCheckoutInMillis() != 0) ) )
		{
//			System.out.println("CheckinInMillis="+assessedEntity.getCheckinInMillis()+" < (now="+Calendar.getInstance().getTimeInMillis()+" MINUS timePeriod="+this.getPeriodInMillis()+")" );
			matchStatus = false;
		}
		
		return matchStatus;
	}
	
	/*----------------------------
	  methods getter/setter
	----------------------------*/

	public long getTimeInMillisOfNow()
	{
		Calendar tmp = Calendar.getInstance();
		return tmp.getTimeInMillis();
	}

	public void print()
	{
		this.toString();
		System.out.println("id: "+this.id);
		System.out.println("process: "+this.process);
		System.out.println("user: "+this.user);
		System.out.println("host: "+this.host);
		System.out.println("active: "+this.active);
		System.out.println("checkin: "+this.getCheckinAsString());
		System.out.println("checkout: "+this.getCheckoutAsString());
		System.out.println("exitcode: "+this.exitcode);
	}

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

	/**
	 * @return the parentid
	 */
	public String getParentid()
	{
		return this.parentid;
	}

	/**
	 * @param parentid the parentid to set
	 */
	public void setParentid(String parentid)
	{
		this.parentid = parentid;
	}

	public String getParentidSqlPattern()
	{
		if (this.parentid.matches("all") || this.host.matches(""))
		{
			return "%";
		}
		else
		{
			return "%"+this.parentid+"%";
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
