package data_representation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author miriamhuijser
 * Class BilingualDocument saves a bilingual representation. It expects a 
 * textfile with at each line a (source-target)wordpair and its probability.
 * After creating a BilingualDocument object, the method createList should be
 * called to parse the textfile and create a list of the wordpairs/probabilities.
 */
public class BilingualDocument extends Document{
	public Map<String, Double> wordContainer;
	
	/**
	 * Constructor
	 * @param textFile - name of textfile that contains the wordpairs and 
	 * probabilities.
	 */
	public BilingualDocument( String textFile ){
		super( textFile, null );
	}

	/**
	 * This method creates a list of all the wordpairs and their probabilities.
	 * @param wordContainer - container of words that appear in this document,
	 * but also in previously instantiated bilingual documents. Its purpose
	 * is to collect all the words that appear in the entire dataset.
	 * @return
	 */
	public Map<String, Double> createList(Map<String, Double> wordContainer){
		Map<String, Double> list = new HashMap<String, Double>();
		this.wordContainer = wordContainer;
		parseFile();
		list = words;

		return list;
	}

	/**
	 * This method reads and parses the textfile that contains the wordpairs
	 * and probabilities. It then adds these values to the list.
	 */
	protected void parseFile(){
		File fileName;
		Scanner s;
		try{
			fileName = new File(textFile);
			s = new Scanner( fileName );
			while( s.hasNext() ){
				String word1 = s.next();
				String word2 = s.next();
				double probability = Double.parseDouble(s.next());
				String wordpair = word1+"-"+word2;
				if( (word1 != null) && (word2 != null) ){
					addToList( wordpair, probability );
				}
			}
			s.close();
		}	catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method adds a wordpair and its probability to the list and to
	 * the word container (which contains the words of the entire dataset)
	 * @param wordpair - wordpair as appearing in the textfile
	 * @param probability - corresponding probability as appearing in the 
	 * textfile
	 */
	private void addToList( String wordpair, double probability ){
		words.put( wordpair, probability );
		wordContainer.put(wordpair, 0.0);
	}
}