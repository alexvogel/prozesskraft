package de.caegroup.process;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Syscall {

	/* Aufruf:
	 * $1 Vollstaendiger Aufruf mit allen Parametern
	 * $2 absoluter Pfad zum stdout-ausgabefile. Dieses wird neu angelegt und darin wird der ganze stdout von $1 geschrieben
	 * $3 absoluter Pfad zum stderr-ausgabefile. Dieses wird neu angelegt und darin wird der ganze stderr von $1 geschrieben
	*/
	public static void main(String[] args) {
	 
		try
		{
			if (args.length < 4)
			{
				System.out.println("Please specify arg1=systemcall arg-3=fullpath_stdout arg-2=fullpath_stderr arg-1=fullpath_pid");
			}
			
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.println("***ArrayIndexOutOfBoundsException\n" + e.toString());
		}

		// umwandeln aller parameter in eine liste
		ArrayList<String> argumente = new ArrayList<String>(Arrays.asList(args));
		
		// umkopieren der stdout/stderr filepfade
		String absdir_stdout = argumente.get(argumente.size()-3);
		String absdir_stderr = argumente.get(argumente.size()-2);
		String absdir_pid    = argumente.get(argumente.size()-1);

		// entfernen der stdout/sdterr/pid filepfade
		argumente.remove(argumente.size()-1);
		argumente.remove(argumente.size()-1);
		argumente.remove(argumente.size()-1);
		
		try {

			String aufruf = new String();
			
			Iterator<String> iterstring = argumente.iterator();
			while(iterstring.hasNext())
			{
				aufruf = iterstring.next()+" ";
			}
			
			// Aufruf taetigen
			java.lang.Process sysproc = Runtime.getRuntime().exec(aufruf);

			// feststellen der Process-ID des laufenden JavaVM und in die PID-Datei schreiben
			String pid = ManagementFactory.getRuntimeMXBean().getName();
			
			// da die pid von der ManagementFactory normalerweise die Form "267353@ws11.caegroup" hat
			// soll nur die fuehrende Zahl erfasst werden
			String patt = "(\\d+)";
			Pattern r = Pattern.compile(patt);
			Matcher m = r.matcher(pid);
			FileWriter fw_pid = new FileWriter(absdir_pid);
			// wenn eine fuehrende zahl gefunden wird, wird diese als pid verwendet
			if (m.find())
			{
				System.out.println("PID WIRD FESTGESTELLT ALS: "+m.group(1));
				fw_pid.write(m.group(1));
			}
			// wenn keine fuehrende zahl gefunden wird, wird der gesamte string als pid verwendet
			else
			{
				fw_pid.write(pid);
			}
			fw_pid.flush();
			fw_pid.close();
			
			// einfangen der stdout- und stderr
			InputStream is_stdout = sysproc.getInputStream();
			InputStream is_stderr = sysproc.getErrorStream();
			
			// Send your InputStream to an InputStreamReader:
			InputStreamReader isr_stdout = new InputStreamReader(is_stdout);
			InputStreamReader isr_stderr = new InputStreamReader(is_stderr);

			// That needs to go to a BufferedReader:
			BufferedReader br_stdout = new BufferedReader(isr_stdout);
			BufferedReader br_stderr = new BufferedReader(isr_stderr);
			
			// oeffnen der OutputStreams zu den Ausgabedateien
			FileWriter fw_stdout = new FileWriter(absdir_stdout);
			FileWriter fw_stderr = new FileWriter(absdir_stderr);
			
			// zeilenweise in die files schreiben
			String line_out = new String();
			String line_err = new String();

			// DEBUG
			System.out.println("CALL: "+aufruf);
			System.out.println("SOUT: "+absdir_stdout);
			System.out.println("SERR: "+absdir_stderr);

			while ((((line_out = br_stdout.readLine()) != null) && ((line_err = br_stderr.readLine()) != null)) || ((line_err = br_stderr.readLine()) != null))
			{
				if (!(line_out == null))
				{
					System.out.println(line_out);
					fw_stdout.write(line_out);
					fw_stdout.write("\n");
					fw_stdout.flush();
				}
				if (!(line_err == null))
				{
					System.out.println(line_err);
					fw_stderr.write(line_err);
					fw_stderr.write("\n");
					fw_stderr.flush();
				}
			}

//			while ((line_err = br_stderr.readLine()) != null)
//			{
//				System.out.println(line_err);
//				fw_stderr.write(line_err);
//				fw_stderr.write("\n");
//				fw_stdout.flush();
//			}
//			
			
			sysproc.waitFor();
			int sysproc_exitvalue = sysproc.exitValue();
			System.out.println("EXITVALUE: "+sysproc_exitvalue);
			
			fw_stderr.write("EXITVALUE: "+sysproc_exitvalue);
			
			fw_stdout.close();
			fw_stderr.close();

			System.exit(sysproc_exitvalue);

		}
		catch (IOException e)
		{

			// I haven't figured out how to trip this yet.
			// Which makes sense. Java doesn't really know
			// if your process failed. That must be determined
			// from the exit value.
			System.out.println("Exception happened - here's what I know: ");
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			
			// You need this for that waitFor() diddy.
			System.out.println("Something got interrupted, "+
				"I guess: "+e);
		}

	}

}
