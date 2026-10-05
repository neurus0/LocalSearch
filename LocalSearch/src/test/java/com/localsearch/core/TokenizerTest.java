package com.localsearch.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenizerTest {

    private Tokenizer tokenizer;

    @BeforeEach
    public void setUp() {
        tokenizer = new Tokenizer();
    }

    @Test
    public void testBasicTokenization() {
        String text = "Packet-routing protocols are important.";
        List<String> tokens = tokenizer.tokenize(text);

        assertTrue(tokens.contains("packet-routing") || (tokens.contains("packet") && tokens.contains("routing")));
        assertTrue(tokens.contains("protocols"));
        assertTrue(tokens.contains("important"));
    }

    @Test
    public void testTechnicalTermsPreserved() {
        String text = "We tested TCP/IP, HTTP, JavaFX, HashMap, and JSON APIs with C++ and .NET on node.js.";
        List<String> tokens = tokenizer.tokenize(text);

        assertTrue(tokens.contains("tcp/ip"));
        assertTrue(tokens.contains("http"));
        assertTrue(tokens.contains("javafx"));
        assertTrue(tokens.contains("hashmap"));
        assertTrue(tokens.contains("json"));
        assertTrue(tokens.contains("c++"));
        assertTrue(tokens.contains(".net"));
        assertTrue(tokens.contains("node.js"));
    }

    @Test
    public void testPositionsTracking() {
        String text = "packet switching routing";
        List<Tokenizer.TokenPosition> tokens = tokenizer.tokenizeWithPositions(text);

        assertEquals(3, tokens.size());
        assertEquals("packet", tokens.get(0).getToken());
        assertEquals(0, tokens.get(0).getPosition());
        assertEquals("switching", tokens.get(1).getToken());
        assertEquals(1, tokens.get(1).getPosition());
        assertEquals("routing", tokens.get(2).getToken());
        assertEquals(2, tokens.get(2).getPosition());
    }

    @Test
    public void testEmptyOrNull() {
        assertTrue(tokenizer.tokenize(null).isEmpty());
        assertTrue(tokenizer.tokenize("   ").isEmpty());
    }
}
