package com.ems;

import java.awt.*;
import javax.swing.*;

/**
 * A modern Swing JPanel rendering with antialiased rounded corners,
 * optional subtle soft drop-shadow, and customizable background/border colors.
 * Used across the EMS application to create web-SaaS card containers.
 */
public class RoundedPanel extends JPanel {

    private int cornerRadius;
    private Color backgroundColor;
    private Color borderColor;
    private int borderWidth;
    private boolean drawShadow = false;

    public RoundedPanel(int radius, Color bgColor) {
        this(radius, bgColor, UIUtils.COLOR_BORDER, 1);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderWidth) {
        this(radius, bgColor, borderColor, borderWidth, false);
    }

    public RoundedPanel(int radius, Color bgColor, Color borderColor, int borderWidth, boolean drawShadow) {
        super();
        this.cornerRadius = radius;
        this.backgroundColor = bgColor;
        this.borderColor = borderColor;
        this.borderWidth = borderWidth;
        this.drawShadow = drawShadow;
        setOpaque(false);
    }

    public void setBackgroundColor(Color bgColor) {
        this.backgroundColor = bgColor;
        repaint();
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int w = getWidth();
        int h = getHeight();

        int shadowOffset = drawShadow ? 2 : 0;
        int cardW = w - (drawShadow ? 4 : 1);
        int cardH = h - (drawShadow ? 4 : 1);

        // Optional soft drop shadow simulation
        if (drawShadow) {
            g2.setColor(new Color(15, 23, 42, 10)); // Ultra soft outer shadow
            g2.fillRoundRect(2, 3, cardW, cardH, cornerRadius, cornerRadius);
            g2.setColor(new Color(15, 23, 42, 16)); // Soft inner shadow
            g2.fillRoundRect(1, 2, cardW, cardH, cornerRadius, cornerRadius);
        }

        // Draw card background
        g2.setColor(backgroundColor != null ? backgroundColor : getBackground());
        g2.fillRoundRect(0, 0, cardW, cardH, cornerRadius, cornerRadius);

        // Draw card border
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawRoundRect(0, 0, cardW, cardH, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
