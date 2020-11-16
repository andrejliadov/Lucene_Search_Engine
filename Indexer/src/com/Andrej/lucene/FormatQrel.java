package com.Andrej.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FormatQrel {
	
	private static String QRELS_FILE = "../corpus/cranqrel";
	private static String QRELSFORMATTED_FILE = "../corpus/formattedQrels.txt";
	
	public static char mapRelScore(char rel) {
		int score = Integer.parseInt(String.valueOf(rel));
		char result = '9';
		
		if(score <= 3) {
			result = '1';
		}
		else {
			result = '0';
		}
		
		return result;
	}
	
	public static String insertString(String originalString, String stringToBeInserted, int index) { 
	  
	    // Create a new StringBuffer 
	        StringBuffer newString = new StringBuffer(originalString); 
	  
	        // Insert the strings to be inserted 
	    // using insert() method 
	        newString.insert(index + 1, stringToBeInserted); 
	  
	        // return the modified String 
	    return newString.toString(); 
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
	
	private static void FormatDocuments(File input, File output) throws Exception {
    	//Read in the corpus
        FileInputStream fin = new FileInputStream(input);
        String all = convertStreamToString(fin);
        fin.close();
        
        FileWriter writer = new FileWriter(output);
        
        char result = '1';
        String[] lines = all.split(System.getProperty("line.separator"));
        for(int i = 0; i < lines.length; i++) {
        	lines[i] = insertString(lines[i], "0 ", lines[i].indexOf(' '));
        	StringBuilder stringBuilder = new StringBuilder(lines[i]);
        	result = lines[i].charAt(lines[i].length()-2);
        	if(result != '-') {
        		stringBuilder.setCharAt(lines[i].length()-2, mapRelScore(result));
        	}
        	else {
        		stringBuilder.deleteCharAt(lines[i].length()-2);
        	}
        	System.out.print(stringBuilder.toString() + "\n");
        	
        	writer.write(stringBuilder.toString() + "\n");
        }
        
        writer.close();
	}

	public static void main(String[] args) throws Exception {
		File input = new File(QRELS_FILE);
		File output = new File(QRELSFORMATTED_FILE);
		
		FormatDocuments(input, output);
	}

}
