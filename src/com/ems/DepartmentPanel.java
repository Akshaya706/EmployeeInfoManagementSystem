package com.ems;

import java.awt.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
//a
/**
 * Department Analytics Panel:
 * Resembles a modern web SaaS resource distribution dashboard.
 * Displays department metric cards, visual workforce ratio bars,
 * and an interactive department-personnel drilldown table.
 */
public class DepartmentPanel extends JPanel {

    private final EmployeeDAO employeeDAO;
    private final MainFrame mainFrame;

    private JLabel lblItCount;
    private JLabel lblHrCount;
    private JLabel lblFinCount;
    private JLabel lblMktCount;

    private JLabel lblItShare;
    private JLabel lblHrShare;
    private JLabel lblFinShare;
    private JLabel lblMktShare;

    // Visual distribution progress meters
    private JProgressBar barIt;
    private JProgressBar barHr;
    private JProgressBar barFin;
    private JProgressBar barMkt;

    private JTable deptTable;
    private DefaultTableModel deptTableModel;

    private JTable deptEmployeesTable;
    private DefaultTableModel deptEmployeesTableModel;
    private JLabel lblSelectedDept;

    private static final DecimalFormat CURRENCY = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat PERCENT = new DecimalFormat("0.0%");

    public DepartmentPanel(EmployeeDAO dao, MainFrame parentFrame) {
        this.employeeDAO = dao;
        this.mainFrame = parentFrame;

        setLayout(new BorderLayout(0, 18));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(18, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // --- 1. Top Section: Banner & Refresh ---
        JPanel topBanner = new JPanel(new BorderLayout());
        topBanner.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Department Analytics & Resource Allocation");
        titleLabel.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 17));
        titleLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel subtitleLabel = new JLabel("Workforce allocation, department headcount, and compensation metrics");
        subtitleLabel.setFont(UIUtils.FONT_SMALL);
        subtitleLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);
        topBanner.add(titleBlock, BorderLayout.WEST);

        JButton btnRefresh = UIUtils.createOutlineButton("Refresh Data");
        btnRefresh.addActionListener(e -> refreshData());
        topBanner.add(btnRefresh, BorderLayout.EAST);

        add(topBanner, BorderLayout.NORTH);

        // --- 2. Main Content ---
        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);

        // 4 Department Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setPreferredSize(new Dimension(0, 126));

        cardsGrid.add(createDeptCard("IT & Software", "Engineers & Tech", "IT", UIUtils.COLOR_DEPT_IT, UIUtils.COLOR_DEPT_IT_LIGHT,
                lbl -> lblItCount = lbl, lbl -> lblItShare = lbl, bar -> barIt = bar));

        cardsGrid.add(createDeptCard("Human Resources", "People & Culture", "HR", UIUtils.COLOR_DEPT_HR, UIUtils.COLOR_DEPT_HR_LIGHT,
                lbl -> lblHrCount = lbl, lbl -> lblHrShare = lbl, bar -> barHr = bar));

        cardsGrid.add(createDeptCard("Finance & Ops", "Accounting & Billing", "FN", UIUtils.COLOR_DEPT_FIN, UIUtils.COLOR_DEPT_FIN_LIGHT,
                lbl -> lblFinCount = lbl, lbl -> lblFinShare = lbl, bar -> barFin = bar));

        cardsGrid.add(createDeptCard("Marketing & Sales", "Growth & Outreach", "MK", UIUtils.COLOR_DEPT_MKT, UIUtils.COLOR_DEPT_MKT_LIGHT,
                lbl -> lblMktCount = lbl, lbl -> lblMktShare = lbl, bar -> barMkt = bar));

        contentPanel.add(cardsGrid, BorderLayout.NORTH);

        // Split Grid for Department Aggregation Table & Employees in Department
        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesGrid.setOpaque(false);

        // Left Table: Department Aggregation
        RoundedPanel leftCard = UIUtils.createCardPanel();
        leftCard.setLayout(new BorderLayout(0, 12));

        JPanel leftTitleRow = new JPanel(new GridLayout(2, 1, 0, 2));
        leftTitleRow.setOpaque(false);
        JLabel leftTitle = new JLabel("Department Summary");
        leftTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        leftTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel leftSub = new JLabel("Select any row to inspect team members in that unit");
        leftSub.setFont(UIUtils.FONT_SMALL);
        leftSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        leftTitleRow.add(leftTitle);
        leftTitleRow.add(leftSub);
        leftCard.add(leftTitleRow, BorderLayout.NORTH);

        String[] deptCols = {"Department", "Headcount", "Average Salary", "Total Payroll"};
        deptTableModel = new DefaultTableModel(deptCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        deptTable = new JTable(deptTableModel);
        UIUtils.styleTable(deptTable);
        deptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Clicking a department row filters the right employee table
        deptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = deptTable.getSelectedRow();
                if (row >= 0) {
                    String dept = (String) deptTableModel.getValueAt(row, 0);
                    loadEmployeesForDept(dept);
                }
            }
        });

        JScrollPane leftScroll = new JScrollPane(deptTable);
        leftScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        leftScroll.getViewport().setBackground(Color.WHITE);
        leftCard.add(leftScroll, BorderLayout.CENTER);

        // Right Table: Employees in selected department
        RoundedPanel rightCard = UIUtils.createCardPanel();
        rightCard.setLayout(new BorderLayout(0, 12));

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);

        lblSelectedDept = new JLabel("Team Members (All Departments)");
        lblSelectedDept.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        lblSelectedDept.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JButton btnClearFilter = UIUtils.createOutlineButton("Show All");
        btnClearFilter.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        btnClearFilter.setBorder(new EmptyBorder(5, 12, 5, 12));
        btnClearFilter.addActionListener(e -> {
            deptTable.clearSelection();
            loadEmployeesForDept(null);
        });

        rightHeader.add(lblSelectedDept, BorderLayout.WEST);
        rightHeader.add(btnClearFilter, BorderLayout.EAST);
        rightCard.add(rightHeader, BorderLayout.NORTH);

        String[] empCols = {"Employee ID", "Full Name", "Designation", "Salary"};
        deptEmployeesTableModel = new DefaultTableModel(empCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        deptEmployeesTable = new JTable(deptEmployeesTableModel);
        UIUtils.styleTable(deptEmployeesTable);

        JScrollPane rightScroll = new JScrollPane(deptEmployeesTable);
        rightScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        rightScroll.getViewport().setBackground(Color.WHITE);
        rightCard.add(rightScroll, BorderLayout.CENTER);

        tablesGrid.add(leftCard);
        tablesGrid.add(rightCard);

        contentPanel.add(tablesGrid, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createDeptCard(String name, String subtitle, String iconSymbol,
                                  Color accent, Color accentLight,
                                  java.util.function.Consumer<JLabel> countConsumer,
                                  java.util.function.Consumer<JLabel> shareConsumer,
                                  java.util.function.Consumer<JProgressBar> barConsumer) {
        RoundedPanel card = new RoundedPanel(12, UIUtils.COLOR_CARD_BG, UIUtils.COLOR_BORDER, 1);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Top Row: Title + Icon Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblName = new JLabel(name.toUpperCase());
        lblName.setFont(UIUtils.FONT_CARD_TITLE);
        lblName.setForeground(UIUtils.COLOR_TEXT_MUTED);
        topRow.add(lblName, BorderLayout.WEST);

        JLabel lblIcon = new JLabel(iconSymbol, SwingConstants.CENTER);
        lblIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 13));
        lblIcon.setOpaque(true);
        lblIcon.setBackground(accentLight);
        lblIcon.setForeground(accent);
        lblIcon.setPreferredSize(new Dimension(28, 28));
        lblIcon.setBorder(new LineBorder(accent, 1, true));
        topRow.add(lblIcon, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Center Value
        JLabel lblCount = new JLabel("0 Staff");
        lblCount.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        lblCount.setForeground(UIUtils.COLOR_TEXT_MAIN);
        countConsumer.accept(lblCount);
        card.add(lblCount, BorderLayout.CENTER);

        // Bottom Container: Visual Ratio Progress Bar + Share Label
        JPanel bottomContainer = new JPanel(new GridLayout(2, 1, 0, 4));
        bottomContainer.setOpaque(false);

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setPreferredSize(new Dimension(0, 6));
        bar.setForeground(accent);
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorderPainted(false);
        barConsumer.accept(bar);

        JLabel lblShare = new JLabel("0.0% of total workforce");
        lblShare.setFont(UIUtils.FONT_SMALL);
        lblShare.setForeground(UIUtils.COLOR_TEXT_MUTED);
        shareConsumer.accept(lblShare);

        bottomContainer.add(bar);
        bottomContainer.add(lblShare);

        card.add(bottomContainer, BorderLayout.SOUTH);
        return card;
    }

    public void refreshData() {
        try {
            Map<String, Integer> counts = employeeDAO.getDepartmentCounts();
            int it = counts.getOrDefault("IT", 0);
            int hr = counts.getOrDefault("HR", 0);
            int fin = counts.getOrDefault("Finance", 0);
            int mkt = counts.getOrDefault("Marketing", 0);

            int total = 0;
            for (int c : counts.values()) total += c;

            lblItCount.setText(it + " Personnel");
            lblHrCount.setText(hr + " Personnel");
            lblFinCount.setText(fin + " Personnel");
            lblMktCount.setText(mkt + " Personnel");

            if (total > 0) {
                int itPct = (int) Math.round(((double) it / total) * 100);
                int hrPct = (int) Math.round(((double) hr / total) * 100);
                int finPct = (int) Math.round(((double) fin / total) * 100);
                int mktPct = (int) Math.round(((double) mkt / total) * 100);

                barIt.setValue(itPct);
                barHr.setValue(hrPct);
                barFin.setValue(finPct);
                barMkt.setValue(mktPct);

                lblItShare.setText(PERCENT.format((double) it / total) + " of workforce");
                lblHrShare.setText(PERCENT.format((double) hr / total) + " of workforce");
                lblFinShare.setText(PERCENT.format((double) fin / total) + " of workforce");
                lblMktShare.setText(PERCENT.format((double) mkt / total) + " of workforce");
            } else {
                barIt.setValue(0);
                barHr.setValue(0);
                barFin.setValue(0);
                barMkt.setValue(0);

                lblItShare.setText("0.0% of workforce");
                lblHrShare.setText("0.0% of workforce");
                lblFinShare.setText("0.0% of workforce");
                lblMktShare.setText("0.0% of workforce");
            }

            // Populate department breakdown table
            List<EmployeeDAO.DepartmentStat> deptStats = employeeDAO.getDepartmentStatisticsList();
            deptTableModel.setRowCount(0);
            for (EmployeeDAO.DepartmentStat stat : deptStats) {
                deptTableModel.addRow(new Object[]{
                        stat.getDepartment(),
                        stat.getEmployeeCount() + " Employees",
                        stat.getAvgSalary(),
                        stat.getTotalSalary()
                });
            }

            // Load all or selected department employees
            loadEmployeesForDept(null);

        } catch (SQLException e) {
            deptTableModel.setRowCount(0);
            deptEmployeesTableModel.setRowCount(0);
        }
    }

    private void loadEmployeesForDept(String deptFilter) {
        try {
            List<Employee> list;
            if (deptFilter != null && !deptFilter.isEmpty()) {
                list = employeeDAO.getEmployeesByDepartment(deptFilter);
                lblSelectedDept.setText("Team Members in '" + deptFilter + "' (" + list.size() + ")");
            } else {
                list = employeeDAO.getAllEmployees();
                lblSelectedDept.setText("Team Members (All Departments - " + list.size() + ")");
            }

            deptEmployeesTableModel.setRowCount(0);
            for (Employee emp : list) {
                deptEmployeesTableModel.addRow(new Object[]{
                        emp.getEmployeeId(),
                        emp.getName(),
                        emp.getDesignation(),
                        emp.getSalary()
                });
            }
        } catch (SQLException e) {
            deptEmployeesTableModel.setRowCount(0);
        }
    }
}
