package com.example.nactik_chat;


public class ChatRoom {
    private String roomId;
    private String roomName;
    private String lastMessage;
    private String lastMessageTime;
    private String imageUrl;
    private boolean online;
    private int unreadCount;

    public ChatRoom(String roomId, String roomName, String lastMessage,
                    String lastMessageTime, String imageUrl, boolean online,
                    int unreadCount) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.lastMessage = lastMessage;
        // Convert timestamp to readable format if needed
        this.lastMessageTime = formatTimestamp(lastMessageTime);
        this.imageUrl = imageUrl;
        this.online = online;
        this.unreadCount = unreadCount;
    }
    private String formatTimestamp(String timestamp) {
        try {
            long time = Long.parseLong(timestamp);
            // Convert to date format or relative time
            // Example: return TimeUtils.getFormattedTime(time);
            return String.valueOf(time); // Or implement your time formatting logic
        } catch (NumberFormatException e) {
            return timestamp;
        }
    }


    public String getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public String getLastMessage() { return lastMessage; }
    public String getLastMessageTime() { return lastMessageTime; }
    public String getImageUrl() { return imageUrl; }
    public boolean isOnline() { return online; }
    public int getUnreadCount() { return unreadCount; }
}