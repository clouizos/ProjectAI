package dimensionality_reduction;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
	private ArrayList<Integer> docWithFeature(AbstractMatrix matrix, int featureIndex){
		AbstractMatrix D=new Matrix(matrix.getRowDimension(),0);
		ArrayList<Integer> D2 = new ArrayList<Integer>();
		for (int i=0;i<matrix.getColumnDimension();i++){
			if (matrix.get(featureIndex, i)>0){
//				D.insertColumnsEquals(D.getColumnDimension(), matrix.getColumn(i));
				D2.add(i);
			}
		}
		return D2;
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
		Map<Integer,ArrayList<Integer>> E = new HashMap<Integer,ArrayList<Integer>>();
		ArrayList<Integer> notE = new ArrayList<Integer>();
		Map<Integer,Integer> phi = new HashMap<Integer,Integer>();
		System.out.println("Dimension of original matrix C : "+C.getRowDimension()+" x "+C.getColumnDimension());
		System.out.println("Collecting rare features...");
		for (int i=0;i<C.getRowDimension();i++){
			ArrayList<Integer> Dfi = docWithFeature(C,i);
			if (Dfi.size()<this.treshold){
				E.put(i,Dfi);
			} else {
				notE.add(i);
				phi.put(i,k);
				k++;
			}
		}
		System.out.println("Total new dimension : "+k);
		System.out.println("Computing the R matrix...");
		AbstractMatrix R = new Matrix(k,C.getRowDimension());
		System.out.println("Processing the E set");
		System.out.println("Size of set E : "+E.size());
		int count=0;
		for (int i:E.keySet()){
			int l=0;
			for (int j : E.get(i)){
				ArrayList<Integer> Elist = new ArrayList<Integer>();
				Elist.addAll(E.keySet());
				R.setColumns(i, R.getColumn(i).plus(truncateVector(C.getColumn(j), Elist).times(C.get(i, j))));
				l+=Math.abs(C.get(i, j));
			}
			if (l!=0){
				R.setColumns(i, R.getColumn(i).divide(l));
			}
			System.out.println(count++);
		}
		
		System.out.println("Processing the not E set");
		System.out.println("Size of set notE "+notE.size());
		count=0;
		for (int i:notE){
			System.out.println(count++);
			R.setColumns(i, getBasisVector(k, phi.get(i)));
		}
		
		this.reducedMatrix = R.times(C);
	}
	
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String filePath = "../Testdata/dataset/English";
		int nDocs = 200;
		int threshold=15;  //term that appear in less than this number will be replaced
		FeatureMatrix FM = new FeatureMatrix(filePath,nDocs,"english","prob");
		RTVR rtvr = new RTVR(FM, threshold);
		System.out.println("ROW "+rtvr.reducedMatrix.getRowDimension());
		System.out.println("COLUMN "+rtvr.reducedMatrix.getColumnDimension());
		
		
		
		try{
			 
			FileOutputStream fout = new FileOutputStream("reducedMatrix.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);   
			oos.writeObject(rtvr.reducedMatrix);
			oos.close();
			System.out.println("Done");
	 
		   }catch(Exception ex){
			   ex.printStackTrace();
		   }
		
		
//		AbstractMatrix N=null;
//		try{
//			FileInputStream fin = new FileInputStream("reducedMatrix.ser");
//			ObjectInputStream ois = new ObjectInputStream(fin);
//			 N = (AbstractMatrix) ois.readObject();
//		} catch(Exception e){
//			e.printStackTrace();
//		}
		
//		System.out.println(N);

	}

}
