package com.example.hospitalappointment;

import android.content.Intent;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText etOTP;
    private Button btnVerifyOTP;
    private ImageView ivBack;
    private TextView tvResendOTP, tvTimer, tvMobileNumber;
    private ProgressBar progressBar;
    
    private String mobileNumber;
    private String correctOTP;
    private String userId;
    private CountDownTimer countDownTimer;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp_verification);

        // Initialize Firebase Database
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Get data from previous activity
        Intent intent = getIntent();
        mobileNumber = intent.getStringExtra("mobileNumber");
        correctOTP = intent.getStringExtra("otp");

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Display mobile number
        if (mobileNumber != null) {
            tvMobileNumber.setText("+91 " + mobileNumber);
        }

        // Start countdown timer
        startCountdownTimer();

        // Edge-to-edge with proper system bar insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply padding only to the main container to avoid content hiding behind system bars
            View mainContainer = findViewById(R.id.main);
            if (mainContainer != null) {
                mainContainer.setPadding(
                    Math.max(systemBars.left, 0),
                    Math.max(systemBars.top, 0),
                    Math.max(systemBars.right, 0),
                    Math.max(systemBars.bottom, 0)
                );
            }
            
            return insets;
        });
    }

    private void initializeViews() {
        etOTP = findViewById(R.id.etOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        ivBack = findViewById(R.id.ivBack);
        tvResendOTP = findViewById(R.id.tvResendOTP);
        tvTimer = findViewById(R.id.tvTimer);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Back button click
        ivBack.setOnClickListener(v -> finish());

        // Verify OTP button click
        btnVerifyOTP.setOnClickListener(v -> verifyOTP());

        // Resend OTP click
        tvResendOTP.setOnClickListener(v -> resendOTP());
    }

    private void verifyOTP() {
        String enteredOTP = etOTP.getText().toString().trim();

        // Validate OTP
        if (enteredOTP.isEmpty()) {
            etOTP.setError("Please enter OTP");
            etOTP.requestFocus();
            return;
        }

        if (enteredOTP.length() != 6) {
            etOTP.setError("OTP must be 6 digits");
            etOTP.requestFocus();
            return;
        }

        // Show progress bar
        showLoading(true);

        // Verify OTP
        if (enteredOTP.equals(correctOTP)) {
            // OTP is correct, find user and navigate to password update
            findUserByMobileNumber();
        } else {
            showLoading(false);
            Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void findUserByMobileNumber() {
        Query query = usersRef.orderByChild("phone").equalTo(mobileNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                
                if (snapshot.exists()) {
                    // Get the first matching user
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next();
                    String password = userSnapshot.child("password").getValue(String.class);
                    String name = userSnapshot.child("name").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    
                    if (password != null && !password.isEmpty()) {
                        // User has password in database
                        showPasswordDialog(name, email, password);
                    } else {
                        // User exists but no password in database (old user)
                        // Generate a temporary password and update the database
                        String tempPassword = generateTempPassword();
                        updateUserPassword(userSnapshot.getKey(), tempPassword, name, email);
                    }
                } else {
                    Toast.makeText(OTPVerificationActivity.this, 
                            "User not found. Please try again.", 
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(OTPVerificationActivity.this, 
                        "Error finding user: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendOTP() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Generate new OTP
        correctOTP = generateOTP();

        // New OTP generated
        showOTPDialog(mobileNumber, correctOTP);

        // Start new countdown timer
        startCountdownTimer();
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate 6-digit OTP
        return String.valueOf(otp);
    }

    private void showOTPDialog(String mobileNumber, String otp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📱 OTP Resent");
        builder.setMessage("New OTP has been sent to " + mobileNumber + "\n\n" +
                          "For testing purposes, your OTP is:\n\n" +
                          "🔢 " + otp + "\n\n" +
                          "Click 'OK' to continue verification.");
        builder.setPositiveButton("OK", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPasswordDialog(String name, String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔐 Password Retrieved");
        builder.setMessage("OTP verified successfully!\n\n" +
                          "Name: " + (name != null ? name : "N/A") + "\n" +
                          "Email: " + (email != null ? email : "N/A") + "\n\n" +
                          "Your current password is:\n\n" +
                          "🔑 " + password + "\n\n" +
                          "Please use this password to login to your account.");
        builder.setPositiveButton("Go to Login", (dialog, which) -> {
            // Navigate to login screen
            Intent intent = new Intent(OTPVerificationActivity.this, Login.class);
            intent.putExtra("email", email);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String generateTempPassword() {
        // Generate a simple 8-character password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void updateUserPassword(String userId, String tempPassword, String name, String email) {
        showLoading(true);
        
        // Update password in database
        usersRef.child(userId).child("password").setValue(tempPassword)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        showPasswordDialog(name, email, tempPassword);
                    } else {
                        Toast.makeText(OTPVerificationActivity.this, 
                                "Failed to generate password. Please try again.", 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void startCountdownTimer() {
        tvResendOTP.setVisibility(View.GONE);
        tvTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(600000, 1000) { // 10 minutes = 600,000 milliseconds
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                
                if (minutes > 0) {
                    tvTimer.setText("Resend OTP in " + minutes + "m " + seconds + "s");
                } else {
                    tvTimer.setText("Resend OTP in " + seconds + " seconds");
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                tvResendOTP.setVisibility(View.VISIBLE);
            }
        };

        countDownTimer.start();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnVerifyOTP.setEnabled(false);
            btnVerifyOTP.setText("Verifying...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnVerifyOTP.setEnabled(true);
            btnVerifyOTP.setText("Verify OTP");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
