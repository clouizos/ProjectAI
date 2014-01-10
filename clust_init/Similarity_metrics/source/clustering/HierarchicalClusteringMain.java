package clustering;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;

/**
 * 
 * @author miriamhuijser
 * Class HierarchicalClusteringMain is used to do hierarchical clustering
 * using single link clustering (see Class SingleLinkClustering) for 
 * similarity scores obtained from bilingual representations or from
 * monolingual representations. And for every metric.
 */
public class HierarchicalClusteringMain{
	public static void main(String[] args){
		int numberOfDocs = 200;
	//	String directory = "/scratch/clust_init/Similarity_metrics/results/BilingualResults/";
		String directory = "/scratch/clust_init/Similarity_metrics/results/MonolingualResults/";
		String fDirectory = directory+"filenameLists/";
		String fileNames = directory+"filenameLists.txt";
		String sDirectory = directory+"SimilarityScores/";
		String similarityScores = directory+"SimilarityScores.txt";
		String cosine = "Cosine";
		String jaccards = "Jaccards";		
		ArrayList<String> fileNamesLists = new ArrayList<String>();
		ArrayList<String> similarityScoresFiles = new ArrayList<String>();		
		File file1, file2; 
		Scanner s1, s2;
		
		try{
			file1 = new File(fileNames);
			s1 = new Scanner(file1);
			while(s1.hasNextLine()){
				String fileN = s1.nextLine();
				fileNamesLists.add(fileN);
			}
			file2 = new File(similarityScores);
			s2 = new Scanner(file2);
			while( s2.hasNextLine()){
				String simScore = s2.nextLine();
				similarityScoresFiles.add(simScore);
			}
		} catch(IOException e){
			System.err.println(e.getMessage());
		}

		for( int i = 0; i < fileNamesLists.size(); i++ ){
			String documentNamesFile = fDirectory+fileNamesLists.get(i);
			String matrixFile = sDirectory+similarityScoresFiles.get(i);
			/*
			 *  Cosine and Jaccards both assign higher scores to more similar
			 *  documents, whereas the other metrics assign lower scores
			 *  to more similar documents (more of a distance).
			 */
			boolean lowScoreIsSimilar = true;
			if( matrixFile.contains(cosine) || matrixFile.contains(jaccards) ){
				lowScoreIsSimilar = false;
			}
			SingleLinkClustering c = new SingleLinkClustering(matrixFile, 
					documentNamesFile, numberOfDocs, lowScoreIsSimilar);
			c.init();
			c.startClustering();
		}
	}
}