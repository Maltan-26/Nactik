package com.example.nactik_chat;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewChatActivity extends AppCompatActivity {
    private static final String TAG = "NewChatActivity";
    private static final String CURRENT_TIME = "2025-03-30 10:26:00";

    private static  Long CURRENT_USER ;
    private static final long SEARCH_DELAY_MS = 500;

    // Views
    private Toolbar toolbar;
    private TextInputEditText searchInput;
    private RecyclerView userRecyclerView;
    private ProgressBar loadingIndicator;
    private LinearLayout emptyState;

    // Data
    private UserSearchAdapter userAdapter;
    private UserRepository userRepository;
    private ExecutorService executorService;
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        SessionManager sessionManager = new SessionManager(this);
        CURRENT_USER = sessionManager.getUserId();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchListener();

        executorService = Executors.newSingleThreadExecutor();
        userRepository = new UserRepository();

        Log.d(TAG, "NewChatActivity created at " + CURRENT_TIME + " by " + CURRENT_USER);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchInput = findViewById(R.id.searchInput);
        userRecyclerView = findViewById(R.id.userRecyclerView);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        emptyState = findViewById(R.id.emptyState);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        userAdapter = new UserSearchAdapter(user -> {
            // Handle user click - start specific chat
            startSpecificChat(user);
        });
        userRecyclerView.setAdapter(userAdapter);
    }

    private void setupSearchListener() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchInput.removeCallbacks(searchRunnable);
                }

                // Create new search with delay
                searchRunnable = () -> searchUsers(s.toString());
                searchInput.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUsers(String query) {
        if (query.length() < 3) {
            return; // Avoid searching too short queries
        }

        showLoading();

        executorService.execute(() -> {
            try {
                List<User> users = userRepository.searchUsersByPhone(query);
                runOnUiThread(() -> updateUIWithUsers(users));
            } catch (Exception e) {
                Log.e(TAG, "Error searching users: " + e.getMessage());
                runOnUiThread(() -> showError("Error searching users"));
            }
        });
    }

    private void updateUIWithUsers(List<User> users) {
        hideLoading();

        if (users != null && !users.isEmpty()) {
            userAdapter.setUsers(users);
            showContent();
        } else {
            showEmptyState();
        }
    }

    private void startSpecificChat(User user) {
        try {
            Intent intent = new Intent(NewChatActivity.this, specificchat.class);
            intent.putExtra("userName", CURRENT_USER);
            intent.putExtra("receivername", user.getName());
            intent.putExtra("receiveruid", user.getUid());

            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error starting chat: " + e.getMessage());
            showError("Error starting chat");
        }
    }

    private void showLoading() {
        loadingIndicator.setVisibility(View.VISIBLE);
        userRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showContent() {
        userRecyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        userRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        hideLoading();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}