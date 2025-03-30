package com.example.nactik_chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {
    private final DatabaseHelper dbHelper;

    public UserRepository() {
        this.dbHelper = DatabaseHelper.getInstance();
    }

    public User getUserById(String userId) throws SQLException {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "SELECT * FROM users WHERE uid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new User(
                            rs.getString("uid"),
                            rs.getString("name"),
                            rs.getString("image_url"),
                            rs.getString("status")
                    );
                }
                throw new SQLException("User not found");
            }
        }
    }

    public void updateUser(User user) throws SQLException {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "UPDATE users SET name = ?, image_url = ?, status = ? WHERE uid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getImageUrl());
                stmt.setString(3, user.getStatus());
                stmt.setString(4, user.getUid());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("User update failed");
                }
            }
        }
    }

    public void updateUserStatus(String userId, String status) throws SQLException {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "UPDATE users SET status = ? WHERE uid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }
        }
    }
}