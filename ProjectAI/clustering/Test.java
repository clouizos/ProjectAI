package clustering;

import org.jmat.data.AbstractMatrix;
import org.jmat.data.Matrix;

import plugin_metrics.KLdivergence;
import cluster_evaluation.SilhouetteCoefficient;
import data_representation.Cluster;
import data_representation.Document;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String extFilePath = "./featureVectorsLDA/";
		boolean useExtPath = true;
		int numTopics = 20;
		int seed = 20;
		
		KLdivergence KL = new KLdivergence(true, "average");
		Kmeans KM=new  Kmeans(10, "./Testdata/dataset/English", "english", KL, seed, useExtPath, extFilePath, numTopics);
//		Kmeans KM=new Kmeans( 8, String filePath, String language, Metric metric, int seed, boolean externalDataset, String extFilePath){
		KM.startClustering();
		SilhouetteCoefficient SC = new SilhouetteCoefficient(KL);
		SC.computeScore(KM);
		
		int i=1;
		for(Cluster cluster : Clustering.clusters){
			System.out.println("Cluster "+i+":");
			//System.out.println("Centroid:"+cluster.centroid.distribution);
			for(Document doc : cluster.members){
				System.out.println(doc.getFilename());
			}
			i++;
			System.out.println("");
		}
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
