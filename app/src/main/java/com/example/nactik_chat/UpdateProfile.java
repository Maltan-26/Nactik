package com.example.nactik_chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class UpdateProfile extends AppCompatActivity {
    private static final String TAG = "UpdateProfile";
    private static final int PICK_IMAGE = 123;
    private static final String CURRENT_TIME = "2025-04-03 01:19:23";
    private static  String CURRENT_USER;

    // UI Components
    private MaterialCardView imageCardView;
    private ImageView profileImageView;
    private TextInputEditText usernameField;
    private MaterialButton updateButton;
    private MaterialButton logout;
    private CircularProgressIndicator progressBar;
    private Toolbar toolbar;
    private ImageButton backButton;

    // Data Management
    private SessionManager sessionManager;
    private UserRepository userRepository;
    private ImageRepository imageRepository;
    private Uri selectedImageUri;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
         CURRENT_USER = getIntent().getStringExtra("Name");
        initializeManagers();
        initializeViews();
        setupToolbar();
        loadCurrentProfile();
        setupClickListeners();

        Log.d(TAG, String.format("Update Profile activity started at %s for user %s",
                CURRENT_TIME, CURRENT_USER));
    }

    private void initializeManagers() {
        sessionManager = new SessionManager(this);
        userRepository = new UserRepository();
        imageRepository = new ImageRepository(this);

        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, String.format("User not logged in at %s", CURRENT_TIME));
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void initializeViews() {
        imageCardView = findViewById(R.id.getnewuserimage);
        profileImageView = findViewById(R.id.getnewuserimageinimageview);
        String imagePath = getIntent().getStringExtra("Url");; // Change this path accordingly
        Glide.with(this).load(new File(imagePath)).into(profileImageView);

        usernameField = findViewById(R.id.getnewusername);
        usernameField.setText(CURRENT_USER);
        updateButton = findViewById(R.id.updateprofilebutton);
        progressBar = findViewById(R.id.progressbarofupdateprofile);
        toolbar = findViewById(R.id.toolbarofupdateprofile);
        backButton = findViewById(R.id.backbuttonofupdateprofile);
        logout = findViewById(R.id.logout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadCurrentProfile() {
        showLoading();
        new Thread(() -> {
            try {
                Long userId = sessionManager.getUserId();
                User user = userRepository.getUserById(userId);

                runOnUiThread(() -> {
                    if (user != null) {
                        usernameField.setText(user.getName());
                        if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                            currentImageUrl = user.getImageUrl();
                            Picasso.get()
                                    .load(currentImageUrl)
                                    .placeholder(R.drawable.defaultprofile)
                                    .error(R.drawable.defaultprofile)
                                    .into(profileImageView);
                        }
                    }
                    hideLoading();
                });
            } catch (Exception e) {
                Log.e(TAG, String.format("Error loading profile at %s: %s",
                        CURRENT_TIME, e.getMessage()));
                runOnUiThread(() -> {
                    showError("Failed to load profile");
                    hideLoading();
                });
            }
        }).start();
    }

    private void setupClickListeners() {
        imageCardView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        // Add ImageView click listener
        profileImageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });
        updateButton.setOnClickListener(v -> updateProfile());
        logout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }



    private void updateProfile() {
        String newUsername = usernameField.getText().toString().trim();
        if (newUsername.isEmpty()) {
            usernameField.setError("Username cannot be empty");
            return;
        }

        showLoading();
        new Thread(() -> {
            try {
                Long userId = sessionManager.getUserId();
                String imageUrl = selectedImageUri != null ?
                        uploadImage(selectedImageUri) : currentImageUrl;

                User updatedUser = new User(
                        userId,
                        newUsername,
                        imageUrl,
                        "Online",
                        sessionManager.getUserphone()
                );

                userRepository.updateUser(updatedUser);

                runOnUiThread(() -> {
                    showSuccess("Profile updated successfully");
                    hideLoading();
                    finish();
                });

                Log.d(TAG, String.format("Profile updated at %s for user %s",
                        CURRENT_TIME, userId));

            } catch (Exception e) {
                Log.e(TAG, String.format("Error updating profile at %s: %s",
                        CURRENT_TIME, e.getMessage()));
                runOnUiThread(() -> {
                    showError("Failed to update profile");
                    hideLoading();
                });
            }
        }).start();
    }

    private String uploadImage(Uri imageUri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageData = baos.toByteArray();

        return imageRepository.uploadImage(
                sessionManager.getUserId(),
                imageData
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Picasso.get()
                        .load(selectedImageUri)
                        .placeholder(R.drawable.defaultprofile)
                        .error(R.drawable.defaultprofile)
                        .into(profileImageView);
            } catch (Exception e) {
                Log.e(TAG, String.format("Error loading selected image at %s: %s",
                        CURRENT_TIME, e.getMessage()));
                showError("Failed to load selected image");
            }
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        updateButton.setEnabled(true);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileImageView != null) {
            profileImageView.setImageDrawable(null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, String.format("Update Profile activity closing at %s", CURRENT_TIME));
    }
}