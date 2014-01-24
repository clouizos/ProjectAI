package cluster_evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import plugin_metrics.Chisquare;
import plugin_metrics.Cosine;
import plugin_metrics.EuclidianDistance;
import plugin_metrics.HellingerFunction;
import plugin_metrics.JSdivergence;
import plugin_metrics.JaccardsCoefficient;
import plugin_metrics.KLdivergence;
import plugin_metrics.L1norm;
import plugin_metrics.Metric;
import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;

public class Intrinsic_Evaluation {

	public static ArrayList<Cluster> clusters;
	public static Metric metric;
	
	public Intrinsic_Evaluation(ArrayList<Cluster> clusters, String metric) {
		this.clusters = clusters;
		switch(metric){
		case "kldiv" : this.metric = new KLdivergence(true, "average");break;
		case "chi" : this.metric = new Chisquare(true);break;
		case "cos" : this.metric = new Cosine(true);break;
		case "eucl" : this.metric = new EuclidianDistance(true);break;
		case "hell" : this.metric = new HellingerFunction(true);break;
		case "jac" : this.metric = new JaccardsCoefficient(true);break;
		case "js" : this.metric = new JSdivergence(true);break;
		case "l1" : this.metric = new L1norm(true);break;
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	// possibly only applicable to K-means(needs centroids)
	public static double DBindex(){
		double dbindex = 0;
		ArrayList<Double> dis = new ArrayList<Double>();
		Map<String, Double> D = new HashMap<String, Double>(); 
		double cent_dist = 0;
		
		for(int j =0; j<clusters.size(); j++){
			double ti = clusters.get(j).members.size();
			Centroid centroid = clusters.get(j).centroid;
			double dist = 0;
			for (int i=0; i<ti; i++){
				Document doc = clusters.get(j).members.get(i);
				dist += metric.computeDist(doc.words, doc.corpusSize, centroid.distribution, centroid.distributionSize);
			}
			dist = (1/ti)*dist;
			//double si = Math.pow(dist, 1/q);
			dis.add(dist);
			
			for (int k=0;k<clusters.size();k++){
				if(k!=j){
					dist = 0;
					String key = Double.toString(j)+" "+Double.toString(k);
					Centroid centroid_new = clusters.get(k).centroid;
					double size_cl = clusters.get(k).members.size();
					for(int i=0; i<ti; i++){
						Document doc = clusters.get(j).members.get(i);
						dist += metric.computeDist(doc.words, doc.corpusSize, centroid_new.distribution, centroid_new.distributionSize);
					}
					dist = (1/size_cl)*dist;
					D.put(key, dist);
					cent_dist = metric.computeDist(centroid.distribution, centroid.distributionSize, centroid_new.distribution, centroid_new.distributionSize);
					D.put("Cent:"+Double.toString(j)+" Cent:"+Double.toString(k),cent_dist);
				}
			}
			
			dist = 0;
			
		}
		
		double DB = 0;
		double di = 0;
		String key = "";
		String cent_key = "";
		double Ri = Double.MIN_VALUE;
		
		for(int i=0;i<clusters.size();i++){
			di = dis.get(i);
			for(int j=0; j<clusters.size();j++){
				if(j!=i){
					key = Double.toString(i)+" "+Double.toString(j);
					cent_key = "Cent:"+Double.toString(i)+" Cent:"+Double.toString(j);
					DB = (di + D.get(key))/D.get(cent_key);
					if(DB>Ri){
						Ri=DB;
					}
				}
			}
			dbindex += Ri;
			Ri = Double.MIN_VALUE;
		}
	
		return dbindex/clusters.size();
	}
	
	
	// not finished yet
	public static ArrayList<Double> silhouette (){
		ArrayList<Double> sil_per_cluster = new ArrayList<Double>();
		
		for(Cluster cluster : clusters){
			if(cluster.members.size() == 1)
				sil_per_cluster.add(-1.0);
			else{
				for(Document doc :cluster.members){
					double min_b = Double.MAX_VALUE;
				}
			}
			
		}
		
		return sil_per_cluster;
	}
	

}
