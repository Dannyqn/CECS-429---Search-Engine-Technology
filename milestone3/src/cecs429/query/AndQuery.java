package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other Query objects and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements Query {
	private List<Query> mChildren;
	private boolean isPos;

	public AndQuery(Iterable<Query> children) {
		isPos = true;
		mChildren = (List<Query>) children;
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> pList = new ArrayList<Posting>();
		pList = mChildren.get(0).getPostings(index); // set pList to first element

		List<Posting> curList = new ArrayList<Posting>(); // current list of postings for the query

		for (int i = 1; i < mChildren.size(); i++) {
			List<Posting> tempList = new ArrayList<Posting>(); // templist to hold the merge between pList and curList
			curList = mChildren.get(i).getPostings(index);

			int countA = 0;
			int countB = 0;
			
			Posting p_A;
			Posting p_B;
			
			int docID_A;
			int docID_B;
			
			Posting lastPosting = null;
			
			if (mChildren.get(0).isPositive() == false && i == 1 || mChildren.get(i).isPositive() == false) { // if the first or current element is negative
				
				p_A = pList.get(countA); // current posting in pList
				p_B = curList.get(countB); // current posting in curList
																												 
				docID_A = p_A.getDocumentId(); // docID counter for pList
				docID_B = p_B.getDocumentId(); // docID counter for curList

				//last posting in tempList
				while (countA < pList.size() && countB < curList.size()) { // while both the counters for both pList and
																			// curList do not exceed their size
					p_A = pList.get(countA); 
					p_B = curList.get(countB);
					
					docID_A = p_A.getDocumentId(); // docID_A = the "countA" docid in the postings list
																	// for pList
					docID_B = p_B.getDocumentId(); // docID_B = the "countB" docid in the postings list
																	// for curList
					if (docID_A == docID_B) { // if docIDs match increment the counts but dont add either because we are
												// performing an AND NOT
						countA++;
						countB++;
					} // end of if
					else {// if docIDs do not match increment the smaller of the two
						if (docID_A < docID_B) { // add the posting from pList if it is positive
							if (mChildren.get(0).isPositive()) {
								tempList.add(p_A);
							}
							countA++;
						} else if (docID_B < docID_A) { // add the posting from curList if it is positive
							if (mChildren.get(i).isPositive()) {
								tempList.add(p_B);
							}
							countB++;
						}
					} // end of else
				} // end of while loop
				
				//once broken out of the loop add the remaining postings from the list if is positive
				if (countA == pList.size() && mChildren.get(i).isPositive() && docID_B != curList.size()) { 
					for (int j = countB; j < curList.size(); j++) {
						tempList.add(curList.get(j));
					}
				} else if (countB == curList.size() && mChildren.get(0).isPositive() && docID_A != pList.size()) {
					for (int j = countA; j < pList.size(); j++) {
						tempList.add(pList.get(j));
					}
				}
			} else {
				//perform regular AND operation
				while (countA < pList.size() && countB < curList.size()) {
					
					p_A = pList.get(countA); // current posting in pList
					p_B = curList.get(countB); // current posting in curList
					
					
					docID_A = p_A.getDocumentId();
					docID_B = p_B.getDocumentId();
					
					if (docID_A == docID_B) { // if docIDs match iterate through pos lists
						tempList.add(p_A);
						lastPosting = tempList.get(tempList.size() - 1);
						for (int pos : p_B.getPositions()) {
							lastPosting.addPosition(pos);
						}

						countA++;
						countB++;
					} // end of if
					else {// if docIDs do not match increment the smaller of the two
						if (docID_A <= docID_B) {
							countA++;
						} else {
							countB++;
						}
					} // end of else
				} // end of while loop
			}//end of else
			pList = tempList; // set pList to tempList
		} // end of for loop
			// TODO: program the merge for an AndQuery, by gathering the postings of the
			// composed QueryComponents and
			// intersecting the resulting postings.

		return pList;
	}

	@Override
	public String toString() {
		return String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}

	@Override
	public boolean isPositive() {
		// TODO Auto-generated method stub
		return isPos;
	}

	@Override
	public void setNegative() {
		isPos = false;
	}
}
