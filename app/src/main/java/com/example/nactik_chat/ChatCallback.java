package com.example.nactik_chat;

public interface ChatCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}