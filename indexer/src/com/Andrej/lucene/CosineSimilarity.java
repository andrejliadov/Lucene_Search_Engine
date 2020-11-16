package com.Andrej.lucene;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class CosineSimilarity extends SimilarityBase{
	
	@Override
	protected double score(BasicStats stats, double termFreq, double docLength) {
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
