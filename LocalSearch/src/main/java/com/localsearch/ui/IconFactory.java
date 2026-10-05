package com.localsearch.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Generates crisp vector icons programmatically for Swing UI components.
 */
public final class IconFactory {

    private IconFactory() {}

    public static Icon createSearchIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int r = (int) (w * 0.32);
            int cx = (int) (w * 0.40);
            int cy = (int) (h * 0.40);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.drawLine((int) (cx + r * 0.7), (int) (cy + r * 0.7), (int) (w * 0.85), (int) (h * 0.85));
        });
    }

    public static Icon createDocumentIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = (int) (w * 0.2);
            int y = (int) (h * 0.15);
            int dw = (int) (w * 0.6);
            int dh = (int) (h * 0.7);
            int fold = (int) (dw * 0.35);

            Path2D path = new Path2D.Float();
            path.moveTo(x, y);
            path.lineTo(x + dw - fold, y);
            path.lineTo(x + dw, y + fold);
            path.lineTo(x + dw, y + dh);
            path.lineTo(x, y + dh);
            path.closePath();
            g2.draw(path);

            // Fold corner
            g2.drawLine(x + dw - fold, y, x + dw - fold, y + fold);
            g2.drawLine(x + dw - fold, y + fold, x + dw, y + fold);

            // Text lines
            g2.drawLine(x + 4, y + (int) (dh * 0.45), x + dw - 4, y + (int) (dh * 0.45));
            g2.drawLine(x + 4, y + (int) (dh * 0.65), x + dw - 6, y + (int) (dh * 0.65));
        });
    }

    public static Icon createFolderIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = (int) (w * 0.15);
            int y = (int) (h * 0.25);
            int dw = (int) (w * 0.7);
            int dh = (int) (h * 0.55);

            Path2D path = new Path2D.Float();
            path.moveTo(x, y + 4);
            path.lineTo(x, y + dh);
            path.lineTo(x + dw, y + dh);
            path.lineTo(x + dw, y + 4);
            path.lineTo(x + (int)(dw * 0.55), y + 4);
            path.lineTo(x + (int)(dw * 0.45), y);
            path.lineTo(x + 4, y);
            path.closePath();
            g2.draw(path);
        });
    }

    public static Icon createSettingsIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2;
            int cy = h / 2;
            int r = (int) (w * 0.2);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);

            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4.0;
                int x1 = (int) (cx + (r + 2) * Math.cos(angle));
                int y1 = (int) (cy + (r + 2) * Math.sin(angle));
                int x2 = (int) (cx + (r + 5) * Math.cos(angle));
                int y2 = (int) (cy + (r + 5) * Math.sin(angle));
                g2.drawLine(x1, y1, x2, y2);
            }
        });
    }

    public static Icon createInfoIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2;
            int cy = h / 2;
            int r = (int) (w * 0.38);
            g2.drawOval(cx - r, cy - r, r * 2, r * 2);
            g2.fillOval(cx - 1, cy - 5, 2, 2);
            g2.drawLine(cx, cy - 2, cx, cy + 5);
        });
    }

    public static Icon createRefreshIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2;
            int cy = h / 2;
            int r = (int) (w * 0.32);
            g2.drawArc(cx - r, cy - r, r * 2, r * 2, 45, 270);
            // Arrow head
            int ax = (int) (cx + r * Math.cos(Math.toRadians(45)));
            int ay = (int) (cy - r * Math.sin(Math.toRadians(45)));
            g2.drawLine(ax, ay, ax + 4, ay - 1);
            g2.drawLine(ax, ay, ax - 1, ay - 4);
        });
    }

    public static Icon createPlusIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2;
            int cy = h / 2;
            int len = (int) (w * 0.3);
            g2.drawLine(cx - len, cy, cx + len, cy);
            g2.drawLine(cx, cy - len, cx, cy + len);
        });
    }

    public static Icon createTrashIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int x = (int) (w * 0.25);
            int y = (int) (h * 0.3);
            int dw = (int) (w * 0.5);
            int dh = (int) (h * 0.55);

            g2.drawLine((int)(w * 0.2), y, (int)(w * 0.8), y);
            g2.drawRect(x, y, dw, dh);
            g2.drawLine((int)(w * 0.4), (int)(h * 0.22), (int)(w * 0.6), (int)(h * 0.22));
            g2.drawLine((int)(w * 0.4), y + 3, (int)(w * 0.4), y + dh - 3);
            g2.drawLine((int)(w * 0.6), y + 3, (int)(w * 0.6), y + dh - 3);
        });
    }

    public static Icon createExternalLinkIcon(int size, Color color) {
        return new VectorIcon(size, size, (g2, w, h) -> {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawRect((int) (w * 0.2), (int) (h * 0.35), (int) (w * 0.45), (int) (h * 0.45));
            g2.drawLine((int) (w * 0.5), (int) (h * 0.2), (int) (w * 0.8), (int) (h * 0.2));
            g2.drawLine((int) (w * 0.8), (int) (h * 0.2), (int) (w * 0.8), (int) (h * 0.5));
            g2.drawLine((int) (w * 0.45), (int) (h * 0.55), (int) (w * 0.8), (int) (h * 0.2));
        });
    }

    private interface IconPainter {
        void paint(Graphics2D g2, int width, int height);
    }

    private static class VectorIcon implements Icon {
        private final int width;
        private final int height;
        private final IconPainter painter;

        public VectorIcon(int width, int height, IconPainter painter) {
            this.width = width;
            this.height = height;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            painter.paint(g2, width, height);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return width;
        }

        @Override
        public int getIconHeight() {
            return height;
        }
    }
}
