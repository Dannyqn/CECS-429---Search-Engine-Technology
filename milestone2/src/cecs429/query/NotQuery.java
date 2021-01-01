package cecs429.query;

import java.util.List;
import java.util.stream.Collectors;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;

public class NotQuery implements Query{
	private boolean isPos;
	
	private List<Query> mChildren;
	public NotQuery(Iterable<Query> children) {
		mChildren = (List<Query>) children;
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		for(Query q: mChildren) {
			q.setNegative();
			return q.getPostings(index);
		}
		return null;
	}
	
	@Override
	public String toString() {	
		return String.join("-", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
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
