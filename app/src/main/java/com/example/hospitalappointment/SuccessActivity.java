package com.example.hospitalappointment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SuccessActivity extends AppCompatActivity {

    private TextView tvSuccessTitle, tvSuccessMessage;
    private LinearLayout navHome, navInbox, navUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_success);
        
        // Debug: Log that SuccessActivity is being created
        System.out.println("DEBUG: SuccessActivity onCreate called");
        
        // Initialize views
        initializeViews();
        
        // Set up navigation listeners
        setupNavigationListeners();
        
        // Get appointment details from intent if available
        displayAppointmentDetails();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        tvSuccessTitle = findViewById(R.id.tvSuccessTitle);
        tvSuccessMessage = findViewById(R.id.tvSuccessMessage);
        
        // Initialize navigation buttons
        navHome = findViewById(R.id.navHome);
        navInbox = findViewById(R.id.navInbox);
        navUser = findViewById(R.id.navUser);
    }

    private void setupNavigationListeners() {
        // Home button click listener
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finish SuccessActivity so user can't go back to it
            }
        });

        // Inbox button click listener
        navInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessActivity.this, AppointmentHistoryActivity.class);
                startActivity(intent);
            }
        });

        // User button click listener
        navUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuccessActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void displayAppointmentDetails() {
        // Get appointment details from intent if available
        String doctorName = getIntent().getStringExtra("doctorName");
        String doctorCategory = getIntent().getStringExtra("doctorCategory");
        String appointmentDate = getIntent().getStringExtra("doctorDate");
        String appointmentTime = getIntent().getStringExtra("doctorTime");
        
        if (doctorName != null && !doctorName.isEmpty()) {
            // You can customize the message based on appointment details
            String customMessage = "Your appointment with Dr. " + doctorName + 
                                 " (" + doctorCategory + ") on " + appointmentDate + 
                                 " at " + appointmentTime + " has been confirmed. " +
                                 "Please arrive 15 minutes early and carry any previous medical reports or prescriptions.";
            tvSuccessMessage.setText(customMessage);
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button press
        Intent intent = new Intent(SuccessActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}