package com.ems;
//person2
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * Employee Management Panel:
 * The primary CRUD administration page.
 * Features a modern SaaS data table on the left with live search & department filtering,
 * and a streamlined interactive card form on the right.
 */
public class EmployeePanel extends JPanel {

    private final EmployeeDAO employeeDAO;
    private final MainFrame mainFrame;

    // Search & Filter toolbar components
    private JTextField txtSearch;
    private JComboBox<String> cmbDeptFilter;
    private JButton btnSearch;
    private JButton btnResetSearch;
    private JLabel lblRecordBadge;

    // Table components
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    // Form components
    private JLabel lblFormTitle;
    private JLabel lblFormSubtitle;
    private JLabel lblModeBadge;
    private JTextField txtEmployeeId;
    private JTextField txtName;
    private JComboBox<String> cmbDepartment;
    private JTextField txtDesignation;
    private JTextField txtSalary;
    private JLabel lblFormStatus;

    // Form action buttons
    private JButton btnSave;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;

    // State tracking
    private boolean isEditMode = false;
    private List<Employee> currentCachedList = new ArrayList<>();

    private static final String[] DEPARTMENTS = {
            "Select Department",
            "IT",
            "HR",
            "Finance",
            "Marketing"
    };

    private static final String[] FILTER_DEPARTMENTS = {
            "All Departments",
            "IT",
            "HR",
            "Finance",
            "Marketing"
    };

    public EmployeePanel(EmployeeDAO dao, MainFrame parentFrame) {
        this.employeeDAO = dao;
        this.mainFrame = parentFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(18, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // --- 1. Top Section: Search Bar, Department Filter & Add Employee Action ---
        JPanel topContainer = new JPanel(new BorderLayout(14, 0));
        topContainer.setOpaque(false);

        // Search & Filter Card
        RoundedPanel searchCard = UIUtils.createCardPanel();
        searchCard.setLayout(new BorderLayout(12, 0));
        searchCard.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchControls.setOpaque(false);

        JLabel lblSearchIcon = new JLabel("Search:");
        lblSearchIcon.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 12));
        lblSearchIcon.setForeground(UIUtils.COLOR_TEXT_MUTED);

        txtSearch = UIUtils.createStyledTextField(20);
        txtSearch.setToolTipText("Live search by ID, Name, Department, or Designation");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyLiveFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyLiveFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyLiveFilter(); }
        });
        txtSearch.addActionListener(e -> applyLiveFilter());

        cmbDeptFilter = UIUtils.createStyledComboBox(FILTER_DEPARTMENTS);
        cmbDeptFilter.setToolTipText("Filter by Department");
        cmbDeptFilter.addActionListener(e -> applyLiveFilter());

        btnSearch = UIUtils.createStyledButton("Search", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSearch.addActionListener(e -> applyLiveFilter());

        btnResetSearch = UIUtils.createOutlineButton("Clear");
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            cmbDeptFilter.setSelectedIndex(0);
            loadAllEmployees();
        });

        searchControls.add(lblSearchIcon);
        searchControls.add(txtSearch);
        searchControls.add(new JLabel("Dept:"));
        searchControls.add(cmbDeptFilter);
        searchControls.add(btnSearch);
        searchControls.add(btnResetSearch);

        // Right side of top bar: Record Badge & "+ Add Employee" Button
        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topActions.setOpaque(false);

        lblRecordBadge = UIUtils.createPillBadge("Records: 0", UIUtils.COLOR_PRIMARY_LIGHT, UIUtils.COLOR_PRIMARY);

        JButton btnAddNew = UIUtils.createStyledButton("+ Add Employee", UIUtils.COLOR_SUCCESS, Color.WHITE);
        btnAddNew.addActionListener(e -> startAddNewEmployee());

        topActions.add(lblRecordBadge);
        topActions.add(btnAddNew);

        searchCard.add(searchControls, BorderLayout.WEST);
        searchCard.add(topActions, BorderLayout.EAST);

        topContainer.add(searchCard, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. Main Workspace: Left Table (62%) & Right Form (38%) ---
        JPanel workspacePanel = new JPanel(new GridBagLayout());
        workspacePanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Table Card
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 16);
        JPanel tableCard = createTableCard();
        workspacePanel.add(tableCard, gbc);

        // Right Form Card
        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        JPanel formCard = createFormCard();
        workspacePanel.add(formCard, gbc);

        add(workspacePanel, BorderLayout.CENTER);
    }

    private JPanel createTableCard() {
        RoundedPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout(0, 12));

        JPanel tableHeaderRow = new JPanel(new BorderLayout());
        tableHeaderRow.setOpaque(false);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        textBlock.setOpaque(false);
        JLabel lblListTitle = new JLabel("Employee Directory");
        lblListTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        lblListTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel lblHint = new JLabel("Click any row to view, edit, or delete record");
        lblHint.setFont(UIUtils.FONT_SMALL);
        lblHint.setForeground(UIUtils.COLOR_TEXT_MUTED);

        textBlock.add(lblListTitle);
        textBlock.add(lblHint);
        tableHeaderRow.add(textBlock, BorderLayout.WEST);

        card.add(tableHeaderRow, BorderLayout.NORTH);

        // Setup JTable
        String[] columnNames = {"Employee ID", "Full Name", "Department", "Designation", "Salary"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        employeeTable = new JTable(tableModel);
        UIUtils.styleTable(employeeTable);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Row selection event handler
        employeeTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = employeeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromSelectedRow(selectedRow);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormCard() {
        RoundedPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout(0, 14));

        // Form Header Block with Title & Mode Badge
        JPanel formHeader = new JPanel(new BorderLayout());
        formHeader.setOpaque(false);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        textBlock.setOpaque(false);
        lblFormTitle = new JLabel("Add New Employee");
        lblFormTitle.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 15));
        lblFormTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        lblFormSubtitle = new JLabel("Fill out details to register staff in MySQL database");
        lblFormSubtitle.setFont(UIUtils.FONT_SMALL);
        lblFormSubtitle.setForeground(UIUtils.COLOR_TEXT_MUTED);
        textBlock.add(lblFormTitle);
        textBlock.add(lblFormSubtitle);

        lblModeBadge = UIUtils.createPillBadge("+ New Entry", UIUtils.COLOR_SUCCESS_LIGHT, UIUtils.COLOR_SUCCESS);

        formHeader.add(textBlock, BorderLayout.WEST);
        formHeader.add(lblModeBadge, BorderLayout.EAST);
        card.add(formHeader, BorderLayout.NORTH);

        // Form Fields (GridBagLayout)
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        GridBagConstraints fgbc = new GridBagConstraints();
        fgbc.fill = GridBagConstraints.HORIZONTAL;
        fgbc.insets = new Insets(3, 0, 3, 0);
        fgbc.gridx = 0;
        fgbc.weightx = 1.0;

        // Employee ID
        fgbc.gridy = 0;
        fieldsPanel.add(createFieldLabel("EMPLOYEE ID *"), fgbc);
        fgbc.gridy = 1;
        txtEmployeeId = UIUtils.createStyledTextField(15);
        txtEmployeeId.setToolTipText("Unique alphanumeric identifier, e.g., 101 or EMP-01");
        fieldsPanel.add(txtEmployeeId, fgbc);

        // Full Name
        fgbc.gridy = 2;
        fieldsPanel.add(createFieldLabel("FULL NAME *"), fgbc);
        fgbc.gridy = 3;
        txtName = UIUtils.createStyledTextField(15);
        txtName.setToolTipText("Enter employee legal name");
        fieldsPanel.add(txtName, fgbc);

        // Department
        fgbc.gridy = 4;
        fieldsPanel.add(createFieldLabel("DEPARTMENT *"), fgbc);
        fgbc.gridy = 5;
        cmbDepartment = UIUtils.createStyledComboBox(DEPARTMENTS);
        fieldsPanel.add(cmbDepartment, fgbc);

        // Designation
        fgbc.gridy = 6;
        fieldsPanel.add(createFieldLabel("DESIGNATION / JOB TITLE *"), fgbc);
        fgbc.gridy = 7;
        txtDesignation = UIUtils.createStyledTextField(15);
        txtDesignation.setToolTipText("e.g. Senior Software Engineer");
        fieldsPanel.add(txtDesignation, fgbc);

        // Salary
        fgbc.gridy = 8;
        fieldsPanel.add(createFieldLabel("ANNUAL SALARY ($) *"), fgbc);
        fgbc.gridy = 9;
        txtSalary = UIUtils.createStyledTextField(15);
        txtSalary.setToolTipText("Numeric positive value, e.g., 75000.00");
        fieldsPanel.add(txtSalary, fgbc);

        // Status Feedback Banner
        fgbc.gridy = 10;
        fgbc.insets = new Insets(8, 0, 0, 0);
        lblFormStatus = new JLabel(" ");
        lblFormStatus.setFont(UIUtils.FONT_SMALL);
        lblFormStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);
        fieldsPanel.add(lblFormStatus, fgbc);

        // Glue space at bottom of fields
        fgbc.gridy = 11;
        fgbc.weighty = 1.0;
        fieldsPanel.add(Box.createVerticalGlue(), fgbc);

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Form Action Buttons (2 Rows)
        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setLayout(new BoxLayout(buttonsContainer, BoxLayout.Y_AXIS));
        buttonsContainer.setOpaque(false);

        // Row 1: Primary actions (Save / Update)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0));
        row1.setOpaque(false);

        btnSave = UIUtils.createStyledButton("Save Employee", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSave.addActionListener(e -> handleAddEmployee());

        btnUpdate = UIUtils.createStyledButton("Update Record", UIUtils.COLOR_INFO, Color.WHITE);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> handleUpdateEmployee());

        row1.add(btnSave);
        row1.add(btnUpdate);

        // Row 2: Secondary actions (Delete / Clear)
        JPanel row2 = new JPanel(new GridLayout(1, 2, 8, 0));
        row2.setOpaque(false);

        btnDelete = UIUtils.createDangerButton("Delete Record");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> handleDeleteEmployee());

        btnClear = UIUtils.createOutlineButton("Clear Form");
        btnClear.addActionListener(e -> resetForm(true));

        row2.add(btnDelete);
        row2.add(btnClear);

        buttonsContainer.add(row1);
        buttonsContainer.add(Box.createVerticalStrut(8));
        buttonsContainer.add(row2);

        card.add(buttonsContainer, BorderLayout.SOUTH);
        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(UIUtils.FONT_FAMILY, Font.BOLD, 11));
        label.setForeground(new Color(71, 85, 105));
        return label;
    }

    /**
     * Resets form to fresh "Add New Employee" state and focuses the ID input.
     */
    public void startAddNewEmployee() {
        resetForm(true);
        txtEmployeeId.requestFocus();
    }

    /**
     * Populates form fields with selected row data.
     */
    private void populateFormFromSelectedRow(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return;

        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String dept = (String) tableModel.getValueAt(row, 2);
        String designation = (String) tableModel.getValueAt(row, 3);
        Object rawSalary = tableModel.getValueAt(row, 4);

        txtEmployeeId.setText(id);
        txtEmployeeId.setEditable(false);
        txtName.setText(name);

        // Select department in combo box
        boolean deptFound = false;
        for (int i = 0; i < cmbDepartment.getItemCount(); i++) {
            if (cmbDepartment.getItemAt(i).equalsIgnoreCase(dept)) {
                cmbDepartment.setSelectedIndex(i);
                deptFound = true;
                break;
            }
        }
        if (!deptFound) {
            cmbDepartment.setSelectedIndex(0);
        }

        txtDesignation.setText(designation);

        // Clean currency symbols if formatted
        String cleanSalary = String.valueOf(rawSalary).replaceAll("[^0-9.]", "");
        txtSalary.setText(cleanSalary);

        // Transition form to edit mode
        isEditMode = true;
        lblFormTitle.setText("Edit Employee (" + id + ")");
        lblFormSubtitle.setText("Update attributes or delete employee record");
        lblModeBadge.setText("Editing #" + id);
        lblModeBadge.setBackground(UIUtils.COLOR_INFO_LIGHT);
        lblModeBadge.setForeground(UIUtils.COLOR_INFO);

        lblFormStatus.setText("Loaded employee details for editing.");
        lblFormStatus.setForeground(UIUtils.COLOR_TEXT_MUTED);

        btnSave.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    /**
     * Resets form to initial 'Add New Employee' state.
     */
    public void resetForm(boolean clearTableSelection) {
        txtEmployeeId.setText("");
        txtEmployeeId.setEditable(true);
        txtName.setText("");
        cmbDepartment.setSelectedIndex(0);
        txtDesignation.setText("");
        txtSalary.setText("");
        lblFormStatus.setText(" ");

        if (clearTableSelection) {
            employeeTable.clearSelection();
        }

        isEditMode = false;
        lblFormTitle.setText("Add New Employee");
        lblFormSubtitle.setText("Fill out details to register staff in MySQL database");
        lblModeBadge.setText("+ New Entry");
        lblModeBadge.setBackground(UIUtils.COLOR_SUCCESS_LIGHT);
        lblModeBadge.setForeground(UIUtils.COLOR_SUCCESS);

        btnSave.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }

    // --- Validation and CRUD Operations ---

    /**
     * Validates form input fields according to academic project specifications.
     */
    private Employee validateAndBuildEmployee() {
        String empId = txtEmployeeId.getText().trim();
        String name = txtName.getText().trim();
        String dept = (String) cmbDepartment.getSelectedItem();
        String designation = txtDesignation.getText().trim();
        String salaryStr = txtSalary.getText().trim();

        // 1. Employee ID validation
        if (empId.isEmpty()) {
            setFormError("Please enter an Employee ID.");
            txtEmployeeId.requestFocus();
            return null;
        }

        if (empId.length() > 20) {
            setFormError("Employee ID cannot exceed 20 characters.");
            txtEmployeeId.requestFocus();
            return null;
        }

        // 2. Name validation
        if (name.isEmpty()) {
            setFormError("Please enter employee name.");
            txtName.requestFocus();
            return null;
        }

        // 3. Department validation
        if (dept == null || dept.startsWith("Select") || dept.isEmpty()) {
            setFormError("Please select a valid department from the dropdown.");
            cmbDepartment.requestFocus();
            return null;
        }

        // 4. Designation validation
        if (designation.isEmpty()) {
            setFormError("Please enter employee designation.");
            txtDesignation.requestFocus();
            return null;
        }

        // 5. Salary validation
        if (salaryStr.isEmpty()) {
            setFormError("Salary must be a valid positive number.");
            txtSalary.requestFocus();
            return null;
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
            if (salary <= 0) {
                setFormError("Salary must be a valid positive number.");
                txtSalary.requestFocus();
                return null;
            }
        } catch (NumberFormatException ex) {
            setFormError("Salary must be a valid positive number.");
            txtSalary.requestFocus();
            return null;
        }

        return new Employee(empId, name, dept, designation, salary);
    }

    private void setFormError(String message) {
        lblFormStatus.setText("! " + message);
        lblFormStatus.setForeground(UIUtils.COLOR_DANGER);
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setFormSuccess(String message) {
        lblFormStatus.setText("OK: " + message);
        lblFormStatus.setForeground(UIUtils.COLOR_SUCCESS);
    }

    /**
     * Action Handler: Add Employee (INSERT)
     */
    private void handleAddEmployee() {
        Employee emp = validateAndBuildEmployee();
        if (emp == null) return;

        try {
            if (employeeDAO.isEmployeeIdExists(emp.getEmployeeId())) {
                setFormError("An employee with ID '" + emp.getEmployeeId() + "' already exists.");
                txtEmployeeId.requestFocus();
                return;
            }

            boolean success = employeeDAO.addEmployee(emp);
            if (success) {
                setFormSuccess("Employee added successfully!");
                JOptionPane.showMessageDialog(this,
                        "Employee '" + emp.getName() + "' (ID: " + emp.getEmployeeId() + ") was successfully added!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm(true);
                loadAllEmployees();
                mainFrame.onDataChanged();
            } else {
                setFormError("Failed to add employee. Database did not accept the record.");
            }
        } catch (SQLException ex) {
            showDatabaseError("Error inserting employee record", ex);
        }
    }

    /**
     * Action Handler: Update Employee (UPDATE)
     */
    private void handleUpdateEmployee() {
        Employee emp = validateAndBuildEmployee();
        if (emp == null) return;

        try {
            boolean success = employeeDAO.updateEmployee(emp);
            if (success) {
                setFormSuccess("Employee updated successfully!");
                JOptionPane.showMessageDialog(this,
                        "Employee record for '" + emp.getName() + "' (ID: " + emp.getEmployeeId() + ") updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                resetForm(true);
                loadAllEmployees();
                mainFrame.onDataChanged();
            } else {
                setFormError("Could not update employee. The record might have been removed.");
            }
        } catch (SQLException ex) {
            showDatabaseError("Error updating employee record", ex);
        }
    }

    /**
     * Action Handler: Delete Employee (DELETE)
     */
    private void handleDeleteEmployee() {
        String empId = txtEmployeeId.getText().trim();
        String name = txtName.getText().trim();

        if (empId.isEmpty()) {
            setFormError("Please select an employee record to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this employee?\n\n" +
                "ID: " + empId + "\n" +
                "Name: " + name,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean success = employeeDAO.deleteEmployee(empId);
                if (success) {
                    setFormSuccess("Employee deleted.");
                    JOptionPane.showMessageDialog(this,
                            "Employee ID '" + empId + "' has been successfully removed.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    resetForm(true);
                    loadAllEmployees();
                    mainFrame.onDataChanged();
                } else {
                    setFormError("Could not delete employee. Record not found.");
                }
            } catch (SQLException ex) {
                showDatabaseError("Error deleting employee record", ex);
            }
        }
    }

    /**
     * Filter handler combining text keyword search and department filter.
     */
    private void applyLiveFilter() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedDept = (String) cmbDeptFilter.getSelectedItem();

        if (currentCachedList == null || currentCachedList.isEmpty()) {
            return;
        }

        List<Employee> filtered = new ArrayList<>();
        for (Employee emp : currentCachedList) {
            boolean matchesDept = selectedDept == null || selectedDept.equals("All Departments") ||
                    emp.getDepartment().equalsIgnoreCase(selectedDept);

            boolean matchesText = keyword.isEmpty() ||
                    emp.getEmployeeId().toLowerCase().contains(keyword) ||
                    emp.getName().toLowerCase().contains(keyword) ||
                    emp.getDepartment().toLowerCase().contains(keyword) ||
                    emp.getDesignation().toLowerCase().contains(keyword);

            if (matchesDept && matchesText) {
                filtered.add(emp);
            }
        }

        populateTable(filtered);
        lblRecordBadge.setText("Showing: " + filtered.size() + " of " + currentCachedList.size());
    }

    /**
     * Loads all employee records into the table and caches the list for live filtering.
     */
    public void loadAllEmployees() {
        try {
            currentCachedList = employeeDAO.getAllEmployees();
            populateTable(currentCachedList);
            lblRecordBadge.setText("Total: " + currentCachedList.size() + " records");
        } catch (SQLException ex) {
            tableModel.setRowCount(0);
            currentCachedList = new ArrayList<>();
            lblRecordBadge.setText("Database Disconnected");
        }
    }

    private void populateTable(List<Employee> employees) {
        tableModel.setRowCount(0);
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                    emp.getEmployeeId(),
                    emp.getName(),
                    emp.getDepartment(),
                    emp.getDesignation(),
                    emp.getSalary()
            });
        }
    }

    private void showDatabaseError(String message, SQLException ex) {
        JOptionPane.showMessageDialog(this,
                message + ":\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
