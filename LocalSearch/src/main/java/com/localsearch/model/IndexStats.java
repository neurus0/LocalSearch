package com.localsearch.model;

import java.io.Serializable;

/**
 * Encapsulates statistics about the current inverted index.
 */
public class IndexStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int documentCount;
    private final long totalWords;
    private final int uniqueTerms;
    private final long totalPostings;
    private final long lastIndexTime;
    private final long indexSizeBytes;

    public IndexStats(int documentCount, long totalWords, int uniqueTerms,
                      long totalPostings, long lastIndexTime, long indexSizeBytes) {
        this.documentCount = documentCount;
        this.totalWords = totalWords;
        this.uniqueTerms = uniqueTerms;
        this.totalPostings = totalPostings;
        this.lastIndexTime = lastIndexTime;
        this.indexSizeBytes = indexSizeBytes;
    }

    public int getDocumentCount() {
        return documentCount;
    }

    public long getTotalWords() {
        return totalWords;
    }

    public int getUniqueTerms() {
        return uniqueTerms;
    }

    public long getTotalPostings() {
        return totalPostings;
    }

    public long getLastIndexTime() {
        return lastIndexTime;
    }

    public long getIndexSizeBytes() {
        return indexSizeBytes;
    }

    @Override
    public String toString() {
        return "IndexStats{" +
                "docs=" + documentCount +
                ", words=" + totalWords +
                ", uniqueTerms=" + uniqueTerms +
                ", postings=" + totalPostings +
                '}';
    }
}
