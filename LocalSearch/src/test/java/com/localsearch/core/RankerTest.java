package com.localsearch.core;

import com.localsearch.index.InvertedIndex;
import com.localsearch.model.Document;
import com.localsearch.model.SearchQuery;
import com.localsearch.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RankerTest {

    private InvertedIndex index;
    private Tokenizer tokenizer;
    private Ranker ranker;

    @BeforeEach
    public void setUp() {
        index = new InvertedIndex();
        tokenizer = new Tokenizer();
        ranker = new Ranker(index);
    }

    @Test
    public void testRelevanceRanking() {
        // Doc 1 has high frequency of "packet" and "routing"
        Document doc1 = new Document("d1", "Computer_Networks.txt", "/Computer_Networks.txt", "txt", 1000, 1000L, 50, 1000L, "h1");
        index.addDocument(doc1, tokenizer.tokenizeWithPositions("packet routing packet routing packet switching protocols"));

        // Doc 2 has low frequency
        Document doc2 = new Document("d2", "Other.txt", "/Other.txt", "txt", 1000, 1000L, 50, 1000L, "h2");
        index.addDocument(doc2, tokenizer.tokenizeWithPositions("introduction to packet headers"));

        SearchQuery query = SearchQuery.keyword("packet routing", Arrays.asList("packet", "routing"));
        Set<String> candidates = new HashSet<>(Arrays.asList("d1", "d2"));
        Map<String, String> contents = new HashMap<>();
        contents.put("d1", "packet routing packet routing packet switching protocols");
        contents.put("d2", "introduction to packet headers");

        List<SearchResult> results = ranker.rank(query, candidates, contents, 100);

        assertEquals(2, results.size());
        assertEquals("d1", results.get(0).getDocument().getId());
        assertTrue(results.get(0).getScorePercentage() > results.get(1).getScorePercentage());
    }
}
