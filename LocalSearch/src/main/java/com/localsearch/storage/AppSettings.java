package com.localsearch.storage;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Application configuration settings with persistent properties storage.
 */
public class AppSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Path SETTINGS_FILE = Paths.get("index", "settings.properties");

    private boolean stopWordFilterEnabled = true;
    private boolean caseInsensitiveSearch = true;
    private boolean showSnippets = true;
    private int maxResults = 50;
    private String indexStoragePath = "index/index.dat";
    private Set<String> supportedExtensions = new LinkedHashSet<>(
            Arrays.asList("txt", "md", "java", "csv", "log", "xml", "json", "html", "css")
    );

    public AppSettings() {}

    public boolean isStopWordFilterEnabled() {
        return stopWordFilterEnabled;
    }

    public void setStopWordFilterEnabled(boolean stopWordFilterEnabled) {
        this.stopWordFilterEnabled = stopWordFilterEnabled;
    }

    public boolean isCaseInsensitiveSearch() {
        return caseInsensitiveSearch;
    }

    public void setCaseInsensitiveSearch(boolean caseInsensitiveSearch) {
        this.caseInsensitiveSearch = caseInsensitiveSearch;
    }

    public boolean isShowSnippets() {
        return showSnippets;
    }

    public void setShowSnippets(boolean showSnippets) {
        this.showSnippets = showSnippets;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public String getIndexStoragePath() {
        return indexStoragePath;
    }

    public void setIndexStoragePath(String indexStoragePath) {
        this.indexStoragePath = indexStoragePath;
    }

    public Set<String> getSupportedExtensions() {
        return Collections.unmodifiableSet(supportedExtensions);
    }

    public void setSupportedExtensions(Set<String> supportedExtensions) {
        this.supportedExtensions = new LinkedHashSet<>(supportedExtensions);
    }

    public synchronized void save() {
        try {
            Path parent = SETTINGS_FILE.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            Properties props = new Properties();
            props.setProperty("stopWordFilterEnabled", String.valueOf(stopWordFilterEnabled));
            props.setProperty("caseInsensitiveSearch", String.valueOf(caseInsensitiveSearch));
            props.setProperty("showSnippets", String.valueOf(showSnippets));
            props.setProperty("maxResults", String.valueOf(maxResults));
            props.setProperty("indexStoragePath", indexStoragePath);
            props.setProperty("supportedExtensions", String.join(",", supportedExtensions));

            try (OutputStream os = Files.newOutputStream(SETTINGS_FILE)) {
                props.store(os, "LocalSearch Configuration Settings");
            }
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    public static AppSettings load() {
        AppSettings settings = new AppSettings();
        if (!Files.exists(SETTINGS_FILE)) {
            return settings;
        }
        try (InputStream is = Files.newInputStream(SETTINGS_FILE)) {
            Properties props = new Properties();
            props.load(is);
            settings.stopWordFilterEnabled = Boolean.parseBoolean(props.getProperty("stopWordFilterEnabled", "true"));
            settings.caseInsensitiveSearch = Boolean.parseBoolean(props.getProperty("caseInsensitiveSearch", "true"));
            settings.showSnippets = Boolean.parseBoolean(props.getProperty("showSnippets", "true"));
            settings.maxResults = Integer.parseInt(props.getProperty("maxResults", "50"));
            settings.indexStoragePath = props.getProperty("indexStoragePath", "index/index.dat");

            String exts = props.getProperty("supportedExtensions");
            if (exts != null && !exts.trim().isEmpty()) {
                Set<String> list = new LinkedHashSet<>();
                for (String ext : exts.split(",")) {
                    if (!ext.trim().isEmpty()) {
                        list.add(ext.trim().toLowerCase(Locale.ROOT));
                    }
                }
                if (!list.isEmpty()) {
                    settings.supportedExtensions = list;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load settings: " + e.getMessage());
        }
        return settings;
    }
}
