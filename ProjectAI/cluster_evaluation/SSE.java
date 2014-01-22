package cluster_evaluation;

import plugin_metrics.Metric;
import clustering.Clustering;
import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;

public class SSE extends IntrinsicEvaluation{
	
	Metric metric;
	
	public SSE(Metric metric) {
		this.metric = metric;
	}

	
	@Override
	public void computeScore(Clustering C) {
		//clusters = C.clusters;
		// only works for external datasets for now
		
		int K = C.clusters.size();
		
		double SSE = 0;
		
	/*	One common method of choosing the appropriate cluster solution is to compare the 
		sum of squared error (SSE) for a number of cluster solutions. SSE is defined as 
		the sum of the squared distance between each member of a cluster and its cluster 
		centroid. Thus, SSE can be seen as a global measure of error. In general, as the 
		number of clusters increases, the SSE should decrease because clusters are, by definition,
		smaller. A plot of the SSE against a series of sequential cluster levels can provide 
		a useful graphical way to choose an appropriate cluster level. Such a plot can be 
		interpreted much like a scree plot used in factor analysis. That is, an appropriate 
		cluster solution could be defined as the solution at which the reduction in SSE slows 
		dramatically. This produces an "elbow" in the plot of SSE against cluster solutions. */
		
		for(Cluster cluster : C.clusters){
			Centroid cent = cluster.centroid;
			for(Document doc : cluster.members){
				SSE += Math.pow(metric.computeDist(doc.words, doc.corpusSize, cent.distribution, cent.distributionSize),2);
			}
		}
		
		System.out.println("SSE error for K = "+K+": "+SSE);
		
		
	}

}
