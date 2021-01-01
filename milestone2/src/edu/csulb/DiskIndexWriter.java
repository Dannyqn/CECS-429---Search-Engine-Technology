package edu.csulb;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.*;

import cecs429.index.Index;
import cecs429.index.Posting;

public class DiskIndexWriter {
	DB db = DBMaker.fileDB("file.db").make();
	ConcurrentMap<String, Long> cmap = db.hashMap("map", Serializer.STRING, Serializer.LONG).createOrOpen();
	ArrayList<HashMap<String, Double>> weightList = new ArrayList<HashMap<String, Double>>();
	int docSize;
	List<String> vList = new ArrayList<String>();

	public List<Long> writeIndex(Index index, Path path) {

		List<Long> bytePositions = new ArrayList<Long>();

		for (int i = 0; i < docSize; i++) {
			weightList.add(new HashMap<String, Double>());
		}

		boolean firstDocID = true;
		boolean firstPos = true;

		int lastDocID = 0;
		int lastPos = 0;

		try {
			RandomAccessFile weightsFile = new RandomAccessFile(path.toString() + "/docWeights.bin", "rw");
			RandomAccessFile raf = new RandomAccessFile(path.toString() + "/postings.bin", "rw");
			raf.seek(0);
			for (String s : index.getVocabulary()) {
				bytePositions.add(raf.getFilePointer());
				cmap.put(s, raf.getFilePointer());
				raf.writeInt(index.getPostings(s).size());
				for (Posting p : index.getPostings(s)) {
					if (firstDocID) {
						raf.writeInt(p.getDocumentId());
						firstDocID = false;
						lastDocID = p.getDocumentId();
					} else {
						raf.writeInt(p.getDocumentId() - lastDocID);
						lastDocID = p.getDocumentId();
					}
					raf.writeDouble(1 + Math.log(p.getPositions().size()));
					raf.writeInt(p.getPositions().size());
					weightList.get(p.getDocumentId()).put(s, 1 + Math.log(p.getPositions().size()));
					for (int pos : p.getPositions()) {
						if (firstPos) {
							raf.writeInt(pos);
							firstPos = false;
							lastPos = pos;
						} else if (firstPos == false) {
							raf.writeInt(pos - lastPos);
							lastPos = pos;
						}
					}
					firstPos = true;
				}
				firstDocID = true;
			}
			double sumwd = 0;
			for (int i = 0; i < weightList.size(); i++) {
				for (String key : weightList.get(i).keySet()) {
					sumwd += Math.pow(weightList.get(i).get(key), 2);
				}
				weightsFile.writeDouble(Math.sqrt(sumwd));
				sumwd = 0;
			}
			weightsFile.close();
			raf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytePositions;
	}

	public void setDocSize(int size) {
		docSize = size;
	}

	public ConcurrentMap<String, Long> getMap() {
		return cmap;
	}

	public void closeDB() {
		db.close();
	}
}
