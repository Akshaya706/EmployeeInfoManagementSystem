-- ============================================================
-- Employee Information Management System (EMS)
-- Database Initialization Script for MySQL
-- ============================================================

-- 1. Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS employee_management;

-- 2. Switch to the newly created database
USE employee_management;

-- 3. Drop existing table if needed for clean setup
DROP TABLE IF EXISTS employees;

-- 4. Create the 'employees' table
CREATE TABLE IF NOT EXISTS employees (
    employee_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL
);

-- 5. Drop and recreate 'users' authentication table
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 6. Insert default administrative and HR user accounts
INSERT INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'System Administrator', 'ADMIN'),
('manager', 'manager123', 'HR Operations Manager', 'MANAGER'),
('hr', 'hr123', 'HR Specialist', 'HR');

-- 7. Insert sample employee records for academic demonstration
INSERT INTO employees (employee_id, name, department, designation, salary) VALUES
('EMP-101', 'Alexander Wright', 'IT', 'Senior Software Engineer', 85000.00),
('EMP-102', 'Sophia Martinez', 'HR', 'HR Operations Manager', 68000.00),
('EMP-103', 'David Chen', 'Finance', 'Financial Analyst', 72000.00),
('EMP-104', 'Emily Watson', 'Marketing', 'Digital Marketing Specialist', 61000.00),
('EMP-105', 'Michael Brown', 'IT', 'DevOps & Cloud Engineer', 90000.00),
('EMP-106', 'Rachel Green', 'Finance', 'Senior Accountant', 75000.00);

-- 8. Verification queries
SELECT * FROM users;
SELECT * FROM employees;
