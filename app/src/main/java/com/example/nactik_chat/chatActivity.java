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
    private String currentUserId;
    private String currentRoomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeDependencies();
        initializeViews();
        setupToolbar();
        setupViewPager();
        setupMessagePolling();
    }

    private void initializeDependencies() {
        userRepository = new UserRepository();
        chatRepository = new ChatRepository();
        messageRepository = new MessageRepository();
        sessionManager = new SessionManager(this);
        pollingService = new MessagePollingService(messageRepository);

        // Add debug logging
        boolean isLoggedIn = sessionManager.isLoggedIn();
        currentUserId = sessionManager.getUserId();
        Log.d("chatActivity", "isLoggedIn: " + isLoggedIn + ", currentUserId: " + currentUserId);

        if (currentUserId == null) {
            // Handle not logged in state
            navigateToLogin();
            return;
        }
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.include);
        mchat = findViewById(R.id.chat);
        mcall = findViewById(R.id.calls);
        mstatus = findViewById(R.id.status);
        viewPager = findViewById(R.id.fragmentcontainer);
        mtoolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.chat_recycler_view);

        // Setup RecyclerView
        MessagesAdapter adapter = new MessagesAdapter(this, currentUserId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

    private void setupMessagePolling() {
        pollingService.startPolling(currentRoomId, System.currentTimeMillis(),
                new MessagePollingService.MessageCallback() {
                    @Override
                    public void onNewMessages(List<Message> messages) {
                        if (recyclerView.getAdapter() instanceof MessagesAdapter) {
                            ((MessagesAdapter) recyclerView.getAdapter()).setMessages(messages);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(chatActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
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
        super.onDestroy();
        if (pollingService != null) {
            pollingService.stopPolling();
        }
    }
}