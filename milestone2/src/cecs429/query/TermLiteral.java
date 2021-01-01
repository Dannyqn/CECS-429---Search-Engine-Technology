package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query {
	private String mTerm;
	private boolean isPos;
	private TokenProcessor mProcessor;
	public TermLiteral(String term, TokenProcessor processor) {
		isPos = true;
		mTerm = term;
		mProcessor = processor;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		String stemmed = mProcessor.processToken(mTerm).get(0);
		return index.getPostingswithPos(stemmed);
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
