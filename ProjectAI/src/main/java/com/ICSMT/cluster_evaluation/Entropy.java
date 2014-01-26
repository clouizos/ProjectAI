package cluster_evaluation;

import java.util.HashMap;

import clustering.Clustering;
import io.IOFile;

public class Entropy extends ExtrinsicEvaluation{
	/**
	 * @param args
	 */
	
	@Override
	public void computeScore(Clustering C) {
		HashMap<String, HashMap<String, Integer>> confMatrix = createConfusionMatrix(C.filePath, C.clusters);
		compute(confMatrix);
		IOFile io = new IOFile();
		io.openWriteFile("Entropy.csv");
		io.write(C.ID);
		io.write(",");
		io.write(Double.toString(this.score));
		io.write("\n");
		io.close();
	}
	
	public void compute(HashMap<String, HashMap<String, Integer>>confMatrix){
		
		if (confMatrix.size()==0){return;}
		
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
				if (precision>0)
					entropy += -(precision*Math.log(precision));
			}
			
			System.out.println("Evaluation on Cluster "+cluster);
			System.out.println("Entropy : "+entropy);
			totalEntropy+=(confMatrix.get(cluster).get("total")/totalDocs)*entropy;
		}
		System.out.println("Entropy total : "+totalEntropy);
		this.score = totalEntropy;
		

		
	}
	
	
//	public static void main(String[] args) {
//		String classDir = "../TESTEVAL/TrueClass";
//		String clusterDir = "../TESTEVAL/Cluster";
////		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterDir);
//		HashMap<String, HashMap<String, Integer>> confMatrix=new HashMap<String,HashMap<String,Integer>>();
//		//Count entropy for each cluster and entropy for all cluster
//		double totalEntropy=0;
//		double totalDocs = confMatrix.get("total").get("total");
//		for (String cluster : confMatrix.keySet()){
//			if (cluster=="total"){ continue; }
//			double entropy=0;
//			for (String label : confMatrix.get(cluster).keySet()){
//				if (label=="total"){ continue; }
//				double tp = confMatrix.get(cluster).get(label);
//				double totalInCluster = confMatrix.get(cluster).get("total");
//				double precision = Math.max(Double.MIN_VALUE, tp/totalInCluster);
//				entropy+= -precision*Math.log(precision);
//			}
//			System.out.println("Evaluation on Cluster "+cluster);
//			System.out.println("Entropy : "+entropy);
//			totalEntropy+=(confMatrix.get(cluster).get("total")/totalDocs)*entropy;
//		}
//		System.out.println("Entropy total : "+totalEntropy);
//	}

}
