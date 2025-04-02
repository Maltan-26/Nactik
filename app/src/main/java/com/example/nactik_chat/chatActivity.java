package com.example.nactik_chat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class chatActivity extends AppCompatActivity {
    private static final int POLLING_INTERVAL = 3000; // 3 seconds

    // Views
    private TabLayout tabLayout;
    private TabItem mchat, mcall, mstatus;
    private ViewPager2 viewPager;
    private PagerAdapter pagerAdapter;
    private androidx.appcompat.widget.Toolbar mtoolbar;
    private RecyclerView recyclerView;

    // Repositories and Services
    private UserRepository userRepository;
    private ChatRepository chatRepository;
    private MessageRepository messageRepository;
    private MessagePollingService pollingService;
    private SessionManager sessionManager;

    // State
    private Long currentUserId;
    private String currentRoomId;
    private static final String TAG = "chatActivity";
    private static final String CURRENT_USER = "Maltan-26";
    private static final long TOAST_DELAY = 5000; // 5 seconds
    private long lastToastTime = 0;
    private MessagesAdapter messagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:19:05
        Log.d(TAG, String.format("Creating chatActivity at %s for user %s",
                currentTime, CURRENT_USER));

        initializeDependencies();
        initializeViews();
        setupToolbar();
        setupViewPager();

        // Get room ID from intent
        currentRoomId = getIntent().getStringExtra("room_id");
        if (currentRoomId != null && !currentRoomId.trim().isEmpty()) {
            setupMessagePolling(currentRoomId);
        } else {
            Log.w(TAG, String.format("No room ID provided at %s", currentTime));
        }
    }

    private void initializeDependencies() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:19:05

        try {
            userRepository = new UserRepository();
            chatRepository = new ChatRepository();
            messageRepository = new MessageRepository();
            sessionManager = new SessionManager(this);
            pollingService = new MessagePollingService(messageRepository);

            boolean isLoggedIn = sessionManager.isLoggedIn();
            currentUserId = sessionManager.getUserId();

            Log.d(TAG, String.format("Dependencies initialized at %s - isLoggedIn: %b, userId: %d",
                    currentTime, isLoggedIn, currentUserId));

            if (currentUserId == null) {
                Log.w(TAG, String.format("No user ID found at %s, redirecting to login",
                        currentTime));
                navigateToLogin();
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("Error initializing dependencies at %s: %s",
                    currentTime, e.getMessage()));
            finish();
        }
    }


    private void initializeViews() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:19:05

        try {
            tabLayout = findViewById(R.id.include);
            mchat = findViewById(R.id.chat);
            mcall = findViewById(R.id.calls);
            mstatus = findViewById(R.id.status);
            viewPager = findViewById(R.id.fragmentcontainer);
            mtoolbar = findViewById(R.id.toolbar);
            recyclerView = findViewById(R.id.chat_recycler_view);

            // Setup RecyclerView
            messagesAdapter = new MessagesAdapter(this, currentUserId);
            recyclerView.setAdapter(messagesAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            Log.d(TAG, String.format("Views initialized at %s", currentTime));
        } catch (Exception e) {
            Log.e(TAG, String.format("Error initializing views at %s: %s",
                    currentTime, e.getMessage()));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(mtoolbar);
        if (getSupportActionBar() != null) {
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_baseline_more_vert_24);
            mtoolbar.setOverflowIcon(drawable);
        }
    }

    private void setupViewPager() {
        pagerAdapter = new PagerAdapter(this, tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() >= 0 && tab.getPosition() <= 2) {
                    pagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void setupMessagePolling(String roomId) {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:19:05

        if (roomId == null || roomId.trim().isEmpty()) {
            Log.e(TAG, String.format("Invalid room ID provided at %s", currentTime));
            return;
        }

        try {
            Log.d(TAG, String.format("Starting message polling at %s for room: %s",
                    currentTime, roomId));

            pollingService.startPolling(roomId, System.currentTimeMillis(),
                    new MessagePollingService.MessageCallback() {
                        @Override
                        public void onNewMessages(List<Message> messages) {
                            runOnUiThread(() -> updateMessages(messages));
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> showError(error));
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, String.format("Error setting up polling at %s: %s",
                    currentTime, e.getMessage()));
        }
    }
    private void updateMessages(List<Message> messages) {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:19:05

        try {
            if (messages != null && !messages.isEmpty()) {
                messagesAdapter.setMessages(messages);
                recyclerView.scrollToPosition(messages.size() - 1);
                Log.d(TAG, String.format("Updated %d messages at %s",
                        messages.size(), currentTime));
            }
        } catch (Exception e) {
            Log.e(TAG, String.format("Error updating messages at %s: %s",
                    currentTime, e.getMessage()));
            showError("Failed to update messages");
        }
    }

    private void showError(String error) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastToastTime > TOAST_DELAY) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            lastToastTime = currentTime;
            Log.e(TAG, String.format("Showing error at %s: %s",
                    TimeUtils.getCurrentUTCTime(), error));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;

        } else if (item.getItemId() == R.id.settings) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateUserStatus(String status) {
        if (currentUserId != null) {
            new Thread(() -> {
                try {
                    userRepository.updateUserStatus(currentUserId, status);
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(chatActivity.this,
                            "Failed to update status", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
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

    @Override
    protected void onDestroy() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:19:05
        Log.d(TAG, String.format("Destroying chatActivity at %s", currentTime));

        if (pollingService != null) {
            pollingService.stopPolling();
        }
        super.onDestroy();
    }

    public Long getCurrentUserId() {
        return currentUserId = sessionManager.getUserId();
    }
}