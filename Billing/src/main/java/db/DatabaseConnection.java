package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class
 */
public class DatabaseConnection {
    // Update these values with your actual database information
    private static final String URL = "jdbc:mysql://localhost:3306/billing_system";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "password";

    // Static block to load the driver once when the class is loaded
    static {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver registered successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading MySQL JDBC driver: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception here to allow compilation, but getConnection will fail
        }
    }

    /**
     * Get a connection to the database
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            // Test that connection is valid
            if (conn.isValid(5)) {
                return conn;
            } else {
                throw new SQLException("Database connection validation failed");
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to database: " + e.getMessage(), e);
        }
    }
}
