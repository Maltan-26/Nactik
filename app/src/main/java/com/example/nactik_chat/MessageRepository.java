package com.example.nactik_chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageRepository {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "MessageRepository";

    public MessageRepository() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public long saveMessage(Message message) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "INSERT INTO messages (room_id, sender_id, message_text, timestamp, time_string, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, message.getRoomId());
                stmt.setLong(2, message.getSenderUid());
                stmt.setString(3, message.getMessageText());
                stmt.setLong(4, message.getTimestamp());
                stmt.setString(5, message.getTimeString());
                stmt.setBoolean(6, message.isRead());

                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error saving message", e);
        }
        return -1;
    }


    public List<Message> getMessagesForRoom(String roomId, long lastTimestamp) {
        List<Message> messages = new ArrayList<>();
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:00:40

        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }

        try (Connection connection = dbHelper.getConnection()) {
            String query = "SELECT message_id, message_text, sender_uid, " +
                    "timestamp, time_string, room_id, is_read, " +
                    "is_delivered, message_type, media_url " +
                    "FROM messages " +
                    "WHERE room_id = ? AND timestamp > ? " +
                    "ORDER BY timestamp ASC";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, roomId);
                stmt.setLong(2, lastTimestamp);
                Log.d(TAG, String.format("Executing query at %s for room: %s", currentTime, roomId));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs != null && rs.next()) {
                        try {
                            Message message = new Message();
                            message.setId(rs.getLong("message_id"));
                            message.setRoomId(rs.getString("room_id"));
                            message.setSenderId(rs.getLong("sender_uid"));
                            message.setMessage(rs.getString("message_text"));
                            message.setTimestamp(rs.getLong("timestamp"));
                            message.setCurrenttime(rs.getString("time_string"));
                            message.setRead(rs.getBoolean("is_read"));
                            message.setDelivered(rs.getBoolean("is_delivered"));
                            message.settype(rs.getString("message_type"));
                            message.setmedia_url(rs.getString("media_url"));
                            messages.add(message);
                        } catch (SQLException e) {
                            Log.e(TAG, String.format("Error parsing message at %s: %s",
                                    currentTime, e.getMessage()));
                        }
                    }
                }
            }

            Log.d(TAG, String.format("Retrieved %d messages at %s for room %s",
                    messages.size(), currentTime, roomId));

            return messages;

        } catch (SQLException e) {
            String errorMsg = String.format("Database error at %s for room %s: %s",
                    currentTime, roomId, e.getMessage());
            Log.e(TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }


    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 07:53:04

        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {

            Log.e(TAG, String.format("Error closing resources at %s: %s",
                    currentTime, e.getMessage()));
        }
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

    public void markMessagesAsRead(String roomId, String userId) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "UPDATE messages SET is_read = true " +
                    "WHERE room_id = ? AND sender_id != ? AND is_read = false";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, roomId);
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error marking messages as read", e);
        }
    }

    public void deleteMessage(long messageId) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "DELETE FROM messages WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, messageId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting message", e);
        }
    }}
