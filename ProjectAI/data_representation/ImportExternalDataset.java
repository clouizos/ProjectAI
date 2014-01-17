package data_representation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cc.mallet.topics.PolylingualTopicModel;

public class ImportExternalDataset {

	private String filePath;
	
	public ImportExternalDataset(String filePath) {
		this.filePath = filePath;
	}

	public Map<String,ArrayList<Double>> importData(String options){
		Map<String, ArrayList<Double>> dataset = new HashMap<String, ArrayList<Double>>();
		try{
			FileInputStream fin = new FileInputStream(filePath+options);
			InputStreamReader in = new InputStreamReader(fin, "UTF-8");
			BufferedReader br = new BufferedReader(in);
			String line;
			while ((line = br.readLine()) != null) {
			    String[] words = line.split(",");
			    String[] numbers;
			    String key;
			    ArrayList<Double> data = new ArrayList<Double>();
			    
			    key = words[0];
			    numbers = words[1].split(" ");
			    //System.out.println("size:"+numbers.length);
			    for (String number: numbers){
			    	//System.out.println(Double.parseDouble(number));
			    	data.add(Double.parseDouble(number));
			    }
			    dataset.put(key, data);
			}
			
			//System.out.println(dataset.keySet());
			br.close();
			fin = null;
			in = null;
			br = null;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return dataset;
	}
	
	public static void main(String[] args) {
		String language = "English";
		int numTopics = 30;
		String filePath = "./featureVectorsLDA/";
		Map<String, ArrayList<Double>> dataset = new HashMap<String, ArrayList<Double>>();
		ImportExternalDataset imp = new ImportExternalDataset(filePath);
		String options = "featureVectors_language_"+language+"_"+numTopics+".data";
		dataset = imp.importData(options);
		System.out.println(dataset);
	}

}
