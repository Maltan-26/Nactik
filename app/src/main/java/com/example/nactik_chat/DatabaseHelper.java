package com.example.nactik_chat;

import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "chat_database";
    private static final int DATABASE_VERSION = 1;
    private static final int CONNECTION_TIMEOUT = 30; // seconds
    private static final String CURRENT_USER = "Maltan-26";

    private static DatabaseHelper instance;
    private Connection connection;
    private final ReentrantLock connectionLock;
    private long lastConnectionTime;

    private DatabaseHelper() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58
        Log.d(TAG, String.format("Initializing DatabaseHelper at %s for user %s",
                currentTime, CURRENT_USER));

        this.connectionLock = new ReentrantLock();
        initializeDatabase();
    }

    private void initializeDatabase() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58

        try {
            // Create initial connection
            createConnection();


            Log.d(TAG, String.format("Database initialized at %s", currentTime));
        } catch (Exception e) {
            String errorMsg = String.format("Error initializing database at %s: %s",
                    currentTime, e.getMessage());
            Log.e(TAG, errorMsg, e);
        }
    }

    private void executeSchemaFile(String filename) {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58

        try {
            Log.d(TAG, String.format("Executing schema file at %s: %s", currentTime, filename));
            // Implementation for executing SQL files
            // Add your implementation here
        } catch (Exception e) {
            String errorMsg = String.format("Error executing schema file at %s: %s - %s",
                    currentTime, filename, e.getMessage());
            Log.e(TAG, errorMsg, e);
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private void createConnection() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            Log.d(TAG, String.format("Attempting database connection at %s for user %s",
                    currentTime, CURRENT_USER));

            // Establish connection
            connection = DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );

            // Verify connection
            if (connection != null && connection.isValid(CONNECTION_TIMEOUT)) {
                lastConnectionTime = System.currentTimeMillis();
                Log.d(TAG, String.format("Database connection successful at %s", currentTime));
            } else {
                throw new SQLException("Connection created but not valid");
            }

        } catch (ClassNotFoundException e) {
            String errorMsg = String.format("MySQL JDBC Driver not found at %s: %s",
                    currentTime, e.getMessage());
            Log.e(TAG, errorMsg);
            throw new RuntimeException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = String.format("Database connection failed at %s: %s\nCause: %s",
                    currentTime, e.getMessage(),
                    (e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
            Log.e(TAG, errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public Connection getConnection() throws SQLException {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58

        connectionLock.lock();
        try {
            // Check if connection needs refresh
            boolean shouldRefresh = connection == null || connection.isClosed() || !connection.isValid(CONNECTION_TIMEOUT) || isConnectionStale();

            if (shouldRefresh) {
                Log.d(TAG, String.format("Creating new database connection at %s", currentTime));
                closeConnection();
                createConnection();
            }

            // Verify connection
            if (connection != null && connection.isValid(CONNECTION_TIMEOUT)) {
                return connection;
            } else {
                String errorMsg = String.format("Invalid connection at %s", currentTime);
                Log.e(TAG, errorMsg);
                throw new SQLException(errorMsg);
            }

        } finally {
            connectionLock.unlock();
        }
    }

    private boolean isConnectionStale() {
        // Consider connection stale after 15 minutes
        return System.currentTimeMillis() - lastConnectionTime > 900000;
    }

    private void closeConnection() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58

        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    Log.d(TAG, String.format("Database connection closed at %s", currentTime));
                }
            } catch (SQLException e) {
                String errorMsg = String.format("Error closing connection at %s: %s",
                        currentTime, e.getMessage());
                Log.e(TAG, errorMsg);
            } finally {
                connection = null;
            }
        }
    }

    public void cleanup() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:29:58
        Log.d(TAG, String.format("Cleaning up database resources at %s", currentTime));
        closeConnection();
    }
}