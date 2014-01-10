package clustering;

import io.FileLoadingUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import plugin_metrics.*;
import data_representation.*;


/**
 * @author miriamhuijser
 * Class Kmeans provides methods for clustering documents with the Kmeans 
 * algorithm. 
 * One provides this class with the desired amount of clusters k, the pathname
 * where the to be clustered documents are, and optionally the language of 
 * the documents (Then a shortlist can be used).
 * After creating a Kmeans object, the startClustering() method has to be 
 * called in order to start the clustering process.
 */
public class Kmeans{
	public int k;
	public Metric metric;
	public int iterationsMax = 2500;
	public int topNChisquare = 500;
	public int seed;
	public String language;
	public String filePath;
	public boolean changes = true;
	public boolean relativeFreq = true;
	ArrayList<Document> documentObjects = new ArrayList<Document>();
	public ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	Random r = new Random();

	/**
	 * Constructor
	 * @param k - desired number of clusters
	 * @param filePath - pathname where documents are
	 * @param language - language of the documents (or null)
	 * @param metric - the (distance) metric used. See method 
	 * getClosestCluster() for more information on the numbers 
	 * for the different metrics.
	 */
	public Kmeans( int k, String filePath, String language, Metric metric, int seed){
		this.k = k;
		this.filePath = filePath;
		this.language = language;
		this.metric = metric;
		this.seed = seed;
	}

	/**
	 * This method starts the clustering process by initializing the datapoints
	 * (documents) and the k initial centroids. It then assigns documents to
	 * their closest cluster and reestimates the cluster centroids based on
	 * the new assignment of the documents. It repeats this assignment-
	 * reestimation task until certain conditions are met.
	 * This method prints the member-assignment results.
	 */
	public void startClustering(){
		int iterations = 0;
		// Initialize datapoints and initial centroids
		init();

		while( changes && iterations < iterationsMax ){
			assignMembers();
			reestimateCentroids();
			
			System.out.println("Iteration " + iterations);
			changes = false;
			for( int i = 0; i < clusters.size(); i++ ){
				if( iterations == 0 || clusters.get(i).hasChanged() ){
					changes = true;
				}
				System.out.println("cluster " + i );
				System.out.println(clusters.get(i).historyMembers.get(iterations));
				System.out.println("");
			}
			iterations++;
		}
	}

	/**
	 * This method initializes the datapoints (documents) and the initial k
	 * clusters by randomly taking k documents as the initial centroids
	 * (Forgy Method)
	 */
	public void init(){
		System.out.println("Initializing datapoints and clusters...");
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		Centroid allWords = new Centroid();
		Random r = new Random();
		r.setSeed(seed);
		for( int i = 0; i < documentNames.size(); i++ ){
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
		}
		for( int i = 0; i < documentObjects.size(); i++ ){
			documentObjects.get(i).createList( allWords, "forgy" );
			System.out.println("Document parsed...");
			allWords = documentObjects.get(i).initCentroid;
		}

		int docs = documentObjects.size();
		ArrayList<Integer> possibleMeanIndices = new ArrayList<Integer>();
		for( int i = 0; i < docs; i++ ){
			possibleMeanIndices.add(i);
		}
		if( docs < k ){
			k = docs;
			System.out.println("K was too large for the amount of documents, K revised to "+k + " ...");
		}

		for( int i = 0; i < k; i++ ){
			Integer indexNewMean = 0;
			Integer indexNewMean2 = 0;
			do{ 
				indexNewMean = r.nextInt(docs);
				indexNewMean2 = r.nextInt(docs);
			}
			while( (indexNewMean == indexNewMean2) || (!possibleMeanIndices.contains(indexNewMean)) || (!possibleMeanIndices.contains(indexNewMean2)) );
			System.out.println(indexNewMean + " " + documentObjects.get(indexNewMean).textFile);
			System.out.println(indexNewMean2 + " " + documentObjects.get(indexNewMean2).textFile);
			System.out.println("");
			possibleMeanIndices.remove(indexNewMean);
			possibleMeanIndices.remove(indexNewMean2);
			Map<String, Double> newMean = documentObjects.get(indexNewMean).words;
			Map<String, Double> newMean2 = documentObjects.get(indexNewMean2).words;
			Map<String, Double> d = new HashMap<String, Double>(allWords.distribution);

			for( Entry<String, Double> entry:newMean.entrySet() ){
				String key = entry.getKey();
				Double value = entry.getValue() * 0.5;
				d.put(key, value);
			}
			for( Entry<String, Double> entry: newMean2.entrySet() ){
				String key = entry.getKey();
				Double value = entry.getValue() * 0.5;
				if( d.get(key) > 0 ){
					value = value + d.get(key);
				}
				d.put(key, value);
			}

			Centroid c = new Centroid(d);
			Cluster cluster = new Cluster(c);
			clusters.add(cluster);		
		}

		System.out.println("Clusters created...");
	}

/*	public void init(){
		System.out.println("Initializing datapoints and clusters...");
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		Centroid allWords = new Centroid();
		for( int i = 0; i < documentNames.size(); i++ ){
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
		}
		for( int i = 0; i < documentObjects.size(); i++ ){
			Map<String, Double> list = documentObjects.get(i).createList( allWords, null );
			System.out.println("Document parsed...");
			allWords = documentObjects.get(i).initCentroid;
		}
		if( documentObjects.size() < k ){
			k = documentObjects.size();
			System.out.println("K was too large for the amount of documents, K revised to "+k + " ...");
		}
		
		Random random = new Random();
		random.setSeed(123456);
		ArrayList<Map<String, Double>> initMeans = new ArrayList<Map<String, Double>>();
		for( int i = 0; i < k; i++){
			Map<String, Double> m = new HashMap<String, Double>();
			initMeans.add(m);
		}
		for( Entry<String, Double> entry:allWords.distribution.entrySet() ){
			for( int i = 0; i < initMeans.size(); i++ ){
				initMeans.get(i).put(entry.getKey(), random.nextDouble() );
			}
		}
		for( int i = 0; i < initMeans.size(); i++ ){
			Centroid c = new Centroid(initMeans.get(i));
			Cluster cluster = new Cluster(c);
			clusters.add(cluster);
		}

		System.out.println("Clusters created...");
	}*/

	/**
	 * This method assigns documents to clusters based on the distance function
	 */
	private void assignMembers(){
		for( int c = 0; c < clusters.size(); c++ ){
			clusters.get(c).members.clear();
		}

		for( int i = 0; i < documentObjects.size(); i++ ){
			int bestCluster = metric.getBestCluster(documentObjects.get(i), clusters);
			clusters.get(bestCluster).addMember( documentObjects.get(i) );
		}
		for( int i = 0; i < clusters.size(); i++ ){
			clusters.get(i).updateSizeDistrCentroid();
			clusters.get(i).updateHistory();
		}
	}

	/**
	 * This method reestimates the clustercentroids by computing the average
	 * of its members (the mean).
	 */
	private void reestimateCentroids(){
		for( int i = 0; i < clusters.size(); i++ ){
			clusters.get(i).centroid.distribution = computeMean( clusters.get(i) );
		}
	}

	/**
	 * This method computes the mean of the cluster it receives as input by
	 * computing the average of its members.
	 * @param cluster - a cluster of which to compute the mean
	 * @return newMean - the newly computed mean of the cluster
	 */
	private Map<String, Double> computeMean( Cluster cluster ){
		Map<String, Double> currentMean = cluster.centroid.distribution;
		Map<String, Double> newMean = new HashMap<String, Double>();
		if( cluster.members.size() > 0 ){
			for( Entry<String, Double> entry:currentMean.entrySet() ){
				String word = entry.getKey();
				double value = 0;
				for( int i = 0; i < cluster.members.size(); i++ ){
					if( cluster.members.get(i).words.containsKey(word) ){
						value = value + cluster.members.get(i).words.get(word);
					}
				}
				value = value / (double) cluster.members.size();
				newMean.put(word, value);
			}
		}
		else newMean = currentMean;

		return newMean;
	}
}