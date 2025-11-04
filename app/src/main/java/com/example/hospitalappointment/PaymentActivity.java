package com.example.hospitalappointment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvThankYou, tvTotalAmount, tvSelectedPaymentMethod;
    private Button btnConfirmPayment;
    private RadioGroup rgPaymentMethods;
    private String selectedPaymentMethod = "";

    private String doctorId, doctorName, doctorCategory, doctorDate, doctorTime, doctorImage, doctorPrice;
    private double doctorRating;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        EdgeToEdge.enable(this);

        // Initialize Views
        ivBack = findViewById(R.id.ivPaymentBack);
        tvThankYou = findViewById(R.id.tvThankYou);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvSelectedPaymentMethod = findViewById(R.id.tvSelectedPaymentMethod);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        rgPaymentMethods = findViewById(R.id.rgPaymentMethods);

        // Ensure no radio button is selected initially
        rgPaymentMethods.clearCheck();

        // Back button functionality
        ivBack.setOnClickListener(v -> finish());

        // Get doctor details from Intent
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorCategory = getIntent().getStringExtra("doctorCategory");
        doctorDate = getIntent().getStringExtra("doctorDate");
        doctorTime = getIntent().getStringExtra("doctorTime");
        doctorImage = getIntent().getStringExtra("doctorImage");
        doctorPrice = getIntent().getStringExtra("doctorPrice");
        doctorRating = getIntent().getDoubleExtra("doctorRating", 0.0);

        // Set dynamic total amount
        if (doctorPrice != null && !doctorPrice.isEmpty()) {
            tvTotalAmount.setText("Total Amount: \n ₹" + doctorPrice);
        } else {
            tvTotalAmount.setText("Total Amount: \n N/A");
        }

        // Set doctor amount text
        TextView tvDoctorPrice = findViewById(R.id.tvDoctorPrice);
        if (doctorPrice != null && !doctorPrice.isEmpty()) {
            tvDoctorPrice.setText("Amount: ₹" + doctorPrice);
        } else {
            tvDoctorPrice.setText("Amount: N/A");
        }

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Listen for payment method selection
        rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadio = findViewById(checkedId);
            if (selectedRadio != null) {
                selectedPaymentMethod = selectedRadio.getText().toString();
                tvSelectedPaymentMethod.setText("Payment Method: " + selectedPaymentMethod);
            }
        });

        // Debug: Add a test button to directly go to SuccessActivity (remove this later)
        // You can add this temporarily to test if SuccessActivity works
        // Uncomment the lines below to test:
        /*
        Button testButton = findViewById(R.id.btnTestSuccess); // You need to add this button in XML
        if (testButton != null) {
            testButton.setOnClickListener(v -> {
                Intent testIntent = new Intent(PaymentActivity.this, SuccessActivity.class);
                testIntent.putExtra("doctorName", "Test Doctor");
                testIntent.putExtra("doctorCategory", "Test Category");
                testIntent.putExtra("doctorDate", "Test Date");
                testIntent.putExtra("doctorTime", "Test Time");
                startActivity(testIntent);
            });
        }
        */

        // Confirm Appointment / Pay button click
        btnConfirmPayment.setOnClickListener(v -> {
            // Debug: Check if user is logged in
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in! Please login first.", Toast.LENGTH_LONG).show();
                // Redirect to login page
                Intent loginIntent = new Intent(PaymentActivity.this, Login.class);
                startActivity(loginIntent);
                return;
            }

            if (selectedPaymentMethod.isEmpty()) {
                Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userEmail = currentUser.getEmail();
            String userId = currentUser.getUid();

            DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                    .getReference("Appointments");

            String appointmentId = appointmentsRef.push().getKey();

            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("appointmentId", appointmentId);
            appointmentData.put("userId", userId);
            appointmentData.put("userEmail", userEmail);
            appointmentData.put("doctorId", doctorId);
            appointmentData.put("doctorName", doctorName);
            appointmentData.put("doctorCategory", doctorCategory);
            appointmentData.put("date", doctorDate);
            appointmentData.put("time", doctorTime);
            appointmentData.put("doctorImage", doctorImage);
            appointmentData.put("doctorPrice", doctorPrice);
            appointmentData.put("doctorRating", doctorRating);
            appointmentData.put("paymentMethod", selectedPaymentMethod);
            appointmentData.put("status", "Booked");

            appointmentsRef.child(appointmentId).setValue(appointmentData)
                    .addOnSuccessListener(aVoid -> {
                        // Show success toast
                        Toast.makeText(PaymentActivity.this,
                                "Appointment booked successfully!\nPaid via " + selectedPaymentMethod,
                                Toast.LENGTH_SHORT).show();

                        // Show appointment confirmation notification
                        NotificationManager.showAppointmentConfirmation(
                                PaymentActivity.this,
                                doctorName,
                                doctorCategory,
                                doctorDate,
                                doctorTime,
                                doctorPrice,
                                selectedPaymentMethod
                        );

                        // Schedule appointment reminder (30 minutes before)
                        AppointmentReminderScheduler.scheduleAppointmentReminder(
                                PaymentActivity.this,
                                doctorName,
                                doctorCategory,
                                doctorDate,
                                doctorTime,
                                30 // 30 minutes before appointment
                        );

                        // Debug: Log before navigating to SuccessActivity
                        System.out.println("DEBUG: Navigating to SuccessActivity with doctor: " + doctorName);

                        // Directly go to SuccessActivity with appointment details
                        Intent intent = new Intent(PaymentActivity.this, SuccessActivity.class);
                        intent.putExtra("doctorName", doctorName);
                        intent.putExtra("doctorCategory", doctorCategory);
                        intent.putExtra("doctorDate", doctorDate);
                        intent.putExtra("doctorTime", doctorTime);
                        intent.putExtra("doctorPrice", doctorPrice);
                        intent.putExtra("paymentMethod", selectedPaymentMethod);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        // Finish PaymentActivity
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to book: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        System.out.println("DEBUG: Booking failed with error: " + e.getMessage());
                    });

        });
    }
}
