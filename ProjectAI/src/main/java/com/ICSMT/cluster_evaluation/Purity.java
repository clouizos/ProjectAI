package cluster_evaluation;

import io.IOFile;

import java.util.HashMap;

import clustering.Clustering;

/**
 * @author said.al.faraby
 * Class Purity will provide the evaluation score based on matching the objects
 * of clusters and classes through their filenames. Check ExtrinsicEvaluation class 
 * to look at the regex if the matching does not work.
 * Documents with label on their filenames should be placed under 'filepath' attribute
 * of Clustering object.
 */
public class Purity extends ExtrinsicEvaluation {
	
	/**
	 * @param args
	 */

	@Override
	public void computeScore(Clustering C) {
		HashMap<String, HashMap<String, Integer>> confMatrix = createConfusionMatrix(
				C.filePath, C.clusters);
		compute(confMatrix);
		
		IOFile io = new IOFile();
		io.openWriteFile("Purity.csv");
		io.write(C.ID);
		io.write(",");
		io.write(Double.toString(this.score));
		io.write("\n");
		io.close();
	}

	private void compute(
			HashMap<String, HashMap<String, Integer>> confMatrix) {

		if (confMatrix.size() == 0) {
			return;
		}

		double totalDocs = confMatrix.get("total").get("total");
		double totalPurity = 0;
		for (String cluster : confMatrix.keySet()) {

			if (cluster == "total") {
				continue;
			}

			double maxPurity = 0;
			String maxLabel = "";

			for (String label : confMatrix.get(cluster).keySet()) {

				if (label == "total") {
					continue;
				}

				double tp = confMatrix.get(cluster).get(label);
				double totalInCluster = confMatrix.get(cluster).get("total");
				double purity = tp / totalInCluster;
				if (purity >= maxPurity) {
					maxPurity = purity;
					maxLabel = label;
				}

			}
			System.out.println("Evaluation on Cluster " + cluster);
			System.out.println("Coresponding True Class " + maxLabel);
			System.out.println("Purity : " + maxPurity);
			System.out.println();

			totalPurity += (confMatrix.get(cluster).get("total") / totalDocs)
					* maxPurity;
		}
		this.score = totalPurity;
		System.out.println("Purity of Clustering : " + totalPurity);

	}


}
