package metrics;

import java.util.Map;
import plugin_metrics.JSdivergence;
import data_representation.*;

/**
 * 
 * @author miriamhuijser
 * Class JSdivergenceMetric provides methods that compute the Jensen-Shannon
 * divergence between two distributions. (Either two with a monolingual
 * representation or with a bilingual representation)
 */
public class JSdivergenceMetric {
	private String textFileA;
	private String textFileB;
	private String language;
	private FrequencyList freqObjectA;
	private FrequencyList freqObjectB;
	private Map<String, Double> freqListA;
	private Map<String, Double> freqListB;
	private int corpusSizeA;
	private int corpusSizeB;
	private JSdivergence j;
	private boolean bilingual;
	
	/**
	 * Constructor
	 * @param textFileA - first document
	 * @param textFileB - second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation is used
	 */	
	public JSdivergenceMetric( String textFileA, String textFileB, 
			String language, boolean bilingual ){
		this.textFileA = textFileA;
		this.textFileB = textFileB;
		this.language = language;
		this.bilingual = bilingual;
	}
	
	/**
	 * This method initializes the desired representations and computes the
	 * similarity between the two distributions
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
		result = j.computeDistance(freqListA, corpusSizeA, freqListB, corpusSizeB);
		
		return result;
	}
	
	/**
	 * This method initializes the monolingual representations
	 */
	private void init(){
		j = new JSdivergence( false );
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
		j = new JSdivergence(true);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}
}