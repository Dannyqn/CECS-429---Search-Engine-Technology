package edu.csulb;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;

import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

public class PositionalInvertedIndexer {
	public static void main(String[] args) throws Exception {
		Scanner scan = new Scanner(System.in);
		System.out.println("What is the path of the directory you would like to index: ");
		String s = scan.nextLine();
		System.out.println(Paths.get(s).toAbsolutePath());
		DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(s).toAbsolutePath());
		long startTime = System.nanoTime();
		Index index = indexCorpus(corpus);
		long endTime = System.nanoTime();
		long totalTime = endTime - startTime;
		System.out.println("Corpus indexed in: " + totalTime / 1000000000 + " seconds");
		// We aren't ready to use a full query parser; for now, we'll only support
		// single-term queries.
		while (true) {
			System.out.println("Enter search query: ");
			String query = scan.next(); // Asks user for term to search
			switch (query) {
			case "q":
				System.out.println("Program exited...");
				scan.close();
				return;
			case "stem":
				AdvancedTokenProcessor processor = new AdvancedTokenProcessor(); 
				System.out.println(processor.processToken(scan.next()));
				break;
			case "vocab":
				List<String> vList = index.getVocabulary();
				if(vList.size() >= 1000) {
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
				corpus = DirectoryCorpus.loadDirectory(Paths.get(scan.next()).toAbsolutePath());
				startTime = System.nanoTime();
				index = indexCorpus(corpus);
				endTime = System.nanoTime();
				totalTime = endTime - startTime;
				System.out.println("Corpus indexed in: " + totalTime / 1000000000 + " seconds");
				break;
			default:
				query += scan.nextLine();
				BooleanQueryParser b = new BooleanQueryParser();
				int docCount = 0;
				for (Posting p : b.parseQuery(query).getPostings(index)) {	
					System.out.println(p.getDocumentId() + ". " + corpus.getDocument(p.getDocumentId()).getTitle());
					docCount++;
					System.out.println(p.getPositions());
				}
				System.out.println("Number of Documents: " + docCount);

				// getting contents of a document selected by the user
				System.out.println("Would you like to view the contents of a Document? (Enter Doc ID) -1 for no");
				int docID = scan.nextInt();
				if(docID > 0 ) {
					BufferedReader bufferedReader = new BufferedReader(corpus.getDocument(docID).getContent());
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
	}

	private static Index indexCorpus(DocumentCorpus corpus) {
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		AdvancedTokenProcessor processor = new AdvancedTokenProcessor();
		ArrayList<String> wordList = new ArrayList<String>();
		int position = 0;

		for (Document doc : corpus.getDocuments()) {
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
			try {
				e.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			position = 0;
		}
		return index;
	}
}
