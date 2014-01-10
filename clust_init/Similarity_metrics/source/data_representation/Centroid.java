package data_representation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/**
 * @author miriamhuijser
 * Class Centroid contains variables that store a centroid's distribution.
 * It also provides a method that creates a twin centroid.
 */
public class Centroid {
	public Map<String, Double> distribution;
	public double initialDivergenceTwin = 0;
	public double divergenceWithTwin = 0;
	public int distributionSize = 10000;
	
	/**
	 * Default constructor
	 */
	public Centroid(){
		this.distribution = new HashMap<String, Double>();
	}
	
	/**
	 * Constructor
	 * @param distribution - initial distribution of this centroid
	 */
	public Centroid( Map<String, Double> distribution ){
		this.distribution = distribution;
	}

	/**
	 * This method returns a twin centroid by roughly duplicating
	 * the distribution of the current centroid.
	 * @return twin - twin centroid that has been created
	 */
	public Centroid createTwinCentroid(){
		Random random = new Random();
		int size = distribution.size();
		ArrayList<Integer> perturbance = new ArrayList<Integer>();
		int placesChanged = random.nextInt((size/5)) + 1;

		for( int i = 0; i < placesChanged; i++ ){
			perturbance.add(random.nextInt((size)));
		}
		 
		Map<String, Double> twinDistribution = new HashMap<String, Double>(distribution);
		List<String> original = new ArrayList<String>(distribution.keySet());
		for( int i = 0; i < perturbance.size(); i++ ){
			int p = perturbance.get(i);
			String w = original.get(p);
			double newValue = 1.0 - random.nextDouble();
			twinDistribution.put(w, newValue);
		}
		Centroid twin = new Centroid( twinDistribution );
		
		return twin;
	}
}