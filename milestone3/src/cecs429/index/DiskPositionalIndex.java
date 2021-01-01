package cecs429.index;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import edu.csulb.DiskIndexWriter;

public class DiskPositionalIndex implements Index {
	DB db = DBMaker.fileDB("file.db").make();
	ConcurrentMap<String, Long> cmap = db.hashMap("map", Serializer.STRING, Serializer.LONG).createOrOpen();
	Path path = Paths.get("index/postings.bin").toAbsolutePath();
	RandomAccessFile raf, weightsFile;

	int corpusSize;

	public DiskPositionalIndex() {
		try {
			raf = new RandomAccessFile(path.toString(), "r");
			weightsFile = new RandomAccessFile(Paths.get("index").toAbsolutePath() + "/docWeights.bin", "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Posting> getPostings(String term) {
		if(cmap.get(term)!= null) {
			return getPostingsFromFile(cmap.get(term), false);
		}
		return null;
	}

	public List<Posting> getPostingswithPos(String term) {
		return getPostingsFromFile(cmap.get(term), true);
	}

	public List<Posting> getPostingsFromFile(long termPos, boolean withPos) {
		List<Posting> postings = new ArrayList<Posting>();
		ArrayList<Integer> positions;
		try {
			raf.seek(termPos);
			int docFreq = raf.readInt();
			int lastDoc = 0;

			for (int i = 0; i < docFreq; i++) {
				int docID = raf.readInt() + lastDoc;
				lastDoc = docID;

				double weight = raf.readDouble();

				int termFreq = raf.readInt();
				int lastPos = 0;
				positions = new ArrayList<Integer>();
				if (withPos) {
					for (int j = 0; j < termFreq; j++) {
						int curPos = raf.readInt() + lastPos;
						positions.add(curPos);
						lastPos = curPos;
					}
				} else {
					raf.seek(raf.getFilePointer() + (termFreq * 4));
				}
				postings.add(new Posting(docID, positions, cmap, weight));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return postings;
	}

	@Override
	public List<String> getVocabulary() {
		Set<String> keys = cmap.keySet();
		List<String> vocabulary = new ArrayList<String>();
		for (String s : keys) {
			vocabulary.add(s);
		}
		Collections.sort(vocabulary);
		return Collections.unmodifiableList(vocabulary);
	}

	public double getLD(int docID) {
		double LD = 0;
		try {
			weightsFile.seek(docID * 8);
			LD = weightsFile.readDouble();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return LD;
	}

	public void setCorpusSize(int size) {
		corpusSize = size;
	}

	public int getCorpusSize() {
		return corpusSize;
	}

	public double getWQT(String term) {
		int docFreq = getDocFreq(term);
		if(docFreq == 0) {
			return 0;
		}
		double WQT = Math.log(1 + ((double) corpusSize / (double) docFreq));
		return WQT;
	}

	public int getDocFreq(String term) {
		List<Posting> list = new ArrayList<Posting>();
		list = getPostings(term);
		if(list == null) {
			return 0;
		}
		return list.size();
	}

	public void closeDB() {
		try {
			weightsFile.close();
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db.close();
	}
}
