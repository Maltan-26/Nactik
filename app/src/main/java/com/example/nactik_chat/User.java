package com.example.nactik_chat;

public class User {
    private String uid;
    private String name;
    private String imageUrl;
    private String status;

    private String phonenumber;

    public User(String uid, String name, String imageUrl, String status, String phonenumber) {
        this.uid = uid;
        this.phonenumber = phonenumber;
        this.name = name;
        this.imageUrl = imageUrl;
        this.status = status;
    }
    public String getPhone() {
        return this.phonenumber;
    }

    public String getName() {
        return this.name;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public String getStatus() {
        return this.status;
    }

    public String getUid() {
        return this.uid;
    }

    // Getters and setters
}