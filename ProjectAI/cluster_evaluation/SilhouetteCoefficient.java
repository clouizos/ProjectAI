package cluster_evaluation;

import java.util.ArrayList;

import plugin_metrics.Metric;
import clustering.Clustering;
import data_representation.Cluster;
import data_representation.Document;

public class SilhouetteCoefficient extends IntrinsicEvaluation {
	private Metric metric;
	
	public SilhouetteCoefficient(Metric metric){
		this.metric = metric;
	}
	
		
	@Override
	public void computeScore(Clustering C) {
//		ArrayList<Double> A = new ArrayList<Double>();
		System.out.println("Computing silhoutte coefficient...");
		ArrayList<Double> S = new ArrayList<Double>();
		double clusteringMean = 0;
		int count =0;
		for (Cluster cluster : C.clusters){
			double A=0;
			double B=0;
			for (Document d :cluster.members){
				double sumA = 0;
				for (Document notD : cluster.members){
					if (d==notD){continue;}
					sumA += metric.computeDist(d.words, 0, notD.words, 0);
				}
				A = sumA/(double) (cluster.members.size()-1) ;
				
				//cari cluster terdekat
				double minDist = Double.MAX_VALUE;
				Cluster closestCluster=null;
				for (Cluster c : C.clusters){
					double dist = metric.computeDist(cluster.centroid.distribution, 0, c.centroid.distribution, 0);
					if (dist<minDist){
						minDist=dist;
						closestCluster = c;
					}
				}
				
				//hitung sum distance dengann semua point di cluster tsb
				double sumB = 0;
				for (Document D : closestCluster.members){
					if (d==D){continue;}
					sumB += metric.computeDist(d.words, 0, D.words, 0);
				}
				B = sumB/(double) (closestCluster.members.size()) ;
				
			}
			double Si = (B-A)/Math.max(B, A);
			System.out.println("Silhoutte Coefficient for Cluster "+count+" : "+Si);
			S.add(Si);
			count++;
			clusteringMean+=Si;
		}
		clusteringMean = clusteringMean/C.clusters.size();
		System.out.println("Silhouette Coefficient for CLUSTERING : "+clusteringMean);
		
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
