package com.localsearch.ui;

import com.localsearch.core.*;
import com.localsearch.index.IndexStorage;
import com.localsearch.index.InvertedIndex;
import com.localsearch.model.Document;
import com.localsearch.model.IndexStats;
import com.localsearch.model.SearchResult;
import com.localsearch.storage.AppSettings;
import com.localsearch.storage.DocumentStorage;
import com.localsearch.util.FileUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window for LocalSearch.
 */
public class MainWindow extends JFrame {

    private static final String VIEW_SEARCH = "SEARCH_RESULTS";
    private static final String VIEW_DOCUMENTS = "DOCUMENTS_MANAGEMENT";

    private final AppSettings settings;
    private final InvertedIndex invertedIndex;
    private final IndexStorage indexStorage;
    private final DocumentStorage documentStorage;
    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;
    private final QueryProcessor queryProcessor;
    private final Ranker ranker;
    private final Indexer indexer;
    private final SearchEngine searchEngine;

    // UI Components
    private SearchPanel searchPanel;
    private ResultsPanel resultsPanel;
    private DocumentPanel documentPanel;
    private JPanel mainCardContainer;
    private CardLayout cardLayout;

    private JLabel statusLabel;
    private JLabel quickStatsLabel;

    public MainWindow() {
        super("LocalSearch - Offline Document Search Engine");

        // 1. Initialize Core Services & Load Index
        this.settings = AppSettings.load();
        this.indexStorage = new IndexStorage();
        this.invertedIndex = indexStorage.load();
        this.documentStorage = new DocumentStorage();
        this.tokenizer = new Tokenizer();
        this.stopWordFilter = new StopWordFilter();
        this.queryProcessor = new QueryProcessor(tokenizer, stopWordFilter);
        this.ranker = new Ranker(invertedIndex);
        this.indexer = new Indexer(invertedIndex, tokenizer, documentStorage, settings);
        this.searchEngine = new SearchEngine(invertedIndex, queryProcessor, ranker, documentStorage, settings);

        // 2. Setup Main Frame Properties
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        // 3. Build UI Components
        initUI();

        // 4. Setup Event Listeners & Shortcuts
        setupKeyBindings();
        setupWindowClosing();

        // 5. Initial State & Background File Verification
        updateAllStats();
        checkDocumentChangesAsync(false);
    }

    private void initUI() {
        // --- Top Header ---
        JPanel topHeader = new JPanel(new BorderLayout(16, 0));
        topHeader.setBackground(UITheme.BG_DARK);
        topHeader.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel logoIcon = new JLabel(IconFactory.createSearchIcon(24, UITheme.ACCENT_LIGHT));
        JLabel titleLabel = new JLabel("LOCALSEARCH");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Offline Document Search Engine  •  \"Search your documents. Offline. Fast.\"");
        subtitleLabel.setFont(UITheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel titleTextGroup = new JPanel();
        titleTextGroup.setLayout(new BoxLayout(titleTextGroup, BoxLayout.Y_AXIS));
        titleTextGroup.setOpaque(false);
        titleTextGroup.add(titleLabel);
        titleTextGroup.add(subtitleLabel);

        titlePanel.add(logoIcon);
        titlePanel.add(titleTextGroup);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setOpaque(false);

        JButton settingsBtn = UITheme.createSecondaryButton("Settings");
        settingsBtn.setIcon(IconFactory.createSettingsIcon(14, UITheme.TEXT_SECONDARY));
        settingsBtn.addActionListener(e -> openSettings());

        JButton aboutBtn = UITheme.createSecondaryButton("About");
        aboutBtn.setIcon(IconFactory.createInfoIcon(14, UITheme.TEXT_SECONDARY));
        aboutBtn.addActionListener(e -> openAbout());

        headerButtons.add(settingsBtn);
        headerButtons.add(aboutBtn);

        topHeader.add(titlePanel, BorderLayout.WEST);
        topHeader.add(headerButtons, BorderLayout.EAST);

        // --- Search Control Panel ---
        searchPanel = new SearchPanel();
        searchPanel.setSearchListener(new SearchPanel.SearchListener() {
            @Override
            public void onSearchRequested(String query, String fileTypeFilter, SearchEngine.SortOrder sortOrder) {
                performSearch(query, fileTypeFilter, sortOrder);
            }

            @Override
            public void onAddFilesRequested() {
                openAddFilesDialog();
            }

            @Override
            public void onToggleDocumentManagerRequested() {
                showDocumentManagement();
            }

            @Override
            public void onClearSearchRequested() {
                resultsPanel.showInitialEmptyState(invertedIndex.getDocumentCount());
                setStatus("Ready");
                searchPanel.updateSearchStats(0, 0, "");
            }
        });

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(topHeader);
        topContainer.add(searchPanel);

        // --- Center View Container with CardLayout ---
        cardLayout = new CardLayout();
        mainCardContainer = new JPanel(cardLayout);
        mainCardContainer.setOpaque(false);

        resultsPanel = new ResultsPanel();
        resultsPanel.setActionHandler(new ResultsPanel.ActionHandler() {
            @Override
            public void onAddFilesRequested() {
                openAddFilesDialog();
            }

            @Override
            public void onOpenFileRequested(File file) {
                if (!FileUtils.openFile(file)) {
                    showError("Could not open file", "Unable to open: " + file.getAbsolutePath());
                }
            }

            @Override
            public void onRevealInFolderRequested(File file) {
                if (!FileUtils.revealInExplorer(file)) {
                    showError("Could not reveal file", "Unable to find: " + file.getAbsolutePath());
                }
            }
        });

        documentPanel = new DocumentPanel();
        documentPanel.setActionHandler(new DocumentPanel.DocumentActionHandler() {
            @Override
            public void onAddFilesRequested() {
                openAddFilesDialog();
            }

            @Override
            public void onReindexDocuments(List<Document> documents) {
                reindexDocuments(documents);
            }

            @Override
            public void onRemoveDocuments(List<Document> documents) {
                removeDocuments(documents);
            }

            @Override
            public void onCheckChangesRequested() {
                checkDocumentChangesAsync(true);
            }

            @Override
            public void onBackToSearchRequested() {
                showSearchResults();
            }
        });

        mainCardContainer.add(resultsPanel, VIEW_SEARCH);
        mainCardContainer.add(documentPanel, VIEW_DOCUMENTS);

        // --- Bottom Status Bar ---
        JPanel statusBar = new JPanel(new BorderLayout(10, 0));
        statusBar.setBackground(UITheme.BG_CARD);
        statusBar.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 16, 6, 16)
        ));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);

        quickStatsLabel = new JLabel("Terms: 0 | Postings: 0");
        quickStatsLabel.setFont(UITheme.FONT_SMALL);
        quickStatsLabel.setForeground(UITheme.TEXT_MUTED);

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(quickStatsLabel, BorderLayout.EAST);

        // Assemble Window
        add(topContainer, BorderLayout.NORTH);
        add(mainCardContainer, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupKeyBindings() {
        JRootPane root = getRootPane();

        // Ctrl + F: Focus Search Bar
        root.registerKeyboardAction(e -> {
            showSearchResults();
            searchPanel.focusSearchField();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + O: Add Files
        root.registerKeyboardAction(e -> openAddFilesDialog(),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Ctrl + R: Re-index / check changes
        root.registerKeyboardAction(e -> checkDocumentChangesAsync(true),
                KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Esc: Clear or Switch back to Search View
        root.registerKeyboardAction(e -> {
            showSearchResults();
            searchPanel.setSearchQuery("");
            resultsPanel.showInitialEmptyState(invertedIndex.getDocumentCount());
            setStatus("Ready");
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void setupWindowClosing() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveStateAndExit();
            }
        });
    }

    private void saveStateAndExit() {
        try {
            indexStorage.save(invertedIndex);
            settings.save();
        } catch (Exception ex) {
            System.err.println("Error saving state: " + ex.getMessage());
        }
        dispose();
        System.exit(0);
    }

    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    public void updateAllStats() {
        int docCount = invertedIndex.getDocumentCount();
        long lastTime = invertedIndex.getLastIndexTime();
        searchPanel.updateStats(docCount, lastTime);

        IndexStats stats = invertedIndex.getStats(indexStorage.getStorageSizeBytes());
        quickStatsLabel.setText(String.format("Unique Terms: %,d  |  Postings: %,d  |  Index: %s",
                stats.getUniqueTerms(), stats.getTotalPostings(), FileUtils.formatFileSize(stats.getIndexSizeBytes())));

        documentPanel.refreshDocuments(new ArrayList<>(invertedIndex.getAllDocuments().values()));
    }

    public void showSearchResults() {
        cardLayout.show(mainCardContainer, VIEW_SEARCH);
    }

    public void showDocumentManagement() {
        documentPanel.refreshDocuments(new ArrayList<>(invertedIndex.getAllDocuments().values()));
        cardLayout.show(mainCardContainer, VIEW_DOCUMENTS);
        setStatus("Managing " + invertedIndex.getDocumentCount() + " indexed documents.");
    }

    public void performSearch(String query, String fileTypeFilter, SearchEngine.SortOrder sortOrder) {
        showSearchResults();
        setStatus("Searching for \"" + query + "\"...");

        long startTime = System.currentTimeMillis();
        List<SearchResult> results = searchEngine.search(query, fileTypeFilter, sortOrder);
        long elapsed = System.currentTimeMillis() - startTime;

        resultsPanel.displayResults(results, query);
        searchPanel.updateSearchStats(results.size(), elapsed, query);
        setStatus("Search completed in " + elapsed + "ms. Found " + results.size() + " matching document(s).");
    }

    public void openAddFilesDialog() {
        AddFilesDialog dialog = new AddFilesDialog(this, indexer, settings.getSupportedExtensions(), result -> {
            persistIndex();
            updateAllStats();
            resultsPanel.showInitialEmptyState(invertedIndex.getDocumentCount());
            setStatus("Indexed " + result.getSuccessCount() + " documents successfully.");
        });
        dialog.chooseAndIndex();
    }

    public void reindexDocuments(List<Document> docs) {
        if (docs == null || docs.isEmpty()) return;

        List<File> files = new ArrayList<>();
        for (Document d : docs) {
            files.add(new File(d.getAbsolutePath()));
        }

        AddFilesDialog dialog = new AddFilesDialog(this, indexer, settings.getSupportedExtensions(), result -> {
            persistIndex();
            updateAllStats();
            setStatus("Re-indexed " + result.getSuccessCount() + " documents.");
        });
        dialog.startIndexing(files.toArray(new File[0]));
        dialog.setVisible(true);
    }

    public void removeDocuments(List<Document> docs) {
        if (docs == null || docs.isEmpty()) return;

        int removed = 0;
        for (Document doc : docs) {
            if (invertedIndex.removeDocument(doc.getId())) {
                removed++;
            }
        }

        persistIndex();
        updateAllStats();
        setStatus("Removed " + removed + " document(s) from index.");
        JOptionPane.showMessageDialog(this, "Removed " + removed + " document(s) from the index.", "Documents Removed", JOptionPane.INFORMATION_MESSAGE);
    }

    public void checkDocumentChangesAsync(boolean notifyUser) {
        setStatus("Checking for modified or missing documents...");
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            private int modifiedCount = 0;
            private int missingCount = 0;

            @Override
            protected Integer doInBackground() {
                for (Document doc : invertedIndex.getAllDocuments().values()) {
                    Document.DocumentStatus status = documentStorage.checkStatus(doc);
                    if (status == Document.DocumentStatus.MODIFIED_NEEDS_REINDEX) {
                        modifiedCount++;
                    } else if (status == Document.DocumentStatus.MISSING) {
                        missingCount++;
                    }
                }
                return modifiedCount + missingCount;
            }

            @Override
            protected void done() {
                updateAllStats();
                if (modifiedCount > 0 || missingCount > 0) {
                    setStatus("Status: " + modifiedCount + " file(s) need re-indexing, " + missingCount + " file(s) missing.");
                    if (notifyUser) {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "File change check complete:\n" +
                                        "• " + modifiedCount + " modified file(s) require re-indexing\n" +
                                        "• " + missingCount + " missing file(s) detected\n\n" +
                                        "Use the Document Management panel to re-index or remove them.",
                                "File Status Check",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    setStatus("Ready. All " + invertedIndex.getDocumentCount() + " indexed files are up to date.");
                    if (notifyUser) {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "All indexed documents are up to date.",
                                "Files Up to Date",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }

    public void rebuildEntireIndex() {
        List<Document> currentDocs = new ArrayList<>(invertedIndex.getAllDocuments().values());
        invertedIndex.clear();
        persistIndex();
        updateAllStats();

        if (currentDocs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Index was cleared. No documents to rebuild.", "Index Rebuilt", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        reindexDocuments(currentDocs);
    }

    private void persistIndex() {
        try {
            indexStorage.save(invertedIndex);
        } catch (IOException e) {
            System.err.println("Failed to persist index: " + e.getMessage());
        }
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(
                this,
                settings,
                invertedIndex,
                indexStorage.getStorageSizeBytes(),
                new SettingsDialog.SettingsCallback() {
                    @Override
                    public void onSettingsSaved(AppSettings updatedSettings) {
                        updateAllStats();
                        setStatus("Settings updated successfully.");
                    }

                    @Override
                    public void onRebuildIndexRequested() {
                        rebuildEntireIndex();
                    }
                }
        );
        dialog.setVisible(true);
    }

    private void openAbout() {
        AboutDialog dialog = new AboutDialog(this);
        dialog.setVisible(true);
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
