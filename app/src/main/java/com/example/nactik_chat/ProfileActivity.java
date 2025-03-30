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

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE = 123;
    private static final String CURRENT_TIME = "2025-03-27 16:58:13";
    private static final String CURRENT_USER = "Maltan-26";

    // UI Components
    private ImageView userProfilePic;
    private EditText userNameField;
    private EditText userStatusField;
    private ImageButton saveProfileButton;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView lastSeenText;

    // Database helper
    private DatabaseHelper dbHelper;

    // User data
    private String imageUriAccessToken;
    private Uri imageUri;
    private String userName;
    private String userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initializeViews();
        setupDatabase();
        setupToolbar();
        loadUserProfile();
        setupClickListeners();

        Log.d(TAG, "Profile activity started at " + CURRENT_TIME + " by user " + CURRENT_USER);
    }

    private void initializeViews() {
        // Update view references to match your layout IDs
        userProfilePic = findViewById(R.id.viewuserimageinimageview);
        userNameField = findViewById(R.id.viewusername);
        toolbar = findViewById(R.id.toolbarofviewprofile);

        // Back button in toolbar
        ImageButton backButton = findViewById(R.id.backbuttonofviewprofile);
        backButton.setOnClickListener(v -> onBackPressed());

        // Update profile button
        MaterialButton updateProfileButton = findViewById(R.id.movetoupdateprofile);
        updateProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UpdateProfile.class);
            startActivity(intent);
        });

        // Set the current time in the toolbar title
        TextView titleText = findViewById(R.id.myapptext);
        titleText.setText(getString(R.string.your_profile_text));
    }

    private void setupDatabase() {
        dbHelper = DatabaseHelper.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try (Connection conn = dbHelper.getConnection()) {
                String sql = "SELECT username, status, profile_image_url, last_updated " +
                        "FROM users WHERE user_id = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, CURRENT_USER);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String username = rs.getString("username");
                    String status = rs.getString("status");
                    String imageUrl = rs.getString("profile_image_url");
                    String lastUpdated = rs.getString("last_updated");

                    runOnUiThread(() -> {
                        userNameField.setText(username);
                        userStatusField.setText(status);
                        lastSeenText.setText("Last updated: " + lastUpdated);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(userProfilePic);
                            imageUriAccessToken = imageUrl;
                        }
                        progressBar.setVisibility(View.GONE);
                    });
                }
            } catch (SQLException e) {
                Log.e(TAG, "Database error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Failed to load profile", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void setupClickListeners() {
        userProfilePic.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Image"),
                    PICK_IMAGE);
        });

        saveProfileButton.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        String newName = userNameField.getText().toString();
        String newStatus = userStatusField.getText().toString();

        if (newName.isEmpty() || newStatus.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try (Connection conn = dbHelper.getConnection()) {
                String sql = "UPDATE users SET username = ?, status = ?, " +
                        "profile_image_url = ?, last_updated = ? " +
                        "WHERE user_id = ?";

                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newName);
                stmt.setString(2, newStatus);
                stmt.setString(3, imageUriAccessToken);
                stmt.setString(4, CURRENT_TIME);
                stmt.setString(5, CURRENT_USER);

                int rowsAffected = stmt.executeUpdate();

                runOnUiThread(() -> {
                    if (rowsAffected > 0) {
                        Toast.makeText(ProfileActivity.this,
                                "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        lastSeenText.setText("Last updated: " + CURRENT_TIME);
                    } else {
                        Toast.makeText(ProfileActivity.this,
                                "No changes made", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });

                Log.d(TAG, "Profile updated at " + CURRENT_TIME +
                        " by user " + CURRENT_USER);

            } catch (SQLException e) {
                Log.e(TAG, "Database error: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this,
                            "Failed to update profile", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                userProfilePic.setImageURI(imageUri);
                // Here you would typically upload the image to your image hosting service
                // and get back a URL to store in the database
                // For now, we'll just store the local URI
                imageUriAccessToken = imageUri.toString();
            } catch (Exception e) {
                Log.e(TAG, "Error setting profile image: " + e.getMessage());
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Profile activity paused at " + CURRENT_TIME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Profile activity resumed at " + CURRENT_TIME);
    }
}