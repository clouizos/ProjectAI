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
		int numTopics = 10;
		int seed = 20;
		boolean bilingual = true;
		String language = "english";
		
		// FCM parameter
		double fuzziness = 2;
		double thres = Math.pow(10, -5);

		// DBSCAN parameter
		boolean removeSingleton = true;
		int minPts = 5;
		double distThres = 0.3;
		boolean smartInit = true;
		int nrdocsinit = 40;
		String choice = "minimum";
		int nrdocs = 400;

		// GMM parameters
		int EMiter = 300;
		double tolerance = Double.MIN_VALUE;
		int numComponents = 10;

		// DPC parameters
		int numInitClusters = 100;
		int numIterPerSample = 10;
		double initAlpha = 100.0;
		boolean recalcAlpha = true;
		int burnIn = 5;
		
		
		String pathEval = "";
		
		if(bilingual) {
			pathEval = "./Testdata/dataset/Both";
			language = "both";
		} else
			pathEval = "./Testdata/dataset/English";
		
		//if(bilingual)
			//extFilePath = extFilePath + "bilfeatureVectors_language_"+language+"_"+numTopics+".data";
		//	extFilePath = extFilePath + "features_lda_lsa_"+numTopics+".data";
		//else
		//	extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";
			
//		extFilePath = extFilePath + "featureVectors_language_english_20.data";
		extFilePath = extFilePath + "2monolsa_50_d_nn.data";
		//extFilePath = extFilePath + "lsa_50_English_nn.data";
		
		Metric[] metrics = new Metric[6];
		metrics[0] = new KLdivergence(true, "average");
		//metrics[0] = Metric metric = new KLdivergence(true, "minimum");
		metrics[1] = new JSdivergence(true);
		metrics[2] = new HellingerFunction(true);
		metrics[3] = new JaccardsCoefficient(true);
		metrics[4] = new EuclidianDistance(true);
		metrics[5] = new L1norm(true);
		
		for(Metric metric : metrics) {
		Kmeans clusterer = new  Kmeans(10, pathEval, language, metric, seed, useExtPath, extFilePath);
		//FuzzyCmeans clusterer = new FuzzyCmeans(10, fuzziness, thres, pathEval, language, metric, seed, useExtPath, extFilePath);
		//DBScan clusterer = new DBScan(minPts, distThres, pathEval, language, metric, seed, nrdocs, removeSingleton, useExtPath, extFilePath, smartInit, nrdocsinit, choice);
		
		//GMM clusterer = new GMM(numComponents, pathEval, language, EMiter, tolerance, numTopics, seed, bilingual, extFilePath);
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
}
