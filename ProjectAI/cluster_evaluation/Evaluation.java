package cluster_evaluation;

import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import data_representation.Cluster;

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
//			return confusionMatrix;
		} else {
			System.out.println("Error : No cluster directories in "+clusterDir);
//			return confusionMatrix;
		}
		
		return fillMatrix(clusterMap, classMap);
	}
	
	/**
	 * This method create a confusion matrix between the clusters and the true classes.
	 * The method compute the number of document intersection between cluster and true class 
	 * by comparing the documents name.
	 * @param classDir - directory contains documents with class name as part of documents' name.
	 * @param clusterList - ArrayList<Cluster> list of cluster objects from clustering.
	 * @return HashMap<String cluster, HashMap<String class, Integer intersection>>
	 */
	public static HashMap<String, HashMap<String, Integer>> createConfusionMatrix(String classDir, ArrayList<Cluster> clusterList){
		
		ArrayList<String> listClass= FileLoadingUtils.listFilesName(classDir);
		
		HashMap<String, ArrayList<String>> classMap = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> clusterMap = new HashMap<String, ArrayList<String>>();
		
		if (listClass.size()>0 && clusterList.size()>0){
			for (String name:listClass){
				String label = name.replaceAll(".*_", "");
				label = label.replaceAll("\\..*","");
				label = label.replaceAll("[0-9]","");
				try{
					classMap.get(label).add(name);	
				} catch (Exception e){
					ArrayList<String> s = new ArrayList<String>();
					s.add(name);
					classMap.put(label,s);
				}
			}
			
			for (int i=0;i<clusterList.size();i++){
				clusterMap.put("Topic"+Integer.toString(i),new ArrayList<String>());
				int l = clusterList.get(i).historyMembers.size();
				for (String name :clusterList.get(i).historyMembers.get(l-1)){
					String fileName = name.replaceAll(".*/", "");
					clusterMap.get("Topic"+Integer.toString(i)).add(fileName);	
				}
			}
		} else if (listClass.size()==0){
			System.out.println("Error : There's no files in "+classDir);
//			return confusionMatrix;
		} else {
			System.out.println("Error : There's no cluster ");
//			return confusionMatrix;
		}
		
		return fillMatrix(clusterMap, classMap);		
		
	}
	
	private static HashMap<String, HashMap<String, Integer>> fillMatrix(HashMap<String, ArrayList<String>> clusterMap, HashMap<String, ArrayList<String>> classMap){
		HashMap<String, HashMap<String, Integer>> confusionMatrix = new HashMap<String, HashMap<String, Integer>>();
		HashMap<String, Integer> sumOfColumn = new HashMap<String, Integer>();
		
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
