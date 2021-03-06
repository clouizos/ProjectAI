/**
 * 
 */
package clustering;

import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;

import metrics.KLdivergenceMetric;
import plugin_metrics.KLdivergence;
import plugin_metrics.Metric;
import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.ImportExternalDataset;

/**
 * @author christos
 * Class DBScan provides methods for clustering documents with the DBScan 
 * algorithm. It was based on the pseudocode available at Wikipedia.
 * One provides this class with two parameters, minPts which defines the minimum number
 * of points in order to form a cluster and 
 * eps which defines the threshold on the distance for a point to be density-reachable,
 * the pathname where the to be clustered documents are, and optionally the language of 
 * the documents (Then a shortlist can be used).
 * After creating a DBScan object, the startClustering() method has to be 
 * called in order to start the clustering process.
 */
public class DBScan extends Clustering{
	
	public int minPts;
	public int nrdocs;
	public double eps;
	public Metric metric;
	public KLdivergenceMetric kldiv;
	public int iterationsMax = 2500;
	public int topNChisquare = 500;
	public String language;
	//public String filePath;
	public boolean changes = true;
	public boolean relativeFreq = true;
	ArrayList<Document> documentObjects = new ArrayList<Document>();
	public Map<Document, Boolean> visited = new HashMap<Document, Boolean>();
	public static Map<Document, Boolean> noise = new HashMap<Document, Boolean>();
	//public static ArrayList<Cluster> clusters = new ArrayList<Cluster>();
	Random r = new Random();
	public int seed;
	boolean removeSingle;
	boolean useExternal;
	String extFilePath;
	int numTopics;
	boolean smartInit; 
	int nrdocsinit;
	String choice;


	/**
	 * Constructor
	 * @param minPts - number of required points in order to form a cluster
	 * @param eps - threshold for density-reachable points
	 * @param filePath - pathname where documents are
	 * @param language - language of the documents (or null)
	 * @param metric - the (distance) metric used. See method
	 * @param seed - not used, put a dummy one
	 * @param nrdocs - nr of docs to be clustered
	 * @param removeSingle - define if you want to remove singleton clustering results
	 * @param useExternal - define if you will use external feature vectors
	 * @param extFilePath - File path of the external dataset
	 * @param smartinit - define if you want to initialize the eps relatively to the
	 * pairwise distances of the first documents
	 * @param nrdocsinit - number of documents used for the initialization
	 * @param choice - choice for the initialization (average, median, minimum)
	 * getClosestCluster() for more information on the numbers 
	 * for the different metrics.
	 */
	public DBScan(int minPts, double eps, String filePath, String language, Metric metric, int seed, int nrdocs, boolean removeSingle, boolean useExternal, String extFilePath, boolean smartInit, int nrdocsinit, String choice) {
		this.filePath = filePath;
		this.language = language;
		this.metric = metric;
		this.minPts = minPts;
		this.eps = eps;
		this.seed = seed;
		this.nrdocs = nrdocs;
		this.removeSingle = removeSingle;
		this.useExternal = useExternal;
		this.extFilePath = extFilePath;
		this.ID = "DBSCAN-"+minPts+"-"+eps+"-"+metric.ID+"-"+useExternal+"-"+extFilePath;
		this.smartInit = smartInit;
		this.nrdocsinit = nrdocsinit;
		this.choice = choice;
	}

	/**
	 * This method starts the clustering process by first creating the dataset
	 * (documents). Afterwards it performs the clustering process by first starting with an
	 * arbitrary starting point that has not been visited. 
	 * This point's eps-neighborhood is retrieved, and if it contains sufficiently many points, 
	 * a cluster is started. Otherwise, the point is labeled as noise. Note that this point might 
	 * later be found in a sufficiently sized eps-environment of a different point and hence 
	 * be made part of a cluster.
	 * 
	 * DBSCAN(D, eps, MinPts)
	 * C = 0
	 * for each unvisited point P in dataset D
	 * 		mark P as visited
	 * 		NeighborPts = regionQuery(P, eps)
	 * 		if sizeof(NeighborPts) < MinPts
	 * 			mark P as NOISE
	 * 		else
	 * 			C = next cluster
	 * 			expandCluster(P, NeighborPts, C, eps, MinPts)
	 */
	public void startClustering(){
		if(!useExternal)
			init();
		else
			init_external();

		if(smartInit){
			smartIniteps(documentObjects, nrdocsinit, this.choice);
			System.out.println("Automatically initialized eps at: " + this.eps);
			this.ID = "DBScan-"+this.minPts+"-"+this.eps+"-"+this.metric.ID+"-"+this.useExternal;

		}
		Centroid dummycent = new Centroid();
		
		for(int i=0; i< documentObjects.size(); i++){
			Document doc = documentObjects.get(i);
			if(!visited.get(doc)){
				visited.put(doc, true);
			
				ArrayList<Document> neighboors = new ArrayList<Document>();
				neighboors = regionQuery(doc);
				if (neighboors.size() < minPts)
					noise.put(doc, true);
				else{
					System.out.println("Creating new cluster...");
					Cluster cluster = new Cluster(dummycent);
					clusters.add(cluster);
					expandCluster(doc,cluster,neighboors);
				}
			}
		}
		
		if(removeSingle)
			removeSingletonClusters();
		int total_clustered =0;
		for(int i=0;i<clusters.size();i++)
			total_clustered += clusters.get(i).members.size();

		this.ID = "DBScan-"+this.minPts+"-"+this.eps+"-"+this.metric.ID+"-"+this.useExternal+"-"+this.extFilePath+"-"+(nrdocs-total_clustered);

	}
	
	/**
	* Iteratively expand the cluster based on the point neighborhood
	*  
	* expandCluster(P, NeighborPts, C, eps, MinPts)
	*   add P to cluster C
	*   for each point P' in NeighborPts 
	*      if P' is not visited
	*         mark P' as visited
	*         NeighborPts' = regionQuery(P', eps)
	*         if sizeof(NeighborPts') >= MinPts
	*            NeighborPts = NeighborPts joined with NeighborPts'
	*      if P' is not yet member of any cluster
	*         add P' to cluster C
	*          
	*/
	
	public void expandCluster(Document document, Cluster cluster, ArrayList<Document> neighboors){
		System.out.println("Expanding the cluster...");
		cluster.addMember(document);
		for(int i=0; i<neighboors.size(); i++){
			Document doc = neighboors.get(i);
			// if not visited
			if(!visited.get(doc)){
				visited.put(doc, true);
				ArrayList<Document> neighboors_new = regionQuery(doc);
				if (neighboors_new.size() >=  minPts){
					for(int j=0; j < neighboors_new.size(); j++){
						if (!neighboors.contains(neighboors_new.get(j))){
							neighboors.add(neighboors_new.get(j));
						}
					}
				}
			}
			
			boolean contained = false;
			for(int k=0; k<clusters.size(); k++){
				if(clusters.get(k).members.contains(doc)){
					contained = true;
				}
			}
			
			if(!contained)
				cluster.addMember(doc);
			
		}
	}
	
	/**
	 * return all points within document's eps-neighborhood (including document)
	 * @param document - the document to find neighborhood
	 * @param eps - threshold on distance for the neighborhood
	 * @return an array list of document objects	 
	 */
	
	public ArrayList<Document> regionQuery(Document document){
		//System.out.println("Performing a regionQuery...");
		ArrayList<Document> neighbors = new ArrayList<Document>();
		neighbors.add(document);
		double distance = 0;

		for(int i=0; i<documentObjects.size(); i++){
			distance = metric.computeDist(document.words, document.corpusSize, documentObjects.get(i).words, documentObjects.get(i).corpusSize);
			if(distance <= eps){
				neighbors.add(documentObjects.get(i));
			}
		}
		return neighbors;
		
	}
	
	private void removeSingletonClusters(){
	// remove empty clusters (it can happen, dbscan can 'steal' points of a cluster and put it in another, if they are near each other)
		for(int i=clusters.size()-1; i>=0; i--){
			
			if (clusters.get(i).members.size() == 0) 
				clusters.remove(i);
			if(true)		// if you wanna remove clusters with only one point(document)
				if(clusters.get(i).members.size() == 1)
					clusters.remove(i);
		}
	}
	
	/**
	 * This method initializes the dataset (documents) 
	 */
	public void init(){
		System.out.println("Creating dataset...");
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		for( int i = 0; i < documentNames.size(); i++ ){
			//if(i == nrdocs) // in order to test, pick only a small number of documents 
			//	break; 
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
			visited.put(doc, false);
		}
		
		// dummy centroid in order to use the method to get the normalized distributions
		Centroid dummyCentroid = new Centroid();
		for( int i = 0; i < documentObjects.size(); i++ ){
			// get the normalized distribution
			documentObjects.get(i).createList( dummyCentroid, "forgy" );
			//System.out.println("Document parsed...");
				
		}
		System.out.println("Finished parsing the documents...");
		System.out.println("Number of documents to be clustered:"+documentObjects.size()+"\n");
	}
	
	public void init_external(){
		System.out.println("Creating external dataset...");
		
		Map<String, ArrayList<Double>> dataset = new HashMap<String, ArrayList<Double>>();
		ImportExternalDataset imp = new ImportExternalDataset(extFilePath);
		dataset = imp.importData();
		
		ArrayList<String> documentNames = new ArrayList<String>();
		documentNames.addAll(dataset.keySet());
		
		for( int i = 0; i < documentNames.size(); i++ ){
			//if(i == nrdocs) // in order to test, pick only a small number of documents 
			//	break; 
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
			visited.put(doc, false);
		}
		
		// dummy centroid in order to use the method to get the normalized distributions
		Centroid dummyCentroid = new Centroid();
		for( int i = 0; i < documentObjects.size(); i++ ){
			// get the normalized distribution
			documentObjects.get(i).createListExternal( dummyCentroid, "forgy", dataset.get(documentObjects.get(i).getFilename()) );
			//System.out.println("Document parsed...");

		}
		System.out.println("Finished parsing the documents...");
		System.out.println("Number of documents to be clustered:"+documentObjects.size()+"\n");
	}

	public void smartIniteps(ArrayList<Document> documentObjects, int nravg, String choice){
		ArrayList<Double> eps_to_check = new ArrayList<Double>();
		double avg_eps = 0.0;
		int totDiss = 0;
		for (int i=0; i<nravg; i++){
			Document doc1 = documentObjects.get(i);
			Document doc2 = documentObjects.get(i+1);
			totDiss ++;
			eps_to_check.add(metric.computeDist(doc1.words, doc1.corpusSize, doc2.words, doc2.corpusSize));
		}

		Collections.sort(eps_to_check);
		
		
		for(int i=0; i<eps_to_check.size(); i++){
			avg_eps += eps_to_check.get(i);
		}
		
		if(choice.equals("average"))
			this.eps = avg_eps/totDiss; //return average
		else if (choice.equals("minimum"))
			this.eps = eps_to_check.get(0);		// return minimum
		//this.eps = eps_to_check.get(0)*this.minPts;	// return minimum*required minPts
		else if (choice.equals("median"))
			this.eps = eps_to_check.get(eps_to_check.size()/2); //return median
	}
	
	public static void main(String[] args){
		String filePath = "./Testdata/dataset/English"; // directory of english dataset // changed according to new structure
		String language = "english"; // using shortlist for english language
		boolean useExtPath = false;
		
		Metric m7 = new KLdivergence(true, "minimum");
		ArrayList<Metric> metrics = new ArrayList<Metric>();
		metrics.add(m7);
		int minPts = 5;
		double eps = 2.0;
		int seed = 1234;
		int nrdocs = 139;
		boolean removeSingle = true;
		String directory = "features/";
		String fileName = "featureVectors_language_english_20.data";
        String extFilePath = directory+fileName;
		
		DBScan dbscan = new DBScan(minPts, eps, filePath, language, metrics.get(0), seed, nrdocs, removeSingle, useExtPath, extFilePath, true, 10, "minimum");
		dbscan.startClustering();
		System.out.println("Finished clustering!\n");
		
		// remove empty clusters (it can happen, dbscan can 'steal' points of a cluster and put it in another, if they are near each other)
		/*for(int i=clusters.size()-1; i>=0; i--){
			//System.out.println(clusters.get(i).members.size());
			if (clusters.get(i).members.size() == 0) 
				clusters.remove(i);
			if(removeSingleton)		// if you wanna remove clusters with only one point(document)
				if(clusters.get(i).members.size() == 1)
					clusters.remove(i);
		}*/
		
		// show clusters and their members
		for(int i=0; i< dbscan.clusters.size(); i++){
			System.out.println("cluster " + i );
			for(int j = 0; j< dbscan.clusters.get(i).members.size(); j++){
				System.out.println(dbscan.clusters.get(i).members.get(j).textFile);
			}
			System.out.println("");
		}
		
		// find total number of clustered points, as well as points treated as noise
		int tot_clustered_points = 0;
		for (int j =0; j<dbscan.clusters.size(); j++){
			tot_clustered_points += dbscan.clusters.get(j).members.size();
		}
		
		System.out.println("Total number of clustered points:"+tot_clustered_points);
		System.out.println("Total number of clusters:"+dbscan.clusters.size());
		System.out.println("Total number noise points(singleton clusters):"+(nrdocs - tot_clustered_points));
		
		for (Document doc: noise.keySet()) 
			System.out.println(doc.textFile); 	
		
	}

}
