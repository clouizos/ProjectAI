package data_representation;

import java.util.ArrayList;

/**
 * 
 * @author miriamhuijser
 * Class Cluster saves a cluster's centroid, members, a history of members
 * for every iteration. Furthermore, it provides useful methods to update these.
 */
public class Cluster{
	public ArrayList<Document> members = new ArrayList<Document>();
	public ArrayList<ArrayList<String>> historyMembers = new ArrayList<ArrayList<String>>();
	public Centroid centroid;

	/**
	 * Constructor
	 * @param centroid - centroid of this cluster
	 */
	public Cluster( Centroid centroid ){
		this.centroid = centroid;
	}

	/**
	 * This method adds a document as a new member to the cluster.
	 * @param doc - document
	 */
	public void addMember( Document doc ){
		members.add(doc);
	}

	/**
	 * This method updates the (rough) size of the centroid distribution by 
	 * taking the average size of the distributions of the members.
	 */
	public void updateSizeDistrCentroid(){
		double size = 0;
		for( int i = 0; i < members.size(); i++ ){
			size = size + members.get(i).corpusSize;
		}
		centroid.distributionSize = (int) (size / (double) members.size());
	}

	/**
	 * This method updates the history of the cluster with its current state.
	 * (current members)
	 */
	public void updateHistory(){
		ArrayList<String> newList = new ArrayList<String>();
		for( int i = 0; i < members.size(); i++ ){
			String file = members.get(i).textFile;
			newList.add(file);
		}
		historyMembers.add(newList);
	}

	/**
	 * This method checks whether the cluster has changed since the last 
	 * iteration.
	 * @return changed
	 */
	public boolean hasChanged(){
		boolean changed = false;
		ArrayList<String> mostRecent = historyMembers.get(historyMembers.size()-1);
		ArrayList<String> recentHistory = historyMembers.get(historyMembers.size()-2);
		if( mostRecent.size() != recentHistory.size() ){
			changed = true;
		}
		else{
			for( int i = 0; i < mostRecent.size(); i++ ){
				if( !recentHistory.contains(mostRecent.get(i))){
					changed = true;
					break;
				}
			}
		}

		return changed;
	}
}