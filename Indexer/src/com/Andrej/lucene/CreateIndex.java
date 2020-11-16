package com.Andrej.lucene;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
// import org.apache.lucene.store.RAMDirectory;

public class CreateIndex
{

    // Directory where the search index will be saved
    private static String INDEX_DIRECTORY = "../index";
    private static String CORPUS_FILE = "../corpus/cran.all.1400";
    
    public static String readFileAsString(String fileName) {
        String text = "";
        try {
          text = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
          e.printStackTrace();
        }

        return text;
    }
    
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
    
    //Split the Cranfield Collection into 1400 separate files    
    private static ArrayList<ArrayList<String>> parseDocuments(File file) throws Exception {
    	//Read in the corpus
        FileInputStream fin = new FileInputStream(file);
        String all = convertStreamToString(fin);
        fin.close();
        
    	String documentNumber = null;
    	String title = null;
    	String author = null;
    	String journal = null;
    	String text = null;
    	
    	//Create fields for all of the doc nums, titles, journals and text fields via parsing
    	ArrayList<String> documentNumbers = new ArrayList<String>();
    	ArrayList<String> titles = new ArrayList<String>();
    	ArrayList<String> authors = new ArrayList<String>();
    	ArrayList<String> journals = new ArrayList<String>();
    	ArrayList<String> texts = new ArrayList<String>();
    	
    	int index = 0;
    	while(all != null && index < all.length()) {
    		//The -1 is to remove '\n'
    		//The +3 is to get past ".I\n" etc...
    		index = all.indexOf(".I");
    		if (index+3 < all.indexOf(".T")) {
    			documentNumber = all.substring(index+3, all.indexOf(".T")-1);
    			documentNumbers.add(documentNumber);
    		}
    		else {
    			documentNumbers.add("");
    		}
    		
    		index = all.indexOf(".T");
    		if (index+3 < all.indexOf(".A")) {
    			title = all.substring(index + 3, all.indexOf(".A")-1);
        		titles.add(title);
    		}
    		else {
    			titles.add("");
    		}
    		
    		index = all.indexOf(".A");
    		if (index+3 < all.indexOf(".B")) {
    			author = all.substring(index + 3, all.indexOf(".B")-1);
    			authors.add(author);
    		}
    		else {
    			authors.add("");
    		}
    		
    		index = all.indexOf(".B");
    		if (index+3 < all.indexOf(".W")) {
    			journal = all.substring(index+3, all.indexOf(".W")-1);
        		journals.add(journal);
    		}
    		else {
    			journals.add("");
    		}
  
    		index = all.indexOf(".W");
    		if(Integer.parseInt(documentNumbers.get(documentNumbers.size()-1)) < 1400) {
    			all = all.substring(index, all.length());
    			index = all.indexOf(".W");
    			if (index+3 < all.indexOf(".I")) {
    				text = all.substring(index+3, all.indexOf(".I")-1);
    				texts.add(text);
    			}
    			else {
    				texts.add("");
    			}
    			all = all.substring(all.indexOf(".I"), all.length());
    		} 
    		else {
    			text = all.substring(index+3, all.length()-1);
    			texts.add(text);
    			all = null;
    			
    			break;
    		}
    	}
    	
    	
    	//Return the results in a 2D vector
    	ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
    	results.add(documentNumbers); results.add(titles); results.add(authors); results.add(journals); results.add(texts);
    	
    	return results;
    }

    public static void main(String[] args) throws Exception
    {   
    	// Analyzer that is used to process TextField
		Analyzer analyzer = new StandardAnalyzer();
    	
        //Organise and parse the documents
        File file = new File(CORPUS_FILE);
        if (!file.exists()){
        	System.out.print("File Not Found: 404\n");
        }
        ArrayList<ArrayList<String>> fields = parseDocuments(file);

        // To store an index in memory
        // Directory directory = new RAMDirectory();
        // To store an index on disk        
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));      
        
        // Set up an index writer to add process and save documents to the index
 		IndexWriterConfig config = new IndexWriterConfig(analyzer);
 		config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
 		IndexWriter iwriter = new IndexWriter(directory, config);
        
 		ArrayList<Document> documnets = new ArrayList<Document>(); 

        // Create a new document
        Document doc = new Document();
        
        //FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
        //myFieldType.setStoreTermVectors(true);
        
        for (int i = 0; i < 1400; i++) {
        	for (int j = 0; j < fields.size(); j++) {
        		//Use switch to get the parsed data under the right heading
        		switch(j) {
	        		case 0:
				        doc.add(new TextField("Document_number", fields.get(j).get(i), Field.Store.YES));
				        System.out.print("Indexing Document: ");
	        			System.out.print(fields.get(j).get(i));
	        			System.out.print("\n");
				        break;
	        		case 1:
	        			doc.add(new TextField("Title", fields.get(j).get(i), Field.Store.YES));	        			
	        			break;
	        		case 2:
	        			doc.add(new TextField("Author", fields.get(j).get(i), Field.Store.YES));
	        			break;
	        		case 3:
	        			doc.add(new TextField("Journal", fields.get(j).get(i), Field.Store.YES));
	        			break;
	        		case 4:
	        			doc.add(new TextField("Text", fields.get(j).get(i), Field.Store.YES));
	        			break;
        		}
        	}
        	// Save the document to the index
    		documnets.add(doc);
    		doc = new Document();
        }
        
        iwriter.addDocuments(documnets);

        // Commit changes and close everything
        iwriter.close();
        directory.close();
        System.out.print("Job done");
    }
}

