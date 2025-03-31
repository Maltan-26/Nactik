package com.example.nactik_chat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.List;

public class MessagePollingService {
    private static final String TAG = "MessagePollingService";
    private static final long POLLING_INTERVAL = 3000; // 3 seconds
    private static final String CURRENT_USER = "Maltan-26";

    private final MessageRepository messageRepository;
    private final Handler handler;
    private String currentRoomId;
    private long lastMessageTimestamp;
    private MessageCallback callback;
    private boolean isPolling;

    private final Object pollingLock = new Object();

    public interface MessageCallback {
        void onNewMessages(List<Message> messages);
        void onError(String error);
    }

    public MessagePollingService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void startPolling(String roomId, long lastTimestamp, MessageCallback callback) {
        String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:00:40

        if (roomId == null || roomId.trim().isEmpty()) {
            String errorMsg = String.format("Invalid room ID provided at %s", currentTime);
            Log.e(TAG, errorMsg);
            if (callback != null) {
                callback.onError(errorMsg);
            }
            return;
        }

        synchronized (pollingLock) {
            // Stop any existing polling
            stopPolling();

            Log.d(TAG, String.format("Starting polling at %s for room: %s, user: %s",
                    currentTime, roomId, CURRENT_USER));

            this.currentRoomId = roomId;
            this.lastMessageTimestamp = lastTimestamp;
            this.callback = callback;
            this.isPolling = true;
            pollMessages();
        }
    }


    public void stopPolling() {
        synchronized (pollingLock) {
            if (isPolling) {
                String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:00:40
                Log.d(TAG, String.format("Stopping polling at %s for room: %s",
                        currentTime, currentRoomId));

                isPolling = false;
                handler.removeCallbacksAndMessages(null);
                currentRoomId = null;
                callback = null;
            }
        }
    }

    private void pollMessages() {
        if (!isPolling) return;

        new Thread(() -> {
            String currentTime = TimeUtils.getCurrentUTCTime(); // 2025-03-31 08:00:40

            synchronized (pollingLock) {
                if (!isPolling || currentRoomId == null) {
                    return;
                }

                try {
                    List<Message> newMessages = messageRepository.getMessagesForRoom(
                            currentRoomId, lastMessageTimestamp
                    );

                    if (newMessages != null && !newMessages.isEmpty()) {
                        lastMessageTimestamp = newMessages.get(newMessages.size() - 1).getTimestamp();

                        Log.d(TAG, String.format("Found %d new messages at %s for room: %s",
                                newMessages.size(), currentTime, currentRoomId));

                        final MessageCallback currentCallback = this.callback;
                        if (currentCallback != null) {
                            handler.post(() -> currentCallback.onNewMessages(newMessages));
                        }
                    }
                } catch (Exception e) {
                    String errorMsg = String.format("Error polling at %s for room %s: %s",
                            currentTime, currentRoomId, e.getMessage());
                    Log.e(TAG, errorMsg);

                    final MessageCallback currentCallback = this.callback;
                    if (currentCallback != null) {
                        handler.post(() -> currentCallback.onError(errorMsg));
                    }
                }

                // Schedule next poll if still polling
                if (isPolling) {
                    handler.postDelayed(this::pollMessages, POLLING_INTERVAL);
                }
            }
        }).start();
    }

    // Utility method to get current room ID
    public String getCurrentRoomId() {
        synchronized (pollingLock) {
            return currentRoomId;
        }
    }

    public boolean isPolling() {
        synchronized (pollingLock) {
            return isPolling;
        }
    }
}