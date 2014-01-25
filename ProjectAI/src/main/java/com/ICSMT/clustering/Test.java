package clustering;

import plugin_metrics.*;
import cluster_evaluation.DBIndex;
import cluster_evaluation.Entropy;
import cluster_evaluation.Precision;
import cluster_evaluation.Purity;
import cluster_evaluation.SilhouetteCoefficient;
import data_representation.Cluster;
import data_representation.Document;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String extFilePath = "./src/main/java/com/ICSMT/features/";
		boolean useExtPath = true;
		int numTopics = 20;
		int seed = 20;
		boolean bilingual = false;
		String language = "English";
		
		// DBSCAN parameter
		boolean removeSingleton = true;
		
		// GMM parameters
		int EMiter = 300;
		double tolerance = Double.MIN_VALUE;
		
		// DPC parameters
		int numInitClusters = 20;
		int numIterPerSample = 10;
		double initAlpha = 2.0;
		boolean recalcAlpha = true;
		int burnIn = 2;
		
		
		String pathEval = "";
		
		if(language.equals("both"))
			pathEval = "./target/Testdata/dataset/Both";
		else
			pathEval = "./target/Testdata/dataset/English";
		/*
		if(bilingual)
			//extFilePath = extFilePath + "bilfeatureVectors_language_"+language+"_"+numTopics+".data";
			extFilePath = extFilePath + "features_lda_lsa_"+numTopics+".data";
		else
			extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";*/
			
//		extFilePath = extFilePath + "featureVectors_language_english_20.data";
		extFilePath = extFilePath + "features_lsa_English_100.data";
		//extFilePath = extFilePath + "features_cca_lsa_100.data";
		
		//Metric metric = new KLdivergence(true, "minimum");
		//Metric metric = new KLdivergence(true, "average");
		//Metric metric = new JSdivergence(true);
		//Metric metric = new HellingerFunction(true);
		//Metric metric = new JaccardsCoefficient(true);
		
		Metric metric  = new EuclidianDistance(true);
		//Metric metric = new L1norm(true);
		
		Kmeans clusterer = new  Kmeans(10, pathEval, language, metric, seed, useExtPath, extFilePath);
		//FuzzyCmeans clusterer = new FuzzyCmeans(10, 2, 0.001, pathEval, language, metric, seed, useExtPath, extFilePath);
		//DBScan clusterer = new DBScan(5, 0.5, pathEval, language, metric, seed, 400, removeSingleton, useExtPath, extFilePath);
		//GMM clusterer = new GMM(10, pathEval, language, EMiter, tolerance, numTopics, seed, bilingual, extFilePath);
		//DPC clusterer = new DPC(numInitClusters, pathEval, language, numIterPerSample, initAlpha, recalcAlpha, burnIn, numTopics, seed, bilingual, extFilePath);
		//AssignHighest clusterer = new AssignHighest(pathEval, language, numTopics, bilingual, extFilePath);
		
		
		clusterer.startClustering();
		
		
		int i=0;
		for(Cluster cluster : clusterer.clusters){
			System.out.println("Cluster "+i+":");
			//System.out.println("Centroid:"+cluster.centroid.distribution);
			for(Document doc : cluster.members){
				System.out.println(doc.getFilename());
			}
			i++;
			System.out.println("");
		}
		 
		
		// intrinsic evaluation
		SilhouetteCoefficient SC = new SilhouetteCoefficient(metric);
		DBIndex DB = new DBIndex(metric);
				
		SC.computeScore(clusterer);
		System.out.println("");
		DB.computeScore(clusterer);
		System.out.println("");
		
		// extrinsic evaluation
		Precision evaluate = new Precision();
		Purity pure = new Purity();
		Entropy entr = new Entropy();
		
		evaluate.computeScore(clusterer);
		System.out.println("");
		pure.computeScore(clusterer);
		System.out.println("");
		entr.computeScore(clusterer);
		
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
