package metrics;

import java.util.Map;
import plugin_metrics.KLdivergence;
import data_representation.FrequencyList;
import data_representation.BilingualDocument;

/**
 * 
 * @author miriamhuijser
 * Class KLdivergenceMetric provides methods that compute the Kullback-Leibler
 * divergence between two distributions (either with a monolingual or
 * bilingual representation)
 */
public class KLdivergenceMetric{
	private String textFileA;
	private String textFileB;
	private String combinationMethod;
	private String language;
	private FrequencyList freqObjectA;
	private FrequencyList freqObjectB;
	private Map<String, Double> freqListA;
	private Map<String, Double> freqListB;
	private int corpusSizeA;
	private int corpusSizeB;
	private boolean bilingual;
	private KLdivergence k;

	/**
	 * Constructor
	 * @param textFileA - first document
	 * @param textFileB - second document
	 * @param language - language of document (null if one does not want
	 * stopwords to be filtered out)
	 * @param combinationMethod - method of how to combine the similarity
	 * scores computed A to B and B to A. ("average" or "minimum")
	 * @param bilingual - boolean that indicates whether the bilingual
	 * representation is used
	 */
	public KLdivergenceMetric( String textFileA, String textFileB, 
		String language, String combinationMethod, boolean bilingual ){
		this.textFileA = textFileA;
		this.textFileB = textFileB;
		this.language = language;
		this.combinationMethod = combinationMethod;
		this.bilingual = bilingual;
	}

	/**
	 * This method initializes the desired representations and computes
	 * the similarity between the two distributions
	 * @return similarityScore - resulting similarity score
	 */
	public double computeSimilarity(){
		double similarityScore = -1;
		if(bilingual){
			init(bilingual);
		}
		else{
			init();
		}	
		similarityScore = k.computeDistance( freqListA, corpusSizeA, 
			freqListB, corpusSizeB );

		return similarityScore;
	}

	/**
	 * This method initializes the monolingual representations
	 */
	private void init(){
		k = new KLdivergence(false, combinationMethod);
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
		k = new KLdivergence(true, combinationMethod);
		freqObjectA = new BilingualDocument(textFileA);
		freqObjectB = new BilingualDocument(textFileB);
		freqListA = freqObjectA.createList();
		freqListB = freqObjectB.createList();
		corpusSizeA = freqObjectA.corpusSize;
		corpusSizeB = freqObjectB.corpusSize;
	}	
}