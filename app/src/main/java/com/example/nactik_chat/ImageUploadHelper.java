package com.example.nactik_chat;

import android.util.Log;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUploadHelper {
    private static final String TAG = "ImageUploadHelper";
    private static final String UPLOAD_URL = "https://your-freedatabase-image-upload-url.com/upload";

    public String uploadImage(String filename, byte[] imageData) {
        try {
            URL url = new URL(UPLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Set up multipart form data
            String boundary = "*****" + System.currentTimeMillis() + "*****";
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                // Write image data
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; " +
                        "filename=\"" + filename + "\"\r\n").getBytes());
                os.write(("Content-Type: image/jpeg\r\n\r\n").getBytes());
                os.write(imageData);
                os.write(("\r\n--" + boundary + "--\r\n").getBytes());
            }

            // Get response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Return the URL of the uploaded image
                return "https://your-freedatabase-cdn.com/images/" + filename;
            } else {
                Log.e(TAG, "Upload failed with response code: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error uploading image: " + e.getMessage());
            return null;
        }
    }
}