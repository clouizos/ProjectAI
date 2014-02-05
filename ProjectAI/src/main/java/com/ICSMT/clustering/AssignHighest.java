package clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.ImportExternalDataset;

/**
 * 
 * @author christos
 * Dummy clustering class that assigns documents to the clusters according to the most probable
 * topics from the LDA.
 *
 */
public class AssignHighest extends Clustering{
	
	public ArrayList<Document> documentObjects = new ArrayList<Document>();
	String extFilePath;
	String language;
	int numTopics;
	
	/**
	 * Constructor
	 * @param filePath - the filepath of the documents
	 * @param language - the language of the documents
	 * @param numTopics - the number of topics(the dimensionality of the feature vectors)
	 * @param bilingual - define if you will use bilingual data or not
	 * @param extFilePath - the file path of the external feature vectors
	 */

	public AssignHighest(String filePath, String language, int numTopics, boolean bilingual, String extFilePath) {
		this.numTopics = numTopics;
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
		String best = "";
		double value = 0.0;
		
		for(int i=0; i<numTopics; i++){
			Centroid dummycent = new Centroid();
			Cluster cluster = new Cluster(dummycent);
			clusters.add(cluster);
		}
		
		for(Document doc : documentObjects){
			Map<String, Double> words = doc.words;
			
			
			for(String key : words.keySet()){
				double temp = words.get(key);
				if(temp > value){
					value = temp;
					best = key;
				}
			}
			
			int index = Integer.parseInt(best);
			System.out.println(doc.getFilename()+" "+best+": "+value);
			
			clusters.get(index).addMember(doc);
			
		}
		
		// remove empty clusters, needed in case the topics are more than the true domains
		for(int i=clusters.size()-1; i>-1; i--){
			ArrayList<Document> members = clusters.get(i).members;
			if(members.size() == 0)
				clusters.remove(i);
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

		}
		System.out.println("Finished parsing the documents...");
		System.out.println("Number of documents to be clustered:"+documentObjects.size()+"\n");
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
