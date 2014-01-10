package data_representation;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author miriamhuijser
 * Class AdjacencyMatrix saves the adjacency matrix (the matrix with the
 * similarity scores between each document in the dataset). This adjacency 
 * matrix is merely the lower triangle of the full adjacency matrix.
 * To use this class, one needs a .csv file with the matrix and a textfile
 * containing the names of the documents in the dataset.
 */
public class AdjacencyMatrix{
	String matrixFile;
	String documentNamesFile;
	int numberOfDocuments;
	boolean lowScoreIsSimilar;
	Map<Integer, MatrixIndices> toMatrixIndices;
	public ArrayList<Double> matrixValues;
	public ArrayList<ArrayList<Double>> matrixValuesPerRow;
	public ArrayList<String> documentList;
	ArrayList<Double> listValues;

	/**
	 * Constructor
	 * @param matrixFile - name of .csv file that contains adjacency matrix
	 * @param documentNamesFile - name of textfile that contains the names of
	 * the documents in the dataset
	 * @param numberOfDocuments - number of documents in the dataset
	 * @param lowScoreIsSimilar - boolean that indicates whether similarity
	 * metric is used that assigns low scores to highly similar documents.
	 * (e.g. this is the case for metrics that compute the distance)
	 */
	public AdjacencyMatrix( String matrixFile, String documentNamesFile, 
		int numberOfDocuments, boolean lowScoreIsSimilar ){
		this.matrixFile = matrixFile;
		this.numberOfDocuments = numberOfDocuments;
		this.documentNamesFile = documentNamesFile;
		this.lowScoreIsSimilar = lowScoreIsSimilar;
	}

	/**
	 * This method creates and initializes the matrix representation by reading 
	 * the values from the matrix file. 
	 * It also initializes the list of document names by reading from the file
	 * that contains the names of the documents that are in the dataset.
	 */
	public void init(){
		createMatrixRepresentation();
		initializeValuesMatrix();
		initializeDocumentList();
	}

	/**
	 * This method creates the matrix representation.
	 */
	public void createMatrixRepresentation(){
		int numberOfIndices = (int) (Math.pow(numberOfDocuments, 2) - 
				numberOfDocuments) / 2;
		toMatrixIndices = new HashMap<Integer, MatrixIndices>();
		int row = 1;
		int column = 0; 
		int counter = 0;
		for( int i = 0; i < numberOfIndices; i++ ){
			if( counter == row ){
				column = 0;
				row++;
				counter = 0;
			}
			MatrixIndices m = new MatrixIndices( row, column );
			toMatrixIndices.put(i, m);
			column++;
			counter++;
		}
	}

	/**
	 * This method initializes the matrix representation by reading its values
	 * from the matrix file.
	 */
	public void initializeValuesMatrix(){
		matrixValues = new ArrayList<Double>();
		matrixValuesPerRow = new ArrayList<ArrayList<Double>>();
		File file; 
		Scanner s;
		try{
			file = new File(matrixFile);
			s = new Scanner(file);
			matrixValuesPerRow.add(new ArrayList<Double>());
			while(s.hasNextLine()){
				ArrayList<Double> row = new ArrayList<Double>();
				String line = s.nextLine();
				Scanner s2 = new Scanner(line);
				s2.useDelimiter(",");
				while( s2.hasNext() ){
					double value = Double.parseDouble(s2.next());
					matrixValues.add(value);
					row.add(value);
				}
				s2.close();
				matrixValuesPerRow.add(row);
			}
		} catch( IOException e){
			System.err.println(e.getMessage());
		}
	}

	/**
	 * This method initializes the list with document names by reading from the 
	 * file that contains the names of the documents that are in the dataset.
	 */
	public void initializeDocumentList(){
		documentList = new ArrayList<String>();
		File file; 
		Scanner s;
		try{
			file = new File(documentNamesFile);
			s = new Scanner(file);
			while(s.hasNextLine()){
				String name = s.nextLine();
				documentList.add(name);
			}
		} catch( IOException e){
			System.err.println(e.getMessage());
		}
	}

	/**
	 * This method returns the document names that correspond to the similarity
	 * score at the index given as input. (e.g. The first value in the matrix
	 * has index 0 and corresponds to documents 1 and 2)
	 * @param arrayIndex - index of similarity score of which the corresponding
	 * documents are inquired.
	 * @return documentNames - the names of the documents that correspond to
	 * the value at index arrayIndex.
	 */
	public ArrayList<String> arrayIndexToDocumentNames( int arrayIndex ){
		ArrayList<String> documentNames = new ArrayList<String>();
		MatrixIndices indices = toMatrixIndices.get(arrayIndex);
		int row = indices.row;
		int column = indices.column;
		String doc1 = documentList.get(row);
		String doc2 = documentList.get(column);
		documentNames.add(doc1);
		documentNames.add(doc2);
		return documentNames;
	}

	/**
	 * This method returns a list of indices from the matrix that have
	 * minimal values.
	 * @return similarValues - List of matrix indices that have minimal values
	 */
	public ArrayList<Integer> getIndicesMinDistanceDocs(){
		ArrayList<Integer> similarValues = new ArrayList<Integer>();
		listValues = new ArrayList<Double>();
		for( int i = 0; i < matrixValues.size(); i++ ){
			listValues.add(matrixValues.get(i));
		}
		if(lowScoreIsSimilar){
			for( int i = 0; i < listValues.size(); i++ ){
				int minIndex = listValues.indexOf(Collections.min(listValues));
				similarValues.add(minIndex);
				listValues.set(minIndex,Double.POSITIVE_INFINITY);
			}
		}
		else{
			for( int i = 0; i < listValues.size(); i++ ){
				int maxIndex = listValues.indexOf(Collections.max(listValues));
				similarValues.add(maxIndex);
				listValues.set(maxIndex,Double.NEGATIVE_INFINITY);
			}			
		}
		return similarValues;
	}
}