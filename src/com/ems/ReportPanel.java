package com.ems;

import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
//c
/**
 * Reports Panel:
 * Resembles a modern executive financial and compensation dashboard.
 * Generates summary KPI metrics (Total Employees, Average Salary, Highest Salary,
 * Lowest Salary, Gross Payroll) computed via ANSI SQL aggregate operations.
 */
public class ReportPanel extends JPanel {

    private final EmployeeDAO employeeDAO;
    private final MainFrame mainFrame;

    private JLabel lblTotalEmployees;
    private JLabel lblTotalPayroll;
    private JLabel lblAvgSalary;
    private JLabel lblMaxSalary;
    private JLabel lblMinSalary;

    private JTable reportTable;
    private DefaultTableModel reportTableModel;

    private JLabel lblSalarySpread;
    private JLabel lblHighestDept;
    private JLabel lblCostPerHead;
    private JLabel lblAuditTimestamp;

    private static final DecimalFormat CURRENCY = new DecimalFormat("₹#,##0.00");
    private static final DecimalFormat PERCENT = new DecimalFormat("0.0%");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy • HH:mm:ss");

    public ReportPanel(EmployeeDAO dao, MainFrame parentFrame) {
        this.employeeDAO = dao;
        this.mainFrame = parentFrame;

        setLayout(new BorderLayout(0, 18));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(18, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // --- 1. Top Banner ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Compensation & Executive Reports");
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 17));
        titleLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel subtitleLabel = new JLabel("Aggregate compensation metrics computed dynamically via SQL aggregate operations");
        subtitleLabel.setFont(UIUtils.FONT_SMALL);
        subtitleLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JButton btnRefresh = UIUtils.createOutlineButton("Recalculate");
        btnRefresh.addActionListener(e -> refreshData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Content Panel ---
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);

        // 5 KPI Cards Grid
        JPanel kpiGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setPreferredSize(new Dimension(0, 114));

        kpiGrid.add(createKpiCard("TOTAL STAFF", "0", "Active Records", "#", UIUtils.COLOR_PRIMARY, UIUtils.COLOR_PRIMARY_LIGHT, lbl -> lblTotalEmployees = lbl));
        kpiGrid.add(createKpiCard("AVERAGE SALARY", "$0.00", "Mean Compensation", "$", UIUtils.COLOR_PURPLE, UIUtils.COLOR_PURPLE_LIGHT, lbl -> lblAvgSalary = lbl));
        kpiGrid.add(createKpiCard("HIGHEST SALARY", "$0.00", "Peak Compensation", "H", UIUtils.COLOR_SUCCESS, UIUtils.COLOR_SUCCESS_LIGHT, lbl -> lblMaxSalary = lbl));
        kpiGrid.add(createKpiCard("LOWEST SALARY", "$0.00", "Base Compensation", "L", UIUtils.COLOR_WARNING, UIUtils.COLOR_WARNING_LIGHT, lbl -> lblMinSalary = lbl));
        kpiGrid.add(createKpiCard("TOTAL PAYROLL", "$0.00", "Gross Expenditure", "P", UIUtils.COLOR_INFO, UIUtils.COLOR_INFO_LIGHT, lbl -> lblTotalPayroll = lbl));

        contentPanel.add(kpiGrid, BorderLayout.NORTH);

        // Center Split: Table on Left, Statistical Analysis Card on Right
        JPanel centerGrid = new JPanel(new GridBagLayout());
        centerGrid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Table: Department-wise metrics (62%)
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 16);

        RoundedPanel tableCard = UIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JPanel tableTitleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        tableTitleBlock.setOpaque(false);
        JLabel tableTitle = new JLabel("Department Compensation Breakdown");
        tableTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        tableTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel tableSub = new JLabel("Headcount, average compensation, total payroll and budget allocation per department");
        tableSub.setFont(UIUtils.FONT_SMALL);
        tableSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        tableTitleBlock.add(tableTitle);
        tableTitleBlock.add(tableSub);
        tableCard.add(tableTitleBlock, BorderLayout.NORTH);

        String[] cols = {"Department", "Headcount", "Average Salary", "Total Payroll", "% of Total Budget"};
        reportTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reportTable = new JTable(reportTableModel);
        UIUtils.styleTable(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerGrid.add(tableCard, gbc);

        // Right Card: Key Highlights & Statistical Insights (38%)
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);

        RoundedPanel summaryCard = UIUtils.createCardPanel();
        summaryCard.setLayout(new BorderLayout(0, 14));

        JPanel summaryHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        summaryHeader.setOpaque(false);
        JLabel summaryTitle = new JLabel("Statistical Analysis");
        summaryTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        summaryTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel summarySub = new JLabel("Payroll distribution & aggregate summary");
        summarySub.setFont(UIUtils.FONT_SMALL);
        summarySub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        summaryHeader.add(summaryTitle);
        summaryHeader.add(summarySub);
        summaryCard.add(summaryHeader, BorderLayout.NORTH);

        JPanel insightsList = new JPanel(new GridLayout(5, 1, 0, 8));
        insightsList.setOpaque(false);

        lblSalarySpread = new JLabel("Salary Spread: $0.00");
        lblSalarySpread.setFont(UIUtils.FONT_BODY_BOLD);
        lblSalarySpread.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblCostPerHead = new JLabel("Mean Compensation: $0.00 / staff");
        lblCostPerHead.setFont(UIUtils.FONT_BODY_BOLD);
        lblCostPerHead.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblHighestDept = new JLabel("Largest Department: None");
        lblHighestDept.setFont(UIUtils.FONT_BODY_BOLD);
        lblHighestDept.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel lblCalcNotice = new JLabel("<html><span style='font-family: Segoe UI, sans-serif; font-size: 11px; color: #475569;'><b>Database Engine Aggregation:</b><br/>ANSI SQL aggregate functions: <code>COUNT(*)</code>, <code>AVG()</code>, <code>MAX()</code>, <code>MIN()</code>, <code>SUM()</code> executed via JDBC PreparedStatement.</span></html>");

        lblAuditTimestamp = new JLabel("Report generated dynamically.");
        lblAuditTimestamp.setFont(new Font(UIUtils.FONT_FAMILY, Font.ITALIC, 11));
        lblAuditTimestamp.setForeground(UIUtils.COLOR_TEXT_MUTED);

        insightsList.add(lblSalarySpread);
        insightsList.add(lblCostPerHead);
        insightsList.add(lblHighestDept);
        insightsList.add(lblCalcNotice);
        insightsList.add(lblAuditTimestamp);

        summaryCard.add(insightsList, BorderLayout.CENTER);
        centerGrid.add(summaryCard, gbc);

        contentPanel.add(centerGrid, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createKpiCard(String title, String val, String sub, String iconSymbol,
                                 Color accent, Color accentLight,
                                 java.util.function.Consumer<JLabel> labelConsumer) {
        RoundedPanel card = new RoundedPanel(12, UIUtils.COLOR_CARD_BG, UIUtils.COLOR_BORDER, 1);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Top Row: Title + Icon Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lblT = new JLabel(title);
        lblT.setFont(UIUtils.FONT_CARD_TITLE);
        lblT.setForeground(UIUtils.COLOR_TEXT_MUTED);
        top.add(lblT, BorderLayout.WEST);

        JLabel lblIcon = new JLabel(iconSymbol, SwingConstants.CENTER);
        lblIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 13));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(accentLight);
        lblIcon.setForeground(accent);
        lblIcon.setPreferredSize(new Dimension(28, 28));
        lblIcon.setBorder(new LineBorder(accent, 1, true));
        top.add(lblIcon, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        // Center Value
        JLabel lblV = new JLabel(val);
        lblV.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        lblV.setForeground(UIUtils.COLOR_TEXT_MAIN);
        labelConsumer.accept(lblV);
        card.add(lblV, BorderLayout.CENTER);

        // Bottom Subtitle
        JLabel lblS = new JLabel(sub);
        lblS.setFont(UIUtils.FONT_SMALL);
        lblS.setForeground(UIUtils.COLOR_TEXT_MUTED);
        card.add(lblS, BorderLayout.SOUTH);

        return card;
    }

    public void refreshData() {
        try {
            EmployeeDAO.SalaryStatistics stats = employeeDAO.getSalaryStatistics();

            lblTotalEmployees.setText(String.valueOf(stats.getCount()));
            lblTotalPayroll.setText(CURRENCY.format(stats.getTotalPayroll()));
            lblAvgSalary.setText(CURRENCY.format(stats.getAverageSalary()));
            lblMaxSalary.setText(CURRENCY.format(stats.getHighestSalary()));
            lblMinSalary.setText(CURRENCY.format(stats.getLowestSalary()));

            double spread = stats.getHighestSalary() - stats.getLowestSalary();
            lblSalarySpread.setText("<html>Salary Spread (Max - Min): <span style='color: #4f46e5;'>" + CURRENCY.format(spread) + "</span></html>");
            lblCostPerHead.setText("<html>Mean Compensation: <span style='color: #0284c7;'>" + CURRENCY.format(stats.getAverageSalary()) + "</span></html>");

            // Populate department breakdown
            List<EmployeeDAO.DepartmentStat> deptStats = employeeDAO.getDepartmentStatisticsList();
            reportTableModel.setRowCount(0);

            String topDept = "None";
            int maxHeadcount = 0;

            for (EmployeeDAO.DepartmentStat stat : deptStats) {
                if (stat.getEmployeeCount() > maxHeadcount) {
                    maxHeadcount = stat.getEmployeeCount();
                    topDept = stat.getDepartment() + " (" + maxHeadcount + " staff)";
                }

                double share = stats.getTotalPayroll() > 0 ? (stat.getTotalSalary() / stats.getTotalPayroll()) : 0.0;

                reportTableModel.addRow(new Object[]{
                        stat.getDepartment(),
                        stat.getEmployeeCount() + " Employees",
                        stat.getAvgSalary(),
                        stat.getTotalSalary(),
                        PERCENT.format(share)
                });
            }

            lblHighestDept.setText("<html>Largest Department: <span style='color: #10b981;'>" + topDept + "</span></html>");
            lblAuditTimestamp.setText("Last recalculated: " + TIME_FORMAT.format(new Date()));

        } catch (SQLException e) {
            lblTotalEmployees.setText("-");
            lblTotalPayroll.setText("-");
            lblAvgSalary.setText("-");
            lblMaxSalary.setText("-");
            lblMinSalary.setText("-");
            reportTableModel.setRowCount(0);
        }
    }
}
