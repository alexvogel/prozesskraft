package de.caegroup.pradar;

import java.sql.Timestamp;
import java.util.Calendar;

public class Entity
{
	/*----------------------------
	  structure
	----------------------------*/

	public String id = "";
	public Calendar checkin = Calendar.getInstance();
	public Calendar checkout = Calendar.getInstance();
	public String process = "";
	public String host = "";
	public String user = "";
	public String active = "";
	public String exitcode = "";
	public String resource = "";

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
		return (this.id + this.process + this.host + this.user);
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
		return new Timestamp(this.checkin.getTimeInMillis()).toString();
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
		else {return new Timestamp(this.checkout.getTimeInMillis()).toString();}
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
		if (!(this.process.isEmpty()) || (this.process.matches("all")) )
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

	public void setResource(String resource)
	{
		this.resource = resource;
	}

}
