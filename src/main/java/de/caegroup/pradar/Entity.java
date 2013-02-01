package de.caegroup.pradar;

import java.util.Calendar;

public class Entity
{
	/*----------------------------
	  structure
	----------------------------*/
	
	public String id = new String();
	public Calendar checkin = Calendar.getInstance();
	public Calendar checkout = Calendar.getInstance();
	public String processname = "worldpower";
	public String host = "HAL";
	public String user = "johndoe";
	public boolean restart = false;
	
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
		String time_as_string = "" + time;
		this.setId(time_as_string);
	}

	/*----------------------------
	  methods getter/setter
	----------------------------*/

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Calendar getCheckin()
	{
		return checkin;
	}

	public void setCheckin(Calendar checkin)
	{
		this.checkin = checkin;
	}

	public Calendar getCheckout()
	{
		return checkout;
	}

	public void setCheckout(Calendar checkout)
	{
		this.checkout = checkout;
	}

	public String getProcessname()
	{
		return processname;
	}

	public void setProcessname(String processname)
	{
		this.processname = processname;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public boolean isRestart()
	{
		return restart;
	}

	public void setRestart(boolean restart)
	{
		this.restart = restart;
	}
}
