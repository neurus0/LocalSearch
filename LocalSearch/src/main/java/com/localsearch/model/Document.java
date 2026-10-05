package com.localsearch.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a document indexed by LocalSearch.
 */
public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum DocumentStatus {
        INDEXED("Indexed"),
        MODIFIED_NEEDS_REINDEX("Needs re-indexing"),
        MISSING("Missing");

        private final String displayName;

        DocumentStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String id;
    private final String fileName;
    private final String absolutePath;
    private final String fileExtension;
    private long fileSize;
    private long lastModified;
    private int wordCount;
    private long indexedTimestamp;
    private String contentHash;
    private DocumentStatus status;

    public Document(String id, String fileName, String absolutePath, String fileExtension,
                    long fileSize, long lastModified, int wordCount, long indexedTimestamp,
                    String contentHash) {
        this.id = id;
        this.fileName = fileName;
        this.absolutePath = absolutePath;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
        this.wordCount = wordCount;
        this.indexedTimestamp = indexedTimestamp;
        this.contentHash = contentHash;
        this.status = DocumentStatus.INDEXED;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public long getIndexedTimestamp() {
        return indexedTimestamp;
    }

    public void setIndexedTimestamp(long indexedTimestamp) {
        this.indexedTimestamp = indexedTimestamp;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(absolutePath, document.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath);
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", fileName='" + fileName + '\'' +
                ", path='" + absolutePath + '\'' +
                ", size=" + fileSize +
                ", words=" + wordCount +
                ", status=" + status +
                '}';
    }
}
