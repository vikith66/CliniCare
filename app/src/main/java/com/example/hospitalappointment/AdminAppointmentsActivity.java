package com.example.hospitalappointment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminAppointmentsActivity extends AppCompatActivity {

    private ImageView ivBack, ivSearch;
    private EditText etSearch;
    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private LinearLayout emptyState;

    private AdminAppointmentAdapter appointmentAdapter;
    private List<AdminAppointmentData> allAppointments;
    private List<AdminAppointmentData> filteredAppointments;

    private DatabaseReference appointmentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_appointments);

        // Initialize Firebase
        appointmentsRef = FirebaseDatabase.getInstance().getReference("Appointments");

        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadAllAppointments();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                Math.max(systemBars.left, 0),
                Math.max(systemBars.top, 0),
                Math.max(systemBars.right, 0),
                Math.max(systemBars.bottom, 0)
            );
            return insets;
        });
    }

    private void initializeViews() {
        ivBack = findViewById(R.id.ivBack);
        ivSearch = findViewById(R.id.ivSearch);
        etSearch = findViewById(R.id.etSearch);
        rvAppointments = findViewById(R.id.rvAppointments);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);

        allAppointments = new ArrayList<>();
        filteredAppointments = new ArrayList<>();
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivSearch.setOnClickListener(v -> performSearch());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });


    }

    private void setupRecyclerView() {
        appointmentAdapter = new AdminAppointmentAdapter(this, filteredAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setAdapter(appointmentAdapter);
    }

    private void loadAllAppointments() {
        showLoading(true);
        
        appointmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allAppointments.clear();
                
                for (DataSnapshot appointmentSnapshot : snapshot.getChildren()) {
                    AdminAppointmentData appointment = appointmentSnapshot.getValue(AdminAppointmentData.class);
                    if (appointment != null) {
                        appointment.setAppointmentId(appointmentSnapshot.getKey());
                        allAppointments.add(appointment);
                    }
                }
                
                // Sort by date and time (newest first)
                allAppointments.sort((a, b) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        Date dateA = sdf.parse(a.getDate() + " " + a.getTime());
                        Date dateB = sdf.parse(b.getDate() + " " + b.getTime());
                        return dateB.compareTo(dateA);
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                
                // Show all appointments without filtering
                filteredAppointments.clear();
                filteredAppointments.addAll(allAppointments);
                appointmentAdapter.notifyDataSetChanged();
                updateEmptyState();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(AdminAppointmentsActivity.this, 
                    "Failed to load appointments: " + error.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });
    }



    private void performSearch() {
        String searchQuery = etSearch.getText().toString().trim().toLowerCase();
        
        if (TextUtils.isEmpty(searchQuery)) {
            // Show all appointments if search is empty
            filteredAppointments.clear();
            filteredAppointments.addAll(allAppointments);
            appointmentAdapter.notifyDataSetChanged();
            updateEmptyState();
            return;
        }
        
        filteredAppointments.clear();
        
        for (AdminAppointmentData appointment : allAppointments) {
            // Check if matches search query
            boolean matchesSearch = 
                (appointment.getUserEmail() != null && appointment.getUserEmail().toLowerCase().contains(searchQuery)) ||
                (appointment.getDoctorName() != null && appointment.getDoctorName().toLowerCase().contains(searchQuery)) ||
                (appointment.getDate() != null && appointment.getDate().toLowerCase().contains(searchQuery)) ||
                (appointment.getTime() != null && appointment.getTime().toLowerCase().contains(searchQuery)) ||
                (appointment.getDoctorCategory() != null && appointment.getDoctorCategory().toLowerCase().contains(searchQuery)) ||
                (appointment.getPaymentMethod() != null && appointment.getPaymentMethod().toLowerCase().contains(searchQuery));
            
            if (matchesSearch) {
                filteredAppointments.add(appointment);
            }
        }
        
        appointmentAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredAppointments.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            rvAppointments.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            rvAppointments.setVisibility(View.VISIBLE);
        }
    }
}
