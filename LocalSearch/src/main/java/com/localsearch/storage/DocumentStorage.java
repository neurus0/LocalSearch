package com.localsearch.storage;

import com.localsearch.model.Document;
import com.localsearch.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles physical file reading, metadata creation, and change detection.
 */
public class DocumentStorage {

    public DocumentStorage() {}

    /**
     * Reads text content of a file.
     */
    public String readContent(File file) throws IOException {
        if (file == null || !file.exists() || !file.canRead()) {
            throw new IOException("File is not accessible: " + (file != null ? file.getAbsolutePath() : "null"));
        }
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Creates a Document model from a physical file.
     */
    public Document createDocument(File file, int wordCount) throws IOException {
        String absPath = file.getAbsolutePath();
        String name = file.getName();
        String ext = FileUtils.getFileExtension(file);
        long size = file.length();
        long lastMod = file.lastModified();
        String hash = FileUtils.calculateSha256(file);
        String id = UUID.nameUUIDFromBytes(absPath.getBytes(StandardCharsets.UTF_8)).toString();

        return new Document(id, name, absPath, ext, size, lastMod, wordCount, System.currentTimeMillis(), hash);
    }

    /**
     * Checks if physical file has changed, been modified, or is missing.
     * Updates Document status.
     */
    public Document.DocumentStatus checkStatus(Document doc) {
        if (doc == null) return Document.DocumentStatus.MISSING;
        File file = new File(doc.getAbsolutePath());

        if (!file.exists()) {
            doc.setStatus(Document.DocumentStatus.MISSING);
            return Document.DocumentStatus.MISSING;
        }

        // Fast check: timestamp and file size
        if (file.lastModified() > doc.getLastModified() || file.length() != doc.getFileSize()) {
            // Detailed hash check
            try {
                String currentHash = FileUtils.calculateSha256(file);
                if (!currentHash.equals(doc.getContentHash())) {
                    doc.setStatus(Document.DocumentStatus.MODIFIED_NEEDS_REINDEX);
                    return Document.DocumentStatus.MODIFIED_NEEDS_REINDEX;
                }
            } catch (Exception e) {
                doc.setStatus(Document.DocumentStatus.MODIFIED_NEEDS_REINDEX);
                return Document.DocumentStatus.MODIFIED_NEEDS_REINDEX;
            }
        }

        doc.setStatus(Document.DocumentStatus.INDEXED);
        return Document.DocumentStatus.INDEXED;
    }
}
