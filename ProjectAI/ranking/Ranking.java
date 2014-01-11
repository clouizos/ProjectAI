package ranking;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import data_representation.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * 
 * @author miriamhuijser
 * Class Ranking provides methods that will evaluate metrics using ranking.
 * Each document is labeled with its domain. For a document the rest of
 * the documents are ranked according to their similarity to the document.
 * Then the rank score for this ranking is computed. This is done for every
 * document in the data set and the average of that, the average rank score,
 * is computed.
 */
public class Ranking{
	String dataFile;
	String docNamesFile;
	int numberOfDocs;
	boolean lowScoreIsSimilar;
	AdjacencyMatrix matrix;
	HashMap<String, String> documentDomains;

	/**
	 * Constructor
	 * @param dataFile - name of file that contains the matrix with the
	 * similarity scores for each pair of documents
	 * @param docNamesFile - name of file that contains the names of the documents
	 * @param numberOfDocs - number of documents in the dataset
	 * @param lowScoreIsSimilar - boolean that indicates whether similarity
	 * metric is used that assigns low scores to highly similar documents.
	 * (e.g. this is the case for metrics that compute the distance)
	 */
	public Ranking( String dataFile, String docNamesFile, int numberOfDocs, 
			boolean lowScoreIsSimilar ){
		this.dataFile = dataFile;
		this.docNamesFile = docNamesFile;
		this.numberOfDocs = numberOfDocs;
		this.lowScoreIsSimilar = lowScoreIsSimilar;
	}

	/**
	 * This method initializes the adjacency matrix and labels all the 
	 * documents with their domain.
	 */
	public void init(){
		matrix = new AdjacencyMatrix(dataFile, docNamesFile, numberOfDocs, 
				lowScoreIsSimilar );
		matrix.init();
		labelDocumentsWithDomain();
	}

	/**
	 * This method labels all of the documents with their domain.
	 */
	public void labelDocumentsWithDomain(){
		documentDomains = new HashMap<String, String>();
		ArrayList<String> docList = matrix.documentList;
		for( int i = 0; i < docList.size(); i++ ){
			String docName = docList.get(i);
			String domainLabel = createDomainLabel(docName);
			documentDomains.put(docName, domainLabel);
		}
	}

	/**
	 * This method creates the domain label for a document
	 * @param docName - name of document from which the domain can be 
	 * deduced
	 * @return domainLabel - domain label for the document
	 */
	public String createDomainLabel(String docName){
		String domainLabel;
		int begin = docName.lastIndexOf("_")+1;
		String sub = docName.substring(begin);
		String[] subs = sub.split("\\d");
		domainLabel = subs[0];
		return domainLabel;
	}

	/**
	 * This method ranks for every document and computes the average rank score.
	 * @return averageRankScore
	 */
	public double startRanking(){
		double averageRankScore = 0;
		ArrayList<String> docList = matrix.documentList;
		for( int i = 0; i < docList.size(); i++ ){
			String doc = docList.get(i);
			double rankScore = rankForDoc(doc);
			averageRankScore+=rankScore;
		}		
		averageRankScore = averageRankScore / (double) docList.size();
		
		return averageRankScore;
	}

	/**
	 * This method ranks for one document and computes the rankscore of this
	 * ranking.
	 * @param docName - document for which is ranked
	 * @return rankScore - rank score of this ranking
	 */
	public double rankForDoc( String docName ){
		double rankScore = 0;
		int docID = matrix.documentList.indexOf(docName);		
		Map<String, Double> unsortedRankingList = createRankingListForDoc(docID);
		Map<String, Double> rankingList = sortRankingList(unsortedRankingList);
		int numberDocsSameDomain = 0;
		double rank = 0;
		for( Entry<String, Double> entry:rankingList.entrySet() ){
			if( haveSameDomain(docName, entry.getKey()) ){
				numberDocsSameDomain++;
				double score = 1 - rank/((double) numberOfDocs - 2); // rank starts at 0
				rankScore = rankScore + score;
			}
			rank+=1;
		}
		rankScore = rankScore / (double) numberDocsSameDomain;
		return rankScore;
	}

	/**
	 * This method determines whether two documents belong to the same domain
	 * @param doc1
	 * @param doc2
	 * @return same - boolean that indicates whether the two documents
	 * belong to the same domain.
	 */
	public boolean haveSameDomain(String doc1, String doc2 ){
		boolean same = false;
		if( documentDomains.get(doc1).equals(documentDomains.get(doc2)) ){
			same = true;
		}
		return same;
	}

	/**
	 * This method creates a (unsorted) ranking list for one document
	 * @param docID - index of document in the documents list
	 * @return rankingList - ranking list for document
	 */
	public Map<String, Double> createRankingListForDoc(int docID){
		Map<String, Double> rankingList = new HashMap<String, Double>();
		ArrayList<Double> row = matrix.matrixValuesPerRow.get(docID);
		// docID > 0, not first document
		if( !row.isEmpty() ){
			for( int i = 0; i < row.size(); i++ ){
				int id = i;
				double value = row.get(i);
				String name = matrix.documentList.get(id);
				rankingList.put(name, value);
			}
		}
		for( int i = docID + 1; i < matrix.matrixValuesPerRow.size(); i++ ){
			int id = i;
			double value = matrix.matrixValuesPerRow.get(i).get(docID);
			String name = matrix.documentList.get(id);
			rankingList.put(name, value);
		}

		return rankingList;		
	}

	/**
	 * This method sorts the ranking last, so that the documents with the
	 * highest similarity score are placed up top.
	 * @param unsortedRankingList
	 * @return sortedRankingList
	 */
	public Map<String, Double> sortRankingList(Map<String, Double> unsortedRankingList){
		List<Entry<String, Double>> sorted = new LinkedList<Entry<String, 
				Double>>(unsortedRankingList.entrySet());
		Collections.sort(sorted, new FreqComparator(!lowScoreIsSimilar));
		Map<String, Double> sortedRankingList = new LinkedHashMap<String, Double>();
		for( int i = 0; i < sorted.size(); i++ ){
			Entry<String, Double> entry = sorted.get(i);
			sortedRankingList.put(entry.getKey(), entry.getValue());	
		}
		return sortedRankingList;			
	}
}