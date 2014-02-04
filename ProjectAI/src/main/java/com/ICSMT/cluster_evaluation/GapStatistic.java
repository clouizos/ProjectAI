package cluster_evaluation;

import io.IOFile;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.Locale;

import plugin_metrics.L1norm;
import plugin_metrics.Metric;
import cc.mallet.topics.TopicInferencer;
import clustering.Clustering;
import clustering.Kmeans;
import clustering.FuzzyCmeans;
		
import data_representation.Cluster;
import data_representation.Document;
import data_representation.FeatureMatrix;
import data_representation.FrequencyList;


/**
 * @author said.al.faraby
 * Class GapStatistic provides methods for evaluating clustering result. 
 * This method will create B reference datasets. The value of each feature in reference dataset
 * is drawn randomly between the lowest and the highest value of that feature in the real dataset.
 * Metric object is required when initialize this object.
 * Since this method usually to find the optimum number of clusters, so now it just
 * works with K-means and FuzzyCmeans.
 */
public class GapStatistic extends IntrinsicEvaluation {
	private int B=20; //default value of the number of reference dataset
	public double sk;

	public GapStatistic(Metric metric) {
		this.metric = metric;
	}
	
	
	/**
	 * This method will compute the gap statistic and write the result into a file
	 * GapStatistic.csv in working directory(default)
	 * @param C - clustering object. This method need to access the 'clusters' attribute
	 * of clustering to evaluate the clusters. Clusterer object should save the final 
	 * clusters of clustering in 'clusters' attribute.
	 */
	@Override
	public void computeScore(Clustering C) {
		
		double Wk = computeWk(C);
		
		
		ArrayList<FrequencyList> allDocs = new ArrayList<FrequencyList>();
		for (Cluster cluster : C.clusters) {
			for (Document d : cluster.members) {
				allDocs.add(d);
			}
		}
		
		// use reference datasets to compute the gap and standard deviations
		// if there's no reference datasets in file yet, create them
		// comment the code below if you reference datasets are already exist and
		// you don't want to create(overwrite) them anymore.
		createReferenceDataset(allDocs);
		
		
		//cluster each of those reference dataset with the same clustering algorithm
		double[] Wkb = new double[this.B];
		Clustering clusterer=null;
		for (int i=0;i<this.B;i++){
			if (C instanceof Kmeans){
				clusterer = new Kmeans(((Kmeans) C).k, C.filePath, ((Kmeans) C).language, ((Kmeans) C).metric, ((Kmeans) C).seed, ((Kmeans) C).externalDataset, "Reference-"+i+".data");
				clusterer.startClustering();
			} else if (C instanceof FuzzyCmeans){
				clusterer = new FuzzyCmeans(((FuzzyCmeans)C).c, ((FuzzyCmeans)C).m, ((FuzzyCmeans)C).thres, ((FuzzyCmeans)C).filePath, ((FuzzyCmeans)C).language, ((FuzzyCmeans)C).metric, ((FuzzyCmeans)C).seed, ((FuzzyCmeans)C).externalDataset, "Reference-"+i+".data");
				clusterer.startClustering();
			} 
			// *************************add other clustering algorithms
			
			Wkb[i]=computeWk(clusterer);
		}
		for (int x=0;x<this.B;x++)
			System.out.println(Wkb[x]);
		
		//compute the gap
		double gap=0;
		double sumWkb=0;
		for (int i=0;i<this.B;i++){
			sumWkb+=Math.log(Wkb[i]);
		}
		gap = sumWkb/(double)B - Math.log(Wk);
		this.score=gap;
		
		//compute standard deviation
		double mean=(1/(double)B)*sumWkb;
		double sdk=0;
		for (int i=0;i<this.B;i++){
			sdk+=Math.pow(Math.log(Wkb[i])-mean, 2);
		}
		sdk = sdk/this.B;
		sdk = Math.pow(sdk, 0.5);
		this.sk = sdk*Math.pow(1+1/(double)B, 0.5);
		
		//write the results to file
		IOFile io = new IOFile();
		io.openWriteFile("GapStatistic.csv");
		io.write(C.ID);
		io.write(",");
		io.write(Double.toString(Wk));
		io.write(",");
		io.write(Double.toString(this.score));
		io.write(",");
		io.write(Double.toString(this.sk));
		io.write("\n");
		io.close();
		
		System.out.println("Gap Statistic for " + C.clusters.size()
				+ " clusters : "+gap );
		

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
				Wk += (1 / (double)(2 * cluster.members.size())) * D;
			}
		}
		return Wk;
	}

	/**
	 * This method will create some reference datasets that are needed for
	 * computing Gap Statistics, and save each dataset to a file.
	 * 
	 * @param docs  - arraylist of Document or FrequencyList object.
	 */
	public void createReferenceDataset(ArrayList<FrequencyList> docs) {
		FeatureMatrix FM = new FeatureMatrix(docs);
		if (this.B < 1) {
			throw new IllegalArgumentException("B should be greater than 0");
		}
		for (int i = 0; i < this.B; i++) {
			IOFile IO = new IOFile();
			String fileName = "Reference-" + i + ".data";
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
//		String extFilePath = "./src/main/java/com/ICSMT/features/";
//		String language = "english";
//		int numTopics = 10;
//		Metric metric = new L1norm(true);
//		extFilePath = extFilePath + "featureVectors_language_"+language+"_"+numTopics+".data";
//		Clustering KM = new Kmeans(5, "./Testdata/dataset/English",  language, metric, 20, true, extFilePath);
//		System.out.println(KM.getClass().getName());
//	}

}
