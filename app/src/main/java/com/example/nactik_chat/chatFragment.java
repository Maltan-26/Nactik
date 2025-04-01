package com.example.nactik_chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class chatFragment extends Fragment {
    private static final String TAG = "chatFragment";
    private static final String CURRENT_USER = "Maltan-26";

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private CircularProgressIndicator loadingIndicator;
    private FloatingActionButton fabNewChat;

    // Adapters and Data
    private ChatAdapter chatAdapter;
    private ChatRepository chatRepository;

    public chatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        //loadChats();
        return view;
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerview);
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        fabNewChat = view.findViewById(R.id.fabNewChat);
        chatRepository = new ChatRepository();

        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(getContext(), CURRENT_USER);
        recyclerView.setAdapter(chatAdapter);
        // LinearLayoutManager is already set in XML using app:layoutManager
    }

    private void loadChats() {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 06:46:59
        Log.d(TAG, String.format("Loading chats at %s for user: %s", currentTime, CURRENT_USER));

        showLoadingState();

        new Thread(() -> {
            try {
                // Get chats from repository
                List<Message> chats = chatRepository.getRecentMessages("defaultRoom", 0);

                // Update UI on main thread
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            updateUIWithChats(chats);
                        } catch (Exception e) {
                            String errorMsg = String.format("Error updating UI at %s: %s",
                                    TimeUtils.getCurrentUTCTime(), e.getMessage());
                            Log.e(TAG, errorMsg);
                            showError("Failed to update chats");
                        }
                    });
                }
            } catch (Exception e) {
                String errorMsg = String.format("Error loading chats at %s: %s",
                        TimeUtils.getCurrentUTCTime(), e.getMessage());
                Log.e(TAG, errorMsg);

                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Failed to load chats");
                        showEmptyState();
                    });
                }
            }
        }).start();
    }

    private void updateUIWithChats(List<Message> chats) {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 06:46:59

        try {
            hideLoadingState();

            if (chats != null && !chats.isEmpty()) {
                chatAdapter.setMessages(chats);
                showContentState();
                Log.d(TAG, String.format("Loaded %d chats at %s", chats.size(), currentTime));
            } else {
                showEmptyState();
                Log.d(TAG, String.format("No chats found at %s for user %s",
                        currentTime, CURRENT_USER));
            }
        } catch (Exception e) {
            String errorMsg = String.format("Error updating UI with chats at %s: %s",
                    currentTime, e.getMessage());
            Log.e(TAG, errorMsg);
            showError("Failed to display chats");
        }
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //loadChats();
            Log.d(TAG, "Refreshing chats for user " + CURRENT_USER);
        });

        fabNewChat.setOnClickListener(v -> {
            startNewChat();
            Log.d(TAG, "New chat button clicked by user " + CURRENT_USER);
        });
    }

    private void showLoadingState() {
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoadingState() {
        loadingIndicator.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showContentState() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                hideLoadingState();
            });
        }
    }

    private void startNewChat() {
        try {
            Intent intent = new Intent(getActivity(), NewChatActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting new chat: " + e.getMessage());
            showError("Failed to start new chat");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadChats();
        Log.d(TAG, "Chat fragment resumed by user " + CURRENT_USER);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any resources
        recyclerView.setAdapter(null);
    }
}