package data_representation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Random;

/**
 * 
 * @author miriamhuijser
 * As opposed to FrequencyList, this class creates a FrecuencyList of the document
 * with the relative frequency instead of the mere count of words.
 * mainly used for clustering
 */
public class Document extends FrequencyList {
	public Centroid initCentroid = new Centroid();
	public ArrayList<ArrayList<Double>> membershipProb = new ArrayList<ArrayList<Double>>();
	public ArrayList<ArrayList<Double>> membershipProbTwin = new ArrayList<ArrayList<Double>>();
	public Map<String, Double> words = new HashMap<String, Double>();
	String c;
	Random random = new Random();
	
	/**
	 * Constructor
	 * @param textFile - name of textfile that contains the corpus
	 * @param language - language of corpus (should be null if one does not
	 * want "stopwords" to be filtered out)
	 */
	public Document(String textFile, String language ) {
		super(textFile, language, -1);
	}

	/**
	 * This method creates a list of words that occur in the document
	 * and their relative frequency in the document. Furthermore, the input
	 * parameters have to do with clustering (for which this class is mostly
	 * used). 
	 * @param initCentroid - centroid to which the words appearing in this 
	 * document will be added.
	 * @param c - method of centroid initialization.
	 * @return words - list of words and their relative frequency
	 */
	public Map<String, Double> createList( Centroid initCentroid, String c ){
		this.c = c;
		this.initCentroid = initCentroid;
		if( language != null){
			standardVocab = new StandardVocabulary(language);
			standardVocabUsed = true;
		}
		parseFile();
		
		if( c != null && (c.equals("forgy") || c.equals("soft")) ){
			addRelativeFreq(c);
		}
		else addRelativeFreq();
		sortList(); // List is sorted, highly frequent words are placed up top.
		words = list;
		
		return words;
	}

	/**
	 * This method reads and parses the textfile that contains the corpus and
	 * adds each word to the wordlist.
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
	 * This method adds a word to the wordlist and its frequency is recorded.
	 * @param word - word to be added.
	 */
	private void addToList( String word ){
		/* If a shortlist is used and the word appear in it, it is not added
		 * to the wordlist
		 */
		if( !standardVocabUsed || !standardVocab.shortlist.containsKey(word) ){ 
			corpusSize++;	
			if( list.containsKey( word ) ){
				double frequency = list.get( word );
				frequency++;
				list.put( word, frequency );
			}
			else list.put( word, 1.0 );	
		}
	}

	/**
	 * This method computes the relative frequency for each word in the list
	 * by dividing its frequency by the size of the corpus. Furthermore, it adds
	 * words to the centroid and, depending on which (centroid) initialization 
	 * method used, the corresponding relative frequency or a zero.
	 * @param c - centroid initialization method used
	 */
	private void addRelativeFreq(String c){
		if( c.equals("forgy") ){
			for( Entry<String, Double> entry:list.entrySet() ){
				String word = entry.getKey();
				double relativeFreq = entry.getValue()/(double) corpusSize;
				list.put(word, relativeFreq );
				initCentroid.distribution.put(word, 0.0);
			}
		}
		else if( c.equals("soft") ){
			for( Entry<String, Double> entry:list.entrySet() ){
				String word = entry.getKey();
				double relativeFreq = entry.getValue()/(double) corpusSize;
				list.put(word, relativeFreq );
				if( initCentroid.distribution.containsKey( word ) ){
					double value = initCentroid.distribution.get(word);
					value = value + relativeFreq;
					initCentroid.distribution.put(word, value);
				}
				else{
					initCentroid.distribution.put(word, relativeFreq);
				}
			}
		}
	}

	/**
	 * This method computes the relative frequency for each word in the list
	 * by dividing its frequency by the size of the corpus. Furthermore,
	 * it adds words the centroid and random relative frequencies between 
	 * 0 and 1.
	 */
	private void addRelativeFreq(){
		for( Entry<String, Double> entry:list.entrySet() ){
			String word = entry.getKey();
			double relativeFreq = entry.getValue()/(double) corpusSize;
			list.put(word, relativeFreq );
			double newValue = random.nextDouble();
			initCentroid.distribution.put(word, newValue);
		}
	}
}
