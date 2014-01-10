package evaluation; 

import io.*;
import data_representation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author miriamhuijser
 * This class measures the sparsity of the different representations
 * (monolingual English, monolingual Dutch or bilingual) with or without having 
 * the top 500 word/wordpairs of one document. It then prints the results.
 */
public class MeasureSparsity{
	public static void main(String[] args){
		boolean top500 = true; // set these to false for evaluation of 
					//entire distribution (not just top 500 words/wordpairs)
		boolean bilingual = true; // set this to false for monolingual evaluation
		if(bilingual){
			String directory = "/scratch/clust_init/models";
            String fileName = "/model/lex.e2f";
            String[] files = FileLoadingUtils.listDirectoriesDirectory(directory);
            ArrayList<BilingualDocument> documentObjects = new ArrayList<BilingualDocument>();
            Map<String, Double> allWords = new HashMap<String, Double>();

            for(int i = 0; i < files.length; i++){
            	String document = directory+"/"+files[i]+fileName;
            	BilingualDocument bDoc = new BilingualDocument(document);
            	documentObjects.add(bDoc);
            }
			for( int i = 0; i < documentObjects.size(); i++ ){
				documentObjects.get(i).createList( allWords );
				allWords = documentObjects.get(i).wordContainer;
			}
			int sizeEntireCorpus = allWords.size();

			// Bilinugal top 500 wordpairs per document
			if(top500){
				double averageNumberZeroes = sizeEntireCorpus - 500;
				double ratio = averageNumberZeroes / (double) sizeEntireCorpus;
				System.out.println("Top 500 bilingual representation");
				System.out.println("Sparsity ratio: "+ratio);
			}
			// Bilingual all wordpairs are taken into account
			else{
				int totalNumberZeroes = 0;
				double averageNumberZeroes;
				for( int i = 0; i < documentObjects.size(); i++ ){
					int sizeValues = documentObjects.get(i).words.size();
					int numberOfZeroes = sizeEntireCorpus - sizeValues;
					totalNumberZeroes = totalNumberZeroes + numberOfZeroes;
				}
				averageNumberZeroes = (double) totalNumberZeroes / (double) documentObjects.size();
				double ratio = averageNumberZeroes / (double) sizeEntireCorpus;
				System.out.println("Bilingual representatation (no top 500)");
				System.out.println("Total number of zeroes: "+totalNumberZeroes);
				System.out.println("Average number of zeroes: "+averageNumberZeroes);
				System.out.println("Sparsity ratio: "+ratio);
			}
		}

		// Monolingual representation
		else{
			String filePath = "../Testdata/dataset/Dutch"; // directory of dutch documents
			//String filePath = "../Testdata/dataset/English"; for english representation
			ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
			Centroid wordContainer = new Centroid();
			Map<String, Double> allWords = new HashMap<String,Double>();
			ArrayList<Document> documentObjects = new ArrayList<Document>();

			for( int i = 0; i < documentNames.size(); i++ ){
				Document doc = new Document( documentNames.get(i), null );
				documentObjects.add(doc);
			}
			for( int i = 0; i < documentObjects.size(); i++ ){
				documentObjects.get(i).createList( wordContainer, null );
				wordContainer = documentObjects.get(i).initCentroid;
			}	
			allWords = wordContainer.distribution;
			int sizeEntireCorpus = allWords.size();

			// Top 500 distribution
			if(top500){
				double averageNumberZeroes = sizeEntireCorpus - 500;
				double ratio = averageNumberZeroes / (double) sizeEntireCorpus;
				System.out.println("Top 500 monolingual");
				System.out.println("Sparsity ratio: "+ratio);
			}
			// No top 500 distribution (entire documents)
			else{
				int totalNumberZeroes = 0;
				double averageNumberZeroes;
				for( int i = 0; i < documentObjects.size(); i++ ){
					int sizeValues = documentObjects.get(i).words.size();
					int numberOfZeroes = sizeEntireCorpus - sizeValues;
					totalNumberZeroes = totalNumberZeroes + numberOfZeroes;
				}
				averageNumberZeroes = (double) totalNumberZeroes / (double) documentObjects.size();
				double ratio = averageNumberZeroes / (double) sizeEntireCorpus;
				System.out.println("Monolingual representatation (no top 500)");
				System.out.println("Total number of zeroes: "+totalNumberZeroes);
				System.out.println("Average number of zeroes: "+averageNumberZeroes);
				System.out.println("Sparsity ratio: "+ratio);				
			}
		}
	}
}