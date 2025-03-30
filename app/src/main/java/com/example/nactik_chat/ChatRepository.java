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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public String createChatRoom(String user1Id, String user2Id) {
        String roomId = generateRoomId(user1Id, user2Id);
        try (Connection conn = dbHelper.getConnection()) {
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
                stmt2.setString(2, user1Id);
                stmt2.executeUpdate();

                // Add second participant
                stmt2.setString(1, roomId);
                stmt2.setString(2, user2Id);
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
    public String generateRoomId(String user1Id, String user2Id) {
        // Sort IDs to ensure consistent room ID regardless of order
        String[] ids = {user1Id, user2Id};
        Arrays.sort(ids);
        return String.format("%s_%s", ids[0], ids[1]);
    }
    public void sendMessage(String senderRoom, String senderUid, String message, long timestamp, String timeString) {
        try (Connection conn = dbHelper.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Insert message
                String sql = "INSERT INTO messages (room_id, sender_uid, message_text, timestamp, time_string) " +
                        "VALUES (?, ?, ?, ?, ?)";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, senderRoom);
                stmt.setString(2, senderUid);
                stmt.setString(3, message);
                stmt.setLong(4, timestamp);
                stmt.setString(5, timeString);
                stmt.executeUpdate();

                // Update last message in chat room
                String updateSql = "UPDATE chat_rooms SET last_message = ?, last_message_time = ? " +
                        "WHERE room_id = ?";

                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setString(1, message);
                updateStmt.setLong(2, timestamp);
                updateStmt.setString(3, senderRoom);
                updateStmt.executeUpdate();

                // Commit transaction
                conn.commit();

                Log.d("ChatRepository", "Message sent successfully at ");

            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                Log.e("ChatRepository", "Failed to send message: " + e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Message> getMessages(String roomId) {
        List<Message> messages = new ArrayList<>();

        try (Connection conn = dbHelper.getConnection()) {
            String sql = "SELECT m.*, u.username as sender_name " +
                    "FROM messages m " +
                    "LEFT JOIN users u ON m.sender_uid = u.user_id " +
                    "WHERE m.room_id = ? " +
                    "ORDER BY m.timestamp DESC " +
                    "LIMIT ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, roomId);
                stmt.setInt(2, MESSAGE_FETCH_LIMIT);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Message message = new Message(
                            rs.getString("message_text"),
                            rs.getString("sender_uid"),
                            rs.getLong("timestamp"),
                            rs.getString("time_string")
                    );
                    // Set additional properties if needed

                }
            }

            Log.d(TAG, "Retrieved " + messages.size() + " messages for room " +
                    roomId + " at " );

        } catch (SQLException e) {
            Log.e(TAG, "Error getting messages: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }
    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(
                    DatabaseConfig.DB_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }
    }

    public void createChatRoomAsync(String user1Id, String user2Id, ChatCallback<String> callback) {
        executorService.execute(() -> {
            try {
                String roomId = generateRoomId(user1Id, user2Id);
                try (Connection conn = getConnection()) {
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
                            stmt.setString(2, user1Id);
                            stmt.executeUpdate();

                            stmt.setString(1, roomId);
                            stmt.setString(2, user2Id);
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

    public void sendMessageAsync(String roomId, String senderId, String message,
                                 long timestamp, String timeString, ChatCallback<Void> callback) {
        executorService.execute(() -> {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // Insert message
                    String sql = "INSERT INTO messages (room_id, sender_uid, message_text, timestamp, time_string) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, roomId);
                        stmt.setString(2, senderId);
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
                try (Connection conn = getConnection()) {
                    String sql = "SELECT m.*, u.username as sender_name " +
                            "FROM messages m " +
                            "LEFT JOIN users u ON m.sender_uid = u.user_id " +
                            "WHERE m.room_id = ? " +
                            "ORDER BY m.timestamp DESC " +
                            "LIMIT ?";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, roomId);
                        stmt.setInt(2, MESSAGE_FETCH_LIMIT);

                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                Message message = new Message(
                                        rs.getString("message_text"),
                                        rs.getString("sender_uid"),
                                        rs.getLong("timestamp"),
                                        rs.getString("time_string")
                                );
                                messages.add(message);
                            }
                        }
                    }
                }
                new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(messages));
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public List<Message> getRecentMessages(String roomId, long lastMessageTimestamp) {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "SELECT * FROM messages WHERE room_id = ? AND timestamp > ? " +
                    "ORDER BY timestamp DESC LIMIT ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomId);
            stmt.setLong(2, lastMessageTimestamp);
            stmt.setInt(3, MESSAGE_FETCH_LIMIT);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(new Message(
                        rs.getString("message_text"),
                        rs.getString("sender_uid"),
                        rs.getLong("timestamp"),
                        rs.getString("time_string")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}