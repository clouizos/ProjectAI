package topic_modelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;


/**
 * 
 * @author christos
 * 
 * Class that performs the monolingual Latent Dirichlet Allocation (LDA) - wrapped around the mallet toolkit
 *
 */
public class LDA {
	
	private ParallelTopicModel model;
	String stopwords;
	String data_source;
	private int numTopics;
	private int numIterations;
	private String language;

	
	/**
	 * Constructor
	 * @param stopwords - the path of the stopwords
	 * @param data_source - the path of the dataset for the LDA
	 * @param numTopics - the desired number of topics
	 * @param numIterations - the iterations to be performed by the Gibbs sampler
	 * @param language - the language of the documents
	 */
	public LDA(String stopwords, String data_source, int numTopics, int numIterations,String language) {
		this.stopwords = stopwords;
		this.data_source = data_source;
		this.numTopics = numTopics;
		this.numIterations = numIterations;
		this.language = language;
	}

	public void createModel() throws Exception {
		
		        // Begin by importing documents from text to feature sequences
		        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

		        // Pipes: lowercase, tokenize, remove stopwords, map to features
		        pipeList.add( new CharSequenceLowercase() );
		        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwords), "UTF-8", false, false, false) );
		        pipeList.add( new TokenSequence2FeatureSequence() );

		        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

		        Reader fileReader = new InputStreamReader(new FileInputStream(new File(data_source)), "UTF-8");
		        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
		                                               3, 2, 1)); // data, label, name fields
		        
		        
		        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
		        //  Note that the first parameter is passed as the sum over topics, while
		        //  the second is the parameter for a single dimension of the Dirichlet prior.
		        //int numTopics = 8;
		        model = new ParallelTopicModel(numTopics, 1.0, 0.01);
		        
		        model.addInstances(instances);
		        
		        // Try and see how this works for the bilingual setting
		        //PolylingualTopicModel model = new PolylingualTopicModel(numTopics);
		        //model.addInstances(training);
		        
		        // Use two parallel samplers, which each look at one half the corpus and combine
		        //  statistics after every iteration.
		        model.setNumThreads(8);

		        // Run the model for 50 iterations and stop (this is for testing only, 
		        //  for real applications, use 1000 to 2000 iterations)
		        model.setNumIterations(numIterations);
		        model.estimate();

		        // Show the words and topics in the first instance

		        // The data alphabet maps word IDs to strings
		        Alphabet dataAlphabet = instances.getDataAlphabet();
		        
		        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
		        LabelSequence topics = model.getData().get(0).topicSequence;
		        
		        Formatter out = new Formatter(new StringBuilder(), Locale.US);
		        for (int position = 0; position < tokens.getLength(); position++) {
		            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
		        }
		        System.out.println(out);
		        
		        // Estimate the topic distribution of the first instance, 
		        //  given the current Gibbs state.
		        double[] topicDistribution = model.getTopicProbabilities(1);
		        //System.out.println("Distribution of topic 0: "+topicDistribution[0]);
		        // Get an array of sorted sets of word ID/count pairs
		        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		        
		        // Show top 5 words in topics with proportions for the first document
		        for (int topic = 0; topic < numTopics; topic++) {
		            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
		            
		            out = new Formatter(new StringBuilder(), Locale.US);
		            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
		            int rank = 0;
		            while (iterator.hasNext() && rank < 8) {
		                IDSorter idCountPair = iterator.next();
		                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
		                rank++;
		            }
		            System.out.println(out);
		        }
		        

		        
//		        DecimalFormat df = new DecimalFormat("#.##");
//		        for(int i = 0; i < instances.size(); i ++){
//		        	double[] topicDistr = model.getTopicProbabilities(i);
//		        	for(int topic = 0; topic < numTopics; topic ++){
//		        		String roundedNumber = df.format(topicDistr[topic]);
//		        		System.out.print(roundedNumber+ " ");
//		        	}
//		        	System.out.println("");
//		        }
		        
		        // Create a new instance with high probability of topic 0
//		        StringBuilder topicZeroText = new StringBuilder();
//		        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();
//
//		        int rank = 0;
//		        while (iterator.hasNext() && rank < 5) {
//		            IDSorter idCountPair = iterator.next();
//		            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
//		            rank++;
//		        }
//
//		        // Create a new instance named "test instance" with empty target and source fields.
//		        InstanceList testing = new InstanceList(instances.getPipe());
//		        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));
//
//		        TopicInferencer inferencer = model.getInferencer();
//		        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
//		        System.out.println("0\t" + testProbabilities[0]);
		    }
	
	/**
	 * Does the inference on a testing set of documents
	 * @param model	 - the model to use (load a model previously to get it)
	 * @param language - language of the inferencer (0 English, 1 Dutch)
	 * @param datasource - the source of the data file
	 * @param stopwords - the source of the stopwords list
	 * @param numTopics - the number of topics the model was trained on
	 * @param write - define if you want to write the feature vectors(distributions over topics) in a file
	 * @throws Exception
	 */
	
	public void doInference(ParallelTopicModel model, String datasource, String stopwords, int numTopics, boolean write) throws Exception{
		 ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

	        // Pipes: lowercase, tokenize, remove stopwords, map to features
	        pipeList.add( new CharSequenceLowercase() );
	        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
		//pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\s")) );
	        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwords), "UTF-8", false, false, false) );
	        pipeList.add( new TokenSequence2FeatureSequence() );

	        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

	        Reader fileReader = new InputStreamReader(new FileInputStream(new File(datasource)), "UTF-8");
	        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
	                                               3, 2, 1)); // data, label, name fields
	        
	        
	        
	        Formatter out = new Formatter(new StringBuilder(), Locale.US);
	        String extFilePath = "./src/main/java/com/ICSMT/features/";
	        PrintWriter writer = new PrintWriter(extFilePath+"featureVectors_language_"+language+"_"+numTopics+".data", "UTF-8");
	        //PrintWriter writer = new PrintWriter("./features/featureVectors_language_"+language+"_"+numTopics+"_align"+".data", "UTF-8");

		TopicInferencer inf = model.getInferencer();
	        
	        DecimalFormat df = new DecimalFormat("#.###");
	        for(int i=0; i< instances.size(); i++){
	        	//double[] topicDistribution = model.getTopicProbabilities(i);
	        	double[] topicDistribution = inf.getSampledDistribution(instances.get(i), 100, 10, 10);
	        	System.out.println(instances.get(i).getName());
	        	if (write)
	        		writer.print(instances.get(i).getName());
	        	for (int topic = 0; topic < numTopics; topic++) {
	                //Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
	                
	                out = new Formatter(new StringBuilder(), Locale.US);
	                out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
	                System.out.println(out);
	                if(write)
	                	writer.print(topicDistribution[topic]+" ");
	            
	            }
	        	if(write)
	        		writer.print("\n");
                System.out.println("");
	        }
	        
	        writer.close();
	        if(write)
	        	System.out.println("Finished Writing the feature vectors.");
	}
	
	/**
	 * Saves a model into a directory modelsLDA
	 * @param model
	 * @param options - String that contains number of topics and number of iterations for the Gibbs sampling
	 * Format of the string is for example: "numTopics_"+numTopics+"_numIterations_"+numIterations
	 */
	
	public void writeModel(ParallelTopicModel model, String options){
		String extFilePath = "./src/main/java/com/ICSMT/modelsLDA/";
		try{
			FileOutputStream fout = new FileOutputStream(extFilePath+"model_"+language+"_"+options+".model");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(model);
			oos.close();
			System.out.println("Done writing to file");
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	/**
	 * Loads a model from the directory modelsLDA
	 * @param options - String that contains number of topics and number of iterations for the Gibbs sampling
	 * @param language - The string of the language of the monolingual model
	 * Format of the string is for example: "numTopics_"+numTopics+"_numIterations_"+numIterations
	 */
	
	public void loadModel(String options,String language){
		String extFilePath = "./src/main/java/com/ICSMT/modelsLDA/";
		try{
			FileInputStream fin = new FileInputStream(extFilePath+"model_"+language+"_"+options+".model");
			ObjectInputStream ois = new ObjectInputStream(fin);
			this.model = (ParallelTopicModel) ois.readObject();
			ois.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}


	public ParallelTopicModel getModel() {
		return model;
	}

	public void setModel(ParallelTopicModel model) {
		this.model = model;
	}

	public int getNumTopics() {
		return numTopics;
	}

	public void setNumTopics(int numTopics) {
		this.numTopics = numTopics;
	}

	public int getNumIterations() {
		return numIterations;
	}

	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public static void main(String[] args) throws Exception{
		int numTopics = 50;
		int numIterations = 1000;
		String language = "english";
		//String options = "numTopics_"+numTopics+"_numIterations_"+numIterations+"_align";
		String options = "numTopics_"+numTopics+"_numIterations_"+numIterations;
		String extFilePath = "./src/main/java/com/ICSMT/";

		String pathEnglishData = extFilePath+"DataLDA/english_final.data";
		String pathDutchData = extFilePath+"DataLDA/dutch_final.data";
		
		String pathStopEn = extFilePath+"englishStopwords_mixed.txt";
		String pathStopDu = extFilePath+"dutchStopwords_mixed.txt";
		
		//String dataAlign = "src/main/java/com/ICSMT/DataLDA/data_align_lda.data";

		LDA lda = new LDA(pathStopEn, pathEnglishData, numTopics, numIterations, language);
		//LDA lda = new LDA(pathStopEn, dataAlign, numTopics, numIterations, language);
		// if you want to train the model
		lda.createModel();
	    	ParallelTopicModel model = lda.getModel();
		lda.writeModel(model, options);
		
		// parse an already trained one and estimate the feature vectors
		lda.loadModel(options,language);
		lda.doInference(lda.getModel(), pathEnglishData, pathStopEn, numTopics, true);
	}
	
	}

