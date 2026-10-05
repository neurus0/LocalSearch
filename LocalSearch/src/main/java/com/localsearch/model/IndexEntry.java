package com.localsearch.model;

import java.io.Serializable;
import java.util.*;

/**
 * Represents postings and occurrences for a specific term in the inverted index.
 */
public class IndexEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Posting holds term occurrence details inside a specific document.
     */
    public static class Posting implements Serializable {
        private static final long serialVersionUID = 1L;

        private int termFrequency;
        private final List<Integer> positions;

        public Posting() {
            this.termFrequency = 0;
            this.positions = new ArrayList<>();
        }

        public void addOccurrence(int position) {
            this.termFrequency++;
            this.positions.add(position);
        }

        public int getTermFrequency() {
            return termFrequency;
        }

        public List<Integer> getPositions() {
            return Collections.unmodifiableList(positions);
        }
    }

    private final String term;
    // Map of documentId -> Posting
    private final Map<String, Posting> postings;

    public IndexEntry(String term) {
        this.term = term;
        this.postings = new HashMap<>();
    }

    public String getTerm() {
        return term;
    }

    public void addOccurrence(String docId, int position) {
        postings.computeIfAbsent(docId, k -> new Posting()).addOccurrence(position);
    }

    public void removeDocument(String docId) {
        postings.remove(docId);
    }

    public Map<String, Posting> getPostings() {
        return Collections.unmodifiableMap(postings);
    }

    public int getDocumentFrequency() {
        return postings.size();
    }

    public boolean containsDocument(String docId) {
        return postings.containsKey(docId);
    }

    public Posting getPosting(String docId) {
        return postings.get(docId);
    }

    public int getTermFrequency(String docId) {
        Posting p = postings.get(docId);
        return p != null ? p.getTermFrequency() : 0;
    }
}
