package metrics;

import data_representation.FrequencyList;
import data_representation.BilingualDocument;
import plugin_metrics.JaccardsCoefficient;
import java.util.Map;

/**
 * 
 * @author miriamhuijser
 * Class JaccardsCoefficientMetric provides methods that compute the similarity
 * between two distributions. (Either with a monolingual representation or 
 * a bilingual representation)
 */
public class JaccardsCoefficientMetric{
	private String textFileA;
	private String textFileB;
	private String language;
	private FrequencyList freqObjectA;
	private FrequencyList freqObjectB;
	private Map<String, Double> freqListA;
	private Map<String, Double> freqListB;
	private int corpusSizeA;
	private int corpusSizeB;
	private boolean bilingual;
	private JaccardsCoefficient j;
	
	/**
	 * Constructor
	 * @param textFileA - first document
	 * @param textFileB - second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation is used
	 */	
	public JaccardsCoefficientMetric( String textFileA, String textFileB, 
			String language, boolean bilingual ){
		this.textFileA = textFileA;
		this.textFileB = textFileB;
		this.language = language;
		this.bilingual = bilingual;
	}

	/**
	 * This method initializes the desired representations and computes the
	 * similarity between the two distributions.
	 * @return result - similarity score
	 */
	public double computeSimilarity(){
		double result = 0;
		if(bilingual){
			init(bilingual);
		}
		else{
			init();
		}	
		
		result = j.computeDistance(freqListA, 
				corpusSizeA, freqListB, corpusSizeB);
		
		return result;
	}
	
	/**
	 * This method initializes the monolingual representations
	 */
	private void init(){
		j = new JaccardsCoefficient(false);
		freqObjectA = new FrequencyList( textFileA, language, -1 );
		freqObjectB = new FrequencyList( textFileB, language, -1 );
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}

	/**
	 * This method initializes the bilingual representations
	 * @param bilingual
	 */
	private void init( boolean bilingual){
		j = new JaccardsCoefficient(true);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}	
}