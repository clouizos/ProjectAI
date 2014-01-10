package plugin_metrics;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import data_representation.*;
/**
 * 
 * @author miriamhuijser
 * Class EuclidianDistance provides methods that compute the distance between two 
 * distributions or finds the closest cluster to a certain document.
 */
public class EuclidianDistance extends Metric{
	boolean relativeFreq;

	/**
	 * Constructor
	 * @param relativeFreq - boolean that indicates whether the representation
	 * uses relative frequency/probability or the "normal" frequency of the 
	 * words/wordpairs.
	 */
	public EuclidianDistance(boolean relativeFreq){
		this.relativeFreq = relativeFreq;
	}

	/**
	 * This method determines which method should be used to compute the euclidian
	 * distance between two documents, depending on whether the relative frequency
	 * (or probability) is used or the mere frequency of words. In that case
	 * the corpus sizes of the two documents will be used in the computation.
	 * @param q - first document
	 * @param corpusSizeQ - corpus size of first document
	 * @param r - second document
	 * @param corpusSizeR - corpus size of second document
	 * @return distance - resulting similarity score
	 */
	public double computeDistance(Map<String, Double> q, int corpusSizeQ, 
			Map<String, Double> r, int corpusSizeR){
		double distance = 0;
		if( relativeFreq ){
			distance = computeEuclidianDistance(q, r);
		}
		else{
			distance = computeEuclidianDistance(q, corpusSizeQ, r, corpusSizeR);
		}
		return distance;
	}

	/**
	 * This method determines the closest centroids for a document and returns
	 * a list of the corresponding indices in the clusters-list.
	 * @param doc - document for which the closest centroids need to be 
	 * determined
	 * @param clusters - list of clusters
	 * @return closestCentroids - list of indices corresponding to the closest
	 * centroids.
	 */
	public ArrayList<Integer> getClosestCentroids(Document doc, 
			ArrayList<Cluster> clusters){
		ArrayList<Integer> closestCentroids = new ArrayList<Integer>();
		double bestDistance = Double.POSITIVE_INFINITY;
		for(int c = 0; c < clusters.size(); c++){
			double euclidian =  computeDistance(
								clusters.get(c).centroid.distribution,
								clusters.get(c).centroid.distributionSize,
								doc.words,
								doc.corpusSize
								);
			if( euclidian == bestDistance ){
				closestCentroids.add(c);
			}
			else if( euclidian < bestDistance ){
				closestCentroids.clear();
				closestCentroids.add(c);
				bestDistance = euclidian;
			}
		}
		return closestCentroids;
	}		

	/**
	 * This method computes the euclidian distance between two documents
	 * that are represented using the relative frequencies of the words.
	 * @param q - first document
	 * @param r - second document
	 * @return result - similarity score
	 */
	public static double computeEuclidianDistance( Map<String, Double> q, 
			Map<String, Double> r){
		double result = 0;
		ArrayList<String> vocab = new ArrayList<String>();
		for( Entry<String, Double> entry:q.entrySet() ){
			result = result + calculateDistanceWord(entry.getKey(), q, r);
			vocab.add(entry.getKey());
		}
		for( Entry<String, Double> entry:r.entrySet() ){
			if( !vocab.contains(entry.getKey()) ){
				result = result + calculateDistanceWord(entry.getKey(), q, r);
			}
		}
		result = Math.sqrt(result);
		
		return result;	
	}

	/**
	 * This method computes the distance between two documents that are 
	 * represented using the frequency, or count, of the words, rather than
	 * their relative frequencies.
	 * @param q - first document
	 * @param sizeQ - corpus size of first document
	 * @param r - second document
	 * @param sizeR - corpus size of second document
	 * @return result - similarity score
	 */
	public static double computeEuclidianDistance( Map<String, Double> q, 
			int sizeQ, Map<String, Double> r, int sizeR ){
		double result = 0;
		ArrayList<String> vocab = new ArrayList<String>();
		for( Entry<String, Double> entry:q.entrySet() ){
			result = result + calculateDistanceWord(entry.getKey(), q, sizeQ, 
					r, sizeR );
			vocab.add(entry.getKey());
		}
		for( Entry<String, Double> entry:r.entrySet() ){
			if( !vocab.contains(entry.getKey()) ){
				result = result + calculateDistanceWord(entry.getKey(), q, sizeQ, 
						r, sizeR);
			}
		}
		result = Math.sqrt(result);	
			
		return result;	
	}

	/**
	 * This method computes the distance for a word between two documents.
	 * @param word
	 * @param q - first document
	 * @param r - second document
	 * @return result - distance
	 */
	private static double calculateDistanceWord(String word, Map<String,
			Double> q, Map<String, Double> r){
		double result = 0;
		double qFreq = 0;
		double rFreq = 0;
		if( q.containsKey(word) ){
			qFreq = q.get(word);
		}
		if( r.containsKey(word) ){
			rFreq = r.get(word);
		}
		double difference = qFreq - rFreq;
		result = Math.pow(difference, 2);
	
		return result;
	}

	/**
	 * This method computes the distance for one word between two documents
	 * @param word
	 * @param q - first document
	 * @param sizeQ - corpus size first document
	 * @param r - second document
	 * @param sizeR - corpus size second document
	 * @return result - distance
	 */
	private static double calculateDistanceWord(String word, 
			Map<String, Double> q, int sizeQ, Map<String, Double> r, int sizeR){
		double result = 0;
		double qFreq = 0;
		double rFreq = 0;
		
		if( q.containsKey(word) ){
			qFreq = q.get(word) / (double) sizeQ;
		}
		if( r.containsKey(word) ){
			rFreq = r.get(word) / (double) sizeR;
		}
		double difference = qFreq - rFreq;
		result = Math.pow(difference, 2);
		
		return result;
	}
}