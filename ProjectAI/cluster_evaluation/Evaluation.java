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
		
		if (listClass.length>0 && listClusters.length>0){
			for (String label : listClass){
				classMap.put(label, FileLoadingUtils.listFilesName(classDir+"/"+label));
			}	
			
			for (String cluster : listClusters){
				clusterMap.put(cluster, FileLoadingUtils.listFilesName(clusterDir+"/"+cluster));
			}
		} else if (listClass.length==0){
			System.out.println("Error : No class directories in "+classDir);
			return confusionMatrix;
		} else {
			System.out.println("Error : No cluster directories in "+clusterDir);
			return confusionMatrix;
		}
		
				
		int totalDocs=0;
		for (String cluster:clusterMap.keySet()){
			confusionMatrix.put(cluster, new HashMap<String, Integer>());
			for (String label:classMap.keySet()){
				List<String> intersect = intersection(classMap.get(label), clusterMap.get(cluster));
				int count = intersect.size();
				confusionMatrix.get(cluster).put(label, count);
				sumOfColumn.put(label,classMap.get(label).size());
			}
			confusionMatrix.get(cluster).put("total", clusterMap.get(cluster).size());
			totalDocs+=clusterMap.get(cluster).size();
		}
		sumOfColumn.put("total",totalDocs);
		confusionMatrix.put("total", sumOfColumn);
		return confusionMatrix;
	}
	
	


	

}
