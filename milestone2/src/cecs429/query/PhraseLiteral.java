package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements Query {
	// The list of individual terms in the phrase.
	private List<Query> mChildren = new ArrayList<>();
	private boolean isPos;
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(Iterable<Query> children) {
		isPos = true;
		for (Query q: children) {
			mChildren.add(q);
		}
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> pList = new ArrayList<Posting>();
		pList = mChildren.get(0).getPostings(index); //set pList to first element
		
		List<Posting> curList = new ArrayList<Posting>();
		
		for(int i = 1; i < mChildren.size(); i++) {
			List<Posting> tempList = new ArrayList<Posting>();
			curList = mChildren.get(i).getPostings(index);
			
			int countA = 0;
			int countB = 0;
			
			Posting p_A = pList.get(countA);
			Posting p_B = curList.get(countB);
			
			int docID_A = p_A.getDocumentId();
			int docID_B = p_B.getDocumentId();
			
			
			while(countA < pList.size() && countB < curList.size()) {
				
				p_A = pList.get(countA);
				p_B = curList.get(countB);
				
				docID_A = p_A.getDocumentId();
				docID_B = p_B.getDocumentId();
				
				if(docID_A == docID_B) { //if docIDs match iterate through pos lists  
					
					int pos1 = 0;
					int pos2 = 0;
					
					ArrayList<Integer> posA = p_A.getPositions();
					ArrayList<Integer> posB = p_B.getPositions();
					
					Posting lastPosting = null;
					
					
					while (pos1 < posA.size() && pos2 < posB.size()) {
						if(!(tempList.isEmpty())) {
							lastPosting = tempList.get(tempList.size() - 1);
						}
						if(posB.get(pos2) == posA.get(pos1) + i) { //if posB = posA + i, it means it is consecutive #'s so add posting of A to tempList
							if(tempList.isEmpty()) { //checks if list is empty
								tempList.add(p_A);
							}
							else if(lastPosting.getDocumentId() == docID_A) { //if doc id is already in temp list just add the position
								lastPosting.addPosition(countA);
							}
							else { //else just add the posting
								tempList.add(p_A);
							}
							pos1++; //increment position of posA
							pos2++; //increment position of posB
						}
						//increment the smaller of the two positions if posB != posA + i
						else if(posA.get(pos1) <= posB.get(pos2)) { 
							pos1++;
						}
						else {
							pos2++;
						}
					}
					countA++;
					countB++;
				}//end of if
				else {//if docIDs do not match increment the smaller of the two
					if(docID_A <= docID_B) {
						countA++;
					}
					else {
						countB++;
					}
				}//end of else 
			}//end of while loop
			pList = tempList; //set pList to tempList
		} //end of for loop
		return pList;
		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
	}
	@Override
	public String toString() {
		return "\"" + String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList())) + "\"";
	}

	@Override
	public boolean isPositive() {
		return isPos;
	}
	public void setNegative() {
		isPos = false;
	}
}
