package metrics;

import java.util.Map;
import plugin_metrics.EuclidianDistance;
import data_representation.FrequencyList;
import data_representation.BilingualDocument;

/**
 * 
 * @author miriamhuijser
 * Class EuclidianDistanceMetric provides methods that computes the euclidian
 * distance between two distributions (Either monolingual or bilingual).
 */
public class EuclidianDistanceMetric {
	private String textFileA;
	private String textFileB;
	private String language;
	private FrequencyList freqObjectA;
	private FrequencyList freqObjectB;
	private Map<String, Double> freqListA;
	private Map<String, Double> freqListB;
	private int corpusSizeA;
	private int corpusSizeB;
	private EuclidianDistance e;
	private boolean bilingual;

	/**
	 * Constructor
	 * @param textFileA - name of first document
	 * @param textFileB - name of second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation should be used
	 */
	public EuclidianDistanceMetric( String textFileA, String textFileB, 
			String language, boolean bilingual ){
		this.textFileA = textFileA;
		this.textFileB = textFileB;
		this.language = language;
		this.bilingual = bilingual;
	}
	
	/**
	 * This method initializes the desired distributions and computes the 
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
		result = e.computeDistance(freqListA, corpusSizeA, freqListB, corpusSizeB);
		
		return result;
	}
	
	/**
	 * This method initializes the monolingual representations
	 */
	private void init(){
		e = new EuclidianDistance(false);
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
		e = new EuclidianDistance(true);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}
}
