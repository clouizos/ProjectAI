package plugin_metrics;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import data_representation.*;

/**
 * 
 * @author miriamhuijser
 * Class KLdivergence provides methods that compute the Kullback-Leibler 
 * divergence between two distributions or finds the closest cluster to a 
 * certain document.
 */
public class KLdivergence extends Metric{
	boolean relativeFreq;
	String combiMethod;

	/**
	 * Constructor
	 * @param relativeFreq - boolean that indicates whether the representation
	 * uses relative frequency/probability or the "normal" frequency of the 
	 * words/wordpairs.
	 * @param combiMethod - combination method of the two similarity scores
	 * (q -> r and r -> q). This can be "average" or "minimum".
	 */
	public KLdivergence(boolean relativeFreq, String combiMethod){
		this.relativeFreq = relativeFreq;
		this.combiMethod = combiMethod;
	}
	
	/**
	 * This method determines which method should be called depending on
	 * the combination method of the scores and whether the documents
	 * are represented using the counts or the relative frequencies/probabilities
	 * of the words/wordpairs. It then calls this method and returns the
	 * distance between the documents.
	 * @param q - first document
	 * @param corpusSizeQ - corpus size first document
	 * @param r - second document
	 * @param corpusSizeR - corpus size second document
	 * @return distance - distance between the two documents
	 */
	public double computeDistance(Map<String, Double> q, int corpusSizeQ, 
			Map<String, Double> r, int corpusSizeR){
		double distance = 0;
		if( relativeFreq ){
			if( combiMethod != null ){
				distance = computeKLdivergence(q, corpusSizeQ, r, corpusSizeR,
						combiMethod, relativeFreq);
			}
			else distance = computeKLdivergence(q, corpusSizeQ, r, corpusSizeR, 
					relativeFreq);
		}
		else{
			if( combiMethod != null ){
				distance = computeKLdivergence(q, corpusSizeQ, r, corpusSizeR,
						combiMethod);
			}
			else distance = computeKLdivergence(q, corpusSizeQ, r, corpusSizeR);
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
		double bestKLdivergence = Double.POSITIVE_INFINITY;
		for( int c = 0; c < clusters.size(); c++ ){
			double divergence = 
					Math.abs(computeDistance( 
							clusters.get(c).centroid.distribution, 
							clusters.get(c).centroid.distributionSize,
							doc.words,
							doc.corpusSize )
							);
			if( divergence == bestKLdivergence ){
				closestCentroids.add(c);
			}
			else if( divergence < bestKLdivergence  ){
				closestCentroids.clear();
				closestCentroids.add(c);
				bestKLdivergence = divergence;
			}
		}
		return closestCentroids;
	}	

	/**
	 * This method computes the KL divergence between two documents (that are
	 * represented using the counts of the words, not the relative frequency)
	 * both ways and takes either the minimum or average of those two. 
	 * @param q - first document
	 * @param corpusSizeQ - corpus size of first document
	 * @param r - second document
	 * @param corpusSizeR - corpus size of second document
	 * @param combinationMethod - combination method of the two similarity scores
	 * (q -> r and r -> q). This can be "average" or "minimum".
	 * @return similarityScore - similarity score
	 */
	public static double computeKLdivergence( Map<String, Double> q, 
			int corpusSizeQ, Map<String, Double> r, int corpusSizeR, 
			String combinationMethod ){
		double scoreQtoR = computeKLdivergence( q, corpusSizeQ, r, corpusSizeR );
		double scoreRtoQ = computeKLdivergence( r, corpusSizeR, q, corpusSizeQ );
		double similarityScore = -2;

		if( combinationMethod.equals("minimum") ){
			if( scoreQtoR < scoreRtoQ )
				similarityScore = scoreQtoR;
			else similarityScore = scoreRtoQ;
		}
		else if( combinationMethod.equals("average") ){
			similarityScore = 0.5 * scoreQtoR + 0.5 * scoreRtoQ;
		}
		
		else System.out.println("Other method than minimum or average not yet" +
				" implemented");

		return similarityScore;
	}

	/**
	 * This method computes the KL divergence between two documents (that are
	 * represented using the relative frequencies of the words) both ways and
	 * either the average or the minimum of those two scores is taken to be
	 * the similarity score.
 	 * @param q - first document
	 * @param corpusSizeQ - corpus size first document
	 * @param r - second document
	 * @param corpusSizeR - corpus size second document
	 * @param combinationMethod - combination method of the two similarity scores
	 * (q -> r and r -> q). This can be "average" or "minimum".
	 * @param relativeFreq - representation with relative frequency is used
	 * @return similarityScore - similarity score
	 */
	public static double computeKLdivergence( Map<String, Double> q,
			int corpusSizeQ, Map<String, Double> r, int corpusSizeR,
			String combinationMethod, boolean relativeFreq ){
		
		double scoreQtoR = computeKLdivergence( q, corpusSizeQ, r,
				corpusSizeR, relativeFreq );
		double scoreRtoQ = computeKLdivergence( r, corpusSizeR, q, 
				corpusSizeQ, relativeFreq );
		double similarityScore = -2;
		
		if( combinationMethod.equals("minimum") ){
			if( scoreQtoR < scoreRtoQ )
				similarityScore = scoreQtoR;
			else similarityScore = scoreRtoQ;
		}
		
		else if( combinationMethod.equals("average") ){
			similarityScore = 0.5 * scoreQtoR + 0.5 * scoreRtoQ;
		}
		
		else System.out.println("Other method than minimum or average not yet" +
				" implemented");

		return similarityScore;
	}


	/**
	 * This method computes the KL divergence between two documents that
	 * are represented using the counts of the words. Add-one smoothing is used.
	 * @param q - first document
	 * @param corpusSizeQ - corpus size first document
	 * @param r - second document
	 * @param corpusSizeR - corpus size second document
	 * @return result - similarity score
	 */
	public static double computeKLdivergence( Map<String, Double> q, 
			int corpusSizeQ, Map<String, Double> r, int corpusSizeR ){
		double result = 0;
		double q1 = 0;
		double r1 = 0;

		for( Entry<String, Double> entry : q.entrySet() ){
			String word = entry.getKey();
			q1 = entry.getValue() / (double) corpusSizeQ;
			
			if( r.containsKey(word) ){
				r1 = r.get(word)/(double) corpusSizeR;
			}
			// Add-one smoothing
			else{
				r1 = 1 / (double) (corpusSizeR + 1);
			}
			double q2 = q1 / r1;
			result = result + (q1 *  Math.log(q2));
		}

			return result;
	}

	/**
	 * This method computes the KL divergence between two documents that are
	 * represented using the relative frequencies of the words. Add-one
	 * smoothing is used.
	 * @param q - first document
	 * @param corpusSizeQ - corpus size first document
	 * @param r - second document
	 * @param corpusSizeR - corpus size second document
	 * @param relativeFreq - representation with relative frequency is used
	 * @return result - similarity score
	 */
	public static double computeKLdivergence( Map<String, Double> q, 
			int corpusSizeQ, Map<String, Double> r, int corpusSizeR, 
			boolean relativeFreq ){
		double result = 0;
		double q1 = 0;
		double r1 = 1;

		for( Entry<String, Double> entry : q.entrySet() ){
			String word = entry.getKey();
			q1 = entry.getValue();
			if( q1 == 0 ){
				q1 = 1 / (double) (corpusSizeQ+1);
			}

			if( r.containsKey(word) && r.get(word) != 0 ){
				r1 = r.get(word);
			}
			else{
				r1 = 1 / (double) (corpusSizeR + 1);
			}
			double q2 = q1 / r1;
			result = result + (q1 *  Math.log(q2));
		}
		return result;
	}
}