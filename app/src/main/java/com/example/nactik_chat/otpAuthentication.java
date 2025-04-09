package com.example.nactik_chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class otpAuthentication extends AppCompatActivity {
    private static final String TAG = "otpAuthentication";
    private static final String CURRENT_TIME = "2025-03-27 17:15:45";

    // UI Components
    private TextInputEditText getotpEditText;
    private MaterialButton verifyOtpButton;
    private TextView changeNumberButton;
    private ProgressBar progressBar;

    // Service
    private OtpService otpService;

    // Data
    private String phoneNumber;
    private String enteredOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_authentication);

        // Initialize service
        otpService = new OtpService();

        // Initialize views
        initializeViews();

        // Get phone number from intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Phone number not provided");

            finish();
            return;
        }

        Log.d(TAG, "OTP Authentication started for phone: " + phoneNumber + " at " + CURRENT_TIME);

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        getotpEditText = findViewById(R.id.getotp);
        verifyOtpButton = findViewById(R.id.verifyotp);
        changeNumberButton = findViewById(R.id.changenumber);
        progressBar = findViewById(R.id.progressbarofotpauth);
    }

    private void setupClickListeners() {
        verifyOtpButton.setOnClickListener(v -> verifyOtp());

        changeNumberButton.setOnClickListener(v -> {
            Intent intent = new Intent(otpAuthentication.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void verifyOtp() {
        enteredOtp = getotpEditText.getText().toString().trim();

        if (enteredOtp.isEmpty()) {
            getotpEditText.setError("Please enter OTP");
            return;
        }

        if (enteredOtp.length() != 6) {
            getotpEditText.setError("Please enter valid 6-digit OTP");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        verifyOtpButton.setEnabled(false);

        new Thread(() -> {
            try {
                boolean isVerified = otpService.verifyOtp(phoneNumber, enteredOtp);

                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    verifyOtpButton.setEnabled(true);

                    if (isVerified) {
                        // OTP verified successfully
                        Log.d(TAG, "OTP verified successfully for phone: " + phoneNumber + " at " + CURRENT_TIME);
                        navigateToSetProfile();
                    } else {
                        // Invalid OTP
                        Toast.makeText(otpAuthentication.this,
                                "Invalid OTP or OTP expired", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error verifying OTP: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    verifyOtpButton.setEnabled(true);
                    Toast.makeText(otpAuthentication.this,"Error verifying OTP", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void navigateToSetProfile() {
        Intent intent = new Intent(otpAuthentication.this, setProfile.class);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("timestamp", CURRENT_TIME);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "OTP Authentication resumed at " + CURRENT_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "OTP Authentication paused at " + CURRENT_TIME);
    }
}
