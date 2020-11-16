package com.Andrej.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Version;

public class Utils {
	public String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
    
    public ArrayList<ArrayList<String>> parseQuesrys(File file) throws Exception{
    	ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
    	
    	//Read in the queriescran
        FileInputStream fin = new FileInputStream(file);
        String all = convertStreamToString(fin);
        fin.close();
        
        String queryNumber = null;
        String text = null;
        
        ArrayList<String> queryNumbers = new ArrayList<String>();
        ArrayList<String> texts = new ArrayList<String>();
        
        int index = 0;
        int queryCount = 1;
        while(all != null && index < all.length()) {
        	index = all.indexOf(".I");
    		if (index+3 < all.indexOf(".W")) {
    			queryNumber = String.valueOf(queryCount);
    			queryNumbers.add(queryNumber);
    			queryCount += 1;
    		}
    		
    		index = all.indexOf(".W");
    		if(Integer.parseInt(queryNumbers.get(queryNumbers.size()-1)) < 225) {
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
    	
        results.add(queryNumbers); results.add(texts);
    	return results;
    }
    
    public ArrayList<String> tokeniseQuery(String query) throws IOException{
    	ArrayList<String> results = new ArrayList<String>();
    	StopList stopList = new StopList();
    	CharArraySet set = stopList.stopList;
    	Analyzer analyzer = new StandardAnalyzer(set);
    	analyzer.setVersion(Version.LUCENE_8_6_3);
    	
    	TokenStream stream = analyzer.tokenStream("query", query);
    	CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
    	
    	try {
    	      stream.reset();
    	    
    	      //print all tokens until stream is exhausted
    	      while (stream.incrementToken()) {
    	        results.add(termAtt.toString());
    	      }
    	      
    	      stream.end();
    	}finally {
    	      stream.close();
        }

    	analyzer.close();
    	return results;
    }
    
    public float[] makeQuery(ArrayList<String> queryTokens, IndexSearcher isearcher) throws IOException {
    	//Create a data-structure that accumulates the score for every document
    	float[] documentScoreCounter = new float[1400];        	
    	
    	//Make a seperate query for every word and combine the scores
    	//Search the text fields of the index first  	        	
    	for(int i = 0; i < queryTokens.size(); i++) {
    		Query term = new TermQuery(new Term("Text", queryTokens.get(i)));
    		BooleanQuery.Builder query = new BooleanQuery.Builder();
    		query.add(new BooleanClause(term, BooleanClause.Occur.SHOULD));
    		
    		ScoreDoc[] hits = isearcher.search(query.build(), 20).scoreDocs;        		
    		for (int j = 0; j < hits.length; j++){
            	Document hitDoc = isearcher.doc(hits[j].doc);
            	int docNum = Integer.parseInt(hitDoc.get("Document_number"));
            	documentScoreCounter[docNum-1] += hits[j].score;	               
            }
    	}
    	
    	return documentScoreCounter;
    }
    
    public float[] makeSingleQuery(ArrayList<String> queryTokens, IndexSearcher isearcher) throws IOException {
    	//Create a data-structure that accumulates the score for every document
    	float[] documentScoreCounter = new float[1400];
    	BooleanQuery.Builder query = new BooleanQuery.Builder();
    	
    	//Make a single Query for all the relevant terms in a query
    	//The score for the sinle query will be taken as the final score
    	for(int i = 0; i < queryTokens.size(); i++) {
    		Query term = new TermQuery(new Term("Text", queryTokens.get(i)));
    		query.add(new BooleanClause(term, BooleanClause.Occur.SHOULD));
    	}
    	
    	//A query has been built with all the query terms added
    	ScoreDoc[] hits = isearcher.search(query.build(), 20).scoreDocs;        		
		for (int j = 0; j < hits.length; j++){
        	Document hitDoc = isearcher.doc(hits[j].doc);
        	int docNum = Integer.parseInt(hitDoc.get("Document_number"));
        	documentScoreCounter[docNum-1] += hits[j].score;	               
        }
		
		return documentScoreCounter;
    }
    
    public ArrayList<DocumentScore> rankDocumnets(float[] documentScores) {
    	ArrayList<DocumentScore> docScores = new ArrayList<DocumentScore>();
    	        	
    	for(int i = 0; i < documentScores.length; i++) {
    		if(documentScores[i] > 0.1) {
    			DocumentScore score = new DocumentScore(i, documentScores[i]);
    			docScores.add(score);
    		}
    	}
    	
    	Collections.sort(docScores);
    	return docScores;
    }
    
    //We need term frequency and a means of representing the 
    /*private static void getCosineSimilarity(ArrayList<String> queryTokens, DirectoryReader ireader) throws IOException {
    	double sumMultiples = 0;
    }*/
    
    public float mapScore(float score, float start, float end) {
    	float result = 5;
    	
    	float inputStart = start;
    	float inputEnd = end;
    	float outputStart = 0;
    	float outputEnd = 1;
    	
    	if(score > inputEnd) {
    		result = (float) 0.99;
    		return result;
    	}
    	
    	result = (score - inputStart) / (inputEnd - inputStart) * (outputEnd - outputStart) + outputStart;
    	
    	return result;
    }

}
