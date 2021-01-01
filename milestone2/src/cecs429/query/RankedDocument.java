package cecs429.query;

public class RankedDocument implements Comparable<RankedDocument> {

	int docID;
	double acc;

	public RankedDocument(int docID, double acc) {
		this.docID = docID;
		this.acc = acc;
	}

	public int getDocID() {
		return docID;
	}

	public double getAcc() {
		return acc;
	}

	@Override
	public int compareTo(RankedDocument rd) {
		if (this.acc < rd.getAcc()) {
			return 1;
		} else if (this.acc == rd.getAcc()) {
			return 0;
		} else {
			return -1;
		}
	}

}
