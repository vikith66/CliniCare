package com.example.hospitalappointment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookAppointment extends AppCompatActivity {

    private ImageView ivDoctorImage, ivBack;
    private TextView tvDoctorName, tvDoctorCategory, tvDoctorRating, tvAboutParagraph;
    private Button btnConfirm;
    private RecyclerView rvDates, rvTimes;

    private String doctorId, doctorName, doctorCategory, doctorImageBase64, doctorAbout, doctorAvailability;
    private double doctorRating;

    private DateAdapter dateAdapter;
    private TimeAdapter timeAdapter;
    private List<DateItem> dateList;
    private List<String> timeList;

    private String selectedDate = "";
    private String selectedTime = "";
    private String doctorPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_appointment);

        // --- Get doctor details from Intent ---
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorCategory = getIntent().getStringExtra("doctorCategory");
        doctorRating = getIntent().getDoubleExtra("doctorRating", 0.0);
        doctorImageBase64 = getIntent().getStringExtra("doctorImage");
        doctorAbout = getIntent().getStringExtra("doctorAbout");
        doctorAvailability = getIntent().getStringExtra("doctorAvailability");
        doctorPrice = getIntent().getStringExtra("doctorPrice");

        // --- Fetch doctor fees from Firebase if not passed ---
        if (doctorPrice == null || doctorPrice.isEmpty()) {
            // Initialize secondary Firebase app for doctor details
            FirebaseApp secondaryApp;
            try {
                secondaryApp = FirebaseApp.getInstance("doctorApp");
            } catch (IllegalStateException e) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setDatabaseUrl("https://doctordetails-712c2-default-rtdb.firebaseio.com/")
                        .setApiKey("AIzaSyB6m5C02wyjd9KELdmPfrd2WH2bx8TesBE")
                        .setApplicationId("1:446332953537:android:01dbd5ff6420f19088c540")
                        .build();
                secondaryApp = FirebaseApp.initializeApp(this, options, "doctorApp");
            }

            DatabaseReference doctorRef = FirebaseDatabase.getInstance(secondaryApp)
                    .getReference("DoctorDetails")
                    .child(doctorId);

            doctorRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    doctorPrice = snapshot.child("fees").getValue(String.class);
                    if (doctorPrice != null && !doctorPrice.isEmpty()) {
                        Toast.makeText(this, "Fees Loaded: ₹" + doctorPrice, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No fees data found for this doctor", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Doctor not found in database", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Error loading fees: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }


        // --- Initialize Views ---
        ivDoctorImage = findViewById(R.id.ivDoctorDetailImage);
        ivBack = findViewById(R.id.ivDoctorBack);
        tvDoctorName = findViewById(R.id.tvDoctorDetailName);
        tvDoctorCategory = findViewById(R.id.tvDoctorDetailCategory);
        tvDoctorRating = findViewById(R.id.tvDoctorDetailRating);
        tvAboutParagraph = findViewById(R.id.tvAboutParagraph);
        btnConfirm = findViewById(R.id.btnConfirmAppointment);
        rvDates = findViewById(R.id.rvDates);
        rvTimes = findViewById(R.id.rvTimes);

        // --- Set doctor details ---
        tvDoctorName.setText(doctorName != null ? doctorName : "N/A");
        tvDoctorCategory.setText(doctorCategory != null ? doctorCategory : "N/A");
        tvDoctorRating.setText("★ " + doctorRating);
        tvAboutParagraph.setText(doctorAbout != null && !doctorAbout.isEmpty() ? doctorAbout : "No information available.");

        // --- Decode doctor image ---
        if (doctorImageBase64 != null && !doctorImageBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(doctorImageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivDoctorImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                ivDoctorImage.setImageResource(R.drawable.adddoctorbtn);
            }
        } else {
            ivDoctorImage.setImageResource(R.drawable.adddoctorbtn);
        }

        // --- Back button ---
        ivBack.setOnClickListener(v -> finish());

        // --- Disable booking if doctor not available ---
        boolean isAvailable = !"Not Available".equalsIgnoreCase(doctorAvailability);
        btnConfirm.setEnabled(isAvailable);
        btnConfirm.setAlpha(isAvailable ? 1f : 0.5f);

        // --- Setup 7-day date selector ---
        dateList = generateNext7Days();
        dateAdapter = new DateAdapter(this, dateList, date -> {
            if (!isAvailable) {
                Toast.makeText(this, "Doctor is not available", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedDate = date;
        });
        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);

        // --- Setup time slots ---
        timeList = generateTimeSlots();
        timeAdapter = new TimeAdapter(this, timeList, time -> {
            if (!isAvailable) {
                Toast.makeText(this, "Doctor is not available", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedTime = time;
        });
        rvTimes.setLayoutManager(new GridLayoutManager(this, 3));
        rvTimes.setAdapter(timeAdapter);

        // --- Confirm button click ---
        btnConfirm.setOnClickListener(v -> {
            if (!isAvailable) {
                Toast.makeText(this, "Doctor is not available", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Please select a date and time.", Toast.LENGTH_SHORT).show();
                return;
            }
            checkSlotAndProceed();
        });
    }

    // --- Check if slot is already booked ---
    private void checkSlotAndProceed() {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance()
                .getReference("Appointments");

        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean slotTaken = false;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String bookedDoctorId = ds.child("doctorId").getValue(String.class);
                    String bookedDoctorName = ds.child("doctorName").getValue(String.class);
                    String bookedDoctorCategory = ds.child("doctorCategory").getValue(String.class);
                    String bookedDate = ds.child("date").getValue(String.class);
                    String bookedTime = ds.child("time").getValue(String.class);

                    if (doctorId.equals(bookedDoctorId)
                            && doctorName.equals(bookedDoctorName)
                            && doctorCategory.equals(bookedDoctorCategory)
                            && selectedDate.equals(bookedDate)
                            && selectedTime.equals(bookedTime)) {
                        slotTaken = true;
                        break;
                    }
                }

                if (slotTaken) {
                    Toast.makeText(BookAppointment.this,
                            "This slot is already booked for " + doctorName,
                            Toast.LENGTH_LONG).show();
                } else {
                    // Proceed to PaymentActivity
                    Intent intent = new Intent(BookAppointment.this, PaymentActivity.class);
                    intent.putExtra("doctorId", doctorId);
                    intent.putExtra("doctorName", doctorName);
                    intent.putExtra("doctorCategory", doctorCategory);
                    intent.putExtra("doctorRating", doctorRating);
                    intent.putExtra("doctorDate", selectedDate);
                    intent.putExtra("doctorTime", selectedTime);
                    intent.putExtra("doctorImage", doctorImageBase64);
                    intent.putExtra("doctorPrice", doctorPrice);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookAppointment.this,
                        "Error checking slot: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Generate next 7 days ---
    private List<DateItem> generateNext7Days() {
        List<DateItem> list = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1); // Start from tomorrow

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            list.add(new DateItem(dayFormat.format(calendar.getTime()), dateFormat.format(calendar.getTime())));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return list;
    }

    // --- Generate sample time slots ---
    private List<String> generateTimeSlots() {
        List<String> slots = new ArrayList<>();
        slots.add("09:00 AM"); slots.add("10:00 AM"); slots.add("11:00 AM");
        slots.add("12:00 PM"); slots.add("01:00 PM"); slots.add("02:00 PM");
        slots.add("03:00 PM"); slots.add("04:00 PM"); slots.add("05:00 PM");
        return slots;
    }
}
