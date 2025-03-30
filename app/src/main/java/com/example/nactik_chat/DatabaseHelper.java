package com.example.nactik_chat;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {
    private static final String DATABASE_NAME = "chat_database";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper instance;
    private Connection connection;

    private DatabaseHelper() {
        // Initialize your database connection here
        initializeDatabase();
    }



    private void initializeDatabase() {
        try {
            // Execute schema.sql and views.sql
            executeSchemaFile("database/schema.sql");
            executeSchemaFile("database/views.sql");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error initializing database", e);
        }
    }

    private void executeSchemaFile(String filename) {
        try {
            // Read and execute SQL file
            // Implementation depends on your database setup
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error executing schema file: " + filename, e);
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    private void createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            connection = DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );
            Log.d("DatabaseHelper", "Database connection successful");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Database connection failed", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }
        return connection;
    }
}