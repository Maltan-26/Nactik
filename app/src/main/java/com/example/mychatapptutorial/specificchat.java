package com.example.mychatapptutorial;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class specificchat extends AppCompatActivity {
    private static final String TAG = "specificchat";
    private String roomId;
    private static final String CURRENT_TIME = "2025-03-27 19:08:52";
    private static  String CURRENT_USER;

    // UI Components
    private MaterialCardView msendmessagecardview;
    private RecyclerView mrecyclerview;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private ImageButton backButton;
    private TextView userNameText;
    private ImageView userProfileImage;
    private Toolbar toolbar;

    // Data
    private MessagesAdapter messagesAdapter;
    private ArrayList<Message> messagesArrayList;
    private ChatRepository chatRepository;
    private String receiverUserId;
    private String receiverUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificchat);

        // Get current user first
        CURRENT_USER = getIntent().getStringExtra("currentUser");
        if (CURRENT_USER == null) {
            CURRENT_USER = "Maltan-26"; // Default value
        }

        initializeViews();
        setupToolbar();

        // Generate room ID after getting receiver ID
        roomId = chatRepository.generateRoomId(CURRENT_USER, receiverUserId);

        setupRecyclerView();
        setupClickListeners();
        createChatRoomIfNeeded();
        // loadMessages will be called from createChatRoomIfNeeded
    }

    private void createChatRoomIfNeeded() {
        chatRepository.createChatRoomAsync(CURRENT_USER, receiverUserId, new ChatCallback<String>() {
            @Override
            public void onSuccess(String createdRoomId) {
                if (createdRoomId != null) {
                    loadMessages();
                } else {
                    Toast.makeText(specificchat.this, "Failed to create chat room",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(specificchat.this, "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            sendButton.setEnabled(false);
            long timestamp = System.currentTimeMillis();
            String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                    .format(new Date());

            Message pendingMessage = new Message(messageText, CURRENT_USER, timestamp, timeString);
            messagesArrayList.add(0, pendingMessage);
            messagesAdapter.notifyItemInserted(0);
            mrecyclerview.scrollToPosition(0);

            messageInput.setText("");

            chatRepository.sendMessageAsync(roomId, CURRENT_USER, messageText, timestamp, timeString,
                    new ChatCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            sendButton.setEnabled(true);
                            loadMessages();
                        }

                        @Override
                        public void onError(Exception e) {
                            sendButton.setEnabled(true);
                            messagesArrayList.remove(pendingMessage);
                            messagesAdapter.notifyDataSetChanged();
                            Toast.makeText(specificchat.this, "Failed to send message: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadMessages() {
        chatRepository.getMessagesAsync(roomId, new ChatCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {
                messagesAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    mrecyclerview.scrollToPosition(0);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(specificchat.this, "Failed to load messages: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        // Initialize all views
        msendmessagecardview = findViewById(R.id.carviewofsendmessage);
        mrecyclerview = findViewById(R.id.recyclerviewofspecific);
        messageInput = findViewById(R.id.getmessage);
        sendButton = findViewById(R.id.imageviewsendmessage);
        backButton = findViewById(R.id.backbuttonofspecificchat);
        userNameText = findViewById(R.id.Nameofspecificuser);
        userProfileImage = findViewById(R.id.specificuserimageinimageview);
        toolbar = findViewById(R.id.toolbarofspecificchat);

        // Initialize data objects
        chatRepository = new ChatRepository();
        messagesArrayList = new ArrayList<>();

        // Get intent extras
        receiverUserId = getIntent().getStringExtra("receiveruid");
        receiverUserName = getIntent().getStringExtra("name");
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        userNameText.setText(receiverUserName);
    }

    private void setupRecyclerView() {
        messagesAdapter = new MessagesAdapter(this, messagesArrayList);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(messagesAdapter);
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        backButton.setOnClickListener(v -> onBackPressed());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatRepository != null) {
            chatRepository.shutdown();
        }
    }
    public String getCurrentUser() {
        return CURRENT_USER;
    }

}