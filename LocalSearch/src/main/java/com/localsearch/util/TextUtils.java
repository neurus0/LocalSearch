package com.localsearch.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for string processing, snippet generation, and search highlighting.
 */
public final class TextUtils {

    private TextUtils() {}

    /**
     * Escapes raw text for safe rendering in Swing HTML labels / panes.
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * Highlights matched terms in the text snippet with a dark-theme styled highlight span.
     */
    public static String highlightTermsHtml(String text, Set<String> terms, String phrase) {
        if (text == null || text.isEmpty()) return "";
        String escaped = escapeHtml(text);

        Set<String> searchTerms = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (phrase != null && !phrase.trim().isEmpty()) {
            searchTerms.add(phrase.trim());
        }
        if (terms != null) {
            for (String t : terms) {
                if (t != null && !t.trim().isEmpty()) {
                    searchTerms.add(t.trim());
                }
            }
        }

        if (searchTerms.isEmpty()) {
            return escaped;
        }

        // Sort terms by length descending to match longer phrases before substrings
        List<String> sortedTerms = new ArrayList<>(searchTerms);
        sortedTerms.sort((a, b) -> Integer.compare(b.length(), a.length()));

        for (String term : sortedTerms) {
            String escapedTerm = Pattern.quote(escapeHtml(term));
            Pattern pattern = Pattern.compile("(?i)\\b(" + escapedTerm + ")\\b");
            Matcher matcher = pattern.matcher(escaped);
            if (matcher.find()) {
                escaped = matcher.replaceAll("<span style=\"background-color:#0369A1; color:#F0F9FF; font-weight:bold; padding:1px 3px; border-radius:3px;\">$1</span>");
            } else {
                // If boundary match didn't match (for special characters like / or -), try general match
                Pattern relaxedPattern = Pattern.compile("(?i)(" + escapedTerm + ")");
                escaped = relaxedPattern.matcher(escaped).replaceAll("<span style=\"background-color:#0369A1; color:#F0F9FF; font-weight:bold; padding:1px 3px; border-radius:3px;\">$1</span>");
            }
        }

        return escaped;
    }

    /**
     * Generates a context snippet around the best matching position.
     */
    public static String generateSnippet(String fullContent, Set<String> queryTerms, String phrase, int maxSnippetLength) {
        if (fullContent == null || fullContent.isEmpty()) {
            return "...";
        }

        String normalizedContent = fullContent.replaceAll("\\s+", " ").trim();
        if (normalizedContent.length() <= maxSnippetLength) {
            return normalizedContent;
        }

        // Find the earliest/strongest match location
        int bestMatchIndex = -1;
        String lowerContent = normalizedContent.toLowerCase(Locale.ROOT);

        if (phrase != null && !phrase.trim().isEmpty()) {
            bestMatchIndex = lowerContent.indexOf(phrase.trim().toLowerCase(Locale.ROOT));
        }

        if (bestMatchIndex == -1 && queryTerms != null) {
            for (String term : queryTerms) {
                if (term == null || term.trim().isEmpty()) continue;
                int idx = lowerContent.indexOf(term.trim().toLowerCase(Locale.ROOT));
                if (idx != -1 && (bestMatchIndex == -1 || idx < bestMatchIndex)) {
                    bestMatchIndex = idx;
                }
            }
        }

        if (bestMatchIndex == -1) {
            // Default to start of document if no match found in content
            int end = Math.min(normalizedContent.length(), maxSnippetLength);
            return normalizedContent.substring(0, end) + (normalizedContent.length() > maxSnippetLength ? "..." : "");
        }

        int half = maxSnippetLength / 2;
        int start = Math.max(0, bestMatchIndex - half);
        int end = Math.min(normalizedContent.length(), start + maxSnippetLength);

        // Adjust boundaries to word boundaries if possible
        if (start > 0) {
            int spaceIdx = normalizedContent.indexOf(' ', start);
            if (spaceIdx != -1 && spaceIdx < start + 20) {
                start = spaceIdx + 1;
            }
        }
        if (end < normalizedContent.length()) {
            int spaceIdx = normalizedContent.lastIndexOf(' ', end);
            if (spaceIdx != -1 && spaceIdx > end - 20) {
                end = spaceIdx;
            }
        }

        String snippet = normalizedContent.substring(start, end).trim();
        String prefix = (start > 0) ? "..." : "";
        String suffix = (end < normalizedContent.length()) ? "..." : "";

        return prefix + snippet + suffix;
    }
}
