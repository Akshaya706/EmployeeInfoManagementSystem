package com.ems;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for the Employee Information Management System.
 * Enables high-DPI text antialiasing, initializes Look-and-Feel, and launches
 * the main window on the Swing Event Dispatch Thread.
 */
public class Main {

    public static void main(String[] args) {
        // Enable subpixel text antialiasing for crisp modern web typography
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set System Look and Feel for native operating system rendering
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Notice: Could not set System Look and Feel, using default Swing L&F.");
        }

        // Print academic project startup header
        System.out.println("==================================================================");
        System.out.println(" Employee Information Management System (EMS)");
        System.out.println(" College Academic Project - Java Swing & MySQL JDBC");
        System.out.println("==================================================================");
        System.out.println("Starting application interface...");

        // Launch GUI safely on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                System.out.println("Login portal successfully launched and ready.");
            } catch (Exception e) {
                System.err.println("Fatal error initializing EMS interface: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
