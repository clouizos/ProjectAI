package io;

import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author miriamhuijser
 * Class CreationDatafiles can be used to either create dummyfiles
 * (duplicate document, but replaces every fifth word with a dummyword)
 * or it can be used to split documents into documents with a desired
 * number of lines.
 */
public class CreationDatafiles{
	public static void main(String[] args){
		// set to false to create snippets with a certain number of files
		boolean createDummyfiles = true; 
		
		if(createDummyfiles){
			String language = "English";
			//String language = "Dutch";
			String output_dir = "../Testdata/";
			output_dir = output_dir + language + "/";
			
			// Directory that contains the files of which dummyfiles should
			// be created.
			String directory = "../Testdata/dummy/"+language+"/"; 
			String prefix = "D_"; // Desired prefix of the resulting dummy files
			ArrayList<String> files = FileLoadingUtils.listFilesDirectory(directory);
			String dummy = "dop"; // dummyword to which a number will be concatenated
			ArrayList<String> dummywords = new ArrayList<String>();
			for( int d = 1; d <= files.size(); d++ ){
				String dummyword = dummy+d+dummy;
				dummywords.add(dummyword);
				System.out.println(dummyword);
			}
	
			for( int i = 0; i < files.size(); i++ ){
				String file = files.get(i);
				int index2 = file.lastIndexOf("/")+1;
				String file2 = prefix + file.substring(index2);
	
				try{
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					PrintWriter writer = new PrintWriter(output_dir+file2);
					int count5 = 1;
					while((line = br.readLine()) != null) {
	            		Scanner s = new Scanner(line);
	        			while (s.hasNext()) {
	        				String word = s.next();
	        				if( count5 == 5 ){
	        					writer.print(dummywords.get(i) + " ");
	        					count5 = 0;
	        				}
	        				else{
	        					writer.print(word + " ");
	        				}
	        				count5++;
	           			}
	        			s.close();
	           			writer.println("");			
					}
					writer.close();
					br.close();
				} catch(IOException e){System.out.println(e.getMessage());}
			}
		}

		else{
			// With the following code one can create snippets from files with 
			// a certain number of lines
			String language = "English";
			//String language = "Dutch";
			
			//String directory = "../data/TAUS/uk/nl"; // UK-dutch directory
			//String directory = "../data/TAUS/us/nl"; // US-dutch directory
			//String directory = "../data/TAUS/uk/en"; // UK-english directory
			String directory = "../data/TAUS/us/en"; // US-english directory
			
			String output_dir = "../Testdata/";
			output_dir = output_dir + language + "/";
			
			String prefix = "CS"; // CS for US-english/dutch dataset, CK for UK-english/dutch
			
			//String extension = ".nl"; // Dutch
			String extension = ".en"; // English
			
			ArrayList<String> files = FileLoadingUtils.listFilesDirectory(directory);
			int desiredNumberOfLines = 2000;
			int fileMax = 40;
	
				for( int i = 0; i < files.size(); i++ ){
					if( files.get(i).contains(extension) ){
					String file = files.get(i);
					int index = file.indexOf(".en"); // change to .en for english
					int index2 = file.lastIndexOf("/")+1;
					int fileN = 1;
					String file2 = prefix + file.substring(index2, index) + 
							fileN + extension;
		
					int counter = 1;
					try{
						BufferedReader br = new BufferedReader(new FileReader(file));
						String line;
						PrintWriter writer2 = new PrintWriter(output_dir+file2);
		
						while((line = br.readLine()) != null && fileN<=fileMax) {
							if( counter <= desiredNumberOfLines ){
								writer2.println(line);
							}
							else{
								writer2.close();
								fileN++;
								file2 = prefix + file.substring(index2, index) + 
										fileN + extension;
								writer2 = new PrintWriter(output_dir+file2);
								writer2.println(line);
								counter = 1;
							}
		
					   		counter++;
						}
						writer2.close();
						br.close();
					} catch(IOException e){System.out.println(e.getMessage());}
				}
			}
		}
	}
}