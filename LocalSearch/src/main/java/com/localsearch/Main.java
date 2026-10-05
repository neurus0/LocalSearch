package com.localsearch;

import com.localsearch.ui.MainWindow;
import com.localsearch.ui.UITheme;

import javax.swing.SwingUtilities;

/**
 * Entry point for LocalSearch desktop application.
 */
public class Main {

    public static void main(String[] args) {
        // Configure System properties for clean anti-aliasing on Windows/Linux/macOS
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("file.encoding", "UTF-8");

        SwingUtilities.invokeLater(() -> {
            UITheme.applyGlobalTheme();
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
