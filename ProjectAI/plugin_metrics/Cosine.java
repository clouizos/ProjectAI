package plugin_metrics;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import data_representation.*;

/**
 * 
 * @author miriamhuijser
 * Class Cosine provides methods that compute the cosing of the angle between two 
 * distributions or finds the closest cluster to a certain document.
 */
public class Cosine extends Metric{
	boolean relativeFreq;
	
	/**
	 * Constructor
	 * @param relativeFreq - boolean that indicates whether the representation
	 * uses relative frequency/probability or the "normal" frequency of the 
	 * words/wordpairs.
	 */
	public Cosine(boolean relativeFreq){
		this.relativeFreq = relativeFreq;
	}

	/**
	 * This method determines which method should be used to compute the 
	 * cosine of two documents, depending on whether the relative frequency
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
			distance = computeCosine(q, r);
		}
		else{
			distance = computeCosine(q, corpusSizeQ, r, corpusSizeR);
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
		double bestCosine = 0;
		for( int c = 0; c < clusters.size(); c++ ){
			double cosine = computeDistance(
						clusters.get(c).centroid.distribution,
						clusters.get(c).centroid.distributionSize,
						doc.words,
						doc.corpusSize
											);
			if( cosine == bestCosine ){
				closestCentroids.add(c);
			}
			else if( cosine > bestCosine ){
				closestCentroids.clear();
				closestCentroids.add(c);
				bestCosine = cosine;
			}
		}

		return closestCentroids;
	}	

	/**
	 * This method computes the cosine between two distributions
	 * (representations where the relative frequency is used).
	 * @param q - first document
	 * @param r - second document
	 * @return result - similarity score
	 */
	public static double computeCosine( Map<String, Double> q, 
			Map<String, Double> r ){
		double result = 0;
		double a = 0;
		double b1 = 0;
		double b2 = 0;
		ArrayList<String> vocab = new ArrayList<String>();
		for( Entry<String, Double> entry:q.entrySet() ){
			String word = entry.getKey();
			double q1 = entry.getValue();
			double r1 = 0;
			if( r.containsKey(word) ){
				r1 = r.get(word);
			}
			a = a + (q1*r1);
			b1 = b1 + Math.pow(q1, 2);
			b2 = b2 + Math.pow(r1, 2);
			vocab.add(word);
		}
		for( Entry<String, Double> entry:r.entrySet() ){
			if( !vocab.contains(entry.getKey()) ){
				double q1 = 0;
				double r1 = entry.getValue();
				
				a = a + (q1*r1);
				b1 = b1 + Math.pow(q1, 2);
				b2 = b2 + Math.pow(r1, 2);
			}
		}

		b1 = Math.sqrt(b1);
		b2 = Math.sqrt(b2);

		result = a / (b1 * b2);

		return result;
	}

	/**
	 * This method computes the cosine of the angle between two documents
	 * that are represented using the frequency (not the relative frequency), 
	 * so the corpus size also needs to be provided to this method.
	 * @param q - first document
	 * @param sizeQ - size of corpus of first document
	 * @param r - second document
	 * @param sizeR - size of corpus of second document
	 * @return result - similarity score
	 */
	public static double computeCosine( Map<String, Double> q, int sizeQ,
			Map<String, Double> r, int sizeR ){
		double result = 0;
		double a = 0;
		double b1 = 0;
		double b2 = 0;
		ArrayList<String> vocab = new ArrayList<String>();
		for( Entry<String, Double> entry:q.entrySet() ){
			String word = entry.getKey();
			double q1 = entry.getValue() / (double) sizeQ;
			double r1 = 0;
			if( r.containsKey(word) ){
				r1 = r.get(word) / (double) sizeR;
			}
			a = a + (q1*r1);
			b1 = b1 + Math.pow(q1, 2);
			b2 = b2 + Math.pow(r1, 2);
			vocab.add(word);
		}
		for( Entry<String, Double> entry:r.entrySet() ){
			if( !vocab.contains(entry.getKey()) ){
				double q1 = 0;
				double r1 = entry.getValue() / (double) sizeR;
				
				a = a + (q1*r1);
				b1 = b1 + Math.pow(q1, 2);
				b2 = b2 + Math.pow(r1, 2);
			}
		}
		b1 = Math.sqrt(b1);
		b2 = Math.sqrt(b2);

		result = a / (b1 * b2);

		return result;
	}
}