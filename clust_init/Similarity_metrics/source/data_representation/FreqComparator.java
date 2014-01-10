package data_representation;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * @author miriamhuijser
 * Class FreqComparator provides a method that compares entries of a hashmap.
 * In combination with the method:
 * Collections.sort(List<Entry<String, Double>> list,(new) FreqComparator comparator)
 * it helps sort a hashmap in ascending or descending order.
 * (See method sortList in class FrequencyList)
 */
public class FreqComparator implements Comparator<Entry<String, Double>>{
	boolean descending;
	
	/**
	 * Constructor
	 * @param descending - boolean that indicates whether the method compare
	 * should sort values in a descending or in an ascending order.
	 */
	public FreqComparator(boolean descending){
		this.descending = descending;
	}

	/**
	 * This method compares two entries and returns a value indicating their
	 * relation. (see java.util.Comparator for more information)
	 * @param entry1 - entry to be compared with entry2
	 * @param entry2 - entry to be compared with entry1
	 */
	public int compare(Entry<String, Double> entry1, 
			Entry<String, Double> entry2){
			int value = 0;
			if( descending ){
				value = entry2.getValue().compareTo(entry1.getValue());
			}
			else{
				value = entry1.getValue().compareTo(entry2.getValue());
			}
            return value;
    }
}