package com.localsearch.core;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes text into searchable words while preserving technical acronyms and terms.
 */
public class Tokenizer {

    // Regex to match technical terms like TCP/IP, C++, C#, .NET, SHA-256, Node.js, CI/CD, HashMap, UTF-8
    // Matches sequences of alphanumeric characters optionally joined by internal symbols (. / - _ # +)
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "c\\+\\+|c#|\\.net|[a-zA-Z0-9]+(?:[-._/+#][a-zA-Z0-9]+)*",
            Pattern.CASE_INSENSITIVE
    );

    public Tokenizer() {}

    /**
     * Tokenizes input text into a list of normalized lowercase tokens.
     */
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return tokens;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group().toLowerCase(Locale.ROOT).trim();
            // Strip any accidental leading or trailing punctuation
            token = cleanPunctuation(token);
            if (!token.isEmpty() && token.length() <= 64) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Tokenizes input text and retains the word position (1-based index).
     */
    public List<TokenPosition> tokenizeWithPositions(String text) {
        List<TokenPosition> result = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return result;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(text);
        int pos = 0;
        while (matcher.find()) {
            String token = matcher.group().toLowerCase(Locale.ROOT).trim();
            token = cleanPunctuation(token);
            if (!token.isEmpty() && token.length() <= 64) {
                result.add(new TokenPosition(token, pos++));
            }
        }
        return result;
    }

    private String cleanPunctuation(String token) {
        // Strip trailing dots, commas, colons unless it's part of a known term like .net
        while (token.endsWith(".") && !token.equalsIgnoreCase(".net")) {
            token = token.substring(0, token.length() - 1);
        }
        while (token.endsWith(",") || token.endsWith(";") || token.endsWith(":") || token.endsWith(")") || token.endsWith("]")) {
            token = token.substring(0, token.length() - 1);
        }
        while (token.startsWith("(") || token.startsWith("[") || token.startsWith("{")) {
            token = token.substring(1);
        }
        return token;
    }

    public static class TokenPosition {
        private final String token;
        private final int position;

        public TokenPosition(String token, int position) {
            this.token = token;
            this.position = position;
        }

        public String getToken() {
            return token;
        }

        public int getPosition() {
            return position;
        }
    }
}
