package com.ems;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
/**person1 */
/**
 * Data Access Object (DAO) for Employee operations.
 * Separates database persistence logic from GUI presentation logic.
 * Employs PreparedStatement for all queries to ensure security and prevent SQL injection.
 */
public class EmployeeDAO {

    /**
     * DTO helper class to hold aggregated salary statistics for reports.
     */
    public static class SalaryStatistics {
        private final int count;
        private final double totalPayroll;
        private final double averageSalary;
        private final double highestSalary;
        private final double lowestSalary;

        public SalaryStatistics(int count, double totalPayroll, double averageSalary, double highestSalary, double lowestSalary) {
            this.count = count;
            this.totalPayroll = totalPayroll;
            this.averageSalary = averageSalary;
            this.highestSalary = highestSalary;
            this.lowestSalary = lowestSalary;
        }

        public int getCount() { return count; }
        public double getTotalPayroll() { return totalPayroll; }
        public double getAverageSalary() { return averageSalary; }
        public double getHighestSalary() { return highestSalary; }
        public double getLowestSalary() { return lowestSalary; }
    }

    /**
     * DTO helper class to hold department-wise metrics.
     */
    public static class DepartmentStat {
        private final String department;
        private final int employeeCount;
        private final double avgSalary;
        private final double totalSalary;

        public DepartmentStat(String department, int employeeCount, double avgSalary, double totalSalary) {
            this.department = department;
            this.employeeCount = employeeCount;
            this.avgSalary = avgSalary;
            this.totalSalary = totalSalary;
        }

        public String getDepartment() { return department; }
        public int getEmployeeCount() { return employeeCount; }
        public double getAvgSalary() { return avgSalary; }
        public double getTotalSalary() { return totalSalary; }
    }

    /**
     * Inserts a new employee record into the database.
     * 
     * @param emp Employee object to insert
     * @return true if insertion succeeded, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean addEmployee(Employee emp) throws SQLException {
        String sql = "INSERT INTO employees (employee_id, name, department, designation, salary) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, emp.getEmployeeId().trim());
            pstmt.setString(2, emp.getName().trim());
            pstmt.setString(3, emp.getDepartment().trim());
            pstmt.setString(4, emp.getDesignation().trim());
            pstmt.setDouble(5, emp.getSalary());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Retrieves all employee records from the database ordered by Employee ID.
     * 
     * @return List of all Employee objects
     * @throws SQLException if database error occurs
     */
    public List<Employee> getAllEmployees() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, name, department, designation, salary FROM employees ORDER BY employee_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEmployee(rs));
            }
        }
        return list;
    }

    /**
     * Searches employees by matching ID, name, department, or designation.
     * 
     * @param keyword Search term
     * @return List of matching Employee objects
     * @throws SQLException if database error occurs
     */
    public List<Employee> searchEmployee(String keyword) throws SQLException {
        List<Employee> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployees();
        }

        String searchPattern = "%" + keyword.trim() + "%";
        String sql = "SELECT employee_id, name, department, designation, salary FROM employees " +
                     "WHERE employee_id LIKE ? OR name LIKE ? OR department LIKE ? OR designation LIKE ? " +
                     "ORDER BY employee_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmployee(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all employees belonging strictly to a specified department.
     */
    public List<Employee> getEmployeesByDepartment(String department) throws SQLException {
        if (department == null || department.trim().isEmpty() || department.equalsIgnoreCase("All Departments")) {
            return getAllEmployees();
        }
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, name, department, designation, salary FROM employees WHERE department = ? ORDER BY employee_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmployee(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves an employee by their exact Employee ID.
     * 
     * @param employeeId Unique employee ID
     * @return Employee object or null if not found
     * @throws SQLException if database error occurs
     */
    public Employee getEmployeeById(String employeeId) throws SQLException {
        String sql = "SELECT employee_id, name, department, designation, salary FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmployee(rs);
                }
            }
        }
        return null;
    }

    /**
     * Updates an existing employee record in the database.
     * 
     * @param emp Employee object with updated data
     * @return true if updated successfully, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean updateEmployee(Employee emp) throws SQLException {
        String sql = "UPDATE employees SET name = ?, department = ?, designation = ?, salary = ? WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, emp.getName().trim());
            pstmt.setString(2, emp.getDepartment().trim());
            pstmt.setString(3, emp.getDesignation().trim());
            pstmt.setDouble(4, emp.getSalary());
            pstmt.setString(5, emp.getEmployeeId().trim());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Deletes an employee record from the database by ID.
     * 
     * @param employeeId ID of employee to delete
     * @return true if deleted successfully, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean deleteEmployee(String employeeId) throws SQLException {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId.trim());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Checks if an employee ID already exists in the database.
     * Used for duplicate ID input validation before insertion.
     * 
     * @param employeeId ID to check
     * @return true if exists, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean isEmployeeIdExists(String employeeId) throws SQLException {
        String sql = "SELECT 1 FROM employees WHERE employee_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, employeeId.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retrieves employee counts grouped by department.
     * 
     * @return Map containing department name as key and count as value
     * @throws SQLException if database error occurs
     */
    public Map<String, Integer> getDepartmentCounts() throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        // Pre-initialize common departments with 0 count
        counts.put("IT", 0);
        counts.put("HR", 0);
        counts.put("Finance", 0);
        counts.put("Marketing", 0);

        String sql = "SELECT department, COUNT(*) AS total FROM employees GROUP BY department ORDER BY department ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String dept = rs.getString("department");
                int count = rs.getInt("total");
                counts.put(dept, count);
            }
        }
        return counts;
    }

    /**
     * Calculates overall salary statistics (count, total payroll, average, max, min).
     * 
     * @return SalaryStatistics object
     * @throws SQLException if database error occurs
     */
    public SalaryStatistics getSalaryStatistics() throws SQLException {
        String sql = "SELECT COUNT(*) AS total_count, " +
                     "COALESCE(SUM(salary), 0) AS total_payroll, " +
                     "COALESCE(AVG(salary), 0) AS avg_salary, " +
                     "COALESCE(MAX(salary), 0) AS max_salary, " +
                     "COALESCE(MIN(salary), 0) AS min_salary " +
                     "FROM employees";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                int count = rs.getInt("total_count");
                double total = rs.getDouble("total_payroll");
                double avg = rs.getDouble("avg_salary");
                double max = rs.getDouble("max_salary");
                double min = rs.getDouble("min_salary");
                return new SalaryStatistics(count, total, avg, max, min);
            }
        }
        return new SalaryStatistics(0, 0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Helper returning total number of employee records.
     */
    public int getTotalEmployeeCount() throws SQLException {
        return getSalaryStatistics().getCount();
    }

    /**
     * Helper returning mean employee salary.
     */
    public double getAverageSalary() throws SQLException {
        return getSalaryStatistics().getAverageSalary();
    }

    /**
     * Helper returning gross payroll expenditure.
     */
    public double getTotalPayroll() throws SQLException {
        return getSalaryStatistics().getTotalPayroll();
    }

    /**
     * Retrieves detailed statistics per department for Reports & Departments sections.
     * 
     * @return List of DepartmentStat objects
     * @throws SQLException if database error occurs
     */
    public List<DepartmentStat> getDepartmentStatisticsList() throws SQLException {
        List<DepartmentStat> list = new ArrayList<>();
        String sql = "SELECT department, COUNT(*) AS dept_count, " +
                     "COALESCE(AVG(salary), 0) AS avg_sal, " +
                     "COALESCE(SUM(salary), 0) AS total_sal " +
                     "FROM employees " +
                     "GROUP BY department " +
                     "ORDER BY dept_count DESC, department ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(new DepartmentStat(
                        rs.getString("department"),
                        rs.getInt("dept_count"),
                        rs.getDouble("avg_sal"),
                        rs.getDouble("total_sal")
                ));
            }
        }
        return list;
    }

    /**
     * Retrieves the most recent employees up to the given limit.
     * 
     * @param limit Maximum number of records to return
     * @return List of Employee records
     * @throws SQLException if database error occurs
     */
    public List<Employee> getRecentEmployees(int limit) throws SQLException {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT employee_id, name, department, designation, salary FROM employees ORDER BY employee_id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEmployee(rs));
                }
            }
        }
        return list;
    }

    /**
     * Helper method to map a ResultSet row to an Employee object.
     */
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getString("employee_id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getString("designation"),
                rs.getDouble("salary")
        );
    }
}
