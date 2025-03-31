package com.example.nactik_chat;

import android.content.Context;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    private static final String TAG = "TimeUtils";
    private static final SimpleDateFormat UTC_FORMAT;
    private static Context applicationContext;

    static {
        UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static String getCurrentUTCTime() {
        try {
            return UTC_FORMAT.format(new Date());
        } catch (Exception e) {
            String errorMsg = "Error generating timestamp: " + e.getMessage();
            Log.e(TAG, errorMsg);
            return "";
        }
    }

    public static long getCurrentTimestampUTC() {
        return System.currentTimeMillis();
    }
}