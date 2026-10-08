package com.ems;

import java.awt.*;
import java.text.DecimalFormat;
import javax.swing.*;

/**
 * A custom visual chart component rendered with Java2D antialiased graphics.
 * Displays horizontal department workforce distribution bars with percentage shares,
 * headcount counts, and modern SaaS rounded styling.
 */
public class DepartmentBarChart extends JPanel {

    private int itCount = 0;
    private int hrCount = 0;
    private int finCount = 0;
    private int mktCount = 0;

    private static final DecimalFormat PCT_FORMAT = new DecimalFormat("0.0%");

    private static class DeptItem {
        final String name;
        final String role;
        final int count;
        final Color barColor;

        DeptItem(String name, String role, int count, Color barColor) {
            this.name = name;
            this.role = role;
            this.count = count;
            this.barColor = barColor;
        }
    }

    public DepartmentBarChart() {
        setOpaque(false);
        setPreferredSize(new Dimension(320, 220));
        setMinimumSize(new Dimension(240, 180));
    }

    public void setDepartmentData(int it, int hr, int finance, int marketing) {
        this.itCount = it;
        this.hrCount = hr;
        this.finCount = finance;
        this.mktCount = marketing;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int total = itCount + hrCount + finCount + mktCount;

        DeptItem[] items = new DeptItem[]{
                new DeptItem("IT & Software", "Engineers & DevOps", itCount, UIUtils.COLOR_DEPT_IT),
                new DeptItem("Human Resources", "Talent & Culture", hrCount, UIUtils.COLOR_DEPT_HR),
                new DeptItem("Finance & Ops", "Accounting & Payroll", finCount, UIUtils.COLOR_DEPT_FIN),
                new DeptItem("Marketing & Sales", "Growth & Client Relations", mktCount, UIUtils.COLOR_DEPT_MKT)
        };

        int width = getWidth();
        int height = getHeight();
        int paddingX = 14;
        int paddingY = 8;
        int availableHeight = height - (paddingY * 2);
        int rowHeight = availableHeight / items.length;

        for (int i = 0; i < items.length; i++) {
            DeptItem item = items[i];
            int rowY = paddingY + (i * rowHeight);

            // Row 1: Department Color Dot + Title + Count & Percentage
            int dotSize = 9;
            int textY = rowY + 14;

            g2.setColor(item.barColor);
            g2.fillOval(paddingX, textY - 8, dotSize, dotSize);

            // Department Name
            g2.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
            g2.setColor(UIUtils.COLOR_TEXT_MAIN);
            g2.drawString(item.name, paddingX + dotSize + 8, textY);

            // Count & Percentage Label
            double ratio = total > 0 ? (double) item.count / total : 0.0;
            String statsText = item.count + " staff (" + PCT_FORMAT.format(ratio) + ")";

            g2.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
            g2.setColor(UIUtils.COLOR_TEXT_MUTED);
            FontMetrics fm = g2.getFontMetrics();
            int statsWidth = fm.stringWidth(statsText);
            g2.drawString(statsText, width - paddingX - statsWidth, textY);

            // Row 2: Progress Track & Fill Bar
            int barY = rowY + 22;
            int barHeight = 8;
            int barWidth = width - (paddingX * 2);

            // Background Track
            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(paddingX, barY, barWidth, barHeight, 6, 6);

            // Fill Bar
            int fillWidth = total > 0 ? (int) Math.round(barWidth * ratio) : 0;
            if (fillWidth > 0) {
                // Ensure small minimum width if count > 0 for visibility
                fillWidth = Math.max(fillWidth, 8);
                fillWidth = Math.min(fillWidth, barWidth);
                g2.setColor(item.barColor);
                g2.fillRoundRect(paddingX, barY, fillWidth, barHeight, 6, 6);
            }
        }

        g2.dispose();
    }
}
