package cecs429.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */

public class Posting {
	private int mDocumentId;
	private ArrayList<Integer> mPositions;
	private ConcurrentMap<String, Long> cmap;
	private double weight;
	public Posting(int documentId, ArrayList<Integer> position) {
		mDocumentId = documentId;
		mPositions = position;
		
	}
	public Posting(int documentId, ArrayList<Integer> position, ConcurrentMap<String, Long> cmap, double weight) {
		mDocumentId = documentId;
		mPositions = position;
		this.weight = weight;
		this.cmap = cmap;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
	public double getWDT(String term, int docID) {
		return weight; 
	}
	public ArrayList<Integer> getPositions() {
		return mPositions;
	}
	public void addPosition(int position) {
		mPositions.add(position);
		Collections.sort(mPositions);
	}
}
