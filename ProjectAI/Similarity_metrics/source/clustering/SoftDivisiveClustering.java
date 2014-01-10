package clustering;
import io.FileLoadingUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import plugin_metrics.KLdivergence;

import data_representation.Centroid;
import data_representation.Document;

/**
 * 
 * @author miriamhuijser
 * This class is unfinished. It represents the soft divisive clustering 
 * algorithm. It was left here because it might be interesting for clustering
 * purposes.
 */
public class SoftDivisiveClustering {
	String filePath;
	String language;
	ArrayList<Document> documentObjects = new ArrayList<Document>();
	Centroid initialCentroid;
	ArrayList<Centroid> centroids;
	ArrayList<Centroid> twinCentroids;
	double probDocument;
	boolean twin = true;

	public SoftDivisiveClustering( String filePath, String language ){
		this.filePath = filePath;
		this.language = language;
	}

	public void init(){
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		for( int i = 0; i < documentNames.size(); i++ ){
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
		}
		Centroid basicInitialCentroid = new Centroid();
		for( int i = 0; i < documentObjects.size(); i++ ){
			documentObjects.get(i).createList( basicInitialCentroid, "soft" );
			basicInitialCentroid = documentObjects.get(i).initCentroid;
		}
		createInitialCentroid( basicInitialCentroid);
		probDocument = 1 / (double) documentObjects.size();
	}

	// Initial centroid distribution = Average of all document distributions
	private void createInitialCentroid( Centroid basicInitialCentroid){
		initialCentroid = basicInitialCentroid;
		for( Entry<String, Double> entry:initialCentroid.distribution.entrySet() ){
			String y = entry.getKey();
			double p = entry.getValue() / (double) documentObjects.size(); // revised relative frequency
			initialCentroid.distribution.put(y, p);
		}
	}


	public void startClustering(double beta){
		init();
		centroids = new ArrayList<Centroid>();
		twinCentroids = new ArrayList<Centroid>();
		double betaMax = 1;
		int clusters = 1;
		int clustersMax = 5;
		int iterations = 0;
		int iterationsMax = 2;
		int numberOfSplits = 0;
		centroids.add(initialCentroid);
		boolean twinsSplit = false;

		while( beta < betaMax && clusters <= clustersMax ){
			
			// For each centroid c create twin c*
			for( int i = 0; i < centroids.size(); i++ ){
				System.out.println(centroids.get(i).distribution);
				Centroid twin = centroids.get(i).createTwinCentroid();
				twinCentroids.add(twin);
				System.out.println(twin.distribution);
				double divergenceTwin = Math.abs(KLdivergence.computeKLdivergence( centroids.get(i).distribution,centroids.get(i).distribution.size(), twin.distribution, twin.distribution.size() ));
				centroids.get(i).divergenceWithTwin = divergenceTwin;
				centroids.get(i).initialDivergenceTwin = divergenceTwin;
			}
			
			iterations = 0;
			while( !twinsSplit && iterations < iterationsMax  ){
				iterations++;
				//System.out.println(iterations);

				estimateMembershipProbs(beta);
				estimateCentroids();
				numberOfSplits = checkTwinSplit();
				if( numberOfSplits > 0 ){
					twinsSplit = true; 
				}
			
			}
				System.out.println("\nBeta: " + beta);
				for( int i = 0; i < documentObjects.size(); i++ ){
				System.out.println("Document " + documentObjects.get(i).textFile);
				System.out.println("Membership probabilities with centroids: "+documentObjects.get(i).membershipProb);
				System.out.println("Membership probabilities with twin centroids: "+documentObjects.get(i).membershipProbTwin);
				System.out.println("");
				}
		
			if( numberOfSplits > 1 ){
				beta = beta - 0.2;
			}
			else if( !twinsSplit ){
				beta = beta + 0.4;
			}
			else if( numberOfSplits == 1 ){
				beta = beta + 0.4;
			}
			twinCentroids.clear();

		}
	}

	private void estimateMembershipProbs(double beta){
		for( int i = 0; i < documentObjects.size(); i++ ){
			estimateMembershipProbsForDoc( documentObjects.get(i).words, i, beta );
		}
	}

	private void estimateMembershipProbsForDoc(Map<String, Double> document, int index, double beta){
		ArrayList<Double> c = new ArrayList<Double>();
		ArrayList<Double> cTwin = new ArrayList<Double>();
		double Zx = 0;
		for( int i = 0; i < centroids.size(); i++ ){
			// compute e^-(beta*d(x,c)) for every centroid
			double v1 = KLdivergence.computeKLdivergence( document, document.size(), centroids.get(i).distribution, centroids.get(i).distribution.size() );
			double v2 = -1 * beta * v1; 
			double v = Math.exp(v2);
			c.add(v);
			double v1Twin = KLdivergence.computeKLdivergence( document, document.size(), twinCentroids.get(i).distribution, twinCentroids.get(i).distribution.size() );
			double v2Twin = -1 * beta * v1Twin; 
			double vTwin = Math.exp(v2Twin);
			cTwin.add(vTwin);
			Zx = Zx + v + vTwin;
		}

		// Compute probability of membership x to every centroid
		for( int i = 0; i < c.size(); i++ ){
			double p = c.get(i);
			p = p / Zx;
			c.set(i, p);
			double pTwin = cTwin.get(i);
			pTwin = pTwin / Zx;
			cTwin.set(i, pTwin);
		}

		documentObjects.get(index).membershipProb.add(c);
		documentObjects.get(index).membershipProbTwin.add(cTwin);
	}

	private void estimateCentroids(){
		int iterationAlg = documentObjects.get(0).membershipProb.size() - 1;
		for( int i = 0; i < centroids.size(); i++ ){
			estimateCentroid( i, iterationAlg  );
		}
		for( int i = 0; i < twinCentroids.size(); i++ ){
			estimateCentroid( i, iterationAlg, twin );
		}
	}

	private void estimateCentroid( int index, int iterationAlg ){
		Centroid centroid = centroids.get(index);
		for( int i = 0; i < documentObjects.size(); i++){
			double bayesInverseMship = computeBayesInvMship( i, index, !twin, iterationAlg );

			for( Entry<String, Double> entry:centroid.distribution.entrySet() ){
				String word = entry.getKey();
				if( documentObjects.get(i).words.containsKey(word) ){
					double d = bayesInverseMship * documentObjects.get(i).words.get(word);
					double value = centroid.distribution.get(word);
					double newValue = value + d;
					centroid.distribution.put(word, newValue);
				}
			}
		}
		// Replace old distribution by updated distribution
		centroids.set(index, centroid);
	}
	
	private void estimateCentroid( int index, int iterationAlg, boolean twin ){
		Centroid centroid = twinCentroids.get(index);

		for( int i = 0; i < documentObjects.size(); i++){
			double bayesInverseMship = computeBayesInvMship( i, index, twin, iterationAlg );

			for( Entry<String, Double> entry:centroid.distribution.entrySet() ){
				String word = entry.getKey();
				if( i == 0 ){
					centroid.distribution.put(word, 0.0);
				}
				
				if( documentObjects.get(i).words.containsKey(word) ){
					double d = bayesInverseMship * documentObjects.get(i).words.get(word);
					double value = centroid.distribution.get(word);
					double newValue = value + d;

					centroid.distribution.put(word, newValue);
				}
			}
		}
		// Replace old distribution by updated distribution
		twinCentroids.set(index, centroid);
	}

	private double computeBayesInvMship( int doc, int centroid, boolean twin, int iterationAlg ){
		double result = 0;
		double r = 0;
		double d = 0;
		int last = iterationAlg;
		
		if( !twin ){
			for( int i = 0; i < documentObjects.size(); i++ ){
				double inv = documentObjects.get(i).membershipProb.get(last).get(centroid) * probDocument;
				if( i == doc ){
					d = inv;
				}
				r = r + inv;
			}
		}
		else{
			for( int i = 0; i < documentObjects.size(); i++ ){
				double inv =  documentObjects.get(i).membershipProbTwin.get(last).get(centroid) * probDocument;
				if( i == doc ){
					d = inv;
				}
				r = r + inv;
			}
		}
		result = d / r;
		
		return result;
	}


	/*private double computeKLdivergence( Map<String, Double> q, Map<String, Double> r ){
		double result = 0;
		double q1 = 0;
		double r1 = 0;

		for( Entry<String, Double> entry : q.entrySet() ){
			String word = entry.getKey();
			q1 = entry.getValue();
			r1 = r.get(word);	
			double q2 = q1 / r1;
			result = result + (q1 *  Math.log(q2));


			/*if( q2 == 0 || r1 == 0 ){
				//result = 0;
				System.out.println("0, Needs smoothing!");
				System.out.println("q2: "+q2);
				System.out.println("q1: "+q1);
				System.out.println("r1: "+r1);

			}
			else{
				System.out.println("q2: "+q2);
				System.out.println("q1: "+q1);
				System.out.println("r1: "+r1);

			}
		}
		return result;
	}*/

	private int checkTwinSplit(){
		int numberOfSplits = 0;
		for( int i = 0; i < centroids.size(); i++){
			double newDivergence = Math.abs(KLdivergence.computeKLdivergence( centroids.get(i).distribution, centroids.get(i).distribution.size(), twinCentroids.get(i).distribution, twinCentroids.get(i).distribution.size() ));
			double difference = Math.abs(centroids.get(i).divergenceWithTwin - newDivergence);
			double differenceWithInitial = Math.abs(centroids.get(i).initialDivergenceTwin - newDivergence);
			if( difference > (2 * differenceWithInitial ) ){
				System.out.println("Twins split!");
				centroids.add(twinCentroids.get(i));
				numberOfSplits++;
			}
		}
		return numberOfSplits;
	}
}