package com.localsearch.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Developer-Tool Dark Theme definitions and custom UI component builders.
 */
public final class UITheme {

    // Modern Dark Developer Theme Palette
    public static final Color BG_DARK = new Color(0x0F, 0x17, 0x2A);          // #0F172A Deep Slate
    public static final Color BG_CARD = new Color(0x1E, 0x29, 0x3B);          // #1E293B Card Slate
    public static final Color BG_CARD_HOVER = new Color(0x27, 0x35, 0x4D);    // #27354D
    public static final Color BG_INPUT = new Color(0x16, 0x20, 0x32);         // #162032 Input background
    public static final Color BORDER_COLOR = new Color(0x33, 0x41, 0x55);     // #334155 Border
    public static final Color BORDER_FOCUS = new Color(0x38, 0xBD, 0xF8);     // #38BDF8 Accent border

    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);     // #F8FAFC
    public static final Color TEXT_SECONDARY = new Color(0x94, 0xA3, 0xB8);   // #94A3B8
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8B);       // #64748B

    public static final Color ACCENT_PRIMARY = new Color(0x02, 0x84, 0xC7);   // Sky 600
    public static final Color ACCENT_HOVER = new Color(0x03, 0x69, 0xA1);     // Sky 700
    public static final Color ACCENT_LIGHT = new Color(0x38, 0xBD, 0xF8);     // #38BDF8 Sky 400
    public static final Color ACCENT_BG = new Color(0x0C, 0x4A, 0x6E);        // Sky 900 badge

    public static final Color SUCCESS_COLOR = new Color(0x22, 0xC5, 0x5E);    // Green 500
    public static final Color SUCCESS_BG = new Color(0x05, 0x2E, 0x16);       // Green 950
    public static final Color WARNING_COLOR = new Color(0xF5, 0x9E, 0x0B);    // Amber 500
    public static final Color WARNING_BG = new Color(0x45, 0x1A, 0x03);       // Amber 950
    public static final Color ERROR_COLOR = new Color(0xEF, 0x44, 0x44);      // Red 500
    public static final Color ERROR_BG = new Color(0x45, 0x0A, 0x0A);         // Red 950

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);
    public static final Font FONT_SEARCH = new Font("Segoe UI", Font.PLAIN, 15);

    private UITheme() {}

    /**
     * Applies global Swing Look and Feel tweaks.
     */
    public static void applyGlobalTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("OptionPane.background", BG_DARK);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_REGULAR);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("ComboBox.font", FONT_REGULAR);
        UIManager.put("ToolTip.background", BG_CARD);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", new LineBorder(BORDER_COLOR, 1));
        UIManager.put("ScrollBar.thumb", BORDER_COLOR);
        UIManager.put("ScrollBar.track", BG_DARK);
        UIManager.put("ScrollBar.width", 10);
    }

    /**
     * Creates a custom styled primary button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (btn.getModel().isPressed()) {
                    g2.setColor(ACCENT_HOVER.darker());
                } else if (btn.getModel().isRollover()) {
                    g2.setColor(ACCENT_HOVER);
                } else {
                    g2.setColor(ACCENT_PRIMARY);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 8, 8));
                g2.dispose();
                super.paint(g, c);
            }
        });
        return btn;
    }

    /**
     * Creates a custom styled secondary/outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_CARD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (btn.getModel().isPressed()) {
                    g2.setColor(BG_CARD_HOVER.darker());
                } else if (btn.getModel().isRollover()) {
                    g2.setColor(BG_CARD_HOVER);
                } else {
                    g2.setColor(BG_CARD);
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), 8, 8));
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, c.getWidth() - 1, c.getHeight() - 1, 8, 8));
                g2.dispose();
                super.paint(g, c);
            }
        });
        return btn;
    }

    /**
     * Creates a styled dark scroll pane.
     */
    public static JScrollPane createStyledScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);

        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /**
     * Custom Dark Scrollbar UI
     */
    public static class CustomScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = BORDER_COLOR;
            this.trackColor = BG_DARK;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? ACCENT_PRIMARY : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }
    }
}
