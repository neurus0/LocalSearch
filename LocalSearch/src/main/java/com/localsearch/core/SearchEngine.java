package com.localsearch.core;

import com.localsearch.index.InvertedIndex;
import com.localsearch.model.Document;
import com.localsearch.model.IndexEntry;
import com.localsearch.model.SearchQuery;
import com.localsearch.model.SearchResult;
import com.localsearch.storage.AppSettings;
import com.localsearch.storage.DocumentStorage;

import java.io.File;
import java.util.*;

/**
 * Main Search Engine coordinating query processing, candidate retrieval, ranking, and filtering.
 */
public class SearchEngine {

    public enum SortOrder {
        RELEVANCE("Relevance"),
        FILE_NAME("File Name"),
        LAST_MODIFIED("Last Modified"),
        FILE_SIZE("File Size");

        private final String displayName;

        SortOrder(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final InvertedIndex invertedIndex;
    private final QueryProcessor queryProcessor;
    private final Ranker ranker;
    private final DocumentStorage documentStorage;
    private final AppSettings settings;

    public SearchEngine(InvertedIndex invertedIndex, QueryProcessor queryProcessor,
                        Ranker ranker, DocumentStorage documentStorage, AppSettings settings) {
        this.invertedIndex = invertedIndex;
        this.queryProcessor = queryProcessor;
        this.ranker = ranker;
        this.documentStorage = documentStorage;
        this.settings = settings;
    }

    /**
     * Executes a search query with optional file type filter and sort order.
     */
    public List<SearchResult> search(String rawQuery, String fileTypeFilter, SortOrder sortOrder) {
        if (rawQuery == null || rawQuery.trim().isEmpty()) {
            return Collections.emptyList();
        }

        SearchQuery query = queryProcessor.parse(rawQuery, settings.isStopWordFilterEnabled());
        Set<String> candidateDocIds = findCandidateDocuments(query);
        if (candidateDocIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Read content only for candidate documents to build snippets efficiently
        Map<String, String> docContents = new HashMap<>();
        if (settings.isShowSnippets()) {
            for (String docId : candidateDocIds) {
                Document doc = invertedIndex.getDocument(docId);
                if (doc != null) {
                    try {
                        docContents.put(docId, documentStorage.readContent(new File(doc.getAbsolutePath())));
                    } catch (Exception ignored) {
                        docContents.put(docId, "");
                    }
                }
            }
        }

        // Rank results with TF-IDF
        List<SearchResult> results = ranker.rank(query, candidateDocIds, docContents, 220);

        // Apply File Type Filter
        if (fileTypeFilter != null && !fileTypeFilter.equalsIgnoreCase("All")
                && !fileTypeFilter.equalsIgnoreCase("All Files")
                && !fileTypeFilter.trim().isEmpty()) {
            String filterExt = fileTypeFilter.replace(".", "").trim().toLowerCase(Locale.ROOT);
            results.removeIf(r -> !r.getDocument().getFileExtension().equalsIgnoreCase(filterExt));
        }

        // Apply Sorting
        SortOrder order = sortOrder != null ? sortOrder : SortOrder.RELEVANCE;
        switch (order) {
            case FILE_NAME:
                results.sort(Comparator.comparing(r -> r.getDocument().getFileName().toLowerCase(Locale.ROOT)));
                break;
            case LAST_MODIFIED:
                results.sort((a, b) -> Long.compare(b.getDocument().getLastModified(), a.getDocument().getLastModified()));
                break;
            case FILE_SIZE:
                results.sort((a, b) -> Long.compare(b.getDocument().getFileSize(), a.getDocument().getFileSize()));
                break;
            case RELEVANCE:
            default:
                Collections.sort(results);
                break;
        }

        // Apply max results limit
        int max = settings.getMaxResults();
        if (max > 0 && results.size() > max) {
            return new ArrayList<>(results.subList(0, max));
        }

        return results;
    }

    /**
     * Resolves candidate document IDs from the inverted index based on query semantics.
     */
    private Set<String> findCandidateDocuments(SearchQuery query) {
        Set<String> candidates = new HashSet<>();
        List<String> terms = query.getTerms();

        if (terms.isEmpty()) {
            return candidates;
        }

        switch (query.getQueryType()) {
            case PHRASE: {
                // Must contain all phrase tokens and in exact sequential order
                Set<String> commonDocs = null;
                for (String term : terms) {
                    IndexEntry entry = invertedIndex.getEntry(term);
                    if (entry == null) return Collections.emptySet();
                    Set<String> docsWithTerm = entry.getPostings().keySet();
                    if (commonDocs == null) {
                        commonDocs = new HashSet<>(docsWithTerm);
                    } else {
                        commonDocs.retainAll(docsWithTerm);
                    }
                }
                if (commonDocs == null || commonDocs.isEmpty()) {
                    return Collections.emptySet();
                }
                for (String docId : commonDocs) {
                    if (invertedIndex.containsExactPhrase(docId, terms)) {
                        candidates.add(docId);
                    }
                }
                break;
            }

            case AND: {
                // Intersection of all term postings
                Set<String> commonDocs = null;
                for (String term : terms) {
                    IndexEntry entry = invertedIndex.getEntry(term);
                    if (entry == null) return Collections.emptySet();
                    Set<String> docsWithTerm = entry.getPostings().keySet();
                    if (commonDocs == null) {
                        commonDocs = new HashSet<>(docsWithTerm);
                    } else {
                        commonDocs.retainAll(docsWithTerm);
                    }
                }
                if (commonDocs != null) {
                    candidates.addAll(commonDocs);
                }
                break;
            }

            case OR:
            case KEYWORD:
            default: {
                // Union of term postings
                for (String term : terms) {
                    IndexEntry entry = invertedIndex.getEntry(term);
                    if (entry != null) {
                        candidates.addAll(entry.getPostings().keySet());
                    }
                }
                break;
            }
        }

        return candidates;
    }
}
