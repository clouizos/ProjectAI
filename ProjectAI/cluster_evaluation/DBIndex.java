package cluster_evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import clustering.Clustering;
import plugin_metrics.Metric;
import data_representation.Centroid;
import data_representation.Cluster;
import data_representation.Document;

public class DBIndex extends IntrinsicEvaluation{
	
	public DBIndex(Metric metric){
		this.metric = metric;
	}

	@Override
	public void computeScore(Clustering C) {
//		double DB=0;
//		for (Cluster cluster : C.clusters){
//			double maxVal = Double.MIN_VALUE;
//			for (Cluster cluster2 : C.clusters){
//				if (cluster != cluster2){
//					
//				}
//			}
//		}
		
		double dbindex = 0;
		ArrayList<Double> dis = new ArrayList<Double>();
		Map<String, Double> D = new HashMap<String, Double>(); 
		double cent_dist = 0;
		
		for(int j =0; j<C.clusters.size(); j++){
			double ti = C.clusters.get(j).members.size();
			Centroid centroid = C.clusters.get(j).centroid;
			double dist = 0;
			for (int i=0; i<ti; i++){
				Document doc = C.clusters.get(j).members.get(i);
				dist += metric.computeDist(doc.words, doc.corpusSize, centroid.distribution, centroid.distributionSize);
			}
			dist = (1/ti)*dist;
			//double si = Math.pow(dist, 1/q);
			dis.add(dist);
			
			for (int k=0;k<C.clusters.size();k++){
				if(k!=j){
					dist = 0;
					String key = Double.toString(j)+" "+Double.toString(k);
					Centroid centroid_new = C.clusters.get(k).centroid;
					double size_cl = C.clusters.get(k).members.size();
					for(int i=0; i<ti; i++){
						Document doc = C.clusters.get(j).members.get(i);
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
		
		for(int i=0;i<C.clusters.size();i++){
			di = dis.get(i);
			for(int j=0; j<C.clusters.size();j++){
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
		dbindex = dbindex/C.clusters.size();
//		return dbindex;
		System.out.println("DB Index is : "+dbindex);
		
	}
	
	
}