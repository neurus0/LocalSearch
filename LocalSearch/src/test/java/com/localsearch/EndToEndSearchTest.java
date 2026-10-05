package com.localsearch;

import com.localsearch.core.*;
import com.localsearch.index.IndexStorage;
import com.localsearch.index.InvertedIndex;
import com.localsearch.model.Document;
import com.localsearch.model.SearchResult;
import com.localsearch.storage.AppSettings;
import com.localsearch.storage.DocumentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EndToEndSearchTest {

    private InvertedIndex invertedIndex;
    private DocumentStorage documentStorage;
    private Tokenizer tokenizer;
    private StopWordFilter stopWordFilter;
    private QueryProcessor queryProcessor;
    private Ranker ranker;
    private Indexer indexer;
    private SearchEngine searchEngine;
    private AppSettings settings;

    @BeforeEach
    public void setUp() {
        settings = new AppSettings();
        invertedIndex = new InvertedIndex();
        documentStorage = new DocumentStorage();
        tokenizer = new Tokenizer();
        stopWordFilter = new StopWordFilter();
        queryProcessor = new QueryProcessor(tokenizer, stopWordFilter);
        ranker = new Ranker(invertedIndex);
        indexer = new Indexer(invertedIndex, tokenizer, documentStorage, settings);
        searchEngine = new SearchEngine(invertedIndex, queryProcessor, ranker, documentStorage, settings);
    }

    @Test
    public void testFullDemoScenario(@TempDir Path tempDir) throws Exception {
        // 1. Index sample documents
        File docDir = new File("documents");
        assertTrue(docDir.exists() && docDir.isDirectory(), "documents/ directory must exist");
        File[] files = docDir.listFiles((dir, name) -> name.endsWith(".txt"));
        assertNotNull(files);
        assertTrue(files.length >= 8, "Expected at least 8 sample documents");

        Indexer.IndexResult indexResult = indexer.indexFiles(Arrays.asList(files), null);
        assertTrue(indexResult.isAllSuccess());
        assertEquals(files.length, invertedIndex.getDocumentCount());

        // 2. Search: packet routing (Keyword)
        List<SearchResult> results = searchEngine.search("packet routing", "All Files", SearchEngine.SortOrder.RELEVANCE);
        assertFalse(results.isEmpty());
        // Computer_Networks.txt should be the top ranked result
        assertEquals("Computer_Networks.txt", results.get(0).getDocument().getFileName());
        assertTrue(results.get(0).getScorePercentage() >= 80);
        assertNotNull(results.get(0).getSnippet());

        // 3. Search: "packet switching" (Phrase)
        List<SearchResult> phraseResults = searchEngine.search("\"packet switching\"", "All Files", SearchEngine.SortOrder.RELEVANCE);
        assertFalse(phraseResults.isEmpty());
        assertEquals("Computer_Networks.txt", phraseResults.get(0).getDocument().getFileName());

        // 4. Search: packet AND routing (AND)
        List<SearchResult> andResults = searchEngine.search("packet AND routing", "All Files", SearchEngine.SortOrder.RELEVANCE);
        assertFalse(andResults.isEmpty());
        for (SearchResult r : andResults) {
            String content = documentStorage.readContent(new File(r.getDocument().getAbsolutePath())).toLowerCase();
            assertTrue(content.contains("packet") && content.contains("routing"));
        }

        // 5. Search: packet OR routing (OR)
        List<SearchResult> orResults = searchEngine.search("packet OR routing", "All Files", SearchEngine.SortOrder.RELEVANCE);
        assertTrue(orResults.size() >= andResults.size());

        // 6. Test File Type Filter
        List<SearchResult> filteredResults = searchEngine.search("packet", "TXT", SearchEngine.SortOrder.RELEVANCE);
        for (SearchResult r : filteredResults) {
            assertEquals("txt", r.getDocument().getFileExtension().toLowerCase());
        }

        // 7. Test Sorting: File Name
        List<SearchResult> sortedByName = searchEngine.search("data", "All Files", SearchEngine.SortOrder.FILE_NAME);
        for (int i = 0; i < sortedByName.size() - 1; i++) {
            String nameA = sortedByName.get(i).getDocument().getFileName().toLowerCase();
            String nameB = sortedByName.get(i + 1).getDocument().getFileName().toLowerCase();
            assertTrue(nameA.compareTo(nameB) <= 0);
        }

        // 8. Test Persistence
        Path testPersistPath = tempDir.resolve("persisted_index.dat");
        IndexStorage storage = new IndexStorage(testPersistPath);
        storage.save(invertedIndex);
        InvertedIndex reloadedIndex = storage.load();
        assertEquals(invertedIndex.getDocumentCount(), reloadedIndex.getDocumentCount());

        // 9. Test Modification Detection
        Path testDocPath = tempDir.resolve("Live_Doc.txt");
        Files.writeString(testDocPath, "initial version of text document");
        File liveFile = testDocPath.toFile();
        indexer.indexDocument(liveFile);
        Document liveDoc = invertedIndex.getDocumentByPath(liveFile.getAbsolutePath());
        assertNotNull(liveDoc);
        assertEquals(Document.DocumentStatus.INDEXED, documentStorage.checkStatus(liveDoc));

        // Modify content
        Files.writeString(testDocPath, "modified updated version of text document with new content");
        liveFile.setLastModified(System.currentTimeMillis() + 5000);
        assertEquals(Document.DocumentStatus.MODIFIED_NEEDS_REINDEX, documentStorage.checkStatus(liveDoc));

        // Re-index
        indexer.indexDocument(liveFile);
        Document reindexedDoc = invertedIndex.getDocumentByPath(liveFile.getAbsolutePath());
        assertNotNull(reindexedDoc);
        assertEquals(Document.DocumentStatus.INDEXED, documentStorage.checkStatus(reindexedDoc));
    }
}
