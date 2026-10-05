package com.localsearch.ui;

import com.localsearch.index.InvertedIndex;
import com.localsearch.model.IndexStats;
import com.localsearch.storage.AppSettings;
import com.localsearch.util.DateUtils;
import com.localsearch.util.FileUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Settings & Index Statistics modal dialog.
 */
public class SettingsDialog extends JDialog {

    public interface SettingsCallback {
        void onSettingsSaved(AppSettings updatedSettings);
        void onRebuildIndexRequested();
    }

    private final AppSettings settings;
    private final InvertedIndex invertedIndex;
    private final long indexStorageSizeBytes;
    private final SettingsCallback callback;

    private JCheckBox stopWordCheck;
    private JCheckBox caseInsensitiveCheck;
    private JCheckBox showSnippetsCheck;
    private JComboBox<Integer> maxResultsCombo;

    public SettingsDialog(Frame parent, AppSettings settings, InvertedIndex invertedIndex, long indexStorageSizeBytes, SettingsCallback callback) {
        super(parent, "Settings & Statistics", true);
        this.settings = settings;
        this.invertedIndex = invertedIndex;
        this.indexStorageSizeBytes = indexStorageSizeBytes;
        this.callback = callback;

        setSize(580, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(UITheme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Statistics Card
        IndexStats stats = invertedIndex.getStats(indexStorageSizeBytes);
        JPanel statsCard = createStatsCard(stats);

        // 2. Configuration Settings Card
        JPanel configCard = createConfigCard();

        // 3. Maintenance Card (Rebuild index)
        JPanel maintenanceCard = createMaintenanceCard();

        mainPanel.add(statsCard);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(configCard);
        mainPanel.add(Box.createVerticalStrut(14));
        mainPanel.add(maintenanceCard);

        // Bottom Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.BG_CARD);
        buttonPanel.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = UITheme.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> saveAndClose());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(UITheme.createStyledScrollPane(mainPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatsCard(IndexStats stats) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("INDEX STATISTICS");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_LIGHT);

        JPanel grid = new JPanel(new GridLayout(3, 2, 12, 8));
        grid.setOpaque(false);

        grid.add(createStatItem("Documents Indexed", String.valueOf(stats.getDocumentCount())));
        grid.add(createStatItem("Unique Search Terms", String.format("%,d", stats.getUniqueTerms())));
        grid.add(createStatItem("Total Words Cataloged", String.format("%,d", stats.getTotalWords())));
        grid.add(createStatItem("Total Index Postings", String.format("%,d", stats.getTotalPostings())));
        grid.add(createStatItem("Last Index Time", DateUtils.formatFriendly(stats.getLastIndexTime())));
        grid.add(createStatItem("Index File Size", FileUtils.formatFileSize(stats.getIndexSizeBytes())));

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(grid);
        return card;
    }

    private JPanel createStatItem(String label, String value) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);

        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_BOLD);
        val.setForeground(UITheme.TEXT_PRIMARY);

        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }

    private JPanel createConfigCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel title = new JLabel("SEARCH CONFIGURATION");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_LIGHT);

        stopWordCheck = new JCheckBox("Enable Stop-Word Filtering (removes frequent grammar words like 'the', 'is')", settings.isStopWordFilterEnabled());
        stopWordCheck.setFont(UITheme.FONT_REGULAR);
        stopWordCheck.setForeground(UITheme.TEXT_PRIMARY);
        stopWordCheck.setOpaque(false);

        caseInsensitiveCheck = new JCheckBox("Case-Insensitive Search", settings.isCaseInsensitiveSearch());
        caseInsensitiveCheck.setFont(UITheme.FONT_REGULAR);
        caseInsensitiveCheck.setForeground(UITheme.TEXT_PRIMARY);
        caseInsensitiveCheck.setOpaque(false);

        showSnippetsCheck = new JCheckBox("Show Highlighted Snippets in Results", settings.isShowSnippets());
        showSnippetsCheck.setFont(UITheme.FONT_REGULAR);
        showSnippetsCheck.setForeground(UITheme.TEXT_PRIMARY);
        showSnippetsCheck.setOpaque(false);

        JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        maxPanel.setOpaque(false);
        JLabel maxLbl = new JLabel("Maximum Search Results Displayed:");
        maxLbl.setFont(UITheme.FONT_REGULAR);
        maxLbl.setForeground(UITheme.TEXT_PRIMARY);

        maxResultsCombo = new JComboBox<>(new Integer[]{10, 20, 50, 100});
        maxResultsCombo.setSelectedItem(settings.getMaxResults());
        maxResultsCombo.setBackground(UITheme.BG_INPUT);
        maxResultsCombo.setForeground(UITheme.TEXT_PRIMARY);

        maxPanel.add(maxLbl);
        maxPanel.add(maxResultsCombo);

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(stopWordCheck);
        card.add(Box.createVerticalStrut(6));
        card.add(caseInsensitiveCheck);
        card.add(Box.createVerticalStrut(6));
        card.add(showSnippetsCheck);
        card.add(Box.createVerticalStrut(8));
        card.add(maxPanel);

        return card;
    }

    private JPanel createMaintenanceCard() {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel title = new JLabel("INDEX MAINTENANCE");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.ACCENT_LIGHT);

        JLabel desc = new JLabel("Wipes the persistent index cache and re-reads all cataloged files.");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_MUTED);

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(desc);

        JButton rebuildBtn = UITheme.createSecondaryButton("Rebuild Index");
        rebuildBtn.setIcon(IconFactory.createRefreshIcon(13, UITheme.WARNING_COLOR));
        rebuildBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Rebuild the entire inverted index from scratch?\nThis will re-read all indexed documents.",
                    "Confirm Rebuild Index",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                if (callback != null) {
                    callback.onRebuildIndexRequested();
                }
            }
        });

        card.add(textPanel, BorderLayout.CENTER);
        card.add(rebuildBtn, BorderLayout.EAST);
        return card;
    }

    private void saveAndClose() {
        settings.setStopWordFilterEnabled(stopWordCheck.isSelected());
        settings.setCaseInsensitiveSearch(caseInsensitiveCheck.isSelected());
        settings.setShowSnippets(showSnippetsCheck.isSelected());
        settings.setMaxResults((Integer) maxResultsCombo.getSelectedItem());
        settings.save();

        if (callback != null) {
            callback.onSettingsSaved(settings);
        }
        dispose();
    }
}
