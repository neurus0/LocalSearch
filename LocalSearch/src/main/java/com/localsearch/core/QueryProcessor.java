package com.localsearch.core;

import com.localsearch.model.SearchQuery;
import java.util.*;

/**
 * Parses raw search strings into structured SearchQuery objects.
 * Supports:
 * - Single keyword: routing
 * - Multi-keyword: packet routing
 * - Boolean AND: packet AND routing
 * - Boolean OR: packet OR routing
 * - Phrase search: "packet switching"
 */
public class QueryProcessor {

    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;

    public QueryProcessor(Tokenizer tokenizer, StopWordFilter stopWordFilter) {
        this.tokenizer = tokenizer != null ? tokenizer : new Tokenizer();
        this.stopWordFilter = stopWordFilter != null ? stopWordFilter : new StopWordFilter();
    }

    /**
     * Parses the user query string into a SearchQuery.
     *
     * @param rawQuery The user input query string
     * @param filterStopWords Whether to filter stop words
     * @return Processed SearchQuery
     */
    public SearchQuery parse(String rawQuery, boolean filterStopWords) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return new SearchQuery("", SearchQuery.QueryType.KEYWORD, Collections.emptyList(), "");
        }

        String trimmed = rawQuery.trim();

        // 1. Check for Quoted Phrase Search: "packet switching"
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 2) {
            String phraseContent = trimmed.substring(1, trimmed.length() - 1).trim();
            List<String> phraseTokens = tokenizer.tokenize(phraseContent);
            if (!phraseTokens.isEmpty()) {
                return SearchQuery.phrase(trimmed, phraseTokens, phraseContent);
            }
        } else if (trimmed.contains("\"")) {
            // Embedded phrase query or unclosed quote
            int firstQuote = trimmed.indexOf('\"');
            int secondQuote = trimmed.indexOf('\"', firstQuote + 1);
            if (secondQuote > firstQuote + 1) {
                String phraseContent = trimmed.substring(firstQuote + 1, secondQuote).trim();
                List<String> phraseTokens = tokenizer.tokenize(phraseContent);
                if (!phraseTokens.isEmpty()) {
                    return SearchQuery.phrase(trimmed, phraseTokens, phraseContent);
                }
            }
        }

        // 2. Check for Explicit Boolean AND (case-insensitive " AND " or " + ")
        if (trimmed.contains(" AND ") || trimmed.contains(" and ") || trimmed.contains(" + ")) {
            String clean = trimmed.replaceAll("(?i)\\bAND\\b", " ").replace("+", " ");
            List<String> tokens = processTokens(clean, filterStopWords);
            if (!tokens.isEmpty()) {
                return SearchQuery.and(trimmed, tokens);
            }
        }

        // 3. Check for Explicit Boolean OR (case-insensitive " OR " or " | ")
        if (trimmed.contains(" OR ") || trimmed.contains(" or ") || trimmed.contains(" | ")) {
            String clean = trimmed.replaceAll("(?i)\\bOR\\b", " ").replace("|", " ");
            List<String> tokens = processTokens(clean, filterStopWords);
            if (!tokens.isEmpty()) {
                return SearchQuery.or(trimmed, tokens);
            }
        }

        // 4. Default: Standard keyword search
        List<String> tokens = processTokens(trimmed, filterStopWords);
        return SearchQuery.keyword(trimmed, tokens);
    }

    private List<String> processTokens(String text, boolean filterStopWords) {
        List<String> rawTokens = tokenizer.tokenize(text);
        if (!filterStopWords) {
            return rawTokens;
        }

        List<String> filtered = stopWordFilter.filter(rawTokens);
        // If all terms were stop words (e.g. searching "to be or not to be"), don't discard everything
        if (filtered.isEmpty() && !rawTokens.isEmpty()) {
            return rawTokens;
        }
        return filtered;
    }
}
