package com.example.nactik_chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UpdateProfile extends AppCompatActivity {
    private EditText mnewusername;
    private ImageView mgetnewimageinimageview;
    private ProgressBar mprogressbarofupdateprofile;
    private Button mupdateprofilebutton;
    private ImageButton mbackbuttonofupdateprofile;
    private Toolbar mtoolbarofupdateprofile;

    private UserRepository userRepository;
    private ImageRepository imageRepository;
    private SessionManager sessionManager;

    private Uri imagepath;
    private static final int PICK_IMAGE = 123;
    private String newname;
    private String currentImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        initializeViews();
        initializeRepositories();
        setupToolbar();
        loadCurrentUserData();
        setupClickListeners();
    }

    private void initializeViews() {
        mtoolbarofupdateprofile = findViewById(R.id.toolbarofupdateprofile);
        mbackbuttonofupdateprofile = findViewById(R.id.backbuttonofupdateprofile);
        mgetnewimageinimageview = findViewById(R.id.getnewuserimageinimageview);
        mprogressbarofupdateprofile = findViewById(R.id.progressbarofupdateprofile);
        mnewusername = findViewById(R.id.getnewusername);
        mupdateprofilebutton = findViewById(R.id.updateprofilebutton);
    }

    private void initializeRepositories() {
        userRepository = new UserRepository();
        imageRepository = new ImageRepository(this);
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbarofupdateprofile);
        mbackbuttonofupdateprofile.setOnClickListener(v -> finish());
    }

    private void setSupportActionBar(Toolbar mtoolbarofupdateprofile) {
        // Set the toolbar title
        mtoolbarofupdateprofile.setTitle("Update Profile");

        // Set the toolbar as the action bar
        setSupportActionBar(mtoolbarofupdateprofile);

        // Enable back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set navigation click listener
        mtoolbarofupdateprofile.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void loadCurrentUserData() {
        String userId = sessionManager.getUserId();
        new Thread(() -> {
            try {
                User user = userRepository.getUserById(userId);
                runOnUiThread(() -> {
                    mnewusername.setText(user.getName());
                    if (user.getImageUrl() != null && !user.getImageUrl().isEmpty()) {
                        currentImageUrl = user.getImageUrl();
                        Picasso.get().load(user.getImageUrl()).into(mgetnewimageinimageview);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupClickListeners() {
        mgetnewimageinimageview.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        });

        mupdateprofilebutton.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        newname = mnewusername.getText().toString().trim();
        if (newname.isEmpty()) {
            Toast.makeText(this, "Name is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mprogressbarofupdateprofile.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                String userId = sessionManager.getUserId();
                String imageUrl = imagepath != null ?
                        uploadImage(userId, imagepath) : currentImageUrl;

                User updatedUser = new User(userId, newname, imageUrl, "Online", sessionManager.getUserphone());
                userRepository.updateUser(updatedUser);

                runOnUiThread(() -> {
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    navigateToChatActivity();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    mprogressbarofupdateprofile.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String uploadImage(String userId, Uri imageUri) throws IOException {
        // Compress image
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] imageData = baos.toByteArray();

        // Upload to server
        return imageRepository.uploadImage(userId, imageData);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imagepath = data.getData();
            mgetnewimageinimageview.setImageURI(imagepath);
        }
    }

    private void navigateToChatActivity() {
        Intent intent = new Intent(this, chatActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUserStatus("Online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateUserStatus("Offline");
    }

    private void updateUserStatus(String status) {
        new Thread(() -> {
            try {
                userRepository.updateUserStatus(sessionManager.getUserId(), status);
                if (status.equals("Offline")) {
                    runOnUiThread(() -> Toast.makeText(this, "Now User is Offline", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Now User is Online", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}