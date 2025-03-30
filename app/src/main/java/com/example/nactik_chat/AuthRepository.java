package com.example.nactik_chat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
public class AuthRepository {
    private final DatabaseHelper dbHelper;
    private static final int OTP_EXPIRY_MINUTES = 5;

    public AuthRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void saveOtpForPhone(String phoneNumber, String otp) throws SQLException {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "INSERT INTO otp_verification (phone_number, otp, expires_at) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Timestamp expiryTime = new Timestamp(System.currentTimeMillis() + (OTP_EXPIRY_MINUTES * 60 * 1000));
                stmt.setString(1, phoneNumber);
                stmt.setString(2, otp);
                stmt.setTimestamp(3, expiryTime);
                stmt.executeUpdate();
            }
        }
    }

    public boolean verifyOtp(String phoneNumber, String otp) throws SQLException {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "SELECT * FROM otp_verification WHERE phone_number = ? AND otp = ? AND expires_at > ? AND attempt_count = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phoneNumber);
                stmt.setString(2, otp);
                stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    // Mark OTP as used
                    markOtpAsUsed(conn, phoneNumber, otp);
                    return true;
                }
                return false;
            }
        }
    }

    private void markOtpAsUsed(Connection conn, String phoneNumber, String otp) throws SQLException {
        String sql = "UPDATE otp_verification SET attempt_count = 1 WHERE phone_number = ? AND otp = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            stmt.setString(2, otp);
            stmt.executeUpdate();
        }
    }
}
