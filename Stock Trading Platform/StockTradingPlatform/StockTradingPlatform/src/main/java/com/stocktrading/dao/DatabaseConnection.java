package com.stocktrading.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single shared JDBC connection to the MySQL database.
 * Implements a simple Singleton-style accessor so all DAOs reuse one connection.
 */
public class DatabaseConnection {

    // ── Configuration ─────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/stock_trading_db"
                                         + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";          // change if needed
    private static final String PASSWORD = "Root@123";          // change to your MySQL password

    // ── Singleton instance ────────────────────────────────────
    private static Connection connection = null;

    /** Private constructor – not instantiable. */
    private DatabaseConnection() {}

    /**
     * Returns the shared Connection, creating it on first call.
     *
     * @return live {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load MySQL JDBC driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Connected to MySQL database: stock_trading_db");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. "
                        + "Add mysql-connector-java to your classpath.", e);
            }
        }
        return connection;
    }

    /**
     * Gracefully closes the shared connection.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}
