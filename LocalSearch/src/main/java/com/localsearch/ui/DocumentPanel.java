package com.localsearch.ui;

import com.localsearch.model.Document;
import com.localsearch.util.DateUtils;
import com.localsearch.util.FileUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Document Management panel for inspecting, searching, removing, and re-indexing cataloged files.
 */
public class DocumentPanel extends JPanel {

    public interface DocumentActionHandler {
        void onAddFilesRequested();
        void onReindexDocuments(List<Document> documents);
        void onRemoveDocuments(List<Document> documents);
        void onCheckChangesRequested();
        void onBackToSearchRequested();
    }

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> rowSorter;
    private final JTextField filterField;
    private final JLabel countLabel;
    private final List<Document> currentDocuments;

    private DocumentActionHandler actionHandler;

    public DocumentPanel() {
        this.currentDocuments = new ArrayList<>();
        setLayout(new BorderLayout(0, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Top Header & Action Toolbar
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setOpaque(false);

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);

        JButton backBtn = UITheme.createSecondaryButton("← Back to Search");
        backBtn.addActionListener(e -> {
            if (actionHandler != null) actionHandler.onBackToSearchRequested();
        });

        JLabel titleLabel = new JLabel("Document Management");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        countLabel = new JLabel("0 files");
        countLabel.setFont(UITheme.FONT_SMALL);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        headerLeft.add(backBtn);
        headerLeft.add(titleLabel);
        headerLeft.add(countLabel);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);

        JButton scanBtn = UITheme.createSecondaryButton("Check for Changes");
        scanBtn.setIcon(IconFactory.createRefreshIcon(13, UITheme.TEXT_SECONDARY));
        scanBtn.addActionListener(e -> {
            if (actionHandler != null) actionHandler.onCheckChangesRequested();
        });

        JButton addBtn = UITheme.createPrimaryButton("+ Add Files");
        addBtn.setIcon(IconFactory.createPlusIcon(13, Color.WHITE));
        addBtn.addActionListener(e -> {
            if (actionHandler != null) actionHandler.onAddFilesRequested();
        });

        headerRight.add(scanBtn);
        headerRight.add(addBtn);

        headerPanel.add(headerLeft, BorderLayout.WEST);
        headerPanel.add(headerRight, BorderLayout.EAST);

        // 2. Search filter in document table
        JPanel searchBarPanel = new JPanel(new BorderLayout(8, 0));
        searchBarPanel.setBackground(UITheme.BG_INPUT);
        searchBarPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel filterIcon = new JLabel(IconFactory.createSearchIcon(14, UITheme.TEXT_MUTED));
        filterField = new JTextField();
        filterField.setFont(UITheme.FONT_REGULAR);
        filterField.setForeground(UITheme.TEXT_PRIMARY);
        filterField.setCaretColor(UITheme.ACCENT_LIGHT);
        filterField.setBackground(UITheme.BG_INPUT);
        filterField.setBorder(new EmptyBorder(4, 6, 4, 6));
        filterField.putClientProperty("JTextField.placeholderText", "Filter indexed documents by name or path...");

        searchBarPanel.add(filterIcon, BorderLayout.WEST);
        searchBarPanel.add(filterField, BorderLayout.CENTER);

        // 3. Document Table Setup
        String[] columns = {"File Name", "Extension", "Size", "Words", "Status", "Last Modified", "Path"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setFont(UITheme.FONT_REGULAR);
        table.setRowHeight(32);
        table.setSelectionBackground(UITheme.BG_CARD_HOVER);
        table.setSelectionForeground(UITheme.ACCENT_LIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setBackground(UITheme.BG_INPUT);
        table.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.getTableHeader().setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(6).setPreferredWidth(350);

        // Custom renderer for Status column
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateFilter() {
                String text = filterField.getText().trim();
                if (text.isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateFilter(); }
        });

        // Double click on row to open file
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row != -1) {
                        int modelRow = table.convertRowIndexToModel(row);
                        if (modelRow >= 0 && modelRow < currentDocuments.size()) {
                            Document doc = currentDocuments.get(modelRow);
                            FileUtils.openFile(new File(doc.getAbsolutePath()));
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = UITheme.createStyledScrollPane(table);

        // 4. Bottom Action Toolbar
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 0, 0, 0));

        JPanel bottomLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bottomLeft.setOpaque(false);

        JButton reindexSelBtn = UITheme.createSecondaryButton("Re-index Selected");
        reindexSelBtn.setIcon(IconFactory.createRefreshIcon(13, UITheme.TEXT_SECONDARY));
        reindexSelBtn.addActionListener(e -> handleReindexSelected());

        JButton removeSelBtn = UITheme.createSecondaryButton("Remove from Index");
        removeSelBtn.setIcon(IconFactory.createTrashIcon(13, UITheme.ERROR_COLOR));
        removeSelBtn.addActionListener(e -> handleRemoveSelected());

        JButton reindexAllBtn = UITheme.createSecondaryButton("Re-index All");
        reindexAllBtn.addActionListener(e -> {
            if (actionHandler != null && !currentDocuments.isEmpty()) {
                actionHandler.onReindexDocuments(new ArrayList<>(currentDocuments));
            }
        });

        bottomLeft.add(reindexSelBtn);
        bottomLeft.add(removeSelBtn);
        bottomLeft.add(reindexAllBtn);

        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomRight.setOpaque(false);

        JButton openBtn = UITheme.createSecondaryButton("Open File");
        openBtn.setIcon(IconFactory.createExternalLinkIcon(13, UITheme.TEXT_SECONDARY));
        openBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int modelRow = table.convertRowIndexToModel(row);
                Document doc = currentDocuments.get(modelRow);
                FileUtils.openFile(new File(doc.getAbsolutePath()));
            }
        });

        JButton revealBtn = UITheme.createSecondaryButton("Reveal in Folder");
        revealBtn.setIcon(IconFactory.createFolderIcon(13, UITheme.TEXT_SECONDARY));
        revealBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int modelRow = table.convertRowIndexToModel(row);
                Document doc = currentDocuments.get(modelRow);
                FileUtils.revealInExplorer(new File(doc.getAbsolutePath()));
            }
        });

        bottomRight.add(revealBtn);
        bottomRight.add(openBtn);

        bottomBar.add(bottomLeft, BorderLayout.WEST);
        bottomBar.add(bottomRight, BorderLayout.EAST);

        // Assemble View
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(searchBarPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setActionHandler(DocumentActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    public void refreshDocuments(List<Document> documents) {
        currentDocuments.clear();
        tableModel.setRowCount(0);

        if (documents != null) {
            currentDocuments.addAll(documents);
            for (Document doc : documents) {
                tableModel.addRow(new Object[]{
                        doc.getFileName(),
                        doc.getFileExtension().toUpperCase(),
                        FileUtils.formatFileSize(doc.getFileSize()),
                        doc.getWordCount() + " words",
                        doc.getStatus(),
                        DateUtils.formatFriendly(doc.getLastModified()),
                        doc.getAbsolutePath()
                });
            }
        }
        countLabel.setText(currentDocuments.size() + " files");
    }

    private void handleReindexSelected() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select one or more documents to re-index.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Document> selectedDocs = new ArrayList<>();
        for (int r : rows) {
            int modelRow = table.convertRowIndexToModel(r);
            selectedDocs.add(currentDocuments.get(modelRow));
        }

        if (actionHandler != null) {
            actionHandler.onReindexDocuments(selectedDocs);
        }
    }

    private void handleRemoveSelected() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select one or more documents to remove from index.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int count = rows.length;
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove " + count + " document" + (count == 1 ? "" : "s") + " from the LocalSearch index?\n\n" +
                "Note: Removing documents from the index does NOT delete the original files from your disk.",
                "Confirm Removal from Index",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            List<Document> selectedDocs = new ArrayList<>();
            for (int r : rows) {
                int modelRow = table.convertRowIndexToModel(r);
                selectedDocs.add(currentDocuments.get(modelRow));
            }

            if (actionHandler != null) {
                actionHandler.onRemoveDocuments(selectedDocs);
            }
        }
    }

    /**
     * Custom renderer for Document Status badges.
     */
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setFont(UITheme.FONT_SMALL);

            if (value instanceof Document.DocumentStatus) {
                Document.DocumentStatus status = (Document.DocumentStatus) value;
                label.setText(status.getDisplayName());
                switch (status) {
                    case INDEXED:
                        label.setForeground(UITheme.SUCCESS_COLOR);
                        break;
                    case MODIFIED_NEEDS_REINDEX:
                        label.setForeground(UITheme.WARNING_COLOR);
                        break;
                    case MISSING:
                        label.setForeground(UITheme.ERROR_COLOR);
                        break;
                }
            }
            return label;
        }
    }
}
