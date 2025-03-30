package com.example.nactik_chat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DatabaseService extends Service {
    private static final String TAG = "DatabaseService";
    private static final int RECONNECT_DELAY = 5000; // 5 seconds
    private boolean isRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            maintainConnection();
        }
        return START_STICKY;
    }

    private void maintainConnection() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    DatabaseHelper.getInstance().getConnection();
                    Thread.sleep(RECONNECT_DELAY);
                } catch (Exception e) {
                    Log.e(TAG, "Connection maintenance failed", e);
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }
}