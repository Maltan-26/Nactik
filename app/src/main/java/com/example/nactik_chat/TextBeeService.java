package com.example.nactik_chat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;  // Add this import
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TextBeeService {
    private static final String TAG = "TextBeeService";
    private static final String BASE_URL = "https://api.textbee.dev/v1/"; // Replace with your TextBee instance URL
    private static final String API_KEY = "80083030-00fd-4db8-a430-e493092b3494"; // Replace with your TextBee API key
    private static final String SENDER_ID = "NACTIK"; // Your app's sender ID

    private final OkHttpClient client;
    private final Gson gson;

    public TextBeeService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public void sendOTP(String phoneNumber, String otp, SMSCallback callback) {
        String currentTime = TimeUtils.getCurrentUTCTime();
        Log.d(TAG, String.format("Sending OTP at %s to %s", currentTime, phoneNumber));

        // Create message body
        String messageBody = String.format(
                "Your NACTIK verification code is: %s. Valid for 5 minutes. Do not share this code.",
                otp
        );

        // Create request body
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("to", phoneNumber);
        jsonBody.addProperty("message", messageBody);
        jsonBody.addProperty("sender_id", SENDER_ID);

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        // Create request
        Request request = new Request.Builder()
                .url(BASE_URL + "messages/send")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, String.format("SMS sending failed at %s: %s",
                        TimeUtils.getCurrentUTCTime(), e.getMessage()));
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful() || responseBody == null) {
                        String error = String.format("Error: %s", response.code());
                        Log.e(TAG, String.format("SMS sending failed at %s: %s",
                                TimeUtils.getCurrentUTCTime(), error));
                        callback.onFailure(error);
                        return;
                    }

                    String jsonResponse = responseBody.string();
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

                    if (jsonObject.has("success") && jsonObject.get("success").getAsBoolean()) {
                        String messageId = jsonObject.get("message_id").getAsString();
                        Log.d(TAG, String.format("SMS sent successfully at %s, ID: %s",
                                TimeUtils.getCurrentUTCTime(), messageId));
                        callback.onSuccess(messageId);
                    } else {
                        String error = jsonObject.has("error") ?
                                jsonObject.get("error").getAsString() : "Unknown error";
                        Log.e(TAG, String.format("SMS sending failed at %s: %s",
                                TimeUtils.getCurrentUTCTime(), error));
                        callback.onFailure(error);
                    }
                }
            }
        });
    }

    // Callback interface for SMS operations
    public interface SMSCallback {
        void onSuccess(String messageId);
        void onFailure(String error);
    }
}
