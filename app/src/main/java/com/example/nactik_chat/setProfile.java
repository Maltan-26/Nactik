package com.example.nactik_chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class setProfile extends AppCompatActivity {
    private static final String TAG = "setProfile";
    private static final String CURRENT_TIME = "2025-03-27 17:22:06";
    private static final String CURRENT_USER = "26";
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

        // Add ImageView click listener
        mgetuserimageinimageview.setOnClickListener(view -> {
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
                // First save the image locally
                String imagePath = saveImageLocally();
                if (imagePath == null) {
                    showError("Failed to save image locally");
                    return;
                }

                // Then save user data
                boolean success = saveUserData(imagePath);
                if (!success) {
                    showError("Failed to save user data");
                    return;
                }

                // Add this block to create login session
                String phoneNumber = getIntent().getStringExtra("phoneNumber");
                SessionManager sessionManager = new SessionManager(this);
                Long userId = getUserIdFromDatabase(phoneNumber); // You need to implement this method
                sessionManager.createLoginSession(userId, phoneNumber);

                runOnUiThread(() -> {
                    mprogressbarofsetprofile.setVisibility(View.INVISIBLE);

                    // Navigate to chat activity
                    Intent intent = new Intent(setProfile.this, chatActivity.class);
                    intent.putExtra("Name", name);
                    intent.putExtra("imagePath", imagePath);
                    startActivity(intent);
                    finish();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error saving profile: " + e.getMessage());
                showError("Error saving profile");
            }
        }).start();
    }

    // Add this method to get the user ID from the database
    private Long getUserIdFromDatabase(String phoneNumber) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "SELECT user_id FROM users WHERE phone_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, phoneNumber);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("user_id");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Database error getting user ID: " + e.getMessage());
        }
        return null;
    }

    private String saveImageLocally() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagepath);
            File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String filename = UUID.randomUUID().toString() + ".jpg";
            File file = new File(directory, filename);
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Error saving image locally: " + e.getMessage());
            return null;
        }
    }

    private boolean saveUserData(String imagePath) {
        try (Connection conn = DatabaseHelper.getInstance().getConnection()) {
            String sql = "INSERT INTO users ( username, profile_image_url, status, created_at, phone_number, last_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, imagePath);
                stmt.setString(3, "Online");
                stmt.setString(4, CURRENT_TIME);
                stmt.setString(5,  getIntent().getStringExtra("phoneNumber").toString());
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
            }
        }
    }
}