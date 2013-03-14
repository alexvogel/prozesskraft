package de.caegroup.pradar;

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
}

