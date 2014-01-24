package data_representation;

import java.util.ArrayList;

/**
 * @author miriamhuijser
 * Class BinaryCluster represents a binary cluster and saves its two
 * subclusters and the minimal distance between their members.
 */
public class BinaryCluster extends HierarchicalCluster{
	public HierarchicalCluster cluster1;
	public HierarchicalCluster cluster2;

	/**
	 * Constructor
	 * @param cluster1 - subcluster of current cluster
	 * @param cluster2 - subcluster of current cluster
	 * @param minDistance - minimal distance between members of the two 
	 * subclusters
	 */
	public BinaryCluster( HierarchicalCluster cluster1, HierarchicalCluster cluster2, double minDistance ){
		this.cluster1 = cluster1;
		this.cluster2 = cluster2;
		this.minDistance = minDistance;
		this.members = new ArrayList<String>();
		members.addAll(cluster1.members);
		members.addAll(cluster2.members);
	}

	/**
	 * This method returns whether this cluster is a binary cluster.
	 * @return true
	 */
	public boolean isBinaryCluster(){
		return true;
	}
}