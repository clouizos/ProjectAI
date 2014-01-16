package cluster_evaluation;

import java.util.HashMap;

public class Entropy {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String classDir = "../TESTEVAL/TrueClass";
		String clusterDir = "../TESTEVAL/Cluster";
		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterDir);

		//Count entropy for each cluster and entropy for all cluster
		double totalEntropy=0;
		double totalDocs = confMatrix.get("total").get("total");
		for (String cluster : confMatrix.keySet()){
			if (cluster=="total"){ continue; }
			double entropy=0;
			for (String label : confMatrix.get(cluster).keySet()){
				if (label=="total"){ continue; }
				double tp = confMatrix.get(cluster).get(label);
				double totalInCluster = confMatrix.get(cluster).get("total");
				double precision = tp/totalInCluster;
				entropy+= -precision*Math.log(precision);
			}
			System.out.println("Evaluation on Cluster "+cluster);
			System.out.println("Entropy : "+entropy);
			totalEntropy+=(confMatrix.get(cluster).get("total")/totalDocs)*entropy;
		}
		System.out.println("Entropy total : "+totalEntropy);
	}

}
