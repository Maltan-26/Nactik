package com.example.nactik_chat;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.content.ContentValues;
import android.util.Log;

import androidx.constraintlayout.widget.Constraints;

import java.sql.Connection;
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
import java.util.concurrent.TimeUnit;

public class UserRepository {
    private final DatabaseHelper dbHelper;
    private final ExecutorService executorService;
    public UserRepository() {
        this.executorService = Executors.newCachedThreadPool();
        this.dbHelper = DatabaseHelper.getInstance();
    }
    public String generateRoomId(Long user1Id, Long user2Id) {
        // Sort IDs to ensure consistent room ID regardless of order
        String[] ids = {String.valueOf(user1Id), String.valueOf(user2Id)};
        Arrays.sort(ids);
        // Use string concatenation instead of String.format
        return ids[0] + "_" + ids[1];
    }
    public String generateRoomIdr(Long user1Id, Long user2Id) {
        // Sort IDs to ensure consistent room ID regardless of order
        String[] ids = {String.valueOf(user1Id), String.valueOf(user2Id)};
        Arrays.sort(ids);
        // Use string concatenation instead of String.format
        return ids[1] + "_" + ids[0];
    }
    public List<ChatRoom> getChatRoomsForUser(Long userId) throws SQLException {
        List<ChatRoom> chatRooms = new ArrayList<>();

        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = " SELECT cr.room_id, CASE WHEN cr.is_group THEN cr.last_message ELSE (SELECT u.username FROM room_participants rp JOIN users u ON u.user_id = rp.user_id WHERE rp.room_id = cr.room_id AND rp.user_id != ? LIMIT 1)END as room_name, cr.last_message, cr.last_message_time, (SELECT u.profile_image_url FROM users u JOIN room_participants rp ON rp.user_id = u.user_id WHERE rp.room_id = cr.room_id AND rp.user_id != ? LIMIT 1) as profile_image_url,(SELECT u.status FROM users u JOIN room_participants rp ON rp.user_id = u.user_id WHERE rp.room_id = cr.room_id AND rp.user_id != ? LIMIT 1) as user_status, (SELECT u.user_id FROM users u JOIN room_participants rp ON rp.user_id = u.user_id WHERE rp.room_id = cr.room_id AND rp.user_id != ? LIMIT 1) as uid, ( SELECT COUNT(*) FROM messages m WHERE m.room_id = cr.room_id AND m.is_read = FALSE AND m.sender_uid != ?) as unread_count FROM chat_rooms cr JOIN room_participants rp ON rp.room_id = cr.room_id WHERE rp.user_id = ? GROUP BY cr.room_id ORDER BY cr.last_message_time ASC ";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set the userId parameter for all placeholders
                stmt.setString(1, userId.toString()); // For room_name subquery
                stmt.setString(2, userId.toString()); // For profile_image_url subquery
                stmt.setString(3, userId.toString()); // For user_status subquery
                stmt.setString(4, userId.toString()); // For unread_count subquery
                stmt.setString(5, userId.toString()); // For main WHERE clause
                stmt.setString(6, userId.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println(rs.getInt("uid")+"is the user ---------");
                    ChatRoom room = new ChatRoom(
                            rs.getString("room_id"),
                            rs.getString("room_name"),
                            rs.getString("last_message"),
                            String.valueOf(rs.getLong("last_message_time")),
                            rs.getString("profile_image_url"),
                            "Online".equals(rs.getString("user_status")),
                            rs.getInt("unread_count"),
                            rs.getInt("uid"),
                            userId
                    );
                    chatRooms.add(room);
                }
            }
        }
        System.out.println(chatRooms);

        return chatRooms;
    }
    public String createChatRoom(Long user1Id, Long user2Id) throws SQLException {
        String roomId = generateRoomId(user1Id, user2Id);
        try (Connection conn = dbHelper.getConnection()) {
            // Start transaction
            String sql1 = "INSERT INTO chat_rooms (room_id) VALUES (?)";
            try (PreparedStatement stmt1 = conn.prepareStatement(sql1)) {
                stmt1.setString(1, roomId);
                stmt1.executeUpdate();
            } catch (SQLException e) {
                Log.e(Constraints.TAG, "Error inserting chat_room: " + e.getMessage());
                throw e;
            }
            String sql2 = "INSERT INTO room_participants (room_id, user_id) VALUES (?, ?)";
            try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {

                // Add first participant
                stmt2.setString(1, roomId);
                stmt2.setLong(2, user1Id);
                stmt2.executeUpdate();

                // Add second participant
                stmt2.setString(1, roomId);
                stmt2.setLong(2, user2Id);
                stmt2.executeUpdate();

            } catch (SQLException e) {
                Log.e(Constraints.TAG, "Error inserting chat_room: " + e.getMessage());
                throw e;
            }

        } catch (SQLException e) {
            Log.e(Constraints.TAG, "Error inserting chat_room: " + e.getMessage());
            throw e;
        }
        return roomId;
    }
    public static String getCurrentUTCTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e(ContentValues.TAG, "Error generating UTC timestamp: " + e.getMessage());
            return "";
        }
    }
    public boolean isRoomExist(String roomId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = dbHelper.getConnection();
            String sql = "SELECT 1 FROM chat_rooms WHERE room_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, roomId);
            rs = stmt.executeQuery();

            exists = rs.next(); // If a row is returned, the room exists

        } catch (SQLException e) {
            Log.e(ContentValues.TAG, "Error checking if room exists: " + e.getMessage());
            // Depending on your error handling strategy, you might:
            // - Return false (assume it doesn't exist in case of error)
            // - Throw the exception to be handled upstream
            // - Return a more specific error indicator (e.g., an enum)
            // For this example, we'll assume it doesn't exist on error:
            exists = false;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    Log.e(ContentValues.TAG, "Error closing ResultSet: " + e.getMessage());
                }
            }


        }

        return exists;
    }
    public long saveMessage(Message message) throws SQLException {
        String currentTime = TimeUtils.getCurrentUTCTime();



        try(Connection conn = dbHelper.getConnection()) {


            // First, insert the message
            String sql = "INSERT INTO messages (room_id, sender_uid, message_text, " +
                    "timestamp, time_string, message_type, media_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

            // Get the generated message ID
            ResultSet rs = stmt.getGeneratedKeys();
            long messageId;
            if (rs.next()) {
                messageId = rs.getLong(1);
            } else {
                throw new SQLException("Creating message failed, no ID obtained.");
            }

            // Update the chat room's last message
            String updateRoomSql = "UPDATE chat_rooms SET last_message = ?, last_message_time = ? " +
                    "WHERE room_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateRoomSql)) {
                updateStmt.setString(1, message.getMessageText());
                updateStmt.setLong(2, System.currentTimeMillis());
                updateStmt.setString(3, message.getRoomId());
                updateStmt.executeUpdate();
            }

            // Commit the transaction

            Log.d(TAG, String.format("Message saved successfully at %s with ID: %d",
                    currentTime, messageId));

            return messageId;

        } catch (SQLException e) {
            // Rollback transaction on error

            Log.e(TAG, String.format("Error saving message at %s: %s",
                    currentTime, e.getMessage()));
            throw e;
        }
    }
    public User getUserById(long userId) throws SQLException {
        try (Connection conn =dbHelper.getConnection()) {
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
    public void shutdown() {
        String currentTime = TimeUtils.getCurrentUTCTime();
        Log.d(TAG, String.format("Shutting down UserRepository at %s", currentTime));

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, String.format("Error shutting down at %s: %s",
                        TimeUtils.getCurrentUTCTime(), e.getMessage()));
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    public void searchByPhone(String phoneNumber, long currentUserId, ChatCallback<List<User>> callback) {
        String currentTime = TimeUtils.getCurrentUTCTime();
        Log.d(TAG, String.format("Searching for phone number %s at %s", phoneNumber, currentTime));

        executorService.execute(() -> {
            Connection connection = null;
            List<User> users = new ArrayList<>();

            try {
                connection = dbHelper.getConnection();

                String query = "SELECT u.user_id, u.username, u.phone_number, u.profile_image_url, us.status_text, u.last_active, \n" +
                        "       CASE WHEN ub.blocked_id IS NOT NULL THEN true ELSE false END as is_blocked\n" +
                        "FROM users u\n" +
                        "LEFT JOIN user_blocks ub ON ub.blocked_id = u.user_id AND ub.blocker_id = ?  -- Assuming '?' represents the current user's ID\n" +
                        "LEFT JOIN user_status us ON us.user_id = u.user_id  -- Join to get the user's status\n" +
                        "WHERE u.phone_number LIKE ?  -- Assuming '?' represents the search pattern\n" +
                        "  AND u.user_id != ?  -- Assuming '?' represents the current user's ID\n" +
                        "ORDER BY u.last_active DESC  -- Ordering by last active time\n" +
                        "LIMIT 20";

                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setLong(1, currentUserId);
                    stmt.setString(2, "%" + phoneNumber + "%");
                    stmt.setLong(3, currentUserId);

                    Log.d(TAG, String.format("Executing query at %s: %s",
                            TimeUtils.getCurrentUTCTime(), query));

                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            User user = new User(
                                    rs.getLong("uid"),
                                    rs.getString("name"),
                                    rs.getString("phone_number"),
                                    rs.getString("profile_image_url"),
                                    rs.getString("status")
                            );
                            users.add(user);
                        }
                    }
                }

                Log.d(TAG, String.format("Found %d users at %s for phone number %s",
                        users.size(), TimeUtils.getCurrentUTCTime(), phoneNumber));

                callback.onSuccess(users);

            } catch (SQLException e) {
                String errorMsg = String.format("Database error at %s: %s",
                        TimeUtils.getCurrentUTCTime(), e.getMessage());
                Log.e(TAG, errorMsg);
                callback.onError(new Exception(errorMsg));
            } catch (Exception e) {
                String errorMsg = String.format("Error searching users at %s: %s",
                        TimeUtils.getCurrentUTCTime(), e.getMessage());
                Log.e(TAG, errorMsg);
                callback.onError(new Exception(errorMsg));
            }
        });
    }

    public List<User> searchUsersByPhone(String Phone) throws SQLException {
        List<User> users = new ArrayList<>();

        try (Connection conn = dbHelper.getConnection()) {
            // Using LIKE for partial phone number matching
            String sql = "SELECT * FROM users WHERE phone_number LIKE ? " +  // Exclude current user
                    "ORDER BY username ASC " +
                    "LIMIT 20";  // Limit results for performance

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, "%" + Phone + "%"); // Exclude current user from results

                Log.d(TAG, "Searching users with phone query: " + Phone + " at " );

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

        Log.d(TAG, "Found " + users.size() + " users matching query: " + Phone);
        return users;
    }

    public void updateUser(User user) throws SQLException {
        try (Connection conn = dbHelper.getConnection()) {
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
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "UPDATE users SET status = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status);
                stmt.setLong(2, userId);
                stmt.executeUpdate();
            }
        }
    }
}