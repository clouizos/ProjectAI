package clustering;

import plugin_metrics.KLdivergence;
import cluster_evaluation.SilhouetteCoefficient;
import data_representation.Cluster;
import data_representation.Document;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String extFilePath = "./features/";
		boolean useExtPath = true;
		int numTopics = 20;
		int seed = 20;
		boolean bilingual = true;
		String language = "2";
		
		if(bilingual)
			extFilePath = extFilePath + "bilfeatureVectors_language_"+language+"_"+numTopics+".data";
		else
			extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";
			
		
		KLdivergence KL = new KLdivergence(true, "average");
		Kmeans KM=new  Kmeans(10, "./Testdata/dataset/English", "english", KL, seed, useExtPath, extFilePath);
		KM.startClustering();
		SilhouetteCoefficient SC = new SilhouetteCoefficient(KL);
		SC.computeScore(KM);
		
		int i=1;
		for(Cluster cluster : KM.clusters){
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
		
//		double[] nn = new double[]{1,2,3};
//		double[] nn2 = new double[]{3,3,3};
//		AbstractMatrix m = new Matrix(nn,1);
//		AbstractMatrix n = new Matrix(nn2,1);
//		AbstractMatrix mm = new Matrix(0,3);
//		AbstractMatrix cc;
//		mm.insertRowsEquals(mm.getRowDimension(), n);
//		mm.insertRowsEquals(mm.getRowDimension(), m);
////		mm.setColumns(0, m);
//		System.out.println(mm.getColumn(1).plus(mm.getColumn(2)));
////		cc = mm;
//		cc = mm.deleteColumn(0);
//		cc = cc.deleteColumn(1);
//		
//		mm.deleteColumnEquals(1);
//		
//		System.out.println(n.transpose().times(n));
		
//		ArrayList<Integer> list = new ArrayList<Integer>();
//		list.add(4);
//		list.add(2);
//		list.add(3);
////		Collections.sort(list);
//		Collections.reverse(list);
//		System.out.println(list.toString());
		
//		ArrayList<Integer> list = new ArrayList<Integer>();
//		list.add(1);
//		list.add(2);
//		list.add(3);
//		list.add(4);
//		list.add(5);
//		System.out.println(list.toString());
//		Collections.shuffle(list);
//		System.out.println(list.toString());
//		for (int i=0;i<list.size()/2;i++){
//			System.out.println(i);
//		}

	}

}
