package com.example.nactik_chat;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "MessageRepository";

    public MessageRepository() {
        dbHelper = DatabaseHelper.getInstance();
    }

    public long saveMessage(Message message) {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "INSERT INTO messages (room_id, sender_id, message_text, timestamp, time_string, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, message.getRoomId());
                stmt.setString(2, message.getSenderId());
                stmt.setString(3, message.getMessage());
                stmt.setLong(4, message.getTimestamp());
                stmt.setString(5, message.getCurrenttime());
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

    public List<Message> getMessagesForRoom(String roomId, long lastMessageTimestamp) {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "SELECT * FROM messages WHERE room_id = ? AND timestamp > ? " +
                    "ORDER BY timestamp ASC LIMIT 50";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, roomId);
                stmt.setLong(2, lastMessageTimestamp);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getLong("id"));
                    message.setRoomId(rs.getString("room_id"));
                    message.setSenderId(rs.getString("sender_id"));
                    message.setMessage(rs.getString("message_text"));
                    message.setTimestamp(rs.getLong("timestamp"));
                    message.setCurrenttime(rs.getString("time_string"));
                    message.setRead(rs.getBoolean("is_read"));
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error fetching messages", e);
        }
        return messages;
    }

    public void markMessagesAsRead(String roomId, String userId) {
        try (Connection conn = dbHelper.getConnection()) {
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
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "DELETE FROM messages WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, messageId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting message", e);
        }
    }
}