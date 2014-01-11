package clustering;

import data_representation.*;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * 
 * @author miriamhuijser
 * Class SingleLinkClustering provides methods for the single link clustering
 * algorithm, which is a hierarchical clustering algorithm.
 */
public class SingleLinkClustering{
	String matrixFile;
	String documentNamesFile;
	int numberOfDocuments;
	boolean lowScoreIsSimilar;
	ArrayList<Integer> indicesMinDistanceDocs;
	ArrayList<HierarchicalCluster> clusters;
	public AdjacencyMatrix matrix;
	PrintWriter writer;

	/**
	 * Constructor
	 * @param matrixFile - the name of the .csv file containing the matrix
	 * with the similarity scores
	 * @param documentNamesFile - the name of the file that contains the 
	 * names of the documents
	 * @param numberOfDocuments - the number of documents in the data set
	 * @param lowScoreIsSimilar - boolean that indicates whether similarity
	 * metric is used that assigns low scores to highly similar documents.
	 * (e.g. this is the case for metrics that compute the distance)
	 */
	public SingleLinkClustering( String matrixFile, String documentNamesFile, 
			int numberOfDocuments, boolean lowScoreIsSimilar ){
		this.matrixFile = matrixFile;
		this.numberOfDocuments = numberOfDocuments;
		this.documentNamesFile = documentNamesFile;
		this.lowScoreIsSimilar = lowScoreIsSimilar;		
	}

	/**
	 * This method initializes the adjacency matrix, a list with the indices of 
	 * the smallest similarity scores to the largest similarity scores and 
	 * the clusters. (Initially, those are unary clusters. Each cluster
	 * contains exactly 1 document)
	 */
	public void init(){
		matrix = new AdjacencyMatrix(matrixFile, documentNamesFile, 
				numberOfDocuments, lowScoreIsSimilar);
		matrix.init();
		indicesMinDistanceDocs = matrix.getIndicesMinDistanceDocs();
		clusters = new ArrayList<HierarchicalCluster>();

		// Create unary cluster for every document
		for( int i = 0; i < matrix.documentList.size(); i++ ){
			HierarchicalCluster c = 
					new UniCluster( matrix.documentList.get(i), i );
			clusters.add(c);
		}
	}

	/**
	 * This method starts the clustering. It looks for the smallest similarity
	 * score, find the documents that correspond to this similarity score,
	 * and unites the clusters of which the documents are members. It repeats
	 * this until there is one cluster left, containing a layered representation
	 * of all the other clusters. It then prints the corresponding dendrogram, 
	 * which visualises this layering of clusters. 
	 */
	public void startClustering(){
		for( int i = 0; i < indicesMinDistanceDocs.size(); i++){
			int arrayIndex = indicesMinDistanceDocs.get(i);
			double minDistance = matrix.matrixValues.get(arrayIndex);
			ArrayList<String> docs = matrix.arrayIndexToDocumentNames(arrayIndex);
			String doc1 = docs.get(0);
			String doc2 = docs.get(1);
			int indexCluster1 = getCluster(doc1);
			int indexCluster2 = getCluster(doc2);
			if( indexCluster1 != indexCluster2 ){
				HierarchicalCluster cluster1 = clusters.get(indexCluster1);
				HierarchicalCluster cluster2 = clusters.get(indexCluster2);
				HierarchicalCluster newCluster = 
						new BinaryCluster(cluster1, cluster2, minDistance );
				clusters.add(newCluster);
				clusters.remove(cluster1);
				clusters.remove(cluster2);
			}
		}
		
		String fileNameOutput = matrixFile+"-Clustering.txt";
		try{
			writer = new PrintWriter(fileNameOutput, "UTF-8");
			String indent = "||||";
			HierarchicalCluster root = clusters.get(0);
			printDendrogram(indent, root);
			writer.close();
		} catch(IOException e){
			System.err.println(e.getMessage());
		}
	}

	/**
	 * This method returns the cluster(index) to which the input is a member
	 * @param member
	 * @return
	 */
	public int getCluster(String member){
		int clusterIndex = -1;

		for( int i = 0; i < clusters.size(); i++ ){
			if( clusters.get(i).members.contains(member) ){
				clusterIndex = i;
			}
		}

		return clusterIndex;
	}

	/**
	 * This method prints the dendrogram corresponding to the clustering
	 * process. It does this in a recursive manner.
	 * @param indent - the indentation printed each time a new cluster is formed
	 * @param cluster - cluster to be printed.
	 */
	public void printDendrogram(String indent, HierarchicalCluster cluster ){
		if( cluster.isBinaryCluster() ){
			BinaryCluster c = (BinaryCluster) cluster;
			HierarchicalCluster c1 = c.cluster1;
			HierarchicalCluster c2 = c.cluster2;
			if( c1.minDistance > c2.minDistance ){
				printDendrogram( indent+"|", c1 );
				writer.println(indent+"||"+c.minDistance);
				printDendrogram( indent+"|", c2 );				
			}
			else{
				printDendrogram( indent+"|", c2 );
				writer.println(indent+"||"+c.minDistance);
				printDendrogram( indent+"|", c1 );							
			}
		}
		// Unicluster is reached (single document/leaf)
		else{
			UniCluster c = (UniCluster) cluster;
			String name = c.docName;
			writer.println(indent+"-Observation: "+ name);		
		}
	}
}