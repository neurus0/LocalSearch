package com.localsearch.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a parsed search query with its execution mode and terms.
 */
public class SearchQuery {
    public enum QueryType {
        KEYWORD,
        AND,
        OR,
        PHRASE
    }

    private final String rawQuery;
    private final QueryType queryType;
    private final List<String> terms;
    private final String phrase;

    public SearchQuery(String rawQuery, QueryType queryType, List<String> terms, String phrase) {
        this.rawQuery = rawQuery;
        this.queryType = queryType;
        this.terms = terms != null ? new ArrayList<>(terms) : new ArrayList<>();
        this.phrase = phrase != null ? phrase.trim() : "";
    }

    public static SearchQuery keyword(String rawQuery, List<String> terms) {
        return new SearchQuery(rawQuery, QueryType.KEYWORD, terms, null);
    }

    public static SearchQuery and(String rawQuery, List<String> terms) {
        return new SearchQuery(rawQuery, QueryType.AND, terms, null);
    }

    public static SearchQuery or(String rawQuery, List<String> terms) {
        return new SearchQuery(rawQuery, QueryType.OR, terms, null);
    }

    public static SearchQuery phrase(String rawQuery, List<String> phraseTerms, String phrase) {
        return new SearchQuery(rawQuery, QueryType.PHRASE, phraseTerms, phrase);
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public List<String> getTerms() {
        return Collections.unmodifiableList(terms);
    }

    public String getPhrase() {
        return phrase;
    }

    public boolean isAnd() {
        return queryType == QueryType.AND;
    }

    public boolean isOr() {
        return queryType == QueryType.OR;
    }

    public boolean isPhrase() {
        return queryType == QueryType.PHRASE;
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "type=" + queryType +
                ", terms=" + terms +
                (phrase.isEmpty() ? "" : ", phrase='" + phrase + '\'') +
                '}';
    }
}
