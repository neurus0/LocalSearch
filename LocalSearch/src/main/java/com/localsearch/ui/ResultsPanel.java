package com.localsearch.ui;

import com.localsearch.model.SearchResult;
import com.localsearch.util.DateUtils;
import com.localsearch.util.FileUtils;
import com.localsearch.util.TextUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * Displays search results cards or helpful empty states with dark developer theme.
 */
public class ResultsPanel extends JPanel {

    public interface ActionHandler {
        void onAddFilesRequested();
        void onOpenFileRequested(File file);
        void onRevealInFolderRequested(File file);
    }

    private final JPanel cardsContainer;
    private final JScrollPane scrollPane;
    private ActionHandler actionHandler;

    public ResultsPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_DARK);

        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(UITheme.BG_DARK);
        cardsContainer.setBorder(new EmptyBorder(12, 16, 20, 16));

        scrollPane = UITheme.createStyledScrollPane(cardsContainer);
        add(scrollPane, BorderLayout.CENTER);

        showInitialEmptyState(0);
    }

    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    /**
     * Shows initial empty state when no documents are in the index.
     */
    public void showInitialEmptyState(int totalDocs) {
        cardsContainer.removeAll();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(UITheme.BG_DARK);
        centerPanel.setBorder(new EmptyBorder(60, 40, 60, 40));

        JLabel titleLabel = new JLabel("LOCALSEARCH", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel;
        if (totalDocs == 0) {
            subLabel = new JLabel("Your local documents are not indexed yet.", JLabel.CENTER);
        } else {
            subLabel = new JLabel("Enter a search term above or try \"phrase search\", AND, OR operators.", JLabel.CENTER);
        }
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subLabel.setForeground(UITheme.TEXT_SECONDARY);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel(totalDocs == 0 ? "Add text files to start searching." : "Fast offline keyword & phrase search across all indexed files.", JLabel.CENTER);
        hintLabel.setFont(UITheme.FONT_REGULAR);
        hintLabel.setForeground(UITheme.TEXT_MUTED);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton addBtn = UITheme.createPrimaryButton("+ Add Files to Index");
        addBtn.setIcon(IconFactory.createPlusIcon(16, Color.WHITE));
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(e -> {
            if (actionHandler != null) actionHandler.onAddFilesRequested();
        });

        JLabel supportedLabel = new JLabel("Supported: TXT · MD · JAVA · CSV · LOG · XML · JSON · HTML · CSS", JLabel.CENTER);
        supportedLabel.setFont(UITheme.FONT_SMALL);
        supportedLabel.setForeground(UITheme.TEXT_MUTED);
        supportedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(subLabel);
        centerPanel.add(Box.createVerticalStrut(6));
        centerPanel.add(hintLabel);
        centerPanel.add(Box.createVerticalStrut(24));
        centerPanel.add(addBtn);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(supportedLabel);
        centerPanel.add(Box.createVerticalGlue());

        cardsContainer.add(centerPanel);
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    /**
     * Shows empty state when a search query produces no matches.
     */
    public void showNoResultsState(String query) {
        cardsContainer.removeAll();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(UITheme.BG_DARK);
        centerPanel.setBorder(new EmptyBorder(60, 40, 60, 40));

        JLabel titleLabel = new JLabel("No matching documents found", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel queryLabel = new JLabel("No results for: \"" + query + "\"", JLabel.CENTER);
        queryLabel.setFont(UITheme.FONT_REGULAR);
        queryLabel.setForeground(UITheme.ACCENT_LIGHT);
        queryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tipsLabel = new JLabel("<html><div style='text-align:center; color:#94A3B8;'>" +
                "<b>Search Tips:</b><br>" +
                "• Check for spelling errors or try alternative keywords<br>" +
                "• Use quotes for exact phrases: <code>\"packet switching\"</code><br>" +
                "• Use <b>OR</b> to match either term: <code>packet OR routing</code><br>" +
                "• Use <b>AND</b> to match all terms: <code>packet AND routing</code>" +
                "</div></html>", JLabel.CENTER);
        tipsLabel.setFont(UITheme.FONT_REGULAR);
        tipsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(8));
        centerPanel.add(queryLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(tipsLabel);
        centerPanel.add(Box.createVerticalGlue());

        cardsContainer.add(centerPanel);
        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    /**
     * Renders a list of search result cards.
     */
    public void displayResults(List<SearchResult> results, String query) {
        cardsContainer.removeAll();

        if (results == null || results.isEmpty()) {
            showNoResultsState(query);
            return;
        }

        for (int i = 0; i < results.size(); i++) {
            SearchResult res = results.get(i);
            JPanel card = createResultCard(res, i + 1);
            cardsContainer.add(card);
            cardsContainer.add(Box.createVerticalStrut(12));
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    private JPanel createResultCard(SearchResult res, int rank) {
        File file = new File(res.getDocument().getAbsolutePath());

        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(UITheme.BG_CARD_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(UITheme.BG_CARD);
            }
        });

        // Top Row: Icon + File Name + Relevance Badge
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);

        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleLeft.setOpaque(false);

        JLabel iconLabel = new JLabel(IconFactory.createDocumentIcon(20, UITheme.ACCENT_LIGHT));
        titleLeft.add(iconLabel);

        JLabel nameLabel = new JLabel(res.getDocument().getFileName());
        nameLabel.setFont(UITheme.FONT_HEADER);
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nameLabel.setToolTipText("Click to open file in default application");
        nameLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (actionHandler != null) actionHandler.onOpenFileRequested(file);
            }
        });
        titleLeft.add(nameLabel);

        // Badge: Relevance %
        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgePanel.setOpaque(false);

        JLabel badge = new JLabel(res.getScorePercentage() + "% relevance");
        badge.setFont(UITheme.FONT_SMALL);
        badge.setForeground(UITheme.ACCENT_LIGHT);
        badge.setBackground(UITheme.ACCENT_BG);
        badge.setOpaque(true);
        badge.setBorder(new CompoundBorder(
                new LineBorder(UITheme.ACCENT_PRIMARY, 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));
        badgePanel.add(badge);

        topPanel.add(titleLeft, BorderLayout.WEST);
        topPanel.add(badgePanel, BorderLayout.EAST);

        // Path Label
        JLabel pathLabel = new JLabel(res.getDocument().getAbsolutePath());
        pathLabel.setFont(UITheme.FONT_MONO);
        pathLabel.setForeground(UITheme.TEXT_MUTED);

        // Middle: Snippet
        String highlightedHtml = TextUtils.highlightTermsHtml(res.getSnippet(), res.getMatchedTerms(), null);
        JLabel snippetLabel = new JLabel("<html><div style='color:#E2E8F0; font-family:Segoe UI, sans-serif; font-size:13px; line-height:1.4;'>"
                + highlightedHtml + "</div></html>");
        snippetLabel.setBorder(new EmptyBorder(4, 0, 4, 0));

        // Bottom Row: Metadata + Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setOpaque(false);

        JPanel metaLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        metaLeft.setOpaque(false);

        JLabel matchesLabel = new JLabel(res.getMatchCount() + " matches");
        matchesLabel.setFont(UITheme.FONT_SMALL);
        matchesLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel modifiedLabel = new JLabel("Modified: " + DateUtils.formatDate(res.getDocument().getLastModified()));
        modifiedLabel.setFont(UITheme.FONT_SMALL);
        modifiedLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel sizeLabel = new JLabel("Size: " + FileUtils.formatFileSize(res.getDocument().getFileSize()));
        sizeLabel.setFont(UITheme.FONT_SMALL);
        sizeLabel.setForeground(UITheme.TEXT_SECONDARY);

        metaLeft.add(matchesLabel);
        metaLeft.add(new JLabel("•") {{ setForeground(UITheme.TEXT_MUTED); }});
        metaLeft.add(modifiedLabel);
        metaLeft.add(new JLabel("•") {{ setForeground(UITheme.TEXT_MUTED); }});
        metaLeft.add(sizeLabel);

        JPanel actionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsRight.setOpaque(false);

        JButton openBtn = UITheme.createSecondaryButton("Open");
        openBtn.setIcon(IconFactory.createExternalLinkIcon(13, UITheme.TEXT_SECONDARY));
        openBtn.addActionListener(e -> {
            if (actionHandler != null) actionHandler.onOpenFileRequested(file);
        });

        JButton revealBtn = UITheme.createSecondaryButton("Reveal");
        revealBtn.setIcon(IconFactory.createFolderIcon(13, UITheme.TEXT_SECONDARY));
        revealBtn.addActionListener(e -> {
            if (actionHandler != null) actionHandler.onRevealInFolderRequested(file);
        });

        actionsRight.add(revealBtn);
        actionsRight.add(openBtn);

        bottomPanel.add(metaLeft, BorderLayout.WEST);
        bottomPanel.add(actionsRight, BorderLayout.EAST);

        // Assemble Card
        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setOpaque(false);
        center.add(pathLabel, BorderLayout.NORTH);
        center.add(snippetLabel, BorderLayout.CENTER);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }
}
