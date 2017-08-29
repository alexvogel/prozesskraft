package de.prozesskraft.commons;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
public class DistantHostActions {
  
	
	public static boolean isHostReachable(String host)
	{
		return isHostReachable(getDefaultSshIdRsa(), host);
	}
	
	/**
	 * determines whether host is reachable via ssh
	 * @return boolean
	 */
	public static boolean isHostReachable(String sshIdRelPath, String host)
	{
		boolean reachable = false;
		
		String sshIdAbsPath = System.getProperty("user.home")+"/"+sshIdRelPath;
		System.out.println("using ssh-id-rsa: "+sshIdAbsPath);

		try
		{
			JSch jsch = new JSch();
			jsch.addIdentity(getDefaultSshIdRsa());
//			jsch.addIdentity(sshIdAbsPath);
			Session session = jsch.getSession(System.getProperty("user.name"), host, 22);
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
			System.err.println("host "+host+" not reachable");
		}
		
		return reachable;
	}

	/**
	 * executes a call on distant host
	 * @return boolean
	 * @throws JSchException 
	 */
	public static void sysCallOnDistantHost(String host, String sysCall) throws JSchException
	{
		sysCallOnDistantHost(getDefaultSshIdRsa(), host, sysCall);
		
	}
	
	/**
	 * executes a call on distant host
	 * @return boolean
	 * @throws JSchException 
	 */
	public static void sysCallOnDistantHost(String sshIdRelPath, String host, String sysCall) throws JSchException
	{
		String sshIdAbsPath = System.getProperty("user.home")+"/"+sshIdRelPath;
		System.out.println("using ssh-id-rsa: "+sshIdAbsPath);

		JSch jsch = new JSch();
	
		jsch.addIdentity(sshIdAbsPath);
	
		Session session = jsch.getSession(System.getProperty("user.name"), host, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		
		ChannelExec channelExec = (ChannelExec)session.openChannel("exec");
		channelExec.setCommand(sysCall);
	
		//System.out.println("setting command to: "+command);
		channelExec.connect();
	
		channelExec.disconnect();
		session.disconnect();
		
	}
	
	public static int getDefaultPortNumber()
	{
		return 37888;
	}

	public static String getDefaultSshIdRsa()
	{
		return ".ssh/id_rsa";
	}
}

