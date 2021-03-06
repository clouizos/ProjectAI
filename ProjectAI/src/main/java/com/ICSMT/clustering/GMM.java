package clustering;

import gov.sandia.cognition.learning.algorithm.clustering.KMeansClusterer;
import gov.sandia.cognition.learning.algorithm.clustering.KMeansClustererWithRemoval;
import gov.sandia.cognition.learning.algorithm.clustering.KMeansFactory;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.CentroidCluster;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.ClusterCreator;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.GaussianCluster;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.GaussianClusterCreator;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.VectorMeanCentroidClusterCreator;
import gov.sandia.cognition.learning.algorithm.clustering.divergence.ClusterCentroidDivergenceFunction;
import gov.sandia.cognition.learning.algorithm.clustering.divergence.ClusterDivergenceFunction;
import gov.sandia.cognition.learning.algorithm.clustering.divergence.GaussianClusterDivergenceFunction;
import gov.sandia.cognition.learning.algorithm.clustering.divergence.CentroidClusterDivergenceFunction;
import gov.sandia.cognition.learning.algorithm.clustering.initializer.GreedyClusterInitializer;
import gov.sandia.cognition.learning.algorithm.clustering.initializer.NeighborhoodGaussianClusterInitializer;
import gov.sandia.cognition.learning.function.distance.CosineDistanceMetric;
import gov.sandia.cognition.math.DivergenceFunction;
import gov.sandia.cognition.math.Semimetric;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorEntry;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.Vectorizable;
import gov.sandia.cognition.statistics.distribution.MixtureOfGaussians;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian;
import gov.sandia.cognition.statistics.distribution.MultivariateGaussian.WeightedMaximumLikelihoodEstimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.ImportExternalDataset;

/**
 * 
 * @author christos
 * Class that wraps the mixture of Gaussians clustering, through the cognitive foundry toolkit
 *
 */
public class GMM extends Clustering{
	
	public ArrayList<Document> documentObjects = new ArrayList<Document>();
	String extFilePath;
	String language;
	int numComponents;
	int numTopics;
	int seed;
	int EMiter;
	double tolerance;
	
	/**
	 *  Mixture of Gaussians clustering, trained with the Expectation-Maximization
	 *  
	 * @param numComponents - the number of Gaussians to fit to the data
	 * @param filePath - the filepath used for the evaluation
	 * @param language - the language of the documents
	 * @param EMiter - the iterations for the Expectation-Maximization
	 * @param tolerance - the stoping criterion
	 * @param numTopics - the dimensionality of the training set
	 * @param seed - the seed for the random generator
	 * @param bilingual - define if you want bilingual document clustering
	 * @param extFilePath - the path of the external dataset
	 */
	
	public GMM(int numComponents, String filePath, String language, int EMiter, double tolerance, int numTopics, int seed, boolean bilingual, String extFilePath){
		this.numComponents = numComponents;
		this.numTopics = numTopics;
		this.seed = seed;
		this.filePath = filePath;
		this.extFilePath = extFilePath;
		this.EMiter = EMiter;
		this.tolerance = tolerance;

		if(!bilingual)
			this.language = language;
		else
			this.language = "both";
		
		//if(!bilingual)
		//	extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";
		//else
		//	extFilePath = extFilePath + "bilfeatureVectors_language_"+this.language+"_"+numTopics+".data";
		System.out.println(extFilePath);
		this.ID = "GMM-"+numComponents+"-"+numTopics+"-"+EMiter+"-"+extFilePath;

		
	}
	
	public void startClustering(){
		init_external();
		
		Random random = new Random(seed);
		
		ArrayList<Vector> data = new ArrayList<Vector>();
		
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
		
		
		
		MixtureOfGaussians.EMLearner softLearner = new MixtureOfGaussians.EMLearner( numComponents, random );
		softLearner.setMaxIterations(EMiter);
		softLearner.setTolerance(tolerance);
		MixtureOfGaussians.PDF learnedMixture = softLearner.learn(data);
		
      for(int i=0; i<learnedMixture.getDistributionCount(); i++){
      	Centroid cent = new Centroid();
      	Cluster cluster = new Cluster(cent);
      	// set the mean of the gaussian as the centroid
      	Vector mean = learnedMixture.getDistributions().get(i).getMean();
      	Map<String, Double> cent_dist = new HashMap<String, Double>();
      	Iterator<VectorEntry> it = mean.iterator();
      	int j = 0;
      	while(it.hasNext()){
      		cent_dist.put(Integer.toString(j), it.next().getValue());
      		j++;
      	}
      	cluster.centroid.distribution = cent_dist;
      	clusters.add(cluster);
      }
      
      int assignment = 0;
      System.out.println("Soft assignments at clusters:");
      System.out.println();
      for (Vector vec : data){
      	//System.out.println(learnedMixture.computeRandomVariableProbabilities(vec).length);
      	
    	// get the most probable cluster
      	assignment = learnedMixture.getMostLikelyRandomVariable(vec);
      	
      	String filename = inv_mapping.get(vec.toString());
      	Document doc = new Document(filename,"english");
      	
      	double[] probs = learnedMixture.computeRandomVariableProbabilities(vec);
      	System.out.print(filename+": ");
      	for(int i=0; i<probs.length; i++){
      		System.out.print(probs[i]+" ");
      	}
      	
      	System.out.println();
      	clusters.get(assignment).addMember(doc);
      }
//      
//      int j = 0;
//      for(Cluster cluster : clusters){
//      	System.out.println("Cluster: "+j);
//      	for (Document doc : cluster.members){
//      		System.out.println(doc.getFilename());
//      	}
//      	j++;
//      	System.out.println();
//      }
      
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
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
