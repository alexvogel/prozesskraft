package de.caegroup.commons;

import java.io.*;
import java.net.*;
public class WhereAmI {
  
  /**
   * Get the installation directory of the application
   * @param clazz the class that contains the main() method
   * @return The installation directory of the application.
   */
  public static File WhereAmI(Class clazz) {
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    File file = null;
    try {
      file  = new File(url.toURI());
    }
    catch (URISyntaxException e) {
      // Let's trust the JDK to get it rigth.
    }
 
    if (file.isDirectory()) {
      // Application consists of loose class files
      return file;
    }
    else {
      // Application is packaged in a JAR file
      return file.getParentFile();
    }
  }
  
	public static File getDefaultInifile(Class clazz)
	{
		File myPosition = WhereAmI(clazz);
		String iniFilepath = myPosition.getParentFile().getAbsolutePath()+"/etc/default.ini";
		File iniFile = new File(iniFilepath);
		return iniFile;
	}

	/**
	 * constructor
	 * the dbfile is set to be at the default position
	 * default position is relative to installation position
	 * <installationdir>/../../../../data/pradar/pradar.db
	 */
	public static File getDefaultDbfile(Class clazz)
	{
		File myPosition = WhereAmI(clazz);
		File dbfile = myPosition;
	  
		for (int x=0; x<4; x++)
		{
			try
			{
				myPosition = myPosition.getParentFile();
			}
			catch (Exception e)
			{
				System.err.println("fatal: default position of databasefile cannot be determined.");
			}
		}
		
		try
		{
			dbfile = new File(myPosition.getAbsoluteFile()+"/data/pradar/pradar.db");
		}
		catch (NullPointerException e)
		{
			System.err.println("fatal: default position of databasefile cannot be determined.");
			System.exit(1);
		}
		return dbfile;
	}
	
	public static int getDefaultPortNumber()
	{
		return 37888;
	}

	public static String getDefaultSshIdRsa()
	{
		return "/.ssh/id_rsa";
	}
}

