package data_representation;

import java.util.Scanner;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.io.*;

/**
 * @author miriamhuijser
 * Class FrequencyList represents a document using a list with the words
 * listed with their frequency in the document. There is an option to use
 * a shortlist that will filter out highly frequent non-representative stopwords
 * (see class data_represenation.StandardVocabulary). 
 */
public class FrequencyList{
	protected int topN;
	public String textFile;
	protected String language;
	protected Map<String, Double> list = new HashMap<String, Double>();
	protected StandardVocabulary standardVocab;
	public int corpusSize = 0;
	protected boolean standardVocabUsed = false;
	public int limitList;

	/**
	 * Constructor
	 * @param textFile - name of textfile that contains the corpus
	 * @param language - language of corpus (should be null if one does not
	 * want "stopwords" to be filtered out)
	 * @param topN - should be -1 if one wants to use all words, not just the
	 * top N words; Otherwise, it indicates the number of words that will be
	 * taken from the top and used as frequency list.
	 */
	public FrequencyList( String textFile, String language, int topN ){
		this.topN = topN;
		this.language = language;
		this.textFile = textFile;
	}

	/**
	 * This method creates a sorted list of words and their corresponding 
	 * frequencies in the document by parsing the textfile, creating a list
	 * and sorting that list with the words with the highest frequencies up
	 * top.
	 * @return list - created list with words and corresponding frequencies
	 */
	public Map<String, Double> createList(){
		// Use shortlist
		if( language != null){
			standardVocab = new StandardVocabulary(language);
			standardVocabUsed = true;
		}
		parseFile();
		sortList();

		return list;
	}

	/**
	 * Getter for the list of words and corresponding frequencies
	 * @return list - with words and corresponding frequencies
	 */
	public Map<String, Double> getList(){
		return list;
	}

	/**
	 * This method parses the document by removing punctuation. Furthermore,
	 * it adds the words to the list.
	 */
	protected void parseFile(){
		File fileName;
		Scanner s;
		try {
			fileName = new File(textFile);
			s = new Scanner( fileName );
			s.useDelimiter("[ ,!?.:()\"\n]+");
			while( s.hasNext() ){
				String word = s.next();
				word = removePunctuation(word);

				if( (word != null) ){
					addToList(word);
				}
			}
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method removes the puncuation from a word and uncapitalizes it.
	 * It returns the resulting word.
	 * @param word - word that needs uncapitalization and removal of
	 * punctuation.
	 * @return word - word after punctuation is removed and after 
	 * uncapitalization.
	 */
	protected String removePunctuation( String word ){
		// Remove punctuation that follows the word
		word = word.replaceAll("(\\w+)\\p{Punct}(\\s|$)", "$1$2");
				
		/* Remove punctuation that preceeds a word that contains 
			punctuation, e.g ",,Don't" */
		if(word.matches("\\p{Punct}+\\w+\\p{Punct}\\w+")){
			word = word.replaceFirst("(\\p{Punct}+)(\\w+)(\\p{Punct})" +
						"(\\w+)", "$2$3$4");
		}
		/* Remove punctuation that preceeds a word that does not contain
		   additional punctuation */
		else if( word.matches("\\p{Punct}+\\w+") ){
			word = word.replaceAll( "\\p{Punct}+(\\w+)", "$1" );
		}
		// Uncapitalize word
		word = word.toLowerCase();

		/* Check if word is not empty or contains a single symbol or
		   whitespace charachter */
		if( (word.matches("\\p{Punct}+|\\s")|| word.isEmpty()) ){
			word = null;
		}
		return word;
	}

	/**
	 * This method adds a word to the list and adjusts its frequency accordingly.
	 * @param word - word to be added to the list.
	 */
	private void addToList( String word ){
		if( !standardVocabUsed || !standardVocab.shortlist.containsKey(word) ){ 
			corpusSize++;
			// Increase frequency of word if list already contains it
			if( list.containsKey( word ) ){
				double frequency = list.get( word );
				frequency++;
				list.put( word, frequency );
			}
			// Else frequency of word equals 1
			else list.put( word, 1.0 );
		}
	}

	/**
	 * This method sorts the list so that the words with the highest frequencies
	 * are listed up top.
	 */
	protected void sortList(){
		List<Entry<String, Double>> sorted = new LinkedList<Entry<String, 
				Double>>(list.entrySet());
		Collections.sort(sorted, new FreqComparator(true));
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		limitList = topN;
		if( sorted.size() < topN || topN == -1 ){
			limitList = sorted.size();
		}
		for( int i = 0; i < limitList; i++ ){
			Entry<String, Double> entry = sorted.get(i);
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		list = sortedMap;
	}
}