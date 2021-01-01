package cecs429.documents;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import com.google.gson.*;

public class JsonFileDocument implements FileDocument{
	private int jDocumentId;
	private Path jFilePath;
	private String jTitle;
	
	public JsonFileDocument(int id, Path absoluteFilePath) {
		jDocumentId = id;
		jFilePath = absoluteFilePath;
	}
	public int getId() {
		return jDocumentId;
	}

	@Override
	public Reader getContent() {
		Reader reader;
		Reader Sreader;
		try {
			reader = new FileReader(jFilePath.toString());
			JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
			Sreader = new StringReader(jsonObject.get("body").getAsString());
			return Sreader;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getTitle() {
		Reader reader;
		try {
			reader = new FileReader(jFilePath.toString());
			JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
			jTitle = jsonObject.get("title").getAsString();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jTitle;
	}

	@Override
	public Path getFilePath() {

		return jFilePath;
	}
	public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
	
}
