package clustering;

import gov.sandia.cognition.learning.algorithm.clustering.DirichletProcessClustering;
import gov.sandia.cognition.learning.algorithm.clustering.cluster.GaussianCluster;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.ImportExternalDataset;

public class DPC extends Clustering{
	public ArrayList<Document> documentObjects = new ArrayList<Document>();
	String extFilePath;
	String language;
	int numComponents;
	int numTopics;
	int seed;
	
	public DPC(int numComponents, String filePath, String language, int numTopics, int seed, boolean bilingual, String extFilePath) {
		this.numComponents = numComponents;
		this.numTopics = numTopics;
		this.seed = seed;
		this.filePath = filePath;
		this.extFilePath = extFilePath;
		
		if(!bilingual)
			this.language = language;
		else
			this.language = "both";
		
		//if(!bilingual)
		//	extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";
		//else
		//	extFilePath = extFilePath + "bilfeatureVectors_language_"+this.language+"_"+numTopics+".data";
		System.out.println(extFilePath);
	}
	
	public void startClustering(){
		
		init_external();
		
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
		
		DirichletProcessClustering dpc = new DirichletProcessClustering();
		//dpc.setMaxIterations(2000);
        dpc.learn(data);
        
        ArrayList<GaussianCluster> res = dpc.getResult();
        
        for(int i=0; i<res.size(); i++){
        	Centroid dummycent = new Centroid();
        	Cluster cluster = new Cluster(dummycent);
        	
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
