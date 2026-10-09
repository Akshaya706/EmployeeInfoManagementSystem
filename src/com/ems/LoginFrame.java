package com.ems;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;//p2

/**
 * Modern, enterprise-grade login window for the Employee Management System.
 * Features a split-pane layout with a branded dark sidebar and a clean authentication card.
 * Supports authentication against MySQL 'users' table, with built-in fallback credentials
 * (admin / admin123) for seamless standalone demonstration.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox chkShowPassword;
    private JCheckBox chkRemember;
    private JLabel lblError;
    private JPanel errorBanner;
    private JLabel lblDbDot;
    private JLabel lblDbStatus;

    public LoginFrame() {
        setTitle("EMS - Administrative Portal Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 540);
        setResizable(false);
        setLocationRelativeTo(null);
        initUI();
        checkDatabaseStatus();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.COLOR_BG);

        // Split panel: Left branded banner + Right login form
        root.add(createBrandedBanner(), BorderLayout.WEST);
        root.add(createLoginForm(),     BorderLayout.CENTER);

        setContentPane(root);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BRANDED SIDEBAR (LEFT)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createBrandedBanner() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep gradient background from Navy to Dark Indigo
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 23, 42),
                        0, getHeight(), new Color(30, 27, 75)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle background circle accents for depth
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fillOval(-60, -60, 220, 220);
                g2.fillOval(getWidth() - 100, getHeight() - 120, 200, 200);

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(340, 540));
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(36, 32, 32, 32));

        // Content container
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // Top tag pill
        JLabel pill = new JLabel("ENTERPRISE HR SYSTEM");
        pill.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 10));
        pill.setForeground(new Color(250, 204, 21)); // Gold
        pill.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Circular EMS Logo Emblem
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.COLOR_PRIMARY);
                g2.fillRoundRect(0, 0, 52, 52, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String text = "EMS";
                int x = (52 - fm.stringWidth(text)) / 2;
                int y = ((52 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(52, 52));
        logoPanel.setMaximumSize(new Dimension(52, 52));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // App Title
        JLabel title = new JLabel("<html>Employee<br>Management<br>System</html>");
        title.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 26));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("<html>Workforce & Payroll Administration Platform with MySQL Persistence.</html>");
        subtitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        subtitle.setForeground(new Color(148, 163, 184));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Bullet feature badges
        JPanel features = new JPanel();
        features.setOpaque(false);
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        features.setAlignmentX(Component.LEFT_ALIGNMENT);


        content.add(pill);
        content.add(Box.createVerticalStrut(16));
        content.add(logoPanel);
        content.add(Box.createVerticalStrut(18));
        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));
        content.add(features);

        panel.add(content, BorderLayout.NORTH);

        // Bottom version note
        JLabel footer = new JLabel("Version 1.0 - Academic Release");
        footer.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        footer.setForeground(new Color(100, 116, 139));
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createFeatureItem(String text) {
        JLabel lbl = new JLabel("- " + text);
        lbl.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        lbl.setForeground(new Color(203, 213, 225));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN FORM CARD (RIGHT)
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel createLoginForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new EmptyBorder(36, 42, 28, 42));

        // Form Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel lblSignIn = new JLabel("Sign In");
        lblSignIn.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 24));
        lblSignIn.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel lblSub = new JLabel("Please enter your administrator credentials to continue");
        lblSub.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        lblSub.setForeground(UIUtils.COLOR_TEXT_MUTED);

        header.add(lblSignIn);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);
        header.add(Box.createVerticalStrut(16));

        // Error banner (hidden initially)
        errorBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        errorBanner.setBackground(UIUtils.COLOR_DANGER_LIGHT);
        errorBanner.setBorder(new CompoundBorder(
                new javax.swing.border.LineBorder(new Color(252, 165, 165), 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
        errorBanner.setVisible(false);

        lblError = new JLabel("Invalid username or password.");
        lblError.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        lblError.setForeground(UIUtils.COLOR_DANGER);
        errorBanner.add(lblError);

        header.add(errorBanner);
        wrapper.add(header, BorderLayout.NORTH);

        // Form fields center container
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Username Field
        JLabel lblUser = new JLabel("Username / Admin ID");
        lblUser.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        lblUser.setForeground(UIUtils.COLOR_TEXT_MAIN);

        txtUsername = UIUtils.createStyledTextField(20);
        txtUsername.setText("admin");
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Password Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        lblPass.setForeground(UIUtils.COLOR_TEXT_MAIN);

        txtPassword = UIUtils.createStyledPasswordField(20);
        txtPassword.setText("admin123");
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Trigger login on pressing Enter in password field
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        // Options row: Show Password & Remember me
        JPanel optionsRow = new JPanel(new BorderLayout());
        optionsRow.setOpaque(false);
        optionsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        chkRemember = new JCheckBox("Remember username", true);
        chkRemember.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        chkRemember.setForeground(UIUtils.COLOR_TEXT_MUTED);
        chkRemember.setOpaque(false);

        chkShowPassword = new JCheckBox("Show password");
        chkShowPassword.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        chkShowPassword.setForeground(UIUtils.COLOR_TEXT_MUTED);
        chkShowPassword.setOpaque(false);
        chkShowPassword.addActionListener(e -> {
            if (chkShowPassword.isSelected()) {
                txtPassword.setEchoChar((char) 0);
            } else {
                txtPassword.setEchoChar('•');
            }
        });

        optionsRow.add(chkRemember, BorderLayout.WEST);
        optionsRow.add(chkShowPassword, BorderLayout.EAST);

        // Buttons container
        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnLogin = UIUtils.createPrimaryButton("Sign In to EMS");
        btnLogin.addActionListener(e -> handleLogin());

        JButton btnDemo = UIUtils.createOutlineButton("Fill Demo Login");
        btnDemo.setToolTipText("Auto-fills default administrative credentials");
        btnDemo.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
            hideError();
        });

        actions.add(btnLogin);
        actions.add(btnDemo);

        // Assembly
        form.add(lblUser);
        form.add(Box.createVerticalStrut(6));
        form.add(txtUsername);
        form.add(Box.createVerticalStrut(14));
        form.add(lblPass);
        form.add(Box.createVerticalStrut(6));
        form.add(txtPassword);
        form.add(Box.createVerticalStrut(10));
        form.add(optionsRow);
        form.add(Box.createVerticalStrut(20));
        form.add(actions);

        wrapper.add(form, BorderLayout.CENTER);

        // Bottom Database Status Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER_LIGHT),
                new EmptyBorder(12, 0, 0, 0)
        ));

        JPanel dbInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dbInfo.setOpaque(false);

        lblDbDot = new JLabel("●");
        lblDbDot.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 12));
        lblDbDot.setForeground(UIUtils.COLOR_WARNING);

        lblDbStatus = new JLabel("Checking MySQL...");
        lblDbStatus.setFont(new Font(UIUtils.FONT_FAMILY, Font.PLAIN, 11));
        lblDbStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);

        dbInfo.add(lblDbDot);
        dbInfo.add(lblDbStatus);

        JButton btnConfig = UIUtils.createOutlineButton("DB Settings");
        btnConfig.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 11));
        btnConfig.addActionListener(e -> showDbConfigDialog());

        bottomBar.add(dbInfo, BorderLayout.WEST);
        bottomBar.add(btnConfig, BorderLayout.EAST);

        wrapper.add(bottomBar, BorderLayout.SOUTH);
        return wrapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTHENTICATION LOGIC
    // ─────────────────────────────────────────────────────────────────────────

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // 1. Try DB authentication first if available
        String validatedFullName = authenticateFromDb(username, password);

        // 2. If not DB-authenticated, check default demo admin accounts
        if (validatedFullName == null) {
            validatedFullName = authenticateBuiltIn(username, password);
        }

        if (validatedFullName != null) {
            hideError();
            // Launch main frame with authenticated user
            final String finalUser = validatedFullName;
            SwingUtilities.invokeLater(() -> {
                dispose();
                MainFrame frame = new MainFrame(finalUser);
                frame.setVisible(true);
            });
        } else {
            showError("Invalid credentials. Try demo credentials: admin / admin123");
        }
    }

    private String authenticateFromDb(String user, String pass) {
        String sql = "SELECT full_name, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, pass);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("full_name");
                    String role = rs.getString("role");
                    return (name != null && !name.isEmpty()) ? name + " (" + role + ")" : user;
                }
            }
        } catch (Exception ignored) {
            // Table doesn't exist or MySQL is offline; fallback to built-in accounts
        }
        return null;
    }

    private String authenticateBuiltIn(String user, String pass) {
        // Built-in administrative and demo accounts
        if (user.equalsIgnoreCase("admin") && (pass.equals("admin123") || pass.equals("admin") || pass.equals("password"))) {
            return "System Administrator";
        }
        if (user.equalsIgnoreCase("manager") && (pass.equals("manager123") || pass.equals("password") || pass.equals("admin"))) {
            return "HR Operations Manager";
        }
        if (user.equalsIgnoreCase("hr") && (pass.equals("hr123") || pass.equals("admin") || pass.equals("hr"))) {
            return "HR Specialist";
        }
        return null;
    }

    private void showError(String msg) {
        lblError.setText(msg);
        errorBanner.setVisible(true);
        errorBanner.revalidate();
        errorBanner.repaint();
    }

    private void hideError() {
        errorBanner.setVisible(false);
        errorBanner.revalidate();
        errorBanner.repaint();
    }

    private void checkDatabaseStatus() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return DBConnection.testConnection();
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        lblDbDot.setForeground(UIUtils.COLOR_SUCCESS);
                        lblDbStatus.setText("MySQL Connected");
                        lblDbStatus.setForeground(UIUtils.COLOR_SUCCESS);
                    } else {
                        lblDbDot.setForeground(UIUtils.COLOR_WARNING);
                        lblDbStatus.setText("MySQL Disconnected (Standalone Demo Mode)");
                        lblDbStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);
                    }
                } catch (Exception e) {
                    lblDbDot.setForeground(UIUtils.COLOR_DANGER);
                    lblDbStatus.setText("Database error");
                }
            }
        };
        worker.execute();
    }

    private void showDbConfigDialog() {
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
            checkDatabaseStatus();
        }
    }
}
