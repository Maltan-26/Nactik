package com.example.nactik_chat;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final DatabaseHelper dbHelper;

    public UserRepository() {
        this.dbHelper = DatabaseHelper.getInstance();
    }

    public User getUserById(long userId) throws SQLException {
        try (Connection conn =DatabaseHelper.getInstance().getConnection()) {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new User(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("profile_image_url"),
                            rs.getString("status"),
                            rs.getString("phone_number")
                    );
                }
                throw new SQLException("User not found");
            }
        }
    }

    public List<User> searchUsersByPhone(String phoneQuery) throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            // Using LIKE for partial phone number matching
            String sql = "SELECT * FROM users WHERE phone_number LIKE ? " +  // Exclude current user
                    "ORDER BY username ASC " +
                    "LIMIT 20";  // Limit results for performance

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + phoneQuery + "%"); // Exclude current user from results

                Log.d(TAG, "Searching users with phone query: " + phoneQuery + " at " );

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    User user = new User(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("profile_image_url"),
                            rs.getString("status"),
                            rs.getString("phone_number")
                    );
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error searching users by phone: " + e.getMessage());
            throw e;
        }

        Log.d(TAG, "Found " + users.size() + " users matching query: " + phoneQuery);
        return users;
    }

    public void updateUser(User user) throws SQLException {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "UPDATE users SET username = ?, profile_image_url = ?, status = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getImageUrl());
                stmt.setString(3, user.getStatus());
                stmt.setLong(4, user.getUid());
                int updated = stmt.executeUpdate();
                if (updated == 0) {
                    throw new SQLException("User update failed");
                }
            }
        }
    }


    public void updateUserStatus(long userId, String status) throws SQLException {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "UPDATE users SET status = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setLong(2, userId);
                stmt.executeUpdate();
            }
        }
    }
}