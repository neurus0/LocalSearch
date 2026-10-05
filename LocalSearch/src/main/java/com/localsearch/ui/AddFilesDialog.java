package com.localsearch.ui;

import com.localsearch.core.Indexer;
import com.localsearch.util.FileUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Modal dialog for selecting files/folders and tracking indexing progress via SwingWorker.
 */
public class AddFilesDialog extends JDialog {

    public interface IndexingCallback {
        void onIndexingFinished(Indexer.IndexResult result);
    }

    private final Indexer indexer;
    private final Set<String> supportedExtensions;
    private final IndexingCallback callback;

    private final JProgressBar progressBar;
    private final JLabel titleLabel;
    private final JLabel currentFileLabel;
    private final JLabel processedCountLabel;
    private final JTextArea errorArea;
    private final JScrollPane errorScrollPane;
    private final JButton closeButton;
    private final JButton cancelButton;

    private SwingWorker<Indexer.IndexResult, Void> worker;

    public AddFilesDialog(Frame parent, Indexer indexer, Set<String> supportedExtensions, IndexingCallback callback) {
        super(parent, "Indexing Documents", true);
        this.indexer = indexer;
        this.supportedExtensions = supportedExtensions;
        this.callback = callback;

        setSize(540, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(24, 28, 16, 28));

        titleLabel = new JLabel("Indexing documents...");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(UITheme.ACCENT_PRIMARY);
        progressBar.setBackground(UITheme.BG_INPUT);
        progressBar.setFont(UITheme.FONT_BOLD);
        progressBar.setPreferredSize(new Dimension(480, 24));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        progressBar.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true));

        currentFileLabel = new JLabel("Preparing files...");
        currentFileLabel.setFont(UITheme.FONT_SMALL);
        currentFileLabel.setForeground(UITheme.ACCENT_LIGHT);

        processedCountLabel = new JLabel("0 / 0");
        processedCountLabel.setFont(UITheme.FONT_SMALL);
        processedCountLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel infoRow = new JPanel(new BorderLayout());
        infoRow.setOpaque(false);
        infoRow.add(currentFileLabel, BorderLayout.WEST);
        infoRow.add(processedCountLabel, BorderLayout.EAST);

        errorArea = new JTextArea();
        errorArea.setFont(UITheme.FONT_MONO);
        errorArea.setForeground(UITheme.ERROR_COLOR);
        errorArea.setBackground(UITheme.BG_CARD);
        errorArea.setEditable(false);
        errorScrollPane = UITheme.createStyledScrollPane(errorArea);
        errorScrollPane.setPreferredSize(new Dimension(480, 100));
        errorScrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        errorScrollPane.setVisible(false);

        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(progressBar);
        contentPanel.add(Box.createVerticalStrut(8));
        contentPanel.add(infoRow);
        contentPanel.add(Box.createVerticalStrut(14));
        contentPanel.add(errorScrollPane);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttonPanel.setBackground(UITheme.BG_CARD);
        buttonPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 16, 6, 16)
        ));

        cancelButton = UITheme.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> {
            if (worker != null && !worker.isDone()) {
                worker.cancel(true);
            }
            dispose();
        });

        closeButton = UITheme.createPrimaryButton("Done");
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(closeButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Shows file chooser and starts background indexing.
     */
    public void chooseAndIndex() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Files or Directory to Index");
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int result = chooser.showOpenDialog(getParent());
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = chooser.getSelectedFiles();
            if (selectedFiles == null || selectedFiles.length == 0) {
                File single = chooser.getSelectedFile();
                if (single != null) {
                    selectedFiles = new File[]{single};
                }
            }
            if (selectedFiles != null && selectedFiles.length > 0) {
                startIndexing(selectedFiles);
                setVisible(true);
            }
        }
    }

    /**
     * Starts indexing the provided files or folders.
     */
    public void startIndexing(File[] filesOrDirs) {
        List<File> allFiles = new ArrayList<>();
        for (File f : filesOrDirs) {
            if (f.isDirectory()) {
                allFiles.addAll(FileUtils.scanDirectoryRecursively(f, supportedExtensions));
            } else if (f.isFile() && FileUtils.isSupported(f, supportedExtensions)) {
                allFiles.add(f);
            }
        }

        if (allFiles.isEmpty()) {
            JOptionPane.showMessageDialog(
                    getParent(),
                    "No supported text files found in the selection.\nSupported extensions: " + String.join(", ", supportedExtensions),
                    "No Files Found",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        int totalCount = allFiles.size();
        progressBar.setMaximum(totalCount);
        progressBar.setValue(0);
        processedCountLabel.setText("0 / " + totalCount);

        worker = new SwingWorker<>() {
            @Override
            protected Indexer.IndexResult doInBackground() {
                return indexer.indexFiles(allFiles, (current, total, currentFileName, statusMessage) -> {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(current);
                        int pct = (int) Math.round(((double) current / total) * 100);
                        progressBar.setString(pct + "%");
                        currentFileLabel.setText(currentFileName);
                        processedCountLabel.setText(current + " / " + total);
                    });
                });
            }

            @Override
            protected void done() {
                try {
                    Indexer.IndexResult indexResult = get();
                    cancelButton.setEnabled(false);
                    closeButton.setEnabled(true);

                    if (indexResult.isAllSuccess()) {
                        titleLabel.setText("Indexing Completed");
                        currentFileLabel.setText(indexResult.getSuccessCount() + " documents indexed successfully.");
                        currentFileLabel.setForeground(UITheme.SUCCESS_COLOR);
                    } else {
                        titleLabel.setText("Indexing Completed with Warnings");
                        currentFileLabel.setText(indexResult.getSuccessCount() + " indexed successfully, " +
                                indexResult.getFailureCount() + " failed.");
                        currentFileLabel.setForeground(UITheme.WARNING_COLOR);

                        StringBuilder errs = new StringBuilder();
                        for (String msg : indexResult.getErrorMessages()) {
                            errs.append("• ").append(msg).append("\n");
                        }
                        errorArea.setText(errs.toString());
                        errorScrollPane.setVisible(true);
                        setSize(540, 440);
                    }

                    if (callback != null) {
                        callback.onIndexingFinished(indexResult);
                    }
                } catch (Exception e) {
                    titleLabel.setText("Indexing Interrupted");
                    currentFileLabel.setText("Operation cancelled or failed.");
                    currentFileLabel.setForeground(UITheme.ERROR_COLOR);
                    closeButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
}
