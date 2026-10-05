package com.localsearch.core;

import com.localsearch.model.SearchQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryProcessorTest {

    private QueryProcessor queryProcessor;

    @BeforeEach
    public void setUp() {
        Tokenizer tokenizer = new Tokenizer();
        StopWordFilter stopWordFilter = new StopWordFilter();
        queryProcessor = new QueryProcessor(tokenizer, stopWordFilter);
    }

    @Test
    public void testKeywordQuery() {
        SearchQuery query = queryProcessor.parse("packet routing", true);
        assertEquals(SearchQuery.QueryType.KEYWORD, query.getQueryType());
        assertTrue(query.getTerms().contains("packet"));
        assertTrue(query.getTerms().contains("routing"));
    }

    @Test
    public void testAndQuery() {
        SearchQuery query = queryProcessor.parse("packet AND routing", true);
        assertTrue(query.isAnd());
        assertEquals(2, query.getTerms().size());
        assertTrue(query.getTerms().contains("packet"));
        assertTrue(query.getTerms().contains("routing"));
    }

    @Test
    public void testOrQuery() {
        SearchQuery query = queryProcessor.parse("packet OR routing", true);
        assertTrue(query.isOr());
        assertEquals(2, query.getTerms().size());
        assertTrue(query.getTerms().contains("packet"));
        assertTrue(query.getTerms().contains("routing"));
    }

    @Test
    public void testPhraseQuery() {
        SearchQuery query = queryProcessor.parse("\"packet switching\"", true);
        assertTrue(query.isPhrase());
        assertEquals("packet switching", query.getPhrase());
        assertEquals(2, query.getTerms().size());
        assertEquals("packet", query.getTerms().get(0));
        assertEquals("switching", query.getTerms().get(1));
    }

    @Test
    public void testSingleKeyword() {
        SearchQuery query = queryProcessor.parse("routing", true);
        assertEquals(SearchQuery.QueryType.KEYWORD, query.getQueryType());
        assertEquals(1, query.getTerms().size());
        assertEquals("routing", query.getTerms().get(0));
    }

    @Test
    public void testEmptyAndMalformed() {
        SearchQuery q1 = queryProcessor.parse("", true);
        assertTrue(q1.getTerms().isEmpty());

        SearchQuery q2 = queryProcessor.parse("   ", true);
        assertTrue(q2.getTerms().isEmpty());

        SearchQuery q3 = queryProcessor.parse("\"unclosed phrase", true);
        assertNotNull(q3);
    }
}
