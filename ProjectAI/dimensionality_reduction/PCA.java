package dimensionality_reduction;



import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.jmat.data.*;
import org.jmat.data.matrixDecompositions.*;

import data_representation.Document;

/**
 * 
 *  
 * @chris & dimitra
 */
public class PCA {

	private AbstractMatrix covariance;
	private AbstractMatrix EigenVectors;
	private AbstractMatrix EigenValues;
	private Matrix X;
	private int nPC;
	public PCA(ArrayList<Document> documentObjects, int nPC) {
		this.nPC = nPC;
		double[][] matrix= construct2dMatrix(documentObjects);
		this.X = new Matrix(matrix);
		covariance = this.X.covariance();
		EigenvalueDecomposition e = covariance.eig();
		EigenVectors = e.getV();
		EigenValues = e.getD();
	}

	public AbstractMatrix getVectors() {
		return EigenVectors;
	}

	public AbstractMatrix getValues() {
		return EigenValues;
	}
	
	/**
	 * This method will construct a 2d array of double. Each row is observation and 
	 * each column is feature
	 * @param ArrayList<Document> - list of document objects
	 */
	private double[][] construct2dMatrix(ArrayList<Document> documentObjects){
		
		Set<String> vocabs = new TreeSet<String>();
		for (Document d : documentObjects){
			vocabs.addAll(d.words.keySet());
		}
		
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

	public static void main(String[] arg) {
		//construct the matrix X
		AbstractMatrix x1 = RandomMatrix.normal(100, 1, 0, 1);
		AbstractMatrix x2 = RandomMatrix.normal(100, 1, 0, 1);
		AbstractMatrix X = x1.plus(x2).mergeColumns(x2);

//		PCA pca = new PCA(X);
		

		//display a Frame with data in a 2D-Plot and EigenValues and EigenVectors in the command line.
//		new FrameView(X.toPlot2DPanel("[x1+x2,x2]", PlotPanel.SCATTER));
//		pca.getValues().toCommandLine("EigenValues");
//		pca.getVectors().toCommandLine("EigenVectors");
	}
}