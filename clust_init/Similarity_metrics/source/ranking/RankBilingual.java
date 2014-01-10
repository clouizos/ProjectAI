package ranking;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.util.ArrayList;
/**
 * 
 * @author miriamhuijser
 * Class RankBilingual computes the ranking score for a dataset of 200
 * bilingual documents for every metric and prints the result to the console.
 */
public class RankBilingual{
	public static void main(String[] args){
		int numberOfDocs = 200;
		String directory = "/scratch/clust_init/Similarity_metrics/" +
				"results/BilingualResults/";
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
			System.out.println(e.getMessage());
		}

		for( int i = 0; i < fileNamesLists.size(); i++ ){
			String docNamesFile = fDirectory+fileNamesLists.get(i);
			String dataFile = sDirectory+similarityScoresFiles.get(i);
			boolean lowScoreIsSimilar = true;
			if( dataFile.contains(cosine) || dataFile.contains(jaccards) ){
				lowScoreIsSimilar = false;
			}
			Ranking r = new Ranking(dataFile, docNamesFile, numberOfDocs, 
					lowScoreIsSimilar);
			r.init();
			System.out.println(dataFile);
			System.out.println(r.startRanking());
		}
	}
}
