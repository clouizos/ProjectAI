package cluster_evaluation;

import clustering.Clustering;
import plugin_metrics.Metric;

public abstract class IntrinsicEvaluation {
	protected Metric metric;
	public abstract void computeScore(Clustering C);
}
