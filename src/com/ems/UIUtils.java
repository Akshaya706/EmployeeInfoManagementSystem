package com.ems;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
//d
/**
 * Modern design system with rounded, custom-painted buttons for EMS.
 * All buttons are pill-shaped with smooth hover transitions and crisp antialiased rendering.
 */
public class UIUtils {

    // --- Color Palette ---
    public static final Color COLOR_PRIMARY        = new Color(79, 70, 229);
    public static final Color COLOR_PRIMARY_HOVER  = new Color(67, 56, 202);
    public static final Color COLOR_PRIMARY_LIGHT  = new Color(238, 242, 255);

    public static final Color COLOR_SIDEBAR        = new Color(15, 23, 42);
    public static final Color COLOR_SIDEBAR_CARD   = new Color(30, 41, 59);
    public static final Color COLOR_SIDEBAR_HOVER  = new Color(30, 41, 59);
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(79, 70, 229);
    public static final Color COLOR_SIDEBAR_TEXT   = new Color(248, 250, 252);
    public static final Color COLOR_SIDEBAR_MUTED  = new Color(148, 163, 184);
    public static final Color COLOR_SIDEBAR_BORDER = new Color(30, 41, 59);

    public static final Color COLOR_BG             = new Color(245, 247, 250);
    public static final Color COLOR_HEADER_BG      = Color.WHITE;
    public static final Color COLOR_CARD_BG        = Color.WHITE;
    public static final Color COLOR_BORDER         = new Color(220, 226, 236);
    public static final Color COLOR_BORDER_LIGHT   = new Color(241, 245, 249);
    public static final Color COLOR_BORDER_FOCUS   = new Color(99, 102, 241);
    public static final Color COLOR_TEXT_MAIN      = new Color(15, 23, 42);
    public static final Color COLOR_TEXT_MUTED     = new Color(100, 116, 139);
    public static final Color COLOR_INPUT_BG       = Color.WHITE;

    public static final Color COLOR_SUCCESS        = new Color(16, 185, 129);
    public static final Color COLOR_SUCCESS_LIGHT  = new Color(236, 253, 245);
    public static final Color COLOR_DANGER         = new Color(239, 68, 68);
    public static final Color COLOR_DANGER_LIGHT   = new Color(254, 242, 242);
    public static final Color COLOR_WARNING        = new Color(245, 158, 11);
    public static final Color COLOR_WARNING_LIGHT  = new Color(254, 243, 199);
    public static final Color COLOR_INFO           = new Color(2, 132, 199);
    public static final Color COLOR_INFO_LIGHT     = new Color(240, 249, 255);
    public static final Color COLOR_PURPLE         = new Color(139, 92, 246);
    public static final Color COLOR_PURPLE_LIGHT   = new Color(245, 243, 255);

    public static final Color COLOR_DEPT_IT        = new Color(2, 132, 199);
    public static final Color COLOR_DEPT_IT_LIGHT  = new Color(224, 242, 254);
    public static final Color COLOR_DEPT_HR        = new Color(124, 58, 237);
    public static final Color COLOR_DEPT_HR_LIGHT  = new Color(237, 233, 254);
    public static final Color COLOR_DEPT_FIN       = new Color(5, 150, 105);
    public static final Color COLOR_DEPT_FIN_LIGHT = new Color(209, 250, 229);
    public static final Color COLOR_DEPT_MKT       = new Color(217, 119, 6);
    public static final Color COLOR_DEPT_MKT_LIGHT = new Color(254, 243, 199);

    // --- Typography ---
    public static final String FONT_FAMILY     = getSystemFontFamily();
    public static final Font FONT_APP_TITLE    = new Font(FONT_FAMILY, Font.BOLD, 15);
    public static final Font FONT_TITLE        = new Font(FONT_FAMILY, Font.BOLD, 20);
    public static final Font FONT_SUBTITLE     = new Font(FONT_FAMILY, Font.PLAIN, 12);
    public static final Font FONT_CARD_TITLE   = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_CARD_VALUE   = new Font(FONT_FAMILY, Font.BOLD, 26);
    public static final Font FONT_FORM_LABEL   = new Font(FONT_FAMILY, Font.BOLD, 12);
    public static final Font FONT_BODY         = new Font(FONT_FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD    = new Font(FONT_FAMILY, Font.BOLD, 13);
    public static final Font FONT_SMALL        = new Font(FONT_FAMILY, Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD   = new Font(FONT_FAMILY, Font.BOLD, 11);
    public static final Font FONT_TABLE_HEADER = new Font(FONT_FAMILY, Font.BOLD, 12);

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₹#,##0.00");

    private static String getSystemFontFamily() {
        String[] preferred = {"Segoe UI", "Inter", "Helvetica Neue", "Arial", "SansSerif"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        for (String pref : preferred) {
            for (String avail : available) {
                if (avail.equalsIgnoreCase(pref)) return avail;
            }
        }
        return "SansSerif";
    }

    // =========================================================================
    // ROUNDED BUTTON — Custom-painted pill button with antialiased corners
    // =========================================================================

    /**
     * A JButton that paints its own rounded background, ignoring the L&F.
     * Guarantees consistent appearance on Windows, macOS, and Linux.
     */
    public static class RoundedButton extends JButton {
        private Color bgColor;
        private Color hoverColor;
        private Color pressedColor;
        private Color borderColor;
        private final Color fgColor;
        private final int arc;
        private boolean hovered = false;
        private boolean pressed = false;

        public RoundedButton(String text, Color bg, Color fg, int arc) {
            this(text, bg, fg, null, arc);
        }

        public RoundedButton(String text, Color bg, Color fg, Color border, int arc) {
            super(text);
            this.bgColor      = bg;
            this.hoverColor   = bg.equals(Color.WHITE) ? new Color(241, 245, 249) : darkenColor(bg, 0.88f);
            this.pressedColor = bg.equals(Color.WHITE) ? new Color(226, 232, 240) : darkenColor(bg, 0.78f);
            this.borderColor  = border;
            this.fgColor      = fg;
            this.arc          = arc;
            setForeground(fg);
            setFont(FONT_BODY_BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 22, 10, 22));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true; repaint();
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false; pressed = false; repaint();
                }
                @Override public void mousePressed(java.awt.event.MouseEvent e) {
                    pressed = true; repaint();
                }
                @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                    pressed = false; repaint();
                }
            });
        }

        @Override
        public void setBackground(Color bg) {
            this.bgColor      = bg;
            this.hoverColor   = bg.equals(Color.WHITE) ? new Color(241, 245, 249) : darkenColor(bg, 0.88f);
            this.pressedColor = bg.equals(Color.WHITE) ? new Color(226, 232, 240) : darkenColor(bg, 0.78f);
            repaint();
        }

        public void setBorderColor(Color border) {
            this.borderColor = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color fill;
            if (!isEnabled()) {
                fill = new Color(226, 232, 240);
            } else if (pressed) {
                fill = pressedColor;
            } else if (hovered) {
                fill = hoverColor;
            } else {
                fill = bgColor;
            }

            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));

            if (borderColor != null) {
                g2.setColor(isEnabled() ? (hovered ? COLOR_BORDER_FOCUS : borderColor) : new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, arc, arc));
            } else if (isEnabled() && !bgColor.equals(Color.WHITE)) {
                // Subtle bottom shadow line for depth
                g2.setColor(darkenColor(fill, 0.82f));
                g2.drawLine(arc / 2, getHeight() - 1, getWidth() - arc / 2, getHeight() - 1);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Creates a rounded filled action button.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        return new RoundedButton(text, bgColor, fgColor, 22);
    }

    /**
     * Creates a primary (indigo) rounded button.
     */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_PRIMARY, Color.WHITE);
    }

    /**
     * Creates a success (emerald green) rounded button.
     */
    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_SUCCESS, Color.WHITE);
    }

    /**
     * Creates a rounded outline button with a crisp border.
     */
    public static JButton createOutlineButton(String text) {
        RoundedButton btn = new RoundedButton(text, Color.WHITE, COLOR_TEXT_MAIN, COLOR_BORDER, 22);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    /**
     * Creates a danger (red) rounded button.
     */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, COLOR_DANGER, Color.WHITE);
    }

    // =========================================================================
    // CARD FACTORIES
    // =========================================================================

    public static RoundedPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(10, COLOR_CARD_BG, COLOR_BORDER, 1, false);
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        return panel;
    }

    public static RoundedPanel createElevatedCardPanel() {
        RoundedPanel panel = new RoundedPanel(10, COLOR_CARD_BG, COLOR_BORDER, 1, true);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        return panel;
    }

    // =========================================================================
    // FORM INPUT FACTORIES
    // =========================================================================

    public static JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBackground(COLOR_INPUT_BG);
        tf.setForeground(COLOR_TEXT_MAIN);
        tf.setCaretColor(COLOR_PRIMARY);
        Border normal  = new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true),      new EmptyBorder(8, 12, 8, 12));
        Border focused = new CompoundBorder(new LineBorder(COLOR_BORDER_FOCUS, 2, true), new EmptyBorder(7, 11, 7, 11));
        tf.setBorder(normal);
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.setBorder(focused); }
            @Override public void focusLost(FocusEvent e)   { tf.setBorder(normal);  }
        });
        return tf;
    }

    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(FONT_BODY);
        pf.setBackground(COLOR_INPUT_BG);
        pf.setForeground(COLOR_TEXT_MAIN);
        pf.setCaretColor(COLOR_PRIMARY);
        Border normal  = new CompoundBorder(new LineBorder(COLOR_BORDER, 1, true),      new EmptyBorder(8, 12, 8, 12));
        Border focused = new CompoundBorder(new LineBorder(COLOR_BORDER_FOCUS, 2, true), new EmptyBorder(7, 11, 7, 11));
        pf.setBorder(normal);
        pf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { pf.setBorder(focused); }
            @Override public void focusLost(FocusEvent e)   { pf.setBorder(normal);  }
        });
        return pf;
    }

    public static <T> JComboBox<T> createStyledComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(COLOR_INPUT_BG);
        combo.setForeground(COLOR_TEXT_MAIN);
        combo.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(4, 6, 4, 6)
        ));
        return combo;
    }

    // =========================================================================
    // TABLE STYLING
    // =========================================================================

    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                String colName = t.getColumnName(col).toLowerCase();
                String text    = val != null ? val.toString() : "";

                if (colName.contains("department")) {
                    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
                    panel.setOpaque(true);
                    panel.setBackground(isSelected ? new Color(238, 242, 255)
                            : (row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251)));
                    JLabel pill = createPillBadge(text, getDeptLightColor(text), getDeptColor(text));
                    panel.add(pill);
                    return panel;
                }

                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 14, 0, 14));

                if (colName.contains("salary") || colName.contains("payroll") || colName.contains("compensation")) {
                    if (val instanceof Number)
                        setText(CURRENCY_FORMAT.format(((Number) val).doubleValue()));
                    else if (text.matches("^\\d+(\\.\\d+)?$")) {
                        try { setText(CURRENCY_FORMAT.format(Double.parseDouble(text))); }
                        catch (Exception ignored) { setText(text); }
                    }
                    setFont(new Font(FONT_FAMILY, Font.BOLD, 13));
                    setForeground(new Color(5, 150, 105));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else if (colName.contains("id")) {
                    setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
                    setForeground(new Color(79, 70, 229));
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    setFont(FONT_BODY);
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                    if (!colName.contains("salary") && !colName.contains("payroll") && !colName.contains("id"))
                        c.setForeground(COLOR_TEXT_MAIN);
                } else {
                    c.setBackground(new Color(238, 242, 255));
                    c.setForeground(COLOR_TEXT_MAIN);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    public static Color getDeptColor(String dept) {
        if (dept == null) return COLOR_TEXT_MUTED;
        switch (dept.trim().toUpperCase()) {
            case "IT":        return COLOR_DEPT_IT;
            case "HR":        return COLOR_DEPT_HR;
            case "FINANCE":   return COLOR_DEPT_FIN;
            case "MARKETING": return COLOR_DEPT_MKT;
            default:          return COLOR_TEXT_MUTED;
        }
    }

    public static Color getDeptLightColor(String dept) {
        if (dept == null) return COLOR_BORDER_LIGHT;
        switch (dept.trim().toUpperCase()) {
            case "IT":        return COLOR_DEPT_IT_LIGHT;
            case "HR":        return COLOR_DEPT_HR_LIGHT;
            case "FINANCE":   return COLOR_DEPT_FIN_LIGHT;
            case "MARKETING": return COLOR_DEPT_MKT_LIGHT;
            default:          return COLOR_BORDER_LIGHT;
        }
    }

    public static JLabel createPillBadge(String text, Color bgColor, Color fgColor) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(FONT_SMALL_BOLD);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setForeground(fgColor);
        badge.setBorder(new CompoundBorder(
                new LineBorder(fgColor, 1, true),
                new EmptyBorder(3, 10, 3, 10)
        ));
        return badge;
    }

    public static JPanel createMetricCard(String title, String value, String subtitle,
            String iconSymbol, Color accentColor, Color accentLightColor) {
        RoundedPanel card = new RoundedPanel(10, COLOR_CARD_BG, COLOR_BORDER, 1, false);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        JLabel lblT = new JLabel(title.toUpperCase());
        lblT.setFont(FONT_CARD_TITLE);
        lblT.setForeground(COLOR_TEXT_MUTED);
        topRow.add(lblT, BorderLayout.WEST);

        JLabel lblI = new JLabel(iconSymbol, SwingConstants.CENTER);
        lblI.setFont(new Font(FONT_FAMILY, Font.BOLD, 12));
        lblI.setForeground(accentColor);
        lblI.setOpaque(true);
        lblI.setBackground(accentLightColor);
        lblI.setPreferredSize(new Dimension(30, 30));
        lblI.setBorder(new LineBorder(accentColor, 1, true));
        topRow.add(lblI, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        JLabel lblV = new JLabel(value);
        lblV.setFont(FONT_CARD_VALUE);
        lblV.setForeground(COLOR_TEXT_MAIN);
        card.add(lblV, BorderLayout.CENTER);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblS = new JLabel(subtitle);
            lblS.setFont(FONT_SMALL);
            lblS.setForeground(COLOR_TEXT_MUTED);
            card.add(lblS, BorderLayout.SOUTH);
        }
        return card;
    }

    public static Color darkenColor(Color c, float factor) {
        return new Color(
                Math.max((int)(c.getRed()   * factor), 0),
                Math.max((int)(c.getGreen() * factor), 0),
                Math.max((int)(c.getBlue()  * factor), 0),
                c.getAlpha()
        );
    }
}
