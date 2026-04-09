package com.camara.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;

public class CircularTimerPanel extends JPanel {
    private int remainingSeconds;
    private int totalSeconds;
    private String displayTime = "00:00";
    private boolean isFinished = false;

    public CircularTimerPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 400));
    }

    public void updateTime(int remainingSeconds, int totalSeconds, String displayTime) {
        this.remainingSeconds = remainingSeconds;
        this.totalSeconds = totalSeconds;
        this.displayTime = displayTime;
        this.isFinished = remainingSeconds < 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 60;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        int strokeWidth = Math.max(4, size / 20);

        // Background circle
        if (!isFinished) {
            g2.setColor(new Color(80, 80, 80));
            g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(x, y, size, size);

            // Progress arc
            if (totalSeconds > 0) {
                double progress = (double) remainingSeconds / totalSeconds;
                double angle = progress * 360;

                g2.setColor(remainingSeconds <= 30 ? Theme.WARNING_COLOR : Theme.ACCENT_COLOR);
                g2.draw(new Arc2D.Double(x, y, size, size, 90, angle, Arc2D.OPEN));
            }
        }

        // Time text
        g2.setColor(isFinished ? Theme.WARNING_COLOR : Theme.TEXT_PRIMARY);

        if (isFinished) {
            int fontSize = Math.max(12, size / 6);
            g2.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();

            String line1 = "TEMPO";
            String line2 = "ESGOTADO";

            int textX1 = (getWidth() - fm.stringWidth(line1)) / 2;
            int textX2 = (getWidth() - fm.stringWidth(line2)) / 2;

            // Draw two lines centered vertically
            int totalHeight = fm.getHeight() * 2;
            int startY = (getHeight() - totalHeight) / 2 + fm.getAscent();

            g2.drawString(line1, textX1, startY);
            g2.drawString(line2, textX2, startY + fm.getHeight());
        } else {
            int fontSize = Math.max(20, size / 3);
            g2.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();

            int textX = (getWidth() - fm.stringWidth(displayTime)) / 2;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(displayTime, textX, textY);
        }
    }
}
