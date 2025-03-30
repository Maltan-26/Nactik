package com.example.nactik_chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;



public class splashscreen extends AppCompatActivity {

    private static int SPLASH_TIMER=3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SessionManager sessionManager = new SessionManager(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (!sessionManager.isLoggedIn() || sessionManager.getUserId() == null) {
                    // User not logged in, go to login screen
                    startActivity(new Intent(splashscreen.this, MainActivity.class));
                } else {
                    // User is logged in, go to chat
                    startActivity(new Intent(splashscreen.this, chatActivity.class));
                }
                finish();


            }
        },SPLASH_TIMER);

    }}


