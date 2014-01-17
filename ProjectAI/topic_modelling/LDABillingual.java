package topic_modelling;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.*;
import java.io.*;

public class LDABillingual {
	
	private PolylingualTopicModel model;
	String stopwords_en;
	String stopwords_nl;
	String data_source_en;
	String data_source_nl;
	private int numTopics;
	private int numIterations;

	public LDABillingual(String stopwords_en, String stopwords_nl, String data_source_en, String data_source_nl, int numTopics, int numIterations) {
		this.stopwords_en = stopwords_en;
		this.stopwords_nl = stopwords_nl;
		this.data_source_en = data_source_en;
		this.data_source_nl = data_source_nl;
		this.numTopics = numTopics;
		this.numIterations = numIterations;
	}

	/**
	 * Creates a model for the Bilingual LDA, all the necessary variables are initialized through the constructor
	 * @throws Exception
	 */
	public void createModel() throws Exception{
		 // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        //pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwords_en), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwords_nl), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances_en = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(new File(data_source_en)), "UTF-8");
        instances_en.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields
        
        // use this also if you want billingual setting
        InstanceList instances_nl = new InstanceList(new SerialPipes(pipeList));
        fileReader = new InputStreamReader(new FileInputStream(new File(data_source_nl)), "UTF-8");
        instances_nl.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
         										3, 2, 1));
        
        
        // for the billingual setting
        InstanceList[] training = new InstanceList[] {instances_en,instances_nl};
        
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        //int numTopics = 30;
        model = new PolylingualTopicModel(numTopics,1.0);
        
        model.addInstances(training);
        
        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        //model.setNumThreads(4);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        //int numIterations = 2;
        model.setNumIterations(numIterations);
        model.estimate();

        // Show the words and topics in the first instance

        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet_en = instances_en.getDataAlphabet();
        Alphabet dataAlphabet_nl = instances_nl.getDataAlphabet();
        
        FeatureSequence tokens_en = (FeatureSequence) model.getData().get(0).instances[0].getData();
        FeatureSequence tokens_nl = (FeatureSequence) model.getData().get(0).instances[1].getData();
        
        LabelSequence topics_en = model.getData().get(0).topicSequences[0];
        LabelSequence topics_nl = model.getData().get(0).topicSequences[1];
        
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        //for (int position = 0; position < tokens_en.getLength(); position++) {
        //    out.format("%s-%d ", dataAlphabet_en.lookupObject(tokens_en.getIndexAtPosition(position)), topics_en.getIndexAtPosition(position));
        //}
        //System.out.println(out);
        
        
        
        // Estimate the topic distribution of the first English instance, 
        //  given the current Gibbs state.
        //Labeling td = model.getData().get(2).topicDistribution;
        //System.out.println("test:"+model.getData().get(0));
        TopicInferencer infer_en = model.getInferencer(0);
        TopicInferencer infer_nl = model.getInferencer(1);
        
        // Compare the distributions of topics for two documents (one is the translation of the other)
        // the distributions should be approximately the same
        double[] topicDistribution_en = infer_en.getSampledDistribution(instances_en.get(3), 0, 0, 0);
        double[] topicDistribution_nl= infer_nl.getSampledDistribution(instances_nl.get(3), 0, 0, 0);
        System.out.println("----------------------------------------------");
        //model.printTopWords(System.out, 15, false);
        
        
        //Labeling topicDistribution = model.getData().get(0).topicDistribution.;
        //System.out.println("Distribution of topic 0: "+topicDistribution[0]);
        // Get an array of sorted sets of word ID/count pairs
        //ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        
        // Show top 5 words in topics with proportions for the first document
        
        for (int topic = 0; topic < numTopics; topic++) {
            //Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            
            out = new Formatter(new StringBuilder(), Locale.US);
            out.format("%d\t%.3f\t", topic, topicDistribution_en[topic]);
            out.format("%d\t%.3f\t", topic, topicDistribution_nl[topic]);
            int rank = 0;
            //while (iterator.hasNext() && rank < 8) {
            //    IDSorter idCountPair = iterator.next();
            //    out.format("%s (%.0f) ", dataAlphabet_en.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                //out.format("%s (%.0f) ", dataAlphabet_nl.lookupObject(idCountPair.getID()), idCountPair.getWeight());
            //    rank++;
            //}
            System.out.println(out);
        }
        
        
        //String options = "numTopics_"+numTopics+"_numIterations_"+numIterations;
        
        //writeModel(model,options);
        
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
	
	public void doInference(PolylingualTopicModel model, int language, String datasource, String stopwords, int numTopics, boolean write) throws Exception{
		 ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

	        // Pipes: lowercase, tokenize, remove stopwords, map to features
	        pipeList.add( new CharSequenceLowercase() );
	        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
	        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwords), "UTF-8", false, false, false) );
	        pipeList.add( new TokenSequence2FeatureSequence() );

	        InstanceList instances = new InstanceList (new SerialPipes(pipeList));

	        Reader fileReader = new InputStreamReader(new FileInputStream(new File(datasource)), "UTF-8");
	        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
	                                               3, 2, 1)); // data, label, name fields
	        
	        
	        
	        TopicInferencer infer_lang = model.getInferencer(language);
	        Formatter out = new Formatter(new StringBuilder(), Locale.US);
	        PrintWriter writer = new PrintWriter("./featureVectorsLDA/bilfeatureVectors_language_"+language+"_"+numTopics+".data", "UTF-8");
	        		
	        for(Instance doc : instances){
	        	double[] topicDistribution = infer_lang.getSampledDistribution(doc, 0, 0, 0);
	        	System.out.println(doc.getName());
	        	if (write)
	        		writer.print(doc.getName());
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
	
	public void writeModel(PolylingualTopicModel model, String options){
		try{
			FileOutputStream fout = new FileOutputStream("./modelsLDA/bilmodel_"+options+".model");
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
	 * Format of the string is for example: "numTopics_"+numTopics+"_numIterations_"+numIterations
	 */
	
	public void loadModel(String options){
		try{
			FileInputStream fin = new FileInputStream("./modelsLDA/bilmodel_"+options+".model");
			ObjectInputStream ois = new ObjectInputStream(fin);
			this.model = (PolylingualTopicModel) ois.readObject();
			ois.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
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
	
	public PolylingualTopicModel getModel() {
		return model;
	}

	public void setModel(PolylingualTopicModel model) {
		this.model = model;
	}
	
	public static void main(String[] args) throws Exception{
		int numTopics = 30;
		int numIterations = 50;
		String options = "numTopics_"+numTopics+"_numIterations_"+numIterations;
		
		LDABillingual ldabil = new LDABillingual(args[2], args[3], args[0], args[1], numTopics, numIterations);
		
		// if you want to train the model
		//ldabil.createModel();
		//PolylingualTopicModel model = ldabil.getModel();
		//ldabil.writeModel(model, options);
		
		// parse an already trained one and estimate the feature vectors
		ldabil.loadModel(options);
		ldabil.doInference(ldabil.getModel(), 1, args[1], args[3], numTopics, true);
	}

	
	
}
