package com.ems;

import java.text.DecimalFormat;
import java.util.Objects;
/**person1 */
/**
 * Model class representing an Employee in the Employee Information Management System.
 * Implements standard Object-Oriented principles (Encapsulation).
 */
public class Employee {
    private String employeeId;
    private String name;
    private String department;
    private String designation;
    private double salary;

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("₹#,##0.00");

    // Default constructor
    public Employee() {
    }

    // Parameterized constructor
    public Employee(String employeeId, String name, String department, String designation, double salary) {
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
    }

    // Getters and Setters (Encapsulation)
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    /**
     * Formats salary with commas and two decimal places.
     * @return Formatted salary string, e.g., "$85,000.00"
     */
    public String getFormattedSalary() {
        return CURRENCY_FORMAT.format(salary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeId, employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", salary=" + salary +
                '}';
    }
}
