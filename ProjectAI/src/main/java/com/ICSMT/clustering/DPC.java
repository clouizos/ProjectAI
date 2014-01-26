package clustering;

import gov.sandia.cognition.learning.algorithm.clustering.DirichletProcessClustering;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.GaussianCluster;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorEntry;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.statistics.bayesian.DirichletProcessMixtureModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.ImportExternalDataset;

public class DPC extends Clustering{
	public ArrayList<Document> documentObjects = new ArrayList<Document>();
	String extFilePath;
	String language;
	int numInitClusters;
	int numTopics;
	int seed;
	int numIterPerSample;
	Random random;
	double initAlpha;
	boolean recalcAlpha;
	int burnIn;
	
	/**
	 * Dirichlet Process Mixture clustering, with the Chinese Restaurant Process as the prior on clustering.
	 * 
	 * @param numInitClusters - initial estimate of the clusters (can change)
	 * @param filePath - filePath that will be used for the evaluation
	 * @param language - language of the documents being clustered
	 * @param numIterPerSample - How many iterations the Gibbs sampler will perform for each data point
	 * @param initAlpha - initial alpha, parameter of the Dirichlet distribution
	 * @param recalcAlpha - Define if you want the alpha to adapt, or stay fixed
	 * @param burnIn - Number of iterations before the sampler starts saving the results
	 * @param numTopics - number of dimensions at the feature vectors (needed in order to convert to the format the library needs)
	 * @param seed - seed of the random number generator
	 * @param bilingual - if bilingual documents are being used
	 * @param extFilePath - the file path of the external dataset
	 */
	
	public DPC(int numInitClusters, String filePath, String language, int numIterPerSample, double initAlpha, boolean recalcAlpha, int burnIn, int numTopics, int seed, boolean bilingual, String extFilePath) {
		this.numInitClusters = numInitClusters;
		this.numTopics = numTopics;
		this.seed = seed;
		this.filePath = filePath;
		this.extFilePath = extFilePath;
		this.numIterPerSample = numIterPerSample;
		random = new Random(seed);
		this.initAlpha = initAlpha;
		this.recalcAlpha = recalcAlpha;
		this.burnIn = burnIn;
		if(!bilingual)
			this.language = language;
		else
			this.language = "both";
		
		//if(!bilingual)
		//	extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";
		//else
		//	extFilePath = extFilePath + "bilfeatureVectors_language_"+this.language+"_"+numTopics+".data";
		System.out.println(extFilePath);
		this.ID = "DPC-"+numInitClusters+"-"+numTopics+"-"+numIterPerSample+"-"+burnIn+"-"+initAlpha+"-"+recalcAlpha+"-"+extFilePath;

	}
	
	public void startClustering(){
		// parse the external dataset
		init_external();
				
		ArrayList<Vector> data = new ArrayList<Vector>();
		
		// perform the connection between filenames and Vector representations
		Map<String, String> inv_mapping = new HashMap<String,String>();
		
		for (Document doc : documentObjects){
			Map<String, Double> words = doc.words;
			Vector data_point = VectorFactory.getDefault().createVector(numTopics);
			double val = 0;
			for(int i=0; i< numTopics; i++){
				val = words.get(Integer.toString(i));
				data_point.setElement(i, val);
			}
			//System.out.println(doc.getFilename() +" "+ data_point);
			inv_mapping.put(data_point.toString(), doc.getFilename());
			data.add(data_point);
		}
		
		DirichletProcessClustering dpc = new DirichletProcessClustering();
		DirichletProcessMixtureModel<Vector> alg = dpc.getAlgorithm();
		
		// recalcAlpha false for best results at monolingual
		// best alpha for monolingual is 100 (too much probably, why?)
		
		System.out.println("Initial Parameters:");
		System.out.println("numInitClusters: "+numInitClusters+", numIterations: "+numIterPerSample+", recalcAlpha: "+recalcAlpha+", initAlpha: "+initAlpha+", burnIn: "+burnIn);
		System.out.println();
		
		alg.setNumInitialClusters(numInitClusters);
		alg.setIterationsPerSample(numIterPerSample);
		alg.setRandom(random);
		alg.setReestimateAlpha(recalcAlpha);
		alg.setInitialAlpha(initAlpha);
		alg.setBurnInIterations(burnIn);
		
        dpc.learn(data);
        
        //DirichletProcessMixtureModel<Vector> alg2 = dpc.getAlgorithm();
        
        
        //System.out.println("NumInit: "+alg.getNumInitialClusters());
        ArrayList<GaussianCluster> res = dpc.getResult();
        
        for(int i=0; i<res.size(); i++){
        	Centroid dummycent = new Centroid();
        	Cluster cluster = new Cluster(dummycent);
        	
        	// set the mean of the Gaussian as the cluster centroid
        	Vector mean = res.get(i).getGaussian().getMean();
        	Map<String, Double> cent_dist = new HashMap<String, Double>();
          	Iterator<VectorEntry> it = mean.iterator();
          	int j = 0;
          	while(it.hasNext()){
          		cent_dist.put(Integer.toString(j), it.next().getValue());
          		j++;
          	}
          	cluster.centroid.distribution = cent_dist;
        	
          	
          	// add the members at the clusters
        	ArrayList<Vector> members = res.get(i).getMembers();
        	for(Vector member : members){
        		String filename = inv_mapping.get(member.toString());
              	Document doc = new Document(filename, language);
              	cluster.addMember(doc);
        	}
        	clusters.add(cluster);
        	
        }
		
	}
	
	public void init_external(){
		System.out.println("Creating external dataset...");
		//String options = "featureVectors_language_"+language+"_"+numTopics+".data";
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
		}
		
		// dummy centroid in order to use the method to get the normalized distributions
		Centroid dummyCentroid = new Centroid();
		for( int i = 0; i < documentObjects.size(); i++ ){
			// get the normalized distribution
			documentObjects.get(i).createListExternal( dummyCentroid, "forgy", dataset.get(documentObjects.get(i).getFilename()) );
			//System.out.println("Document parsed...");
			//System.out.println(words_test.toString());
			//allWords = documentObjects.get(i).initCentroid;	
		}
		System.out.println("Finished parsing the documents...");
		System.out.println("Number of documents to be clustered:"+documentObjects.size()+"\n");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
