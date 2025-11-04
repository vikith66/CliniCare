package com.example.hospitalappointment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AppointmentHistoryActivity extends AppCompatActivity {

    private ImageView ivBack;
    private LinearLayout navHome, navInbox, navUser;
    private LinearLayout emptyStateLayout, appointmentsListLayout;
    private Button btnRefresh, btnClearAll;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference appointmentsRef;

    private List<AppointmentData> appointmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_history);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to view appointment history", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        initializeViews();
        setupClickListeners();
        
        // Clear any test appointments that might exist
        clearTestAppointments();
        
        // Test database connection
        testDatabaseConnection();
        
        loadAppointmentHistory();

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
        ivBack = findViewById(R.id.ivBack);
        navHome = findViewById(R.id.navHome);
        navInbox = findViewById(R.id.navInbox);
        navUser = findViewById(R.id.navUser);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        appointmentsListLayout = findViewById(R.id.appointmentsListLayout);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnClearAll = findViewById(R.id.btnClearAll);

        appointmentList = new ArrayList<>();
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentHistoryActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        navInbox.setOnClickListener(v -> Toast.makeText(this, "You are already in Inbox", Toast.LENGTH_SHORT).show());

        navUser.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentHistoryActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        btnRefresh.setOnClickListener(v -> {
            System.out.println("DEBUG: Refresh button clicked");
            loadAppointmentHistory();
        });

        btnClearAll.setOnClickListener(v -> showClearAllConfirmationDialog());

        // Add a test appointment for debugging (remove in production)
        findViewById(R.id.btnRefresh).setOnLongClickListener(v -> {
            createTestAppointment();
            return true;
        });
    }

    private void clearTestAppointments() {
        // Remove any test appointments that might have been created
        appointmentsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            try {
                                AppointmentData appointment = dataSnapshot.getValue(AppointmentData.class);
                                if (appointment != null && 
                                    (appointment.getDoctorName() != null && appointment.getDoctorName().contains("Test")) ||
                                    (appointment.getAppointmentId() != null && appointment.getAppointmentId().startsWith("test_"))) {
                                    
                                    System.out.println("DEBUG: Removing test appointment: " + appointment.getDoctorName());
                                    dataSnapshot.getRef().removeValue();
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: Error checking test appointment: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println("DEBUG: Error clearing test appointments: " + error.getMessage());
                    }
                });
    }

    private void testDatabaseConnection() {
        System.out.println("DEBUG: Testing database connection...");
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("DEBUG: Database connection successful! Total appointments: " + snapshot.getChildrenCount());
                if (snapshot.getChildrenCount() > 0) {
                    System.out.println("DEBUG: Database has appointments, proceeding with load...");
                } else {
                    System.out.println("DEBUG: Database is empty, no appointments found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("DEBUG: Database connection failed: " + error.getMessage());
                Toast.makeText(AppointmentHistoryActivity.this, "Database connection failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createTestAppointment() {
        // This is for debugging purposes - long press refresh button to create test appointment
        try {
            String testAppointmentId = "test_" + System.currentTimeMillis();
            Map<String, Object> testAppointment = new HashMap<>();
            testAppointment.put("appointmentId", testAppointmentId);
            testAppointment.put("userId", currentUser.getUid());
            testAppointment.put("userEmail", currentUser.getEmail());
            testAppointment.put("doctorId", "test_doctor_1");
            testAppointment.put("doctorName", "Dr. Test Doctor");
            testAppointment.put("doctorCategory", "Cardiologist");
            testAppointment.put("doctorRating", "4.8");
            testAppointment.put("doctorImage", "");
            testAppointment.put("doctorPrice", "500");
            // Create a future date (tomorrow)
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String futureDate = sdf.format(cal.getTime());
            testAppointment.put("date", futureDate); // Future date
            testAppointment.put("time", "10:30 AM");
            testAppointment.put("paymentMethod", "Credit Card");
            testAppointment.put("status", "Booked");

            appointmentsRef.child(testAppointmentId).setValue(testAppointment)
                    .addOnSuccessListener(aVoid -> {
                        System.out.println("DEBUG: Test appointment created successfully");
                        Toast.makeText(AppointmentHistoryActivity.this, "Test appointment created", Toast.LENGTH_SHORT).show();
                        loadAppointmentHistory(); // Refresh the list
                    })
                    .addOnFailureListener(e -> {
                        System.out.println("DEBUG: Failed to create test appointment: " + e.getMessage());
                        Toast.makeText(AppointmentHistoryActivity.this, "Failed to create test appointment", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            System.out.println("DEBUG: Error creating test appointment: " + e.getMessage());
            Toast.makeText(AppointmentHistoryActivity.this, "Error creating test appointment", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAppointmentHistory() {
        System.out.println("DEBUG: Loading appointment history for user: " + currentUser.getUid());
        System.out.println("DEBUG: User email: " + currentUser.getEmail());
        
        // Try multiple approaches to find appointments
        loadAppointmentsWithMultipleQueries();
    }

    private void loadAppointmentsWithMultipleQueries() {
        appointmentList.clear();
        
        // Method 1: Query by userId
        appointmentsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        System.out.println("DEBUG: Method 1 (userId query) - Found " + snapshot.getChildrenCount() + " appointments");
                        
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            try {
                                AppointmentData appointment = dataSnapshot.getValue(AppointmentData.class);
                                if (appointment != null) {
                                    System.out.println("DEBUG: Method 1 - Found appointment: " + appointment.getDoctorName());
                                    appointmentList.add(appointment);
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: Method 1 - Error parsing appointment: " + e.getMessage());
                            }
                        }
                        
                        // If no appointments found with userId, try userEmail
                        if (appointmentList.isEmpty()) {
                            System.out.println("DEBUG: Method 1 found no appointments, trying Method 2 (userEmail)");
                            loadAppointmentsByEmail();
                        } else {
                            System.out.println("DEBUG: Method 1 found " + appointmentList.size() + " appointments");
                            sortAndDisplayAppointments();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println("DEBUG: Method 1 failed: " + error.getMessage());
                        // Try Method 2 as fallback
                        loadAppointmentsByEmail();
                    }
                });
    }

    private void loadAppointmentsByEmail() {
        appointmentsRef.orderByChild("userEmail").equalTo(currentUser.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        System.out.println("DEBUG: Method 2 (userEmail query) - Found " + snapshot.getChildrenCount() + " appointments");
                        
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            try {
                                AppointmentData appointment = dataSnapshot.getValue(AppointmentData.class);
                                if (appointment != null) {
                                    System.out.println("DEBUG: Method 2 - Found appointment: " + appointment.getDoctorName());
                                    appointmentList.add(appointment);
                                }
                            } catch (Exception e) {
                                System.out.println("DEBUG: Method 2 - Error parsing appointment: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        System.out.println("DEBUG: Method 2 found " + appointmentList.size() + " appointments");
                        sortAndDisplayAppointments();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        System.out.println("DEBUG: Method 2 failed: " + error.getMessage());
                        // Try Method 3 as final fallback
                        loadAllAppointments();
                    }
                });
    }

    private void loadAllAppointments() {
        System.out.println("DEBUG: Method 3 - Loading ALL appointments and filtering manually");
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("DEBUG: Method 3 - Total appointments in database: " + snapshot.getChildrenCount());
                
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    try {
                        String appointmentUserId = dataSnapshot.child("userId").getValue(String.class);
                        String appointmentUserEmail = dataSnapshot.child("userEmail").getValue(String.class);
                        
                        System.out.println("DEBUG: Method 3 - Checking appointment: userId=" + appointmentUserId + ", userEmail=" + appointmentUserEmail);
                        
                        if ((appointmentUserId != null && appointmentUserId.equals(currentUser.getUid())) ||
                            (appointmentUserEmail != null && appointmentUserEmail.equals(currentUser.getEmail()))) {
                            
                            AppointmentData appointment = dataSnapshot.getValue(AppointmentData.class);
                            if (appointment != null) {
                                System.out.println("DEBUG: Method 3 - Found matching appointment: " + appointment.getDoctorName());
                                appointmentList.add(appointment);
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("DEBUG: Method 3 - Error checking appointment: " + e.getMessage());
                    }
                }

                System.out.println("DEBUG: Method 3 found " + appointmentList.size() + " appointments");
                sortAndDisplayAppointments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("DEBUG: Method 3 failed: " + error.getMessage());
                displayAppointments(); // Show empty state
            }
        });
    }

    private void sortAndDisplayAppointments() {
        // Sort by date (newest first)
        try {
            Collections.sort(appointmentList, (a1, a2) -> {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date date1 = sdf.parse(a1.getDate());
                    Date date2 = sdf.parse(a2.getDate());
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    return 0;
                }
            });
        } catch (Exception e) {
            System.out.println("DEBUG: Error sorting appointments: " + e.getMessage());
        }

        displayAppointments();
    }

    private void displayAppointments() {
        appointmentsListLayout.removeAllViews();

        if (appointmentList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            appointmentsListLayout.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            appointmentsListLayout.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);

            for (AppointmentData appointment : appointmentList) {
                View appointmentView = createAppointmentView(appointment);
                appointmentsListLayout.addView(appointmentView);
            }
        }
    }

    private View createAppointmentView(AppointmentData appointment) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.item_appointment_history, appointmentsListLayout, false);

            // Status
            TextView tvStatus = view.findViewById(R.id.tvAppointmentStatus);
            if (appointment.getDate() != null) {
                boolean isUpcoming = isUpcomingAppointment(appointment.getDate(), appointment.getTime());
                System.out.println("DEBUG: Appointment " + appointment.getDoctorName() + " - Date: " + appointment.getDate() + " - Time: " + appointment.getTime() + " - IsUpcoming: " + isUpcoming);
                
                if (isUpcoming) {
                    tvStatus.setText("Upcoming");
                    tvStatus.setBackgroundResource(R.drawable.status_upcoming_background);
                } else {
                    tvStatus.setText("Completed");
                    tvStatus.setBackgroundResource(R.drawable.status_completed_background);
                }
            } else {
                tvStatus.setText("Completed");
                tvStatus.setBackgroundResource(R.drawable.status_completed_background);
            }

            // Date
            TextView tvDate = view.findViewById(R.id.tvAppointmentDate);
            tvDate.setText(appointment.getDate() != null ? formatDateForHeader(appointment.getDate()) : "N/A");

            // Doctor Image
            CircleImageView ivDoctorImage = view.findViewById(R.id.ivDoctorImage);
            if (appointment.getDoctorImage() != null && !appointment.getDoctorImage().isEmpty()) {
                try {
                    byte[] decodedString = android.util.Base64.decode(appointment.getDoctorImage(), android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivDoctorImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    System.out.println("DEBUG: Error loading doctor image: " + e.getMessage());
                    ivDoctorImage.setImageResource(R.drawable.ic_doctor_placeholder);
                }
            } else {
                ivDoctorImage.setImageResource(R.drawable.ic_doctor_placeholder);
            }

            // Doctor Name
            TextView tvDoctorName = view.findViewById(R.id.tvDoctorName);
            tvDoctorName.setText(appointment.getDoctorName() != null ? appointment.getDoctorName() : "Unknown Doctor");

            // Doctor Category
            TextView tvDoctorCategory = view.findViewById(R.id.tvDoctorCategory);
            tvDoctorCategory.setText(appointment.getDoctorCategory() != null ? appointment.getDoctorCategory() : "General");

            // Doctor Rating
            TextView tvDoctorRating = view.findViewById(R.id.tvDoctorRating);
            String ratingStr = "0.0";
            if (appointment.getDoctorRating() != null) {
                if (appointment.getDoctorRating() instanceof String) {
                    ratingStr = (String) appointment.getDoctorRating();
                } else if (appointment.getDoctorRating() instanceof Long) {
                    ratingStr = String.valueOf((Long) appointment.getDoctorRating());
                } else if (appointment.getDoctorRating() instanceof Double) {
                    ratingStr = String.valueOf((Double) appointment.getDoctorRating());
                }
            }
            tvDoctorRating.setText("⭐ " + ratingStr);

            // Doctor Price
            TextView tvDoctorPrice = view.findViewById(R.id.tvDoctorPrice);
            String priceStr = "0";
            if (appointment.getDoctorPrice() != null) {
                if (appointment.getDoctorPrice() instanceof String) {
                    priceStr = (String) appointment.getDoctorPrice();
                } else if (appointment.getDoctorPrice() instanceof Long) {
                    priceStr = String.valueOf((Long) appointment.getDoctorPrice());
                } else if (appointment.getDoctorPrice() instanceof Double) {
                    priceStr = String.valueOf((Double) appointment.getDoctorPrice());
                }
            }
            tvDoctorPrice.setText("₹" + priceStr);

            // Appointment Date Detail
            TextView tvAppointmentDateDetail = view.findViewById(R.id.tvAppointmentDateDetail);
            tvAppointmentDateDetail.setText(appointment.getDate() != null ? formatDateForDetail(appointment.getDate()) : "N/A");

            // Appointment Time
            TextView tvAppointmentTime = view.findViewById(R.id.tvAppointmentTime);
            tvAppointmentTime.setText(appointment.getTime() != null ? appointment.getTime() : "N/A");

            // Payment Method
            TextView tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
            tvPaymentMethod.setText(appointment.getPaymentMethod() != null ? appointment.getPaymentMethod() : "N/A");

            // Cancel Button (only for upcoming appointments)
            Button btnCancel = view.findViewById(R.id.btnCancelAppointment);
            if (appointment.getDate() != null && isUpcomingAppointment(appointment.getDate(), appointment.getTime())) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> showCancelConfirmationDialog(appointment));
            } else {
                btnCancel.setVisibility(View.GONE);
            }

            return view;
        } catch (Exception e) {
            System.out.println("DEBUG: Error creating appointment view: " + e.getMessage());
            e.printStackTrace();
            // Return a simple error view
            TextView errorView = new TextView(this);
            errorView.setText("Error loading appointment");
            errorView.setPadding(16, 16, 16, 16);
            return errorView;
        }
    }

    private String formatDateForHeader(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private String formatDateForDetail(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private boolean isUpcomingAppointment(String dateStr, String timeStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        
        try {
            // Parse the appointment date
            Date appointmentDate = parseAppointmentDate(dateStr);
            if (appointmentDate == null) {
                System.out.println("DEBUG: Could not parse appointment date: " + dateStr);
                return false;
            }
            
            // Parse the appointment time
            int appointmentHour = 0;
            int appointmentMinute = 0;
            if (timeStr != null && !timeStr.isEmpty()) {
                int[] timeParts = parseAppointmentTime(timeStr);
                appointmentHour = timeParts[0];
                appointmentMinute = timeParts[1];
            }
            
            // Set the appointment date and time
            Calendar appointmentCal = Calendar.getInstance();
            appointmentCal.setTime(appointmentDate);
            appointmentCal.set(Calendar.HOUR_OF_DAY, appointmentHour);
            appointmentCal.set(Calendar.MINUTE, appointmentMinute);
            appointmentCal.set(Calendar.SECOND, 0);
            appointmentCal.set(Calendar.MILLISECOND, 0);
            
            // Get current date and time
            Calendar currentCal = Calendar.getInstance();
            
            // Compare appointment datetime with current datetime
            boolean isUpcoming = appointmentCal.getTime().after(currentCal.getTime());
            
            System.out.println("DEBUG: Appointment comparison - Date: " + dateStr + ", Time: " + timeStr + 
                             ", Appointment: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(appointmentCal.getTime()) + 
                             ", Current: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(currentCal.getTime()) + 
                             ", IsUpcoming: " + isUpcoming);
            
            return isUpcoming;
        } catch (Exception e) {
            System.out.println("DEBUG: Error in isUpcomingAppointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private Date parseAppointmentDate(String dateStr) {
            SimpleDateFormat[] formats = {
                new SimpleDateFormat("EEE dd MMM", Locale.getDefault()), // For "Thu 16 Oct" format
                new SimpleDateFormat("EEE dd MMM yyyy", Locale.getDefault()), // For "Thu 16 Oct 2024" format
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
                new SimpleDateFormat("MMM dd", Locale.getDefault()), // For "Oct 12" format
                new SimpleDateFormat("dd MMM", Locale.getDefault()), // For "12 Oct" format
                new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            };
            
            for (SimpleDateFormat sdf : formats) {
                try {
                Date date = sdf.parse(dateStr);
                    // If it's a date without year (like "Oct 12" or "Thu 16 Oct"), assume current year
                    if (sdf.toPattern().contains("MMM") && !sdf.toPattern().contains("yyyy")) {
                        Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                        Calendar currentCal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, currentCal.get(Calendar.YEAR));
                    date = cal.getTime();
                    }
                return date;
                } catch (ParseException ignored) {
                    // Try next format
                }
            }
        return null;
    }
    
    private int[] parseAppointmentTime(String timeStr) {
        int[] timeParts = {0, 0}; // [hour, minute]
        
        try {
            // Handle formats like "09:00 AM", "9:00 AM", "21:00", "9:00"
            timeStr = timeStr.trim().toUpperCase();
            
            boolean isPM = timeStr.contains("PM");
            boolean isAM = timeStr.contains("AM");
            
            // Remove AM/PM from the string
            String timeOnly = timeStr.replaceAll("\\s*(AM|PM)\\s*", "");
            
            // Split by colon
            String[] parts = timeOnly.split(":");
            if (parts.length >= 2) {
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                
                // Convert to 24-hour format
                if (isPM && hour != 12) {
                    hour += 12;
                } else if (isAM && hour == 12) {
                    hour = 0;
                }
                
                timeParts[0] = hour;
                timeParts[1] = minute;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Error parsing time: " + timeStr + " - " + e.getMessage());
        }
        
        return timeParts;
    }

    private void showCancelConfirmationDialog(AppointmentData appointment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Cancel Appointment");
        builder.setMessage("Are you sure you want to cancel this appointment?\n\n" +
                "Doctor: " + appointment.getDoctorName() + "\n" +
                "Date: " + formatDateForDetail(appointment.getDate()) + "\n" +
                "Time: " + appointment.getTime() + "\n\n" +
                "🚨 IMPORTANT NOTICE:\n" +
                "• Payment made will NOT be refunded\n" +
                "• This action cannot be undone\n" +
                "• You will need to book a new appointment");
        
        builder.setPositiveButton("CANCEL APPOINTMENT", (dialog, which) -> {
            cancelAppointment(appointment);
        });
        
        builder.setNegativeButton("Keep Appointment", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Make the positive button red to indicate danger
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });
    }

    private void cancelAppointment(AppointmentData appointment) {
        // Find the appointment in the database and remove it
        appointmentsRef.orderByChild("userId").equalTo(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            AppointmentData dbAppointment = dataSnapshot.getValue(AppointmentData.class);
                            if (dbAppointment != null && 
                                dbAppointment.getAppointmentId().equals(appointment.getAppointmentId())) {
                                
                                dataSnapshot.getRef().removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(AppointmentHistoryActivity.this, 
                                                    "Appointment cancelled successfully", 
                                                    Toast.LENGTH_SHORT).show();
                                            
                                            // Show appointment cancellation notification
                                            NotificationManager.showAppointmentCancellation(
                                                    AppointmentHistoryActivity.this,
                                                    appointment.getDoctorName(),
                                                    appointment.getDoctorCategory(),
                                                    appointment.getDate(),
                                                    appointment.getTime()
                                            );

                                            // Cancel scheduled reminder
                                            AppointmentReminderScheduler.cancelAppointmentReminder(
                                                    AppointmentHistoryActivity.this,
                                                    appointment.getDoctorName()
                                            );
                                            
                                            loadAppointmentHistory(); // Refresh the list
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(AppointmentHistoryActivity.this, 
                                                    "Failed to cancel appointment: " + e.getMessage(), 
                                                    Toast.LENGTH_LONG).show();
                                        });
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AppointmentHistoryActivity.this, 
                                "Failed to cancel appointment: " + error.getMessage(), 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showClearAllConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🗑️ Clear All Appointments");
        builder.setMessage("Are you sure you want to clear all appointments from your history?\n\n" +
                "⚠️ IMPORTANT NOTICE:\n" +
                "• This will only clear the display (frontend only)\n" +
                "• Your appointments will still exist in the database\n" +
                "• You can refresh to see them again\n" +
                "• This action can be undone by refreshing");
        
        builder.setPositiveButton("CLEAR DISPLAY", (dialog, which) -> {
            clearAllAppointmentsFromDisplay();
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Make the positive button orange to indicate warning
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        });
    }

    private void clearAllAppointmentsFromDisplay() {
        // Clear the appointment list
        appointmentList.clear();
        
        // Update the display
        displayAppointments();
        
        // Show success message
        Toast.makeText(this, "All appointments cleared from display", Toast.LENGTH_SHORT).show();
        
        // Hide the clear all button since there are no appointments to clear
        btnClearAll.setVisibility(View.GONE);
    }

    // Appointment Data class
    public static class AppointmentData {
        private String appointmentId;
        private String userId;
        private String userEmail;
        private String doctorId;
        private String doctorName;
        private String doctorCategory;
        private Object doctorRating; // Changed to Object to handle both String and Long
        private String doctorImage;
        private Object doctorPrice; // Changed to Object to handle both String and Long
        private String date;
        private String time;
        private String paymentMethod;
        private String status;

        public AppointmentData() {}

        public AppointmentData(String appointmentId, String userId, String userEmail, String doctorId, String doctorName, String doctorCategory,
                             Object doctorRating, String doctorImage, Object doctorPrice,
                             String date, String time, String paymentMethod, String status) {
            this.appointmentId = appointmentId;
            this.userId = userId;
            this.userEmail = userEmail;
            this.doctorId = doctorId;
            this.doctorName = doctorName;
            this.doctorCategory = doctorCategory;
            this.doctorRating = doctorRating;
            this.doctorImage = doctorImage;
            this.doctorPrice = doctorPrice;
            this.date = date;
            this.time = time;
            this.paymentMethod = paymentMethod;
            this.status = status;
        }

        // Getters and Setters
        public String getAppointmentId() { return appointmentId; }
        public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public String getDoctorId() { return doctorId; }
        public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

        public String getDoctorName() { return doctorName; }
        public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

        public String getDoctorCategory() { return doctorCategory; }
        public void setDoctorCategory(String doctorCategory) { this.doctorCategory = doctorCategory; }

        public Object getDoctorRating() { return doctorRating; }
        public void setDoctorRating(Object doctorRating) { this.doctorRating = doctorRating; }

        public String getDoctorImage() { return doctorImage; }
        public void setDoctorImage(String doctorImage) { this.doctorImage = doctorImage; }

        public Object getDoctorPrice() { return doctorPrice; }
        public void setDoctorPrice(Object doctorPrice) { this.doctorPrice = doctorPrice; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
