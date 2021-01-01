package edu.csulb;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;

import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.RankedDocument;
import cecs429.query.RankedQueryParser;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class PositionalInvertedIndexer {
	public static void main(String[] args) throws Exception {
		Scanner scan = new Scanner(System.in);
		System.out.print("1.Build Index\n2.Query Index\n");
		while(true) {
			switch(scan.nextInt()) {
				case 1: 
					Scanner dir = new Scanner(System.in);
					System.out.println("What is the path of the directory you would like to index: ");
					String s = dir.nextLine();
					System.out.println(Paths.get(s).toAbsolutePath());
					DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(s).toAbsolutePath());
					long startTime = System.nanoTime();
					Index index = indexCorpus(corpus, Paths.get(s).toAbsolutePath());
					DiskIndexWriter dw = new DiskIndexWriter();
					dw.setDocSize(corpus.getCorpusSize());
					dw.writeIndex(index, Paths.get("index").toAbsolutePath());
					dw.closeDB();
					long endTime = System.nanoTime();
					long totalTime = endTime - startTime;
					System.out.println("Corpus indexed in: " + totalTime / 1000000000 + " seconds");
					return;
				case 2:
					System.out.println("1.Boolean Retrieval\n2.Ranked Retrieval");
					switch(scan.nextInt()) {
					case 1:
						System.out.println("Enter corpus path: ");
						scan.nextLine();
						String pathName = scan.nextLine();	
						System.out.println(Paths.get(pathName).toAbsolutePath());
						DocumentCorpus corpusB = DirectoryCorpus.loadDirectory(Paths.get(pathName).toAbsolutePath());
						corpusB.getDocuments();
						DiskPositionalIndex d = new DiskPositionalIndex();
						while (true) {
							System.out.println("Enter search query: ");
							String query = scan.next(); // Asks user for term to search
							switch (query) {
							case "q":
								System.out.println("Program exited...");
								scan.close();
								d.closeDB();
								return;
							case "stem":
								AdvancedTokenProcessor processor = new AdvancedTokenProcessor(); 
								System.out.println(processor.processToken(scan.next()));
								break;
							case "vocab":
								List<String> vList = d.getVocabulary();
								if(vList.size() >= 100000000) {
									for(int i = 0; i < 1000; i++) {
										System.out.println(vList.get(i));
									}
								}
								else {
									for(int i = 0; i < vList.size(); i++) {
										System.out.println(vList.get(i));
									}
								}
								System.out.println("Total number of vocabulary: " + vList.size());
								break;
							case "index":
								String dirName = scan.next();
								corpus = DirectoryCorpus.loadDirectory(Paths.get(dirName).toAbsolutePath());
								startTime = System.nanoTime();
								index = indexCorpus(corpus, Paths.get(dirName).toAbsolutePath());
								endTime = System.nanoTime();
								totalTime = endTime - startTime;
								System.out.println("Corpus indexed in: " + totalTime / 1000000000 + " seconds");
								break;
							default:
								query += scan.nextLine();
								BooleanQueryParser b = new BooleanQueryParser();
								int docCount = 0;
								for (Posting p : b.parseQuery(query).getPostings(d)) {	
									System.out.println(p.getDocumentId() + ". " + corpusB.getDocument(p.getDocumentId()).getTitle());
									docCount++;
									//System.out.println(p.getPositions());
								}
								System.out.println("Number of Documents: " + docCount);

								// getting contents of a document selected by the user
								System.out.println("Would you like to view the contents of a Document? (Enter Doc ID) -1 for no");
								int docID = scan.nextInt();
								if(docID > 0 ) {
									BufferedReader bufferedReader = new BufferedReader(corpusB.getDocument(docID).getContent());
									StringBuilder stringBuilder = new StringBuilder();

									String line;
									while ((line = bufferedReader.readLine()) != null) {
										stringBuilder.append(line);
									}

									String str = stringBuilder.toString();
									System.out.println(str);
									bufferedReader.close();
								}
								break;
							}
						}
					case 2:
						System.out.println("Enter corpus path: ");
						scan.nextLine();
						String pathNameR = scan.nextLine();	
						System.out.println(Paths.get(pathNameR).toAbsolutePath());
						DocumentCorpus corpusR = DirectoryCorpus.loadDirectory(Paths.get(pathNameR).toAbsolutePath());
						corpusR.getDocuments();
						DiskPositionalIndex d2 = new DiskPositionalIndex();
						d2.setCorpusSize(corpusR.getCorpusSize());
						RankedQueryParser r = new RankedQueryParser(d2);
						ArrayList<ArrayList<Integer>> list = getRelevant(Paths.get(pathNameR).toAbsolutePath());
						ArrayList<String> queries = getQueries(Paths.get(pathNameR).toAbsolutePath());
						HashMap<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
						ArrayList<Integer> listofRelDocs = new ArrayList<Integer>();
						Set<Integer> set;
						for(int i = 0; i < queries.size(); i++) {
							map.put(queries.get(i), list.get(i));
						}
						List<RankedDocument> topKDocs = new ArrayList<RankedDocument>();
						double MRT = 0;
						for(int i = 0; i < 30; i++) {
							startTime = System.nanoTime();
							topKDocs = r.RankedRetrieval("what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft.", new AdvancedTokenProcessor());
							endTime = System.nanoTime();
							totalTime = endTime - startTime;
							MRT += ((double)totalTime/1000000000);
						}
						System.out.println("Mean Response Time for query1 over 30 iterations: " + (MRT/30) +" seconds");
						System.out.println("Throughput for query1 over 30 iterations: " + (1/(MRT/30)) +" queries per second");
						while(true) {
							System.out.println("Enter search query: ");
							String query = scan.nextLine();
							//String query = "what similarity laws must be obeyed when constructing aeroelastic models of heated high speed aircraft.";
							if(query.equals("q")) {
								return;
							}
							else if (query.equals("map")){
								int mapcount = 0;
								int maprelDocs = 0;
								double mapsumofrelDocs = 0;
								double mapsumofaveprecision = 0;
								for(int i = 0; i < queries.size(); i++) {
									topKDocs = r.RankedRetrieval(queries.get(i), new AdvancedTokenProcessor());
									listofRelDocs = map.get(queries.get(i));
									set = new HashSet<>(listofRelDocs);
									//System.out.println(i + "Hello world");
									mapsumofrelDocs = 0;
									maprelDocs = 0;
									mapcount = 0;
									for(RankedDocument rd : topKDocs) {
										mapcount++;
										if(set.contains(rd.getDocID() + 1)){
											maprelDocs++;
											mapsumofrelDocs += ((double) maprelDocs/ (double) mapcount);
										}
									}
									mapsumofaveprecision += ((double) mapsumofrelDocs / (double) listofRelDocs.size());
								}
								System.out.println("MAP: " + ((double)mapsumofaveprecision / (double)queries.size()));
							}
							else {
							listofRelDocs = map.get(query);
							int relDocs = 0;
							int count = 0;
							double sumofrelDocs = 0;
							topKDocs = r.RankedRetrieval(query, new AdvancedTokenProcessor());
							for(RankedDocument rd : topKDocs) {
								if(listofRelDocs == null) {
									System.out.println("DocID " + rd.getDocID() +": " + "(" + corpusR.getDocument(rd.getDocID()).getTitle() + ")"+ " -- " + rd.getAcc());
								}
								else {
									count++;
									if(listofRelDocs.contains(rd.getDocID() + 1)){
										System.out.println("Relevant at index " + count +": " +"DocID " + (rd.getDocID() + 1) +": " + "(" + corpusR.getDocument(rd.getDocID()).getTitle() + ")"+ " -- " + rd.getAcc());
										relDocs++;
										sumofrelDocs += ((double)relDocs/(double)count);
									}
								}
								//else {
								//	System.out.println("Not Rel - DocID " + (rd.getDocID() + 1) +": " + "(" + corpusR.getDocument(rd.getDocID()).getTitle() + ")"+ " -- " + rd.getAcc());
								//}
							}
								if(listofRelDocs != null) {
									System.out.println("Average Precision: " + ((double)sumofrelDocs/(double)listofRelDocs.size()));
								}
							}
						}
					}
			}
		}
	}
	private static ArrayList<ArrayList<Integer>> getRelevant(Path path){
		ArrayList<ArrayList<Integer>> relDocs = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> list;
		try {
			BufferedReader br = new BufferedReader(new FileReader(path.toString() + "/relevance/qrel.txt"));
			String line;
			while((line = br.readLine()) != null) {
				String [] strList = line.split(" ");
				list = new ArrayList<Integer>();
				for(int i = 0; i <strList.length ; i++) {
					list.add(Integer.parseInt(strList[i]));
				}
				relDocs.add(list);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return relDocs;
	}
	private static ArrayList<String> getQueries(Path path){
		ArrayList<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(path.toString() + "/relevance/queries.txt"));
			String line;
			while((line = br.readLine()) != null) {
				line = line.replace(" . ", ".");
				line = line.replace(" --", "");
				line = line.replace("  ", " ");
				//System.out.println(line);
				list.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	private static Index indexCorpus(DocumentCorpus corpus, Path path) {
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		ArrayList<String> wordList = new ArrayList<String>();
		int position = 0;
		
		for (Document doc : corpus.getDocuments()) {
			try {
				EnglishTokenStream e = new EnglishTokenStream(doc.getContent());
				for (String token : e.getTokens()) {
					wordList = processor.processToken(token);
					for (String s : wordList) {
						if (s.length() > 0) {
							index.addTerm(s, doc.getId(), position);
							position++;
						}
					}
				}
				e.close();
			} catch (FileNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			position = 0;
		}
		return index;
	}
}
