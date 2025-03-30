package com.example.nactik_chat;

import android.content.Context;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ImageRepository {
    private final Context context;
    private final DatabaseHelper dbHelper;
    private static final String IMAGES_DIRECTORY = "profile_images/";

    public ImageRepository(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance();
    }

    public String uploadImage(String userId, byte[] imageData) throws IOException {
        String fileName = userId + "_" + System.currentTimeMillis() + ".jpg";
        String imageUrl = uploadToServer(fileName, imageData);

        try (Connection conn = dbHelper.getConnection()) {
            String sql = "UPDATE users SET image_url = ? WHERE uid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, imageUrl);
                stmt.setString(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new IOException("Failed to update image URL in database", e);
        }

        return imageUrl;
    }

    private String uploadToServer(String fileName, byte[] imageData) throws IOException {
        // Implementation would depend on your image storage solution
        // This is a placeholder that would need to be implemented based on your server setup
        String serverUrl = "https://your-image-server.com/";
        return serverUrl + IMAGES_DIRECTORY + fileName;
    }
}