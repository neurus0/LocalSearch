package com.localsearch.core;

import com.localsearch.index.InvertedIndex;
import com.localsearch.model.Document;
import com.localsearch.model.IndexEntry;
import com.localsearch.model.SearchQuery;
import com.localsearch.model.SearchResult;
import com.localsearch.util.TextUtils;

import java.io.File;
import java.util.*;

/**
 * Academic TF-IDF inspired ranking engine with term coverage, phrase boost, and title matches.
 */
public class Ranker {

    private final InvertedIndex invertedIndex;

    public Ranker(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    /**
     * Ranks candidate documents for a given search query and builds SearchResult list.
     */
    public List<SearchResult> rank(SearchQuery query, Set<String> candidateDocIds, Map<String, String> documentContents, int maxSnippetLength) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || candidateDocIds == null || candidateDocIds.isEmpty()) {
            return results;
        }

        int totalDocs = invertedIndex.getDocumentCount();
        if (totalDocs == 0) return results;

        double avgDocLen = Math.max(1.0, invertedIndex.getAverageDocumentLength());
        List<String> queryTerms = query.getTerms();
        if (queryTerms.isEmpty() && query.getPhrase().isEmpty()) {
            return results;
        }

        Map<String, Double> rawScores = new HashMap<>();
        Map<String, Integer> matchCounts = new HashMap<>();
        Map<String, Set<String>> matchedTermsMap = new HashMap<>();

        double maxScore = 0.0;

        for (String docId : candidateDocIds) {
            Document doc = invertedIndex.getDocument(docId);
            if (doc == null) continue;

            int docLength = Math.max(1, invertedIndex.getDocumentLength(docId));
            double lengthNorm = 0.75 + 0.25 * ((double) docLength / avgDocLen);

            double docScore = 0.0;
            int totalMatches = 0;
            Set<String> matchedTerms = new HashSet<>();

            // 1. TF-IDF for individual query terms
            for (String term : queryTerms) {
                IndexEntry entry = invertedIndex.getEntry(term);
                if (entry != null && entry.containsDocument(docId)) {
                    int tf = entry.getTermFrequency(docId);
                    totalMatches += tf;
                    matchedTerms.add(term);

                    int df = entry.getDocumentFrequency();
                    // Smoothed IDF
                    double idf = Math.log(1.0 + (double) (totalDocs - df + 0.5) / (df + 0.5));
                    // Sublinear TF
                    double tfWeight = 1.0 + Math.log(tf);

                    docScore += (tfWeight * idf) / lengthNorm;
                }
            }

            // 2. Exact Phrase Match Boost
            if (query.isPhrase() || (!query.getPhrase().isEmpty() && queryTerms.size() > 1)) {
                int phraseOccurrences = invertedIndex.countExactPhraseOccurrences(docId, queryTerms);
                if (phraseOccurrences > 0) {
                    docScore += phraseOccurrences * 4.0;
                    totalMatches += phraseOccurrences;
                }
            }

            // 3. File Name Match Bonus
            String lowerFileName = doc.getFileName().toLowerCase(Locale.ROOT);
            for (String term : queryTerms) {
                if (lowerFileName.contains(term.toLowerCase(Locale.ROOT))) {
                    docScore += 3.5; // Strong boost for matching in file name
                }
            }

            // 4. Query Term Coverage Bonus
            if (!queryTerms.isEmpty()) {
                double coverage = (double) matchedTerms.size() / queryTerms.size();
                docScore *= (0.7 + 0.6 * coverage);
            }

            if (docScore > 0) {
                rawScores.put(docId, docScore);
                matchCounts.put(docId, totalMatches);
                matchedTermsMap.put(docId, matchedTerms);
                if (docScore > maxScore) {
                    maxScore = docScore;
                }
            }
        }

        // Generate normalized percentages and SearchResult items
        for (Map.Entry<String, Double> entry : rawScores.entrySet()) {
            String docId = entry.getKey();
            double score = entry.getValue();
            Document doc = invertedIndex.getDocument(docId);
            if (doc == null) continue;

            int percentage;
            if (maxScore > 0) {
                // Scale so that highest score is between 85% and 98%, others scaled proportionately
                double normalized = score / maxScore;
                percentage = (int) Math.round(30 + normalized * 65);
                percentage = Math.min(99, Math.max(15, percentage));
            } else {
                percentage = 50;
            }

            String content = documentContents != null ? documentContents.get(docId) : null;
            String snippet = TextUtils.generateSnippet(content, new HashSet<>(queryTerms), query.getPhrase(), maxSnippetLength);

            results.add(new SearchResult(
                    doc,
                    score,
                    percentage,
                    matchCounts.getOrDefault(docId, 1),
                    snippet,
                    matchedTermsMap.getOrDefault(docId, Collections.emptySet())
            ));
        }

        Collections.sort(results);
        return results;
    }
}
