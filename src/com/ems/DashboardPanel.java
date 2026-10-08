package com.ems;

import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;
//p3
/**
 * Dashboard Panel — hotel-ops dashboard style:
 * - Greeting header with timestamp
 * - KPI stat cards with left-side colour accent border (matching reference image)
 * - Left: employee table (60%) with hint text
 * - Right: Quick Actions panel (40%) with clearly visible coloured buttons
 */
public class DashboardPanel extends JPanel {

    private final EmployeeDAO employeeDAO;
    private final MainFrame   mainFrame;

    private JLabel lblTotalCount;
    private JLabel lblItCount;
    private JLabel lblHrCount;
    private JLabel lblFinanceCount;
    private JLabel lblMarketingCount;
    private JLabel lblAvgSalary;
    private JLabel lblTableStatus;

    private JTable recentTable;
    private DefaultTableModel tableModel;

    private static final DecimalFormat CURRENCY = new DecimalFormat("$#,##0.00");
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy  •  HH:mm");

    public DashboardPanel(EmployeeDAO dao, MainFrame parentFrame) {
        this.employeeDAO = dao;
        this.mainFrame   = parentFrame;
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(22, 26, 22, 26));
        initUI();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYOUT
    // ─────────────────────────────────────────────────────────────────────────

    private void initUI() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        // 1. Greeting header
        body.add(createGreetingHeader(), BorderLayout.NORTH);

        // 2. KPI cards row + main split below
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);
        center.add(createStatCardsRow(), BorderLayout.NORTH);

        // 3. Table (left) + Quick Actions (right)
        JPanel split = new JPanel(new GridBagLayout());
        split.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.BOTH;
        g.weighty = 1.0;

        g.gridx   = 0; g.weightx = 0.61;
        g.insets  = new Insets(0, 0, 0, 16);
        split.add(createTableCard(), g);

        g.gridx   = 1; g.weightx = 0.39;
        g.insets  = new Insets(0, 0, 0, 0);
        split.add(createQuickActionsCard(), g);

        center.add(split, BorderLayout.CENTER);
        body.add(center, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GREETING HEADER
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createGreetingHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);

        // Left: greeting text
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel greeting = new JLabel("Good day, System Administrator");
        greeting.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 23));
        greeting.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel sub = new JLabel("Here is your workforce snapshot for today  •  "
                + LocalDateTime.now().format(DT_FMT));
        sub.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 13));
        sub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        left.add(greeting);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);
        panel.add(left, BorderLayout.WEST);

        // Right: action buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);

        JButton btnAdd = UIUtils.createStyledButton("+ Add Employee", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnAdd.addActionListener(e -> mainFrame.showSection("EMPLOYEES"));

        JButton btnRefresh = UIUtils.createOutlineButton("Refresh");
        btnRefresh.addActionListener(e -> refreshData());

        btns.add(btnAdd);
        btns.add(btnRefresh);
        panel.add(btns, BorderLayout.EAST);
        return panel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // KPI STAT CARDS  — left accent border style from reference image
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createStatCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 6, 10, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 88));

        row.add(buildStatCard("TOTAL EMPLOYEES", "0",     new Color(79, 70, 229),  lbl -> lblTotalCount     = lbl));
        row.add(buildStatCard("IT DEPT",         "0",     new Color(2, 132, 199),  lbl -> lblItCount        = lbl));
        row.add(buildStatCard("HR DEPT",         "0",     new Color(124, 58, 237), lbl -> lblHrCount        = lbl));
        row.add(buildStatCard("FINANCE",         "0",     new Color(5, 150, 105),  lbl -> lblFinanceCount   = lbl));
        row.add(buildStatCard("MARKETING",       "0",     new Color(217, 119, 6),  lbl -> lblMarketingCount = lbl));
        row.add(buildStatCard("AVG SALARY",      "$0.00", new Color(220, 38, 38),  lbl -> lblAvgSalary      = lbl));

        return row;
    }

    private JPanel buildStatCard(String title, String initVal, Color accent,
                                  java.util.function.Consumer<JLabel> setter) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        // Outer thin border + thick left accent strip
        card.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1),
                new CompoundBorder(
                        new MatteBorder(0, 5, 0, 0, accent),
                        new EmptyBorder(11, 13, 11, 13)
                )
        ));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
        lblT.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JLabel lblV = new JLabel(initVal);
        lblV.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 26));
        lblV.setForeground(UIUtils.COLOR_TEXT_MAIN);
        setter.accept(lblV);

        card.add(lblT, BorderLayout.NORTH);
        card.add(lblV, BorderLayout.CENTER);
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // EMPLOYEE TABLE (left panel)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1),
                new EmptyBorder(15, 18, 15, 18)
        ));

        // Header row
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);

        JLabel lblTitle = new JLabel("Employee Directory");
        lblTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblTableStatus = new JLabel("Loading...");
        lblTableStatus.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        lblTableStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);

        titleBlock.add(lblTitle);
        titleBlock.add(Box.createVerticalStrut(2));
        titleBlock.add(lblTableStatus);

        JButton btnOpen = UIUtils.createStyledButton("View All & Manage", UIUtils.COLOR_PRIMARY_LIGHT, UIUtils.COLOR_PRIMARY);
        btnOpen.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        btnOpen.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnOpen.addActionListener(e -> mainFrame.showSection("EMPLOYEES"));

        hdr.add(titleBlock, BorderLayout.WEST);
        hdr.add(btnOpen,    BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        // JSeparator
        JSeparator sep = new JSeparator();
        sep.setForeground(UIUtils.COLOR_BORDER);
        card.add(sep, BorderLayout.BEFORE_FIRST_LINE);

        // JTable
        String[] cols = {"Employee ID", "Full Name", "Department", "Designation", "Salary"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        recentTable = new JTable(tableModel);
        UIUtils.styleTable(recentTable);
        recentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) mainFrame.showSection("EMPLOYEES");
            }
        });

        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Bottom hint
        JLabel hint = new JLabel("Double-click any row to open the employee editor");
        hint.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        hint.setForeground(UIUtils.COLOR_TEXT_MUTED);
        hint.setBorder(new EmptyBorder(6, 0, 0, 0));
        card.add(hint, BorderLayout.SOUTH);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUICK ACTIONS (right panel)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createQuickActionsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setOpaque(true);
        card.setBorder(new CompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        // Title
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Quick actions");
        lblTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        lblTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        titleRow.add(lblTitle, BorderLayout.WEST);

        // Separator under title
        JSeparator sep = new JSeparator();
        sep.setForeground(UIUtils.COLOR_BORDER);

        JPanel topBlock = new JPanel(new BorderLayout(0, 10));
        topBlock.setOpaque(false);
        topBlock.add(titleRow, BorderLayout.NORTH);
        topBlock.add(sep, BorderLayout.SOUTH);
        card.add(topBlock, BorderLayout.NORTH);

        // Button list
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));
        btns.setOpaque(false);

        btns.add(buildActionBtn("Add new employee",         new Color(13, 148, 136), e -> mainFrame.showSection("EMPLOYEES")));
        btns.add(Box.createVerticalStrut(10));
        btns.add(buildActionBtn("View all employees",       new Color(30, 41, 59),   e -> mainFrame.showSection("EMPLOYEES")));
        btns.add(Box.createVerticalStrut(10));
        btns.add(buildActionBtn("Department analytics",     new Color(109, 40, 217), e -> mainFrame.showSection("DEPARTMENTS")));
        btns.add(Box.createVerticalStrut(10));
        btns.add(buildActionBtn("Compensation reports",     new Color(202, 97, 11),  e -> mainFrame.showSection("REPORTS")));
        btns.add(Box.createVerticalStrut(10));
        btns.add(buildActionBtn("Search / filter employees", new Color(59, 59, 210),  e -> mainFrame.showSection("EMPLOYEES")));
        btns.add(Box.createVerticalStrut(10));
        btns.add(buildActionBtn("Refresh dashboard",        new Color(22, 101, 52),  e -> refreshData()));

        card.add(btns, BorderLayout.CENTER);

        // DB config hint at bottom
        JLabel dbHint = new JLabel("Use 'DB Config' in the top bar to set your MySQL password");
        dbHint.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        dbHint.setForeground(UIUtils.COLOR_TEXT_MUTED);
        dbHint.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(10, 0, 0, 0)
        ));
        card.add(dbHint, BorderLayout.SOUTH);
        return card;
    }

    /**
     * Creates a full-width Quick Action button.
     * Uses BasicButtonUI to guarantee text visibility on all platforms.
     */
    private JButton buildActionBtn(String label, Color bg, java.awt.event.ActionListener action) {
        UIUtils.RoundedButton btn = new UIUtils.RoundedButton(label, bg, Color.WHITE, null, 14);
        btn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(12, 18, 12, 18));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.addActionListener(action);
        return btn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA REFRESH
    // ─────────────────────────────────────────────────────────────────────────

    public void refreshData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            int total = 0, it = 0, hr = 0, fin = 0, mkt = 0;
            double avg = 0;
            List<Employee> list;

            @Override
            protected Void doInBackground() {
                try {
                    total = employeeDAO.getTotalEmployeeCount();
                    Map<String, Integer> counts = employeeDAO.getDepartmentCounts();
                    it    = counts.getOrDefault("IT", 0);
                    hr    = counts.getOrDefault("HR", 0);
                    fin   = counts.getOrDefault("Finance", 0);
                    mkt   = counts.getOrDefault("Marketing", 0);
                    avg   = employeeDAO.getAverageSalary();
                    list  = employeeDAO.getAllEmployees();
                } catch (SQLException e) {
                    System.err.println("Dashboard refresh error: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                if (lblTotalCount     != null) lblTotalCount.setText(String.valueOf(total));
                if (lblItCount        != null) lblItCount.setText(String.valueOf(it));
                if (lblHrCount        != null) lblHrCount.setText(String.valueOf(hr));
                if (lblFinanceCount   != null) lblFinanceCount.setText(String.valueOf(fin));
                if (lblMarketingCount != null) lblMarketingCount.setText(String.valueOf(mkt));
                if (lblAvgSalary      != null) lblAvgSalary.setText(CURRENCY.format(avg));

                tableModel.setRowCount(0);
                if (list != null) {
                    int n = 0;
                    for (Employee emp : list) {
                        tableModel.addRow(new Object[]{
                                emp.getEmployeeId(), emp.getName(), emp.getDepartment(),
                                emp.getDesignation(), emp.getSalary()
                        });
                        if (++n >= 25) break;
                    }
                    if (lblTableStatus != null)
                        lblTableStatus.setText("Showing " + Math.min(n, list.size())
                                + " of " + list.size() + " employees  •  select and double-click to edit");
                }
            }
        };
        worker.execute();
    }
}
