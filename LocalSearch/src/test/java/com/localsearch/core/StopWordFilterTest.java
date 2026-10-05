package com.localsearch.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StopWordFilterTest {

    private StopWordFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new StopWordFilter();
    }

    @Test
    public void testIsStopWord() {
        assertTrue(filter.isStopWord("the"));
        assertTrue(filter.isStopWord("is"));
        assertTrue(filter.isStopWord("and"));
        assertTrue(filter.isStopWord("with"));

        assertFalse(filter.isStopWord("packet"));
        assertFalse(filter.isStopWord("routing"));
        assertFalse(filter.isStopWord("java"));
    }

    @Test
    public void testFilterTokens() {
        List<String> tokens = Arrays.asList("the", "packet", "is", "routing", "to", "the", "network");
        List<String> filtered = filter.filter(tokens);

        assertEquals(3, filtered.size());
        assertEquals("packet", filtered.get(0));
        assertEquals("routing", filtered.get(1));
        assertEquals("network", filtered.get(2));
    }

    @Test
    public void testAllStopWords() {
        List<String> allStops = Arrays.asList("to", "be", "or", "not", "to", "be");
        assertTrue(filter.allStopWords(allStops));

        List<String> mixed = Arrays.asList("to", "be", "routing");
        assertFalse(filter.allStopWords(mixed));
    }
}
