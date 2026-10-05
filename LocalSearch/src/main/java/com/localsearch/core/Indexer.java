package com.localsearch.core;

import com.localsearch.index.InvertedIndex;
import com.localsearch.model.Document;
import com.localsearch.storage.AppSettings;
import com.localsearch.storage.DocumentStorage;
import com.localsearch.util.FileUtils;

import java.io.File;
import java.util.*;

/**
 * Indexes documents into the InvertedIndex with progress notification and error handling.
 */
public class Indexer {

    public interface ProgressListener {
        void onProgress(int current, int total, String currentFileName, String statusMessage);
    }

    public static class IndexResult {
        private final int successCount;
        private final int failureCount;
        private final int skippedCount;
        private final List<String> errorMessages;

        public IndexResult(int successCount, int failureCount, int skippedCount, List<String> errorMessages) {
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.skippedCount = skippedCount;
            this.errorMessages = errorMessages != null ? errorMessages : new ArrayList<>();
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public List<String> getErrorMessages() {
            return Collections.unmodifiableList(errorMessages);
        }

        public boolean isAllSuccess() {
            return failureCount == 0;
        }
    }

    private final InvertedIndex invertedIndex;
    private final Tokenizer tokenizer;
    private final DocumentStorage documentStorage;
    private final AppSettings settings;

    public Indexer(InvertedIndex invertedIndex, Tokenizer tokenizer, DocumentStorage documentStorage, AppSettings settings) {
        this.invertedIndex = invertedIndex;
        this.tokenizer = tokenizer != null ? tokenizer : new Tokenizer();
        this.documentStorage = documentStorage != null ? documentStorage : new DocumentStorage();
        this.settings = settings != null ? settings : new AppSettings();
    }

    /**
     * Indexes a single file.
     */
    public boolean indexDocument(File file) throws Exception {
        if (!FileUtils.isSupported(file, settings.getSupportedExtensions())) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getName());
        }

        String content = documentStorage.readContent(file);
        List<Tokenizer.TokenPosition> tokensWithPos = tokenizer.tokenizeWithPositions(content);

        Document doc = documentStorage.createDocument(file, tokensWithPos.size());
        invertedIndex.addDocument(doc, tokensWithPos);
        return true;
    }

    /**
     * Batch indexes multiple files with progress reporting.
     */
    public IndexResult indexFiles(List<File> files, ProgressListener listener) {
        int success = 0;
        int failure = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            return new IndexResult(0, 0, 0, errors);
        }

        int total = files.size();
        for (int i = 0; i < total; i++) {
            File file = files.get(i);
            String fileName = file != null ? file.getName() : "Unknown";

            if (listener != null) {
                listener.onProgress(i + 1, total, fileName, "Indexing " + (i + 1) + " of " + total + "...");
            }

            if (file == null || !file.exists() || !file.isFile()) {
                failure++;
                errors.add("File not found or inaccessible: " + (file != null ? file.getAbsolutePath() : "null"));
                continue;
            }

            if (!FileUtils.isSupported(file, settings.getSupportedExtensions())) {
                skipped++;
                errors.add("Skipped unsupported file: " + fileName);
                continue;
            }

            try {
                // Check if identical document is already indexed
                String currentHash = FileUtils.calculateSha256(file);
                Document existingDoc = invertedIndex.getDocumentByPath(file.getAbsolutePath());
                if (existingDoc != null && currentHash.equals(existingDoc.getContentHash())) {
                    // Document unchanged, just ensure status is OK
                    existingDoc.setStatus(Document.DocumentStatus.INDEXED);
                    success++;
                    continue;
                }

                String content = documentStorage.readContent(file);
                List<Tokenizer.TokenPosition> tokens = tokenizer.tokenizeWithPositions(content);
                Document doc = documentStorage.createDocument(file, tokens.size());

                invertedIndex.addDocument(doc, tokens);
                success++;
            } catch (Exception e) {
                failure++;
                errors.add("Failed to index " + fileName + ": " + e.getMessage());
            }
        }

        if (listener != null) {
            listener.onProgress(total, total, "Completed", "Finished indexing " + success + " documents.");
        }

        return new IndexResult(success, failure, skipped, errors);
    }
}
