package clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;


import plugin_metrics.KLdivergence;
import plugin_metrics.Metric;
import cluster_evaluation.*;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KLdivergence KL = new KLdivergence(true, "average");
		Kmeans KM=new  Kmeans(8, "../Testdata/dataset/English", "english", KL, 20, false, null);
//		Kmeans KM=new Kmeans( 8, String filePath, String language, Metric metric, int seed, boolean externalDataset, String extFilePath){
		KM.startClustering();
		SilhouetteCoefficient SC = new SilhouetteCoefficient(KL);
		SC.computeScore(KM);
//		Precision.computeF1("../Testdata/dataset/English", KM.clusters);
		
//		String name = "D_pharma21.txt";
//		String label = name.replaceAll(".*_", "");
//		label = label.replaceAll("\\..*","");
//		label = label.replaceAll("[0-9]","");
//		System.out.println(label);
		
//		Set<Integer> numbers = new TreeSet<Integer>();
//		Set<Integer> numbers2 = new TreeSet<Integer>();
//	    numbers.add(2);
//	    numbers.add(5);
//	    numbers2.add(2);
//	    numbers2.add(9);
//	    numbers.addAll(numbers2);
//	    System.out.println(numbers.toString());
		
		double[] nn = new double[]{1,2,3};
		double[] nn2 = new double[]{3,3,3};
		AbstractMatrix m = new Matrix(nn,1);
		AbstractMatrix n = new Matrix(nn2,1);
		AbstractMatrix mm = new Matrix(0,3);
		AbstractMatrix cc;
		mm.insertRowsEquals(mm.getRowDimension(), n);
		mm.insertRowsEquals(mm.getRowDimension(), m);
//		mm.setColumns(0, m);
		System.out.println(mm.getColumn(1).plus(mm.getColumn(2)));
//		cc = mm;
		cc = mm.deleteColumn(0);
		cc = cc.deleteColumn(1);
		
		mm.deleteColumnEquals(1);
		
		System.out.println(n.transpose().times(n));
		
//		ArrayList<Integer> list = new ArrayList<Integer>();
//		list.add(4);
//		list.add(2);
//		list.add(3);
////		Collections.sort(list);
//		Collections.reverse(list);
//		System.out.println(list.toString());

	}

}
