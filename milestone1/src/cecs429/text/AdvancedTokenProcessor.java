package cecs429.text;

import org.tartarus.snowball.ext.englishStemmer;
import java.util.ArrayList;

public class AdvancedTokenProcessor implements TokenProcessor {

	@Override
	public ArrayList<String> processToken(String token) {
		// TODO Auto-generated method stub
		englishStemmer stemmer = new englishStemmer();
		ArrayList<String> list = new ArrayList<String>();
		token = token.replaceAll("^\\W+|\\W+$", ""); // remove non-alphanumerics from the beginning and the end of a string
		token = token.replaceAll("\"",""); //replace all quotes
        token = token.replaceAll("'", "").toLowerCase(); //replace all apostrophes and change it to lowercase
        if (token.contains("-")) { //split on hyphens adding all terms split by the hyphen and the full word without the hyphens
            String[] tokenList = token.split("-");
            for (String s : tokenList) {
                if (s.length() > 0) {
                    list.add(processToken(s).get(0));
                }
            }
            token = token.replaceAll("-", "");
        }
        list.add(token);
        //stems all the tokens in the list
        for (int i = 0; i < list.size(); i++) {
			stemmer.setCurrent(list.get(i));
			stemmer.stem();
			list.set(i,stemmer.getCurrent());
			list.set(i, (list.get(i)));
		}
		return list;
	}

}
