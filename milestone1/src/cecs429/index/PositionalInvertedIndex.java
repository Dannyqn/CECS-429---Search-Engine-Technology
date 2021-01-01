package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PositionalInvertedIndex implements Index{
	private HashMap<String, List<Posting>> map = new HashMap<String, List<Posting>>();
	
	@Override
	public List<Posting> getPostings(String term) {
		if(map.containsKey(term)) {
			return map.get(term);
		}
		return null;
	}
	public void addTerm(String term, int documentId, int position) {
		List<Posting> pList = map.get(term);
		if(map.containsKey(term)) {
			if(pList.get(pList.size()-1).getDocumentId() != documentId) {
				List<Posting> list = getPostings(term);
				ArrayList<Integer> posList = new ArrayList<Integer>();
				posList.add(position);
				list.add(new Posting(documentId,posList));
				map.put(term, list);
			}
			else {
				List<Posting> list = getPostings(term);
				Posting p = list.get(list.size() - 1);
				p.addPosition(position);
			}
		}
		else {
			List<Posting> list = new ArrayList<Posting>();
			ArrayList<Integer> posList = new ArrayList<Integer>();
			posList.add(position);
			list.add(new Posting(documentId,posList));
			map.put(term, list);
		}
	}
	@Override
	public List<String> getVocabulary() {
		Set<String> keys = map.keySet();
		List<String> vocabulary = new ArrayList<String>();
		for(String s: keys) {
			vocabulary.add(s);
		}
		Collections.sort(vocabulary);
		return Collections.unmodifiableList(vocabulary); 
	}

}
