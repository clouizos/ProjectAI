package cluster_evaluation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.*;

/**
 * 
 * @author 
 * Assume we have the true cluster of documents, then this class will compute 
 * the precision of clusters resulted by clustering process.
 */
public class Precision extends Evaluation{
	
	
	/**
	 * @param args
	 */
	
	
	public static void main(String[] args) {
		String classDir = "../TrueLabel";
		String clusterDir = "../MeasuredLabel";
		HashMap<String, HashMap<String, Integer>> confMatrix = Evaluation.createConfusionMatrix(classDir, clusterDir);
		
		
		

	}

}
