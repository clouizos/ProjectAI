package cluster_evaluation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data_representation.Cluster;

import io.*;

/**
 * 
 * @author 
 * Assume we have the true cluster of documents, then this class will compute 
 * the precision of clusters resulted by clustering process.
 */
public class Precision {
	
	
	/**
	 * @param args
	 */
	public static void computeF1(String classDir, String clusterDir){
		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterDir);
		compute(confMatrix);
	}
	
	public static void computeF1(String classDir, ArrayList<Cluster> clusterList){
		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterList);
		compute(confMatrix);
	}
	
	private static void compute(HashMap<String, HashMap<String, Integer>>confMatrix){
		if (confMatrix.size()==0){return;}
		//Count the pricision and recall
		//For each cluster, we choose the corresponding class that gives
		//the F1-Score.
		double totalDocs = confMatrix.get("total").get("total");
		double totalPrecision = 0;
		double totalRecall = 0;
		double totalF1 = 0;
		for (String cluster : confMatrix.keySet()){
			if (cluster=="total"){ continue; }
			double maxF1=0;
			double maxPrec=0;
			double maxRecall=0;
			String maxLabel="";
			for (String label : confMatrix.get(cluster).keySet()){
				if (label=="total"){ continue; }
				double tp = confMatrix.get(cluster).get(label);
				double totalInCluster = confMatrix.get(cluster).get("total");
				double totalInClass = confMatrix.get("total").get(label);
				double precision = tp/totalInCluster;
				double recall = tp/totalInClass;
				double F1 = 2*precision*recall/(precision+recall);
				if (F1>=maxF1){
					maxF1 = F1;
					maxPrec = precision;
					maxRecall = recall;
					maxLabel = label;
				}
				
//				System.out.println("Cluster : "+cluster+" Class : "+label+" Value : "+confMatrix.get(cluster).get(label));
			}
			System.out.println("Evaluation on Cluster "+cluster);
			System.out.println("Coresponding True Class "+maxLabel);
			System.out.println("Precision : "+maxPrec);
			System.out.println("Recall : "+maxRecall);
			System.out.println("F1 score : "+maxF1);
			System.out.println();
			
			//
			totalPrecision+=(confMatrix.get(cluster).get("total")/totalDocs)*maxPrec;
			totalRecall+=(confMatrix.get(cluster).get("total")/totalDocs)*maxRecall;
			totalF1+=(confMatrix.get(cluster).get("total")/totalDocs)*maxF1;
		}
		
		System.out.println("Evaluation on total Cluster :");
		System.out.println("Precision : "+totalPrecision);
		System.out.println("Recall : "+totalRecall);
		System.out.println("F1 score : "+totalF1);
	}
	
	public static void main(String[] args) {
		String classDir = "../TESTEVAL/TrueClass";
		String clusterDir = "../TESTEVAL/Cluster";
		
	}

}
