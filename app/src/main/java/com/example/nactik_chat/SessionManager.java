package com.example.nactik_chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String PREF_NAME = "ChatAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_PHONE = "phone";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String phone) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_PHONE, phone);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public String getUserId() {
        String userId = pref.getString(KEY_USER_ID, null);
        Log.d("SessionManager", "getUserId called, returning: " + userId);
        return userId;
    }
    public String getUserphone() {
        String userphone = pref.getString(KEY_PHONE, null);
        Log.d("SessionManager", "getUserId called, returning: " + userphone);
        return userphone;
    }
}