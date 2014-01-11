package plugin_metrics;

import java.util.Map.Entry;
import java.util.Map;
import java.util.HashMap;
import data_representation.*;
import java.util.ArrayList;
/**
 * 
 * @author miriamhuijser
 * Class JSdivergence provides methods that compute the Jensen-Shannon divergence
 * between two distributions or finds the closest cluster to a certain document.
 */
public class JSdivergence extends Metric{
	boolean relativeFreq;
	
	/**
	 * Constructor
	 * @param relativeFreq - boolean that indicates whether the representation
	 * uses relative frequency/probability or the "normal" frequency of the 
	 * words/wordpairs.
	 */
	public JSdivergence(boolean relativeFreq){
		this.relativeFreq = relativeFreq;
	}

	/**
	 * This method determines which method should be used to compute the JS-
	 * divergence between two documents, depending on whether the relative frequency
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
			distance = computeJSdivergence(q, corpusSizeQ, r, corpusSizeR, 
					relativeFreq);
		}
		else{
			distance = computeJSdivergence(q, corpusSizeQ, r, corpusSizeR);
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
		double bestDivergence = Double.POSITIVE_INFINITY;
		for( int c = 0; c < clusters.size(); c++ ){
			double divergence = computeDistance( 
								clusters.get(c).centroid.distribution,
								clusters.get(c).centroid.distributionSize,
								doc.words,
								doc.corpusSize
							);
			if( divergence == bestDivergence ){
				closestCentroids.add(c);
			}
			else if( divergence < bestDivergence ){
				closestCentroids.clear();
				closestCentroids.add(c);
				bestDivergence = divergence;
			}
		}
		return closestCentroids;
	}	

	/**
	 * This method computes the JS divergence between two distributions
	 * (representations where the relative frequency is used).
	 * @param q - first document
	 * @param sizeQ - corpus size of first document
	 * @param r - second document
	 * @param sizeR - corpus size of second document
	 * @param relativeFreq - representation with relative frequency is used
	 * @return
	 */
	public static double computeJSdivergence(  Map<String, Double> q, int sizeQ, 
			Map<String, Double> r, int sizeR, boolean relativeFreq ){
		double result = 0;
		Map<String, Double> average = createAverage(q, r);
		int sizeAverage = (sizeQ + sizeR) / 2;
		double klDivQ = KLdivergence.computeKLdivergence(q, sizeQ, average, 
				sizeAverage, true );
		double klDivR = KLdivergence.computeKLdivergence(r, sizeR, average, 
				sizeAverage, true );
		result = 0.5 * (klDivQ + klDivR);
		return result;
	}

	/**
	 * This method computes the JS divergence between two documents that are
	 * represented with the counts (frequency of words). 
	 * @param q - first document
	 * @param sizeQ - corpus size of first document
	 * @param r - second document
	 * @param sizeR - corpus size of second document
	 * @return result - similarity score
	 */
	public static double computeJSdivergence(  Map<String, Double> q, int sizeQ,
			Map<String, Double> r, int sizeR ){
		double result = 0;
		Map<String, Double> average = createAverage(q, r);
		int sizeAverage = (sizeQ + sizeR) / 2;
		double klDivQ = KLdivergence.computeKLdivergence(q, sizeQ, average,
				sizeAverage );
		double klDivR = KLdivergence.computeKLdivergence(r, sizeR, average, 
				sizeAverage );
		result = 0.5 * (klDivQ + klDivR);
		
		return result;
	}

	/**
	 * This method creates an average distribution between the two distributions
	 * given as input.
	 * @param q - first distribution (document)
	 * @param r - second distribution (document)
	 * @return average - average distribution between the two input distributions
	 */
	private static Map<String, Double> createAverage( Map<String, Double> q,
			Map<String, Double> r ){
		Map<String, Double> average = new HashMap<String, Double>();
		for( Entry<String, Double> entry:q.entrySet()){
			double count = entry.getValue() * 0.5;
			average.put(entry.getKey(), count);
		}
		for( Entry<String, Double> entry:r.entrySet()){
			String word = entry.getKey();
			double count = entry.getValue() * 0.5;
			if( average.containsKey( word )){
				count = count + average.get(word);
			}
			average.put(word, count);
		}

		return average;
	}
}