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
    private static final String CURRENT_TIME = "2025-03-30 10:15:28";
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
        loadChats();
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

    private void setupListeners() {
        // Setup pull to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadChats();
            Log.d(TAG, "Refreshing chats at " + CURRENT_TIME);
        });

        // Setup FAB click listener
        fabNewChat.setOnClickListener(v -> {
            startNewChat();
            Log.d(TAG, "New chat button clicked by " + CURRENT_USER);
        });
    }

    private void loadChats() {
        showLoadingState();

        try {
            // Load chats in background
            new Thread(() -> {
                try {
                    List<Message> chats = chatRepository.getRecentMessages("defaultRoom", 0);

                    // Update UI on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateUIWithChats(chats));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error loading chats: " + e.getMessage());
                    showError("Failed to load chats");
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Error starting chat load: " + e.getMessage());
            showError("Error starting chat load");
        }
    }

    private void updateUIWithChats(List<Message> chats) {
        hideLoadingState();

        if (chats != null && !chats.isEmpty()) {
            chatAdapter.setMessages(chats);
            showContentState();
            Log.d(TAG, "Loaded " + chats.size() + " chats");
        } else {
            showEmptyState();
            Log.d(TAG, "No chats found");
        }
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
        loadChats();
        Log.d(TAG, "Chat fragment resumed by " + CURRENT_USER);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any resources
        recyclerView.setAdapter(null);
    }
}