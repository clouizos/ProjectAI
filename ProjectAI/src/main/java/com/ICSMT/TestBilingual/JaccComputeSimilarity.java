package TestBilingual;

import metrics.*;
import java.io.PrintWriter;
import java.io.IOException;
import io.*;

/**
 * 
 * @author miriamhuijser
 * Class JaccComputeSimilarity computes the similarity scores for each
 * pair of documents using the Jaccard's metric. It writes these similarity
 * scores to a .csv file. It also creates a filenameslist, listing all the
 * names of the documents in the dataset.
 */
public class JaccComputeSimilarity{
	public static void main(String[] args){
        String directory = "../models";
        String fileName = "/model/lex.e2f";
        String[] files = FileLoadingUtils.listDirectoriesDirectory(directory);
        boolean bilingual = true;
        String language = null;

        // Computing similarity scores for bilingual dataset
		try{
			PrintWriter writer = new PrintWriter("Biling-ResultsJaccards.csv", "UTF-8");
			PrintWriter writer2 = new PrintWriter("fileNames5.txt");				
			for( int i = 0; i < files.length; i++ ){
				 writer2.println(files[i]);					
				for( int j = 0; j < i; j++ ){
                    String a = directory+"/"+files[i]+fileName;
                    String b = directory+"/"+files[j]+fileName;
					
					JaccardsCoefficientMetric m = new JaccardsCoefficientMetric( a, b, language, bilingual );
					writer.print(m.computeSimilarity()+",");
				}
				if( i != 0 ){
					writer.println("");
				}
			}
			writer.close();
			writer2.close();
		} catch(IOException e){
			System.err.println(e.getMessage());
		};
	}
}