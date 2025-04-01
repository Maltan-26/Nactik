package com.example.nactik_chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Constraints;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class specificchat extends AppCompatActivity {
    private static final String TAG = "specificchat";
    private String roomId;
    private static final String CURRENT_TIME = "2025-03-27 19:08:52";
    private static  long CURRENT_USER;

    // UI Components
    private MaterialCardView msendmessagecardview;
    private RecyclerView mrecyclerview;
    private EditText messageInput;
    private FloatingActionButton sendButton;
    private ImageButton backButton;
    private TextView userNameText;
    private ImageView userProfileImage;
    private Toolbar toolbar;
    private static final long DEFAULT_USER_ID = 0L;
    // Data
    private MessagesAdapter messagesAdapter;
    private ArrayList<Message> messagesArrayList;
    private ChatRepository chatRepository;
    private Long receiverUserId;
    private String receiverUserName;
    private final UserRepository userRepository  = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specificchat);
        try {
            String userIdStr = getIntent().getStringExtra("userName");
            Log.d(TAG, "Received userName from intent: " + userIdStr);

            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    CURRENT_USER = Long.parseLong(userIdStr.trim());
                    Log.d(TAG, "Successfully parsed CURRENT_USER: " + CURRENT_USER);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing CURRENT_USER: " + e.getMessage());
                    CURRENT_USER = DEFAULT_USER_ID;
                }
            } else {
                Log.w(TAG, "No userName provided in intent");
                CURRENT_USER = DEFAULT_USER_ID;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting userName from intent: " + e.getMessage());
            CURRENT_USER = DEFAULT_USER_ID;
        }

// In initializeViews method:
        String receiverUidStr = getIntent().getStringExtra("receiveruid");
        Log.d(TAG, "Received receiveruid from intent: " + receiverUidStr);

        if (receiverUidStr != null && !receiverUidStr.trim().isEmpty()) {
            try {
                receiverUserId = Long.parseLong(receiverUidStr.trim());
                Log.d(TAG, "Successfully parsed receiverUserId: " + receiverUserId);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing receiverUserId: " + e.getMessage());
                receiverUserId = DEFAULT_USER_ID;
            }
        } else {
            Log.w(TAG, "No receiveruid provided in intent");
            receiverUserId = DEFAULT_USER_ID;
        }

        receiverUserName = getIntent().getStringExtra("receivername");
        Log.d(TAG, "Received receivername from intent: " + receiverUserName);

        initializeViews();
        setupToolbar();
        System.out.println("" + CURRENT_USER + receiverUserId + receiverUserName);

        // Generate room ID after getting receiver ID

        setupRecyclerView();

        setupClickListeners();
        createChatRoomIfNeeded();
        loadMessages();
        // loadMessages will be called from createChatRoomIfNeeded
    }

    private void createChatRoomIfNeeded() {
        new Thread(() -> {
            try {

                if(!userRepository.isRoomExist(userRepository.generateRoomId(CURRENT_USER,receiverUserId))){

                String rid = userRepository.createChatRoom(CURRENT_USER, receiverUserId);
                runOnUiThread(() -> {
                    if(rid != null){
                        roomId = rid;
                       loadMessages();
                    }
                    else {
                        System.out.println("Error creating chat room");
                    }
                });}
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    public static String getCurrentUTCTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.format(new Date());
        } catch (Exception e) {
            Log.e(TAG, "Error generating UTC timestamp: " + e.getMessage());
            return "";
        }
    }
    private void sendMessage() {

        String rid = userRepository.generateRoomId(CURRENT_USER,receiverUserId);
        String messageText = messageInput.getText().toString().trim();

        if (!messageText.isEmpty()) {

            sendButton.setEnabled(false);
            long timestamp = System.currentTimeMillis();
            String timeString = getCurrentUTCTime(); // Using the TimeUtils class

            Message pendingMessage = new Message(0, rid, CURRENT_USER, messageText, timestamp, timeString,false,false,"text",null );
            new Thread(() -> {
            runOnUiThread(() -> {
            // Add to UI immediately
            messagesArrayList.add(0, pendingMessage);
            messagesAdapter.notifyItemInserted(0);
            mrecyclerview.scrollToPosition(0);
            messageInput.setText("");
            });

            try {
               long mid = userRepository.saveMessage(pendingMessage);
                loadMessages();
               System.out.println(mid+"is  the massage send");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            }).start();

        }

    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                chatRepository.getMessagesAsync(userRepository.generateRoomId(CURRENT_USER,receiverUserId), new ChatCallback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> messages) {
                        messagesAdapter.setMessages(messages);
                        if (!messages.isEmpty()) {
                            mrecyclerview.scrollToPosition(0);
                        }
                        new Thread(() -> {
                        loadMessages();}).start();
                    }

                    @Override
                    public void onError(Exception e) {

                        Toast.makeText(specificchat.this, "Failed to load messages: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        new Thread(() -> {
                            loadMessages();}).start();

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


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
        String userIdStr = getIntent().getStringExtra("receiveruid");
        System.out.println(userIdStr);
        if (userIdStr != null && !userIdStr.trim().isEmpty()) {
            try {
                receiverUserId = Long.parseLong(userIdStr.trim());
                Log.d(TAG, "Successfully parsed userId: " + receiverUserId);
            } catch (NumberFormatException e) {
                receiverUserId = DEFAULT_USER_ID;
                Log.e(TAG, "Error parsing userId: " + e.getMessage());
            }
        } else {
            receiverUserId = DEFAULT_USER_ID;
            Log.w(TAG, "No userName provided in intent");
        }
        receiverUserName = getIntent().getStringExtra("receivername");
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
    public long getCurrentUser() {
        return CURRENT_USER;
    }

}