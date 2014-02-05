package clustering;

import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import plugin_metrics.KLdivergence;
import plugin_metrics.Metric;
import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.ImportExternalDataset;


/**
 * 
 * @author christos
 * Class that implements the fuzzy c-means clustering. It's a simple extension of the kmeans 
 * with a membership function and a fuzziness parameter. The latter controls the uncertainty
 * of the clustering assignments.
 *
 */
public class FuzzyCmeans extends Clustering{
	
	public int c;
	public double m;
	public Metric metric;
	public int iterationsMax = 2500;
	public int topNChisquare = 500;
	public int seed;
	public String language;
	//public String filePath;
	public boolean changes = true;
	public boolean relativeFreq = true;
	ArrayList<Document> documentObjects = new ArrayList<Document>();
	//public ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	Random r = new Random();
	public boolean externalDataset;
	String extFilePath;
	public boolean converged = false;
	public double thres;
	int numTopics;

	public static Map<String, ArrayList<Double>> weights = new HashMap<String, ArrayList<Double>>();
	
	/**
	 * Constructor
	 * @param c - the number of clusters c
	 * @param m - the fuzziness parameter m (m!=1)
	 * @param thres - the threshold for termination (no change at the membership function)
	 * @param filePath - pathname where the documents are
	 * @param language - language of the documents
	 * @param metric - metric used in order to compute the distances
	 * @param seed -  seed for the initialization of the clusters
	 * @param externalDataset - define if you will use external feature vectors
	 * @param extFilePath - the path of the external feature vectors
	 */

	public FuzzyCmeans(int c, double m, double thres, String filePath, String language, Metric metric, int seed, boolean externalDataset, String extFilePath) {
		
		this.c = c;
		this.m = m;
		this.thres = thres;
		this.filePath = filePath;
		this.language = language;
		this.metric = metric;
		this.seed = seed;
		this.externalDataset = externalDataset;
		this.extFilePath = extFilePath;
		this.ID = "FuzzyCmeans-"+c+"-"+m+"-"+metric.ID+"-"+externalDataset+"-"+extFilePath;
	}
	
	public void initWeights(){
		for (Document doc : documentObjects){
			ArrayList<Double> weight = new ArrayList<Double>();
			
			for(int i=0; i< c; i ++){
				weight.add(1/(double)c);
			}
			weights.put(doc.getFilename(), weight);
		}
	}
	
	
	public void startClustering(){
		int iterations = 0;
		// Initialize datapoints and initial centroids
		if(!externalDataset)
			init();
		else
			init_external();
		
		initWeights();
		while( changes && iterations < iterationsMax ){
			assignMembers();
			reestimateCentroids();
			
			System.out.println("Iteration " + iterations);
			changes = false;
			for( int i = 0; i < clusters.size(); i++ ){
				if( iterations == 0 || (!converged && clusters.get(i).hasChanged()) ){
					changes = true;
				}
			}
			iterations++;
		}
		// only show final clusters and not on every iteration
		/*for (int i=0;i<clusters.size();i++){
			System.out.println("cluster " + i );
			System.out.println(clusters.get(i).historyMembers.get(iterations - 1)); 
			System.out.println("");
		}*/
		
	}
	
	public void init(){
		System.out.println("Initializing datapoints and clusters...");
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		Centroid allWords = new Centroid();
		Random r = new Random();
		r.setSeed(seed);
		for( int i = 0; i < documentNames.size(); i++ ){
			//if(i == 200) // in order to test, pick only a small number of documents 
		//		break; 
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
		if( docs < c ){
			c = docs;
			System.out.println("C was too large for the amount of documents, C revised to "+c + " ...");
		}

		for( int i = 0; i < c; i++ ){
			Integer indexNewMean = 0;
			Integer indexNewMean2 = 0;
			do{ 
				indexNewMean = r.nextInt(docs);
				indexNewMean2 = r.nextInt(docs);
			}
			while( (indexNewMean == indexNewMean2) || (!possibleMeanIndices.contains(indexNewMean)) || (!possibleMeanIndices.contains(indexNewMean2)) );
			//System.out.println(indexNewMean + " " + documentObjects.get(indexNewMean).textFile);
			//System.out.println(indexNewMean2 + " " + documentObjects.get(indexNewMean2).textFile);
			//System.out.println("");
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

			Centroid cent = new Centroid(d);
			Cluster cluster = new Cluster(cent);
			clusters.add(cluster);		
		}

		System.out.println("Clusters created...");
		System.out.println("Number of documents to be clustered:"+documentObjects.size()+"\n");
	}

	public void init_external(){
		System.out.println("Initializing datapoints and clusters from external source...");
		Map<String, ArrayList<Double>> dataset = new HashMap<String, ArrayList<Double>>();
		ImportExternalDataset imp = new ImportExternalDataset(extFilePath);
		dataset = imp.importData();
		
		//ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		Centroid allWords = new Centroid();
		Random r = new Random();
		r.setSeed(seed);
		ArrayList<String> documentNames = new ArrayList<String>();
		documentNames.addAll(dataset.keySet());
		
		for( int i = 0; i < documentNames.size(); i++ ){
			//if(i == 200) // in order to test, pick only a small number of documents 
			//	break; 
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
		}
		
		for( int i = 0; i < documentObjects.size(); i++ ){
			documentObjects.get(i).createListExternal(allWords, "forgy", dataset.get(documentObjects.get(i).getFilename()));
			System.out.println("Document parsed...");
			allWords = documentObjects.get(i).initCentroid;	
		}

		int docs = documentObjects.size();
		ArrayList<Integer> possibleMeanIndices = new ArrayList<Integer>();
		for( int i = 0; i < docs; i++ ){
			possibleMeanIndices.add(i);
		}
		if( docs < c ){
			c = docs;
			System.out.println("C was too large for the amount of documents, C revised to "+c + " ...");
		}

		for( int i = 0; i < c; i++ ){
			Integer indexNewMean = 0;
			Integer indexNewMean2 = 0;
			do{ 
				indexNewMean = r.nextInt(docs);
				indexNewMean2 = r.nextInt(docs);
			}
			while( (indexNewMean == indexNewMean2) || (!possibleMeanIndices.contains(indexNewMean)) || (!possibleMeanIndices.contains(indexNewMean2)) );
			//System.out.println(indexNewMean + " " + documentObjects.get(indexNewMean).textFile);
			//System.out.println(indexNewMean2 + " " + documentObjects.get(indexNewMean2).textFile);
			//System.out.println("");
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

			Centroid cent = new Centroid(d);
			Cluster cluster = new Cluster(cent);
			clusters.add(cluster);		
		}

		System.out.println("Clusters created...");
		System.out.println("Number of documents to be clustered:"+documentObjects.size()+"\n");
	}
	
	/**
	 * This method assigns documents to clusters based on the distance function
	 */
	private void assignMembers(){
		for( int c = 0; c < clusters.size(); c++ ){
			clusters.get(c).members.clear();
		}
		
		double change = 0;
		
		for( int i = 0; i < documentObjects.size(); i++ ){
			int bestCluster = metric.getBestCluster(documentObjects.get(i), clusters);
			
			// update weights here
			ArrayList<Double> bestClusterfuzzy = metric.getBestClusterFuzzy(documentObjects.get(i), clusters);
			ArrayList<Double> doc_memberships = weights.get(documentObjects.get(i).getFilename());
			for(int j = 0; j<doc_memberships.size(); j++){
				double dist = bestClusterfuzzy.get(j);
				if (dist == 0.0){ // datapoint is the centroid
					doc_memberships.set(j, 1.0);
					for(int k=0; k<j; k++)
						doc_memberships.set(k, 0.0);
					for(int k=doc_memberships.size() - 1; k>j; k --)
						doc_memberships.set(k, 0.0);
					break;
				}
				else{
					double newWeight = 0;
					
					 for(int k=0; k<doc_memberships.size(); k++)
					 	newWeight += Math.pow(dist/bestClusterfuzzy.get(k),(double)2/(m-1));
					 
					
					newWeight =  1/newWeight;
					change += Math.abs(newWeight - doc_memberships.get(j));
					doc_memberships.set(j, newWeight);
				}
			}
			weights.put(documentObjects.get(i).getFilename(), doc_memberships);
			clusters.get(bestCluster).addMember( documentObjects.get(i) );
		}
		for( int i = 0; i < clusters.size(); i++ ){
			clusters.get(i).updateSizeDistrCentroid();
			clusters.get(i).updateHistory();
		}
		if(change < thres)
			converged = true;
	}
	
	/**
	 * This method reestimates the cluster centroids by computing the average
	 * of its members (the mean).
	 */
	private void reestimateCentroids(){
		for( int i = 0; i < clusters.size(); i++ ){
			clusters.get(i).centroid.distribution = computeMean( clusters.get(i), i );
		}
	}
	
	private Map<String, Double> computeMean( Cluster cluster, int index ){
		Map<String, Double> currentMean = cluster.centroid.distribution;
		Map<String, Double> newMean = new HashMap<String, Double>();
		if( cluster.members.size() > 0 ){
			for( Entry<String, Double> entry:currentMean.entrySet() ){
				String word = entry.getKey();
				double value = 0;
				double normalize = 0;
				for( int i = 0; i < cluster.members.size(); i++ ){
					if( cluster.members.get(i).words.containsKey(word) ){
						double weight = Math.pow(weights.get(cluster.members.get(i).getFilename()).get(index), m);
						value = value + (cluster.members.get(i).words.get(word)*weight);
						normalize += weight;
					}
				}
				value = value / normalize;//(double) cluster.members.size();
				newMean.put(word, value);
			}
		}
		else newMean = currentMean;

		return newMean;
	}
	
	

	public static void main(String[] args) {
		String directory = "features/";
		String fileName = "featureVectors_language_english_10.data";
        String extFilePath = directory+fileName;
		String filePath = "./Testdata/dataset/English";
		String language = "english";
		int c = 10; //total number of domains (true is 9)
		double m = 2; // fuzziness 2/(m-1)
		double thres = Math.pow(10, -3);
		int seed = 1234;
		boolean useExternal = true;
		
		Metric metric = new KLdivergence(true, "average");
		//Metric metric = new JSdivergence(true);
		//Metric metric = new EuclidianDistance(true);
		//Metric metric = new L1norm(true);
		//Metric metric = new JaccardsCoefficient(true);
		//Metric metric = new HellingerFunction(true);
		//Metric metric = new Chisquare(true);
		
		FuzzyCmeans fuzzyCmeans = new FuzzyCmeans(c, m, thres, filePath, language, metric, seed, useExternal, extFilePath);
		fuzzyCmeans.startClustering();
		ArrayList<Cluster> clusters = fuzzyCmeans.clusters;
		
		int i=1;
		for(Cluster cluster : clusters){
			System.out.println("Cluster "+i+":");
			//System.out.println("Centroid:"+cluster.centroid.distribution);
			for(Document doc : cluster.members){
				System.out.println(doc.getFilename());
			}
			i++;
			System.out.println("");
		}
		ArrayList<Double> check = weights.get("English/D_CKTAUS_hardware1.en");
		System.out.println(weights.get("English/D_CKTAUS_hardware1.en"));
		double sum = 0;
		for(double val : check)
			sum += val;
		System.out.println(sum);
	}

}
