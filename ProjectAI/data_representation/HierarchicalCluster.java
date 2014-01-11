package data_representation;

import java.util.List;

/**
 * Class HierarchicalCluster is an abstract class that represents a hierachical
 * cluster, see clustering.SingleLinkClustering and 
 * clustering.HierarhicalClusteringMain.
 * @author miriamhuijser
 *
 */
public abstract class HierarchicalCluster{
	public List<String> members;
	public double minDistance;

	public abstract boolean isBinaryCluster();
}
