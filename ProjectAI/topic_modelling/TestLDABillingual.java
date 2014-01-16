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

public class TestLDABillingual {

	public TestLDABillingual() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception{
		 // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        //pipeList.add( new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(args[2]), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(args[3]), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances_en = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8");
        instances_en.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields
        
        // use this also if you want billingual setting
        InstanceList instances_nl = new InstanceList(new SerialPipes(pipeList));
        fileReader = new InputStreamReader(new FileInputStream(new File(args[1])), "UTF-8");
        instances_nl.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
         										3, 2, 1));
        
        
        // for the billingual setting
        InstanceList[] training = new InstanceList[] {instances_en,instances_nl};
        
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 10;
        PolylingualTopicModel model = new PolylingualTopicModel(numTopics,1.0);
        
        model.addInstances(training);
        
        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        //model.setNumThreads(4);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(2);
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
        

        
//        DecimalFormat df = new DecimalFormat("#.##");
//        for(int i = 0; i < instances.size(); i ++){
//        	double[] topicDistr = model.getTopicProbabilities(i);
//        	for(int topic = 0; topic < numTopics; topic ++){
//        		String roundedNumber = df.format(topicDistr[topic]);
//        		System.out.print(roundedNumber+ " ");
//        	}
//        	System.out.println("");
//        }
        
        // Create a new instance with high probability of topic 0
//        StringBuilder topicZeroText = new StringBuilder();
//        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();
//
//        int rank = 0;
//        while (iterator.hasNext() && rank < 5) {
//            IDSorter idCountPair = iterator.next();
//            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
//            rank++;
//        }
//
//        // Create a new instance named "test instance" with empty target and source fields.
//        InstanceList testing = new InstanceList(instances.getPipe());
//        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));
//
//        TopicInferencer inferencer = model.getInferencer();
//        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
//        System.out.println("0\t" + testProbabilities[0]);
    }
	

	}
