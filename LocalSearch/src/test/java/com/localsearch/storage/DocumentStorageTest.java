package com.localsearch.storage;

import com.localsearch.model.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentStorageTest {

    @Test
    public void testDocumentCreationAndStatusChange(@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("doc_test.txt");
        Files.writeString(filePath, "initial document content for search test");

        DocumentStorage storage = new DocumentStorage();
        File file = filePath.toFile();

        Document doc = storage.createDocument(file, 6);
        assertNotNull(doc);
        assertEquals("doc_test.txt", doc.getFileName());
        assertEquals(Document.DocumentStatus.INDEXED, doc.getStatus());

        // Initial check: should be INDEXED
        assertEquals(Document.DocumentStatus.INDEXED, storage.checkStatus(doc));

        // Modify file content
        Files.writeString(filePath, "modified content that has different hash and size");
        // Ensure timestamp changes
        file.setLastModified(System.currentTimeMillis() + 2000);

        assertEquals(Document.DocumentStatus.MODIFIED_NEEDS_REINDEX, storage.checkStatus(doc));

        // Delete file: should be MISSING
        Files.delete(filePath);
        assertEquals(Document.DocumentStatus.MISSING, storage.checkStatus(doc));
    }
}
