package plugin_metrics;

import java.util.ArrayList;
import java.util.Map;
import data_representation.*;

/**
 * 
 * @author miriamhuijser
 * Class Metric is an abstract class for a similarity metric with a method
 * that computes the distance between two distributions and a method that
 * computes the closest centroids to a document. This class provides an 
 * implemented method that determines the closest cluster to a document.
 */
public abstract class Metric{
	abstract double computeDistance(Map<String, Double> q, int corpusSizeQ, 
			Map<String, Double> r, int corpusSizeR);
	abstract ArrayList<Integer> getClosestCentroids(Document doc, 
			ArrayList<Cluster> clusters);

	/**
	 * This method determines the closest cluster for a document by computing
	 * the distance to each of the clusters' centroids. It then returns
	 * the index of the closest cluster.
	 * @param doc - document for which closest cluster needs to be determined
	 * @param clusters - clusters from which closest cluster to document
	 * needs to be determined
	 * @return bestCluster - index of closest cluster to the document
	 */
	public int getBestCluster(Document doc, ArrayList<Cluster> clusters){
		int bestCluster = 0;
		ArrayList<Integer> closestCentroid = getClosestCentroids(doc, clusters);

		// Determine closest centroid from the resulting list
		int numberMembers = Integer.MAX_VALUE;
		for( int c:closestCentroid ){
			if( clusters.get(c).members.size() < numberMembers ){
				bestCluster = c;
				numberMembers = clusters.get(c).members.size();
			}
		}
		return bestCluster;
	}
}