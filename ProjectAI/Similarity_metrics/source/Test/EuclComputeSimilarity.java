package Test;

import metrics.*;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;
import io.*;

/**
 * 
 * @author miriamhuijser
 * Class EuclComputeSimilarity computes the similarity scores for each
 * pair of documents using the euclidian distance. It writes these similarity
 * scores to a .csv file. It also creates a filenameslist, listing all the
 * names of the documents in the dataset.
 */
public class EuclComputeSimilarity{
	public static void main(String[] args){
		String directory = "../Testdata/dataset/English";
		String directoryDutch = "../Testdata/dataset/Dutch/";
		ArrayList<String> files = FileLoadingUtils.listFilesDirectory(directory);
		String language = null; // no shortlist used

		// Similarity scores computed for the English dataset
		try{
			PrintWriter writer = new PrintWriter("EN-ResultsEuclidian.csv", "UTF-8");
			PrintWriter writerF = new PrintWriter("fileNamesListEN-EUCLIDIAN.txt");
			for( int i = 0; i < files.size(); i++ ){
				writerF.println(files.get(i));
				for( int j = 0; j < i; j++ ){
					String a = files.get(i);
					String b = files.get(j);

					EuclidianDistanceMetric m = 
							new EuclidianDistanceMetric( a, b, language, false );
					writer.print(m.computeSimilarity()+",");
				}
				if( i != 0 ){
					writer.println("");
				}
			}
			writer.close();
			writerF.close();
		} catch(IOException e){
			System.err.println(e.getMessage());
		};

		// Similarity scores computed for the Dutch dataset
		try{
			PrintWriter writer2 = new PrintWriter("NL-ResultsEuclidian.csv", "UTF-8");
			PrintWriter writerF2 = new PrintWriter("fileNamesListNL-EUCLIDIAN.txt");
			for( int i = 0; i < files.size(); i++ ){
				if( files.get(i).contains(".en") ){
					int index = files.get(i).indexOf(".en");
					int index2 = files.get(i).lastIndexOf("/")+1;
					String a = directoryDutch+
							files.get(i).substring(index2, index) + ".nl";
					writerF2.println(a);
					for( int j = 0; j < i; j++ ){
						if( files.get(j).contains(".en") ){
							int index3 = files.get(j).indexOf(".en");
							int index4 = files.get(j).lastIndexOf("/")+1;
							String b = directoryDutch+
									files.get(j).substring(index4, index3) + ".nl";

							EuclidianDistanceMetric m2 = 
									new EuclidianDistanceMetric(a, b, language, false);
							writer2.print(m2.computeSimilarity()+",");
						}
					}
					writer2.println("");
				}
			}
			writer2.close();
			writerF2.close();
		} catch(IOException e){
			System.err.println(e.getMessage());
		};
	}
}