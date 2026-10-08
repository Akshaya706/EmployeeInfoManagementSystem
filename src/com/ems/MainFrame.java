package com.ems;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
//p3
/**
 * Main application window — top-navbar layout inspired by modern hotel/SaaS dashboards.
 * - Row 1: Brand name, "Signed in as" label, DB Config + Sign Out buttons
 * - Row 2: Horizontal navigation tabs with gold underline on active tab
 * - Center: CardLayout content panels
 * - Bottom: Slim status bar with coloured DB connection dot
 *
 * All buttons use BasicButtonUI to guarantee text visibility on Windows L&F.
 */
public class MainFrame extends JFrame {

    private final EmployeeDAO employeeDAO;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    private DashboardPanel  dashboardPanel;
    private EmployeePanel   employeePanel;
    private DepartmentPanel departmentPanel;
    private ReportPanel     reportPanel;

    private final Map<String, JButton> navTabs = new HashMap<>();
    private String currentSection = "DASHBOARD";

    private JLabel lblStatusLeft;
    private JLabel lblDbDot;
    private JLabel lblStatusRight;

    private final String currentUsername;

    public MainFrame() {
        this("System Administrator");
    }

    public MainFrame(String username) {
        this.currentUsername = (username != null && !username.trim().isEmpty()) ? username.trim() : "System Administrator";
        this.employeeDAO = new EmployeeDAO();
        setTitle("Employee Information Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        initUI();
        initData();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYOUT ASSEMBLY
    // ─────────────────────────────────────────────────────────────────────────

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.COLOR_BG);

        root.add(createTopNavBar(),  BorderLayout.NORTH);
        root.add(createContentArea(), BorderLayout.CENTER);
        root.add(createStatusBar(),  BorderLayout.SOUTH);

        setContentPane(root);
        showSection("DASHBOARD");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOP NAVIGATION BAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createTopNavBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(17, 24, 39));   // very dark navy

        // ── Row 1: Brand + right controls ──────────────────────────────────
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(new Color(17, 24, 39));
        row1.setBorder(new EmptyBorder(12, 22, 12, 22));

        // Brand block
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setOpaque(false);

        JLabel lblTag = new JLabel("EMS  •  HR PORTAL");
        lblTag.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 11));
        lblTag.setForeground(new Color(250, 204, 21));   // amber

        JLabel lblTitle = new JLabel("Employee Management System");
        lblTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        brand.add(lblTag);
        brand.add(Box.createVerticalStrut(2));
        brand.add(lblTitle);
        row1.add(brand, BorderLayout.WEST);

        // Right controls
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel lblUser = new JLabel("Signed in as   " + currentUsername);
        lblUser.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 13));
        lblUser.setForeground(new Color(209, 213, 219));

        JButton btnDb  = makeNavBarBtn("DB Config");
        btnDb.addActionListener(e -> showDatabaseConfigDialog());

        JButton btnOut = makeNavBarBtn("Sign out");
        btnOut.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?",
                    "Confirm Sign Out", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                this.dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            }
        });

        right.add(lblUser);
        right.add(btnDb);
        right.add(btnOut);
        row1.add(right, BorderLayout.EAST);
        wrapper.add(row1, BorderLayout.NORTH);

        // ── Row 2: Navigation tabs ──────────────────────────────────────────
        JPanel tabsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabsRow.setBackground(new Color(23, 32, 50));
        tabsRow.setBorder(new EmptyBorder(0, 16, 0, 16));

        addNavTab(tabsRow, "DASHBOARD",   "Dashboard");
        addNavTab(tabsRow, "EMPLOYEES",   "Employees");
        addNavTab(tabsRow, "DEPARTMENTS", "Departments");
        addNavTab(tabsRow, "REPORTS",     "Reports");

        wrapper.add(tabsRow, BorderLayout.SOUTH);
        return wrapper;
    }

    /**
     * Makes a top-bar action button (DB Config / Sign Out).
     * Uses BasicButtonUI so text is always readable regardless of L&F.
     */
    private JButton makeNavBarBtn(String text) {
        JButton btn = new JButton(text);
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(55, 65, 81));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
                new javax.swing.border.LineBorder(new Color(75, 85, 99), 1, true),
                new EmptyBorder(6, 16, 6, 16)
        ));
        Color hover = new Color(75, 85, 99);
        Color base  = new Color(55, 65, 81);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited (java.awt.event.MouseEvent e) { btn.setBackground(base);  }
        });
        return btn;
    }

    /**
     * Adds a horizontal navigation tab to the tab row.
     */
    private void addNavTab(JPanel container, String key, String label) {
        JButton tab = new JButton(label);
        tab.setUI(new BasicButtonUI());
        tab.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 13));
        tab.setForeground(new Color(156, 163, 175));
        tab.setBackground(new Color(23, 32, 50));
        tab.setOpaque(true);
        tab.setContentAreaFilled(true);
        tab.setFocusPainted(false);
        tab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tab.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 3, 0, new Color(23, 32, 50)),
                new EmptyBorder(11, 22, 11, 22)
        ));
        tab.addActionListener((ActionEvent e) -> showSection(key));
        tab.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!key.equals(currentSection)) {
                    tab.setForeground(Color.WHITE);
                    tab.setBackground(new Color(31, 43, 65));
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!key.equals(currentSection)) {
                    tab.setForeground(new Color(156, 163, 175));
                    tab.setBackground(new Color(23, 32, 50));
                }
            }
        });
        navTabs.put(key, tab);
        container.add(tab);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONTENT AREA
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createContentArea() {
        cardLayout       = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UIUtils.COLOR_BG);

        dashboardPanel  = new DashboardPanel(employeeDAO, this);
        employeePanel   = new EmployeePanel(employeeDAO, this);
        departmentPanel = new DepartmentPanel(employeeDAO, this);
        reportPanel     = new ReportPanel(employeeDAO, this);

        mainContentPanel.add(dashboardPanel,  "DASHBOARD");
        mainContentPanel.add(employeePanel,   "EMPLOYEES");
        mainContentPanel.add(departmentPanel, "DEPARTMENTS");
        mainContentPanel.add(reportPanel,     "REPORTS");

        return mainContentPanel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STATUS BAR
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(5, 20, 5, 20)
        ));
        bar.setPreferredSize(new Dimension(0, 28));

        lblStatusLeft = new JLabel("Dashboard loaded");
        lblStatusLeft.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        lblStatusLeft.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        lblDbDot = new JLabel("●");
        lblDbDot.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        lblDbDot.setForeground(UIUtils.COLOR_TEXT_MUTED);

        lblStatusRight = new JLabel("Checking database...");
        lblStatusRight.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        lblStatusRight.setForeground(UIUtils.COLOR_TEXT_MUTED);

        right.add(lblDbDot);
        right.add(lblStatusRight);

        bar.add(lblStatusLeft, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NAVIGATION SWITCHING
    // ─────────────────────────────────────────────────────────────────────────

    public void showSection(String key) {
        this.currentSection = key;
        cardLayout.show(mainContentPanel, key);

        Color AMBER  = new Color(250, 204, 21);
        Color TABROW = new Color(23, 32, 50);

        for (Map.Entry<String, JButton> e : navTabs.entrySet()) {
            JButton tab  = e.getValue();
            boolean active = e.getKey().equals(key);
            tab.setForeground(active ? Color.WHITE : new Color(156, 163, 175));
            tab.setBackground(active ? new Color(31, 43, 65) : TABROW);
            tab.setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 3, 0, active ? AMBER : TABROW),
                    new EmptyBorder(11, 22, 11, 22)
            ));
        }

        switch (key) {
            case "DASHBOARD":
                lblStatusLeft.setText("Dashboard refreshed");
                dashboardPanel.refreshData();
                break;
            case "EMPLOYEES":
                lblStatusLeft.setText("Employee directory loaded");
                employeePanel.loadAllEmployees();
                break;
            case "DEPARTMENTS":
                lblStatusLeft.setText("Department analytics refreshed");
                departmentPanel.refreshData();
                break;
            case "REPORTS":
                lblStatusLeft.setText("Executive reports recalculated");
                reportPanel.refreshData();
                break;
        }
    }

    public void onDataChanged() {
        dashboardPanel.refreshData();
        departmentPanel.refreshData();
        reportPanel.refreshData();
        lblStatusLeft.setText("Data updated — all panels synchronized");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DATA INIT & DB STATUS
    // ─────────────────────────────────────────────────────────────────────────

    private void initData() {
        SwingUtilities.invokeLater(() -> {
            boolean ok = DBConnection.testConnection();
            updateConnectionStatus(ok);
            if (ok) {
                dashboardPanel.refreshData();
                employeePanel.loadAllEmployees();
            } else {
                showConnectionHelpNotice();
            }
        });
    }

    public void updateConnectionStatus(boolean connected) {
        if (connected) {
            lblDbDot.setForeground(UIUtils.COLOR_SUCCESS);
            lblStatusRight.setText("Database connected");
            lblStatusRight.setForeground(UIUtils.COLOR_SUCCESS);
        } else {
            lblDbDot.setForeground(UIUtils.COLOR_DANGER);
            lblStatusRight.setText("Database disconnected - click DB Config");
            lblStatusRight.setForeground(UIUtils.COLOR_DANGER);
        }
    }

    public void showDatabaseConfigDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 10));
        JTextField txtUrl   = new JTextField(DBConnection.getDbUrl());
        JTextField txtUser  = new JTextField(DBConnection.getDbUser());
        JPasswordField txtPass = new JPasswordField();

        panel.add(new JLabel("Database URL:"));  panel.add(txtUrl);
        panel.add(new JLabel("Username:"));       panel.add(txtUser);
        panel.add(new JLabel("Password:"));       panel.add(txtPass);

        int res = JOptionPane.showConfirmDialog(this, panel,
                "Configure MySQL Connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            DBConnection.setCredentials(txtUrl.getText().trim(),
                    txtUser.getText().trim(), new String(txtPass.getPassword()));
            boolean ok = DBConnection.testConnection();
            updateConnectionStatus(ok);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Connected to MySQL successfully!",
                        "Connection Success", JOptionPane.INFORMATION_MESSAGE);
                onDataChanged();
                employeePanel.loadAllEmployees();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Could not connect. Check that MySQL is running and credentials are correct.",
                        "Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showConnectionHelpNotice() {
        JOptionPane.showMessageDialog(this,
                "<html><body style='width:330px;font-family:Segoe UI,sans-serif'>" +
                "<h3 style='color:#dc2626'>Database Not Connected</h3>" +
                "<p>Could not connect to MySQL on startup.</p><br/>" +
                "<b>Quick Setup:</b><ol>" +
                "<li>Ensure MySQL is running (port 3306).</li>" +
                "<li>Run <b>database.sql</b> in MySQL Workbench.</li>" +
                "<li>Click <b>DB Config</b> in the top navbar.</li></ol></body></html>",
                "MySQL Connection Notice", JOptionPane.WARNING_MESSAGE);
    }
}
