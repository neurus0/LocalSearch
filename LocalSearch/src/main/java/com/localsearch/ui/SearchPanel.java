package com.localsearch.ui;

import com.localsearch.core.SearchEngine;
import com.localsearch.util.DateUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Top control panel containing search input, filters, sort controls, and index stats.
 */
public class SearchPanel extends JPanel {

    public interface SearchListener {
        void onSearchRequested(String query, String fileTypeFilter, SearchEngine.SortOrder sortOrder);
        void onAddFilesRequested();
        void onToggleDocumentManagerRequested();
        void onClearSearchRequested();
    }

    private final JTextField searchField;
    private final JComboBox<String> fileTypeCombo;
    private final JComboBox<String> sortCombo;
    private final JButton searchButton;
    private final JButton clearButton;
    private final JButton addFilesButton;
    private final JButton manageDocsButton;

    private final JLabel docCountLabel;
    private final JLabel lastIndexedLabel;
    private final JLabel searchStatsLabel;

    private SearchListener searchListener;

    public SearchPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 20, 10, 20));

        // 1. Search Bar Container
        JPanel searchBarPanel = new JPanel(new BorderLayout(8, 0));
        searchBarPanel.setBackground(UITheme.BG_INPUT);
        searchBarPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 6)
        ));
        searchBarPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel searchIconLabel = new JLabel(IconFactory.createSearchIcon(18, UITheme.TEXT_SECONDARY));

        searchField = new JTextField();
        searchField.setFont(UITheme.FONT_SEARCH);
        searchField.setForeground(UITheme.TEXT_PRIMARY);
        searchField.setCaretColor(UITheme.ACCENT_LIGHT);
        searchField.setBackground(UITheme.BG_INPUT);
        searchField.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Clear button
        clearButton = new JButton("✕");
        clearButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearButton.setForeground(UITheme.TEXT_MUTED);
        clearButton.setBackground(UITheme.BG_INPUT);
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setVisible(false);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            clearButton.setVisible(false);
            if (searchListener != null) searchListener.onClearSearchRequested();
            searchField.requestFocusInWindow();
        });

        searchButton = UITheme.createPrimaryButton("Search");
        searchButton.setIcon(IconFactory.createSearchIcon(14, Color.WHITE));
        searchButton.addActionListener(e -> triggerSearch());

        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                clearButton.setVisible(!searchField.getText().trim().isEmpty());
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    triggerSearch();
                }
            }
        });

        JPanel searchInputGroup = new JPanel(new BorderLayout(4, 0));
        searchInputGroup.setOpaque(false);
        searchInputGroup.add(searchIconLabel, BorderLayout.WEST);
        searchInputGroup.add(searchField, BorderLayout.CENTER);
        searchInputGroup.add(clearButton, BorderLayout.EAST);

        searchBarPanel.add(searchInputGroup, BorderLayout.CENTER);
        searchBarPanel.add(searchButton, BorderLayout.EAST);

        // 2. Filter, Sort, Mode bar
        JPanel controlsBar = new JPanel(new BorderLayout(10, 0));
        controlsBar.setOpaque(false);
        controlsBar.setBorder(new EmptyBorder(10, 0, 8, 0));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setOpaque(false);

        // File Type Filter Combo
        String[] fileTypes = {"All Files", "TXT", "MD", "JAVA", "CSV", "LOG", "XML", "JSON", "HTML", "CSS"};
        fileTypeCombo = createStyledComboBox(fileTypes);
        fileTypeCombo.addActionListener(e -> {
            if (!searchField.getText().trim().isEmpty()) {
                triggerSearch();
            }
        });

        // Sort Combo
        String[] sortOptions = {"Relevance", "File Name", "Last Modified", "File Size"};
        sortCombo = createStyledComboBox(sortOptions);
        sortCombo.addActionListener(e -> {
            if (!searchField.getText().trim().isEmpty()) {
                triggerSearch();
            }
        });

        // Mode hints
        JLabel hintsLabel = new JLabel("<html><span style='color:#64748B;'>Modes:</span> <span style='color:#94A3B8; font-family:Consolas;'>AND · OR · \"phrase\"</span></html>");
        hintsLabel.setFont(UITheme.FONT_SMALL);

        leftControls.add(new JLabel("Filter: ") {{ setForeground(UITheme.TEXT_SECONDARY); setFont(UITheme.FONT_SMALL); }});
        leftControls.add(fileTypeCombo);
        leftControls.add(Box.createHorizontalStrut(6));
        leftControls.add(new JLabel("Sort: ") {{ setForeground(UITheme.TEXT_SECONDARY); setFont(UITheme.FONT_SMALL); }});
        leftControls.add(sortCombo);
        leftControls.add(Box.createHorizontalStrut(10));
        leftControls.add(hintsLabel);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        manageDocsButton = UITheme.createSecondaryButton("Manage Documents");
        manageDocsButton.setIcon(IconFactory.createFolderIcon(14, UITheme.TEXT_SECONDARY));
        manageDocsButton.addActionListener(e -> {
            if (searchListener != null) searchListener.onToggleDocumentManagerRequested();
        });

        addFilesButton = UITheme.createPrimaryButton("+ Add Files");
        addFilesButton.setIcon(IconFactory.createPlusIcon(14, Color.WHITE));
        addFilesButton.addActionListener(e -> {
            if (searchListener != null) searchListener.onAddFilesRequested();
        });

        rightControls.add(manageDocsButton);
        rightControls.add(addFilesButton);

        controlsBar.add(leftControls, BorderLayout.WEST);
        controlsBar.add(rightControls, BorderLayout.EAST);

        // 3. Stats & Result Summary Line
        JPanel statsBar = new JPanel(new BorderLayout(10, 0));
        statsBar.setOpaque(false);
        statsBar.setBorder(new EmptyBorder(4, 2, 4, 2));

        JPanel statsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statsLeft.setOpaque(false);

        docCountLabel = new JLabel("0 documents indexed");
        docCountLabel.setFont(UITheme.FONT_SMALL);
        docCountLabel.setForeground(UITheme.TEXT_SECONDARY);

        lastIndexedLabel = new JLabel("Last indexed: Never");
        lastIndexedLabel.setFont(UITheme.FONT_SMALL);
        lastIndexedLabel.setForeground(UITheme.TEXT_MUTED);

        statsLeft.add(docCountLabel);
        statsLeft.add(new JLabel("•") {{ setForeground(UITheme.TEXT_MUTED); }});
        statsLeft.add(lastIndexedLabel);

        searchStatsLabel = new JLabel("");
        searchStatsLabel.setFont(UITheme.FONT_SMALL);
        searchStatsLabel.setForeground(UITheme.ACCENT_LIGHT);

        statsBar.add(statsLeft, BorderLayout.WEST);
        statsBar.add(searchStatsLabel, BorderLayout.EAST);

        // Assemble search panel
        add(searchBarPanel);
        add(controlsBar);
        add(statsBar);
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(UITheme.FONT_SMALL);
        combo.setForeground(UITheme.TEXT_PRIMARY);
        combo.setBackground(UITheme.BG_CARD);
        combo.setFocusable(false);
        combo.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(3, 6, 3, 6)
        ));
        return combo;
    }

    public void setSearchListener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }

    public void focusSearchField() {
        searchField.requestFocusInWindow();
        searchField.selectAll();
    }

    public String getSearchQuery() {
        return searchField.getText().trim();
    }

    public void setSearchQuery(String query) {
        searchField.setText(query);
        clearButton.setVisible(!query.trim().isEmpty());
    }

    public void updateStats(int docCount, long lastIndexedTimestamp) {
        docCountLabel.setText(docCount + " document" + (docCount == 1 ? "" : "s") + " indexed");
        lastIndexedLabel.setText("Last indexed: " + DateUtils.formatFriendly(lastIndexedTimestamp));
    }

    public void updateSearchStats(int resultCount, long elapsedMs, String query) {
        if (query == null || query.trim().isEmpty()) {
            searchStatsLabel.setText("");
        } else {
            searchStatsLabel.setText("Found " + resultCount + " result" + (resultCount == 1 ? "" : "s") + " in " + elapsedMs + "ms");
        }
    }

    public void triggerSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a search query to search across indexed documents.",
                    "Empty Query", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String filter = (String) fileTypeCombo.getSelectedItem();
        String sortStr = (String) sortCombo.getSelectedItem();
        SearchEngine.SortOrder sortOrder = SearchEngine.SortOrder.RELEVANCE;
        if ("File Name".equals(sortStr)) sortOrder = SearchEngine.SortOrder.FILE_NAME;
        else if ("Last Modified".equals(sortStr)) sortOrder = SearchEngine.SortOrder.LAST_MODIFIED;
        else if ("File Size".equals(sortStr)) sortOrder = SearchEngine.SortOrder.FILE_SIZE;

        if (searchListener != null) {
            searchListener.onSearchRequested(query, filter, sortOrder);
        }
    }
}
