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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class chatFragment extends Fragment {
    private static final String TAG = "chatFragment";
    private static final String CURRENT_USER = "Maltan-26";
    private static final int POLLING_INTERVAL = 3000; // 3 seconds

    // Views
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private CircularProgressIndicator loadingIndicator;
    private FloatingActionButton fabNewChat;

    // Adapter and Data
    private ChatRoomAdapter chatRoomAdapter;
    private UserRepository userRepository;
    private Long currentUserId;
    private boolean isPolling = true;
    private volatile boolean isLoadingRooms = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        userRepository = new UserRepository();
        currentUserId = ((chatActivity) requireActivity()).getCurrentUserId();
        // Initialize everything only once
        initializeViews(view);
        setupRecyclerView();
        setupListeners();



        // Get user ID

        // Initial load
        loadChatRooms();
        startPolling();

        return view;
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerview);
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        fabNewChat = view.findViewById(R.id.fabNewChat);

        // Configure SwipeRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
    }

    private void setupRecyclerView() {
        chatRoomAdapter = new ChatRoomAdapter(requireContext(), currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(chatRoomAdapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadChatRooms);
        fabNewChat.setOnClickListener(v -> {
            startNewChat();
            Log.d(TAG, String.format("New chat button clicked at %s by user %s",
                    TimeUtils.getCurrentUTCTime(), CURRENT_USER));
        });
    }

    private void loadChatRooms() {
        String currentTime = TimeUtils.getCurrentUTCTime();

        if (currentUserId == null) {
            Log.e(TAG, String.format("Cannot load chat rooms: no user ID at %s", currentTime));
            hideLoadingState();
            return;
        }

        if (isLoadingRooms) {
            Log.d(TAG, String.format("Skip loading: already in progress at %s", currentTime));
            return;
        }

        showLoadingState();
        isLoadingRooms = true;

        new Thread(() -> {
            try {
                final List<ChatRoom> rooms = userRepository.getChatRoomsForUser(currentUserId);

                if (getActivity() == null) {
                    isLoadingRooms = false;
                    return;
                }

                getActivity().runOnUiThread(() -> {
                    try {
                        if (rooms != null && !rooms.isEmpty()) {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyState.setVisibility(View.GONE);
                            chatRoomAdapter.setChatRooms(rooms);
                            Log.d(TAG, String.format("Loaded and displayed %d chat rooms at %s",
                                    rooms.size(), currentTime));
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            emptyState.setVisibility(View.VISIBLE);
                            Log.d(TAG, String.format("No chat rooms found at %s", currentTime));
                        }
                    } finally {
                        hideLoadingState();
                        isLoadingRooms = false;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, String.format("Error loading chat rooms at %s: %s",
                        currentTime, e.getMessage()));
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showError("Failed to load chat rooms");
                        hideLoadingState();
                        isLoadingRooms = false;
                    });
                }
            }
        }).start();
    }

    private void showLoadingState() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(true);
        });
    }

    private void hideLoadingState() {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            loadingIndicator.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void showError(String message) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void startNewChat() {
        try {
            Intent intent = new Intent(getActivity(), NewChatActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, String.format("Error starting new chat at %s: %s",
                    TimeUtils.getCurrentUTCTime(), e.getMessage()));
            showError("Failed to start new chat");
        }
    }

    private void startPolling() {
        new Thread(() -> {
            while (isPolling && !Thread.interrupted()) {
                try {
                    loadChatRooms();
                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPolling = true;
        loadChatRooms();
        startPolling();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPolling = false;
    }
}