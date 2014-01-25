package dimensionality_reduction;



import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.jmat.data.*;
import org.jmat.data.matrixDecompositions.*;

import data_representation.Centroid;
import data_representation.Document;
import data_representation.FeatureMatrix;

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
	
	public PCA(String filePath, int nPC, int nDoc) {
		this.nPC = nPC;
		FeatureMatrix fm = new FeatureMatrix(filePath,nDoc,"english","prob");
		this.X = new Matrix(fm.matrix);
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
	
	
	

	public static void main(String[] arg) {
		String filePath = "./target/Testdata/dataset/English";
		int nPrincipleComp = 50;
		int nDocs = 10;
		PCA pca = new PCA(filePath, nPrincipleComp,nDocs);
		System.out.println(pca.EigenValues.getColumnDimension());
		System.out.println(pca.EigenValues.getRowDimension());
		System.out.println(pca.EigenValues.toString());
//		System.out.println(pca.EigenVectors.toString());
		

		//display a Frame with data in a 2D-Plot and EigenValues and EigenVectors in the command line.
//		new FrameView(X.toPlot2DPanel("[x1+x2,x2]", PlotPanel.SCATTER));
//		pca.getValues().toCommandLine("EigenValues");
//		pca.getVectors().toCommandLine("EigenVectors");
	}
}
