package ranking;

import io.FileLoadingUtils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import data_representation.Document;
import data_representation.Centroid;

public class IDFStopwords {

	private Map<String, Double> idf = new HashMap<String, Double>();
	private String filePath;
	ArrayList<Document> documentObjects = new ArrayList<Document>();
	private String language;
	private double nrDocs;
	
	public IDFStopwords(String filePath, String language) {
		this.filePath = filePath;
		this.language = language;
	}
	
	public void init(){
		ArrayList<String> documentNames = FileLoadingUtils.listFilesDirectory(filePath);
		for( int i = 0; i < documentNames.size(); i++ ){
			Document doc = new Document( documentNames.get(i), language );
			documentObjects.add(doc);
		}
		nrDocs = (double)documentNames.size();
		Centroid dummyCent = new Centroid();
		double value = 0;
		for( int i = 0; i < documentObjects.size(); i++ ){
			documentObjects.get(i).createList( dummyCent, "forgy" );
			Set<String> words = documentObjects.get(i).words.keySet();
			
			for(String word: words){
				try{
					value = idf.get(word);
					value ++;
					idf.put(word, value);
				}catch(NullPointerException ex){ // if the key doesnt exist
					//ex.printStackTrace();
					idf.put(word, 1.0);
				}
			}
			System.out.println("Document parsed...");
			
		}
	}
	
	public ArrayList<String> createRareList(double percentageRare, boolean writeList, String path, String filename){
		Set<String> keys = idf.keySet();
		double value = 0;
		double thres_common = nrDocs * percentageRare; // usually 1%
		ArrayList<String> words = new ArrayList<String>();
		for(String key : keys){
			value = idf.get(key);
			if(value >= thres_common)
				words.add(key);
		}
		
		if(writeList)
			write(path, filename, words);
		
		return words;
		
	}
	
	public ArrayList<String> createCommonList(double percentageCommon, boolean writeList, String path, String filename){
		Set<String> keys = idf.keySet();
		double value = 0;
		double thres_common = nrDocs * percentageCommon; // 90% of documents is considered common
		ArrayList<String> words = new ArrayList<String>();
		for(String key : keys){
			value = idf.get(key);
			if(value >= thres_common)
				words.add(key);
		}
		
		if(writeList)
			write(path, filename, words);
			
		return words;
		
	}
	
	public ArrayList<String> createMixedList(double percentageCommon, double percentageRare, boolean writeList, String path, String filename){
		Set<String> keys = idf.keySet();
		double value = 0;
		double thres_common = nrDocs * percentageCommon; // 90% of documents is considered common
		double thres_rare = nrDocs * percentageRare; // usually 1%
		ArrayList<String> words = new ArrayList<String>();
		for(String key : keys){
			value = idf.get(key);
			if(value >= thres_common)
				words.add(key);
			else if(value <= thres_rare)
				words.add(key);
		}
		
		System.out.println(words.size());
		if(writeList)
			write(path, filename, words);
			
		return words;
	}
	
	/**
	 * Writes the stopwords list
	 * @param path - path of the stopwords list
	 * @param filename - name of the file
	 * @param words - ArrayList of the words to be inserted
	 * @throws Exception
	 */
	public void write(String path, String filename, ArrayList<String> words){
		 try{
			 PrintWriter writer = new PrintWriter(path+filename, "UTF-8");
			 for(int i=0; i< words.size(); i++){
				 writer.println(words.get(i));
			 }
			 writer.close();
		 }catch(Exception ex){
			 ex.printStackTrace();
		 }
		 System.out.println("Finished writing.");
		 
		
	}
	
	public double getNrDocs() {
		return nrDocs;
	}

	public void setNrDocs(int nrDocs) {
		this.nrDocs = nrDocs;
	}

	public Map<String, Double> getIdf() {
		return idf;
	}

	public void setIdf(Map<String, Double> idf) {
		this.idf = idf;
	}

	public static void main(String[] args) {
		String filePath = "./Testdata/dataset/Dutch";
		String language = "dutch";
		String path = "./";
		String filename = language+"Stopwords_mixed.txt";
		
		IDFStopwords IDF = new IDFStopwords(filePath, language);
		IDF.init();
		IDF.createMixedList(0.9, 0.01, true, path, filename);
		
		/*idf = IDF.getIdf();
		Set<String> keys = idf.keySet();
		double value = 0;
		double nrDocs = IDF.getNrDocs();
		double thres_common = nrDocs * 0.9; // 90% of documents is considered common
		for(String key : keys){
			value = idf.get(key);
			if(value >= thres_common)
				System.out.println("Common: "+key);
			else if(value == 1.0)
				System.out.println("Rare: "+key);
		}
		*/

	}

}
