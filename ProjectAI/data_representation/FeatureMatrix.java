package data_representation;

import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author saidalfaraby
 * Class FeatureMatrix create a 2d matrix representation from documents, where each row
 * is document and column is feature.
 */
public class FeatureMatrix {
	
	public double[][] matrix;
	public ArrayList<String> rowLabel;
	public ArrayList<String> columnLabel;
	private String type;
	
	
	
	
	/**
	 * Constructor 
	 * @param filePath - path of directory contains documents
	 * @param maxDocs - maximum number of document to be proceeded
	 * @param language - "english", "dutch", or "german" if using stopword, or null otherwise
	 * @param type - how do you want to represent the word occurences. 
	 * "freq" : raw frequency. "prob" : relative frequency
	 */
	public FeatureMatrix(String filePath, int maxDocs, String language, String type){
		this.type = type;
		ArrayList<FrequencyList> documentObjects = new ArrayList<FrequencyList>();
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		this.rowLabel = new ArrayList<String>();
		for( int i = 0; i < documentNames.size(); i++ ){
			if(i == maxDocs) // in order to test, pick only a small number of documents 
				break; 
			if (this.type.equals("prob")){
				Document doc = new Document( documentNames.get(i), language );
				this.rowLabel.add(documentNames.get(i));
				documentObjects.add(doc);
			}
			
		}
		
		Centroid allWords = new Centroid();
		for( int i = 0; i < documentObjects.size(); i++ ){
			if (this.type.equals("prob")){
				Document D = (Document)documentObjects.get(i);
				D.createList( allWords, "forgy" );
			} else if(this.type.equals("freq")){
				documentObjects.get(i).createListVoid();
			}
			
			System.out.println("Document parsed...");
		}
		
		this.matrix = construct2dMatrix(documentObjects);
	}
	
	
	public FeatureMatrix(ArrayList<FrequencyList> documentObjects){
		this.matrix = construct2dMatrix(documentObjects);
	}
	
	/**
	 * This method will construct a 2d array of double. Each row is observation and 
	 * each column is feature
	 * @param ArrayList<Document> - list of document objects
	 */
	private double[][] construct2dMatrix(ArrayList<FrequencyList> documentObjects){
		this.columnLabel = new ArrayList<String>();
		Set<String> vocabs = new TreeSet<String>();
		for (FrequencyList d : documentObjects){
			if (this.type.equals("prob")){
				Document D = (Document)d;
				vocabs.addAll(D.words.keySet());
			} else if (this.type.equals("freq")){
				vocabs.addAll(d.list.keySet());
			}
			
		}
		this.columnLabel.addAll(vocabs);
		System.out.println("Vocab size : "+vocabs.size());
		double[][] matrix = new double[documentObjects.size()][vocabs.size()];
		
		//Fill the matrix
		for (int i=0;i< documentObjects.size();i++){
			int j=0;
			for (String v : vocabs){
				try {
					if (this.type.equals("prob")){
						Document D = (Document)documentObjects.get(i);
						matrix[i][j] = D.words.get(v);
					} else if (this.type.equals("freq")){
						matrix[i][j] = documentObjects.get(i).list.get(v);
					}
					
				} catch(Exception e){
					matrix[i][j] = 0;
				}
				j++;
			}
		}
		
		return matrix;
	}
}
