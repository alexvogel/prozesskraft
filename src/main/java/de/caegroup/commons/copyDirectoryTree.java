package de.caegroup.commons;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

public class copyDirectoryTree {

	/**
	 * copies a directory tree
	 * @param Path source
	 * @param Path target
	 * @throws IOException
	 */
	public static void copyDirectoryTree(final Path source, final Path target) throws IOException
	{
		Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
				new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
					{
						Path targetdir = target.resolve(source.relativize(dir));
						try
						{
							Files.copy(dir,  targetdir);
						}
						catch (FileAlreadyExistsException e)
						{
							if (!Files.isDirectory(targetdir)) throw e;
						}
						return FileVisitResult.CONTINUE ;
					}
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
					{
						Files.copy(file,  target.resolve(source.relativize(file)));
						return FileVisitResult.CONTINUE;
					}
					
				});
	}
	
}
