package com.example.nactik_chat;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class chatFragment extends Fragment {
    // Current values from your input
    private static final String CURRENT_TIME = "2025-03-27 16:44:05";
    private static final String CURRENT_USER = "Maltan-26";

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private ChatRepository chatRepository;

    public chatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize views and adapter
        initializeViews(view);
        setupRecyclerView();
        loadChats();

        return view;
    }

    private void initializeViews(View view) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        chatRepository = new ChatRepository();
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(getContext(), CURRENT_USER);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);
    }

    private void loadChats() {
        try {
            // Example of loading chats - implement according to your needs
            List<Message> chats = chatRepository.getRecentMessages("defaultRoom", 0);
            chatAdapter.setMessages(chats);
        } catch (Exception e) {
            Log.e("chatFragment", "Error loading chats: " + e.getMessage());
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading chats", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChats(); // Refresh chats when fragment becomes visible
    }
}