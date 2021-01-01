package cecs429.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.TokenProcessor;

public class RankedQueryParser {
	DiskPositionalIndex index;

	public RankedQueryParser(DiskPositionalIndex index) {
		this.index = index;
	}

	public List<RankedDocument> RankedRetrieval(String query, TokenProcessor proc) {
		String[] strList = query.split(" ");
		double WDT = 0;
		double WQT;
		double temp = 0;
		String processed;
		HashMap<Integer, Double> accumulatorVals = new HashMap<Integer, Double>();
		PriorityQueue<RankedDocument> rQueue = new PriorityQueue<RankedDocument>();
		List<RankedDocument> topDocs = new ArrayList<RankedDocument>();
		
		for (String s : strList) {
			processed = proc.processToken(s).get(0);
			//System.out.println(processed);
			WQT = index.getWQT(processed);
			//System.out.println(s + ": " + WQT);
			if(WQT > 0) {
			for (Posting p : index.getPostings(processed)) {
				WDT = p.getWDT(processed, p.getDocumentId());
				if (accumulatorVals.containsKey(p.getDocumentId())) {
					temp = accumulatorVals.get(p.getDocumentId());
					accumulatorVals.put(p.getDocumentId(), temp + (WQT * WDT));
				} else {
					accumulatorVals.put(p.getDocumentId(), WQT * WDT);
				}
			}
			}
		}
		for (int docID : accumulatorVals.keySet()) {
			temp = accumulatorVals.get(docID);
			if (temp != 0) {
				accumulatorVals.put(docID, ((double)temp /(double) index.getLD(docID)));
			}
		}
		for (int key : accumulatorVals.keySet()) {
			rQueue.add(new RankedDocument(key, accumulatorVals.get(key)));
		}
		for (int i = 0; i < 50; i++) {
			RankedDocument rd = rQueue.poll();
			if (rd != null) {
				topDocs.add(rd);
			}
		}
		return topDocs;
	}
}
