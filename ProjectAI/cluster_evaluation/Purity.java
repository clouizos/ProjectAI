package cluster_evaluation;

import java.util.HashMap;

import clustering.Clustering;

public class Purity extends ExtrinsicEvaluation{

	/**
	 * @param args
	 */
	
	@Override
	public void computeScore(Clustering C) {
		HashMap<String, HashMap<String, Integer>> confMatrix = createConfusionMatrix(C.filePath, C.clusters);
		compute(confMatrix);
	}
	
	public static void compute(HashMap<String, HashMap<String, Integer>>confMatrix){
		
		if (confMatrix.size()==0){return;}
		
		//Count the pricision and recall
				//For each cluster, we choose the corresponding class that gives
				//the F1-Score.
				double totalDocs = confMatrix.get("total").get("total");
				double totalPurity = 0;
				for (String cluster : confMatrix.keySet()){
					
					if (cluster=="total"){ continue; }
					
					double maxPurity=0;
					String maxLabel="";
					
					for (String label : confMatrix.get(cluster).keySet()){
						
						if (label=="total"){ continue; }
						
						double tp = confMatrix.get(cluster).get(label);
						double totalInCluster = confMatrix.get(cluster).get("total");
						double purity = tp/totalInCluster;
						if (purity>=maxPurity){
							maxPurity = purity;
							maxLabel = label;
						}
						
					}
					System.out.println("Evaluation on Cluster "+cluster);
					System.out.println("Coresponding True Class "+maxLabel);
					System.out.println("Purity : "+maxPurity);
					System.out.println();
					
					totalPurity+=(confMatrix.get(cluster).get("total")/totalDocs)*maxPurity;
				}
				
				System.out.println("Purity of Clustering : "+totalPurity);
		
	}
	
//	public static void main(String[] args) {
//		String classDir = "../TESTEVAL/TrueClass";
//		String clusterDir = "../TESTEVAL/Cluster";
////		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterDir);
//		HashMap<String, HashMap<String, Integer>> confMatrix=new HashMap<String,HashMap<String,Integer>>();
//		
//		if (confMatrix.size()==0){return;}
//		
//		//Count the pricision and recall
//		//For each cluster, we choose the corresponding class that gives
//		//the F1-Score.
//		double totalDocs = confMatrix.get("total").get("total");
//		double totalPurity = 0;
//		for (String cluster : confMatrix.keySet()){
//			
//			if (cluster=="total"){ continue; }
//			
//			double maxPurity=0;
//			String maxLabel="";
//			
//			for (String label : confMatrix.get(cluster).keySet()){
//				
//				if (label=="total"){ continue; }
//				
//				double tp = confMatrix.get(cluster).get(label);
//				double totalInCluster = confMatrix.get(cluster).get("total");
//				double purity = tp/totalInCluster;
//				if (purity>=maxPurity){
//					maxPurity = purity;
//					maxLabel = label;
//				}
//				
//			}
//			System.out.println("Evaluation on Cluster "+cluster);
//			System.out.println("Coresponding True Class "+maxLabel);
//			System.out.println("Purity : "+maxPurity);
//			System.out.println();
//			
//			totalPurity+=(confMatrix.get(cluster).get("total")/totalDocs)*maxPurity;
//		}
//		
//		System.out.println("Purity of Clustering : "+totalPurity);
//	}


}
