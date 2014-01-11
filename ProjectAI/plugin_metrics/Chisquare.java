package plugin_metrics;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import data_representation.*;

/**
 * 
 * @author miriamhuijser
 * Class Chisquare provides methods that compute the chisquare distance between 
 * two distributions or finds the closest cluster to a certain document.
 */
public class Chisquare extends Metric{
	boolean relativeFreq;
	int topN;
	
	/**
	 * Constructor
	 * @param relativeFreq - boolean that indicates whether the representation
	 * uses relative frequency/probability or the "normal" frequency of the 
	 * words/wordpairs.
	 */
	public Chisquare(boolean relativeFreq){
		this.relativeFreq = relativeFreq;
	}

	/**
	 * This method initializes topN, which is the number of words that will be
	 * used in the computation of the similarity score. It then calls a method
	 * that actually computes the distance and returns the result.
	 * @param q - first document
	 * @param corpusSizeQ - size of corpus of first document
	 * @param r - second document
	 * @param corpusSizeR - size of corpus of second document
	 * @param topN - number of words that will be used in the computation
	 * @return result - similarity score
	 */
	public double computeDistance(Map<String,Double> q, int corpusSizeQ, 
			Map<String, Double> r, int corpusSizeR, int topN){
		this.topN = topN;
		double result = 0;
		result = computeDistance(q, corpusSizeQ, r, corpusSizeR);
		return result;
	}

	/**
	 * This method determines which method should be used to compute the 
	 * chisquare of two documents, depending on whether the relative frequency
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
			distance = computeChisquare(q, r, topN);
		}
		else{
			distance = computeChisquare(q, corpusSizeQ, r, corpusSizeR, topN);
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
		double bestScore = Double.POSITIVE_INFINITY;
		for(int c = 0; c < clusters.size(); c++){
			double chisquare =  computeDistance(
								clusters.get(c).centroid.distribution,
								clusters.get(c).centroid.distributionSize,
								doc.words,
								doc.corpusSize);
			if( chisquare == bestScore ){
				closestCentroids.add(c);
			}
			else if( chisquare < bestScore ){
				closestCentroids.clear();
				closestCentroids.add(c);
				bestScore = chisquare;
			}
		}

		return closestCentroids;
	}

	/**
	 * This method computes the chisquare similarity between two documents
	 * that are represented using the frequency (not the relative frequency), 
	 * so the corpus size also needs to be provided to this method.
	 * @param freqList1 - first document
	 * @param corpusSize1 - size of corpus of first document
	 * @param freqList2 - second document
	 * @param corpusSize2 - size of corpus of second document
	 * @param topNrevised - number of words used in the computation
	 * @return chiSquare - Chisquare similarity score
	 */
	public static double computeChisquare( Map<String, Double> freqList1, 
			int corpusSize1, Map<String, Double> freqList2, int corpusSize2, 
			int topNrevised ){
		double chiSquare = 0;
		int counter = 0;
		
		for (Entry<String, Double> entry : freqList1.entrySet()){
			counter++;
			String word = entry.getKey();
			double freq1 = entry.getValue() / (double) corpusSize1;
			if( freqList2.containsKey(word) ){
				double freq2 = freqList2.get(word)/ (double) corpusSize2;
				chiSquare = chiSquare + ( Math.pow( (freq1 - freq2), 2 ) / freq1 );
			}
			else chiSquare = chiSquare + freq1;

			if( counter >= topNrevised )
				break;
		}
		return chiSquare;
	}

	/**
	 * This method computes the chisquare similarity between two distributions
	 * (representations where the relative frequency is used).
	 * @param freqList1 - first document
	 * @param freqList2 - second document
	 * @param topNrevised - number of words used in computation
	 * @return chiSquare - chisquare similarity score
	 */
	public static double computeChisquare( Map<String, Double> freqList1,
			Map<String, Double> freqList2, int topNrevised ){
		double chiSquare = 0;
		int counter = 0;
		
		for (Entry<String, Double> entry : freqList1.entrySet()){
			counter++;
			String word = entry.getKey();
			double freq1 = entry.getValue();
			if( freqList2.containsKey(word) ){
				double freq2 = freqList2.get(word);
				chiSquare = chiSquare + (Math.pow((freq1 - freq2), 2) / freq1);
			}
			else chiSquare = chiSquare + freq1;

			if( counter >= topNrevised )
				break;
		}
		return chiSquare;
	}	
}