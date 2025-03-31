package com.example.nactik_chat;
public class Message {
    private long messageId;
    private String roomId;
    private long  senderUid;
    private String messageText;
    private long timestamp;
    private String timeString;
    private boolean isRead;
    private boolean isDelivered;
    private String messageType;
    private String mediaUrl;
    private String senderName;        // From users table
    private String senderProfileImage; // From users table

    public Message(long messageId, String roomId, long senderUid, String messageText,
                   long timestamp, String timeString, boolean isRead, boolean isDelivered,
                   String messageType, String mediaUrl) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.senderUid = senderUid;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.timeString = timeString;
        this.isRead = isRead;
        this.isDelivered = isDelivered;
        this.messageType = messageType;
        this.mediaUrl = mediaUrl;
    }
    public Message(){

    }

    // Getters
    public long getMessageId() { return messageId; }
    public String getRoomId() { return roomId; }
    public long getSenderUid() { return senderUid; }
    public String getMessageText() { return messageText; }
    public long getTimestamp() { return timestamp; }
    public String getTimeString() { return timeString; }
    public boolean isRead() { return isRead; }
    public boolean isDelivered() { return isDelivered; }
    public String getMessageType() { return messageType; }
    public String getMediaUrl() { return mediaUrl; }
    public String getSenderName() { return senderName; }
    public String getSenderProfileImage() { return senderProfileImage; }

    // Setters
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderProfileImage(String senderProfileImage) {
        this.senderProfileImage = senderProfileImage;
    }
    public void setRead(boolean read) { isRead = read; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }

    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", senderUid='" + senderUid + "'" +
                ", messageText='" + messageText + "'" +
                ", timeString='" + timeString + "'" +
                "}";
    }

    public void setId(long id) {

    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setSenderId(long  senderId) {
        this.senderUid = senderId;
    }

    public void setMessage(String messageText) {
        this.messageText = messageText;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setCurrenttime(String timeString) {
        this.timeString =timeString;
    }

    public void setdelivered(boolean isDelivered) {
        this.isDelivered= isDelivered;
    }

    public void settype(String messageType) {
        this.messageType = messageType;
    }

    public void setmedia_url(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }
}