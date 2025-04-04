package com.example.nactik_chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static  String CURRENT_USER;
    private static final String CURRENT_TIME = TimeUtils.getCurrentUTCTime();

    // UI Components
    private ImageView profileImageView;
    private EditText usernameField;
    private MaterialButton updateProfileButton;
    private CircularProgressIndicator progressBar;
    private Toolbar toolbar;
    private ImageButton backButton;

    // Data Management
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        CURRENT_USER = getIntent().getStringExtra("userId");

        // Initialize session management
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, String.format("User not logged in at %s", CURRENT_TIME));
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        loadUserProfile();

        Log.d(TAG, String.format("Profile activity started at %s for user %s",
                CURRENT_TIME, CURRENT_USER));
    }

    private void initializeViews() {
        // Find views
        profileImageView = findViewById(R.id.viewuserimageinimageview);
        String imagePath = getIntent().getStringExtra("Url");; // Change this path accordingly
        Glide.with(this).load(new File(imagePath)).into(profileImageView);
        usernameField = findViewById(R.id.viewusername);
        usernameField.setText(CURRENT_USER);
        updateProfileButton = findViewById(R.id.movetoupdateprofile);
        progressBar = findViewById(R.id.progressbarofviewprofile);
        toolbar = findViewById(R.id.toolbarofviewprofile);
        backButton = findViewById(R.id.backbuttonofviewprofile);

        // Setup click listeners
        updateProfileButton.setOnClickListener(v -> openUpdateProfile());
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void loadUserProfile() {
        String currentTime = TimeUtils.getCurrentUTCTime();
        Long userId = sessionManager.getUserId();

        if (userId == null || userId == 0) {
            Log.e(TAG, String.format("Invalid user ID at %s", currentTime));
            showError("Invalid user session");
            return;
        }

        showLoading();
        new Thread(() -> {
            try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
                String sql = "SELECT username, profile_image_url, status, last_updated " +
                        "FROM users WHERE user_id = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, userId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String username = rs.getString("username");
                            String imageUrl = rs.getString("profile_image_url");
                            String status = rs.getString("status");
                            String lastUpdated = rs.getString("last_updated");

                            runOnUiThread(() -> {
                                updateUI(username, imageUrl, status, lastUpdated);
                                hideLoading();
                            });
                        } else {
                            runOnUiThread(() -> {
                                showError("User not found");
                                hideLoading();
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e(TAG, String.format("Database error at %s: %s",
                        currentTime, e.getMessage()));
                runOnUiThread(() -> {
                    showError("Failed to load profile");
                    hideLoading();
                });
            }
        }).start();
    }

    private void updateUI(String username, String imageUrl, String status, String lastUpdated) {
        usernameField.setText(username);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.defaultprofile)
                    .error(R.drawable.defaultprofile)
                    .into(profileImageView);
            currentImageUrl = imageUrl;
        }

        Log.d(TAG, String.format("UI updated at %s for user %s with status %s",
                CURRENT_TIME, username, status));
    }

    private void openUpdateProfile() {
        try {
            Intent intent = new Intent(this, UpdateProfile.class);
            intent.putExtra("userId", sessionManager.getUserId());
            intent.putExtra("Name", CURRENT_USER);
            intent.putExtra("Url", currentImageUrl);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, String.format("Error opening update profile at %s: %s",
                    CURRENT_TIME, e.getMessage()));
            showError("Unable to open profile update");
        }
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile(); // Reload profile data when returning to the screen
        Log.d(TAG, String.format("Profile activity resumed at %s",
                TimeUtils.getCurrentUTCTime()));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, String.format("Profile activity closing at %s",
                TimeUtils.getCurrentUTCTime()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources
        if (profileImageView != null) {
            profileImageView.setImageDrawable(null);
        }
    }
}