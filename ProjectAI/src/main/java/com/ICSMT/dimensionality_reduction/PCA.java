package dimensionality_reduction;



import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.jmat.data.*;
import org.jmat.data.matrixDecompositions.*;
import io.IOFile;

import data_representation.Centroid;
import data_representation.Document;
import data_representation.FeatureMatrix;

/**
 * @said.al.faraby
 * This class will compute the Principle Component of a feature matrix.
 */
public class PCA {

	private AbstractMatrix covariance;
	private AbstractMatrix EigenVectors;
	private AbstractMatrix EigenValues;
	private Matrix X;
	private int nPC;
	
	/**
	 * 
	 * @param filePath - directory contains the documents.
	 * @param nPC - number of principle component to be used
	 * @param nDoc - number of documents used, should be less then collections size.
	 */
	public PCA(String filePath, int nPC, int nDoc) {
		this.nPC = nPC;
		FeatureMatrix fm = new FeatureMatrix(filePath,nDoc,"english","prob");
		this.X = new Matrix(fm.matrix);
		covariance = this.X.covariance();
		EigenvalueDecomposition e = covariance.eig();
		EigenVectors = e.getV();
		EigenValues = e.getD();
		EigenVectors = EigenVectors.getColumns(0,nPC);
		IOFile IO = new IOFile();
		IO.createWriteFile("PCAPCA.data");
		for (int i=0;i<EigenValues.getRowDimension();i++){
			IO.write(fm.rowLabel.get(i)+",");
			for (int j=0;j<nPC ;j++){
				IO.write(Double.toString(EigenVectors.get(i,j)));
				IO.write(" ");
			}
			IO.write("\n");
		}
		IO.close();
		
	}

	public AbstractMatrix getVectors() {
		return EigenVectors;
	}

	public AbstractMatrix getValues() {
		return EigenValues;
	}
	
	
	

	public static void main(String[] arg) {
		String filePath = "./target/Testdata/dataset/English";
		int nPrincipleComp = 100;
		int nDocs = 200;
		PCA pca = new PCA(filePath, nPrincipleComp,nDocs);
		System.out.println(pca.EigenValues.getColumnDimension());
		System.out.println(pca.EigenValues.getRowDimension());
		System.out.println(pca.EigenValues.toString());
//		System.out.println(pca.EigenVectors.toString());
		


	}
}
