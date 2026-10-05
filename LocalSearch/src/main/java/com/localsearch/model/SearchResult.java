package com.localsearch.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a ranked search result item.
 */
public class SearchResult implements Comparable<SearchResult> {
    private final Document document;
    private final double relevanceScore;
    private int scorePercentage;
    private final int matchCount;
    private final String snippet;
    private final Set<String> matchedTerms;

    public SearchResult(Document document, double relevanceScore, int scorePercentage,
                        int matchCount, String snippet, Set<String> matchedTerms) {
        this.document = document;
        this.relevanceScore = relevanceScore;
        this.scorePercentage = Math.max(0, Math.min(100, scorePercentage));
        this.matchCount = matchCount;
        this.snippet = snippet;
        this.matchedTerms = matchedTerms != null ? new HashSet<>(matchedTerms) : new HashSet<>();
    }

    public Document getDocument() {
        return document;
    }

    public double getRelevanceScore() {
        return relevanceScore;
    }

    public int getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(int scorePercentage) {
        this.scorePercentage = Math.max(0, Math.min(100, scorePercentage));
    }

    public int getMatchCount() {
        return matchCount;
    }

    public String getSnippet() {
        return snippet;
    }

    public Set<String> getMatchedTerms() {
        return Collections.unmodifiableSet(matchedTerms);
    }

    @Override
    public int compareTo(SearchResult other) {
        // Descending relevance score
        int scoreComp = Double.compare(other.relevanceScore, this.relevanceScore);
        if (scoreComp != 0) {
            return scoreComp;
        }
        // Descending match count fallback
        int matchComp = Integer.compare(other.matchCount, this.matchCount);
        if (matchComp != 0) {
            return matchComp;
        }
        // Ascending file name
        return this.document.getFileName().compareToIgnoreCase(other.document.getFileName());
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "file=" + document.getFileName() +
                ", score=" + relevanceScore +
                ", percentage=" + scorePercentage + "%" +
                ", matches=" + matchCount +
                '}';
    }
}
