package metrics;

import data_representation.FrequencyList;
import data_representation.BilingualDocument;
import plugin_metrics.L1norm;
import java.util.Map;

/**
 * 
 * @author miriamhuijser
 * Class L1normMetric provides methods that compute the L1norm (or Manhattan
 * distance) between two distributions that have either a monolingual
 * or a bilingual representation.
 */
public class L1normMetric{
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
	private L1norm l;
	
	/**
	 * Constructor
	 * @param textFileA - first document
	 * @param textFileB - second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation is used
	 */		
	public L1normMetric( String textFileA, String textFileB, 
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
		
		result = l.computeDistance(freqListA, 
				corpusSizeA, freqListB, corpusSizeB);
		
		return result;
	}
	
	/**
	 * This method initializes the monolingual representations
	 */
	private void init(){
		l = new L1norm(false);
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
		l = new L1norm(true);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}	
}