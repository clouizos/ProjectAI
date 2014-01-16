package cluster_evaluation;

import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluation {
	
	/**
	 *  Compute find intersection of filenames of two lists
	 *  Return the intersection lists
	 */
	public static <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<T>();
        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }
	
	/**
	 * This method create a confusion matrix between the clusters 
	 * and the true classes.
	 * @param classDir - directory contains subdirectories of true class.
	 * @param clusterDir - directory contains subdirectories of clustesr.
	 * @return HashMap<String cluster, HashMap<String class, Integer intersection>>
	 */
	public static HashMap<String, HashMap<String, Integer>> createConfusionMatrix(String classDir, String clusterDir){
		HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> sumOfColumn = new HashMap<String, Integer>(); 
		
		String[] listClass= FileLoadingUtils.listDirectoriesDirectory(classDir);
		String[] listClusters = FileLoadingUtils.listDirectoriesDirectory(clusterDir);
		
		HashMap<String, ArrayList<String>> classMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> clusterMap = new HashMap<String, ArrayList<String>>();
		
		for (String label : listClass){
			classMap.put(label, FileLoadingUtils.listFilesName(classDir+"/"+label));
		}
		
		for (String cluster : listClusters){
			clusterMap.put(cluster, FileLoadingUtils.listFilesName(clusterDir+"/"+cluster));
		}
		
		for (String cluster:clusterMap.keySet()){
			confusionMatrix.put(cluster, new HashMap<String, Integer>());
			for (String label:classMap.keySet()){
				List<String> intersect = intersection(classMap.get(label), clusterMap.get(cluster));
				int count = intersect.size();
				confusionMatrix.get(cluster).put(label, count);
				sumOfColumn.put(label,classMap.get(label).size());
			}
			confusionMatrix.get(cluster).put("total", clusterMap.get(cluster).size());
		}
		confusionMatrix.put("total", sumOfColumn);
		return confusionMatrix;
	}
	
	public static double F1Score(double prec, double rec){
		return 2*prec*rec/(prec+rec);
	}

	class Metrics{
		
	}
	
	public static void main(String[] args) {
		String classDir = "../TrueLabel";
		String clusterDir = "../MeasuredLabel";
		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterDir);
		//Count the pricision and recall
		//For each cluster, we choose the corresponding class that gives
		//the F1-Score.
		for (String cluster : confMatrix.keySet()){
			double maxF1=0;
			String maxLabel="";
			for (String label : confMatrix.get(cluster).keySet()){
				double tp = confMatrix.get(cluster).get(label);
				double totalInCluster = confMatrix.get(cluster).get("total");
				double totalInClass = confMatrix.get("total").get(label);
				double precision = tp/totalInCluster;
				double recall = tp/totalInClass;
				double F1 = 2*precision*recall/(precision+recall);
				if (F1>=maxF1){
					maxLabel = label;
				}
//				System.out.println("Cluster : "+cluster+" Class : "+label+" Value : "+confMatrix.get(cluster).get(label));
			}
		}
		
		
		
	}

}
