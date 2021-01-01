package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other Query objects and merges their postings with a
 * union-type operation.
 */
public class OrQuery implements Query {
	// The components of the Or query.
	private List<Query> mChildren;
	private boolean isPos;
	public OrQuery(Iterable<Query> children) {
		isPos = true;
		mChildren = (List<Query>) children;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> pList = new ArrayList<Posting>();
		pList = mChildren.get(0).getPostings(index); // set pList to first element

		List<Posting> curList = new ArrayList<Posting>();
		
		for (int i = 1; i < mChildren.size(); i++) {
			
			List<Posting> tempList = new ArrayList<Posting>();
			curList = mChildren.get(i).getPostings(index);

			int countA = 0;
			int countB = 0;
			
			Posting p_A = pList.get(countA);
			Posting p_B = curList.get(countB);
			
			int docID_A = p_A.getDocumentId();
			int docID_B = p_B.getDocumentId();
			
			while (countA < pList.size() && countB < curList.size()) { // while both the counters for both pList and curList do not exceed their size
				
				p_A = pList.get(countA);
				p_B = curList.get(countB);
				
				docID_A = p_A.getDocumentId();
				docID_B = p_B.getDocumentId();
				
				Posting lastPosting = null;
				if(!(tempList.isEmpty())) {
					lastPosting = tempList.get(tempList.size() - 1);
				}
				
				if (docID_A == docID_B) { //if docIDs match add both postings to the list
					tempList.add(p_A);
					lastPosting = tempList.get(tempList.size() - 1);
					for (int pos : p_B.getPositions()) {
						lastPosting.addPosition(pos);
					}
					countA++;
					countB++;
				} else if (docID_A < docID_B) { //if docID_A is less than B add the postings from pList to tempList
					if(tempList.isEmpty()) {
						tempList.add(p_A);
					}
					else if (lastPosting.getDocumentId() == docID_A) {
						for (int pos : p_A.getPositions()) {
							lastPosting.addPosition(pos);
						}
					} else {
						tempList.add(p_A);
					}
					countA++;
				} else if (docID_B < docID_A) { //if docID_B is less than A add the postings from curList to tempList
					if(tempList.isEmpty()) {
						tempList.add(p_B);
					}
					else if (lastPosting.getDocumentId() == docID_B) {
						for (int pos : p_B.getPositions()) {
								lastPosting.addPosition(pos);
						}
					} else {
						tempList.add(p_B);
					}
					countB++;
				}
			}
			
			if (countA == pList.size() && docID_B == curList.size()) { //once broken out of the loop add the remaining postings from the remaining list

			} else if (countA == pList.size()) {
				for (int j = countB; j < curList.size(); j++) {
					tempList.add(curList.get(j));
				}
			} else if (countB == curList.size()) {
				for (int j = countA; j < pList.size(); j++) {
					tempList.add(pList.get(j));
				}
			}
			pList = tempList; // set pList to tempList
		} // end of for loop
			// TODO: program the merge for an OrQuery, by gathering the postings of the
			// composed Query children and
			// unioning the resulting postings.

		return pList;
	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" + String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList())) + " )";
	}

	@Override
	public boolean isPositive() {
		return isPos;
	}

	@Override
	public void setNegative() {
		isPos = false;
	}
}
