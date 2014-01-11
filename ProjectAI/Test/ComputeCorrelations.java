package Test;

import evaluation.PearsonsRCorrelation;

/**
 * 
 * @author miriamhuijser
 * Class ComputeCorrelations is the main for computing the correlations.
 * Beneath is an example for L1norm of how to compute the correlations.
 * For all the .csv files containing the similarity scores see:
 * "/scratch/clust_init/Similarity_metrics/results/MonolingualResults/SimilarityScores"
 */
public class ComputeCorrelations{
	public static void main(String[] args){
		String a = "EN-ResultsL1norm.csv";
		String b = "NL-ResultsL1norm.csv";
		PearsonsRCorrelation p = new PearsonsRCorrelation( a, b);
		p.makeMatrices();
		System.out.println(p.computeCorrelation());
	}
}
