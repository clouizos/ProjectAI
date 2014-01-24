package data_representation;

import java.util.ArrayList;

/**
 *
 * @author miriamhuijser
 * Class UniCluster represents an unary cluster in which the document name
 * of its member is saved and its index in the document arraylist. 
 * (see data_representation.AdjacencyMatrix)
 */
public class UniCluster extends HierarchicalCluster{
	public String docName;
	int index;

	/**
	 * Constructor
	 * @param docName - name of document file
	 * @param index - index of this document in document arraylist
	 */
	public UniCluster( String docName, int index ){
		this.docName = docName;
		this.index = index;
		this.members = new ArrayList<String>();
		members.add(docName);
		this.minDistance = Double.POSITIVE_INFINITY;
	}

	/**
	 * This method determines whether this cluster is binary.
	 * @return false - this cluster is not a binary cluster, but an unary cluster
	 */
	public boolean isBinaryCluster(){
		return false;
	}
}