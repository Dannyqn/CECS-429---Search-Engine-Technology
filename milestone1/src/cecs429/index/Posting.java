package cecs429.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private ArrayList<Integer> mPositions;
	public Posting(int documentId, ArrayList<Integer> position) {
		mDocumentId = documentId;
		mPositions = position;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
	
	public ArrayList<Integer> getPositions() {
		return mPositions;
	}
	public void addPosition(int position) {
		mPositions.add(position);
		Collections.sort(mPositions);
	}
}
