package cluster_evaluation;

import java.util.ArrayList;
import java.util.Collections;

import plugin_metrics.Metric;
import clustering.Clustering;
import data_representation.Cluster;
import data_representation.Document;

public class GapStatistic extends IntrinsicEvaluation{
	
	public GapStatistic(Metric metric){
		this.metric = metric;
	}
	
	@Override
	public void computeScore(Clustering C) {
		double Wk=0;
		for (Cluster cluster : C.clusters){
			//pairwise sampling, so need more than 1 point in a cluster
			if (cluster.members.size()>1){
				
				//sample 2 point without replacement and count the distance
				ArrayList<Integer> index = new ArrayList<Integer>();
				for (int i=0;i<cluster.members.size();i++){
					index.add(i);
				}
				Collections.shuffle(index);
				
				double D=0; //sum of the pairwise distances for all points in this cluster;
				for (int i=0;i<index.size()/2;i++){ //if the number of point
					Document d1 = cluster.members.get(i*2);
					Document d2 = cluster.members.get(i*2+1);
					D += metric.computeDist(d1.words, d1.words.size(), d2.words, d2.words.size());
				}
				Wk+=1/(2*cluster.members.size())*D;
			}
		}
		System.out.println("Gap Statistic for "+C.clusters.size()+" clusters : "+Wk);
		
	}
	
	

}
