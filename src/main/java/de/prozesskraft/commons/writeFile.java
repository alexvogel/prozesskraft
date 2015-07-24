package de.prozesskraft.commons;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class writeFile {

	/**
	 * writes a file
	 * @param targetFile
	 * @param fileContent
	 * @throws IOException
	 */
	public static void writeFile(java.io.File targetFile, ArrayList<String> fileContent) throws IOException
	{
		if(targetFile.exists())
		{
			System.err.println("error: skipping creating file, because a file does already exist: " + targetFile.getCanonicalPath());
		}
		else
		{
			System.out.println("info: writing file: " + targetFile.getCanonicalPath());
			try
			{
				PrintWriter writer = new PrintWriter(targetFile.getCanonicalPath(), "UTF-8");
				for (String line : fileContent)
				{
					writer.println(line);
				}
				writer.close();
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnsupportedEncodingException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	
}
