package cluster_evaluation;

import io.IOFile;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Locale;

import plugin_metrics.Metric;
import cc.mallet.topics.TopicInferencer;
import clustering.Clustering;
import data_representation.Cluster;
import data_representation.Document;
import data_representation.FeatureMatrix;
import data_representation.FrequencyList;

public class GapStatistic extends IntrinsicEvaluation {
	private int B=5; //default value of the number of reference dataset

	public GapStatistic(Metric metric) {
		this.metric = metric;
	}

	@Override
	public void computeScore(Clustering C) {
		
		double Wk = computeWk(C);

		// use reference datasets to compute the gap and standard deviations

		// if there's no reference datasets in file yet, create them
		// comment below code if the data already exist.
		//---------------------------------------------------------------------
		ArrayList<FrequencyList> allDocs = new ArrayList<FrequencyList>();
		for (Cluster cluster : C.clusters) {
			for (Document d : cluster.members) {
				allDocs.add(d);
			}
		}
		createReferenceDataset(allDocs);
		//---------------------------------------------------------------------
		
		
		//cluster each of those reference dataset with the same clustering algorithm
		double[] Wkb = new double[this.B];
		//******************************need to be filled later
		
		
		//compute the gap
		double gap=0;
		double sumWkb=0;
		for (int i=0;i<this.B;i++){
			gap += Math.log(Wkb[i])-Math.log(Wk);
			sumWkb+=Math.log(Wkb[i]);
		}
		gap = gap/this.B;
		
		//compute standard deviation
		double mean=(1/this.B)*sumWkb;
		double sdk=0;
		for (int i=0;i<this.B;i++){
			sdk+=Math.pow(Math.log(Wkb[i]-mean), 2);
		}
		sdk = sdk/this.B;
		sdk = Math.pow(sdk, 0.5);
		double sk = sdk*Math.pow(1+1/this.B, 0.5);
		
		System.out.println("Gap Statistic for " + C.clusters.size()
				+ " clusters : "+Wk );

	}
	
	private double computeWk(Clustering C){
		double Wk = 0;
		for (Cluster cluster : C.clusters) {
			// pairwise sampling, so need more than 1 point in a cluster
			if (cluster.members.size() > 1) {

				// sample 2 point without replacement and count the distance
				ArrayList<Integer> index = new ArrayList<Integer>();
				for (int i = 0; i < cluster.members.size(); i++) {
					index.add(i);
				}
				Collections.shuffle(index);

				double D = 0; // sum of the pairwise distances for all points in
								// this cluster;
				for (int i = 0; i < index.size() / 2; i++) { // if the number of
																// point
					Document d1 = cluster.members.get(i * 2);
					Document d2 = cluster.members.get(i * 2 + 1);
					D += metric.computeDist(d1.words, d1.words.size(),
							d2.words, d2.words.size());
				}
				Wk += 1 / (2 * cluster.members.size()) * D;
			}
		}
		return Wk;
	}

	/**
	 * This method will create some reference datasets that are needed for
	 * computing Gap Statistics, and save each dataset to a file.
	 * 
	 * @param N
	 *            - number of reference dataset to be created.
	 */
	public void createReferenceDataset(ArrayList<FrequencyList> docs) {
		FeatureMatrix FM = new FeatureMatrix(docs);
		if (this.B < 1) {
			throw new IllegalArgumentException("N should be greater than 0");
		}
		for (int i = 0; i < this.B; i++) {
			IOFile IO = new IOFile();
			String fileName = "./cluster_evaluation/Reference-" + i + ".data";
			IO.createWriteFile(fileName);
			for (int j = 0; j < docs.size(); j++) {
				IO.write("Doc-"+j+",");
				for (int k = 0; k < FM.columnLabel.size(); k++) {
					double randVal = FM.minValue[k]
							+ (Math.random() * (FM.maxValue[k] - FM.minValue[k]));
					IO.write(Double.toString(randVal));
					IO.write(" ");
				}
				IO.write("\n");
			}
			IO.close();
		}
	}
	
//	public static void main(String[] args){
//		double min = 0.2;
//		double max = 0.4;
//		double randVal = min
//				+ (Math.random() * (max - min));
//		System.out.println(randVal);
//	}

}
