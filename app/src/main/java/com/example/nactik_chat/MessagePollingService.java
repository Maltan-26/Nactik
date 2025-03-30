package com.example.nactik_chat;

import android.os.Handler;
import android.os.Looper;

import java.util.List;

public class MessagePollingService {
    private static final long POLLING_INTERVAL = 3000; // 3 seconds
    private final MessageRepository messageRepository;
    private final Handler handler;
    private String currentRoomId;
    private long lastMessageTimestamp;
    private MessageCallback callback;
    private boolean isPolling;

    public interface MessageCallback {
        void onNewMessages(List<Message> messages);
        void onError(String error);
    }

    public MessagePollingService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startPolling(String roomId, long lastTimestamp, MessageCallback callback) {
        this.currentRoomId = roomId;
        this.lastMessageTimestamp = lastTimestamp;
        this.callback = callback;
        this.isPolling = true;
        pollMessages();
    }

    public void stopPolling() {
        isPolling = false;
        handler.removeCallbacksAndMessages(null);
    }

    private void pollMessages() {
        if (!isPolling) return;

        new Thread(() -> {
            try {
                List<Message> newMessages = messageRepository.getMessagesForRoom(
                        currentRoomId, lastMessageTimestamp
                );

                if (!newMessages.isEmpty()) {
                    lastMessageTimestamp = newMessages.get(newMessages.size() - 1).getTimestamp();
                    handler.post(() -> callback.onNewMessages(newMessages));
                }
            } catch (Exception e) {
                handler.post(() -> callback.onError(e.getMessage()));
            }

            handler.postDelayed(this::pollMessages, POLLING_INTERVAL);
        }).start();
    }
}