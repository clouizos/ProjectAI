package metrics;

import java.util.Map;
import plugin_metrics.Cosine;
import data_representation.FrequencyList;
import data_representation.BilingualDocument;

/**
 * 
 * @author miriamhuijser
 * Class CosineMetric provides methods that will compute the cosine of the 
 * angle between two distributions. (Either monolingual or bilingual).
 */
public class CosineMetric {
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
	private Cosine c;
	
	/**
	 * Constructor
	 * @param textFileA - name of first document
	 * @param textFileB - name of second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation is used
	 */
	public CosineMetric( String textFileA, String textFileB, 
			String language, boolean bilingual ){
		this.textFileA = textFileA;
		this.textFileB = textFileB;
		this.language = language;
		this.bilingual = bilingual;
	}

	/**
	 * This method initializes the desired distributions and computes the 
	 * similarity between the two.
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
		result = c.computeDistance(freqListA, 
				corpusSizeA, freqListB, corpusSizeB);
		
		return result;
	}

	/**
	 * This method initializes the monolingual distributions
	 */
	private void init(){
		c = new Cosine(false);
		freqObjectA = new FrequencyList( textFileA, language, -1 );
		freqObjectB = new FrequencyList( textFileB, language, -1 );
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}

	/**
	 * This method initializes the bilingual distributions
	 * @param bilingual
	 */
	private void init( boolean bilingual){
		c = new Cosine(true);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}	
}