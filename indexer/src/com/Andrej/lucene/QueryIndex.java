package com.Andrej.lucene;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class QueryIndex
{

        // the location of the search index
		private static String INDEX_DIRECTORY = "../index";
        private static String QUERY_FILE = "../corpus/cran.qry";
        private static String RESULTS_FILE = "../results/SimpleQueryStandardScoring.txt";
        private static boolean appendToFile = false;

        // Limit the number of search results we get
        private static int MAX_RESULTS = 20;
        
        public static void main(String[] args) throws Exception{
        	//Organise and parse the documents
        	Utils utils = new Utils();
            File file = new File(QUERY_FILE);
            if (!file.exists()){
            	System.out.print("File Not Found: 404\n");
            }
            ArrayList<ArrayList<String>> querys = utils.parseQuesrys(file);
        	
            // Open the folder that contains our search index
            Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

            // create objects to read and search across the index
            DirectoryReader ireader = DirectoryReader.open(directory);
            IndexSearcher isearcher = new IndexSearcher(ireader);
            
            //Setup a means of storing results
            File outputFile = new File(RESULTS_FILE);
            FileWriter writer = new FileWriter(outputFile, appendToFile);
            
            float[] resultArray = new float[1400];
            ArrayList<String> queryTokens = null;
            ArrayList<DocumentScore> scores = null;
            
            for(int i = 0; i < querys.get(1).size(); i++) {
            	queryTokens = utils.tokeniseQuery(querys.get(1).get(i));
            	//getCosineSimilarity(queryTokens, ireader);
            	//resultArray = utils.makeQuery(queryTokens, isearcher);
            	resultArray = utils.makeSingleQuery(queryTokens, isearcher);
            	scores = utils.rankDocumnets(resultArray);
            	
            	for(int j = 0; j < MAX_RESULTS; j++) {
            		if (j >= scores.size()) {
            			break;
            		}
            		
            		if (scores.get(j).score > 3) {
            			writer.write(String.valueOf(i + 1) + " Q0 " + (scores.get(j).documentNumber + 1) + " " + (j+1) + " " + utils.mapScore(scores.get(j).score, 3, 13) + " SimpleQueryDefaultScoring\n");
            		}
            	}
            }            

            // close everything we used
            ireader.close();
            directory.close();
            writer.close();
        }
}
      