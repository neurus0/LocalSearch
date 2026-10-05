package com.localsearch.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * About dialog describing the LocalSearch project architecture and technologies.
 */
public class AboutDialog extends JDialog {

    public AboutDialog(Frame parent) {
        super(parent, "About LocalSearch", true);
        setSize(480, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel titleLabel = new JLabel("LOCALSEARCH");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("File-Based Document Search Engine");
        subtitleLabel.setFont(UITheme.FONT_HEADER);
        subtitleLabel.setForeground(UITheme.ACCENT_LIGHT);

        JLabel versionLabel = new JLabel("Version 1.0.0 • Offline Desktop Edition");
        versionLabel.setFont(UITheme.FONT_SMALL);
        versionLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel descLabel = new JLabel("<html><div style='color:#CBD5E1; font-family:Segoe UI, sans-serif; font-size:13px; line-height:1.5;'>"
                + "An offline Java application for indexing, searching, and retrieving information from local text documents.<br><br>"
                + "<b>Core Technologies & Architecture:</b><br>"
                + "• Java 17+ Standard Library<br>"
                + "• Java Swing Custom Dark UI Engine<br>"
                + "• Inverted Index with Positional Token Postings<br>"
                + "• TF-IDF & Term Coverage Relevance Ranking<br>"
                + "• Multi-mode Query Processor (AND, OR, \"phrase\")<br>"
                + "• Dynamic Snippet Generation & Term Highlighting<br>"
                + "• SHA-256 Change & Modification Detection<br>"
                + "• 100% Offline & Private (No telemetry or cloud APIs)"
                + "</div></html>");
        descLabel.setBorder(new EmptyBorder(12, 0, 12, 0));

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(versionLabel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(descLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.BG_CARD);
        buttonPanel.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));

        JButton closeBtn = UITheme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
