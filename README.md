# Employee Information Management System (EMS)

An enterprise-styled, standalone Java desktop application built from scratch to manage employee personnel records, department allocations, and executive compensation reports. 

Designed and engineered to satisfy the academic requirements for college-level Java, Object-Oriented Programming (OOP), Java Swing Graphical User Interface (GUI), and Java Database Connectivity (JDBC) with MySQL.

---

## 📌 Problem Statement & Objectives

> **College Problem Statement:**  
> *"Develop an Employee Information Management System using Java and an appropriate GUI framework. The application should allow the user to add, view, search, modify, and delete employee information through the graphical interface. Appropriate GUI controls and event handling mechanisms should be used to perform the required operations. JDBC should be used to establish connectivity with a relational database and perform basic insert, retrieve, update, and delete operations on employee records."*

---

## ✨ Features & Functional Modules

The application is structured around a modern navigation layout featuring a persistent **left sidebar** and a high-performance **CardLayout content area**:

### 0. 🔐 Authentication & Access Portal
- **Branded Login Portal:** High-contrast split-pane login interface with EMS monogram, features list, and real-time database connection status.
- **Role-Based Authentication:** Verifies credentials against MySQL `users` table or falls back to built-in administrative credentials.
- **Default Credentials:**
  - `admin` / `admin123` (System Administrator)
  - `manager` / `manager123` (HR Operations Manager)
  - `hr` / `hr123` (HR Specialist)
- **One-Click Demo Login:** Quick auto-fill button for fast evaluation and testing.
- **Password Visibility Toggle:** Show / hide password support with real-time glyph masking.
- **Session Management:** Displays current authenticated user in the top navigation bar with full Sign Out and re-authentication support.

### 1. 📊 Executive Dashboard
- **Headcount Metric Cards:** Real-time summary tiles showcasing total employees and department breakdowns (IT, HR, Finance, Marketing).
- **Recent Staff Table:** Displays recently registered employees with dynamic row population.
- **Quick Actions:** Direct shortcuts to register new employees and refresh real-time statistics.

### 2. 👥 Employee Management (Full CRUD)
- **Interactive Directory Table:** Styled `JTable` rendering employee records with alternating row highlights and responsive column sizing.
- **Live Search & Filter:** Instant multi-field search filtering by ID, Name, Department, or Designation.
- **Master-Detail Selection:** Selecting any table row immediately loads the employee's data into the management form.
- **Create (Add):** Form validation ensuring unique IDs, positive non-zero salaries, and mandatory fields.
- **Read (View):** Displays all persistent records with formatted currency values.
- **Update (Modify):** Allows updating names, departments, designations, and salaries while protecting primary key integrity.
- **Delete (Remove):** Safely deletes records backed by a `JOptionPane` confirmation dialog.
- **Clear / Reset:** One-click form reset back to initial addition mode.

### 3. 🏢 Department Analytics
- **Workforce Distribution:** Visual overview of IT, Human Resources, Finance, and Marketing personnel.
- **Workforce Share (%):** Calculates and formats departmental percentage of total company headcount.
- **Interactive Department Drill-Down:** Clicking any department immediately filters and displays employees belonging to that department.
- **Dynamic Aggregation:** Computed dynamically from employee records without unnecessary database schemas.

### 4. 📈 Executive & Compensation Reports
- **Key Performance Indicators (KPIs):**
  - Total Staff Count
  - Average Company Salary
  - Highest Recorded Salary
  - Lowest Base Salary
  - Total Monthly / Annual Payroll
- **Department Payroll Table:** Summarizes headcount, average salary, total compensation, and payroll budget share per department.
- **Database Aggregate Operations:** Values are computed in MySQL using ANSI SQL functions (`COUNT`, `SUM`, `AVG`, `MIN`, `MAX`).
- **Interactive Database Config:** Quick access to configure credentials from the top bar and sidebar at any time.

---

## 🛠️ Technology Stack

| Component | Technology / Library | Description |
|---|---|---|
| **Programming Language** | Java (JDK 8 / 11 / 17 / 21 / 26+) | Core Java, OOP Principles |
| **GUI Framework** | Java Swing & AWT | Desktop GUI controls, layouts & events |
| **UI Design System** | Custom-painted Rounded Controls | Pill buttons, card panels, subpixel anti-aliasing |
| **Database** | MySQL 8.x / MariaDB | Relational Database Management System |
| **Persistence Driver** | MySQL Connector/J 8.3.0 | Type-4 JDBC driver (`lib/mysql-connector-j.jar`) |
| **Architecture Pattern** | DAO (Data Access Object) | Separation of GUI from Persistence |
| **Query Mechanism** | JDBC `PreparedStatement` | High security, SQL Injection prevention |
| **Build System** | Standalone (`javac` / scripts) | No Maven, Gradle, or external tools required |

---

## 🏗️ Design Pattern & Architecture

This project implements the **Data Access Object (DAO)** architectural pattern:

```
[Swing GUI Presentation Layer]
  - LoginFrame (Authentication Portal)
  - MainFrame (Top Navbar, CardLayout)
  - DashboardPanel, EmployeePanel, DepartmentPanel, ReportPanel
         │
         ▼ (Calls Business / Data Methods)
[Data Access Object (DAO) Layer]
  - EmployeeDAO.java
         │
         ▼ (Manages Connection & Executes PreparedStatements)
[Database Connectivity Layer]
  - DBConnection.java (Connection pooling / configuration fallback)
         │
         ▼ (JDBC Type-4 Driver)
[Relational Database]
  - MySQL Database (employee_management -> employees & users tables)
```

### Key OOP Principles Demonstrated:
1. **Encapsulation:** All fields in `Employee.java` (`employeeId`, `name`, `department`, `designation`, `salary`) are strictly private and accessible solely via getters and setters.
2. **Separation of Concerns:** Zero SQL code resides within Swing panels; all database interaction is encapsulated in `EmployeeDAO.java`.
3. **Data Integrity & Validation:** Input validation checks are applied before invoking persistence logic.
4. **Exception Handling:** Robust `try-with-resources` statements ensure automatic closure of `Connection`, `PreparedStatement`, and `ResultSet` objects, eliminating connection leaks.

---

## 📁 Project Structure

```
EmployeeManagementSystem/
├── src/
│   └── com/
│       └── ems/
│           ├── Main.java               # Application entry point & L&F initialization
│           ├── LoginFrame.java         # Authentication window with role & DB status
│           ├── MainFrame.java          # Primary application window with navbar & session
│           ├── Employee.java           # Model class with encapsulation & formatting
│           ├── DBConnection.java       # JDBC connection manager & credential loader
│           ├── EmployeeDAO.java        # DAO implementation (CRUD & aggregate queries)
│           ├── UIUtils.java            # Rounded buttons, design system & palettes
│           ├── DashboardPanel.java     # KPI cards & recent employee activity
│           ├── EmployeePanel.java      # Main CRUD form, search bar & directory table
│           ├── DepartmentPanel.java    # Department workforce analysis & drill-down
│           └── ReportPanel.java        # Compensation statistics & SQL aggregate reports
├── lib/
│   └── mysql-connector-j.jar          # MySQL Connector/J JDBC Driver (included)
├── database.sql                        # SQL initialization script (employees & users)
├── db.properties                       # Optional external configuration for DB credentials
├── run.bat                             # Windows batch script to compile & launch EMS
└── README.md                           # Comprehensive documentation (this file)
```

---

## 💾 Database Setup Instructions

### 1. Open MySQL Command Line or MySQL Workbench
Launch the **MySQL Command Line Client** or open **MySQL Workbench**.

### 2. Execute `database.sql`
Run the included SQL script located in the project root:

**Option A — In MySQL Workbench:**
1. Open MySQL Workbench and connect to your local MySQL instance.
2. Go to **File -> Open SQL Script...** and select `database.sql`.
3. Click the ⚡ **Execute** button (or press `Ctrl + Shift + Enter`).

**Option B — In MySQL Command Prompt:**
```bash
mysql -u root -p < database.sql
```

### Table Schema:
The script creates the database `employee_management` and table `employees`:

```sql
CREATE DATABASE IF NOT EXISTS employee_management;
USE employee_management;

CREATE TABLE IF NOT EXISTS employees (
    employee_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    salary DECIMAL(10, 2) NOT NULL
);
```
*(6 sample employee records across IT, HR, Finance, and Marketing are automatically populated for academic demonstration).*

---

## 🔐 Database Credential Configuration

You have **three flexible and safe options** to set your MySQL password without hardcoding secrets:

### Option 1: Edit `db.properties` (Recommended)
Edit the `db.properties` file in the project root directory:
```properties
db.url=jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_MYSQL_PASSWORD_HERE
```

### Option 2: From the Running GUI
1. Launch the application.
2. In the bottom-left corner of the sidebar, click the **⚙ DB Credentials** button.
3. Enter your MySQL password and click **OK**.
4. The application will test the connection and immediately refresh all tables.

### Option 3: Modify `DBConnection.java`
Open `src/com/ems/DBConnection.java` and adjust the default constants:
```java
private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
private static final String DEFAULT_USER = "root";
private static final String DEFAULT_PASSWORD = "YOUR_MYSQL_PASSWORD";
```

---

## 🚀 How to Compile and Run

### Method 1: Using 1-Click Batch Script (Windows)
Double-click `run.bat` (or open Command Prompt and execute):
```cmd
run.bat
```

### Method 2: Using PowerShell
Run the included PowerShell script:
```powershell
.\run.ps1
```

### Method 3: Standard Terminal / Command Prompt Commands

**Step 1: Compile the Java source files**
```cmd
mkdir bin
javac -d bin -cp ".;lib/mysql-connector-j.jar" src/com/ems/*.java
```

**Step 2: Run the Application**
```cmd
java -cp "bin;lib/mysql-connector-j.jar" com.ems.Main
```

---

## 🛡️ Input Validation & Error Handling

| Scenario | Handled By | Behavior |
|---|---|---|
| **Empty Employee ID** | Validation logic | Alerts user via `JOptionPane`; focuses input |
| **Empty Name / Designation** | Validation logic | Rejects submission; prompts for valid text |
| **Unselected Department** | Validation logic | Requires selection from active departments |
| **Non-numeric / Negative Salary** | `NumberFormatException` & logic | Rejects input; prompts for positive decimal |
| **Duplicate Employee ID** | `EmployeeDAO.isEmployeeIdExists` | Prevents SQL primary key collision gracefully |
| **Database Disconnection** | `SQLException` catch blocks | Displays visual diagnostic indicators without crashing |
| **Record Deletion** | Confirmation dialog | Prompts confirmation before permanent removal |

---

## 🔮 Future Improvements
- User authentication and role-based access control (Admin vs. Standard Employee).
- Export reports to PDF or Excel formats.
- Profile picture attachment support using BLOB storage or file paths.
- Dark / Light UI theme toggle.
