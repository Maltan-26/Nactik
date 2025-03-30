package com.example.nactik_chat;

public class Message {
    private static final String CURRENT_TIME = "2025-03-27 18:31:30";
    private static final String CURRENT_USER = "Maltan-26";

    private long id;
    private String message;
    private String senderId;
    private long timestamp;
    private String currenttime;
    private String roomId;
    private boolean isRead;
    private String senderName; // Added for sender's display name

    public Message() {
    }

    public Message(String message, String senderId, long timestamp, String currenttime) {
        this.message = message;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.currenttime = currenttime;
    }

    // Add the missing methods needed by ChatAdapter
    public String getMessageText() {
        return getMessage(); // Uses existing getMessage()
    }

    public String getTimeString() {
        return getCurrenttime(); // Uses existing getCurrenttime()
    }

    public String getSenderUid() {
        return getSenderId(); // Uses existing getSenderId()
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // Existing getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getCurrenttime() {
        return currenttime;
    }

    public String getMessage() {
        return message;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setMessage(String messageText) {
        this.message = messageText;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCurrenttime(String timeString) {
        this.currenttime = timeString;
    }

    // Add toString method for debugging
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", timestamp=" + timestamp +
                ", currenttime='" + currenttime + '\'' +
                ", roomId='" + roomId + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}