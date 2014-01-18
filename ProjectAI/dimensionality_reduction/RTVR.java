package dimensionality_reduction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jmat.data.*;

import data_representation.FeatureMatrix;

/**
 * This class will construct a new representation of document vectors, by replacing
 * rare-term features with linear combination of common-term features;
 */
public class RTVR {
	private double treshold;
//	private Matrix originalMatrix;
	public AbstractMatrix reducedMatrix;
	
	
	public RTVR(FeatureMatrix fm, double treshold){
		this.treshold = treshold;
		AbstractMatrix C = new Matrix(fm.matrix);
		
		C.transposeEquals();
		computeReducedMatrix(C);
	}
	
	/**
	 * This method will find all documents that a term/feature occurs in
	 * @param matrix - term-document matrix, where row is term/feature.
	 * @param featureIndex - index of the feature
	 * @return D -  documents contain feature with index featureIndex
	 */
	private AbstractMatrix docWithFeature(AbstractMatrix matrix, int featureIndex){
		AbstractMatrix D=new Matrix(matrix.getRowDimension(),0);
		for (int i=0;i<matrix.getColumnDimension();i++){
			if (matrix.get(featureIndex, i)>0){
				D.insertColumnsEquals(D.getColumnDimension(), matrix.getColumn(i));
			}
		}
		return D;
	}
	
	private AbstractMatrix getBasisVector(int nRows, int index){
		AbstractMatrix e = new Matrix(nRows,1);
		e.set(index, 1, 1);
		return e;
	}
	
	private AbstractMatrix truncateVector(AbstractMatrix vector, ArrayList<Integer> E){
		Collections.sort(E);
		Collections.reverse(E);
		for (int i=0;i<E.size();i++){
			vector.deleteRowEquals(E.get(i));
		}
		return vector;
	}
	
	private void computeReducedMatrix(AbstractMatrix C){
		int k=0;
		ArrayList<Integer> E = new ArrayList<Integer>();
		ArrayList<Integer> notE = new ArrayList<Integer>();
		Map<Integer,Integer> phi = new HashMap<Integer,Integer>();
		for (int i=0;i<C.getRowDimension();i++){
			if (docWithFeature(C,i).getColumnDimension()<this.treshold){
				E.add(i);
			} else {
				notE.add(i);
				phi.put(i,k);
				k++;
			}
		}
		
		AbstractMatrix R = new Matrix(k,C.getRowDimension());
		
		for (int i:E){
			int l=0;
			AbstractMatrix Df = docWithFeature(C,i);
			for (int j=0;j<Df.getColumnDimension();j++){
				R.setColumns(i, R.getColumn(i).plus(truncateVector(C.getColumn(j), E).times(C.get(i, j))));
				l+=Math.abs(C.get(i, j));
			}
			if (l!=0){
				R.setColumns(i, R.getColumn(i).divide(l));
			}
		}
		
		for (int i:notE){
			R.setColumns(i, getBasisVector(k, phi.get(i)));
		}
		
		this.reducedMatrix = R.times(C);
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
