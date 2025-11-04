package com.example.hospitalappointment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etMobileNumber;
    private Button btnSendOTP;
    private ImageView ivBack;
    private TextView tvBackToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize views
        initializeViews();

        // Set up click listeners
        setupClickListeners();

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
        etMobileNumber = findViewById(R.id.etMobileNumber);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        ivBack = findViewById(R.id.ivBack);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // Back button click
        ivBack.setOnClickListener(v -> finish());

        // Back to login click
        tvBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Send OTP button click
        btnSendOTP.setOnClickListener(v -> sendOTP());
    }

    private void sendOTP() {
        String mobileNumber = etMobileNumber.getText().toString().trim();

        // Validate mobile number
        if (mobileNumber.isEmpty()) {
            etMobileNumber.setError("Mobile number is required");
            etMobileNumber.requestFocus();
            return;
        }

        if (mobileNumber.length() < 10) {
            etMobileNumber.setError("Please enter a valid mobile number");
            etMobileNumber.requestFocus();
            return;
        }

        // Show progress bar and disable button
        showLoading(true);

        // Generate 6-digit OTP
        generatedOTP = generateOTP();

        // Check if mobile number exists in database
        Query query = usersRef.orderByChild("phone").equalTo(mobileNumber);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                
                if (snapshot.exists()) {
                    // Mobile number found, send OTP
                    Toast.makeText(ForgotPasswordActivity.this, 
                            "OTP sent to " + mobileNumber, 
                            Toast.LENGTH_LONG).show();
                    
                    // Show OTP in dialog for testing
                    showOTPDialog(mobileNumber, generatedOTP);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, 
                            "No account found with this mobile number", 
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, 
                        "Error checking mobile number: " + error.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate 6-digit OTP
        return String.valueOf(otp);
    }

    private void showOTPDialog(String mobileNumber, String otp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📱 OTP Sent");
        builder.setMessage("OTP has been sent to " + mobileNumber + "\n\n" +
                          "For testing purposes, your OTP is:\n\n" +
                          "🔢 " + otp + "\n\n" +
                          "Click 'Continue' to proceed to verification.");
        builder.setPositiveButton("Continue", (dialog, which) -> {
            // Navigate to OTP verification screen
            Intent intent = new Intent(ForgotPasswordActivity.this, OTPVerificationActivity.class);
            intent.putExtra("mobileNumber", mobileNumber);
            intent.putExtra("otp", otp);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnSendOTP.setEnabled(false);
            btnSendOTP.setText("Sending...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnSendOTP.setEnabled(true);
            btnSendOTP.setText("Send OTP");
        }
    }
}
