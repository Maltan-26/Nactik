package com.example.nactik_chat;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class OtpService {
    private static final String TAG = "OtpService";
    private static final String CURRENT_TIME = "2025-03-27 17:12:36";
    private static final String CURRENT_USER = "Maltan-26";

    private DatabaseHelper dbHelper;
    private static final int OTP_LENGTH = 6;
    private static final long OTP_VALIDITY_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

    public OtpService() {
        dbHelper = DatabaseHelper.getInstance();
    }
    public boolean sendOtp(String phoneNumber, String otp) {
        Log.d(TAG, "Attempting to send OTP to phone number: " + phoneNumber + " at " + CURRENT_TIME);

        try {
            // Save OTP to database
            saveOtp(phoneNumber, otp);

            // Simulate sending SMS (in real implementation, you would integrate with an SMS service)
            boolean smsSent = sendSmsOtp(phoneNumber, otp);

            if (smsSent) {
                Log.d(TAG, "OTP sent successfully to: " + phoneNumber + " at " + CURRENT_TIME);
                return true;
            } else {
                Log.e(TAG, "Failed to send OTP SMS to: " + phoneNumber);
                // Clean up the saved OTP if SMS sending fails
                deleteOtp(phoneNumber);
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending OTP: " + e.getMessage());
            return false;
        }
    }

    private boolean sendSmsOtp(String phoneNumber, String otp) {
        try {
            // TODO: Replace this with your actual SMS service integration
            // For example, using Twilio, MessageBird, or any other SMS service provider

            String message = String.format("Your OTP for MyChat App is: %s. Valid for 5 minutes.", otp);

            // For demonstration, we'll just log the message
            Log.i(TAG, "Would send SMS to " + phoneNumber + ": " + message);

            // Simulate successful SMS sending
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error in SMS sending: " + e.getMessage());
            return false;
        }
    }

    // Optional: Add method to track SMS sending attempts
    private void recordSmsAttempt(String phoneNumber, boolean success) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "INSERT INTO sms_attempts (phone_number, success, attempt_time) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phoneNumber);
                stmt.setBoolean(2, success);
                stmt.setString(3, CURRENT_TIME);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error recording SMS attempt: " + e.getMessage());
        }
    }
    public String generateOtp(String phoneNumber) {
        // Generate a random 6-digit OTP
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        String generatedOtp = otp.toString();
        saveOtp(phoneNumber, generatedOtp);

        Log.d(TAG, "OTP generated for phone: " + phoneNumber + " at " + CURRENT_TIME);
        return generatedOtp;
    }

    private void saveOtp(String phoneNumber, String otp) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            // First delete any existing OTP for this phone number
            String deleteSql = "DELETE FROM otp_verification WHERE phone_number = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, phoneNumber);
                deleteStmt.executeUpdate();
            }

            // Insert new OTP
            String insertSql = "INSERT INTO otp_verification (phone_number, otp, created_at) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, phoneNumber);
                insertStmt.setString(2, otp);
                insertStmt.setString(3, CURRENT_TIME);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error saving OTP: " + e.getMessage());
            throw new RuntimeException("Failed to save OTP", e);
        }
    }

    public boolean verifyOtp(String phoneNumber, String submittedOtp) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "SELECT otp, created_at FROM otp_verification WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phoneNumber);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedOtp = rs.getString("otp");
                    String createdAt = rs.getString("created_at");

                    // Check if OTP matches and is still valid
                    if (storedOtp.equals(submittedOtp) && !isOtpExpired(createdAt)) {
                        // Delete the used OTP
                        deleteOtp(phoneNumber);
                        Log.d(TAG, "OTP verified successfully for phone: " + phoneNumber + " at " + CURRENT_TIME);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error verifying OTP: " + e.getMessage());
            throw new RuntimeException("Failed to verify OTP", e);
        }
        return false;
    }

    private boolean isOtpExpired(String createdAt) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date created = sdf.parse(createdAt);
            java.util.Date now = sdf.parse(CURRENT_TIME);

            return now.getTime() - created.getTime() > OTP_VALIDITY_DURATION;
        } catch (Exception e) {
            Log.e(TAG, "Error checking OTP expiration: " + e.getMessage());
            return true;
        }
    }

    private void deleteOtp(String phoneNumber) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "DELETE FROM otp_verification WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phoneNumber);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting OTP: " + e.getMessage());
        }
    }
}