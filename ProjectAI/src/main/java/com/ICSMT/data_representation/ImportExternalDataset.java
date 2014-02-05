package data_representation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author christos
 * Class that imports the external datasets created from LDA and LSA
 *
 */
public class ImportExternalDataset {

	private String filePath;
	
	/**
	 * Constructor
	 * @param filePath - the path of the file
	 */
	public ImportExternalDataset(String filePath) {
		this.filePath = filePath;
	}

	public Map<String,ArrayList<Double>> importData(){
		Map<String, ArrayList<Double>> dataset = new HashMap<String, ArrayList<Double>>();
		try{
			FileInputStream fin = new FileInputStream(filePath);
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
		String filePath = "./features/";
		filePath = filePath + "features_lsa_English_100.data";
		Map<String, ArrayList<Double>> dataset = new HashMap<String, ArrayList<Double>>();
		ImportExternalDataset imp = new ImportExternalDataset(filePath);
		dataset = imp.importData();
		System.out.println(dataset);
	}

}
