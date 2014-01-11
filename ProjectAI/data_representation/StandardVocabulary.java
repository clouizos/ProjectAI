package data_representation;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.io.*;

/**
 * 
 * @author miriamhuijser
 * Class StandardVocabulary saves a standard vocabulary (or shortlist) for
 * a language. (Here English, German or Dutch, but this can easily be extended
 * by adding another textfile in another language.) This class parses the 
 * given textfile and saves the stopwords in a list. This class can be
 * consulted when one wants to know whether some word is a stopword. 
 * (Highly frequent non-representative word).
 */
public class StandardVocabulary{
	private String language;
	public Map<String, String> shortlist; 
	private String english = "englishShortlist.txt";
	private String german = "germanShortlist.txt";
	private String dutch = "dutchShortlist.txt";
	
	/**
	 * Constructor
	 * @param language - language of standard vocabulary
	 */
	public StandardVocabulary( String language ){
		this.language = language;
		this.shortlist = createShortlist(this.language);
	}

	/**
	 * This method creates a shortlist for the specified language.
	 * @param language - language for which a shortlist should be created
	 * @return sList - shortlist
	 */
	private Map<String, String> createShortlist( String language ){
		Map<String, String> sList = null;
		if( language.equals("english") )
			sList = createShortlistLanguage(english);
		else if( language.equals("dutch"))
			sList = createShortlistLanguage(dutch);
		else if( language.equals("german") )
			sList = createShortlistLanguage(german);
		return sList;
	}

	/**
	 * This method creates a shortlist for the specified language by parsing
	 * the appropriate textfile and adding these words to the shortlist.
	 * @param languageSList - textfile with words for the shortlist
	 * @return sList - resulting shortlist
	 */
	private Map<String, String> createShortlistLanguage(String languageSList){
		Map<String, String> sList = new HashMap<String, String>();
		File fileName = new File(languageSList);
		Scanner s;
		try {
			s = new Scanner( fileName );
			s.useDelimiter("[ ,!?.:()\"\n]+");
			while( s.hasNext() ){
				sList.put(s.next(), null);
			}
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sList;
	}
}
