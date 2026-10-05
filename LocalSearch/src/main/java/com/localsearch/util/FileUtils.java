package com.localsearch.util;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Utility methods for file management, hashing, extensions, and OS interactions.
 */
public final class FileUtils {

    public static final Set<String> DEFAULT_SUPPORTED_EXTENSIONS = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList("txt", "md", "java", "csv", "log", "xml", "json", "html", "css"))
    );

    private FileUtils() {}

    /**
     * Extracts extension without leading dot in lowercase.
     */
    public static String getFileExtension(File file) {
        if (file == null) return "";
        return getFileExtension(file.getName());
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        }
        return "";
    }

    /**
     * Checks whether the file extension is supported.
     */
    public static boolean isSupported(File file, Set<String> supportedExtensions) {
        if (file == null || !file.exists() || !file.isFile()) return false;
        String ext = getFileExtension(file);
        Set<String> exts = (supportedExtensions != null) ? supportedExtensions : DEFAULT_SUPPORTED_EXTENSIONS;
        return exts.contains(ext.toLowerCase(Locale.ROOT));
    }

    /**
     * Calculates SHA-256 hash of a file for duplicate and modification detection.
     */
    public static String calculateSha256(File file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Formats bytes to human-readable format.
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.ROOT, "%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Opens the file in the OS default application.
     */
    public static boolean openFile(File file) {
        if (file == null || !file.exists()) return false;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file);
                return true;
            }
            // Fallback for Windows / Linux / Mac
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", file.getAbsolutePath()).start();
                return true;
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
                return true;
            } else {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
                return true;
            }
        } catch (Exception ex) {
            System.err.println("Error opening file: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Opens the containing folder and highlights/reveals the file.
     */
    public static boolean revealInExplorer(File file) {
        if (file == null || !file.exists()) return false;
        try {
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            if (os.contains("win")) {
                new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath()).start();
                return true;
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", "-R", file.getAbsolutePath()).start();
                return true;
            } else {
                File parent = file.getParentFile();
                if (parent != null) {
                    new ProcessBuilder("xdg-open", parent.getAbsolutePath()).start();
                    return true;
                }
            }
        } catch (Exception ex) {
            System.err.println("Error revealing in explorer: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Reads text file safely with UTF-8 encoding.
     */
    public static String readFileToString(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Scans directory recursively for supported files.
     */
    public static List<File> scanDirectoryRecursively(File directory, Set<String> supportedExtensions) {
        List<File> result = new ArrayList<>();
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return result;
        }

        File[] files = directory.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                // Avoid recursive indexing of application index directory or hidden directories
                if (!file.getName().startsWith(".") && !file.getName().equalsIgnoreCase("target") && !file.getName().equalsIgnoreCase("index")) {
                    result.addAll(scanDirectoryRecursively(file, supportedExtensions));
                }
            } else if (file.isFile() && isSupported(file, supportedExtensions)) {
                result.add(file);
            }
        }
        return result;
    }
}
