package com.localsearch.core;

import java.util.*;

/**
 * Filters common English stop words while preserving technical keywords.
 */
public class StopWordFilter {

    private static final Set<String> DEFAULT_STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
            "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being",
            "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't",
            "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during",
            "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't",
            "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her", "here",
            "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
            "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it",
            "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or",
            "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
            "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so",
            "some", "such", "than", "that", "that's", "the", "their", "theirs", "them",
            "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll",
            "they're", "they've", "this", "those", "through", "to", "too", "under", "until",
            "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've",
            "were", "weren't", "what", "what's", "when", "when's", "where", "where's",
            "which", "while", "who", "who's", "whom", "why", "why's", "with", "won't",
            "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your",
            "yours", "yourself", "yourselves"
    ));

    private final Set<String> stopWords;

    public StopWordFilter() {
        this.stopWords = new HashSet<>(DEFAULT_STOP_WORDS);
    }

    public StopWordFilter(Set<String> customStopWords) {
        this.stopWords = (customStopWords != null) ? new HashSet<>(customStopWords) : new HashSet<>(DEFAULT_STOP_WORDS);
    }

    /**
     * Checks if a word is in the stop word list.
     */
    public boolean isStopWord(String word) {
        if (word == null) return false;
        return stopWords.contains(word.toLowerCase(Locale.ROOT).trim());
    }

    /**
     * Filters a list of tokens by removing stop words.
     */
    public List<String> filter(List<String> tokens) {
        List<String> filtered = new ArrayList<>();
        if (tokens == null) return filtered;

        for (String token : tokens) {
            if (!isStopWord(token)) {
                filtered.add(token);
            }
        }
        return filtered;
    }

    /**
     * Checks if all tokens in a query are stop words.
     */
    public boolean allStopWords(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) return false;
        for (String t : tokens) {
            if (!isStopWord(t)) return false;
        }
        return true;
    }

    public Set<String> getStopWords() {
        return Collections.unmodifiableSet(stopWords);
    }
}
