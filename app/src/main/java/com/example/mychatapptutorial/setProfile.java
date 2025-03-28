package com.example.mychatapptutorial;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class setProfile extends AppCompatActivity {
    private static final String TAG = "setProfile";
    private static final String CURRENT_TIME = "2025-03-27 17:22:06";
    private static final String CURRENT_USER = "Maltan-26";
    private static final int PICK_IMAGE = 123;

    // UI Components
    private CardView mgetuserimage;
    private ImageView mgetuserimageinimageview;
    private EditText mgetusername;
    private Button msaveprofile;
    private ProgressBar mprogressbarofsetprofile;

    // Data
    private Uri imagepath;
    private String name;
    private String imageUrl;
    private DatabaseHelper dbHelper;
    private ImageUploadHelper imageUploadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_profile);

        // Initialize helpers
        dbHelper = DatabaseHelper.getInstance();
        imageUploadHelper = new ImageUploadHelper();

        // Initialize views
        initializeViews();
        setupClickListeners();

        Log.d(TAG, "SetProfile activity started at " + CURRENT_TIME + " by user " + CURRENT_USER);
    }

    private void initializeViews() {
        mgetusername = findViewById(R.id.getusername);
        mgetuserimage = findViewById(R.id.getuserimage);
        mgetuserimageinimageview = findViewById(R.id.getuserimageinimageview);
        msaveprofile = findViewById(R.id.saveProfile);
        mprogressbarofsetprofile = findViewById(R.id.progressbarofsetProfile);
    }

    private void setupClickListeners() {
        mgetuserimage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        msaveprofile.setOnClickListener(view -> {
            name = mgetusername.getText().toString().trim();
            if(name.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        "Name is Empty", Toast.LENGTH_SHORT).show();
            }
            else if(imagepath == null) {
                Toast.makeText(getApplicationContext(),
                        "Image is Empty", Toast.LENGTH_SHORT).show();
            }
            else {
                mprogressbarofsetprofile.setVisibility(View.VISIBLE);
                saveUserProfile();
            }
        });
    }

    private void saveUserProfile() {
        new Thread(() -> {
            try {
                // First upload the image
                String imageUrl = uploadImage();
                if (imageUrl == null) {
                    showError("Failed to upload image");
                    return;
                }

                // Then save user data
                boolean success = saveUserData(imageUrl);
                if (!success) {
                    showError("Failed to save user data");
                    return;
                }

                runOnUiThread(() -> {
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),
                            "Profile saved successfully", Toast.LENGTH_SHORT).show();

                    // Navigate to chat activity
                    Intent intent = new Intent(setProfile.this, chatActivity.class);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error saving profile: " + e.getMessage());
                showError("Error saving profile");
            }
        }).start();
    }

    private String uploadImage() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + ".jpg";

            // Upload to your image hosting service
            return imageUploadHelper.uploadImage(filename, imageData);
        } catch (IOException e) {
            Log.e(TAG, "Error processing image: " + e.getMessage());
            return null;
        }
    }

    private boolean saveUserData(String imageUrl) {
        try (Connection conn = dbHelper.getConnection()) {
            String sql = "INSERT INTO users (user_id, username, profile_image_url, status, created_at, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, CURRENT_USER);
                stmt.setString(2, name);
                stmt.setString(3, imageUrl);
                stmt.setString(4, "Online");
                stmt.setString(5, CURRENT_TIME);
                stmt.setString(6, CURRENT_TIME);

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            Log.e(TAG, "Database error: " + e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            mprogressbarofsetprofile.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imagepath = data.getData();
            try {
                mgetuserimageinimageview.setImageURI(imagepath);
            } catch (Exception e) {
                Log.e(TAG, "Error setting image: " + e.getMessage());
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}