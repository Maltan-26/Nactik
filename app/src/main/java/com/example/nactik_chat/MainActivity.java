package com.example.nactik_chat;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.hbb20.CountryCodePicker;

public class MainActivity extends AppCompatActivity {
    private EditText mgetphonenumber;
    private Button msendotp;
    private final String CHANNEL_ID = "otp_notification_channel";
    private CountryCodePicker mcountrycodepicker;
    private String countrycode;
    private String phonenumber;
    private ProgressBar mprogressbarofmain;

    private AuthRepository authRepository;
    private OtpService otpService;
    private  TextBeeService textBeeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.textBeeService = new TextBeeService();
        createNotificationChannel();
        requestNotificationPermission();
        initializeViews();
        initializeServices();
        setupCountryCodePicker();
        setupSendOtpButton();

    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    private void initializeViews() {
        mcountrycodepicker = findViewById(R.id.countrycodepicker);
        msendotp = findViewById(R.id.sendotpbutton);
        mgetphonenumber = findViewById(R.id.getphonenumber);
        mprogressbarofmain = findViewById(R.id.progressbarofmain);
    }

    private void initializeServices() {
        authRepository = new AuthRepository(DatabaseHelper.getInstance());
        otpService = new OtpService();
    }

    private void setupCountryCodePicker() {
        countrycode = mcountrycodepicker.getSelectedCountryCodeWithPlus();
        mcountrycodepicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countrycode = mcountrycodepicker.getSelectedCountryCodeWithPlus();
            }
        });
    }

    private void setupSendOtpButton() {
        msendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = mgetphonenumber.getText().toString();
                if (validatePhoneNumber(number)) {
                      sendOtp(number);
                }
            }
        });
    }

    private boolean validatePhoneNumber(String number) {
        if (number.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter Your number", Toast.LENGTH_SHORT).show();
            return false;
        } else if (number.length() < 10) {
            Toast.makeText(getApplicationContext(), "Please Enter correct number", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private String  otp;
    private void sendOtp(String number) {
        mprogressbarofmain.setVisibility(View.VISIBLE);
        phonenumber = countrycode + number;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Generate and save OTP
                     otp = otpService.generateOtp(phonenumber);
                    authRepository.saveOtpForPhone(phonenumber, otp);

                    runOnUiThread(() -> {

                        mprogressbarofmain.setVisibility(View.INVISIBLE);
                        // Show OTP using a notification
                        showOtpNotification(otp);
                        Toast.makeText(getApplicationContext(), "OTP Notification Sent", Toast.LENGTH_SHORT).show();
                        navigateToOtpScreen(otp, phonenumber);
                    });



                    // Send OTP via SMS service
//                    textBeeService.sendOTP(phonenumber, otp, new TextBeeService.SMSCallback() {
//                        @Override
//                        public void onSuccess(String messageId) {
//                            Toast.makeText(getApplicationContext(), "OTP is Sent", Toast.LENGTH_SHORT).show();
//                            navigateToOtpScreen(otp, phonenumber);
//                        }
//
//                        @Override
//                        public void onFailure(String error) {
//                            Toast.makeText(getApplicationContext(), "Failed to send OTP", Toast.LENGTH_SHORT).show();
//                        }
//                    });



                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mprogressbarofmain.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "OTP is Sent", Toast.LENGTH_SHORT).show();
                            navigateToOtpScreen(otp, phonenumber);
                            System.out.println( e.getMessage()+e);
                        }
                    });
                }

            }
        }).start();
    }
    private void showOtpNotification(String otp) {
        // Build the notification with the OTP information
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.mychatapplogo) // Replace with your notification icon
                .setContentTitle("OTP Verification")
                .setContentText("Your OTP is: " + otp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Display the notification with a unique notification ID (e.g., 101)
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(101, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "OTP Notification";
            String description = "Channel for OTP notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void navigateToOtpScreen(String otp,String phonenumber) {
        Intent intent = new Intent(MainActivity.this, otpAuthentication.class);
        intent.putExtra("otp", otp);
        intent.putExtra("phoneNumber", phonenumber);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkExistingSession();
    }

    private void checkExistingSession() {
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, chatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}