package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query {
	private String mTerm;
	private boolean isPos;
	public TermLiteral(String term) {
		isPos = true;
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		return index.getPostings(processor.processToken(mTerm).get(0));
	}
	
	@Override
	public String toString() {
		return mTerm;
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
