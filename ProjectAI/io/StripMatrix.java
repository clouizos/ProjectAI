package io;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;

/**
 * 
 * @author miriamhuijser
 * Class StripMatrix provides methods that take a .csv file with a matrix 
 * and will output a .csv file with the lower triangle of the matrix. 
 */
public class StripMatrix{
	String fileNameInput;
	String fileNameOutput;
	
	/**
	 * Constructor
	 * @param fileNameInput - name of input file
	 * @param fileNameOutput - desired name of output file
	 */
	public StripMatrix(String fileNameInput, String fileNameOutput ){
		this.fileNameInput = fileNameInput;
		this.fileNameOutput = fileNameOutput;
	}

	/**
	 * This method reads the matrix from the inputfile and writes the
	 * lower triangle of the matrix to the outputfile.
	 */
	public void startStripping(){
		File file;
		Scanner s;
		try{
			PrintWriter writer = new PrintWriter(fileNameOutput, "UTF-8");
			file = new File(fileNameInput);
			s = new Scanner(file);
			int lineN = 0;
			while(s.hasNextLine()){
				String line = s.nextLine();
				Scanner s2 = new Scanner(line);
				s2.useDelimiter(",");
				int valueN = 0;
				while( s2.hasNext() ){
					String value = s2.next();
					if( valueN < lineN ){
						writer.print(value+",");
					}
					else{
						if( lineN != 0 ){
							writer.println("");
						}
						break;
					}
					valueN++;
				}
				lineN++;
				s2.close();
			}
			writer.close();

		}catch(IOException e){System.out.println(e.getMessage());}
	}
}