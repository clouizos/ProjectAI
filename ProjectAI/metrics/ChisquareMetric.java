package metrics;

import java.util.Map;
import plugin_metrics.Chisquare;
import data_representation.FrequencyList;
import data_representation.BilingualDocument;

/**
 * 
 * @author miriamhuijser
 * Class ChisquareMetric provides methods that computes the chisquare between
 * the top 500 words/wordpairs of two distributions 
 * (Either monolingual or bilingual).
 */
public class ChisquareMetric{
	private String textFileA;
	private String textFileB;
	private String combinationMethod;
	private String language;
	private int topN = 500;
	private int corpusSizeA;
	private int corpusSizeB;
	private FrequencyList freqObjectA;
	private FrequencyList freqObjectB;
	private Map<String, Double> freqListA;
	private Map<String, Double> freqListB;
	private int topNrevised;
	private boolean bilingual;
	private Chisquare c;

	/**
	 * Constructor
	 * @param textFileA - name of first document
	 * @param textFileB - name of second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param combinationMethod - method of how to combine the similarity
	 * scores computed A to B and B to A. ("average" or "minimum")
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation is used
	 */
	public ChisquareMetric( String textFileA, String textFileB, String language,
					 	String combinationMethod, boolean bilingual ){
		this.textFileA = textFileA;
		this.textFileB = textFileB;
		this.language = language;
		this.combinationMethod = combinationMethod;
		this.bilingual = bilingual;
	}

	/**
	 * This method initializes the desired distributions and computes the 
	 * similarity between the two.
	 * @return similarityScore - similarity score
	 */	
	public double computeSimilarity(){
		if(bilingual){
			init(bilingual);
		}
		else{
			init();
		}
		// If one of the distributions has fewer words than 500 words, 
		// then that number words are taken as the top.
		if( freqObjectA.limitList <= freqObjectB.limitList ){
			topNrevised = freqObjectA.limitList;
		}
		else topNrevised = freqObjectB.limitList;
		if( topNrevised > topN )
			topNrevised = topN;

		double similarityScore = -1;

		double scoreAtoB = c.computeDistance( freqListA, corpusSizeA, freqListB, corpusSizeB, topNrevised );
		double scoreBtoA = c.computeDistance( freqListB, corpusSizeB, freqListA, corpusSizeA, topNrevised );

		// Minimum score of the two scores is taken to be the similarity score
		if( combinationMethod.equals("minimum") ){
			if( scoreAtoB < scoreBtoA )
				similarityScore = scoreAtoB;
			else similarityScore = scoreBtoA;
		}
		// The average of the two scores is taken to be the similarity score
		else if( combinationMethod.equals("average") ){
			similarityScore = 0.5 * scoreAtoB + 0.5 * scoreBtoA;
		}
		
		else System.out.println("Other method than minimum or average not yet implemented");

		return similarityScore;
	}

	/**
	 * This method initializes the monolingual representations
	 */
	private void init(){
		c = new Chisquare(false);
		freqObjectA = new FrequencyList( textFileA, language, topN );
		freqObjectB = new FrequencyList( textFileB, language, topN );
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}

	/**
	 * This method initializes the bilingual representations
	 * @param bilingual
	 */
	private void init( boolean bilingual ){
		c = new Chisquare(true);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}
}