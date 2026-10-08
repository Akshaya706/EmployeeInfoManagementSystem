package com.ems;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages the MySQL JDBC connection for the Employee Information Management System.
 * 
 * =========================================================================================
 * DATABASE CONFIGURATION INSTRUCTIONS:
 * 1. By default, this application connects to MySQL on localhost:3306 with database: employee_management
 * 2. Update the DB_USER and DB_PASSWORD constants below to match your MySQL credentials,
 *    OR create a 'db.properties' file in the project root directory with:
 *       db.url=jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
 *       db.user=root
 *       db.password=YOUR_MYSQL_PASSWORD
 * =========================================================================================
 */
public class DBConnection {

    // -------------------------------------------------------------------------------------
    // CONFIGURE YOUR DATABASE CONNECTION DETAILS HERE:
    // -------------------------------------------------------------------------------------
    private static final String DEFAULT_URL = 
            "jdbc:mysql://localhost:3306/employee_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    
    // Set your MySQL root password here (e.g., "root", "admin", "password", or "" for blank)
    private static final String DEFAULT_PASSWORD = "YOUR_MYSQL_PASSWORD";
    // -------------------------------------------------------------------------------------

    private static String dbUrl = DEFAULT_URL;
    private static String dbUser = DEFAULT_USER;
    private static String dbPassword = DEFAULT_PASSWORD;
    private static boolean driverLoaded = false;

    static {
        // Attempt to load settings from db.properties if available
        loadProperties();
        // Load MySQL Driver
        loadDriver();
    }

    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver class 'com.mysql.cj.jdbc.Driver' not found. " +
                               "Ensure mysql-connector-j.jar is included in the classpath.");
            driverLoaded = false;
        }
    }

    /**
     * Attempts to read configuration from db.properties in the working directory
     * or project root, or environment variables.
     */
    public static void loadProperties() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream in = new FileInputStream(propFile)) {
                Properties props = new Properties();
                props.load(in);
                if (props.getProperty("db.url") != null) {
                    dbUrl = props.getProperty("db.url").trim();
                }
                if (props.getProperty("db.user") != null) {
                    dbUser = props.getProperty("db.user").trim();
                }
                if (props.getProperty("db.password") != null) {
                    dbPassword = props.getProperty("db.password").trim();
                }
            } catch (Exception e) {
                System.err.println("Notice: Could not load db.properties, using configured defaults.");
            }
        } else {
            // Optional fallback to environment variable if present
            String envPass = System.getenv("MYSQL_PWD");
            if (envPass != null && !envPass.isEmpty()) {
                dbPassword = envPass;
            }
        }
    }

    /**
     * Obtains a new JDBC Connection to MySQL.
     * Uses try-with-resources in calling methods to properly close connections.
     * 
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests if the database connection can be established successfully.
     * @return true if connected, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Allows dynamic updates to credentials at runtime (e.g., from a settings dialog).
     */
    public static void setCredentials(String url, String user, String password) {
        if (url != null && !url.trim().isEmpty()) {
            dbUrl = url.trim();
        }
        if (user != null && !user.trim().isEmpty()) {
            dbUser = user.trim();
        }
        if (password != null) {
            dbPassword = password;
        }
    }

    // Getters for non-sensitive connection details
    public static String getDbUrl() {
        return dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }
}
