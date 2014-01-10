package evaluation;

import clustering.Kmeans;
import data_representation.*;
import plugin_metrics.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * 
 * @author miriamhuijser
 * Class SumOfSquaredError computes the sum of squared error for the clusters
 * that result from kmeans with every metric as distance function. It then
 * prints the results to a seperate file for every metric.
 * 
 */
public class SumOfSquaredError{
	public static void main(String[] args){
		int k = 10; // number of clusters
		// Seeds are used to duplicate "random initalization" of clusters for 
		// each metric.
		Integer[] seeds = {1234, 123456, 99999, 98765};
		String filePath = "../Testdata/English"; // directory of english dataset
		String language = "english"; // using shortlist for english language
		
		Metric m1 = new Chisquare(true);
		Metric m2 = new Cosine(true);
		Metric m3 = new EuclidianDistance(true);
		Metric m4 = new HellingerFunction(true);
		Metric m5 = new JaccardsCoefficient(true);
		Metric m6 = new JSdivergence(true);
		Metric m7 = new KLdivergence(true, "minimum");
		Metric m8 = new L1norm(true);
		ArrayList<Metric> metrics = new ArrayList<Metric>();
		metrics.add(m1);
		metrics.add(m2);
		metrics.add(m3);
		metrics.add(m4);
		metrics.add(m5);
		metrics.add(m6);
		metrics.add(m7);
		metrics.add(m8);
		
		try{
			for( int i = 0; i < metrics.size(); i++ ){
				PrintWriter writer = new PrintWriter(k+"SUMOFSQ"+i+".csv");
				ArrayList<Double> results = new ArrayList<Double>();
				for(int s = 0; s < seeds.length; s ++){
					System.out.println("Seed : " + s);
					Kmeans kmeans = new Kmeans(k, filePath, language, 
							metrics.get(i), seeds[s]);
					double result = 0;
					kmeans.startClustering();
					ArrayList<Cluster> clusters = kmeans.clusters;

					// Compute sum of squared error for every cluster
					for( int j = 0; j < clusters.size(); j++ ){
						Cluster c = clusters.get(j);
						ArrayList<Document> members = c.members;
						for( int l = 0; l < members.size(); l++ ){
							result = result + 
									computeSquaredError( members.get(l).words, 
											c.centroid.distribution );
						}
					}
					results.add(result);
					System.out.println("Metric " + metrics.get(i) + " : " + result);
				}
				double average = 0;
				double total = 0;
				for( Double r:results ){
					System.out.println(r);
					writer.print(r+",");
					total = total + r;
				}
				average = total/(double)results.size();
				writer.print(average+"\n");
				writer.close();
			}
		} catch(IOException e){System.out.println(e.getMessage());}
	}

	/**
	 * This method computes the squared error from the mean of a cluster to 
	 * a member.
	 * @param member - distribution of one member of a cluster
	 * @param mean - distribution of mean of cluster
	 * @return
	 */
	public static double computeSquaredError( Map<String, Double> member,
			Map<String, Double> mean ){
		ArrayList<String> computed = new ArrayList<String>();
		double result = 0;
		for( Entry<String, Double> entry:member.entrySet() ){
			String key = entry.getKey();
			double difference = entry.getValue() - mean.get(key);
			difference = Math.pow(difference, 2);
			result = result + difference;
			computed.add(key);
		}
		for( Entry<String, Double> entry:mean.entrySet() ){
			if( !computed.contains(entry.getKey()) ){
				double difference = 0 - entry.getValue();
				difference = Math.pow(difference, 2);
				result = result + difference;
			}
		}
		return result;
	}
}
