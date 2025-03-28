package com.example.mychatapptutorial;

public interface ChatCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}