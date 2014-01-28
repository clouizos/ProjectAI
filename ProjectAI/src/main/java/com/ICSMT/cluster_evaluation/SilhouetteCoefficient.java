package cluster_evaluation;

import io.IOFile;

import java.util.ArrayList;

import plugin_metrics.Metric;
import clustering.Clustering;
import data_representation.Cluster;
import data_representation.Document;

/*
 * This class is an internal evaluation on clustering. The highest the score the better
 * the clustering quality. Silhouette Index of a point i Si = (b - a)/Max(b,a). 
 * A is average distance of point i from other points within the same cluster,
 * B is average distance of point i from other points from the closest cluster.
 */
public class SilhouetteCoefficient extends IntrinsicEvaluation {
	
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
		System.out.println("Number of clusters : "+C.clusters.size());
		for (Cluster cluster : C.clusters){
			System.out.println("Computing Silhouette Coeff Cluster "+count);
			System.out.println("Number of document in cluster : "+cluster.members.size());
			double clusterMean = 0;
			for (Document d :cluster.members){
				double A=0;
				double B=0;
				double sumA = 0;
				for (Document notD : cluster.members){
					if (d==notD){continue;}
					sumA += metric.computeDist(d.words, d.words.size(), notD.words, notD.words.size());
//					System.out.println("SUMA "+sumA);
				}
				if (cluster.members.size()>1){
					A = sumA/(double) (cluster.members.size()-1) ;
//				System.out.println("A : "+A);
				}
				//cari cluster terdekat
				double minDist = Double.MAX_VALUE;
				Cluster closestCluster=null;
				for (Cluster c : C.clusters){
					if (c==cluster) {continue;}
					if (c.members.size()==0){continue;}
					double dist = metric.computeDist(cluster.centroid.distribution, cluster.centroid.distributionSize, c.centroid.distribution, c.centroid.distributionSize);
					if (dist<minDist){
						minDist=dist;
						closestCluster = c;
					}
				}
				
				//hitung sum distance dengann semua point di cluster tsb
				double sumB = 0;
				for (Document D : closestCluster.members){
					if (d==D){continue;}
					sumB += metric.computeDist(d.words, d.words.size(), D.words, D.words.size());
				}
				B = sumB/(double) (closestCluster.members.size()) ;
				//System.out.println("B : "+B);
				//System.out.println("A : "+A);
				
				double Si = (B-A)/Math.max(B, A);
				if (Math.max(B, A)==0){
					Si = 0;
				}
				clusterMean+=Si;
//				System.out.println(clusterMean);
			}
			
			if (cluster.members.size()>0){
				clusterMean=clusterMean/cluster.members.size();
			}
			
			System.out.println("Silhoutte Coefficient for Cluster "+count+" : "+clusterMean);
			System.out.println();
			S.add(clusterMean);
			count++;
			clusteringMean+=clusterMean;
		}
		clusteringMean = clusteringMean/C.clusters.size();
		this.score = clusteringMean;
		IOFile io = new IOFile();
		io.openWriteFile("Silhouette.csv");
		io.write(C.ID);
		io.write(",");
		io.write(Double.toString(this.score));
		io.write("\n");
		io.close();
		System.out.println("Silhouette Coefficient for CLUSTERING : "+clusteringMean);
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
