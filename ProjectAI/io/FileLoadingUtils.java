package io;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FilenameFilter;


/**
 * 
 * @author miriamhuijser
 * Class FileLoadingUtils provides methods that list the files in a directory
 * or the directories in a directory.
 */
public class FileLoadingUtils {

	/**
	 * This method lists the files in the directory given as input.
	 * @param filePath - directory of which the files will be listed.
	 * @return fileList - list with the names of the files in the directory
	 */
	public static ArrayList<String> listFilesDirectory( String filePath ){
		ArrayList<String> fileList = new ArrayList<String>();
		int index = 0;
		Path dir = FileSystems.getDefault().getPath( filePath );
		DirectoryStream<Path> stream = null;
		try {
			stream = Files.newDirectoryStream( dir );
			for (Path path : stream) {
				String name = filePath + "/" + path.getFileName().toString();
				try{
					fileList.add(index, name);
					index++;
					Scanner s = new Scanner(new File(name));
					s.close();
				} catch( IOException e ){
					index--;
					e.printStackTrace();
				}
			}
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileList;
	}

	/**
	 * This method lists the directories in the directory gives as input.
	 * @param path - directory of which the subdirectories will be listed.
	 * @return directories - list of the names of the subdirectories.
	 */
	public static String[] listDirectoriesDirectory( String path ){
		File file = new File(path);
		String[] directories = file.list(new FilenameFilter() {
 		 @Override
 		 public boolean accept(File dir, String name) {
   			 return new File(dir, name).isDirectory();
		  }
		});
		return directories;	
	}
}