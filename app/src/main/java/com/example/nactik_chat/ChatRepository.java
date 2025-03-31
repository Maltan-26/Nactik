package com.example.nactik_chat;


import static android.content.ContentValues.TAG;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatRepository {
    private DatabaseHelper dbHelper;
    private static final int MESSAGE_FETCH_LIMIT = 50;
    private final ExecutorService executorService;


    public ChatRepository() {
        this.executorService = Executors.newFixedThreadPool(4);
        dbHelper = DatabaseHelper.getInstance();
    }

    public String createChatRoom(Long user1Id, Long user2Id) {
        String roomId = generateRoomId(user1Id, user2Id);
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            try {
                // Create chat room
                String sql1 = "INSERT INTO chat_rooms (room_id) VALUES (?)";
                PreparedStatement stmt1 = conn.prepareStatement(sql1);
                stmt1.setString(1, roomId);
                stmt1.executeUpdate();

                // Add participants
                String sql2 = "INSERT INTO room_participants (room_id, user_id) VALUES (?, ?)";
                PreparedStatement stmt2 = conn.prepareStatement(sql2);

                // Add first participant
                stmt2.setString(1, roomId);
                stmt2.setLong(2, user1Id);
                stmt2.executeUpdate();

                // Add second participant
                stmt2.setString(1, roomId);
                stmt2.setLong(2, user2Id);
                stmt2.executeUpdate();

                conn.commit();
                return roomId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public String generateRoomId(Long user1Id, Long user2Id) {
        // Sort IDs to ensure consistent room ID regardless of order
        String[] ids = {String.valueOf(user1Id), String.valueOf(user2Id)};
        Arrays.sort(ids);
        // Use string concatenation instead of String.format
        return ids[0] + "_" + ids[1];
    }
    public static String getCurrentUTCTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error generating UTC timestamp: " + e.getMessage());
            return "";
        }
    }
    public long saveMessage(Message message) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseHelper.getInstance().getConnection();

            String sql = "INSERT INTO messages (room_id, sender_uid, message_text, " +
                    "timestamp, time_string, message_type, media_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, message.getRoomId());
            stmt.setLong(2, message.getSenderUid());
            stmt.setString(3, message.getMessageText());
            stmt.setLong(4, System.currentTimeMillis());
            stmt.setString(5, getCurrentUTCTime());
            stmt.setString(6, message.getMessageType());
            stmt.setString(7, message.getMediaUrl());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                long messageId = rs.getLong(1);

                return messageId;
            } else {
                throw new SQLException("Creating message failed, no ID obtained.");
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error saving message: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }



    public void createChatRoomAsync(Long user1Id, Long user2Id, ChatCallback<String> callback) {
        executorService.execute(() -> {
            try {
                String roomId = generateRoomId(user1Id, user2Id);
                try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
                    conn.setAutoCommit(false);
                    try {
                        // Create chat room
                        String sql1 = "INSERT INTO chat_rooms (room_id) VALUES (?) ON CONFLICT DO NOTHING";
                        try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                            stmt.setString(1, roomId);
                            stmt.executeUpdate();
                        }

                        // Add participants
                        String sql2 = "INSERT INTO room_participants (room_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
                        try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                            stmt.setString(1, roomId);
                            stmt.setLong(2, user1Id);
                            stmt.executeUpdate();

                            stmt.setString(1, roomId);
                            stmt.setLong(2, user2Id);
                            stmt.executeUpdate();
                        }

                        conn.commit();
                        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(roomId));
                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    }
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    public void sendMessageAsync(String roomId, long senderId, String message,
                                 long timestamp, String timeString, ChatCallback<Void> callback) {
        executorService.execute(() -> {
            try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Insert message
                    String sql = "INSERT INTO messages (room_id, sender_uid, message_text, timestamp, time_string) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, roomId);
                        stmt.setLong(2, senderId);
                        stmt.setString(3, message);
                        stmt.setLong(4, timestamp);
                        stmt.setString(5, timeString);
                        stmt.executeUpdate();
                    }

                    // Update last message
                    String updateSql = "UPDATE chat_rooms SET last_message = ?, last_message_time = ? " +
                            "WHERE room_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.setString(1, message);
                        stmt.setLong(2, timestamp);
                        stmt.setString(3, roomId);
                        stmt.executeUpdate();
                    }

                    conn.commit();
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(null));
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    public void getMessagesAsync(String roomId, ChatCallback<List<Message>> callback) {
        executorService.execute(() -> {
            try {
                List<Message> messages = new ArrayList<>();
                try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
                    String sql = "SELECT m.*, u.username as sender_name, u.profile_image_url " +
                            "FROM messages m " +
                            "LEFT JOIN users u ON m.sender_uid = u.user_id " +
                            "WHERE m.room_id = ? " +
                            "ORDER BY m.timestamp DESC " +
                            "LIMIT ?";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, roomId);
                        stmt.setInt(2, MESSAGE_FETCH_LIMIT);

                        Log.d(TAG, String.format("Fetching messages for room %s at %s",
                                roomId, "2025-03-30 16:03:28"));

                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                Message message = new Message(
                                        rs.getLong("message_id"),
                                        rs.getString("room_id"),
                                        rs.getLong("sender_uid"),
                                        rs.getString("message_text"),
                                        rs.getLong("timestamp"),
                                        rs.getString("time_string"),
                                        rs.getBoolean("is_read"),
                                        rs.getBoolean("is_delivered"),
                                        rs.getString("message_type"),
                                        rs.getString("media_url")
                                );

                                // Set additional user information
                                message.setSenderName(rs.getString("sender_name"));
                                message.setSenderProfileImage(rs.getString("profile_image_url"));

                                messages.add(message);
                            }
                        }
                    }
                }

                // Log success
                Log.d(TAG, String.format("Successfully fetched %d messages for room %s - User: %s",
                        messages.size(), roomId, "Maltan-26"));

                // Return results on main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (messages.isEmpty()) {
                        Log.i(TAG, "No messages found for room " + roomId);
                    }
                    callback.onSuccess(messages);
                });

            } catch (Exception e) {
                Log.e(TAG, "Error fetching messages: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public List<Message> getRecentMessages(String roomId, long lastMessageId) {
        List<Message> messages = new ArrayList<>();
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 06:46:59

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            connection = DatabaseHelper.getInstance().getConnection();
            if (connection == null) {
                throw new SQLException("Database connection is null");
            }

            String query = "SELECT m.*, u.username FROM messages m " +
                    "JOIN users u ON m.user_id = u.id " +
                    "WHERE m.room_id = ? AND m.message_id > ? " +
                    "ORDER BY m.timestamp DESC LIMIT 50";

            stmt = connection.prepareStatement(query);
            stmt.setString(1, roomId);
            stmt.setLong(2, lastMessageId);

            Log.d(TAG, String.format("Executing query at %s for room: %s", currentTime, roomId));
            rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                        rs.getLong("message_id"),
                        rs.getString("room_id"),
                        rs.getLong("sender_uid"),
                        rs.getString("message_text"),
                        rs.getLong("timestamp"),
                        rs.getString("time_string"),
                        rs.getBoolean("is_read"),
                        rs.getBoolean("is_delivered"),
                        rs.getString("message_type"),
                        rs.getString("media_url")
                );
                messages.add(message);
            }

            Log.d(TAG, String.format("Retrieved %d messages at %s", messages.size(), currentTime));
            return messages;

        } catch (SQLException e) {
            String errorMsg = String.format("Database error at %s: %s", currentTime, e.getMessage());
            Log.e(TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        } finally {
            closeResources(connection, stmt, rs);
        }
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            Log.e(TAG, "Error closing resources: " + e.getMessage());
        }
    }
}
