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
	
	
	/**
	 * Constructor 
	 * @param filePath - path of directory contains documents
	 */
	public FeatureMatrix(String filePath, int maxDocs){
//		int maxDocs = 100;
//		filePath = "../Testdata/dataset/English";
		ArrayList<Document> documentObjects = new ArrayList<Document>();
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		this.rowLabel = new ArrayList<String>();
		for( int i = 0; i < documentNames.size(); i++ ){
			if(i == maxDocs) // in order to test, pick only a small number of documents 
				break; 
			
			Document doc = new Document( documentNames.get(i), "english" );
			this.rowLabel.add(documentNames.get(i));
			documentObjects.add(doc);
		}
		
		Centroid allWords = new Centroid();
		for( int i = 0; i < documentObjects.size(); i++ ){
			documentObjects.get(i).createList( allWords, "forgy" );
			System.out.println("Document parsed...");
			allWords = documentObjects.get(i).initCentroid;	
		}
		
		this.matrix = construct2dMatrix(documentObjects);
	}
	
	/**
	 * This method will construct a 2d array of double. Each row is observation and 
	 * each column is feature
	 * @param ArrayList<Document> - list of document objects
	 */
	private double[][] construct2dMatrix(ArrayList<Document> documentObjects){
		this.columnLabel = new ArrayList<String>();
		Set<String> vocabs = new TreeSet<String>();
		for (Document d : documentObjects){
			vocabs.addAll(d.words.keySet());
		}
		this.columnLabel.addAll(vocabs);
		System.out.println("Vocab size : "+vocabs.size());
		double[][] matrix = new double[documentObjects.size()][vocabs.size()];
		
		//Fill the matrix
		for (int i=0;i< documentObjects.size();i++){
			int j=0;
			for (String v : vocabs){
				try {
					matrix[i][j] = documentObjects.get(i).words.get(v);
				} catch(Exception e){
					matrix[i][j] = 0;
				}
				j++;
			}
		}
		
		return matrix;
	}
}
