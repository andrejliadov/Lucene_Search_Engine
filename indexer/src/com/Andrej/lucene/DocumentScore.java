package com.Andrej.lucene;

public class DocumentScore implements Comparable<DocumentScore>{
	public int documentNumber;
	public float score;
	
	DocumentScore(int num, float s){
		documentNumber = num;
		score = s;
	}

	@Override
	public int compareTo(DocumentScore doc) {		
		return Float.compare(doc.score, this.score);
	}
}
